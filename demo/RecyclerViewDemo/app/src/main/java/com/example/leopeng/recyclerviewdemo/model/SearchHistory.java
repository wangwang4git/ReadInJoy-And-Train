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
        public static final String TABLE_NAME = "username_search_history";
        public static final String COLUMN_NAME_SEARCH_WORD = "search_word";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }

    public static class BookNameSearchHistoryTable implements BaseColumns {
        public static final String TABLE_NAME = "book_name_search_history";
        public static final String COLUMN_NAME_SEARCH_WORD = "search_word";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
    }

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + UsernameSearchHistoryTable.TABLE_NAME + " (" +
                    UsernameSearchHistoryTable._ID + " INTEGER PRIMARY KEY," +
                    UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " TEXT," +
                    UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT + " TEXT)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + UsernameSearchHistoryTable.TABLE_NAME;


    private static final String SQL_CREATE_TABLE_BOOK_NAME =
            "CREATE TABLE " + BookNameSearchHistoryTable.TABLE_NAME + " (" +
                    BookNameSearchHistoryTable._ID + " INTEGER PRIMARY KEY," +
                    BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " TEXT," +
                    BookNameSearchHistoryTable.COLUMN_NAME_UPDATED_AT + " TEXT)";

    private static final String SQL_DELETE_TABLE_BOOK_NAME =
            "DROP TABLE IF EXISTS " + BookNameSearchHistoryTable.TABLE_NAME;

    public static class SearchHistoryDBHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "SearchHistory.db";

        public SearchHistoryDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_TABLE_BOOK_NAME);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_TABLE);
            db.execSQL(SQL_DELETE_TABLE_BOOK_NAME);
            onCreate(db);
        }

    }
}