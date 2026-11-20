package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public final class MusicVisualizerView extends View {

    private static final int BARS = 5;
    private static final long CYCLE = 250L;
    private final float[] from = new float[BARS];
    private final float[] to = new float[BARS];
    private final float[] now = new float[BARS];
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean playing;
    private int prominent;
    private long stamp;
    private float barW;
    private float gap;

    public MusicVisualizerView(Context context) {
        this(context, null);
    }

    public MusicVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFFFFFFFF);
        float d = getResources().getDisplayMetrics().density;
        barW = 2.5f * d;
        gap = 1.5f * d;
        roll();
        stamp = SystemClock.uptimeMillis();
    }

    public void setPlaying(boolean playing) {
        if (this.playing == playing) return;
        this.playing = playing;
        setVisibility(playing ? VISIBLE : GONE);
        if (playing) {
            stamp = SystemClock.uptimeMillis();
            invalidate();
        }
    }

    private void roll() {
        prominent = (prominent + 1) % BARS;
        for (int i = 0; i < BARS; i++) {
            from[i] = now[i] == 0 ? 0.2f : now[i];
            to[i] = i == prominent ? 1.4f : 0.3f + random.nextFloat() * 0.4f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!playing) return;
        long nowMs = SystemClock.uptimeMillis();
        float t = Math.min(1f, (nowMs - stamp) / (float) CYCLE);
        if (t >= 1f) {
            System.arraycopy(to, 0, from, 0, BARS);
            roll();
            stamp = nowMs;
            t = 0f;
        }
        float h = getHeight();
        float total = BARS * barW + (BARS - 1) * gap;
        float x = (getWidth() - total) / 2f;
        float radius = getResources().getDisplayMetrics().density;
        for (int i = 0; i < BARS; i++) {
            now[i] = from[i] + (to[i] - from[i]) * t;
            float bh = Math.max(h * 0.2f, Math.min(h, 16f * getResources().getDisplayMetrics().density * now[i]));
            float top = (h - bh) / 2f;
            canvas.drawRoundRect(x, top, x + barW, top + bh, radius, radius, paint);
            x += barW + gap;
        }
        postInvalidateOnAnimation();
    }
}
