package com.example.CineBook.dto.seattemplate;

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
public class ApplyTemplateRequest {
    
    @NotNull(message = "Template ID không được để trống")
    private UUID templateId;
    
    @NotNull(message = "Room ID không được để trống")
    private UUID roomId;
}
