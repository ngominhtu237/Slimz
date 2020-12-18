package com.tunm.slimz.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by tu.nm1 on 15,December,2020
 */
public class RelativeLayoutCustom extends RelativeLayout {
    public RelativeLayoutCustom(Context context) {
        super(context);
        init();
    }

    public RelativeLayoutCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RelativeLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RelativeLayoutCustom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    void init() {

    }
}
