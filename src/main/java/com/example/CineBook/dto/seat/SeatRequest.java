package com.example.CineBook.dto.seat;

import com.example.CineBook.common.constant.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatRequest {
    
    @NotBlank(message = "Số ghế không được để trống")
    private String seatNumber;
    
    @NotNull(message = "Loại ghế không được để trống")
    private SeatType type;
}
