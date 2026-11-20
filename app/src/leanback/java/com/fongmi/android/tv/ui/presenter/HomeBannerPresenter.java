package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.HomeBanner;
import com.fongmi.android.tv.ui.adapter.HomeBannerAdapter;
import com.fongmi.android.tv.utils.ResUtil;

public class HomeBannerPresenter extends Presenter {

    private final HomeBannerAdapter adapter;

    public HomeBannerPresenter(HomeBannerAdapter.OnClickListener listener) {
        this.adapter = new HomeBannerAdapter(listener);
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_home_banner, parent, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ResUtil.dp2px(168)));
        return new Presenter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        HomeBanner banner = (HomeBanner) object;
        adapter.setItems(banner.getItems());
        if (adapter.getItemCount() == 0) return;
        HomeBannerAdapter.Holder holder = new HomeBannerAdapter.Holder(viewHolder.view);
        adapter.onBindViewHolder(holder, adapter.startPosition());
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
    }
}
