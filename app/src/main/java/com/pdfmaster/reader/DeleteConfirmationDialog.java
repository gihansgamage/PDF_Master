package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DeleteConfirmationDialog extends DialogFragment {

    private OnDeleteConfirmedListener deleteListener;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed();
    }

    public DeleteConfirmationDialog(OnDeleteConfirmedListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Delete File")
                .setMessage("Are you sure you want to delete this file? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, id) -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteConfirmed();
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }
}