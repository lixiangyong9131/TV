package com.fongmi.android.tv.ui.custom;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public final class StartSnapHelper extends LinearSnapHelper {

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        RecyclerView parent = (RecyclerView) targetView.getParent();
        if (layoutManager.canScrollHorizontally()) out[0] = targetView.getLeft() - parent.getPaddingLeft();
        return out;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        if (!(layoutManager instanceof LinearLayoutManager)) return super.findSnapView(layoutManager);
        LinearLayoutManager manager = (LinearLayoutManager) layoutManager;
        int first = manager.findFirstVisibleItemPosition();
        if (first == RecyclerView.NO_POSITION) return null;
        View view = manager.findViewByPosition(first);
        if (view == null) return null;
        View next = manager.findViewByPosition(first + 1);
        if (next != null && Math.abs(view.getLeft()) > view.getWidth() / 2) return next;
        return view;
    }
}
