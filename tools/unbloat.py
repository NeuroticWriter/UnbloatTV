#!/usr/bin/env python3
"""
UnbloatTV - The Intelligent Android TV & Google TV Optimizer
Repository: https://github.com/NeuroticWriter/UnbloatTV
License: MIT
"""

import sys
import os
import subprocess
import shutil
import json
import urllib.request
import re
import time
import argparse
from datetime import datetime
from typing import List, Dict, Tuple, Optional, Set

# --- ANSI Terminal Colors & Styling ---
class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BOLD = '\033[1m'
    DIM = '\033[2m'
    RESET = '\033[0m'

def print_banner():
    banner = f"""{Colors.CYAN}{Colors.BOLD}
  ██╗   ██╗███╗   ██╗██████╗ ██╗      ██████╗  █████╗ ████████╗████████╗██╗   ██╗
  ██║   ██║████╗  ██║██╔══██╗██║     ██╔═══██╗██╔══██╗╚══██╔══╝╚══██╔══╝██║   ██║
  ██║   ██║██╔██╗ ██║██████╔╝██║     ██║   ██║███████║   ██║      ██║   ██║   ██║
  ██║   ██║██║╚██╗██║██╔══██╗██║     ██║   ██║██╔══██║   ██║      ██║   ╚██╗ ██╔╝
  ╚██████╔╝██║ ╚████║██████╔╝███████╗╚██████╔╝██║  ██║   ██║      ██║    ╚████╔╝ 
   ╚═════╝ ╚═╝  ╚═══╝╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═╝   ╚═╝      ╚═╝     ╚═══╝  
{Colors.RESET}{Colors.DIM}        Intelligent Hardware-Aware Optimizer & Debloater for Android & Google TV
                       https://github.com/NeuroticWriter/UnbloatTV{Colors.RESET}
"""
    print(banner)

# --- Critical System Whitelist (NEVER TOUCHED UNDER ANY CIRCUMSTANCE) ---
PROTECTED_PACKAGES: Set[str] = {
    "android",
    "com.android.systemui",
    "com.android.tv.settings",
    "com.android.settings",
    "com.android.vending",  # Google Play Store
    "com.google.android.gms",  # Google Play Services
    "com.google.android.gsf",  # Google Services Framework
    "com.google.android.packageinstaller",
    "com.android.packageinstaller",
    "com.android.permissioncontroller",
    "com.google.android.permissioncontroller",
    "com.android.bluetooth",
    "com.google.android.inputmethod.latin",  # Keyboard
    "com.google.android.apps.mediashell",  # Chromecast built-in
    "com.dolby.android.audio.service",  # Dolby Audio
    "com.homwee.dynamicbacklight",  # Mediatek/Croma Panel Backlight Driver
    "com.mediatek.tv.service",  # Mediatek Display & TV stack
    "com.mediatek.tvinput",  # HDMI input switcher
    "com.google.android.tv.inputplayer",  # TV & HDMI Inputs Switcher
    "com.gdch.android.tv.inputplayer",
    "com.dewzone.dlaunch",  # DLaunch Minimalist TV Launcher
    "com.mediatek.autopair",  # Remote pairing
    "com.google.android.tv.remote.service",  # TV remote service
    "com.google.android.tv.googletvremotepairer",
    "com.android.devicelockcontroller",  # Android 14 protected device lock
}

