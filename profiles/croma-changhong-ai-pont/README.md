# Croma / Changhong AI PONT (Mediatek MT5867)

- **Manufacturer:** Changhong (Croma Google TV)
- **SoC / Board:** Mediatek `mt5867` (32-bit `armeabi-v7a`)
- **OS Version:** Android TV 14 (API 34)
- **RAM Pool:** 950 MB Physical RAM
- **Available RAM Before:** 259 MB
- **Available RAM After:** 348 MB (+35% increase)
- **Health Score:** 48/100 ➔ 98/100

## Hardware Optimizations Applied
1. **Safe Debloat:** 32 background daemons removed (duplicate OEM store, crash dumps, calendar sync).
2. **AppOps Freeze:** Hotstar, Prime Video, Netflix, and SmartTube frozen on Home press.
3. **Animations:** Window/Transition/Animator scales set to `0.0x`.
4. **Display:** Forced 2D hardware GPU composition (`force_gpu_rendering=1`).
5. **Ad-Blocking:** AdGuard Private DNS (`dns.adguard-dns.com`).
6. **Launcher:** Projectivy Launcher with live dynamic 4K backdrops and "Watch Next" movie carousels.

> **CRITICAL HARDWARE RULE:** Never touch `com.homwee.dynamicbacklight` (panel backlight driver) or `com.mediatek.tvinput` (HDMI switcher).

