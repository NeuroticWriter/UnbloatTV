package com.dewzone.dlaunch;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final String packageName;
    private final String activityName;
    private final String label;
    private final Drawable icon;
    private final boolean isLeanback;
    private boolean isFavorite;

    public AppItem(String packageName, String activityName, String label, Drawable icon, boolean isLeanback) {
        this.packageName = packageName != null ? packageName : "";
        this.activityName = activityName != null ? activityName : "";
        this.label = label != null ? label : this.packageName;
        this.icon = icon;
        this.isLeanback = isLeanback;
        this.isFavorite = false;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getLabel() {
        return label;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isLeanback() {
        return isLeanback;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}