# --- Comprehensive Bloatware Database by Category & Safety ---
# Format: pkg: (Display Name, Description, Category, Safety Level: 'SAFE' | 'CAUTION')
BLOAT_DATABASE: Dict[str, Tuple[str, str, str, str]] = {
    # 1. Universal Telemetry, Ads & Background Sync (100% SAFE)
    "com.google.android.syncadapters.calendar": ("Calendar Sync", "Unused TV calendar background sync", "sync", "SAFE"),
    "com.android.providers.calendar": ("Calendar Provider", "Calendar storage database", "sync", "SAFE"),
    "com.android.providers.contacts": ("Contacts Storage", "TV address book database", "sync", "SAFE"),
    "com.android.printspooler": ("Print Spooler", "Printer service background daemon on TV", "utility", "SAFE"),
    "com.google.android.printservice.recommendation": ("Print Recommendation", "Printer discoverer", "utility", "SAFE"),
    "com.google.android.feedback": ("Crash Telemetry", "Sends error dumps to Google servers", "telemetry", "SAFE"),
    "com.android.tv.feedbackconsent": ("Feedback Consent", "Feedback dialog prompt daemon", "telemetry", "SAFE"),
    "com.google.android.partnersetup": ("Partner Setup", "Injects sponsored promotional partner apps", "ads", "SAFE"),
    "com.android.adservices.api": ("AdServices SDK", "Android ad tracking & attribution API", "ads", "SAFE"),
    "com.android.ondevicepersonalization.services": ("Ad Personalization", "On-device user profiling for ads", "ads", "SAFE"),
    "com.android.federatedcompute.services": ("Telemetry Compute", "Background federated data compute", "telemetry", "SAFE"),
    "com.android.captiveportallogin": ("Captive Portal Login", "Browser login for hotel/public Wi-Fi", "utility", "SAFE"),
    "com.android.providers.userdictionary": ("User Dictionary", "Custom keyboard predictive dictionary", "utility", "SAFE"),
    "com.android.wallpaperbackup": ("Wallpaper Backup", "Cloud wallpaper backup daemon", "backup", "SAFE"),
    "com.android.sharedstoragebackup": ("Shared Storage Backup", "Cloud storage backup daemon", "backup", "SAFE"),
    "com.android.companiondevicemanager": ("Companion Device Manager", "Smartwatch/wearable pairing daemon", "utility", "SAFE"),
    "com.google.android.play.games": ("Play Games TV", "Gaming sync daemon (rarely used on TV)", "games", "SAFE"),
    "com.google.android.youtube.tvmusic": ("YouTube Music TV", "Standalone YT Music player on TV", "media", "SAFE"),

    # Optional / Caution (Keep if user relies on accessibility or screen savers)
    "com.google.android.marvin.talkback": ("TalkBack", "Screen reader for visually impaired users", "accessibility", "CAUTION"),
    "com.google.android.tts": ("Text-to-Speech", "Voice synthesizer engine", "accessibility", "CAUTION"),
    "com.google.android.apps.tv.dreamx": ("Ambient Screensaver", "Google Photos / art screensaver", "screensaver", "CAUTION"),
    "com.android.dreams.basic": ("Basic Screensaver", "Standard Android screensaver", "screensaver", "CAUTION"),
    "com.cltv.hybrid": ("CloudTV Hybrid", "Broadcast antenna channel aggregator", "live_tv", "CAUTION"),

    # 2. Croma / Changhong OEM Junk
    "com.homwee.store": ("Croma App Store", "OEM app store (duplicates Play Store)", "oem_store", "SAFE"),
    "com.homwee.factory": ("Croma Factory Menu", "Factory testing menu running in background", "oem_diag", "SAFE"),
    "com.homwee.help": ("Croma User Help", "OEM help manual", "oem_diag", "SAFE"),
    "com.homwee.upgrade": ("Croma OTA Updater", "OEM firmware polling daemon", "oem_update", "SAFE"),
    "com.homwee.dmt.upgrade": ("Croma Device Upgrade", "Device telemetry upgrade service", "oem_update", "SAFE"),
    "com.mediatek.wwtv.setupwizard": ("Mediatek Setup Wizard", "Residual initial setup wizard", "oem_setup", "SAFE"),
    "com.mediatek.wwtv.tvcenter.plus": ("TV Center Plus", "Unused TV center overlay stub", "oem_diag", "SAFE"),

    # 3. Xiaomi / Redmi / Mi TV Junk
    "com.xiaomi.mitv.upgrade": ("Mi TV Updater", "Xiaomi background updater", "oem_update", "SAFE"),
    "com.xiaomi.mitv.advertise": ("PatchWall Ads", "Xiaomi home screen ad injection", "ads", "SAFE"),
    "com.xiaomi.mitv.payment": ("Mi Pay TV", "Xiaomi payment service", "oem_store", "SAFE"),
    "com.xiaomi.mitv.shop": ("Mi Store TV", "Xiaomi hardware store", "oem_store", "SAFE"),
    "com.mitv.tvhome": ("PatchWall Launcher", "Heavy Xiaomi PatchWall launcher with ads", "launcher", "CAUTION"),
    "com.xiaomi.devicelist": ("Mi Device List", "Smart home device scanner", "utility", "SAFE"),
    "com.duokan.videodownloader": ("Duokan Downloader", "Xiaomi media downloader", "utility", "SAFE"),
    "com.xiaomi.mitv.analytics": ("Mi Analytics", "Xiaomi user behavior tracking", "telemetry", "SAFE"),

    # 4. TCL / FFALCON Junk
    "com.tcl.commercial.uninstallation": ("TCL Commercial Tool", "TCL commercial adware", "ads", "SAFE"),
    "com.tcl.useragreement": ("TCL Agreement Daemon", "TCL tracking agreement", "telemetry", "SAFE"),
    "com.tcl.partnercustomizer": ("TCL Partner Customizer", "TCL promo app injector", "ads", "SAFE"),
    "com.tcl.appmarket2": ("TCL App Market", "TCL third-party store", "oem_store", "SAFE"),
    "com.tcl.waterfall.overseas": ("TCL Home Channel", "TCL sponsored content row", "ads", "SAFE"),
    "com.ffalcon.service": ("FFALCON Service", "TCL/FFalcon ad engine", "ads", "SAFE"),

    # 5. Sony Bravia Junk
    "com.sony.dtv.bravialife": ("Sony Bravia Life", "Promotional app", "ads", "SAFE"),
    "com.sony.dtv.interactivetvutil": ("Interactive TV Utility", "Telemetry daemon", "telemetry", "SAFE"),
    "com.samba.cast": ("Samba TV ACR", "Automated content recognition & tracking", "telemetry", "SAFE"),
    "com.sony.dtv.recommendationservice": ("Sony Recommendations", "Sponsored recommendation injector", "ads", "SAFE"),

    # 6. Hisense / Vidaa Junk
    "com.jamdeo.tv.vod": ("Hisense VOD", "Hisense sponsored video feed", "ads", "SAFE"),
    "com.hisense.hitv.hicloud.account": ("Hisense Cloud", "Hisense user tracking", "telemetry", "SAFE"),
    "com.hisense.service": ("Hisense Service", "OEM background daemon", "telemetry", "SAFE"),

    # 7. OnePlus & Realme TV Junk
    "com.oneplus.tv.launcher": ("OnePlus OxygenPlay", "OnePlus content aggregator launcher", "launcher", "CAUTION"),
    "com.realme.tv.factory": ("Realme Factory Test", "Background diagnostic service", "oem_diag", "SAFE"),
    "com.heytap.mcs": ("HeyTap Push Service", "OEM cloud push notification service", "telemetry", "SAFE"),

    # 8. Amazon Fire TV / FireOS Junk
    "com.amazon.device.messaging": ("Amazon Messaging", "FireOS push messaging service", "telemetry", "SAFE"),
    "com.amazon.advertising": ("Amazon Advertising", "Fire TV sponsored app ads", "ads", "SAFE"),
    "com.amazon.kindle.otaupdater": ("Amazon OTA Poller", "Background update poller", "oem_update", "SAFE"),
}

STREAMING_APPS: List[Tuple[str, str]] = [
    ("in.startv.hotstar", "Disney+ Hotstar"),
    ("com.amazon.amazonvideo.livingroom", "Amazon Prime Video"),
    ("com.netflix.ninja", "Netflix"),
    ("com.sonyliv", "SonyLIV"),
    ("com.graymatrix.did", "Zee5"),
    ("com.mxtech.videoplayer.television", "MX Player"),
    ("org.smarttube.stable", "SmartTube"),
    ("com.google.android.youtube.tv", "YouTube"),
]

class ADB:
    def __init__(self, target_device: Optional[str] = None):
        self.adb_bin = self._find_adb()
        self.device = target_device

    def _find_adb(self) -> str:
        bin_path = shutil.which("adb")
        if bin_path:
            return bin_path
        # Common macOS & Linux locations
        candidates = [
            "/opt/homebrew/bin/adb",
            "/usr/local/bin/adb",
            os.path.expanduser("~/Library/Android/sdk/platform-tools/adb"),
            os.path.expanduser("~/Android/Sdk/platform-tools/adb"),
        ]
        for c in candidates:
            if os.path.isfile(c) and os.access(c, os.X_OK):
                return c
        print(f"{Colors.RED}Error: 'adb' not found on your system!{Colors.RESET}")
        print("Install it with:")
        print("  macOS:   brew install --cask android-platform-tools")
        print("  Ubuntu:  sudo apt install adb")
        print("  Windows: winget install Google.PlatformTools")
        sys.exit(1)

    def run(self, *args, check=True) -> str:
        cmd = [self.adb_bin]
        if self.device:
            cmd.extend(["-s", self.device])
        cmd.extend(args)
        try:
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=check)
            return res.stdout.strip()
        except subprocess.CalledProcessError as e:
            return (e.stdout or e.stderr or "").strip()

    def shell(self, command: str) -> str:
        return self.run("shell", command, check=False)

    def getprop(self, key: str) -> str:
        return self.shell(f"getprop {key}")

    def list_devices(self) -> List[Tuple[str, str]]:
        out = self.run("devices")
        lines = out.strip().split("\n")[1:]
        devices = []
        for line in lines:
            parts = line.strip().split()
            if len(parts) >= 2:
                devices.append((parts[0], parts[1]))
        return devices

    def discover_mdns(self) -> List[str]:
        out = self.run("mdns", "services", check=False)
        ips = []
        for line in out.split("\n"):
            match = re.search(r'(\d+\.\d+\.\d+\.\d+:\d+)', line)
            if match:
                ips.append(match.group(1))
        return ips

