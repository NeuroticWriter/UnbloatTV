package com.dewzone.dlaunch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DLaunchSettingsDialog {

    private final DLaunchActivity activity;
    private final PrefsManager prefs;
    private Dialog activeDialog;

    public DLaunchSettingsDialog(DLaunchActivity activity, PrefsManager prefs) {
        this.activity = activity;
        this.prefs = prefs;
    }

    private Dialog createGlassDialog(String title, String subtitle) {
        if (activeDialog != null && activeDialog.isShowing()) {
            try {
                activeDialog.dismiss();
            } catch (Throwable ignored) {}
        }

        Dialog dialog = new Dialog(activity, R.style.Theme_DLaunch_Dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_dlaunch_settings);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            float density = activity.getResources().getDisplayMetrics().density;
            window.setLayout((int) (640 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView titleView = (TextView) dialog.findViewById(R.id.dialog_title);
        TextView subtitleView = (TextView) dialog.findViewById(R.id.dialog_subtitle);
        View closeBtn = dialog.findViewById(R.id.btn_dialog_close);

        if (titleView != null) titleView.setText(title);
        if (subtitleView != null) subtitleView.setText(subtitle);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(new DialogDismissClickListener(dialog));
            closeBtn.setOnFocusChangeListener(new DLaunchActivity.FocusAnimator(1.05f));
        }

        activeDialog = dialog;
        return dialog;
    }

    private View addSettingRow(ViewGroup container, String emoji, String title, String subtitle, String badge, View.OnClickListener onClick) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View row = inflater.inflate(R.layout.item_dialog_setting_row, container, false);

        TextView emojiView = (TextView) row.findViewById(R.id.setting_icon_emoji);
        TextView titleView = (TextView) row.findViewById(R.id.setting_title);
        TextView subtitleView = (TextView) row.findViewById(R.id.setting_subtitle);
        TextView badgeView = (TextView) row.findViewById(R.id.setting_status_badge);

        if (emojiView != null) emojiView.setText(emoji);
        if (titleView != null) titleView.setText(title);
        if (subtitleView != null) subtitleView.setText(subtitle);

        if (badgeView != null) {
            if (badge != null && !badge.isEmpty()) {
                badgeView.setVisibility(View.VISIBLE);
                badgeView.setText(badge);
                badgeView.setTextColor(prefs.getAccentColor());
            } else {
                badgeView.setVisibility(View.GONE);
            }
        }

        // Dynamic accent glow focus border
        float density = activity.getResources().getDisplayMetrics().density;
        row.setBackground(createRowSelector(density, prefs.getAccentColor()));
        row.setOnFocusChangeListener(new DLaunchActivity.FocusAnimator(1.02f));
        row.setOnClickListener(onClick);

        container.addView(row);
        return row;
    }

    private static StateListDrawable createRowSelector(float density, int accentColor) {
        int radiusPx = (int) (14 * density);
        int strokeNormalPx = (int) (1 * density);
        int strokeFocusedPx = (int) (2.5f * density);

        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radiusPx);
        normal.setColor(Color.parseColor("#24142135"));
        normal.setStroke(strokeNormalPx, Color.parseColor("#20FFFFFF"));

        GradientDrawable focused = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#66223B63"), Color.parseColor("#80335893")}
        );
        focused.setShape(GradientDrawable.RECTANGLE);
        focused.setCornerRadius(radiusPx);
        focused.setStroke(strokeFocusedPx, accentColor);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{android.R.attr.state_pressed}, focused);
        states.addState(new int[]{}, normal);
        return states;
    }

    public void showMainMenu() {
        Dialog dialog = createGlassDialog("DLaunch Settings", "Futuristic TV Preferences & Display");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        boolean isDefault = isDefaultHome(activity);
        String defaultBadge = isDefault ? "Active Default" : "Set Default";

        String wpMode = prefs.getWallpaperMode();
        String wpBadge;
        if ("system".equalsIgnoreCase(wpMode)) {
            wpBadge = "System WP";
        } else if ("custom".equalsIgnoreCase(wpMode)) {
            wpBadge = "Custom Photo";
        } else {
            String theme = prefs.getTheme();
            if ("halloween".equalsIgnoreCase(theme) || "haunted".equalsIgnoreCase(theme)) wpBadge = "Haunted Twilight";
            else if ("ocean".equalsIgnoreCase(theme) || "liquid_ocean".equalsIgnoreCase(theme)) wpBadge = "Liquid Ocean";
            else if ("liquid_nebula".equalsIgnoreCase(theme) || "nebula".equalsIgnoreCase(theme)) wpBadge = "Liquid Nebula";
            else if ("liquid_aurora".equalsIgnoreCase(theme) || "aurora".equalsIgnoreCase(theme)) wpBadge = "Liquid Aurora";
            else if ("liquid_sunset".equalsIgnoreCase(theme) || "sunset".equalsIgnoreCase(theme)) wpBadge = "Liquid Sunset";
            else if ("obsidian".equalsIgnoreCase(theme)) wpBadge = "Obsidian";
            else if ("oled".equalsIgnoreCase(theme)) wpBadge = "OLED Black";
            else wpBadge = theme;
        }

        int screensaverTimeout = prefs.getScreensaverTimeout();
        String screensaverBadge = (screensaverTimeout <= 0) ? "Disabled" : (screensaverTimeout + " min");

        String colBadge = prefs.getColumnCount() == 0 ? "Auto" : (prefs.getColumnCount() + " Cols");
        String labelsBadge = prefs.isShowLabels() ? "Shown" : "Hidden";
        String dockBadge = prefs.isShowFavorites() ? "Shown" : "Hidden";
        int hiddenCount = prefs.getHiddenApps().size();

        View firstRow = addSettingRow(list, "🏠", "Set as Default TV Launcher", "Official TV Home selector & 1-Click permanent ADB lock", defaultBadge, new MainRowClickListener(this, 0));
        addSettingRow(list, "🎨", "Neon Accent Color", "Electric neon contours for remote focus & highlights", getAccentColorName(prefs.getAccentColor()), new MainRowClickListener(this, 1));
        addSettingRow(list, "🌊", "Liquid Wallpaper & Themes", "Calm oceanic abyss, cosmic nebula & custom photos", wpBadge, new MainRowClickListener(this, 2));
        addSettingRow(list, "🌙", "Calm Ambient Screensaver", "Tranquil floating clock & peaceful ambient relax mode", screensaverBadge, new MainRowClickListener(this, 3));
        addSettingRow(list, "🌘", "Wallpaper Dimming Filter", "Contrast overlay for text & squircle card readability", prefs.getWallpaperDim() + "%", new MainRowClickListener(this, 4));
        addSettingRow(list, "🪟", "Card Transparency & Style", "Frosted glass, ultra glass, or solid cards", prefs.getCardTransparency(), new MainRowClickListener(this, 5));
        addSettingRow(list, "📐", "Grid Column Layout", "Adaptive sizing or custom columns per row", colBadge, new MainRowClickListener(this, 6));
        addSettingRow(list, "🏷️", "App Text Labels", "Show or hide application title labels below cards", labelsBadge, new MainRowClickListener(this, 7));
        addSettingRow(list, "⭐", "Favorites Dock Capsule", "Top floating squircle capsule bar", dockBadge, new MainRowClickListener(this, 8));
        addSettingRow(list, "👁️", "Hidden Applications", "Manage and restore hidden apps", hiddenCount + " hidden", new MainRowClickListener(this, 9));
        addSettingRow(list, "⚙️", "Android TV System Settings", "Network, audio, display & TV device settings", "System", new MainRowClickListener(this, 10));

        dialog.show();
        if (firstRow != null) {
            firstRow.requestFocus();
        }
    }

    public void showSetDefaultLauncherDialog() {
        Dialog dialog = createGlassDialog("Set as Default Launcher", "Android TV Home Configuration");
        TextView msgView = (TextView) dialog.findViewById(R.id.dialog_message);
        LinearLayout actions = (LinearLayout) dialog.findViewById(R.id.dialog_actions_container);
        Button positiveBtn = (Button) dialog.findViewById(R.id.btn_action_positive);

        if (msgView != null) {
            msgView.setVisibility(View.VISIBLE);
            msgView.setText(
                "Make DLaunch your main TV home screen:\n\n" +
                "1. Official Android Settings:\n" +
                "Tap 'Open Home Settings' below to select DLaunch as your default Home app.\n\n" +
                "2. 1-Click ADB Lock (Recommended for Google TV):\n" +
                "If your TV overrides default launcher settings, run this from your terminal:\n" +
                "  ./unbloat.sh --dlaunch\n" +
                "This permanently removes OEM sponsored ads and locks DLaunch to your remote Home button!"
            );
        }

        if (actions != null && positiveBtn != null) {
            actions.setVisibility(View.VISIBLE);
            positiveBtn.setText("Open Home Settings");
            positiveBtn.setOnClickListener(new DefaultHomeBtnClickListener(activity, dialog));
            positiveBtn.setOnFocusChangeListener(new DLaunchActivity.FocusAnimator(1.05f));
            positiveBtn.requestFocus();
        }

        dialog.show();
    }

    public void showAccentColorPicker() {
        Dialog dialog = createGlassDialog("Select Accent Color", "Electric neon glow for remote focus contours");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        final int[] colors = new int[]{
            PrefsManager.COLOR_CYAN,
            PrefsManager.COLOR_EMERALD,
            PrefsManager.COLOR_PURPLE,
            PrefsManager.COLOR_SUNSET,
            PrefsManager.COLOR_WHITE,
            PrefsManager.COLOR_YELLOW,
            PrefsManager.COLOR_PINK,
            PrefsManager.COLOR_PUMPKIN
        };
        final String[] names = new String[]{
            "Electric Cyan", "Emerald Green", "Electric Purple", "Sunset Orange", "Diamond White", "Cyber Yellow", "Neon Pink", "Pumpkin Orange"
        };
        final String[] emojis = new String[]{"💠", "🌲", "🔮", "🌅", "❄️", "⚡", "🌸", "🎃"};

        int current = prefs.getAccentColor();
        View focusTarget = null;
        for (int i = 0; i < colors.length; i++) {
            boolean isSel = (colors[i] == current);
            View row = addSettingRow(list, emojis[i], names[i], "Futuristic neon focus contour", isSel ? "Active" : "", new AccentClickListener(this, colors[i], dialog));
            if (isSel) focusTarget = row;
        }

        dialog.show();
        if (focusTarget != null) focusTarget.requestFocus();
    }

    public void showWallpaperMenu() {
        Dialog dialog = createGlassDialog("Liquid Wallpaper & Themes", "Calm ambient backgrounds & serene gradients");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        String currentWp = prefs.getWallpaperMode();
        String currentTheme = prefs.getTheme();

        View first = addSettingRow(list, "🎃", "Haunted Twilight (Halloween)", "Spooky obsidian purple & jack-o'-lantern mist", ("gradient".equals(currentWp) && ("halloween".equals(currentTheme) || "haunted".equals(currentTheme))) ? "Active" : "", new ThemeClickListener(this, "halloween", dialog));
        addSettingRow(list, "🌊", "Liquid Ocean (Default)", "Calm deep sapphire abyss with liquid lighting", ("gradient".equals(currentWp) && ("ocean".equals(currentTheme) || "liquid_ocean".equals(currentTheme))) ? "Active" : "", new ThemeClickListener(this, "ocean", dialog));
        addSettingRow(list, "🌌", "Liquid Nebula", "Cosmic twilight indigo & obsidian calm", ("gradient".equals(currentWp) && ("nebula".equals(currentTheme) || "liquid_nebula".equals(currentTheme))) ? "Active" : "", new ThemeClickListener(this, "liquid_nebula", dialog));
        addSettingRow(list, "🌲", "Liquid Aurora", "Serene teal & boreal emerald abyss", ("gradient".equals(currentWp) && ("aurora".equals(currentTheme) || "liquid_aurora".equals(currentTheme))) ? "Active" : "", new ThemeClickListener(this, "liquid_aurora", dialog));
        addSettingRow(list, "🌅", "Liquid Sunset", "Peaceful dusk horizon & plum amethyst", ("gradient".equals(currentWp) && ("sunset".equals(currentTheme) || "liquid_sunset".equals(currentTheme))) ? "Active" : "", new ThemeClickListener(this, "liquid_sunset", dialog));
        addSettingRow(list, "⬛", "Liquid Obsidian", "Sleek obsidian slate with glass depth", ("gradient".equals(currentWp) && "obsidian".equals(currentTheme)) ? "Active" : "", new ThemeClickListener(this, "obsidian", dialog));
        addSettingRow(list, "🌑", "True Black OLED", "Zero light emissions & perfect contrast", ("gradient".equals(currentWp) && "oled".equals(currentTheme)) ? "Active" : "", new ThemeClickListener(this, "oled", dialog));
        addSettingRow(list, "🖼️", "Android System Wallpaper", "Live wallpaper from connected TV system", "system".equals(currentWp) ? "Active" : "", new SystemWpClickListener(this, dialog));
        addSettingRow(list, "📁", "Custom Photo Wallpaper", "Load photo from USB or /sdcard/wallpaper.jpg", "custom".equals(currentWp) ? "Active" : "", new CustomWpMenuClickListener(this, dialog));

        dialog.show();
        if (first != null) first.requestFocus();
    }

    public void showScreensaverMenu() {
        Dialog dialog = createGlassDialog("Calm Ambient Screensaver", "Peaceful floating clock & tranquil relax mode");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        int current = prefs.getScreensaverTimeout();

        View first = addSettingRow(list, "✨", "Preview Calm Screensaver Now", "Experience the tranquil ambient display immediately", "Preview", new PreviewScreensaverClickListener(this, dialog));
        addSettingRow(list, "⏱️", "Idle Timeout: 1 Minute", "Activates screensaver after 1 minute of inactivity", current == 1 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 1, dialog));
        addSettingRow(list, "⏱️", "Idle Timeout: 2 Minutes", "Activates screensaver after 2 minutes of inactivity", current == 2 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 2, dialog));
        addSettingRow(list, "⏱️", "Idle Timeout: 3 Minutes (Recommended)", "Balanced tranquil idle timer", current == 3 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 3, dialog));
        addSettingRow(list, "⏱️", "Idle Timeout: 5 Minutes", "Activates screensaver after 5 minutes of inactivity", current == 5 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 5, dialog));
        addSettingRow(list, "⏱️", "Idle Timeout: 10 Minutes", "Activates screensaver after 10 minutes of inactivity", current == 10 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 10, dialog));
        addSettingRow(list, "🚫", "Screensaver Disabled", "Keep launcher awake permanently", current <= 0 ? "Selected" : "", new ScreensaverTimeoutClickListener(this, 0, dialog));

        dialog.show();
        if (first != null) first.requestFocus();
    }

    public void showCustomWallpaperMenu() {
        Dialog dialog = createGlassDialog("Custom Wallpaper Photo", "Storage, USB, and ADB image detection");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        View first = addSettingRow(list, "📁", "Select Photo via File App", "Open installed gallery / file manager picker", "", new CustomWpActionClickListener(this, 0, dialog));
        addSettingRow(list, "🔍", "Auto-Detect /sdcard/wallpaper.jpg", "Scan TV storage & USB drives for wallpaper.jpg", "", new CustomWpActionClickListener(this, 1, dialog));
        addSettingRow(list, "ℹ️", "USB / ADB Wallpaper Instructions", "How to push your favorite 4K wallpapers", "", new CustomWpActionClickListener(this, 2, dialog));

        dialog.show();
        if (first != null) first.requestFocus();
    }

    public void showDimPicker() {
        Dialog dialog = createGlassDialog("Wallpaper Dimming Filter", "Contrast layer over background for readability");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        int current = prefs.getWallpaperDim();
        int[] dims = new int[]{0, 25, 45, 65, 80};
        String[] labels = new String[]{"0% (Original Brightness)", "25% (Light Dim)", "45% (Balanced — Recommended)", "65% (Deep Dark Dim)", "80% (Subtle Ambient Glow)"};

        View focusTarget = null;
        for (int i = 0; i < dims.length; i++) {
            boolean isSel = (dims[i] == current);
            View row = addSettingRow(list, "🌘", labels[i], "Adjust background opacity", isSel ? "Active" : "", new DimOptionClickListener(this, dims[i], dialog));
            if (isSel || focusTarget == null) focusTarget = row;
        }

        dialog.show();
        if (focusTarget != null) focusTarget.requestFocus();
    }

    public void showCardTransparencyPicker() {
        Dialog dialog = createGlassDialog("Card Style & Transparency", "Aesthetic styling for application squircle tiles");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        String current = prefs.getCardTransparency();

        View row1 = addSettingRow(list, "🪟", "Frosted Glass (Default)", "Smooth translucent glass tile surface", "glass".equals(current) ? "Active" : "", new TransparencyOptionClickListener(this, "glass", dialog));
        addSettingRow(list, "💎", "Ultra Glass", "30% High-transparency crystal look", "ultra".equals(current) ? "Active" : "", new TransparencyOptionClickListener(this, "ultra", dialog));
        addSettingRow(list, "⬛", "Solid Dark Cards", "100% Opaque high-contrast dark cards", "solid".equals(current) ? "Active" : "", new TransparencyOptionClickListener(this, "solid", dialog));

        dialog.show();
        if (row1 != null) row1.requestFocus();
    }

    public void showGridPicker() {
        Dialog dialog = createGlassDialog("Grid Column Layout", "Number of cards displayed per row on your TV");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        int current = prefs.getColumnCount();
        View first = addSettingRow(list, "🔄", "Auto (Dynamic TV Resolution)", "Automatically optimizes columns for 720p/1080p/4K", current == 0 ? "Active" : "", new GridOptionClickListener(this, 0, dialog));
        addSettingRow(list, "4️⃣", "4 Columns (Spacious Cards)", "Large, easy-to-read app cards", current == 4 ? "Active" : "", new GridOptionClickListener(this, 4, dialog));
        addSettingRow(list, "5️⃣", "5 Columns (Standard 1080p)", "Balanced layout for modern smart TVs", current == 5 ? "Active" : "", new GridOptionClickListener(this, 5, dialog));
        addSettingRow(list, "6️⃣", "6 Columns (Dense Layout)", "More apps visible without scrolling", current == 6 ? "Active" : "", new GridOptionClickListener(this, 6, dialog));
        addSettingRow(list, "7️⃣", "7 Columns (Compact Wide)", "Ideal for ultra-wide or large 4K displays", current == 7 ? "Active" : "", new GridOptionClickListener(this, 7, dialog));

        dialog.show();
        if (first != null) first.requestFocus();
    }

    public void toggleLabels() {
        boolean current = prefs.isShowLabels();
        prefs.setShowLabels(!current);
        Toast.makeText(activity, current ? "App labels hidden" : "App labels shown", Toast.LENGTH_SHORT).show();
        activity.loadAndDisplayApps();
        showMainMenu();
    }

    public void toggleFavorites() {
        boolean current = prefs.isShowFavorites();
        prefs.setShowFavorites(!current);
        Toast.makeText(activity, current ? "Favorites dock capsule hidden" : "Favorites dock capsule shown", Toast.LENGTH_SHORT).show();
        activity.loadAndDisplayApps();
        showMainMenu();
    }

    public void showHiddenAppsDialog() {
        Set<String> hidden = prefs.getHiddenApps();
        if (hidden == null || hidden.isEmpty()) {
            Toast.makeText(activity, "No apps are currently hidden", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = createGlassDialog("Hidden Applications", "Manage hidden apps or unhide them");
        LinearLayout list = (LinearLayout) dialog.findViewById(R.id.settings_list_container);
        if (list == null) return;

        final List<String> hiddenList = new ArrayList<String>(hidden);
        for (final String pkg : hiddenList) {
            addSettingRow(list, "👁️", pkg, "Tap to unhide and restore to grid", "Unhide", new UnhideSingleClickListener(this, pkg));
        }

        addSettingRow(list, "✨", "Unhide All Applications", "Restore all hidden apps at once", "Restore", new UnhideAllClickListener(this, dialog));

        dialog.show();
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

    public static boolean isDefaultHome(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo != null && resolveInfo.activityInfo != null) {
                return context.getPackageName().equals(resolveInfo.activityInfo.packageName);
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private static String getAccentColorName(int color) {
        if (color == PrefsManager.COLOR_CYAN) return "Cyan";
        if (color == PrefsManager.COLOR_EMERALD) return "Emerald";
        if (color == PrefsManager.COLOR_PURPLE) return "Purple";
        if (color == PrefsManager.COLOR_SUNSET) return "Sunset";
        if (color == PrefsManager.COLOR_WHITE) return "White";
        if (color == PrefsManager.COLOR_YELLOW) return "Yellow";
        if (color == PrefsManager.COLOR_PINK) return "Pink";
        if (color == PrefsManager.COLOR_PUMPKIN) return "Pumpkin Orange";
        return "Custom";
    }

    // --- STATIC LISTENERS (Safe for JDK 21 / d8) ---

    private static class DialogDismissClickListener implements View.OnClickListener {
        private final Dialog dialog;
        DialogDismissClickListener(Dialog dialog) { this.dialog = dialog; }
        @Override public void onClick(View v) { dialog.dismiss(); }
    }

    private static class MainRowClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int action;
        MainRowClickListener(DLaunchSettingsDialog parent, int action) {
            this.parent = parent;
            this.action = action;
        }
        @Override
        public void onClick(View v) {
            switch (action) {
                case 0: parent.showSetDefaultLauncherDialog(); break;
                case 1: parent.showAccentColorPicker(); break;
                case 2: parent.showWallpaperMenu(); break;
                case 3: parent.showScreensaverMenu(); break;
                case 4: parent.showDimPicker(); break;
                case 5: parent.showCardTransparencyPicker(); break;
                case 6: parent.showGridPicker(); break;
                case 7: parent.toggleLabels(); break;
                case 8: parent.toggleFavorites(); break;
                case 9: parent.showHiddenAppsDialog(); break;
                case 10: parent.launchSystemSettings(); break;
            }
        }
    }

    private static class DefaultHomeBtnClickListener implements View.OnClickListener {
        private final DLaunchActivity activity;
        private final Dialog dialog;
        DefaultHomeBtnClickListener(DLaunchActivity activity, Dialog dialog) {
            this.activity = activity;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            activity.openDefaultHomeSettings();
            dialog.dismiss();
        }
    }

    private static class AccentClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int color;
        private final Dialog dialog;
        AccentClickListener(DLaunchSettingsDialog parent, int color, Dialog dialog) {
            this.parent = parent;
            this.color = color;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setAccentColor(color);
            parent.activity.applyThemeAndAccent();
            parent.activity.loadAndDisplayApps();
            dialog.dismiss();
            Toast.makeText(parent.activity, "Neon accent applied", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ThemeClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final String theme;
        private final Dialog dialog;
        ThemeClickListener(DLaunchSettingsDialog parent, String theme, Dialog dialog) {
            this.parent = parent;
            this.theme = theme;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setWallpaperMode("gradient");
            parent.prefs.setTheme(theme);
            parent.activity.applyThemeAndAccent();
            dialog.dismiss();
            Toast.makeText(parent.activity, "Liquid theme applied", Toast.LENGTH_SHORT).show();
        }
    }

    private static class SystemWpClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final Dialog dialog;
        SystemWpClickListener(DLaunchSettingsDialog parent, Dialog dialog) {
            this.parent = parent;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setWallpaperMode("system");
            parent.activity.applyThemeAndAccent();
            dialog.dismiss();
            Toast.makeText(parent.activity, "System wallpaper applied", Toast.LENGTH_SHORT).show();
        }
    }

    private static class CustomWpMenuClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final Dialog dialog;
        CustomWpMenuClickListener(DLaunchSettingsDialog parent, Dialog dialog) {
            this.parent = parent;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            parent.showCustomWallpaperMenu();
        }
    }

    private static class CustomWpActionClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int action;
        private final Dialog dialog;
        CustomWpActionClickListener(DLaunchSettingsDialog parent, int action, Dialog dialog) {
            this.parent = parent;
            this.action = action;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            if (action == 0) parent.activity.pickCustomWallpaper();
            else if (action == 1) parent.activity.autoDetectWallpaper();
            else if (action == 2) parent.activity.showWallpaperInstructions();
        }
    }

    private static class ScreensaverTimeoutClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int minutes;
        private final Dialog dialog;
        ScreensaverTimeoutClickListener(DLaunchSettingsDialog parent, int minutes, Dialog dialog) {
            this.parent = parent;
            this.minutes = minutes;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setScreensaverTimeout(minutes);
            parent.activity.resetScreensaverTimer();
            dialog.dismiss();
            String msg = (minutes <= 0) ? "Screensaver disabled" : ("Screensaver set to " + minutes + " min");
            Toast.makeText(parent.activity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private static class PreviewScreensaverClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final Dialog dialog;
        PreviewScreensaverClickListener(DLaunchSettingsDialog parent, Dialog dialog) {
            this.parent = parent;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            dialog.dismiss();
            parent.activity.previewScreensaver();
        }
    }

    private static class DimOptionClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int dim;
        private final Dialog dialog;
        DimOptionClickListener(DLaunchSettingsDialog parent, int dim, Dialog dialog) {
            this.parent = parent;
            this.dim = dim;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setWallpaperDim(dim);
            parent.activity.applyThemeAndAccent();
            dialog.dismiss();
            Toast.makeText(parent.activity, "Dimming set to " + dim + "%", Toast.LENGTH_SHORT).show();
        }
    }

    private static class TransparencyOptionClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final String style;
        private final Dialog dialog;
        TransparencyOptionClickListener(DLaunchSettingsDialog parent, String style, Dialog dialog) {
            this.parent = parent;
            this.style = style;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setCardTransparency(style);
            parent.activity.loadAndDisplayApps();
            dialog.dismiss();
            Toast.makeText(parent.activity, "Card transparency updated", Toast.LENGTH_SHORT).show();
        }
    }

    private static class GridOptionClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final int cols;
        private final Dialog dialog;
        GridOptionClickListener(DLaunchSettingsDialog parent, int cols, Dialog dialog) {
            this.parent = parent;
            this.cols = cols;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setColumnCount(cols);
            parent.activity.calculateOptimalColumns();
            parent.activity.loadAndDisplayApps();
            dialog.dismiss();
            Toast.makeText(parent.activity, "Grid layout updated", Toast.LENGTH_SHORT).show();
        }
    }

    private static class UnhideSingleClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final String pkg;
        UnhideSingleClickListener(DLaunchSettingsDialog parent, String pkg) {
            this.parent = parent;
            this.pkg = pkg;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.setHidden(pkg, false);
            Toast.makeText(parent.activity, "Unhid " + pkg, Toast.LENGTH_SHORT).show();
            parent.activity.loadAndDisplayApps();
            parent.showHiddenAppsDialog();
        }
    }

    private static class UnhideAllClickListener implements View.OnClickListener {
        private final DLaunchSettingsDialog parent;
        private final Dialog dialog;
        UnhideAllClickListener(DLaunchSettingsDialog parent, Dialog dialog) {
            this.parent = parent;
            this.dialog = dialog;
        }
        @Override
        public void onClick(View v) {
            parent.prefs.unhideAll();
            Toast.makeText(parent.activity, "All apps unhidden", Toast.LENGTH_SHORT).show();
            parent.activity.loadAndDisplayApps();
            dialog.dismiss();
        }
    }
}
