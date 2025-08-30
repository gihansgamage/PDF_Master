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

    private static final String ARG_CURRENT_NAME = "current_name";
    private String currentName;
    private OnRenameListener renameListener;

    public interface OnRenameListener {
        void onRename(String newName);
    }

    public static RenameFileDialog newInstance(String currentName, OnRenameListener listener) {
        RenameFileDialog dialog = new RenameFileDialog();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_NAME, currentName);
        dialog.setArguments(args);
        dialog.renameListener = listener;
        return dialog;
    }

    // Keep the old constructor for backward compatibility but make it safer
    public RenameFileDialog() {
        // Default constructor required for DialogFragment
    }

    public RenameFileDialog(String currentName, OnRenameListener renameListener) {
        this.currentName = currentName;
        this.renameListener = renameListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentName = getArguments().getString(ARG_CURRENT_NAME);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            if (getActivity() == null || isDetached()) {
                Log.e("RenameFileDialog", "Activity is null or fragment is detached");
                return super.onCreateDialog(savedInstanceState);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_rename_file, null);

            EditText editFileName = view.findViewById(R.id.edit_file_name);

            if (editFileName == null) {
                Log.e("RenameFileDialog", "EditText not found in layout");
                return super.onCreateDialog(savedInstanceState);
            }

            String nameWithoutExtension = currentName != null ? currentName : "Untitled";
            if (nameWithoutExtension.toLowerCase().endsWith(".pdf")) {
                nameWithoutExtension = nameWithoutExtension.substring(0, nameWithoutExtension.length() - 4);
            }
            editFileName.setText(nameWithoutExtension);
            editFileName.selectAll();

            builder.setView(view)
                    .setTitle("Rename File")
                    .setPositiveButton("Rename", (dialog, id) -> {
                        try {
                            String newName = editFileName.getText().toString().trim();
                            if (!newName.isEmpty() && renameListener != null) {
                                if (isValidFileName(newName)) {
                                    Log.d("RenameFileDialog", "Attempting to rename to: " + newName);
                                    renameListener.onRename(newName);
                                } else {
                                    if (getContext() != null) {
                                        android.widget.Toast.makeText(getContext(),
                                                "Invalid filename. Avoid these characters: / \\ : * ? \" < > |",
                                                android.widget.Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else if (newName.isEmpty()) {
                                if (getContext() != null) {
                                    android.widget.Toast.makeText(getContext(),
                                            "Please enter a filename",
                                            android.widget.Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("RenameFileDialog", "Error in rename button click", e);
                            if (getContext() != null) {
                                android.widget.Toast.makeText(getContext(),
                                        "Error occurred while renaming",
                                        android.widget.Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null);

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(dialogInterface -> {
                try {
                    if (getActivity() != null && !isDetached()) {
                        editFileName.requestFocus();
                        InputMethodManager imm =
                                (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.showSoftInput(editFileName, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                } catch (Exception e) {
                    Log.w("RenameFileDialog", "Error showing keyboard", e);
                }
            });

            return dialog;
        } catch (Exception e) {
            Log.e("RenameFileDialog", "Error creating dialog", e);
            return super.onCreateDialog(savedInstanceState);
        }
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
