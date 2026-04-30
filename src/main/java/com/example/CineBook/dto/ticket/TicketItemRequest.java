package com.example.CineBook.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Item trong TicketBatchRequest.
 *
 * @deprecated Kể từ version 2.0, chỉ dùng cho {@link TicketBatchRequest} đã deprecated.
 *             Class này sẽ bị xóa trong version 3.0.
 */
@Deprecated(since = "2.0", forRemoval = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketItemRequest {
    @NotNull(message = "Seat ID không được để trống")
    private UUID seatId;
    
    @NotNull(message = "Giá vé không được để trống")
    private BigDecimal price;
}
