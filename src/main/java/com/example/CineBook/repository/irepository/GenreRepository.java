package com.example.CineBook.repository.irepository;

import com.example.CineBook.dto.genre.GenreSearchDTO;
import com.example.CineBook.model.Genre;
import com.example.CineBook.repository.base.BaseRepositoryCustom;
import com.example.CineBook.repository.custom.GenreRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GenreRepository extends BaseRepositoryCustom<Genre, GenreSearchDTO>, JpaRepository<Genre, UUID>, GenreRepositoryCustom{
    boolean existsByName(String name);
    Optional<Genre> findByName(String name);
}
