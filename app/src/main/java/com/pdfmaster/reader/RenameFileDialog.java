package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.inputmethod.InputMethodManager;

public class RenameFileDialog extends DialogFragment {

    private String currentName;
    private OnRenameListener renameListener;

    public interface OnRenameListener {
        void onRename(String newName);
    }

    public RenameFileDialog(String currentName, OnRenameListener renameListener) {
        this.currentName = currentName;
        this.renameListener = renameListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_rename_file, null);

        EditText editFileName = view.findViewById(R.id.edit_file_name);

        String nameWithoutExtension = currentName;
        if (nameWithoutExtension.toLowerCase().endsWith(".pdf")) {
            nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.length() - 4);
        }
        editFileName.setText(nameWithoutExtension);
        editFileName.selectAll();

        builder.setView(view)
                .setTitle("Rename File")
                .setPositiveButton("Rename", (dialog, id) -> {
                    String newName = editFileName.getText().toString().trim();
                    if (!newName.isEmpty() && renameListener != null) {
                        if (isValidFileName(newName)) {
                            Log.d("RenameFileDialog", "Attempting to rename to: " + newName);
                            renameListener.onRename(newName);
                        } else {
                            android.widget.Toast.makeText(getContext(),
                                    "Invalid filename. Avoid these characters: / \\ : * ? \" < > |",
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    } else if (newName.isEmpty()) {
                        android.widget.Toast.makeText(getContext(),
                                "Please enter a filename",
                                android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            editFileName.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editFileName, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        return dialog;
    }

    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        // Check for invalid characters
        String invalidChars = "/\\:*?\"<>|";
        for (char c : invalidChars.toCharArray()) {
            if (fileName.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }
}
