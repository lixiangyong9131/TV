package androidx.media3.decoder.ffmpeg;
import androidx.media3.exoplayer.ExoPlayer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
public final class FfmpegMotionInterpolationController {
    public FfmpegMotionInterpolationController(ExoPlayer player) {}
    public boolean isSupported() { return false; }
    public int getTargetFps() { return 0; }
    public boolean isUpdatePending() { return false; }
    public void addListener(Runnable listener) {}
    public void removeListener(Runnable listener) {}
    public ListenableFuture<Void> setTargetFps(int fps) { return Futures.immediateVoidFuture(); }
    public void release() {}
}
