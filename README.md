# Student Management Portal

A comprehensive **Spring Boot-based web application** for managing student records, fees, and transactions. The application provides both admin and student interfaces for efficient educational institution management with containerized deployment support.

## 📋 Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Local Development Setup](#local-development-setup)
- [Docker Deployment](#docker-deployment)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Project Overview

The **Student Management Portal** is a full-stack web application designed for educational institutions to manage student data, track fee payments, and maintain transaction history. It features role-based access control with separate dashboards for administrators and students.

### Key Capabilities:
- **Student Management**: Add, view, and manage student records
- **Fee Management**: Track and process student fee payments
- **Transaction Tracking**: Maintain detailed payment history
- **User Authentication**: Secure login with role-based authorization
- **Admin Dashboard**: Comprehensive overview and student management
- **Student Dashboard**: Personal information and fee payment tracking
- **Responsive UI**: Modern web interface with CSS styling and interactive tables

---

## ✨ Features

### Admin Features
- Dashboard overview with key metrics
- Manage student database (CRUD operations)
- View all student records and payment status
- Process and track fee payments
- Generate transaction reports
- Manage user accounts and permissions

### Student Features
- Login portal with secure authentication
- Personal information dashboard
- View payment history and transactions
- Track fee status
- Manage profile information

### Technical Features
- **RESTful API** endpoints for backend operations
- **Thymeleaf templating** for dynamic HTML generation
- **Spring Security** for authentication and authorization
- **Hibernate JPA** for object-relational mapping
- **PostgreSQL** database with automatic schema management
- **Swagger/OpenAPI** documentation for API endpoints
- **Docker & Docker Compose** for containerized deployment
- **Remote debugging** support via JDWP
- **Reverse proxy** configuration with Caddy

---

## 🛠 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 3.5.3 |
| **Language** | Java | 17 |
| **Build Tool** | Maven | 3.8+ |
| **Database** | PostgreSQL | 15 |
| **ORM** | Hibernate JPA | 6.x |
| **Template Engine** | Thymeleaf | 3.x |
| **Security** | Spring Security | 6.x |
| **API Documentation** | SpringDoc OpenAPI | 2.5.0 |
| **Code Generation** | Lombok | 1.18.32 |
| **Containerization** | Docker | Latest |
| **Orchestration** | Docker Compose | 3.8 |
| **Web Server (Reverse Proxy)** | Caddy | Latest |
| **Runtime** | Eclipse Temurin JDK | 17 |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Client Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Admin Portal │  │ Student Portal│  │ Login Portal │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
              ↓ HTTP/HTTPS via Caddy ↓
┌─────────────────────────────────────────────────────────────┐
│              Reverse Proxy Layer (Caddy)                    │
│         Routing: edumanage.localhost → localhost:8080             │
└─────────────────────────────────────────────────────────────┘
              ↓ HTTP ↓
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Application                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│  │Controllers│  │ Services │  │Repositories│               │
│  └──────────┘  └──────────┘  └──────────┘                 │
│  - Authentication & Authorization (Spring Security)        │
│  - RESTful API Endpoints                                    │
│  - Thymeleaf Template Rendering                            │
└─────────────────────────────────────────────────────────────┘
              ↓ JDBC ↓
┌─────────────────────────────────────────────────────────────┐
│            PostgreSQL Database                              │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │ student     │  │ transactions│  │ user_credential   │   │
│  └─────────────┘  └─────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Prerequisites

### System Requirements
- **Operating System**: macOS, Linux, or Windows (with WSL2)
- **CPU**: 2+ cores recommended
- **RAM**: 4GB minimum (8GB recommended)
- **Disk Space**: 5GB minimum

### Software Requirements

#### For Local Development
- **Java**: JDK 17 or higher
  ```bash
  java -version  # Should show Java 17+
  ```
- **Maven**: 3.8.0 or higher
  ```bash
  mvn -version  # Should show Maven 3.8.0+
  ```
- **PostgreSQL**: 12 or higher (if running locally)
  ```bash
  psql --version
  ```

#### For Docker Deployment
- **Docker**: 20.10+ or higher
  ```bash
  docker --version
  ```
- **Docker Compose**: 2.0+ or higher
  ```bash
  docker compose version
  ```

---

## 🚀 Local Development Setup

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd student-management-portal
```

### Step 2: Install Dependencies

```bash
mvn clean install
```

This will:
- Download all required Maven dependencies
- Compile Java source code
- Run test cases
- Package the application

### Step 3: Start PostgreSQL Database

**Option A: Using Docker (Recommended)**
```bash
docker run --name postgres-smp \
  -e POSTGRES_DB=feesDb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=root \
  -p 5432:5432 \
  -d postgres:15
```

**Option B: Using Local PostgreSQL Installation**
```bash
# macOS with Homebrew
brew services start postgresql@15

# Create database
createdb -U postgres feesDb

# Create user
psql -U postgres -c "CREATE USER postgres WITH PASSWORD 'root';"
```

### Step 4: Configure Application Properties

The `application.yaml` is pre-configured for local development:

```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/feesDb
  username: postgres
  password: root
```

To override these settings:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/feesDb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=root
```

### Step 5: Run the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:
```bash
mvn clean package
java -jar target/student-management-portal-0.0.1-SNAPSHOT.jar
```

### Step 6: Access the Application

- **Web Application**: http://localhost:8080
- **Login Page**: http://localhost:8080/login
- **API Documentation**: http://localhost:8080/swagger-ui.html

---

## 🐳 Docker Deployment

### Prerequisites for Docker
- Docker installed and running
- Docker Compose installed
- At least 4GB free disk space

### Automated Deployment (Recommended)

The project includes a convenient startup script:

```bash
chmod +x startApp.sh
./startApp.sh
```

This script automatically:
1. Runs Maven clean build
2. Skips tests for faster deployment
3. Builds Docker images
4. Starts all services using Docker Compose

### Manual Docker Deployment

#### Step 1: Build the Docker Image

```bash
# Clean build with Maven
mvn clean package -DskipTests

# Build Docker image
docker build -t student-management-backend:latest .
```

#### Step 2: Start Services with Docker Compose

```bash
docker compose up --build -d
```

**What this does:**
- Builds the Spring Boot application Docker image
- Starts PostgreSQL container (postgres:15)
- Starts the Spring Boot application container
- Creates a persistent volume for database data
- Establishes network connectivity between services

#### Step 3: Verify Deployment

```bash
# Check running containers
docker ps

# Check logs
docker compose logs -f

# Check specific service logs
docker compose logs -f feepay
docker compose logs -f postgres
```

### Docker Compose Configuration Details

The `docker-compose.yml` provides two services:

#### PostgreSQL Service
- **Image**: postgres:15
- **Container Name**: postgres
- **Database**: feesDb
- **User**: postgres
- **Password**: root
- **Port**: 5433 (host) → 5432 (container)
- **Volume**: postgres-data (persistent)
- **Restart Policy**: always

#### Spring Boot Application Service
- **Image**: student-management-backend:latest
- **Port**: 8080 (web), 5005 (debug)
- **Environment**:
  - Database URL: jdbc:postgresql://postgres:5432/feesDb
  - Username: postgres
  - Password: root
  - Profile: dev
- **Debug**: Enabled on port 5005 for remote debugging
- **Dependency**: Waits for PostgreSQL service

### Accessing the Application

| Service | URL                                   | Notes |
|---------|---------------------------------------|-------|
| **Web Application** | http://localhost:8080                 | Main application |
| **Reverse Proxy (Caddy)** | https://edumanage.localhost           | If Caddy is configured |
| **PostgreSQL** | localhost:5433                        | Database connection |
| **Debug Port** | localhost:5005                        | For remote debugging |
| **API Docs** | http://localhost:8080/swagger-ui.html | OpenAPI documentation |

### Database Initialization

On first startup, Hibernate will:
1. Connect to PostgreSQL
2. Auto-create database schema using DDL
3. Create required tables:
   - `student`
   - `transaction`
   - `user_credential`
   - `student_roll_counter`

### Stopping Services

```bash
# Stop all services (containers keep running)
docker compose stop

# Stop and remove containers
docker compose down

# Stop, remove containers, and delete volumes (WARNING: deletes database data)
docker compose down -v

# Stop specific service
docker compose stop feepay
docker compose stop postgres
```

### Viewing Logs

```bash
# Stream logs from all services
docker compose logs -f

# Stream logs from specific service
docker compose logs -f feepay

# View logs for last 100 lines
docker compose logs --tail 100

# Timestamp logs
docker compose logs -t
```

### Environment Variables

Customize deployment by setting environment variables in `docker-compose.yml`:

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/feesDb
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: root
  SPRING_PROFILES_ACTIVE: dev
  JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

Or use `.env` file:

```bash
# Create .env file
cat > .env << EOF
POSTGRES_DB=feesDb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=root
SPRING_PROFILES_ACTIVE=dev
EOF

# Then use in docker-compose.yml:
# environment:
#   POSTGRES_DB: ${POSTGRES_DB}
```

### Remote Debugging

With Docker, you can debug the application remotely:

```bash
# In your IDE (IntelliJ IDEA):
# 1. Go to Run → Edit Configurations
# 2. Add new Remote JVM Debug configuration
# 3. Set Host: localhost, Port: 5005
# 4. Click Debug button
```

The debug port is already exposed in `docker-compose.yml`:
```yaml
ports:
  - "8080:8080"
  - "5005:5005"
environment:
  JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

---

## 📁 Project Structure

```
student-management-portal/
├── src/
│   ├── main/
│   │   ├── java/com/ija/student_management_portal/
│   │   │   ├── StudentManagementPortalApplication.java     # Main Spring Boot application
│   │   │   ├── controller/
│   │   │   │   ├── AdminHomeController.java               # Admin dashboard endpoints
│   │   │   │   ├── StudentHomeController.java             # Student dashboard endpoints
│   │   │   │   ├── LoginController.java                   # Authentication endpoints
│   │   │   │   └── RootController.java                    # Root page routing
│   │   │   ├── entity/
│   │   │   │   ├── Student.java                           # Student entity/model
│   │   │   │   ├── Transaction.java                       # Transaction entity
│   │   │   │   ├── UserCredential.java                    # User authentication entity
│   │   │   │   └── StudentRollCounter.java               # Roll number generator
│   │   │   ├── repository/                                # Data access layer (JPA)
│   │   │   ├── service/                                   # Business logic layer
│   │   │   ├── dto/                                       # Data transfer objects
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java                    # Spring Security configuration
│   │   │   └── util/                                      # Utility classes
│   │   └── resources/
│   │       ├── application.yaml                           # Application configuration
│   │       ├── application.properties                     # Alternative properties file
│   │       ├── templates/
│   │       │   ├── login.html                             # Login page
│   │       │   ├── home.html                              # Home page
│   │       │   ├── admin-home.html                        # Admin dashboard
│   │       │   ├── admin-students.html                    # Admin student list
│   │       │   ├── student-home.html                      # Student dashboard
│   │       │   ├── students.html                          # Student list view
│   │       │   ├── student-details.html                   # Student details page
│   │       │   ├── add_student.html                       # Add student form
│   │       │   └── accept_fee.html                        # Fee payment form
│   │       └── static/
│   │           ├── css/
│   │           │   ├── navbar-sidebar.css                 # Navigation styling
│   │           │   ├── home.css                           # Home page styling
│   │           │   ├── student-details.css                # Student details styling
│   │           │   └── students.css                       # Students list styling
│   │           └── js/
│   │               └── table.js                           # Interactive table functionality
│   └── test/
│       └── java/com/ija/student_management_portal/
│           └── StudentManagementPortalApplicationTests.java
├── Dockerfile                                              # Docker image configuration
├── docker-compose.yml                                      # Multi-container Docker setup
├── Caddyfile                                               # Caddy reverse proxy config
├── pom.xml                                                 # Maven dependencies & build config
├── mvnw & mvnw.cmd                                        # Maven wrapper scripts
├── startApp.sh                                             # Automated startup script
├── target/                                                 # Compiled output (generated)
└── README.md                                               # This file
```

### Key Component Descriptions

| Component | Purpose |
|-----------|---------|
| **Controllers** | Handle HTTP requests and route them to services |
| **Services** | Contain business logic and coordinate operations |
| **Repositories** | Provide database access using Spring Data JPA |
| **Entities** | Represent database tables as Java objects |
| **DTOs** | Transfer data between layers without exposing entities |
| **Config** | Configure Spring Security and other frameworks |
| **Templates** | Thymeleaf HTML templates for dynamic page rendering |
| **Static** | CSS and JavaScript for frontend styling and interactivity |

---

## 📚 API Documentation

### Swagger/OpenAPI Documentation

Once the application is running, access the interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

This provides:
- All available API endpoints
- Request/response schemas
- Parameter descriptions
- Try-it-out functionality

### Common Endpoints

#### Authentication
```
POST   /api/auth/login        - User login
POST   /api/auth/logout       - User logout
POST   /api/auth/register     - User registration
```

#### Admin Operations
```
GET    /api/admin/students    - List all students
POST   /api/admin/students    - Create new student
GET    /api/admin/students/{id}  - Get student by ID
PUT    /api/admin/students/{id}  - Update student
DELETE /api/admin/students/{id}  - Delete student
GET    /api/admin/transactions   - List all transactions
```

#### Student Operations
```
GET    /api/student/profile   - Get current user profile
GET    /api/student/fees      - Get fee information
GET    /api/student/transactions - Get transaction history
POST   /api/student/pay-fee   - Submit fee payment
```

---

## ⚙️ Configuration

### Application Configuration File: `application.yaml`

```yaml
server:
  port: 8080                    # Server port

spring:
  application:
    name: StudentManagementProject

  datasource:
    url: jdbc:postgresql://postgres:5432/feesDb    # Database URL
    username: postgres                              # Database user
    password: root                                  # Database password
    driver-class-name: org.postgresql.Driver       # JDBC driver
    initialization-mode: always                     # Initialize on startup

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: update          # Auto-update schema (create/update/validate/none)
    show-sql: false             # Log SQL queries (set to true for debugging)
```

### Hibernate DDL-Auto Options

| Option | Behavior |
|--------|----------|
| `create` | Drop existing schema and create new (data loss) |
| `create-drop` | Create on startup, drop on shutdown |
| `update` | Update schema without dropping data (recommended for production) |
| `validate` | Validate schema but don't modify |
| `none` | No automatic schema management |

### Environment Variables Override

```bash
# Database configuration
export SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/dbname
export SPRING_DATASOURCE_USERNAME=user
export SPRING_DATASOURCE_PASSWORD=password

# Server configuration
export SERVER_PORT=8080

# JPA/Hibernate configuration
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
export SPRING_JPA_SHOW_SQL=true

# Profile selection
export SPRING_PROFILES_ACTIVE=dev,prod
```

### Security Configuration

Spring Security is configured in `config/SecurityConfig.java`:

- **Default Credentials**: Configure in SecurityConfig
- **Authentication Method**: Form-based login via Thymeleaf templates
- **Authorization**: Role-based access control (ADMIN, STUDENT)
- **Session Management**: Default Spring Security session handling

---

## 🔧 Troubleshooting

### Issue: Connection Refused to PostgreSQL

**Problem**: `Connection refused: connect` error when starting application

**Solutions**:
```bash
# Check if PostgreSQL container is running
docker ps | grep postgres

# If not running, start it
docker compose up -d postgres

# Check logs
docker compose logs postgres

# Verify connection
psql -h localhost -U postgres -d feesDb
```

### Issue: Port Already in Use

**Problem**: `Address already in use` error for port 8080 or 5433

**Solutions**:
```bash
# Find process using port 8080
lsof -i :8080
kill -9 <PID>

# Or change port in docker-compose.yml
# Change "8080:8080" to "8081:8080"

# Restart services
docker compose up -d
```

### Issue: Database Not Initializing

**Problem**: Tables not created or schema mismatch

**Solutions**:
```bash
# View Hibernate logs
export SPRING_JPA_SHOW_SQL=true
mvn spring-boot:run

# Recreate schema (WARNING: deletes data)
# Change ddl-auto to 'create' in application.yaml, restart, then revert

# Manual database reset
docker compose down -v
docker compose up -d
```

### Issue: Out of Memory

**Problem**: `Java.lang.OutOfMemoryError`

**Solutions**:
```bash
# Increase heap size in docker-compose.yml
environment:
  JAVA_TOOL_OPTIONS: -Xmx2g -Xms1g

# Or for local development
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run
```

### Issue: Build Fails with Test Failures

**Problem**: Maven build fails during test execution

**Solutions**:
```bash
# Skip tests
mvn clean package -DskipTests

# Run specific test
mvn test -Dtest=StudentManagementPortalApplicationTests

# Show detailed test output
mvn test -X
```

### Issue: Can't Access Application at http://localhost:8080

**Problem**: Connection refused or application doesn't respond

**Solutions**:
```bash
# Check if container is running
docker ps

# Check application logs
docker compose logs -f feepay

# Restart application
docker compose restart feepay

# Check port mapping
docker port feepay

# Check network connectivity
curl http://localhost:8080
```

### Issue: Remote Debugging Not Working

**Problem**: Can't connect debugger on port 5005

**Solutions**:
```bash
# Verify debug port is exposed
docker compose logs feepay | grep 5005

# Check if port is available
lsof -i :5005

# Ensure JAVA_TOOL_OPTIONS includes debug configuration
# in docker-compose.yml:
# JAVA_TOOL_OPTIONS: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

### Getting Help with Logs

```bash
# Real-time logs
docker compose logs -f

# Last 100 lines
docker compose logs --tail 100

# With timestamps
docker compose logs -t

# Specific service
docker compose logs -f feepay

# Export logs to file
docker compose logs > app.log
```

---

## 📝 Notes

### Database Schema

The application automatically creates the following tables:

- **student**: Stores student information
- **transaction**: Records fee payments and transactions
- **user_credential**: Manages login credentials
- **student_roll_counter**: Generates unique roll numbers

### Security Considerations

⚠️ **For Production Deployment**:

1. Change default credentials:
   - Update PostgreSQL password in `docker-compose.yml`
   - Configure Spring Security with strong credentials

2. Use environment variables for sensitive data:
   ```bash
   export POSTGRES_PASSWORD=$(openssl rand -base64 32)
   export SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD
   ```

3. Enable HTTPS:
   - Configure Caddy with SSL certificates
   - Set up proper reverse proxy configuration

4. Database backups:
   ```bash
   # Backup database
   docker exec postgres pg_dump -U postgres feesDb > backup.sql
   
   # Restore from backup
   docker exec -i postgres psql -U postgres feesDb < backup.sql
   ```

### Performance Optimization

```yaml
# In application.yaml for better performance
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
```

---

## 📄 License

[Add your license information here]

## 👥 Contributors

[Add contributor information here]

## 📞 Support

For issues or questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review logs: `docker compose logs`
3. Check database connectivity
4. Verify all prerequisites are installed

---

**Last Updated**: March 2026  
**Version**: 0.0.1-SNAPSHOT
