package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookmarkManager {

    private static final String TAG = "BookmarkManager";
    private Context context;
    private SharedPreferences preferences;
    private String currentFileUri;

    public BookmarkManager(Context context, String fileUri) {
        this.context = context;
        this.currentFileUri = fileUri;
        this.preferences = context.getSharedPreferences("bookmarks_" + fileUri.hashCode(), Context.MODE_PRIVATE);
    }

    public void addBookmark(int pageNumber, String title) {
        try {
            Set<String> existingBookmarks = preferences.getStringSet("bookmarks", new HashSet<>());
            Set<String> bookmarks = existingBookmarks != null ? new HashSet<>(existingBookmarks) : new HashSet<>();

            String bookmark = pageNumber + "|" + title + "|" + System.currentTimeMillis();
            bookmarks.add(bookmark);

            boolean success = preferences.edit().putStringSet("bookmarks", bookmarks).commit();
            if (!success) {
                Log.e(TAG, "Failed to save bookmark to SharedPreferences");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding bookmark", e);
        }
    }

    public void removeBookmark(int pageNumber) {
        try {
            Set<String> existingBookmarks = preferences.getStringSet("bookmarks", new HashSet<>());
            if (existingBookmarks != null) {
                Set<String> bookmarks = new HashSet<>(existingBookmarks);
                bookmarks.removeIf(bookmark -> bookmark != null && bookmark.startsWith(pageNumber + "|"));
                preferences.edit().putStringSet("bookmarks", bookmarks).commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing bookmark", e);
        }
    }

    public List<Bookmark> getBookmarks() {
        List<Bookmark> bookmarks = new ArrayList<>();
        try {
            Set<String> bookmarkStrings = preferences.getStringSet("bookmarks", new HashSet<>());
            if (bookmarkStrings != null) {
                for (String bookmarkString : bookmarkStrings) {
                    if (bookmarkString != null && !bookmarkString.isEmpty()) {
                        try {
                            String[] parts = bookmarkString.split("\\|");
                            if (parts.length >= 3) {
                                int pageNumber = Integer.parseInt(parts[0]);
                                String title = parts[1];
                                long timestamp = Long.parseLong(parts[2]);
                                bookmarks.add(new Bookmark(pageNumber, title, timestamp));
                            }
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid bookmark format: " + bookmarkString, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading bookmarks", e);
        }

        // Sort by page number
        bookmarks.sort((a, b) -> Integer.compare(a.getPageNumber(), b.getPageNumber()));
        return bookmarks;
    }

    public boolean isBookmarked(int pageNumber) {
        try {
            Set<String> bookmarks = preferences.getStringSet("bookmarks", new HashSet<>());
            if (bookmarks != null) {
                return bookmarks.stream().anyMatch(bookmark ->
                        bookmark != null && bookmark.startsWith(pageNumber + "|"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking bookmark status", e);
        }
        return false;
    }

    public static class Bookmark {
        private int pageNumber;
        private String title;
        private long timestamp;

        public Bookmark(int pageNumber, String title, long timestamp) {
            this.pageNumber = pageNumber;
            this.title = title != null ? title : "Untitled";
            this.timestamp = timestamp;
        }

        public int getPageNumber() { return pageNumber; }
        public String getTitle() { return title; }
        public long getTimestamp() { return timestamp; }
    }
}
