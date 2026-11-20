package androidx.media3.exoplayer.libass;
public final class LibassConfiguration {
    public static final class Builder {
        public Builder setFontConfig(String path) { return this; }
        public Builder setFontsDirectory(String path) { return this; }
        public Builder setDefaultFontFamily(String family) { return this; }
        public Builder setMaximumRenderPixels(int pixels) { return this; }
        public Builder setMaximumGlyphCount(int count) { return this; }
        public Builder setMaximumBitmapCacheSizeMb(int sizeMb) { return this; }
        public LibassConfiguration build() { return new LibassConfiguration(); }
    }
}
