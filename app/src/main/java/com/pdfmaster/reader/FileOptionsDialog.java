package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.provider.DocumentsContract;

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
        try {
            Uri uri = Uri.parse(pdfFile.getPath());

            // Try to open the default file manager app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setType("resource/folder");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(getContext(), "File manager opened - navigate to your PDF location", Toast.LENGTH_LONG).show();
                return;
            }

            // Fallback: Try to open Files app specifically
            try {
                Intent filesIntent = new Intent();
                filesIntent.setAction(Intent.ACTION_VIEW);
                filesIntent.setData(Uri.parse("content://com.android.externalstorage.documents/root/primary"));
                filesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (filesIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(filesIntent);
                    Toast.makeText(getContext(), "Navigate to your file location", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Log.w("FileOptions", "Could not open Files app", e);
            }

            // Last fallback: Try to open any file manager by package name
            try {
                Intent packageIntent = requireContext().getPackageManager().getLaunchIntentForPackage("com.google.android.documentsui");
                if (packageIntent != null) {
                    startActivity(packageIntent);
                    Toast.makeText(getContext(), "File manager opened", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.w("FileOptions", "Could not open Documents UI", e);
            }

            // If all else fails
            Toast.makeText(getContext(), "No file manager found. Please install a file manager app.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("FileOptions", "Error opening file location", e);
            Toast.makeText(getContext(), "Unable to open file location", Toast.LENGTH_SHORT).show();
        }
    }
}
