package com.example.leopeng.recyclerviewdemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;

import com.example.leopeng.recyclerviewdemo.util.BookRequest;
import com.example.leopeng.recyclerviewdemo.util.CommonJsonTask;
import com.example.leopeng.recyclerviewdemo.util.Constant;
import com.example.leopeng.recyclerviewdemo.R;
import com.example.leopeng.recyclerviewdemo.util.VerticalDividerItemDecoration;
import com.example.leopeng.recyclerviewdemo.model.Book;
import com.example.leopeng.recyclerviewdemo.model.BookAdapter;

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
    private Toolbar myToolbar;
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

        init();

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        boolean firstLoad = getIntent().getBooleanExtra(Constant.FIRST_LOAD_KEY, false);
        if (updateURL != null && !updateURL.isEmpty() && !firstLoad) {
            CommonJsonTask commonJsonTask = new CommonJsonTask(RecyclerViewActivity.this);
            if (username != null && !username.isEmpty()) {
                commonJsonTask.cacheKey = username;
            }
            if (searchBookName != null && !searchBookName.isEmpty()) {
                commonJsonTask.cacheKey = Constant.SEARCH_BOOK_CACHE_FILE_PREFIX + searchBookName;
            }
            commonJsonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,updateURL);
        }
    }

    private void init() {

        // Init RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Add RecyclerView Item Decoration
        VerticalDividerItemDecoration verticalDividerItemDecoration = new VerticalDividerItemDecoration(10);
        recyclerView.addItemDecoration(verticalDividerItemDecoration);

        // Init Adapter
        bookList = new ArrayList<Book>();
        adapter = new BookAdapter(bookList, bitmapLruCache);
        recyclerView.setAdapter(adapter);

        // Init Toolbar
        myToolbar= (Toolbar) findViewById(R.id.myRecyclerViewToolbar);
        myToolbar.setTitle("BookLists");
        setSupportActionBar(myToolbar);

        // Init Image Cache
        this.bitmapLruCache = new LruCache<>(cacheSize);
    }

    private void handleIntent(Intent intent) {
        searchBookName = intent.getStringExtra(Constant.SEARCH_KEY).toLowerCase();
        if (searchBookName!= null && !searchBookName.isEmpty()) {
            updateURL = BookRequest.getSearchBooksURL(searchBookName);
            Log.d(RECYCLERVIEWACTIVITYTAG, "search book: " + searchBookName);
            updateBookList(JSONParse(intent.getStringExtra(Constant.BOOK_JSON_KEY)));
        } else {
            username = intent.getStringExtra(Constant.USERNAME_KEY);
            updateURL = BookRequest.getUserCollectionsURL(username);
            String jsonString = intent.getStringExtra(Constant.BOOK_JSON_KEY);
            updateBookList(JSONParse(jsonString));
            Log.d(RECYCLERVIEWACTIVITYTAG, "bookList Size: " + bookList.size());
        }

    }

    /**
     * 分析回包的json字符串 (目前支持解析两种回包：1. 用户收藏的书本 2. 搜索图书返回结果)
     * @param jsonString json字符串
     * @return List
     */
    public List<Book> JSONParse(String jsonString) {
        List<Book> res = new ArrayList<>();
        if (jsonString != null && !jsonString.isEmpty() ) {
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

                            res.add(oneBookModel);

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

                            res.add(oneBookModel);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

//                adapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    public void updateBookList(List<Book> newBookList) {
        bookList.clear();
        bookList.addAll(newBookList);

        adapter.notifyDataSetChanged();
    }
}
