# Quarkus Skill

Use this checklist when building cloud-native services with Quarkus.

## Scope

- Focus: Quarkus runtime patterns, HTTP resource design, configuration, testing, and ops readiness.
- Pair with Java and Maven skills for language and build details.
- Use this skill when the task is specifically about Quarkus architecture, CDI behavior, transaction boundaries, resource or service layering, or runtime execution model concerns.

## Defaults

- Quarkus 3.x
- Java 21
- Maven build
- RESTEasy Reactive for HTTP APIs
- Config via `application.properties` and profiles
- Testing with `@QuarkusTest` and RestAssured

## Working Approach

### Identify The Layer First

- Determine whether the target code is a resource, service bean, repository, mapper, configuration producer, or security component before changing it.
- Keep HTTP concerns in resources, business rules in services, and persistence details below the service layer.
- Do not blur framework roles just to reduce file count.

### Match CDI Scope To Lifecycle

- Prefer `@ApplicationScoped` for stateless shared services and repositories.
- Use `@RequestScoped` only when true per-request state is needed.
- Use `@Dependent` only when dependent lifecycle behavior is intentional.
- Keep bean scopes explicit when lifecycle choice materially affects correctness or resource use.

## Resource and Service Design

- Keep JAX-RS resources thin and delegate logic to services.
- Validate request payloads at boundary with Bean Validation.
- Use DTOs for API contracts; avoid leaking entity internals.
- Return explicit status codes and consistent error payloads.
- Keep dependency injection constructor-based when possible.
- Keep HTTP status handling, request parsing, and header concerns in the resource layer.
- Keep mapping logic in mappers or clearly bounded conversion code rather than spreading it across resources and services.

## Quarkus Patterns

- Use CDI scopes intentionally (`@ApplicationScoped`, `@RequestScoped`).
- Externalize environment-specific values via config profiles.
- Use `@ConfigProperty` for required config with clear names.
- Prefer reactive clients and non-blocking paths for I/O-heavy flows.
- Keep startup logic minimal to preserve fast boot.
- Avoid field injection unless there is a clear framework-specific reason.
- Keep required dependencies explicit and constructor-injected for easier testing.

## Transactions

- Put `@Transactional` on service-layer methods that mutate state.
- Do not treat resources as the primary transaction boundary for business logic.
- Do not scatter transactional behavior across several layers without a clear reason.
- Do not add `@Transactional` to read-only flows by habit.

## Caching

- Cache only deterministic, stable reads with clear freshness rules.
- Be explicit about cache keys and invalidation assumptions.
- Do not cache outputs whose staleness or ownership rules are unclear.

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
- Protect endpoints with explicit authorization rules such as `@RolesAllowed` where appropriate.
- Avoid logging secrets, tokens, or sensitive user data in Quarkus resources and services.

## Performance Checklist

- Avoid blocking calls on event-loop threads.
- Use pagination and filtering on list APIs.
- Keep JSON payloads compact and versioned.
- Cache expensive reads where consistency allows.
- Benchmark critical endpoints before tuning.
- Call out blocking-versus-reactive mismatches explicitly when a reactive path performs blocking work.

## Review Order

1. Is the behavior correct?
2. Is the concern in the right layer?
3. Is the CDI scope appropriate?
4. Is transaction handling placed correctly?
5. Is configuration externalized?
6. Are security and sensitive-data concerns handled correctly?
7. Is there a blocking or reactive mismatch?

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

### Refactor Quarkus Code

Refactor this Quarkus code while preserving behavior.
Requirements:
- move business logic out of resources when needed
- keep persistence details out of higher layers where practical
- tighten CDI scopes and constructor injection
- place `@Transactional` at the right service boundary
- call out any blocking versus reactive runtime mismatch
