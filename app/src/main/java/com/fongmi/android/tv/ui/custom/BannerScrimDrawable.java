package com.fongmi.android.tv.ui.custom;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class BannerScrimDrawable extends Drawable {

    private final Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private LinearGradient bottomGradient;

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        float h = Math.max(bounds.height(), 1);
        float top = h * 0.55f;
        bottomGradient = new LinearGradient(0, top, 0, h, new int[]{0x00000000, 0x66000000, 0xCC000000}, new float[]{0f, 0.4f, 0.8f}, Shader.TileMode.CLAMP);
        bottomPaint.setShader(bottomGradient);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (bottomGradient == null) return;
        canvas.drawRect(getBounds(), bottomPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        bottomPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        bottomPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
