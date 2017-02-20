package com.example.leopeng.recyclerviewdemo;

import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by leopeng on 13/02/2017.
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private ArrayList<Book> books;
    LruCache<String, Bitmap> cache;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView cardView;
        TextView bookName;
        TextView authorName;
        TextView status;
        TextView summary;
        ImageView imageView;

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
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bookName.setText(books.get(position).getBookName());
        holder.authorName.setText(books.get(position).getAuthor());
        holder.status.setText(books.get(position).getStatus());
        holder.status.setTextColor(books.get(position).getStatusColor());
        holder.summary.setText(books.get(position).getBookDescription());
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView, cache).execute(books.get(position).getImageURL());
        }
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
