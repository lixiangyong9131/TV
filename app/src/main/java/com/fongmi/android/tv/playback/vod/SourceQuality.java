package com.fongmi.android.tv.playback.vod;

import com.fongmi.android.tv.bean.Result;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.UriUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

public final class SourceQuality {

    static final long MIN_BYTES_PER_SECOND = 1024L * 1024L;
    static final long TIER_800 = 800L * 1024L;
    static final long TIER_500 = 500L * 1024L;
    static final long TIER_400 = 400L * 1024L;
    static final long FLOOR_BYTES_PER_SECOND = 200L * 1024L;
    static final int MIN_HEIGHT = 1080;

    private static final int SAMPLE_BYTES = 96 * 1024;
    private static final int FAST_BYTES = 32 * 1024;
    private static final long FAST_MILLIS = 150L;
    private static final long SLOW_MILLIS = 450L;
    private static final int PLAYLIST_BYTES = 64 * 1024;
    private static final int PREVIEW_SECONDS = 10 * 60;
    private static final Pattern REJECT_NAME = Pattern.compile("夸克|quark|阿里云|阿里網盤|阿里网盘|alipan|aliyundrive|aliyunpan|迅雷|xunlei|百度网盘|百度云|百度雲|baidupan|uc网盘|uc云盘|uc網盤|115|天翼|网盘|網盤|云盘|雲盤|cookie|pikpak|123盘|123云|123雲|和彩云|移动云盘|蓝奏|藍奏|城通|磁力", Pattern.CASE_INSENSITIVE);
    private static final Pattern REJECT_URL = Pattern.compile("quark\\.cn|aliyundrive|alipan\\.com|pan\\.baidu|baidupan|yun\\.baidu|115\\.com|123pan|pikpak|caiyun\\.139|yun\\.139|magnet:|ed2k:|thunder://", Pattern.CASE_INSENSITIVE);
    private static final Pattern PREVIEW_TITLE = Pattern.compile("试看|試看|预告|預告|预览|預覽|花絮|片花|trailer|preview", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESOLUTION = Pattern.compile("RESOLUTION=(\\d+)x(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BANDWIDTH = Pattern.compile("BANDWIDTH=(\\d+)", Pattern.CASE_INSENSITIVE);

    private SourceQuality() {
    }

    public static boolean rejectedName(String text) {
        return text != null && REJECT_NAME.matcher(text).find();
    }

    static boolean rejectedTarget(String text) {
        String value = decoded(text);
        return rejectedName(value) || REJECT_URL.matcher(value).find();
    }

    static int namedHeight(String text) {
        if (text == null || text.isEmpty()) return 0;
        String value = text.toLowerCase(Locale.ROOT);
        if (value.contains("4320") || value.contains("8k")) return 4320;
        if (value.contains("2160") || value.contains("4k") || value.contains("uhd")) return 2160;
        if (value.contains("1440") || value.contains("2k")) return 1440;
        if (value.contains("1080") || value.contains("fhd") || value.contains("蓝光") || value.contains("藍光") || value.contains("超清")) return 1080;
        if (value.contains("720")) return 720;
        if (value.contains("480") || value.contains("360") || value.contains("标清") || value.contains("標清") || value.contains("普清") || value.contains("流畅") || value.contains("流暢")) return 480;
        return 0;
    }

    static boolean lowResolution(String text) {
        int height = namedHeight(text);
        return height > 0 && height < MIN_HEIGHT;
    }

    static boolean previewTitle(String text) {
        return text != null && PREVIEW_TITLE.matcher(text).find();
    }

    static boolean shortClip(String context, int seconds) {
        if (seconds <= 0 || seconds >= PREVIEW_SECONDS) return false;
        String text = context == null ? "" : context;
        return !text.contains("短剧") && !text.contains("短劇");
    }

    static Hit inspect(Result result, String context) {
        if (result == null || !result.getResolvedEpisodes().isEmpty()) return null;
        if (rejectedName(result.getMsg()) || result.getMsg().contains("复制") || result.getMsg().contains("複製")) return null;
        int index = bestUrlIndex(result);
        if (index < 0) return null;
        result.getUrl().set(index);
        String label = result.getUrl().n(index) + " " + result.getFlag();
        if (rejectedName(label) || previewTitle(label)) return null;
        String url = decoded(result.getRealUrl());
        int named = namedHeight(label + " " + url);
        if (rejectedName(url) || REJECT_URL.matcher(url).find()) return null;
        if (!isDirectHttp(url) || (result.needParse() && !looksLikeMedia(url))) return null;
        Probe probe = probe(result.getRealUrl(), result, 0);
        if (probe == null) return null;
        if (shortClip(context + " " + label, probe.durationSeconds)) return null;
        int height = Math.max(named, probe.height);
        return new Hit(height, probe.bytesPerSecond, probe.pingMs, probe.bitrateBps);
    }

    private static int bestUrlIndex(Result result) {
        int count = result.getUrl().getValues().size();
        if (count == 0 || result.getUrl().isEmpty()) return -1;
        int best = 0;
        int bestHeight = -1;
        for (int i = 0; i < count; i++) {
            String label = decoded(result.getUrl().n(i) + " " + result.getUrl().v(i));
            if (rejectedName(label) || REJECT_URL.matcher(label).find()) continue;
            int height = namedHeight(label);
            if (height > bestHeight) {
                bestHeight = height;
                best = i;
            }
        }
        String chosen = decoded(result.getUrl().n(best) + " " + result.getUrl().v(best));
        if (rejectedName(chosen)) return -1;
        return best;
    }

    private static Probe probe(String url, Result result, int depth) {
        Request.Builder builder = new Request.Builder().url(url).header("Range", "bytes=0-" + (SAMPLE_BYTES - 1));
        Map<String, String> headers = result.getHeader();
        boolean userAgent = false;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) continue;
            builder.header(header.getKey(), header.getValue());
            if ("user-agent".equalsIgnoreCase(header.getKey())) userAgent = true;
        }
        if (!userAgent) builder.header("User-Agent", "Mozilla/5.0");
        Call call = OkHttp.player(result.getAuth()).newCall(builder.build());
        call.timeout().timeout(2, TimeUnit.SECONDS);
        long began = System.nanoTime();
        try (Response response = call.execute()) {
            int ping = (int) Math.min(Integer.MAX_VALUE, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - began));
            if (response.body() == null || (!response.isSuccessful() && response.code() != 206)) return null;
            String type = response.header("Content-Type", "");
            if (uselessType(type) && !looksLikeMedia(url) && !looksLikePlaylist(url, type)) return null;
            Probe probe = read(response, url, type == null ? "" : type, result, depth);
            return depth == 0 ? stamp(probe, ping, 0) : probe;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Probe read(Response response, String url, String type, Result result, int depth) throws IOException {
        BufferedSource source = response.body().source();
        boolean playlist = depth < 2 && (looksLikePlaylist(url, type));
        Buffer sample = new Buffer();
        long read = 0;
        long start = System.nanoTime();
        int limit = playlist ? PLAYLIST_BYTES : SAMPLE_BYTES;
        while (read < limit) {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (!playlist && read >= FAST_BYTES && elapsed >= FAST_MILLIS) {
                long rate = read * 1000L / Math.max(1, elapsed);
                if (rate >= MIN_BYTES_PER_SECOND) break;
            }
            if (!playlist && elapsed >= SLOW_MILLIS && read > 0) break;
            long count = source.read(sample, Math.min(8192, limit - read));
            if (count < 0) break;
            read += count;
            if (!playlist && read >= 16 && startsWith(sample, "#EXTM3U")) playlist = depth < 2;
            if (playlist && read >= 16 && !startsWith(sample, "#EXTM3U") && !looksLikePlaylist(url, type)) playlist = false;
        }
        if (playlist || startsWith(sample, "#EXTM3U")) return followPlaylist(url, sample.readUtf8(), result, depth);
        long elapsed = Math.max(1, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
        if (read < 32 * 1024 || !playableContainer(sample)) return null;
        return new Probe(0, read * 1000L / elapsed, mp4Duration(sample), 0, 0);
    }

    private static Probe followPlaylist(String url, String text, Result result, int depth) {
        String variant = null;
        String media = null;
        int bestHeight = -1;
        long bestBandwidth = -1;
        String info = null;
        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("#EXT-X-STREAM-INF")) {
                info = line;
                continue;
            }
            if (line.startsWith("#")) continue;
            String resolved = UriUtil.resolve(url, line);
            if (info != null) {
                int height = resolutionHeight(info);
                long bandwidth = bandwidth(info);
                if (height > bestHeight || (height == bestHeight && bandwidth > bestBandwidth)) {
                    bestHeight = height;
                    bestBandwidth = bandwidth;
                    variant = resolved;
                }
                info = null;
            } else if (media == null) {
                media = resolved;
            }
        }
        int listed = playlistDuration(text);
        if (variant != null) {
            Probe next = probe(variant, result, depth + 1);
            if (next == null) return null;
            int duration = listed > 0 ? listed : next.durationSeconds;
            return stamp(new Probe(Math.max(bestHeight, next.height), next.bytesPerSecond, duration, next.pingMs, next.bitrateBps), 0, bestBandwidth);
        }
        if (media == null || depth >= 2) return null;
        Probe next = probe(media, result, depth + 1);
        if (next == null) return null;
        return new Probe(next.height, next.bytesPerSecond, listed > 0 ? listed : 0, next.pingMs, next.bitrateBps);
    }

