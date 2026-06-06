package com.example.CineBook.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingCountDTO {
    private Integer stars;  // Mức sao (1, 2, 3, 4, 5)
    private Long count;     // Số lượng đánh giá
}
