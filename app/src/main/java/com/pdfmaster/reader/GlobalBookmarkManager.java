package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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

    private String getFileNameFromUri(String uriString) {
        try {
            if (uriString == null || uriString.isEmpty()) {
                return "Unknown File";
            }

            // Handle content URIs using ContentResolver
            if (uriString.startsWith("content://")) {
                try {
                    Uri uri = Uri.parse(uriString);
                    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                                if (nameIndex != -1) {
                                    String displayName = cursor.getString(nameIndex);
                                    if (displayName != null && !displayName.isEmpty()) {
                                        return displayName;
                                    }
                                }
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not query content resolver for URI: " + uriString, e);
                }

                // Fallback: try to extract from URI path
                String[] segments = uriString.split("/");
                String lastSegment = segments[segments.length - 1];

                if (lastSegment.contains(":")) {
                    String[] parts = lastSegment.split(":");
                    if (parts.length > 1) {
                        String potentialName = parts[parts.length - 1];
                        if (potentialName.toLowerCase().endsWith(".pdf")) {
                            return java.net.URLDecoder.decode(potentialName, "UTF-8");
                        }
                    }
                }

                return "PDF Document";
            }

            // Handle file URIs (file://...)
            if (uriString.startsWith("file://")) {
                String[] segments = uriString.split("/");
                String fileName = segments[segments.length - 1];
                if (fileName.contains("%")) {
                    fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
                }
                return fileName;
            }

            // Handle regular file paths
            String[] segments = uriString.split("/");
            String fileName = segments[segments.length - 1];
            if (fileName.contains("%")) {
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            }

            // If filename is still not meaningful, provide a fallback
            if (fileName.isEmpty() || (fileName.matches(".*\\d+.*") && !fileName.toLowerCase().endsWith(".pdf"))) {
                return "PDF Document";
            }

            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting filename from URI: " + uriString, e);
            return "PDF Document";
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
