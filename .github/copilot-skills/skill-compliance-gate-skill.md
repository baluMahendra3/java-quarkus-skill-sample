- Logger skill: obvious sensitive-data logging patterns in Java, non-dev SQL logging checks, logger presence in `/api` resources and `service` classes, and at least one INFO, WARN, or ERROR operational event in those classes.
- Logger skill: correlation IDs, structured logging shape, event catalog quality, field usefulness, and duplication across layers are not fully enforced by this script.
# Skill Compliance Gate Skill

Use this workflow as the final pass after any code change.

## How This Gate Works

- This skill file defines the final-pass workflow and reporting expectations.
- The actual automated checks are executed by `scripts/Invoke-SkillComplianceCheck.ps1`.
- The VS Code entry point is the `Skill Compliance Check` task in `.vscode/tasks.json`.
- Repo-level automatic routing comes from `.github/copilot-instructions.md`, which tells the agent to read this skill and run the script.
- The Markdown report includes the repository name and is written to `target/skill-compliance-report.md` by default, or to a path passed through script parameters.

## Goal

- Verify the implementation against the relevant repo skills.
- Show users the low-risk differences that the gate can apply automatically.
- Fix low-risk mechanical violations automatically when possible.
- Explicitly report what could not be auto-fixed and why.
- Recommend a better approach when the current implementation is acceptable but not ideal.

## What Can Be Automated Reliably

- Build and test execution.
- Static pattern checks for common architecture drift.
- Package-structure checks for expected module folders such as `resource`, `service`, `repository`, `entity`, `dto`, and `mapper`.
- Static pattern checks for DTO boundary leakage in resources.
- Static pattern checks for risky Lombok usage on entities.
- Static pattern checks for direct persistence calls from resources.
- Basic detection of unbounded list endpoints and obvious high-volume risks.

## Current Coverage Compared To All Skills

### Currently Enforced By The Script

- Architecture skill: module package structure and direct resource-to-persistence drift checks.
- Quarkus skill: bounded list endpoint checks and build or test execution.
- Java skill: partial support through boundary and layering checks, but not full design review.
- Testing skill: build-integrated JaCoCo coverage verification with a 95% line coverage threshold.
- Exception Handling skill: no generic `RuntimeException` or `Exception` throws in main code, no raw `exception.getMessage()` leakage in exception mappers, and presence checks for shared exception mappers.
- Security skill: JWT verification config presence, non-wildcard base CORS checks, non-dev secret placeholder checks in `application.properties`, and required authorization annotations on `/api` resource endpoints.
- Logger skill: obvious sensitive-data logging patterns in Java and non-dev SQL logging checks.
- Swagger skill: OpenAPI dependency presence, OpenAPI metadata presence, shared security-scheme presence, class or endpoint-level OpenAPI annotations across `/api` resources, pagination parameter documentation on list endpoints, and rejection of vague object response schemas where concrete models should exist.
- Maven skill: no `LATEST` or `RELEASE` versions or version ranges, explicit build-plugin versions, and required Surefire or JaCoCo plugin presence.
- Maven skill: build-integrated coverage must be wired into the actual test runner, but the script does not yet fully validate JaCoCo `argLine` propagation into Surefire or Failsafe.
- Lombok skill: risky `@Data` usage on entities.
- Performance and High-Volume skill: basic detection of unbounded list endpoints.
- Testing skill: test execution when the script is run without `-SkipTests`.

### Not Fully Automated Today

