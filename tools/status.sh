#!/usr/bin/env bash
# Quick Hardware Status & Health Score Scanner
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec python3 "$SCRIPT_DIR/unbloat.py" --status "$@"
