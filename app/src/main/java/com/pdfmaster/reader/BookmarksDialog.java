package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookmarksDialog extends DialogFragment {

    private BookmarkManager bookmarkManager;
    private OnBookmarkClickListener bookmarkClickListener;
    private int currentPage;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(int pageNumber);
    }

    public BookmarksDialog(BookmarkManager bookmarkManager, int currentPage, OnBookmarkClickListener listener) {
        this.bookmarkManager = bookmarkManager;
        this.currentPage = currentPage;
        this.bookmarkClickListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_bookmarks, null);

        setupRecyclerView(view);
        setupAddBookmarkButton(view);

        builder.setView(view)
                .setTitle("Bookmarks")
                .setNegativeButton("Close", null);

        return builder.create();
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_bookmarks);
        List<BookmarkManager.Bookmark> bookmarks = bookmarkManager.getBookmarks();

        BookmarksAdapter adapter = new BookmarksAdapter(bookmarks, bookmark -> {
            if (bookmarkClickListener != null) {
                bookmarkClickListener.onBookmarkClick(bookmark.getPageNumber());
            }
            dismiss();
        }, bookmark -> {
            bookmarkManager.removeBookmark(bookmark.getPageNumber());
            setupRecyclerView(view); // Refresh the list
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupAddBookmarkButton(View view) {
        view.findViewById(R.id.btn_add_bookmark).setOnClickListener(v -> {
            showAddBookmarkDialog();
        });
    }

    private void showAddBookmarkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_bookmark, null);

        EditText editTitle = dialogView.findViewById(R.id.edit_bookmark_title);
        editTitle.setText("Page " + (currentPage + 1));

        builder.setView(dialogView)
                .setTitle("Add Bookmark")
                .setPositiveButton("Add", (dialog, id) -> {
                    String title = editTitle.getText().toString().trim();
                    if (!title.isEmpty()) {
                        bookmarkManager.addBookmark(currentPage, title);
                        Toast.makeText(getContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
                        setupRecyclerView(getView()); // Refresh the list
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }
}