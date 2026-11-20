package androidx.media3.common;
import androidx.annotation.Nullable;
public class DiscMenuController {
    public enum OpenResult { RUNTIME_MISSING, UNAVAILABLE, TIMED_OUT, OPENED }
    public boolean isNavigationPlayback() { return false; }
    public boolean isActive() { return false; }
    public boolean sendAction(String action) { return false; }
    public boolean supportsPointer() { return false; }
    public boolean open(String action, @Nullable java.util.function.Consumer<OpenResult> callback) { return false; }
}
