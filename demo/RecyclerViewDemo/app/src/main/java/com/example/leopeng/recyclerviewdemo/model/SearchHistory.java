package com.example.leopeng.recyclerviewdemo.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by leopeng on 22/02/2017.
 */

public final class SearchHistory {
    private SearchHistory() {
    }

    public static class UsernameSearchHistoryTable implements BaseColumns {
        public static final String TABLE_NAME = "searchHistory";
        public static final String COLUMN_NAME_SEARCH_WORD = "searchWord";
        public static final String COLUMN_NAME_UPDATED_AT = "updatedAt";
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + UsernameSearchHistoryTable.TABLE_NAME + " (" +
                    UsernameSearchHistoryTable._ID + " INTEGER PRIMARY KEY," +
                    UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " TEXT," +
                    UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT + " TEXT)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + UsernameSearchHistoryTable.TABLE_NAME;


    public static class SearchHistoryDBHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "SearchHistory.db";

        public SearchHistoryDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_TABLE);
            onCreate(db);
        }

    }
}