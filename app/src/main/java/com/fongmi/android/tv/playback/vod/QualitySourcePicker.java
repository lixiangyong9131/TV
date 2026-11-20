package com.fongmi.android.tv.playback.vod;

import com.fongmi.android.tv.App;
import com.fongmi.android.tv.api.SiteApi;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.bean.Flag;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Vod;
import com.github.catvod.utils.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class QualitySourcePicker {

    private static final int PARALLEL = 3;
    private static final int MAX_FLAGS = 4;

    private QualitySourcePicker() {
    }

    static List<RankedLine> collectNow(Vod item, boolean builtin, List<Flag> flags, String remarks, String context) {
        return collectNow(() -> true, item, builtin, flags, remarks, context);
    }

    static List<RankedLine> collectNow(BooleanSupplier active, Vod item, boolean builtin, List<Flag> flags, String remarks, String context) {
        return collect(active, item, builtin, flags, remarks, context, null, null, false, 2);
    }

    static void pick(BooleanSupplier active, Vod item, boolean builtin, List<Flag> flags, String remarks, String context, Consumer<PickStatus> progress, BiConsumer<List<RankedLine>, Boolean> callback) {
        Task.executeSerial(() -> {
            Consumer<List<RankedLine>> partial = snapshot -> App.post(() -> {
                if (active.getAsBoolean() && !snapshot.isEmpty()) callback.accept(snapshot, false);
            });
            List<RankedLine> lines = active.getAsBoolean() ? collect(active, item, builtin, flags, remarks, context, status -> App.post(() -> {
                if (active.getAsBoolean()) progress.accept(status);
            }), partial, true, MAX_FLAGS) : List.of();
            App.post(() -> {
                if (active.getAsBoolean()) callback.accept(lines, true);
            });
        });
    }

    private static List<RankedLine> collect(BooleanSupplier active, Vod item, boolean builtin, List<Flag> flags, String remarks, String context, Consumer<PickStatus> progress, Consumer<List<RankedLine>> partial, boolean parallel, int maxFlags) {
        List<Flag> candidates = new ArrayList<>();
        if (flags != null) {
            for (Flag flag : flags) {
                if (!active.getAsBoolean()) return ranked(List.of());
                if (flag.getEpisodes().isEmpty()) continue;
                String label = flag.getShow() + " " + flag.getFlag() + " " + flag.getEpisodes().get(0).getName() + " " + flag.getEpisodes().get(0).getUrl();
                if (SourceQuality.rejectedTarget(label) || SourceQuality.previewTitle(label)) continue;
                candidates.add(flag);
            }
        }
        candidates.sort(Comparator.comparingInt((Flag flag) -> SourceQuality.namedHeight(flag.getShow() + " " + flag.getFlag())).reversed());
        if (candidates.size() > maxFlags) candidates = new ArrayList<>(candidates.subList(0, maxFlags));
        if (candidates.isEmpty()) return ranked(List.of());
        List<RankedLine> lines = new CopyOnWriteArrayList<>();
        AtomicInteger done = new AtomicInteger();
        int total = candidates.size();
        if (!parallel) {
            for (Flag flag : candidates) {
                if (!active.getAsBoolean()) break;
                RankedLine line = inspect(item, builtin, flag, remarks, context);
                if (line != null) {
                    lines.add(line);
                    break;
                }
                if (progress != null) progress.accept(PickStatus.measure(1, 1, 1, done.incrementAndGet(), total, flag.getShow().isEmpty() ? flag.getFlag() : flag.getShow()));
            }
        } else {
            ExecutorService pool = Executors.newFixedThreadPool(Math.min(PARALLEL, total));
            CountDownLatch latch = new CountDownLatch(total);
            for (Flag flag : candidates) {
                pool.execute(() -> {
                    try {
                        if (!active.getAsBoolean()) return;
                        RankedLine line = inspect(item, builtin, flag, remarks, context);
                        if (line != null) {
                            lines.add(line);
                            if (partial != null) partial.accept(ranked(lines));
                        }
                    } finally {
                        int finished = done.incrementAndGet();
                        if (progress != null) progress.accept(PickStatus.measure(1, 1, 1, finished, total, flag.getShow().isEmpty() ? flag.getFlag() : flag.getShow()));
                        latch.countDown();
                    }
                });
            }
            pool.shutdown();
            try {
                latch.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pool.shutdownNow();
        }
        return ranked(lines);
    }

    private static List<RankedLine> ranked(List<RankedLine> lines) {
        List<RankedLine> ranked = new ArrayList<>(lines);
        RankedLine.score(ranked);
        return ranked;
    }

    private static RankedLine inspect(Vod item, boolean builtin, Flag flag, String remarks, String context) {
        Episode episode = flag.find(remarks, false);
        if (episode == null) episode = flag.getEpisodes().get(0);
        try {
            Result result = SiteApi.preloadContent(item.getSiteKey(), flag.getFlag(), episode.getUrl(), builtin);
            SourceQuality.Hit hit = SourceQuality.inspect(result, context);
            if (hit == null) return null;
            String siteName = item.getSiteName();
            if (siteName.isEmpty()) siteName = VodConfig.get().getSite(item.getSiteKey()).getName();
            RankedLine line = new RankedLine(item.getSiteKey(), siteName, item.getId(), item.getName(), item.getPic(), flag, episode, result, hit);
            line.setEpisodes(item.getRemarks(), flag.getEpisodes() == null ? 0 : flag.getEpisodes().size());
            return line;
        } catch (Exception ignored) {
            return null;
        }
    }
}
