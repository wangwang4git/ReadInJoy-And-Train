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

public class UsernameSearchHistoryModel {
    private SearchHistory.SearchHistoryDBHelper searchHistoryDBHelper;
    private String lowercaseUsername;
    private List<String> usernameList;

    public UsernameSearchHistoryModel(Context context) {
        searchHistoryDBHelper = new SearchHistory.SearchHistoryDBHelper(context);
    }

    public void setLowercaseUsername(String username) {
        this.lowercaseUsername = username;
    }

    public void setUsernameList(List<String> list) {
        this.usernameList = list;
    }

    public List<String> getUsernameList() {
        return this.usernameList;
    }

    public long putInfoIntoDB() {
        SQLiteDatabase db = searchHistoryDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD, lowercaseUsername);

        Date now = new Date();
        values.put(SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT, now.getTime());

        long id = db.insert(SearchHistory.UsernameSearchHistoryTable.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public boolean updateInfoIntoDB(String username) {
        SQLiteDatabase db = searchHistoryDBHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        Date now = new Date();
        values.put(SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT, now.getTime());

        String selection = SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " LIKE ?";
        String[] selectionArgs = { username };

        int count = db.update(
                SearchHistory.UsernameSearchHistoryTable.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );

        db.close();

        return count > 0 ;
    }

    public boolean isExistInfo() {
        SQLiteDatabase db = searchHistoryDBHelper.getReadableDatabase();
        String[] projections = {SearchHistory.UsernameSearchHistoryTable._ID};

        String selection = SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD + " = ?";
        String[] selectionArgs = {lowercaseUsername};

        Cursor cursor = db.query(
                SearchHistory.UsernameSearchHistoryTable.TABLE_NAME,
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
        return res ;
    }

    public void getInfoFromDB() {
        SQLiteDatabase db = searchHistoryDBHelper.getReadableDatabase();
        String[] projections = {
                SearchHistory.UsernameSearchHistoryTable._ID,
                SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD,
                SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT
        };

        String sortOrder = SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_UPDATED_AT + " DESC";
//        String limits = "5";

        Cursor cursor = db.query(
                SearchHistory.UsernameSearchHistoryTable.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder,
                null
        );

        if (usernameList == null) {
            usernameList = new ArrayList<>();
        }

        usernameList.clear();

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(SearchHistory.UsernameSearchHistoryTable.COLUMN_NAME_SEARCH_WORD));
            usernameList.add(name);
        }

        for (String username : usernameList) {
            Log.d(UsernameSearchHistoryModel.class.getName(), "username: " + username);
        }

        if (usernameList.isEmpty()) {
            Log.d(UsernameSearchHistoryModel.class.getName(), "No username stored.");
        }

        cursor.close();
        db.close();
    }

}
