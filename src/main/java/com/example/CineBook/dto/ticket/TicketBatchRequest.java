package com.example.CineBook.dto.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Request DTO để thêm nhiều vé vào booking.
 *
 * @deprecated Kể từ version 2.0, API này không còn được sử dụng.
 *             Vé sẽ được tạo tự động trong quá trình checkout.
 *             Class này sẽ bị xóa trong version 3.0.
 */
@Deprecated(since = "Sử dụng TicketBatchCreateRequest thay thế")
@Data
public class TicketBatchRequest {
    @NotEmpty(message = "Danh sách vé không được để trống")
    @Valid
    private List<TicketItemRequest> tickets;
}
