package com.example.exe2update.service;

import com.example.exe2update.entity.ArticleComment;

import java.util.List;

public interface ArticleCommentService {
    List<ArticleComment> getCommentsByArticleId(Integer articleId);
    List<ArticleComment> getTopLevelCommentsByArticleId(Integer articleId);
    void postComment(ArticleComment comment);
    void postReplyComment(Integer parentCommentId, String name, String email, String message, Integer articleId);
}
