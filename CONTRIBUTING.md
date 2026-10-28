# Contributing to UnbloatTV 🤝📺

Thank you for helping make Android & Google TV fast, clean, and ad-free for everyone worldwide!

The single most impactful way to contribute is by **sharing your TV model's debloat profile**, so the next person with that exact TV gets automated 1-click optimization.

---

## 🚀 How to Share Your TV Model Profile (The 1-Minute Method)

UnbloatTV has a built-in automated profile exporter!

1. Connect your TV to your computer via ADB:
   ```bash
   ./unbloat.sh -d <YOUR_TV_IP>:5555 --export-profile
   ```
2. UnbloatTV will automatically inspect your hardware, gather the debloated package list, compute your health score, and generate a verified profile folder in:
   ```
   profiles/<your-brand>-<your-model>/
   ├── profile.json
   ├── debloated_packages.txt
   └── README.md
   ```
3. Commit the new folder to a branch:
   ```bash
   git checkout -b add-profile-<your-model>
   git add profiles/
   git commit -m "feat(profile): Add verified profile for <Brand Model>"
   git push origin add-profile-<your-model>
   ```
4. Open a **Pull Request** on [GitHub](https://github.com/NeuroticWriter/UnbloatTV)!

---

## 🛡️ Critical Safety Guidelines for Contributors

Before submitting package additions to `BLOAT_DATABASE`:
1. **Never Touch System Essentials:**
   - Backlight drivers (e.g. `com.homwee.dynamicbacklight`)
   - HDMI inputs and switchers (e.g. `com.mediatek.tvinput`)
   - Bluetooth and remote pairing services
   - Audio HALs and Dolby daemons
   - Android Permission Controller and Package Installer
2. **Always Use `SAFE` vs `CAUTION` Tiers:**
   - Mark crash loggers, ad attribution APIs, duplicate stores as `SAFE`.
   - Mark accessibility services (TalkBack, TTS) and screensavers as `CAUTION`.
3. **Test on Real Hardware:**
   - Always confirm that after a full reboot, HDMI inputs, audio volume, and remote keys function 100%.

---

## ☕ Sponsors & Supporters

If you enjoy the project and want to support ongoing hardware benchmarking, consider [Buying Me a Coffee](https://buymeacoffee.com/dewzone)! Every cup keeps the code clean and verified.
