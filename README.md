# Incident Search Application

Fullstack application to search and filter incidents in a PostgreSQL database containing 100,000 entries. Built with Spring Boot (backend) and Angular 21 (frontend).

## 📋 Architecture

```
my-research/
├── backend/              # Spring Boot REST API
├── frontend/             # Angular 21 Application
├── scripts-sql/          # Database initialization scripts
│   ├── 01-ddl.sql       # Table schema
│   └── 02-data.sql      # Test data (100,000 incidents)
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

**Existing indexes**:
- `idx_person_id` on `person(id)`
- `idx_incident_id` on `incident(id)`

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