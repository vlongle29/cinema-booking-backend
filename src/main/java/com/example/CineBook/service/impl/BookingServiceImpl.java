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
import com.example.CineBook.dto.seat.SeatHoldRequest;
import com.example.CineBook.dto.ticket.TicketBatchRequest;
import com.example.CineBook.dto.ticket.TicketItemRequest;
import com.example.CineBook.dto.ticket.TicketResponse;
import com.example.CineBook.mapper.BookingMapper;
import com.example.CineBook.mapper.BookingProductMapper;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.BookingCodeGenerator;
import com.example.CineBook.service.BookingService;
import com.example.CineBook.service.SeatHoldService;
import com.example.CineBook.service.TicketCodeGenerator;
import com.example.CineBook.websocket.service.SeatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
    private final TicketCodeGenerator ticketCodeGenerator;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final BookingCodeGenerator bookingCodeGenerator;
    private final ProductRepository productRepository;

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
    public BookingResponse confirmBooking(BookingConfirmRequest request) {
        // Validate showtime
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));

        // Check if showtime already started
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(MessageCode.SHOWTIME_ALREADY_STARTED);
        }

        // Get current user
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID customerId = null;

        if (!SecurityUtils.hasRole("ADMIN")) {
            customerId = customerRepository.findByUserId(userId)
                    .map(Customer::getId)
                    .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        }

        // Validate all seats exist and check availability
        for (UUID seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new BusinessException(MessageCode.SEAT_NOT_FOUND));
            
            // Check if seat already booked in DB
            boolean alreadyBooked = ticketRepository.existsBySeatIdAndShowtimeId(seatId, request.getShowtimeId());
            if (alreadyBooked) {
                throw new BusinessException(MessageCode.SEAT_ALREADY_BOOKED);
            }
            
            // Check if seat already held in Redis
            boolean alreadyHeld = seatHoldService.isSeatHeld(seatId, request.getShowtimeId());
            if (alreadyHeld) {
                throw new BusinessException(MessageCode.SEAT_ALREADY_HELD);
            }
        }

        // Validate products if provided
        if (request.getProducts() != null) {
            for (BookingConfirmRequest.ProductItem item : request.getProducts()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new BusinessException(MessageCode.PRODUCT_NOT_FOUND));
            }
        }

        //

        // Create draft booking
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

        Booking savedBooking = bookingRepository.save(booking);
        UUID bookingId = savedBooking.getId();

        // Hold all seats in Redis
        for (UUID seatId : request.getSeatIds()) {
            try {
                seatHoldService.holdSeats(bookingId, SeatHoldRequest.builder()
                        .seatId(seatId)
                        .showtimeId(request.getShowtimeId())
                        .build());

                    // Broadcast SEAT_SELECTED to WebSocket subscribers
                seatWebSocketService.notifySeatSelected(savedBooking.getShowtimeId() , seatId, bookingId, savedBooking.getExpiredAt());
            } catch (BusinessException e) {
                // If any seat fails to hold, release all previously held seats and delete booking
                seatHoldService.clearHolds(bookingId);
                bookingRepository.delete(savedBooking);
                throw e;
            }
        }

        // Save products if provided
        List<BookingProductResponse> productResponses = new ArrayList<>();
        if (request.getProducts() != null && !request.getProducts().isEmpty()) {
            List<BookingProduct> products = new ArrayList<>();
            for (BookingConfirmRequest.ProductItem item : request.getProducts()) {
                BookingProduct product = BookingProduct.builder()
                        .bookingId(bookingId)
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .build();
                products.add(product);
            }
            List<BookingProduct> savedProduct = bookingProductRepository.saveAll(products);
                productResponses = savedProduct.stream()
                        .map(bookingProductMapper::toResponse)
                        .collect(Collectors.toList());
        }

        // Build ticket responses from held seats
        List<TicketResponse> ticketResponses = new ArrayList<>();
        for (UUID seatId : request.getSeatIds()) {
            Seat seat = seatRepository.findById(seatId).orElseThrow();
            SeatType seatType = seatTypeRepository.findById(seat.getSeatTypeId()).orElseThrow();
            String seatName = seat.getRowChar() + seat.getSeatNumber();
            ticketResponses.add(TicketResponse.builder()
                    .bookingId(bookingId)
                    .seatId(seatId)
                    .seatName(seatName)
                    .seatTypeName(seatType.getName())
                    .showtimeId(request.getShowtimeId())
                    .price(seatType.getBasePrice())
                    .build());
        }

        BookingResponse response = bookingMapper.toResponse(savedBooking);
        response.setTickets(ticketResponses);
        response.setProducts(productResponses);

        return response;
    }

    /**
     * @deprecated Sử dụng {@link #confirmBooking(BookingConfirmRequest)} thay thế
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @Override
    @Transactional
    public BookingResponse createDraftBooking(BookingDraftRequest request) {
        if (!showtimeRepository.existsById(request.getShowtimeId())) {
            throw new BusinessException(MessageCode.SHOWTIME_NOT_FOUND);
        }

        // Get userId from token
        UUID userId = SecurityUtils.getCurrentUserId();
        UUID customerId = null;

        if (!SecurityUtils.hasRole("ADMIN")) {
            customerId = customerRepository.findByUserId(userId)
                    .map(Customer::getId)
                    .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));
        }

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

    @Override
    @Transactional
    public BookingResponse refreshOrCreateBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        // If booking is still DRAFT and not expired, just extend expiration
        if (booking.getStatus() == BookingStatus.DRAFT && 
            booking.getExpiredAt().isAfter(LocalDateTime.now())) {
            booking.setExpiredAt(LocalDateTime.now().plusMinutes(BOOKING_EXPIRATION_MINUTES));
            Booking saved = bookingRepository.save(booking);
            return bookingMapper.toResponse(saved);
        }

        // If booking is EXPIRED or other status, create new draft booking
        if (booking.getStatus() == BookingStatus.EXPIRED || 
            booking.getStatus() == BookingStatus.CANCELLED) {
            
            // Clear old holds if any
            seatHoldService.clearHolds(bookingId);
            
            // Create new draft booking with same showtime
            BookingDraftRequest newRequest = new BookingDraftRequest();
            newRequest.setShowtimeId(booking.getShowtimeId());
            return createDraftBooking(newRequest);
        }

        // For other statuses (PAID, CONFIRMED, etc.), cannot refresh
        throw new BusinessException(MessageCode.BOOKING_NOT_IN_DRAFT_STATUS);
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
                .map(t -> {
                    Seat seat = seatRepository.findById(t.getSeatId())
                            .orElseThrow(() -> new BusinessException(MessageCode.SEAT_NOT_FOUND));
                    SeatType seatType = seatTypeRepository.findById(seat.getSeatTypeId())
                            .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND));
                    String seatName = seat.getRowChar() + seat.getSeatNumber();
                    
                    return TicketResponse.builder()
                            .id(t.getId())
                            .bookingId(t.getBookingId())
                            .seatId(t.getSeatId())
                            .seatName(seatName)
                            .seatTypeName(seatType.getName())
                            .showtimeId(t.getShowtimeId())
                            .price(t.getPrice())
                            .ticketCode(t.getTicketCode())
                            .build();
                })
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
            String ticketCode = ticketCodeGenerator.generateTicketCode();

            // Get actual price from Seat -> SeatType
            Seat seat = seatRepository.findById(hold.getSeatId())
                    .orElseThrow(() -> new BusinessException(MessageCode.SEAT_NOT_FOUND));
            SeatType seatType = seatTypeRepository.findById(seat.getSeatTypeId())
                    .orElseThrow(() -> new BusinessException(MessageCode.SEAT_TYPE_NOT_FOUND));

            Ticket ticket = Ticket.builder()
                    .bookingId(bookingId)
                    .seatId(hold.getSeatId())
                    .showtimeId(hold.getShowtimeId())
                    .price(seatType.getBasePrice())
                    .ticketCode(ticketCode)
                    .isCheckedIn(false)
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
        
        // Generate booking code for QR
        String bookingCode = bookingCodeGenerator.generateBookingCode();
        booking.setBookingCode(bookingCode);

        Booking saved = bookingRepository.save(booking);

        // Clear holds from Redis (not release - seats are now booked in DB)
        seatHoldService.clearHolds(bookingId);

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

        // TODO: Delete all products selected

        // Release all held seats
        seatHoldService.releaseSeats(bookingId);

        // Get all tickets and notify via WebSocket
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        if (!tickets.isEmpty()) {
            ticketRepository.deleteByBookingId(bookingId);

            // Notify WebSocket
            for (Ticket ticket : tickets) {
                seatWebSocketService.notifySeatReleased(
                        ticket.getShowtimeId(),
                        ticket.getSeatId()
                );
            }
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

    @Override
    @Transactional
    public BookingCheckInResponse checkInByBookingCode(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new BusinessException(MessageCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PAID) {
            throw new BusinessException(MessageCode.BOOKING_NOT_PAID);
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        if (tickets.isEmpty()) {
            throw new BusinessException(MessageCode.BOOKING_NO_TICKETS);
        }

        // Check if already checked in
        boolean alreadyCheckedIn = tickets.stream().allMatch(Ticket::getIsCheckedIn);
        Instant checkedInAt = null;

        if (!alreadyCheckedIn) {
            // Mark all tickets as checked in
            for (Ticket ticket : tickets) {
                ticket.setIsCheckedIn(true);
            }
            ticketRepository.saveAll(tickets);
            checkedInAt = Instant.now();
        } else {
            // Get first ticket's update time as check-in time
            checkedInAt = tickets.get(0).getUpdateTime();
        }

        // Get booking details
        Showtime showtime = showtimeRepository.findById(booking.getShowtimeId()).orElse(null);
        Movie movie = showtime != null ? movieRepository.findById(showtime.getMovieId()).orElse(null) : null;
        Branch branch = showtime != null ? branchRepository.findById(showtime.getBranchId()).orElse(null) : null;
        Room room = showtime != null ? roomRepository.findById(showtime.getRoomId()).orElse(null) : null;

        List<String> seatNames = tickets.stream()
                .map(ticket -> {
                    Seat seat = seatRepository.findById(ticket.getSeatId()).orElse(null);
                    return seat != null ? seat.getRowChar() + seat.getSeatNumber() : "Unknown";
                })
                .collect(Collectors.toList());

        return BookingCheckInResponse.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .movieTitle(movie != null ? movie.getTitle() : null)
                .showtimeStartTime(showtime != null ? showtime.getStartTime() : null)
                .branchName(branch != null ? branch.getName() : null)
                .roomName(room != null ? room.getName() : null)
                .seatNames(seatNames)
                .ticketCount(tickets.size())
                .totalAmount(booking.getFinalAmount())
                .isCheckedIn(true)
                .checkedInAt(checkedInAt)
                .build();
    }
}
