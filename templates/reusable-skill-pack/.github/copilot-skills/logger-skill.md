# Logging and Observability Skill

Use this checklist for structured, safe, and actionable logging in Java + Quarkus services.

## Scope

- Focus: log design, correlation, error diagnostics, security redaction, and operations readiness.
- Pair with Java, Quarkus, Architecture, and Testing skills for implementation and validation.

## Defaults

- SLF4J API with Quarkus logging backend.
- Structured logs (JSON in non-local environments).
- Correlation ID propagated across request lifecycle.
- UTC timestamps and consistent service/environment metadata.
- INFO for business milestones, DEBUG for local diagnosis only.
- One primary log event per meaningful state transition, not per line of code.
- Log once at the owning boundary; avoid duplicate logs across resource, service, and mapper layers.

## Log Design Rules

- Log events, not noise; each log line should answer an operational question.
- Use stable event names and key fields (for example: `event`, `entityId`, `status`).
- Keep message templates consistent to improve search and alert accuracy.
- Include contextual identifiers: requestId, correlationId, principal, and module.
- Prefer parameterized logging to avoid unnecessary string allocations.
- Put high-cardinality values in structured fields, not in ad hoc free-text messages.
- Prefer machine-searchable field names over prose-heavy messages.
- Treat logs as an operational contract; avoid renaming key event names casually.

## Level and Volume Rules

- ERROR: failed operation requiring investigation or user impact.
- WARN: unexpected but recoverable condition.
- INFO: state transitions and major business flow checkpoints.
- DEBUG/TRACE: temporary diagnostics and local troubleshooting.
- Do not log inside tight loops without rate limits or sampling.
- Do not use INFO for request-by-request noise on high-volume endpoints.
- Do not emit the same warning repeatedly for the same recoverable condition without suppression or aggregation.

## Boundary and Ownership Rules

- Resource layers log request start or completion only when it adds operational value, not by default for every endpoint.
- Service layers log business-significant transitions, external dependency failures, and unexpected control flow.
- Repository or low-level helper layers should avoid routine success logs.
- Background jobs and async handlers must include job, batch, or message identifiers in every meaningful log.
- Correlation context must be created at ingress, propagated across async boundaries, and cleared correctly after request completion.

## API End-To-End Minimum

- Every `/api` resource class should declare a logger.
- Every `/api` resource class should emit at least one INFO, WARN, or ERROR event that helps operators trace request lifecycle or failure outcome.
- Every service class under a `service` package should declare a logger.
- Every service class under a `service` package should emit at least one INFO, WARN, or ERROR event for a business-significant transition or failure path.
- For create, update, delete, auth, admin, and expensive read flows, prefer start and completion events with stable fields such as `event`, `requestId`, `principal`, `entityId`, `outcome`, and `durationMs`.
- Do not log raw request DTOs, response DTOs, passwords, bearer tokens, cookies, or authorization headers to satisfy these requirements.
- When a resource delegates to a service, avoid duplicate success logs with the same meaning; choose the boundary that best answers the operational question.
- Failure logs should include safe identifiers and the operation name so support staff can reconstruct the path without reading stack traces first.

## Security and Compliance Rules

- Never log secrets, passwords, tokens, API keys, or private credentials.
- Redact or hash PII and sensitive identifiers.
- Avoid full request/response body logging in production by default.
- Sanitize user-controlled strings to prevent log injection.
- Enforce retention and access controls per environment policy.
- Treat bearer tokens, authorization headers, cookies, and session identifiers as secrets.
- Avoid logging raw validation payloads when field-level errors are sufficient.

## Quarkus and AWS Rules

- Keep logging configuration centralized in `application.properties` profiles.
- Set production log format for CloudWatch queryability.
- Ensure correlation IDs are available at resource and service boundaries.
- Log DynamoDB retries, throttling, and conditional write conflicts with context.
- Keep AWS SDK wire logging disabled in production unless incident-scoped.
- Prefer MDC or equivalent request context propagation over manual string concatenation.
- Keep dev and test logging verbose enough for diagnosis, but production logging minimal and structured.

## Exception Logging Rules

- Log exceptions once at the correct boundary.
- Include stack traces for server errors, not for expected validation failures.
- Map internal exceptions to safe API error payloads.
- Capture root cause fields without duplicating the same exception across layers.
- Ensure typed domain exceptions include actionable messages.
- Do not log and rethrow the same exception in multiple layers.
- Validation, business-rule, and authorization failures should usually be counted and classified, not logged as ERROR stack traces.

## Forbidden Patterns

- Do not log secrets, credentials, JWTs, or full security headers.
- Do not concatenate untrusted raw input directly into log messages.
- Do not add temporary DEBUG logs without removing or guarding them before closure.
- Do not log success messages from repositories or simple getters or mappers.
- Do not use inconsistent event names for the same workflow.

## Logger Anti-Patterns

- Do not log raw request or response bodies by default, especially on auth or user-facing endpoints.
- Do not treat INFO as a trace stream for every request on high-volume APIs.
- Do not log the same exception in multiple layers and then rethrow it.
- Do not log stack traces for expected validation, authorization, or business-rule failures.
- Do not declare a logger in a resource or service class and then leave it unused.
- Do not rely on free-text prose when stable fields such as `event`, `principal`, `entityId`, `outcome`, and `durationMs` are what operators need.
- Do not manually concatenate strings when parameterized logging can preserve structure and avoid unnecessary allocations.
- Do not log inside loops, polling paths, retries, or hot code paths without suppression, aggregation, or sampling.
- Do not add routine success logs in repositories, mappers, or trivial helper methods.
- Do not duplicate start and completion logs at both the resource and service boundary unless each one answers a different operational question.
- Do not rename key event names casually after dashboards, searches, or alerts depend on them.
- Do not ship production code without meaningful INFO, WARN, or ERROR events on business-significant API flows.
- Do not use logs as a substitute for metrics, tracing, or safe API error payloads.

## Testing and Quality Gate

- Critical failure paths assert expected log levels and key fields.
- Correlation ID presence is validated in API integration tests.
- No sensitive fields appear in logs under test fixtures.
- Log format remains parseable by downstream tooling.
- Observability dashboards and alerts are mapped to key events.
- New features define which events are INFO, WARN, and ERROR before implementation is considered complete.
- Log review rejects duplicate exception logging and noisy request-level INFO spam.
- The skill compliance script currently enforces logger presence plus at least one operational INFO, WARN, or ERROR event in `/api` resources and `service` classes.
- The skill compliance script does not prove event usefulness, structured field quality, correlation propagation, or duplication quality; review those manually.

## Reusable Prompts

### Generate Logging Plan

Create a logging plan for `<feature>`.
Requirements:
- event catalog with levels and required fields
- correlation and request tracing strategy
- redaction rules for sensitive attributes
- Quarkus profile-specific logging config
- CloudWatch search and alert examples

### Review Logging Quality

Review this implementation for:
- over-logging or missing critical events
- inconsistent levels and event names
- missing correlation IDs
- sensitive data leakage risks
- weak diagnostics for incident response
Return findings ordered by severity with concrete fixes.
