package com.example.exe2update;

import com.example.exe2update.entity.Product;
import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.entity.Category;
import com.example.exe2update.repository.CategoryRepository;
import com.example.exe2update.repository.ProductRepository;
import com.example.exe2update.repository.RoleRepository;
import com.example.exe2update.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostConstruct
    public void init() {
        createAdminIfNotExist();
        createUserIfNotExist();
        createCategoryAndProductsIfNotExist();
    }

    private void createAdminIfNotExist() {
        if (userRepository.findByEmailNormalized("admin@example.com").isEmpty() && userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByRoleName("ADMIN");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setRoleName("ADMIN");
                roleRepository.save(adminRole);
            }

            User admin = new User();
            admin.setFullName("Administrator");
            admin.setEmail("admin@example.com");
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setPhone("0123456789");
            admin.setAddress("Hanoi, Vietnam");
            admin.setRole(adminRole);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setStatus(true);

            userRepository.save(admin);
            System.out.println("Created admin user.");
        } else {
            System.out.println("Admin user already exists.");
        }
    }

    private void createUserIfNotExist() {
        if (userRepository.findByEmailNormalized("user@example.com").isEmpty() && userRepository.findByUsername("user").isEmpty()) {
            Role userRole = roleRepository.findByRoleName("USER");
            if (userRole == null) {
                userRole = new Role();
                userRole.setRoleName("USER");
                roleRepository.save(userRole);
            }

            User user = new User();
            user.setFullName("Normal User");
            user.setEmail("user@example.com");
            user.setUsername("user");
            user.setPasswordHash(passwordEncoder.encode("user123"));
            user.setPhone("0987654321");
            user.setAddress("Hanoi, Vietnam");
            user.setRole(userRole);
            user.setCreatedAt(LocalDateTime.now());
            user.setStatus(true);

            userRepository.save(user);
            System.out.println("Created normal user.");
        } else {
            System.out.println("Normal user already exists.");
        }
    }

    private void createCategoryAndProductsIfNotExist() {
        if (categoryRepository.count() == 0) {
            Category category = new Category();
            category.setName("Electronics");
            category.setDescription("Electronic gadgets and devices");
            category = categoryRepository.save(category);
            System.out.println("Created default category: " + category.getName());

            if (productRepository.count() == 0) {
                addProduct("Smartphone X10", "Latest model with AI features", new BigDecimal("20000"), 100, "https://suckhoedoisong.qltns.mediacdn.vn/324455921873985536/2022/6/18/luu-y-khi-an-muop-cach-trong-muop-trong-trhung-xop-2345-9872-1655534034556435234420.jpg", category, true, 0.15);
                addProduct("Laptop Z5", "Powerful gaming laptop", new BigDecimal("35000"), 50, "https://thuocdantoc.vn/wp-content/uploads/2019/06/chua-benh-tri-bang-xo-muop-574x385.jpg", category, true, null);
                addProduct("Wireless Earbuds", "Noise cancelling earbuds", new BigDecimal("5000"), 200, "https://www.vwu.vn/documents/246915/6071816/xo+muop+2.jpg/aa1be85b-5912-4f97-8f8c-7606b266e87b", category, true, 0.10);
                System.out.println("Created default products.");
            } else {
                System.out.println("Products already exist.");
            }
        } else {
            System.out.println("Category already exists.");
        }
    }

    private void addProduct(String name, String description, BigDecimal price, int stock, String imageUrl, Category category, boolean isActive, Double discount) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setCreatedAt(LocalDateTime.now());
        product.setIsActive(isActive);
        product.setDiscount(discount);
        productRepository.save(product);
        System.out.println("Added product: " + name);
    }
}
