package com.example.CineBook.dto.product;

import com.example.CineBook.common.constant.ProductCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    
    private String description;
    
    @NotNull(message = "Giá không được để trống")
    private BigDecimal price;
    
    private String imageUrl;
    private ProductCategory category;
    private Boolean isActive;
}
