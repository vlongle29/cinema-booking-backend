package com.example.CineBook.repository.irepository;

import com.example.CineBook.common.constant.ProductCategory;
import com.example.CineBook.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategory(ProductCategory category);
}
