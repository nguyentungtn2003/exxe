package com.example.exe2update.service;

import com.example.exe2update.entity.Article;
import java.util.List;

public interface ArticlesService {
    List<Article> getAllArticlesWithAuthorName();
    Article getArticleById(Integer id);
    List<Article> findAll();
    Article findById(Integer id);
    Article save(Article article);
    void deleteById(Integer id);
}
