package com.samsung.lookup.data.mydb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.samsung.lookup.data.Constant;
import com.samsung.lookup.model.WorkMark;

import java.util.ArrayList;

public class DictionaryDB extends SQLiteOpenHelper {

    private static DictionaryDB mInstance = null;

    private static final String TAG = "SaveDB";
    private static final String DATABASE_NAME = "word.db";
    private static final int DATABASE_VERSION = 5;
    private SQLiteDatabase mSQLiteDatabase;

    public DictionaryDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Approach #2
    public static DictionaryDB getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DictionaryDB(context.getApplicationContext());
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WORD_HISTORY_TABLE = "CREATE TABLE "
                + Constant.TABLE_WORD_HISTORY
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constant.COLUMN_WORDNAME_HISTORY + " TEXT, "
                + Constant.COLUMN_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

        String CREATE_WORD_FAVORITE_TABLE = "CREATE TABLE "
                + Constant.TABLE_WORD_FAVORITE
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Constant.COLUMN_WORDNAME_FAVORITE + " TEXT, "
                + Constant.COLUMN_FAVORITE_COLOR + " TEXT, "
                + Constant.COLUMN_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
        db.execSQL(CREATE_WORD_HISTORY_TABLE);
        db.execSQL(CREATE_WORD_FAVORITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constant.TABLE_WORD_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + Constant.TABLE_WORD_FAVORITE);
        onCreate(db);
    }

    // to open the database
    public void open(){
        mSQLiteDatabase = this.getWritableDatabase();
    }

    // close the database connection
    public void close(){
        if(mSQLiteDatabase != null) {
            this.mSQLiteDatabase.close();
        }
    }

    public void addHistoryWord(Context context, String wordName) {
        // Check if word already exits in DB
        Cursor c = mSQLiteDatabase.rawQuery("SELECT " + Constant.COLUMN_WORDNAME_HISTORY + " FROM " + Constant.TABLE_WORD_HISTORY + " WHERE " + Constant.COLUMN_WORDNAME_HISTORY + "=\'" + wordName + "\'", null);
        if(c.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Constant.COLUMN_WORDNAME_HISTORY, wordName);
            if(wordName != null) {
                long rowInserted = mSQLiteDatabase.insert(Constant.TABLE_WORD_HISTORY, null, values);
            }
//            if (rowInserted != -1)
//                Toast.makeText(context, "New row added, row id: " + rowInserted, Toast.LENGTH_SHORT).show();
//            else
//                Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show();
        } else {
            // nếu đã có rồi thì update (TIMESTAMP)
            mSQLiteDatabase.execSQL("UPDATE " + Constant.TABLE_WORD_HISTORY + " SET " + Constant.COLUMN_TIME + " = CURRENT_TIMESTAMP" + " WHERE " + Constant.COLUMN_WORDNAME_HISTORY + "=\'" + wordName + "\'");
        }
    }

    public ArrayList<String> getHistoryWord(int limit) {
        ArrayList<String> mHistoryWord = new ArrayList<>();
        Cursor c = mSQLiteDatabase.rawQuery("SELECT " + Constant.COLUMN_WORDNAME_HISTORY + " FROM " + Constant.TABLE_WORD_HISTORY + " ORDER BY " + Constant.COLUMN_TIME + " DESC " + "LIMIT " + limit, null);
        if(c.moveToFirst()) {
            do {
                mHistoryWord.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        return mHistoryWord;
    }

    public void deleteHistoryWord(Context context, String wordName) {
        int affectedRow = 0;
        try {
            affectedRow = mSQLiteDatabase.delete(Constant.TABLE_WORD_HISTORY, Constant.COLUMN_WORDNAME_HISTORY + " = ?",
                    new String[]{wordName});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Deleteing last row" + e.toString());
        }
        if (affectedRow == 1)
            Toast.makeText(context, "Delete successful!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show();
    }

    public void addFavoriteWord(Context context, String wordName, String color) {
        Cursor c = mSQLiteDatabase.rawQuery("SELECT " + Constant.COLUMN_WORDNAME_FAVORITE + " FROM " + Constant.TABLE_WORD_FAVORITE + " WHERE " + Constant.COLUMN_WORDNAME_FAVORITE + "=\'" + wordName + "\'", null);
        if(c.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Constant.COLUMN_WORDNAME_FAVORITE, wordName);
            values.put(Constant.COLUMN_FAVORITE_COLOR, color);
            if(wordName != null) {
                long rowInserted = mSQLiteDatabase.insert(Constant.TABLE_WORD_FAVORITE, null, values);
            }
        } else {
            // nếu đã có rồi thì update (TIMESTAMP)
            mSQLiteDatabase.execSQL("UPDATE " + Constant.TABLE_WORD_FAVORITE + " SET " + Constant.COLUMN_TIME + " = CURRENT_TIMESTAMP, " + Constant.COLUMN_FAVORITE_COLOR + " =\'" + color + "\'" + " WHERE " + Constant.COLUMN_WORDNAME_FAVORITE + "=\'" + wordName + "\'");
        }
    }

    public String getColorFavoriteWordByName(Context context, String wordName) {
        String color = null;
        Cursor c = mSQLiteDatabase.rawQuery("SELECT " + Constant.COLUMN_FAVORITE_COLOR + " FROM " + Constant.TABLE_WORD_FAVORITE + " WHERE " + Constant.COLUMN_WORDNAME_FAVORITE + "=\'" + wordName + "\'", null);
        if(c.getCount() > 0) {
            if(c.moveToFirst()) {
                do {
                    color = c.getString(0);
                } while (c.moveToNext());
            }
            return color;
        } else {
            return "noColor";
        }
    }

    public ArrayList<WorkMark> getFavoriteWordByColor(Context context, String color) {
        ArrayList<WorkMark> mArrWorkMark = new ArrayList<>();
        Cursor c = mSQLiteDatabase.rawQuery("SELECT * FROM " + Constant.TABLE_WORD_FAVORITE + " WHERE " + Constant.COLUMN_FAVORITE_COLOR + "=\'" + color + "\'", null);
        if(c.moveToFirst()) {
            do {
                WorkMark workMark = new WorkMark();
                workMark.setWorkName(c.getString(c.getColumnIndex(Constant.COLUMN_WORDNAME_FAVORITE)));
                workMark.setColor(c.getString(c.getColumnIndex(Constant.COLUMN_FAVORITE_COLOR)));
                mArrWorkMark.add(workMark);
            } while (c.moveToNext());
        }
        return mArrWorkMark;
    }

    public ArrayList<WorkMark> getFavoriteWord(int limit) {
        ArrayList<WorkMark> mArrWorkMark = new ArrayList<>();
        Cursor c = mSQLiteDatabase.rawQuery("SELECT * FROM " + Constant.TABLE_WORD_FAVORITE + " ORDER BY " + Constant.COLUMN_TIME + " DESC " + "LIMIT " + limit, null);
        if(c.moveToFirst()) {
            do {
                WorkMark workMark = new WorkMark();
                workMark.setWorkName(c.getString(c.getColumnIndex(Constant.COLUMN_WORDNAME_FAVORITE)));
                workMark.setColor(c.getString(c.getColumnIndex(Constant.COLUMN_FAVORITE_COLOR)));
                mArrWorkMark.add(workMark);
            } while (c.moveToNext());
        }
        return mArrWorkMark;
    }

    public void removeFavoriteWord(Context context, String wordName) {
        int affectedRow =  mSQLiteDatabase.delete(Constant.TABLE_WORD_FAVORITE , Constant.COLUMN_WORDNAME_FAVORITE + " = ?",
                new String[] { wordName });
        if (affectedRow == 1)
            Toast.makeText(context, "Delete successful!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show();
    }

}
