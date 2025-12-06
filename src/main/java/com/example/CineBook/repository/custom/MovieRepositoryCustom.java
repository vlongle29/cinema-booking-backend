package com.example.CineBook.repository.custom;

import com.example.CineBook.dto.movie.MovieSearchDTO;
import com.example.CineBook.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MovieRepositoryCustom {
    Page<Movie> searchWithFilters(MovieSearchDTO searchDTO, Pageable pageable);
}
