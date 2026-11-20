package androidx.media3.common;
public final class DiscMenuState {
    public static final DiscMenuState EMPTY = new DiscMenuState();
    public static final int AVAILABILITY_AVAILABLE = 1;
    public static final int PHASE_IDLE = 0;
    public static final int PHASE_OPENING = 1;
    public static final int PHASE_ACTIVE = 2;
    public int availability;
    public int phase;
    public boolean navigationPlaybackOngoing;
}
