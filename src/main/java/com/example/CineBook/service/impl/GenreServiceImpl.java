package com.example.CineBook.service.impl;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.genre.CreateGenreRequest;
import com.example.CineBook.dto.genre.GenreResponse;
import com.example.CineBook.dto.genre.GenreSearchDTO;
import com.example.CineBook.dto.genre.UpdateGenreRequest;
import com.example.CineBook.mapper.GenreMapper;
import com.example.CineBook.model.Genre;
import com.example.CineBook.repository.irepository.GenreRepository;
import com.example.CineBook.repository.irepository.MovieGenreRepository;
import com.example.CineBook.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final GenreMapper genreMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private final static String GENRE_CACHE_KEY = "genreCache:";
    private final static String ALL_GENRES_KEY = "genres:all";
    private final static String SEARCH_CACHE_KEY = "genres:search:";
    private final static Duration CACHE_TTL = Duration.ofHours(24);

    @Override
    @Transactional
    public GenreResponse createGenre(CreateGenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.GENRE_NAME_ALREADY_EXISTS);
        }

        Genre genre = genreMapper.toEntity(request);
        Genre saved = genreRepository.save(genre);

        redisTemplate.delete(ALL_GENRES_KEY);
        clearSearchCache();

        return genreMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {

        @SuppressWarnings("unchecked")
        List<GenreResponse> cached = (List<GenreResponse>) redisTemplate.opsForValue().get(ALL_GENRES_KEY);
        if (cached != null) {
            return cached;
        }

        List<GenreResponse> genres = genreRepository.findAll().stream()
                .filter(genre -> !Boolean.TRUE.equals(genre.getIsDelete()))
                .map(genreMapper::toResponse)
                .collect(Collectors.toList());

        if (!genres.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_GENRES_KEY, genres, CACHE_TTL);
        }

        return genres;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> searchGenres(GenreSearchDTO searchDTO) {
        String cacheKey = SEARCH_CACHE_KEY + searchDTO.hashCode();
        @SuppressWarnings("unchecked")
        PageResponse<GenreResponse> cached = (PageResponse<GenreResponse>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Genre> entityPage = genreRepository.searchWithFilters(searchDTO, pageable);
        Page<GenreResponse> responsePage = entityPage.map(genreMapper::toResponse);
        PageResponse<GenreResponse> result = PageResponse.of(responsePage);
        
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(UUID id) {

        @SuppressWarnings("unchecked")
        GenreResponse cached = (GenreResponse) redisTemplate.opsForValue().get(GENRE_CACHE_KEY + id);
        if (cached != null) {
            return cached;
        }

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.GENRE_NOT_FOUND));
        
        if (Boolean.TRUE.equals(genre.getIsDelete())) {
            throw new BusinessException(MessageCode.GENRE_NOT_FOUND);
        }

        GenreResponse response = genreMapper.toResponse(genre);
        redisTemplate.opsForValue().set(GENRE_CACHE_KEY + id, response, CACHE_TTL);
        
        return response;
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(UUID id, UpdateGenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.GENRE_NOT_FOUND));

        if (request.getName() != null && !request.getName().equals(genre.getName())) {
            if (genreRepository.existsByName(request.getName())) {
                throw new BusinessException(MessageCode.GENRE_NAME_ALREADY_EXISTS);
            }
        }

        genreMapper.updateEntityFromDto(request, genre);
        Genre updated = genreRepository.save(genre);

        redisTemplate.delete(GENRE_CACHE_KEY + id);
        redisTemplate.delete(ALL_GENRES_KEY);
        clearSearchCache();

        return genreMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteGenre(UUID id) {
        if (!genreRepository.existsById(id)) {
            throw new BusinessException(MessageCode.GENRE_NOT_FOUND);
        }

        long movieCount = movieGenreRepository.countByGenreId(id);
        if (movieCount > 0) {
            throw new BusinessException(MessageCode.GENRE_IN_USE);
        }

        redisTemplate.delete(GENRE_CACHE_KEY + id);
        redisTemplate.delete(ALL_GENRES_KEY);
        clearSearchCache();

        genreRepository.softDeleteById(id);
    }

    private void clearSearchCache() {
        redisTemplate.keys(SEARCH_CACHE_KEY + "*").forEach(redisTemplate::delete);
    }
}
