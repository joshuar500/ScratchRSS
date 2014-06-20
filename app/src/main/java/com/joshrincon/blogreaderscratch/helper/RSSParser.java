package com.joshrincon.blogreaderscratch.helper;

public class RSSParser {

    private String line;
    private String title;
    private String link;

    public RSSParser() {

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

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }
}
