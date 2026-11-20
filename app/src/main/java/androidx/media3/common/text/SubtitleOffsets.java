package androidx.media3.common.text;
public final class SubtitleOffsets {
    public static final SubtitleOffsets ZERO = new SubtitleOffsets(0, 0);
    public final long primaryMs;
    public final long secondaryMs;
    public SubtitleOffsets(long primaryMs, long secondaryMs) {
        this.primaryMs = primaryMs;
        this.secondaryMs = secondaryMs;
    }
}
