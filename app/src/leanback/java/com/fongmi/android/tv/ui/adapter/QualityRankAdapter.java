package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.AdapterQualityRankBinding;
import com.fongmi.android.tv.playback.vod.RankedLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QualityRankAdapter extends RecyclerView.Adapter<QualityRankAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(RankedLine line);
    }

    private final List<RankedLine> items = new ArrayList<>();
    private final OnClickListener listener;
    private boolean measuring = true;

    public QualityRankAdapter(OnClickListener listener) {
        this.listener = listener;
    }

    public void setMeasuring(boolean measuring) {
        this.measuring = measuring;
        notifyItemRangeChanged(0, items.size());
    }

    public void setItems(List<RankedLine> lines) {
        items.clear();
        if (lines != null) items.addAll(lines);
        notifyDataSetChanged();
    }

    public boolean scored() {
        for (RankedLine line : items) if (line.probed() && line.bytesPerSecond > 0) return true;
        return items.isEmpty();
    }

    public void unlock() {
        setMeasuring(false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterQualityRankBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankedLine line = items.get(position);
        String flag = line.flag == null ? "" : line.flag.getShow().isEmpty() ? line.flag.getFlag() : line.flag.getShow();
        String title = line.vodName.isEmpty() ? flag : line.vodName;
        if (title.isEmpty()) title = line.siteName;
        holder.binding.rank.setText(String.valueOf(position + 1));
        holder.binding.title.setText(title);
        holder.binding.tags.setText(tags(holder, line, flag));
        holder.binding.score.setText(line.probed() ? String.format(Locale.US, "%.0f", line.score) : "");
        boolean ready = !measuring && (line.probed() || !line.siteKey.isEmpty());
        holder.binding.play.setText(measuring ? (line.probed() ? R.string.pick_play_ready : R.string.pick_play_wait) : R.string.pick_play);
        if (position == 0) holder.binding.getRoot().setNextFocusUpId(R.id.pickBest);
        holder.binding.getRoot().setFocusable(ready);
        holder.binding.getRoot().setClickable(ready);
        holder.binding.getRoot().setAlpha(ready || line.probed() ? 1f : 0.7f);
        holder.binding.getRoot().setOnClickListener(view -> {
            if (!ready) return;
            listener.onClick(line);
        });
    }

    private String tags(ViewHolder holder, RankedLine line, String flag) {
        String site = line.siteName == null || line.siteName.isEmpty() ? line.siteKey : line.siteName;
        String speed = speed(line.bytesPerSecond);
        String ping = line.pingMs > 0 ? line.pingMs + " ms" : "";
        String quality = line.probed() ? line.quality : holder.binding.getRoot().getContext().getString(R.string.pick_unscored);
        StringBuilder text = new StringBuilder();
        for (String part : new String[]{site, flag, quality, line.episodes, speed, ping, line.good() ? holder.binding.getRoot().getContext().getString(R.string.pick_good) : ""}) {
            if (part == null || part.isEmpty()) continue;
            if (text.length() > 0) text.append(" · ");
            text.append(part);
        }
        return text.toString();
    }

    private static String speed(long bytesPerSecond) {
        if (bytesPerSecond <= 0) return "";
        if (bytesPerSecond >= 1024 * 1024) return String.format(Locale.US, "%.1f MB/s", bytesPerSecond / 1024.0 / 1024.0);
        return String.format(Locale.US, "%.0f KB/s", bytesPerSecond / 1024.0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final AdapterQualityRankBinding binding;

        ViewHolder(AdapterQualityRankBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
