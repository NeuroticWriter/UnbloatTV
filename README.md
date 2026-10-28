# 📺 UnbloatTV

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Python 3.8+](https://img.shields.io/badge/python-3.8+-blue.svg)](https://www.python.org/downloads/)
[![Android TV](https://img.shields.io/badge/Android%20TV-9%20to%2014-green.svg)](https://android.com/tv/)
[![Google TV](https://img.shields.io/badge/Google%20TV-Compatible-orange.svg)](https://tv.google/)
[![Windows Compatible](https://img.shields.io/badge/Windows-unbloat.bat-0078D6.svg)]()
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20Python)-brightgreen.svg)]()
[![Reversible](https://img.shields.io/badge/Safety-100%25%20Reversible-success.svg)]()
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-Donate-yellow.svg?style=flat&logo=buy-me-a-coffee)](https://buymeacoffee.com/dewzone)

**The Intelligent, Hardware-Aware Optimizer & Debloater for Android TV & Google TV.**  
*Turn laggy, stuttering smart TVs into fluid, lightning-fast streaming machines in minutes.*

[Quick Start](#-quick-start) • [Disable vs Uninstall](#-choose-your-mode-disable-vs-uninstall) • [Why UnbloatTV?](#-why-unbloat-tv-vs-alternatives) • [Features](#-features) • [CLI Usage](#-command-line-options) • [AI Agent Protocol](#-ai-assistant--agent-prompt) • [Safety Guarantee](#-safety-guarantee)

</div>

---

```
  ██╗   ██╗███╗   ██╗██████╗ ██╗      ██████╗  █████╗ ████████╗████████╗██╗   ██╗
  ██║   ██║████╗  ██║██╔══██╗██║     ██╔═══██╗██╔══██╗╚══██╔══╝╚══██╔══╝██║   ██║
  ██║   ██║██╔██╗ ██║██████╔╝██║     ██║   ██║███████║   ██║      ██║   ██║   ██║
  ██║   ██║██║╚██╗██║██╔══██╗██║     ██║   ██║██╔══██║   ██║      ██║   ╚██╗ ██╔╝
  ╚██████╔╝██║ ╚████║██████╔╝███████╗╚██████╔╝██║  ██║   ██║      ██║    ╚████╔╝ 
   ╚═════╝ ╚═╝  ╚═══╝╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝      ╚═╝     ╚═══╝  
        Intelligent Hardware-Aware Optimizer & Debloater for Android & Google TV
```

## 💡 The Problem with Modern Smart TVs

Most budget and mid-range Android & Google TVs (Croma, Xiaomi, TCL, Hisense, OnePlus, Realme, and even Chromecast dongles) ship with **1 GB to 1.5 GB of RAM** and entry-level quad-core ARM processors. 

Out of the box, they are overwhelmed by:
1. **Manufacturer Bloatware:** Hidden background services, analytics, crash dumpers, and duplicate OEM app stores.
2. **Aggressive Google TV Ads & Promos:** Heavy video preview carousels constantly buffering in background RAM.
3. **Runaway Background Streaming Apps:** Apps like Hotstar, Prime Video, Netflix, and SonyLIV continue executing in the background after you return home, consuming up to 70% of remaining RAM.
4. **JIT Compilation Jitter:** Bytecode interpretation creates micro-stutters during menu navigation.

**UnbloatTV** solves this intelligently by analyzing your specific TV hardware (SoC architecture, memory pool, and installed brand packages) and applying surgical, customizable, and non-destructive optimizations.

---

## 🛡️ Choose Your Mode: Disable vs. Uninstall

UnbloatTV puts you in complete control. Whenever you debloat, you can choose between two safe operation modes:

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│ [Mode 1] 🛡️ Safe Disable (Recommended / 100% Safe)                                │
│   • Command: pm disable-user --user 0 <package>                                  │
│   • Keeps the APK file safely inside the read-only /system partition             │
│   • Completely stops the service from running in background (frees 100% RAM/CPU) │
│   • Can be re-enabled in 1 second via 'pm enable'. Zero risk to system updates    │
├──────────────────────────────────────────────────────────────────────────────────┤
│ [Mode 2] 🧹 Deep User Uninstall (Advanced Storage Cleaner)                       │
│   • Command: pm uninstall -k --user 0 <package>                                  │
│   • Uninstalls the package registration and clears local app cache for User 0    │
│   • Frees user storage partition space in addition to background RAM             │
│   • System APK is preserved in ROM; restorable via 'cmd package install-existing'│
└──────────────────────────────────────────────────────────────────────────────────┘
```

Every action automatically saves a timestamped JSON snapshot in `./backups/` so you can restore your original setup anytime!

---

## 🥊 Why UnbloatTV vs. Alternatives?

| Feature | **UnbloatTV** | **CleanYourTV** | **Generic ADB Scripts** |
| :--- | :---: | :---: | :---: |
| **Hardware Architecture Profiler** | ⚡ **Automatic** (RAM pool, CPU ABI, SoC) | ❌ None | ❌ None |
| **TV Health & Optimization Score** | 📊 **Live 0-100 Score** with diagnostics | ❌ None | ❌ None |
| **User Choice: Disable vs Uninstall** | 🛡️ **Full Choice** with safety guidance | ❌ Forces uninstall | ⚠️ Varies |
| **Model & Brand Specific Bloat** | ✅ Croma, Xiaomi, TCL, Sony, Hisense, OnePlus | ⚠️ Basic Android TV only | ❌ Hardcoded |
| **"Watch Next" & Channel Streaming** | 🌟 **Full Support** (Pairs with Projectivy) | ❌ Broken (Flat static dock) | ⚠️ Manual |
| **AppOps Background Process Freeze** | 🚀 **Yes** (Streaming apps freeze on exit) | ❌ No | ❌ No |
| **Ahead-Of-Time (AOT) ARM Compilation**| 🏎️ **Yes** (`dex2oat -m speed`) | ❌ No | ❌ No |
| **Zero-RAM AdGuard Private DNS** | 🛡️ **1-Click System-Wide** | ❌ No | ⚠️ Manual shell command |
| **Architecture-Matched SmartTube** | 📺 **Auto-fetches armeabi-v7a / arm64-v8a** | ❌ No | ❌ Manual sideload |
| **Safety Snapshots & Restore** | 🔒 **Auto-backup manifest + 1-Click Restore** | ❌ No backups | ⚠️ Risk of bootloop |
| **Cross-Platform Launchers** | 💻 **macOS/Linux (.sh) + Windows (.bat)** | ⚠️ Bash only | ⚠️ Varies |
| **External Dependencies** | 🟢 **Zero** (Pure Python 3 stdlib) | ⚠️ Node / Bash / Curl | ⚠️ Varies |
| **AI Agent Support** | 🤖 **Built-in `PROMPT.md` Protocol** | ⚠️ Generic prompt | ❌ None |

---

## ✨ Features

- 🔍 **Hardware & Memory Scanner:** Automatically inspects your TV's SoC (Mediatek, Amlogic, Realtek, Qualcomm), CPU ABI (`armeabi-v7a` or `arm64-v8a`), total physical RAM, available memory, and disabled package list.
- 📊 **Intelligent Health Score (0-100):** Evaluates system bottlenecks in real-time (RAM strain, active background bloatware count, animation speeds, and Wi-Fi scanning latency spikes).
- 🧹 **Granular Package Selection:**
  - **All Recommended:** 1-click clean of all verified bloatware.
  - **By Category:** Filter by Telemetry, Ads, OEM Stores, Utilities, or Media.
  - **Custom Range:** Toggle specific packages with simple number inputs (e.g. `1, 3, 5-8`).
- ⚡ **Extreme Speed Preset:** Sets UI animation scales to `0.0x` (instant touch/remote response) or `0.5x` (liquid glass motion) and forces full 2D GPU hardware acceleration.
- ❄️ **AppOps Background Freeze:** Configures `cmd appops set <pkg> RUN_IN_BACKGROUND ignore` on heavy streaming apps (Hotstar, Netflix, Amazon Prime, SonyLIV, Zee5, MX Player). When you press the Home button, apps immediately release CPU/RAM!
- 🏎️ **Ahead-Of-Time (AOT) Compilation:** Compiles high-priority TV apps directly into native ARM machine code using Android's `dex2oat` compiler (`cmd package compile -m speed`), eliminating startup lag.
- 🛡️ **System-Wide Ad-Blocking (0 MB RAM):** Enables encrypted Private DNS (AdGuard / Quad9) natively at the Android OS layer. Blocks banner ads and telemetry tracking across the entire TV without running heavy VPN background apps.
- 📺 **SmartTube Installer:** Auto-detects whether your TV runs 32-bit or 64-bit ARM, fetches the latest official release from GitHub, installs it via ADB, and pre-compiles it with AOT speed optimization.
- 🔄 **Advanced Factory Restoration:**
  - Re-enable all disabled packages (`pm enable`).
  - Re-install user uninstalled packages (`cmd package install-existing`).
  - Restore state from any saved JSON snapshot file.
  - Reset animations and GPU compositions back to factory default.

---

## 🚀 Quick Start

### Prerequisites
- Any computer (macOS, Linux, or Windows) connected to the **same Wi-Fi network** as your TV.
- **Python 3.8+** installed.
- **ADB (Android Debug Bridge)** installed:
  - macOS: `brew install --cask android-platform-tools`
  - Ubuntu/Debian: `sudo apt install adb`
  - Windows: `winget install Google.PlatformTools`

### 1. Enable USB / Network Debugging on your TV
1. Open TV **Settings** ⚙️ ➔ **System** ➔ **About**.
2. Scroll to **Android TV OS build** and click the remote button **7 times** until you see *"You are now a developer!"*.
3. Go back to **System** ➔ **Developer options**.
4. Enable **USB debugging** (and **Network debugging / Wireless debugging** if available).
5. Note your TV's Wi-Fi IP address in **Settings** ➔ **Network & Internet** (e.g. `192.168.1.50`).

### 2. Run UnbloatTV

#### On macOS & Linux (One-Liner):
```bash
curl -sSL https://raw.githubusercontent.com/NeuroticWriter/UnbloatTV/main/unbloat.sh | bash
```

#### On Windows:
Double-click `unbloat.bat` or run:
```cmd
git clone https://github.com/NeuroticWriter/UnbloatTV.git
cd UnbloatTV
unbloat.bat
```

#### On Any System via Python:
```bash
python3 unbloat.py
```

*When prompted on your TV screen, check **"Always allow from this computer"** and select **OK**.*

---

## 💻 Command-Line Options

UnbloatTV can be used either as an interactive terminal UI or in headless/automated scripts:

```
usage: unbloat.py [-h] [-d DEVICE] [-y] [--auto] [--debloat]
                  [--mode {disable,uninstall}]
                  [--category {telemetry,ads,oem_store,utility,all}]
                  [--freeze] [--aot] [--dns {adguard,quad9,off}] [--smarttube]
                  [--restore] [--status]

options:
  -h, --help            Show this help message and exit
  -d, --device DEVICE   Target TV IP address (e.g. 192.168.1.50) or ADB serial
  -y, --yes             Skip confirmation prompts (auto-confirm)
  --auto                Execute complete optimization (Debloat + Freeze + AOT + DNS)
  --debloat             Disable brand bloatware and background telemetry
  --mode {disable,uninstall}
                        Debloat operation mode: 'disable' (safe) or 'uninstall' (user 0)
  --category {telemetry,ads,oem_store,utility,all}
                        Filter bloatware by category
  --freeze              Freeze background streaming apps and boost GPU rendering
  --aot                 Pre-compile installed apps to native ARM machine code
  --dns {adguard,quad9,off}
                        Configure system-wide Private DNS ad-blocker
  --smarttube           Download and install architecture-matched SmartTube APK
  --restore             Restore all disabled packages and reset animation scales
  --status              Display hardware profile, Health Score, and memory metrics then exit
```

### Examples

```bash
# Check TV hardware specifications and Health Score
python3 unbloat.py --status

# Full 1-Click Safe Optimization (Debloat + Freeze + AOT + AdGuard DNS)
python3 unbloat.py --auto -y

# Deep uninstall telemetry and ads for User 0
python3 unbloat.py --debloat --mode uninstall --category ads -y

# Connect directly to a specific TV IP
python3 unbloat.py -d 192.168.1.50:5555 --auto -y

# Factory restore all packages and settings
python3 unbloat.py --restore -y
```

---

## 🤖 AI Assistant & Agent Prompt

Are you using an autonomous AI coding agent like **Claude Code**, **Gemini CLI**, **Antigravity**, **Cursor**, or **GitHub Copilot**?

UnbloatTV includes a dedicated [`PROMPT.md`](PROMPT.md) protocol file. You can simply feed it to your AI:

```markdown
Run the UnbloatTV protocol in PROMPT.md to inspect my connected Android TV and apply optimal hardware tuning.
```

Your AI assistant will autonomously scan your device, identify hardware bottlenecks, avoid critical system HALs, and optimize your TV safely.

---

## 🎨 Recommended UI: The Liquid Glass Setup

For the ultimate TV experience, combine UnbloatTV with **Projectivy Launcher**:

1. **Install Projectivy Launcher** from the Google Play Store on your TV.
2. Enable Projectivy Launcher's Accessibility Service:
   ```bash
   adb shell settings put secure enabled_accessibility_services com.spocky.projengmenu/.services.AccessibilityService
   adb shell settings put secure accessibility_enabled 1
   ```
3. Disable the laggy stock Google TV launcher:
   ```bash
   adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
   ```
4. In Projectivy settings:
   - Enable **Dynamic 4K Backdrops** with Reddit / Unsplash feeds.
   - Configure **Rounded Cards** and enable **Watch Next / Android TV Channels**.
   - Your TV will now launch instantly, display movie recommendations, and consume ~150 MB less RAM than stock Google TV!

---

## 🛡️ Safety Guarantee

UnbloatTV is engineered from the ground up for maximum safety:

- **Unbypassable System Whitelist:** Critical system packages (System UI, Settings, Package Installer, Dolby Audio, Bluetooth, Remote pairing services, and Mediatek panel backlight drivers) are hardcoded in `PROTECTED_PACKAGES` and can never be disabled or uninstalled.
- **Automated Safety Snapshots:** A JSON manifest of your installed and disabled package states is saved to `./backups/` before any changes are written.
- **100% Reversible:** Running `python3 unbloat.py --restore` instantly re-enables all packages or re-registers uninstalled packages via `cmd package install-existing`.

---

## ☕ Support & Donations

UnbloatTV is 100% free, open-source, and independently maintained. If it sped up your TV, gave you ad-free streaming, or saved you from buying expensive streaming hardware, consider supporting the project with a coffee! Every donation helps keep the project active and up to date.

<div align="center">

<a href="https://buymeacoffee.com/dewzone" target="_blank">
  <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="52" width="200">
</a>

[![Buy Me A Coffee](https://img.shields.io/badge/Donate-Buy%20Me%20A%20Coffee-yellow.svg?style=for-the-badge&logo=buy-me-a-coffee)](https://buymeacoffee.com/dewzone)

</div>

---

## 🤝 Contributing

Contributions are welcome! If you know of bloatware packages for specific TV manufacturers (Vu, Toshiba, Sharp, Philips, Motorola, etc.):
1. Fork this repository.
2. Add the package IDs and descriptions to `BLOAT_DATABASE` in `unbloat.py`.
3. Submit a Pull Request.

---

## 📜 License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for details.

---

<div align="center">
Made with ❤️ by <a href="https://github.com/NeuroticWriter">NeuroticWriter</a> for the Android TV Community.
</div>
