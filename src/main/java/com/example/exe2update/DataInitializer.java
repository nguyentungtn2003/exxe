package com.example.exe2update;

import com.example.exe2update.entity.Product;
import com.example.exe2update.entity.Role;
import com.example.exe2update.entity.User;
import com.example.exe2update.repository.ArticlesRepository;
import com.example.exe2update.entity.Article;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {

    private final ArticlesRepository articlesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    DataInitializer(ArticlesRepository articlesRepository) {
        this.articlesRepository = articlesRepository;
    }

    @PostConstruct
    public void init() {
        createAdminIfNotExist();
        createUserIfNotExist();
        createCategoryAndProductsIfNotExist();
        createArticlesIfNotExist();
    }

    private void createAdminIfNotExist() {
        if (userRepository.findByEmailNormalized("admin@example.com").isEmpty()
                && userRepository.findByUsername("admin").isEmpty()) {
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
        if (userRepository.findByEmailNormalized("user@example.com").isEmpty()
                && userRepository.findByUsername("user").isEmpty()) {
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
            Category cat1 = createCategory("Natural Loofah", "Products made from raw natural loofah");
            Category cat2 = createCategory("Loofah Skincare", "Loofah products used in skincare and spa treatments");
            Category cat3 = createCategory("Loofah Kitchen", "Loofah-based cleaning tools for kitchens");
            Category cat4 = createCategory("Loofah Handicrafts", "Decorative handmade items from loofah");
            Category cat5 = createCategory("Loofah Gift Sets", "Gift sets made from loofah-based products");

            if (productRepository.count() == 0) {
                addProduct("Dried Natural Loofah", "Sun-dried natural loofah, chemical-free", new BigDecimal("10000"),
                        100,
                        "https://api.nongthonviet.com.vn/media/2022/04/08/624fb8e76c30a919cfde450b_5dd707843b261e9f3b18458dbb56d211_high.jpg",
                        cat1, true, 0.10);
                addProduct("Loofah Shower Pad", "Perfect for exfoliating during shower", new BigDecimal("15000"), 80,
                        "https://images2.thanhnien.vn/thumb_w/640/528068263637045248/2023/10/2/img4200-16962386086551668552859.jpg",
                        cat2, true, 0.05);
                addProduct("Loofah Kitchen Sponge", "Eco-friendly dish sponge made from loofah",
                        new BigDecimal("12000"), 150,
                        "https://laxanh.net/wp-content/uploads/2020/06/xm4.jpg", cat3, true, 0.08);
                addProduct("Loofah Bath Ball", "Soft bath ball for gentle skin cleansing", new BigDecimal("17000"), 60,
                        "http://bizweb.dktcdn.net/100/019/496/files/tinh-dau-ha-na-96a39adc-7c0b-4570-a506-8ddde9914151.jpg?v=1572256038636",
                        cat2, true, null);
                addProduct("Loofah Scrubber", "Multi-purpose loofah scrubber for kitchen or bathroom",
                        new BigDecimal("11000"), 70,
                        "https://images2.thanhnien.vn/528068263637045248/2023/10/2/img4192-1696238607422354143989.jpg",
                        cat3, true, 0.12);
                addProduct("Loofah Flower Craft", "Decorative handmade flower from loofah", new BigDecimal("25000"), 40,
                        "https://xomuopkb.vn/Uploads/EDE87413BFF92B82FF9F8CF1AD8FBB02/files/z5347516761624_8e5a83a826981febe94f68909708a485.jpg",
                        cat4, true, 0.15);
                addProduct("Loofah Wall Art", "Eco-friendly wall decoration made with loofah", new BigDecimal("30000"),
                        25,
                        "https://www.hieutruong.com/uploads/thu-vien/2023/11/03/image-20231103213654-4.jpeg", cat4,
                        true, null);
                addProduct("Mini Loofah Gift Set", "A small gift set of 3 loofah items", new BigDecimal("28000"), 90,
                        "http://static1.bestie.vn/Mlog/ImageContent/201707/bestie-cong-dung-cua-xo-muop-1-20170717133525.jpg",
                        cat5, true, 0.10);
                addProduct("Spa Loofah Gift Box", "Premium gift box with spa-grade loofah products",
                        new BigDecimal("45000"), 30,
                        "https://down-vn.img.susercontent.com/file/5fe738e51ba0f528fd68d344b4a63063", cat5, true, null);
                addProduct("Premium Loofah Sponge", "Long-lasting loofah sponge for daily bath use",
                        new BigDecimal("18000"), 100,
                        "https://changreenchoice.com/wp-content/uploads/2024/06/Natural-Bath-Loofah-Sponge-With-Custom-Design-In-Vietnam-1-e1718809115282.jpeg",
                        cat1, true, 0.07);

                System.out.println("Default products created.");
            } else {
                System.out.println("Products already exist.");
            }
        } else {
            System.out.println("Categories already exist.");
        }
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private void addProduct(String name, String description, BigDecimal price, int stock, String imageUrl,
            Category category, boolean isActive, Double discount) {
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

    private void createArticlesIfNotExist() {
        if (articlesRepository.count() == 0) {
            List<Article> articles = new ArrayList<>();

            articles.add(createArticle(
                    "The Amazing Benefits of Natural Loofah for Your Skin",
                    "Natural loofah is a fantastic exfoliating tool derived from the fibrous skeleton of the loofah plant. It helps remove dead skin cells gently, leaving your skin smooth and refreshed. Unlike synthetic sponges, natural loofahs are biodegradable and eco-friendly. Using a loofah regularly can stimulate blood circulation, improve skin texture, and prevent clogged pores. Its natural texture is perfect for a relaxing bath or shower routine that enhances your skin's health without harsh chemicals.",
                    1,
                    "Explore how natural loofah can improve your skincare routine by exfoliating and revitalizing your skin naturally.",
                    "https://suckhoedoisong.qltns.mediacdn.vn/324455921873985536/2022/4/25/trong-muop-huong-11-1650895342271997058777.jpg"));

            articles.add(createArticle(
                    "Natural Loofah in Kitchen Cleaning: Eco-Friendly and Effective",
                    "Natural loofah sponges are not just for skincare — they are also excellent for cleaning in the kitchen. Their coarse fibers make them perfect for scrubbing pots, pans, and dishes without scratching surfaces. Because they are biodegradable, natural loofah sponges reduce plastic waste compared to synthetic kitchen sponges. Moreover, they resist bacteria buildup better than traditional sponges, promoting a healthier kitchen environment. Incorporating loofah cleaning tools into your daily routine supports sustainability and effective hygiene.",
                    2,
                    "Discover the many ways natural loofah can be used in the kitchen as a sustainable and hygienic cleaning tool.",
                    "https://api.nongthonviet.com.vn/media/2022/04/08/624fb8e76c30a919cfde450b_5dd707843b261e9f3b18458dbb56d211_high.jpg"));

            articles.add(createArticle(
                    "Creative DIY Loofah Crafts to Beautify Your Home",
                    "Loofah is an incredibly versatile material for creative crafts and home decor. Its unique texture and natural appearance allow for beautiful handmade flowers, wall hangings, and decorative objects. Crafting with loofah is not only fun but also environmentally friendly, as it uses natural, renewable resources. Whether you want to create personalized gifts or add a rustic charm to your living space, loofah crafts provide endless possibilities. Dive into easy DIY projects that bring warmth and sustainability into your home.",
                    1,
                    "Learn how to make beautiful and eco-friendly handmade decorations using natural loofah with these easy DIY craft ideas.",
                    "https://images2.thanhnien.vn/thumb_w/640/528068263637045248/2023/10/2/img4200-16962386086551668552859.jpg"));

            articles.add(createArticle(
                    "How to Choose the Right Loofah for Your Skin Type",
                    "Choosing the right loofah can make a significant difference in your skincare routine. Natural loofahs vary in texture and coarseness, so it's important to select one suited to your skin type. For sensitive skin, softer, smaller loofahs are ideal, providing gentle exfoliation without irritation. For tougher or oily skin, coarser loofahs help remove excess oils and dead cells effectively. Regular use of a properly chosen loofah boosts skin renewal, promotes circulation, and leaves your skin glowing. Remember to replace your loofah regularly to maintain hygiene.",
                    2,
                    "Tips and guidelines to help you select the perfect natural loofah for your skin’s unique needs and enjoy the full benefits.",
                    "https://suckhoedoisong.qltns.mediacdn.vn/thumb_w/1200/324455921873985536/2021/8/1/muop-16277845641761514843377-0-0-312-500-crop-16277845755441950000950.jpg"));

            articles.add(createArticle(
                    "Loofah Gift Sets: Thoughtful, Eco-Friendly Presents for Any Occasion",
                    "Loofah-based gift sets make excellent presents for eco-conscious friends and family. These sets often include natural loofah sponges, bath accessories, and skincare products that promote relaxation and natural wellness. Giving a loofah gift set shows thoughtfulness towards both the recipient and the environment. Perfect for birthdays, holidays, or special events, these gift sets combine beauty, practicality, and sustainability. Explore various loofah gift collections designed to pamper and delight your loved ones while supporting green living.",
                    1,
                    "Explore the beauty and benefits of loofah gift sets, perfect for those who value natural, eco-friendly, and practical gifts.",
                    "https://trungtamthuoc.com/images/others/xo-muop-6-6562.jpg"));

            articlesRepository.saveAll(articles);
            System.out.println("Default articles created.");
        } else {
            System.out.println("Articles already exist.");
        }
    }

    private Article createArticle(String title, String content, Integer userId, String description, String imageUrl) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setUserId(userId);
        article.setCreatedAt(LocalDateTime.now());
        article.setDescription(description);
        article.setImageUrl(imageUrl);
        return article;
    }
}
