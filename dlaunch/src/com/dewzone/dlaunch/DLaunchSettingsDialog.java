package com.dewzone.dlaunch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DLaunchSettingsDialog {

    private final DLaunchActivity activity;
    private final PrefsManager prefs;

    public DLaunchSettingsDialog(DLaunchActivity activity, PrefsManager prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    public void showMainMenu() {
        String wpMode = prefs.getWallpaperMode();
        String wpLabel;
        if ("system".equalsIgnoreCase(wpMode)) {
            wpLabel = "System Wallpaper";
        } else if ("custom".equalsIgnoreCase(wpMode)) {
            wpLabel = "Custom Photo";
        } else {
            wpLabel = "Theme (" + prefs.getTheme() + ")";
        }

        String[] menuItems = new String[]{
            "🏠  Set as Default TV Launcher",
            "🎨  Accent Color",
            "🖼️  Wallpaper & Themes (" + wpLabel + ")",
            "🌘  Wallpaper Dimming (" + prefs.getWallpaperDim() + "%)",
            "🪟  Card Transparency (" + prefs.getCardTransparency() + ")",
            "📐  Grid Layout",
            "🏷️  App Labels (" + (prefs.isShowLabels() ? "Shown" : "Hidden") + ")",
            "⭐  Favorites Dock Capsule (" + (prefs.isShowFavorites() ? "Shown" : "Hidden") + ")",
            "👁️  Hidden Apps (" + prefs.getHiddenApps().size() + " hidden)",
            "⚙️  Android TV System Settings"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("DLaunch Settings");
        builder.setItems(menuItems, new MainMenuListener(this));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showSetDefaultLauncherDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Set DLaunch as Default Launcher");
        builder.setMessage(
                "Choose an option to make DLaunch your main TV home screen:\n\n" +
                "1. Official Android Settings:\n" +
                "Tap 'Open Home Settings' below to select DLaunch as your default Home app.\n\n" +
                "2. 1-Click ADB Lock (Recommended for Google TV):\n" +
                "If your TV overrides default launcher settings, run this from your computer:\n" +
                "  ./unbloat.sh --dlaunch\n" +
                "This removes OEM sponsored ads and permanently locks DLaunch to your remote Home button!"
        );
        builder.setPositiveButton("Open Home Settings", new DefaultHomeClickListener(activity));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showAccentColorPicker() {
        final String[] names = new String[]{
            "💠  Electric Cyan (Default)",
            "🌲  Emerald Green",
            "🔮  Electric Purple",
            "🌅  Sunset Orange",
            "❄️  Diamond White",
            "⚡  Cyber Yellow",
            "🌸  Neon Pink"
        };
        final int[] colors = new int[]{
            PrefsManager.COLOR_CYAN,
            PrefsManager.COLOR_EMERALD,
            PrefsManager.COLOR_PURPLE,
            PrefsManager.COLOR_SUNSET,
            PrefsManager.COLOR_WHITE,
            PrefsManager.COLOR_YELLOW,
            PrefsManager.COLOR_PINK
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Accent Color");
        builder.setItems(names, new AccentColorListener(this, colors));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showWallpaperMenu() {
        final String[] names = new String[]{
            "🌌  Nebula Cosmic Blue (Futuristic Ambient — Default)",
            "🔮  Cosmic Violet Ambient",
            "💠  Cyberpunk Neon Matrix",
            "🌌  Obsidian Space Glass",
            "🌅  Sunset Twilight Dusk",
            "⬛  OLED Pure Black (Zero Backlight)",
            "🖼️  Android System Wallpaper",
            "📁  Custom Wallpaper Photo (Storage/USB/ADB)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Wallpaper & Themes");
        builder.setItems(names, new WallpaperMenuListener(this));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showCustomWallpaperMenu() {
        final String[] names = new String[]{
            "📁  Select Photo with File/Gallery App",
            "🔍  Auto-Detect /sdcard/wallpaper.jpg",
            "ℹ️  How to Add Wallpapers via USB / ADB"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Custom Wallpaper Photo");
        builder.setItems(names, new CustomWallpaperListener(this));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showDimPicker() {
        final String[] names = new String[]{
            "0%  (Bright / Original Photo)",
            "25% (Light Dim Overlay)",
            "45% (Balanced Dim — Recommended)",
            "65% (Deep Dark Dim)",
            "80% (Subtle Ambient Glow)"
        };
        final int[] values = new int[]{0, 25, 45, 65, 80};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Wallpaper Dim Filter (Readability)");
        builder.setItems(names, new DimListener(this, values));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showCardTransparencyPicker() {
        final String[] names = new String[]{
            "🪟  Frosted Glass (60% Opacity — Recommended)",
            "💎  Ultra Glass (30% High Transparency)",
            "⬛  Solid Cards (100% Opaque Dark)"
        };
        final String[] values = new String[]{"glass", "ultra", "solid"};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Card Style & Transparency");
        builder.setItems(names, new TransparencyListener(this, values));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void showGridPicker() {
        final String[] names = new String[]{
            "🔄  Auto (Adaptive based on screen DPI)",
            "4️⃣  4 Columns (Large Cards)",
            "5️⃣  5 Columns (Standard FHD)",
            "6️⃣  6 Columns (Dense Power-User)"
        };
        final int[] cols = new int[]{0, 4, 5, 6};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Grid Layout");
        builder.setItems(names, new GridListener(this, cols));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void toggleLabels() {
        boolean current = prefs.isShowLabels();
        prefs.setShowLabels(!current);
        Toast.makeText(activity, current ? "App labels hidden" : "App labels shown", Toast.LENGTH_SHORT).show();
        activity.loadAndDisplayApps();
    }

    public void toggleFavorites() {
        boolean current = prefs.isShowFavorites();
        prefs.setShowFavorites(!current);
        Toast.makeText(activity, current ? "Favorites dock hidden" : "Favorites dock shown", Toast.LENGTH_SHORT).show();
        activity.loadAndDisplayApps();
    }

    public void showHiddenAppsDialog() {
        Set<String> hidden = prefs.getHiddenApps();
        if (hidden == null || hidden.isEmpty()) {
            Toast.makeText(activity, "No apps are currently hidden", Toast.LENGTH_SHORT).show();
            return;
        }

        final List<String> list = new ArrayList<String>(hidden);
        List<String> displayList = new ArrayList<String>();
        for (String pkg : list) {
            displayList.add("Unhide: " + pkg);
        }
        displayList.add("✨ Unhide All Apps");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Hidden Applications");
        builder.setItems(displayList.toArray(new String[0]), new HiddenAppsListener(this, list));
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    public void launchSystemSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Cannot open System Settings", Toast.LENGTH_SHORT).show();
        }
    }

    private static class DefaultHomeClickListener implements DialogInterface.OnClickListener {
        private final DLaunchActivity activity;

        DefaultHomeClickListener(DLaunchActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            activity.openDefaultHomeSettings();
        }
    }

    private static class MainMenuListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;

        MainMenuListener(DLaunchSettingsDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            switch (which) {
                case 0:
                    dialog.showSetDefaultLauncherDialog();
                    break;
                case 1:
                    dialog.showAccentColorPicker();
                    break;
                case 2:
                    dialog.showWallpaperMenu();
                    break;
                case 3:
                    dialog.showDimPicker();
                    break;
                case 4:
                    dialog.showCardTransparencyPicker();
                    break;
                case 5:
                    dialog.showGridPicker();
                    break;
                case 6:
                    dialog.toggleLabels();
                    break;
                case 7:
                    dialog.toggleFavorites();
                    break;
                case 8:
                    dialog.showHiddenAppsDialog();
                    break;
                case 9:
                    dialog.launchSystemSettings();
                    break;
            }
        }
    }

    private static class WallpaperMenuListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;

        WallpaperMenuListener(DLaunchSettingsDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            switch (which) {
                case 0: // Nebula Cosmic Blue (Futuristic)
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("nebula");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "Nebula theme applied", Toast.LENGTH_SHORT).show();
                    break;
                case 1: // Cosmic Violet
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("violet");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "Cosmic Violet applied", Toast.LENGTH_SHORT).show();
                    break;
                case 2: // Cyber Neon
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("neon");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "Cyber Neon applied", Toast.LENGTH_SHORT).show();
                    break;
                case 3: // Obsidian Space Glass
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("obsidian");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "Obsidian theme applied", Toast.LENGTH_SHORT).show();
                    break;
                case 4: // Sunset Twilight
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("sunset");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "Sunset theme applied", Toast.LENGTH_SHORT).show();
                    break;
                case 5: // OLED Pure Black
                    dialog.prefs.setWallpaperMode("gradient");
                    dialog.prefs.setTheme("oled");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "OLED Pure Black applied", Toast.LENGTH_SHORT).show();
                    break;
                case 6: // Android System Wallpaper
                    dialog.prefs.setWallpaperMode("system");
                    dialog.activity.applyThemeAndAccent();
                    Toast.makeText(dialog.activity, "System wallpaper applied", Toast.LENGTH_SHORT).show();
                    break;
                case 7: // Custom Wallpaper
                    dialog.showCustomWallpaperMenu();
                    break;
            }
        }
    }

    private static class CustomWallpaperListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;

        CustomWallpaperListener(DLaunchSettingsDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which == 0) {
                dialog.activity.pickCustomWallpaper();
            } else if (which == 1) {
                dialog.activity.autoDetectWallpaper();
            } else if (which == 2) {
                dialog.activity.showWallpaperInstructions();
            }
        }
    }

    private static class DimListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final int[] values;

        DimListener(DLaunchSettingsDialog dialog, int[] values) {
            this.dialog = dialog;
            this.values = values;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which >= 0 && which < values.length) {
                dialog.prefs.setWallpaperDim(values[which]);
                dialog.activity.applyThemeAndAccent();
                Toast.makeText(dialog.activity, "Wallpaper dimming set to " + values[which] + "%", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class TransparencyListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final String[] values;

        TransparencyListener(DLaunchSettingsDialog dialog, String[] values) {
            this.dialog = dialog;
            this.values = values;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which >= 0 && which < values.length) {
                dialog.prefs.setCardTransparency(values[which]);
                dialog.activity.loadAndDisplayApps();
                Toast.makeText(dialog.activity, "Card style updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class AccentColorListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final int[] colors;

        AccentColorListener(DLaunchSettingsDialog dialog, int[] colors) {
            this.dialog = dialog;
            this.colors = colors;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which >= 0 && which < colors.length) {
                dialog.prefs.setAccentColor(colors[which]);
                dialog.activity.applyThemeAndAccent();
                dialog.activity.loadAndDisplayApps();
                Toast.makeText(dialog.activity, "Accent color applied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class GridListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final int[] cols;

        GridListener(DLaunchSettingsDialog dialog, int[] cols) {
            this.dialog = dialog;
            this.cols = cols;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which >= 0 && which < cols.length) {
                dialog.prefs.setColumnCount(cols[which]);
                dialog.activity.calculateOptimalColumns();
                dialog.activity.loadAndDisplayApps();
                Toast.makeText(dialog.activity, "Grid layout updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class HiddenAppsListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final List<String> list;

        HiddenAppsListener(DLaunchSettingsDialog dialog, List<String> list) {
            this.dialog = dialog;
            this.list = list;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which < list.size()) {
                dialog.prefs.setHidden(list.get(which), false);
                Toast.makeText(dialog.activity, "App unhidden", Toast.LENGTH_SHORT).show();
            } else {
                dialog.prefs.unhideAll();
                Toast.makeText(dialog.activity, "All apps unhidden", Toast.LENGTH_SHORT).show();
            }
            dialog.activity.loadAndDisplayApps();
        }
    }
}
