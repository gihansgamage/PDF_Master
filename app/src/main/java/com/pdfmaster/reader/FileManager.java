package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileManager {

    private static final String TAG = "FileManager";
    private Context context;
    private SharedPreferences preferences;

    public FileManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("recent_files", Context.MODE_PRIVATE);
    }

    public void addToRecentFiles(Uri uri, String fileName) {
        addToRecentFiles(uri, fileName, 0);
    }

    public void addToRecentFiles(Uri uri, String fileName, int pageCount) {
        try {
            Set<String> recentFiles = new HashSet<>(preferences.getStringSet("files", new HashSet<>()));
            
            // Get file size
            long fileSize = getFileSize(uri);
            
            String fileEntry = uri.toString() + "|" + fileName + "|" + System.currentTimeMillis() + "|" + fileSize + "|" + pageCount;

            // Remove if already exists (to update timestamp)
            recentFiles.removeIf(entry -> {
                String[] parts = entry.split("\\|");
                return parts.length > 0 && parts[0].equals(uri.toString());
            });

            // Add new entry
            recentFiles.add(fileEntry);

            // Keep only last 20 files
            if (recentFiles.size() > 20) {
                List<String> sortedFiles = new ArrayList<>(recentFiles);
                sortedFiles.sort((a, b) -> {
                    try {
                        String[] partsA = a.split("\\|");
                        String[] partsB = b.split("\\|");
                        if (partsA.length >= 3 && partsB.length >= 3) {
                            long timestampA = Long.parseLong(partsA[2]);
                            long timestampB = Long.parseLong(partsB[2]);
                            return Long.compare(timestampB, timestampA); // Newest first
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error sorting files", e);
                    }
                    return 0;
                });
                
                // Keep only the 20 most recent
                recentFiles = new HashSet<>(sortedFiles.subList(0, Math.min(20, sortedFiles.size())));
            }

            preferences.edit().putStringSet("files", recentFiles).apply();
            Log.d(TAG, "Added file to recent: " + fileName);
        } catch (Exception e) {
            Log.e(TAG, "Error adding file to recent files", e);
        }
    }

    public List<PDFFile> getRecentFiles() {
        Set<String> recentFiles = preferences.getStringSet("files", new HashSet<>());
        List<PDFFile> pdfFiles = new ArrayList<>();

        for (String entry : recentFiles) {
            try {
                String[] parts = entry.split("\\|");
                if (parts.length >= 5) {
                    String uriString = parts[0];
                    String fileName = parts[1];
                    long timestamp = Long.parseLong(parts[2]);
                    long fileSize = Long.parseLong(parts[3]);
                    int pageCount = Integer.parseInt(parts[4]);

                    Uri uri = Uri.parse(uriString);
                    
                    // Check if file is still accessible
                    if (isUriAccessible(uri)) {
                        pdfFiles.add(new PDFFile(fileName, uriString, fileSize, new Date(timestamp), pageCount));
                    } else {
                        Log.d(TAG, "File no longer accessible: " + fileName);
                    }
                } else if (parts.length >= 4) {
                    // Handle format without page count
                    String uriString = parts[0];
                    String fileName = parts[1];
                    long timestamp = Long.parseLong(parts[2]);
                    long fileSize = Long.parseLong(parts[3]);
                    
                    Uri uri = Uri.parse(uriString);
                    if (isUriAccessible(uri)) {
                        pdfFiles.add(new PDFFile(fileName, uriString, fileSize, new Date(timestamp), 0));
                    }
                } else if (parts.length >= 3) {
                    // Handle old format without file size
                    String uriString = parts[0];
                    String fileName = parts[1];
                    long timestamp = Long.parseLong(parts[2]);
                    
                    Uri uri = Uri.parse(uriString);
                    if (isUriAccessible(uri)) {
                        long fileSize = getFileSize(uri);
                        pdfFiles.add(new PDFFile(fileName, uriString, fileSize, new Date(timestamp), 0));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing recent file entry: " + entry, e);
            }
        }

        // Sort by last modified date (newest first)
        pdfFiles.sort((a, b) -> b.getLastModified().compareTo(a.getLastModified()));

        Log.d(TAG, "Loaded " + pdfFiles.size() + " recent files");
        return pdfFiles;
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file name", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "Unknown file";
    }

    public long getFileSize(Uri uri) {
        long size = 0;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        size = cursor.getLong(sizeIndex);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting file size", e);
            }
        }
        return size;
    }

    public boolean hasUriPermission(Uri uri) {
        try {
            List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
            for (UriPermission permission : permissions) {
                if (permission.getUri().equals(uri) && permission.isReadPermission()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking URI permission", e);
            return false;
        }
    }

    public boolean isUriAccessible(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "URI not accessible: " + uri.toString());
        }
        return false;
    }

    public boolean renameFile(Uri uri, String newName) {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                Uri renamedUri = DocumentsContract.renameDocument(context.getContentResolver(), uri, newName);
                return renamedUri != null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error renaming file", e);
        }
        return false;
    }

    public boolean deleteFile(Uri uri) {
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                boolean deleted = DocumentsContract.deleteDocument(context.getContentResolver(), uri);
                if (deleted) {
                    removeFromRecentFiles(uri.toString());
                }
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting file", e);
        }
        return false;
    }

    public void removeFromRecentFiles(String filePath) {
        try {
            Set<String> recentFiles = new HashSet<>(preferences.getStringSet("files", new HashSet<>()));
            recentFiles.removeIf(entry -> entry.startsWith(filePath + "|"));
            preferences.edit().putStringSet("files", recentFiles).apply();
            Log.d(TAG, "Removed file from recent files: " + filePath);
        } catch (Exception e) {
            Log.e(TAG, "Error removing file from recent files", e);
        }
    }

    public void clearRecentFiles() {
        preferences.edit().remove("files").apply();
        Log.d(TAG, "Cleared all recent files");
    }
}