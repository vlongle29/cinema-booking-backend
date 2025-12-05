package com.example.CineBook.dto.genre;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GenreSearchDTO extends SearchBaseDto {
    private String keyword;
}
