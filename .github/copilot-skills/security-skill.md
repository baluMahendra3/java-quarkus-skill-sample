# Security Skill

Use this checklist for secure-by-default Java + Quarkus APIs running on AWS.

## Scope

- Focus: authentication, authorization, input validation, secrets handling, data protection, and operational security.
- Pair with Architecture, Quarkus, Logger, Testing, and Maven skills for full implementation coverage.

## Defaults

- Deny by default and grant least privilege.
- Validate all untrusted input at system boundaries.
- Keep secrets out of source code and logs.
- Use TLS for all network communication.
- Keep dependencies patched and versions controlled.
- Apply Bean Validation plus semantic validation at resource boundaries.
- Prefer framework security features over custom ad hoc auth logic.

## Identity and Access Rules

- Require authentication for protected endpoints.
- Enforce authorization at API boundary and sensitive business operations.
- Use role or scope checks with explicit policy mapping.
- Keep IAM permissions minimal and resource-scoped.
- Separate machine-to-machine and end-user permissions.
- Fail closed when authentication or authorization context is missing.
- Protect admin, financial, and cross-tenant operations with explicit authorization checks.
- JWT or token-based auth must validate issuer, audience, expiry, and signature.

## API Security Rules

- Validate request payloads with strict schemas and constraints.
- Sanitize path, query, and header values.
- Apply safe CORS configuration for allowed origins and methods.
- Add rate limiting and abuse protections for public endpoints.
- Return safe error messages without exposing internal details.
- Use `@Valid` and field constraints for request DTOs rather than manual null or blank checks where possible.
- Never trust client-supplied identifiers, roles, or ownership fields without server-side verification.
- Avoid wildcard CORS origins or headers in production defaults.
- Authentication endpoints should defend against brute-force and credential-stuffing patterns.

## Data Protection Rules

- Encrypt data in transit and at rest.
- Classify and minimize stored sensitive data.
- Redact PII, tokens, and secrets from logs and traces.
- Use tokenization or hashing where raw values are not required.
- Define retention and deletion policies for regulated data.
- Passwords must be salted and hashed with a modern password hashing function; never stored or compared in plain text.
- Avoid returning sensitive internal identifiers when stable public identifiers are sufficient.

## Secrets and Configuration Rules

- Load secrets from secure secret stores or environment injection.
- Rotate secrets regularly and on incident response.
- Never commit credentials, keys, or certificates to repositories.
- Keep environment-specific security settings in Quarkus profiles.
- Fail startup when required security config is missing.
- Security-sensitive defaults must be secure even in non-production profiles unless explicitly documented as local-only.
- Local development shortcuts must never silently carry into test or production profiles.

## AWS and Infrastructure Rules

- Use IAM roles instead of static long-lived keys.
- Restrict DynamoDB and other resource actions to required operations.
- Enable audit logs and monitoring for access anomalies.
- Use private networking and VPC endpoints when required.
- Apply infrastructure guardrails and policy-as-code checks.
- Scope access by environment, tenant, table, bucket, or queue where possible rather than broad `*` resource grants.

## Dependency and Supply Chain Rules

- Pin dependencies and plugin versions in build files.
- Scan dependencies for known vulnerabilities in CI.
- Remove unused dependencies to reduce attack surface.
- Verify artifact provenance and trusted repositories.
- Track and remediate CVEs by severity and exploitability.
- Do not add security libraries or crypto code when a maintained framework feature already exists.

## Incident and Observability Rules

- Log authentication failures, authorization denials, and suspicious patterns.
- Monitor error spikes, latency anomalies, and throttle events.
- Alert on repeated failed access attempts and privilege misuse.
- Preserve correlation IDs for forensic investigation.
- Maintain runbooks for containment, recovery, and postmortems.

## Forbidden Patterns

- Do not hardcode secrets, JWT signing keys, API keys, or passwords.
- Do not expose stack traces, SQL errors, or internal exception details to API consumers.
- Do not trust security decisions based only on client input.
- Do not use broad allow-all CORS settings outside explicitly local-only development.
- Do not log credentials, tokens, or security headers.

## Testing and Quality Gate

- Add negative tests for auth, authz, validation, and error handling.
- Verify protected endpoints reject unauthorized requests.
- Confirm sensitive values never appear in logs or responses.
- Include dependency vulnerability checks in CI quality gates.
- Block release when critical security findings are unresolved.
- Test expired, invalid, and insufficient-scope token scenarios.
- Review startup configuration to confirm missing required security settings fail fast.

## Reusable Prompts

### Generate Security Plan

Create a security plan for `<feature>`.
Requirements:
- authentication and authorization model
- input validation and abuse protection strategy
- secrets/configuration handling
- data protection and logging redaction rules
- CI security checks and release gates

### Review Security Posture

Review this implementation for:
- auth/authz gaps
- input validation weaknesses
- secrets or sensitive data exposure risks
- IAM and infrastructure misconfiguration risks
- missing security tests and CI controls
Return findings ordered by severity with concrete fixes.
