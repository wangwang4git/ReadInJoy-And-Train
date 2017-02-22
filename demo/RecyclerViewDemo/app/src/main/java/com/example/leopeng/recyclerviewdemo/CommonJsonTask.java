package com.example.leopeng.recyclerviewdemo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Created by leopeng on 21/02/2017.
 */

public class CommonJsonTask extends AsyncTask<String, Integer, String> {

    private volatile boolean running = true;
    private volatile boolean network = true;
    private Context mContext;

    public LruCache<String, String> lruCache;
    public String cacheKey;

    private String TAG;

    public CommonJsonTask(Context context) {
        this.mContext = context;
        this.TAG = CommonJsonTask.class.getName();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mContext, "Loading ...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            Long timeStart = System.currentTimeMillis();
            Log.d(TAG, "Connect begin: " + timeStart);

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            Log.d(TAG, "Connect end: " + System.currentTimeMillis());
            Log.d(TAG, "Time used: " + (System.currentTimeMillis() - timeStart));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                Log.d(TAG, "Response: > " + line);
            }

            // Store to memory cache
            if (lruCache != null) {
               synchronized (lruCache) {
                   if (cacheKey != null && !cacheKey.isEmpty()) {
                       if (isTotalNotZero(buffer.toString())) {
                           lruCache.put(cacheKey, buffer.toString());
                           Log.d(TAG, "Store cache to memory");
                       }
                   }
               }
            }

            // Store to disk cache
           if (cacheKey != null && !cacheKey.isEmpty())  {
               if (isTotalNotZero(buffer.toString())) {
                   storeCacheFile(cacheKey + "_cache", buffer.toString());
               }
           }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            network = false;
            if (running) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Please check your network setting.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (SocketTimeoutException e) {
            if (running) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Please check your network setting.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (mContext instanceof RecyclerViewActivity) {
            ((RecyclerViewActivity) mContext).JSONParse(s);
            Toast.makeText(mContext, "Refresh Succeed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        running = false;
    }

    /**
     * 存储缓存至disk中
     * @param filename 文件名
     * @param value 文件内容
     */
    public void storeCacheFile(String filename, String value) {
        try {
            File file = new File(mContext.getCacheDir(), filename);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(value.getBytes());
            Log.d(TAG, "Store cache to disk: " + filename);
            Log.d(TAG, "Cache File: " + file.getName());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断返回的数据图书的数量不为零
     * 仅适用于该demo
     * @param json
     * @return
     */
    public boolean isTotalNotZero(String json) {
        if (json != null && !json.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                int total = jsonObject.getInt("total");
                if (total > 0) {
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
