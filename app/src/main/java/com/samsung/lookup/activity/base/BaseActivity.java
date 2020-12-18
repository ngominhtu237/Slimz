package com.samsung.lookup.activity.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mikepenz.iconics.context.IconicsContextWrapper;

/**
 * Created by tu.nm1 on 18,December,2020
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();

        setContentView(getLayoutId());
        initToolbar();
        loadAd();
    }

    protected void initToolbar() {
    }

    protected void loadAd() {
    }

    protected abstract int getLayoutId();


    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(context));
    }
}
