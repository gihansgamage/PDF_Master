package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteManager {

    private static final String TAG = "FavoriteManager";
    private static final String FAVORITES_PREF = "favorite_files";
    private Context context;
    private SharedPreferences preferences;

    public FavoriteManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(FAVORITES_PREF, Context.MODE_PRIVATE);
    }

    public void addToFavorites(String fileUri, String fileName) {
        try {
            Set<String> existingFavorites = preferences.getStringSet("favorites", new HashSet<>());
            Set<String> favorites = existingFavorites != null ? new HashSet<>(existingFavorites) : new HashSet<>();

            String favorite = fileUri + "|" + fileName + "|" + System.currentTimeMillis();
            favorites.add(favorite);

            preferences.edit().putStringSet("favorites", favorites).commit();
        } catch (Exception e) {
            Log.e(TAG, "Error adding to favorites", e);
        }
    }

    public void removeFromFavorites(String fileUri) {
        try {
            Set<String> existingFavorites = preferences.getStringSet("favorites", new HashSet<>());
            if (existingFavorites != null) {
                Set<String> favorites = new HashSet<>(existingFavorites);
                favorites.removeIf(favorite -> favorite != null && favorite.startsWith(fileUri + "|"));
                preferences.edit().putStringSet("favorites", favorites).commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing from favorites", e);
        }
    }

    public boolean isFavorite(String fileUri) {
        try {
            Set<String> favorites = preferences.getStringSet("favorites", new HashSet<>());
            if (favorites != null) {
                return favorites.stream().anyMatch(favorite ->
                        favorite != null && favorite.startsWith(fileUri + "|"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking favorite status", e);
        }
        return false;
    }

    public List<FavoriteFile> getFavoriteFiles() {
        List<FavoriteFile> favoriteFiles = new ArrayList<>();
        try {
            Set<String> favoriteStrings = preferences.getStringSet("favorites", new HashSet<>());
            if (favoriteStrings != null) {
                for (String favoriteString : favoriteStrings) {
                    if (favoriteString != null && !favoriteString.isEmpty()) {
                        try {
                            String[] parts = favoriteString.split("\\|");
                            if (parts.length >= 3) {
                                String fileUri = parts[0];
                                String fileName = parts[1];
                                long timestamp = Long.parseLong(parts[2]);
                                favoriteFiles.add(new FavoriteFile(fileUri, fileName, timestamp));
                            }
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid favorite format: " + favoriteString, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites", e);
        }

        // Sort by timestamp (most recent first)
        favoriteFiles.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        return favoriteFiles;
    }

    public static class FavoriteFile {
        private String fileUri;
        private String fileName;
        private long timestamp;

        public FavoriteFile(String fileUri, String fileName, long timestamp) {
            this.fileUri = fileUri;
            this.fileName = fileName != null ? fileName : "Unknown File";
            this.timestamp = timestamp;
        }

        public String getFileUri() { return fileUri; }
        public String getFileName() { return fileName; }
        public long getTimestamp() { return timestamp; }
    }
}
