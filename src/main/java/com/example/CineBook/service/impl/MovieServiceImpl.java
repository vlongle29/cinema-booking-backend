package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.MovieStatus;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.genre.GenreResponse;
import com.example.CineBook.dto.movie.CreateMovieRequest;
import com.example.CineBook.dto.movie.MovieResponse;
import com.example.CineBook.dto.movie.MovieSearchDTO;
import com.example.CineBook.dto.movie.UpdateMovieRequest;
import com.example.CineBook.mapper.GenreMapper;
import com.example.CineBook.mapper.MovieMapper;
import com.example.CineBook.model.Genre;
import com.example.CineBook.model.Movie;
import com.example.CineBook.model.MovieGenre;
import com.example.CineBook.repository.irepository.GenreRepository;
import com.example.CineBook.repository.irepository.MovieGenreRepository;
import com.example.CineBook.repository.irepository.MovieRepository;
import com.example.CineBook.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieMapper movieMapper;
    private final GenreMapper genreMapper;

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        // Validate movie title uniqueness
        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new BusinessException(MessageCode.MOVIE_ALREADY_EXISTS);
        }

        // Validate all genreIds exist
        for (UUID genreId : request.getGenreIds()) {
            if (!genreRepository.existsById(genreId)) {
                throw new BusinessException(MessageCode.GENRE_NOT_FOUND);
            }
        }

        // Create movie
        Movie movie = movieMapper.toEntity(request);
        movie.setStatus(MovieStatus.valueOf(request.getStatus()));
        Movie saved = movieRepository.save(movie);

        // Create movie-genre mappings
        List<MovieGenre> movieGenres = request.getGenreIds().stream()
                .map(genreId -> MovieGenre.builder()
                        .movieId(saved.getId())
                        .genreId(genreId)
                        .build())
                .collect(Collectors.toList());
        movieGenreRepository.saveAll(movieGenres);

        return buildMovieResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MovieResponse> searchMovies(MovieSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Movie> entityPage = movieRepository.searchWithFilters(searchDTO, pageable);
        Page<MovieResponse> responsePage = entityPage.map(this::buildMovieResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));

        if (Boolean.TRUE.equals(movie.getIsDelete())) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }

        return buildMovieResponse(movie);
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(UUID id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));

        // Validate genreIds if provided
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            for (UUID genreId : request.getGenreIds()) {
                if (!genreRepository.existsById(genreId)) {
                    throw new BusinessException(MessageCode.GENRE_NOT_FOUND);
                }
            }

            // Delete old mappings and create new ones
            movieGenreRepository.deleteByMovieId(id);
            List<MovieGenre> movieGenres = request.getGenreIds().stream()
                    .map(genreId -> MovieGenre.builder()
                            .movieId(id)
                            .genreId(genreId)
                            .build())
                    .collect(Collectors.toList());
            movieGenreRepository.saveAll(movieGenres);
        }

        // Update movie fields
        movieMapper.updateEntityFromDto(request, movie);
        if (request.getStatus() != null) {
            movie.setStatus(MovieStatus.valueOf(request.getStatus()));
        }
        Movie updated = movieRepository.save(movie);

        return buildMovieResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMovie(UUID id) {
        if (!movieRepository.existsById(id)) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }

        // Delete movie-genre mappings
        movieGenreRepository.deleteByMovieId(id);

        // Soft delete movie
        movieRepository.softDeleteById(id);
    }

    private MovieResponse buildMovieResponse(Movie movie) {
        MovieResponse response = movieMapper.toResponse(movie);
        
        // Get genres for this movie
        List<UUID> genreIds = movieGenreRepository.findGenreIdsByMovieId(movie.getId());
        List<GenreResponse> genres = genreIds.stream()
                .map(genreId -> genreRepository.findById(genreId).orElse(null))
                .filter(genre -> genre != null && !Boolean.TRUE.equals(genre.getIsDelete()))
                .map(genreMapper::toResponse)
                .collect(Collectors.toList());
        
        response.setGenres(genres);
        response.setStatus(movie.getStatus() != null ? movie.getStatus().name() : null);
        
        return response;
    }
}
