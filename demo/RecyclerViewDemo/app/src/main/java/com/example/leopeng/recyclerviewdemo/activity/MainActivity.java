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
    private ListView searchHistoryListView;
    private Toolbar mToolbar;
    private Context mContext;
    private SearchHistory.SearchHistoryDBHelper searchHistoryDBHelper;
    private UsernameSearchHistoryModel model;
    private List<String> usernameList;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        init();
    }

    private void init() {
        // Get the init instance
        mToolbar = (Toolbar) findViewById(R.id.myToolbar);
        editText = (EditText) findViewById(R.id.username);
        backgroundView = (ViewGroup) findViewById(R.id.backgroundView);
        searchHistoryListView = (ListView) findViewById(R.id.searchHistory);
        searchHistoryDBHelper = new SearchHistory.SearchHistoryDBHelper(this);
        usernameList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(mContext, R.layout.username_item, usernameList);
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Init Toolbar
        mToolbar.setTitle("豆瓣 Demo");
        setSupportActionBar(mToolbar);

        // Init search username history
        model = new UsernameSearchHistoryModel(mContext);
        model.setUsernameList(usernameList);

        backgroundView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    model.getInfoFromDB();
                    adapter.notifyDataSetChanged();
                    searchHistoryListView.setVisibility(View.VISIBLE);
                } else {
                    searchHistoryListView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Init SearchHistory ListView
        searchHistoryListView.setAdapter(adapter);
        searchHistoryListView.setDividerHeight(2);
        searchHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                username = usernameList.get(position);
                editText.setText(username);
                jumpToBookList();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        model.getInfoFromDB();
        adapter.notifyDataSetChanged();
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
        SearchView searchView =  (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setQueryHint("Search Books...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(mContext, RecyclerViewActivity.class);
                intent.putExtra(Constant.SEARCHKEY, query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    public void jsonTask(View view) {
        username = editText.getText().toString();
        jumpToBookList();
    }

    private void jumpToBookList() {
        lowercaseUsername = username.toLowerCase();

        Log.d(MainActivity.class.getName(), "Username: " + username);
        Log.d(MainActivity.class.getName(), "LowerCaseUsername: " + lowercaseUsername);

        if (lowercaseUsername.trim().isEmpty()) {
            Toast.makeText(mContext, "Please enter username.", Toast.LENGTH_SHORT).show();
            return;
        }

        model.setLowercaseUsername(lowercaseUsername);
        if (!model.isExistInfo()) {
            model.putInfoIntoDB();
        } else {
            model.updateInfoIntoDB(lowercaseUsername);
        }

        Intent intent = new Intent(mContext, RecyclerViewActivity.class);
        intent.putExtra(Constant.USERNAMEKEY, username);

        String jsonString = getCacheJSONString(lowercaseUsername);
        if (jsonString != null && !jsonString.isEmpty()) {
            intent.putExtra(Constant.BOOKJSONKEY, jsonString);
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
