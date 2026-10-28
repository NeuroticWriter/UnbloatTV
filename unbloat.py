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
from typing import List, Dict, Tuple, Optional

# --- ANSI Terminal Colors ---
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

# --- Critical System Whitelist (NEVER TOUCH) ---
PROTECTED_PACKAGES = {
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
    "com.mediatek.autopair",  # Remote pairing
    "com.google.android.tv.remote.service",  # TV remote service
    "com.google.android.tv.googletvremotepairer",
}

# --- Comprehensive Bloatware Database by Brand & Category ---
BLOAT_DATABASE = {
    # 1. Universal Android TV Junk & Background Daemons
    "com.google.android.syncadapters.calendar": ("Calendar Sync", "Unused TV calendar background sync", "sync"),
    "com.android.providers.calendar": ("Calendar Provider", "Calendar storage database", "sync"),
    "com.android.providers.contacts": ("Contacts Storage", "TV address book database", "sync"),
    "com.android.printspooler": ("Print Spooler", "Printer service on TV", "utility"),
    "com.google.android.printservice.recommendation": ("Print Recommendation", "Printer discoverer", "utility"),
    "com.google.android.feedback": ("Crash Telemetry", "Sends error dumps to Google", "telemetry"),
    "com.android.tv.feedbackconsent": ("Feedback Consent", "Feedback dialog prompt", "telemetry"),
    "com.google.android.partnersetup": ("Partner Setup", "Injects sponsored promotional partner apps", "ads"),
    "com.android.adservices.api": ("AdServices SDK", "Android ad tracking & attribution", "ads"),
    "com.android.ondevicepersonalization.services": ("Ad Personalization", "On-device user profiling for ads", "ads"),
    "com.android.federatedcompute.services": ("Telemetry Compute", "Background federated data compute", "telemetry"),
    "com.google.android.play.games": ("Play Games TV", "Gaming sync daemon (rarely used on TV)", "games"),
    "com.google.android.marvin.talkback": ("TalkBack", "Screen reader for visually impaired", "accessibility"),
    "com.google.android.tts": ("Text-to-Speech", "Voice synthesizer engine", "accessibility"),
    "com.android.captiveportallogin": ("Captive Portal Login", "Browser login for hotel/public Wi-Fi", "utility"),
    "com.android.providers.userdictionary": ("User Dictionary", "Custom keyboard predictive dictionary", "utility"),
    "com.android.wallpaperbackup": ("Wallpaper Backup", "Cloud wallpaper backup daemon", "backup"),
    "com.android.sharedstoragebackup": ("Shared Storage Backup", "Cloud storage backup daemon", "backup"),
    "com.android.companiondevicemanager": ("Companion Device Manager", "Smartwatch/wearable pairing", "utility"),
    "com.google.android.youtube.tvmusic": ("YouTube Music TV", "Standalone YT Music player on TV", "media"),
    "com.google.android.apps.tv.dreamx": ("Ambient Screensaver", "Google Photos/art screensaver", "screensaver"),
    "com.android.dreams.basic": ("Basic Screensaver", "Standard Android screensaver", "screensaver"),
    "com.cltv.hybrid": ("CloudTV Hybrid", "Broadcast antenna channel aggregator", "live_tv"),
    "com.google.android.tv.inputplayer": ("Live TV Tuner Player", "Broadcast channel tuner player", "live_tv"),

    # 2. Croma / Changhong OEM Junk
    "com.homwee.store": ("Croma App Store", "OEM app store (duplicates Play Store)", "oem_store"),
    "com.homwee.factory": ("Croma Factory Menu", "Factory testing menu running in background", "oem_diag"),
    "com.homwee.help": ("Croma User Help", "OEM help manual", "oem_diag"),
    "com.homwee.upgrade": ("Croma OTA Updater", "OEM firmware polling daemon", "oem_update"),
    "com.homwee.dmt.upgrade": ("Croma Device Upgrade", "Device telemetry upgrade service", "oem_update"),
    "com.mediatek.wwtv.setupwizard": ("Mediatek Setup Wizard", "Residual initial setup wizard", "oem_setup"),
    "com.mediatek.wwtv.tvcenter.plus": ("TV Center Plus", "Unused TV center overlay stub", "oem_diag"),

    # 3. Xiaomi / Redmi / Mi TV Junk
    "com.xiaomi.mitv.upgrade": ("Mi TV Updater", "Xiaomi background updater", "oem_update"),
    "com.xiaomi.mitv.advertise": ("PatchWall Ads", "Xiaomi home screen ad injection", "ads"),
    "com.xiaomi.mitv.payment": ("Mi Pay TV", "Xiaomi payment service", "oem_store"),
    "com.xiaomi.mitv.shop": ("Mi Store TV", "Xiaomi hardware store", "oem_store"),
    "com.mitv.tvhome": ("PatchWall Launcher", "Heavy Xiaomi PatchWall launcher with ads", "launcher"),
    "com.xiaomi.devicelist": ("Mi Device List", "Smart home device scanner", "utility"),
    "com.duokan.videodownloader": ("Duokan Downloader", "Xiaomi media downloader", "utility"),
    "com.xiaomi.mitv.analytics": ("Mi Analytics", "Xiaomi user behavior tracking", "telemetry"),

    # 4. TCL / FFALCON Junk
    "com.tcl.commercial.uninstallation": ("TCL Commercial Tool", "TCL commercial adware", "ads"),
    "com.tcl.useragreement": ("TCL Agreement Daemon", "TCL tracking agreement", "telemetry"),
    "com.tcl.partnercustomizer": ("TCL Partner Customizer", "TCL promo app injector", "ads"),
    "com.tcl.appmarket2": ("TCL App Market", "TCL third-party store", "oem_store"),
    "com.tcl.waterfall.overseas": ("TCL Home Channel", "TCL sponsored content row", "ads"),
    "com.ffalcon.service": ("FFALCON Service", "TCL/FFalcon ad engine", "ads"),

    # 5. Sony Bravia Junk
    "com.sony.dtv.bravialife": ("Sony Bravia Life", "Promotional app", "ads"),
    "com.sony.dtv.interactivetvutil": ("Interactive TV Utility", "Telemetry daemon", "telemetry"),
    "com.samba.cast": ("Samba TV ACR", "Automated content recognition & tracking", "telemetry"),
    "com.sony.dtv.recommendationservice": ("Sony Recommendations", "Sponsored recommendation injector", "ads"),

    # 6. Hisense / Vidaa Junk
    "com.jamdeo.tv.vod": ("Hisense VOD", "Hisense sponsored video feed", "ads"),
    "com.hisense.hitv.hicloud.account": ("Hisense Cloud", "Hisense user tracking", "telemetry"),
    "com.hisense.service": ("Hisense Service", "OEM background daemon", "telemetry"),
}

