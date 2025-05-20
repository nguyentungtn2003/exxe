package com.example.exe2update.repository;

import com.example.exe2update.entity.ArticleComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleCommentRepository extends JpaRepository<ArticleComment, Integer> {
    List<ArticleComment> findByArticle_ArticleId(Integer articleId);
    List<ArticleComment> findByArticle_ArticleIdAndParentCommentIsNullOrderByCreatedAtAsc(Integer articleId);
    List<ArticleComment> findByParentCommentCommentId(Integer parentCommentId);

}