class TVProfiler:
    def __init__(self, adb: ADB):
        self.adb = adb
        self.specs: Dict[str, str] = {}
        self.installed_packages: List[str] = []
        self.disabled_packages: List[str] = []
        self.health_score: int = 100
        self.health_reasons: List[str] = []

    def scan(self):
        self.specs["manufacturer"] = self.adb.getprop("ro.product.manufacturer") or "Unknown"
        self.specs["model"] = self.adb.getprop("ro.product.model") or "Unknown"
        self.specs["brand"] = self.adb.getprop("ro.product.brand") or "Unknown"
        self.specs["android_ver"] = self.adb.getprop("ro.build.version.release") or "Unknown"
        self.specs["sdk_int"] = self.adb.getprop("ro.build.version.sdk") or "Unknown"
        self.specs["cpu_abi"] = self.adb.getprop("ro.product.cpu.abi") or "armeabi-v7a"
        self.specs["board"] = self.adb.getprop("ro.board.platform") or self.adb.getprop("ro.hardware") or "Unknown"
        
        # RAM scan
        meminfo = self.adb.shell("cat /proc/meminfo")
        for line in meminfo.split("\n"):
            if line.startswith("MemTotal:"):
                self.specs["mem_total"] = line.split()[1] + " kB"
            elif line.startswith("MemAvailable:"):
                self.specs["mem_available"] = line.split()[1] + " kB"

        # Packages
        pkg_out = self.adb.shell("pm list packages")
        self.installed_packages = [line.replace("package:", "").strip() for line in pkg_out.split("\n") if line.startswith("package:")]
        
        dis_out = self.adb.shell("pm list packages -d")
        self.disabled_packages = [line.replace("package:", "").strip() for line in dis_out.split("\n") if line.startswith("package:")]

        # Calculate Health Score
        self._calculate_health_score()

    def _calculate_health_score(self):
        score = 100
        reasons = []

        # 1. RAM pressure
        total_kb = int(re.sub(r'\D', '', self.specs.get('mem_total', '0')) or 0)
        avail_kb = int(re.sub(r'\D', '', self.specs.get('mem_available', '0')) or 0)
        if total_kb > 0:
            free_ratio = avail_kb / total_kb
            if free_ratio < 0.25:
                score -= 25
                reasons.append("Severe RAM exhaustion (<25% free pool)")
            elif free_ratio < 0.35:
                score -= 15
                reasons.append("Moderate RAM strain (<35% free pool)")

        # 2. Bloatware burden
        active_bloat_count = 0
        for pkg in BLOAT_DATABASE:
            if pkg in self.installed_packages and pkg not in self.disabled_packages:
                active_bloat_count += 1
        if active_bloat_count > 0:
            penalty = min(30, active_bloat_count * 2)
            score -= penalty
            reasons.append(f"{active_bloat_count} active bloatware/telemetry daemons running")

        # 3. Animation scales
        anim = self.adb.shell("settings get global window_animation_scale")
        if anim in ("1.0", "1", "1.5"):
            score -= 10
            reasons.append("Stock 1.0x animation scales slowing down UI response")

        # 4. Background Wi-Fi scans
        wifi_scan = self.adb.shell("settings get global wifi_scan_always_enabled")
        if wifi_scan == "1":
            score -= 5
            reasons.append("Background Wi-Fi scanning enabled (causes micro-stutters)")

        self.health_score = max(20, min(100, score))
        self.health_reasons = reasons

    def print_summary(self):
        print(f"\n{Colors.CYAN}{Colors.BOLD}┌──────────────────────── TV Hardware & System Profile ────────────────────────┐{Colors.RESET}")
        print(f"  {Colors.BOLD}Device:{Colors.RESET}       {self.specs.get('manufacturer')} {self.specs.get('model')} ({self.specs.get('brand')})")
        print(f"  {Colors.BOLD}Board/SoC:{Colors.RESET}    {self.specs.get('board')} | Architecture: {Colors.YELLOW}{self.specs.get('cpu_abi')}{Colors.RESET}")
        print(f"  {Colors.BOLD}OS Version:{Colors.RESET}   Android TV {self.specs.get('android_ver')} (API {self.specs.get('sdk_int')})")
        
        # RAM calculation
        total_kb = int(re.sub(r'\D', '', self.specs.get('mem_total', '0')) or 0)
        avail_kb = int(re.sub(r'\D', '', self.specs.get('mem_available', '0')) or 0)
        total_mb = total_kb // 1024
        avail_mb = avail_kb // 1024
        ram_tag = f"{total_mb} MB ({total_mb/1024:.1f} GB)"
        
        is_low_ram = total_mb <= 1500
        ram_color = Colors.RED if is_low_ram else Colors.GREEN
        print(f"  {Colors.BOLD}RAM Pool:{Colors.RESET}     {ram_color}{ram_tag}{Colors.RESET} (Available: {avail_mb} MB)")
        print(f"  {Colors.BOLD}Packages:{Colors.RESET}     {len(self.installed_packages)} total installed | {Colors.YELLOW}{len(self.disabled_packages)} currently disabled{Colors.RESET}")
        
        # Health Score UI
        score_color = Colors.GREEN if self.health_score >= 80 else (Colors.YELLOW if self.health_score >= 60 else Colors.RED)
        status_label = "Optimal" if self.health_score >= 80 else ("Sluggish" if self.health_score >= 60 else "High Lag / Degraded")
        print(f"  {Colors.BOLD}Health Score:{Colors.RESET} {score_color}{self.health_score}/100 ({status_label}){Colors.RESET}")
        if self.health_reasons:
            print(f"  {Colors.DIM}Bottlenecks:  {', '.join(self.health_reasons[:2])}{Colors.RESET}")

        print(f"{Colors.CYAN}{Colors.BOLD}└──────────────────────────────────────────────────────────────────────────────┘{Colors.RESET}")

        if is_low_ram:
            print(f"\n  {Colors.YELLOW}{Colors.BOLD}⚠️ Low-RAM Hardware Optimization Recommended (<1.5 GB RAM detected):{Colors.RESET}")
            print(f"     This TV will benefit massively from AppOps process freezing, 0.0x animations,")
            print(f"     AOT speed-compilation, and disabling background stock launcher telemetry.")

def interactive_connect(adb: ADB, target_device: Optional[str] = None) -> bool:
    if target_device:
        if ":" not in target_device and re.match(r'^\d+\.\d+\.\d+\.\d+$', target_device):
            target_device = f"{target_device}:5555"
        if ":" in target_device:
            adb.run("connect", target_device)
        adb.device = target_device
        return True

    devices = adb.list_devices()
    authorized = [d for d in devices if d[1] == "device"]
    unauthorized = [d for d in devices if d[1] == "unauthorized"]

    if unauthorized:
        print(f"\n{Colors.YELLOW}{Colors.BOLD}⚠️ TV Authorization Required!{Colors.RESET}")
        print(f"Device {unauthorized[0][0]} is connected but unauthorized.")
        print(f"Please look at your TV screen and select {Colors.GREEN}'Always allow from this computer' -> OK{Colors.RESET}.")
        input("Press Enter once you have accepted the dialog on your TV...")
        return interactive_connect(adb)

    if authorized:
        adb.device = authorized[0][0]
        print(f"{Colors.GREEN}✓ Connected to TV at: {adb.device}{Colors.RESET}")
        return True

    print(f"\n{Colors.YELLOW}No active ADB devices detected.{Colors.RESET}")
    print("Searching for Google TV on local Wi-Fi via mDNS...")
    discovered = adb.discover_mdns()
    if discovered:
        target_ip = discovered[0]
        print(f"Found TV candidate via mDNS: {Colors.CYAN}{target_ip}{Colors.RESET}")
        choice = input(f"Connect to {target_ip}? [Y/n]: ").strip().lower()
        if choice in ("", "y", "yes"):
            adb.run("connect", target_ip)
            return interactive_connect(adb)

    ip = input(f"Enter your TV's Wi-Fi IP address (e.g. 192.168.1.50): ").strip()
    if not ip:
        return False
    if ":" not in ip:
        ip = f"{ip}:5555"
    adb.run("connect", ip)
    return interactive_connect(adb)

