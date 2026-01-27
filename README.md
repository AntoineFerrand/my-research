# Incident Search Application

Fullstack application to search and filter incidents in a PostgreSQL database containing 100,000 entries. Built with Spring Boot (backend) and Angular 21 (frontend).

## 📋 Architecture

```
my-research/
├── backend/              # Spring Boot REST API
├── frontend/             # Angular 21 Application
├── scripts-sql/          # Database initialization scripts
│   ├── 01-ddl.sql       # Table schema + indexes
│   ├── 02-data.sql      # Test data (100,000 incidents)
│   └── 03-performance-test.sql  # Performance benchmarking queries
├── test-perf.sh          # Automated performance test script
├── compose.yaml          # Docker Compose configuration
└── README.md            # This file
```

## 🚀 Quick Start

### 1. Prerequisites

- **Docker & Docker Compose**: for PostgreSQL database
- **Java 17+** & **Maven**: for Spring Boot backend
- **Node.js 18.19+** & **npm**: for Angular frontend

### 2. Launch the complete environment

#### a) Start PostgreSQL database

```bash
# From project root
docker compose up -d
```

This will:
- Create a PostgreSQL 17 container
- Initialize the `incidents` database with `person` and `incident` tables
- Insert 100,000 test incidents
- Expose PostgreSQL on `localhost:5432`

**Verify database is ready**:
```bash
docker compose logs database | grep "database system is ready"
```

#### b) Start Spring Boot backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend will be available at `http://localhost:8080`

**Verify backend is running**:
```bash
curl http://localhost:8080/incidents | head -50
```

#### c) Start Angular frontend

```bash
cd frontend
npm install  # First time only
npm start
```

Frontend will be available at `http://localhost:4200`

### 3. Use the application

1. Open browser at `http://localhost:4200`
2. Use search filters:
   - **Title**: partial search in incident title
   - **Description**: partial search in description
   - **Severity**: LOW, MEDIUM or HIGH
   - **Owner**: search in last name, first name or email
3. Click **Search**
4. Observe:
   - Query execution time (in seconds)
   - Number of results found
   - Table with incident details

## 🛠️ Useful Commands

### Database

```bash
# Stop database
docker compose down

# Delete data and reset everything
docker compose down -v
docker compose up -d

# Access psql
docker compose exec database psql -U user -d incidents

# View incident count
docker compose exec database psql -U user -d incidents -c "SELECT COUNT(*) FROM incident;"
```

### Backend

```bash
cd backend

# Compile without running
./mvnw clean package

# Run tests
./mvnw test

# Create executable JAR
./mvnw clean package -DskipTests
java -jar target/*.jar
```

### Frontend

```bash
cd frontend

# Install dependencies
npm install

# Run in development mode
npm start

# Build for production
npm run build

# Lint code
npm run lint
```

## 📊 Database Structure

### Table `person`
| Column       | Type    | Description                    |
|--------------|---------|--------------------------------|
| id           | SERIAL  | Primary key                    |
| last_name    | TEXT    | Last name                      |
| first_name   | TEXT    | First name                     |
| email        | TEXT    | Email (can be null)            |

### Table `incident`
| Column       | Type      | Description                       |
|--------------|-----------|-----------------------------------|
| id           | SERIAL    | Primary key                       |
| title        | TEXT      | Incident title                    |
| description  | TEXT      | Detailed description              |
| severity     | TEXT      | LOW, MEDIUM or HIGH               |
| owner_id     | INTEGER   | Foreign key to `person(id)`       |
| created_at   | TIMESTAMP | Creation date                     |

## ⚡ Performance Optimizations

This section documents each optimization applied to improve search performance on 100,000 incidents.

### Optimization #1: PostgreSQL Trigram Indexes (pg_trgm + GIN)

**Objective**: Optimize partial text searches (`LIKE '%keyword%'`) on textual columns

**Implementation**:
- Enabled PostgreSQL extension `pg_trgm` for trigram similarity
- Created GIN indexes on all text search columns:
  - `incident.title`, `incident.description` → trigram indexes
  - `incident.severity` → B-tree index (exact match)
  - `person.last_name`, `person.first_name`, `person.email` → trigram indexes
- Removed redundant indexes (`idx_person_id`, `idx_incident_id`) since PostgreSQL automatically indexes primary keys

You can check the result of performance tests realised in PERFORMANCE_RESULT.md
with the new script 03-performance-test.sql. This script is mount in the container if you want to run by connecting
inside the postgres image.

### Optimization #2: Add eager loading for GET incidents endpoint

