package com.example.leopeng.recyclerviewdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by leopeng on 13/02/2017.
 */

public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String username;
    private String updateURL;

    private ArrayList<Book> bookList;
    private static int cacheSize = 8 * 1024 * 1024;
    LruCache<String, Bitmap> bitmapLruCache;

    public final static String RECYCLERVIEWACTIVITYTAG = "RecyclerViewActivity";
    public final static String DETAILKEY = "DETAILKEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        bookList = new ArrayList<Book>();

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        VerticalDividerItemDecoration verticalDividerItemDecoration = new VerticalDividerItemDecoration(10);
        recyclerView.addItemDecoration(verticalDividerItemDecoration);

        this.bitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {
        };

        adapter = new BookAdapter(bookList, bitmapLruCache);
        recyclerView.setAdapter(adapter);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        if (updateURL != null) {
            CommonJsonTask commonJsonTask = new CommonJsonTask(RecyclerViewActivity.this);
            commonJsonTask.cacheKey = username;
            commonJsonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,updateURL);
        }
    }

    private void handleIntent(Intent intent) {
        setTitle(intent.getStringExtra(MainActivity.USERNAMEKEY) + " 收藏的图书");
        username = intent.getStringExtra(MainActivity.USERNAMEKEY);
        updateURL = "https://api.douban.com/v2/book/user/" + username + "/collections?count=100";
        String jsonString = intent.getStringExtra(MainActivity.BOOKJSONKEY);
        JSONParse(jsonString);
        Log.d(RECYCLERVIEWACTIVITYTAG, "bookList Size: " + bookList.size());
        if (bookList.size() == 0) {
            Book noBooksInfo = new Book("该用户没有收藏任何书本。", "", "", "", "");
            bookList.add(noBooksInfo);
        }
    }

    public void JSONParse(String jsonString) {
        if (jsonString != null && !jsonString.isEmpty() ) {
            bookList.clear();
            try {
                JSONObject json = new JSONObject(jsonString);
                String count = json.getString("count");
                String start = json.getString("start");
                String total = json.getString("total");

                Log.d(RECYCLERVIEWACTIVITYTAG, "count: " + count);
                Log.d(RECYCLERVIEWACTIVITYTAG, "start: " + start);
                Log.d(RECYCLERVIEWACTIVITYTAG, "total: " + total);

//                if (Integer.parseInt(total) > 0) {
//                  Book bookNumber = new Book(username + " 一共收藏了 " + total + " 本图书", "", "一次最多显示 100 本图书信息", "", "");
//                   bookList.add(bookNumber);
//                }

                JSONArray jsonArray = json.getJSONArray("collections");
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject oneBook = jsonArray.getJSONObject(i);
                        String updated = oneBook.getString("updated");
                        String status = oneBook.getString("status");

                        JSONObject bookInfo = oneBook.getJSONObject("book");
                        String bookTitle = bookInfo.getString("title");
                        String summary = bookInfo.getString("summary");
                        String imageURL = bookInfo.getString("image");
                        JSONArray author = bookInfo.getJSONArray("author");
                        String authorName = "";

                        int maxAuthorNumber = Math.min(5, author.length());
                        for (int j = 0; j < maxAuthorNumber; j++) {
                            authorName += author.getString(j);
                            if (j + 1 != maxAuthorNumber) {
                                authorName += ", ";
                            }
                        }

                        Book oneBookModel = new Book(bookTitle, authorName, summary, status, imageURL);
                        bookList.add(oneBookModel);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.switch_button, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_switch:
                if (layoutManager.getClass() == LinearLayoutManager.class) {
                    layoutManager = new GridLayoutManager(this, 1);
                } else {
                    layoutManager = new LinearLayoutManager(this);
                }

                recyclerView.setLayoutManager(layoutManager);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
