package com.joshrincon.blogreaderscratch.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class RSSViewActivity extends Activity {

    protected String mUrl;
    protected String mTitle;
    protected String mDesc;
    private TextView tTitle;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_web_view);

        Intent intent = getIntent();
        Uri blogUri = intent.getData();
        mUrl = blogUri.toString();

        mTitle = intent.getExtras().getString("EXTRA_TITLE");
        mDesc = intent.getExtras().getString("EXTRA_DESC");

        tTitle = (TextView) findViewById(R.id.titleTextView);
        tTitle.setText(mTitle);

        webView = (WebView) findViewById(R.id.coolWebView);
        webView.loadData(customHtml() + mDesc + "</body></html>", "text/html", "UTF-8");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.blog_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_share) {
            sharePost();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sharePost() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);

        startActivity(Intent.createChooser(shareIntent, "How do you want to share?"));
    }

    private String customHtml() {
        String html = "<html><head>"
                + "<style type=\"text/css\">body{color: #ffdec2; background-color: #000;}"
                + "img{max-width:100%; height:auto;}"
                + "</style></head>"
                + "<body>"
                + "<p align=\"left\">"
                + "</p>";

        return html;
    }
}