    private static int playlistDuration(String text) {
        if (text == null || !text.contains("#EXT-X-ENDLIST")) return 0;
        double seconds = 0;
        boolean any = false;
        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (!line.startsWith("#EXTINF:")) continue;
            String number = line.substring(8);
            int comma = number.indexOf(',');
            if (comma >= 0) number = number.substring(0, comma);
            try {
                seconds += Double.parseDouble(number.trim());
                any = true;
            } catch (NumberFormatException ignored) {
            }
        }
        return any ? (int) Math.round(seconds) : 0;
    }

    private static int mp4Duration(Buffer sample) {
        byte[] data = sample.snapshot().toByteArray();
        for (int i = 0; i + 32 < data.length; i++) {
            if (data[i] != 'm' || data[i + 1] != 'v' || data[i + 2] != 'h' || data[i + 3] != 'd') continue;
            int version = data[i + 4] & 0xff;
            int time = i + 12 + (version == 0 ? 8 : 16);
            if (version == 0 && time + 8 > data.length) continue;
            if (version != 0 && time + 12 > data.length) continue;
            long timescale = u32(data, time);
            long duration = version == 0 ? u32(data, time + 4) : u64(data, time + 4);
            if (timescale <= 0 || duration <= 0) continue;
            return (int) (duration / timescale);
        }
        return 0;
    }

    private static long u32(byte[] data, int offset) {
        return ((long) data[offset] & 0xff) << 24 | ((long) data[offset + 1] & 0xff) << 16 | ((long) data[offset + 2] & 0xff) << 8 | ((long) data[offset + 3] & 0xff);
    }

    private static long u64(byte[] data, int offset) {
        return u32(data, offset) << 32 | u32(data, offset + 4);
    }

    private static String decoded(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return value;
        }
    }

    private static boolean uselessType(String type) {
        String value = type == null ? "" : type.toLowerCase(Locale.ROOT);
        return value.contains("text/html") || value.contains("application/json") || value.contains("text/xml") || value.contains("application/xml");
    }

    private static boolean playableContainer(Buffer sample) {
        int size = (int) Math.min(sample.size(), 189);
        if (size < 4) return false;
        byte[] data = sample.snapshot(size).toByteArray();
        if (findAscii(data, "ftyp") >= 0) return true;
        if ((data[0] & 0xff) == 0x1a && data[1] == 0x45 && (data[2] & 0xff) == 0xdf && (data[3] & 0xff) == 0xa3) return true;
        if (data[0] == 'F' && data[1] == 'L' && data[2] == 'V') return true;
        if (data[0] == 'O' && data[1] == 'g' && data[2] == 'g' && data[3] == 'S') return true;
        if (data[0] == 'I' && data[1] == 'D' && data[2] == '3') return true;
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F') return true;
        if (data[0] == 0 && data[1] == 0 && data[2] == 1 && (data[3] & 0xff) == 0xba) return true;
        return data[0] == 0x47 && size > 188 && data[188] == 0x47;
    }

    private static int findAscii(byte[] data, String text) {
        byte[] needle = text.getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i + needle.length <= data.length && i < 32; i++) {
            boolean match = true;
            for (int j = 0; j < needle.length; j++) if (data[i + j] != needle[j]) match = false;
            if (match) return i;
        }
        return -1;
    }

    private static boolean isDirectHttp(String url) {
        String value = url == null ? "" : url.toLowerCase(Locale.ROOT);
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private static boolean looksLikeMedia(String url) {
        String value = url.toLowerCase(Locale.ROOT);
        return value.contains(".m3u8") || value.contains(".mp4") || value.contains(".mkv") || value.contains(".flv") || value.contains(".ts") || value.contains(".mpd") || value.contains("m3u8") || value.contains("mpegurl");
    }

    private static boolean looksLikePlaylist(String url, String type) {
        String value = (url + " " + type).toLowerCase(Locale.ROOT);
        return value.contains("mpegurl") || value.contains("m3u8") || value.contains("application/vnd.apple.mpegurl") || value.contains("audio/mpegurl");
    }

    private static boolean startsWith(Buffer sample, String prefix) {
        if (sample.size() < prefix.length()) return false;
        return prefix.equals(sample.snapshot(prefix.length()).utf8());
    }

    private static int resolutionHeight(String info) {
        Matcher matcher = RESOLUTION.matcher(info);
        if (!matcher.find()) return 0;
        return Integer.parseInt(matcher.group(2));
    }

    private static long bandwidth(String info) {
        Matcher matcher = BANDWIDTH.matcher(info);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0;
    }

    static String qualityLabel(int height) {
        if (height >= 2160) return "4K";
        if (height >= 1440) return "2K";
        if (height >= 1080) return "1080p";
        if (height >= 720) return "720p";
        if (height >= 480) return "480p";
        if (height > 0) return "SD";
        return "未知";
    }

    static int qualityPoints(int height) {
        if (height >= 2160) return 100;
        if (height >= 1440) return 85;
        if (height >= 1080) return 75;
        if (height >= 720) return 60;
        if (height >= 480) return 40;
        if (height > 0) return 20;
        return 0;
    }

    private static Probe stamp(Probe probe, int ping, long bitrate) {
        if (probe == null) return null;
        return new Probe(probe.height, probe.bytesPerSecond, probe.durationSeconds, ping > 0 ? ping : probe.pingMs, bitrate > 0 ? bitrate : probe.bitrateBps);
    }

    static final class Hit {
        final int height;
        final long bytesPerSecond;
        final int pingMs;
        final long bitrateBps;

        Hit(int height, long bytesPerSecond, int pingMs, long bitrateBps) {
            this.height = height;
            this.bytesPerSecond = bytesPerSecond;
            this.pingMs = pingMs;
            this.bitrateBps = bitrateBps;
        }
    }

    private static final class Probe {
        final int height;
        final long bytesPerSecond;
        final int durationSeconds;
        final int pingMs;
        final long bitrateBps;

        Probe(int height, long bytesPerSecond, int durationSeconds, int pingMs, long bitrateBps) {
            this.height = height;
            this.bytesPerSecond = bytesPerSecond;
            this.durationSeconds = durationSeconds;
            this.pingMs = pingMs;
            this.bitrateBps = bitrateBps;
        }
    }
}
