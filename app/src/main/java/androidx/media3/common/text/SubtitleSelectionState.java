package androidx.media3.common.text;
import androidx.media3.common.TrackSelectionOverride;
import java.util.List;
public final class SubtitleSelectionState {
    public static final SubtitleSelectionState EMPTY = new SubtitleSelectionState();
    public TrackSelectionOverride activeSecondarySelection;
    public TrackSelectionOverride primarySelection;
    public TrackSelectionOverride requestedSecondarySelection;
    public List<TrackSelectionOverride> secondaryCandidates = List.of();
    public boolean secondaryPromotedToPrimary;
    public boolean isPrimary(TrackSelectionOverride selection) { return false; }
    public boolean isActiveSecondary(TrackSelectionOverride selection) { return false; }
}
