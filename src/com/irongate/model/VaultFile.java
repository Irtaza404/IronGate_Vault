package com.irongate.model;

import java.time.LocalDateTime;

public class VaultFile {
    private int           fileId;
    private int           userId;
    private String        fileName;
    private String        fileType;
    private String        filePath;
    private long          fileSize;
    private LocalDateTime uploadDate;
    private LocalDateTime lastModified;
    private String        fileStatus;

    public VaultFile() {}

    // Getters
    public int           getFileId()       { return fileId; }
    public int           getUserId()       { return userId; }
    public String        getFileName()     { return fileName; }
    public String        getFileType()     { return fileType; }
    public String        getFilePath()     { return filePath; }
    public long          getFileSize()     { return fileSize; }
    public LocalDateTime getUploadDate()   { return uploadDate; }
    public LocalDateTime getLastModified() { return lastModified; }
    public String        getFileStatus()   { return fileStatus; }

    // Setters
    public void setFileId(int v)              { fileId       = v; }
    public void setUserId(int v)              { userId       = v; }
    public void setFileName(String v)         { fileName     = v; }
    public void setFileType(String v)         { fileType     = v; }
    public void setFilePath(String v)         { filePath     = v; }
    public void setFileSize(long v)           { fileSize     = v; }
    public void setUploadDate(LocalDateTime v){ uploadDate   = v; }
    public void setLastModified(LocalDateTime v){ lastModified = v; }
    public void setFileStatus(String v)       { fileStatus   = v; }

    /** Human-readable size string */
    public String getDisplaySize() {
        if (fileSize < 1024)             return fileSize + " B";
        if (fileSize < 1024 * 1024)      return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024L * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024));
        return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
    }
}
