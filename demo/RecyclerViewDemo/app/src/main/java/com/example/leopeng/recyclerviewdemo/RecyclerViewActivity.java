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
import java.util.List;

/**
 * Created by leopeng on 13/02/2017.
 */

public class RecyclerViewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private String username;
    private String updateURL;
    private String searchBookName;

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

        boolean firstLoad = getIntent().getBooleanExtra(MainActivity.FIRSTLOADKEY, false);
        if (updateURL != null && !updateURL.isEmpty() && !firstLoad) {
            CommonJsonTask commonJsonTask = new CommonJsonTask(RecyclerViewActivity.this);
            if (username != null && !username.isEmpty()) {
                commonJsonTask.cacheKey = username;
            }
            commonJsonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,updateURL);
        }
    }

    private void handleIntent(Intent intent) {
        searchBookName = intent.getStringExtra(MainActivity.SEARCHKEY);
        if (searchBookName!= null && !searchBookName.isEmpty()) {
            updateURL = "https://api.douban.com/v2/book/search" + "?q=" + searchBookName;
            Log.d(RECYCLERVIEWACTIVITYTAG, "search book: " + searchBookName);
        } else {
//        setTitle(intent.getStringExtra(MainActivity.USERNAMEKEY) + " 收藏的图书");
            username = intent.getStringExtra(MainActivity.USERNAMEKEY);
            updateURL = "https://api.douban.com/v2/book/user/" + username + "/collections?count=100";
            String jsonString = intent.getStringExtra(MainActivity.BOOKJSONKEY);
            JSONParse(jsonString);
            Log.d(RECYCLERVIEWACTIVITYTAG, "bookList Size: " + bookList.size());
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


                JSONArray jsonArray = null;
                if (json.has("collections")) {
                    jsonArray = json.getJSONArray("collections");
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

                            // Set rating info
                            JSONObject rating = bookInfo.getJSONObject("rating");
                            oneBookModel.setRating(rating.getString("max"), rating.getString("min"), rating.getString("numRaters"), rating.getString("average"));

                            // Set tags info
                            JSONArray tags = bookInfo.getJSONArray("tags");
//                        int maxTagNumber = Math.min(10, tags.length());
                            List<Book.Tag> tagsList = new ArrayList<Book.Tag>();
                            for (int j = 0; j < tags.length(); j++) {
                                JSONObject oneTag = tags.getJSONObject(j);
                                Book.Tag tag = new Book.Tag();
                                tag.count = oneTag.getInt("count");
                                tag.tagName = oneTag.getString("name");

                                tagsList.add(tag);
                            }
                            oneBookModel.setTags(tagsList);

                            bookList.add(oneBookModel);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (json.has("books")) {
                    jsonArray = json.getJSONArray("books");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject oneBook = jsonArray.getJSONObject(i);

                            String bookTitle = oneBook.getString("title");
                            String summary = oneBook.getString("summary");
                            String imageURL = oneBook.getString("image");
                            JSONArray author = oneBook.getJSONArray("author");

                            String authorName = "";
                            int maxAuthorNumber = Math.min(5, author.length());
                            for (int j = 0; j < maxAuthorNumber; j++) {
                                authorName += author.getString(j);
                                if (j + 1 != maxAuthorNumber) {
                                    authorName += ", ";
                                }
                            }

                            Book oneBookModel = new Book(bookTitle, authorName, summary, "", imageURL);

                            JSONObject rating = oneBook.getJSONObject("rating");
                            oneBookModel.setRating(rating.getString("max"), rating.getString("min"), rating.getString("numRaters"), rating.getString("average"));

                            JSONArray tags = oneBook.getJSONArray("tags");
                            List<Book.Tag> tagsList = new ArrayList<Book.Tag>();
                            for (int j = 0; j < tags.length(); j++) {
                                JSONObject oneTag = tags.getJSONObject(j);
                                Book.Tag tag = new Book.Tag();
                                tag.count = oneTag.getInt("count");
                                tag.tagName = oneTag.getString("name");

                                tagsList.add(tag);
                            }
                            oneBookModel.setTags(tagsList);

                            bookList.add(oneBookModel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
