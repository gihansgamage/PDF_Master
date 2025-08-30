package com.pdfmaster.reader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PDF_REQUEST = 1;

    private RecyclerView recyclerView;
    private RecentFilesAdapter adapter;
    private List<PDFFile> recentFiles;
    private FloatingActionButton fabOpenFile, fabRecentFiles;
    private FileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Handle intent from other apps
        handleIncomingIntent();

        // Check and request permissions
        if (!PermissionManager.hasStoragePermission(this)) {
            PermissionManager.requestStoragePermission(this);
        }

        initViews();
        setupRecyclerView();

        fileManager = new FileManager(this);
        loadRecentFiles();
    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && "application/pdf".equals(type)) {
            Uri pdfUri = intent.getData();
            if (pdfUri != null) {
                // Check permissions first
                if (!PermissionManager.hasStoragePermission(this)) {
                    // Store the URI to open after permission is granted
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
        recyclerView = findViewById(R.id.recyclerView);
        fabOpenFile = findViewById(R.id.fab_open_file);
        fabRecentFiles = findViewById(R.id.fab_recent_files);
        
        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("PDF Master");
        }

        fabOpenFile.setOnClickListener(v -> openFileChooser());
        fabRecentFiles.setOnClickListener(v -> openRecentFiles());
    }

    private void setupRecyclerView() {
        recentFiles = new ArrayList<>();
        adapter = new RecentFilesAdapter(recentFiles, this::onFileClick, this::onFileOptionsClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /** Open PDF using Storage Access Framework (SAF) */
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
                
                // Check if there's a pending PDF to open
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri pdfUri = data.getData();
            if (pdfUri != null) {
                // Take persistable permission to reopen file later
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

    private void loadRecentFiles() {
        recentFiles.clear();
        recentFiles.addAll(fileManager.getRecentFiles());
        if (adapter != null) {
            adapter.updateFiles(recentFiles);
        }
    }

    private void onFileClick(PDFFile pdfFile) {
        Uri uri = Uri.parse(pdfFile.getPath());
        openPDFViewer(uri);
    }

    private void onFileOptionsClick(PDFFile pdfFile) {
        FileOptionsDialog dialog = new FileOptionsDialog(pdfFile, fileManager);
        dialog.show(getSupportFragmentManager(), "FileOptions");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFiles();
    }
}
