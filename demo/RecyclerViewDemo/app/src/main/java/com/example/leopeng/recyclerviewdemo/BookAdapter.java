package com.example.leopeng.recyclerviewdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by leopeng on 13/02/2017.
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> books;
    private LruCache<String, Bitmap> cache;

    public final static String bookAdapterKey = "bookAdapter";
    public final static String bookNameKey = "bookName";
    public final static String authorNameKey = "authorName";
    public final static String summaryKey = "summary";
    public final static String imageURLKey = "imageURL";


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CardView cardView;
        private TextView bookName;
        private TextView authorName;
        private TextView status;
        private TextView summary;
        private ImageView imageView;

        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
            bookName = (TextView) view.findViewById(R.id.bookName);
            authorName = (TextView) view.findViewById(R.id.author);
            status = (TextView) view.findViewById(R.id.status);
            summary = (TextView) view.findViewById(R.id.bookDescription);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }
    }

    public BookAdapter(ArrayList<Book> books, LruCache cache) {
        this.books = books;
        this.cache = cache;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(cardView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bookName.setText(books.get(position).getBookName());
        holder.authorName.setText(books.get(position).getAuthor());
        holder.status.setText(books.get(position).getStatus());
        holder.status.setTextColor(books.get(position).getStatusColor());
        holder.summary.setText(books.get(position).getShortBookDescription());
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView).setBitmapLruCache(cache).execute(books.get(position).getImageURL());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(RecyclerViewActivity.RECYCLERVIEWACTIVITYTAG, "Click");
                Bundle args = new Bundle();
                args.putString(bookNameKey, books.get(position).getBookName());
                args.putString(authorNameKey, books.get(position).getAuthor());
                args.putString(summaryKey, books.get(position).getBookDescription());
                args.putString(imageURLKey, books.get(position).getImageURL());

                Intent intent = new Intent(v.getContext(), DetailViewActivity.class);
                intent.putExtra(bookAdapterKey, args);
                v.getContext().startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}
