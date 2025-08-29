package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class FileOptionsDialog extends DialogFragment {

    private PDFFile pdfFile;
    private FileManager fileManager;

    public FileOptionsDialog(PDFFile pdfFile, FileManager fileManager) {
        this.pdfFile = pdfFile;
        this.fileManager = fileManager;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_file_options, null);

        setupButtons(view);

        builder.setView(view)
                .setTitle(pdfFile.getName())
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    private void setupButtons(View view) {
        Button btnOpen = view.findViewById(R.id.btn_open);
        Button btnRename = view.findViewById(R.id.btn_rename);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        Button btnShare = view.findViewById(R.id.btn_share);
        Button btnOpenLocation = view.findViewById(R.id.btn_open_location);

        btnOpen.setOnClickListener(v -> {
            openFile();
            dismiss();
        });

        btnRename.setOnClickListener(v -> {
            showRenameDialog();
            dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmation();
            dismiss();
        });

        btnShare.setOnClickListener(v -> {
            shareFile();
            dismiss();
        });

        btnOpenLocation.setOnClickListener(v -> {
            openFileLocation();
            dismiss();
        });
    }

    private void openFile() {
        Intent intent = new Intent(getContext(), PDFViewActivity.class);
        intent.setData(Uri.parse(pdfFile.getPath()));
        startActivity(intent);
    }

    private void showRenameDialog() {
        RenameFileDialog dialog = new RenameFileDialog(pdfFile.getName(), newName -> {
            if (fileManager.renameFile(Uri.parse(pdfFile.getPath()), newName)) {
                pdfFile.setName(newName);
                Toast.makeText(getContext(), "File renamed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to rename file", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getParentFragmentManager(), "RenameFile");
    }

    private void showDeleteConfirmation() {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(() -> {
            if (fileManager.deleteFile(Uri.parse(pdfFile.getPath()))) {
                fileManager.removeFromRecentFiles(pdfFile.getPath());
                Toast.makeText(getContext(), "File deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete file", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getParentFragmentManager(), "DeleteConfirmation");
    }

    private void shareFile() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(pdfFile.getPath()));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Sharing: " + pdfFile.getName());
        startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
    }

    private void openFileLocation() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(pdfFile.getPath()), "resource/folder");
        if (intent.resolveActivityInfo(requireContext().getPackageManager(), 0) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No file manager found", Toast.LENGTH_SHORT).show();
        }
    }
}