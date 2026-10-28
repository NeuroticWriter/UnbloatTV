@echo off
:: UnbloatTV - Windows Quick Launcher
:: Repository: https://github.com/NeuroticWriter/UnbloatTV
:: License: MIT

title UnbloatTV - Android & Google TV Optimizer
color 0B

echo ===============================================================================
echo     UnbloatTV - Intelligent Android TV & Google TV Optimizer
echo     https://github.com/NeuroticWriter/UnbloatTV
echo ===============================================================================
echo.

:: Check for Python
where python >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Python was not found in your PATH!
    echo Please install Python 3.8+ from https://www.python.org/downloads/
    echo (Make sure to check "Add Python to PATH" during installation)
    pause
    exit /b 1
)

:: Check for ADB
where adb >nul 2>nul
if %errorlevel% neq 0 (
    echo [WARNING] 'adb' command was not found in your PATH.
    echo If you have Android platform-tools, please add it to PATH.
    echo To install via winget, run: winget install Google.PlatformTools
    echo.
)

:: Execute unbloat.py passing all arguments
python "%~dp0unbloat.py" %*
if %errorlevel% neq 0 (
    pause
)
