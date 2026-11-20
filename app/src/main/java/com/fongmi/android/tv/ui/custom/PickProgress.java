package com.fongmi.android.tv.ui.custom;

import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.ViewPickProgressBinding;
import com.fongmi.android.tv.playback.vod.PickStatus;
import com.fongmi.android.tv.playback.vod.RankedLine;
import com.fongmi.android.tv.ui.adapter.QualityRankAdapter;

import java.util.List;

public final class PickProgress {

    private static final java.util.WeakHashMap<RecyclerView, Runnable> pending = new java.util.WeakHashMap<>();

    private PickProgress() {
    }

    private static void schedule(ViewPickProgressBinding binding) {
        RecyclerView list = binding.pickList;
        if (pending.containsKey(list)) return;
        Runnable job = () -> {
            pending.remove(list);
            if (!(list.getAdapter() instanceof QualityRankAdapter adapter)) return;
            if (list.getTag() instanceof List<?> lines) adapter.setItems(cast(lines));
        };
        pending.put(list, job);
        list.postDelayed(job, 90);
    }

    @SuppressWarnings("unchecked")
    private static List<RankedLine> cast(List<?> lines) {
        return (List<RankedLine>) lines;
    }

    private static void shine(View view, int[] index) {
        if (view instanceof android.view.ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) shine(group.getChildAt(i), index);
            return;
        }
        if (view.getBackground() != null) view.setBackground(new ShimmerDrawable(index[0]++ * 110L));
    }

    private static void shimmer(View view, boolean run) {
        if (view instanceof android.view.ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++) shimmer(group.getChildAt(i), run);
        }
        if (view.getBackground() instanceof ShimmerDrawable drawable) {
            if (run) drawable.start();
            else drawable.stop();
        }
    }

    public static void show(ViewPickProgressBinding binding, String headline, PickStatus status) {
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.pickTrack.setVisibility(View.VISIBLE);
        binding.pickCount.setVisibility(View.VISIBLE);
        binding.pickDone.setVisibility(View.GONE);
        binding.pickTitle.setText(headline == null || headline.isEmpty() ? binding.getRoot().getContext().getString(R.string.pick_title) : headline);
        binding.pickMessage.setVisibility(View.GONE);
        if (binding.pickList.getVisibility() != View.VISIBLE && binding.pickBones.getVisibility() != View.VISIBLE) {
            binding.pickBones.setVisibility(View.VISIBLE);
            binding.pickBones.setAlpha(1f);
            shine(binding.pickBones, new int[]{0});
            shimmer(binding.pickBones, true);
        }
        boolean measuring = status.phase >= PickStatus.MEASURE && status.phase != PickStatus.FAILED;
        int done = measuring ? status.measureDone : status.searchDone;
        int total = Math.max(measuring ? status.measureTotal : status.searchTotal, 1);
        binding.pickCount.setText(measuring ? binding.getRoot().getContext().getString(R.string.pick_measure_plain, done, total) : binding.getRoot().getContext().getString(R.string.pick_search_count, done, total, status.found));
        binding.pickBar.setPivotX(0f);
        float fraction = total <= 0 ? 0f : Math.max(0.04f, Math.min(1f, done / (float) total));
        binding.pickBar.animate().scaleX(fraction).setDuration(280).start();
    }

    public static void begin(ViewPickProgressBinding binding) {
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter adapter) adapter.setMeasuring(true);
    }

    public static void showList(ViewPickProgressBinding binding, String headline, List<RankedLine> lines, QualityRankAdapter.OnClickListener listener) {
        shimmer(binding.pickBones, false);
        binding.pickBones.setAlpha(1f);
        binding.pickBones.setVisibility(View.GONE);
        binding.getRoot().setVisibility(View.VISIBLE);
        binding.pickTitle.setText(headline == null || headline.isEmpty() ? binding.getRoot().getContext().getString(R.string.pick_title) : headline);
        binding.pickList.setVisibility(View.VISIBLE);
        if (binding.pickList.getLayoutManager() == null) binding.pickList.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
        if (binding.pickList.getItemAnimator() == null) {
            androidx.recyclerview.widget.DefaultItemAnimator animator = new androidx.recyclerview.widget.DefaultItemAnimator();
            animator.setSupportsChangeAnimations(false);
            animator.setMoveDuration(320);
            animator.setAddDuration(220);
            animator.setRemoveDuration(160);
            binding.pickList.setItemAnimator(animator);
        }
        binding.pickList.setTag(lines);
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter) {
            schedule(binding);
            return;
        }
        QualityRankAdapter adapter = new QualityRankAdapter(listener);
        binding.pickList.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(binding.getRoot().getContext(), R.anim.layout_netflix));
        binding.pickList.setAdapter(adapter);
        adapter.setItems(lines);
        if (com.fongmi.android.tv.utils.Util.isLeanback()) binding.pickList.post(() -> {
            if (binding.pickList.getChildCount() > 0) binding.pickList.getChildAt(0).requestFocus();
        });
    }

    public static void complete(ViewPickProgressBinding binding) {
        binding.pickCount.setVisibility(View.GONE);
        binding.pickBar.animate().scaleX(1f).setDuration(220).withEndAction(() -> {
            binding.pickTrack.setVisibility(View.GONE);
            binding.pickDone.setScaleX(0.4f);
            binding.pickDone.setScaleY(0.4f);
            binding.pickDone.setAlpha(0f);
            binding.pickDone.setVisibility(View.VISIBLE);
            binding.pickDone.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(280).start();
        }).start();
        boolean scored = true;
        if (binding.pickList.getAdapter() instanceof QualityRankAdapter adapter) {
            adapter.unlock();
            scored = adapter.scored();
        }
        binding.pickMessage.setVisibility(View.VISIBLE);
        binding.pickMessage.setText(binding.getRoot().getContext().getString(scored ? R.string.pick_list : R.string.pick_try));
    }

    public static void empty(ViewPickProgressBinding binding) {
        shimmer(binding.pickBones, false);
        binding.pickBones.setAlpha(1f);
        binding.pickBones.setVisibility(View.GONE);
        binding.pickDone.setVisibility(View.GONE);
        binding.pickTrack.setVisibility(View.GONE);
        binding.pickCount.setVisibility(View.GONE);
        binding.pickMessage.setVisibility(View.VISIBLE);
        binding.pickMessage.setText(binding.getRoot().getContext().getString(R.string.pick_empty));
    }

    public static void hide(ViewPickProgressBinding binding) {
        Runnable job = pending.remove(binding.pickList);
        if (job != null) binding.pickList.removeCallbacks(job);
        shimmer(binding.pickBones, false);
        binding.pickBones.setAlpha(1f);
        binding.pickBones.setVisibility(View.GONE);
        binding.pickDone.setVisibility(View.GONE);
        binding.pickTrack.setVisibility(View.GONE);
        binding.pickList.setAdapter(null);
        binding.pickList.setVisibility(View.GONE);
        binding.getRoot().setVisibility(View.GONE);
    }
}
