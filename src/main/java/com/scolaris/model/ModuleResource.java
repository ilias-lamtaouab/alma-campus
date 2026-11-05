package com.scolaris.model;

import java.time.LocalDateTime;

public class ModuleResource {
    private int id;
    private int moduleId;
    private String title;
    private String description;
    private String fileName;
    private String filePath;
    private long fileSize;
    private String uploadedBy;
    private LocalDateTime uploadedAt;

    private String moduleName;
    private String moduleCode;

    public ModuleResource() {}

    public ModuleResource(int id, int moduleId, String title, String description, String fileName, String filePath, long fileSize, String uploadedBy, LocalDateTime uploadedAt) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
}