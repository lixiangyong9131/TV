package com.fongmi.android.tv.ui.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.leanback.widget.Presenter;

import com.fongmi.android.tv.bean.Hero;
import com.fongmi.android.tv.databinding.AdapterHeroBinding;
import com.fongmi.android.tv.utils.ImgUtil;

/** Renders a full-width Netflix-style hero banner as the first row. */
public class HeroPresenter extends Presenter {

    public interface OnClickListener {
        void onPlay();
    }

    private final OnClickListener listener;
    private Hero current;

    public HeroPresenter(OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Presenter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new ViewHolder(AdapterHeroBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Presenter.ViewHolder viewHolder, Object object) {
        AdapterHeroBinding binding = ((ViewHolder) viewHolder).binding;
        Hero hero = (Hero) object;
        current = hero;
        if (hero.getName() != null) binding.heroTitle.setText(hero.getName());
        if (hero.getDesc() != null) binding.heroDesc.setText(hero.getDesc());
        if (hero.getPic() != null) ImgUtil.load(hero.getName(), hero.getPic(), binding.heroPic);
        binding.heroPlay.setOnClickListener(v -> {
            if (listener != null) listener.onPlay();
        });
        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) listener.onPlay();
        });
    }

    public Hero getCurrent() {
        return current;
    }

    @Override
    public void onUnbindViewHolder(@NonNull Presenter.ViewHolder viewHolder) {
    }

    public static class ViewHolder extends Presenter.ViewHolder {
        private final AdapterHeroBinding binding;

        public ViewHolder(AdapterHeroBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
