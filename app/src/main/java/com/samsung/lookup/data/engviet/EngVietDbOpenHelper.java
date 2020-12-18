package com.samsung.lookup.data.engviet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class EngVietDbOpenHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 3;

    public EngVietDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }
}
