package com.example.CineBook.dto.seattemplate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateSeatTemplateRequest {
    
    @NotBlank(message = "Tên template không được để trống")
    private String name;
    
    private String description;
    
    @NotNull(message = "Số hàng không được để trống")
    @Min(value = 1, message = "Số hàng phải lớn hơn 0")
    @Max(value = 26, message = "Số hàng không được vượt quá 26 (A-Z)")
    private Integer rows;
    
    @NotNull(message = "Số cột không được để trống")
    @Min(value = 1, message = "Số cột phải lớn hơn 0")
    @Max(value = 50, message = "Số cột không được vượt quá 50")
    private Integer columns;
}
