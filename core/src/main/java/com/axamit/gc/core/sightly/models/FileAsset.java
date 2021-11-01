package com.axamit.gc.core.sightly.models;

public class FileAsset {

    private FileType fileType;

    private String fileName;

    private String path;


    public FileAsset(FileType fileType, String path, String fileName) {
        this.fileType = fileType;
        this.fileName = fileName;
        this.path = path;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public enum FileType {
        IMAGE,
        OTHER
    }
}
