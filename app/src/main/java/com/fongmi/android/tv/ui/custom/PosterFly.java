package com.fongmi.android.tv.ui.custom;

import android.app.Activity;
import android.graphics.Outline;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;

public final class PosterFly {

    private PosterFly() {
    }

    public static void open(Activity activity, View source, String name, String pic, Runnable then) {
        if (activity == null || activity.isFinishing()) {
            if (then != null) then.run();
            return;
        }
        if (source == null || !source.isAttachedToWindow()) {
            if (then != null) then.run();
            return;
        }
        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) {
            if (then != null) then.run();
            return;
        }
        Rect start = new Rect();
        source.getGlobalVisibleRect(start);
        if (start.width() < 8 || start.height() < 8) {
            if (then != null) then.run();
            return;
        }
        int[] origin = new int[2];
        root.getLocationOnScreen(origin);
        float x = start.left - origin[0];
        float y = start.top - origin[1];
        int w = start.width();
        int h = start.height();
        int screen = root.getWidth() > 0 ? root.getWidth() : ResUtil.getScreenWidth();
        float scale = Math.max(1.2f, screen * 0.45f / Math.max(w, 1));
        float endW = w * scale;
        float endH = h * scale;
        float endX = (root.getWidth() - endW) / 2f;
        float endY = (root.getHeight() - endH) / 2f;
        ImageView fly = new ImageView(activity);
        fly.setScaleType(ImageView.ScaleType.CENTER_CROP);
        float r0 = ResUtil.dp2px(8);
        fly.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), r0);
            }
        });
        fly.setClipToOutline(true);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
        fly.setX(x);
        fly.setY(y);
        root.addView(fly, lp);
        ImgUtil.load(name, pic, fly);
        source.setAlpha(0f);
        fly.animate().x(endX).y(endY).scaleX(scale).scaleY(scale).setDuration(700).setInterpolator(new PathInterpolator(0.2f, 0.9f, 0.25f, 1f)).withEndAction(() -> {
            if (then != null) then.run();
            fly.animate().alpha(0f).setDuration(180).withEndAction(() -> {
                root.removeView(fly);
                source.setAlpha(1f);
            }).start();
        }).start();
    }
}
