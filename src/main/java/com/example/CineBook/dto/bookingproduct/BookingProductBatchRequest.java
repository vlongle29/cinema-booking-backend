package com.example.CineBook.dto.bookingproduct;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BookingProductBatchRequest {
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<BookingProductItemRequest> products;
}
