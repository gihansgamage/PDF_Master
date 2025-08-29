package com.pdfmaster.reader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentFilesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecentFilesAdapter adapter;
    private List<PDFFile> recentFiles;
    private FileManager fileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_files);

        initViews();
        setupToolbar();
        loadRecentFiles();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recent Files");
        }

        fileManager = new FileManager(this);
    }

    private void setupToolbar() {
        // Toolbar is already set in initViews()
    }

    private void loadRecentFiles() {
        recentFiles = fileManager.getRecentFiles();
        adapter = new RecentFilesAdapter(recentFiles, this::onFileClick, this::onFileOptionsClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void onFileClick(PDFFile pdfFile) {
        Intent intent = new Intent(this, PDFViewActivity.class);
        intent.setData(Uri.parse(pdfFile.getPath()));
        startActivity(intent);
    }

    private void onFileOptionsClick(PDFFile pdfFile) {
        FileOptionsDialog dialog = new FileOptionsDialog(pdfFile, fileManager);
        dialog.show(getSupportFragmentManager(), "FileOptions");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentFiles();
    }
}