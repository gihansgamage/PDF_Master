package com.pdfmaster.reader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AllBookmarksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AllBookmarksAdapter adapter;
    private GlobalBookmarkManager globalBookmarkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_bookmarks);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("All Bookmarks");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        globalBookmarkManager = new GlobalBookmarkManager(this);

        recyclerView = findViewById(R.id.recycler_view_bookmarks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadBookmarks();
    }

    private void loadBookmarks() {
        List<GlobalBookmarkManager.GlobalBookmark> bookmarks = globalBookmarkManager.getAllBookmarks();

        if (bookmarks.isEmpty()) {
            Toast.makeText(this, "No bookmarks found", Toast.LENGTH_SHORT).show();
        }

        adapter = new AllBookmarksAdapter(bookmarks, this::onBookmarkClick);
        recyclerView.setAdapter(adapter);
    }

    private void onBookmarkClick(GlobalBookmarkManager.GlobalBookmark bookmark) {
        try {
            Intent intent = new Intent(this, PDFViewActivity.class);
            intent.setData(Uri.parse(bookmark.getFileUri()));
            intent.putExtra("goto_page", bookmark.getPageNumber());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
