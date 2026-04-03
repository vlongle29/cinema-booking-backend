package com.example.CineBook.dto.seattemplate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplateDetailRequest {
    
    @NotBlank(message = "Số ghế không được để trống")
    private String seatNumber; // e.g., A1, B2
    
    @NotNull(message = "Loại ghế không được để trống")
    private UUID seatTypeId;
    
    private Boolean isAisle = false;
}
