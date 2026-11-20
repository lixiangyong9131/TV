package androidx.media3.exoplayer.source;
import androidx.media3.common.MediaItem;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy;
public final class ContentTypeResolvingMediaSource {
    public static final class Factory implements MediaSource.Factory {
        private final MediaSource.Factory delegate;
        public Factory(MediaSource.Factory delegate, DataSource.Factory dataSourceFactory) { this.delegate = delegate; }
        @Override public MediaSource.Factory setDrmSessionManagerProvider(DrmSessionManagerProvider provider) {
            delegate.setDrmSessionManagerProvider(provider); return this;
        }
        @Override public MediaSource.Factory setLoadErrorHandlingPolicy(LoadErrorHandlingPolicy policy) {
            delegate.setLoadErrorHandlingPolicy(policy); return this;
        }
        @Override public int[] getSupportedTypes() { return delegate.getSupportedTypes(); }
        @Override public MediaSource createMediaSource(MediaItem mediaItem) { return delegate.createMediaSource(mediaItem); }
    }
}
