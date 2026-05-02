package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.booking.BookingSearchDTO;
import com.example.CineBook.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BookingRepositoryCustom {
    Page<Booking> searchWithFilters(BookingSearchDTO searchDTO, Pageable pageable);
}
