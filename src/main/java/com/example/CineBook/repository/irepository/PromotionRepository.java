package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Promotion;
import com.example.CineBook.repository.custom.PromotionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, UUID>, PromotionRepositoryCustom {
    Optional<Promotion> findByCodeAndIsDeleteFalse(String code);
    boolean existsByCodeAndIsDeleteFalse(String code);
    boolean existsByCodeAndIdNotAndIsDeleteFalse(String code, UUID id);
}
