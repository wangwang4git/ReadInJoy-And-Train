package com.example.leopeng.recyclerviewdemo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Map;

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

        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        args = intent.getBundleExtra(BookAdapter.bookAdapterKey);

        bookName.setText(args.getString(BookAdapter.bookNameKey));
        authorName.setText(args.getString(BookAdapter.authorNameKey));
        summary.setText(args.getString(BookAdapter.summaryKey));

        String average = args.getString("average");
        if (average != null) {
            Log.d(DetailViewActivity.class.getName(), "Rating: " + Float.parseFloat(average));
            ratingBar.setRating(Float.parseFloat(average) / 2);
        }

        setTitle(args.getString(BookAdapter.bookNameKey));

        if (bookCover != null) {
            new ImageDownloaderTask(bookCover).execute(args.getString(BookAdapter.imageURLKey));
        }
    }
}
