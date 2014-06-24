package com.joshrincon.blogreaderscratch.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import com.joshrincon.blogreaderscratch.app.R;

public class RSSHelper {

    private String line;
    private String title;
    private String link;

    public RSSHelper() {

    }

    public void RSSHandler(String line) {

        if (line.contains("<title>")) {
            int firstPos = line.indexOf("<title>");
            String temp = line.substring(firstPos);
            temp = temp.replace("<title>", "");
            int lastPos = temp.indexOf("</title>");
            temp = temp.substring(0, lastPos);
            title += temp + "\n";
            System.out.print(title);
        }

        if (line.contains("<link>")) {
            int firstPos = line.indexOf("<link>");
            String temp = line.substring(firstPos);
            temp = temp.replace("<link>", "");
            int lastPos = temp.indexOf("</link>");
            temp = temp.substring(0, lastPos);
            link += temp + "\n";
            System.out.print(link);
        }
    }

    public int logException(String tag, Exception e) {
        return Log.e(tag, "Exception caught: ", e);
    }

    public boolean isNetworkAvailable(Context c) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    public void updateDisplayForError(Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(c.getString(R.string.title));
        builder.setMessage(c.getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = new TextView(c);
        emptyTextView.setText(c.getString(R.string.no_items));
    }
}
