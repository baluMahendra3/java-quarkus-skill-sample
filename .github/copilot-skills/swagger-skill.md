# Swagger / OpenAPI Skill

Use this checklist when designing and maintaining Swagger/OpenAPI contracts for REST APIs.

## Scope

- Focus: API contract quality, documentation clarity, consistency, and consumer safety.
- Pair with Java, Maven, and Quarkus skills for implementation and build guidance.

## Defaults

- OpenAPI 3.0 or 3.1
- JSON as primary media type unless explicitly required otherwise
- Reusable schemas and responses in `components`
- Stable, predictable operation IDs and tags
- One shared error response model for API-wide consistency
- Pagination, filtering, and sorting documented explicitly on list endpoints

## Contract Design Rules

- Define one clear responsibility per endpoint.
- Use nouns for resources and verbs only when action endpoints are required.
- Keep paths consistent (`/api/v1/<resource>` style when versioning in path).
- Specify request and response schemas for every operation.
- Always document error responses (`200`,`400`, `401`, `403`, `500` as applicable).
- Use unique, stable `operationId` values that map cleanly to business actions.
- Document `404`, `409`, `422`, and `204` where the behavior applies rather than collapsing everything into generic `400` and `500` responses.
- Keep response codes aligned with actual implementation behavior, not aspirational design.

## Schema Rules

- Use explicit types, formats, and nullable behavior.
- Mark required fields accurately; avoid overusing optional fields.
- Add field descriptions and realistic examples.
- Reuse shared models with `$ref` to avoid duplication.
- Define enums and constraints (`minLength`, `maxLength`, `pattern`, ranges) where relevant.
- Reflect Bean Validation constraints in the documented schema whenever the implementation enforces them.
- Hide internal-only fields and implementation artifacts from public contracts.
- Keep request DTOs and response DTOs distinct when write and read semantics differ.

## Security and Versioning

- Document auth schemes in `components.securitySchemes`.
- Apply security requirements globally, then override only when needed.
- Treat contract changes as versioned changes; avoid breaking existing clients.
- Mark deprecated endpoints/fields explicitly with migration notes.
- Mark public or anonymous endpoints explicitly when they override global security.
- Do not weaken documented security requirements to match temporary implementation shortcuts.

## Error and Pagination Rules

- Use one consistent error payload shape across endpoints.
- Document validation errors with example payloads when clients need field-level handling.
- List endpoints must document pagination inputs and the shape of paged results.
- If sorting or filtering is supported, document parameter names, allowed values, and defaults.

## Forbidden Patterns

- Do not publish undocumented fields that are returned by accident from entities or internal models.
- Do not leave request or response bodies as vague `object` schemas when a concrete model exists.
- Do not omit security documentation for protected endpoints.
- Do not change `operationId`, enum values, or response shapes casually once clients rely on them.

## Quality Gate

- OpenAPI document validates with no schema errors.
- All operations include summaries, tags, and response definitions.
- Error model is consistent across endpoints.
- Examples are present for key requests and responses.
- Contract matches actual implementation behavior.
- Protected endpoints show security requirements explicitly or inherit them consistently.
- List endpoints expose documented pagination behavior, not hidden ad hoc query parameters.

## Review Checklist

- Are status codes semantically correct?
- Are request/response models complete and consistent?
- Are pagination, filtering, and sorting parameters documented where needed?
- Are auth requirements clear per endpoint?
- Are breaking changes called out and versioned?

## Reusable Prompts

### Generate OpenAPI Spec

Create an OpenAPI specification for `<resource>`.
Requirements:
- versioned path and operation tags
- request/response schemas with examples
- standard error model
- security scheme and per-operation security
- pagination parameters for list endpoints

### Review OpenAPI Spec

Review this OpenAPI document for:
- correctness and completeness
- schema consistency and reuse opportunities
- status code and error model quality
- security documentation gaps
- breaking-change risks
Return findings ordered by severity with concrete fixes.
