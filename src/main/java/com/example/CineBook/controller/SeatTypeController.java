package com.example.CineBook.controller;

import com.example.CineBook.common.response.ApiResponse;
import com.example.CineBook.dto.seattype.SeatTypeRequest;
import com.example.CineBook.dto.seattype.SeatTypeResponse;
import com.example.CineBook.service.SeatTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat-types")
@RequiredArgsConstructor
@Tag(name = "Seat Type", description = "Seat Type Management APIs")
public class SeatTypeController {
    
    private final SeatTypeService seatTypeService;
    
    @PostMapping
    @Operation(summary = "Create new seat type")
    public ResponseEntity<ApiResponse<SeatTypeResponse>> create(@Valid @RequestBody SeatTypeRequest request) {
        seatTypeService.create(request);
        return ResponseEntity.ok(ApiResponse.success(seatTypeService.create(request)));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update seat type")
    public ResponseEntity<ApiResponse<SeatTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SeatTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(seatTypeService.update(id, request)));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get seat type by ID")
    public ResponseEntity<ApiResponse<SeatTypeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(seatTypeService.getById(id)));
    }
    
    @GetMapping
    @Operation(summary = "Get all seat types")
    public ResponseEntity<ApiResponse<List<SeatTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(seatTypeService.getAll()));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete seat type")
    public ResponseEntity<ApiResponse<Void>>  delete(@PathVariable UUID id) {
        seatTypeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
