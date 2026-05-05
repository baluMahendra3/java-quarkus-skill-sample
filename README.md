# Java + Quarkus Sample Project

This is a starter Quarkus project with a hello endpoint and test.

## Prerequisites

- Java 21+
- Maven 3.9+

## Run in Dev Mode

```bash
mvn quarkus:dev
```

Dev mode uses the `%dev` profile with an in-memory H2 database, local CORS origins for common frontend ports, and Swagger UI enabled.

Then open:

- `http://localhost:8080/hello`

## Runtime Configuration

Non-dev startup requires environment-injected database and CORS settings:

- `TRAVEL_DB_USERNAME`
- `TRAVEL_DB_PASSWORD`
- `TRAVEL_DB_JDBC_URL`
- `TRAVEL_CORS_ORIGINS`

Example PowerShell session:

```powershell
$env:TRAVEL_DB_USERNAME = "travel_user"
$env:TRAVEL_DB_PASSWORD = "change-me"
$env:TRAVEL_DB_JDBC_URL = "jdbc:postgresql://localhost:5432/travel_db"
$env:TRAVEL_CORS_ORIGINS = "https://app.example.com"
mvn quarkus:dev
```

## Run Tests

```bash
mvn test
```

## Build Fast-JAR

```bash
mvn clean package
```

Output artifact:

- `target/quarkus-app/quarkus-run.jar`

## Native Build (optional)

```bash
mvn clean package -Dnative
```

## Skill Docs

See `.github/copilot-skills/quarkus-skill.md` for Quarkus-specific framework guidance and review prompts.
See `.github/copilot-skills/java-skill.md` for a framework-agnostic Java coding and review playbook.
See `.github/copilot-skills/data-structures-algorithms-skill.md` for collection choice, complexity, and scalable in-memory processing guidance.
See `.github/copilot-skills/lombok-skill.md` for Lombok usage guidance, annotation selection, and entity safety rules.
See `.github/copilot-skills/maven-skill.md` for Maven build, dependency, and CI guidance.
See `.github/copilot-skills/swagger-skill.md` for Swagger/OpenAPI contract and documentation guidance.
See `.github/copilot-skills/exception-handling-skill.md` for validation, business-rule, technical-failure, and safe client error-response guidance.
See `.github/copilot-skills/dynamodb-skill.md` for AWS DynamoDB database design and operational best practices.
See `.github/copilot-skills/logger-skill.md` for structured logging, redaction, and observability guidance.
See `.github/copilot-skills/code-design-principles-skill.md` for SOLID-based modular design and maintainable code structure guidance.
See `.github/copilot-skills/concurrency-thread-safety-skill.md` for thread safety, async patterns, and shared-state reliability guidance.
See `.github/copilot-skills/performance-high-volume-skill.md` for latency, throughput, batching, pagination, and high-volume API guidance.
See `.github/copilot-skills/security-skill.md` for secure API, IAM, secrets, and data protection guidance.
See `.github/copilot-skills/testing-skill.md` for unit and integration testing guidelines.
See `.github/copilot-skills/architecture-skill.md` for layered architecture rules across API, service, and repository layers.
See `.github/copilot-skills/skill-compliance-gate-skill.md` for the final skill-based review and reporting workflow.
See `.github/copilot-skills/combined-workflow-skill.md` for using all skills together in one delivery flow.

## Automated Skill Check

Run the VS Code task `Skill Compliance Check` or execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Invoke-SkillComplianceCheck.ps1
```

To apply low-risk mechanical fixes automatically after review, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Invoke-SkillComplianceCheck.ps1 -AutoFix
```

The script writes a report to `target/skill-compliance-report.md` and includes the repository name automatically from the repo folder.
It also writes an auto-fix preview to `target/skill-compliance-autofix-preview.md` so users can see the low-risk changes the gate would apply.
Coverage is verified automatically during the repo-standard `verify` flow with JaCoCo, using a default 95% line coverage threshold.
It can automatically detect some mechanical issues and run tests, but it cannot replace human judgment for full security, concurrency, and performance validation.

## How To Inspect The Skill System

The reusable pieces are separated so you can see what is generic and what is repo-specific:

- Skills: `.github/copilot-skills/`
- Repo instructions and skill routing: `.github/copilot-instructions.md`
- Reusable compliance script logic: `scripts/Invoke-SkillComplianceCheck.ps1`
- Optional VS Code task wrapper: `.vscode/tasks.json`
- Latest report: `target/skill-compliance-report.md`
- HTML coverage report: `target/site/jacoco/index.html`

The report now includes the repository name, the skills listed as checked, and the effective automation parameters used for the run.
The preview file is the user-facing difference list for safe automated fixes.
When coverage falls below 95%, the compliance report also lists the lowest-covered classes that need new JUnit coverage.

## Reuse In Other Repositories

This setup is suitable for other Java or Quarkus service repositories with similar layered architecture expectations.

Copy these files first:

- `.github/copilot-instructions.md`
- `.github/copilot-skills/`
- `scripts/Invoke-SkillComplianceCheck.ps1`
- `.vscode/tasks.json` if you want the same VS Code task

Then only change the script invocation if the new repository differs from the defaults:

- `-RepositoryName`
- `-MainSourceRoot` and `-TestSourceRoot`
- `-ExcludeModules`
- `-PaginationParameterNames`
- `-BuildCommand`
- `-ReportPath`
- `-PreviewPath`
- `-AutoFix`

If a repo uses a different stack or different naming rules, pass those values as parameters first and only change the script if you need a genuinely new generic check.

## Copy-Ready Template Pack

A self-contained copy-ready pack now exists under `templates/reusable-skill-pack/`.

Use that folder when you want to seed a brand-new repository with:

- generic Copilot instructions
- the reusable skill files
- the preview plus auto-fix capable compliance script
- no extra repo-specific config file
- the optional VS Code task wrapper

The template pack is meant for copying into another repo root and then customizing there, while the live files at the top level of this repo remain the active configuration for this project.

