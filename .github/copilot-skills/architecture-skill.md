# Architecture Guidelines Skill

Use this checklist for layered service architecture using controller or resource, service, and repository layers.

## Scope

- Focus: clear layer boundaries, dependency direction, and maintainable module structure.
- Pair with Java, Quarkus, DynamoDB, and Testing skills for implementation details.

## Project Structure Standard (Java + Quarkus + AWS)

- Root package: `com.projectname`.
- Prefer domain-first modular structure by bounded context.
- Each module owns its own resource, service, repository, entity, dto, mapper, exception, config, and util packages.
- Shared concerns must be explicit in a shared package, not copied across modules.

### Recommended Modular Layout (DDD)

```text
src/main/java/com/projectname
	common/
		config/
		exception/
		util/
	customer/
		resource/
		service/
		repository/
		entity/
		dto/
		mapper/
		exception/
		config/
		util/
	order/
		resource/
		service/
		repository/
		entity/
		dto/
		mapper/
		exception/
		config/
		util/
```

### Minimal Layout (Single Domain)

Use this only for small services with one bounded context.

```text
src/main/java/com/projectname
	resource/
	service/
	repository/
	entity/
	dto/
	mapper/
	exception/
	config/
	util/
```

### AWS Placement Rules

- Keep AWS clients and settings in module `config` or `common/config`.
- Keep DynamoDB item mapping and key logic in `repository`.
- Keep AWS DTO/request objects out of `resource` contracts.
- Keep business invariants in `service` and `entity`, not in AWS adapters.
- Keep IAM/resource names and environment values in Quarkus config profiles.

## Layer Model

- API Layer: resource (JAX-RS/Quarkus style).
- Service Layer: business rules, orchestration, transactions, and invariants.
- Repository Layer: persistence access and query logic only.
- Domain Layer: entities, value objects, domain services, and business constraints.

## Dependency Rules

- Allowed direction: API -> Service -> Repository.
- Domain should not depend on API or persistence frameworks.
- Repository should not call API layer classes.
- Service should not expose persistence-specific models to API layer.
- No circular dependencies across layers.

## Package Dependency Rules (Modular)

- Allowed in module: `resource -> service -> repository`.
- `entity` and domain logic must not depend on `resource`.
- `dto` is boundary-focused and should not carry persistence annotations.
- `mapper` can depend on `dto` and `entity`, but not on HTTP concerns.
- `exception` contains typed errors used by `service` and translated by `resource`.
- `config` wires frameworks and clients, but does not own business rules.
- `util` must stay small and framework-agnostic; avoid turning it into a dumping ground.
- Cross-module calls should go through service interfaces, not direct repository access.

## API Layer Rules (Controller or Resource)

- Keep endpoints thin; delegate business logic to services.
- Validate inbound request payloads at the boundary.
- Translate service results to API DTOs.
- Return consistent HTTP status codes and error payloads.
- Do not access repositories directly from API layer.

## Service Layer Rules

- Own business workflows and invariants.
- Coordinate calls across repositories and external clients.
- Keep methods use-case oriented, not CRUD-only by default.
- Raise typed domain exceptions for expected failures.
- Avoid framework-heavy annotations unless needed by cross-cutting concerns.

## Repository Layer Rules

- Encapsulate persistence details and query expressions.
- Return domain-friendly models, not transport DTOs.
- Keep repository methods explicit and access-pattern driven.
- Avoid embedding business decisions in repository logic.
- Handle pagination and key-based access patterns consistently.

## DTO and Mapping Rules

- Use request and response DTOs at API boundary.
- Keep domain entities internal to service and repository layers.
- Use dedicated mappers for non-trivial transformations.
- Avoid leaking persistence-only attributes to public APIs.
- Version API contracts when making breaking changes.

## Cross-Cutting Rules

- Centralize error handling and logging policy.
- Add authorization checks at boundary and business-rule checkpoints.
- Propagate correlation IDs through all layers.
- Keep configuration in one place and inject through constructors.
- Enforce idempotency for retry-prone operations.

## Anti-Patterns

- Fat API layer: resource/controller contains business logic, branching, or persistence calls.
- Anemic service layer: service only forwards calls with no business invariants.
- Smart repository: repository owns business decisions instead of persistence concerns.
- Model leakage: persistence entities or internal domain objects returned directly in API responses.
- Layer skipping: API calls repository directly, bypassing service orchestration.
- Cyclic dependencies: modules or packages depend on each other across layers.
- God service: one service owns unrelated use cases across multiple bounded contexts.
- Generic catch-all interfaces: repositories/services with vague methods like `process` or `handle`.
- Shared mutable state: cross-request state stored in singleton services without thread-safety.
- Transaction sprawl: transaction boundaries spread across API and repository layers.

## Anti-Pattern Corrections

- Move business rules from API to service use-case methods.
- Keep repositories focused on access patterns and data mapping.
- Introduce DTOs and mappers at API boundaries.
- Split large services by feature or bounded context.
- Enforce one-way dependencies through package/module rules.
- Define transaction boundaries in service layer orchestration.

## Testing by Layer

- API layer: contract, validation, status code, and serialization tests.
- Service layer: business rule and orchestration tests with mocked repositories.
- Repository layer: integration tests against real datastore behavior.
- End-to-end tests: only critical user journeys.

## Quality Gate

- No API-to-repository direct calls.
- No domain model leakage in external API contracts.
- Each business use case has service-layer tests.
- Error mapping is consistent across endpoints.
- Layer dependencies follow architecture rules.

## Reusable Prompts

### Generate Layered Feature

Create a layered implementation for <feature>.
Requirements:
- API resource/controller with validation
- service with business rules and typed exceptions
- repository interfaces and implementations
- DTO mappings between API and domain
- unit and integration tests per layer

### Generate DDD Modular Feature (Quarkus + AWS)

Create a domain-first feature for `<module>` under `com.projectname`.
Requirements:
- package layout: `resource`, `service`, `repository`, `entity`, `dto`, `mapper`, `exception`, `config`, `util`
- JAX-RS resource with validation and consistent error responses
- use-case oriented service methods with typed domain exceptions
- repository methods aligned to DynamoDB access patterns
- explicit mapper between API DTOs and domain entities
- Quarkus config for AWS settings by profile
- tests for resource, service, and repository behavior

### Review Layered Architecture

Review this code for architecture issues:
- layer boundary violations
- wrong dependency direction
- domain leakage in API
- repository business-logic drift
- anti-patterns and refactoring priority
- missing tests by layer
Return findings ordered by severity with concrete fixes.
