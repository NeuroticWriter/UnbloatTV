# AGENTS.md

Instructions for AI coding agents (Claude Code, Codex, Gemini CLI, Cursor, Copilot, Antigravity) working in this repository.

---

## 🎯 Primary Directives

When asked to inspect, debloat, or optimize an Android TV or Google TV:
1. **Always use the built-in tool:** Run [`tools/unbloat.py`](tools/unbloat.py) or execute it via `./unbloat.sh`. Do not write ad-hoc scripts that risk bypassing the hardware whitelist.
2. **Never touch critical system HALs:** Packages in `PROTECTED_PACKAGES` (panel backlight controller, HDMI input switchers, Bluetooth remote pairing, Dolby Audio, Wi-Fi, Package Installer, System UI) must **never** be disabled or uninstalled.
3. **Prefer Safe Disable over Deep Uninstall:** Default to `pm disable-user --user 0` so any modification is 100% reversible in 1 second. Only use `uninstall` if the user explicitly requests storage partition cleanup.
4. **Preserve Content Discovery:** Do not strip down the launcher into a blank static dock unless requested. Pair with Projectivy Launcher to preserve dynamic "Watch Next" movie carousels.

---

## 🛠️ CLI Quick Reference for Agents

```bash
# 1. Connect and inspect hardware + compute live Health Score (0-100)
python3 tools/unbloat.py --status

# 2. Complete 1-Click Automated Tuning (Safe Debloat + AppOps Freeze + AOT + AdGuard DNS)
python3 tools/unbloat.py --auto -y

# 3. Safe Debloat Only (Brand-specific telemetry and junk)
python3 tools/unbloat.py --debloat --mode disable -y

# 4. Background Process Freezing & GPU Boost (0.0x / 0.5x animations)
python3 tools/unbloat.py --freeze

# 5. Ahead-Of-Time (AOT) Machine Code Compilation
python3 tools/unbloat.py --aot

# 6. System-Wide Ad-Blocker (0 MB RAM Private DNS)
python3 tools/unbloat.py --dns adguard

# 7. Install Architecture-Matched SmartTube (with SponsorBlock)
python3 tools/unbloat.py --smarttube

# 8. Full Factory Emergency Restoration
python3 tools/unbloat.py --restore -y
```

---

## 🛡️ Safety & Whitelist Rules

- **Android 14+ Guard:** Modern Android TV OS versions enforce restrictions on `com.android.devicelockcontroller`. `tools/unbloat.py` catches `SecurityException` automatically. Never force-delete system partition files.
- **Automated Snapshots:** Safety snapshots are generated in `./backups/` before any changes are written. Always confirm snapshots are created before large batch operations.
- **TV HDMI / Tuner Guard:** Do not disable `com.mediatek.tvinput`, `com.cltv.hybrid`, or `com.google.android.tv.inputplayer` unless the user exclusively uses HDMI streaming sticks and has agreed.
