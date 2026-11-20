package androidx.media3.common;
public final class VideoTransform {
    public static final float MAX_SCALE = 8f;
    public static final VideoTransform DEFAULT = new VideoTransform(1f, 1f, 0f, 0f);
    public final float scaleX;
    public final float scaleY;
    public final float panX;
    public final float panY;
    public VideoTransform(float scaleX, float scaleY) { this(scaleX, scaleY, 0f, 0f); }
    public VideoTransform(float scaleX, float scaleY, float panX, float panY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.panX = panX;
        this.panY = panY;
    }
}
