# UnbloatTV - AI Assistant & Agent Optimization Protocol 🤖📺

> **For:** Claude Code, Gemini CLI, Antigravity, Cursor, GitHub Copilot, or any LLM agent with terminal access.
> **Repository:** [NeuroticWriter/UnbloatTV](https://github.com/NeuroticWriter/UnbloatTV)

---

## 🎯 Agent Mission

You are an expert Android TV / Google TV systems engineer. Your objective is to safely inspect, diagnose, and optimize the user's connected Android TV or Google TV box to achieve:
1. **Zero UI lag and micro-stutter** (liquid-smooth or instant 0.0x responsiveness).
2. **Maximum available RAM pool** (critical for 1 GB – 1.5 GB budget TVs).
3. **Background freeze on streaming apps** (stop Hotstar, Netflix, Prime, SonyLIV, Zee5 from siphoning CPU/RAM when minimized).
4. **Native ARM machine-code AOT pre-compilation** (`dex2oat -m speed`).
5. **System-wide zero-RAM ad blocking** via Private DNS.
6. **User Choice & Full Safety:** Support for both Safe Disable (`pm disable-user --user 0`) and Deep User Uninstall (`pm uninstall -k --user 0`) with automatic safety snapshots.

---

## 📋 Step-by-Step Autonomous Execution Workflow

### Step 1: Pre-Flight Connection & Diagnostic Hardware Profile

Run the following command to discover or connect to the TV and read its hardware specifications and Health Score:

```bash
# Check status and read hardware profile + Health Score
python3 unbloat.py --status
```

*If the TV's IP address is known (e.g., `192.168.1.50`):*
```bash
adb connect 192.168.1.50:5555
python3 unbloat.py -d 192.168.1.50:5555 --status
```

#### Hardware Analysis Checklist:
- **Processor / SoC Architecture:**
  - 32-bit ARM (`armeabi-v7a`): Typical on Mediatek MT58xx, Realtek RTD, or budget Amlogic. Requires lightweight binaries and aggressive AOT compilation.
  - 64-bit ARM (`arm64-v8a`): Typical on NVIDIA Shield, high-end Sony Bravia, or modern Google TV streamer boxes.
- **Physical RAM Pool & Health Score:**
  - **< 1.2 GB RAM (Ultra-Low RAM / 1 GB Pool):** Extreme priority! Must freeze background apps via AppOps, disable Google LauncherX recommendation daemons, and set animation scales to 0.0x.
  - **1.5 GB – 2.0 GB RAM:** Standard debloat, 0.5x liquid animations, AdGuard DNS.
  - **3.0+ GB RAM (High-End):** Mild debloat, AOT compilation, 0.5x liquid animations.
- **Android OS Version:**
  - Android TV 9 / 10 / 11 / 12 / 14 (API 28 - 34). Note that Android 14 protects certain core system controllers (`devicelockcontroller`), which UnbloatTV safely skips automatically.

---

### Step 2: Model & Brand Specific Vulnerabilities

Identify the manufacturer from `ro.product.manufacturer` or `ro.product.brand` and cross-reference with the model-specific guide below:

| Brand / Platform | Common SoC | Hidden Bloat / Bottleneck | Crucial Protection Rule |
| :--- | :--- | :--- | :--- |
| **Croma / Changhong** | Mediatek MT5867 / MT58xx | `com.homwee.store`, `com.homwee.upgrade`, `com.homwee.factory`, `com.cltv.hybrid` | **DO NOT TOUCH** `com.homwee.dynamicbacklight` (panel backlight will fail) or `com.mediatek.tvinput` (HDMI switcher). |
| **Xiaomi / Redmi / Mi TV** | Amlogic / Mediatek | `com.mitv.tvhome` (PatchWall), `com.xiaomi.mitv.advertise`, `com.xiaomi.mitv.analytics`, `com.xiaomi.mitv.payment` | Ensure alternative launcher (e.g. Projectivy) is active before disabling PatchWall. |
| **TCL / FFALCON** | Realtek / Mediatek | `com.tcl.commercial.uninstallation`, `com.tcl.partnercustomizer`, `com.ffalcon.service` | Keep `com.tcl.settings` and TCL display calibration daemons. |
| **Sony Bravia** | Mediatek / Cognitive XR | `com.samba.cast` (Samba ACR tracking), `com.sony.dtv.bravialife`, `com.sony.dtv.recommendationservice` | Never touch Sony picture processing engine or Bravia sync CEC. |
| **Hisense / Vidaa** | Novatek / Mediatek | `com.hisense.hitv.hicloud.account`, `com.jamdeo.tv.vod`, `com.hisense.service` | Keep Hisense picture mode selector. |
| **OnePlus / Realme TV** | Mediatek / Amlogic | `com.oneplus.tv.launcher`, `com.heytap.mcs`, `com.realme.tv.factory` | Keep OnePlus audio tuning & remote services. |
| **Chromecast with Google TV** | Amlogic S905X3 / S905X4 | `com.google.android.apps.tv.launcherx`, Google TV promotional video row autoplays | Projectivy Launcher replacement provides 3x speed boost. |

---

### Step 3: Execute Hardware-Aware Optimization

To run the complete automated suite non-interactively:

```bash
python3 unbloat.py --auto -y
```

#### Choose Between Debloat Modes:
- **Mode 1: Safe Disable (Default & Recommended)**
  ```bash
  python3 unbloat.py --debloat --mode disable -y
  ```
  *Halts background execution, frees 100% CPU/RAM, keeps APK in `/system` partition. Reversible in 1 second via `pm enable`.*
- **Mode 2: Deep User Uninstall**
  ```bash
  python3 unbloat.py --debloat --mode uninstall -y
  ```
  *Completely removes package registration and app data for User 0 (frees internal storage). Reversible via `cmd package install-existing`.*

#### Additional Granular Optimizations:
```bash
# 1. Freeze streaming apps in background & set 0.0x instant animations + GPU 2D boost
python3 unbloat.py --freeze

# 2. Compile high-priority apps to native ARM machine code
python3 unbloat.py --aot

# 3. Enable zero-RAM AdGuard Private DNS
python3 unbloat.py --dns adguard

# 4. Download and install architecture-matched SmartTube APK
python3 unbloat.py --smarttube
```

---

### Step 4: UI/UX Modernization (Projectivy Launcher Setup)

For the ultimate experience with dynamic 4K backdrops and "Watch Next" movie carousels:
1. Verify if Projectivy Launcher is installed:
   ```bash
   adb shell pm list packages | grep projengmenu
   ```
2. If installed, set Projectivy as the default launcher override:
   - Enable Accessibility service:
     ```bash
     adb shell settings put secure enabled_accessibility_services com.spocky.projengmenu/.services.AccessibilityService
     adb shell settings put secure accessibility_enabled 1
     ```
   - Disable stock heavy Google TV Launcher:
     ```bash
     adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
     ```
3. Verify on TV that the home button lands smoothly on Projectivy Launcher.

---

### Step 5: Post-Optimization Verification

Run memory audit and confirm improvements:
```bash
adb shell dumpsys meminfo
python3 unbloat.py --status
```
Verify that:
- Available RAM has increased (+50 MB to +150 MB).
- TV Health Score improved (e.g. from 55/100 to 90+/100).
- Stock animation scale is `0.0` (`adb shell settings get global window_animation_scale`).
- Private DNS is active (`adb shell settings get global private_dns_specifier`).
- Safety snapshot file exists in `./backups/`.
- No critical TV functions (HDMI, volume, Wi-Fi, audio) are broken.

---

## 🛡️ Emergency Restoration Protocol

If the user ever reports an issue or wants factory stock behavior:
```bash
python3 unbloat.py --restore -y
```
This re-enables all disabled packages, re-registers any uninstalled user packages, and resets animation scales to 1.0x with zero data loss.
