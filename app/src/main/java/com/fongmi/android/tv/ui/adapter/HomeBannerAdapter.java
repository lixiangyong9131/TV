package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.ui.custom.BannerScrimDrawable;
import com.fongmi.android.tv.ui.custom.HomeBanners;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.VodScores;

import java.util.ArrayList;
import java.util.List;

public class HomeBannerAdapter extends RecyclerView.Adapter<HomeBannerAdapter.Holder> {

    private static final int LOOP = 1000;

    public interface OnClickListener {
        void onBannerClick(Vod item, View source);
    }

    private final List<Vod> items = new ArrayList<>();
    private final OnClickListener listener;

    public HomeBannerAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Vod> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public int startPosition() {
        if (items.size() <= 1) return 0;
        return (LOOP / 2) * items.size();
    }

    public Vod itemAt(int position) {
        return items.get(position % items.size());
    }

    private boolean looping() {
        return items.size() > 1;
    }

    private int rankAt(int position) {
        return position % items.size() + 1;
    }

    @Override
    public int getItemCount() {
        if (items.isEmpty()) return 0;
        return looping() ? items.size() * LOOP : items.size();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_home_banner, parent, false);
        if (!(parent.getParent() instanceof ViewPager2)) {
            int width = parent.getMeasuredWidth();
            if (width <= 0) width = parent.getResources().getDisplayMetrics().widthPixels;
            int height = parent.getMeasuredHeight();
            if (height <= 0) height = ResUtil.dp2px(248);
            view.setLayoutParams(new RecyclerView.LayoutParams(width, height));
        }
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Vod item = itemAt(position);
        holder.name.setText(item.getName());
        holder.topBadge.setText(holder.itemView.getContext().getString(R.string.home_banner_top_n, rankAt(position)));
        ImgUtil.loadBanner(item.getName(), HomeBanners.pic(item), holder.back);
        VodScores.bind(holder.scoreBadge, item);
        holder.itemView.setOnClickListener(v -> listener.onBannerClick(item, holder.back));
    }

    public static final class Holder extends RecyclerView.ViewHolder {
        final ImageView back;
        final View scrim;
        final TextView topBadge;
        final TextView scoreBadge;
        final TextView name;

        public Holder(View itemView) {
            super(itemView);
            back = itemView.findViewById(R.id.bannerBack);
            scrim = itemView.findViewById(R.id.bannerScrim);
            topBadge = itemView.findViewById(R.id.bannerTopBadge);
            scoreBadge = itemView.findViewById(R.id.bannerScoreBadge);
            name = itemView.findViewById(R.id.bannerName);
            if (scrim.getBackground() == null) scrim.setBackground(new BannerScrimDrawable());
        }
    }
}
