package com.example.CineBook.dto.seattemplate;

import com.example.CineBook.dto.seattype.SeatTypeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplateDetailResponse {
    private UUID id;
    private String seatNumber;
    private String rowChar;
    private Integer rowIndex;
    private Integer columnIndex;
    private Integer seatNum;
    private UUID seatTypeId;
    private SeatTypeResponse seatType;
    private Boolean isAisle;
}
