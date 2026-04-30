package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.promotion.PromotionRequest;
import com.example.CineBook.dto.promotion.PromotionResponse;
import com.example.CineBook.dto.promotion.PromotionSearchDTO;
import com.example.CineBook.mapper.PromotionMapper;
import com.example.CineBook.model.Promotion;
import com.example.CineBook.repository.irepository.PromotionRepository;
import com.example.CineBook.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    @CacheEvict(value = "promotions", allEntries = true)
    @Override
    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        if (promotionRepository.existsByCodeAndIsDeleteFalse(request.getCode())) {
            throw new BusinessException(MessageCode.PROMOTION_CODE_ALREADY_EXISTS);
        }

        Promotion promotion = promotionMapper.toEntity(request);
        Promotion saved = promotionRepository.save(promotion);
        return promotionMapper.toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "promotions", key = "#id"),
            @CacheEvict(value = "promotions", allEntries = true)
    })
    @Override
    @Transactional
    public PromotionResponse update(UUID id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDelete()))
                .orElseThrow(() -> new BusinessException(MessageCode.PROMOTION_NOT_FOUND));

        if (!promotion.getCode().equals(request.getCode()) &&
                promotionRepository.existsByCodeAndIdNotAndIsDeleteFalse(request.getCode(), id)) {
            throw new BusinessException(MessageCode.PROMOTION_CODE_ALREADY_EXISTS);
        }

        promotionMapper.updateEntityFromDto(request, promotion);
        Promotion updated = promotionRepository.save(promotion);
        return promotionMapper.toResponse(updated);
    }

    @Cacheable(value = "promotions", key = "#id")
    @Override
    @Transactional(readOnly = true)
    public PromotionResponse getById(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDelete()))
                .orElseThrow(() -> new BusinessException(MessageCode.PROMOTION_NOT_FOUND));
        return promotionMapper.toResponse(promotion);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromotionResponse> search(PromotionSearchDTO searchDTO) {
        Page<Promotion> page = promotionRepository.searchPromotions(searchDTO);
        Page<PromotionResponse> responsePage = page.map(promotionMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Caching(evict = {
            @CacheEvict(value = "promotions", key = "#id"),
            @CacheEvict(value = "promotions", allEntries = true)
    })
    @Override
    @Transactional
    public void delete(UUID id) {
        Promotion promotion = promotionRepository.findById(id)
                .filter(p -> !Boolean.TRUE.equals(p.getIsDelete()))
                .orElseThrow(() -> new BusinessException(MessageCode.PROMOTION_NOT_FOUND));
        
        promotion.setDeleted(true);
        promotionRepository.save(promotion);
    }
}
