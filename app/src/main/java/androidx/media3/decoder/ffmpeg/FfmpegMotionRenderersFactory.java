package androidx.media3.decoder.ffmpeg;
import android.content.Context;
import androidx.media3.exoplayer.DecoderManager;
import androidx.media3.exoplayer.DefaultRenderersFactory;
public class FfmpegMotionRenderersFactory extends DefaultRenderersFactory {
    public FfmpegMotionRenderersFactory(Context context, int targetFps, int maxHeight, boolean enabled) {
        super(context);
    }
    public void setDecoderManager(DecoderManager decoderManager) {}
    public FfmpegMotionRenderersFactory setEnableSurfaceControlFrameRetention(boolean enabled) { return this; }
    public FfmpegMotionRenderersFactory setEnableRuntimeUpdates(boolean enabled) { return this; }
}
