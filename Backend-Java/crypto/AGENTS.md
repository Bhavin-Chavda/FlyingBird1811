# AGENTS.md - Crypto Microservice Development Guide

## Architecture Overview
This is a Spring Boot 4.0.5 microservice for crypto operations, following layered architecture with mandatory separation: controller, service, repository, entity, dto, mapper, config, exception, util.

- **Package Structure**: `com.flyingbird.crypto` with subpackages for each layer.
- **Data Flow**: Controllers handle API requests, delegate to services for business logic, services use repositories for data access via JPA.
- **Security**: Spring Security enabled, JWT/OAuth2 expected (currently basic config in `SecurityConfig.java`).
- **Database**: MySQL with JPA/Hibernate, ddl-auto=update in dev (migrate to Flyway/Liquibase for prod).
- **Observability**: Actuator integrated for metrics, OpenAPI for docs.

## Key Patterns & Conventions
- **Lombok Usage**: Entities use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` (e.g., `User.java`).
- **Repository Naming**: Interfaces extend `JpaRepository<Entity, ID>`, custom methods like `findByUsername` (e.g., `userRepository.java` - note: class names should be PascalCase, avoid lowercase starts).
- **Configuration**: Externalize secrets; avoid hardcoded values like DB password in `application.yaml`.
- **Validation**: Use `@Valid` on DTOs, global exception handler for errors.
- **Logging**: Structured with levels (INFO for checkpoints, e.g., "Order created | orderId=123"), include correlation IDs.
- **API Design**: RESTful, proper HTTP methods, status codes; Swagger at `/swagger-ui/**`.

## Developer Workflows
- **Build**: Use `./mvnw clean compile` (wrapper included).
- **Run**: `./mvnw spring-boot:run` (devtools for hot reload).
- **Test**: `./mvnw test` (H2 in-memory for unit tests, avoid MySQL in tests).
- **Debug**: Enable `show-sql: true` in `application.yaml` for Hibernate queries.
- **Dependencies**: Add via `pom.xml`, use starters (e.g., `spring-boot-starter-web`).

## Integration Points
- **Database**: MySQL connector, connection pooling via HikariCP (default).
- **Security**: Basic auth setup, extend for JWT in `SecurityConfig.java`.
- **Docs**: SpringDoc OpenAPI at `/v3/api-docs/**`.
- **External**: No inter-service comms yet; use REST/gRPC with DTOs, Resilience4j for resiliency.

## Code Quality Rules
- **Complexity**: Methods <50 lines, classes <500, no nested loops >2.
- **Naming**: PascalCase classes, camelCase methods/vars, UPPER_CASE constants.
- **Injection**: Constructor only, no field injection.
- **Testing**: JUnit + Mockito, 80% coverage, integration tests with test slices.

Reference: `.github/copilot-instructions.md` for detailed coding standards.</content>
<parameter name="filePath">/home/bhavin-chavda/Desktop/crypto/crypto/AGENTS.md
