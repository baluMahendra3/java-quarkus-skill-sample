param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot ".." )).Path,
    [string]$RepositoryName = (Split-Path ((Resolve-Path (Join-Path $PSScriptRoot ".." )).Path) -Leaf),
    [string]$MainSourceRoot = "src/main/java",
    [string]$TestSourceRoot = "src/test/java",
    [string]$ReportPath = "target/skill-compliance-report.md",
    [string]$PreviewPath = "target/skill-compliance-autofix-preview.md",
    [string]$CoverageReportPath = "target/site/jacoco/jacoco.xml",
    [double]$CoverageThreshold = 95.0,
    [string[]]$CoverageExcludePatterns = @("*.dto.*", "*.entity.*", "*.repository.*", "*.resource.*", "*.common.config.*", "*.JwtService"),
    [string[]]$RequiredDirectories = @("resource", "service", "repository", "entity", "dto", "mapper"),
    [string[]]$ExcludeModules = @("common"),
    [string]$ResourceFilePattern = "*Resource.java",
    [string[]]$PaginationParameterNames = @("page", "size", "limit"),
    [string[]]$BuildCommand = @("mvn", "-q", "-DskipTests=false", "verify"),
    [switch]$AutoFix,
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$skillsChecked = @(
    "architecture-skill.md",
    "quarkus-skill.md",
    "java-skill.md",
    "lombok-skill.md",
    "performance-high-volume-skill.md",
    "testing-skill.md",
    "maven-skill.md",
    "data-structures-algorithms-skill.md",
    "exception-handling-skill.md",
    "security-skill.md",
    "logger-skill.md",
    "swagger-skill.md",
    "concurrency-thread-safety-skill.md",
    "skill-compliance-gate-skill.md"
)

function Resolve-ProjectPath {
    param(
        [string]$PathValue
    )

    if ([string]::IsNullOrWhiteSpace($PathValue)) {
        return $null
    }

    if ([System.IO.Path]::IsPathRooted($PathValue)) {
        return $PathValue
    }

    return Join-Path $ProjectRoot $PathValue
}

function Get-LineNumberFromIndex {
    param(
        [string]$Content,
        [int]$Index
    )

    return ($Content.Substring(0, $Index) -split "`r?`n").Count
}

function Add-Result {
    param(
        [string]$Category,
        [string]$Status,
        [string]$Message
    )

    $script:results.Add([pscustomobject]@{
        Category = $Category
        Status = $Status
        Message = $Message
    }) | Out-Null
}

function Add-ProposedFix {
    param(
        [string]$Category,
        [string]$Message
    )

    $script:proposedFixes.Add([pscustomobject]@{
        Category = $Category
        Message = $Message
    }) | Out-Null
}

function Add-AutoFixedItem {
    param(
        [string]$Category,
        [string]$Message
    )

    $script:autoFixedItems.Add([pscustomobject]@{
        Category = $Category
        Message = $Message
    }) | Out-Null
}

function Add-ManualReviewItem {
    param(
        [string]$Message
    )

    $script:manualReviewItems.Add($Message) | Out-Null
}

function Add-NotImplementedItem {
    param(
        [string]$Message
    )

    $script:notImplementedItems.Add($Message) | Out-Null
}

function Get-Percent {
    param(
        [double]$Covered,
        [double]$Missed
    )

    $total = $Covered + $Missed
    if ($total -le 0) {
        return 100.0
    }

    return [math]::Round(($Covered / $total) * 100, 2)
}

function Test-CoverageExcluded {
    param(
        [string]$ClassName,
        [string[]]$CoverageExcludePatterns
    )

    foreach ($pattern in $CoverageExcludePatterns) {
        if ($ClassName -like $pattern) {
            return $true
        }
    }

    return $false
}

function Get-RelativeProjectPath {
    param(
        [string]$PathValue
    )

    return $PathValue.Replace($ProjectRoot + '\\', '')
}

function Test-Pattern {
    param(
        [string]$Category,
        [string]$Description,
        [string]$SearchBasePath,
        [string]$IncludeFilter,
        [string]$Pattern
    )

    if (-not (Test-Path $SearchBasePath)) {
        Add-Result -Category $Category -Status "FAIL" -Message ("Search base path not found for pattern check '{0}': {1}" -f $Description, $SearchBasePath)
        return
    }

    $matches = Get-ChildItem -Path $SearchBasePath -Recurse -File -Filter $IncludeFilter |
        Select-String -Pattern $Pattern

    if ($matches) {
        foreach ($match in $matches) {
            Add-Result -Category $Category -Status "FAIL" -Message ("{0}: {1}:{2}" -f $Description, $match.Path.Replace($ProjectRoot + '\\', ''), $match.LineNumber)
        }
    }
    else {
        Add-Result -Category $Category -Status "PASS" -Message $Description
    }
}

