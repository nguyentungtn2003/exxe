package com.example.exe2update.repository;

import com.example.exe2update.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeRepository extends JpaRepository<Product,Long> {
}
