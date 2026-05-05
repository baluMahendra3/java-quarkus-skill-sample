# Copilot Instructions

Apply these repository instructions by default when working in this project.

## General

- Treat the markdown files under `.github/copilot-skills/` as the primary repo-specific guidance.
- Before making changes, read the skill file that matches the task area and follow it.
- Prefer the smallest change that satisfies the request and preserves the existing project structure.
- After code changes, perform a final compliance pass against the relevant skills before closing the task.
- Run `scripts/Invoke-SkillComplianceCheck.ps1` for an automated best-effort check, then fix low-risk issues and explicitly report remaining gaps.
- If something cannot be implemented or auto-fixed safely, explain why and recommend a better approach.

## Required Testing Workflow

- For any testing-related task, do not make the first edit until the required skill files have been read.
- The minimum required file for any test work is `.github/copilot-skills/testing-skill.md`.
- If the tests touch Quarkus resources, request handling, CDI, profiles, or framework behavior, also read `.github/copilot-skills/quarkus-skill.md` before editing.
- If the tests touch Java models, helpers, DTOs, entities, naming, or maintainability concerns, also read `.github/copilot-skills/java-skill.md` before editing.
- If the task involves Maven commands, test execution strategy, plugin behavior, or dependency changes, also read `.github/copilot-skills/maven-skill.md` before editing.
- For testing work that spans multiple areas, read all relevant skill files first and only then start code changes.

## Skill Routing

- For JUnit, integration, API, or test coverage work, read `.github/copilot-skills/testing-skill.md` first.
- For Quarkus resources, CDI, configuration, profiles, and framework behavior, read `.github/copilot-skills/quarkus-skill.md` first.
- For Java code structure, naming, DTO/entity patterns, and maintainability, read `.github/copilot-skills/java-skill.md` first.
- For collection choice, algorithmic complexity, in-memory processing, or Java hot-path scaling, read `.github/copilot-skills/data-structures-algorithms-skill.md`.
- For Lombok adoption, annotation choice, DTO boilerplate reduction, or Lombok review work, read `.github/copilot-skills/lombok-skill.md`.
- For Maven commands, dependencies, plugins, build lifecycle, and test execution, read `.github/copilot-skills/maven-skill.md` first.
- For API contracts and OpenAPI/Swagger changes, read `.github/copilot-skills/swagger-skill.md`.
- For exception taxonomy, user-safe error messages, validation failures, or boundary error mapping, read `.github/copilot-skills/exception-handling-skill.md`.
- For authentication, authorization, JWT, secrets, and secure defaults, read `.github/copilot-skills/security-skill.md`.
- For architecture or layering decisions, read `.github/copilot-skills/architecture-skill.md` and `.github/copilot-skills/code-design-principles-skill.md`.
- For logging and observability changes, read `.github/copilot-skills/logger-skill.md`.
- For concurrency or shared-state behavior, read `.github/copilot-skills/concurrency-thread-safety-skill.md`.
- For performance, fast-response APIs, higher-volume processing, pagination, batching, caching, or latency-sensitive work, read `.github/copilot-skills/performance-high-volume-skill.md`.
- For DynamoDB-related work, read `.github/copilot-skills/dynamodb-skill.md`.
- For final cross-skill validation and reporting after changes, read `.github/copilot-skills/skill-compliance-gate-skill.md`.
- If a task spans multiple areas, read the relevant skill files before editing.

## For This Repository

- This is a Java 21 Quarkus Maven project.
- Prefer Quarkus tests for endpoint behavior and focused unit tests for simple model and helper classes.
- Keep test setup deterministic and avoid depending on external local services when a test profile or mock is sufficient.
