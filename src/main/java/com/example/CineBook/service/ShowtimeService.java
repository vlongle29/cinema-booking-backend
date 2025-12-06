package com.example.CineBook.service;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.dto.showtime.CreateShowtimeRequest;
import com.example.CineBook.dto.showtime.ShowtimeResponse;
import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.dto.showtime.UpdateShowtimeRequest;

import java.util.UUID;

public interface ShowtimeService {
    ShowtimeResponse createShowtime(CreateShowtimeRequest request);
    PageResponse<ShowtimeResponse> searchShowtimes(ShowtimeSearchDTO searchDTO);
    ShowtimeResponse getShowtimeById(UUID id);
    ShowtimeResponse updateShowtime(UUID id, UpdateShowtimeRequest request);
    void deleteShowtime(UUID id);
}
