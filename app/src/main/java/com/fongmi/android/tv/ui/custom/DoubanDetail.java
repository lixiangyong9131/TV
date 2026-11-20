package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.setting.Setting;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import org.json.JSONArray;
import org.json.JSONObject;

public final class DoubanDetail {

    private DoubanDetail() {
    }

    public static void tintPlaceholder(View skeleton, View poster) {
        int color = Setting.getDynamicColor();
        if (color == 0) return;
        if (skeleton instanceof ViewGroup group) tintBones(group, color);
        if (poster != null) poster.setBackground(bone(poster.getContext(), color));
    }

    private static void tintBones(ViewGroup group, int color) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup nested) tintBones(nested, color);
            else if (child.getBackground() != null) child.setBackground(bone(child.getContext(), color));
        }
    }

    private static GradientDrawable bone(Context context, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, 8));
        return drawable;
    }

    public static String stars(String score) {
        if (TextUtils.isEmpty(score)) return "";
        try {
            int full = (int) Math.round(Double.parseDouble(score) / 2.0);
            full = Math.max(0, Math.min(5, full));
            return "★★★★★".substring(0, full) + "☆☆☆☆☆".substring(full);
        } catch (NumberFormatException ignored) {
            return "";
        }
    }

    public static void renderCast(Context context, LinearLayout row, Vod item) {
        row.removeAllViews();
        String raw = item.getDoubanCast();
        if (!raw.isEmpty()) {
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject one = array.getJSONObject(i);
                    String name = one.optString("name");
                    String avatar = one.optString("avatar");
                    if (name.isEmpty() || avatar.isEmpty() || !avatar.startsWith("http")) continue;
                    row.addView(chip(context, row, name, one.optString("role"), avatar));
                    if (row.getChildCount() >= 12) break;
                }
            } catch (Exception ignored) {
            }
        }
        syncCast(row);
    }

    private static void syncCast(LinearLayout row) {
        if (!(row.getParent() instanceof View scroll) || !(scroll.getParent() instanceof ViewGroup box)) return;
        int visible = row.getChildCount() == 0 ? View.GONE : View.VISIBLE;
        scroll.setVisibility(visible);
        for (int i = 0; i < box.getChildCount(); i++) {
            View child = box.getChildAt(i);
            if (child.getId() == R.id.castTitle) child.setVisibility(visible);
        }
    }

    private static View chip(Context context, LinearLayout row, String name, String role, String avatar) {
        int pad = dp(context, 10);
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(dp(context, 76), LinearLayout.LayoutParams.WRAP_CONTENT);
        columnParams.setMarginEnd(pad);
        column.setLayoutParams(columnParams);
        ShapeableImageView image = new ShapeableImageView(context);
        image.setLayoutParams(new LinearLayout.LayoutParams(dp(context, 72), dp(context, 96)));
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackgroundResource(R.drawable.bg_douban_bone);
        image.setShapeAppearanceModel(new ShapeAppearanceModel.Builder().setAllCorners(CornerFamily.ROUNDED, dp(context, 8)).build());
        Glide.with(image).load(avatar).centerCrop().listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (column.getParent() instanceof ViewGroup parent) parent.removeView(column);
                syncCast(row);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(image);
        TextView nameView = label(context, name, 12, 0xFFFFFFFF);
        TextView roleView = label(context, role, 11, 0xFFB3B3B3);
        column.addView(image);
        column.addView(nameView);
        if (!role.isEmpty()) column.addView(roleView);
        return column;
    }

    private static TextView label(Context context, String text, int sp, int color) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(sp);
        view.setMaxLines(1);
        view.setEllipsize(android.text.TextUtils.TruncateAt.END);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(context, 4);
        view.setLayoutParams(params);
        return view;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
