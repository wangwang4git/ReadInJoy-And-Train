package com.example.leopeng.recyclerviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by leopeng on 14/02/2017.
 */

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewWeakReference;
    private final LruCache<String, Bitmap> bitmapLruCache;

    public ImageDownloaderTask(ImageView imageView, LruCache cache) {
        imageViewWeakReference = new WeakReference<>(imageView);
        bitmapLruCache = (LruCache<String, Bitmap>) cache;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return downloadBitmap(params[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewWeakReference != null) {
            ImageView imageView = imageViewWeakReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageBitmap(null);
                }
            }
        }
    }

    private Bitmap downloadBitmap(String url) {
        HttpURLConnection urlConnection = null;
        try {

            if (bitmapLruCache != null) {
                synchronized (bitmapLruCache) {
                    if (url != null && bitmapLruCache.get(url) != null) {
                        Log.d("Cache Size: ", Integer.toString(bitmapLruCache.size()));
                        return bitmapLruCache.get(url);
                    }
                }
            }


            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != 200) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmapLruCache != null) {
                    synchronized (bitmapLruCache) {
                        if (bitmapLruCache.get(url) == null) {
                            bitmapLruCache.put(url, bitmap);
                            Log.d("Cache image: ", url);
                        }
                    }
                }
                return bitmap;
            }
        } catch (Exception e) {
            if (urlConnection != null) {
                urlConnection.disconnect();
                Log.w("ImageDownloader", "Error downloading image from " + url);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }
}
