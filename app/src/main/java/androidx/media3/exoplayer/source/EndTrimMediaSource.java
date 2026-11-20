package androidx.media3.exoplayer.source;
public final class EndTrimMediaSource extends WrappingMediaSource {
    public EndTrimMediaSource(MediaSource mediaSource, long endingMs) { super(mediaSource); }
    public void setEndTrimDurationMs(long durationMs) {}
    public boolean isEndTrimActive() { return false; }
    public long getOriginalDurationMs() { return 0; }
}
