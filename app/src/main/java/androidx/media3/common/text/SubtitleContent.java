package androidx.media3.common.text;
import java.util.List;
public final class SubtitleContent {
    public enum State { LOADING, ERROR, UNSUPPORTED, PARTIAL, TRUNCATED, COMPLETE }
    public static final class Entry {
        public boolean image;
        public String text;
        public long startTimeUs;
        public long endTimeUs;
    }
    public static final SubtitleContent UNSUPPORTED = new SubtitleContent(State.UNSUPPORTED);
    public static final SubtitleContent LOADING = new SubtitleContent(State.LOADING);
    public final State state;
    public final List<Entry> entries = List.of();
    public SubtitleContent(State state) { this.state = state; }
}
