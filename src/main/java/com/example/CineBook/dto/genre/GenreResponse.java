package com.example.CineBook.dto.genre;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GenreResponse {
    private UUID id;
    private String name;
    private String description;
}
