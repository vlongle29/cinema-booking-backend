package com.example.CineBook.controller;

import com.example.CineBook.common.constant.AgeRating;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.movie.*;
import com.example.CineBook.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Tag(name = "Movie Management", description = "APIs quản lý phim")
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo phim mới")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.ok(ApiResponse.success(movieService.createMovie(request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm phim")
    public ResponseEntity<ApiResponse<PageResponse<MovieResponse>>> searchMovies(@ModelAttribute MovieSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(movieService.searchMovies(searchDTO)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết phim")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getMovieById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật phim")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMovieRequest request) {
        return ResponseEntity.ok(ApiResponse.success(movieService.updateMovie(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa phim")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/age-ratings")
    @Operation(summary = "Lấy danh sách phân loại độ tuổi")
    public ResponseEntity<ApiResponse<List<AgeRatingDTO>>> getAgeRatings() {
        List<AgeRatingDTO> ratings = Arrays.stream(AgeRating.values()).map(rating -> new AgeRatingDTO(
                rating.getValue(),
                rating.getDescription()
        )).toList();
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }

    @GetMapping("/now-showing")
    @Operation(summary = "Lấy danh sách phim đang chiếu (cached)")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getNowShowing() {
        return ResponseEntity.ok(ApiResponse.success(movieService.getNowShowingMovies()));
    }

    @GetMapping("/coming-soon")
    @Operation(summary = "Lấy danh sách phim sắp chiếu (cached)")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getComingSoon() {
        return ResponseEntity.ok(ApiResponse.success(movieService.getComingSoonMovies()));
    }

}
