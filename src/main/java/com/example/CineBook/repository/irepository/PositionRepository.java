package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Position;
import com.example.CineBook.repository.custom.PositionRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID>, JpaSpecificationExecutor<Position>, PositionRepositoryCustom {
    Optional<Position> findByCode(String code);
    boolean existsByCode(String code);
}
