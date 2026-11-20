package androidx.media3.datasource;
import androidx.media3.datasource.cache.Cache;
public final class ProgressiveIsoCache {
    private ProgressiveIsoCache() {}
    public static DataSource.Factory withDiskCache(DataSource.Factory factory, Cache cache) { return factory; }
}
