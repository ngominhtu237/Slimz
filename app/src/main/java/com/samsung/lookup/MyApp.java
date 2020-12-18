package com.samsung.lookup;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.samsung.lookup.data.engviet.EngVietDbAccess;
import com.samsung.lookup.data.mydb.DictionaryDB;

/**
 * Created by tu.nm1 on 18,December,2020
 */
public class MyApp extends Application {
    private static DictionaryDB dictionaryDB;
    private static EngVietDbAccess engVietDbAccess;

    @Override
    public void onCreate() {
        super.onCreate();
        dictionaryDB = new DictionaryDB(this);
        dictionaryDB.open();

        engVietDbAccess = new EngVietDbAccess(this);
        engVietDbAccess.open();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

    }

    public static DictionaryDB getDictionaryDB() {
        return dictionaryDB;
    }

    public static EngVietDbAccess getEngVietDbAccess() {
        return engVietDbAccess;
    }
}
