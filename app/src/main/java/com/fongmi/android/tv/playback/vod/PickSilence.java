package com.fongmi.android.tv.playback.vod;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class PickSilence {

    private static final AtomicInteger DEPTH = new AtomicInteger();
    private static final AtomicBoolean MUTE = new AtomicBoolean();

    private PickSilence() {
    }

    public static void enter() {
        DEPTH.incrementAndGet();
    }

    public static void exit() {
        DEPTH.updateAndGet(value -> Math.max(0, value - 1));
    }

    public static boolean active() {
        return DEPTH.get() > 0;
    }

    public static void muteToast() {
        MUTE.set(true);
    }

    public static void unmuteToast() {
        MUTE.set(false);
    }

    public static boolean toastMuted() {
        return MUTE.get() || DEPTH.get() > 0;
    }
}
