package com.dewzone.dlaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DLaunchActivity extends Activity {

    private PrefsManager prefs;
    private final List<AppItem> allApps = new ArrayList<AppItem>();
    private final List<AppItem> favoriteApps = new ArrayList<AppItem>();

    private LinearLayout favoritesRow;
    private LinearLayout allAppsContainer;
    private View sectionFavoritesContainer;
    private ScrollView mainScroll;
    private TextView dateView;
    private TextView emptyView;

    private int calculatedColumns = 5;

    private static final AppComparator APP_COMPARATOR = new AppComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlaunch);

        prefs = new PrefsManager(this);

        initViews();
        setupHeader();
        calculateOptimalColumns();
        loadAndDisplayApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDate();
        loadAndDisplayApps();
    }

    private void initViews() {
        favoritesRow = (LinearLayout) findViewById(R.id.favorites_row);
        allAppsContainer = (LinearLayout) findViewById(R.id.all_apps_grid_container);
        sectionFavoritesContainer = findViewById(R.id.section_favorites_container);
        mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        dateView = (TextView) findViewById(R.id.date_view);
        emptyView = (TextView) findViewById(R.id.tv_empty);
    }

    private void setupHeader() {
        updateDate();

        View btnInputs = findViewById(R.id.btn_inputs);
        if (btnInputs != null) {
            btnInputs.setOnFocusChangeListener(new FocusAnimator(1.06f));
            btnInputs.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_INPUTS));
        }

        View btnSettings = findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnFocusChangeListener(new FocusAnimator(1.06f));
            btnSettings.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_SETTINGS));
        }
    }

    private void updateDate() {
        if (dateView != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            dateView.setText(sdf.format(new Date()));
        }
    }

    /**
     * Auto-Adjusting Grid Engine:
     * Calculates optimal column count and tile dimensions based on TV resolution and DPI.
     */
    private void calculateOptimalColumns() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float widthDp = metrics.widthPixels / metrics.density;

        if (widthDp < 850) {
            calculatedColumns = 4; // 720p / compact display
        } else if (widthDp < 1250) {
            calculatedColumns = 5; // 1080p standard FHD TV
        } else {
            calculatedColumns = 6; // 4K UHD or wide format
        }
    }

    private void loadAndDisplayApps() {
        PackageManager pm = getPackageManager();
        Map<String, AppItem> appMap = new HashMap<String, AppItem>();

        // 1. Query Leanback Launcher apps (primary TV apps)
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN, null);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> leanbackApps = pm.queryIntentActivities(leanbackIntent, 0);

        if (leanbackApps != null) {
            for (ResolveInfo info : leanbackApps) {
                String pkg = info.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue; // Skip DLaunch self

                String label = info.loadLabel(pm).toString();
                Drawable icon = null;
                if (info.activityInfo != null) {
                    icon = info.activityInfo.loadBanner(pm);
                }
                if (icon == null) {
                    icon = info.loadIcon(pm);
                }

                AppItem item = new AppItem(pkg, info.activityInfo.name, label, icon, true);
                appMap.put(pkg, item);
            }
        }

        // 2. Query Standard Launcher apps (sideloaded / mobile apps without TV banners)
        Intent standardIntent = new Intent(Intent.ACTION_MAIN, null);
        standardIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> standardApps = pm.queryIntentActivities(standardIntent, 0);

        if (standardApps != null) {
            for (ResolveInfo info : standardApps) {
                String pkg = info.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue;

                if (!appMap.containsKey(pkg)) {
                    String label = info.loadLabel(pm).toString();
                    Drawable icon = info.loadIcon(pm);
                    AppItem item = new AppItem(pkg, info.activityInfo.name, label, icon, false);
                    appMap.put(pkg, item);
                }
            }
        }

        allApps.clear();
        favoriteApps.clear();

        for (AppItem app : appMap.values()) {
            if (prefs.isHidden(app.getPackageName())) {
                continue;
            }

            if (prefs.isFavorite(app.getPackageName())) {
                app.setFavorite(true);
                favoriteApps.add(app);
            }
            allApps.add(app);
        }

        Collections.sort(allApps, APP_COMPARATOR);
        Collections.sort(favoriteApps, APP_COMPARATOR);

        renderUI();
    }

    private void renderUI() {
        favoritesRow.removeAllViews();
        allAppsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        // 1. Render Favorites Shelf
        if (favoriteApps.isEmpty()) {
            sectionFavoritesContainer.setVisibility(View.GONE);
        } else {
            sectionFavoritesContainer.setVisibility(View.VISIBLE);
            for (AppItem app : favoriteApps) {
                View tile = createTileView(inflater, favoritesRow, app, true);
                favoritesRow.addView(tile);
            }
        }

        // 2. Render All Apps in Auto-Adjusting Grid Rows
        if (allApps.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);

            int total = allApps.size();
            int rows = (int) Math.ceil((double) total / calculatedColumns);

            for (int r = 0; r < rows; r++) {
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setClipChildren(false);
                rowLayout.setClipToPadding(false);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                rowLayout.setLayoutParams(rowParams);

                for (int c = 0; c < calculatedColumns; c++) {
                    int index = r * calculatedColumns + c;
                    if (index < total) {
                        AppItem app = allApps.get(index);
                        View tile = createTileView(inflater, rowLayout, app, false);
                        rowLayout.addView(tile);
                    } else {
                        // Invisible dummy spacer to preserve exact column widths
                        View dummy = new View(this);
                        LinearLayout.LayoutParams dummyParams = new LinearLayout.LayoutParams(
                                0,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                1.0f
                        );
                        dummy.setLayoutParams(dummyParams);
                        rowLayout.addView(dummy);
                    }
                }

                allAppsContainer.addView(rowLayout);
            }
        }
    }

    private View createTileView(LayoutInflater inflater, ViewGroup parent, AppItem app, boolean isFavoriteShelf) {
        View tile = inflater.inflate(R.layout.item_app_tile, parent, false);

        LinearLayout.LayoutParams params;
        if (isFavoriteShelf) {
            int tileWidth = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size) * 3;
            params = new LinearLayout.LayoutParams(tileWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8);
        } else {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(8, 8, 8, 8);
        }
        tile.setLayoutParams(params);

        ImageView iconView = (ImageView) tile.findViewById(R.id.app_icon);
        TextView labelView = (TextView) tile.findViewById(R.id.app_label);
        ImageView starBadge = (ImageView) tile.findViewById(R.id.badge_favorite);

        if (app.getIcon() != null) {
            iconView.setImageDrawable(app.getIcon());
        }

        labelView.setText(app.getLabel());

        if (app.isFavorite() && !isFavoriteShelf) {
            starBadge.setVisibility(View.VISIBLE);
        } else {
            starBadge.setVisibility(View.GONE);
        }

        // TV Remote Focus Animation & Marquee Effect
        tile.setOnFocusChangeListener(new TileFocusListener(labelView));
        tile.setOnClickListener(new TileClickListener(this, app));
        tile.setOnLongClickListener(new TileLongClickListener(this, app));

        return tile;
    }

    public void launchApp(AppItem app) {
        try {
            Intent intent;
            if (app.isLeanback()) {
                intent = getPackageManager().getLeanbackLaunchIntentForPackage(app.getPackageName());
            } else {
                intent = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
            }

            if (intent == null) {
                intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName(app.getPackageName(), app.getActivityName()));
            }

            intent.setAction(Intent.ACTION_MAIN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to launch " + app.getLabel(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showAppContextMenu(final AppItem app) {
        final boolean isFav = prefs.isFavorite(app.getPackageName());
        String favAction = isFav ? getString(R.string.dialog_unpin_favorite) : getString(R.string.dialog_pin_favorite);

        CharSequence[] options = new CharSequence[]{
                favAction,
                getString(R.string.dialog_app_info),
                getString(R.string.dialog_hide_app)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle(app.getLabel());
        builder.setItems(options, new DialogMenuListener(this, app, isFav));
        builder.show();
    }

    public void openAppDetails(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open details", Toast.LENGTH_SHORT).show();
        }
    }

    public void launchInputs() {
        Intent[] inputIntents = new Intent[]{
                new Intent("com.android.tv.action.VIEW_INPUTS"),
                new Intent("android.media.tv.action.VIEW_INPUTS"),
                new Intent(Intent.ACTION_VIEW, Uri.parse("content://android.media.tv/passthrough"))
        };

        for (Intent intent : inputIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Exception ignored) {
            }
        }
        Toast.makeText(this, "Input switcher not supported on this TV hardware", Toast.LENGTH_SHORT).show();
    }

    public void launchSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open Settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mainScroll != null && mainScroll.getScrollY() > 50) {
                mainScroll.smoothScrollTo(0, 0);
                return true;
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (mainScroll != null) {
                mainScroll.smoothScrollTo(0, 0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // --- STATIC LISTENERS (Prevents JDK21 synthetic capture bug) ---

    private static class TileFocusListener implements View.OnFocusChangeListener {
        private final TextView labelView;

        public TileFocusListener(TextView labelView) {
            this.labelView = labelView;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (labelView != null) {
                labelView.setSelected(hasFocus);
            }
            if (hasFocus) {
                v.animate().scaleX(1.09f).scaleY(1.09f).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(12f);
                }
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(0f);
                }
            }
        }
    }

    private static class TileClickListener implements View.OnClickListener {
        private final DLaunchActivity activity;
        private final AppItem app;

        public TileClickListener(DLaunchActivity activity, AppItem app) {
            this.activity = activity;
            this.app = app;
        }

        @Override
        public void onClick(View v) {
            activity.launchApp(app);
        }
    }

    private static class TileLongClickListener implements View.OnLongClickListener {
        private final DLaunchActivity activity;
        private final AppItem app;

        public TileLongClickListener(DLaunchActivity activity, AppItem app) {
            this.activity = activity;
            this.app = app;
        }

        @Override
        public boolean onLongClick(View v) {
            activity.showAppContextMenu(app);
            return true;
        }
    }

    private static class DialogMenuListener implements DialogInterface.OnClickListener {
        private final DLaunchActivity activity;
        private final AppItem app;
        private final boolean isFav;

        public DialogMenuListener(DLaunchActivity activity, AppItem app, boolean isFav) {
            this.activity = activity;
            this.app = app;
            this.isFav = isFav;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    activity.prefs.setFavorite(app.getPackageName(), !isFav);
                    activity.loadAndDisplayApps();
                    break;
                case 1:
                    activity.openAppDetails(app.getPackageName());
                    break;
                case 2:
                    activity.prefs.setHidden(app.getPackageName(), true);
                    activity.loadAndDisplayApps();
                    Toast.makeText(activity, app.getLabel() + " hidden", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private static class ActionClickListener implements View.OnClickListener {
        public static final int ACTION_INPUTS = 1;
        public static final int ACTION_SETTINGS = 2;

        private final DLaunchActivity activity;
        private final int action;

        public ActionClickListener(DLaunchActivity activity, int action) {
            this.activity = activity;
            this.action = action;
        }

        @Override
        public void onClick(View v) {
            if (action == ACTION_INPUTS) {
                activity.launchInputs();
            } else if (action == ACTION_SETTINGS) {
                activity.launchSettings();
            }
        }
    }

    public static class FocusAnimator implements View.OnFocusChangeListener {
        private final float scale;

        public FocusAnimator(float scale) {
            this.scale = scale;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.animate().scaleX(scale).scaleY(scale).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(8f);
                }
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(0f);
                }
            }
        }
    }
}
