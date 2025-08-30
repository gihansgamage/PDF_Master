package com.pdfmaster.reader;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_file, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteManager.FavoriteFile favorite = favorites.get(position);
        holder.bind(favorite, clickListener, removeListener);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameText;
        private TextView timestampText;
        private ImageButton removeButton;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.text_file_name);
            timestampText = itemView.findViewById(R.id.text_timestamp);
            removeButton = itemView.findViewById(R.id.btn_remove_favorite);
        }

        public void bind(FavoriteManager.FavoriteFile favorite,
                         OnFavoriteClickListener clickListener,
                         OnRemoveFavoriteListener removeListener) {
            fileNameText.setText(favorite.getFileName());

            SimpleDateFormat sdf = new SimpleDateFormat("Added on MMM dd, yyyy", Locale.getDefault());
            timestampText.setText(sdf.format(new Date(favorite.getTimestamp())));

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onFavoriteClick(favorite);
                }
            });

            removeButton.setOnClickListener(v -> {
                if (removeListener != null) {
                    removeListener.onRemoveFavorite(favorite);
                }
            });
        }
    }
}
