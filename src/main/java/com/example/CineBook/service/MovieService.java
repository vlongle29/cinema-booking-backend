package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.movie.CreateMovieRequest;
import com.example.CineBook.dto.movie.MovieResponse;
import com.example.CineBook.dto.movie.MovieSearchDTO;
import com.example.CineBook.dto.movie.UpdateMovieRequest;

import java.util.List;
import java.util.UUID;

public interface MovieService {
    MovieResponse createMovie(CreateMovieRequest request);
    PageResponse<MovieResponse> searchMovies(MovieSearchDTO searchDTO);
    MovieResponse getMovieById(UUID id);
    MovieResponse updateMovie(UUID id, UpdateMovieRequest request);
    void deleteMovie(UUID id);
    List<MovieResponse> getNowShowingMovies();
    List<MovieResponse> getComingSoonMovies();
}
