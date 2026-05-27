# Script: build-entity.ps1
# Builda il modulo Common/Entity e copia il jar (e il pom) nelle local-repo di BE e BFF.

$ErrorActionPreference = 'Stop'

$repoRoot   = $PSScriptRoot
$entityDir  = Join-Path $repoRoot 'Common\Entity'

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
if (-not $mvnCmd) {
    throw "Maven non trovato nel PATH. Installa Maven o aggiungilo al PATH."
}
$mvn = $mvnCmd.Source

$version    = '1.0.0'
$jarName    = "Entity-$version.jar"
$pomName    = "Entity-$version.pom"
$sourceJar  = Join-Path $entityDir "target\$jarName"
$sourcePom  = Join-Path $entityDir 'pom.xml'

$commonPom  = Join-Path $repoRoot 'Common\pom.xml'
$rootPom    = Join-Path $repoRoot 'pom.xml'

$localRepos = @(
    Join-Path $repoRoot 'dfp-piao-be\local-repo'
    Join-Path $repoRoot 'dfp-piao-bff\local-repo'
)

Write-Host "==> Build di Common/Entity..." -ForegroundColor Cyan
Push-Location $entityDir
try {
    & $mvn -B clean package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Build di Entity fallita (exit code $LASTEXITCODE)" }
} finally {
    Pop-Location
}

if (-not (Test-Path $sourceJar)) {
    throw "Jar non trovato: $sourceJar"
}

foreach ($repo in $localRepos) {
    $entityDest = Join-Path $repo "it\ey\Entity\$version"
    $commonDest = Join-Path $repo "it\ey\common\$version"
    $piaoDest   = Join-Path $repo "it\ey\piao\$version"

    foreach ($d in @($entityDest, $commonDest, $piaoDest)) {
        if (-not (Test-Path $d)) { New-Item -ItemType Directory -Path $d -Force | Out-Null }
    }

    Write-Host "==> Copio Entity in $entityDest" -ForegroundColor Cyan
    Copy-Item -Path $sourceJar -Destination (Join-Path $entityDest $jarName) -Force
    Copy-Item -Path $sourcePom -Destination (Join-Path $entityDest $pomName) -Force

    Write-Host "==> Copio parent common in $commonDest" -ForegroundColor Cyan
    Copy-Item -Path $commonPom -Destination (Join-Path $commonDest "common-$version.pom") -Force

    Write-Host "==> Copio parent piao in $piaoDest" -ForegroundColor Cyan
    Copy-Item -Path $rootPom -Destination (Join-Path $piaoDest "piao-$version.pom") -Force
}

Write-Host "==> Fatto." -ForegroundColor Green


