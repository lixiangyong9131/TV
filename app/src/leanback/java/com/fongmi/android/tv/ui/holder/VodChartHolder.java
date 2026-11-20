package com.fongmi.android.tv.ui.holder;

import android.view.View;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterVodChartBinding;
import com.fongmi.android.tv.ui.base.BaseVodHolder;
import com.fongmi.android.tv.ui.presenter.VodPresenter;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.VodScores;

public class VodChartHolder extends BaseVodHolder {

    private final VodPresenter.OnClickListener listener;
    private final AdapterVodChartBinding binding;

    public VodChartHolder(@NonNull AdapterVodChartBinding binding, VodPresenter.OnClickListener listener) {
        super(binding.getRoot());
        this.binding = binding;
        this.listener = listener;
    }

    @Override
    public void initView(Vod item) {
        boolean header = item.getId() != null && item.getId().startsWith("chart-header");
        binding.rank.setVisibility(header ? View.GONE : View.VISIBLE);
        binding.image.setVisibility(header ? View.GONE : View.VISIBLE);
        binding.remark.setVisibility(header ? View.GONE : View.VISIBLE);
        binding.name.setText(item.getName());
        binding.getRoot().setFocusable(!header);
        binding.getRoot().setFocusableInTouchMode(!header);
        if (header) {
            binding.score.setVisibility(View.GONE);
            binding.getRoot().setOnClickListener(null);
            binding.getRoot().setOnLongClickListener(null);
            return;
        }
        binding.rank.setText(item.getYear());
        VodScores.bind(binding.score, item);
        VodScores.bindRemark(binding.remark, item.getRemarks());
        binding.getRoot().setOnClickListener(v -> listener.onItemClick(item, binding.image));
        binding.getRoot().setOnLongClickListener(v -> listener.onLongClick(item));
        ImgUtil.load(item.getName(), item.getPic(), binding.image);
    }

    @Override
    public void unbind() {
        Glide.with(binding.image).clear(binding.image);
    }
}
