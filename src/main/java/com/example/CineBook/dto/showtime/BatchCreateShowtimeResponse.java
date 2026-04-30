package com.example.CineBook.dto.showtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateShowtimeResponse {
    private List<CreatedShowtimeItem> created = new ArrayList<>();
    private List<RejectedShowtimeItem> rejected = new ArrayList<>();
}
