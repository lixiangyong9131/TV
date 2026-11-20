package androidx.media3.common.util;
import androidx.media3.common.Player;
import java.util.List;
public final class TrackSelectionUtil {
    private TrackSelectionUtil() {}
    public static boolean cycleTrack(Player player, int trackType, boolean reverse, boolean enabled) { return false; }
    public static boolean cycleTrack(Player player, int trackType, List<String> trackIds, boolean reverse, boolean enabled) { return false; }
    public static boolean setTrackTypeDisabled(Player player, int trackType, boolean disabled) { return false; }
    public static boolean resetTrackSelection(Player player, int trackType) { return false; }
    public static boolean selectTrack(Player player, int trackType, String trackId) { return false; }
}
