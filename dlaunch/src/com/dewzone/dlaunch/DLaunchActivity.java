package com.dewzone.dlaunch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.content.res.Configuration;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DLaunchActivity extends Activity {

    public static final int REQUEST_PICK_WALLPAPER = 1001;

    public PrefsManager prefs;
    private final List<AppItem> allApps = new ArrayList<AppItem>();
    private final List<AppItem> favoriteApps = new ArrayList<AppItem>();

    private FrameLayout appRoot;
    private ImageView wallpaperView;
    private View wallpaperDimOverlay;
    private ImageView heroArtView;
    private View heroScrim;
    private View ambientGlowView;
    private View rootLayout;

    // Header Views
    private TextView greetingView;
    private TextClock clockView;
    private TextClock ampmView;
    private View clockDivider;
    private TextView dateView;
    private View brandContainer;
    private TextView brandLetter;
    private TextView brandTitle;
    private View btnInputs;
    private View btnWifi;
    private View btnSettings;
    private TextView tvInputsLabel;
    private TextView tvWifiLabel;
    private TextView tvSettingsLabel;

    // Hero Showcase Views
    private View heroContainer;
    private View heroIconFrame;
    private ImageView heroIconView;
    private TextView heroTitleView;
    private TextView heroSubtitleView;
    private TextView heroBadgePill;

    // Floating Glass Highlight
    private GlassHighlightView glassHighlight;

    // Lists & Grids
    private LinearLayout favoritesRow;
    private HorizontalScrollView favoritesScroll;
    private LinearLayout allAppsContainer;
    private View sectionFavoritesContainer;
    private ScrollView mainScroll;
    private TextView labelAllApps;
    private TextView emptyView;

    private DLaunchSettingsDialog settingsDialog;

    // Calm Ambient Screensaver
    private View screensaverLayout;
    private TextClock screensaverClock;
    private TextView screensaverDate;
    private TextView screensaverSubtitle;
    private final Handler screensaverHandler = new Handler(Looper.getMainLooper());
    private boolean isScreensaverActive = false;
    private final Runnable screensaverRunnable = new ScreensaverTimerRunnable(this);

    private int calculatedColumns = 5;
    private float density = 1.0f;
    private View currentlyFocusedTile = null;

    // Dynamic Responsive Multi-Size TV Metrics
    private float widthDp = 960f;
    private float heightDp = 540f;
    private int responsiveFavoriteTileSize = 88;
    private int responsiveFavoriteIconSize = 56;
    private int responsiveGridIconSize = 64;
    private int responsiveHeroIconFrameSize = 54;
    private float responsiveHeroTitleSp = 26f;
    private float responsiveClockSp = 40f;

    private static final AppComparator APP_COMPARATOR = new AppComparator();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dlaunch);

        density = getResources().getDisplayMetrics().density;
        prefs = new PrefsManager(this);
        settingsDialog = new DLaunchSettingsDialog(this, prefs);

        initViews();
        applyResponsiveMetrics();
        setupHeader();
        handleIntentExtras(getIntent());
        applyThemeAndAccent();
        calculateOptimalColumns();
        loadAndDisplayApps();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }

    private void handleIntentExtras(Intent intent) {
        if (intent == null) return;
        boolean changed = false;
        if (intent.hasExtra("set_theme")) {
            prefs.setTheme(intent.getStringExtra("set_theme"));
            prefs.setWallpaperMode("gradient");
            changed = true;
        }
        if (intent.hasExtra("set_accent")) {
            prefs.setAccentColor(intent.getIntExtra("set_accent", PrefsManager.COLOR_PUMPKIN));
            changed = true;
        }
        if (changed) {
            applyThemeAndAccent();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyResponsiveMetrics();
        updateGreetingAndDate();
        applyThemeAndAccent();
        calculateOptimalColumns();
        loadAndDisplayApps();
        resetScreensaverTimer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyResponsiveMetrics();
        calculateOptimalColumns();
        loadAndDisplayApps();
    }

    @Override
    protected void onPause() {
        super.onPause();
        screensaverHandler.removeCallbacks(screensaverRunnable);
        if (isScreensaverActive) {
            hideScreensaver();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        screensaverHandler.removeCallbacks(screensaverRunnable);
    }

    private void initViews() {
        appRoot = (FrameLayout) findViewById(R.id.app_root);
        wallpaperView = (ImageView) findViewById(R.id.wallpaper_view);
        wallpaperDimOverlay = findViewById(R.id.wallpaper_dim_overlay);
        heroArtView = (ImageView) findViewById(R.id.hero_art_view);
        heroScrim = findViewById(R.id.hero_scrim);
        ambientGlowView = findViewById(R.id.ambient_glow_view);
        rootLayout = findViewById(R.id.root_layout);

        greetingView = (TextView) findViewById(R.id.greeting_view);
        clockView = (TextClock) findViewById(R.id.clock_view);
        ampmView = (TextClock) findViewById(R.id.ampm_view);
        clockDivider = findViewById(R.id.clock_divider);
        dateView = (TextView) findViewById(R.id.date_view);
        brandContainer = findViewById(R.id.brand_container);
        brandLetter = (TextView) findViewById(R.id.brand_letter);
        brandTitle = (TextView) findViewById(R.id.brand_title);

        btnInputs = findViewById(R.id.btn_inputs);
        btnWifi = findViewById(R.id.btn_wifi);
        btnSettings = findViewById(R.id.btn_settings);
        tvInputsLabel = (TextView) findViewById(R.id.tv_inputs_label);
        tvWifiLabel = (TextView) findViewById(R.id.tv_wifi_label);
        tvSettingsLabel = (TextView) findViewById(R.id.tv_settings_label);

        heroContainer = findViewById(R.id.hero_container);
        heroIconFrame = findViewById(R.id.hero_icon_frame);
        heroIconView = (ImageView) findViewById(R.id.hero_icon_view);
        heroTitleView = (TextView) findViewById(R.id.hero_title_view);
        heroSubtitleView = (TextView) findViewById(R.id.hero_subtitle_view);
        heroBadgePill = (TextView) findViewById(R.id.hero_badge_pill);

        glassHighlight = (GlassHighlightView) findViewById(R.id.glass_highlight);

        favoritesScroll = (HorizontalScrollView) findViewById(R.id.favorites_scroll);
        favoritesRow = (LinearLayout) findViewById(R.id.favorites_row);
        sectionFavoritesContainer = findViewById(R.id.section_favorites_container);
        allAppsContainer = (LinearLayout) findViewById(R.id.all_apps_grid_container);
        labelAllApps = (TextView) findViewById(R.id.label_all_apps);
        mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        emptyView = (TextView) findViewById(R.id.tv_empty);

        // Screensaver views
        screensaverLayout = findViewById(R.id.screensaver_layout);
        screensaverClock = (TextClock) findViewById(R.id.screensaver_clock);
        screensaverDate = (TextView) findViewById(R.id.screensaver_date);
        screensaverSubtitle = (TextView) findViewById(R.id.screensaver_subtitle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mainScroll != null) {
            mainScroll.setOnScrollChangeListener(new ScrollListener(this));
        }
    }

    private void setupHeader() {
        updateGreetingAndDate();

        if (btnInputs != null) {
            btnInputs.setOnFocusChangeListener(new FocusAnimator(this, 1.06f));
            btnInputs.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_INPUTS));
            if (btnWifi != null) {
                btnInputs.setNextFocusRightId(btnWifi.getId());
            }
            btnInputs.setNextFocusDownId(R.id.favorites_scroll);
        }

        if (btnWifi != null) {
            btnWifi.setOnFocusChangeListener(new FocusAnimator(this, 1.06f));
            btnWifi.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_WIFI));
            if (btnInputs != null) {
                btnWifi.setNextFocusLeftId(btnInputs.getId());
            }
            if (btnSettings != null) {
                btnWifi.setNextFocusRightId(btnSettings.getId());
            }
            btnWifi.setNextFocusDownId(R.id.favorites_scroll);
        }

        if (btnSettings != null) {
            btnSettings.setOnFocusChangeListener(new FocusAnimator(this, 1.06f));
            btnSettings.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_SETTINGS));
            if (btnWifi != null) {
                btnSettings.setNextFocusLeftId(btnWifi.getId());
            }
            btnSettings.setNextFocusDownId(R.id.favorites_scroll);
        }

        if (brandContainer != null) {
            brandContainer.setClickable(true);
            brandContainer.setOnClickListener(new ActionClickListener(this, ActionClickListener.ACTION_SETTINGS));
        }
    }

    public void applyThemeAndAccent() {
        int accentColor = prefs.getAccentColor();
        String theme = prefs.getTheme();
        String wpMode = prefs.getWallpaperMode();
        int dimPercent = prefs.getWallpaperDim();

        // 1. Wallpaper Handling
        if ("system".equalsIgnoreCase(wpMode)) {
            try {
                WallpaperManager wm = WallpaperManager.getInstance(this);
                Drawable wp = wm.getDrawable();
                if (wp != null && wallpaperView != null) {
                    wallpaperView.setImageDrawable(wp);
                    wallpaperView.setVisibility(View.VISIBLE);
                    applyDimOverlay(dimPercent);
                    if (rootLayout != null) rootLayout.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    applyGradientTheme(theme);
                }
            } catch (Throwable t) {
                applyGradientTheme(theme);
            }
        } else if ("custom".equalsIgnoreCase(wpMode)) {
            File customWpFile = getCustomWallpaperFile();
            Bitmap b = loadWallpaperBitmap(customWpFile);
            if (b != null && wallpaperView != null) {
                wallpaperView.setImageBitmap(b);
                wallpaperView.setVisibility(View.VISIBLE);
                applyDimOverlay(dimPercent);
                if (rootLayout != null) rootLayout.setBackgroundColor(Color.TRANSPARENT);
            } else {
                applyGradientTheme(theme);
            }
        } else {
            applyGradientTheme(theme);
        }

        // 2. Brand & Header Accents
        boolean isHalloween = "halloween".equalsIgnoreCase(theme) || "haunted".equalsIgnoreCase(theme);
        if (isHalloween) {
            if (brandLetter != null) {
                brandLetter.setText("🎃 ");
                brandLetter.setTextColor(accentColor);
            }
            if (brandTitle != null) {
                brandTitle.setText("Spooky TV");
            }
            if (tvInputsLabel != null) {
                tvInputsLabel.setText("🦇 Inputs");
            }
            if (tvWifiLabel != null) {
                tvWifiLabel.setText("📶 Wi-Fi");
            }
            if (tvSettingsLabel != null) {
                tvSettingsLabel.setText("👻 Settings");
            }
        } else {
            if (brandLetter != null) {
                brandLetter.setText("D");
                brandLetter.setTextColor(accentColor);
            }
            if (brandTitle != null) {
                brandTitle.setText("Launch");
            }
            if (tvInputsLabel != null) {
                tvInputsLabel.setText(R.string.btn_inputs);
            }
            if (tvWifiLabel != null) {
                tvWifiLabel.setText(R.string.btn_wifi);
            }
            if (tvSettingsLabel != null) {
                tvSettingsLabel.setText(R.string.btn_settings);
            }
        }

        if (labelAllApps != null) {
            labelAllApps.setTextColor(Color.parseColor("#94A3B8"));
        }

        // 3. Header Action Buttons
        if (btnInputs != null) {
            btnInputs.setBackground(createPillSelector(density, accentColor));
        }
        if (btnWifi != null) {
            btnWifi.setBackground(createPillSelector(density, accentColor));
        }
        if (btnSettings != null) {
            btnSettings.setBackground(createPillSelector(density, accentColor));
        }

        if (glassHighlight != null) {
            glassHighlight.setAccentColor(accentColor);
        }
    }

    private void applyDimOverlay(int dimPercent) {
        if (wallpaperDimOverlay == null) return;
        if (dimPercent <= 0) {
            wallpaperDimOverlay.setVisibility(View.GONE);
        } else {
            wallpaperDimOverlay.setVisibility(View.VISIBLE);
            int alpha = (int) (dimPercent * 2.55f);
            wallpaperDimOverlay.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
        }
    }

    private void applyGradientTheme(String theme) {
        if (wallpaperView != null) wallpaperView.setVisibility(View.GONE);
        if (wallpaperDimOverlay != null) wallpaperDimOverlay.setVisibility(View.GONE);
        if (rootLayout != null) {
            if ("halloween".equalsIgnoreCase(theme) || "haunted".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_halloween);
            } else if ("ocean".equalsIgnoreCase(theme) || "liquid_ocean".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_liquid_ocean);
            } else if ("liquid_nebula".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_liquid_nebula);
            } else if ("liquid_aurora".equalsIgnoreCase(theme) || "aurora".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_liquid_aurora);
            } else if ("liquid_sunset".equalsIgnoreCase(theme) || "sunset".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_liquid_sunset);
            } else if ("obsidian".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_obsidian);
            } else if ("cyber".equalsIgnoreCase(theme) || "neon".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_cyber_neon);
            } else if ("violet".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_cosmic_violet);
            } else if ("oled".equalsIgnoreCase(theme)) {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_oled);
            } else {
                rootLayout.setBackgroundResource(R.drawable.bg_theme_liquid_ocean);
            }
        }
    }

    public static Drawable createPillSelector(float density, int accentColor) {
        int radiusPx = (int) (24 * density);
        int strokeNormalPx = (int) (1 * density);
        int strokeFocusedPx = (int) (2.5f * density);

        GradientDrawable normal = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#331E2D44"), Color.parseColor("#44101A29")}
        );
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radiusPx);
        normal.setStroke(strokeNormalPx, Color.parseColor("#2EFFFFFF"));

        GradientDrawable focused = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#66203D66"), Color.parseColor("#80152640")}
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

    public static Drawable createTileSelector(float density, int radiusDp, int accentColor, String transparency) {
        int radiusPx = (int) (radiusDp * density);

        int normalColor;
        if ("solid".equalsIgnoreCase(transparency)) {
            normalColor = Color.parseColor("#141E2E");
        } else if ("ultra".equalsIgnoreCase(transparency)) {
            normalColor = Color.parseColor("#38141E2E");
        } else {
            normalColor = Color.parseColor("#66141E2E"); // Sleek borderless frosted glass
        }

        // 100% Borderless card: no tile border/stroke to completely eliminate double lining!
        GradientDrawable normal = new GradientDrawable();
        normal.setShape(GradientDrawable.RECTANGLE);
        normal.setCornerRadius(radiusPx);
        normal.setColor(normalColor);

        // When focused, subtle illumination from within with NO stroke.
        // GlassHighlightView provides the sole, clean moving diamond rim and glow!
        GradientDrawable focused = new GradientDrawable();
        focused.setShape(GradientDrawable.RECTANGLE);
        focused.setCornerRadius(radiusPx);
        focused.setColor(Color.parseColor("#801E2E44"));

        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focused);
        states.addState(new int[]{android.R.attr.state_pressed}, focused);
        states.addState(new int[]{}, normal);
        return states;
    }

    private void updateGreetingAndDate() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String theme = prefs.getTheme();
        boolean isHalloween = "halloween".equalsIgnoreCase(theme) || "haunted".equalsIgnoreCase(theme);

        if (greetingView != null) {
            if (isHalloween) {
                greetingView.setText("🎃 HAPPY HALLOWEEN");
                greetingView.setTextColor(prefs.getAccentColor());
            } else {
                String greeting;
                if (hour >= 5 && hour < 12) {
                    greeting = "GOOD MORNING";
                } else if (hour >= 12 && hour < 17) {
                    greeting = "GOOD AFTERNOON";
                } else if (hour >= 17 && hour < 22) {
                    greeting = "GOOD EVENING";
                } else {
                    greeting = "LATE NIGHT TV";
                }
                greetingView.setText(greeting);
                greetingView.setTextColor(Color.parseColor("#A0CBD5E1"));
            }
        }

        if (dateView != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            dateView.setText(sdf.format(new Date()));
        }
    }

    public void applyResponsiveMetrics() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        density = metrics.density;
        widthDp = metrics.widthPixels / density;
        heightDp = metrics.heightPixels / density;

        // 1. Dynamic Safe Overscan Margins
        // Auto-scaled proportional to screen dimensions (~4.2% horiz, ~3.6% vert)
        int horizPad = Math.max((int) (28 * density), (int) (widthDp * 0.042f * density));
        int vertPad = Math.max((int) (16 * density), (int) (heightDp * 0.036f * density));
        int bottomPad = Math.max((int) (14 * density), (int) (heightDp * 0.028f * density));
        if (rootLayout != null) {
            rootLayout.setPadding(horizPad, vertPad, horizPad, bottomPad);
        }

        // 2. Proportional Component Scaling based on heightDp
        if (heightDp <= 560f) {
            // Compact 720p TV / streaming stick (e.g. 1280x720 tvdpi)
            responsiveFavoriteTileSize = (int) (76 * density);
            responsiveFavoriteIconSize = (int) (48 * density);
            responsiveGridIconSize = (int) (52 * density);
            responsiveHeroIconFrameSize = (int) (46 * density);
            responsiveHeroTitleSp = 22f;
            responsiveClockSp = 34f;
        } else if (heightDp <= 750f) {
            // Standard 1080p FHD TV (e.g. 1920x1080 xhdpi)
            responsiveFavoriteTileSize = (int) (88 * density);
            responsiveFavoriteIconSize = (int) (56 * density);
            responsiveGridIconSize = (int) (64 * density);
            responsiveHeroIconFrameSize = (int) (54 * density);
            responsiveHeroTitleSp = 26f;
            responsiveClockSp = 40f;
        } else {
            // Large Screen / 4K UHD Native (e.g. 3840x2160 xxxhdpi)
            responsiveFavoriteTileSize = (int) (98 * density);
            responsiveFavoriteIconSize = (int) (64 * density);
            responsiveGridIconSize = (int) (72 * density);
            responsiveHeroIconFrameSize = (int) (62 * density);
            responsiveHeroTitleSp = 30f;
            responsiveClockSp = 46f;
        }

        // Apply dynamically scaled sizes to layout views
        if (clockView != null) {
            clockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, responsiveClockSp);
        }
        if (heroTitleView != null) {
            heroTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, responsiveHeroTitleSp);
        }
        if (heroIconFrame != null) {
            ViewGroup.LayoutParams lp = heroIconFrame.getLayoutParams();
            if (lp != null) {
                lp.width = responsiveHeroIconFrameSize;
                lp.height = responsiveHeroIconFrameSize;
                heroIconFrame.setLayoutParams(lp);
            }
        }
    }

    public void calculateOptimalColumns() {
        int overrideCols = prefs.getColumnCount();
        if (overrideCols >= 3 && overrideCols <= 8) {
            calculatedColumns = overrideCols;
            return;
        }

        // Dynamic formula based on available DP width
        int horizPadDp = Math.max(28, (int) (widthDp * 0.042f));
        float availWidthDp = widthDp - (2 * horizPadDp);
        int targetCardWidthDp = 160;

        int cols = Math.round(availWidthDp / (float) targetCardWidthDp);
        calculatedColumns = Math.max(3, Math.min(8, cols));
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
                if (pkg.equals(getPackageName())) continue;

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

        // 2. Query Standard Launcher apps (sideloaded apps)
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
                if (btnWifi != null) {
                    btnWifi.setNextFocusDownId(favoritesRow.getChildAt(0).getId());
                }
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
                        if (btnWifi != null) {
                            btnWifi.setNextFocusDownId(rowLayout.getChildAt(0).getId());
                        }
                        btnSettings.setNextFocusDownId(rowLayout.getChildAt(0).getId());
                    }
                }

                allAppsContainer.addView(rowLayout);
            }
        }

        // Remote focus lands immediately on first tile
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

        LinearLayout.LayoutParams params;
        int radiusDp;
        View contentBox = tile.findViewById(R.id.tile_content_box);
        if (isFavoriteShelf) {
            int squircleSize = responsiveFavoriteTileSize;
            params = new LinearLayout.LayoutParams(squircleSize, squircleSize);
            int marginHoriz = (int) (6 * density);
            int marginVert = (int) (4 * density);
            params.setMargins(marginHoriz, marginVert, marginHoriz, marginVert);
            if (contentBox != null) {
                contentBox.setPadding((int) (10 * density), (int) (10 * density), (int) (10 * density), (int) (10 * density));
            }
            radiusDp = 22;
        } else {
            params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            int margin = (int) (7 * density);
            params.setMargins(margin, margin, margin, margin);
            if (contentBox != null) {
                contentBox.setPadding((int) (12 * density), (int) (12 * density), (int) (12 * density), (int) (12 * density));
            }
            radiusDp = prefs.getCardRadius();
        }
        tile.setPadding(0, 0, 0, 0);
        tile.setLayoutParams(params);

        tile.setBackground(createTileSelector(density, radiusDp, prefs.getAccentColor(), prefs.getCardTransparency()));

        ImageView iconView = (ImageView) tile.findViewById(R.id.app_icon);
        TextView labelView = (TextView) tile.findViewById(R.id.app_label);
        ImageView starBadge = (ImageView) tile.findViewById(R.id.badge_favorite);

        if (app.getIcon() != null) {
            iconView.setImageDrawable(app.getIcon());
            if (isFavoriteShelf) {
                int iconSize = responsiveFavoriteIconSize;
                iconView.getLayoutParams().width = iconSize;
                iconView.getLayoutParams().height = iconSize;
                iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                int w = app.getIcon().getIntrinsicWidth();
                int h = app.getIcon().getIntrinsicHeight();
                if (w > 0 && h > 0 && (float) w / h >= 1.35f) {
                    iconView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    iconView.getLayoutParams().height = responsiveGridIconSize;
                } else {
                    iconView.getLayoutParams().width = responsiveGridIconSize;
                    iconView.getLayoutParams().height = responsiveGridIconSize;
                }
            }
        }

        if (isFavoriteShelf) {
            labelView.setVisibility(View.GONE);
            starBadge.setVisibility(View.GONE);
            tile.setNextFocusUpId(R.id.btn_settings);
        } else {
            if (prefs.isShowLabels()) {
                labelView.setVisibility(View.VISIBLE);
                labelView.setText(app.getLabel());
            } else {
                labelView.setVisibility(View.GONE);
            }

            if (app.isFavorite()) {
                starBadge.setVisibility(View.VISIBLE);
            } else {
                starBadge.setVisibility(View.GONE);
            }
        }

        tile.setOnFocusChangeListener(new TileFocusListener(this, app, labelView, isFavoriteShelf ? 22 : radiusDp));
        tile.setOnKeyListener(new TileKeyListener());
        tile.setOnClickListener(new TileClickListener(this, app));
        tile.setOnLongClickListener(new TileLongClickListener(this, app));

        return tile;
    }

    public void onTileFocused(final View tileView, final AppItem app, int radiusDp) {
        currentlyFocusedTile = tileView;

        // 1. Update Cinematic Hero Showcase
        updateHeroShowcase(app);

        // 2. Smoothly Glide Floating Glass Highlight over focused item
        glideGlassTo(tileView, true);
    }

    public void glideGlassTo(final View targetView, final boolean animate) {
        if (targetView == null || appRoot == null || glassHighlight == null) return;
        targetView.post(new GlideRunnable(this, targetView, animate));
    }

    public void executeGlide(View targetView, boolean animate) {
        if (!targetView.isFocused()) return;

        int[] rootCoords = new int[2];
        appRoot.getLocationOnScreen(rootCoords);

        int[] targetCoords = new int[2];
        targetView.getLocationOnScreen(targetCoords);

        float pad = glassHighlight.getGlowInset();
        float scale = 1.08f;
        float w = targetView.getWidth() * scale + 2 * pad;
        float h = targetView.getHeight() * scale + 2 * pad;

        float targetX = (targetCoords[0] - rootCoords[0]) + (targetView.getWidth() / 2f) - (w / 2f);
        float targetY = (targetCoords[1] - rootCoords[1]) + (targetView.getHeight() / 2f) - (h / 2f);
        float radiusPx = 20f * density * scale;

        glassHighlight.glideTo(targetX, targetY, (int) w, (int) h, radiusPx, animate);
    }

    private void updateHeroShowcase(final AppItem app) {
        if (heroContainer == null || app == null) return;

        // Set Icon
        if (heroIconView != null) {
            heroIconView.setImageDrawable(app.getIcon());
        }

        // Set Badge
        if (heroBadgePill != null) {
            if (app.isFavorite()) {
                heroBadgePill.setText("✦ FAVORITE");
                heroBadgePill.setVisibility(View.VISIBLE);
            } else if (app.isLeanback()) {
                heroBadgePill.setText("TV APP");
                heroBadgePill.setVisibility(View.VISIBLE);
            } else {
                heroBadgePill.setVisibility(View.GONE);
            }
        }

        // Extract Dominant Accent Color
        int ambientColor = HeroArtHelper.extractDominantColor(app.getIcon(), prefs.getAccentColor());

        if (glassHighlight != null) {
            glassHighlight.setAccentColor(ambientColor);
        }

        if (heroBadgePill != null) {
            heroBadgePill.setTextColor(ambientColor);
        }

        // Animate Hero Title smoothly (slide + crossfade)
        if (heroTitleView != null) {
            final String newLabel = app.getLabel();
            if (!newLabel.contentEquals(heroTitleView.getText())) {
                heroTitleView.animate().alpha(0f).translationX(-10f * density).setDuration(80)
                    .withEndAction(new HeroTitleUpdateRunnable(heroTitleView, newLabel, density))
                    .start();
            }
        }

        // Cinematic Ambient Backdrop Update
        if (heroArtView != null) {
            Bitmap backdrop = HeroArtHelper.createCinematicBackdrop(app.getIcon());
            if (backdrop != null) {
                heroArtView.setImageBitmap(backdrop);
                heroArtView.animate().alpha(0.42f).setDuration(240).start();
            }
        }

        if (ambientGlowView != null) {
            int glowAlpha = 0x22;
            int subtleGlow = (glowAlpha << 24) | (ambientColor & 0x00FFFFFF);
            ambientGlowView.setBackgroundColor(subtleGlow);
            ambientGlowView.animate().alpha(1.0f).setDuration(300).start();
        }
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

    public void launchWifiSettings() {
        Intent[] wifiIntents = new Intent[]{
                new Intent(Settings.ACTION_WIFI_SETTINGS),
                new Intent("android.settings.WIFI_SETTINGS"),
                new Intent(Settings.ACTION_WIRELESS_SETTINGS),
                new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
                new Intent(Settings.ACTION_SETTINGS)
        };

        for (Intent intent : wifiIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Exception ignored) {
            }
        }
        Toast.makeText(this, "Wi-Fi settings not directly accessible", Toast.LENGTH_SHORT).show();
    }

    public void openDefaultHomeSettings() {
        Intent[] homeIntents = new Intent[]{
                new Intent(Settings.ACTION_HOME_SETTINGS),
                new Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS"),
                new Intent(Settings.ACTION_SETTINGS)
        };

        for (Intent intent : homeIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Exception ignored) {
            }
        }
        Toast.makeText(this, "Home settings not directly accessible on this TV", Toast.LENGTH_SHORT).show();
    }

    public void showSettingsMenu() {
        if (settingsDialog != null) {
            settingsDialog.showMainMenu();
        }
    }

    // --- Custom Wallpaper Support ---

    public void pickCustomWallpaper() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_PICK_WALLPAPER);
        } catch (Exception e) {
            Toast.makeText(this, "No image picker app. Copy image to /sdcard/wallpaper.jpg", Toast.LENGTH_LONG).show();
        }
    }

    public void autoDetectWallpaper() {
        File found = scanForWallpaperFile();
        if (found != null && found.exists()) {
            copyFileToInternalWallpaper(found);
            prefs.setWallpaperMode("custom");
            applyThemeAndAccent();
            Toast.makeText(this, "Wallpaper loaded: " + found.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No wallpaper.jpg found in /sdcard or /Download", Toast.LENGTH_LONG).show();
        }
    }

    public void showWallpaperInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("How to Add Custom Wallpapers");
        builder.setMessage(
                "You can use any personal photo or high-res artwork:\n\n" +
                "1. USB Flash Drive:\n" +
                "Put your picture named 'wallpaper.jpg' on a USB drive and plug it in, or copy it to /sdcard/wallpaper.jpg.\n\n" +
                "2. Push from Computer via ADB:\n" +
                "adb push my_photo.jpg /sdcard/wallpaper.jpg\n\n" +
                "3. In DLaunch Settings, click 'Auto-Detect /sdcard/wallpaper.jpg'."
        );
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public File getCustomWallpaperFile() {
        File internal = new File(getFilesDir(), "custom_wallpaper.jpg");
        if (internal.exists() && internal.length() > 0) return internal;

        File ext = new File(getExternalFilesDir(null), "custom_wallpaper.jpg");
        if (ext != null && ext.exists() && ext.length() > 0) return ext;

        return scanForWallpaperFile();
    }

    private File scanForWallpaperFile() {
        File[] candidates = new File[]{
                new File(getExternalFilesDir(null), "custom_wallpaper.jpg"),
                new File(getExternalFilesDir(null), "wallpaper.jpg"),
                new File(android.os.Environment.getExternalStorageDirectory(), "wallpaper.jpg"),
                new File(android.os.Environment.getExternalStorageDirectory(), "wallpaper.png"),
                new File(android.os.Environment.getExternalStorageDirectory(), "wallpaper.jpeg"),
                new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "wallpaper.jpg"),
                new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "wallpaper.png"),
                new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES), "wallpaper.jpg"),
                new File("/sdcard/wallpaper.jpg"),
                new File("/sdcard/wallpaper.png")
        };
        for (File f : candidates) {
            if (f != null && f.exists() && f.length() > 0) {
                return f;
            }
        }
        return null;
    }

    private void copyFileToInternalWallpaper(File src) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new java.io.FileInputStream(src);
            File dest = new File(getFilesDir(), "custom_wallpaper.jpg");
            out = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ignored) {
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (out != null) try { out.close(); } catch (Exception ignored) {}
        }
    }

    private void copyUriToInternalWallpaper(Uri uri) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getContentResolver().openInputStream(uri);
            File dest = new File(getFilesDir(), "custom_wallpaper.jpg");
            out = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ignored) {
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (out != null) try { out.close(); } catch (Exception ignored) {}
        }
    }

    private Bitmap loadWallpaperBitmap(File file) {
        if (file == null || !file.exists()) return null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

            DisplayMetrics dm = getResources().getDisplayMetrics();
            int reqW = dm.widthPixels;
            int reqH = dm.heightPixels;

            int inSampleSize = 1;
            if (opts.outHeight > reqH || opts.outWidth > reqW) {
                final int halfHeight = opts.outHeight / 2;
                final int halfWidth = opts.outWidth / 2;
                while ((halfHeight / inSampleSize) >= reqH && (halfWidth / inSampleSize) >= reqW) {
                    inSampleSize *= 2;
                }
            }

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = inSampleSize;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_WALLPAPER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            copyUriToInternalWallpaper(uri);
            prefs.setWallpaperMode("custom");
            applyThemeAndAccent();
            Toast.makeText(this, "Custom wallpaper applied!", Toast.LENGTH_SHORT).show();
        }
    }

    private long screensaverShownTime = 0;

    public void resetScreensaverTimer() {
        screensaverHandler.removeCallbacks(screensaverRunnable);
        int timeoutMinutes = prefs.getScreensaverTimeout();
        if (timeoutMinutes > 0 && !isScreensaverActive) {
            screensaverHandler.postDelayed(screensaverRunnable, timeoutMinutes * 60 * 1000L);
        }
    }

    public void showScreensaver() {
        if (isScreensaverActive || isFinishing()) return;
        isScreensaverActive = true;
        screensaverShownTime = android.os.SystemClock.uptimeMillis();

        if (screensaverDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
            screensaverDate.setText(sdf.format(new Date()));
        }
        if (screensaverSubtitle != null) {
            boolean isHalloween = "halloween".equalsIgnoreCase(prefs.getTheme()) || "haunted".equalsIgnoreCase(prefs.getTheme());
            if (isHalloween) {
                screensaverSubtitle.setText("🎃 Whispering Shadows • Happy Halloween");
            } else {
                screensaverSubtitle.setText("Calm Ambient Mode");
            }
            screensaverSubtitle.setTextColor(prefs.getAccentColor());
        }

        View header = findViewById(R.id.header_bar);
        if (header != null) {
            header.animate().alpha(0f).setDuration(450).start();
        }
        if (heroContainer != null) {
            heroContainer.animate().alpha(0f).setDuration(450).start();
        }
        if (mainScroll != null) {
            mainScroll.animate().alpha(0f).setDuration(450).start();
        }
        if (glassHighlight != null) {
            glassHighlight.animate().alpha(0f).setDuration(300).start();
        }
        if (screensaverLayout != null) {
            screensaverLayout.setVisibility(View.VISIBLE);
            screensaverLayout.animate().alpha(1f).setDuration(450).start();
        }
    }

    public void hideScreensaver() {
        if (!isScreensaverActive) return;
        isScreensaverActive = false;

        View header = findViewById(R.id.header_bar);
        if (header != null) {
            header.animate().alpha(1f).setDuration(300).start();
        }
        if (heroContainer != null) {
            heroContainer.animate().alpha(1f).setDuration(300).start();
        }
        if (mainScroll != null) {
            mainScroll.animate().alpha(1f).setDuration(300).start();
        }
        if (glassHighlight != null) {
            glassHighlight.animate().alpha(1f).setDuration(300).start();
        }
        if (screensaverLayout != null) {
            screensaverLayout.animate().alpha(0f).setDuration(300).withEndAction(new ScreensaverHideRunnable(screensaverLayout)).start();
        }
        resetScreensaverTimer();
    }

    public void previewScreensaver() {
        showScreensaver();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        resetScreensaverTimer();
        if (isScreensaverActive) {
            if (android.os.SystemClock.uptimeMillis() - screensaverShownTime > 500) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    hideScreensaver();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void launchVoiceSearch() {
        try {
            Intent intent = new Intent(Intent.ACTION_ASSIST);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t1) {
            try {
                Intent intent = new Intent("android.speech.action.WEB_SEARCH");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Throwable t2) {
                try {
                    Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Throwable t3) {
                    Toast.makeText(this, "Voice search is currently unavailable", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onSearchRequested() {
        launchVoiceSearch();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mainScroll != null && mainScroll.getScrollY() > 50) {
                mainScroll.smoothScrollTo(0, 0);
                if (favoritesRow != null && favoritesRow.getChildCount() > 0) {
                    favoritesRow.getChildAt(0).requestFocus();
                }
                return true;
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (mainScroll != null) {
                mainScroll.smoothScrollTo(0, 0);
                if (favoritesRow != null && favoritesRow.getChildCount() > 0) {
                    favoritesRow.getChildAt(0).requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_VOICE_ASSIST || keyCode == KeyEvent.KEYCODE_ASSIST) {
            launchVoiceSearch();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // --- STATIC LISTENERS (Prevents JDK21 synthetic capture bug) ---

    private static class TileFocusListener implements View.OnFocusChangeListener {
        private final DLaunchActivity activity;
        private final AppItem app;
        private final TextView labelView;
        private final int radiusDp;

        TileFocusListener(DLaunchActivity activity, AppItem app, TextView labelView, int radiusDp) {
            this.activity = activity;
            this.app = app;
            this.labelView = labelView;
            this.radiusDp = radiusDp;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (labelView != null) {
                labelView.setSelected(hasFocus);
            }
            if (hasFocus) {
                v.animate().scaleX(1.08f).scaleY(1.08f).setDuration(130).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(16f);
                }
                activity.onTileFocused(v, app, radiusDp);
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(130).start();
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
        public static final int ACTION_WIFI = 3;

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
            } else if (action == ACTION_WIFI) {
                activity.launchWifiSettings();
            }
        }
    }

    public static class FocusAnimator implements View.OnFocusChangeListener {
        private final DLaunchActivity activity;
        private final float scale;

        public FocusAnimator(float scale) {
            this(null, scale);
        }

        public FocusAnimator(DLaunchActivity activity, float scale) {
            this.activity = activity;
            this.scale = scale;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.animate().scaleX(scale).scaleY(scale).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(8f);
                }
                if (activity != null) {
                    activity.glideGlassTo(v, true);
                }
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(0f);
                }
            }
        }
    }

    private static class ScreensaverTimerRunnable implements Runnable {
        private final DLaunchActivity activity;
        ScreensaverTimerRunnable(DLaunchActivity activity) {
            this.activity = activity;
        }
        @Override
        public void run() {
            activity.showScreensaver();
        }
    }

    private static class ScreensaverHideRunnable implements Runnable {
        private final View layout;
        ScreensaverHideRunnable(View layout) {
            this.layout = layout;
        }
        @Override
        public void run() {
            layout.setVisibility(View.GONE);
        }
    }

    private static class ScrollListener implements View.OnScrollChangeListener {
        private final DLaunchActivity activity;
        ScrollListener(DLaunchActivity activity) {
            this.activity = activity;
        }
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if (activity.currentlyFocusedTile != null && activity.currentlyFocusedTile.isFocused()) {
                activity.glideGlassTo(activity.currentlyFocusedTile, false);
            }
        }
    }

    private static class GlideRunnable implements Runnable {
        private final DLaunchActivity activity;
        private final View targetView;
        private final boolean animate;

        GlideRunnable(DLaunchActivity activity, View targetView, boolean animate) {
            this.activity = activity;
            this.targetView = targetView;
            this.animate = animate;
        }

        @Override
        public void run() {
            activity.executeGlide(targetView, animate);
        }
    }

    private static class HeroTitleUpdateRunnable implements Runnable {
        private final TextView titleView;
        private final String newLabel;
        private final float density;

        HeroTitleUpdateRunnable(TextView titleView, String newLabel, float density) {
            this.titleView = titleView;
            this.newLabel = newLabel;
            this.density = density;
        }

        @Override
        public void run() {
            titleView.setText(newLabel);
            titleView.setTranslationX(10f * density);
            titleView.animate().alpha(1f).translationX(0f).setDuration(160).start();
        }
    }
}
