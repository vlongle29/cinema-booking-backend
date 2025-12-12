package com.example.CineBook.repository.irepository;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.repository.custom.ShowtimeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID>, JpaSpecificationExecutor<Showtime>, ShowtimeRepositoryCustom {
    
    @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId " +
           "AND s.isDelete = false " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Showtime> findOverlappingShowtimes(
        @Param("roomId") UUID roomId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId " +
           "AND s.id != :showtimeId " +
           "AND s.isDelete = false " +
           "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<Showtime> findOverlappingShowtimesExcludingCurrent(
        @Param("roomId") UUID roomId,
        @Param("showtimeId") UUID showtimeId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT DISTINCT CAST(s.startTime AS LocalDate) FROM Showtime s " +
           "WHERE s.movieId = :movieId AND s.isDelete = false " +
           "AND CAST(s.startTime AS LocalDate) >= :fromDate " +
           "ORDER BY CAST(s.startTime AS LocalDate)")
    List<LocalDate> findAvailableDatesByMovieId(@Param("movieId") UUID movieId, @Param("fromDate") LocalDate fromDate);
    
    @Query("SELECT DISTINCT s.format FROM Showtime s " +
           "WHERE s.movieId = :movieId " +
           "AND s.branchId IN (SELECT b.id FROM Branch b WHERE b.cityId = :cityId) " +
           "AND CAST(s.startTime AS LocalDate) = :date AND s.isDelete = false")
    List<MovieFormat> findAvailableFormatsByMovieAndCityAndDate(
        @Param("movieId") UUID movieId,
        @Param("cityId") UUID cityId,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT s FROM Showtime s WHERE s.roomId = :roomId " +
           "AND CAST(s.startTime AS LocalDate) >= :startDate " +
           "AND CAST(s.startTime AS LocalDate) <= :endDate " +
           "AND s.isDelete = false " +
           "ORDER BY s.startTime")
    List<Showtime> findByRoomAndDateRange(
        @Param("roomId") UUID roomId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
