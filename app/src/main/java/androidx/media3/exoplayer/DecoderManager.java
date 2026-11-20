package androidx.media3.exoplayer;
import android.content.Context;
import androidx.media3.exoplayer.trackselection.DecodeTrackSelector;
public final class DecoderManager {
    public DecoderManager(DecoderMode audioMode, DecoderMode videoMode) {}
    public DecodeTrackSelector createTrackSelector(Context context) { return new DecodeTrackSelector(context); }
    public void attach(ExoPlayer player) {}
    public void detach() {}
    public DecoderMode getAudioMode() { return DecoderMode.AUTO; }
    public DecoderMode getVideoMode() { return DecoderMode.AUTO; }
    public void selectAudioDecoder(DecoderMode mode) {}
    public void selectVideoDecoder(DecoderMode mode) {}
}
