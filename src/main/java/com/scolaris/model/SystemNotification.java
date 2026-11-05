package com.scolaris.model;

import java.time.LocalDateTime;

public class SystemNotification {
    private int id;
    private String sender;
    private String targetType; // 'ALL_STUDENTS', 'SPECIFIC_STUDENT', 'ALL_TEACHERS', 'SPECIFIC_TEACHER', 'ALL'
    private String targetUsername;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public SystemNotification() {}

    public SystemNotification(int id, String sender, String targetType, String targetUsername, String title, String content, LocalDateTime createdAt) {
        this.id = id;
        this.sender = sender;
        this.targetType = targetType;
        this.targetUsername = targetUsername;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
