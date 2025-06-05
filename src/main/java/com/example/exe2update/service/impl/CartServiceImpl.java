package com.example.exe2update.service.impl;

import com.example.exe2update.entity.Cart;
import com.example.exe2update.entity.Product;
import com.example.exe2update.entity.User;
import com.example.exe2update.repository.CartRepository;
import com.example.exe2update.repository.ProductRepository;
import com.example.exe2update.service.CartService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    @Override
    public void addToCart(User user, Integer productId, Integer quantity) {
        // Tìm sản phẩm trong cơ sở dữ liệu
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra nếu sản phẩm đã tồn tại trong giỏ hàng của người dùng
        Cart existingCartItem = cartRepository.findByUserAndProduct(user, product);
        if (existingCartItem != null) {
            // Nếu sản phẩm đã có trong giỏ hàng, chỉ cần cập nhật số lượng
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);

            // Gọi updateFromProduct để cập nhật các trường khác như productName,
            // productPrice, productImage
            existingCartItem.updateFromProduct(product);
            existingCartItem.setDiscount(product.getDiscount());
            // Lưu lại giỏ hàng đã cập nhật
            cartRepository.save(existingCartItem);
        } else {
            // Nếu giỏ hàng chưa có sản phẩm, tạo mới giỏ hàng
            Cart newCartItem = new Cart();
            newCartItem.setUser(user);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setDiscount(product.getDiscount());
            // Gọi phương thức updateFromProduct để gán các giá trị của sản phẩm vào giỏ
            // hàng
            newCartItem.updateFromProduct(product);

            // Lưu giỏ hàng mới vào cơ sở dữ liệu
            cartRepository.save(newCartItem);
        }
    }

    @Override
    public boolean updateQuantity(Integer cartId, Integer quantity) {
        Optional<Cart> optional = cartRepository.findById(cartId);
        if (optional.isEmpty())
            return false;

        Cart cart = optional.get();

        // Lấy sản phẩm trong giỏ hàng để kiểm tra số lượng tối đa
        Product product = cart.getProduct(); // Giả sử bạn có mối quan hệ giữa Cart và Product
        int availableStock = product.getStock(); // Số lượng sản phẩm trong kho

        // Kiểm tra số lượng sản phẩm có vượt quá số lượng còn lại trong kho không
        if (quantity > availableStock) {
            return false; // Nếu số lượng vượt quá, trả về false
        }

        cart.setQuantity(quantity);
        cartRepository.save(cart);
        return true;
    }

    @Override
    @Transactional
    public void removeFromCart(Integer cartId) {
        cartRepository.deleteById(cartId);
    }

    @Override
    public double getProductPrice(Integer cartId) {
        Optional<Cart> optional = cartRepository.findById(cartId);
        if (optional.isPresent()) {
            Cart cart = optional.get();
            Product product = cart.getProduct();

            BigDecimal price = product.getPrice();

            Double discountValue = product.getDiscount(); // discount kiểu Double hoặc double
            if (discountValue != null && discountValue > 0) {
                BigDecimal discount = BigDecimal.valueOf(discountValue);
                BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100)));
                price = price.multiply(discountMultiplier);
            }

            return price.multiply(BigDecimal.valueOf(cart.getQuantity())).doubleValue();
        }
        return 0;
    }

    @Override
    public double calculateTotalByUser(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        return cartItems.stream()
                .map(cart -> {
                    Product product = cart.getProduct();
                    BigDecimal price = product.getPrice();

                    Double discountValue = product.getDiscount();
                    if (discountValue != null && discountValue > 0) {
                        // Không chia cho 100 vì discount đã là dạng 0.15 (tức 15%) rồi
                        BigDecimal discount = BigDecimal.valueOf(discountValue);
                        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discount);
                        price = price.multiply(discountMultiplier);
                    }

                    return price.multiply(BigDecimal.valueOf(cart.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }
}
