package com.pdfmaster.reader;

import java.util.Date;

public class PDFFile {
    private String name;
    private String path;
    private long size;
    private Date lastModified;
    private int pageCount;

    public PDFFile(String name, String path, long size, Date lastModified) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
        this.pageCount = 0; // Will be set when PDF is loaded
    }

    public PDFFile(String name, String path, long size, Date lastModified, int pageCount) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
        this.pageCount = pageCount;
    }

    // Getters
    public String getName() { return name; }
    public String getPath() { return path; }
    public long getSize() { return size; }
    public Date getLastModified() { return lastModified; }
    public int getPageCount() { return pageCount; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPath(String path) { this.path = path; }
    public void setSize(long size) { this.size = size; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public String getFormattedSize() {
        if (size <= 0) return "Unknown size";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }

    public String getPageInfo() {
        if (pageCount > 0) {
            return pageCount + " pages";
        }
        return "Unknown pages";
    }
}