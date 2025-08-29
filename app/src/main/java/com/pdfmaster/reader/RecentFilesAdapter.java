package com.pdfmaster.reader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecentFilesAdapter extends RecyclerView.Adapter<RecentFilesAdapter.ViewHolder> {

    private List<PDFFile> files;
    private OnFileClickListener fileClickListener;
    private OnFileOptionsClickListener optionsClickListener;

    public interface OnFileClickListener {
        void onFileClick(PDFFile pdfFile);
    }

    public interface OnFileOptionsClickListener {
        void onOptionsClick(PDFFile pdfFile);
    }

    public RecentFilesAdapter(List<PDFFile> files, OnFileClickListener fileClickListener,
                              OnFileOptionsClickListener optionsClickListener) {
        this.files = files;
        this.fileClickListener = fileClickListener;
        this.optionsClickListener = optionsClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PDFFile file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textFileName, textFileSize, textFileDate;
        private ImageButton buttonOptions;

        ViewHolder(View itemView) {
            super(itemView);
            textFileName = itemView.findViewById(R.id.text_file_name);
            textFileSize = itemView.findViewById(R.id.text_file_size);
            textFileDate = itemView.findViewById(R.id.text_file_date);
            buttonOptions = itemView.findViewById(R.id.button_options);
        }

        void bind(PDFFile file) {
            textFileName.setText(file.getName());
            textFileSize.setText(file.getFormattedSize());

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            textFileDate.setText(dateFormat.format(file.getLastModified()));

            itemView.setOnClickListener(v -> {
                if (fileClickListener != null) {
                    fileClickListener.onFileClick(file);
                }
            });

            buttonOptions.setOnClickListener(v -> {
                if (optionsClickListener != null) {
                    optionsClickListener.onOptionsClick(file);
                }
            });
        }
    }
}