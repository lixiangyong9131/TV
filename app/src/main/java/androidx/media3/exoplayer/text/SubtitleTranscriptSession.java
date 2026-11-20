package androidx.media3.exoplayer.text;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;
import androidx.media3.exoplayer.ExoPlayer;
public final class SubtitleTranscriptSession {
    public void setLoader(Object loader) {}
    public void setPlayer(ExoPlayer player) {}
    public void setPreloadMediaItem(MediaItem mediaItem) {}
    public SubtitleTranscript forMediaItem(MediaItem mediaItem) { return new SubtitleTranscript(); }
    public SubtitleContent getContent(Format format, long offsetMs) { return SubtitleContent.UNSUPPORTED; }
    public void setContentEnabled(boolean enabled) {}
    public boolean canRetryContent(Format format) { return false; }
    public void retryContent(Format format) {}
    public void close() {}
}
