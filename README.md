# CVApp Backend

CVApp is a Spring Boot backend application for managing CVs, companies, job postings, and job applications.

The platform supports three main roles:

| Role      | Description                                                                    |
| --------- | ------------------------------------------------------------------------------ |
| `USER`    | Creates and manages a CV, uploads CV files, searches jobs, and applies to jobs |
| `COMPANY` | Manages company profile, searches CVs, creates jobs, and reviews applications  |
| `ADMIN`   | Manages users, companies, jobs, and platform statistics                        |

---

## Features

### Authentication

* Register and login with email/password
* JWT access tokens
* Refresh tokens
* Logout
* Password reset flow
* Google OAuth support
* Company invite registration flow
* Role-based access control

### User Features

* Create, update, view, and delete CV profile
* Manage education entries
* Manage experience entries
* Manage skills
* Upload CV photo
* Upload CV PDF
* Browse active jobs
* Search jobs
* Apply to jobs
* View submitted applications
* Withdraw applications

### Company Features

* View and update company profile
* Upload company photo
* Search CVs by keyword, skill, and location
* View full CV details
* Favorite CVs
* View CV history
* Create, update, view, and delete company jobs
* Review applications for company jobs
* Update application status

### Admin Features

* View all users
* View user details
* Enable or disable users
* Change user roles
* Delete users
* View all companies
* View company details
* Delete companies
* View all jobs
* View job details
* Toggle job active status
* Delete jobs
* View platform statistics

---

## Tech Stack

* Java 25
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* JWT
* Maven
* Docker
* Cloudinary
* Java Mail Sender
* Google OAuth2
* JUnit
* MockMvc

---

## Project Structure

```text
src/main/java/com/best/cvapp
├── admin
│   ├── company
│   ├── job
│   ├── stats
│   └── user
├── auth
│   ├── companyinvite
│   ├── credentials
│   ├── jwt
│   ├── oauth
│   ├── passwordreset
│   └── session
├── company
│   ├── cv
│   ├── favorite
│   ├── history
│   ├── profile
│   └── upload
├── cv
│   ├── education
│   ├── experience
│   ├── profile
│   ├── skill
│   └── upload
├── job
│   ├── application
│   ├── company
│   ├── core
│   ├── listing
│   └── search
├── shared
└── user
```

---

## Requirements

Before running the application, install:

* Java 25
* Docker
* Docker Compose
* Make
* Maven, or use the included Maven wrapper

Check Java version:

```bash
java --version
```

Check Docker:

```bash
docker --version
```

Check Docker Compose:

```bash
docker compose version
```

Check Make:

```bash
make --version
```

---

## Environment Variables

The application requires database, JWT, email, OAuth, and storage configuration.

Example environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cvapp
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

APP_JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters
APP_JWT_EXPIRATION=86400000

SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password

GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

Make sure the variable names match your `application.properties` or `application.yml`.

---

## Database Setup

The application uses PostgreSQL.

Create a database manually:

```sql
CREATE DATABASE cvapp;
```

Example local database configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cvapp
spring.datasource.username=postgres
spring.datasource.password=postgres
```

The easiest way to start the database is with the provided Makefile:

```bash
make db-up
```

---

## Running the Application Locally

Clone the repository:

```bash
git clone https://github.com/your-username/cvapp.git
cd cvapp
```

Start the database:

```bash
make db-up
```

Run the backend:

```bash
make run
```

The backend should start on:

```text
http://localhost:8080
```

---

## Makefile Commands

This project includes a `Makefile` with shortcuts for common development tasks.

### Run the Application

```bash
make run
```

Equivalent command:

```bash
./mvnw spring-boot:run
```

---

### Build the Application

```bash
make build
```

Equivalent command:

```bash
./mvnw clean install -DskipTests
```

---

### Run Tests

```bash
make test
```

Equivalent command:

```bash
./mvnw test
```

---

### Start the Database

```bash
make db-up
```

Equivalent command:

```bash
docker-compose up -d
```

---

### Stop the Database

```bash
make db-down
```

Equivalent command:

```bash
docker-compose down
```

---

### Reset the Database

```bash
make db-reset
```

This stops the database, removes the database volume, and starts it again.

Equivalent command:

```bash
docker-compose down -v && docker-compose up -d
```

---

### Clean Build Files

```bash
make clean
```

Equivalent command:

```bash
./mvnw clean
```

---

## Recommended Development Flow

Start the database:

```bash
make db-up
```

Run the backend:

```bash
make run
```

Run tests:

```bash
make test
```

Reset the database when needed:

```bash
make db-reset
```

Stop the database:

```bash
make db-down
```

---

## Makefile

```makefile
run:
	./mvnw spring-boot:run

build:
	./mvnw clean install -DskipTests

test:
	./mvnw test

db-up:
	docker-compose up -d

db-down:
	docker-compose down

db-reset:
	docker-compose down -v && docker-compose up -d

clean:
	./mvnw clean
