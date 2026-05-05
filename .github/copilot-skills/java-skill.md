# Java Skill

Use this checklist when building Java applications independent of framework.

## Scope

- Focus: language-level design, code quality, testing, and maintainability.
- Pair with Maven or Quarkus skills when build or framework details are needed.

## Defaults

- Java 21
- Feature-based package structure
- Constructor injection for dependencies
- JUnit 5 for tests
- Structured logging (SLF4J-style)

## Coding Rules

- Prefer immutable value objects and explicit state transitions.
- Keep methods focused; one abstraction level per method.
- Validate at public boundaries and fail fast with clear messages.
- Avoid hidden side effects; make mutations obvious.
- Keep public APIs small and cohesive.

## Error Handling

- Use typed exceptions for domain and validation failures.
- Map internal exceptions to safe boundary messages.
- Do not leak stack traces or internals to clients.
- Include correlation IDs in logs when available.

## Concurrency and Resources

- Favor stateless services unless shared state is required.
- Use `CompletableFuture` and executors deliberately.
- Avoid unbounded thread creation.
- Close I/O resources with try-with-resources.
- Keep blocking operations out of hot paths.
- Document thread-safety assumptions on shared components.

## Quality Gate

- Unit tests cover happy path, edge cases, and failures.
- Public behavior-changing methods are tested.
- Static analysis findings are reviewed and resolved.
- No hardcoded secrets or machine-specific paths.

## Performance Checklist

- Pick data structures based on access pattern and cardinality.
- Avoid repeated object allocation in tight loops.
- Use streaming APIs only when they improve readability or performance.
- Add pagination or batching for large collections.
- Measure with profiling before optimizing.

## Security Checklist

- Sanitize and validate all external inputs.
- Use parameterized queries for database access.
- Apply least privilege to external integrations.
- Keep dependencies pinned and patched.
- Avoid logging secrets, tokens, or personal data.

## Reusable Prompts

### Generate Class

Create a Java 21 class for <feature>.
Requirements:
- clear package and naming
- constructor injection for dependencies
- boundary validation with meaningful errors
- JUnit 5 unit tests
- concise JavaDoc for public methods

### Review Java Code

Review this Java code for:
- correctness
- edge-case handling
- null safety and API contracts
- test coverage gaps
- security and performance risks
Return findings ordered by severity with concrete code fixes.
