package com.example.exe2update.controller;

import com.example.exe2update.entity.Category;
import com.example.exe2update.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dbcategory")
public class DBCategoryController {

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách ban đầu
    @GetMapping
    public String getCategory(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("username", username);
        model.addAttribute("categories", categories);
        model.addAttribute("newCategory", new Category());
        return "dbcategory";
    }

    // Tìm kiếm bằng AJAX (dành cho fetch từ JS)
    @ResponseBody
    @GetMapping("/search")
    public List<Category> searchCategory(@RequestParam("keyword") String keyword) {
        return categoryService.searchByName(keyword);
    }

    // Thêm mới category
    @PostMapping("/add")
    public String addCategory(@ModelAttribute("newCategory") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/dbcategory";
    }

    // Sửa category
    @PostMapping("/edit")
    public String editCategory(@ModelAttribute Category category) {
        categoryService.updateCategory(category);
        return "redirect:/dbcategory";
    }

    // Xoá category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/dbcategory";
    }

    // Lấy thông tin category theo ID (để hiển thị lên form edit qua AJAX)
    @ResponseBody
    @GetMapping("/get/{id}")
    public Category getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
}
