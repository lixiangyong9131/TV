package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.databinding.AdapterTypeBinding;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.ArrayList;
import java.util.List;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.ViewHolder> {

    private static final int LOOP = 200;
    private static final int WINDOW = 3;

    private final OnClickListener listener;
    private final List<Class> mItems;

    public TypeAdapter(OnClickListener listener) {
        this.listener = listener;
        this.mItems = new ArrayList<>();
    }

    public interface OnClickListener {

        void onItemClick(int position, Class item);
    }

    private Class home() {
        Class type = new Class();
        type.setTypeName(ResUtil.getString(R.string.vod_home));
        type.setTypeId("home");
        return type;
    }

    public void setItems(Result result) {
        mItems.clear();
        mItems.addAll(result.getTypes());
        if (!result.getList().isEmpty()) mItems.add(0, home());
        selectItem(0);
        notifyDataSetChanged();
    }

    public int getSelected() {
        for (int i = 0; i < mItems.size(); i++) if (mItems.get(i).isSelected()) return i;
        return 0;
    }

    public void setSelected(int position) {
        setSelected(position, null);
    }

    public void setSelected(int position, RecyclerView list) {
        if (position < 0 || position >= mItems.size() || getSelected() == position) return;
        selectItem(position);
        if (list == null) {
            notifyDataSetChanged();
            return;
        }
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            RecyclerView.ViewHolder raw = list.getChildViewHolder(child);
            if (!(raw instanceof ViewHolder holder)) continue;
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) continue;
            bindState(holder, mItems.get(real(pos)));
        }
    }

    private void selectItem(int position) {
        for (int i = 0; i < mItems.size(); i++) mItems.get(i).setSelected(i == position);
    }

    public Class get(int position) {
        return mItems.get(position);
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
        bindState(holder, item);
        holder.binding.text.setBackgroundResource(0);
        int width = parentWidth(holder.itemView);
        holder.itemView.getLayoutParams().width = Math.max(width / WINDOW, ResUtil.dp2px(72));
        holder.binding.text.setOnClickListener(v -> listener.onItemClick(real(holder.getBindingAdapterPosition()), item));
    }

    private void bindState(ViewHolder holder, Class item) {
        boolean featured = "片单精选".equals(item.getTypeName());
        holder.binding.text.setText(featured ? "" : item.getTypeName());
        boolean selected = !featured && item.isSelected();
        holder.binding.text.setSelected(selected);
        holder.itemView.setSelected(selected);
        holder.binding.text.setTextColor(selected ? 0xFFFFFFFF : 0x73FFFFFF);
    }

    private int parentWidth(android.view.View itemView) {
        ViewGroup parent = (ViewGroup) itemView.getParent();
        int width = parent == null ? 0 : parent.getMeasuredWidth() - parent.getPaddingStart() - parent.getPaddingEnd();
        if (width <= 0) width = ResUtil.getScreenWidth();
        return width;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final AdapterTypeBinding binding;

        ViewHolder(@NonNull AdapterTypeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
