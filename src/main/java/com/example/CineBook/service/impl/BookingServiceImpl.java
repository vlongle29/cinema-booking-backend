package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.SecurityUtils;
import com.example.CineBook.dto.booking.*;
import com.example.CineBook.dto.bookingproduct.BookingProductBatchRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductItemRequest;
import com.example.CineBook.dto.bookingproduct.BookingProductResponse;
import com.example.CineBook.dto.seat.SeatHoldData;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import com.example.CineBook.dto.ticket.TicketItemRequest;
import com.example.CineBook.dto.ticket.TicketResponse;
import com.example.CineBook.mapper.BookingMapper;
import com.example.CineBook.mapper.BookingProductMapper;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.BookingService;
import com.example.CineBook.service.SeatHoldService;
import com.example.CineBook.websocket.service.SeatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SeatWebSocketService seatWebSocketService;
    private final SeatHoldService seatHoldService;
    private final MovieRepository movieRepository;
    private final BranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final CityRepository cityRepository;

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

        // Get userId from token
        UUID userId = SecurityUtils.getCurrentUserId();
        // Find or create customer from userId
        UUID customerId = customerRepository.findByUserId(userId)
                .map(Customer::getId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        Booking booking = Booking.builder()
                .customerId(customerId)
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

    /**
     * @deprecated Use holdSeats() + checkout() flow instead.
     * This method bypasses Redis hold mechanism and should not be used.
     */
    @Deprecated
    @Override
    @Transactional
    public BookingResponse addTicketsBatch(UUID bookingId, TicketBatchRequest request) {
        throw new BusinessException(MessageCode.BAD_REQUEST);
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

        // Check holds from Redis
        List<SeatHoldData> holds = ((SeatHoldServiceImpl) seatHoldService).getHoldsByBooking(bookingId);
        if (holds.isEmpty()) {
            throw new BusinessException(MessageCode.BOOKING_NO_TICKETS);
        }

        // Convert holds to tickets
        List<Ticket> ticketsToBook = new ArrayList<>();
        for (var hold : holds) {
            // TODO: Get actual price from Seat -> SeatType
            Ticket ticket = Ticket.builder()
                    .bookingId(bookingId)
                    .seatId(hold.getSeatId())
                    .showtimeId(hold.getShowtimeId())
                    .price(BigDecimal.valueOf(50000)) // Temporary fixed price
                    .build();
            ticketsToBook.add(ticket);
        }

        ticketRepository.saveAll(ticketsToBook);

        // Calculate prices
        BigDecimal totalTicketPrice = ticketsToBook.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BookingProduct> products = bookingProductRepository.findByBookingId(bookingId);
        BigDecimal totalFoodPrice = products.stream()
                .map(p -> p.getPriceAtPurchase().multiply(new BigDecimal(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = totalTicketPrice.add(totalFoodPrice);
        BigDecimal discountAmount = BigDecimal.ZERO;

        // TODO: Apply promotion logic

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

        // Release holds from Redis
        seatHoldService.releaseSeats(bookingId);

        // Broadcast SEAT_BOOKED
        for (Ticket ticket : ticketsToBook) {
            seatWebSocketService.notifySeatBooked(
                    ticket.getShowtimeId(),
                    ticket.getSeatId(),
                    bookingId
            );
        }

        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(UUID bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        // Check if booking can be cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.REFUNDED) {
            throw new BusinessException(MessageCode.BOOKING_ALREADY_CANCELLED);
        }

        // Get showtime to check if it has started
        var showtime = showtimeRepository.findById(booking.getShowtimeId())
                .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));

        // Cannot cancel if showtime has already started
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.SHOWTIME_ALREADY_STARTED);
        }

        // Release all held seats
        seatHoldService.releaseSeats(bookingId);

        // Get all tickets and notify via WebSocket
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        for (Ticket ticket : tickets) {
            seatWebSocketService.notifySeatReleased(
                    ticket.getShowtimeId(),
                    ticket.getSeatId()
            );
        }

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());

        // TODO: Process refund if payment was made
        // if (booking.getStatus() == BookingStatus.CONFIRMED || 
        //     booking.getStatus() == BookingStatus.PAID) {
        //     processRefund(booking);
        // }

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MyBookingResponse> getMyBookings(Pageable pageable) {
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID customerId = customerRepository.findByUserId(userId)
                .map(Customer::getId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        Page<Booking> bookings = bookingRepository.findByCustomerIdOrderByBookingDateDesc(customerId, pageable);

        Page<MyBookingResponse> responsePage = bookings.map(booking -> {
            Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
            Movie movie = showtime != null ? movieRepository.findById(showtime.getMovieId()).orElse(null) : null;
            Branch branch = showtime != null ? branchRepository.findById(showtime.getBranchId()).orElse(null) : null;
            Room room = showtime != null ? roomRepository.findById(showtime.getRoomId()).orElse(null) : null;
            City city = branch != null ? cityRepository.findById(branch.getCityId()).orElse(null) : null;

            int ticketCount = ticketRepository.findByBookingId(booking.getId()).size();

            return MyBookingResponse.builder()
                    .id(booking.getId())
                    .showtimeId(booking.getShowtimeId())
                    .showtimeStartTime(showtime != null ? showtime.getStartTime() : null)
                    .movieTitle(movie != null ? movie.getTitle() : null)
                    .moviePosterUrl(movie != null ? movie.getPosterUrl() : null)
                    .branchName(branch != null ? branch.getName() : null)
                    .roomName(room != null ? room.getName() : null)
                    .cityName(city != null ? city.getName() : null)
                    .ticketCount(ticketCount)
                    .totalTicketPrice(booking.getTotalTicketPrice())
                    .totalFoodPrice(booking.getTotalFoodPrice())
                    .discountAmount(booking.getDiscountAmount())
                    .finalAmount(booking.getFinalAmount())
                    .bookingDate(booking.getBookingDate())
                    .status(booking.getStatus())
                    .paymentMethod(booking.getPaymentMethod())
                    .build();
        });

        return PageResponse.of(responsePage);
    }
}
