package com.example.leopeng.listdemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by leopeng on 10/02/2017.
 */
// 对于一个页面的名字，就不要取ListView，最好还是带一个Activity的后缀，这样多人开发时也比较好见名知意
public class ListView extends ListActivity {

    ArrayList<Book> bookList;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        getListView().setDividerHeight(10);

        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }

    }

    private void handleIntent(Intent intent) {
        JSONParse(intent);
        Log.d("bookList", "bookList Size: " + bookList.size());

        if (bookList.size() == 0) {
            Book noBooksInfo = new Book("该用户没有收藏任何书本。", "", "", "");
            bookList.add(noBooksInfo);
        }

        BookListAdapter bookListAdapter = new BookListAdapter(this, bookList);
        getListView().setAdapter(bookListAdapter);
    }

    private void JSONParse(Intent intent) {
        bookList = new ArrayList<Book>();
        String books= intent.getStringExtra(MainActivity.BOOKJSONKEY);
        String username = intent.getStringExtra(MainActivity.USERNAMEKEY);

        // 对于字符串的判空，android sdk已经帮我们提供了相关方法TextUtils.isEmpty()
        // 对于常用的函数功能，sdk都有提供的，部分实现在移动端设备上效率会更高
        if (books != null && books != "") {
            try {
                JSONObject json = new JSONObject(books);
                String count = json.getString("count");
                String start = json.getString("start");
                String total = json.getString("total");

                // 还是日志TAG的问题
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