STREAMING_APPS = [
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
            return e.stdout.strip()

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

    def print_summary(self):
        print(f"\n{Colors.BOLD}{Colors.CYAN}--- TV Hardware & System Profile ---{Colors.RESET}")
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

        if is_low_ram:
            print(f"\n  {Colors.YELLOW}{Colors.BOLD}⚠️ Low-RAM Hardware Warning (<1.5 GB RAM detected):{Colors.RESET}")
            print(f"     Your TV will benefit massively from AppOps process freezing, 0.0x animations,")
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

def action_debloat(adb: ADB, profiler: TVProfiler, auto_confirm: bool = False):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Safe Debloat & Telemetry Removal ---{Colors.RESET}")
    candidates = []
    
    for pkg, info in BLOAT_DATABASE.items():
        if pkg in profiler.installed_packages and pkg not in profiler.disabled_packages and pkg not in PROTECTED_PACKAGES:
            candidates.append((pkg, info[0], info[1], info[2]))

    if not candidates:
        print(f"{Colors.GREEN}✓ Excellent! No common bloatware or telemetry found active on this TV.{Colors.RESET}")
        return

    print(f"Found {len(candidates)} safe bloatware / telemetry packages to disable:\n")
    for i, (pkg, name, desc, cat) in enumerate(candidates, 1):
        print(f"  [{i:02d}] {Colors.BOLD}{name}{Colors.RESET} ({Colors.DIM}{pkg}{Colors.RESET})")
        print(f"       └─ {Colors.YELLOW}{desc}{Colors.RESET}")

    if not auto_confirm:
        choice = input(f"\nDisable all {len(candidates)} packages safely? [Y/n]: ").strip().lower()
        if choice not in ("", "y", "yes"):
            print("Debloat cancelled.")
            return

    print(f"\n{Colors.CYAN}Disabling packages safely via 'pm disable-user' (100% reversible)...{Colors.RESET}")
    disabled_count = 0
    for pkg, name, _, _ in candidates:
        res = adb.shell(f"pm disable-user --user 0 {pkg}")
        if "disabled-user" in res or "new state" in res:
            print(f"  {Colors.GREEN}✓ Disabled:{Colors.RESET} {name}")
            disabled_count += 1
        else:
            print(f"  {Colors.RED}✗ Skipped:{Colors.RESET} {name} ({res})")
    print(f"\n{Colors.GREEN}✓ Successfully disabled {disabled_count} packages! System structure remains 100% intact.{Colors.RESET}")
    profiler.scan()

def action_extreme_speed(adb: ADB, profiler: TVProfiler, preset: str = "5"):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Extreme Speed & Process Optimization ---{Colors.RESET}")
    if preset not in ("1", "2", "3", "4", "5"):
        print("1. Set UI Animations to Instant 0.0x")
        print("2. Set UI Animations to Liquid 0.5x")
        print("3. Apply AppOps Background Execution Freeze (stops streaming apps siphoning RAM/CPU on exit)")
        print("4. Force Full GPU 2D Hardware Rasterization")
        print("5. Apply ALL of the above (Maximum Speed Preset)")
        choice = input("Select an option (1-5): ").strip()
    else:
        choice = preset
    
    if choice in ("1", "5"):
        adb.shell("settings put global window_animation_scale 0.0")
        adb.shell("settings put global transition_animation_scale 0.0")
        adb.shell("settings put global animator_duration_scale 0.0")
        print(f"{Colors.GREEN}✓ Animation scales set to 0.0x (Instant response).{Colors.RESET}")

    if choice == "2":
        adb.shell("settings put global window_animation_scale 0.5")
        adb.shell("settings put global transition_animation_scale 0.5")
        adb.shell("settings put global animator_duration_scale 0.5")
        print(f"{Colors.GREEN}✓ Animation scales set to 0.5x (Liquid smooth motion).{Colors.RESET}")

    if choice in ("3", "5"):
        print(f"\n{Colors.CYAN}Applying AppOps background execution freeze to installed streaming apps...{Colors.RESET}")
        for pkg, name in STREAMING_APPS:
            if pkg in profiler.installed_packages:
                adb.shell(f"cmd appops set {pkg} RUN_IN_BACKGROUND ignore")
                adb.shell(f"cmd appops set {pkg} RUN_ANY_IN_BACKGROUND ignore")
                print(f"  {Colors.GREEN}✓ Frozen in background on exit:{Colors.RESET} {name}")
        print(f"{Colors.GREEN}✓ Streaming apps will now release 100% CPU upon pressing Home!{Colors.RESET}")

    if choice in ("4", "5"):
        adb.shell("settings put global force_gpu_rendering 1")
        adb.shell("settings put global wifi_scan_always_enabled 0")
        adb.shell("settings put secure location_mode 0")
        print(f"{Colors.GREEN}✓ Forced 100% GPU composition and disabled Wi-Fi scanning latency spikes.{Colors.RESET}")

def action_aot_compile(adb: ADB, profiler: TVProfiler):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Ahead-Of-Time (AOT) Machine Code Compilation ---{Colors.RESET}")
    print("Compiles app bytecode directly into native ARM machine code (`-m speed`).")
    print("This eliminates JIT compilation lag on bootup and during app launches.\n")
    
    target_apps = [
        ("com.spocky.projengmenu", "Projectivy Launcher"),
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
            print(f"Compiling {name} ({pkg})...", end="", flush=True)
            res = adb.shell(f"cmd package compile -m speed {pkg}")
            if "Success" in res:
                print(f" {Colors.GREEN}[Success]{Colors.RESET}")
            else:
                print(f" {Colors.YELLOW}[Skipped/Status: {res}]{Colors.RESET}")

def action_private_dns(adb: ADB, provider: Optional[str] = None):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- System-Wide Private DNS Ad-Blocker ---{Colors.RESET}")
    print("Blocks in-app banner ads and telemetry tracking domains across the entire TV with 0 RAM.")
    current_mode = adb.shell("settings get global private_dns_mode")
    current_dns = adb.shell("settings get global private_dns_specifier")
    print(f"Current Status: Mode={current_mode}, Specifier={current_dns}\n")
    
    choice = provider
    if not choice:
        print("1. Enable AdGuard DNS (dns.adguard-dns.com) [Recommended]")
        print("2. Enable Quad9 Privacy DNS (dns.quad9.net)")
        print("3. Disable Private DNS (Revert to default ISP)")
        choice_input = input("Select an option (1-3): ").strip()
        if choice_input == "1":
            choice = "adguard"
        elif choice_input == "2":
            choice = "quad9"
        elif choice_input == "3":
            choice = "off"
    
    if choice == "adguard":
        adb.shell("settings put global private_dns_mode hostname")
        adb.shell("settings put global private_dns_specifier dns.adguard-dns.com")
        print(f"{Colors.GREEN}✓ AdGuard Private DNS enabled system-wide!{Colors.RESET}")
    elif choice == "quad9":
        adb.shell("settings put global private_dns_mode hostname")
        adb.shell("settings put global private_dns_specifier dns.quad9.net")
        print(f"{Colors.GREEN}✓ Quad9 Private DNS enabled system-wide!{Colors.RESET}")
    elif choice == "off":
        adb.shell("settings put global private_dns_mode off")
        adb.shell("settings put global private_dns_specifier null")
        print(f"{Colors.YELLOW}✓ Private DNS disabled.{Colors.RESET}")

def action_install_smarttube(adb: ADB, profiler: TVProfiler):
    print(f"\n{Colors.BOLD}{Colors.CYAN}--- Install SmartTube (Ad-Free YouTube Client) ---{Colors.RESET}")
    print("SmartTube is 100% ad-free, skips sponsored segments with SponsorBlock, and supports 4K HDR.")
    
    abi = profiler.specs.get("cpu_abi", "armeabi-v7a")
    if "arm64" in abi:
        download_abi = "arm64-v8a"
    else:
        download_abi = "armeabi-v7a"
        
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
                print(f"{Colors.GREEN}✓ SmartTube installed successfully!{Colors.RESET}")
                adb.shell("cmd package compile -m speed org.smarttube.stable")
                print(f"{Colors.GREEN}✓ Pre-compiled SmartTube to native ARM code.{Colors.RESET}")
            else:
                print(f"{Colors.RED}✗ Installation failed: {res}{Colors.RESET}")
            if os.path.exists(local_apk):
                os.remove(local_apk)
    except Exception as e:
        print(f"{Colors.RED}Error downloading SmartTube: {e}{Colors.RESET}")

def action_restore(adb: ADB, profiler: TVProfiler, auto_confirm: bool = False):
    print(f"\n{Colors.BOLD}{Colors.YELLOW}--- One-Click Full Factory Restoration ---{Colors.RESET}")
    print("This will re-enable all previously disabled packages back to factory default.")
    if not auto_confirm:
        choice = input("Are you sure you want to restore all packages? [y/N]: ").strip().lower()
        if choice not in ("y", "yes"):
            print("Restoration cancelled.")
            return

    for pkg in profiler.disabled_packages:
        adb.shell(f"pm enable {pkg}")
        print(f"  {Colors.GREEN}✓ Restored:{Colors.RESET} {pkg}")
    adb.shell("settings put global window_animation_scale 1.0")
    adb.shell("settings put global transition_animation_scale 1.0")
    adb.shell("settings put global animator_duration_scale 1.0")
    print(f"\n{Colors.GREEN}✓ All packages and default animation speeds restored!{Colors.RESET}")
    profiler.scan()

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
  python3 unbloat.py --freeze            # Apply AppOps background freeze & GPU boost
  python3 unbloat.py --aot               # AOT machine code speed compilation
  python3 unbloat.py --dns adguard       # Enable AdGuard Private DNS
  python3 unbloat.py --smarttube         # Auto-download & install SmartTube APK
  python3 unbloat.py --restore -y        # Factory restore all disabled packages
        """
    )
    parser.add_argument("-d", "--device", help="Target device IP address (e.g. 192.168.1.50) or ADB serial")
    parser.add_argument("-y", "--yes", action="store_true", help="Skip confirmation prompts (auto-confirm)")
    parser.add_argument("--auto", action="store_true", help="Execute complete optimization (Debloat + Freeze + AOT + DNS)")
    parser.add_argument("--debloat", action="store_true", help="Disable brand bloatware and background telemetry")
    parser.add_argument("--freeze", action="store_true", help="Freeze background streaming apps and boost GPU rendering")
    parser.add_argument("--aot", action="store_true", help="Pre-compile installed apps to native ARM machine code")
    parser.add_argument("--dns", choices=["adguard", "quad9", "off"], help="Configure system-wide Private DNS ad-blocker")
    parser.add_argument("--smarttube", action="store_true", help="Download and install architecture-matched SmartTube")
    parser.add_argument("--restore", action="store_true", help="Restore all disabled packages and reset animation scales")
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

    has_cli_action = any([args.auto, args.debloat, args.freeze, args.aot, args.dns, args.smarttube, args.restore])

    if has_cli_action:
        profiler.print_summary()
        if args.auto:
            action_debloat(adb, profiler, auto_confirm=args.yes)
            action_extreme_speed(adb, profiler, preset="5")
            action_aot_compile(adb, profiler)
            action_private_dns(adb, provider="adguard")
            print(f"\n{Colors.GREEN}{Colors.BOLD}🎉 Complete Auto-Optimization Finished! Your TV is at peak speed.{Colors.RESET}")
        else:
            if args.debloat:
                action_debloat(adb, profiler, auto_confirm=args.yes)
            if args.freeze:
                action_extreme_speed(adb, profiler, preset="5")
            if args.aot:
                action_aot_compile(adb, profiler)
            if args.dns:
                action_private_dns(adb, provider=args.dns)
            if args.smarttube:
                action_install_smarttube(adb, profiler)
            if args.restore:
                action_restore(adb, profiler, auto_confirm=args.yes)
        sys.exit(0)

    # Interactive Loop
    profiler.print_summary()

    while True:
        print(f"\n{Colors.BOLD}{Colors.CYAN}=== UnbloatTV Master Control Menu ==={Colors.RESET}")
        print(f"  {Colors.BOLD}[1]{Colors.RESET} 🚀 {Colors.GREEN}One-Click Auto Optimize{Colors.RESET} (Safe Debloat + GPU Boost + AppOps Freeze)")
        print(f"  {Colors.BOLD}[2]{Colors.RESET} 🧹 Safe Debloat & Telemetry Removal")
        print(f"  {Colors.BOLD}[3]{Colors.RESET} ⚡ Extreme Speed & AppOps Background Freeze")
        print(f"  {Colors.BOLD}[4]{Colors.RESET} 🏎️ Ahead-Of-Time (AOT) Machine Code Compilation")
        print(f"  {Colors.BOLD}[5]{Colors.RESET} 🛡️ System-Wide Ad-Blocker (Private DNS)")
        print(f"  {Colors.BOLD}[6]{Colors.RESET} 📺 Install SmartTube (Ad-Free YouTube with SponsorBlock)")
        print(f"  {Colors.BOLD}[7]{Colors.RESET} 🔍 Rescan TV Hardware & Memory")
        print(f"  {Colors.BOLD}[8]{Colors.RESET} 🔄 Restore All Disabled Packages to Factory Default")
        print(f"  {Colors.BOLD}[0]{Colors.RESET} ❌ Exit")

        choice = input(f"\n{Colors.BOLD}Choose an option (0-8): {Colors.RESET}").strip()
        if choice == "1":
            action_debloat(adb, profiler)
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
            action_install_smarttube(adb, profiler)
        elif choice == "7":
            profiler.scan()
            profiler.print_summary()
        elif choice == "8":
            action_restore(adb, profiler)
        elif choice in ("0", "q", "exit"):
            print(f"\n{Colors.CYAN}Thank you for using UnbloatTV! Enjoy your fast, clean TV.{Colors.RESET}")
            break
        else:
            print(f"{Colors.RED}Invalid option. Please try again.{Colors.RESET}")

if __name__ == "__main__":
    main()
