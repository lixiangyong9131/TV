package com.fongmi.android.tv.utils;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.impl.CustomTarget;
import com.fongmi.android.tv.server.Server;
import com.github.catvod.utils.Crypto;
import com.github.catvod.utils.Json;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import jahirfiquitiva.libs.textdrawable.TextDrawable;

public class ImgUtil {

    private static final int MAX_CACHE_SIZE = 16 * 1024 * 1024;
    private static final int MAX_DATA_URI_LENGTH = 8 * 1024 * 1024;
    private static final Pattern IMAGE_MIME = Pattern.compile("image/[a-z0-9.+-]+");
    private static final Map<String, Boolean> failed = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            return size() > 256;
        }
    });
    private static final Cache<String, Image> CACHE = CacheBuilder.newBuilder().maximumWeight(MAX_CACHE_SIZE).weigher((String key, Image value) -> value.data().length).build();

    public static void logo(ImageView view) {
        try {
            Glide.with(view).load(UrlUtil.convert(VodConfig.get().getConfig().getLogo())).circleCrop().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).error(R.drawable.ic_logo).into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(String url, CustomTarget<Bitmap> target) {
        try {
            Glide.with(App.get()).asBitmap().load(getUrl(url)).override(ResUtil.dp2px(96), ResUtil.dp2px(96)).error(R.drawable.artwork).into(target);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(Context context, String url, CustomTarget<Drawable> target) {
        try {
            Glide.with(context).load(getUrl(url)).override(ResUtil.getScreenWidth(), ResUtil.getScreenHeight()).error(R.drawable.artwork).into(target);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String large(String url) {
        if (TextUtils.isEmpty(url)) return "";
        String out = url;
        out = out.replace("/s_ratio_poster/", "/l_ratio_poster/");
        out = out.replace("/m_ratio_poster/", "/l_ratio_poster/");
        out = out.replace("/photo/s/", "/photo/l/");
        out = out.replace("/photo/m/", "/photo/l/");
        out = out.replace("/movie_poster_cover/ipst/", "/movie_poster_cover/lpst/");
        out = out.replace("/t/p/w92/", "/t/p/original/");
        out = out.replace("/t/p/w154/", "/t/p/original/");
        out = out.replace("/t/p/w185/", "/t/p/original/");
        out = out.replace("/t/p/w300/", "/t/p/original/");
        out = out.replace("/t/p/w342/", "/t/p/original/");
        out = out.replace("/t/p/w500/", "/t/p/original/");
        out = out.replace("/t/p/w780/", "/t/p/original/");
        out = out.replaceAll("imageView2/\\d/w/\\d+", "imageView2/2/w/1280");
        out = out.replaceAll("image/resize,w_\\d+", "image/resize,w_1280");
        return out;
    }

    public static void loadBanner(String text, String url, ImageView view) {
        view.setScaleType(CENTER_CROP);
        String src = large(url);
        if (TextUtils.isEmpty(src) || failed.containsKey(src)) {
            Glide.with(view).clear(view);
            view.setImageDrawable(getTextDrawable(text, true));
            return;
        }
        try {
            int w = Math.min(Math.max(ResUtil.getScreenWidth(), 720), 1600);
            int h = Math.min(Math.max(view.getMeasuredHeight(), ResUtil.dp2px(224)), 900);
            Glide.with(view).load(getUrl(src)).listener(getListener(text, src, "", view, true)).override(w, h).centerCrop().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void load(String text, String url, ImageView view) {
        load(text, url, "", view, true);
    }

    public static void load(String text, String url, String fallback, ImageView view) {
        load(text, url, fallback, view, true);
    }

    public static void load(String text, String url, ImageView view, boolean vod) {
        load(text, url, "", view, vod);
    }

    public static void load(String text, String url, String fallback, ImageView view, boolean vod) {
        view.setScaleType(vod ? CENTER_CROP : FIT_CENTER);
        if (!vod && TextUtils.isEmpty(url) && TextUtils.isEmpty(fallback)) {
            Glide.with(view).clear(view);
            view.setImageDrawable(null);
            view.setVisibility(View.GONE);
            return;
        }
        if (!vod) view.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(url) || failed.containsKey(url)) {
            if (!TextUtils.isEmpty(fallback) && !failed.containsKey(fallback)) {
                loadImage(text, fallback, "", view, vod);
                return;
            }
            Glide.with(view).clear(view);
            view.setImageDrawable(getTextDrawable(text, vod));
            return;
        }
        loadImage(text, url, fallback, view, vod);
    }

    private static void loadImage(String text, String url, String fallback, ImageView view, boolean vod) {
        try {
            int[] size = thumbSize(view);
            RequestBuilder<Drawable> builder = Glide.with(view).load(getUrl(url)).listener(getListener(text, url, fallback, view, vod)).override(size[0], size[1]);
            if (vod) builder.centerCrop().into(view);
            else builder.fitCenter().into(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static int[] thumbSize(ImageView view) {
        int w = view.getMeasuredWidth();
        int h = view.getMeasuredHeight();
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (h <= 1 && lp != null && lp.height > 0) h = lp.height;
        if (w <= 1 && lp != null && lp.width > 0) w = lp.width;
        if (w <= 1 && view.getParent() instanceof View parent) {
            ViewGroup.LayoutParams pl = parent.getLayoutParams();
            if (pl != null && pl.width > 0) w = pl.width;
        }
        if (w <= 1) w = ResUtil.dp2px(180);
        if (h <= 1) h = ResUtil.dp2px(240);
        return new int[]{w, h};
    }

    public static Object getUrl(String url) {
        String param = null;
        url = UrlUtil.convert(url);
        if (url.startsWith("data:")) return url;
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        if (url.contains("@Headers=")) addHeader(builder, param = url.split("@Headers=")[1].split("@")[0]);
        if (url.contains("@Cookie=")) builder.addHeader(HttpHeaders.COOKIE, param = url.split("@Cookie=")[1].split("@")[0]);
        if (url.contains("@Referer=")) builder.addHeader(HttpHeaders.REFERER, param = url.split("@Referer=")[1].split("@")[0]);
        if (url.contains("@User-Agent=")) builder.addHeader(HttpHeaders.USER_AGENT, param = url.split("@User-Agent=")[1].split("@")[0]);
        url = param == null ? url : url.split("@")[0];
        return TextUtils.isEmpty(url) ? null : new GlideUrl(url, builder.build());
    }

    public static String cache(String url) {
        if (!isData(url)) return url;
        if (url.length() > MAX_DATA_URI_LENGTH) return "";
        String key = Crypto.md5(url);
        if (TextUtils.isEmpty(key)) return "";
        if (CACHE.asMap().computeIfAbsent(key, ignored -> decode(url)) == null) return "";
        String address = Server.get().getAddress("/image/" + key);
        failed.remove(address);
        return address;
    }

    private static boolean isData(String url) {
        return !TextUtils.isEmpty(url) && url.regionMatches(true, 0, "data:", 0, 5);
    }

    private static Image decode(String url) {
        try {
            int comma = url.indexOf(',');
            if (comma == -1) return null;
            String metadata = url.substring(5, comma).toLowerCase(Locale.ROOT);
            String mime = metadata.split(";", 2)[0];
            if (!IMAGE_MIME.matcher(mime).matches() || !metadata.endsWith(";base64")) return null;
            byte[] data = Base64.decode(url.substring(comma + 1), Base64.DEFAULT);
            return data.length == 0 ? null : new Image(mime, data);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Image getImage(String key) {
        return CACHE.getIfPresent(key);
    }

    public record Image(String mime, byte[] data) {
    }

    private static void addHeader(LazyHeaders.Builder builder, String header) {
        Map<String, String> map = Json.toMap(Json.parse(header));
        for (Map.Entry<String, String> entry : map.entrySet()) builder.addHeader(UrlUtil.fixHeader(entry.getKey()), entry.getValue());
    }

    private static Drawable getTextDrawable(String text, boolean vod) {
        TextDrawable.Builder builder = new TextDrawable.Builder();
        text = TextUtils.isEmpty(text) ? "！" : text.substring(0, 1);
        if (vod) builder.buildRect(text, ColorGenerator.get400(text));
        return builder.buildRoundRect(text, ColorGenerator.get400(text), ResUtil.dp2px(4));
    }

    private static RequestListener<Drawable> getListener(String text, String url, String fallback, ImageView view, boolean vod) {
        return new RequestListener<>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                failed.put(url, Boolean.TRUE);
                if (!TextUtils.isEmpty(fallback) && !failed.containsKey(fallback)) {
                    load(text, fallback, "", view, vod);
                    return true;
                }
                view.setImageDrawable(getTextDrawable(text, vod));
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };
    }
}
