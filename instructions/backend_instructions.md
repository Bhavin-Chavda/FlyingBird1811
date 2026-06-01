# Backend Development Instructions

Backend folder:

```text
D:\Bhavin\Backend-Java
```

Framework:

```text
Java Spring Boot
```

## Main Goals

Backend code should be secure, predictable, validated, modular, testable, easy to debug, safe for production, and aligned with frontend API expectations.

Do not place business logic directly inside controllers if the project uses services.

## Before Editing Backend

First check:

1. Existing package structure
2. Existing controller pattern
3. Existing service pattern
4. Existing repository pattern
5. Existing entity/model pattern
6. Existing DTO pattern
7. Existing validation pattern
8. Existing authentication/security pattern
9. Existing exception handling pattern
10. Existing database configuration

Match the project. Do not invent a second architecture.

## Spring Boot Structure Rules

Prefer the existing structure. Typical layers may be:

```text
controller
service
repository
entity
dto
config
security
exception
mapper
util
```

Use the actual existing package names and conventions.

Do not create new top-level packages unless needed.

## API Design Rules

Every API endpoint should have:

- clear route name
- correct HTTP method
- request validation
- authorization check if needed
- consistent response shape
- consistent error shape
- proper status code
- no leaked internal error details

Use the existing project response pattern.

If no pattern exists, prefer:

```json
{
  "success": true,
  "data": {},
  "message": "Optional message"
}
```

For errors:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human readable error"
  }
}
```

## Controller Rules

Controllers should handle request mapping, request validation trigger, extracting request data, calling service methods, and returning response.

Controllers should not contain heavy business logic.

## Service Rules

Services should handle business rules, database calls through repositories, transaction logic, integration with external services, and decision-making logic.

Use `@Transactional` where needed for multi-step database writes.

## Repository And Database Rules

Before changing database logic:

1. Check existing entities.
2. Check existing repositories.
3. Check relationships.
4. Check indexes or constraints if visible.
5. Check migrations if the project uses them.
6. Confirm whether schema changes are required.

Do not change schema casually.

For database writes, consider validation, duplicate records, transactions, race conditions, rollback behavior, soft delete vs hard delete, and audit fields like `createdAt` and `updatedAt`.

## DTO And Validation Rules

Prefer DTOs for request and response objects.

Do not expose entities directly if the project already uses DTOs.

Use existing validation style. Common options:

```java
@NotNull
@NotBlank
@Email
@Size
@Min
@Max
@Valid
```

Validate all client input.

## Security Rules

Never hardcode secrets, expose passwords, log tokens, return raw database errors, trust client input, skip authorization checks, store plain-text passwords, or expose private config values to frontend.

Always validate input, check permissions, use safe config, use safe password hashing, use parameterized queries or Spring Data protections, and avoid leaking stack traces.

## Authentication And Authorization

For protected APIs:

1. Verify user identity.
2. Check permissions.
3. Check ownership of resource where needed.
4. Return proper status codes.

Use:

- `401` for unauthenticated
- `403` for authenticated but not allowed
- `404` when resource does not exist or should not be revealed
- `400` for validation errors
- `500` only for unexpected server errors

## Exception Handling

Use the existing global exception handler if available.

Common Spring pattern:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Errors should be consistent, logged internally, safe externally, and useful for debugging.

Do not add random try/catch blocks everywhere if centralized handling exists.

## Logging Rules

Use existing logging approach.

Do not log passwords, tokens, secret keys, full personal data, or raw sensitive request bodies.

## Frontend Contract Rule

Before changing backend endpoint behavior:

1. Search frontend usage in `D:\Bhavin\FlyingBird-UI`.
2. Confirm request and response expectations.
3. Preserve existing contract unless the task requires a change.
4. If contract changes, update frontend and `D:\Bhavin\project_details.md`.

## Backend Testing

When backend changes are made, suggest or run the actual project commands.

For Maven:

```bash
cd D:\Bhavin\Backend-Java
mvn clean install
mvn test
mvn spring-boot:run
```

For Gradle:

```bash
cd D:\Bhavin\Backend-Java
gradlew build
gradlew test
gradlew bootRun
```

Use whichever build tool actually exists.

## Backend Final Response Checklist

Before final response, verify:

- Did I read `project_details.md` first?
- Did I inspect only relevant backend files?
- Did I follow existing Spring Boot patterns?
- Did I avoid unrelated refactoring?
- Did I preserve existing behavior?
- Did I check frontend usage if API changed?
- Did I update `project_details.md` if needed?
- Did I provide exact test steps?
