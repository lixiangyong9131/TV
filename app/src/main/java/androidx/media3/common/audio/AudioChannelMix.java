package androidx.media3.common.audio;
public final class AudioChannelMix {
    public static float[][] createStereoMix(int channelCount, boolean reverse) { return new float[0][]; }
    public static float[][] createMonoMix(int channelCount) { return new float[0][]; }
    public static float[][] createFrontBalanceMix(int channelCount, float balance) { return new float[0][]; }
    public static float[][] createFrontCenterGainMix(int channelCount, float gain) { return new float[0][]; }
    public static float[][] compose(float[][] first, float[][] second) { return first; }
}
