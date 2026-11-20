package com.fongmi.android.tv.playback.vod;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Vod;

import com.github.catvod.utils.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class BestSourceOpener {

    private static final long BUDGET_MILLIS = 10000L;
    private static final long EMPTY_BUDGET_MILLIS = 12000L;
    private static final int PARALLEL = 3;
    private static final int MAX_MEASURE = 8;
    private static final AtomicInteger GENERATION = new AtomicInteger();

    public interface Step {
        void onStep(PickStatus status);
    }

    public interface Ready {
        void onLines(List<RankedLine> lines, boolean done);
    }

    private BestSourceOpener() {
    }

    public static void abort() {
        GENERATION.incrementAndGet();
    }

    public static void find(String keyword, Step step, Ready onReady) {
        int gen = GENERATION.incrementAndGet();
        Task.execute(() -> rank(gen, keyword, step, onReady));
    }

    private static boolean stale(int gen, AtomicBoolean closed) {
        return closed.get() || gen != GENERATION.get();
    }

    private static void rank(int gen, String keyword, Step step, Ready onReady) {
        String wanted = normalize(keyword);
        if (wanted.isEmpty()) {
            finish(List.of(), onReady);
            return;
        }
        List<Site> sites = searchableSites();
        if (sites.isEmpty()) {
            finish(List.of(), onReady);
            return;
        }
        report(step, PickStatus.search(0, sites.size(), 0));
        List<Vod> matches = new CopyOnWriteArrayList<>();
        AtomicInteger pending = new AtomicInteger(sites.size());
        AtomicBoolean closed = new AtomicBoolean();
        for (Site site : sites) {
            Task.largeExecutor().execute(() -> {
                if (stale(gen, closed)) {
                    pending.decrementAndGet();
                    return;
                }
                try {
                    Result result = SiteApi.searchContent(site, keyword, false, "1");
                    for (Vod item : result.getList()) {
                        if (!sameTitle(item.getName(), wanted)) continue;
                        if (SourceQuality.rejectedName(site.getName() + item.getName())) continue;
                        matches.add(item);
                    }
                } catch (Exception ignored) {
                } finally {
                    pending.decrementAndGet();
                }
            });
        }
        List<RankedLine> found = new CopyOnWriteArrayList<>();
        List<RankedLine> fallback = new CopyOnWriteArrayList<>();
        Set<String> queued = ConcurrentHashMap.newKeySet();
        AtomicInteger inflight = new AtomicInteger();
        AtomicInteger measured = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL);
        long start = System.currentTimeMillis();
        int seenDone = -1;
        int seenFound = -1;
        int measuring = 0;
        try {
            while (true) {
                if (stale(gen, closed)) break;
                int finished = sites.size() - pending.get();
                if (finished != seenDone || matches.size() != seenFound) {
                    seenDone = finished;
                    seenFound = matches.size();
                    report(step, PickStatus.search(finished, sites.size(), seenFound));
                }
                for (Vod item : matches) {
                    if (item.getSiteKey().isEmpty() || item.getId().isEmpty()) continue;
                    if (!queued.add(item.getSiteKey() + "\n" + item.getId())) continue;
                    RankedLine placeholder = unscored(item);
                    boolean skip = measuring >= MAX_MEASURE;
                    if (skip) placeholder.speedKind = RankedLine.SPEED_NONE;
                    synchronized (found) {
                        if (!closed.get()) fallback.add(placeholder);
                    }
                    publish(found, fallback, onReady, false);
                    if (skip) continue;
                    measuring++;
                    inflight.incrementAndGet();
                    pool.execute(() -> measure(gen, item, sites.size(), finished, queued.size(), found, fallback, inflight, measured, closed, step, onReady));
                }
                long elapsed = System.currentTimeMillis() - start;
                boolean searchDone = pending.get() == 0;
                boolean idle = inflight.get() == 0 && measured.get() >= measuring;
                if (searchDone && idle) break;
                if (!found.isEmpty() && elapsed >= BUDGET_MILLIS) break;
                if (found.isEmpty() && elapsed >= EMPTY_BUDGET_MILLIS && idle) break;
                if (elapsed >= EMPTY_BUDGET_MILLIS + 4000L) break;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            synchronized (found) {
                closed.set(true);
                for (RankedLine line : fallback) {
                    if (line.speedKind == RankedLine.SPEED_WAIT) line.speedKind = RankedLine.SPEED_NONE;
                }
            }
            pool.shutdownNow();
            publish(found, fallback, onReady, true);
        }
    }

    private static void measure(int gen, Vod item, int siteTotal, int searchDone, int total, List<RankedLine> found, List<RankedLine> fallback, AtomicInteger inflight, AtomicInteger measured, AtomicBoolean closed, Step step, Ready onReady) {
        boolean ok = false;
        try {
            if (stale(gen, closed)) return;
            Result detail = SiteApi.detailContent(item.getSiteKey(), item.getId());
            if (detail.getVod() == null || closed.get()) return;
            String context = detail.getVod().getTypeName() + " " + item.getName();
            List<RankedLine> lines = QualitySourcePicker.collectNow(() -> !stale(gen, closed), item, false, detail.getVod().getFlags(), "", context);
            if (lines.isEmpty()) return;
            synchronized (found) {
                if (closed.get()) return;
                found.addAll(lines);
                ok = true;
            }
            publish(found, fallback, onReady, false);
        } catch (Exception ignored) {
        } finally {
            if (!ok && !closed.get()) {
                synchronized (found) {
                    if (!closed.get()) markNone(fallback, item.getSiteKey(), item.getId());
                }
                if (!closed.get()) publish(found, fallback, onReady, false);
            }
            int finished = measured.incrementAndGet();
            inflight.decrementAndGet();
            if (!closed.get()) report(step, PickStatus.measure(searchDone, siteTotal, total, finished, Math.max(total, finished), item.getSiteName()));
        }
    }

    private static void finish(List<RankedLine> found, Ready onReady) {
        publish(found, List.of(), onReady, true);
    }

    private static void publish(List<RankedLine> found, List<RankedLine> fallback, Ready onReady, boolean done) {
        List<RankedLine> ranked;
        synchronized (found) {
            if (!found.isEmpty()) {
                ranked = new ArrayList<>(found);
                RankedLine.score(ranked);
                for (RankedLine line : fallback) if (!covered(ranked, line)) ranked.add(line);
            } else {
                ranked = new ArrayList<>(fallback);
            }
        }
        App.post(() -> onReady.onLines(ranked, done));
    }

    private static void markNone(List<RankedLine> fallback, String siteKey, String vodId) {
        for (RankedLine line : fallback) {
            if (line.speedKind == RankedLine.SPEED_WAIT && line.siteKey.equals(siteKey) && line.vodId.equals(vodId)) {
                line.speedKind = RankedLine.SPEED_NONE;
            }
        }
    }

    private static boolean covered(List<RankedLine> ranked, RankedLine line) {
        for (RankedLine item : ranked) if (item.siteKey.equals(line.siteKey) && item.vodId.equals(line.vodId)) return true;
        return false;
    }

    private static RankedLine unscored(Vod item) {
        String siteName = item.getSiteName();
        if (siteName.isEmpty()) siteName = VodConfig.get().getSite(item.getSiteKey()).getName();
        RankedLine line = new RankedLine(item.getSiteKey(), siteName, item.getId(), item.getName(), item.getPic(), null, null, null, null);
        line.setEpisodes(item.getRemarks(), 0);
        return line;
    }

    private static void report(Step step, PickStatus status) {
        if (step == null) return;
        App.post(() -> step.onStep(status));
    }

    private static List<Site> searchableSites() {
        List<Site> sites = VodConfig.get().getSites().stream().filter(Site::isSearchable).filter(site -> !site.isIndex()).filter(site -> !SourceQuality.rejectedName(site.getName())).toList();
        if (sites.isEmpty()) sites = VodConfig.get().getSites().stream().filter(Site::isSearchable).filter(site -> !SourceQuality.rejectedName(site.getName())).toList();
        return sites;
    }

    private static boolean sameTitle(String name, String wanted) {
        String actual = normalize(name);
        return !actual.isEmpty() && (actual.equals(wanted) || actual.startsWith(wanted));
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\s:：·.。,，!！?？\\-—_（）()\\[\\]【】]", "").toLowerCase(Locale.ROOT);
    }
}
