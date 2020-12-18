package com.samsung.lookup.data.engviet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.samsung.lookup.data.Constant;
import com.samsung.lookup.model.Word;

import java.util.ArrayList;

import static com.samsung.lookup.data.Constant.COLUMN_DETAILS;
import static com.samsung.lookup.data.Constant.COLUMN_WORDNAME;

public class EngVietDbAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static EngVietDbAccess instance;
    Cursor c = null;

    public EngVietDbAccess(Context context) {
        this.openHelper = new EngVietDbOpenHelper(context);
    }

    // no need
    public static EngVietDbAccess getInstance(Context context){
        if(instance == null) {
            instance = new EngVietDbAccess(context);
        }
        return instance;
    }

    // to open the database
    public void open(){
        this.db = openHelper.getWritableDatabase();
    }

    // close the database connection
    public void close(){
        if(db != null) {
            this.db.close();
        }
    }

    // Get suggest word from suggest character
    public ArrayList<String> getWordNames(String stringSuggest, int limit){
        ArrayList<String> wordNameArr = new ArrayList<>();

        // Select All query
        String selectQuery = "SELECT * from " + Constant.TABLE_NAME + " where " + COLUMN_WORDNAME + " LIKE \'" + stringSuggest + "%\' LIMIT " + limit;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                wordNameArr.add(cursor.getString(cursor.getColumnIndex(COLUMN_WORDNAME)));
            } while (cursor.moveToNext());
        }
        return wordNameArr;
    }

//    public ArrayList<Word> getWords(String stringSuggest){
//        ArrayList<Word> wordArrayList = new ArrayList<>();
//
//        // Select All query
//        String selectQuery = "SELECT * from " + Constant.TABLE_NAME + " where " + COLUMN_WORDNAME + " LIKE \'" + stringSuggest + "%\' LIMIT 25";
//        Cursor cursor = db.rawQuery(selectQuery, null);
//        if (cursor.moveToFirst()) {
//            do {
//                Word word = new Word();
//                word.setWordname(cursor.getString(cursor.getColumnIndex(COLUMN_WORDNAME)));
//                word.setDetails(cursor.getString(cursor.getColumnIndex(COLUMN_DETAILS)));
//
//                wordArrayList.add(word);
//            } while (cursor.moveToNext());
//        }
//        return wordArrayList;
//    }

    // Get detail word from wordName
    public Word getWord(String wordName){

        Word word = new Word();
        String selectQuery = "SELECT * from " + Constant.TABLE_NAME + " where " + COLUMN_WORDNAME + " = \'" + wordName + "\'";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                word.setWordname(cursor.getString(cursor.getColumnIndex(COLUMN_WORDNAME)));
                word.setDetails(cursor.getString(cursor.getColumnIndex(COLUMN_DETAILS)));

            } while (cursor.moveToNext());
        }
        return word;
    }
}
