package com.example.CineBook.dto.product;

import com.example.CineBook.common.constant.ProductCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private ProductCategory category;
    private Boolean isActive;
}
