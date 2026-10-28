package com.dewzone.dlaunch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;
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
        String[] menuItems = new String[]{
            "🎨  Accent Color",
            "🌌  Wallpaper Theme",
            "📐  Grid Layout",
            "🏷️  App Labels (" + (prefs.isShowLabels() ? "Shown" : "Hidden") + ")",
            "⭐  Favorites Shelf (" + (prefs.isShowFavorites() ? "Shown" : "Hidden") + ")",
            "👁️  Hidden Apps (" + prefs.getHiddenApps().size() + " hidden)",
            "⚙️  Android TV System Settings"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("DLaunch Settings");
        builder.setItems(menuItems, new MainMenuListener(this));
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

    public void showThemePicker() {
        final String[] names = new String[]{
            "🌌  Obsidian Glass (Deep Navy)",
            "🔮  Aurora Ambient (Cosmic Indigo)",
            "💠  Cyber Teal (Midnight Matrix)",
            "🌅  Sunset Dusk (Crimson Glow)",
            "⬛  OLED Pure Black (Zero Backlight)"
        };
        final String[] keys = new String[]{
            "obsidian",
            "aurora",
            "cyber",
            "sunset",
            "oled"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Select Wallpaper Theme");
        builder.setItems(names, new ThemeListener(this, keys));
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
        Toast.makeText(activity, current ? "Favorites shelf hidden" : "Favorites shelf shown", Toast.LENGTH_SHORT).show();
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

    private static class MainMenuListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;

        MainMenuListener(DLaunchSettingsDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            switch (which) {
                case 0:
                    dialog.showAccentColorPicker();
                    break;
                case 1:
                    dialog.showThemePicker();
                    break;
                case 2:
                    dialog.showGridPicker();
                    break;
                case 3:
                    dialog.toggleLabels();
                    break;
                case 4:
                    dialog.toggleFavorites();
                    break;
                case 5:
                    dialog.showHiddenAppsDialog();
                    break;
                case 6:
                    dialog.launchSystemSettings();
                    break;
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

    private static class ThemeListener implements DialogInterface.OnClickListener {
        private final DLaunchSettingsDialog dialog;
        private final String[] keys;

        ThemeListener(DLaunchSettingsDialog dialog, String[] keys) {
            this.dialog = dialog;
            this.keys = keys;
        }

        @Override
        public void onClick(DialogInterface d, int which) {
            if (which >= 0 && which < keys.length) {
                dialog.prefs.setTheme(keys[which]);
                dialog.activity.applyThemeAndAccent();
                Toast.makeText(dialog.activity, "Theme updated", Toast.LENGTH_SHORT).show();
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