- Code Design Principles skill: cohesion, SOLID, contract clarity, and cyclic-dependency review still need human judgment.
- Exception Handling skill: deeper business vs technical exception taxonomy, error-code design, and safe logging boundaries still need human judgment.
- Security skill: authz completeness, validation depth, abuse protection, and dependency vulnerability review are still not fully verified by this script.
- Logger skill: correlation IDs, structured logging shape, event catalog quality, and duplication across layers are not statically enforced by this script.
- Swagger skill: examples, response-schema quality, and implementation-to-spec drift are not fully checked here.
- Maven skill: dependency duplication, unused dependency cleanup, and full CI parity still need human review unless the configured command checks them explicitly.
- Maven skill: JaCoCo agent propagation into the test JVM still needs human review; plugin presence alone is not enough if coverage wiring is missing.
- Concurrency and Thread Safety skill: runtime interleavings, blocking behavior, and executor safety require design or load review.
- DynamoDB skill: access-pattern validation and table design checks are out of scope for this repository script.
- Combined Workflow skill: it is orchestration guidance, not a separately executable static rule set.

## What Cannot Be Guaranteed Automatically

- Full semantic correctness of business rules.
- Whether authorization rules are complete for every use case.
- Whether performance is sufficient under real production load.
- Whether concurrency behavior is safe in all runtime interleavings.
- Whether a code change reflects the best possible design rather than a merely valid design.

## Required Files For This Automation

### Required In A Repository

- `.github/copilot-instructions.md`
- `.github/copilot-skills/skill-compliance-gate-skill.md`
- other referenced skill files under `.github/copilot-skills/`
- `scripts/Invoke-SkillComplianceCheck.ps1`

### Optional But Recommended

- `.vscode/tasks.json` for a one-click VS Code task
- `README.md` instructions that tell contributors how to run the gate
- CI workflow files if the same gate should run outside local development

### Required In The Codebase Being Checked

- A predictable source layout such as `src/main/java` and `src/test/java`
- Feature folders that follow the expected module structure where applicable: `resource`, `service`, `repository`, `entity`, `dto`, `mapper`
- Testable build tooling available on the machine and reflected in `build.command`

### What Can Be Overridden Without Editing The Script

- Repository name shown in the report.
- The main and test source roots.
- Which module names to exclude from package-structure checks.
- Which pagination parameter names count as bounded-list signals.
- The build command used by the gate.
- The report output path.

## Reusing This In A New Service Or Repo

- If you add a new service inside this repository, the default script behavior should keep working as long as the service follows the same layered package patterns.
- If you create a new repository, copy the instructions file, the relevant skill files, and the compliance script first.
- The script auto-detects module folders and entity imports, so you do not need a separate repo-specific config file.
- If the new repo is not Maven-based or has a different layout, pass the repo-specific values as script parameters instead of creating a new config file.
- `.vscode/tasks.json` is convenience only; it is not the core automation.
- The core automation is the combination of repo instructions, skill files, and the script.

## How To See The Skills, Automation, And Report

- Read the skill files under `.github/copilot-skills/`.
- Inspect `scripts/Invoke-SkillComplianceCheck.ps1` to see the generic automation logic.
- Open `target/skill-compliance-report.md` after running the gate to see the latest result, the skills listed as checked, and the config values used.
- Open `target/skill-compliance-autofix-preview.md` to see the low-risk changes the gate proposes or applied.
- Open `target/site/jacoco/index.html` for the human-readable coverage report when coverage verification runs.

## Required Final Pass After Code Changes

1. Read the relevant skills for the task.
2. Run the automated compliance check.
3. Review the preview output to decide whether the low-risk auto-fixes should be applied.
4. Fix all low-risk mechanical issues that the tool or local review identifies.
5. If approved, rerun the gate with `-AutoFix` for the low-risk mechanical fixes.
6. If something cannot be fixed safely or automatically, explain:
- what is missing
- why it was not auto-fixed
- what constraint in the current codebase prevents it
- what better approach is recommended
7. Do not close the task without reporting the compliance result.

## Required Report Format

- Skills checked
- Automated checks passed
- Automated checks failed
- Auto-fixed items
- Proposed auto-fixes
- Manual-review items
- Logger review checklist
- Not implemented and why
- Better approach recommendation

## Quality Gate

- Relevant skills were consulted.
- Automated compliance script ran.
- Tests passed or failures were explained.
- Remaining gaps were documented explicitly.
- Better approach was suggested when the current implementation is only a compromise.