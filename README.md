# Incident Search Application

Fullstack application to search and filter incidents in a PostgreSQL database containing 100,000 entries. 
Built with Spring Boot 4 (backend) and Angular 21 (frontend).

## Architecture

```
my-research/
├── backend/                     # Spring Boot 4 REST API
├── frontend/                    # Angular 21 Application
├── scripts-sql/                 # Database initialization scripts
│   ├── 01-ddl.sql               # Table schema + indexes
│   ├── 02-data.sql              # Test data (100,000 incidents)
│   └── 03-performance-test.sql  # Performance benchmarking queries
├── compose.yaml                 # Docker Compose configuration
├── PERFORMANCE_RESULT.yaml      # performance after adding index (without cache)
```

## Quick Start

### 1. Prerequisites

- **Docker & Docker Compose**: for PostgreSQL database
- **Java 17+** & **Maven**: for Spring Boot backend
- **Node.js 18.19+** & **npm**: for Angular 21 frontend

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

## Useful Commands

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

# Compile without running
./mvnw clean package

# Run tests
./mvnw test


### Frontend

```bash

# Install dependencies
npm install

# Run in development mode
npm start

```

## Database Structure

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

## Performance Optimizations

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

**Explanation**:

I use spring-boot-starter-cache with Caffeine as the provider. This gives me the Spring Cache abstraction, which allows me to:
1. Decouple the business logic from the cache provider
2. Easily migrate to Redis if we scale horizontally
3. Benefit from Spring Boot auto-configuration
4. Follow Spring best practices

For this single-instance application, Caffeine is optimal (performance). If we were to deploy in a cluster (Kubernetes, multiple pods), 
we would migrate to Redis by simply changing spring.cache.type=redis without modifying the code.


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

#### B) HTTP cache with standard headers

**Implementation** (`IncidentController.java`):

CacheControl configured for 5minutes.
Very useful with "sort" feature for incident listing in front,
because the sort is instantaneous since the returned lines are the same

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

### Optimization #4: Swagger

**Objective**:
See and test the API very easily
Add documentation to the endpoints

**Implementation**

Add springdoc-openapi-starter-webmvc-ui depedency to pom.xml
Swagger UI : http://localhost:8080/swagger-ui/index.html

## Backend API

### Main endpoint

**GET** `/incidents`
- `title`: LIKE filter on title
- `description`: LIKE filter on description
- `severity`: exact filter on severity
- `owner`: LIKE filter on lastName, firstName OR email of owner

## Tests

### Backend (JUnit)

```bash
./mvnw test
```

Included tests:
- `BackendApplicationTests`: Spring context
- `IncidentControllerTest`: Test Controller with MockMVC

## Technologies Used

**Backend**:
- Spring Boot 4
- Spring Data JPA with Hibernate
- Swagger
- Maven

**Frontend**:
- Angular 21
- TypeScript 5.7
- RxJS 7.8

**Infrastructure**:
- Docker & Docker Compose
- PostgreSQL 17