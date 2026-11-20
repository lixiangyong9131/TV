package androidx.media3.exoplayer.source;
public final class DefaultPositionMediaSource extends WrappingMediaSource {
    public DefaultPositionMediaSource(MediaSource mediaSource, long positionMs) { super(mediaSource); }
}
