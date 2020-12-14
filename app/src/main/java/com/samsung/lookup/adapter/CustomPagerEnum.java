package com.samsung.lookup.adapter;

import com.samsung.lookup.R;

public enum CustomPagerEnum {

    ENGVIET(R.string.tab_engviet, R.layout.view_engviet),
    TECHNICAL(R.string.tab_technical, R.layout.view_technical),
    SYNONYM(R.string.tab_synonym, R.layout.view_synonym),
    ENGENG(R.string.tab_engeng, R.layout.view_engeng),
    NOTE(R.string.tab_note, R.layout.view_note);

    private int mTitleResId;
    private int mLayoutResId;

    CustomPagerEnum(int titleResId, int layoutResId) {
        mTitleResId = titleResId;
        mLayoutResId = layoutResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getLayoutResId() {
        return mLayoutResId;
    }

}