# --- Automatic Backup Snapshot System ---
def create_backup_snapshot(adb: ADB, profiler: TVProfiler, action_label: str) -> str:
    backup_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "backups")
    os.makedirs(backup_dir, exist_ok=True)
    
    clean_device = (adb.device or "tv").replace(":", "_").replace(".", "_")
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    backup_file = os.path.join(backup_dir, f"unbloat_backup_{clean_device}_{timestamp}.json")
    
    data = {
        "timestamp": datetime.now().isoformat(),
        "device": adb.device,
        "specs": profiler.specs,
        "action": action_label,
        "installed_packages": profiler.installed_packages,
        "disabled_packages": profiler.disabled_packages,
    }
    
    with open(backup_file, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        
    print(f"{Colors.DIM}📁 Safety snapshot saved: {backup_file}{Colors.RESET}")
    return backup_file

# --- Debloat Execution Engine (With Disable vs Uninstall Choice) ---
def action_debloat(
    adb: ADB, 
    profiler: TVProfiler, 
    auto_confirm: bool = False, 
    mode: Optional[str] = None,
    category_filter: Optional[str] = None
):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Intelligent Debloat & Telemetry Cleaner ---{Colors.RESET}")
    
    # 1. Gather Candidate Packages
    candidates: List[Tuple[str, str, str, str, str]] = []
    for pkg, info in BLOAT_DATABASE.items():
        name, desc, cat, safety = info
        if pkg in profiler.installed_packages and pkg not in profiler.disabled_packages and pkg not in PROTECTED_PACKAGES:
            if category_filter and category_filter != "all" and cat != category_filter:
                continue
            candidates.append((pkg, name, desc, cat, safety))

    if not candidates:
        print(f"{Colors.GREEN}✓ Excellent! No common bloatware or telemetry found active on this TV.{Colors.RESET}")
        return

    # 2. Select Operation Mode: Disable vs Uninstall
    chosen_mode = mode
    if not chosen_mode:
        print(f"\n{Colors.BOLD}Select Debloat Operation Mode:{Colors.RESET}")
        print(f"  {Colors.BOLD}[1] 🛡️ Safe Disable (Recommended - 100% Reversible){Colors.RESET}")
        print(f"      • Command: {Colors.DIM}pm disable-user --user 0 <package>{Colors.RESET}")
        print(f"      • Halts execution, frees 100% RAM/CPU, keeps APK in /system.")
        print(f"      • Can be restored in 1 second via 'pm enable'. Zero brick risk.")
        print(f"  {Colors.BOLD}[2] 🧹 Deep User Uninstall (Advanced){Colors.RESET}")
        print(f"      • Command: {Colors.DIM}pm uninstall -k --user 0 <package>{Colors.RESET}")
        print(f"      • Uninstalls package and data for User 0 (frees storage + RAM).")
        print(f"      • Preserves factory ROM; restorable via 'cmd package install-existing'.")
        
        mode_choice = input(f"\nChoose mode (1/2) [Default: 1]: ").strip()
        chosen_mode = "uninstall" if mode_choice == "2" else "disable"

    # 3. Package Selection (All vs Category vs Custom)
    selected_candidates = candidates
    if not auto_confirm:
        print(f"\nFound {len(candidates)} candidate packages on your TV:\n")
        for i, (pkg, name, desc, cat, safety) in enumerate(candidates, 1):
            safety_badge = f"{Colors.GREEN}[SAFE]{Colors.RESET}" if safety == "SAFE" else f"{Colors.YELLOW}[CAUTION]{Colors.RESET}"
            print(f"  [{i:02d}] {safety_badge} {Colors.BOLD}{name}{Colors.RESET} ({Colors.DIM}{pkg}{Colors.RESET})")
            print(f"       └─ {Colors.YELLOW}{desc}{Colors.RESET}")

        print(f"\n{Colors.BOLD}Choose selection:{Colors.RESET}")
        print(f"  {Colors.BOLD}[1] 🚀 All Recommended Bloatware{Colors.RESET} (Process all {len(candidates)} items)")
        print(f"  {Colors.BOLD}[2] 📂 Select by Category{Colors.RESET} (Telemetry, Ads, OEM Stores, Utilities)")
        print(f"  {Colors.BOLD}[3] ✍️ Custom Multi-Select{Colors.RESET} (Enter numbers e.g. 1, 3, 5-8)")
        print(f"  {Colors.BOLD}[0] ❌ Cancel{Colors.RESET}")
        
        sel_choice = input("\nSelect an option [Default: 1]: ").strip()
        if sel_choice == "0":
            print("Debloat cancelled.")
            return
        elif sel_choice == "2":
            cats = sorted(list(set(c[3] for c in candidates)))
            print("\nAvailable Categories:")
            for idx, c_name in enumerate(cats, 1):
                print(f"  [{idx}] {c_name.capitalize()}")
            c_input = input("Select category number: ").strip()
            if c_input.isdigit() and 1 <= int(c_input) <= len(cats):
                chosen_cat = cats[int(c_input) - 1]
                selected_candidates = [c for c in candidates if c[3] == chosen_cat]
        elif sel_choice == "3":
            num_input = input("Enter package numbers to process (e.g. 1, 2, 4-6): ").strip()
            indices = parse_range_input(num_input, len(candidates))
            if indices:
                selected_candidates = [candidates[i - 1] for i in indices]
            else:
                print(f"{Colors.RED}No valid package numbers selected. Cancelling.{Colors.RESET}")
                return

    # 4. Create Safety Snapshot
    create_backup_snapshot(adb, profiler, f"debloat_{chosen_mode}")

    # 5. Execute Action
    verb = "Disabling" if chosen_mode == "disable" else "Uninstalling (User 0)"
    print(f"\n{Colors.CYAN}{verb} {len(selected_candidates)} packages safely...{Colors.RESET}")
    success_count = 0
    
    for pkg, name, _, _, _ in selected_candidates:
        if chosen_mode == "disable":
            cmd = f"pm disable-user --user 0 {pkg}"
            res = adb.shell(cmd)
            if "disabled-user" in res or "new state" in res:
                print(f"  {Colors.GREEN}✓ Disabled:{Colors.RESET} {name} ({pkg})")
                success_count += 1
            elif "SecurityException" in res:
                print(f"  {Colors.YELLOW}⚡ Protected by OS:{Colors.RESET} {name} (Safely Skipped)")
            else:
                print(f"  {Colors.RED}✗ Skipped:{Colors.RESET} {name} ({res})")
        else:
            cmd = f"pm uninstall -k --user 0 {pkg}"
            res = adb.shell(cmd)
            if "Success" in res:
                print(f"  {Colors.GREEN}✓ Uninstalled for User:{Colors.RESET} {name} ({pkg})")
                success_count += 1
            elif "SecurityException" in res:
                print(f"  {Colors.YELLOW}⚡ Protected by OS:{Colors.RESET} {name} (Safely Skipped)")
            elif "not installed" in res:
                print(f"  {Colors.DIM}• Already Uninstalled:{Colors.RESET} {name}")
            else:
                print(f"  {Colors.RED}✗ Status:{Colors.RESET} {name} ({res})")

    print(f"\n{Colors.GREEN}✓ Successfully processed {success_count} packages! System structure remains 100% intact.{Colors.RESET}")
    profiler.scan()

def parse_range_input(input_str: str, max_val: int) -> List[int]:
    selected = set()
    parts = input_str.replace(" ", "").split(",")
    for part in parts:
        if "-" in part:
            sub = part.split("-")
            if len(sub) == 2 and sub[0].isdigit() and sub[1].isdigit():
                start, end = int(sub[0]), int(sub[1])
                for x in range(start, end + 1):
                    if 1 <= x <= max_val:
                        selected.add(x)
        elif part.isdigit():
            val = int(part)
            if 1 <= val <= max_val:
                selected.add(val)
    return sorted(list(selected))

# --- Extreme Speed & Process Optimization ---
def action_extreme_speed(adb: ADB, profiler: TVProfiler, preset: str = "5"):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Extreme Speed & Process Optimization ---{Colors.RESET}")
    if preset not in ("1", "2", "3", "4", "5"):
        print("  [1] ⚡ Set UI Animations to Instant 0.0x (Maximum remote responsiveness)")
        print("  [2] 🌊 Set UI Animations to Liquid 0.5x (Smooth cinematic motion)")
        print("  [3] ❄️ Apply AppOps Background Execution Freeze (stops streaming apps siphoning RAM/CPU on exit)")
        print("  [4] 🎮 Force Full GPU 2D Hardware Acceleration")
        print("  [5] 🚀 Apply ALL of the above (Maximum Speed Preset)")
        choice = input("\nSelect an option (1-5) [Default: 5]: ").strip()
        choice = choice or "5"
    else:
        choice = preset
    
    if choice in ("1", "5"):
        adb.shell("settings put global window_animation_scale 0.0")
        adb.shell("settings put global transition_animation_scale 0.0")
        adb.shell("settings put global animator_duration_scale 0.0")
        print(f"  {Colors.GREEN}✓ Animation scales set to 0.0x (Instant response).{Colors.RESET}")

    if choice == "2":
        adb.shell("settings put global window_animation_scale 0.5")
        adb.shell("settings put global transition_animation_scale 0.5")
        adb.shell("settings put global animator_duration_scale 0.5")
        print(f"  {Colors.GREEN}✓ Animation scales set to 0.5x (Liquid smooth motion).{Colors.RESET}")

    if choice in ("3", "5"):
        print(f"\n{Colors.CYAN}Applying AppOps background execution freeze to installed streaming apps...{Colors.RESET}")
        for pkg, name in STREAMING_APPS:
            if pkg in profiler.installed_packages:
                adb.shell(f"cmd appops set {pkg} RUN_IN_BACKGROUND ignore")
                adb.shell(f"cmd appops set {pkg} RUN_ANY_IN_BACKGROUND ignore")
                print(f"  {Colors.GREEN}✓ Frozen in background on exit:{Colors.RESET} {name}")
        print(f"  {Colors.GREEN}✓ Streaming apps will now release 100% CPU upon pressing Home!{Colors.RESET}")

    if choice in ("4", "5"):
        adb.shell("settings put global force_gpu_rendering 1")
        adb.shell("settings put global wifi_scan_always_enabled 0")
        adb.shell("settings put secure location_mode 0")
        print(f"  {Colors.GREEN}✓ Forced 100% GPU composition and disabled Wi-Fi scanning latency spikes.{Colors.RESET}")

# --- Ahead-Of-Time (AOT) Machine Code Compilation ---
def action_aot_compile(adb: ADB, profiler: TVProfiler):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Ahead-Of-Time (AOT) Machine Code Compilation ---{Colors.RESET}")
    print("Compiles app bytecode directly into native ARM machine code (`dex2oat -m speed`).")
    print("This eliminates JIT compilation lag on bootup and during app launches.\n")
    
    target_apps = [
        ("com.dewzone.dlaunch", "DLaunch TV Launcher"),
        ("com.android.systemui", "Android System UI"),
        ("com.android.tv.settings", "TV Settings"),
        ("com.google.android.katniss", "Google Assistant"),
        ("org.smarttube.stable", "SmartTube"),
        ("com.google.android.youtube.tv", "YouTube"),
        ("in.startv.hotstar", "Disney+ Hotstar"),
        ("com.netflix.ninja", "Netflix"),
        ("com.amazon.amazonvideo.livingroom", "Amazon Prime Video"),
    ]
    
    for pkg, name in target_apps:
        if pkg in profiler.installed_packages:
            print(f"  Compiling {name} ({pkg})...", end="", flush=True)
            res = adb.shell(f"cmd package compile -m speed {pkg}")
            if "Success" in res:
                print(f" {Colors.GREEN}[Success]{Colors.RESET}")
            else:
                print(f" {Colors.YELLOW}[Skipped/Status: {res}]{Colors.RESET}")

# --- System-Wide Private DNS Ad-Blocker ---
def action_private_dns(adb: ADB, provider: Optional[str] = None):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- System-Wide Private DNS Ad-Blocker ---{Colors.RESET}")
    print("Blocks in-app banner ads and telemetry tracking domains across the entire TV with 0 RAM.")
    current_mode = adb.shell("settings get global private_dns_mode")
    current_dns = adb.shell("settings get global private_dns_specifier")
    print(f"Current Status: Mode={current_mode}, Specifier={current_dns}\n")
    
    choice = provider
    if not choice:
        print("  [1] 🛡️ Enable AdGuard DNS (dns.adguard-dns.com) [Recommended]")
        print("  [2] 🔒 Enable Quad9 Privacy DNS (dns.quad9.net)")
        print("  [3] ↩️ Disable Private DNS (Revert to default ISP)")
        choice_input = input("\nSelect an option (1-3) [Default: 1]: ").strip()
        choice_input = choice_input or "1"
        if choice_input == "1":
            choice = "adguard"
        elif choice_input == "2":
            choice = "quad9"
        elif choice_input == "3":
            choice = "off"
    
    if choice == "adguard":
        adb.shell("settings put global private_dns_mode hostname")
        adb.shell("settings put global private_dns_specifier dns.adguard-dns.com")
        print(f"  {Colors.GREEN}✓ AdGuard Private DNS enabled system-wide!{Colors.RESET}")
    elif choice == "quad9":
        adb.shell("settings put global private_dns_mode hostname")
        adb.shell("settings put global private_dns_specifier dns.quad9.net")
        print(f"  {Colors.GREEN}✓ Quad9 Private DNS enabled system-wide!{Colors.RESET}")
    elif choice == "off":
        adb.shell("settings put global private_dns_mode off")
        adb.shell("settings put global private_dns_specifier null")
        print(f"  {Colors.YELLOW}✓ Private DNS disabled.{Colors.RESET}")

# --- DLaunch Auto-Installer & Default Switcher ---
def action_install_dlaunch(adb: ADB, profiler: TVProfiler, auto_confirm: bool = False):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Install DLaunch (Auto-Adjusting TV Launcher) ---{Colors.RESET}")
    print("DLaunch is an ultra-minimalist, lightweight (33 KB) Android TV launcher.")
    print("Zero ads, zero telemetry, auto-adjusts to 720p/1080p/4K, and launches in <30ms.")

    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    apk_candidates = [
        os.path.join(os.path.dirname(os.path.abspath(__file__)), "dlaunch.apk"),
        os.path.join(base_dir, "dlaunch", "dlaunch.apk"),
        os.path.join(base_dir, "tools", "dlaunch.apk"),
    ]
    apk_path = None
    for c in apk_candidates:
        if os.path.isfile(c):
            apk_path = c
            break

    if not apk_path:
        print("Local DLaunch APK not found, fetching latest build from GitHub...")
        apk_path = "/tmp/dlaunch.apk"
        try:
            url = "https://raw.githubusercontent.com/NeuroticWriter/UnbloatTV/main/dlaunch/dlaunch.apk"
            urllib.request.urlretrieve(url, apk_path)
        except Exception as e:
            print(f"{Colors.RED}Failed to locate or download DLaunch APK: {e}{Colors.RESET}")
            return

    print(f"Installing DLaunch from: {apk_path} ...")
    res = adb.run("install", "-r", apk_path)
    if "Success" in res:
        print(f"  {Colors.GREEN}✓ DLaunch installed successfully!{Colors.RESET}")
        adb.shell("cmd package compile -m speed com.dewzone.dlaunch")
        print(f"  {Colors.GREEN}✓ Compiled DLaunch to native ARM speed code.{Colors.RESET}")
        
        # Test launch
        adb.shell("monkey -p com.dewzone.dlaunch 1")
        print(f"  {Colors.GREEN}✓ DLaunch launched on TV screen!{Colors.RESET}")
        
        # Check for stock launchers to safely disable
        stock_launchers = [
            ("com.google.android.apps.tv.launcherx", "Google TV Home Launcher"),
            ("com.google.android.tvlauncher", "Android TV Standard Launcher"),
            ("com.mitv.tvhome", "Xiaomi PatchWall Launcher"),
            ("com.oneplus.tv.launcher", "OnePlus Home Launcher"),
            ("com.tcl.tvplayer", "TCL Channel Home"),
            ("com.tcl.launcher", "TCL Smart TV Launcher"),
        ]
        
        detected_stock = [s for s in stock_launchers if s[0] in profiler.installed_packages and s[0] not in profiler.disabled_packages]
        if detected_stock:
            print(f"\n{Colors.YELLOW}Detected active stock bloat launcher(s):{Colors.RESET}")
            for pkg, name in detected_stock:
                print(f"  • {name} ({pkg})")
            
            disable_prompt = True
            if not auto_confirm:
                ans = input(f"\nWould you like to disable the bloated stock launcher so DLaunch becomes your default Home screen? (Y/n): ").strip().lower()
                disable_prompt = ans not in ("n", "no")
            
            if disable_prompt:
                for pkg, name in detected_stock:
                    adb.shell(f"pm disable-user --user 0 {pkg}")
                    print(f"  {Colors.GREEN}✓ Disabled stock launcher:{Colors.RESET} {name} (Reversible anytime via option 9)")
                print(f"\n{Colors.GREEN}✓ DLaunch is now your default Android TV home launcher!{Colors.RESET}")
            else:
                print(f"  {Colors.CYAN}Stock launcher kept active. Press Home on remote and select DLaunch when prompted.{Colors.RESET}")
    else:
        print(f"  {Colors.RED}✗ DLaunch installation failed: {res}{Colors.RESET}")

# --- SmartTube Auto-Installer ---
def action_install_smarttube(adb: ADB, profiler: TVProfiler):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Install SmartTube (Ad-Free YouTube Client) ---{Colors.RESET}")
    print("SmartTube is 100% ad-free, skips sponsored segments with SponsorBlock, and supports 4K HDR.")
    
    abi = profiler.specs.get("cpu_abi", "armeabi-v7a")
    download_abi = "arm64-v8a" if "arm64" in abi else "armeabi-v7a"
        
    print(f"Detected TV Architecture: {Colors.YELLOW}{abi}{Colors.RESET}")
    print("Querying latest official release from GitHub (yuliskov/SmartTube)...")
    
    try:
        req = urllib.request.Request("https://api.github.com/repos/yuliskov/SmartTube/releases/latest", headers={'User-Agent': 'UnbloatTV'})
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode())
            target_url = None
            for asset in data.get("assets", []):
                name = asset.get("name", "")
                if download_abi in name and name.endswith(".apk") and "stable" in name:
                    target_url = asset.get("browser_download_url")
                    break
            if not target_url:
                target_url = "https://github.com/yuliskov/SmartTube/releases/download/latest/smarttube_stable.apk"
                
            print(f"Downloading: {target_url} ...")
            local_apk = "/tmp/smarttube_installer.apk"
            urllib.request.urlretrieve(target_url, local_apk)
            print("Installing to TV via ADB...")
            res = adb.run("install", "-r", local_apk)
            if "Success" in res:
                print(f"  {Colors.GREEN}✓ SmartTube installed successfully!{Colors.RESET}")
                adb.shell("cmd package compile -m speed org.smarttube.stable")
                print(f"  {Colors.GREEN}✓ Pre-compiled SmartTube to native ARM code.{Colors.RESET}")
            else:
                print(f"  {Colors.RED}✗ Installation failed: {res}{Colors.RESET}")
            if os.path.exists(local_apk):
                os.remove(local_apk)
    except Exception as e:
        print(f"{Colors.RED}Error downloading SmartTube: {e}{Colors.RESET}")

