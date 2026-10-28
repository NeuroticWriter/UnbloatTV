# Guide for Non-Android TVs (Samsung, LG, Roku, Vizio) 📺

> **Note:** Samsung (Tizen OS), LG (webOS), Roku OS, and Vizio (SmartCast) do not run Android. UnbloatTV's ADB tools cannot run on them directly.  
> However, you can **turn off tracking, ACR (Automatic Content Recognition), and home screen advertising** using your remote control in under 2 minutes:

---

## 1. Samsung Smart TVs (Tizen OS)

Samsung TVs use **Automated Content Recognition (ACR)** to track every movie, show, and video game you play.

### How to Turn Off Ads & Tracking:
1. Open **Settings** ⚙️ ➔ **All Settings** ➔ **Support** ➔ **Terms & Privacy** ➔ **Privacy Choices**.
2. **Turn OFF** `Viewing Information Services` (This stops Samsung from fingerprinting what you watch).
3. **Turn OFF** `Internet-based Advertising` (Stops personalized ad targeting).
4. **Turn OFF** `Voice Recognition Services` (If you don't use Bixby voice commands).
5. Open **Settings** ➔ **General & Privacy** ➔ **Smart Features** ➔ **Disable "Autorun Smart Hub"** (Stops the ad bar from popping up automatically on startup).

---

## 2. LG Smart TVs (webOS)

LG TVs run **Live Plus** ACR tracking and show sponsored banner ads across the Home dashboard.

### How to Turn Off Ads & Tracking:
1. Press the **Settings** button on your Magic Remote ➔ **All Settings**.
2. Go to **General** ➔ **System** (or **Additional Settings**) ➔ **Live Plus** ➔ **Toggle OFF**.
3. Go to **General** ➔ **Privacy & Terms** ➔ **User Agreements** ➔ **Uncheck "Interest-Based Ads"** and **"Voice Information"**.
4. Go to **General** ➔ **Home Settings** ➔ **Turn OFF "Home Promotion"** (Removes sponsored video ads from the top of webOS).

---

## 3. Roku TVs (TCL Roku, Hisense Roku, Roku Express)

### How to Turn Off Ads & Tracking:
1. Press the **Home** button on your Roku remote.
2. Go to **Settings** ➔ **Privacy** ➔ **Advertising**.
3. Check **"Limit Ad Tracking"** and select **"Reset Advertising ID"**.
4. Go to **Settings** ➔ **Privacy** ➔ **Smart TV Experience**.
5. **Uncheck "Use Info from TV Inputs"** (This disables ACR tracking on HDMI inputs like gaming consoles and cable boxes).

---

## 4. System-Wide Ad-Blocking via Router DNS

Since non-Android TVs cannot change their Private DNS hostname directly in TV settings, you can configure **AdGuard DNS** directly inside your home Wi-Fi router settings:

- **Primary DNS:** `94.140.14.14`
- **Secondary DNS:** `94.140.15.15`

This automatically blocks tracking domains and banner ads for **every device** connected to your home Wi-Fi, including Samsung and LG smart TVs!
