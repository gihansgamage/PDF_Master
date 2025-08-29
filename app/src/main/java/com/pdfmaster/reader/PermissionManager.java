package com.pdfmaster.reader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ doesn't need READ_EXTERNAL_STORAGE for document picker
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, 
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            
            return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        return false;
    }
}