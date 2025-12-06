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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final GenreMapper genreMapper;

    @Override
    @Transactional
    public GenreResponse createGenre(CreateGenreRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new BusinessException(MessageCode.GENRE_NAME_ALREADY_EXISTS);
        }

        Genre genre = genreMapper.toEntity(request);
        Genre saved = genreRepository.save(genre);
        return genreMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll().stream()
                .filter(genre -> !Boolean.TRUE.equals(genre.getIsDelete()))
                .map(genreMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GenreResponse> searchGenres(GenreSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage() - 1, searchDTO.getSize());
        Page<Genre> entityPage = genreRepository.searchWithFilters(searchDTO, pageable);
        Page<GenreResponse> responsePage = entityPage.map(genreMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.GENRE_NOT_FOUND));
        
        if (Boolean.TRUE.equals(genre.getIsDelete())) {
            throw new BusinessException(MessageCode.GENRE_NOT_FOUND);
        }
        
        return genreMapper.toResponse(genre);
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

        genreRepository.softDeleteById(id);
    }
}
