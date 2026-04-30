package com.example.CineBook.service.impl;

import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.ticket.TicketCreateRequest;
import com.example.CineBook.dto.ticket.TicketDetailResponse;
import com.example.CineBook.dto.ticket.TicketResponse;
import com.example.CineBook.model.Seat;
import com.example.CineBook.model.SeatType;
import com.example.CineBook.model.Ticket;
import com.example.CineBook.repository.irepository.SeatRepository;
import com.example.CineBook.repository.irepository.SeatTypeRepository;
import com.example.CineBook.repository.irepository.TicketRepository;
import com.example.CineBook.service.QRCodeService;
import com.example.CineBook.service.TicketCodeGenerator;
import com.example.CineBook.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final QRCodeService qrCodeService;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    /**
     * @deprecated Ticket code không còn được dùng cho QR code chính.
     * Sử dụng BookingCodeGenerator và booking check-in flow thay thế.
     * Ticket code vẫn được tạo cho mục đích tracking/audit.
     */
    @Deprecated
    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest request) {
        String ticketCode = ticketCodeGenerator.generateTicketCode();
        
        Ticket ticket = Ticket.builder()
                .bookingId(request.getBookingId())
                .seatId(request.getSeatId())
                .showtimeId(request.getShowtimeId())
                .price(request.getPrice())
                .ticketCode(ticketCode)
                .isCheckedIn(false)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        return toResponse(saved);
    }

    @Override
    public List<TicketResponse> getTicketsByBookingId(UUID bookingId) {
        return ticketRepository.findByBookingId(bookingId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketResponse> getTicketsByShowtimeId(UUID showtimeId) {
        return ticketRepository.findByShowtimeId(showtimeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new BusinessException(MessageCode.TICKET_NOT_FOUND);
        }
        ticketRepository.deleteById(id);
    }

    /**
     * @deprecated Sử dụng booking lookup thay thế.
     * Ticket code vẫn tồn tại cho mục đích tracking nhưng không dùng cho user-facing features.
     */
    @Deprecated
    @Override
    public TicketDetailResponse getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new BusinessException(MessageCode.TICKET_NOT_FOUND));
        
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(ticketCode);
        
        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .bookingId(ticket.getBookingId())
                .seatId(ticket.getSeatId())
                .showtimeId(ticket.getShowtimeId())
                .price(ticket.getPrice())
                .qrCodeBase64(qrCodeBase64)
                .isCheckedIn(ticket.getIsCheckedIn())
                .createdAt(ticket.getCreateTime())
                .build();
    }

    /**
     * @deprecated Sử dụng BookingService.checkInByBookingCode() thay thế.
     * Check-in theo booking sẽ đánh dấu tất cả tickets cùng lúc.
     */
    @Deprecated
    @Override
    @Transactional
    public TicketDetailResponse checkInTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new BusinessException(MessageCode.TICKET_NOT_FOUND));
        
        if (Boolean.TRUE.equals(ticket.getIsCheckedIn())) {
            throw new BusinessException(MessageCode.TICKET_ALREADY_CHECKED_IN);
        }
        
        ticket.setIsCheckedIn(true);
        Ticket updated = ticketRepository.save(ticket);
        
        String qrCodeBase64 = qrCodeService.generateQRCodeBase64(ticketCode);
        
        return TicketDetailResponse.builder()
                .id(updated.getId())
                .ticketCode(updated.getTicketCode())
                .bookingId(updated.getBookingId())
                .seatId(updated.getSeatId())
                .showtimeId(updated.getShowtimeId())
                .price(updated.getPrice())
                .qrCodeBase64(qrCodeBase64)
                .isCheckedIn(updated.getIsCheckedIn())
                .createdAt(updated.getCreateTime())
                .build();
    }

    /**
     * @deprecated QR code hiện tại dựa trên bookingCode, không phải ticketCode.
     * Email sẽ chứa QR code của booking, không phải từng ticket riêng lẻ.
     */
    @Deprecated
    @Override
    public byte[] getQRCodeImage(String ticketCode) {
        if (!ticketRepository.findByTicketCode(ticketCode).isPresent()) {
            throw new BusinessException(MessageCode.TICKET_NOT_FOUND);
        }
        return qrCodeService.generateQRCodeBytes(ticketCode);
    }

    private TicketResponse toResponse(Ticket ticket) {
        Seat seat = seatRepository.findById(ticket.getSeatId())
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_NOT_FOUND));
        
        SeatType seatType = seatTypeRepository.findById(seat.getSeatTypeId())
                .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND));
        
        String seatName = seat.getRowChar() + seat.getSeatNumber();
        
        return TicketResponse.builder()
                .id(ticket.getId())
                .bookingId(ticket.getBookingId())
                .seatId(ticket.getSeatId())
                .seatName(seatName)
                .seatTypeName(seatType.getName())
                .showtimeId(ticket.getShowtimeId())
                .price(ticket.getPrice())
                .ticketCode(ticket.getTicketCode())
                .createdBy(ticket.getCreateBy())
                .createTime(ticket.getCreateTime())
                .build();
    }
}
