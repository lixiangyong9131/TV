package com.fongmi.android.tv.ui.custom;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ViewPickProgressBinding;
import com.fongmi.android.tv.playback.vod.PickStatus;
import com.fongmi.android.tv.playback.vod.RankedLine;
import com.fongmi.android.tv.ui.adapter.QualityRankAdapter;

import java.util.List;

public final class PickProgress {

    private PickProgress() {
    }

    public static void begin(ViewPickProgressBinding binding) {
        binding.pickBest.setVisibility(View.GONE);
        binding.pickBest.setOnClickListener(null);
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter adapter) adapter.setMeasuring(true);
    }

    public static void show(ViewPickProgressBinding binding, String headline, PickStatus status) {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.getRoot().setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);
        if (!binding.pickBest.isShown() && binding.getRoot().findFocus() == null) binding.getRoot().requestFocus();
        binding.pickTrack.setVisibility(View.VISIBLE);
        binding.pickCount.setVisibility(View.VISIBLE);
        binding.pickTitle.setText(headline == null || headline.isEmpty() ? binding.getRoot().getContext().getString(R.string.pick_title) : headline);
        boolean measuring = status.phase >= PickStatus.MEASURE && status.phase != PickStatus.FAILED;
        int done = measuring ? status.measureDone : status.searchDone;
        int total = Math.max(measuring ? status.measureTotal : status.searchTotal, 1);
        binding.pickCount.setText(measuring ? binding.getRoot().getContext().getString(R.string.pick_measure_plain, done, total) : binding.getRoot().getContext().getString(R.string.pick_search_count, done, total, status.found));
        binding.pickBar.setPivotX(0f);
        float fraction = Math.max(0.04f, Math.min(1f, done / (float) total));
        binding.pickBar.animate().scaleX(fraction).setDuration(280).start();
    }

    public static void showList(ViewPickProgressBinding binding, String headline, List<RankedLine> lines, QualityRankAdapter.OnClickListener listener) {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.pickTitle.setText(headline == null || headline.isEmpty() ? binding.getRoot().getContext().getString(R.string.pick_title) : headline);
        binding.pickList.setVisibility(View.VISIBLE);
        if (binding.pickList.getLayoutManager() == null) binding.pickList.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        QualityRankAdapter adapter;
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter existing) {
            adapter = existing;
        } else {
            adapter = new QualityRankAdapter(listener);
            binding.pickList.setAdapter(adapter);
        }
        adapter.setItems(lines);
        binding.pickBest.setOnClickListener(view -> {
            RankedLine best = best(lines);
            if (best != null) listener.onClick(best);
        });
    }

    public static void complete(ViewPickProgressBinding binding) {
        binding.pickBar.animate().scaleX(1f).setDuration(220).start();
        boolean scored = true;
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter adapter) {
            adapter.unlock();
            scored = adapter.scored();
        }
        binding.pickMessage.setVisibility(View.VISIBLE);
        binding.pickMessage.setText(binding.getRoot().getContext().getString(scored ? R.string.pick_list : R.string.pick_try));
        binding.getRoot().setFocusable(false);
        binding.pickBest.setVisibility(View.VISIBLE);
        binding.pickBest.post(binding.pickBest::requestFocus);
    }

    public static void empty(ViewPickProgressBinding binding) {
        binding.pickTrack.setVisibility(View.GONE);
        binding.pickCount.setVisibility(View.GONE);
        binding.pickBest.setVisibility(View.GONE);
        binding.pickMessage.setVisibility(View.VISIBLE);
        binding.pickMessage.setText(binding.getRoot().getContext().getString(R.string.pick_empty));
    }

    public static void hide(ViewPickProgressBinding binding) {
        binding.pickList.setAdapter(null);
        binding.pickList.setVisibility(View.GONE);
        binding.pickBest.setVisibility(View.GONE);
        binding.pickMessage.setVisibility(View.GONE);
        binding.getRoot().setVisibility(View.GONE);
    }

    private static RankedLine best(List<RankedLine> lines) {
        if (lines == null || lines.isEmpty()) return null;
        for (RankedLine line : lines) if (line.probed()) return line;
        return lines.get(0);
    }
}
