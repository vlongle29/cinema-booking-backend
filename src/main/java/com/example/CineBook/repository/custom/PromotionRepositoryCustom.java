package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.promotion.PromotionSearchDTO;
import com.example.CineBook.model.Promotion;
import org.springframework.data.domain.Page;

public interface PromotionRepositoryCustom {
    Page<Promotion> searchPromotions(PromotionSearchDTO searchDTO);
}
