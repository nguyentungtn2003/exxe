//package com.example.exe2update.controller;
//



package com.example.exe2update.controller;

import com.example.exe2update.entity.Article;
import com.example.exe2update.entity.ArticleComment;
import com.example.exe2update.entity.User;
import com.example.exe2update.service.ArticlesService;
import com.example.exe2update.service.ArticleCommentService;
import com.example.exe2update.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ArticleDetailsController {

    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private ArticleCommentService articleCommentService;

    @Autowired
    private UserService userService;

    // Hiển thị chi tiết bài viết và các bình luận gốc (cùng với phản hồi)
    @GetMapping("/article/{id}")
    public String getArticleDetails(Authentication authentication, @PathVariable Integer id, Model model) {
        Article article = articlesService.getArticleById(id);
        if (article == null) {
            model.addAttribute("error", "Article not found");
            return "error";
        }

        List<ArticleComment> topLevelComments = articleCommentService.getTopLevelCommentsByArticleId(id);
        User author = userService.findById(article.getUserId());
        String username = authentication != null ? authentication.getName() : "";

        model.addAttribute("article", article);
        model.addAttribute("comments", topLevelComments); // chỉ truyền bình luận gốc
        model.addAttribute("authorName", author.getFullName());
        model.addAttribute("articleId", article.getArticleId());
        model.addAttribute("username", username);

        return "articles-details"; // view Thymeleaf
    }

    // Đăng bình luận mới
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post-comment")
    public String postComment(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String message,
                              @RequestParam Integer articleId,
                              RedirectAttributes redirectAttributes) {

        Article article = articlesService.getArticleById(articleId);
        if (article == null) {
            redirectAttributes.addFlashAttribute("error", "Article not found.");
            return "redirect:/error";
        }

        ArticleComment comment = new ArticleComment();
        comment.setName(name);
        comment.setEmail(email);
        comment.setContent(message);
        comment.setArticle(article);
        comment.setCreatedAt(LocalDateTime.now());

        articleCommentService.postComment(comment);

        redirectAttributes.addFlashAttribute("message", "Comment posted successfully!");
        return "redirect:/article/" + articleId;
    }

    // Đăng phản hồi bình luận
    @PostMapping("/post-reply")
    public String postReply(@RequestParam String name,
                            @RequestParam String email,
                            @RequestParam String message,
                            @RequestParam Integer articleId,
                            @RequestParam Integer parentCommentId,
                            RedirectAttributes redirectAttributes) {

        Article article = articlesService.getArticleById(articleId);
        if (article == null) {
            redirectAttributes.addFlashAttribute("error", "Article not found.");
            return "redirect:/error";
        }

        articleCommentService.postReplyComment(parentCommentId, name, email, message, articleId);

        redirectAttributes.addFlashAttribute("message", "Reply posted successfully!");
        return "redirect:/article/" + articleId;
    }
}

//import com.example.exe2update.entity.Article;
//import com.example.exe2update.entity.ArticleComment;
//import com.example.exe2update.entity.User;
//import com.example.exe2update.service.ArticlesService;
//import com.example.exe2update.service.ArticleCommentService;
//import com.example.exe2update.service.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Controller
//public class ArticleDetailsController {
//
//    @Autowired
//    private ArticlesService articlesService;
//
//    @Autowired
//    private ArticleCommentService articleCommentService;
//
//    @Autowired
//    private UserService userService;
//
//    // Hiển thị chi tiết bài viết và các bình luận
//    @GetMapping("/article/{id}")
//    public String getArticleDetails(Authentication authentication, @PathVariable Integer id, Model model) {
//        Article article = articlesService.getArticleById(id);
//        List<ArticleComment> comments = articleCommentService.getCommentsByArticleId(id);
//        User author = userService.findById(article.getUserId());
//        String username = authentication.getName();
//
//
//        if (article == null) {
//            model.addAttribute("error", "Article not found");
//            return "error";
//        }
//
//        model.addAttribute("article", article);
//        model.addAttribute("comments", comments);
//        model.addAttribute("authorName", author.getFullName());
//        model.addAttribute("articleId", article.getArticleId());
//        model.addAttribute("username", username);
//
//
//        return "articles-details"; // Tên của view cần render
//    }
//
//    // Xử lý việc đăng bình luận
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/post-comment")
//    public String postComment(@RequestParam String name,
//                              @RequestParam String email,
//                              @RequestParam String message,
//                              @RequestParam Integer articleId,
//                              RedirectAttributes redirectAttributes) {
//        // Lấy bài viết từ articleId
//        Article article = articlesService.getArticleById(articleId);
//
//        if (article == null) {
//            redirectAttributes.addFlashAttribute("error", "Article not found.");
//            return "redirect:/error"; // Nếu không tìm thấy bài viết, chuyển đến trang lỗi
//        }
//
//        // Tạo bình luận mới
//        ArticleComment comment = new ArticleComment();
//        comment.setName(name);
//        comment.setEmail(email);
//        comment.setContent(message);
//        comment.setArticle(article);
//        comment.setCreatedAt(LocalDateTime.now());
//
//        // Lưu bình luận vào DB
//        articleCommentService.postComment(comment);
//
//        // Chuyển hướng lại bài viết cùng bình luận
//        redirectAttributes.addFlashAttribute("message", "Comment posted successfully!");
//        return "redirect:/article/" + articleId;
//    }
//
//
//    // Xử lý việc đăng phản hồi bình luận
//    @PostMapping("/post-reply")
//    public String postReply(@RequestParam String name,
//                            @RequestParam String email,
//                            @RequestParam String message,
//                            @RequestParam Integer articleId,
//                            @RequestParam Integer parentCommentId,
//                            RedirectAttributes redirectAttributes) {
//
//        Article article = articlesService.getArticleById(articleId);
//
//        if (article == null) {
//            redirectAttributes.addFlashAttribute("error", "Article not found.");
//            return "redirect:/error";
//        }
//
//        // Lưu bình luận trả lời
//        articleCommentService.postReplyComment(parentCommentId, name, email, message, articleId);
//
//        redirectAttributes.addFlashAttribute("message", "Reply posted successfully!");
//        return "redirect:/article/" + articleId;
//    }
//
//}
