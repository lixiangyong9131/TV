package com.fongmi.android.tv.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ViewProgressBinding;
import com.fongmi.android.tv.exception.ExtractException;
import com.fongmi.android.tv.exception.SpiderLoadException;
import com.github.catvod.utils.RuntimeModule;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Notify {

    public static final String DEFAULT = "default";
    public static final int ID = 9527;
    private AlertDialog mDialog;
    private View toastView;
    private Runnable hideToast;
    private String pending;

    private static class Loader {
        static volatile Notify INSTANCE = new Notify();
    }

    private static Notify get() {
        return Loader.INSTANCE;
    }

    public static void createChannel() {
        NotificationManagerCompat notifyMgr = NotificationManagerCompat.from(App.get());
        notifyMgr.createNotificationChannel(new NotificationChannelCompat.Builder(DEFAULT, NotificationManagerCompat.IMPORTANCE_LOW).setName("TV").build());
    }

    public static String getError(int resId, Throwable e) {
        String message = getError(e);
        if (message != null) return message;
        if (TextUtils.isEmpty(e.getMessage())) return ResUtil.getString(resId);
        return ResUtil.getString(resId) + "\n" + e.getMessage();
    }

    public static String getError(Throwable e) {
        RuntimeModule.MissingException missing = RuntimeModule.MissingException.find(e);
        if (missing != null) {
            String title = missing.module == RuntimeModule.THUNDER ? ResUtil.getString(R.string.module_thunder) : missing.module.title;
            return ResUtil.getString(R.string.error_module_missing, title);
        }
        for (Throwable current = e; current != null; current = current.getCause()) if (current instanceof ExtractException) return current.getMessage();
        for (Throwable current = e; current != null; current = current.getCause()) if (current instanceof SpiderLoadException) return ResUtil.getString(R.string.error_spider_load);
        return null;
    }

    public static void show(Notification notification) {
        if (ContextCompat.checkSelfPermission(App.get(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(App.get()).notify(ID, notification);
    }

    public static void attach(Activity activity) {
    }

    public static void detach(Activity activity) {
    }

    public static void onForeground() {
        App.post(() -> get().showPending());
    }

    public static void show(int resId) {
        if (resId != 0) show(ResUtil.getString(resId));
    }

    public static void show(String text) {
        show(text, false);
    }

    public static void showAlways(String text) {
        show(text, true);
    }

    private static void show(String text, boolean force) {
        if (!force && com.fongmi.android.tv.playback.vod.PickSilence.toastMuted()) return;
        if (!TextUtils.isEmpty(text)) get().makeText(text);
    }

    public static void progress(Context context) {
        dismiss();
        get().create(context);
    }

    public static void dismiss() {
        try {
            if (get().mDialog != null) get().mDialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    private void create(Context context) {
        ViewProgressBinding binding = ViewProgressBinding.inflate(LayoutInflater.from(context));
        mDialog = new MaterialAlertDialogBuilder(context).setView(binding.getRoot()).create();
        mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        mDialog.show();
    }

    private void makeText(String text) {
        pending = text;
        App.post(this::showPending);
    }

    private void showPending() {
        String text = pending;
        if (TextUtils.isEmpty(text)) return;
        Activity activity = App.activity();
        if (activity == null || activity.isFinishing()) return;
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (!(content instanceof FrameLayout)) return;
        pending = null;
        if (toastView != null && toastView.getParent() != content) {
            if (toastView.getParent() instanceof ViewGroup parent) parent.removeView(toastView);
            toastView = null;
        }
        if (toastView == null) {
            toastView = LayoutInflater.from(activity).inflate(R.layout.view_netflix_toast, content, false);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            int side = ResUtil.dp2px(24);
            params.setMargins(side, 0, side, ResUtil.dp2px(88));
            toastView.setElevation(ResUtil.dp2px(16));
            content.addView(toastView, params);
        }
        content.bringChildToFront(toastView);
        ((TextView) toastView).setText(text);
        toastView.animate().cancel();
        toastView.setAlpha(0f);
        toastView.setTranslationY(ResUtil.dp2px(16));
        toastView.animate().alpha(1f).translationY(0f).setDuration(180).start();
        if (hideToast != null) toastView.removeCallbacks(hideToast);
        View shown = toastView;
        hideToast = () -> shown.animate().alpha(0f).translationY(ResUtil.dp2px(12)).setDuration(160).withEndAction(() -> {
            if (shown.getParent() instanceof ViewGroup parent) parent.removeView(shown);
            if (toastView == shown) toastView = null;
        }).start();
        toastView.postDelayed(hideToast, 2200);
    }
}
