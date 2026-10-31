package com.dewzone.dlaunch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

/**
 * Hardware-cached floating frosted glass focus highlight.
 * Features:
 * - 12 concentric ambient glow rings matching the focused app's accent color
 * - Translucent crystalline glass body with top 45% specular sheen
 * - Specular rim stroke lifted towards white for optical refraction
 * - Smooth deceleration glide across TV remote navigation
 */
public class GlassHighlightView extends View {

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint specularPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final float density;
    private float cornerRadius;
    private int glowRgb = 0x00E5FF;
    private int glowAlpha = 0x55;

    private static final DecelerateInterpolator GLIDE_INTERPOLATOR = new DecelerateInterpolator(1.6f);

    public GlassHighlightView(Context context) {
        this(context, null);
    }

    public GlassHighlightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GlassHighlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        cornerRadius = 20f * density;

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(1.75f * density);
        rimPaint.setColor(0xCCFFFFFF);

        glowPaint.setStyle(Paint.Style.STROKE);

        // Hardware layer caching: moving this view via translationX/Y costs ZERO canvas redraws on MTK GPU
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setAccentColor(int color) {
        if (color == 0) {
            glowRgb = 0x00E5FF;
            glowAlpha = 0x55;
            rimPaint.setColor(0xCCFFFFFF);
        } else {
            glowRgb = color & 0x00FFFFFF;
            glowAlpha = 0x75;

            // Lift rim stroke toward pure white (2 parts white + 1 part accent) for a diamond glass edge
            int r = (((glowRgb >> 16) & 0xFF) + 255 * 2) / 3;
            int g = (((glowRgb >> 8) & 0xFF) + 255 * 2) / 3;
            int b = ((glowRgb & 0xFF) + 255 * 2) / 3;
            rimPaint.setColor(0xE6000000 | (r << 16) | (g << 8) | b);
        }
        invalidate();
    }

    public void setCornerRadius(float radiusPx) {
        if (this.cornerRadius != radiusPx) {
            this.cornerRadius = radiusPx;
            invalidate();
        }
    }

    public float getGlowInset() {
        return 7f * density;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            fillPaint.setShader(new LinearGradient(
                    0, 0, 0, h,
                    0x33FFFFFF, 0x0DFFFFFF,
                    Shader.TileMode.CLAMP
            ));
            specularPaint.setShader(new LinearGradient(
                    0, 0, 0, h * 0.45f,
                    0x66FFFFFF, 0x00FFFFFF,
                    Shader.TileMode.CLAMP
            ));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = getGlowInset();
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // 1. Draw Multi-Ring Ambient Glow
        int rings = 12;
        float step = inset / rings;
        glowPaint.setStrokeWidth(step + 0.5f);
        for (int i = 0; i < rings; i++) {
            float o = step * (i + 0.5f);
            float t = 1.0f - ((float) i / rings);
            int ringAlpha = Math.round(glowAlpha * t * t); // Quadratic falloff for velvety soft aura
            glowPaint.setColor((ringAlpha << 24) | glowRgb);
            canvas.drawRoundRect(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o,
                    cornerRadius + o, cornerRadius + o, glowPaint);
        }

        // 2. Draw Frosted Glass Fill
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint);

        // 3. Draw Specular Top Glare (Top 45%)
        canvas.save();
        canvas.clipRect(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.45f);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, specularPaint);
        canvas.restore();

        // 4. Draw Crystalline Rim Outline
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, rimPaint);
    }

    /**
     * Glides the glass highlight to the target screen coordinates with physics deceleration.
     */
    public void glideTo(float targetX, float targetY, int targetW, int targetH, float targetRadius, boolean animate) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        boolean sizeChanged = false;
        if (lp.width != targetW || lp.height != targetH) {
            lp.width = targetW;
            lp.height = targetH;
            setLayoutParams(lp);
            sizeChanged = true;
        }

        setCornerRadius(targetRadius);

        if (!animate || getVisibility() != View.VISIBLE || getAlpha() < 0.1f) {
            animate().cancel();
            setX(targetX);
            setY(targetY);
            setAlpha(1.0f);
            setVisibility(View.VISIBLE);
        } else {
            animate().cancel();
            animate()
                    .x(targetX)
                    .y(targetY)
                    .alpha(1.0f)
                    .setDuration(160)
                    .setInterpolator(GLIDE_INTERPOLATOR)
                    .start();
        }
    }
}
