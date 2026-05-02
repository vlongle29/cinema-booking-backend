package com.example.CineBook.dto.booking;

import com.example.CineBook.common.dto.request.SearchBaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingSearchDTO extends SearchBaseDto {
    private String keyword;
    private String bookingCode;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String status;
}
