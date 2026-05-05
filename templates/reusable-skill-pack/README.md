# Reusable Skill Pack Template

This folder is a copy-ready starter pack for a new repository that wants the same Copilot skill routing and compliance-gate workflow.

## What This Pack Contains

- `.github/copilot-instructions.md`: repo-level routing rules that tell Copilot which skills to read.
- `.github/copilot-skills/`: reusable skill files.
- `scripts/Invoke-SkillComplianceCheck.ps1`: reusable compliance automation logic.
- `.vscode/tasks.json`: optional VS Code task for running the gate.

This includes a dedicated `data-structures-algorithms-skill.md` for Java collection choice, complexity review, and scale-oriented coding decisions.
It also includes a dedicated `exception-handling-skill.md` for validation, business-rule, technical-failure, and safe client error-response design.

## How To Use It In A New Repo

1. Copy the contents of this folder into the root of the new repository.
2. Update `.github/copilot-instructions.md` if the new repo has a different stack or different review priorities.
3. Pass repo-specific values to the script only if the new repo differs from the defaults.
4. Run `powershell -ExecutionPolicy Bypass -File .\\scripts\\Invoke-SkillComplianceCheck.ps1` from the new repo root.
5. Review `target/skill-compliance-report.md` and `target/skill-compliance-autofix-preview.md` after the run.
6. If the preview looks safe, rerun with `-AutoFix`.

The standard flow also generates JaCoCo coverage output under `target/site/jacoco/` and enforces a default 95% line coverage threshold.

## Script Parameters You Can Override

- `-RepositoryName`
- `-MainSourceRoot`
- `-TestSourceRoot`
- `-RequiredDirectories`
- `-ExcludeModules`
- `-ResourceFilePattern`
- `-PaginationParameterNames`
- `-BuildCommand`
- `-ReportPath`
- `-PreviewPath`
- `-AutoFix`

## What Stays Manual

- Security completeness and authorization review.
- Logging quality and redaction policy.
- Swagger example quality and contract depth.
- Concurrency and load behavior.
- Design quality beyond the mechanical checks in the script.

## Preview And Apply Flow

1. Run the gate in preview mode first.
2. Review `target/skill-compliance-autofix-preview.md`.
3. If the proposed low-risk changes are acceptable, rerun with `-AutoFix`.
4. Review the final compliance report and test result.