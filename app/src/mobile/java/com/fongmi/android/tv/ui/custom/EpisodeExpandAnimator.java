package com.fongmi.android.tv.ui.custom;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class EpisodeExpandAnimator extends DefaultItemAnimator {

    public EpisodeExpandAnimator() {
        setAddDuration(150);
        setSupportsChangeAnimations(false);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        int extra = holder.getLayoutPosition() - 12;
        long delay = Math.max(0, extra) * 50L;
        holder.itemView.animate().cancel();
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(holder.itemView.getResources().getDisplayMetrics().density * 8f);
        dispatchAddStarting(holder);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(getAddDuration())
                .setStartDelay(delay)
                .withEndAction(() -> {
                    holder.itemView.setAlpha(1f);
                    holder.itemView.setTranslationY(0f);
                    dispatchAddFinished(holder);
                })
                .start();
        return true;
    }
}
