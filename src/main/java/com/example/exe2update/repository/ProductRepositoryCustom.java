package com.example.exe2update.repository;

import com.example.exe2update.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductRepositoryCustom {
    Page<Product> findFiltered(int page, int size,
                               List<Integer> categoryIds, List<String> discountRanges, String searchKeyword);
}
