package com.fongmi.android.tv.ui.fragment;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.Updater;
import com.fongmi.android.tv.databinding.FragmentMineBinding;
import com.fongmi.android.tv.ui.activity.HomeActivity;
import com.fongmi.android.tv.ui.base.BaseFragment;

public class MineFragment extends BaseFragment {

    private FragmentMineBinding mBinding;

    public static MineFragment newInstance() {
        return new MineFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentMineBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.versionText.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void initEvent() {
        mBinding.setting.setOnClickListener(view -> {
            if (requireActivity() instanceof HomeActivity home) home.change(1);
        });
        mBinding.version.setOnClickListener(view -> Updater.get().start(requireActivity(), true));
    }
}
