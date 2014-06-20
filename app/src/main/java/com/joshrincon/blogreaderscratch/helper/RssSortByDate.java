package com.joshrincon.blogreaderscratch.helper;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;

import java.util.Comparator;

/**
 * Created by on 6/20/2014.
 */
public class RssSortByDate implements Comparator<SyndEntry> {

    @Override
    public int compare(SyndEntry syndEntry, SyndEntry syndEntry2) {
        return syndEntry2.getPublishedDate().compareTo(syndEntry.getPublishedDate());
    }
}
