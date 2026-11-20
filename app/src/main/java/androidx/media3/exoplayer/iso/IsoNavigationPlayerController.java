package androidx.media3.exoplayer.iso;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
public final class IsoNavigationPlayerController {
    private final ExoPlayer player;

    public IsoNavigationPlayerController(ExoPlayer player) {
        this.player = player;
    }

    public Player getPlayer() { return player; }
    public void close() {}
    public MediaSource prepareMediaSource(Object app, MediaSource source, Object dataSourceFactory) { return source; }
    public boolean isStarted() { return false; }
}
