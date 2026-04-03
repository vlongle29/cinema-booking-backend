package com.example.CineBook.dto.seattemplate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSeatsToTemplateRequest {
    
    @NotEmpty(message = "Danh sách ghế không được để trống")
    @Valid
    private List<SeatTemplateDetailRequest> seats;
}
