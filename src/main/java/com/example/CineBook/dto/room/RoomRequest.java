package com.example.CineBook.dto.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    
    @NotBlank(message = "Tên phòng không được để trống")
    private String name;
    
    @NotNull(message = "Số ghế không được để trống")
    @Positive(message = "Số ghế phải lớn hơn 0")
    private Integer totalSeats;
    
    @NotNull(message = "Chi nhánh không được để trống")
    private UUID branchId;
}
