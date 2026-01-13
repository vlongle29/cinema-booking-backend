package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.ProductCategory;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.product.ProductCreateRequest;
import com.example.CineBook.dto.product.ProductResponse;
import com.example.CineBook.dto.product.ProductUpdateRequest;
import com.example.CineBook.mapper.ProductMapper;
import com.example.CineBook.model.Product;
import com.example.CineBook.repository.irepository.ProductRepository;
import com.example.CineBook.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
            Product product = productMapper.toEntity(request);
            product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(ProductCategory category) {
        return productRepository.findByCategory(category).stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.PRODUCT_NOT_FOUND));

        productMapper.updateEntity(request, product);

        Product updated = productRepository.save(product);
        return productMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException(MessageCode.PRODUCT_NOT_FOUND);
        }
        productRepository.deleteById(id);
    }
}
