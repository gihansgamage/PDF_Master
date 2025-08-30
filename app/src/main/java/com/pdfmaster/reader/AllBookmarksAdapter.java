package com.pdfmaster.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllBookmarksAdapter extends RecyclerView.Adapter<AllBookmarksAdapter.BookmarkViewHolder> {

    private List<GlobalBookmarkManager.GlobalBookmark> bookmarks;
    private OnBookmarkClickListener listener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(GlobalBookmarkManager.GlobalBookmark bookmark);
    }

    public AllBookmarksAdapter(List<GlobalBookmarkManager.GlobalBookmark> bookmarks, OnBookmarkClickListener listener) {
        this.bookmarks = bookmarks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_global_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        GlobalBookmarkManager.GlobalBookmark bookmark = bookmarks.get(position);
        holder.bind(bookmark, listener);
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    static class BookmarkViewHolder extends RecyclerView.ViewHolder {
        private TextView titleText;
        private TextView fileNameText;
        private TextView pageText;
        private TextView timestampText;

        public BookmarkViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_bookmark_title);
            fileNameText = itemView.findViewById(R.id.text_file_name);
            pageText = itemView.findViewById(R.id.text_page_number);
            timestampText = itemView.findViewById(R.id.text_timestamp);
        }

        public void bind(GlobalBookmarkManager.GlobalBookmark bookmark, OnBookmarkClickListener listener) {
            titleText.setText(bookmark.getTitle());
            fileNameText.setText(bookmark.getFileName());
            pageText.setText("Page " + bookmark.getPageNumber());

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            timestampText.setText(sdf.format(new Date(bookmark.getTimestamp())));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(bookmark);
                }
            });
        }
    }
}
