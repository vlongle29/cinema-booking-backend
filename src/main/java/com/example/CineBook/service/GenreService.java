package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.genre.CreateGenreRequest;
import com.example.CineBook.dto.genre.GenreResponse;
import com.example.CineBook.dto.genre.GenreSearchDTO;
import com.example.CineBook.dto.genre.UpdateGenreRequest;

import java.util.List;
import java.util.UUID;

public interface GenreService {
    GenreResponse createGenre(CreateGenreRequest request);
    List<GenreResponse> getAllGenres();
    PageResponse<GenreResponse> searchGenres(GenreSearchDTO searchDTO);
    GenreResponse getGenreById(UUID id);
    GenreResponse updateGenre(UUID id, UpdateGenreRequest request);
    void deleteGenre(UUID id);
}
