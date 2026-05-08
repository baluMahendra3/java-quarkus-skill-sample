# Java Skill

Use this checklist when building Java applications independent of framework.

## Scope

- Focus: language-level design, code quality, testing, and maintainability.
- Pair with Maven or Quarkus skills when build or framework details are needed.
- Use this skill as the default Java code-quality layer when a more specific framework or security skill is not a better fit.

## Defaults

- Java 21
- Feature-based package structure
- Constructor injection for dependencies
- JUnit 5 for tests
- Structured logging (SLF4J-style)

## Working Approach

### Start With The Role Of The Class

- Identify whether the type is a DTO, response object, entity, service, mapper, utility, or resource-support type before changing it.
- Apply conventions that fit the class purpose instead of forcing the same pattern on every Java file.
- Prefer immutable carriers for DTOs and response models.
- Do not convert JPA entities or persistence-managed models to records or patterns that conflict with ORM behavior.

### Prefer Modern Java Only When It Clarifies

- Use Java 21 features when they reduce noise and make intent easier to read.
- Prefer `record` for immutable data carriers.
- Prefer `sealed` hierarchies for closed result or state models when the allowed variants are intentionally fixed.
- Prefer pattern matching for `instanceof` when it removes noisy casts.
- Prefer switch expressions when selecting values across several branches.
- Prefer text blocks for multiline SQL, JSON, or long messages when they improve readability.
- Do not force modern syntax where ordinary Java is clearer.

## Coding Rules

- Prefer immutable value objects and explicit state transitions.
- Prefer `final` fields where mutation is not required.
- Keep methods focused; one abstraction level per method.
- Validate at public boundaries and fail fast with clear messages.
- Avoid hidden side effects; make mutations obvious.
- Keep public APIs small and cohesive.

## Naming And API Design

- Use lowercase reverse-domain package naming.
- Use PascalCase for classes, interfaces, records, and enums.
- Use lowerCamelCase for methods and variables.
- Use UPPER_SNAKE_CASE for constants.
- Suffix custom exception types with `Exception`.
- Prefer intent-revealing names over vague names such as `doIt`, `handle`, or `process` without domain context.
- Prefer specific return types over `Object`, raw collections, or vague maps.
- Use `Optional` only as a return type, not as a field or parameter.
- Prefer immutable collections when mutation is not required.
- Keep required dependencies constructor-injected rather than set through mutators.

## Collections And Streams

- Use streams for mapping, filtering, and collection reshaping when they are clearer than loops.
- Avoid side effects inside stream pipelines.
- Prefer `.toList()` when an unmodifiable result is appropriate.
- Filter nulls explicitly before mapping or collecting.
- Use loops when they are easier to read, need early exits, or fit the control flow better.

## Error Handling

- Use typed exceptions for domain and validation failures.
- Map internal exceptions to safe boundary messages.
- Do not leak stack traces or internals to clients.
- Include correlation IDs in logs when available.
- Preserve the original cause when wrapping failures.
- Log exceptions at the boundary where they are handled, and log business events at the layer that owns the workflow step.
- Use parameterized SLF4J logging for application code.
- Never use `System.out.println` for application logging.
- Do not swallow exceptions silently.

## Null Safety And Validation

- Validate public inputs at API boundaries.
- Use `Objects.requireNonNull` for internal preconditions when it clarifies a non-null contract.
- Be explicit about nullable behavior instead of relying on caller assumptions.

## Concurrency and Resources

- Favor stateless services unless shared state is required.
- Use `CompletableFuture` and executors deliberately.
- Avoid unbounded thread creation.
- Close I/O resources with try-with-resources.
- Keep blocking operations out of hot paths.
- Document thread-safety assumptions on shared components.

## Clean Code Structure

- Keep each class responsible for one core concern.
- Remove unused imports and dead code.
- Prefer private helpers after the public API when that matches the repository style.
- Extract meaningful magic values into named constants.
- Keep parameter lists short; bundle related inputs into a request type or record when appropriate.

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

## Review Order

1. Does the code behave correctly?
2. Is the public API clear and type-safe?
3. Are naming and data flow explicit?
4. Is state handled safely?
5. Are null handling and exceptions explicit?
6. Is the class doing too much?
7. Is there a simpler Java 21 or standard-library approach that improves readability?

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

### Modernize Java Code

Modernize this Java code while preserving behavior.
Requirements:
- prefer clearer Java 21 language features only when they simplify the code
- tighten vague or raw types
- improve immutability where framework constraints allow
- keep public APIs explicit and readable
- avoid framework-breaking refactors for JPA or persistence models
