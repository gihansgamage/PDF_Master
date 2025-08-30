package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "BookmarksDialog";
    private static final String ARG_CURRENT_PAGE = "current_page";
    private static final String ARG_FILE_URI = "file_uri";

    private BookmarkManager bookmarkManager;
    private OnBookmarkClickListener bookmarkClickListener;
    private int currentPage;
    private View dialogView;

    public interface OnBookmarkClickListener {
        void onBookmarkClick(int pageNumber);
    }

    public static BookmarksDialog newInstance(String fileUri, int currentPage, OnBookmarkClickListener listener) {
        BookmarksDialog dialog = new BookmarksDialog();
        Bundle args = new Bundle();
        args.putString(ARG_FILE_URI, fileUri);
        args.putInt(ARG_CURRENT_PAGE, currentPage);
        dialog.setArguments(args);
        dialog.bookmarkClickListener = listener;
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String fileUri = getArguments().getString(ARG_FILE_URI);
            currentPage = getArguments().getInt(ARG_CURRENT_PAGE, 0);
            if (fileUri != null && getContext() != null) {
                bookmarkManager = new BookmarkManager(getContext(), fileUri);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_bookmarks, null);

        setupRecyclerView();
        setupAddBookmarkButton();

        builder.setView(dialogView)
                .setTitle("Bookmarks")
                .setNegativeButton("Close", null);

        return builder.create();
    }

    private void setupRecyclerView() {
        try {
            if (dialogView != null && bookmarkManager != null) {
                RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_bookmarks);
                if (recyclerView != null) {
                    List<BookmarkManager.Bookmark> bookmarks = bookmarkManager.getBookmarks();

                    BookmarksAdapter adapter = new BookmarksAdapter(bookmarks, bookmark -> {
                        if (bookmarkClickListener != null) {
                            bookmarkClickListener.onBookmarkClick(bookmark.getPageNumber());
                        }
                        dismiss();
                    }, bookmark -> {
                        bookmarkManager.removeBookmark(bookmark.getPageNumber());
                        setupRecyclerView(); // Refresh the list
                    });

                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(adapter);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupAddBookmarkButton() {
        try {
            if (dialogView != null) {
                View addButton = dialogView.findViewById(R.id.btn_add_bookmark);
                if (addButton != null) {
                    addButton.setOnClickListener(v -> showAddBookmarkDialog());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up add bookmark button", e);
        }
    }

    private void showAddBookmarkDialog() {
        try {
            if (getContext() == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_add_bookmark, null);

            EditText editTitle = dialogView.findViewById(R.id.edit_bookmark_title);
            if (editTitle != null) {
                editTitle.setText("Page " + (currentPage + 1));
            }

            builder.setView(dialogView)
                    .setTitle("Add Bookmark")
                    .setPositiveButton("Add", (dialog, id) -> {
                        try {
                            if (editTitle != null && bookmarkManager != null) {
                                String title = editTitle.getText().toString().trim();
                                if (!title.isEmpty()) {
                                    bookmarkManager.addBookmark(currentPage, title);
                                    Toast.makeText(getContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
                                    setupRecyclerView(); // Refresh the list
                                } else {
                                    Toast.makeText(getContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error adding bookmark", e);
                            Toast.makeText(getContext(), "Failed to add bookmark", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null);

            builder.create().show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing add bookmark dialog", e);
        }
    }
}
