# Testing Guidelines Skill

Use this checklist for reliable unit and integration testing in Java services.

## Scope

- Focus: test strategy, unit test quality, integration coverage, and CI reliability.
- Pair with Java, Quarkus, Maven, and DynamoDB skills for domain-specific details.

## Test Pyramid Defaults

- Most tests should be unit tests.
- Add integration tests for boundaries: HTTP, database, messaging, and external APIs.
- Keep end-to-end tests small and targeted to critical flows.

## Unit Testing Guidelines

- Test behavior, not implementation details.
- Use clear Arrange-Act-Assert structure.
- Prefer one behavior assertion per test method.
- Name tests as expected behavior and condition.
- Mock only external dependencies, not value objects.
- Cover happy path, edge cases, and failure paths.
- Keep tests deterministic and time-independent.

## Integration Testing Guidelines

- Validate real wiring between layers and real serializers.
- Use test containers or managed test resources for databases and infrastructure.
- Test API contracts: status codes, payload shape, and error responses.
- Verify transaction boundaries and idempotency behavior.
- Avoid network calls to third-party systems in default test runs.
- Keep fixtures minimal and isolated per test.

## Data and Fixture Rules

- Use factory methods/builders for test data readability.
- Keep test data close to test cases.
- Reset shared state between tests.
- Avoid hardcoded timestamps; use controllable clocks.
- Prefer explicit cleanup over order-dependent tests.

## Reliability and CI Rules

- No flaky tests; quarantine and fix immediately.
- Run unit tests on every commit.
- Run integration tests in CI and before release.
- Ensure test execution order does not affect outcomes.
- Fail fast on assertion failures and setup errors.

## Coverage and Quality Gate

- Critical business rules must be covered by tests.
- Error handling and validation flows must be tested.
- New public APIs require both positive and negative tests.
- Merge only when unit and integration suites pass.
- Track coverage trends; prioritize meaningful coverage over raw percentage.
- Enforce automatic code coverage verification in the standard test or verify workflow.
- Ensure the coverage tool is attached to the same test JVMs that execute the suite so coverage results reflect the real `verify` run instead of stale reports.
- Use a default minimum of 95% line coverage unless the repository documents a justified alternative threshold.
- If coverage falls below the threshold, add or generate JUnit tests for the lowest-covered public behaviors before closing the task.

## Reusable Prompts

### Generate Unit Tests

Create JUnit 5 unit tests for <class or method>.
Requirements:
- Arrange-Act-Assert structure
- happy path, edge case, and failure case coverage
- mocks only for external dependencies
- clear test names and no duplicated setup

### Generate Integration Tests

Create integration tests for <feature>.
Requirements:
- validate HTTP status and response schema
- verify database state changes and rollback behavior
- include negative/error scenarios
- isolate test data and avoid external network dependencies

### Review Test Suite

Review this test suite for:
- missing coverage on critical behavior
- flaky patterns and nondeterminism
- over-mocking or implementation-coupled tests
- integration gaps across boundaries
- CI reliability risks
Return findings ordered by severity with concrete fixes.
