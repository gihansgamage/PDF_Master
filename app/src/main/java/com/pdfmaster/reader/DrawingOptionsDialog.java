package com.pdfmaster.reader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class DrawingOptionsDialog extends DialogFragment {

    private DrawingOverlay drawingOverlay;
    private OnColorChangeListener colorChangeListener;
    private OnEraserToggleListener eraserToggleListener;

    public interface OnColorChangeListener {
        void onColorChange(int color);
    }

    public interface OnEraserToggleListener {
        void onEraserToggle(boolean isEraser);
    }

    public DrawingOptionsDialog(DrawingOverlay drawingOverlay,
                                OnColorChangeListener colorChangeListener,
                                OnEraserToggleListener eraserToggleListener) {
        this.drawingOverlay = drawingOverlay;
        this.colorChangeListener = colorChangeListener;
        this.eraserToggleListener = eraserToggleListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_drawing_options, null);

        setupColorButtons(view);
        setupActionButtons(view);

        builder.setView(view)
                .setTitle("Drawing Options")
                .setNegativeButton("Close", null);

        return builder.create();
    }

    private void setupColorButtons(View view) {
        Button btnRed = view.findViewById(R.id.btn_color_red);
        Button btnBlue = view.findViewById(R.id.btn_color_blue);
        Button btnGreen = view.findViewById(R.id.btn_color_green);
        Button btnBlack = view.findViewById(R.id.btn_color_black);

        btnRed.setOnClickListener(v -> changeColor(Color.RED));
        btnBlue.setOnClickListener(v -> changeColor(Color.BLUE));
        btnGreen.setOnClickListener(v -> changeColor(Color.GREEN));
        btnBlack.setOnClickListener(v -> changeColor(Color.BLACK));
    }

    private void setupActionButtons(View view) {
        Button btnEraser = view.findViewById(R.id.btn_eraser);
        Button btnClear = view.findViewById(R.id.btn_clear);

        btnEraser.setOnClickListener(v -> {
            if (eraserToggleListener != null) {
                eraserToggleListener.onEraserToggle(true);
            }
            dismiss();
        });

        btnClear.setOnClickListener(v -> {
            drawingOverlay.clearAllDrawings();
            dismiss();
        });
    }

    private void changeColor(int color) {
        if (colorChangeListener != null) {
            colorChangeListener.onColorChange(color);
        }
        dismiss();
    }
}