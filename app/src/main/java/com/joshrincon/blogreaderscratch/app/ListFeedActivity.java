package com.joshrincon.blogreaderscratch.app;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.SyndFeedInput;
import com.google.code.rome.android.repackaged.com.sun.syndication.io.XmlReader;
import com.joshrincon.blogreaderscratch.helper.RSSHelper;
import com.joshrincon.blogreaderscratch.helper.RssSortByDate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class ListFeedActivity extends ListActivity {

    public static final String TAG = ListFeedActivity.class.getSimpleName();
    protected String mUrl;
    private SyndFeed feed;
    protected ProgressBar mProgressBar;
    private static final RssSortByDate sortByDate = new RssSortByDate();
    private ArrayList<SyndEntry> entrieS = new ArrayList<SyndEntry>();
    private final String KEY_ENTRY_TITLE = "feed_title";
    private final String KEY_LINK = "link";
    RSSHelper rssHelper = new RSSHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_feed);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarListEntries);

        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();

        System.out.println("GOT URL FROM LISTACTIVITY" + mUrl);

        if (rssHelper.isNetworkAvailable(this)) {
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        try {
            // get position of what user is choosing and set url

            SyndEntry getFeedPos = entrieS.get(position);

            String rssTitle = getFeedPos.getTitle();
            String rssDesc = getFeedPos.getDescription().getValue();

            Date publishedDate = getFeedPos.getPublishedDate();
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            String rssDate = df.format(publishedDate);

            String rssUri = getFeedPos.getUri();

            Intent intent = new Intent(this, RSSViewActivity.class);
            intent.setData(Uri.parse(rssUri));
            intent.putExtra("EXTRA_TITLE", rssTitle);
            intent.putExtra("EXTRA_DESC", rssDesc);
            intent.putExtra("EXTRA_DATE", rssDate);

            startActivity(intent);
        } catch (Exception e) {
            rssHelper.logException(TAG, e);
        }
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
            handleRSSResponse(entrieS);
        }
    }

    private void handleRSSResponse(ArrayList<SyndEntry> entries) {

        mProgressBar.setVisibility(View.INVISIBLE);


        if(mUrl == null) {
            rssHelper.updateDisplayForError();
            System.out.println("ERROR IN LISTFEEDACTIVITY");
        } else {
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
                rssHelper.logException(TAG, e);
                e.printStackTrace();
            }
        }
    }
}
