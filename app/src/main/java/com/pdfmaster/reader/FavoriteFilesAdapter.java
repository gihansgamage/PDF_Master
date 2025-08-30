package com.pdfmaster.reader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoriteFilesAdapter extends RecyclerView.Adapter<FavoriteFilesAdapter.FavoriteViewHolder> {

    private List<FavoriteManager.FavoriteFile> favorites;
    private OnFavoriteClickListener clickListener;
    private OnRemoveFavoriteListener removeListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteManager.FavoriteFile favorite);
    }

    public interface OnRemoveFavoriteListener {
        void onRemoveFavorite(FavoriteManager.FavoriteFile favorite);
    }

    public FavoriteFilesAdapter(List<FavoriteManager.FavoriteFile> favorites,
                                OnFavoriteClickListener clickListener,
                                OnRemoveFavoriteListener removeListener) {
        this.favorites = favorites;
        this.clickListener = clickListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_file, parent, false);
            return new FavoriteViewHolder(view);
        } catch (Exception e) {
            Log.e("FavoriteFilesAdapter", "Error inflating layout", e);
            throw new RuntimeException("Failed to create view holder", e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        try {
            if (favorites == null || position < 0 || position >= favorites.size()) {
                Log.w("FavoriteFilesAdapter", "Invalid position or null favorites list");
                return;
            }

            FavoriteManager.FavoriteFile favorite = favorites.get(position);
            if (favorite != null) {
                holder.bind(favorite, clickListener, removeListener);
            }
        } catch (Exception e) {
            Log.e("FavoriteFilesAdapter", "Error binding view holder at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return favorites != null ? favorites.size() : 0;
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameText;
        private TextView timestampText;
        private ImageButton removeButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                fileNameText = itemView.findViewById(R.id.text_file_name);
                timestampText = itemView.findViewById(R.id.text_timestamp);
                removeButton = itemView.findViewById(R.id.btn_remove_favorite);

                if (fileNameText == null || timestampText == null || removeButton == null) {
                    Log.e("FavoriteViewHolder", "One or more views not found in layout");
                }
            } catch (Exception e) {
                Log.e("FavoriteViewHolder", "Error finding views", e);
            }
        }

        public void bind(FavoriteManager.FavoriteFile favorite,
                         OnFavoriteClickListener clickListener,
                         OnRemoveFavoriteListener removeListener) {
            try {
                if (favorite == null) {
                    Log.w("FavoriteViewHolder", "Favorite object is null");
                    return;
                }

                if (fileNameText != null) {
                    String fileName = favorite.getFileName();
                    if (fileName != null && fileName.length() > 30) {
                        fileName = fileName.substring(0, 27) + "...";
                    }
                    fileNameText.setText("📄 " + (fileName != null ? fileName : "Unknown File"));
                }

                if (timestampText != null) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("📅 Added on MMM dd, yyyy HH:mm", Locale.getDefault());
                        timestampText.setText(sdf.format(new Date(favorite.getTimestamp())));
                    } catch (Exception e) {
                        timestampText.setText("📅 Recently added");
                        Log.w("FavoriteViewHolder", "Error formatting timestamp", e);
                    }
                }

                if (itemView != null) {
                    itemView.setOnClickListener(v -> {
                        try {
                            if (clickListener != null && favorite != null) {
                                clickListener.onFavoriteClick(favorite);
                            }
                        } catch (Exception e) {
                            Log.e("FavoriteViewHolder", "Error in click listener", e);
                        }
                    });
                }

                if (removeButton != null) {
                    removeButton.setOnClickListener(v -> {
                        try {
                            if (removeListener != null && favorite != null) {
                                removeListener.onRemoveFavorite(favorite);
                            }
                        } catch (Exception e) {
                            Log.e("FavoriteViewHolder", "Error in remove listener", e);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("FavoriteViewHolder", "Error binding favorite data", e);
            }
        }
    }
}
