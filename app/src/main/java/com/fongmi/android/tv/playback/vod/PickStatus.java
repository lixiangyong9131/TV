package com.fongmi.android.tv.playback.vod;

public final class PickStatus {

    public static final int SEARCH = 1;
    public static final int MEASURE = 2;
    public static final int OPEN = 3;
    public static final int FAILED = 4;

    public final int phase;
    public final int searchDone;
    public final int searchTotal;
    public final int found;
    public final int measureDone;
    public final int measureTotal;
    public final String measureName;
    public final String note;

    private PickStatus(int phase, int searchDone, int searchTotal, int found, int measureDone, int measureTotal, String measureName, String note) {
        this.phase = phase;
        this.searchDone = searchDone;
        this.searchTotal = searchTotal;
        this.found = found;
        this.measureDone = measureDone;
        this.measureTotal = measureTotal;
        this.measureName = measureName == null ? "" : measureName;
        this.note = note == null ? "" : note;
    }

    public static PickStatus loading() {
        return new PickStatus(SEARCH, 0, 1, 0, 0, 0, "", "");
    }

    public static PickStatus search(int done, int total, int found) {
        return new PickStatus(SEARCH, done, total, found, 0, 0, "", "");
    }

    public static PickStatus measure(int searchDone, int searchTotal, int found, int measureDone, int measureTotal, String name) {
        return new PickStatus(MEASURE, searchDone, searchTotal, found, measureDone, measureTotal, name, "");
    }

    public static PickStatus open() {
        return new PickStatus(OPEN, 1, 1, 1, 1, 1, "", "");
    }

    public static PickStatus failed(String note) {
        return new PickStatus(FAILED, 0, 0, 0, 0, 0, "", note);
    }
}
