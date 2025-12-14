package com.example.CineBook.dto.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TicketBatchRequest {
    @NotEmpty(message = "Danh sách vé không được để trống")
    @Valid
    private List<TicketItemRequest> tickets;
}
