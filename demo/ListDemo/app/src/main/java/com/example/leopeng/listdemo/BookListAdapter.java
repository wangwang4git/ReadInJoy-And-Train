package com.example.leopeng.listdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by leopeng on 10/02/2017.
 */

public class BookListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Book> books;

    public BookListAdapter(Context context, ArrayList<Book> books) {
        this.context = context;
        this.books = books;
    }

    public int getCount() {
        return books.size();
    }

    public Object getItem(int position) {
        return books.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false);
        }

        Book currentBook = (Book) getItem(position);

        // 看一看listview的viewholder实现机制，如何做到itemview优雅的复用，这些findViewById都是不用重复执行的
        TextView textViewBookName = (TextView)convertView.findViewById(R.id.bookName);
        TextView textViewAuthor = (TextView)convertView.findViewById(R.id.author);
        TextView textViewBookDescription = (TextView)convertView.findViewById(R.id.bookDescription);
        TextView textViewStatus = (TextView)convertView.findViewById(R.id.status);

        textViewBookName.setText(currentBook.getBookName());
        textViewAuthor.setText(currentBook.getAuthor());
        textViewBookDescription.setText(currentBook.getBookDescription());
        textViewStatus.setText(currentBook.getStatus());

        return convertView;
    }
}
