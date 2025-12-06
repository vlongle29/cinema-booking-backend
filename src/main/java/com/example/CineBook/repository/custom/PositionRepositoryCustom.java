package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.position.PositionSearchDTO;
import com.example.CineBook.model.Position;
import org.springframework.data.jpa.domain.Specification;

public interface PositionRepositoryCustom {
    Specification<Position> searchWithFilters(PositionSearchDTO searchDTO);
}
