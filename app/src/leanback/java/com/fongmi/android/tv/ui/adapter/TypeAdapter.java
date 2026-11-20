package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Cache;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.databinding.AdapterTypeBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder> {

    private static final int LOOP = 200;
    private static final int WINDOW = 3;

    private final OnClickListener mListener;
    private final List<Class> mItems;

    public TypeAdapter(OnClickListener listener) {
        mListener = listener;
        mItems = new ArrayList<>();
    }

    public void addAll(List<Class> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public Class get(int position) {
        return mItems.get(position);
    }

    public int indexOf(Class item) {
        return mItems.indexOf(item);
    }

    public int getRealCount() {
        return mItems.size();
    }

    public boolean looping() {
        return mItems.size() >= WINDOW;
    }

    public int middle() {
        return looping() ? (LOOP / 2) * mItems.size() : 0;
    }

    public int real(int adapterPos) {
        int n = mItems.size();
        if (n == 0) return 0;
        int mod = adapterPos % n;
        return mod < 0 ? mod + n : mod;
    }

    public int nearest(int real, int currentAdapter) {
        if (!looping()) return real;
        int n = mItems.size();
        int base = currentAdapter - real(currentAdapter);
        int pos = base + real;
        if (pos - currentAdapter > n / 2) pos -= n;
        if (currentAdapter - pos > n / 2) pos += n;
        return pos;
    }

    @Override
    public int getItemCount() {
        int n = mItems.size();
        if (n == 0) return 0;
        return looping() ? n * LOOP : n;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Class item = mItems.get(real(position));
        holder.binding.text.setText(item.getTypeName());
        holder.binding.text.setCompoundDrawablePadding(ResUtil.dp2px(4));
        holder.binding.text.setCompoundDrawablesWithIntrinsicBounds(0, 0, getIcon(item), 0);
        holder.binding.text.setListener(() -> mListener.onRefresh(item));
        holder.binding.getRoot().setOnClickListener(v -> mListener.onItemClick(item));
        int width = parentWidth(holder.itemView);
        holder.itemView.getLayoutParams().width = Math.max(width / WINDOW, ResUtil.dp2px(96));
    }

    private int parentWidth(android.view.View itemView) {
        ViewGroup parent = (ViewGroup) itemView.getParent();
        int width = parent == null ? 0 : parent.getMeasuredWidth() - parent.getPaddingStart() - parent.getPaddingEnd();
        if (width <= 0) width = ResUtil.getScreenWidth();
        return width;
    }

    private int getIcon(Class item) {
        return Cache.get(item).isEmpty() ? 0 : item.getFilter() ? R.drawable.ic_vod_filter_off : R.drawable.ic_vod_filter_on;
    }

    public interface OnClickListener {

        void onItemClick(Class item);

        void onRefresh(Class item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterTypeBinding binding;

        ViewHolder(@NonNull AdapterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
