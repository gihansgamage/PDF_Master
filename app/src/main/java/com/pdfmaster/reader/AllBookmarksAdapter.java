package com.pdfmaster.reader;

import android.util.Log;
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
        try {
            GlobalBookmarkManager.GlobalBookmark bookmark = bookmarks.get(position);
            if (bookmark == null) {
                Log.w("AllBookmarksAdapter", "Bookmark at position " + position + " is null");
                return;
            }
            Log.d("AllBookmarksAdapter", "Binding bookmark: title=" + bookmark.getTitle() +
                    ", fileName=" + bookmark.getFileName() + ", page=" + bookmark.getPageNumber());
            holder.bind(bookmark, listener);
        } catch (Exception e) {
            Log.e("AllBookmarksAdapter", "Error binding bookmark at position " + position, e);
        }
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
            try {
                if (bookmark == null) {
                    Log.w("BookmarkViewHolder", "Bookmark object is null");
                    return;
                }

                if (titleText != null) {
                    String fileName = bookmark.getFileName();
                    if (fileName == null || fileName.trim().isEmpty()) {
                        fileName = "Unknown PDF File";
                    } else {
                        // Extract just the filename without path
                        if (fileName.contains("/")) {
                            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                        }
                        // Remove file extension for cleaner display
                        if (fileName.toLowerCase().endsWith(".pdf")) {
                            fileName = fileName.substring(0, fileName.length() - 4);
                        }
                        // Truncate long filenames
                        if (fileName.length() > 25) {
                            fileName = fileName.substring(0, 22) + "...";
                        }
                    }
                    titleText.setText(fileName);
                    Log.d("BookmarkViewHolder", "Set title: " + fileName);
                }

                if (fileNameText != null) {
                    String bookmarkTitle = bookmark.getTitle();
                    if (bookmarkTitle == null || bookmarkTitle.trim().isEmpty()) {
                        fileNameText.setText("Bookmark in this document");
                    } else {
                        fileNameText.setText(bookmarkTitle);
                    }
                    Log.d("BookmarkViewHolder", "Set bookmark description");
                }

                if (pageText != null) {
                    int pageNum = bookmark.getPageNumber();
                    pageText.setText("Page " + (pageNum + 1));
                    Log.d("BookmarkViewHolder", "Set page: " + (pageNum + 1));
                }

                if (timestampText != null) {
                    try {
                        long timestamp = bookmark.getTimestamp();
                        if (timestamp > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                            timestampText.setText(sdf.format(new Date(timestamp)));
                        } else {
                            timestampText.setText("Recently added");
                        }
                        Log.d("BookmarkViewHolder", "Set timestamp: " + timestamp);
                    } catch (Exception e) {
                        timestampText.setText("Recently added");
                        Log.w("BookmarkViewHolder", "Error formatting timestamp", e);
                    }
                }

                if (itemView != null) {
                    itemView.setOnClickListener(v -> {
                        try {
                            if (listener != null && bookmark != null) {
                                Log.d("BookmarkViewHolder", "Bookmark clicked: " + bookmark.getTitle());
                                listener.onBookmarkClick(bookmark);
                            }
                        } catch (Exception e) {
                            Log.e("BookmarkViewHolder", "Error in bookmark click", e);
                        }
                    });
                }

            } catch (Exception e) {
                Log.e("BookmarkViewHolder", "Error binding bookmark data", e);
            }
        }
    }
}