```

On Windows, these commands work best through Git Bash, WSL, or another terminal where `make` is available.

---

## Running Without Make

Run the application:

```bash
./mvnw spring-boot:run
```

Build the project:

```bash
./mvnw clean install -DskipTests
```

Run tests:

```bash
./mvnw test
```

Start Docker services:

```bash
docker-compose up -d
```

Stop Docker services:

```bash
docker-compose down
```

Reset Docker services and volumes:

```bash
docker-compose down -v && docker-compose up -d
```

---

## Running Tests

Run all tests:

```bash
make test
```

Run a specific test class:

```bash
./mvnw -Dtest=CredentialsAuthControllerTest test
```

Examples:

```bash
./mvnw -Dtest=AdminUserControllerTest test
./mvnw -Dtest=AdminCompanyControllerTest test
./mvnw -Dtest=AdminJobControllerTest test
./mvnw -Dtest=CredentialsAuthControllerTest test
```

Run tests from IntelliJ:

1. Open the project in IntelliJ IDEA
2. Open the `src/test` folder
3. Right-click the test package or test folder
4. Click `Run Tests`

---

## Docker

Build the Docker image:

```bash
docker build -t cvapp-backend .
```

Run the container:

```bash
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/cvapp \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e APP_JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters \
  -e APP_JWT_EXPIRATION=86400000 \
  cvapp-backend
```

On Linux, `host.docker.internal` may not work by default. In that case, use Docker Compose or a shared Docker network.

---

## Example Docker Compose

Example `docker-compose.yml`:

```yaml
services:
  postgres:
    image: postgres:15
    container_name: cvapp-postgres
    environment:
      POSTGRES_DB: cvapp
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - cvapp-postgres-data:/var/lib/postgresql/data

  backend:
    build: .
    container_name: cvapp-backend
    depends_on:
      - postgres
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/cvapp
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      APP_JWT_SECRET: your-super-secret-jwt-key-at-least-32-characters
      APP_JWT_EXPIRATION: 86400000

volumes:
  cvapp-postgres-data:
```

Start services:

```bash
make db-up
```

Stop services:

```bash
make db-down
```

Reset services and database volume:

```bash
make db-reset
```

---

## API Overview

Base URL:

```text
http://localhost:8080
```

---

## Auth API

| Method | Endpoint                           | Description            | Access |
| ------ | ---------------------------------- | ---------------------- | ------ |
| `POST` | `/api/auth/register`               | Register user          | Public |
| `POST` | `/api/auth/login`                  | Login user             | Public |
| `POST` | `/api/auth/refresh`                | Refresh access token   | Public |
| `POST` | `/api/auth/logout`                 | Logout user            | Public |
| `POST` | `/api/auth/forgot-password`        | Request password reset | Public |
| `POST` | `/api/auth/reset-password`         | Reset password         | Public |
| `POST` | `/api/auth/company-invites`        | Send company invite    | Admin  |
| `POST` | `/api/auth/company-invites/accept` | Accept company invite  | Public |

---

## User CV API

| Method   | Endpoint                       | Description             | Access |
| -------- | ------------------------------ | ----------------------- | ------ |
| `GET`    | `/api/user/cv`                 | Get my CV               | User   |
| `PUT`    | `/api/user/cv`                 | Create or update my CV  | User   |
| `DELETE` | `/api/user/cv`                 | Delete my CV            | User   |
| `GET`    | `/api/user/cv/education`       | Get education entries   | User   |
| `POST`   | `/api/user/cv/education`       | Add education entry     | User   |
| `PUT`    | `/api/user/cv/education/{id}`  | Update education entry  | User   |
| `DELETE` | `/api/user/cv/education/{id}`  | Delete education entry  | User   |
| `GET`    | `/api/user/cv/experience`      | Get experience entries  | User   |
| `POST`   | `/api/user/cv/experience`      | Add experience entry    | User   |
| `PUT`    | `/api/user/cv/experience/{id}` | Update experience entry | User   |
| `DELETE` | `/api/user/cv/experience/{id}` | Delete experience entry | User   |
| `GET`    | `/api/user/cv/skills`          | Get skills              | User   |
| `POST`   | `/api/user/cv/skills`          | Add skill               | User   |
| `PUT`    | `/api/user/cv/skills/{id}`     | Update skill            | User   |
| `DELETE` | `/api/user/cv/skills/{id}`     | Delete skill            | User   |
| `POST`   | `/api/user/cv/photo`           | Upload CV photo         | User   |
| `DELETE` | `/api/user/cv/photo`           | Delete CV photo         | User   |
| `POST`   | `/api/user/cv/pdf`             | Upload CV PDF           | User   |
| `DELETE` | `/api/user/cv/pdf`             | Delete CV PDF           | User   |

---

## Job API

| Method   | Endpoint                                    | Description          | Access |
| -------- | ------------------------------------------- | -------------------- | ------ |
| `GET`    | `/api/jobs`                                 | Get all active jobs  | User   |
| `GET`    | `/api/jobs/{id}`                            | Get active job by ID | User   |
| `GET`    | `/api/jobs/search`                          | Search jobs          | User   |
| `POST`   | `/api/user/applications/jobs/{jobId}/apply` | Apply to job         | User   |
| `GET`    | `/api/user/applications`                    | Get my applications  | User   |
| `DELETE` | `/api/user/applications/{applicationId}`    | Withdraw application | User   |

---

## Company API

| Method   | Endpoint                                                | Description               | Access  |
| -------- | ------------------------------------------------------- | ------------------------- | ------- |
| `GET`    | `/api/company/me`                                       | Get company profile       | Company |
| `PUT`    | `/api/company/me`                                       | Update company profile    | Company |
| `POST`   | `/api/company/photo`                                    | Upload company photo      | Company |
| `DELETE` | `/api/company/photo`                                    | Delete company photo      | Company |
| `GET`    | `/api/company/cvs`                                      | Get all CV summaries      | Company |
| `GET`    | `/api/company/cvs/{id}`                                 | Get CV details            | Company |
| `GET`    | `/api/company/cvs/search`                               | Search CVs                | Company |
| `POST`   | `/api/company/cvs/{id}/favorite`                        | Add CV to favorites       | Company |
| `DELETE` | `/api/company/cvs/{id}/favorite`                        | Remove CV from favorites  | Company |
| `GET`    | `/api/company/favorites`                                | Get favorite CVs          | Company |
| `GET`    | `/api/company/history`                                  | Get CV view history       | Company |
| `POST`   | `/api/company/jobs`                                     | Create job                | Company |
| `GET`    | `/api/company/jobs`                                     | Get my jobs               | Company |
| `GET`    | `/api/company/jobs/{id}`                                | Get my job by ID          | Company |
| `PUT`    | `/api/company/jobs/{id}`                                | Update job                | Company |
| `DELETE` | `/api/company/jobs/{id}`                                | Delete job                | Company |
| `GET`    | `/api/company/jobs/{jobId}/applications`                | Get applications for job  | Company |
| `PATCH`  | `/api/company/jobs/applications/{applicationId}/status` | Update application status | Company |

---

## Admin API

| Method   | Endpoint                       | Description              | Access |
| -------- | ------------------------------ | ------------------------ | ------ |
| `GET`    | `/api/admin/users`             | Get all users            | Admin  |
| `GET`    | `/api/admin/users/{id}`        | Get user by ID           | Admin  |
| `PATCH`  | `/api/admin/users/{id}/toggle` | Enable or disable user   | Admin  |
| `PATCH`  | `/api/admin/users/{id}/role`   | Change user role         | Admin  |
| `DELETE` | `/api/admin/users/{id}`        | Delete user              | Admin  |
| `GET`    | `/api/admin/companies`         | Get all companies        | Admin  |
| `GET`    | `/api/admin/companies/{id}`    | Get company by ID        | Admin  |
| `DELETE` | `/api/admin/companies/{id}`    | Delete company           | Admin  |
| `GET`    | `/api/admin/jobs`              | Get all jobs             | Admin  |
| `GET`    | `/api/admin/jobs/{id}`         | Get job by ID            | Admin  |
| `PATCH`  | `/api/admin/jobs/{id}/toggle`  | Toggle job active status | Admin  |
| `DELETE` | `/api/admin/jobs/{id}`         | Delete job               | Admin  |
| `GET`    | `/api/admin/stats`             | Get platform statistics  | Admin  |

---

## Authentication Flow

### Register

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "email": "user@test.com",
  "password": "Test@1234",
  "confirmPassword": "Test@1234"
}
```

