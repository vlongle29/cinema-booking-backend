package com.example.CineBook.mapper;

import com.example.CineBook.dto.product.ProductCreateRequest;
import com.example.CineBook.dto.product.ProductResponse;
import com.example.CineBook.dto.product.ProductUpdateRequest;
import com.example.CineBook.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper extends BaseMapper<ProductCreateRequest, Product> {
    Product toEntity(ProductCreateRequest dto);

    void updateEntity(ProductUpdateRequest dto, @MappingTarget Product product);

    ProductResponse toResponse(Product product);
}
