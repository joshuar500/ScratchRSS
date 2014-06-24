package com.joshrincon.blogreaderscratch.app;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;
import com.joshrincon.blogreaderscratch.helper.RSSHelper;
import com.joshrincon.blogreaderscratch.helper.RssSortByDate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class MainListActivity extends ListActivity {

    public static final String TAG = MainListActivity.class.getSimpleName();
    private static final RssSortByDate sortByDate = new RssSortByDate();
    protected ProgressBar mProgressBar;
    private final String KEY_FEED_TITLE = "feed_title";
    private final String KEY_LINK = "link";
    private final String KEY_PREFURL = "url_";
    private SyndFeed feed;
    private ArrayList<SyndFeed> feedS = new ArrayList<SyndFeed>();
    RSSHelper rssHelper = new RSSHelper();

    private ArrayList<String> mUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // TODO: Create button that removes feeds

        //Load URLs if they are saved via SavedPreferences
        loadUrls(this);

        if (rssHelper.isNetworkAvailable()) {
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
            // Get position of user touch
            // TODO: this needs to be mapped. currently getting mUrls position and not entrieS position
            String feedUrl = mUrls.get(position);

            Intent intent = new Intent(this, ListFeedActivity.class);
            intent.setData(Uri.parse(feedUrl));
            startActivity(intent);
        } catch (Exception e) {
            rssHelper.logException(TAG, e);
        }
    }

    private void handleRSSResponse(ArrayList<SyndFeed> feeds) {

        mProgressBar.setVisibility(View.INVISIBLE);

        if(mUrls == null) {
            rssHelper.updateDisplayForError();
        } else {
            //SAVE mURLS with SavedPreferences
            saveUrls(this);
            try {

                ArrayList<HashMap<String, String>> rssPosts =
                        new ArrayList<HashMap<String, String>>();

                    for (SyndFeed feed : feeds) {
                        String title = feed.getTitle();
                        String link = feed.getLink();

                        System.out.println("THIS IS FROM HANDLERSS RESPONSE" + title + link);
                        HashMap<String, String> rssPost = new HashMap<String, String>();
                        rssPost.put(KEY_FEED_TITLE, title);
                        rssPost.put(KEY_LINK, link);
                        rssPosts.add(rssPost);
                    }


                String[] keys  = {KEY_FEED_TITLE, KEY_LINK};
                int[] ids = { android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, rssPosts,
                        android.R.layout.simple_list_item_2, keys, ids);
                setListAdapter(adapter);

            } catch (Exception e) {
                rssHelper.logException(TAG, e);
            }
        }
    }

    private class GetRSSPostsTask extends AsyncTask<Object, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Object[] objects) {

            int responseCode;

            try {

                URL testURL = new URL(mUrls.get(0));
                HttpURLConnection connection = (HttpURLConnection) testURL.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    for (String mUrl : mUrls) {
                        URL rssFeedUrl = new URL(mUrl);
                        SyndFeedInput input = new SyndFeedInput();
                        feed = input.build(new XmlReader(rssFeedUrl));
                        feedS.add(feed);

                        System.out.println("FROM ASYNCTASK" + feed.getTitle());
                    }
                    /*for(SyndFeed f : feedS) {
                        for (SyndEntry entry : (List<SyndEntry>) f.getEntries()) {
                            entrieS.add(entry);
                        }
                    }*/

                } else{
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            } catch (MalformedURLException e) {
                rssHelper.logException(TAG, e);
                e.printStackTrace();
            } catch (IOException e) {
                rssHelper.logException(TAG, e);
                e.printStackTrace();
            } catch (Exception e) {
                rssHelper.logException(TAG, e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            super.onPostExecute(result);
            handleRSSResponse(feedS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_addFeed) {
            addRssFeedDialog();
            System.out.println("ACTION ADDFEED CLICKED");
        }
        return super.onOptionsItemSelected(item);
    }

    private void addRssFeedDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainListActivity.this);

        alert.setTitle("Add feed");
        alert.setMessage("Please add Url with http:// in front.");

        final EditText input = new EditText(MainListActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String value = input.getText().toString();
                addFeedToList(value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alert.show();
    }

    private void addFeedToList(String input) {
        // TODO:if starts with http://

        mUrls.add(input);

        if (rssHelper.isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);

            GetRSSPostsTask getRSSPostsTask = new GetRSSPostsTask();
            getRSSPostsTask.execute();

        } else {
            Toast.makeText(this, "Network is unavailable.", Toast.LENGTH_LONG).show();
        }

    }

    public void saveUrls(Context context) {
        SharedPreferences settings = context.getSharedPreferences("URLS", 0);
        SharedPreferences.Editor editor = settings.edit();

        int size = mUrls.size();
        editor.putInt("numOfUrls", size);

        //clear saved preferences first
        for(int i = 0; i < size; i++) {
            editor.remove(KEY_PREFURL + i);
        }

        //once cleared, add new values
        for (String mUrl : mUrls) {
            editor.putString(KEY_PREFURL, mUrl);
        }

        editor.commit();
    }

    public ArrayList<String> loadUrls(Context context) {
        SharedPreferences file = context.getSharedPreferences("URLS", 0);
        mUrls = new ArrayList<String>();
        int size = file.getInt("numOfUrls", 0);

        for(int i = 0; i < size; i++) {
            String url = file.getString(KEY_PREFURL, "https://s3.amazonaws.com/USACWeb/rss/headlines.rss");
            mUrls.add(url);
        }

        return mUrls;
    }
}
