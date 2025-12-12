package com.example.CineBook.service;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.city.CityResponse;
import com.example.CineBook.dto.showtime.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShowtimeService {
    ShowtimeResponse createShowtime(CreateShowtimeRequest request);
    PageResponse<ShowtimeResponse> searchShowtimes(ShowtimeSearchDTO searchDTO);
    ShowtimeResponse getShowtimeById(UUID id);
    ShowtimeResponse updateShowtime(UUID id, UpdateShowtimeRequest request);
    void deleteShowtime(UUID id);
    
    List<LocalDate> getAvailableDates(UUID movieId);
    List<CityResponse> getAvailableCities(UUID movieId, LocalDate date);
    List<MovieFormat> getAvailableFormats(UUID movieId, LocalDate date, UUID cityId);
    List<ShowtimeGroupedByBranchResponse> getShowtimesGroupedByBranch(UUID movieId, LocalDate date, UUID cityId, MovieFormat format);
    
    // Bulk create
    RoomShowtimeResponse getShowtimesByRoom(UUID roomId, LocalDate startDate, LocalDate endDate);
    BulkCreateShowtimeResponse bulkCreateShowtimes(BulkCreateShowtimeRequest request);
}
