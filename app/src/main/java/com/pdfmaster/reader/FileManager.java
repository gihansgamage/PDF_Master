package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {

    private Context context;
    private SharedPreferences preferences;

    public FileManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("recent_files", Context.MODE_PRIVATE);
    }

    public void addToRecentFiles(Uri uri, String fileName) {
        Set<String> recentFiles = preferences.getStringSet("files", new HashSet<>());
        String fileEntry = uri.toString() + "|" + fileName + "|" + System.currentTimeMillis();

        // Remove if already exists
        recentFiles.removeIf(entry -> entry.startsWith(uri.toString()));

        // Add new entry
        recentFiles.add(fileEntry);

        // Keep only last 20 files
        if (recentFiles.size() > 20) {
            // Remove oldest entries - simplified implementation
        }

        preferences.edit().putStringSet("files", recentFiles).apply();
    }

    public List<PDFFile> getRecentFiles() {
        Set<String> recentFiles = preferences.getStringSet("files", new HashSet<>());
        List<PDFFile> pdfFiles = new ArrayList<>();

        for (String entry : recentFiles) {
            String[] parts = entry.split("\\|");
            if (parts.length >= 3) {
                String uri = parts[0];
                String fileName = parts[1];
                long timestamp = Long.parseLong(parts[2]);

                pdfFiles.add(new PDFFile(fileName, uri, 0, new Date(timestamp)));
            }
        }

        // Sort by last modified date (newest first)
        pdfFiles.sort((a, b) -> b.getLastModified().compareTo(a.getLastModified()));

        return pdfFiles;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public boolean renameFile(Uri uri, String newName) {
        // For content URIs, renaming requires DocumentsContract
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                return DocumentsContract.renameDocument(context.getContentResolver(), uri, newName) != null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFile(Uri uri) {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                return DocumentsContract.deleteDocument(context.getContentResolver(), uri);
            } else {
                File file = new File(uri.getPath());
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void removeFromRecentFiles(String filePath) {
        Set<String> recentFiles = new HashSet<>(preferences.getStringSet("files", new HashSet<>()));
        recentFiles.removeIf(entry -> entry.startsWith(filePath));
        preferences.edit().putStringSet("files", recentFiles).apply();
    }
}