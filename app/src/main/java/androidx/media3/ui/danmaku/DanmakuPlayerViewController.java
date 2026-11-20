package androidx.media3.ui.danmaku;
import android.net.Uri;
import androidx.media3.ui.PlayerView;
import java.util.List;
import okhttp3.OkHttpClient;
public final class DanmakuPlayerViewController {
    public void post(Danmaku danmaku) {}
    public List<Danmaku> getItems() { return List.of(); }
    public void bind(PlayerView playerView) {}
    public void setConfig(DanmakuConfig config) {}
    public void setOkHttpClient(OkHttpClient client) {}
    public void setEnabled(boolean enabled) {}
    public void setDataSource(Uri uri) {}
    public void sendNow(String text) {}
    public void close() {}
}
