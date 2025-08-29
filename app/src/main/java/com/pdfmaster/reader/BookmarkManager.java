package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarkManager {

    private Context context;
    private SharedPreferences preferences;
    private String currentFileUri;

    public BookmarkManager(Context context, String fileUri) {
        this.context = context;
        this.currentFileUri = fileUri;
        this.preferences = context.getSharedPreferences("bookmarks_" + fileUri.hashCode(), Context.MODE_PRIVATE);
    }

    public void addBookmark(int pageNumber, String title) {
        Set<String> bookmarks = new HashSet<>(preferences.getStringSet("bookmarks", new HashSet<>()));
        String bookmark = pageNumber + "|" + title + "|" + System.currentTimeMillis();
        bookmarks.add(bookmark);
        preferences.edit().putStringSet("bookmarks", bookmarks).apply();
    }

    public void removeBookmark(int pageNumber) {
        Set<String> bookmarks = new HashSet<>(preferences.getStringSet("bookmarks", new HashSet<>()));
        bookmarks.removeIf(bookmark -> bookmark.startsWith(pageNumber + "|"));
        preferences.edit().putStringSet("bookmarks", bookmarks).apply();
    }

    public List<Bookmark> getBookmarks() {
        Set<String> bookmarkStrings = preferences.getStringSet("bookmarks", new HashSet<>());
        List<Bookmark> bookmarks = new ArrayList<>();

        for (String bookmarkString : bookmarkStrings) {
            String[] parts = bookmarkString.split("\\|");
            if (parts.length >= 3) {
                int pageNumber = Integer.parseInt(parts[0]);
                String title = parts[1];
                long timestamp = Long.parseLong(parts[2]);
                bookmarks.add(new Bookmark(pageNumber, title, timestamp));
            }
        }

        // Sort by page number
        bookmarks.sort((a, b) -> Integer.compare(a.getPageNumber(), b.getPageNumber()));
        return bookmarks;
    }

    public boolean isBookmarked(int pageNumber) {
        Set<String> bookmarks = preferences.getStringSet("bookmarks", new HashSet<>());
        return bookmarks.stream().anyMatch(bookmark -> bookmark.startsWith(pageNumber + "|"));
    }

    public static class Bookmark {
        private int pageNumber;
        private String title;
        private long timestamp;

        public Bookmark(int pageNumber, String title, long timestamp) {
            this.pageNumber = pageNumber;
            this.title = title;
            this.timestamp = timestamp;
        }

        public int getPageNumber() { return pageNumber; }
        public String getTitle() { return title; }
        public long getTimestamp() { return timestamp; }
    }
}