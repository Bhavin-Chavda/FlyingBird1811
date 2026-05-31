# 🚀 Copilot Instructions: Enterprise Java Microservices Development Guide

## 🧠 Core Philosophy

Build systems, not features.

Every line of code must optimize for:

* Scalability
* Reliability
* Maintainability
* Observability
* Security

Avoid shortcuts that create long-term technical debt.

---

## 🏗️ Architecture Principles

### Follow strictly:

* SOLID Principles
* DRY (Don't Repeat Yourself)
* KISS (Keep It Simple, Stupid)
* Clean Architecture / Layered Architecture
* Separation of Concerns

### Mandatory Layers:

* Controller (API Layer)
* Service (Business Logic)
* Repository (Data Access)
* DTO / Mapper Layer

Never mix responsibilities across layers.

---

## 🧩 Code Structure Guidelines

### Package Structure

```
com.project
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── mapper
 ├── config
 ├── exception
 ├── util
```

### Rules:

* No business logic in controllers
* Services must be interface-driven
* Use constructor injection only
* Avoid field injection

---

## 🧪 Code Quality & Sonar Compliance

### Must Follow:

* No code duplication
* Cyclomatic complexity < 10 per method
* Methods < 50 lines
* Classes < 500 lines
* Avoid nested loops > 2 levels
* No hardcoded values → use constants/config

### Naming:

* Classes → PascalCase
* Methods/variables → camelCase
* Constants → UPPER_CASE

---

## 🔐 Security Guidelines

* Never expose internal exceptions
* Use global exception handler
* Validate all inputs (DTO validation)
* Use parameterized queries (prevent SQL injection)
* Avoid storing secrets in code → use env/config server
* Use HTTPS only
* Implement authentication & authorization (JWT/OAuth2)

---

## 📊 Logging Strategy

Use structured logging.

### Levels:

* ERROR → System failures
* WARN → Unexpected but handled
* INFO → Business flow checkpoints
* DEBUG → Development insights

### Rules:

* Never log sensitive data (passwords, tokens)
* Include correlation IDs for tracing

Example:

```
INFO: Order created | orderId=123 | userId=456
```

---

## 📡 Observability & Monitoring

* Integrate:

    * Metrics (Micrometer + Prometheus)
    * Tracing (OpenTelemetry / Zipkin)
    * Logging (ELK stack)

Track:

* API latency
* Error rates
* Throughput

---

## 🔄 Inter-Service Communication

### Preferred:

* REST (for simplicity)
* gRPC (for high performance)

### Rules:

* Use DTOs for communication
* Never expose internal entities
* Implement retries with backoff
* Use circuit breakers (Resilience4j)

---

## 🛡️ Resiliency Patterns

Implement:

* Retry
* Circuit Breaker
* Timeout
* Bulkhead

Fail gracefully, not catastrophically.

---

## 💾 Database Best Practices

* Use connection pooling (HikariCP)
* Index critical columns
* Avoid N+1 queries
* Use transactions properly
* Use Flyway/Liquibase for migrations (NOT ddl-auto in prod)

---

## ⚙️ Configuration Management

* Use profiles: dev / test / prod
* Externalize configs
* Use environment variables

---

## 📦 API Design Standards

* Use REST conventions

* Proper HTTP methods:

    * GET → fetch
    * POST → create
    * PUT → update
    * DELETE → remove

* Use proper status codes:

    * 200, 201, 400, 404, 500

---

## 📄 Documentation

* Use Swagger / OpenAPI
* Document:

    * APIs
    * Request/Response formats
    * Error codes

---

## 🧪 Testing Strategy

* Unit Tests (JUnit + Mockito)
* Integration Tests
* Minimum coverage: 80%

---

## 🚀 Performance Guidelines

* Avoid blocking calls
* Use async where needed
* Cache frequently used data (Redis)

---

## ❌ Anti-Patterns to Avoid

* God classes
* Hardcoded configs
* Tight coupling
* Silent exception handling
* Over-engineering

---

## 🧠 Copilot Behavior Instructions

When generating code:

* Always follow layered architecture
* Always include logging
* Always validate inputs
* Always consider edge cases
* Always write production-ready code
* Never generate incomplete or pseudo code

---

## 🏁 Final Rule

Code should be:

* Readable by humans
* Maintainable by teams
* Scalable under load
* Debuggable in production

If code fails any of these → rewrite it.

---

🔥 Build like your system will handle 10 million users from Day 1.
