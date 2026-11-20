package androidx.media3.effect;
public final class VideoAdjustment {
    public static final class Builder {
        public Builder setSaturation(float value) { return this; }
        public Builder setContrast(float value) { return this; }
        public Builder setBrightness(float value) { return this; }
        public Builder setGamma(float value) { return this; }
        public Builder setHue(float value) { return this; }
        public Builder setRedGain(float value) { return this; }
        public Builder setBlueGain(float value) { return this; }
        public Builder setSharpness(float value) { return this; }
        public Builder setThreshold(float value) { return this; }
        public Builder setShadowLift(float value) { return this; }
        public VideoAdjustment build() { return new VideoAdjustment(); }
    }
}
