package com.example.exe2update.repository;

import com.example.exe2update.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticlesRepository extends JpaRepository<Article, Integer> {
    @Query("SELECT a, u.username FROM Article a JOIN User u ON a.userId = u.userId")
    List<Object[]> findAllArticlesWithAuthorUsername();

}
