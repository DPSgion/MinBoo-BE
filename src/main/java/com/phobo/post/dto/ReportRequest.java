package com.phobo.post.dto;

public class ReportRequest {
    private String reason;
    private String description;

    // Getters và Setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}