function Test-ModulePackageStructure {
    param(
        [string]$BasePath,
        [string[]]$RequiredDirectories,
        [string[]]$ExcludeModules = @("common")
    )

    if (-not (Test-Path $BasePath)) {
        Add-Result -Category "Architecture" -Status "FAIL" -Message ("Architecture base path not found: {0}" -f $BasePath)
        return
    }

    $layerDirectories = @(Get-ChildItem -Path $BasePath -Recurse -Directory |
        Where-Object { $RequiredDirectories -contains $_.Name })

    if ($layerDirectories.Count -eq 0) {
        Add-Result -Category "Architecture" -Status "FAIL" -Message ("No module layer folders were found under: {0}" -f $BasePath)
        return
    }

    $moduleDirectories = @(
        $layerDirectories |
            ForEach-Object { $_.Parent.FullName } |
            Sort-Object -Unique |
            ForEach-Object { Get-Item $_ } |
            Where-Object { $ExcludeModules -notcontains $_.Name }
    )

    $missingPackages = @()
    foreach ($module in $moduleDirectories) {
        foreach ($requiredDirectory in $RequiredDirectories) {
            $requiredPath = Join-Path $module.FullName $requiredDirectory
            if (-not (Test-Path $requiredPath)) {
                $missingPackages += [pscustomobject]@{
                    Module = Get-RelativeProjectPath -PathValue $module.FullName
                    RequiredPath = Get-RelativeProjectPath -PathValue $requiredPath
                    Directory = $requiredDirectory
                }
            }
        }
    }

    if ($missingPackages.Count -eq 0) {
        Add-Result -Category "Architecture" -Status "PASS" -Message "Module package structure includes required layer folders"
        return
    }

    foreach ($missing in $missingPackages) {
        Add-ProposedFix -Category "Architecture" -Message ("Create missing package '{0}': {1}" -f $missing.Directory, $missing.RequiredPath)
    }

    if ($AutoFix) {
        $autoFixFailures = @()
        foreach ($missing in $missingPackages) {
            $absoluteRequiredPath = Join-Path $ProjectRoot $missing.RequiredPath
            try {
                New-Item -ItemType Directory -Path $absoluteRequiredPath -Force | Out-Null
                Add-AutoFixedItem -Category "Architecture" -Message ("Created missing package '{0}': {1}" -f $missing.Directory, $missing.RequiredPath)
            }
            catch {
                $autoFixFailures += [pscustomobject]@{
                    Directory = $missing.Directory
                    Module = $missing.Module
                    Path = $missing.RequiredPath
                    Reason = $_.Exception.Message
                }
            }
        }

        if ($autoFixFailures.Count -eq 0) {
            Add-Result -Category "Architecture" -Status "PASS" -Message "Module package structure includes required layer folders after auto-fix"
            return
        }

        foreach ($failure in $autoFixFailures) {
            Add-Result -Category "Architecture" -Status "FAIL" -Message ("Unable to create expected package '{0}': {1} ({2})" -f $failure.Directory, $failure.Path, $failure.Reason)
            Add-NotImplementedItem -Message ("Auto-fix could not create '{0}' because {1}" -f $failure.Path, $failure.Reason)
        }
        return
    }

    foreach ($missing in $missingPackages) {
        Add-Result -Category "Architecture" -Status "FAIL" -Message ("Module missing expected package '{0}': {1}" -f $missing.Directory, $missing.Module)
    }
}

