package com.pdfmaster.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HighlightManager {

    private Context context;
    private SharedPreferences preferences;
    private boolean highlightMode = false;
    private List<Highlight> highlights;

    public HighlightManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("highlights", Context.MODE_PRIVATE);
        this.highlights = new ArrayList<>();
        loadHighlights();
    }

    public void toggleHighlightMode() {
        highlightMode = !highlightMode;
    }

    public boolean isHighlightMode() {
        return highlightMode;
    }

    public void addHighlight(String text, int pageNumber, float startX, float startY, float endX, float endY) {
        Highlight highlight = new Highlight(text, pageNumber, startX, startY, endX, endY, Color.YELLOW);
        highlights.add(highlight);
        saveHighlights();
    }

    public void removeHighlight(Highlight highlight) {
        highlights.remove(highlight);
        saveHighlights();
    }

    public List<Highlight> getHighlights() {
        return new ArrayList<>(highlights);
    }

    public List<Highlight> getHighlightsForPage(int pageNumber) {
        List<Highlight> pageHighlights = new ArrayList<>();
        for (Highlight highlight : highlights) {
            if (highlight.getPageNumber() == pageNumber) {
                pageHighlights.add(highlight);
            }
        }
        return pageHighlights;
    }

    private void saveHighlights() {
        Set<String> highlightStrings = new HashSet<>();
        for (Highlight highlight : highlights) {
            highlightStrings.add(highlight.toString());
        }
        preferences.edit().putStringSet("saved_highlights", highlightStrings).apply();
    }

    private void loadHighlights() {
        Set<String> highlightStrings = preferences.getStringSet("saved_highlights", new HashSet<>());
        highlights.clear();
        for (String highlightString : highlightStrings) {
            highlights.add(Highlight.fromString(highlightString));
        }
    }

    public static class Highlight {
        private String text;
        private int pageNumber;
        private float startX, startY, endX, endY;
        private int color;

        public Highlight(String text, int pageNumber, float startX, float startY,
                         float endX, float endY, int color) {
            this.text = text;
            this.pageNumber = pageNumber;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.color = color;
        }

        // Getters
        public String getText() { return text; }
        public int getPageNumber() { return pageNumber; }
        public float getStartX() { return startX; }
        public float getStartY() { return startY; }
        public float getEndX() { return endX; }
        public float getEndY() { return endY; }
        public int getColor() { return color; }

        @Override
        public String toString() {
            return text + "|" + pageNumber + "|" + startX + "|" + startY + "|" + endX + "|" + endY + "|" + color;
        }

        public static Highlight fromString(String str) {
            String[] parts = str.split("\\|");
            return new Highlight(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Float.parseFloat(parts[2]),
                    Float.parseFloat(parts[3]),
                    Float.parseFloat(parts[4]),
                    Float.parseFloat(parts[5]),
                    Integer.parseInt(parts[6])
            );
        }
    }
}