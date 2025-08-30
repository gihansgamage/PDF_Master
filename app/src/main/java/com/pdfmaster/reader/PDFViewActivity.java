package com.pdfmaster.reader;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.provider.DocumentsContract;

import java.util.Locale;

public class PDFViewActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private PDFView pdfView;
    private Uri pdfUri;
    private TextToSpeech textToSpeech;
    private boolean isReading = false;
    private FloatingActionButton fabBookmark, fabReadAloud, fabShare, fabToggleControls;
    private LinearLayout actionButtonsContainer;
    private boolean buttonsVisible = true;
    private Toolbar toolbar;
    private BookmarkManager bookmarkManager;
    private PDFTextExtractor textExtractor;
    private FileManager fileManager;
    private String currentFileName;
    private int currentPage = 0;
    private int totalPages = 0;

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
        fabBookmark = findViewById(R.id.fab_bookmark);
        fabReadAloud = findViewById(R.id.fab_read_aloud);
        fabShare = findViewById(R.id.fab_share);
        fabToggleControls = findViewById(R.id.fab_toggle_controls);
        actionButtonsContainer = findViewById(R.id.action_buttons_container);

        fabToggleControls.setAlpha(0.4f);
        fabToggleControls.setScaleX(0.7f);
        fabToggleControls.setScaleY(0.7f);
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
        textExtractor = new PDFTextExtractor(this);
        textToSpeech = new TextToSpeech(this, this);

        fabBookmark.setOnClickListener(v -> showBookmarksDialog());
        fabReadAloud.setOnClickListener(v -> toggleTextToSpeech());
        fabShare.setOnClickListener(v -> sharePDF());

        fabToggleControls.setOnClickListener(v -> toggleActionButtons());
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
                        .onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                currentPage = page;
                                totalPages = pageCount;
                                updateBookmarkIcon();
                                updateToolbarTitle();
                            }
                        })
                        .onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                totalPages = nbPages;
                                updateToolbarTitle();
                                // Update the file in recent files with page count
                                updateFilePageCount(nbPages);
                            }
                        })
                        .load();

                // Initialize bookmark manager
                bookmarkManager = new BookmarkManager(this, pdfUri.toString());

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Apply custom styling to menu items for better visibility
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item != null) {
                // Force white background and black text for menu items
                item.getIcon();
            }
        }
        return super.onPrepareOptionsMenu(menu);
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

    private void showBookmarksDialog() {
        try {
            if (bookmarkManager != null && pdfUri != null) {
                BookmarksDialog dialog = BookmarksDialog.newInstance(pdfUri.toString(), currentPage, pageNumber -> {
                    pdfView.jumpTo(pageNumber);
                });
                dialog.show(getSupportFragmentManager(), "Bookmarks");
            } else {
                Toast.makeText(this, "Bookmark manager not initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PDFViewActivity", "Error showing bookmarks dialog", e);
            Toast.makeText(this, "Failed to open bookmarks", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBookmarkIcon() {
        if (bookmarkManager != null) {
            if (bookmarkManager.isBookmarked(currentPage)) {
                fabBookmark.setImageResource(R.drawable.ic_bookmark);
            } else {
                fabBookmark.setImageResource(R.drawable.ic_bookmark_border);
            }
        } else {
            fabBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void updateToolbarTitle() {
        if (getSupportActionBar() != null && totalPages > 0) {
            String title = currentFileName;
            if (title.length() > 20) {
                title = title.substring(0, 17) + "...";
            }
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle("Page " + (currentPage + 1) + " of " + totalPages);
        }
    }

    private void updateFilePageCount(int pageCount) {
        // Update the recent files entry with page count information
        if (pdfUri != null && currentFileName != null) {
            fileManager.addToRecentFiles(pdfUri, currentFileName, pageCount);
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
            String textToRead = textExtractor.extractTextFromCurrentView(pdfUri, currentPage);
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null);
            isReading = true;
            fabReadAloud.setImageResource(R.drawable.ic_stop);
            Toast.makeText(this, "Reading page " + (currentPage + 1), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopReading() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        isReading = false;
        fabReadAloud.setImageResource(R.drawable.ic_play);
    }

    private void showRenameDialog() {
        RenameFileDialog dialog = new RenameFileDialog(currentFileName, newName -> {
            if (fileManager.renameFile(pdfUri, newName)) {
                currentFileName = newName + ".pdf"; // Update with extension
                updateToolbarTitle(); // Refresh the title display
                Toast.makeText(this, "File renamed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Rename failed. File may be read-only or provider doesn't support renaming.", Toast.LENGTH_LONG).show();
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
        try {
            if (pdfUri != null) {
                if (DocumentsContract.isDocumentUri(this, pdfUri)) {
                    String documentId = DocumentsContract.getDocumentId(pdfUri);

                    // Handle different document providers
                    if (pdfUri.getAuthority().contains("downloads")) {
                        // Open Downloads folder
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setType("resource/folder");
                        intent.setData(Uri.parse("content://com.android.providers.downloads.documents/tree/downloads"));

                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            return;
                        }
                    } else if (pdfUri.getAuthority().contains("externalstorage")) {
                        // Try to open parent directory for external storage
                        try {
                            String[] parts = documentId.split(":");
                            if (parts.length > 1) {
                                String path = parts[1];
                                int lastSlash = path.lastIndexOf('/');
                                if (lastSlash > 0) {
                                    String parentPath = path.substring(0, lastSlash);
                                    String treeId = parts[0] + ":" + parentPath;
                                    Uri treeUri = DocumentsContract.buildTreeDocumentUri(pdfUri.getAuthority(), treeId);

                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(treeUri);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                    if (intent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(intent);
                                        return;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.w("PDFView", "Could not open parent folder", e);
                        }
                    }
                }

                // Fallback: Open file manager
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    Toast.makeText(this, "Navigate to your file location", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "No file manager available", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e("PDFView", "Error opening file location", e);
            Toast.makeText(this, "Unable to open file location", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleActionButtons() {
        if (buttonsVisible) {
            // Hide buttons
            actionButtonsContainer.setVisibility(View.GONE);
            fabToggleControls.setImageResource(R.drawable.ic_visibility_off);
            fabToggleControls.setAlpha(0.6f);
            buttonsVisible = false;
            Toast.makeText(this, "Controls hidden", Toast.LENGTH_SHORT).show();
        } else {
            // Show buttons
            actionButtonsContainer.setVisibility(View.VISIBLE);
            fabToggleControls.setImageResource(R.drawable.ic_visibility);
            fabToggleControls.setAlpha(0.4f);
            buttonsVisible = true;
            Toast.makeText(this, "Controls visible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show();
            } else {
                // TTS is ready
                textToSpeech.setSpeechRate(0.8f); // Slightly slower for better comprehension
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
