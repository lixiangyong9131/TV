package androidx.media3.exoplayer.text;
import android.net.Uri;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.text.SubtitleContent;
public final class DefaultSubtitleContentLoader {
    public DefaultSubtitleContentLoader(Object factory) {}
    public SubtitleContent load(MediaItem item, Uri uri, Format format) { return SubtitleContent.UNSUPPORTED; }
}
