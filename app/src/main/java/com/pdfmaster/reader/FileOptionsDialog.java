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
        try {
            if (pdfFile != null && pdfFile.getName() != null && getParentFragmentManager() != null) {
                RenameFileDialog dialog = RenameFileDialog.newInstance(pdfFile.getName(), newName -> {
                    try {
                        if (fileManager != null && newName != null && !newName.trim().isEmpty()) {
                            Uri fileUri = Uri.parse(pdfFile.getPath());
                            boolean renameSuccess = fileManager.renameFile(fileUri, newName);
                            if (renameSuccess) {
                                String finalName = newName.endsWith(".pdf") ? newName : newName + ".pdf";
                                pdfFile.setName(finalName);
                                Toast.makeText(getContext(), "File renamed successfully to: " + finalName, Toast.LENGTH_SHORT).show();
                            } else {
                                // Provide more specific error messages based on common failure reasons
                                String errorMessage = "Failed to rename file. ";
                                if (!DocumentsContract.isDocumentUri(getContext(), fileUri)) {
                                    errorMessage += "File location doesn't support renaming.";
                                } else {
                                    errorMessage += "Check if file is read-only or in use.";
                                }
                                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                Log.w("FileOptionsDialog", "Rename failed for URI: " + fileUri.toString());
                            }
                        } else {
                            Toast.makeText(getContext(), "Invalid file or name", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("FileOptionsDialog", "Error during rename operation", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error occurred while renaming: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                dialog.show(getParentFragmentManager(), "RenameFile");
            } else {
                Toast.makeText(getContext(), "Cannot rename file at this time", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("FileOptionsDialog", "Error showing rename dialog", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error opening rename dialog", Toast.LENGTH_SHORT).show();
            }
        }
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

            // Try to open file manager with ACTION_GET_CONTENT to browse files
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(getContext(), "Navigate to your PDF file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fallback 1: Try to open Downloads folder directly
            try {
                Intent downloadsIntent = new Intent(Intent.ACTION_VIEW);
                downloadsIntent.setDataAndType(Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload"), "resource/folder");
                downloadsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (downloadsIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(downloadsIntent);
                    Toast.makeText(getContext(), "Check Downloads folder for your PDF", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.w("FileOptions", "Could not open Downloads folder", e);
            }

            // Fallback 2: Open file manager app directly
            try {
                Intent fileManagerIntent = new Intent("android.intent.action.MAIN");
                fileManagerIntent.addCategory("android.intent.category.APP_FILES");
                fileManagerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (fileManagerIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(fileManagerIntent);
                    Toast.makeText(getContext(), "File manager opened", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Log.w("FileOptions", "Could not open file manager", e);
            }

            // Fallback 3: Try specific file manager apps
            String[] fileManagerPackages = {
                    "com.google.android.documentsui",
                    "com.android.documentsui",
                    "com.mi.android.globalFileexplorer",
                    "com.estrongs.android.pop"
            };

            for (String packageName : fileManagerPackages) {
                try {
                    Intent packageIntent = requireContext().getPackageManager().getLaunchIntentForPackage(packageName);
                    if (packageIntent != null) {
                        startActivity(packageIntent);
                        Toast.makeText(getContext(), "File manager opened", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Log.w("FileOptions", "Could not open " + packageName, e);
                }
            }

            // If all else fails
            Toast.makeText(getContext(), "No file manager found. Please install a file manager app.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e("FileOptions", "Error opening file location", e);
            Toast.makeText(getContext(), "Unable to open file location", Toast.LENGTH_SHORT).show();
        }
    }
}
