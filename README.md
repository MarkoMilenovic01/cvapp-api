# CVApp API

Spring Boot REST API for CV profiles, company recruitment, job listings and applications. Authentication is stateless JWT with `USER`, `COMPANY` and `ADMIN` roles.

## Documentation

- [Authentication](docs/auth.md)
- [User CV](docs/cv.md)
- [Companies](docs/company.md)
- [Jobs and applications](docs/job.md)
- [Administration](docs/admin.md)

## Stack

- Java 25 and Spring Boot 4.1
- Spring Security and Spring Data JPA
- PostgreSQL and Flyway
- Maven Wrapper
- Docker and Docker Compose
- Cloudinary, Java Mail and Google ID-token authentication
- JUnit, MockMvc and Testcontainers

## Roles

| Role | Access |
| --- | --- |
| `USER` | CV management, company directory, job discovery and applications |
| `COMPANY` | Company profile, CV discovery, favorites/history and job management |
| `ADMIN` | User, company and job administration plus platform statistics |

Authenticated requests use:

```http
Authorization: Bearer <accessToken>
```

## Local development with Docker

Requirements: Docker with Compose. The Compose stack starts PostgreSQL 15, Mailpit and the API.

```bash
docker compose up --build
```

Services:

| Service | URL |
| --- | --- |
| API | `http://localhost:8080` |
| Health | `http://localhost:8080/actuator/health` |
| Mailpit UI | `http://localhost:8025` |
| PostgreSQL | `localhost:5432` |

The `dev` profile uses Flyway and `ddl-auto=validate`; Hibernate never creates or updates the schema. On first startup Flyway applies every migration in `src/main/resources/db/migration`.

If a local volume was created by the former Hibernate-managed setup, recreate it once:

```bash
docker compose down -v
docker compose up --build
```

`down -v` permanently deletes local database data.

Useful Make targets:

```bash
make db-up
make db-down
make db-reset   # destructive: deletes the local database volume
make test
```

## Running without the application container

Start PostgreSQL and Mailpit, then run Spring Boot with the `dev` profile:

```bash
docker compose up -d postgres mailpit
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

The profile defaults to PostgreSQL at `localhost:5432/cvapp_db` and Mailpit at `localhost:1025`.

## Local environment variables

Copy local values into an untracked `.env`. Never commit real credentials.

```env
APP_JWT_SECRET=<at-least-32-byte-development-secret>
APP_JWT_EXPIRATION=86400000
APP_JWT_REFRESH_EXPIRATION=604800000
GOOGLE_CLIENT_ID=<google-client-id>
CLOUDINARY_CLOUD_NAME=<cloud-name>
CLOUDINARY_API_KEY=<api-key>
CLOUDINARY_API_SECRET=<api-secret>
```

Compose supplies its own local database and Mailpit settings. `.env` is development-only and is not used by Render automatically.

## Tests

Integration tests share a PostgreSQL 16 Testcontainer for the full test JVM. Docker must be running.

```bash
./mvnw clean test
```

Run one class:

```bash
./mvnw -Dtest=CompanyFavoriteControllerTest test
```

Compile tests without starting containers:

```bash
./mvnw -DskipTests test-compile
```

CI runs the same suite with the `test` profile.

## Database migrations

Flyway is enabled in `dev`, `test` and `prod`. Add schema changes as the next immutable migration; do not edit a migration that has already reached a shared database.

Current history includes CV projects and experience types, unique skill/company-view constraints, and job text-length constraints through V18.

Before deploying a new constraint, verify existing production rows satisfy it. All migration files must be committed or they will not be packaged into the image.

## Render deployment

Render builds the [Dockerfile](Dockerfile); `docker-compose.yml` remains a local-development tool. Create a Docker Web Service, attach managed PostgreSQL, and configure:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
JWT_SECRET
JWT_EXPIRATION
JWT_REFRESH_EXPIRATION
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
FRONTEND_URL
GOOGLE_CLIENT_ID
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
PASSWORD_RESET_EXPIRATION_HOURS
INVITE_EXPIRATION_HOURS
```

The application listens on Render's `PORT`, runs Flyway, and then validates the schema with Hibernate. A new empty database is migrated automatically. A non-empty database without `flyway_schema_history` needs a controlled baseline; do not enable `baseline-on-migrate` blindly.

### Deployment smoke test

```bash
./deployment-smoke-test.sh https://your-api.onrender.com
```

Optional authenticated and CORS checks:

```bash
export TEST_EMAIL="deployment-test@example.com"
read -sr TEST_PASSWORD && export TEST_PASSWORD
export FRONTEND_URL="https://your-frontend.example.com"
./deployment-smoke-test.sh https://your-api.onrender.com
unset TEST_PASSWORD
```

The script is non-destructive. It checks health, anonymous security, request validation, optional login and optional CORS, and exits non-zero on failure.

## Production notes

- Do not deploy `.env`; configure secrets in Render.
- Ensure `.env` is not tracked by Git and rotate any secret that was committed.
- Configure the deployed frontend origin in Spring Security before browser integration testing.
- `/login` is not an API page. Use `POST /api/auth/login`; use `GET /actuator/health` for browser health checks.
- The API exposes only health through Actuator and suppresses production stack traces and error details.
