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
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.joshrincon.blogreaderscratch.helper.RSSParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class MainListActivity extends ListActivity {

    public static final int NUMBER_OF_POSTS = 20;
    public static final String TAG = MainListActivity.class.getSimpleName();
    protected List<ArrayList<String>> mRSSData = new ArrayList<ArrayList<String>>();
    protected List<String> mRSSLinks = new ArrayList<String>();
    protected ProgressBar mProgressBar;
    private final String KEY_TITLE = "title";
    private final String KEY_LINK = "link";

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

        /*Resources resources = getResources();
        mBlogPostTitles = resources.getStringArray(R.array.android_names);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mBlogPostTitles);
        setListAdapter(adapter);
        */

        /*String message = getString(R.string.no_items);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();*/
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


        try {
            String blogURL = "http://www.google.com";

            //explicitly say to open blogwebviewactivity class as opposed to "implying"
            Intent intent = new Intent(this, RSSViewActivity.class);
            intent.setData(Uri.parse(blogURL));

            /* THIS OPENS A WEB BROWSER
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(blogURL));*/

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

    private void handleRSSResponse() {

        mProgressBar.setVisibility(View.INVISIBLE);

        if(mRSSData == null) {
            updateDisplayForError();
        } else {
            try {
                ArrayList<HashMap<String, String>> rssPosts =
                        new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < mRSSData.size()-1; i++) {
                    List<String> getTitle = mRSSData.get(i);
                    List<String> getLink = mRSSData.get(i+1);
                    String title = "";
                    String link = "";
                    for(int p = 0; p < getTitle.size(); p++) {
                        title = Html.fromHtml(getTitle.get(p)).toString();
                        link = Html.fromHtml(getLink.get(p)).toString();
                        System.out.println("THIS IS FROM HANDLERSS RESPONSE" + title + link);
                        HashMap<String, String> rssPost = new HashMap<String, String>();
                        rssPost.put(KEY_TITLE, title);
                        rssPost.put(KEY_LINK, link);
                        rssPosts.add(rssPost);

                    }
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

    private class GetRSSPostsTask extends AsyncTask<Object, Void, List<ArrayList<String>>> {

        @Override
        protected List<ArrayList<String>> doInBackground(Object[] objects) {

            int responseCode;

            List<ArrayList<String>> sourceCode = new ArrayList<ArrayList<String>>();

            ArrayList<String> listTitles;
            ArrayList<String> listLinks;

            String tempTitle = "";
            String tempLink = "";

            try {
                URL rssFeedUrl = new URL("http://feeds.feedburner.com/TechCrunch/startups");
                HttpURLConnection connection = (HttpURLConnection) rssFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(rssFeedUrl.openStream()));

                    String line;
                    RSSParser rssParser = new RSSParser();
                    while ((line = reader.readLine()) != null) {
                        rssParser.RSSHandler(line);
                    }
                    tempTitle = rssParser.getTitle();
                    tempLink = rssParser.getLink();


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

            List<String> titles = Arrays.asList(tempTitle.split("\\n"));
            listTitles = new ArrayList<String>(titles);
            sourceCode.add(listTitles);

            List<String> links = Arrays.asList(tempLink.split("\\n"));
            listLinks = new ArrayList<String>(links);
            sourceCode.add(listLinks);

            return sourceCode;
        }

        @Override
        protected void onPostExecute(List<ArrayList<String>> result) {
            mRSSData = result;
            handleRSSResponse();
        }
    }
}
