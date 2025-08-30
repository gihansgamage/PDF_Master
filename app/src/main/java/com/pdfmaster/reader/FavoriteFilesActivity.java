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

        try {
            setContentView(R.layout.activity_favorite_files);

            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Favorite Files");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            try {
                favoriteManager = new FavoriteManager(this);
            } catch (Exception e) {
                Toast.makeText(this, "Error initializing favorites: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            recyclerView = findViewById(R.id.recycler_view_favorites);
            if (recyclerView == null) {
                Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            loadFavorites();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading favorites screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadFavorites() {
        try {
            if (favoriteManager == null) {
                Toast.makeText(this, "Favorite manager not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            List<FavoriteManager.FavoriteFile> favorites = favoriteManager.getFavoriteFiles();

            if (favorites == null) {
                favorites = new java.util.ArrayList<>();
            }

            if (favorites.isEmpty()) {
                Toast.makeText(this, "No favorite files found", Toast.LENGTH_SHORT).show();
            }

            try {
                adapter = new FavoriteFilesAdapter(favorites, this::onFavoriteClick, this::onRemoveFavorite);
                recyclerView.setAdapter(adapter);
            } catch (Exception e) {
                Toast.makeText(this, "Error creating favorites list: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading favorites: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onFavoriteClick(FavoriteManager.FavoriteFile favorite) {
        try {
            if (favorite == null || favorite.getFileUri() == null || favorite.getFileUri().isEmpty()) {
                Toast.makeText(this, "Invalid favorite file", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, PDFViewActivity.class);
            intent.setData(Uri.parse(favorite.getFileUri()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onRemoveFavorite(FavoriteManager.FavoriteFile favorite) {
        try {
            if (favorite == null || favorite.getFileUri() == null) {
                Toast.makeText(this, "Invalid favorite file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (favoriteManager == null) {
                Toast.makeText(this, "Favorite manager not available", Toast.LENGTH_SHORT).show();
                return;
            }

            favoriteManager.removeFromFavorites(favorite.getFileUri());
            loadFavorites(); // Refresh the list
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error removing favorite: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
