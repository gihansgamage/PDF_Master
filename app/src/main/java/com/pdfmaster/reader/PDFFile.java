package com.pdfmaster.reader;

import java.util.Date;

public class PDFFile {
    private String name;
    private String path;
    private long size;
    private Date lastModified;

    public PDFFile(String name, String path, long size, Date lastModified) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
    }

    // Getters
    public String getName() { return name; }
    public String getPath() { return path; }
    public long getSize() { return size; }
    public Date getLastModified() { return lastModified; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPath(String path) { this.path = path; }
    public void setSize(long size) { this.size = size; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }

    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }
}