package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

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
        editFileName.setText(currentName);
        editFileName.selectAll();

        builder.setView(view)
                .setTitle("Rename File")
                .setPositiveButton("Rename", (dialog, id) -> {
                    String newName = editFileName.getText().toString().trim();
                    if (!newName.isEmpty() && renameListener != null) {
                        renameListener.onRename(newName);
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }
}