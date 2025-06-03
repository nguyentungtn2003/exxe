package com.example.exe2update.controller;

import com.example.exe2update.entity.Article;
import com.example.exe2update.entity.User;
import com.example.exe2update.repository.UserRepository;
import com.example.exe2update.service.ArticlesService;
import com.example.exe2update.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/articles")
public class DBArticlesController {

    @Autowired
    private ArticlesService articleService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository; // gọi thẳng repo luôn

    private final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public String showArticles(Model model) {
        List<User> admins = userRepository.findAllAdmins();

        model.addAttribute("users", admins);
        model.addAttribute("article", new Article());

        List<Article> articles = articleService.findAll();
        model.addAttribute("articles", articles);

        // Tạo Map userId -> email
        Map<Integer, String> userEmails = admins.stream()
                .collect(Collectors.toMap(User::getUserId, User::getEmail));
        model.addAttribute("userEmails", userEmails);

        return "articles";
    }

    @PostMapping("/add")
    public String addArticle(@ModelAttribute("article") Article article,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilenameRaw = imageFile.getOriginalFilename();
            String originalFilename = originalFilenameRaw != null ? StringUtils.cleanPath(originalFilenameRaw) : "";
            String ext = "";

            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                ext = originalFilename.substring(dotIndex);
            }
            String newFileName = UUID.randomUUID() + ext;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            article.setImageUrl("/" + UPLOAD_DIR + newFileName);
        }

        article.setCreatedAt(LocalDateTime.now());
        articleService.save(article);
        return "redirect:/articles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        Article article = articleService.findById(id);
        if (article == null) {
            return "redirect:/articles";
        }

        List<User> admins = userService.getAllUsers().stream()
                .filter(user -> user.getRole() != null &&
                        "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getRoleName()))
                .collect(Collectors.toList());

        model.addAttribute("article", article);
        model.addAttribute("users", admins);
        return "edit_article";
    }

    @PostMapping("/edit")
    public String editArticle(@ModelAttribute("article") Article article,
            @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        Article existing = articleService.findById(article.getArticleId());
        if (existing == null) {
            return "redirect:/articles";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String originalFilenameRaw = imageFile.getOriginalFilename();
            String originalFilename = originalFilenameRaw != null ? StringUtils.cleanPath(originalFilenameRaw) : "";
            String ext = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                ext = originalFilename.substring(dotIndex);
            }
            String newFileName = UUID.randomUUID() + ext;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            article.setImageUrl("/" + UPLOAD_DIR + newFileName);
        } else {
            article.setImageUrl(existing.getImageUrl());
        }

        article.setCreatedAt(existing.getCreatedAt());
        article.setUpdatedAt(LocalDateTime.now());
        articleService.save(article);
        return "redirect:/articles";
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable("id") Integer id) {
        articleService.deleteById(id);
        return "redirect:/articles";
    }
}
