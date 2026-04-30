package com.example.CineBook.controller;

import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.showtime.FormatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Movie Format Management", description = "APIs quản lý định dạng phim")
@RestController
@RequestMapping("/api/movie-formats")
@RequiredArgsConstructor
public class MovieFormatController {
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả định dạng phim")
    public ResponseEntity<ApiResponse<List<FormatsResponse>>> getAllMovieFormats() {
        List<FormatsResponse> formats = Arrays.stream(MovieFormat.values())
                .map(format -> FormatsResponse.builder()
                        .format(format)
                        .displayName(format.getDisplayName())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(formats));
    }
}
