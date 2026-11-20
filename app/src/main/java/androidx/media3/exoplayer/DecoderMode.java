package androidx.media3.exoplayer;
public final class DecoderMode {
    public static final DecoderMode AUTO = new DecoderMode();
    private DecoderMode() {}
    public static DecoderMode fromCommonDecoderMode(androidx.media3.common.DecoderMode mode) { return AUTO; }
    public androidx.media3.common.DecoderMode toCommonDecoderMode() { return androidx.media3.common.DecoderMode.AUTO; }
}
