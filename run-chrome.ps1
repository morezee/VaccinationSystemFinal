$ErrorActionPreference = 'Stop'
$projectPath = $PSScriptRoot
$javaHomePath = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\jbr'
$mavenPath = 'C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd'
$chromePath = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
$logPath = Join-Path $projectPath 'target\chrome-run.log'
$errPath = Join-Path $projectPath 'target\chrome-run-error.log'

if (-not (Test-Path $chromePath)) { throw "Chrome was not found at $chromePath" }
if (-not (Test-Path $mavenPath)) { throw "Maven was not found at $mavenPath" }
if (-not (Test-Path (Join-Path $projectPath 'target'))) { New-Item -ItemType Directory -Path (Join-Path $projectPath 'target') | Out-Null }

$env:JAVA_HOME = $javaHomePath
$env:PATH = "$javaHomePath\bin;$env:PATH"
$server = Start-Process -FilePath $mavenPath -ArgumentList @('-q', 'spring-boot:run') `
    -WorkingDirectory $projectPath -RedirectStandardOutput $logPath `
    -RedirectStandardError $errPath -WindowStyle Hidden -PassThru

$url = 'http://localhost:8080/login'
for ($attempt = 0; $attempt -lt 90; $attempt++) {
    if ($server.HasExited) {
        $details = Get-Content $errPath, $logPath -ErrorAction SilentlyContinue | Select-Object -Last 25
        throw "ImmuneCare stopped before the web page was ready.`n$($details -join "`n")"
    }
    try {
        $response = Invoke-WebRequest -Uri $url -TimeoutSec 2 -ErrorAction Stop
        if ($response.StatusCode -ge 200) { break }
    } catch { }
    Start-Sleep -Seconds 2
}

if ($server.HasExited) { throw 'ImmuneCare exited before Chrome could open.' }
Start-Process -FilePath $chromePath -ArgumentList $url
Write-Host "ImmuneCare is running at $url (server PID $($server.Id))."
Write-Host "After signing in, open the vaccination system from the dashboard."
Write-Host "Server log: $logPath"
