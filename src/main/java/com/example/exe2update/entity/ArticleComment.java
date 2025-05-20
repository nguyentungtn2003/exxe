package com.example.exe2update.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ArticleComments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArticleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article; // Bài viết mà comment vào

    @Column(nullable = false)
    private String name; // Tên người bình luận

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private ArticleComment parentComment; // Bình luận cha (nếu có)

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArticleComment> replies; // Danh sách các bình luận trả lời

    @Column(nullable = false)
    private String email; // Email người bình luận

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content; // Nội dung bình luận

    @Column(nullable = false)
    private Boolean isAdminReply = false; // TRUE: nếu đây là trả lời của admin

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
