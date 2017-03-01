package com.example.leopeng.recyclerviewdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.LruCache;
import android.view.MenuItem;

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
    private Context mContext;

    private ArrayList<Book> bookList;
    private static int cacheSize = 8 * 1024 * 1024;
    LruCache<String, Bitmap> bitmapLruCache;
    public boolean isLoading = false;
    public boolean isNoMore = false;
    public boolean network = true;

    public final static String RECYCLER_VIEW_ACTIVITY_TAG = "RecyclerViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        mContext = this;

        init();

        if (getIntent() != null) {
            handleIntent(getIntent());
        }

        boolean firstLoad = getIntent().getBooleanExtra(Constant.FIRST_LOAD_KEY, false);
        if (updateURL != null && !updateURL.isEmpty() && !firstLoad) {


            //Delay run
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final CommonJsonTask commonJsonTask = new CommonJsonTask(RecyclerViewActivity.this);
                    if (username != null && !username.isEmpty()) {
                        commonJsonTask.cacheKey = username;
                    }
                    if (searchBookName != null && !searchBookName.isEmpty()) {
                        commonJsonTask.cacheKey = Constant.SEARCH_BOOK_CACHE_FILE_PREFIX + searchBookName;
                    }
                    commonJsonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,updateURL);
                    isLoading = true;
                }
            }, 300);
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Init Image Cache
        this.bitmapLruCache = new LruCache<>(cacheSize);

        // Add onScrollListener
        recyclerView.addOnScrollListener(mScrollListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Set up my toolbar
        if (searchBookName != null && !searchBookName.isEmpty()) {
            myToolbar.setTitle(searchBookName + " Search Results: ");
        } else if (username != null && !username.isEmpty()) {
            myToolbar.setTitle(username + " Collected Books: ");
        }
        setSupportActionBar(myToolbar);

    }

    @Override
    protected void onResume() {
        super.onResume();

        network = true;
    }

    private void handleIntent(Intent intent) {
        searchBookName = intent.getStringExtra(Constant.SEARCH_KEY);
        username = intent.getStringExtra(Constant.USERNAME_KEY);
        if ( searchBookName != null && !searchBookName.isEmpty() ) {
            searchBookName = searchBookName.toLowerCase();
            updateURL = BookRequest.getSearchBooksURL(searchBookName);
            Log.d(RECYCLER_VIEW_ACTIVITY_TAG, "search book: " + searchBookName);
            String jsonStrong = intent.getStringExtra(Constant.BOOK_JSON_KEY);
            if (jsonStrong != null && !jsonStrong.isEmpty()) {
                updateBookList(JSONParse(intent.getStringExtra(Constant.BOOK_JSON_KEY)));
            }
        } else if ( username != null && !username.isEmpty() ){
            updateURL = BookRequest.getUserCollectionsURL(username);
            String jsonString = intent.getStringExtra(Constant.BOOK_JSON_KEY);
            if (jsonString != null && !jsonString.isEmpty()) {
                updateBookList(JSONParse(jsonString));
            }
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

                Log.d(RECYCLER_VIEW_ACTIVITY_TAG, "count: " + count);
                Log.d(RECYCLER_VIEW_ACTIVITY_TAG, "start: " + start);
                Log.d(RECYCLER_VIEW_ACTIVITY_TAG, "total: " + total);

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
        if (newBookList.size() < 20) {
            isNoMore = true;
        }

        bookList.clear();
        bookList.addAll(newBookList);

        adapter.notifyDataSetChanged();
    }

    public void addBookListToEnd(List<Book> newBookList) {
        for (Book book : newBookList) {
            if (!bookList.contains(book)) {
                bookList.add(book);
            }
        }

        if (newBookList.size() < 20) {
            isNoMore = true;
        }

        adapter.notifyDataSetChanged();
    }

    public void addBookListToHead(List<Book> newBookList) {
        for (Book book : newBookList) {
            if (!bookList.contains(book)) {
                bookList.add(0, book);
            }
        }

        if (newBookList.size() < 20) {
            isNoMore = true;
        }

        adapter.notifyDataSetChanged();
    }

   protected RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
       @Override
       public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

           if (isLoading || isNoMore || !network) {
               return;
           }

//           Log.d(RecyclerViewActivity.class.getName(), "dy: " + dy);

           int visibleItemCount = layoutManager.getChildCount();
           int totalItemCount = layoutManager.getItemCount();
           int pastVisibleItemCount = ((LinearLayoutManager)layoutManager).findFirstVisibleItemPosition();

           if (pastVisibleItemCount + visibleItemCount + 8 >= totalItemCount) {
               CommonJsonTask commonJsonTask = new CommonJsonTask(mContext);
               commonJsonTask.isAdd = true;
               String url = "";
               if (username != null && !username.isEmpty()) {
                   url = BookRequest.getUserCollectionsURL(username, totalItemCount);
               } else if (searchBookName != null && !searchBookName.isEmpty()) {
                   url = BookRequest.getSearchBooksURL(searchBookName, totalItemCount);
               }

               commonJsonTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
               isLoading = true;
           }

       }
   };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
