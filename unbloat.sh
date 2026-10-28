#!/usr/bin/env bash
# UnbloatTV - One-Click Launcher & Dependency Checker
# Repository: https://github.com/NeuroticWriter/UnbloatTV
# License: MIT

set -e

GREEN='\033[0;32m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${CYAN}▶ Launching UnbloatTV (Intelligent Android & Google TV Optimizer)...${NC}"

# 1. Check Python 3
if ! command -v python3 &>/dev/null; then
    echo -e "${RED}Error: Python 3 is not installed on this system.${NC}"
    echo "Please install Python 3.8+ and try again."
    exit 1
fi

# 2. Check or Guide ADB Installation
if ! command -v adb &>/dev/null; then
    # Check standard local paths
    if [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
        export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
    elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
        export PATH="$HOME/Android/Sdk/platform-tools:$PATH"
    elif [ -f "/opt/homebrew/bin/adb" ]; then
        export PATH="/opt/homebrew/bin:$PATH"
    elif [ -f "/usr/local/bin/adb" ]; then
        export PATH="/usr/local/bin:$PATH"
    else
        echo -e "${YELLOW}Warning: 'adb' command-line tool not found on PATH.${NC}"
        echo "Attempting to guide installation for your OS:"
        if [[ "$OSTYPE" == "darwin"* ]]; then
            echo -e "macOS detected. Run: ${GREEN}brew install --cask android-platform-tools${NC}"
        elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
            echo -e "Linux detected. Run: ${GREEN}sudo apt install adb${NC} or ${GREEN}sudo dnf install android-tools${NC}"
        else
            echo -e "Windows detected. Run: ${GREEN}winget install Google.PlatformTools${NC}"
        fi
        echo ""
    fi
fi

# Locate script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SCRIPT="$SCRIPT_DIR/unbloat.py"

if [ ! -f "$PYTHON_SCRIPT" ]; then
    echo -e "${YELLOW}unbloat.py not found locally. Downloading latest release...${NC}"
    curl -sSL "https://raw.githubusercontent.com/NeuroticWriter/UnbloatTV/main/unbloat.py" -o "/tmp/unbloat.py"
    PYTHON_SCRIPT="/tmp/unbloat.py"
fi

# Execute Python CLI forwarding all arguments
exec python3 "$PYTHON_SCRIPT" "$@"
