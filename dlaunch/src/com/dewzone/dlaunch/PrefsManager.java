package com.dewzone.dlaunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.HashSet;
import java.util.Set;

public class PrefsManager {
    private static final String PREFS_NAME = "dlaunch_prefs";
    private static final String KEY_FAVORITES = "key_favorites";
    private static final String KEY_HIDDEN = "key_hidden";
    private static final String KEY_INITIALIZED = "key_initialized";

    // Visual Customization Keys
    private static final String KEY_ACCENT_COLOR = "key_accent_color";
    private static final String KEY_THEME = "key_theme";
    private static final String KEY_COLUMNS = "key_columns";
    private static final String KEY_SHOW_LABELS = "key_show_labels";
    private static final String KEY_SHOW_FAVORITES = "key_show_favorites";
    private static final String KEY_CARD_RADIUS = "key_card_radius";
    private static final String KEY_WALLPAPER_MODE = "key_wallpaper_mode";
    private static final String KEY_WALLPAPER_DIM = "key_wallpaper_dim";
    private static final String KEY_CARD_TRANSPARENCY = "key_card_transparency";
    private static final String KEY_SCREENSAVER_TIMEOUT = "key_screensaver_timeout";

    // Default accent colors
    public static final int COLOR_CYAN = Color.parseColor("#00E5FF");
    public static final int COLOR_EMERALD = Color.parseColor("#10B981");
    public static final int COLOR_PURPLE = Color.parseColor("#A855F7");
    public static final int COLOR_SUNSET = Color.parseColor("#F97316");
    public static final int COLOR_WHITE = Color.parseColor("#F8FAFC");
    public static final int COLOR_YELLOW = Color.parseColor("#EAB308");
    public static final int COLOR_PINK = Color.parseColor("#EC4899");

    private final SharedPreferences prefs;

    // Smart default favorite packages
    private static final String[] DEFAULT_FAVORITES = {
        "com.google.android.youtube.tv",
        "org.schabi.newpipe",
        "com.liskovsoft.videomanager",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        "org.xbmc.kodi",
        "com.plexapp.android",
        "org.videolan.vlc"
    };

    public PrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initDefaults();
    }

    private void initDefaults() {
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            Set<String> defaults = new HashSet<String>();
            for (String pkg : DEFAULT_FAVORITES) {
                defaults.add(pkg);
            }
            prefs.edit()
                .putStringSet(KEY_FAVORITES, defaults)
                .putInt(KEY_ACCENT_COLOR, COLOR_CYAN)
                .putString(KEY_THEME, "nebula")
                .putInt(KEY_COLUMNS, 0) // 0 = Auto
                .putBoolean(KEY_SHOW_LABELS, true)
                .putBoolean(KEY_SHOW_FAVORITES, true)
                .putInt(KEY_CARD_RADIUS, 18)
                .putBoolean(KEY_INITIALIZED, true)
                .apply();
        }
    }

    // --- Favorites ---
    public boolean isFavorite(String packageName) {
        Set<String> favs = prefs.getStringSet(KEY_FAVORITES, null);
        return favs != null && favs.contains(packageName);
    }

    public void setFavorite(String packageName, boolean favorite) {
        Set<String> favs = new HashSet<String>(prefs.getStringSet(KEY_FAVORITES, new HashSet<String>()));
        if (favorite) {
            favs.add(packageName);
        } else {
            favs.remove(packageName);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
    }

    // --- Hidden Apps ---
    public boolean isHidden(String packageName) {
        Set<String> hidden = prefs.getStringSet(KEY_HIDDEN, null);
        return hidden != null && hidden.contains(packageName);
    }

    public void setHidden(String packageName, boolean hidden) {
        Set<String> set = new HashSet<String>(prefs.getStringSet(KEY_HIDDEN, new HashSet<String>()));
        if (hidden) {
            set.add(packageName);
        } else {
            set.remove(packageName);
        }
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply();
    }

    public Set<String> getHiddenApps() {
        return prefs.getStringSet(KEY_HIDDEN, new HashSet<String>());
    }

    public void unhideAll() {
        prefs.edit().remove(KEY_HIDDEN).apply();
    }

    // --- Accent Color ---
    public int getAccentColor() {
        return prefs.getInt(KEY_ACCENT_COLOR, COLOR_CYAN);
    }

    public void setAccentColor(int color) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply();
    }

    // --- Theme ---
    public String getTheme() {
        return prefs.getString(KEY_THEME, "nebula");
    }

    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }

    // --- Grid Columns ---
    public int getColumnCount() {
        return prefs.getInt(KEY_COLUMNS, 0); // 0 = Auto
    }

    public void setColumnCount(int columns) {
        prefs.edit().putInt(KEY_COLUMNS, columns).apply();
    }

    // --- App Labels ---
    public boolean isShowLabels() {
        return prefs.getBoolean(KEY_SHOW_LABELS, true);
    }

    public void setShowLabels(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_LABELS, show).apply();
    }

    // --- Favorites Shelf Visibility ---
    public boolean isShowFavorites() {
        return prefs.getBoolean(KEY_SHOW_FAVORITES, true);
    }

    public void setShowFavorites(boolean show) {
        prefs.edit().putBoolean(KEY_SHOW_FAVORITES, show).apply();
    }

    // --- Card Corner Radius (dp) ---
    public int getCardRadius() {
        return prefs.getInt(KEY_CARD_RADIUS, 14);
    }

    public void setCardRadius(int radiusDp) {
        prefs.edit().putInt(KEY_CARD_RADIUS, radiusDp).apply();
    }

    // --- Wallpaper Mode ("gradient", "system", "custom") ---
    public String getWallpaperMode() {
        return prefs.getString(KEY_WALLPAPER_MODE, "gradient");
    }

    public void setWallpaperMode(String mode) {
        prefs.edit().putString(KEY_WALLPAPER_MODE, mode).apply();
    }

    // --- Wallpaper Dim Overlay (0, 25, 45, 65, 80 percent) ---
    public int getWallpaperDim() {
        return prefs.getInt(KEY_WALLPAPER_DIM, 45);
    }

    public void setWallpaperDim(int percent) {
        prefs.edit().putInt(KEY_WALLPAPER_DIM, percent).apply();
    }

    // --- Card Transparency ("solid", "glass", "ultra") ---
    public String getCardTransparency() {
        return prefs.getString(KEY_CARD_TRANSPARENCY, "glass");
    }

    public void setCardTransparency(String style) {
        prefs.edit().putString(KEY_CARD_TRANSPARENCY, style).apply();
    }

    // --- Screensaver Timeout (in minutes, 0 = disabled, 1, 2, 3, 5, 10) ---
    public int getScreensaverTimeout() {
        return prefs.getInt(KEY_SCREENSAVER_TIMEOUT, 3);
    }

    public void setScreensaverTimeout(int minutes) {
        prefs.edit().putInt(KEY_SCREENSAVER_TIMEOUT, minutes).apply();
    }
}
