package com.fongmi.android.tv.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.AdapterQualityRankBinding;
import com.fongmi.android.tv.playback.vod.RankedLine;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.MetaTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QualityRankAdapter extends RecyclerView.Adapter<QualityRankAdapter.ViewHolder> {

    public interface OnClickListener {
        void onClick(RankedLine line);
    }

    private static final int PLAY_WAIT = 1;
    private static final int PLAY_OK = 2;
    private static final int PLAY_NONE = 3;
    private static final int PLAY_READY = 4;
    private static final int PLAY_OPEN = 5;

    private final List<RankedLine> items = new ArrayList<>();
    private final OnClickListener listener;
    private boolean measuring = true;
    private boolean opening;
    private RankedLine openingLine;

    public QualityRankAdapter(OnClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setMeasuring(boolean measuring) {
        if (this.measuring == measuring && !opening) return;
        this.measuring = measuring;
        if (measuring) {
            opening = false;
            openingLine = null;
        }
        notifyItemRangeChanged(0, items.size(), "state");
    }

    public void lock(RankedLine line) {
        if (opening) return;
        opening = true;
        openingLine = line;
        notifyItemRangeChanged(0, items.size(), "state");
    }

    public void unlock() {
        measuring = false;
        opening = false;
        openingLine = null;
        notifyItemRangeChanged(0, items.size(), "state");
    }

    public void setItems(List<RankedLine> lines) {
        List<RankedLine> next = lines == null ? List.of() : new ArrayList<>(lines);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                return same(items.get(oldPosition), next.get(newPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                return sameContent(items.get(oldPosition), next.get(newPosition)) && oldPosition == newPosition;
            }

            @Override
            public Object getChangePayload(int oldPosition, int newPosition) {
                RankedLine left = items.get(oldPosition);
                RankedLine right = next.get(newPosition);
                if (Objects.equals(left.pic, right.pic) && Objects.equals(left.vodName, right.vodName) && Objects.equals(left.episodes, right.episodes)) return "rank";
                return null;
            }
        });
        items.clear();
        items.addAll(next);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        RankedLine line = items.get(position);
        return Objects.hash(line.siteKey, line.vodId);
    }

    private static boolean same(RankedLine left, RankedLine right) {
        return Objects.equals(left.siteKey, right.siteKey) && Objects.equals(left.vodId, right.vodId);
    }

    private static boolean sameContent(RankedLine left, RankedLine right) {
        return left.probed() == right.probed() && left.speedKind == right.speedKind && Double.compare(left.score, right.score) == 0 && left.bytesPerSecond == right.bytesPerSecond && left.pingMs == right.pingMs && Objects.equals(left.quality, right.quality) && Objects.equals(left.episodes, right.episodes) && Objects.equals(left.vodName, right.vodName);
    }

    public boolean scored() {
        for (RankedLine line : items) if (line.probed()) return true;
        return items.isEmpty();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(AdapterQualityRankBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            RankedLine line = items.get(position);
            boolean rank = false;
            for (Object payload : payloads) if ("rank".equals(payload)) rank = true;
            if (rank) bindScore(holder, line, position);
            bindPlay(holder, line, true);
            return;
        }
        onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RankedLine line = items.get(position);
        String flag = line.flag == null ? "" : line.flag.getShow().isEmpty() ? line.flag.getFlag() : line.flag.getShow();
        String title = line.vodName.isEmpty() ? flag : line.vodName;
        holder.binding.rank.setText(String.valueOf(position + 1));
        holder.binding.rank.setTextColor(ContextCompat.getColor(holder.binding.getRoot().getContext(), rankColor(position)));
        holder.binding.title.setText(title);
        holder.binding.episodes.setText(line.episodes);
        holder.binding.episodes.setVisibility(line.episodes == null || line.episodes.isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        holder.binding.getRoot().setSelected(opening && same(line, openingLine));
        bindScore(holder, line, position);
        bindPlay(holder, line, false);
        ImgUtil.load(title, line.pic, holder.binding.poster);
        holder.binding.getRoot().setOnClickListener(view -> {
            if (measuring || opening) return;
            lock(line);
            view.setSelected(true);
            listener.onClick(line);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int playState(RankedLine line) {
        if (opening && same(line, openingLine)) return PLAY_OPEN;
        if (!measuring) return PLAY_READY;
        if (line.speedKind == RankedLine.SPEED_OK || (line.probed() && line.bytesPerSecond > 0)) return PLAY_OK;
        if (line.speedKind == RankedLine.SPEED_NONE || line.probed()) return PLAY_NONE;
        return PLAY_WAIT;
    }

    private void bindPlay(ViewHolder holder, RankedLine line, boolean animate) {
        boolean chosen = opening && same(line, openingLine);
        boolean locked = measuring || opening;
        holder.binding.getRoot().setClickable(!locked);
        holder.binding.getRoot().setFocusable(!locked || chosen);
        holder.binding.getRoot().setAlpha(opening && !chosen ? 0.55f : 1f);
        int state = playState(line);
        boolean white = state == PLAY_READY || state == PLAY_OPEN;
        int ink = white ? 0xFF000000 : 0xFFFFFFFF;
        holder.binding.playBox.setBackgroundResource(white ? R.drawable.bg_netflix_play : R.drawable.bg_netflix_secondary);
        holder.binding.play.setTextColor(ink);
        holder.binding.play.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(ink));
        holder.binding.playWait.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(ink));
        holder.binding.play.setAlpha(state == PLAY_NONE ? 0.85f : 1f);
        if (state == PLAY_WAIT) {
            holder.binding.playWait.setVisibility(View.VISIBLE);
            holder.binding.play.setText(R.string.pick_play_wait);
            holder.binding.play.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else if (state == PLAY_OK) {
            holder.binding.playWait.setVisibility(View.GONE);
            holder.binding.play.setText(R.string.pick_play_ready);
            holder.binding.play.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pick_check, 0, 0, 0);
        } else if (state == PLAY_NONE) {
            holder.binding.playWait.setVisibility(View.GONE);
            holder.binding.play.setText(R.string.pick_play_none);
            holder.binding.play.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else if (state == PLAY_OPEN) {
            holder.binding.playWait.setVisibility(View.VISIBLE);
            holder.binding.play.setText(R.string.pick_opening);
            holder.binding.play.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            holder.binding.playWait.setVisibility(View.GONE);
            holder.binding.play.setText(R.string.pick_play);
            holder.binding.play.setCompoundDrawablesRelativeWithIntrinsicBounds(androidx.media3.ui.R.drawable.exo_icon_play, 0, 0, 0);
        }
        Object prev = holder.binding.playBox.getTag();
        holder.binding.playBox.setTag(state);
        if (animate && prev instanceof Integer && (Integer) prev != state) popPlay(holder.binding.playBox, state == PLAY_READY);
    }

    private static void popPlay(View box, boolean toPlay) {
        box.animate().cancel();
        box.setPivotX(Math.max(box.getWidth(), 1) / 2f);
        box.setPivotY(Math.max(box.getHeight(), 1) / 2f);
        box.setScaleX(toPlay ? 0.86f : 0.94f);
        box.setScaleY(toPlay ? 0.86f : 0.94f);
        box.setAlpha(0.55f);
        box.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(toPlay ? 280 : 200).setInterpolator(toPlay ? new OvershootInterpolator(1.4f) : new DecelerateInterpolator()).start();
    }

    private static void bindScore(ViewHolder holder, RankedLine line, int position) {
        String flag = line.flag == null ? "" : line.flag.getShow().isEmpty() ? line.flag.getFlag() : line.flag.getShow();
        String title = line.vodName.isEmpty() ? flag : line.vodName;
        holder.binding.rank.setText(String.valueOf(position + 1));
        holder.binding.rank.setTextColor(ContextCompat.getColor(holder.binding.getRoot().getContext(), rankColor(position)));
        String site = line.siteName == null || line.siteName.isEmpty() ? line.siteKey : line.siteName;
        if (site != null && site.equals(title)) site = "";
        String flagTag = flag.isEmpty() || flag.equals(title) ? "" : flag;
        if (line.probed()) {
            holder.binding.scoreBox.setVisibility(View.VISIBLE);
            holder.binding.score.setText(String.format(Locale.US, "%.0f", line.score));
            MetaTags.fill(holder.binding.tags, line.quality, flagTag, speed(line.bytesPerSecond), line.pingMs > 0 ? line.pingMs + " ms" : "", site, line.good() ? holder.binding.getRoot().getContext().getString(R.string.pick_good) : "");
        } else {
            holder.binding.scoreBox.setVisibility(View.GONE);
            MetaTags.fill(holder.binding.tags, site, holder.binding.getRoot().getContext().getString(R.string.pick_unscored));
        }
    }

    private static int rankColor(int position) {
        if (position == 0) return R.color.pick_rank_1;
        if (position == 1) return R.color.pick_rank_2;
        if (position == 2) return R.color.pick_rank_3;
        return R.color.white;
    }

    private static String speed(long bytesPerSecond) {
        if (bytesPerSecond <= 0) return "";
        if (bytesPerSecond >= 1024 * 1024) return String.format(Locale.US, "%.1f MB/s", bytesPerSecond / 1024.0 / 1024.0);
        return String.format(Locale.US, "%.0f KB/s", bytesPerSecond / 1024.0);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final AdapterQualityRankBinding binding;

        ViewHolder(AdapterQualityRankBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