**Problem**:
Relation Incident → Person (owner). By default with Hibernate, when we retrieve 100 incidents:
- 1 request to load all 100 incidents
- 100 additional requests to load each owner individually when you access them
  This is N+1 problem: very inefficient!

**Objective**:
With eager loading, we should load everything at once with a JOIN:
A single query to retrieve all 100 incidents AND their owners

**Implementation**
@EntityGraph(attributePaths = {"owner"})
List<Incident> findAll(Specification<Incident> spec);

### Optimization #3: Multi-level Caching (Application + HTTP)

**Objective**: Reduce database load and network latency for repeated identical searches

**Implementation**:

#### A) Application cache with Spring Cache + Caffeine

**Dependencies** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

**Configuration** (`application.properties`):
```properties
spring.cache.type=caffeine
spring.cache.cache-names=incidents
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=300s
```

**Activation** (`BackendApplication.java`):
```java
@SpringBootApplication
@EnableCaching
public class BackendApplication { ... }
```

**Usage** (`IncidentService.java`):
```java
@Cacheable(value = "incidents", key = "#title + '_' + #description + '_' + #severity + '_' + #owner + '_' + #page + '_' + #size")
public PageResponseDTO<IncidentDTO> searchIncidents(...) {
    // Database query only executed on cache miss
}
```

- Cache key combines all search parameters to ensure uniqueness
- TTL: 5 minutes, max 1000 entries
- High-performance in-memory cache using Caffeine (Java)

#### B) HTTP cache with standard headers

**Implementation** (`IncidentController.java`):
```java
CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
        .cachePrivate()
        .mustRevalidate();

return ResponseEntity.ok()
        .cacheControl(cacheControl)
        .body(incidents);
```

This generates the HTTP header:
```
Cache-Control: private, max-age=300, must-revalidate
```

- `private`: cache is specific to the browser (not shared in proxies)
- `max-age=300`: valid for 5 minutes
- `must-revalidate`: browser must check with server after expiration

**Benefits**:
- 1st search: Database query (786ms)
- 2nd search (same filters): Application cache (73ms)
- 3rd search (same filters): Browser cache (4ms network)

![alt text](cache%20performance%20screen.png)


## 🔌 Backend API

### Main endpoint

**GET** `/incidents`

**Query Parameters** (all optional):
- `title`: LIKE filter on title
- `description`: LIKE filter on description
- `severity`: exact filter on severity
- `owner`: LIKE filter on lastName, firstName OR email of owner

**Example queries**:

```bash
# All incidents
curl "http://localhost:8080/incidents"

# HIGH severity incidents
curl "http://localhost:8080/incidents?severity=HIGH"

# Incidents containing "error" in title
curl "http://localhost:8080/incidents?title=error"

# Incidents for owner "Smith"
curl "http://localhost:8080/incidents?owner=Smith"

# Combined filters
curl "http://localhost:8080/incidents?severity=HIGH&owner=john"
```

## 🧪 Tests

### Backend (JUnit)

```bash
cd backend
./mvnw test
```

Included tests:
- `BackendApplicationTests`: Spring context
- API integration tests with embedded H2 database


## 🐛 Troubleshooting

### Database won't start

```bash
# Check logs
docker compose logs database

# If port 5432 already in use, modify in compose.yaml
ports:
  - "5433:5432"
```

### Backend can't connect to database

Check `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/${POSTGRES_DB:incidents}
spring.datasource.username=${POSTGRES_USER:user}
spring.datasource.password=${POSTGRES_PASSWORD:password}
```

### Frontend doesn't display data

1. Verify backend is accessible:
   ```bash
   curl http://localhost:8080/incidents
   ```

2. Check configuration in `frontend/src/assets/config.js`:
   ```javascript
   window.dynamicConf = {
     BACKEND_URL: 'http://localhost:8080',
     // ...
   };
   ```

3. Check browser console (F12) for CORS errors


## 📝 Code Standards

### Backend (Java/Spring Boot)
- Clean Architecture (Controller → Service → Repository)
- DTOs for API responses
- JPA Specifications for dynamic filters
- Global exception handling

### Frontend (Angular 21)
- Standalone components architecture
- Injectable services for business logic
- TypeScript models for typing
- SCSS for styling


## 📚 Technologies Used

**Backend**:
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL 17
- Maven

**Frontend**:
- Angular 21
- TypeScript 5.7
- RxJS 7.8
- @michelin/theme 9.5.1

**Infrastructure**:
- Docker & Docker Compose
- PostgreSQL 17