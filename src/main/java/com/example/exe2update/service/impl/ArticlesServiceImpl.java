package com.example.exe2update.service.impl;

import com.example.exe2update.entity.Article;
import com.example.exe2update.repository.ArticlesRepository;
import com.example.exe2update.service.ArticlesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ArticlesServiceImpl implements ArticlesService {

    @Autowired
    private ArticlesRepository articlesRepository;
    public List<Article> getAllArticlesWithAuthorName() {
        List<Object[]> rows = articlesRepository.findAllArticlesWithAuthorUsername();
        List<Article> articles = new ArrayList<>();

        for (Object[] row : rows) {
            Article article = (Article) row[0];
            String username = (String) row[1];

            article.setAuthorUsername(username);
            articles.add(article);
        }

        return articles;
    }

    @Override
    public Article getArticleById(Integer id) {
        Optional<Article> article = articlesRepository.findById(id);
        return article.orElse(null); // Hoặc throw exception nếu muốn
    }

    @Override
    public List<Article> findAll() {
        return articlesRepository.findAll();
    }

    @Override
    public Article findById(Integer id) {
        return articlesRepository.findById(id).orElse(null);
    }

    @Override
    public Article save(Article article) {
        return articlesRepository.save(article);
    }

    @Override
    public void deleteById(Integer id) {
        articlesRepository.deleteById(id);
    }
}
