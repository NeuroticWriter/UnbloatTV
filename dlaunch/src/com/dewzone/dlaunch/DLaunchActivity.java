package com.dewzone.dlaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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

    public PrefsManager prefs;
    private final List<AppItem> allApps = new ArrayList<AppItem>();
    private final List<AppItem> favoriteApps = new ArrayList<AppItem>();

    private View rootLayout;
    private TextView brandLetter;
    private TextView labelFavorites;
    private TextView labelAllApps;
    private View btnInputs;
    private View btnSettings;
    private DLaunchSettingsDialog settingsDialog;

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
        settingsDialog = new DLaunchSettingsDialog(this, prefs);

        initViews();
        setupHeader();
        applyThemeAndAccent();
        calculateOptimalColumns();
        loadAndDisplayApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDate();
        applyThemeAndAccent();
        loadAndDisplayApps();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.root_layout);
        brandLetter = (TextView) findViewById(R.id.brand_letter);
        labelFavorites = (TextView) findViewById(R.id.label_favorites);
        labelAllApps = (TextView) findViewById(R.id.label_all_apps);
        btnInputs = findViewById(R.id.btn_inputs);
        btnSettings = findViewById(R.id.btn_settings);

        favoritesRow = (LinearLayout) findViewById(R.id.favorites_row);
        allAppsContainer = (LinearLayout) findViewById(R.id.all_apps_grid_container);
        sectionFavoritesContainer = findViewById(R.id.section_favorites_container);
        mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        dateView = (TextView) findViewById(R.id.date_view);
        emptyView = (TextView) findViewById(R.id.tv_empty);
    }

    private void setupHeader() {
        updateDate();

        if (btnInputs != null) {
            btnInputs.setOnFocusChangeListener(new FocusAnimator(1.06f));
            btnInputs.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_INPUTS));
            if (btnSettings != null) {
                btnInputs.setNextFocusRightId(btnSettings.getId());
            }
        }

        if (btnSettings != null) {
            btnSettings.setOnFocusChangeListener(new FocusAnimator(1.06f));
            btnSettings.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_SETTINGS));
            if (btnInputs != null) {
                btnSettings.setNextFocusLeftId(btnInputs.getId());
            }
        }

        // Clicking brand pill also opens DLaunch settings
        View brandPill = findViewById(R.id.brand_letter);
        if (brandPill != null && brandPill.getParent() instanceof View) {
            View brandContainer = (View) brandPill.getParent();
            brandContainer.setClickable(true);
            brandContainer.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_SETTINGS));
        }
    }

    public void applyThemeAndAccent() {
        int accentColor = prefs.getAccentColor();
        String theme = prefs.getTheme();

        // 1. Wallpaper Theme
        if (rootLayout != null) {
            if ("aurora".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_aurora);
            } else if ("cyber".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_cyber);
            } else if ("sunset".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_sunset);
            } else if ("oled".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_oled);
            } else {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_obsidian);
            }
        }

        // 2. Brand & Header Accents
        if (brandLetter != null) {
            brandLetter.setTextColor(accentColor);
        }
        if (labelFavorites != null) {
            labelFavorites.setTextColor(accentColor);
        }

        // 3. Header Action Buttons
        float density = getResources().getDisplayMetrics().density;
        if (btnInputs != null) {
            btnInputs.setBackground(createPillSelector(density, accentColor));
        }
        if (btnSettings != null) {
            btnSettings.setBackground(createPillSelector(density, accentColor));
        }
    }

    public static Drawable createPillSelector(float density, int accentColor) {
        int radiusPx = (int) (24 * density);
        int strokeNormalPx = (int) (1 * density);
        int strokeFocusedPx = (int) (2 * density);

        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radiusPx);
        normal.setColor(Color.parseColor("#101A29"));
        normal.setStroke(strokeNormalPx, Color.parseColor("#233346"));

        GradientDrawable focused = new GradientDrawable();
        focused.setShape(GradientDrawable.RECTANGLE);
        focused.setCornerRadius(radiusPx);
        focused.setColor(accentColor);
        focused.setStroke(strokeFocusedPx, Color.WHITE);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{android.R.attr.state_pressed}, focused);
        states.addState(new int[]{}, normal);
        return states;
    }

    public static Drawable createTileSelector(float density, int radiusDp, int accentColor) {
        int radiusPx = (int) (radiusDp * density);
        int strokeNormalPx = (int) (1 * density);
        int strokeFocusedPx = (int) (2.5f * density);

        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radiusPx);
        normal.setColor(Color.parseColor("#141E2E"));
        normal.setStroke(strokeNormalPx, Color.parseColor("#253549"));

        GradientDrawable focused = new GradientDrawable();
        focused.setShape(GradientDrawable.RECTANGLE);
        focused.setCornerRadius(radiusPx);
        focused.setColor(Color.parseColor("#1E2E48"));
        focused.setStroke(strokeFocusedPx, accentColor);

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{android.R.attr.state_pressed}, focused);
        states.addState(new int[]{}, normal);
        return states;
    }

    private void updateDate() {
        if (dateView != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            dateView.setText(sdf.format(new Date()));
        }
    }

    public void calculateOptimalColumns() {
        int overrideCols = prefs.getColumnCount();
        if (overrideCols >= 3 && overrideCols <= 8) {
            calculatedColumns = overrideCols;
            return;
        }

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

    public void loadAndDisplayApps() {
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
        boolean showFavorites = prefs.isShowFavorites();

        // 1. Render Favorites Shelf
        if (!showFavorites || favoriteApps.isEmpty()) {
            sectionFavoritesContainer.setVisibility(View.GONE);
        } else {
            sectionFavoritesContainer.setVisibility(View.VISIBLE);
            for (AppItem app : favoriteApps) {
                View tile = createTileView(inflater, favoritesRow, app, true);
                tile.setId(View.generateViewId());
                favoritesRow.addView(tile);
            }
            // Lock horizontal focus strictly within favorites shelf and link UP to header
            int favCount = favoritesRow.getChildCount();
            for (int i = 0; i < favCount; i++) {
                View curr = favoritesRow.getChildAt(i);
                if (btnInputs != null) {
                    curr.setNextFocusUpId(btnInputs.getId());
                }
                if (i > 0) {
                    View prev = favoritesRow.getChildAt(i - 1);
                    curr.setNextFocusLeftId(prev.getId());
                    prev.setNextFocusRightId(curr.getId());
                }
            }
            if (favCount > 0 && btnInputs != null && btnSettings != null) {
                btnInputs.setNextFocusDownId(favoritesRow.getChildAt(0).getId());
                btnSettings.setNextFocusDownId(favoritesRow.getChildAt(0).getId());
            }
        }

        // 2. Render All Apps in Grid Rows
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
                        tile.setId(View.generateViewId());
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
                        dummy.setFocusable(false);
                        dummy.setFocusableInTouchMode(false);
                        rowLayout.addView(dummy);
                    }
                }

                // Lock horizontal focus strictly within each row
                int rowItemCount = rowLayout.getChildCount();
                View lastRealTile = null;
                for (int i = 0; i < rowItemCount; i++) {
                    View curr = rowLayout.getChildAt(i);
                    if (curr.isFocusable()) {
                        if (lastRealTile != null) {
                            curr.setNextFocusLeftId(lastRealTile.getId());
                            lastRealTile.setNextFocusRightId(curr.getId());
                        }
                        lastRealTile = curr;
                    }
                }

                if (r == 0 && (!showFavorites || favoriteApps.isEmpty())) {
                    for (int i = 0; i < rowItemCount; i++) {
                        View curr = rowLayout.getChildAt(i);
                        if (btnInputs != null) {
                            curr.setNextFocusUpId(btnInputs.getId());
                        }
                    }
                    if (rowItemCount > 0 && btnInputs != null && btnSettings != null) {
                        btnInputs.setNextFocusDownId(rowLayout.getChildAt(0).getId());
                        btnSettings.setNextFocusDownId(rowLayout.getChildAt(0).getId());
                    }
                }

                allAppsContainer.addView(rowLayout);
            }
        }

        // Ensure remote focus lands immediately on first tile with 0 clicks
        if (showFavorites && favoritesRow != null && favoritesRow.getChildCount() > 0) {
            favoritesRow.getChildAt(0).requestFocus();
        } else if (allAppsContainer != null && allAppsContainer.getChildCount() > 0) {
            View firstChild = allAppsContainer.getChildAt(0);
            if (firstChild instanceof LinearLayout) {
                LinearLayout firstRow = (LinearLayout) firstChild;
                if (firstRow.getChildCount() > 0) {
                    firstRow.getChildAt(0).requestFocus();
                }
            }
        }
    }

    private View createTileView(LayoutInflater inflater, ViewGroup parent, AppItem app, boolean isFavoriteShelf) {
        View tile = inflater.inflate(R.layout.item_app_tile, parent, false);

        float density = getResources().getDisplayMetrics().density;
        LinearLayout.LayoutParams params;
        if (isFavoriteShelf) {
            int tileWidth = (int) (180 * density);
            params = new LinearLayout.LayoutParams(tileWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 8, 8, 8);
        } else {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            params.setMargins(8, 8, 8, 8);
        }
        tile.setLayoutParams(params);

        // Apply dynamic theme tile selector (accent border and card radius)
        tile.setBackground(createTileSelector(density, prefs.getCardRadius(), prefs.getAccentColor()));

        ImageView iconView = (ImageView) tile.findViewById(R.id.app_icon);
        TextView labelView = (TextView) tile.findViewById(R.id.app_label);
        ImageView starBadge = (ImageView) tile.findViewById(R.id.badge_favorite);

        if (app.getIcon() != null) {
            iconView.setImageDrawable(app.getIcon());
            int w = app.getIcon().getIntrinsicWidth();
            int h = app.getIcon().getIntrinsicHeight();
            if (w > 0 && h > 0 && (float) w / h >= 1.35f) {
                // Wide Leanback 16:9 banner
                iconView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                iconView.getLayoutParams().height = (int) (64 * density);
            } else {
                // Standard square app icon
                int size = (int) (64 * density);
                iconView.getLayoutParams().width = size;
                iconView.getLayoutParams().height = size;
            }
        }

        // App Labels toggle (Show / Hide)
        if (prefs.isShowLabels()) {
            labelView.setVisibility(View.VISIBLE);
            labelView.setText(app.getLabel());
        } else {
            labelView.setVisibility(View.GONE);
        }

        if (app.isFavorite() && !isFavoriteShelf) {
            starBadge.setVisibility(View.VISIBLE);
        } else {
            starBadge.setVisibility(View.GONE);
        }

        // TV Remote Focus Animation, Marquee & Strict Horizontal Navigation
        tile.setOnFocusChangeListener(new TileFocusListener(labelView));
        tile.setOnKeyListener(new TileKeyListener());
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

    public void showSettingsMenu() {
        if (settingsDialog != null) {
            settingsDialog.showMainMenu();
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

        TileFocusListener(TextView labelView) {
            this.labelView = labelView;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (labelView != null) {
                labelView.setSelected(hasFocus); // Enables marquee text scrolling
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

    private static class TileKeyListener implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    View next = findNextFocusableInRow(v, 1);
                    if (next != null) {
                        next.requestFocus();
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    View prev = findNextFocusableInRow(v, -1);
                    if (prev != null) {
                        prev.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static View findNextFocusableInRow(View current, int delta) {
        if (current != null && current.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) current.getParent();
            int index = parent.indexOfChild(current);
            int target = index + delta;
            while (target >= 0 && target < parent.getChildCount()) {
                View sibling = parent.getChildAt(target);
                if (sibling != null && sibling.isFocusable() && sibling.getVisibility() == View.VISIBLE) {
                    return sibling;
                }
                target += delta;
            }
        }
        return null;
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
                activity.showSettingsMenu();
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
