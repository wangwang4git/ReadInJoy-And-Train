package com.example.leopeng.recyclerviewdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.leopeng.recyclerviewdemo.util.Constant;
import com.example.leopeng.recyclerviewdemo.util.ImageDownloaderTask;
import com.example.leopeng.recyclerviewdemo.R;
import com.example.leopeng.recyclerviewdemo.model.BookAdapter;

import java.util.List;

import me.gujun.android.taggroup.TagGroup;

/**
 * Created by leopeng on 20/02/2017.
 */

public class DetailViewActivity extends AppCompatActivity {
    private ImageView bookCover;
    private TextView bookName;
    private TextView authorName;
    private TextView summary;
    private TextView updatedAt;
    private RatingBar ratingBar;
    private TextView ratingValue;
    private TextView status;
    private TagGroup tagGroup;
    private Bundle args;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        bookCover = (ImageView) findViewById(R.id.bookCover);
        bookName = (TextView) findViewById(R.id.bookName);
        authorName = (TextView) findViewById(R.id.authorName);
        summary = (TextView) findViewById(R.id.summary);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingValue = (TextView) findViewById(R.id.ratingValue);
        status = (TextView) findViewById(R.id.status);
        tagGroup = (TagGroup) findViewById(R.id.tags);

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        args = intent.getBundleExtra(Constant.BOOK_ADAPTER_KEY);

        if (args != null && !args.isEmpty()) {
            bookName.setText(args.getString(Constant.BOOK_NAME_KEY));
            authorName.setText(args.getString(Constant.AUTHOR_NAME_KEY));
            summary.setText(args.getString(Constant.SUMMARY_KEY));

            status.setText(args.getString(Constant.STATUS_KEY));
            status.setTextColor(args.getInt(Constant.STATUS_COLOR_KEY));

            String average = args.getString(Constant.AVERAGE_KEY);
            if (average != null) {
                Log.d(DetailViewActivity.class.getName(), "Rating: " + Float.parseFloat(average));
                ratingBar.setRating(Float.parseFloat(average) / 2);
                ratingValue.setText(average);
            }

            setTitle(args.getString(Constant.BOOK_NAME_KEY));

            if (bookCover != null) {
                new ImageDownloaderTask(bookCover).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, args.getString(Constant.IMAGE_URL_KEY));
            }

            // tags
            List<String> list = args.getStringArrayList(Constant.TAG_KEY);
            if (list != null && !list.isEmpty()) {
                tagGroup.setTags(list);
            }
        }

    }
}
