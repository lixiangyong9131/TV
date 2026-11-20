package androidx.media3.common.audio;
public final class AudioEffectParameters {
    public static final AudioEffectParameters DEFAULT = new Builder().build();
    public final int channelMode;
    public final float balance;
    public final boolean loudnessEnabled;
    public final int boostMillibels;
    public final int preampMillibels;
    public final short[] equalizerLevelsMillibels;
    public final float stability;
    public final float centerGainFactor;
    private AudioEffectParameters(Builder builder) {
        channelMode = builder.channelMode;
        balance = builder.balance;
        loudnessEnabled = builder.loudnessEnabled;
        boostMillibels = builder.boostMillibels;
        preampMillibels = builder.preampMillibels;
        equalizerLevelsMillibels = builder.levels;
        stability = builder.stability;
        centerGainFactor = builder.centerGain;
    }
    public boolean hasEffect() { return false; }
    public boolean hasBands() { return equalizerLevelsMillibels.length > 0; }
    public static boolean isCenterGainAvailable(int channelCount) { return false; }
    public int getChannelMode() { return channelMode; }
    public float getBalance() { return balance; }
    public boolean isLoudnessEnabled() { return loudnessEnabled; }
    public int getBoostMillibels() { return boostMillibels; }
    public int getPreampMillibels() { return preampMillibels; }
    public short[] getEqualizerLevelsMillibels() { return equalizerLevelsMillibels; }
    public boolean shouldLimitOutput(int channelCount) { return false; }
    public boolean hasCenterGain(int channelCount) { return false; }
    public float getCenterGainFactor() { return centerGainFactor; }
    public float getStability() { return stability; }
    public static final class Builder {
        private int channelMode;
        private float balance;
        private boolean loudnessEnabled;
        private int boostMillibels;
        private int preampMillibels;
        private short[] levels = new short[0];
        private float stability;
        private float centerGain;
        public Builder setEqualizerBands(int[] frequencies, short[] levels) { this.levels = levels == null ? new short[0] : levels; return this; }
        public Builder setStability(float value) { stability = value; return this; }
        public Builder setBoostMillibels(int value) { boostMillibels = value; return this; }
        public Builder setPreampMillibels(int value) { preampMillibels = value; return this; }
        public Builder setCenterGainMillibels(int value) { return this; }
        public Builder setBalance(float value) { balance = value; return this; }
        public Builder setChannelMode(int value) { channelMode = value; return this; }
        public Builder setLoudnessEnabled(boolean value) { loudnessEnabled = value; return this; }
        public AudioEffectParameters build() { return new AudioEffectParameters(this); }
    }
}
