# Quarkus Skill

Use this checklist when building cloud-native services with Quarkus.

## Scope

- Focus: Quarkus runtime patterns, HTTP resource design, configuration, testing, and ops readiness.
- Pair with Java and Maven skills for language and build details.

## Defaults

- Quarkus 3.x
- Java 21
- Maven build
- RESTEasy Reactive for HTTP APIs
- Config via `application.properties` and profiles
- Testing with `@QuarkusTest` and RestAssured

## Resource and Service Design

- Keep JAX-RS resources thin and delegate logic to services.
- Validate request payloads at boundary with Bean Validation.
- Use DTOs for API contracts; avoid leaking entity internals.
- Return explicit status codes and consistent error payloads.
- Keep dependency injection constructor-based when possible.

## Quarkus Patterns

- Use CDI scopes intentionally (`@ApplicationScoped`, `@RequestScoped`).
- Externalize environment-specific values via config profiles.
- Use `@ConfigProperty` for required config with clear names.
- Prefer reactive clients and non-blocking paths for I/O-heavy flows.
- Keep startup logic minimal to preserve fast boot.

## Build and Dev Workflow

- Run dev mode with `mvn quarkus:dev`.
- Verify tests with `mvn -q -DskipTests=false test`.
- Build runnable artifact with `mvn clean package`.
- Use native build profile only when required for deployment targets.

## Quality Gate

- Resource tests cover success and failure paths.
- Validation and exception mapping are tested.
- No secrets in code or logs.
- Health and readiness endpoints are configured when needed.
- Configuration keys are documented with defaults.

## Security Checklist

- Apply authentication and authorization before production rollout.
- Validate and sanitize all inbound user input.
- Avoid exposing stack traces in HTTP responses.
- Use secure defaults for CORS and headers.
- Keep dependencies and Quarkus platform versions up to date.

## Performance Checklist

- Avoid blocking calls on event-loop threads.
- Use pagination and filtering on list APIs.
- Keep JSON payloads compact and versioned.
- Cache expensive reads where consistency allows.
- Benchmark critical endpoints before tuning.

## Reusable Prompts

### Generate Quarkus Endpoint

Create a Quarkus REST endpoint in Java 21.
Requirements:
- path `/api/v1/<resource>`
- request/response DTOs
- service layer with validation
- `@QuarkusTest` + RestAssured tests
- explicit error mapping and status codes

### Review Quarkus Service

Review this Quarkus implementation for:
- correctness
- CDI scope and injection usage
- validation and exception mapping gaps
- test coverage gaps
- security and performance risks
Return findings ordered by severity with concrete fixes.
