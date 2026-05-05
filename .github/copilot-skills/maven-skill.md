# Maven Skill

Use this checklist when building and maintaining Java projects with Maven.

## Scope

- Focus: build lifecycle, dependency management, plugin governance, and CI reproducibility.
- Pair with Java or Quarkus skills for language and framework guidance.

## Defaults

- Maven 3.9+
- Java 21 toolchain
- Pinned plugin versions for reproducibility
- Standard lifecycle: `validate`, `test`, `package`, `verify`
- `verify` is the minimum pre-merge gate unless the repository defines a stricter command.
- Coverage reporting belongs in the build flow, not in a manual side script disconnected from `verify`.

## Project Rules

- Keep source in `src/main/java` and tests in `src/test/java`.
- Manage versions in `pom.xml` properties for consistency.
- Use dependency scopes intentionally: `compile`, `provided`, `runtime`, `test`.
- Keep parent POM and module boundaries explicit for multi-module projects.
- Prefer one authoritative place for shared version numbers.
- Keep build behavior deterministic across developer machines and CI.

## Dependency Management

- Prefer BOM imports when frameworks provide them.
- Avoid version drift by centralizing versions in `<dependencyManagement>`.
- Exclude conflicting transitive dependencies explicitly.
- Run `mvn -q dependency:tree` to audit unexpected dependencies.
- Reject `LATEST`, `RELEASE`, floating ranges, and unpinned plugin versions.
- Remove redundant direct dependencies when they are already managed and unused.

## Plugin and Lifecycle Rules

- Pin plugin versions in `<build><pluginManagement>`.
- Configure `maven-compiler-plugin` for release target.
- Use `maven-surefire-plugin` for unit tests and `maven-failsafe-plugin` for integration tests.
- When JaCoCo `prepare-agent` is used, make Surefire or Failsafe pass the generated coverage `argLine` into the test JVM explicitly.
- Keep build profiles minimal and documented.
- Prefer `maven-enforcer-plugin` for baseline rules such as Java and Maven version expectations.
- Ensure JaCoCo or equivalent report generation is deterministic and available to the compliance gate.
- Profiles must not silently change production semantics; keep them narrow and intentional.

## CI and Release Rules

- CI must run the same effective build command that contributors use locally for merge readiness.
- Release builds should avoid SNAPSHOT dependencies unless the repository explicitly allows them.
- Keep repository declarations trusted and minimal; do not add ad hoc public repos without justification.
- Separate unit-test and integration-test phases only when the repository actually benefits from that split.

## Forbidden Patterns

- Do not leave plugin versions implicit when they affect compilation, testing, packaging, or coverage.
- Do not assume JaCoCo coverage agent arguments will be propagated implicitly; wire the generated `argLine` or equivalent property into the test plugin configuration explicitly.
- Do not put secrets or repository credentials in `pom.xml`.
- Do not create overlapping profiles that make the effective build hard to reason about.
- Do not bypass `verify` with weaker local commands when claiming merge readiness.

## Quality Gate

- `mvn -q -DskipTests=false test` passes locally.
- `mvn -q verify` passes before merge.
- Coverage verification should run as part of `verify`, not as a separate optional step.
- Coverage reports must come from the same Maven run that executed the tests; touched classes should not remain stale or uncovered because the coverage agent was not attached.
- No `LATEST` or `RELEASE` dependency versions.
- No duplicate dependency declarations.
- CI uses the same JDK and Maven major versions as local development.
- The repository's documented compliance or release command is reflected in Maven configuration and CI.
- Dependency hygiene review covers convergence, duplicates, and unused or vulnerable dependencies.

## Performance and CI Checklist

- Use local repository caching in CI.
- Keep plugin executions scoped to required phases only.
- Skip optional heavy tasks unless needed (for example, with profiles).
- Use parallel builds carefully: `mvn -T 1C test` when module isolation is safe.

## Security Checklist

- Scan dependencies regularly.
- Remove unused dependencies.
- Pin transitive versions for known CVEs until upstream resolves.
- Do not store credentials in `pom.xml`; use settings and environment variables.

## Reusable Prompts

### Generate Maven Build Setup

Create a Maven configuration for a Java 21 project.
Requirements:
- pinned plugin versions
- dependencyManagement section
- surefire and failsafe setup
- profile for integration tests
- clear properties for tool versions

### Review POM

Review this `pom.xml` for:
- dependency conflicts and version drift
- plugin version pinning
- test lifecycle correctness
- CI reproducibility
- security risks in dependencies
Return findings ordered by severity with concrete fixes.
