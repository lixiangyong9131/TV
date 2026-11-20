package com.fongmi.android.tv.ui.custom;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class ShimmerDrawable extends Drawable implements Runnable {

    private static final long PERIOD = 1400L;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix matrix = new Matrix();
    private final RectF rect = new RectF();
    private final float radius;
    private final long delay;
    private LinearGradient gradient;
    private boolean running;
    private long born;

    public ShimmerDrawable() {
        this(0L);
    }

    public ShimmerDrawable(long delay) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        radius = 6f * metrics.density;
        this.delay = delay;
        this.born = SystemClock.uptimeMillis();
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        float w = Math.max(bounds.width(), 1);
        gradient = new LinearGradient(0, 0, w, 0, new int[]{0xFF1C1C20, 0xFF1C1C20, 0xFF33333A, 0xFF1C1C20, 0xFF1C1C20}, new float[]{0f, 0.35f, 0.5f, 0.65f, 1f}, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (gradient == null) return;
        float w = Math.max(getBounds().width(), 1);
        long elapsed = Math.max(0L, SystemClock.uptimeMillis() - born - delay);
        float t = (elapsed % PERIOD) / (float) PERIOD;
        matrix.setTranslate((t * 2f - 1.5f) * w, 0);
        gradient.setLocalMatrix(matrix);
        rect.set(getBounds());
        canvas.drawRoundRect(rect, radius, radius, paint);
    }

    @Override
    public void run() {
        invalidateSelf();
        if (running) scheduleSelf(this, SystemClock.uptimeMillis() + 16);
    }

    public void start() {
        if (running) return;
        running = true;
        born = SystemClock.uptimeMillis();
        scheduleSelf(this, SystemClock.uptimeMillis());
    }

    public void stop() {
        running = false;
        unscheduleSelf(this);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
