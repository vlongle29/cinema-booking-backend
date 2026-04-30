package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.promotion.PromotionRequest;
import com.example.CineBook.dto.promotion.PromotionResponse;
import com.example.CineBook.dto.promotion.PromotionSearchDTO;

import java.util.UUID;

public interface PromotionService {
    PromotionResponse create(PromotionRequest request);
    PromotionResponse update(UUID id, PromotionRequest request);
    PromotionResponse getById(UUID id);
    PageResponse<PromotionResponse> search(PromotionSearchDTO searchDTO);
    void delete(UUID id);
}
