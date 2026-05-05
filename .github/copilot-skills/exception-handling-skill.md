# Exception Handling Skill

Use this checklist when designing, reviewing, or refactoring exception handling in Java and Quarkus services.

## Scope

- Focus: exception taxonomy, user-safe error messages, validation failures, business rule failures, technical failures, and consistent boundary error responses.
- Pair with Java, Quarkus, Security, Logger, Testing, and Architecture skills for implementation details.

## Goal

- Separate business and validation failures from technical or infrastructure failures.
- Return clear, safe, actionable error messages to API consumers.
- Keep internal implementation details, stack traces, and sensitive data out of client-facing responses.
- Make exception handling predictable across modules and services.

## Important Clarification

- Do not confuse checked vs unchecked exceptions with business vs technical exceptions.
- Checked and unchecked are Java language mechanisms.
- Business, validation, and technical exceptions are application design categories.
- In service code, prefer a domain-oriented exception taxonomy over arbitrary checked-exception usage.

## Exception Categories

### Validation Exceptions

- Use for invalid external input, malformed values, or boundary contract violations.
- Return clear messages that help the user fix the request.
- Prefer HTTP `400` with field-level details when applicable.
- Bean Validation failures belong here.

### Business Rule Exceptions

- Use when input is syntactically valid but violates a domain rule.
- Examples: duplicate registration, invalid status transition, inactive account, unsupported operation for current state.
- Return explicit, user-safe messages that explain the rule failure.
- Use typed domain exceptions instead of generic `RuntimeException`.

### Technical Exceptions

- Use for infrastructure, persistence, network, serialization, configuration, timeout, or third-party failures.
- Do not expose raw exception messages to end users if they reveal internals or are not actionable.
- Translate them to safe boundary messages and log the root cause with correlation context.
- Prefer stable generic responses like `500`, `503`, or `504` depending on the failure mode.

### Unexpected Exceptions

- Treat any uncategorized exception as an internal error.
- Return a generic safe message.
- Preserve diagnostic detail only in logs or telemetry.

## Design Rules

- Use typed exceptions for expected business and validation failures.
- Keep exception meaning aligned to a single use case or rule.
- Do not throw generic `Exception`, `RuntimeException`, or `Throwable` for normal business flows.
- Keep exception creation close to the point where the rule is known.
- Translate lower-level technical exceptions at clear boundaries rather than leaking them upward unchanged.

## Boundary Response Rules

- Map exceptions to a consistent API error response shape.
- Include a stable top-level message, status code, timestamp, path, and optional details list.
- Keep user-facing messages concise and actionable.
- Do not return stack traces, SQL errors, class names, or secret values.
- Include field-level detail only when it helps the caller fix the request safely.

## Message Rules

- Validation messages should say what is wrong and how to fix it.
- Business-rule messages should explain the violated rule, not the internal implementation.
- Technical messages returned to users should stay generic and safe.
- Internal logs may contain richer context, but client responses must not.
- Keep wording consistent across endpoints for the same failure type.

## Layer Rules

- Resource layer: translate exceptions to HTTP responses only.
- Service layer: raise business and validation exceptions with meaningful rule-specific messages.
- Repository and infrastructure layer: throw or wrap technical exceptions, not business exceptions.
- Shared mappers or exception translators should own the final response shape.

## Quarkus and JAX-RS Rules

- Use dedicated `ExceptionMapper` classes for consistent boundary translation.
- Keep Bean Validation failures mapped separately when framework defaults would otherwise change the response shape.
- Prefer one shared error response contract across the API.
- Avoid duplicating try-catch blocks in every resource method.

## Security Rules

- Never expose authentication, authorization, token, database, filesystem, or network internals in client responses.
- Avoid leaking whether a sensitive identifier exists unless the business flow requires it.
- Redact secrets and personal data from exception logs.

## Logging Rules

- Log technical root causes with correlation IDs when available.
- Avoid logging expected validation noise at high severity.
- Use warning level for handled business failures when operationally useful.
- Use error level for unexpected or technical failures that need investigation.

## Anti-Patterns

- Throwing generic runtime exceptions for business rules.
- Returning raw exception messages from persistence or framework layers to clients.
- Catching `Exception` broadly and swallowing root causes.
- Encoding error meaning only in strings without typed exception categories.
- Duplicating HTTP error translation logic across resources.
- Using exceptions for ordinary branching where a normal result type is clearer.

## Quality Gate

- Validation, business, and technical failures are distinguished clearly.
- Client-facing error responses are safe and consistent.
- No raw internal exception text leaks to API consumers.
- Error mapping is centralized and covered by tests.
- Important business-rule failures have explicit tests for both status and message shape.

## Reusable Prompts

### Design Exception Handling

Design exception handling for `<feature>`.
Requirements:
- classify validation, business, and technical failures
- define typed exceptions where needed
- map them to safe HTTP responses
- avoid leaking internals to users
- include tests for error status and payload shape

### Review Exception Handling

Review this code for exception-handling issues:
- wrong exception taxonomy
- leaked internal messages
- inconsistent boundary mapping
- missing business-rule exceptions
- weak validation error feedback
Return findings ordered by severity with concrete fixes.