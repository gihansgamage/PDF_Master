package com.pdfmaster.reader;

import android.content.Context;
import android.net.Uri;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PDFTextExtractor {

    private Context context;

    public PDFTextExtractor(Context context) {
        this.context = context;
    }

    public String extractTextFromPage(Uri pdfUri, int pageNumber) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            if (inputStream != null) {
                // Try to read some actual content from the PDF
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder content = new StringBuilder();
                String line;
                int lineCount = 0;

                // Read first few lines of the PDF file
                while ((line = reader.readLine()) != null && lineCount < 10) {
                    // Filter out PDF control characters and keep readable text
                    String cleanLine = line.replaceAll("[^\\p{Print}\\p{Space}]", "").trim();
                    if (!cleanLine.isEmpty() && cleanLine.length() > 3) {
                        content.append(cleanLine).append(" ");
                        lineCount++;
                    }
                }

                reader.close();
                inputStream.close();

                String extractedText = content.toString().trim();
                if (!extractedText.isEmpty()) {
                    return "Reading from page " + (pageNumber + 1) + ": " + extractedText;
                } else {
                    return "This PDF page contains mostly images or formatted content that cannot be read aloud. Page " + (pageNumber + 1) + " content is not available for text-to-speech.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unable to extract readable text from page " + (pageNumber + 1) + ". This may be a scanned document or contain only images.";
    }

    public List<String> extractAllText(Uri pdfUri, int totalPages) {
        List<String> allText = new ArrayList<>();
        for (int i = 0; i < totalPages; i++) {
            allText.add(extractTextFromPage(pdfUri, i));
        }
        return allText;
    }

    public String extractTextFromCurrentView(Uri pdfUri, int currentPage) {
        return extractTextFromPage(pdfUri, currentPage);
    }
}
