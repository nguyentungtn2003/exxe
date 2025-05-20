package com.example.exe2update.service.impl;

import com.example.exe2update.entity.Article;
import com.example.exe2update.entity.ArticleComment;
import com.example.exe2update.repository.ArticleCommentRepository;
import com.example.exe2update.service.ArticleCommentService;
import com.example.exe2update.service.ArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleCommentServiceImpl implements ArticleCommentService {

    @Autowired
    private ArticleCommentRepository commentRepository;

    @Autowired
    private ArticlesService articlesService;

    @Override
    public List<ArticleComment> getCommentsByArticleId(Integer articleId) {
        return commentRepository.findByArticle_ArticleId(articleId);
    }

    @Override
    public List<ArticleComment> getTopLevelCommentsByArticleId(Integer articleId) {
        return commentRepository.findByArticle_ArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(articleId);
    }

    @Override
    public void postComment(ArticleComment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    public void postReplyComment(Integer parentCommentId, String name, String email, String message, Integer articleId) {
        Article article = articlesService.getArticleById(articleId);
        if (article == null) {
            throw new IllegalArgumentException("Article not found");
        }

        ArticleComment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

        ArticleComment reply = new ArticleComment();
        reply.setArticle(article);
        reply.setParentComment(parentComment);
        reply.setName(name);
        reply.setEmail(email);
        reply.setContent(message);
        reply.setCreatedAt(LocalDateTime.now());

        commentRepository.save(reply);
    }
}
