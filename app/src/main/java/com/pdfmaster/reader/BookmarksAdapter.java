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

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {

    private List<BookmarkManager.Bookmark> bookmarks;
    private OnBookmarkClickListener clickListener;
    private OnBookmarkDeleteListener deleteListener;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(BookmarkManager.Bookmark bookmark);
    }

    public interface OnBookmarkDeleteListener {
        void onBookmarkDelete(BookmarkManager.Bookmark bookmark);
    }

    public BookmarksAdapter(List<BookmarkManager.Bookmark> bookmarks,
                            OnBookmarkClickListener clickListener,
                            OnBookmarkDeleteListener deleteListener) {
        this.bookmarks = bookmarks;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookmarkManager.Bookmark bookmark = bookmarks.get(position);
        holder.bind(bookmark);
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textPage, textDate;
        private ImageButton buttonDelete;

        ViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_bookmark_title);
            textPage = itemView.findViewById(R.id.text_bookmark_page);
            textDate = itemView.findViewById(R.id.text_bookmark_date);
            buttonDelete = itemView.findViewById(R.id.button_delete_bookmark);
        }

        void bind(BookmarkManager.Bookmark bookmark) {
            textTitle.setText(bookmark.getTitle());
            textPage.setText("Page " + (bookmark.getPageNumber() + 1));

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            textDate.setText(dateFormat.format(new Date(bookmark.getTimestamp())));

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onBookmarkClick(bookmark);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onBookmarkDelete(bookmark);
                }
            });
        }
    }
}