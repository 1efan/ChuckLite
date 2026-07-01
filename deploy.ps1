# ChuckLite deploy script
# Uploads one version with separate jars per loader and Minecraft version.
# Usage: .\deploy.ps1 -ProjectId 1582756

param(
    [Parameter(Mandatory=$true)]
    [string]$ProjectId,

    [string]$Token = $env:CURSEFORGE_TOKEN,

    [string]$Version = "1.03",

    [string]$Changelog = @"
ChunkLite 1.03 focuses on smoother chunk-heavy gameplay while staying friendly with other optimization mods.

- Built for Forge and Fabric on Minecraft 1.20.1 and 1.21.1
- Plays nicely with Sodium, Embeddium, and Rubidium by stepping aside when they own chunk rendering
- Works alongside Lithium, Iris, FerriteCore, Entity Culling, ModernFix, and OptiFine with no known conflicts
- Keeps chunk work client-side only, so servers do not need to install it
"@
)

if (-not $Token) {
    throw "CURSEFORGE_TOKEN is not set"
}

$headers = @{ "X-Api-Token" = $Token }

Write-Host "Fetching CurseForge version IDs..." -ForegroundColor Cyan

$allVersions = Invoke-RestMethod -Uri "https://minecraft.curseforge.com/api/game/versions" -Headers $headers

function find-version($name, $typeId) {
    $v = $allVersions | Where-Object { $_.name -eq $name -and $_.gameVersionTypeID -eq $typeId } | Select-Object -First 1
    if (-not $v) { throw "Version not found: $name" }
    return $v.id
}

$mc1201 = find-version "1.20.1" 75125
$mc1211 = find-version "1.21.1" 77784
$forge = find-version "Forge" 68441
$fabric = find-version "Fabric" 68441
$java17 = find-version "Java 17" 2
$java21 = find-version "Java 21" 2

Write-Host "  1.20.1 = $mc1201, 1.21.1 = $mc1211, Forge = $forge, Fabric = $fabric, Java 17 = $java17, Java 21 = $java21"

$uploads = @(
    @{Path="build/release/chunk-lite-1.03-forge-1.20.1.jar"; Name="ChuckLite v$Version + MC 1.20.1 (Forge)"; Versions=@($mc1201,$forge,$java17)},
    @{Path="build/release/chunk-lite-1.03-fabric-1.20.1.jar"; Name="ChuckLite v$Version + MC 1.20.1 (Fabric)"; Versions=@($mc1201,$fabric,$java17)},
    @{Path="build/release/chunk-lite-1.03-forge-1.21.1.jar"; Name="ChuckLite v$Version + MC 1.21.1 (Forge)"; Versions=@($mc1211,$forge,$java21)},
    @{Path="build/release/chunk-lite-1.03-fabric-1.21.1.jar"; Name="ChuckLite v$Version + MC 1.21.1 (Fabric)"; Versions=@($mc1211,$fabric,$java21)}
)

$url = "https://minecraft.curseforge.com/api/projects/$ProjectId/upload-file"

function upload-curseforge-file($url, $headers, $jar, $changelog) {
    Add-Type -AssemblyName System.Net.Http

    $client = [System.Net.Http.HttpClient]::new()
    foreach ($key in $headers.Keys) {
        $client.DefaultRequestHeaders.Add($key, $headers[$key])
    }

    $content = [System.Net.Http.MultipartFormDataContent]::new()
    $fileStream = [System.IO.File]::OpenRead((Resolve-Path -LiteralPath $jar.Path))
    $fileContent = [System.Net.Http.StreamContent]::new($fileStream)
    $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("application/java-archive")

    $metadata = [ordered]@{
        changelog = $changelog
        changelogType = "markdown"
        displayName = $jar.Name
        gameVersions = $jar.Versions
        releaseType = "release"
    } | ConvertTo-Json -Depth 10 -Compress

    $content.Add([System.Net.Http.StringContent]::new($metadata, [System.Text.Encoding]::UTF8, "application/json"), "metadata")
    $content.Add($fileContent, "file", [System.IO.Path]::GetFileName($jar.Path))

    try {
        $response = $client.PostAsync($url, $content).GetAwaiter().GetResult()
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

foreach ($jar in $uploads) {
    if (-not (Test-Path $jar.Path)) {
        Write-Host "SKIP: $($jar.Path) not found" -ForegroundColor Yellow
        continue
    }

    Write-Host "Uploading: $($jar.Name)" -ForegroundColor Cyan

    try {
        $result = upload-curseforge-file $url $headers $jar $Changelog
        Write-Host "  Uploaded: file ID $($result.id)" -ForegroundColor Green
    } catch {
        Write-Host "  FAILED: $_" -ForegroundColor Red
    }
}

Write-Host "Done." -ForegroundColor Green
