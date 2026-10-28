package com.dewzone.dlaunch;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PrefsManager {
    private static final String PREFS_NAME = "dlaunch_prefs";
    private static final String KEY_FAVORITES = "key_favorites";
    private static final String KEY_HIDDEN = "key_hidden";
    private static final String KEY_INITIALIZED = "key_initialized";

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
            Set<String> defaults = new HashSet<>();
            for (String pkg : DEFAULT_FAVORITES) {
                defaults.add(pkg);
            }
            prefs.edit()
                .putStringSet(KEY_FAVORITES, defaults)
                .putBoolean(KEY_INITIALIZED, true)
                .apply();
        }
    }

    public boolean isFavorite(String packageName) {
        Set<String> favs = prefs.getStringSet(KEY_FAVORITES, null);
        return favs != null && favs.contains(packageName);
    }

    public void setFavorite(String packageName, boolean favorite) {
        Set<String> favs = new HashSet<>(prefs.getStringSet(KEY_FAVORITES, new HashSet<String>()));
        if (favorite) {
            favs.add(packageName);
        } else {
            favs.remove(packageName);
        }
        prefs.edit().putStringSet(KEY_FAVORITES, favs).apply();
    }

    public boolean isHidden(String packageName) {
        Set<String> hidden = prefs.getStringSet(KEY_HIDDEN, null);
        return hidden != null && hidden.contains(packageName);
    }

    public void setHidden(String packageName, boolean hidden) {
        Set<String> set = new HashSet<>(prefs.getStringSet(KEY_HIDDEN, new HashSet<String>()));
        if (hidden) {
            set.add(packageName);
        } else {
            set.remove(packageName);
        }
        prefs.edit().putStringSet(KEY_HIDDEN, set).apply();
    }

    public void unhideAll() {
        prefs.edit().remove(KEY_HIDDEN).apply();
    }
}

