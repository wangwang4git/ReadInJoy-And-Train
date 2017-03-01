package com.example.leopeng.recyclerviewdemo.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.example.leopeng.recyclerviewdemo.activity.MainActivity;
import com.example.leopeng.recyclerviewdemo.activity.RecyclerViewActivity;
import com.example.leopeng.recyclerviewdemo.model.Book;

import net.steamcrafted.loadtoast.LoadToast;

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
import java.util.List;

/**
 * Created by leopeng on 21/02/2017.
 */

public class CommonJsonTask extends AsyncTask<String, Integer, String> {

    private volatile boolean running = true;
    private volatile boolean network = true;
    private Context mContext;
    private LoadToast loadToast;

    public String cacheKey;
    public String searchKey;
    public boolean isAdd = false;
    public boolean isAddToHead = false;

    private String TAG;

    public CommonJsonTask(Context context) {
        this.mContext = context;
        this.TAG = CommonJsonTask.class.getName();
        this.loadToast = new LoadToast(context);
        loadToast.setTranslationY(200);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isAddToHead) {
            loadToast.show();
        }
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

            // Store to disk cache
           if (cacheKey != null && !cacheKey.isEmpty())  {
               if (isTotalNotZero(buffer.toString())) {
                   storeCacheFile(cacheKey, buffer.toString());
               }
           }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            network = false;
            if (mContext instanceof RecyclerViewActivity) {
                ((RecyclerViewActivity) mContext).isLoading = false;
                ((RecyclerViewActivity) mContext).isNoMore = false;
                ((RecyclerViewActivity) mContext).network = network;
            }
        } catch (SocketTimeoutException e) {
            network = false;
            if (mContext instanceof RecyclerViewActivity) {
                ((RecyclerViewActivity) mContext).isLoading = false;
                ((RecyclerViewActivity) mContext).isNoMore = false;
                ((RecyclerViewActivity) mContext).network = network;
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
            List<Book> list = ((RecyclerViewActivity) mContext).JSONParse(s);
            if (list != null && !list.isEmpty()) {
                if (isAdd) {
                    if (isAddToHead) {
                        ((RecyclerViewActivity) mContext).addBookListToHead(list);
                    } else {
                        ((RecyclerViewActivity) mContext).addBookListToEnd(list);
                    }
                } else {
                    ((RecyclerViewActivity) mContext).updateBookList(list);
                }
            }
            ((RecyclerViewActivity) mContext).isLoading = false;

            if (s != null) {
                loadToast.success();
            } else {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadToast.error();
                    }
                }, 300);
            }

            ((RecyclerViewActivity) mContext).refreshFinish();
        }

        if (mContext instanceof MainActivity) {
            Intent intent = new Intent(mContext, RecyclerViewActivity.class);
            intent.putExtra(Constant.SEARCH_KEY, (searchKey != null && !searchKey.isEmpty()) ? searchKey : "");
            intent.putExtra(Constant.BOOK_JSON_KEY, s);
            intent.putExtra(Constant.FIRST_LOAD_KEY, true);
            mContext.startActivity(intent);
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
