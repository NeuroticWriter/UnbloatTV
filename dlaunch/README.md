# DLaunch 🚀
### Ultra-Minimalist, Auto-Adjusting Android TV Launcher

**DLaunch** (`com.dewzone.dlaunch`) is a bespoke, featherweight Android TV home launcher designed specifically for **UnbloatTV**. Built with 100% native Android framework code, it provides instant responsiveness, zero bloat, and auto-adapts to any TV display resolution from 720p to 4K UHD.

<p align="center">
  <img src="../docs/dlaunch_live.png" alt="DLaunch Live TV Screenshot" width="850">
</p>

---

## ✨ Features

- **⚡ Ultra-Minimalist & Fast:** Compiled APK is only **33 KB** in size (vs 240 KB - 530 KB for CleanYourTV and 50+ MB for stock launchers). Launches in `<30ms` and uses `<10 MB RAM`.
- **📐 Auto-Adjusting Grid Engine:** Dynamically calculates display resolution, density (DPI), and aspect ratio. Automatically configures **4 columns on 720p**, **5 columns on 1080p**, and **6 columns on 4K UHD** so icons never stretch, crop, or look microscopic.
- **✨ Apple TV & Google TV Glassmorphism:** Ambient obsidian dark background (`#080C14`), frosted glass cards (`#141E2D`), hardware-accelerated 1.09x scale-up animation with vivid cyan border glow on remote focus, and smooth marquee text scrolling.
- **🕒 Top Shelf Quick Actions:** Integrated live digital clock (`TextClock`), full formatted date, instant TV Inputs switcher (HDMI 1, HDMI 2, AV), and TV settings.
- **⭐ Pinned Favorites Shelf:** Horizontal top shelf for your top daily streaming apps. Long-press with the remote to pin, unpin, or hide unwanted apps.
- **🛡️ Pure Leanback Standards:** Full D-pad remote navigation, hardware overscan safe margins (48dp/32dp), and safe remote Back button handling (never leaves the user stuck on a black screen).
- **🔒 Zero Ads, Zero Telemetry:** Pure local offline operation. No background network pings or data collection.

---

## 📦 Installation

### Option 1: 1-Click via UnbloatTV (Recommended)
From your computer:
```bash
# macOS / Linux
./unbloat.sh --dlaunch -y

# Windows PowerShell
.\unbloat.ps1 -dlaunch -y
```

### Option 2: Direct ADB Sideload
```bash
adb install -r dlaunch.apk
adb shell cmd package compile -m speed com.dewzone.dlaunch
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
```

---

## 🛠️ Building from Source

To compile and sign `dlaunch.apk` from source:
```bash
./build.sh
```
Requires Java 8+ and Android SDK platform-34 / build-tools 34+.
Builds and signs a production-ready release in `< 3 seconds`.

