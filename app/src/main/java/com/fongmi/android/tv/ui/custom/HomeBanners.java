package com.fongmi.android.tv.ui.custom;

import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.utils.VodScores;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class HomeBanners {

    private HomeBanners() {
    }

    public static String pic(Vod item) {
        if (item == null) return "";
        return first(item.getPicSlide(), item.getDoubanPic(), item.getPic());
    }

    private static String first(String... urls) {
        for (String url : urls) if (url != null && !url.isEmpty()) return url;
        return "";
    }

    public static List<Vod> pick(List<Vod> list) {
        if (list == null || list.isEmpty()) return List.of();
        List<Vod> section = section(list);
        List<Vod> scored = new ArrayList<>();
        for (Vod item : section) if (usable(item) && score(item) > 0) scored.add(item);
        scored.sort(Comparator.comparingDouble(HomeBanners::score).reversed());
        List<Vod> out = new ArrayList<>();
        for (Vod item : scored) {
            if (out.size() >= 5) break;
            out.add(item);
        }
        if (out.size() < 5) {
            for (Vod item : section) {
                if (out.size() >= 5) break;
                if (usable(item) && !contains(out, item)) out.add(item);
            }
        }
        return out;
    }

    private static List<Vod> section(List<Vod> list) {
        int start = headerIndex(list, true, true);
        if (start < 0) start = headerIndex(list, true, false);
        if (start < 0) start = headerIndex(list, false, true);
        if (start < 0) return list;
        List<Vod> out = new ArrayList<>();
        for (int i = start; i < list.size(); i++) {
            if (header(list.get(i))) break;
            out.add(list.get(i));
        }
        return out.isEmpty() ? list : out;
    }

    public static double score(Vod item) {
        String text = VodScores.badge(item);
        if (text == null || text.isEmpty()) return 0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int headerIndex(List<Vod> list, boolean hot, boolean mix) {
        for (int i = 0; i < list.size(); i++) {
            Vod item = list.get(i);
            if (!header(item)) continue;
            String name = item.getName() == null ? "" : item.getName();
            boolean isHot = name.contains("热榜");
            boolean isMix = name.contains("综合");
            if (hot && mix && isHot && isMix) return i + 1;
            if (hot && !mix && isMix) return i + 1;
            if (!hot && mix && isHot) return i + 1;
        }
        return -1;
    }

    private static boolean header(Vod item) {
        return item.getId() != null && item.getId().startsWith("chart-header");
    }

    private static boolean usable(Vod item) {
        return !header(item) && !item.isFolder() && !item.isAction() && item.getName() != null && !item.getName().isEmpty();
    }

    private static boolean contains(List<Vod> list, Vod item) {
        for (Vod vod : list) if (Objects.equals(vod.getSiteKey(), item.getSiteKey()) && Objects.equals(vod.getId(), item.getId())) return true;
        return false;
    }
}
