package com.example.leopeng.recyclerviewdemo.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by leopeng on 28/02/2017.
 */

public class BookNameSearchHistoryModel {
    private SearchHistory.SearchHistoryDBHelper searchHistoryDBHelper;

    public BookNameSearchHistoryModel(Context context) {
        searchHistoryDBHelper = new SearchHistory.SearchHistoryDBHelper(context);
    }


    public long insert(String keyword) {
        if (isExistEntry(keyword)) {
            update(keyword);
            return 0;
        }

        SQLiteDatabase db = searchHistoryDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD, keyword);

        Date now = new Date();
        values.put(SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_UPDATED_AT, now.getTime());

        Log.d(BookNameSearchHistoryModel.class.getName(), "Insert: " + keyword);

        long id = db.insert(SearchHistory.BookNameSearchHistoryTable.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public boolean isExistEntry(String keyword) {
        SQLiteDatabase db = searchHistoryDBHelper.getReadableDatabase();
        String[] projections = {SearchHistory.BookNameSearchHistoryTable._ID};

        String selection = SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " = ?";
        String[] selectionArgs = {keyword};

        Cursor cursor = db.query(
                SearchHistory.BookNameSearchHistoryTable.TABLE_NAME,
                projections,
                selection,
                selectionArgs,
                null,
                null,
                ""
        );

        boolean res = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return res;
    }

    public boolean update(String keyword) {
        SQLiteDatabase db = searchHistoryDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        Date now = new Date();
        values.put(SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_UPDATED_AT, now.getTime());

        String selection = SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " LIKE ?";
        String[] selectionArgs = { keyword };

        int count = db.update(
                SearchHistory.BookNameSearchHistoryTable.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        db.close();
        return count > 0;
    }

    public List<String> get() {
        SQLiteDatabase db = searchHistoryDBHelper.getReadableDatabase();
        String[] projections = {
                SearchHistory.BookNameSearchHistoryTable._ID,
                SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD,
                SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_UPDATED_AT
        };

        String sortOrder = SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_UPDATED_AT + " DESC";
        String limits = "6";

        Cursor cursor = db.query(
                SearchHistory.BookNameSearchHistoryTable.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder,
                limits
        );

        List<String> list = new ArrayList<>();

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SearchHistory.BookNameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD));
            list.add(name);
        }

        for (String bookName : list) {
            Log.d(BookNameSearchHistoryModel.class.getName(), "bookName: " + bookName);
        }

        cursor.close();
        db.close();
        return list;
    }
}
