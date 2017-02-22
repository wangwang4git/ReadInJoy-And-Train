package com.example.leopeng.recyclerviewdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by leopeng on 14/02/2017.
 */

public class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewWeakReference;
    private LruCache<String, Bitmap> bitmapLruCache;

    public ImageDownloaderTask(ImageView imageView) {
        imageViewWeakReference = new WeakReference<>(imageView);
    }

    public ImageDownloaderTask setBitmapLruCache(LruCache cache) {
        bitmapLruCache = (LruCache<String, Bitmap>) cache;
        return this;
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
        if (url == null || url.isEmpty()) {return null;}
        HttpURLConnection urlConnection = null;
        try {
            if (bitmapLruCache != null) {
                synchronized (bitmapLruCache) {
                    // Hit memory cache
                    if (bitmapLruCache.get(url) != null) {
                        Log.d(ImageDownloaderTask.class.getName(), "Image hit memory cache. " + url);
                        return bitmapLruCache.get(url);
                    }
                }
            }

            // Hit disk cache
            Bitmap bitmap = getImageFromDisk(url);
            if (bitmap != null) {
                storeBitmapToMemoryCache(bitmap, url);
                return bitmap;
            }

            URL uri = new URL(url);
            urlConnection = (HttpURLConnection) uri.openConnection();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != 200) {
                return null;
            }

            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }

            // Store bitmap to memory
            storeBitmapToMemoryCache(bitmap, url);

            // Store bitmap to disk
            storeBitmap(bitmap, url);
            return bitmap;
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

    private Bitmap getImageFromDisk(String url) {
        Bitmap bitmap = null;
        try {
            File file = new File(imageViewWeakReference.get().getContext().getCacheDir(), url.substring(url.lastIndexOf('/') + 1));
            String filePath = file.getPath();
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(ImageDownloaderTask.class.getName(), "Get image from disk: " + url);
        return bitmap;
    }

    private void storeBitmap(Bitmap bitmap, String url) {
        try {
            File file = new File(imageViewWeakReference.get().getContext().getCacheDir(), url.substring(url.lastIndexOf('/') + 1));
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            Log.d(ImageDownloaderTask.class.getName(), "Store image to disk: " + url);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void storeBitmapToMemoryCache(Bitmap bitmap, String url) {
        if (bitmapLruCache != null) {
            // Store bitmap to memory cache
            synchronized (bitmapLruCache) {
                if (bitmapLruCache.get(url) == null) {
                    bitmapLruCache.put(url, bitmap);
                    Log.d("Cache image to memory: ", url);
                }
            }
        }
    }
}
