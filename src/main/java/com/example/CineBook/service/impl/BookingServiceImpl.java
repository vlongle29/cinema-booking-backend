package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.booking.*;
import com.example.CineBook.dto.bookingproduct.BookingProductBatchRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductItemRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import com.example.CineBook.dto.ticket.TicketItemRequest;
import com.example.CineBook.dto.ticket.TicketResponse;
import com.example.CineBook.mapper.BookingMapper;
import com.example.CineBook.mapper.BookingProductMapper;
import com.example.CineBook.model.Booking;
import com.example.CineBook.model.BookingProduct;
import com.example.CineBook.model.Ticket;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ShowtimeRepository showtimeRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketRepository ticketRepository;
    private final BookingProductRepository bookingProductRepository;
    private final BookingProductMapper bookingProductMapper;
    
    private static final int BOOKING_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {
        // Validate showtime exists (required)
        if (!showtimeRepository.existsById(request.getShowtimeId())) {
            throw new BusinessException(MessageCode.SHOWTIME_NOT_FOUND);
        }

        // Validate customer exists if provided
        if (request.getCustomerId() != null && !customerRepository.existsById(request.getCustomerId())) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }

        // Validate staff exists if provided
        if (request.getStaffId() != null && !employeeRepository.existsById(request.getStaffId())) {
            throw new BusinessException(MessageCode.EMPLOYEE_NOT_FOUND);
        }

        Booking booking = bookingMapper.map(request);
        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));
        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByCustomerId(userId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBooking(UUID id) {
        if (!bookingRepository.existsById(id)) {
            throw new BusinessException(MessageCode.BOOKING_NOT_FOUND);
        }
        bookingRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BookingResponse createDraftBooking(BookingDraftRequest request) {
        if (!showtimeRepository.existsById(request.getShowtimeId())) {
            throw new BusinessException(MessageCode.SHOWTIME_NOT_FOUND);
        }

        if (request.getCustomerId() != null && !customerRepository.existsById(request.getCustomerId())) {
            throw new BusinessException(MessageCode.USER_NOT_FOUND);
        }

        Booking booking = Booking.builder()
                .customerId(request.getCustomerId())
                .showtimeId(request.getShowtimeId())
                .status(BookingStatus.DRAFT)
                .bookingDate(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(BOOKING_EXPIRATION_MINUTES))
                .totalTicketPrice(BigDecimal.ZERO)
                .totalFoodPrice(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.ZERO)
                .build();

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse addTicketsBatch(UUID bookingId, TicketBatchRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_DRAFT);
        }

        if (booking.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }

        List<Ticket> tickets = new ArrayList<>();
        for (TicketItemRequest item : request.getTickets()) {
            if (ticketRepository.existsBySeatIdAndShowtimeId(item.getSeatId(), booking.getShowtimeId())) {
                throw new BusinessException(MessageCode.SEAT_ALREADY_BOOKED);
            }

            Ticket ticket = Ticket.builder()
                    .bookingId(bookingId)
                    .seatId(item.getSeatId())
                    .showtimeId(booking.getShowtimeId())
                    .price(item.getPrice())
                    .build();
            tickets.add(ticket);
        }

        try {
            ticketRepository.saveAll(tickets);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(MessageCode.SEAT_ALREADY_BOOKED);
        }

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse addProductsBatch(UUID bookingId, BookingProductBatchRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_DRAFT);
        }

        if (booking.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }

        List<BookingProduct> products = new ArrayList<>();
        for (BookingProductItemRequest item : request.getProducts()) {
            BookingProduct product = BookingProduct.builder()
                    .bookingId(bookingId)
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .priceAtPurchase(item.getPriceAtPurchase())
                    .build();
            products.add(product);
        }

        bookingProductRepository.saveAll(products);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSummaryResponse getBookingSummary(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        BigDecimal totalTicketPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BookingProduct> products = bookingProductRepository.findByBookingId(bookingId);
        BigDecimal totalFoodPrice = products.stream()
                .map(p -> p.getPriceAtPurchase().multiply(new BigDecimal(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = totalTicketPrice.add(totalFoodPrice);
        BigDecimal discountAmount = booking.getDiscountAmount() != null ? booking.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal finalAmount = subtotal.subtract(discountAmount);

        List<TicketResponse> ticketResponses = tickets.stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .bookingId(t.getBookingId())
                        .seatId(t.getSeatId())
                        .showtimeId(t.getShowtimeId())
                        .price(t.getPrice())
                        .build())
                .collect(Collectors.toList());

        List<BookingProductResponse> productResponses = products.stream()
                .map(bookingProductMapper::toResponse)
                .collect(Collectors.toList());

        return BookingSummaryResponse.builder()
                .bookingId(bookingId)
                .showtimeId(booking.getShowtimeId())
                .tickets(ticketResponses)
                .products(productResponses)
                .totalTicketPrice(totalTicketPrice)
                .totalFoodPrice(totalFoodPrice)
                .subtotal(subtotal)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    @Override
    @Transactional
    public BookingResponse checkout(UUID bookingId, BookingCheckoutRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.DRAFT) {
            throw new BusinessException(MessageCode.BOOKING_NOT_DRAFT);
        }

        if (booking.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.BOOKING_EXPIRED);
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (tickets.isEmpty()) {
            throw new BusinessException(MessageCode.BOOKING_NO_TICKETS);
        }

        BigDecimal totalTicketPrice = tickets.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BookingProduct> products = bookingProductRepository.findByBookingId(bookingId);
        BigDecimal totalFoodPrice = products.stream()
                .map(p -> p.getPriceAtPurchase().multiply(new BigDecimal(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = totalTicketPrice.add(totalFoodPrice);
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // TODO: Apply promotion logic here when promotion module is ready
        // if (request.getPromotionId() != null) {
        //     discountAmount = calculateDiscount(request.getPromotionId(), subtotal);
        // }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);

        booking.setTotalTicketPrice(totalTicketPrice);
        booking.setTotalFoodPrice(totalFoodPrice);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(finalAmount);
        booking.setPromotionId(request.getPromotionId());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setExpiredAt(null);

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }
}
