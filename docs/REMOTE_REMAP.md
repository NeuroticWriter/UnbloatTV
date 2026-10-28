# Remote Control Button Remapping Guide 🎮📺

Most Android & Google TV remotes ship with hardcoded, sponsored buttons (e.g. Netflix, Prime Video, Disney+, YouTube, or manufacturer buttons) that cannot be changed in standard TV settings.

This guide explains how to remap those buttons to open **SmartTube**, **Projectivy Launcher**, or **HDMI Inputs** instantly.

---

## Method 1: Using Projectivy Launcher's Built-in Override (Easiest & Free)

If you use **Projectivy Launcher**:
1. Open **Projectivy Settings** ➔ **General** ➔ **Override Home Button**.
2. Turn ON **"Detect Home key press"** and enable the Accessibility Service when prompted.
3. Your TV's physical **Home** button will now land on Projectivy Launcher with zero delay.

---

## Method 2: Remapping Sponsored App Buttons (Button Mapper)

To remap sponsored buttons (e.g., make the Netflix button launch **SmartTube** or **Mute**):

1. Install **Button Mapper: Remap your keys** from the Google Play Store on your TV.
2. Open TV **Settings** ➔ **System** ➔ **Accessibility** ➔ Turn ON **Button Mapper**.
3. Open Button Mapper on your TV:
   - Select **Add Buttons** ➔ Click **Add Buttons**.
   - Press the sponsored button on your remote (e.g. Netflix button).
   - Once captured, click on the button name.
   - Toggle **Customize** ON.
4. Set the action:
   - **Single Tap:** Open Application ➔ Select **SmartTube** (or your favorite app).
   - **Double Tap:** Toggle Mute or Take Screenshot.
   - **Long Press:** Open TV Settings or Switch to HDMI 1.

---

## ⚡ Instant Key Event Testing via ADB

You can also trigger remote keys directly from your computer via ADB:

```bash
# Trigger Home button
adb shell input keyevent KEYCODE_HOME

# Trigger Back button
adb shell input keyevent KEYCODE_BACK

# Trigger TV Settings
adb shell input keyevent KEYCODE_SETTINGS

# Trigger HDMI 1 Input
adb shell input keyevent KEYCODE_TV_INPUT_HDMI_1

# Trigger Volume Mute
adb shell input keyevent KEYCODE_VOLUME_MUTE
```
