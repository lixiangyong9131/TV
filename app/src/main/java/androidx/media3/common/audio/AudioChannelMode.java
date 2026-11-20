package androidx.media3.common.audio;
public final class AudioChannelMode {
    public static final int AUTO = 0;
    public static final int STEREO = 1;
    public static final int MONO = 2;
    public static final int REVERSE = 3;
    private AudioChannelMode() {}
    public static int resolve(int mode, int channelCount) { return mode; }
    public static boolean canBalance(int mode) { return false; }
    public static boolean isAvailable(int mode) { return false; }
    public static boolean isAvailable(int mode, int channelCount) { return false; }
    public static boolean isAuto(int mode) { return mode == AUTO; }
    public static int clamp(int mode) { return mode; }
}
