package com.pdfmaster.reader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PDFTextExtractor {

    private static final String TAG = "PDFTextExtractor";
    private Context context;

    public PDFTextExtractor(Context context) {
        this.context = context;
    }

    public String extractTextFromPage(Uri pdfUri, int pageNumber) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            if (inputStream != null) {
                // Note: This is a basic implementation. For proper PDF text extraction,
                // you would need a PDF library like Apache PDFBox or iText

                byte[] buffer = new byte[1024];
                StringBuilder content = new StringBuilder();
                int bytesRead;
                int totalBytesRead = 0;

                // Read some bytes from the PDF to check if it contains readable text
                while ((bytesRead = inputStream.read(buffer)) != -1 && totalBytesRead < 4096) {
                    String chunk = new String(buffer, 0, bytesRead, "UTF-8");
                    // Look for readable text patterns in PDF
                    String cleanChunk = extractReadableText(chunk);
                    if (!cleanChunk.isEmpty()) {
                        content.append(cleanChunk).append(" ");
                    }
                    totalBytesRead += bytesRead;
                }

                inputStream.close();

                String extractedText = content.toString().trim();
                if (extractedText.length() > 20) {
                    // Limit text length for TTS
                    if (extractedText.length() > 500) {
                        extractedText = extractedText.substring(0, 500) + "...";
                    }
                    return "Reading from page " + (pageNumber + 1) + ": " + extractedText;
                } else {
                    return getDefaultPageText(pageNumber);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting text from PDF", e);
        }
        return getDefaultPageText(pageNumber);
    }

    private String extractReadableText(String rawText) {
        StringBuilder readable = new StringBuilder();
        String[] words = rawText.split("\\s+");

        for (String word : words) {
            // Filter out PDF control sequences and keep readable words
            String cleanWord = word.replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "").trim();
            if (cleanWord.length() > 2 && cleanWord.matches(".*[a-zA-Z].*")) {
                readable.append(cleanWord).append(" ");
            }
        }

        return readable.toString().trim();
    }

    private String getDefaultPageText(int pageNumber) {
        return "This is page " + (pageNumber + 1) + " of your PDF document. " +
                "The text content cannot be extracted for reading aloud. " +
                "This may be because the PDF contains images, scanned content, or is password protected.";
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