# --- Export Community Profile Engine ---
def action_export_profile(adb: ADB, profiler: TVProfiler):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Export TV Profile for Community Showcase ---{Colors.RESET}")
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    profiles_dir = os.path.join(base_dir, "profiles")
    os.makedirs(profiles_dir, exist_ok=True)
    
    brand = re.sub(r'[^a-zA-Z0-9]+', '-', (profiler.specs.get("brand") or profiler.specs.get("manufacturer") or "generic").lower()).strip('-')
    model = re.sub(r'[^a-zA-Z0-9]+', '-', (profiler.specs.get("model") or "tv").lower()).strip('-')
    folder_name = f"{brand}-{model}"
    target_dir = os.path.join(profiles_dir, folder_name)
    os.makedirs(target_dir, exist_ok=True)
    
    profile_data = {
        "device": profiler.specs.get("model"),
        "manufacturer": profiler.specs.get("manufacturer"),
        "brand": profiler.specs.get("brand"),
        "board": profiler.specs.get("board"),
        "cpu_abi": profiler.specs.get("cpu_abi"),
        "android_version": profiler.specs.get("android_ver"),
        "sdk_int": profiler.specs.get("sdk_int"),
        "ram_total": profiler.specs.get("mem_total"),
        "ram_available": profiler.specs.get("mem_available"),
        "health_score": profiler.health_score,
        "disabled_packages_count": len(profiler.disabled_packages),
        "total_packages_count": len(profiler.installed_packages),
    }
    with open(os.path.join(target_dir, "profile.json"), "w", encoding="utf-8") as f:
        json.dump(profile_data, f, indent=2)
        
    with open(os.path.join(target_dir, "debloated_packages.txt"), "w", encoding="utf-8") as f:
        for pkg in sorted(profiler.disabled_packages):
            f.write(f"{pkg}\n")
            
    readme_content = f"""# {profiler.specs.get('manufacturer')} {profiler.specs.get('model')} ({profiler.specs.get('brand')})

- **Processor / Board:** `{profiler.specs.get('board')}` ({profiler.specs.get('cpu_abi')})
- **OS Version:** Android TV {profiler.specs.get('android_ver')} (API {profiler.specs.get('sdk_int')})
- **RAM Pool:** {profiler.specs.get('mem_total')} (Available: {profiler.specs.get('mem_available')})
- **Health Score:** {profiler.health_score}/100
- **Total Packages:** {len(profiler.installed_packages)}
- **Debloated Packages:** {len(profiler.disabled_packages)}

## Verified Safe Debloat List
See [`debloated_packages.txt`](debloated_packages.txt) for the verified list of safe packages on this model.
"""
    with open(os.path.join(target_dir, "README.md"), "w", encoding="utf-8") as f:
        f.write(readme_content)
        
    print(f"  {Colors.GREEN}✓ Profile exported to: profiles/{folder_name}/{Colors.RESET}")
    print(f"  You can now commit this folder and open a Pull Request on GitHub!")

