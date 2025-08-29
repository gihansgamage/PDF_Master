package com.pdfmaster.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawingOverlay extends View {

    private Paint paint;
    private Path currentPath;
    private List<DrawingPath> paths;
    private int currentColor = Color.RED;
    private boolean isEraserMode = false;
    private float strokeWidth = 5f;

    public DrawingOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(currentColor);

        paths = new ArrayList<>();
        currentPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw all saved paths
        for (DrawingPath drawingPath : paths) {
            paint.setColor(drawingPath.color);
            paint.setStrokeWidth(drawingPath.strokeWidth);
            canvas.drawPath(drawingPath.path, paint);
        }

        // Draw current path
        paint.setColor(isEraserMode ? Color.WHITE : currentColor);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawPath(currentPath, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startDrawing(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                continueDrawing(x, y);
                break;
            case MotionEvent.ACTION_UP:
                finishDrawing();
                break;
        }

        invalidate();
        return true;
    }

    private void startDrawing(float x, float y) {
        currentPath.reset();
        currentPath.moveTo(x, y);
    }

    private void continueDrawing(float x, float y) {
        currentPath.lineTo(x, y);
    }

    private void finishDrawing() {
        if (isEraserMode) {
            eraseAtPath(currentPath);
        } else {
            DrawingPath drawingPath = new DrawingPath(new Path(currentPath), currentColor, strokeWidth);
            paths.add(drawingPath);
        }
        currentPath.reset();
    }

    private void eraseAtPath(Path eraserPath) {
        // Simple erase implementation - remove intersecting paths
        paths.removeIf(drawingPath -> pathsIntersect(drawingPath.path, eraserPath));
    }

    private boolean pathsIntersect(Path path1, Path path2) {
        // Simplified intersection check - in a real app you'd need more sophisticated logic
        return true; // This is a placeholder
    }

    public void setPenColor(int color) {
        this.currentColor = color;
        this.isEraserMode = false;
    }

    public void setEraserMode(boolean eraserMode) {
        this.isEraserMode = eraserMode;
    }

    public void clearAllDrawings() {
        paths.clear();
        currentPath.reset();
        invalidate();
    }

    private static class DrawingPath {
        Path path;
        int color;
        float strokeWidth;

        DrawingPath(Path path, int color, float strokeWidth) {
            this.path = path;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }
    }
}