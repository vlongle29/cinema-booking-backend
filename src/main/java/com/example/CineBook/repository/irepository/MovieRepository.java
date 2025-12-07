package com.example.CineBook.repository.irepository;

import com.example.CineBook.model.Movie;
import com.example.CineBook.repository.custom.MovieRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID>, MovieRepositoryCustom {
    
    @Modifying
    @Query("UPDATE Movie m SET m.isDelete = true, m.deleteTime = CURRENT_TIMESTAMP WHERE m.id = :id")
    void softDeleteById(@Param("id") UUID id);

    boolean existsByTitle(String title);
}
