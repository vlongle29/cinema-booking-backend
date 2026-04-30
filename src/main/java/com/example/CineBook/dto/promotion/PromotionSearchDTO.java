package com.example.CineBook.dto.promotion;

import com.example.CineBook.common.constant.DiscountType;
import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PromotionSearchDTO extends SearchBaseDto {
    private String code;
    private String name;
    private DiscountType discountType;
    private Boolean active;
}
