package com.example.CineBook.dto.showtime;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeGroupedByBranchResponse {
    private UUID branchId;
    private String branchName;
    private List<ShowtimeItemResponse> showtimes;
}
