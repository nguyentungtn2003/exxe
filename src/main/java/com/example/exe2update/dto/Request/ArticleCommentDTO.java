package com.example.exe2update.dto.Request;

public class ArticleCommentDTO {
    private String name;
    private String email;
    private String content;
    private Boolean isAdminReply;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsAdminReply() {
        return isAdminReply;
    }

    public void setIsAdminReply(Boolean isAdminReply) {
        this.isAdminReply = isAdminReply;
    }
}