function Test-ResourceEntityLeakage {
    param(
        [string]$SearchBasePath,
        [string]$ResourceFilePattern
    )

    if (-not (Test-Path $SearchBasePath)) {
        Add-Result -Category "DTO Boundary" -Status "FAIL" -Message ("DTO boundary scan base path not found: {0}" -f $SearchBasePath)
        return
    }

    $resourceFiles = @(Get-ChildItem -Path $SearchBasePath -Recurse -File -Filter $ResourceFilePattern)
    $entityImportPattern = [regex]::new("import\s+[\w\.]+\.entity\.(\w+)\s*;")
    $parameterFailures = @()
    $returnFailures = @()

    foreach ($file in $resourceFiles) {
        $content = Get-Content -Path $file.FullName -Raw
        $entityNames = @(
            $entityImportPattern.Matches($content) |
                ForEach-Object { $_.Groups[1].Value } |
                Sort-Object -Unique
        )

        foreach ($entityName in $entityNames) {
            $escapedName = [regex]::Escape($entityName)
            $parameterPattern = [regex]::new("public\s+[A-Za-z0-9_<>,\[\]\? ]+\s+\w+\s*\([^\)]*\b$escapedName\s+\w+", [System.Text.RegularExpressions.RegexOptions]::Singleline)
            $returnPattern = [regex]::new("public\s+(List<\s*$escapedName\s*>|$escapedName)\s+\w+\s*\(", [System.Text.RegularExpressions.RegexOptions]::Singleline)

            $parameterMatch = $parameterPattern.Match($content)
            if ($parameterMatch.Success) {
                $parameterFailures += [pscustomobject]@{
                    Path = $file.FullName.Replace($ProjectRoot + '\\', '')
                    LineNumber = Get-LineNumberFromIndex -Content $content -Index $parameterMatch.Index
                }
            }

            $returnMatch = $returnPattern.Match($content)
            if ($returnMatch.Success) {
                $returnFailures += [pscustomobject]@{
                    Path = $file.FullName.Replace($ProjectRoot + '\\', '')
                    LineNumber = Get-LineNumberFromIndex -Content $content -Index $returnMatch.Index
                }
            }
        }
    }

    if ($parameterFailures.Count -eq 0) {
        Add-Result -Category "DTO Boundary" -Status "PASS" -Message "Resources should not accept entity types imported from entity packages directly"
    }
    else {
        foreach ($failure in $parameterFailures) {
            Add-Result -Category "DTO Boundary" -Status "FAIL" -Message ("Resources should not accept entity types imported from entity packages directly: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($returnFailures.Count -eq 0) {
        Add-Result -Category "DTO Boundary" -Status "PASS" -Message "Resources should not return entity types imported from entity packages directly"
    }
    else {
        foreach ($failure in $returnFailures) {
            Add-Result -Category "DTO Boundary" -Status "FAIL" -Message ("Resources should not return entity types imported from entity packages directly: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }
}

function Test-ListEndpointPagination {
    param(
        [string]$BasePath,
        [string]$ResourceFilePattern,
        [string[]]$PaginationParameterNames
    )

    if (-not (Test-Path $BasePath)) {
        Add-Result -Category "Performance" -Status "FAIL" -Message ("Performance scan base path not found: {0}" -f $BasePath)
        return
    }

    $resourceFiles = Get-ChildItem -Path $BasePath -Recurse -File -Filter $ResourceFilePattern
    $methodPattern = [regex]::new("public\s+List<[^>]+>\s+list\w+\s*\([\s\S]*?\)\s*\{", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    $paginationPattern = "\b(" + (($PaginationParameterNames | ForEach-Object { [regex]::Escape($_) }) -join "|") + ")\b"

    $failures = @()
    foreach ($file in $resourceFiles) {
        $content = Get-Content -Path $file.FullName -Raw
        $matches = $methodPattern.Matches($content)
        foreach ($match in $matches) {
            if ($match.Value -notmatch $paginationPattern) {
                $lineNumber = ($content.Substring(0, $match.Index) -split "`r?`n").Count
                $failures += [pscustomobject]@{
                    Path = $file.FullName.Replace($ProjectRoot + '\\', '')
                    LineNumber = $lineNumber
                }
            }
        }
    }

    if ($failures.Count -eq 0) {
        Add-Result -Category "Performance" -Status "PASS" -Message "Resources should avoid obviously unbounded page-less list signatures when using list endpoints"
        return
    }

    foreach ($failure in $failures) {
        Add-Result -Category "Performance" -Status "FAIL" -Message ("Resources should avoid obviously unbounded page-less list signatures when using list endpoints: {0}:{1}" -f $failure.Path, $failure.LineNumber)
    }
}

function Test-FilePatternPresence {
    param(
        [string]$Category,
        [string]$Description,
        [string]$SearchBasePath,
        [string]$IncludeFilter,
        [string]$Pattern
    )

    if (-not (Test-Path $SearchBasePath)) {
        Add-Result -Category $Category -Status "FAIL" -Message ("Search base path not found for presence check '{0}': {1}" -f $Description, $SearchBasePath)
        return
    }

    $match = Get-ChildItem -Path $SearchBasePath -Recurse -File -Filter $IncludeFilter |
        Select-String -Pattern $Pattern | Select-Object -First 1

    if ($null -ne $match) {
        Add-Result -Category $Category -Status "PASS" -Message $Description
        return
    }

    Add-Result -Category $Category -Status "FAIL" -Message $Description
}

function Test-SingleFilePatternAbsence {
    param(
        [string]$Category,
        [string]$Description,
        [string]$FilePath,
        [string]$Pattern
    )

    if (-not (Test-Path $FilePath)) {
        Add-Result -Category $Category -Status "FAIL" -Message ("File not found for pattern check '{0}': {1}" -f $Description, $FilePath)
        return
    }

    $matches = Select-String -Path $FilePath -Pattern $Pattern
    if ($matches) {
        foreach ($match in $matches) {
            Add-Result -Category $Category -Status "FAIL" -Message ("{0}: {1}:{2}" -f $Description, $match.Path.Replace($ProjectRoot + '\\', ''), $match.LineNumber)
        }
        return
    }

    Add-Result -Category $Category -Status "PASS" -Message $Description
}

function Test-SingleFilePatternPresence {
    param(
        [string]$Category,
        [string]$Description,
        [string]$FilePath,
        [string]$Pattern
    )

    if (-not (Test-Path $FilePath)) {
        Add-Result -Category $Category -Status "FAIL" -Message ("File not found for presence check '{0}': {1}" -f $Description, $FilePath)
        return
    }

    $match = Select-String -Path $FilePath -Pattern $Pattern | Select-Object -First 1
    if ($null -ne $match) {
        Add-Result -Category $Category -Status "PASS" -Message $Description
        return
    }

    Add-Result -Category $Category -Status "FAIL" -Message $Description
}

function Test-PomPluginVersionPinning {
    param(
        [string]$PomPath
    )

    if (-not (Test-Path $PomPath)) {
        Add-Result -Category "Maven" -Status "FAIL" -Message ("pom.xml not found for plugin version pinning check: {0}" -f $PomPath)
        return
    }

    $pomContent = Get-Content -Path $PomPath -Raw
    $pluginPattern = [regex]::new("<plugin>\s*(?:<groupId>(?<groupId>.*?)</groupId>\s*)?<artifactId>(?<artifactId>.*?)</artifactId>(?<body>.*?)</plugin>", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    $pluginMatches = $pluginPattern.Matches($pomContent)

    if ($pluginMatches.Count -eq 0) {
        Add-Result -Category "Maven" -Status "FAIL" -Message "pom.xml should declare build plugins with explicit versions"
        return
    }

    $failures = @()
    foreach ($pluginMatch in $pluginMatches) {
        if ($pluginMatch.Groups["body"].Value -notmatch "<version>\s*[^<\s][^<]*</version>") {
            $lineNumber = Get-LineNumberFromIndex -Content $pomContent -Index $pluginMatch.Index
            $artifactId = $pluginMatch.Groups["artifactId"].Value.Trim()
            if ([string]::IsNullOrWhiteSpace($artifactId)) {
                $artifactId = "unknown-artifact"
            }

            $failures += [pscustomobject]@{
                ArtifactId = $artifactId
                LineNumber = $lineNumber
            }
        }
    }

    if ($failures.Count -eq 0) {
        Add-Result -Category "Maven" -Status "PASS" -Message "Build plugins should declare explicit versions"
        return
    }

    foreach ($failure in $failures) {
        Add-Result -Category "Maven" -Status "FAIL" -Message ("Build plugins should declare explicit versions: pom.xml:{0} ({1})" -f $failure.LineNumber, $failure.ArtifactId)
    }
}

function Test-ResourceAuthorizationAnnotations {
    param(
        [string]$BasePath,
        [string]$ResourceFilePattern
    )

    if (-not (Test-Path $BasePath)) {
        Add-Result -Category "Security" -Status "FAIL" -Message ("Security scan base path not found: {0}" -f $BasePath)
        return
    }

    $resourceFiles = @(Get-ChildItem -Path $BasePath -Recurse -File -Filter $ResourceFilePattern)
    $authAnnotationPattern = '@(RolesAllowed|Authenticated|PermitAll|DenyAll)\b'
    $protectedAuthAnnotationPattern = '@(RolesAllowed|Authenticated)\b'
    $httpMethodPattern = '@(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\b'
    $failures = @()

    foreach ($file in $resourceFiles) {
        $lines = @(Get-Content -Path $file.FullName)
        if ($lines.Count -eq 0) {
            continue
        }

        $classLineIndex = -1
        for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
            if ($lines[$lineIndex] -match '^\s*public\s+(?:final\s+)?class\b') {
                $classLineIndex = $lineIndex
                break
            }
        }

        if ($classLineIndex -lt 0) {
            continue
        }

        $headerText = ""
        if ($classLineIndex -gt 0) {
            $headerStart = $classLineIndex - 1
            while ($headerStart -ge 0) {
                $currentLine = $lines[$headerStart]
                if ([string]::IsNullOrWhiteSpace($currentLine) -or $currentLine -match '^\s*(package|import)\b') {
                    $headerStart++
                    break
                }
                $headerStart--
            }

            if ($headerStart -lt 0) {
                $headerStart = 0
            }

            $headerText = ($lines[$headerStart..($classLineIndex - 1)] -join "`n")
        }

        if ($headerText -notmatch '@Path\s*\(\s*"/api') {
            continue
        }

        if ($headerText -match $authAnnotationPattern) {
            continue
        }

        for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
            if ($lines[$lineIndex] -match '^\s*public\s+' -and $lines[$lineIndex] -notmatch '\bclass\b') {
                $windowStart = [Math]::Max(0, $lineIndex - 25)
                $annotationWindow = ""
                if ($lineIndex -gt 0) {
                    $annotationWindow = ($lines[$windowStart..($lineIndex - 1)] -join "`n")
                }

                if ($annotationWindow -match $httpMethodPattern -and $annotationWindow -notmatch $authAnnotationPattern) {
                    $failures += [pscustomobject]@{
                        Path = $file.FullName.Replace($ProjectRoot + '\\', '')
                        LineNumber = $lineIndex + 1
                    }
                }
            }
        }
    }

    if ($failures.Count -eq 0) {
        Add-Result -Category "Security" -Status "PASS" -Message "API resource endpoints without class-level authorization should declare explicit authorization annotations"
        return
    }

    foreach ($failure in $failures) {
        Add-Result -Category "Security" -Status "FAIL" -Message ("API resource endpoints without class-level authorization should declare explicit authorization annotations: {0}:{1}" -f $failure.Path, $failure.LineNumber)
    }
}

function Test-ResourceOpenApiAnnotations {
    param(
        [string]$BasePath,
        [string]$ResourceFilePattern
    )

    if (-not (Test-Path $BasePath)) {
        Add-Result -Category "Swagger" -Status "FAIL" -Message ("Swagger scan base path not found: {0}" -f $BasePath)
        return
    }

    $resourceFiles = @(Get-ChildItem -Path $BasePath -Recurse -File -Filter $ResourceFilePattern)
    $authAnnotationPattern = '@(RolesAllowed|Authenticated|PermitAll|DenyAll)\b'
    $protectedAuthAnnotationPattern = '@(RolesAllowed|Authenticated)\b'
    $securityRequirementPattern = '@SecurityRequirement\b'
    $httpMethodPattern = '@(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)\b'
    $classTagFailures = @()
    $classResponseFailures = @()
    $classSecurityFailures = @()
    $methodOperationFailures = @()
    $methodResponseFailures = @()
    $methodSecurityFailures = @()

    foreach ($file in $resourceFiles) {
        $lines = @(Get-Content -Path $file.FullName)
        if ($lines.Count -eq 0) {
            continue
        }

        $classLineIndex = -1
        for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
            if ($lines[$lineIndex] -match '^\s*public\s+(?:final\s+)?class\b') {
                $classLineIndex = $lineIndex
                break
            }
        }

        if ($classLineIndex -lt 0) {
            continue
        }

        $headerText = ""
        if ($classLineIndex -gt 0) {
            $headerStart = $classLineIndex - 1
            while ($headerStart -ge 0) {
                $currentLine = $lines[$headerStart]
                if ([string]::IsNullOrWhiteSpace($currentLine) -or $currentLine -match '^\s*(package|import)\b') {
                    $headerStart++
                    break
                }
                $headerStart--
            }

            if ($headerStart -lt 0) {
                $headerStart = 0
            }

            $headerText = ($lines[$headerStart..($classLineIndex - 1)] -join "`n")
        }

        if ($headerText -notmatch '@Path\s*\(\s*"/api') {
            continue
        }

        if ($headerText -notmatch '@Tag\b') {
            $classTagFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $classLineIndex + 1 }
        }

        if ($headerText -notmatch '@APIResponses?\b') {
            $classResponseFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $classLineIndex + 1 }
        }

        if ($headerText -match $protectedAuthAnnotationPattern -and $headerText -notmatch $securityRequirementPattern) {
            $classSecurityFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $classLineIndex + 1 }
        }

        for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
            if ($lines[$lineIndex] -match '^\s*public\s+' -and $lines[$lineIndex] -notmatch '\bclass\b') {
                $annotationWindow = ""
                if ($lineIndex -gt 0) {
                    $windowStart = $lineIndex - 1
                    while ($windowStart -ge 0) {
                        $currentLine = $lines[$windowStart]
                        if ([string]::IsNullOrWhiteSpace($currentLine) -or $currentLine -match '^\s*(public|private|protected)\b' -or $currentLine -match '^\s*}\s*$') {
                            $windowStart++
                            break
                        }
                        $windowStart--
                    }

                    if ($windowStart -lt 0) {
                        $windowStart = 0
                    }

                    $annotationWindow = ($lines[$windowStart..($lineIndex - 1)] -join "`n")
                }

                if ($annotationWindow -notmatch $httpMethodPattern) {
                    continue
                }

                if ($annotationWindow -notmatch '@Operation\b') {
                    $methodOperationFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $lineIndex + 1 }
                }

                if ($annotationWindow -notmatch '@APIResponses?\b') {
                    $methodResponseFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $lineIndex + 1 }
                }

                if ($headerText -notmatch $securityRequirementPattern -and $annotationWindow -match $protectedAuthAnnotationPattern -and $annotationWindow -notmatch $securityRequirementPattern) {
                    $methodSecurityFailures += [pscustomobject]@{ Path = $file.FullName.Replace($ProjectRoot + '\\', ''); LineNumber = $lineIndex + 1 }
                }
            }
        }
    }

    if ($classTagFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "API resources should declare class-level OpenAPI tags"
    }
    else {
        foreach ($failure in $classTagFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("API resources should declare class-level OpenAPI tags: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($classResponseFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "API resources should declare class-level OpenAPI error responses"
    }
    else {
        foreach ($failure in $classResponseFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("API resources should declare class-level OpenAPI error responses: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($classSecurityFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "Protected API resources should document class-level security requirements"
    }
    else {
        foreach ($failure in $classSecurityFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("Protected API resources should document class-level security requirements: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($methodOperationFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "API resource endpoints should declare OpenAPI operations"
    }
    else {
        foreach ($failure in $methodOperationFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("API resource endpoints should declare OpenAPI operations: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($methodResponseFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "API resource endpoints should declare OpenAPI responses"
    }
    else {
        foreach ($failure in $methodResponseFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("API resource endpoints should declare OpenAPI responses: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }

    if ($methodSecurityFailures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "Protected API endpoints without class-level security docs should declare OpenAPI security requirements"
    }
    else {
        foreach ($failure in $methodSecurityFailures) {
            Add-Result -Category "Swagger" -Status "FAIL" -Message ("Protected API endpoints without class-level security docs should declare OpenAPI security requirements: {0}:{1}" -f $failure.Path, $failure.LineNumber)
        }
    }
}

function Test-ListEndpointPaginationDocumentation {
    param(
        [string]$BasePath,
        [string]$ResourceFilePattern,
        [string[]]$PaginationParameterNames
    )

    if (-not (Test-Path $BasePath)) {
        Add-Result -Category "Swagger" -Status "FAIL" -Message ("Swagger pagination scan base path not found: {0}" -f $BasePath)
        return
    }

    $resourceFiles = @(Get-ChildItem -Path $BasePath -Recurse -File -Filter $ResourceFilePattern)
    $methodPattern = [regex]::new("public\s+List<[^>]+>\s+\w+\s*\([\s\S]*?\)\s*\{", [System.Text.RegularExpressions.RegexOptions]::Singleline)
    $failures = @()

    foreach ($file in $resourceFiles) {
        $content = Get-Content -Path $file.FullName -Raw
        if ($content -notmatch '@Path\s*\(\s*"/api') {
            continue
        }

        $matches = $methodPattern.Matches($content)
        foreach ($match in $matches) {
            foreach ($parameterName in $PaginationParameterNames) {
                $queryParamPattern = '@QueryParam\("' + [regex]::Escape($parameterName) + '"\)'
                if ($match.Value -match $queryParamPattern) {
                    $parameterDocPattern = '@Parameter[\s\S]{0,250}@QueryParam\("' + [regex]::Escape($parameterName) + '"\)'
                    if ($match.Value -notmatch $parameterDocPattern) {
                        $lineNumber = Get-LineNumberFromIndex -Content $content -Index $match.Index
                        $failures += [pscustomobject]@{
                            Path = $file.FullName.Replace($ProjectRoot + '\\', '')
                            LineNumber = $lineNumber
                            ParameterName = $parameterName
                        }
                    }
                }
            }
        }
    }

    if ($failures.Count -eq 0) {
        Add-Result -Category "Swagger" -Status "PASS" -Message "List endpoints should document pagination query parameters with OpenAPI @Parameter annotations"
        return
    }

    foreach ($failure in $failures) {
        Add-Result -Category "Swagger" -Status "FAIL" -Message ("List endpoints should document pagination query parameters with OpenAPI @Parameter annotations: {0}:{1} ({2})" -f $failure.Path, $failure.LineNumber, $failure.ParameterName)
    }
}

function Test-CodeCoverage {
    param(
        [string]$CoverageReportPath,
        [double]$CoverageThreshold,
        [string[]]$CoverageExcludePatterns
    )

    if (-not (Test-Path $CoverageReportPath)) {
        Add-Result -Category "Coverage" -Status "FAIL" -Message ("Coverage report not found: {0}" -f $CoverageReportPath)
        Add-NotImplementedItem -Message "Coverage verification could not complete because the JaCoCo XML report was not found."
        return
    }

    [xml]$coverageXml = Get-Content -Path $CoverageReportPath
    $underCoveredClasses = @()
    $coveredLines = 0.0
    $missedLines = 0.0
    foreach ($packageNode in @($coverageXml.report.package)) {
        foreach ($classNode in @($packageNode.class)) {
            $className = ($classNode.name -replace '/', '.')
            if (Test-CoverageExcluded -ClassName $className -CoverageExcludePatterns $CoverageExcludePatterns) {
                continue
            }

            $classLineCounter = @($classNode.counter | Where-Object { $_.type -eq "LINE" } | Select-Object -First 1)
            if ($classLineCounter.Count -eq 0) {
                continue
            }

            $classCovered = [double]$classLineCounter[0].covered
            $classMissed = [double]$classLineCounter[0].missed
            $coveredLines += $classCovered
            $missedLines += $classMissed
            $classCoverage = Get-Percent -Covered $classCovered -Missed $classMissed
            if ($classCoverage -lt $CoverageThreshold) {
                $underCoveredClasses += [pscustomobject]@{
                    Name = $className
                    Coverage = $classCoverage
                }
            }
        }
    }

    $coveragePercent = Get-Percent -Covered $coveredLines -Missed $missedLines
    $message = ("Line coverage {0}% (threshold {1}%)" -f $coveragePercent, $CoverageThreshold)

    if ($coveragePercent -ge $CoverageThreshold) {
        Add-Result -Category "Coverage" -Status "PASS" -Message $message
        return
    }

    Add-Result -Category "Coverage" -Status "FAIL" -Message $message

    $topClasses = @($underCoveredClasses | Sort-Object Coverage, Name | Select-Object -First 10)
    foreach ($classEntry in $topClasses) {
        Add-ProposedFix -Category "Coverage" -Message ("Generate JUnit coverage for {0} ({1}% lines covered)" -f $classEntry.Name, $classEntry.Coverage)
    }

    Add-NotImplementedItem -Message "Coverage below threshold requires targeted JUnit additions for the lowest-covered classes; this gate reports the candidates but does not synthesize semantic tests on its own."
}

function Invoke-BuildCommand {
    param(
        [string[]]$CommandParts
    )

    if ($CommandParts.Count -eq 0) {
        throw "Build command is empty. Pass -BuildCommand with the validation command for this repository."
    }

    $commandName = $CommandParts[0]
    $commandArgs = @()
    if ($CommandParts.Count -gt 1) {
        $commandArgs = $CommandParts[1..($CommandParts.Count - 1)]
    }

    & $commandName $commandArgs
}

$mainSourceRootPath = Resolve-ProjectPath -PathValue $MainSourceRoot
$testSourceRootPath = Resolve-ProjectPath -PathValue $TestSourceRoot
$reportPathValue = Resolve-ProjectPath -PathValue $ReportPath
$previewPathValue = Resolve-ProjectPath -PathValue $PreviewPath
$coverageReportPathValue = Resolve-ProjectPath -PathValue $CoverageReportPath
$applicationPropertiesPath = Resolve-ProjectPath -PathValue "src/main/resources/application.properties"
$pomPathValue = Resolve-ProjectPath -PathValue "pom.xml"

$results = New-Object System.Collections.Generic.List[object]
$proposedFixes = New-Object System.Collections.Generic.List[object]
$autoFixedItems = New-Object System.Collections.Generic.List[object]
$manualReviewItems = New-Object System.Collections.Generic.List[string]
$notImplementedItems = New-Object System.Collections.Generic.List[string]

Add-ManualReviewItem -Message "Security completeness, authorization coverage, and secrets handling still require human review."
Add-ManualReviewItem -Message "Concurrency safety under real load still requires design review and, where relevant, load or stress testing."
Add-ManualReviewItem -Message "Performance suitability for higher volume still requires profiling or load testing; static checks only catch obvious issues."
Add-ManualReviewItem -Message "Swagger contract quality, examples, and implementation-to-spec drift still require human review."

Add-NotImplementedItem -Message "The current auto-fix mode only applies low-risk mechanical fixes, such as creating missing layer directories."
Add-NotImplementedItem -Message "Pattern violations such as DTO leakage, direct persistence access, or missing pagination are reported but not rewritten automatically."

Test-ModulePackageStructure -BasePath $mainSourceRootPath -RequiredDirectories $RequiredDirectories -ExcludeModules $ExcludeModules

Test-Pattern -Category "Architecture" -Description "No direct persist calls inside resources" -SearchBasePath $mainSourceRootPath -IncludeFilter $ResourceFilePattern -Pattern "\.persist\s*\("
Test-Pattern -Category "Architecture" -Description "No direct listAll calls inside resources" -SearchBasePath $mainSourceRootPath -IncludeFilter $ResourceFilePattern -Pattern "\.listAll\s*\("
Test-Pattern -Category "Architecture" -Description "No direct findById calls inside resources" -SearchBasePath $mainSourceRootPath -IncludeFilter $ResourceFilePattern -Pattern "\.findById\s*\("
Test-ResourceEntityLeakage -SearchBasePath $mainSourceRootPath -ResourceFilePattern $ResourceFilePattern
Test-Pattern -Category "Lombok" -Description "Entities should not use Lombok @Data" -SearchBasePath $mainSourceRootPath -IncludeFilter "*.java" -Pattern "@Entity[\s\S]{0,200}@Data|@Data[\s\S]{0,200}@Entity"
Test-ListEndpointPagination -BasePath $mainSourceRootPath -ResourceFilePattern $ResourceFilePattern -PaginationParameterNames $PaginationParameterNames
Test-Pattern -Category "Exception Handling" -Description "No generic RuntimeException, Exception, or Throwable throws in main code" -SearchBasePath $mainSourceRootPath -IncludeFilter "*.java" -Pattern "throw\s+new\s+(RuntimeException|Exception|Throwable)\s*\("
Test-Pattern -Category "Exception Handling" -Description "Exception mappers should not leak raw exception.getMessage() directly" -SearchBasePath $mainSourceRootPath -IncludeFilter "*ExceptionMapper.java" -Pattern "return\s+exception\.getMessage\s*\(\s*\)"
Test-FilePatternPresence -Category "Exception Handling" -Description "A shared Throwable exception mapper should exist" -SearchBasePath $mainSourceRootPath -IncludeFilter "*ExceptionMapper.java" -Pattern "ExceptionMapper<Throwable>"
Test-FilePatternPresence -Category "Exception Handling" -Description "A dedicated ConstraintViolationException mapper should exist" -SearchBasePath $mainSourceRootPath -IncludeFilter "*ExceptionMapper.java" -Pattern "ExceptionMapper<ConstraintViolationException>"
Test-SingleFilePatternPresence -Category "Security" -Description "JWT issuer verification should be configured" -FilePath $applicationPropertiesPath -Pattern "(?m)^mp\.jwt\.verify\.issuer\s*="
Test-SingleFilePatternPresence -Category "Security" -Description "JWT public key verification should be configured" -FilePath $applicationPropertiesPath -Pattern "(?m)^mp\.jwt\.verify\.(publickey|publickey\.location)\s*="
Test-SingleFilePatternPresence -Category "Security" -Description "Base CORS origins should be configured explicitly" -FilePath $applicationPropertiesPath -Pattern "(?m)^quarkus\.http\.cors\.origins\s*="
Test-SingleFilePatternAbsence -Category "Security" -Description "Non-dev and non-test CORS settings should not use wildcard values" -FilePath $applicationPropertiesPath -Pattern "(?m)^(?!%dev\.)(?!%test\.)quarkus\.http\.cors\.(origins|headers|methods)\s*=\s*\*"
Test-SingleFilePatternAbsence -Category "Security" -Description "Sensitive base configuration values should not be hardcoded outside dev or test profiles" -FilePath $applicationPropertiesPath -Pattern "(?im)^(?!\s*#)(?!%dev\.)(?!%test\.)(?:[^=\r\n]*\b(?:password|secret|credential|credentials)\b[^=\r\n]*)=(?!\$\{)[^\r\n]+"
Test-ResourceAuthorizationAnnotations -BasePath $mainSourceRootPath -ResourceFilePattern $ResourceFilePattern
Test-SingleFilePatternAbsence -Category "Logger" -Description "Base SQL logging should stay disabled outside dev or test profiles" -FilePath $applicationPropertiesPath -Pattern "(?im)^(?!\s*#)(?!%dev\.)(?!%test\.)quarkus\.hibernate-orm\.log\.sql\s*=\s*true"
Test-Pattern -Category "Logger" -Description "Application logs should not include passwords, tokens, secrets, or authorization headers" -SearchBasePath $mainSourceRootPath -IncludeFilter "*.java" -Pattern "(?i)(logger|LOGGER|LOG|log)\s*\.\s*(trace|debug|info|warn|error)\s*\([^\n;]*\b(password|passwd|token|authorization|secret|api[-_ ]?key|credential|cookie)\b"
Test-SingleFilePatternPresence -Category "Swagger" -Description "OpenAPI dependency should be declared in pom.xml" -FilePath $pomPathValue -Pattern "<artifactId>quarkus-smallrye-openapi</artifactId>"
Test-SingleFilePatternPresence -Category "Swagger" -Description "OpenAPI info title should be configured" -FilePath $applicationPropertiesPath -Pattern "(?m)^quarkus\.smallrye-openapi\.info-title\s*="
Test-SingleFilePatternPresence -Category "Swagger" -Description "OpenAPI info version should be configured" -FilePath $applicationPropertiesPath -Pattern "(?m)^quarkus\.smallrye-openapi\.info-version\s*="
Test-FilePatternPresence -Category "Swagger" -Description "A shared OpenAPI security scheme should exist" -SearchBasePath $mainSourceRootPath -IncludeFilter "*.java" -Pattern "@SecurityScheme\s*\("
Test-Pattern -Category "Swagger" -Description "API resources should not use vague object response schemas when a concrete schema should exist" -SearchBasePath $mainSourceRootPath -IncludeFilter $ResourceFilePattern -Pattern "@Schema\(\s*type\s*=\s*SchemaType\.OBJECT\s*\)|implementation\s*=\s*Object\.class"
Test-ResourceOpenApiAnnotations -BasePath $mainSourceRootPath -ResourceFilePattern $ResourceFilePattern
Test-ListEndpointPaginationDocumentation -BasePath $mainSourceRootPath -ResourceFilePattern $ResourceFilePattern -PaginationParameterNames $PaginationParameterNames
Test-SingleFilePatternAbsence -Category "Maven" -Description "pom.xml should not use LATEST, RELEASE, or Maven version ranges" -FilePath $pomPathValue -Pattern "(?i)<version>\s*(LATEST|RELEASE)\s*</version>|<version>\s*[\[\(][^<]+[\]\)]\s*</version>"
Test-PomPluginVersionPinning -PomPath $pomPathValue
Test-SingleFilePatternPresence -Category "Maven" -Description "pom.xml should declare maven-surefire-plugin" -FilePath $pomPathValue -Pattern "<artifactId>maven-surefire-plugin</artifactId>"
Test-SingleFilePatternPresence -Category "Maven" -Description "pom.xml should declare jacoco-maven-plugin" -FilePath $pomPathValue -Pattern "<artifactId>jacoco-maven-plugin</artifactId>"

Push-Location $ProjectRoot
try {
    if ($SkipTests) {
        Add-Result -Category "Build" -Status "SKIP" -Message "Tests skipped by caller"
        Add-Result -Category "Coverage" -Status "SKIP" -Message "Coverage verification skipped because tests were skipped"
    }
    else {
        Invoke-BuildCommand -CommandParts $BuildCommand
        if ($LASTEXITCODE -eq 0) {
            Add-Result -Category "Build" -Status "PASS" -Message ((($BuildCommand -join " ") + " passed"))
        }
        else {
            Add-Result -Category "Build" -Status "FAIL" -Message ((($BuildCommand -join " ") + " failed"))
        }
    }
}
catch {
    Add-Result -Category "Build" -Status "FAIL" -Message $_.Exception.Message
}
finally {
    Pop-Location
}

if (-not $SkipTests) {
    Test-CodeCoverage -CoverageReportPath $coverageReportPathValue -CoverageThreshold $CoverageThreshold -CoverageExcludePatterns $CoverageExcludePatterns
}

$reportDir = Split-Path -Path $reportPathValue -Parent
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir | Out-Null
}

$previewDir = Split-Path -Path $previewPathValue -Parent
if (-not (Test-Path $previewDir)) {
    New-Item -ItemType Directory -Path $previewDir | Out-Null
}

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$passes = @($results | Where-Object Status -eq "PASS")
$fails = @($results | Where-Object Status -eq "FAIL")
$skips = @($results | Where-Object Status -eq "SKIP")

$report = @()
$report += "# Skill Compliance Report"
$report += ""
$report += "Generated: $timestamp"
$report += "Repository: $RepositoryName"
$report += "Automation source: script defaults and parameters"
$report += ("Mode: {0}" -f ($(if ($AutoFix) { "auto-fix" } else { "preview-only" })))
$report += ""
$report += "## Summary"
$report += ""
$report += "- Pass: $($passes.Count)"
$report += "- Fail: $($fails.Count)"
$report += "- Skip: $($skips.Count)"
$report += ""
$report += "## Skills Checked"
$report += ""
foreach ($skill in $skillsChecked) {
    $report += "- $skill"
}
$report += ""
$report += "## Automation Configuration"
$report += ""
$report += "- Main source root: $MainSourceRoot"
$report += "- Test source root: $TestSourceRoot"
$report += "- Required module directories: $($RequiredDirectories -join ', ')"
$report += "- Excluded modules: $($ExcludeModules -join ', ')"
$report += "- Resource file pattern: $ResourceFilePattern"
$report += "- Pagination parameter names: $($PaginationParameterNames -join ', ')"
$report += "- Coverage report path: $CoverageReportPath"
$report += "- Coverage threshold: $CoverageThreshold%"
$report += "- Coverage exclusion patterns: $($CoverageExcludePatterns -join ', ')"
$report += "- Build command: $($BuildCommand -join ' ')"
$report += "- Report path: $ReportPath"
$report += "- Preview path: $PreviewPath"
$report += ""
$report += "## Results"
$report += ""
foreach ($result in $results) {
    $report += "- [$($result.Status)] [$($result.Category)] $($result.Message)"
}
$report += ""
$report += "## Proposed Auto-Fixes"
$report += ""
if ($proposedFixes.Count -eq 0) {
    $report += "- None"
}
else {
    foreach ($item in $proposedFixes) {
        $report += "- [$($item.Category)] $($item.Message)"
    }
}
$report += ""
$report += "## Auto-Fixed Items"
$report += ""
if ($autoFixedItems.Count -eq 0) {
    $report += "- None"
}
else {
    foreach ($item in $autoFixedItems) {
        $report += "- [$($item.Category)] $($item.Message)"
    }
}
$report += ""
$report += "## Auto-Fix Scope"
$report += ""
$report += "- Mechanical checks can detect package-structure gaps, but package moves and large refactors are not auto-applied by this script."
$report += "- Missing architecture folders such as resource, repository, or entity packages can be created automatically when `-AutoFix` is used."
$report += "- Most code rewrites still require user approval and intentional refactoring."
$report += ""
$report += "## Manual Review Required"
$report += ""
foreach ($item in $manualReviewItems) {
    $report += "- $item"
}
$report += ""
$report += "## Not Implemented And Why"
$report += ""
foreach ($item in $notImplementedItems) {
    $report += "- $item"
}
$report += ""
$report += "## Better Approach Recommendation"
$report += ""
$report += "- Use preview mode first to review the proposed changes, then rerun with `-AutoFix` only for low-risk mechanical fixes."
$report += "- Keep semantic refactors, contract changes, and business-rule changes behind explicit user approval."

Set-Content -Path $reportPathValue -Value $report -Encoding UTF8
Write-Output "Skill compliance report written to $reportPathValue"

$preview = @()
$preview += "# Skill Compliance Auto-Fix Preview"
$preview += ""
$preview += "Generated: $timestamp"
$preview += "Repository: $RepositoryName"
$preview += ("Mode: {0}" -f ($(if ($AutoFix) { "auto-fix" } else { "preview-only" })))
$preview += ""
if ($proposedFixes.Count -eq 0) {
    $preview += "- No low-risk auto-fix candidates were detected."
}
else {
    foreach ($item in $proposedFixes) {
        $preview += "- [$($item.Category)] $($item.Message)"
    }
}
$preview += ""
if ($AutoFix -and $autoFixedItems.Count -gt 0) {
    $preview += "## Applied"
    $preview += ""
    foreach ($item in $autoFixedItems) {
        $preview += "- [$($item.Category)] $($item.Message)"
    }
}

Set-Content -Path $previewPathValue -Value $preview -Encoding UTF8
Write-Output "Skill compliance preview written to $previewPathValue"

if ($fails.Count -gt 0) {
    exit 1
}

exit 0