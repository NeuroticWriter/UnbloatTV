# UnbloatTV - Windows PowerShell One-Liner Launcher
# Repository: https://github.com/NeuroticWriter/UnbloatTV
# License: MIT

Write-Host "▶ Launching UnbloatTV (Intelligent Android & Google TV Optimizer)..." -ForegroundColor Cyan

# 1. Check Python
if (-not (Get-Command python -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Python 3 was not found in your PATH." -ForegroundColor Red
    Write-Host "Please install Python 3.8+ from https://www.python.org/downloads/ (Check 'Add Python to PATH')"
    exit 1
}

# 2. Check ADB
if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    Write-Host "Warning: 'adb' not found on PATH. If needed, install via: winget install Google.PlatformTools" -ForegroundColor Yellow
}

# 3. Locate or Download unbloat.py
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LocalTool = Join-Path $ScriptDir "tools\unbloat.py"

if (Test-Path $LocalTool) {
    python $LocalTool $args
} else {
    $TempTool = Join-Path $env:TEMP "unbloat.py"
    Write-Host "Downloading latest UnbloatTV engine..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri "https://raw.githubusercontent.com/NeuroticWriter/UnbloatTV/main/tools/unbloat.py" -OutFile $TempTool
    python $TempTool $args
}

