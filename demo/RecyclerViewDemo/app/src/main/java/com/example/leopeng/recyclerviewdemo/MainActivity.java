package com.example.leopeng.recyclerviewdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String username;
    private ProgressDialog progressDialog;
    private EditText editText;
    public static String BOOKJSONKEY = "BOOKJSON";
    public static String USERNAMEKEY = "USERNAMEKEY";
    public final static String MAINACTIVITYTAG = "MainActivity";

    private final LruCache<String, String> lruCache;
    private final static int cacheSize = 4 * 1024 * 1024;

    public MainActivity() {
        lruCache = new LruCache<>(cacheSize);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.username);
    }

    public void jsonTask(View view) {
        username = editText.getText().toString();

        Log.d(MAINACTIVITYTAG, "Username: " + username);

        synchronized (lruCache) {
            // Hit memory cache
            String jsonString = lruCache.get(username);
            if (jsonString != null && !jsonString.isEmpty()) {
                Intent intent = new Intent(this, RecyclerViewActivity.class);
                intent.putExtra(BOOKJSONKEY, lruCache.get(username));
                intent.putExtra(USERNAMEKEY, username);
                Log.d(MAINACTIVITYTAG, "Hit memory cache!");
                startActivity(intent);
            } else {
                // Hit disk cache
                jsonString = getUserCacheJSONString();
                if (jsonString != null && !jsonString.isEmpty()) {
                    Intent intent = new Intent(this, RecyclerViewActivity.class);
                    intent.putExtra(BOOKJSONKEY, jsonString);
                    intent.putExtra(USERNAMEKEY, username);
                    Log.d(MAINACTIVITYTAG, "Hit disk cache!");
                    startActivity(intent);
                } else {
                    String url = "https://api.douban.com/v2/book/user/" + username + "/collections?count=100";
                    Log.d(MAINACTIVITYTAG, url);
                    new JsonTask().execute(url);
                }
            }
        }
    }

    private String getUserCacheJSONString() {
        File file = null;
        BufferedReader reader = null;
        try {
            file = new File(getCacheDir(), username + "_cache");
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class JsonTask extends AsyncTask<String, Integer, String> {

        private volatile boolean running = true;
        private volatile boolean network = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Please wait!");
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(true);
                }
            });
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int i = 0;
                Random random = new Random();
                int stopTime = random.nextInt(35);
                while (i <= stopTime) {
                    try {
                        Thread.sleep(15);
                        publishProgress(i);
                        i++;
                    }
                    catch (Exception e) {
                        Log.i(MAINACTIVITYTAG, e.getMessage());
                    }
                }
                Long timeStart = System.currentTimeMillis();
                Log.d(MAINACTIVITYTAG, "Reader begin! ");

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                Log.d(MAINACTIVITYTAG, "Reader finish! " + (System.currentTimeMillis() - timeStart));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);
                }

                // Store to memory cache
                synchronized (lruCache) {
                    if (lruCache.get(username) == null && !buffer.toString().isEmpty() && isTotalNotZero(buffer.toString())) {
                        lruCache.put(username, buffer.toString());
                    }
                }

                // Store to temp file
                if (!buffer.toString().isEmpty() && isTotalNotZero(buffer.toString())) {
                    Log.d(MAINACTIVITYTAG, buffer.toString());
                    storeFile(username, buffer.toString());
                }

                while (i <= 50) {
                    try {
                        Thread.sleep(25);
                        publishProgress(i);
                        i++;
                    }
                    catch (Exception e) {
                        Log.i(MAINACTIVITYTAG, e.getMessage());
                    }
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                network = false;
                if (running) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Please check your network setting.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
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
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
            intent.putExtra(BOOKJSONKEY, s);

            username = editText.getText().toString();
            intent.putExtra(USERNAMEKEY, username);

            if (running && network) {
                startActivity(intent);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (running) {
                super.onProgressUpdate(percent);
                progressDialog.setMessage(percent[0] * 2 + "%");
            }
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            running = false;
        }

        private File getTempFile(String name) {
            File file = null;
            try {
                file = new File(getCacheDir(), name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return file;
        }

        private void storeFile(String filename, String value) {
            File tempFile = getTempFile(filename + "_cache");
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(tempFile);
                outputStream.write(value.getBytes());
                Log.d(MAINACTIVITYTAG, "Store cache to disk: " + filename);
                Log.d(MAINACTIVITYTAG, "Cache File: " + tempFile.getName());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean isTotalNotZero(String json) {
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
}
