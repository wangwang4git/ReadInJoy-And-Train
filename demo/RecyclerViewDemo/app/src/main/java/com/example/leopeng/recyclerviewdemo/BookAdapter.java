package com.example.leopeng.recyclerviewdemo;

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

public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Book> books;
    private LruCache<String, Bitmap> cache;

    public final static String bookAdapterKey = "bookAdapter";
    public final static String bookNameKey = "bookName";
    public final static String authorNameKey = "authorName";
    public final static String summaryKey = "summary";
    public final static String imageURLKey = "imageURL";

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_FOOTER = 2;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView bookName;
        private TextView authorName;
        private TextView status;
        private TextView summary;
        private ImageView imageView;

        public ItemViewHolder(CardView view) {
            super(view);
            bookName = (TextView) view.findViewById(R.id.bookName);
            authorName = (TextView) view.findViewById(R.id.author);
            status = (TextView) view.findViewById(R.id.status);
            summary = (TextView) view.findViewById(R.id.bookDescription);
            imageView = (ImageView) view.findViewById(R.id.thumbImage);
        }
    }

   public static class HeaderViewHolder extends RecyclerView.ViewHolder {

       private TextView headerText;

       public HeaderViewHolder(CardView view) {
           super(view);
           headerText = (TextView) view.findViewById(R.id.headerText);
       }
   }

    public BookAdapter(ArrayList<Book> books, LruCache cache) {
        this.books = books;
        this.cache = cache;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(cardView);
            return viewHolder;
        } else if (viewType == TYPE_HEADER) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.header_view, parent, false);
            HeaderViewHolder viewHolder= new HeaderViewHolder(cardView);
            return viewHolder;
        } else if (viewType == TYPE_FOOTER) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.header_view, parent, false);
            return new HeaderViewHolder(cardView);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position)) {
            return TYPE_HEADER;
        } else if (isPositionFooter(position))  {
            return TYPE_FOOTER;
        }

        return TYPE_ITEM;
    }

    private boolean isPositionHeader (int position) {
        return position == 0;
    }

    private boolean isPositionFooter (int position) {
        return position == (books.size() + 1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,final int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            final Book book = books.get(position - 1);

            itemViewHolder.bookName.setText(book.getBookName());
            itemViewHolder.authorName.setText(book.getAuthor());
            itemViewHolder.status.setText(book.getStatus());
            itemViewHolder.status.setTextColor(book.getStatusColor());
            itemViewHolder.summary.setText(book.getShortBookDescription());
            if (itemViewHolder.imageView != null) {
                new ImageDownloaderTask(itemViewHolder.imageView).setBitmapLruCache(cache).execute(book.getImageURL());
            }

            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(RecyclerViewActivity.RECYCLERVIEWACTIVITYTAG, "Click");
                    Bundle args = new Bundle();
                    args.putString(bookNameKey, book.getBookName());
                    args.putString(authorNameKey, book.getAuthor());
                    args.putString(summaryKey, book.getBookDescription());
                    args.putString(imageURLKey, book.getImageURL());

                    Intent intent = new Intent(v.getContext(), DetailViewActivity.class);
                    intent.putExtra(bookAdapterKey, args);
                    v.getContext().startActivity(intent);
                }
            });
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            if (position == 0) {
                headerViewHolder.headerText.setText("用户" + "共收藏了 " + books.size() + " 本书");
            } else {
                headerViewHolder.headerText.setText("没有更多内容了");
            }
        }


    }


    @Override
    public int getItemCount() {
        return books.size() + 2;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}
