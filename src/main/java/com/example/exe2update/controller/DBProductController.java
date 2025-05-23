package com.example.exe2update.controller;

import com.example.exe2update.entity.Category;
import com.example.exe2update.entity.Product;
import com.example.exe2update.repository.CategoryRepository;
import com.example.exe2update.repository.ProductRepository;
import com.example.exe2update.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dbproduct")
public class DBProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductService productService;

    @GetMapping
    public String listProducts(Model model, Authentication authentication) {
        String username = authentication.getName();

        model.addAttribute("username", username);
        model.addAttribute("products", productRepository.findAll());
        return "dbproduct"; // trang list sản phẩm
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("actionUrl", "/dbproduct/add");
        model.addAttribute("submitLabel", "Add Product");
        return "addnewproduct"; // view form thêm mới
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Integer id, Model model) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return "redirect:/dbproduct";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("actionUrl", "/dbproduct/edit");
        model.addAttribute("submitLabel", "Update Product");
        return "addnewproduct"; // view form chỉnh sửa
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("product") Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) {

        if (!imageFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists())
                    uploadFolder.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File destinationFile = new File(uploadDir + fileName);
                imageFile.transferTo(destinationFile);

                product.setImageUrl("/uploads/" + fileName);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Image upload failed!");
                model.addAttribute("categories", categoryRepository.findAll());
                return "addnewproduct";
            }
        }

        if (product.getCategory() != null && product.getCategory().getCategoryId() != null) {
            Long categoryId = product.getCategory().getCategoryId().longValue();
            Category category = categoryRepository.findById(categoryId).orElse(null);
            product.setCategory(category);
        }

        product.setCreatedAt(LocalDateTime.now());

        productRepository.save(product);
        return "redirect:/dbproduct";
    }

    @PostMapping("/edit")
    public String updateProduct(@ModelAttribute("product") Product product,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) {

        Product existing = productRepository.findById(product.getProductId()).orElse(null);
        if (existing == null) {
            return "redirect:/dbproduct";
        }

        if (!imageFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists())
                    uploadFolder.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                File destinationFile = new File(uploadDir + fileName);
                imageFile.transferTo(destinationFile);
                product.setImageUrl("/uploads/" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("errorMessage", "Image upload failed!");
                model.addAttribute("categories", categoryRepository.findAll());
                return "addnewproduct";
            }
        } else {
            product.setImageUrl(existing.getImageUrl());
        }

        product.setCreatedAt(existing.getCreatedAt());

        if (product.getCategory() != null && product.getCategory().getCategoryId() != null) {
            Long categoryId = product.getCategory().getCategoryId().longValue();
            Category category = categoryRepository.findById(categoryId).orElse(null);
            product.setCategory(category);
        }

        productRepository.save(product);
        return "redirect:/dbproduct";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam("id") Integer id) {
        productRepository.deleteById(id);
        return "redirect:/dbproduct";
    }

    // AJAX search
    @GetMapping("/search")
    public String searchProducts(@RequestParam(name = "name", required = false) String name, Model model) {
        List<Product> products;
        if (name == null || name.isEmpty()) {
            products = productService.getAllProducts();
        } else {
            products = productService.findByNameContainingIgnoreCase(name);
        }
        model.addAttribute("products", products);
        return "dbproduct :: productRows"; // trả về fragment tbody
    }
}
