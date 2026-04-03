package com.example.CineBook.dto.seattemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplateResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer totalSeats;
    private Integer rows;
    private Integer columns;
    private Instant createTime;
    private Instant updateTime;
    private List<SeatTemplateDetailResponse> seats;
}
