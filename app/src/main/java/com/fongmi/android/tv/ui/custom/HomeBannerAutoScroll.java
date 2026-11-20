package com.fongmi.android.tv.ui.custom;

import android.os.SystemClock;
import android.view.View;

public final class HomeBannerAutoScroll {

    private static final long INTERVAL = 3000L;

    public interface Host {
        View view();
        boolean ready();
        int current();
        int count();
        void show(int position);
    }

    private final Host host;
    private final Runnable tick = this::tick;
    private boolean running;
    private boolean paused;
    private boolean advancing;
    private long lastShow;

    public HomeBannerAutoScroll(Host host) {
        this.host = host;
    }

    public void start() {
        running = true;
        paused = false;
        advancing = false;
        lastShow = SystemClock.elapsedRealtime();
        schedule();
    }

    public void stop() {
        running = false;
        paused = false;
        advancing = false;
        unschedule();
    }

    public void pause() {
        paused = true;
        advancing = false;
        unschedule();
    }

    public void resume() {
        if (!running || !paused) return;
        paused = false;
        lastShow = SystemClock.elapsedRealtime();
        schedule();
    }

    public void onScrollState(int state, int dragging, int idle) {
        if (advancing) {
            if (state == idle) advancing = false;
            return;
        }
        if (state == dragging) pause();
        else if (state == idle) resume();
    }

    private void tick() {
        if (!running || paused) return;
        long now = SystemClock.elapsedRealtime();
        if (now - lastShow >= INTERVAL && host.ready() && host.count() > 1) {
            lastShow = now;
            advancing = true;
            host.show(host.current() + 1);
        }
        schedule();
    }

    private void schedule() {
        View view = host.view();
        if (view == null || !running || paused) return;
        view.removeCallbacks(tick);
        view.postDelayed(tick, INTERVAL);
    }

    private void unschedule() {
        View view = host.view();
        if (view != null) view.removeCallbacks(tick);
    }
}
