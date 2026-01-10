package com.example.CineBook.dto.movie;

import com.example.CineBook.common.constant.MovieStatus;
import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class MovieSearchDTO extends SearchBaseDto {
    private String keyword;
    private String status;
    private LocalDate releaseDateFrom;
    private LocalDate releaseDateTo;
}
