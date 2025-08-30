package com.pdfmaster.reader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;

    private MaterialCardView cardOpenFile, cardRecentFiles;
    private MaterialCardView cardBookmarks, cardFavorites;
    private FileManager fileManager;
    private ImageView infoIcon;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        handleIncomingIntent();

        if (!PermissionManager.hasStoragePermission(this)) {
            PermissionManager.requestStoragePermission(this);
        }

        initViews();
        loadAds();

        fileManager = new FileManager(this);
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && "application/pdf".equals(type)) {
            Uri pdfUri = intent.getData();
            if (pdfUri != null) {
                if (!PermissionManager.hasStoragePermission(this)) {
                    getSharedPreferences("temp", MODE_PRIVATE)
                            .edit()
                            .putString("pending_pdf_uri", pdfUri.toString())
                            .apply();
                    PermissionManager.requestStoragePermission(this);
                } else {
                    openPDFViewer(pdfUri);
                }
            }
        }
    }

    private void initViews() {
        cardOpenFile = findViewById(R.id.card_open_file);
        cardRecentFiles = findViewById(R.id.card_recent_files);
        cardBookmarks = findViewById(R.id.card_bookmarks);
        cardFavorites = findViewById(R.id.card_favorites);
        infoIcon = findViewById(R.id.info_icon);
        mAdView = findViewById(R.id.adView);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PDF Master");
        }

        cardOpenFile.setOnClickListener(v -> openFileChooser());
        cardRecentFiles.setOnClickListener(v -> openRecentFiles());
        cardBookmarks.setOnClickListener(v -> openAllBookmarks());
        cardFavorites.setOnClickListener(v -> openFavoriteFiles());
        infoIcon.setOnClickListener(v -> showAppInfoDialog());
    }

    private void showAppInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_app_info, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button closeButton = dialogView.findViewById(R.id.btn_close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_PDF_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();

                String pendingUri = getSharedPreferences("temp", MODE_PRIVATE)
                        .getString("pending_pdf_uri", null);
                if (pendingUri != null) {
                    getSharedPreferences("temp", MODE_PRIVATE)
                            .edit()
                            .remove("pending_pdf_uri")
                            .apply();
                    openPDFViewer(Uri.parse(pendingUri));
                }
            } else {
                Toast.makeText(this, "Storage permission is required to open PDF files",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openRecentFiles() {
        Intent intent = new Intent(this, RecentFilesActivity.class);
        startActivity(intent);
    }

    private void openAllBookmarks() {
        Intent intent = new Intent(this, AllBookmarksActivity.class);
        startActivity(intent);
    }

    private void openFavoriteFiles() {
        Intent intent = new Intent(this, FavoriteFilesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            if (pdfUri != null) {
                getContentResolver().takePersistableUriPermission(
                        pdfUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                openPDFViewer(pdfUri);
            } else {
                Toast.makeText(this, "Unable to open PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openPDFViewer(Uri pdfUri) {
        Intent intent = new Intent(this, PDFViewActivity.class);
        intent.setData(pdfUri);
        startActivity(intent);
    }

    private void loadAds() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
