# CLAUDE.md - Common-API

## Project Overview

Common-API is the gateway microservice for the AMRIT healthcare platform. It provides shared APIs consumed by all frontend UIs including authentication, beneficiary registration, call handling, location masters, notifications, feedback, reporting, and integrations with external systems (c-Zentrix CTI, Everwell, eAusadha, eSanjeevani, ABDM, Firebase, Honeywell POCT devices).

## Tech Stack

- Java 17
- Spring Boot 3.2.2
- Spring Data JPA / Hibernate
- MySQL 8.0
- Redis (session management, caching)
- MongoDB (optional, for specific integrations)
- Maven (build tool)
- Swagger/OpenAPI (API documentation)
- Lombok, MapStruct
- CryptoJS-compatible AES encryption
- Firebase Admin SDK
- WAR packaging (deploys to Wildfly)

## Build and Run

```bash
# Build
mvn clean install -DENV_VAR=local

# Run locally (start Redis first)
mvn spring-boot:run -DENV_VAR=local

# Package WAR
mvn -B package --file pom.xml -P <profile>   # profiles: dev, local, test, ci, uat

# Run tests
mvn test
```

### Configuration

- Copy `src/main/environment/common_example.properties` to `common_local.properties` and edit.
- Environment selected via `-DENV_VAR=<env>`.
- Swagger UI: `http://localhost:8083/swagger-ui.html`

## Package Structure

Base package: `com.iemr.common`

| Layer | Package | Description |
|-------|---------|-------------|
| Controllers | `controller.*` | REST endpoints (40+ sub-packages) |
| Services | `service.*` | Business logic |
| Repositories | `repository.*`, `repo.*` | JPA repositories |
| Entities | `data.*` | JPA entity classes |
| DTOs | `model.*` | Transfer objects |
| Mappers | `mapper.*` | Object mapping |
| Config | `config.*` | Swagger, encryption, Firebase, Quartz, prototypes |
| Constants | `constant` | Application constants |
| Utils | `utils.*` | Redis, HTTP, session, validation, exception |

## Key Functional Domains

- **Authentication/Authorization**: `controller.users` - login, session, user management
- **Beneficiary Registration**: `controller.beneficiary` - create, search, update beneficiaries
- **Call Handling**: `controller.callhandling` - CTI integration, call lifecycle
- **Feedback/Grievance**: `controller.feedback`, `controller.grievance` - feedback and complaint management
- **Location**: `controller.location` - state, district, block, village masters
- **Notifications**: `controller.notification` - alerts, SMS, email, Firebase push
- **Reporting**: `controller.report`, `controller.secondaryReport` - CRM reports
- **Helpline 104**: `controller.helpline104history` - medical advice history
- **COVID**: `controller.covid` - vaccination status
- **CTI Integration**: `controller.cti` - c-Zentrix computer telephony
- **External Integrations**: `controller.eausadha`, `controller.esanjeevani`, `controller.everwell`, `controller.honeywell`, `controller.brd`, `controller.carestream`
- **ABDM**: `controller.abdmfacility` - Ayushman Bharat Digital Mission
- **KM File Management**: `controller.kmfilemanager` - OpenKM document management
- **OTP/SMS**: `controller.otp`, `controller.sms` (via SMS gateway)
- **Scheduling**: `controller.questionconfig`, `controller.scheme`
- **Door-to-Door App**: `controller.door_to_door_app` - field worker support
- **NHM Dashboard**: `controller.nhmdashboard` - National Health Mission integration

## Architecture Notes

- Entry point: `CommonMain.java` (main class in `utils` package)
- Acts as the API gateway; all frontend UIs authenticate through Common-API
- Session management via Redis with 27-minute timeout
- HTTP interceptors attach `Authorization` and `ServerAuthorization` headers
- Status code `5002` signals session expiration to frontends
- AES + PBKDF2 encryption for password handling (`config.encryption`)
- Firebase integration for push notifications (`config.firebase`)
- Quartz scheduler for background jobs (`config.quartz`)
- Extensive test coverage with unit tests under `src/test/`

## CI/CD

- GitHub Actions: `package.yml`, `build-on-pull-request.yml`, `sast.yml`, `commit-lint.yml`, `codeql.yml`
- Conventional Commits enforced via Husky + commitlint
- Checkstyle configuration in `checkstyle.xml`
- JaCoCo for code coverage, SonarQube integration configured
- Dockerfile for containerized deployment
