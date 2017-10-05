package com.example.leopeng.recyclerviewdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.leopeng.recyclerviewdemo.model.BookNameSearchHistoryModel;
import com.example.leopeng.recyclerviewdemo.model.UsernameSearchHistoryModel;
import com.example.leopeng.recyclerviewdemo.util.Constant;
import com.example.leopeng.recyclerviewdemo.R;
import com.example.leopeng.recyclerviewdemo.model.SearchHistory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String username;
    private String lowercaseUsername;
    private EditText editText;
    private ViewGroup backgroundView;
    private ListView usernameHistoryListView;
    private ListView bookNameHistoryListView;
    private Toolbar mToolbar;
    private Context mContext;
    private SearchHistory.SearchHistoryDBHelper searchHistoryDBHelper;
    private UsernameSearchHistoryModel usernameModel;
    private BookNameSearchHistoryModel bookNameModel;
    private List<String> usernameList;
    private List<String> bookNameList;
    private ArrayAdapter usernameAdapter;
    private ArrayAdapter bookNameAdapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        init();
    }

    private void init() {
        // Get the init instance
        username = "";
        lowercaseUsername = "";
        mToolbar = (Toolbar) findViewById(R.id.myToolbar);
        editText = (EditText) findViewById(R.id.username);
        backgroundView = (ViewGroup) findViewById(R.id.backgroundView);
        usernameHistoryListView = (ListView) findViewById(R.id.search_history);
        bookNameHistoryListView = (ListView) findViewById(R.id.book_name_history_list_view);
        searchHistoryDBHelper = new SearchHistory.SearchHistoryDBHelper(this);
        usernameList = new ArrayList<>();
        usernameAdapter = new ArrayAdapter<String>(mContext, R.layout.username_item, usernameList);
        bookNameList = new ArrayList<>();
        bookNameAdapter = new ArrayAdapter<String>(mContext, R.layout.book_name_item, bookNameList);
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Init Toolbar
        mToolbar.setTitle("豆瓣 Demo");
        setSupportActionBar(mToolbar);

        // Init search username history
        usernameModel = new UsernameSearchHistoryModel(mContext);
        usernameModel.setUsernameList(usernameList);

        // Init search book name history
        bookNameModel = new BookNameSearchHistoryModel(mContext);

        backgroundView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    bookNameHistoryListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    usernameModel.getInfoFromDB();
                    usernameAdapter.notifyDataSetChanged();
                    usernameHistoryListView.setVisibility(View.VISIBLE);
                } else {
                    usernameHistoryListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Init SearchHistory ListView
        usernameHistoryListView.setAdapter(usernameAdapter);
        usernameHistoryListView.setDividerHeight(2);
        usernameHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                username = usernameList.get(position);
                editText.setText(username);
                jumpToBookList();
            }
        });

        // Init bookName search history ListView
        bookNameHistoryListView.setAdapter(bookNameAdapter);
        bookNameHistoryListView.setDividerHeight(2);
        bookNameHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, RecyclerViewActivity.class);
                String searchBookName = bookNameList.get(position);
                intent.putExtra(Constant.SEARCH_KEY, searchBookName);

                if (searchBookName != null && !searchBookName.isEmpty()) {
                    bookNameModel.insert(searchBookName);
                    searchView.setQuery(searchBookName, false);
                }

                if (searchBookName != null && !searchBookName.isEmpty()) {
                    String jsonString = getCacheJSONString(Constant.SEARCH_BOOK_CACHE_FILE_PREFIX + searchBookName.toLowerCase());
                    if (jsonString != null && !jsonString.isEmpty()) {
                        intent.putExtra(Constant.BOOK_JSON_KEY, jsonString);
                    }
                }
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        usernameModel.getInfoFromDB();
        usernameAdapter.notifyDataSetChanged();

        bookNameList.clear();
        bookNameList.addAll(bookNameModel.get());
        bookNameAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        searchHistoryDBHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.searchButton);
        searchView =  (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint("Search Books...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(mContext, RecyclerViewActivity.class);
                intent.putExtra(Constant.SEARCH_KEY, query);
                String jsonString = getCacheJSONString(Constant.SEARCH_BOOK_CACHE_FILE_PREFIX + query.toLowerCase());
                if (jsonString != null && !jsonString.isEmpty()) {
                    intent.putExtra(Constant.BOOK_JSON_KEY, jsonString);
                }
                if (!query.isEmpty()) {
                    bookNameModel.insert(query);
                    startActivity(intent);
                } else {
                    Toast.makeText(mContext, "Please enter key words.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//        searchView.setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bookNameHistoryListView.setVisibility(View.VISIBLE);
                    bookNameList.clear();
                    bookNameList.addAll(bookNameModel.get());
                    bookNameAdapter.notifyDataSetChanged();
                } else {
                    bookNameHistoryListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        return true;
    }

    public void jsonTask(View view) {
        username = editText.getText().toString();
        jumpToBookList();
    }

    private void jumpToBookList() {
        if (username != null) {
            lowercaseUsername = username.toLowerCase();
        }

        Log.d(MainActivity.class.getName(), "Username: " + username);
        Log.d(MainActivity.class.getName(), "LowerCaseUsername: " + lowercaseUsername);

        if (lowercaseUsername.trim().isEmpty()) {
            Toast.makeText(mContext, "Please enter username.", Toast.LENGTH_SHORT).show();
            return;
        }

        usernameModel.setLowercaseUsername(lowercaseUsername);
        if (!usernameModel.isExistInfo()) {
            usernameModel.putInfoIntoDB();
        } else {
            usernameModel.updateInfoIntoDB(lowercaseUsername);
        }

        Intent intent = new Intent(mContext, RecyclerViewActivity.class);
        intent.putExtra(Constant.USERNAME_KEY, username);

        String jsonString = getCacheJSONString(lowercaseUsername);
        if (jsonString != null && !jsonString.isEmpty()) {
            intent.putExtra(Constant.BOOK_JSON_KEY, jsonString);
            Log.d(MainActivity.class.getName(), "Hit disk cache!");
        }

        startActivity(intent);
    }

    private String getCacheJSONString(String fileName) {
        File file = null;
        BufferedReader reader = null;
        try {
            file = new File(getCacheDir(), fileName);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
