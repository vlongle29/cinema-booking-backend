package com.example.CineBook.dto.product;

import com.example.CineBook.common.constant.ProductCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private ProductCategory category;
    private Boolean isActive;
}
