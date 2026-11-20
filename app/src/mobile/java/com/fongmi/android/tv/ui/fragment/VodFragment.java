package com.fongmi.android.tv.ui.fragment;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Class;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentVodBinding;
import com.fongmi.android.tv.event.CastEvent;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.event.StateEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.impl.ConfigListener;
import com.fongmi.android.tv.impl.FilterListener;
import com.fongmi.android.tv.impl.SiteListener;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.HistoryActivity;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.CategoryPagerAdapter;
import com.fongmi.android.tv.ui.adapter.HomeBannerAdapter;
import com.fongmi.android.tv.ui.adapter.TypeAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.HomeBannerAutoScroll;
import com.fongmi.android.tv.ui.custom.HomeBanners;
import com.fongmi.android.tv.ui.custom.PosterFly;
import com.fongmi.android.tv.ui.custom.StartSnapHelper;
import com.fongmi.android.tv.ui.custom.TypeTabSlider;
import com.fongmi.android.tv.ui.dialog.FilterDialog;
import com.fongmi.android.tv.ui.dialog.LinkDialog;
import com.fongmi.android.tv.ui.dialog.ReceiveDialog;
import com.fongmi.android.tv.ui.dialog.SiteDialog;
import com.fongmi.android.tv.utils.ImgUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class VodFragment extends BaseFragment implements ConfigListener, SiteListener, FilterListener, TypeAdapter.OnClickListener {

    private FragmentVodBinding mBinding;
    private SiteViewModel mViewModel;
    private HomeBannerAdapter mBannerAdapter;
    private HomeBannerAutoScroll mBannerScroll;
    private TypeTabSlider mTypeSlider;
    private TypeAdapter mAdapter;
    private Result mResult;
    private boolean hasBanner;
    private boolean typeBlurApplied;

    public static VodFragment newInstance() {
        return new VodFragment();
    }

    private FolderFragment getFragment() {
        return (FolderFragment) mBinding.pager.getAdapter().instantiateItem(mBinding.pager, mBinding.pager.getCurrentItem());
    }

    private Site getHome() {
        return VodConfig.get().getHome();
    }

    private Config getConfig() {
        return VodConfig.get().getConfig();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentVodBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        mBinding.title.setSelected(false);
        setRecyclerView();
        setViewModel();
        showProgress();
        setTitle();
    }

    @Override
    protected void initEvent() {
        mBinding.top.setOnClickListener(this::onTop);
        mBinding.link.setOnClickListener(this::onLink);
        mBinding.title.setOnClickListener(this::onSite);
        mBinding.filter.setOnClickListener(this::onFilter);
        mBinding.filter.setOnLongClickListener(this::onLink);
        mBinding.toolbar.setOnMenuItemClickListener(this::onMenuItemClick);
        mBinding.title.setPivotX(0);
        mBinding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int range = Math.max(appBarLayout.getTotalScrollRange(), 1);
            float factor = Math.min(1f, Math.abs(verticalOffset) / (float) ResUtil.dp2px(90));
            float collapse = Math.abs(verticalOffset * 1f / range);
            mBinding.toolbar.setBackgroundColor(android.graphics.Color.argb((int) (factor * 217), 0, 0, 0));
            float scale = 1f - 0.04f * factor;
            mBinding.title.setScaleX(scale);
            mBinding.title.setScaleY(scale);
            float tabAlpha = collapse < 0.5f ? 1f - collapse * 0.4f : Math.max(0f, 0.8f - (collapse - 0.5f) * 1.6f);
            mBinding.type.setAlpha(tabAlpha);
        });
        mBinding.pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mAdapter.getRealCount() == 0) return;
                if (mTypeSlider != null) mTypeSlider.followPager(position, positionOffset, mAdapter::real, mAdapter.getRealCount());
                int selected = positionOffset >= 0.25f ? (position + 1) % mAdapter.getRealCount() : position;
                mAdapter.setSelected(selected, mBinding.type);
            }

            @Override
            public void onPageSelected(int position) {
                scrollTypeTo(position);
                mAdapter.setSelected(position, mBinding.type);
                setFabVisible(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state != ViewPager.SCROLL_STATE_IDLE) return;
                mAdapter.setSelected(mBinding.pager.getCurrentItem(), mBinding.type);
                if (mTypeSlider != null) mBinding.type.post(() -> mTypeSlider.followSelected(true));
            }
        });
    }

    private void setRecyclerView() {
        mBinding.type.setHasFixedSize(true);
        mBinding.type.setItemAnimator(null);
        mBinding.type.setAdapter(mAdapter = new TypeAdapter(this));
        if (mBinding.type.getOnFlingListener() == null) new StartSnapHelper().attachToRecyclerView(mBinding.type);
        mTypeSlider = new TypeTabSlider(mBinding.typeSlider, mBinding.type);
        mBannerAdapter = new HomeBannerAdapter(this::openBanner);
        mBannerScroll = new HomeBannerAutoScroll(new HomeBannerAutoScroll.Host() {
            @Override
            public View view() {
                return mBinding == null ? null : mBinding.homeBanner;
            }

            @Override
            public boolean ready() {
                return hasBanner && mBinding != null && mBinding.homeBanner.getVisibility() == View.VISIBLE;
            }

            @Override
            public int current() {
                return mBinding.homeBanner.getCurrentItem();
            }

            @Override
            public int count() {
                return mBannerAdapter.size();
            }

            @Override
            public void show(int position) {
                mBinding.homeBanner.setCurrentItem(position, true);
            }
        });
        mBinding.homeBanner.setAdapter(mBannerAdapter);
        mBinding.homeBanner.setOffscreenPageLimit(1);
        mBinding.homeBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bindTypeBlur(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (mBannerScroll != null) {
                    mBannerScroll.onScrollState(state, ViewPager2.SCROLL_STATE_DRAGGING, ViewPager2.SCROLL_STATE_IDLE);
                }
            }
        });
        setPager();
    }

    private void setPager() {
        mBinding.pager.setAdapter(new PageAdapter(getChildFragmentManager()));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
    }

    private void setAdapter(Result result) {
        setCategories(result);
        bindBanner(result);
        hideProgress();
        showContent();
    }

    private void bindBanner(Result result) {
        List<Vod> items = HomeBanners.pick(result == null ? null : result.getList());
        hasBanner = !items.isEmpty();
        mBannerAdapter.setItems(items);
        if (hasBanner) {
            int start = mBannerAdapter.startPosition();
            mBinding.homeBanner.setCurrentItem(start, false);
            bindTypeBlur(start);
            if (mBannerAdapter.size() > 1) mBannerScroll.start();
            else mBannerScroll.stop();
        } else {
            mBannerScroll.stop();
        }
        setBannerVisible(hasBanner);
    }

    private void setBannerVisible(boolean show) {
        int bar = mBinding.toolbar.getLayoutParams().height;
        if (bar <= 0) bar = ResUtil.dp2px(56);
        ViewGroup.LayoutParams lp = mBinding.homeBannerBox.getLayoutParams();
        lp.height = show ? bar + ResUtil.dp2px(168) : bar + ResUtil.dp2px(48);
        mBinding.homeBannerBox.setLayoutParams(lp);
        ViewGroup.LayoutParams typeBlur = mBinding.typeBlur.getLayoutParams();
        typeBlur.height = lp.height;
        mBinding.typeBlur.setLayoutParams(typeBlur);
        mBinding.homeBanner.setVisibility(show ? View.VISIBLE : View.GONE);
        mBinding.typeBlur.setVisibility(show ? View.VISIBLE : View.GONE);
        mBinding.typeDim.setBackgroundColor(show ? 0x80141416 : 0xB8141416);
        if (!show && mBannerScroll != null) mBannerScroll.stop();
    }

    private void bindTypeBlur(int position) {
        if (mBannerAdapter.isEmpty()) return;
        Vod item = mBannerAdapter.itemAt(position);
        ImgUtil.loadBanner(item.getName(), HomeBanners.pic(item), mBinding.typeBlur);
        applyContactBlur(mBinding.typeBlur);
    }

    private void applyContactBlur(ImageView view) {
        if (typeBlurApplied || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        view.setRenderEffect(RenderEffect.createBlurEffect(18f, 18f, Shader.TileMode.CLAMP));
        typeBlurApplied = true;
    }

    private void scrollTypeTo(int real) {
        RecyclerView.LayoutManager manager = mBinding.type.getLayoutManager();
        int current = manager instanceof LinearLayoutManager ? ((LinearLayoutManager) manager).findFirstVisibleItemPosition() : 0;
        if (current < 0) current = mAdapter.middle();
        mBinding.type.smoothScrollToPosition(mAdapter.nearest(real, current));
    }

    private void openBanner(Vod item, View source) {
        PosterFly.open(requireActivity(), source, item.getName(), item.getPic(), () -> VideoActivity.pick(requireActivity(), item));
    }

    private void setFabVisible(int position) {
        if (mAdapter.getRealCount() == 0) {
            mBinding.link.setVisibility(View.VISIBLE);
            mBinding.filter.setVisibility(View.GONE);
        } else if (!mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.link.setVisibility(View.GONE);
            mBinding.filter.show();
        } else if (position == 0 || mAdapter.get(position).getFilters().isEmpty()) {
            mBinding.filter.setVisibility(View.GONE);
            mBinding.link.show();
        }
    }

    private void setTitle() {
        List<String> items = Arrays.asList(getHome().getName(), getConfig().getName(), getString(R.string.app_name));
        Optional<String> optional = items.stream().filter(s -> !TextUtils.isEmpty(s)).findFirst();
        optional.ifPresent(s -> {
            mBinding.title.setText("");
            mBinding.title.setMinWidth(ResUtil.dp2px(72));
            mBinding.title.setContentDescription(s);
        });
    }

    private void onTop(View view) {
        getFragment().scrollToTop();
        mBinding.appBar.setExpanded(true, true);
        if (mBinding.filter.getVisibility() == View.INVISIBLE) mBinding.filter.show();
        else if (mBinding.link.getVisibility() == View.INVISIBLE) mBinding.link.show();
    }

    private boolean onLink(View view) {
        LinkDialog.show(this);
        return true;
    }

    private void onSite(View view) {
        SiteDialog.create().change().show(this);
    }

    private void onFilter(View view) {
        if (mAdapter.getRealCount() > 0) FilterDialog.create().filter(getFragment().getFilters()).show(this);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            SearchActivity.start(requireActivity());
            return true;
        }
        if (item.getItemId() == R.id.history) {
            HistoryActivity.start(requireActivity());
            return true;
        }
        return true;
    }

    private void showProgress() {
        mBinding.progress.getRoot().setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mBinding.progress.getRoot().setVisibility(View.GONE);
    }

    private void hideContent() {
        mBinding.type.setVisibility(View.INVISIBLE);
        mBinding.pager.setVisibility(View.INVISIBLE);
        setBannerVisible(false);
    }

    private void showContent() {
        mBinding.type.setVisibility(View.VISIBLE);
        mBinding.pager.setVisibility(View.VISIBLE);
        setBannerVisible(hasBanner);
    }

    private void homeContent() {
        showProgress();
        setCategories(Result.empty());
        bindBanner(Result.empty());
        mViewModel.homeContent();
    }

    private void setCategories(Result result) {
        clearPages();
        mAdapter.setItems(mResult = result);
        setPager();
        setFabVisible(0);
        mBinding.type.scrollToPosition(mAdapter.middle());
        mBinding.type.post(() -> {
            if (mTypeSlider != null) mTypeSlider.followSelected(false);
        });
    }

    private void clearPages() {
        for (Fragment fragment : getChildFragmentManager().getFragments()) if (fragment instanceof FilterDialog dialog) dialog.dismissAllowingStateLoss();
        ((PageAdapter) mBinding.pager.getAdapter()).clearPages(mBinding.pager);
    }

    public Result getResult() {
        return mResult == null ? new Result() : mResult;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConfigEvent(ConfigEvent event) {
        if (event.type() == ConfigEvent.Type.VOD) setTitle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshEvent(RefreshEvent event) {
        switch (event.getType()) {
            case HOME:
                setTitle();
            case SIZE:
                homeContent();
                break;
            case CATEGORY:
                if (mAdapter.getRealCount() > 0) getFragment().onRefresh();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHomeContent(RefreshEvent.HomeContent event) {
        Site site = getHome();
        if (event.matchesSite(site.getKey())) mViewModel.refreshHomeContent(site);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStateEvent(StateEvent event) {
        switch (event.type()) {
            case EMPTY:
                hideProgress();
                break;
            case PROGRESS:
                showProgress();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCastEvent(CastEvent event) {
        ReceiveDialog.create().event(event).show(this);
    }

    @Override
    public void setConfig(Config config) {
        VodConfig.load(config, new Callback() {
            @Override
            public void start() {
                showProgress();
                hideContent();
                setTitle();
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
                showContent();
            }
        });
    }

    @Override
    public void setSite(Site item) {
        VodConfig.get().setHome(item);
    }

    @Override
    public void onItemClick(int position, Class item) {
        if (position < 0 || position >= mAdapter.getRealCount() || mAdapter.get(position) != item) return;
        mBinding.pager.setCurrentItem(position);
        mAdapter.setSelected(position, mBinding.type);
    }

    @Override
    public void setFilter(String key, Value value) {
        getFragment().setFilter(key, value);
    }

    @Override
    public boolean canBack() {
        if (mBinding.pager.getAdapter() == null || mBinding.pager.getAdapter().getCount() == 0) return true;
        if (!getFragment().canBack()) return true;
        getFragment().goBack();
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBannerScroll != null && hasBanner && mBannerAdapter != null && mBannerAdapter.size() > 1) mBannerScroll.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBannerScroll != null) mBannerScroll.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBannerScroll != null) mBannerScroll.stop();
        EventBus.getDefault().unregister(this);
    }

    class PageAdapter extends CategoryPagerAdapter {

        public PageAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Class type = mAdapter.get(position);
            return FolderFragment.newInstance(getHome().getKey(), type, 4);
        }

        @Override
        public int getCount() {
            return mAdapter.getRealCount();
        }
    }
}
