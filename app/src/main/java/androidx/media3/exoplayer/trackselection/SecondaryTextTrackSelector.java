package androidx.media3.exoplayer.trackselection;
import android.content.Context;
public final class SecondaryTextTrackSelector extends DefaultTrackSelector {
    public static final class Factory implements TrackSelector.Factory {
        private final TrackSelector.Factory delegate;
        public Factory(TrackSelector.Factory delegate) { this.delegate = delegate; }
        @Override public TrackSelector createTrackSelector(Context context) { return delegate.createTrackSelector(context); }
    }
    public SecondaryTextTrackSelector(Context context) { super(context); }
}
