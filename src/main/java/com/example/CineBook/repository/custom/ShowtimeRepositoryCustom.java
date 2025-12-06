package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.model.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ShowtimeRepositoryCustom {
    Specification<Showtime> searchWithFilters(ShowtimeSearchDTO searchDTO);
}
