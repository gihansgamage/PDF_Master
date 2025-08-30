package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalBookmarkManager {

    private static final String TAG = "GlobalBookmarkManager";
    private static final String GLOBAL_BOOKMARKS_PREF = "global_bookmarks";
    private Context context;
    private SharedPreferences preferences;

    public GlobalBookmarkManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(GLOBAL_BOOKMARKS_PREF, Context.MODE_PRIVATE);
    }

    public List<GlobalBookmark> getAllBookmarks() {
        List<GlobalBookmark> allBookmarks = new ArrayList<>();
        try {
            Set<String> fileUris = preferences.getStringSet("file_uris", new HashSet<>());
            if (fileUris != null) {
                for (String fileUri : fileUris) {
                    BookmarkManager bookmarkManager = new BookmarkManager(context, fileUri);
                    List<BookmarkManager.Bookmark> bookmarks = bookmarkManager.getBookmarks();

                    String fileName = getFileNameFromUri(fileUri);
                    for (BookmarkManager.Bookmark bookmark : bookmarks) {
                        allBookmarks.add(new GlobalBookmark(
                                fileUri, fileName, bookmark.getPageNumber(),
                                bookmark.getTitle(), bookmark.getTimestamp()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading all bookmarks", e);
        }

        // Sort by timestamp (most recent first)
        allBookmarks.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return allBookmarks;
    }

    public void registerFile(String fileUri) {
        try {
            Set<String> existingUris = preferences.getStringSet("file_uris", new HashSet<>());
            Set<String> fileUris = existingUris != null ? new HashSet<>(existingUris) : new HashSet<>();
            fileUris.add(fileUri);
            preferences.edit().putStringSet("file_uris", fileUris).commit();
        } catch (Exception e) {
            Log.e(TAG, "Error registering file", e);
        }
    }

    private String getFileNameFromUri(String uri) {
        try {
            String[] segments = uri.split("/");
            String fileName = segments[segments.length - 1];
            if (fileName.contains("%")) {
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            }
            return fileName;
        } catch (Exception e) {
            return "Unknown File";
        }
    }

    public static class GlobalBookmark {
        private String fileUri;
        private String fileName;
        private int pageNumber;
        private String title;
        private long timestamp;

        public GlobalBookmark(String fileUri, String fileName, int pageNumber, String title, long timestamp) {
            this.fileUri = fileUri;
            this.fileName = fileName;
            this.pageNumber = pageNumber;
            this.title = title;
            this.timestamp = timestamp;
        }

        public String getFileUri() { return fileUri; }
        public String getFileName() { return fileName; }
        public int getPageNumber() { return pageNumber; }
        public String getTitle() { return title; }
        public long getTimestamp() { return timestamp; }
    }
}
