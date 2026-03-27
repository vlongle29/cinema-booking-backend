package com.example.CineBook.dto.showtime;

import com.example.CineBook.common.constant.MovieFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormatsResponse {
    private MovieFormat format;
    private String displayName;
}
