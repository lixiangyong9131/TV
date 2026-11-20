package com.fongmi.android.tv.bean;

import java.util.List;

public class HomeBanner {

    private final List<Vod> items;

    public HomeBanner(List<Vod> items) {
        this.items = items;
    }

    public List<Vod> getItems() {
        return items;
    }
}
