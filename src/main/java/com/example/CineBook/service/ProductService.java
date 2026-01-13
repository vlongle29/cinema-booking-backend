package com.example.CineBook.service;

import com.example.CineBook.common.constant.ProductCategory;
import com.example.CineBook.dto.product.ProductCreateRequest;
import com.example.CineBook.dto.product.ProductResponse;
import com.example.CineBook.dto.product.ProductUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getProductsByCategory(ProductCategory category);
    ProductResponse updateProduct(UUID id, ProductUpdateRequest request);
    void deleteProduct(UUID id);
}
