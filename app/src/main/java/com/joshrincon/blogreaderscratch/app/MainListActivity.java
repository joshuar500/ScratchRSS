package com.joshrincon.blogreaderscratch.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.joshrincon.blogreaderscratch.helper.RssAtomFeedRetriever;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainListActivity extends ListActivity {

    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected ProgressBar mProgressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_LINK = "link";
    private SyndFeed feed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);

            GetRSSPostsTask getRSSPostsTask = new GetRSSPostsTask();
            getRSSPostsTask.execute();

        } else {
            Toast.makeText(this, "Network is unavailable.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


        try {
            // get position of what user is choosing and set url
            SyndEntry getFeedPos = (SyndEntry) feed.getEntries().get(position);
            String rssTitle = getFeedPos.getTitle();
            String rssDesc = getFeedPos.getDescription().getValue();

            String rssUrl = getFeedPos.getUri();

            Intent intent = new Intent(this, RSSViewActivity.class);
            intent.setData(Uri.parse(rssUrl));
            intent.putExtra("EXTRA_TITLE", rssTitle);
            intent.putExtra("EXTRA_DESC", rssDesc);

            startActivity(intent);
        } catch (Exception e) {
            logException(e);
        }
    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception caught: ", e);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void handleRSSResponse(SyndFeed feed) {

        mProgressBar.setVisibility(View.INVISIBLE);

        if(feed == null) {
            updateDisplayForError();
        } else {
            try {
                ArrayList<HashMap<String, String>> rssPosts =
                        new ArrayList<HashMap<String, String>>();
                for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                        String title = entry.getTitle();
                        String link = entry.getUri();

                        System.out.println("THIS IS FROM HANDLERSS RESPONSE" + title + link);
                        HashMap<String, String> rssPost = new HashMap<String, String>();
                        rssPost.put(KEY_TITLE, title);
                        rssPost.put(KEY_LINK, link);
                        rssPosts.add(rssPost);
                }

                String[] keys  = {KEY_TITLE, KEY_LINK};
                int[] ids = { android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, rssPosts,
                        android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);

            } catch (Exception e) {
                logException(e);
            }
        }
    }

    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    private class GetRSSPostsTask extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object[] objects) {

            int responseCode;

            try {
                URL rssFeedUrl = new URL("http://feeds.feedburner.com/TechCrunch/startups");
                HttpURLConnection connection = (HttpURLConnection) rssFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    RssAtomFeedRetriever rssAtomFeedRetriever = new RssAtomFeedRetriever();
                    feed = rssAtomFeedRetriever.getMostRecentNews("http://feeds.feedburner.com/TechCrunch/startups");

                    System.out.println("FROM ASYNCTASK" + feed.getEntries());

                } else{
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }


            } catch (MalformedURLException e) {
                logException(e);
                e.printStackTrace();
            } catch (IOException e) {
                logException(e);
                e.printStackTrace();
            } catch (Exception e) {
                logException(e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            handleRSSResponse(feed);
        }
    }
}
