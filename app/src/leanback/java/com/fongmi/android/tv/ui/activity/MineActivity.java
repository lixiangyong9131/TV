package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.Updater;
import com.fongmi.android.tv.databinding.ActivityMineBinding;
import com.fongmi.android.tv.ui.base.BaseActivity;

public class MineActivity extends BaseActivity {

    private ActivityMineBinding mBinding;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, MineActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityMineBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.versionText.setText(BuildConfig.VERSION_NAME);
        mBinding.setting.requestFocus();
    }

    @Override
    protected void initEvent() {
        mBinding.setting.setOnClickListener(view -> SettingActivity.start(this));
        mBinding.version.setOnClickListener(view -> Updater.get().start(this, true));
    }
}