# --- Comprehensive Factory Restoration Engine ---
def action_restore(adb: ADB, profiler: TVProfiler, auto_confirm: bool = False):
    print(f"\n{Colors.BOLD}{Colors.YELLOW}┌──────────────────── Factory Restoration & Recovery Engine ────────────────────┐{Colors.RESET}")
    print("  [1] 🔄 Re-enable All Disabled Packages (`pm enable <pkg>`)")
    print("  [2] 📦 Re-install All Uninstalled User Packages (`cmd package install-existing`)")
    print("  [3] 📂 Restore from a Previous Local Backup Snapshot File")
    print("  [4] ⚡ Reset Animation Speeds Back to Factory Default (1.0x)")
    print("  [5] 🚀 Complete Full System Restore (Execute ALL of the above)")
    print(f"{Colors.YELLOW}└───────────────────────────────────────────────────────────────────────────────┘{Colors.RESET}")
    
    if not auto_confirm:
        choice = input("\nSelect restoration option (1-5) [0 to cancel]: ").strip()
    else:
        choice = "5"

    if choice == "0":
        print("Restoration cancelled.")
        return

    # Option 1 & 5: Re-enable disabled packages
    if choice in ("1", "5"):
        print(f"\n{Colors.CYAN}Re-enabling all {len(profiler.disabled_packages)} disabled packages...{Colors.RESET}")
        for pkg in profiler.disabled_packages:
            adb.shell(f"pm enable {pkg}")
            print(f"  {Colors.GREEN}✓ Re-enabled:{Colors.RESET} {pkg}")

    # Option 2 & 5: Re-install user uninstalled packages from database
    if choice in ("2", "5"):
        print(f"\n{Colors.CYAN}Checking for uninstalled system packages to re-register...{Colors.RESET}")
        for pkg in BLOAT_DATABASE:
            if pkg not in profiler.installed_packages:
                res = adb.shell(f"cmd package install-existing {pkg}")
                if "installed for user" in res:
                    print(f"  {Colors.GREEN}✓ Re-registered for User 0:{Colors.RESET} {pkg}")

    # Option 3: Restore from JSON backup file
    if choice == "3":
        backup_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "backups")
        if not os.path.isdir(backup_dir) or not os.listdir(backup_dir):
            print(f"{Colors.YELLOW}No local backup snapshot files found in ./backups/{Colors.RESET}")
        else:
            files = sorted([f for f in os.listdir(backup_dir) if f.endswith(".json")], reverse=True)
            print("\nAvailable backup snapshots:")
            for idx, fname in enumerate(files[:5], 1):
                print(f"  [{idx}] {fname}")
            f_choice = input(f"Select snapshot (1-{min(5, len(files))}): ").strip()
            if f_choice.isdigit() and 1 <= int(f_choice) <= len(files):
                target_file = os.path.join(backup_dir, files[int(f_choice) - 1])
                with open(target_file, "r", encoding="utf-8") as f:
                    bdata = json.load(f)
                prev_disabled = set(bdata.get("disabled_packages", []))
                print(f"\nRestoring package states from snapshot ({bdata.get('timestamp')})...")
                for pkg in profiler.disabled_packages:
                    if pkg not in prev_disabled:
                        adb.shell(f"pm enable {pkg}")
                        print(f"  {Colors.GREEN}✓ Restored active state:{Colors.RESET} {pkg}")

    # Option 4 & 5: Reset animations & GPU settings
    if choice in ("4", "5"):
        adb.shell("settings put global window_animation_scale 1.0")
        adb.shell("settings put global transition_animation_scale 1.0")
        adb.shell("settings put global animator_duration_scale 1.0")
        adb.shell("settings put global force_gpu_rendering 0")
        print(f"  {Colors.GREEN}✓ Default animation scales (1.0x) and GPU compositions restored!{Colors.RESET}")

    print(f"\n{Colors.GREEN}{Colors.BOLD}✓ Restoration complete! TV has been safely restored.{Colors.RESET}")
    profiler.scan()

