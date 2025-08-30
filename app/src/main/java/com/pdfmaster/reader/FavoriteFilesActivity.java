package com.pdfmaster.reader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FavoriteFilesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavoriteFilesAdapter adapter;
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_files);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Favorite Files");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        favoriteManager = new FavoriteManager(this);

        recyclerView = findViewById(R.id.recycler_view_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFavorites();
    }

    private void loadFavorites() {
        List<FavoriteManager.FavoriteFile> favorites = favoriteManager.getFavoriteFiles();

        if (favorites.isEmpty()) {
            Toast.makeText(this, "No favorite files found", Toast.LENGTH_SHORT).show();
        }

        adapter = new FavoriteFilesAdapter(favorites, this::onFavoriteClick, this::onRemoveFavorite);
        recyclerView.setAdapter(adapter);
    }

    private void onFavoriteClick(FavoriteManager.FavoriteFile favorite) {
        try {
            Intent intent = new Intent(this, PDFViewActivity.class);
            intent.setData(Uri.parse(favorite.getFileUri()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onRemoveFavorite(FavoriteManager.FavoriteFile favorite) {
        favoriteManager.removeFromFavorites(favorite.getFileUri());
        loadFavorites(); // Refresh the list
        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
