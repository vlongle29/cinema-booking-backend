package com.example.CineBook.controller;

import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.genre.CreateGenreRequest;
import com.example.CineBook.dto.genre.GenreResponse;
import com.example.CineBook.dto.genre.GenreSearchDTO;
import com.example.CineBook.dto.genre.UpdateGenreRequest;
import com.example.CineBook.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Genre Management", description = "APIs quản lý thể loại phim")
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Tạo thể loại mới")
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody CreateGenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.createGenre(request)));
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả thể loại")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        return ResponseEntity.ok(ApiResponse.success(genreService.getAllGenres()));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm thể loại")
    public ResponseEntity<ApiResponse<PageResponse<GenreResponse>>> searchGenres(@ModelAttribute GenreSearchDTO searchDTO) {
        return ResponseEntity.ok(ApiResponse.success(genreService.searchGenres(searchDTO)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết thể loại")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(genreService.getGenreById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Cập nhật thể loại")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGenreRequest request) {
        return ResponseEntity.ok(ApiResponse.success(genreService.updateGenre(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @Operation(summary = "Xóa thể loại")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
