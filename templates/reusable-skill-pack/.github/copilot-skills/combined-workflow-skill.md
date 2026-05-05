# Combined Workflow Skill

Use this guide to combine all skills in a single delivery workflow for Java + Quarkus services on AWS DynamoDB.

## Skills Used Together

- Architecture Skill: layer boundaries and anti-pattern checks.
- Java Skill: coding quality, error handling, and maintainability.
- Quarkus Skill: resource patterns, CDI, runtime behavior.
- Swagger/OpenAPI Skill: API contract and documentation quality.
- DynamoDB Skill: access-pattern-first table and index design.
- Logger Skill: structured logging, redaction, and correlation strategy.
- Code Design Principles Skill: modular boundaries, SOLID, and clear contracts.
- Concurrency and Thread Safety Skill: shared-state safety and async execution correctness.
- Security Skill: authentication, authorization, secrets, and data protection.
- Testing Skill: unit and integration strategy.
- Maven Skill: build, dependency, and CI reproducibility.

## Recommended Order

1. Architecture first:
- define API, service, repository, and domain responsibilities
- identify forbidden dependencies and anti-pattern risks

2. API contract next:
- write OpenAPI paths, DTO schemas, error model, and security scheme
- confirm status codes and versioning approach

3. Data model and access patterns:
- list DynamoDB read and write patterns
- design partition/sort keys and required GSIs

4. Implementation:
- build Quarkus resource endpoints from the API contract
- implement service use cases and repository operations
- enforce Java coding, validation, and exception rules

5. Design quality pass:
- review responsibilities, interfaces, and dependency direction
- remove code smells and enforce module boundaries

6. Concurrency and thread safety pass:
- verify shared-state ownership and synchronization strategy
- configure executor limits, timeouts, and interruption handling

7. Logging and observability:
- define event names, log levels, and required fields
- enforce correlation IDs and sensitive-data redaction rules

8. Security hardening:
- enforce authn/authz checks and least-privilege IAM
- validate secrets management and secure configuration defaults

9. Testing:
- unit tests for service/domain rules
- integration tests for resource-to-repository behavior
- contract checks for status codes and payload shape

- add concurrency-focused tests for shared-state and async flows
- add negative security tests for unauthorized, forbidden, and invalid requests

10. Build and release quality:
- run Maven quality gates (`test`, `verify`)
- review dependency and plugin consistency

## Combined Quality Gate

- Layer boundaries are respected.
- OpenAPI matches implementation behavior.
- DynamoDB queries are key-based, not scan-heavy.
- Code design remains modular with no cyclic dependencies.
- Concurrency strategy is explicit and validated for critical paths.
- Logs are structured, correlated, and free of sensitive data.
- Security controls are enforced across auth, secrets, and data handling.
- Unit and integration suites pass.
- Maven `verify` passes with pinned plugin and dependency versions.
- No security issues in API docs, logs, IAM scope, or dependency posture.

## Common Cross-Skill Anti-Patterns

- Contract drift: OpenAPI spec differs from actual endpoint behavior.
- Layer drift: API calls repository directly.
- Data drift: DynamoDB schema does not support actual query patterns.
- Design drift: growing god classes, unclear contracts, and boundary leaks.
- Concurrency drift: race conditions from unmanaged shared mutable state.
- Observability drift: logs are missing correlation IDs or usable event fields.
- Security drift: permissive IAM, weak validation, or secret leakage risk.
- Test drift: heavy mocking hides integration failures.
- Build drift: local build passes but CI fails from plugin/version mismatch.

## Reusable Prompts

### Generate Feature End-to-End

Create a complete feature for `<resource>` using:
- layered architecture (resource, service, repository)
- OpenAPI-first contract
- DynamoDB access-pattern-driven schema
- design-principle checks for cohesion, coupling, and module boundaries
- explicit concurrency/thread-safety strategy for shared-state and async work
- structured logging and redaction policy
- security controls for authn/authz, secrets, and sensitive data handling
- Quarkus implementation and validation
- unit and integration tests
- Maven-ready build and verification steps

### Review End-to-End Delivery

Review this feature across architecture, API contract, Quarkus code, DynamoDB design, design principles, concurrency/thread safety, logging, security, tests, and Maven build.
Return findings ordered by severity with concrete fixes and recommended execution order.
