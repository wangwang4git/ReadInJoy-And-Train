package com.example.leopeng.recyclerviewdemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private String username;
    private ProgressDialog progressDialog;
    public static String BOOKJSONKEY = "BOOKJSON";
    public static String USERNAMEKEY = "USERNAMEKEY";

    private final LruCache<String, String> lruCache;
    private final static int cacheSize = 4 * 1024 * 1024;

    public MainActivity() {
        lruCache = new LruCache<>(cacheSize);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jsonTask(View view) {
        EditText editText = (EditText) findViewById(R.id.username);
        username = editText.getText().toString();

        Log.d("Username: ", username);

        synchronized (lruCache) {
            if (lruCache.get(username) != null) {
                Intent intent = new Intent(this, RecyclerViewActivity.class);
                intent.putExtra(BOOKJSONKEY, lruCache.get(username));
                intent.putExtra(USERNAMEKEY, username);

                startActivity(intent);
            } else {
                new JsonTask().execute("https://api.douban.com/v2/book/user/" + username +"/collections?count=100");
            }
        }
    }

    private class JsonTask extends AsyncTask<String, Integer, String> {

        private volatile boolean running = true;

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
                        Log.d("per", "percent: " + i);
                        i++;
                    }
                    catch (Exception e) {
                        Log.i("makemachine", e.getMessage());
                    }
                }
                Long timeStart = System.currentTimeMillis();
                Log.d("Reader", "Reader begin! ");

                InputStream stream = connection.getInputStream();
                Log.d("Timeout", "time: " + connection.getConnectTimeout());
                reader = new BufferedReader(new InputStreamReader(stream));

                Log.d("Reader", "Reader got! " + (System.currentTimeMillis() - timeStart));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);
                }

                synchronized (lruCache) {
                    if (lruCache.get(username) == null) {
                        lruCache.put(username, buffer.toString());
                    }
                }

                while (i <= 50) {
                    try {
                        Thread.sleep(25);
                        publishProgress(i);
                        Log.d("per", "percent: " + i);
                        i++;
                    }
                    catch (Exception e) {
                        Log.i("makemachine", e.getMessage());
                    }
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
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
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            Intent intent = new Intent(MainActivity.this, RecyclerViewActivity.class);
            intent.putExtra(BOOKJSONKEY, s);

            EditText editText = (EditText) findViewById(R.id.username);
            username = editText.getText().toString();
            intent.putExtra(USERNAMEKEY, username);

            if (running) {
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
    }
}