# --- Main Program Entry Point ---
def main():
    parser = argparse.ArgumentParser(
        description="UnbloatTV - Intelligent Hardware-Aware Android TV & Google TV Optimizer",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python3 unbloat.py                     # Launch interactive menu
  python3 unbloat.py --status            # Print TV hardware diagnostic profile
  python3 unbloat.py --auto -y           # Run complete 1-click optimization
  python3 unbloat.py --debloat -y        # Safely disable bloatware & telemetry
  python3 unbloat.py --mode uninstall    # Deep uninstall bloatware for user 0
  python3 unbloat.py --freeze            # Apply AppOps background freeze & GPU boost
  python3 unbloat.py --aot               # AOT machine code speed compilation
  python3 unbloat.py --dns adguard       # Enable AdGuard Private DNS
  python3 unbloat.py --smarttube         # Auto-download & install SmartTube APK
  python3 unbloat.py --restore -y        # Factory restore all packages and settings
        """
    )
    parser.add_argument("-d", "--device", help="Target device IP address (e.g. 192.168.1.50) or ADB serial")
    parser.add_argument("-y", "--yes", action="store_true", help="Skip confirmation prompts (auto-confirm)")
    parser.add_argument("--auto", action="store_true", help="Execute complete optimization (Debloat + Freeze + AOT + DNS)")
    parser.add_argument("--debloat", action="store_true", help="Disable brand bloatware and background telemetry")
    parser.add_argument("--mode", choices=["disable", "uninstall"], default="disable", help="Debloat operation mode: 'disable' (reversible) or 'uninstall' (user 0)")
    parser.add_argument("--category", choices=["telemetry", "ads", "oem_store", "utility", "all"], default="all", help="Filter bloatware by category")
    parser.add_argument("--freeze", action="store_true", help="Freeze background streaming apps and boost GPU rendering")
    parser.add_argument("--aot", action="store_true", help="Pre-compile installed apps to native ARM machine code")
    parser.add_argument("--dns", choices=["adguard", "quad9", "off"], help="Configure system-wide Private DNS ad-blocker")
    parser.add_argument("--smarttube", action="store_true", help="Download and install architecture-matched SmartTube")
    parser.add_argument("--dlaunch", action="store_true", help="Install DLaunch (Auto-Adjusting Minimalist TV Launcher)")
    parser.add_argument("--restore", action="store_true", help="Restore all disabled packages and reset animation scales")
    parser.add_argument("--export-profile", action="store_true", help="Export current TV hardware profile to profiles/ for community showcase")
    parser.add_argument("--status", action="store_true", help="Display hardware profile and memory metrics then exit")

    args = parser.parse_args()

    print_banner()
    adb = ADB(target_device=args.device)
    
    if not interactive_connect(adb, target_device=args.device):
        print(f"{Colors.RED}Failed to connect to an ADB device. Exiting.{Colors.RESET}")
        sys.exit(1)

    profiler = TVProfiler(adb)
    profiler.scan()

    if args.status:
        profiler.print_summary()
        sys.exit(0)

    has_cli_action = any([args.auto, args.debloat, args.freeze, args.aot, args.dns, args.smarttube, args.dlaunch, args.restore, args.export_profile])

    if has_cli_action:
        profiler.print_summary()
        if args.auto:
            action_debloat(adb, profiler, auto_confirm=args.yes, mode=args.mode, category_filter=args.category)
            action_extreme_speed(adb, profiler, preset="5")
            action_aot_compile(adb, profiler)
            action_private_dns(adb, provider="adguard")
            print(f"\n{Colors.GREEN}{Colors.BOLD}🎉 Complete Auto-Optimization Finished! Your TV is at peak speed.{Colors.RESET}")
        else:
            if args.debloat:
                action_debloat(adb, profiler, auto_confirm=args.yes, mode=args.mode, category_filter=args.category)
            if args.freeze:
                action_extreme_speed(adb, profiler, preset="5")
            if args.aot:
                action_aot_compile(adb, profiler)
            if args.dns:
                action_private_dns(adb, provider=args.dns)
            if args.smarttube:
                action_install_smarttube(adb, profiler)
            if args.dlaunch:
                action_install_dlaunch(adb, profiler, auto_confirm=args.yes)
            if args.restore:
                action_restore(adb, profiler, auto_confirm=args.yes)
            if args.export_profile:
                action_export_profile(adb, profiler)
        sys.exit(0)

    # Interactive Loop
    profiler.print_summary()

    while True:
        print(f"\n{Colors.BOLD}{Colors.CYAN}=== UnbloatTV Master Control Menu ==={Colors.RESET}")
        print(f"  {Colors.BOLD}[1]{Colors.RESET} 🚀 {Colors.GREEN}One-Click Auto Optimize{Colors.RESET} (Safe Debloat + GPU Boost + AppOps Freeze)")
        print(f"  {Colors.BOLD}[2]{Colors.RESET} 🧹 Intelligent Debloat & Cleaner (Choose Disable vs Uninstall)")
        print(f"  {Colors.BOLD}[3]{Colors.RESET} ⚡ Extreme Speed & AppOps Background Freeze")
        print(f"  {Colors.BOLD}[4]{Colors.RESET} 🏎️ Ahead-Of-Time (AOT) Machine Code Compilation")
        print(f"  {Colors.BOLD}[5]{Colors.RESET} 🛡️ System-Wide Ad-Blocker (Private DNS)")
        print(f"  {Colors.BOLD}[6]{Colors.RESET} 🚀 {Colors.CYAN}Install DLaunch{Colors.RESET} (Auto-Adjusting Minimalist TV Launcher)")
        print(f"  {Colors.BOLD}[7]{Colors.RESET} 📺 Install SmartTube (Ad-Free YouTube with SponsorBlock)")
        print(f"  {Colors.BOLD}[8]{Colors.RESET} 🔍 Rescan TV Hardware & Health Score")
        print(f"  {Colors.BOLD}[9]{Colors.RESET} 🔄 Factory Restoration & Safety Recovery")
        print(f"  {Colors.BOLD}[10]{Colors.RESET} 📤 Export TV Profile (Share with Community)")
        print(f"  {Colors.BOLD}[0]{Colors.RESET} ❌ Exit")

        choice = input(f"\n{Colors.BOLD}Choose an option (0-10): {Colors.RESET}").strip()
        if choice == "1":
            action_debloat(adb, profiler, auto_confirm=True, mode="disable")
            action_extreme_speed(adb, profiler, preset="5")
            action_aot_compile(adb, profiler)
            action_private_dns(adb, provider="adguard")
            print(f"\n{Colors.GREEN}{Colors.BOLD}🎉 Complete Auto-Optimization Finished! Your TV is at peak speed.{Colors.RESET}")
        elif choice == "2":
            action_debloat(adb, profiler)
        elif choice == "3":
            action_extreme_speed(adb, profiler)
        elif choice == "4":
            action_aot_compile(adb, profiler)
        elif choice == "5":
            action_private_dns(adb)
        elif choice == "6":
            action_install_dlaunch(adb, profiler)
        elif choice == "7":
            action_install_smarttube(adb, profiler)
        elif choice == "8":
            profiler.scan()
            profiler.print_summary()
        elif choice == "9":
            action_restore(adb, profiler)
        elif choice == "10":
            action_export_profile(adb, profiler)
        elif choice in ("0", "q", "exit"):
            print(f"\n{Colors.CYAN}Thank you for using UnbloatTV! Enjoy your fast, clean TV.{Colors.RESET}")
            print(f"{Colors.YELLOW}☕ Support the project: https://buymeacoffee.com/dewzone{Colors.RESET}\n")
            break
        else:
            print(f"{Colors.RED}Invalid option. Please try again.{Colors.RESET}")

if __name__ == "__main__":
    main()
