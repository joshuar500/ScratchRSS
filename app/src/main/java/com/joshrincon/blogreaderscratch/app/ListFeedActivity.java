package com.joshrincon.blogreaderscratch.app;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;
import com.joshrincon.blogreaderscratch.helper.RssSortByDate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ListFeedActivity extends ListActivity {

    protected String mUrl;
    private SyndFeed feed;
    protected ProgressBar mProgressBar;
    private static final RssSortByDate sortByDate = new RssSortByDate();
    private ArrayList<SyndEntry> entrieS = new ArrayList<SyndEntry>();
    private final String KEY_ENTRY_TITLE = "feed_title";
    private final String KEY_LINK = "link";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_feed);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarListEntries);

        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();

        System.out.println("GOT URL FROM LISTACTIVITY" + mUrl);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);

            GetRSSPostsTask getRSSPostsTask = new GetRSSPostsTask();
            getRSSPostsTask.execute();
        } else {
            Toast.makeText(this, "Network is unavailable.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        System.out.println("NETWORK AVAILABLE LISTACTIVITY" + mUrl);

        return isAvailable;
    }

    private class GetRSSPostsTask extends AsyncTask<Object, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Object[] objects) {

            int responseCode;

            try {

                URL testURL = new URL(mUrl);
                HttpURLConnection connection = (HttpURLConnection) testURL.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    URL rssFeedUrl = new URL(mUrl);
                    SyndFeedInput input = new SyndFeedInput();
                    feed = input.build(new XmlReader(rssFeedUrl));

                    System.out.println("FROM ASYNCTASK" + feed.getTitle());


                    for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                        entrieS.add(entry);
                    }

                } else{
                    Log.i("LISTFEEDACTIVITY", "Unsuccessful HTTP Response Code: " + responseCode);
                }
            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            handleRSSResponse(entrieS);
        }
    }

    private void handleRSSResponse(ArrayList<SyndEntry> entries) {

        mProgressBar.setVisibility(View.INVISIBLE);


        if(mUrl == null) {
            //updateDisplayForError();
            System.out.println("ERROR IN LISTFEEDACTIVITY");
        } else {
            //SAVE mURLS with SavedPreferences
            try {

                Collections.sort(entrieS, sortByDate);

                ArrayList<HashMap<String, String>> rssPosts =
                        new ArrayList<HashMap<String, String>>();

                for (SyndEntry entry : entries) {
                    String title = entry.getTitle();
                    String link = entry.getUri();

                    System.out.println("THIS IS FROM HANDLERSS RESPONSE" + title + link);
                    HashMap<String, String> rssPost = new HashMap<String, String>();
                    rssPost.put(KEY_ENTRY_TITLE, title);
                    rssPost.put(KEY_LINK, link);
                    rssPosts.add(rssPost);
                }


                String[] keys  = {KEY_ENTRY_TITLE, KEY_LINK};
                int[] ids = { android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, rssPosts,
                        android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
