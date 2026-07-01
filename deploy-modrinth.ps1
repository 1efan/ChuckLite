# ChunkLite Modrinth deploy script
# Uploads one version entry per loader and Minecraft version.
# Usage: .\deploy-modrinth.ps1

param(
    [string]$ProjectId = "ZYoYqWLn",

    [string]$Token = $env:MODRINTH_TOKEN,

    [string]$Version = "1.03",

    [string]$Status = "draft",

    [string]$Changelog = @"
ChunkLite 1.03 focuses on smoother chunk-heavy gameplay while staying friendly with other optimization mods.

- Built for Forge and Fabric on Minecraft 1.20.1 and 1.21.1
- Plays nicely with Sodium, Embeddium, and Rubidium by stepping aside when they own chunk rendering
- Works alongside Lithium, Iris, FerriteCore, Entity Culling, ModernFix, and OptiFine with no known conflicts
- Keeps chunk work client-side only, so servers do not need to install it
"@
)

if (-not $Token) {
    throw "MODRINTH_TOKEN is not set"
}

$uploads = @(
    @{Path="build/release/chunk-lite-1.03-forge-1.20.1.jar"; Name="ChunkLite $Version for Forge 1.20.1"; VersionNumber="$Version-forge-1.20.1"; Loaders=@("forge"); GameVersions=@("1.20.1")},
    @{Path="build/release/chunk-lite-1.03-fabric-1.20.1.jar"; Name="ChunkLite $Version for Fabric 1.20.1"; VersionNumber="$Version-fabric-1.20.1"; Loaders=@("fabric"); GameVersions=@("1.20.1")},
    @{Path="build/release/chunk-lite-1.03-forge-1.21.1.jar"; Name="ChunkLite $Version for Forge 1.21.1"; VersionNumber="$Version-forge-1.21.1"; Loaders=@("forge"); GameVersions=@("1.21.1")},
    @{Path="build/release/chunk-lite-1.03-fabric-1.21.1.jar"; Name="ChunkLite $Version for Fabric 1.21.1"; VersionNumber="$Version-fabric-1.21.1"; Loaders=@("fabric"); GameVersions=@("1.21.1")}
)

function upload-modrinth-version($token, $projectId, $upload, $status, $changelog) {
    Add-Type -AssemblyName System.Net.Http

    $data = [ordered]@{
        name = $upload.Name
        version_number = $upload.VersionNumber
        changelog = $changelog
        dependencies = @()
        game_versions = $upload.GameVersions
        version_type = "release"
        loaders = $upload.Loaders
        featured = $true
        status = $status
        requested_status = $status
        project_id = $projectId
        file_parts = @("file")
        primary_file = "file"
        environment = "client_only"
    }

    $json = $data | ConvertTo-Json -Depth 10 -Compress

    $client = [System.Net.Http.HttpClient]::new()
    $client.DefaultRequestHeaders.Add("Authorization", $token)
    $client.DefaultRequestHeaders.Add("User-Agent", "1efan-mod-workflow/1.0 (1efan)")

    $content = [System.Net.Http.MultipartFormDataContent]::new()
    $content.Add([System.Net.Http.StringContent]::new($json, [System.Text.Encoding]::UTF8, "application/json"), "data")

    $fileStream = [System.IO.File]::OpenRead((Resolve-Path -LiteralPath $upload.Path))
    $fileContent = [System.Net.Http.StreamContent]::new($fileStream)
    $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/java-archive")
    $content.Add($fileContent, "file", [System.IO.Path]::GetFileName($upload.Path))

    try {
        $response = $client.PostAsync("https://api.modrinth.com/v2/version", $content).GetAwaiter().GetResult()
        $responseText = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        if (-not $response.IsSuccessStatusCode) {
            throw "HTTP $([int]$response.StatusCode) $($response.StatusCode): $responseText"
        }
        return $responseText | ConvertFrom-Json
    } finally {
        $fileStream.Dispose()
        $content.Dispose()
        $client.Dispose()
    }
}

foreach ($upload in $uploads) {
    if (-not (Test-Path $upload.Path)) {
        Write-Host "SKIP: $($upload.Path) not found" -ForegroundColor Yellow
        continue
    }

    Write-Host "Uploading: $($upload.Name)" -ForegroundColor Cyan
    try {
        $result = upload-modrinth-version $Token $ProjectId $upload $Status $Changelog
        Write-Host "  Uploaded: version ID $($result.id)" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $_" -ForegroundColor Red
    }
}

Write-Host "Done." -ForegroundColor Green