Example response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

---

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "user@test.com",
  "password": "Test@1234"
}
```

Example response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "role": "USER"
}
```

---

### Use Protected Endpoint

```http
GET /api/user/cv
Authorization: Bearer <accessToken>
```

---

## Roles

Available roles:

```text
USER
COMPANY
ADMIN
```

---

## Job Enums

### Employment Type

```text
INTERNSHIP
STUDENT_WORK
PART_TIME
FULL_TIME
```

### Work Mode

```text
ONSITE
REMOTE
HYBRID
```

### Application Status

```text
APPLIED
REVIEWED
SHORTLISTED
CONTACTED
REJECTED
ACCEPTED
WITHDRAWN
```

---

## Common HTTP Status Codes

| Status                  | Meaning                                     |
| ----------------------- | ------------------------------------------- |
| `200 OK`                | Request successful                          |
| `201 Created`           | Resource created                            |
| `204 No Content`        | Request successful, no response body        |
| `400 Bad Request`       | Invalid request body or validation error    |
| `401 Unauthorized`      | Missing or invalid authentication token     |
| `403 Forbidden`         | Authenticated user does not have permission |
| `404 Not Found`         | Resource not found                          |
| `409 Conflict`          | Resource already exists or duplicate action |
| `410 Gone`              | Token expired or already used               |
| `429 Too Many Requests` | Rate limit exceeded                         |

---

## Notes

* All protected endpoints require JWT authentication.
* User endpoints require the `USER` role.
* Company endpoints require the `COMPANY` role.
* Admin endpoints require the `ADMIN` role.
* File upload endpoints use `multipart/form-data`.
* Job and CV list endpoints use Spring pagination.
* Password reset and company invite features require email configuration.
* Cloudinary configuration is required for file uploads.
* Google OAuth requires Google client configuration.

---

## License

This project is currently private and intended for educational and portfolio use.
