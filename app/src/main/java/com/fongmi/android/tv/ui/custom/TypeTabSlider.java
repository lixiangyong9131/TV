package com.fongmi.android.tv.ui.custom;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.function.IntUnaryOperator;

public final class TypeTabSlider {

    private final View slider;
    private final RecyclerView list;
    private final int inset;
    private ValueAnimator animator;
    private float currentX;
    private float currentW;
    private boolean placed;

    public TypeTabSlider(View slider, RecyclerView list) {
        this.slider = slider;
        this.list = list;
        this.inset = 0;
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                followSelected(false);
            }
        });
        list.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> followSelected(false));
    }

    public void followPager(int real, float offset, IntUnaryOperator adapterPosToReal, int realCount) {
        if (realCount <= 0) return;
        View from = childForReal(real, adapterPosToReal);
        View to = childForReal((real + 1) % realCount, adapterPosToReal);
        if (from == null) {
            followSelected(false);
            return;
        }
        float x = from.getX();
        float w = from.getWidth();
        if (to != null && offset > 0f) {
            x += (to.getX() - from.getX()) * offset;
            w += (to.getWidth() - from.getWidth()) * offset;
        }
        place(x, w, false);
    }

    public void followSelected(boolean animate) {
        View target = findSelected();
        if (target == null) {
            if (!placed) slider.setVisibility(View.INVISIBLE);
            return;
        }
        place(target.getX(), target.getWidth(), animate);
    }

    public void moveTo(View target, boolean animate) {
        if (target == null) return;
        place(target.getX(), target.getWidth(), animate);
    }

    private View findSelected() {
        View first = null;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (first == null) first = child;
            if (isSelected(child)) return child;
        }
        return first;
    }

    private boolean isSelected(View child) {
        if (child.isSelected() || child.isFocused()) return true;
        View text = child.findViewById(R.id.text);
        return text != null && (text.isSelected() || text.isFocused());
    }

    private View childForReal(int real, IntUnaryOperator adapterPosToReal) {
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            RecyclerView.ViewHolder holder = list.getChildViewHolder(child);
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && adapterPosToReal.applyAsInt(pos) == real) return child;
        }
        return null;
    }

    private void place(float x, float w, boolean animate) {
        float left = x + inset;
        float width = Math.max(w - inset * 2f, ResUtil.dp2px(48));
        if (placed && Math.abs(left - currentX) < 0.5f && Math.abs(width - currentW) < 0.5f) return;
        slider.setVisibility(View.VISIBLE);
        if (!placed || !animate) {
            if (animator != null) animator.cancel();
            apply(left, width);
            placed = true;
            return;
        }
        if (Math.abs(left - currentX) < 1f && Math.abs(width - currentW) < 1f) return;
        if (animator != null) animator.cancel();
        float startX = currentX;
        float startW = currentW;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(280);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(a -> {
            float f = (float) a.getAnimatedValue();
            apply(startX + (left - startX) * f, startW + (width - startW) * f);
        });
        animator.start();
    }

    private void apply(float x, float w) {
        currentX = x;
        currentW = w;
        slider.setTranslationX(x);
        ViewGroup.LayoutParams lp = slider.getLayoutParams();
        int width = Math.round(w);
        if (lp.width != width) {
            lp.width = width;
            slider.setLayoutParams(lp);
        }
    }
}
