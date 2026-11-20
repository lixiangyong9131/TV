package com.fongmi.android.tv.utils;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.fongmi.android.tv.bean.Vod;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VodScores {

    private static final Cache<String, String> CACHE = CacheBuilder.newBuilder().maximumSize(800).build();
    private static final Pattern RATE = Pattern.compile("(?:豆瓣|评分)\\s*[:：]?\\s*(\\d(?:\\.\\d)?)|★\\s*(\\d(?:\\.\\d)?)|(\\d(?:\\.\\d)?)\\s*分|(?<![\\d.])(\\d\\.\\d)(?![\\d.])");
    private static final Pattern ZERO = Pattern.compile("(?<![\\d.])0(?:\\.0+)?(?![\\d.])\\s*分?");

    private VodScores() {
    }

    public static void bind(TextView view, Vod item) {
        apply(view, badge(item));
    }

    public static void bindInline(TextView view, Vod item) {
        if (view == null) return;
        String score = badge(item);
        if (score.isEmpty()) {
            view.setText("");
            view.setVisibility(View.GONE);
            return;
        }
        String text = "★ " + score;
        if (!text.contentEquals(view.getText())) view.setText(text);
        if (view.getVisibility() != View.VISIBLE) view.setVisibility(View.VISIBLE);
    }

    public static void bind(TextView view, String name) {
        apply(view, cached(name));
    }

    public static void bind(TextView view, String name, String remarks) {
        String score = cached(name);
        if (score.isEmpty()) score = parse(remarks);
        if (!score.isEmpty()) remember(name, score);
        apply(view, score);
    }

    public static void bindRemark(TextView view, String remarks) {
        if (view == null) return;
        String text = caption(remarks);
        if (!text.contentEquals(view.getText())) view.setText(text);
        int vis = text.isEmpty() ? View.GONE : View.VISIBLE;
        if (view.getVisibility() != vis) view.setVisibility(vis);
    }

    public static String caption(String remarks) {
        if (TextUtils.isEmpty(remarks)) return "";
        String trimmed = remarks.trim();
        Double whole = number(trimmed);
        if (whole != null && whole >= 0 && whole <= 10) return "";
        if (trimmed.indexOf('分') < 0 && trimmed.indexOf('★') < 0 && trimmed.indexOf('.') < 0 && trimmed.indexOf('豆') < 0 && trimmed.indexOf('评') < 0) return trimmed;
        String stripped = RATE.matcher(trimmed).replaceAll(" ");
        stripped = ZERO.matcher(stripped).replaceAll(" ").replaceAll("[\\s/|,·]+", " ").trim();
        if (stripped.equals(trimmed) && whole == null) return trimmed;
        Double leftover = number(stripped);
        if (stripped.isEmpty() || (leftover != null && leftover >= 0 && leftover <= 10)) return "";
        return stripped;
    }

    public static String badge(Vod item) {
        if (item == null) return "";
        String score = sanitize(item.getDoubanScore());
        if (score.isEmpty()) score = sanitize(item.getVodScore());
        if (score.isEmpty()) score = parse(item.getRemarks());
        if (score.isEmpty()) score = cached(item.getName());
        if (!score.isEmpty()) remember(item.getName(), score);
        return score;
    }

    public static String sanitize(String raw) {
        if (TextUtils.isEmpty(raw)) return "";
        String text = raw.replace("分", "").trim();
        try {
            double value = Double.parseDouble(text);
            if (value < 1 || value > 10) return "";
            return value == (int) value ? String.valueOf((int) value) : String.format(Locale.US, "%.1f", value);
        } catch (NumberFormatException ignored) {
            return "";
        }
    }

    private static void apply(TextView view, String score) {
        if (view == null) return;
        if (!score.contentEquals(view.getText())) view.setText(score);
        int vis = score.isEmpty() ? View.GONE : View.VISIBLE;
        if (view.getVisibility() != vis) view.setVisibility(vis);
    }

    private static Double number(String text) {
        if (TextUtils.isEmpty(text)) return null;
        try {
            return Double.parseDouble(text.replace("分", "").trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String parse(String remarks) {
        if (TextUtils.isEmpty(remarks)) return "";
        Matcher matcher = RATE.matcher(remarks);
        if (!matcher.find()) return "";
        for (int i = 1; i <= matcher.groupCount(); i++) {
            String score = sanitize(matcher.group(i));
            if (!score.isEmpty()) return score;
        }
        return "";
    }

    private static String cached(String name) {
        if (TextUtils.isEmpty(name)) return "";
        String score = CACHE.getIfPresent(name.trim());
        return score == null ? "" : score;
    }

    private static void remember(String name, String score) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(score)) return;
        CACHE.put(name.trim(), score);
    }
}
