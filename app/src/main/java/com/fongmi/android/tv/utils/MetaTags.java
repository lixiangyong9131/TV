package com.fongmi.android.tv.utils;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fongmi.android.tv.R;

public final class MetaTags {

    private MetaTags() {
    }

    public static void fill(ViewGroup box, String... values) {
        if (box == null) return;
        int shown = 0;
        LayoutInflater inflater = null;
        for (String value : values) {
            if (TextUtils.isEmpty(value)) continue;
            String text = value.trim();
            if (text.isEmpty()) continue;
            TextView tag;
            if (shown < box.getChildCount()) {
                tag = (TextView) box.getChildAt(shown);
                if (tag.getVisibility() != View.VISIBLE) tag.setVisibility(View.VISIBLE);
            } else {
                if (inflater == null) inflater = LayoutInflater.from(box.getContext());
                tag = (TextView) inflater.inflate(R.layout.view_meta_tag, box, false);
                box.addView(tag);
            }
            if (!text.contentEquals(tag.getText())) tag.setText(text);
            shown++;
        }
        for (int i = shown; i < box.getChildCount(); i++) {
            View extra = box.getChildAt(i);
            if (extra.getVisibility() != View.GONE) extra.setVisibility(View.GONE);
        }
        int vis = shown == 0 ? View.GONE : View.VISIBLE;
        if (box.getVisibility() != vis) box.setVisibility(vis);
    }
}
