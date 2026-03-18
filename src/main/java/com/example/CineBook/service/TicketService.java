package com.example.CineBook.service;

import com.example.CineBook.dto.ticket.TicketCreateRequest;
import com.example.CineBook.dto.ticket.TicketDetailResponse;
import com.example.CineBook.dto.ticket.TicketResponse;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    TicketResponse createTicket(TicketCreateRequest request);
    List<TicketResponse> getTicketsByBookingId(UUID bookingId);
    List<TicketResponse> getTicketsByShowtimeId(UUID showtimeId);
    void deleteTicket(UUID id);
    
    // New methods for QR code and check-in
    TicketDetailResponse getTicketByCode(String ticketCode);
    TicketDetailResponse checkInTicket(String ticketCode);
    byte[] getQRCodeImage(String ticketCode);
}
