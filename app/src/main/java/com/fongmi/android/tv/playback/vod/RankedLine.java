package com.fongmi.android.tv.playback.vod;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.Result;

import java.util.Comparator;
import java.util.List;

public final class RankedLine {

    public final String siteKey;
    public final String siteName;
    public final String vodId;
    public final String vodName;
    public final String pic;
    public final Flag flag;
    public final Episode episode;
    public final Result result;
    public final int height;
    public final long bytesPerSecond;
    public final int pingMs;
    public final long bitrateBps;
    public static final int SPEED_WAIT = 0;
    public static final int SPEED_OK = 1;
    public static final int SPEED_NONE = 2;

    public String quality = "未知";
    public String episodes = "";
    public double score;
    public int speedKind = SPEED_WAIT;

    public void setEpisodes(String remarks, int count) {
        if (count > 1) {
            episodes = count + "集";
            return;
        }
        if (remarks == null || !remarks.contains("集")) {
            episodes = "";
            return;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*集").matcher(remarks);
        String digits = "";
        while (matcher.find()) digits = matcher.group(1);
        if (digits.isEmpty()) {
            episodes = remarks;
            return;
        }
        try {
            episodes = Integer.parseInt(digits) + "集";
        } catch (NumberFormatException ignored) {
            episodes = digits + "集";
        }
    }

    public RankedLine(String siteKey, String siteName, String vodId, String vodName, String pic, Flag flag, Episode episode, Result result, SourceQuality.Hit hit) {
        this.siteKey = siteKey == null ? "" : siteKey;
        this.siteName = siteName == null ? "" : siteName;
        this.vodId = vodId == null ? "" : vodId;
        this.vodName = vodName == null ? "" : vodName;
        this.pic = pic == null ? "" : pic;
        this.flag = flag;
        this.episode = episode;
        this.result = result;
        this.height = hit == null ? 0 : hit.height;
        this.bytesPerSecond = hit == null ? 0 : hit.bytesPerSecond;
        this.pingMs = hit == null ? 0 : hit.pingMs;
        this.bitrateBps = hit == null ? 0 : hit.bitrateBps;
        if (hit != null && hit.bytesPerSecond > 0) speedKind = SPEED_OK;
        else if (result != null || hit != null) speedKind = SPEED_NONE;
        else speedKind = SPEED_WAIT;
    }

    public boolean probed() {
        return result != null && flag != null && episode != null;
    }

    public static void score(List<RankedLine> lines) {
        long maxSpeed = 1;
        int minPing = Integer.MAX_VALUE;
        int maxPing = 0;
        for (RankedLine line : lines) {
            if (line.bytesPerSecond > maxSpeed) maxSpeed = line.bytesPerSecond;
            if (line.pingMs > 0) {
                minPing = Math.min(minPing, line.pingMs);
                maxPing = Math.max(maxPing, line.pingMs);
            }
        }
        for (RankedLine line : lines) {
            double quality = SourceQuality.qualityPoints(line.height);
            double speed = line.bytesPerSecond > 0 ? Math.min(100, line.bytesPerSecond * 100.0 / maxSpeed) : 30;
            double ping = 0;
            if (line.pingMs > 0) {
                ping = maxPing == minPing ? 100 : Math.min(100, Math.max(0, (maxPing - line.pingMs) * 100.0 / (maxPing - minPing)));
            }
            line.quality = SourceQuality.qualityLabel(line.height);
            line.score = quality * 0.4 + speed * 0.4 + ping * 0.2;
        }
        lines.sort(Comparator.comparingDouble((RankedLine line) -> line.score).reversed());
    }

    public boolean good() {
        return score >= 70;
    }
}
