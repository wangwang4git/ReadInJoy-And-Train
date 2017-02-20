package com.example.leopeng.recyclerviewdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
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
    private Bundle args;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        bookCover = (ImageView) findViewById(R.id.bookCover);
        bookName = (TextView) findViewById(R.id.bookName);
        authorName = (TextView) findViewById(R.id.authorName);
        summary = (TextView) findViewById(R.id.summary);

        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    private void handleIntent(Intent intent) {
        args = intent.getBundleExtra(BookAdapter.bookAdapterKey);

        bookName.setText(args.getString(BookAdapter.bookNameKey));
        authorName.setText(args.getString(BookAdapter.authorNameKey));
        summary.setText(args.getString(BookAdapter.summaryKey));

        setTitle(args.getString(BookAdapter.bookNameKey));

        if (bookCover != null) {
            new ImageDownloaderTask(bookCover).execute(args.getString(BookAdapter.imageURLKey));
        }
    }
}
