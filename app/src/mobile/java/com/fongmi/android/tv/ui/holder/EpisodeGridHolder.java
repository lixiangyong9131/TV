package com.fongmi.android.tv.ui.holder;

import androidx.annotation.NonNull;

import com.fongmi.android.tv.bean.Episode;
import com.fongmi.android.tv.databinding.AdapterEpisodeGridBinding;
import com.fongmi.android.tv.ui.adapter.EpisodeAdapter;
import com.fongmi.android.tv.ui.base.BaseEpisodeHolder;

public class EpisodeGridHolder extends BaseEpisodeHolder {

    private final EpisodeAdapter.OnClickListener listener;
    private final AdapterEpisodeGridBinding binding;

    public EpisodeGridHolder(@NonNull AdapterEpisodeGridBinding binding, EpisodeAdapter.OnClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    @Override
    public void initView(Episode item) {
        EpisodeAdapter adapter = getBindingAdapter() instanceof EpisodeAdapter a ? a : null;
        boolean watched = adapter != null && adapter.isWatched(getBindingAdapterPosition());
        binding.text.setSelected(item.isSelected());
        binding.text.setText(item.getLabel());
        binding.watched.setVisibility(!item.isSelected() && watched ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.text.setOnClickListener(v -> listener.onItemClick(item));
    }
}
