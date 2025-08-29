package com.pdfmaster.reader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

public class PDFViewActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private PDFView pdfView;
    private Uri pdfUri;
    private TextToSpeech textToSpeech;
    private boolean isReading = false;
    private FloatingActionButton fabDraw, fabHighlight, fabShare, fabTTS;
    private Toolbar toolbar;
    private DrawingOverlay drawingOverlay;
    private HighlightManager highlightManager;
    private FileManager fileManager;
    private String currentFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfview);

        initViews();
        setupToolbar();
        initializeComponents();
        loadPDF();
    }

    private void initViews() {
        pdfView = findViewById(R.id.pdfView);
        toolbar = findViewById(R.id.toolbar);
        fabDraw = findViewById(R.id.fab_draw);
        fabHighlight = findViewById(R.id.fab_highlight);
        fabShare = findViewById(R.id.fab_share);
        fabTTS = findViewById(R.id.fab_tts);
        drawingOverlay = findViewById(R.id.drawing_overlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("PDF Reader");
        }
    }

    private void initializeComponents() {
        fileManager = new FileManager(this);
        highlightManager = new HighlightManager(this);
        textToSpeech = new TextToSpeech(this, this);

        fabDraw.setOnClickListener(v -> toggleDrawingMode());
        fabHighlight.setOnClickListener(v -> toggleHighlightMode());
        fabShare.setOnClickListener(v -> sharePDF());
        fabTTS.setOnClickListener(v -> toggleTextToSpeech());
    }

    private void loadPDF() {
        pdfUri = getIntent().getData();
        if (pdfUri != null) {
            // Check permissions before loading PDF
            if (!PermissionManager.hasStoragePermission(this)) {
                PermissionManager.requestStoragePermission(this);
                return;
            }

            currentFileName = fileManager.getFileName(pdfUri);

            try {
                pdfView.fromUri(pdfUri)
                        .enableSwipe(true)
                        .enableDoubletap(true)
                        .defaultPage(0)
                        .enableAnnotationRendering(false)
                        .scrollHandle(null)
                        .spacing(0)
                        .pageFitPolicy(FitPolicy.WIDTH)
                        .load();

                // Add to recent files
                fileManager.addToRecentFiles(pdfUri, currentFileName);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to open PDF", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "PDF file not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try loading PDF again
                loadPDF();
            } else {
                Toast.makeText(this, "Storage permission is required to view PDF files",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pdf_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_rename) {
            showRenameDialog();
            return true;
        } else if (id == R.id.menu_delete) {
            showDeleteConfirmation();
            return true;
        } else if (id == R.id.menu_open_location) {
            openFileLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleDrawingMode() {
        if (drawingOverlay.getVisibility() == View.VISIBLE) {
            drawingOverlay.setVisibility(View.GONE);
            fabDraw.setImageResource(R.drawable.ic_draw);
        } else {
            drawingOverlay.setVisibility(View.VISIBLE);
            fabDraw.setImageResource(R.drawable.ic_close);
        }
    }

    private void toggleHighlightMode() {
        highlightManager.toggleHighlightMode();
        if (highlightManager.isHighlightMode()) {
            fabHighlight.setImageResource(R.drawable.ic_close);
            Toast.makeText(this, "Highlight mode ON", Toast.LENGTH_SHORT).show();
        } else {
            fabHighlight.setImageResource(R.drawable.ic_highlight);
        }
    }

    private void sharePDF() {
        if (pdfUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing PDF: " + currentFileName);
            startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
        }
    }

    private void toggleTextToSpeech() {
        if (isReading) stopReading();
        else startReading();
    }

    private void startReading() {
        if (textToSpeech != null) {
            String textToRead = "Reading PDF content..."; // Extract text properly here if needed
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
            isReading = true;
            fabTTS.setImageResource(R.drawable.ic_stop);
        }
    }

    private void stopReading() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        isReading = false;
        fabTTS.setImageResource(R.drawable.ic_play);
    }

    private void showRenameDialog() {
        RenameFileDialog dialog = new RenameFileDialog(currentFileName, newName -> {
            if (fileManager.renameFile(pdfUri, newName)) {
                currentFileName = newName;
                Toast.makeText(this, "File renamed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "RenameFile");
    }

    private void showDeleteConfirmation() {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(() -> {
            if (fileManager.deleteFile(pdfUri)) {
                Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "DeleteConfirmation");
    }

    private void openFileLocation() {
        Toast.makeText(this, "Opening file location not supported on Android 11+", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
