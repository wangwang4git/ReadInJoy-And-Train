package com.example.leopeng.recyclerviewdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by leopeng on 13/02/2017.
 */

public class RecyclerViewActivity extends Activity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        VerticalDividerItemDecoration verticalDividerItemDecoration = new VerticalDividerItemDecoration(10);
        recyclerView.addItemDecoration(verticalDividerItemDecoration);

        adapter = new BookAdapter(bookList);
        recyclerView.setAdapter(adapter);
    }

    private void handleIntent(Intent intent) {
        JSONParse(intent);
        Log.d("bookList", "bookList Size: " + bookList.size());

        if (bookList.size() == 0) {
            Book noBooksInfo = new Book("该用户没有收藏任何书本。", "", "", "");
            bookList.add(noBooksInfo);
        }
    }

    private void JSONParse(Intent intent) {
        bookList = new ArrayList<Book>();
        String books= intent.getStringExtra(MainActivity.BOOKJSONKEY);
        String username = intent.getStringExtra(MainActivity.USERNAMEKEY);

        if (books != null && books != "") {
            try {
                JSONObject json = new JSONObject(books);
                String count = json.getString("count");
                String start = json.getString("start");
                String total = json.getString("total");

                Log.d("count", "count: " + count);
                Log.d("start", "start: " + start);
                Log.d("total", "total: " + total);

                if (Integer.parseInt(total) > 0) {
                    Book bookNumber = new Book(username + " 一共收藏了 " + total + " 本图书", "", "一次最多显示 100 本图书信息", "");
                    bookList.add(bookNumber);
                }

                JSONArray jsonArray = json.getJSONArray("collections");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject oneBook = jsonArray.getJSONObject(i);
                        String updated = oneBook.getString("updated");
                        String status = oneBook.getString("status");

                        JSONObject bookInfo = oneBook.getJSONObject("book");
                        String bookTitle = bookInfo.getString("title");
                        String summary = bookInfo.getString("summary");
                        JSONArray author = bookInfo.getJSONArray("author");
                        String authorName = "";

                        for (int j = 0; j < author.length(); j++) {
                            authorName += author.getString(j);
                            if (j + 1 != author.length()) {
                                authorName += ", ";
                            }
                        }

                        Book oneBookModel = new Book(bookTitle, authorName, summary, status);
                        bookList.add(oneBookModel);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
