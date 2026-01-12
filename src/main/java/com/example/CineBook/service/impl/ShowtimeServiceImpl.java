package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.DelFlag;
import com.example.CineBook.common.constant.MovieFormat;
import com.example.CineBook.common.constant.ShowtimeStatus;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.city.CityResponse;
import com.example.CineBook.dto.seat.SeatHoldData;
import com.example.CineBook.dto.showtime.*;
import com.example.CineBook.mapper.BranchMapper;
import com.example.CineBook.mapper.MovieMapper;
import com.example.CineBook.mapper.RoomMapper;
import com.example.CineBook.mapper.ShowtimeMapper;
import com.example.CineBook.model.*;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {
    
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final CityRepository cityRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeMapper showtimeMapper;
    private final MovieMapper movieMapper;
    private final RoomMapper roomMapper;
    private final BranchMapper branchMapper;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    
    @Override
    @Transactional
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) {
        // Validate movie exists
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));
        
        if (Boolean.TRUE.equals(movie.getIsDelete())) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }
        
        // Validate branch exists
        Branch branch = branchRepository.findById(request.getBranchId())
            .orElseThrow(() -> new BusinessException(MessageCode.BRANCH_NOT_FOUND));
        
        if (Boolean.TRUE.equals(branch.getIsDelete())) {
            throw new BusinessException(MessageCode.BRANCH_NOT_FOUND);
        }
        
        // Validate room exists and belongs to the branch
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        
        if (Boolean.TRUE.equals(room.getIsDelete())) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        if (!room.getBranchId().equals(request.getBranchId())) {
            throw new BusinessException(MessageCode.ROOM_NOT_BELONG_TO_BRANCH);
        }
        
        // Calculate end time
        int cleaningBuffer = 15; // minutes
        LocalDateTime calculatedEndTime = request.getStartTime().plusMinutes(movie.getDurationMinutes()).plusMinutes(cleaningBuffer);
        
        // Check for overlapping showtimes
        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
            request.getRoomId(),
            request.getStartTime(),
            calculatedEndTime
        );
        
        if (!overlapping.isEmpty()) {
            throw new BusinessException(MessageCode.SHOWTIME_TIME_OVERLAP);
        }
        
        // Create showtime
        Showtime showtime = showtimeMapper.toEntity(request);
        showtime.setStatus(ShowtimeStatus.OPEN);
        showtime.setEndTime(calculatedEndTime);
        
        Showtime saved = showtimeRepository.save(showtime);
        return buildShowtimeResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ShowtimeResponse> searchShowtimes(ShowtimeSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(
            searchDTO.getPage() != null ? searchDTO.getPage() - 1 : 0,
            searchDTO.getSize() != null ? searchDTO.getSize() : 10
        );
        Page<Showtime> entityPage = showtimeRepository.findAll(showtimeRepository.searchWithFilters(searchDTO), pageable);
        Page<ShowtimeResponse> responsePage = entityPage.map(this::buildShowtimeResponse);
        return PageResponse.of(responsePage);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ShowtimeResponse getShowtimeById(UUID id) {
        Showtime showtime = showtimeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));
        
        if (Boolean.TRUE.equals(showtime.getIsDelete())) {
            throw new BusinessException(MessageCode.SHOWTIME_NOT_FOUND);
        }
        
        return buildShowtimeResponse(showtime);
    }
    
    @Override
    @Transactional
    public ShowtimeResponse updateShowtime(UUID id, UpdateShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));
        
//        // Check if showtime is finished
//        if (showtime.getStatus() == ShowtimeStatus.CLOSED) {
//            throw new BusinessException(MessageCode.SHOWTIME_ALREADY_FINISHED);
//        }
        
        // Validate time range if both times are provided
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime()) || 
                request.getEndTime().isEqual(request.getStartTime())) {
                throw new BusinessException(MessageCode.SHOWTIME_INVALID_TIME_RANGE);
            }
        }
        
        // Validate movie if changed
        if (request.getMovieId() != null) {
            Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));
            
            if (Boolean.TRUE.equals(movie.getIsDelete())) {
                throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
            }
        }
        
        // Validate room if changed and update branchId
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
            
            if (Boolean.TRUE.equals(room.getIsDelete())) {
                throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
            }
            
            showtime.setBranchId(room.getBranchId());
        }
        
        // Check for overlapping showtimes if time or room changed
        UUID roomIdToCheck = request.getRoomId() != null ? request.getRoomId() : showtime.getRoomId();
        var startTime = request.getStartTime() != null ? request.getStartTime() : showtime.getStartTime();
        var endTime = request.getEndTime() != null ? request.getEndTime() : showtime.getEndTime();
        
        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimesExcludingCurrent(
            roomIdToCheck,
            id,
            startTime,
            endTime
        );
        
        if (!overlapping.isEmpty()) {
            throw new BusinessException(MessageCode.SHOWTIME_TIME_OVERLAP);
        }
        
        // Update showtime
        showtimeMapper.updateEntityFromDto(request, showtime);
        Showtime updated = showtimeRepository.save(showtime);
        
        return buildShowtimeResponse(updated);
    }
    
    @Override
    @Transactional
    public void deleteShowtime(UUID id) {
        Showtime showtime = showtimeRepository.findById(id)
            .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));
        
        // Soft delete
        showtime.setDeleted(DelFlag.DELETED.getValue());
    }
    
    private ShowtimeResponse buildShowtimeResponse(Showtime showtime) {
        ShowtimeResponse response = showtimeMapper.toResponse(showtime);
        response.setStatus(showtime.getStatus() != null ? showtime.getStatus().name() : null);
        
        // Load movie
        movieRepository.findById(showtime.getMovieId()).ifPresent(movie -> {
            if (!Boolean.TRUE.equals(movie.getIsDelete())) {
                response.setMovie(movieMapper.toResponse(movie));
            }
        });
        
        // Load room
        roomRepository.findById(showtime.getRoomId()).ifPresent(room -> {
            if (!Boolean.TRUE.equals(room.getIsDelete())) {
                response.setRoom(roomMapper.toResponse(room));
            }
        });
        
        // Load branch
        branchRepository.findById(showtime.getBranchId()).ifPresent(branch -> {
            if (!Boolean.TRUE.equals(branch.getIsDelete())) {
                response.setBranch(branchMapper.toResponse(branch));
            }
        });
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDates(UUID movieId) {
        LocalDate today = LocalDate.now();
        return showtimeRepository.findAvailableDatesByMovieId(movieId, today);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getAvailableCities(UUID movieId, LocalDate date) {
        List<City> cities = cityRepository.findAvailableCitiesByMovieAndDate(movieId, date);
        return cities.stream()
            .map(city -> CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .code(city.getCode())
                .build())
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MovieFormat> getAvailableFormats(UUID movieId, LocalDate date, UUID cityId) {
        return showtimeRepository.findAvailableFormatsByMovieAndCityAndDate(movieId, cityId, date);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeGroupedByBranchResponse> getShowtimesGroupedByBranch(
            UUID movieId, LocalDate date, UUID cityId, MovieFormat format) {
        
        ShowtimeSearchDTO searchDTO = ShowtimeSearchDTO.builder()
            .movieId(movieId)
            .date(date)
            .cityId(cityId)
            .format(format)
            .build();
        
        List<Showtime> showtimes = showtimeRepository.findAll(
            showtimeRepository.searchWithFilters(searchDTO));

        // Group showtime list by branchId into a Map
        Map<UUID, List<Showtime>> groupedByBranch = new HashMap<>();
        for (Showtime showtime : showtimes) {
            UUID branchId = showtime.getBranchId();
            if (!groupedByBranch.containsKey(branchId)) {
                groupedByBranch.put(branchId, new ArrayList<>());
            }
            groupedByBranch.get(branchId).add(showtime);
        }
        
        return groupedByBranch.entrySet().stream()
            .map(entry -> {
                UUID branchId = entry.getKey();
                List<Showtime> branchShowtimes = entry.getValue();
                
                Branch branch = branchRepository.findById(branchId).orElse(null);
                if (branch == null || Boolean.TRUE.equals(branch.getIsDelete())) {
                    return null;
                }
                
                List<ShowtimeItemResponse> items = branchShowtimes.stream()
                    .map(showtime -> {
                        Room room = roomRepository.findById(showtime.getRoomId()).orElse(null);
                        return ShowtimeItemResponse.builder()
                            .showtimeId(showtime.getId())
                            .startTime(showtime.getStartTime())
                            .format(showtime.getFormat())
                            .roomName(room != null ? room.getName() : "Unknown")
                            .availableSeats(room != null ? room.getCapacity() : 0)
                            .build();
                    })
                    .sorted(Comparator.comparing(ShowtimeItemResponse::getStartTime))
                    .collect(Collectors.toList());
                
                return ShowtimeGroupedByBranchResponse.builder()
                    .branchId(branchId)
                    .branchName(branch.getName())
                    .showtimes(items)
                    .build();
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ShowtimeGroupedByBranchResponse::getBranchName))
            .collect(Collectors.toList());
    }

    /**
     *
     * Data when response
     * {
     *   "2025-12-08": [
     *     {
     *       "id": "uuid",
     *       "movieTitle": "Avatar 3",
     *       "startTime": "13:30",
     *       "endTime": "16:00",
     *       "format": "3D"
     *     },
     *     {
     *       "id": "uuid",
     *       "movieTitle": "Spider-Man",
     *       "startTime": "18:00",
     *       "endTime": "20:30",
     *       "format": "2D"
     *     }
     *   ],
     *   "2025-12-09": [
     *     {
     *       "id": "uuid",
     *       "movieTitle": "Avatar 3",
     *       "startTime": "14:00",
     *       "endTime": "16:30",
     *       "format": "3D"
     *     }
     *   ],
     *   "2025-12-10": [],
     *   "2025-12-11": [...]
     * }
     * @param roomId
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public RoomShowtimeResponse getShowtimesByRoom(UUID roomId, LocalDate startDate, LocalDate endDate) {
        // Validate room exists
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        
        if (Boolean.TRUE.equals(room.getIsDelete())) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        // Get existing showtimes
        List<Showtime> showtimes = showtimeRepository.findByRoomAndDateRange(roomId, startDate, endDate);
        
        // Group by date
        Map<LocalDate, List<RoomShowtimeResponse.ShowtimeSlot>> showtimesByDate = new LinkedHashMap<>();
        
        for (Showtime showtime : showtimes) {
            LocalDate date = showtime.getStartTime().toLocalDate();
            
            Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
            
            RoomShowtimeResponse.ShowtimeSlot slot = RoomShowtimeResponse.ShowtimeSlot.builder()
                .id(showtime.getId())
                .movieTitle(movie != null ? movie.getTitle() : "Unknown")
                .startTime(showtime.getStartTime().toLocalTime())
                .endTime(showtime.getEndTime().toLocalTime())
                .format(showtime.getFormat() != null ? showtime.getFormat().name() : null)
                .build();
            
            showtimesByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(slot);
        }
        
        return RoomShowtimeResponse.builder()
            .showtimesByDate(showtimesByDate)
            .build();
    }
    
    @Override
    @Transactional
    public BulkCreateShowtimeResponse bulkCreateShowtimes(BulkCreateShowtimeRequest request) {
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException(MessageCode.SHOWTIME_INVALID_DATE_RANGE);
        }
        
        // Calculate total showtimes
        long days = request.getStartDate().datesUntil(request.getEndDate().plusDays(1)).count();
        int totalShowtimes = (int) (days * request.getTimeSlots().size());
        
        if (totalShowtimes > 100) {
            throw new BusinessException(MessageCode.SHOWTIME_BULK_LIMIT_EXCEEDED);
        }
        
        // Validate movie exists and get duration
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));
        
        if (Boolean.TRUE.equals(movie.getIsDelete())) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }
        
        if (movie.getDurationMinutes() == null || movie.getDurationMinutes() <= 0) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }
        
        // Validate room exists
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        
        if (Boolean.TRUE.equals(room.getIsDelete())) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        // Get existing showtimes in the date range
        List<Showtime> existingShowtimes = showtimeRepository.findByRoomAndDateRange(
            request.getRoomId(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        List<ShowtimeResponse> createdShowtimes = new ArrayList<>();
        List<BulkCreateShowtimeResponse.ConflictInfo> conflicts = new ArrayList<>();
        List<TimeSlot> createdSlots = new ArrayList<>();
        
        // Loop through each date
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            for (LocalTime timeSlot : request.getTimeSlots()) {
                LocalDateTime startTime = LocalDateTime.of(currentDate, timeSlot);           // 2025-12-10 14:30:00
                LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMinutes());   // 2025-12-10 16:30:00
                
                // Check overlap with existing showtimes
                boolean hasOverlap = existingShowtimes.stream()
                    .anyMatch(s -> isOverlapping(s.getStartTime(), s.getEndTime(), startTime, endTime));

                
                // Check overlap with already created showtimes in this batch
                boolean hasOverlapWithBatch = createdSlots.stream()
                    .anyMatch(slot -> isOverlapping(slot.start, slot.end, startTime, endTime));
                
                if (hasOverlap || hasOverlapWithBatch) {
                    conflicts.add(BulkCreateShowtimeResponse.ConflictInfo.builder()
                        .date(currentDate)
                        .time(timeSlot)
                        .reason("Overlaps with another showtime in this batch")
                        .build());
                    continue;
                }
                
                // Create showtime
                Showtime showtime = Showtime.builder()
                    .movieId(request.getMovieId())
                    .roomId(request.getRoomId())
                    .branchId(room.getBranchId())
                    .startTime(startTime)
                    .endTime(endTime)
                    .basePrice(request.getPrice())
                    .format(request.getFormat())
                    .status(ShowtimeStatus.OPEN)
                    .build();
                
                Showtime saved = showtimeRepository.save(showtime);
                createdShowtimes.add(buildShowtimeResponse(saved));
                createdSlots.add(new TimeSlot(startTime, endTime));
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return BulkCreateShowtimeResponse.builder()
            .totalRequested(totalShowtimes)
            .created(createdShowtimes.size())
            .skipped(conflicts.size())
            .conflicts(conflicts)
            .showtimes(createdShowtimes)
            .build();
    }
    
    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, 
                                   LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ShowtimeSeatStatusResponse getSeatStatusByShowtime(UUID showtimeId) {
        // Validate showtime exists
        Showtime showtime = showtimeRepository.findById(showtimeId)
            .orElseThrow(() -> new BusinessException(MessageCode.SHOWTIME_NOT_FOUND));
        
        if (Boolean.TRUE.equals(showtime.getIsDelete())) {
            throw new BusinessException(MessageCode.SHOWTIME_NOT_FOUND);
        }
        
        // Get room info
        Room room = roomRepository.findById(showtime.getRoomId())
            .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        log.info("Getting seat status for showtime {} in room {}", showtimeId, room.getId());


        // Get all seats in room
        List<Seat> seats = seatRepository.findByRoomId(room.getId());
        log.info("Total seats in room {}: {}", room.getId(), seats.size());
        
        // Get booked seats (tickets in DB)
        List<Ticket> bookedTickets = ticketRepository.findByShowtimeId(showtimeId);
        log.info("Total booked tickets for showtime {}: {}", showtimeId, bookedTickets.size());
        Set<UUID> bookedSeatIds = bookedTickets.stream()
            .map(Ticket::getSeatId)
            .collect(Collectors.toSet());
        log.info("Total booked seat IDs for showtime {}: {}", showtimeId, bookedSeatIds.size());
        
        // Get held seats from Redis
        Map<UUID, UUID> heldSeats = new HashMap<>(); // seatId -> bookingId
        for (Seat seat : seats) {
            try {
                String holdKey = String.format("seat:hold:%s:%s", showtimeId, seat.getId());
                Object rawData = redisTemplate.opsForValue().get(holdKey);
                if (rawData instanceof SeatHoldData) {
                    SeatHoldData holdData = (SeatHoldData) rawData;
                    heldSeats.put(seat.getId(), holdData.getBookingId());
                }
            } catch (Exception e) {
                log.warn("Failed to get hold data for seat {}: {}", seat.getId(), e.getMessage());
            }
        }
        
        // Build seat status list
        List<ShowtimeSeatStatusResponse.SeatStatus> seatStatuses = seats.stream()
            .map(seat -> {
                String status;
                UUID heldByBookingId = null;
                
                if (bookedSeatIds.contains(seat.getId())) {
                    status = "BOOKED";
                } else if (heldSeats.containsKey(seat.getId())) {
                    status = "HELD";
                    heldByBookingId = heldSeats.get(seat.getId());
                } else {
                    status = "AVAILABLE";
                }

                String seatType = null;
                try {
                    seatType = seatRepository.getSeatTypeNameById(seat.getId());
                } catch (Exception e) {
                    log.error("Error getting seat type for seat {}: {}", seat.getId(), e.getMessage());
                    seatType = "STANDARD";
                }
                
                return ShowtimeSeatStatusResponse.SeatStatus.builder()
                    .seatId(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .rowChar(seat.getRowChar())
                    .seatType(seatType)
                    .price(showtime.getBasePrice())
                    .status(status)
                    .heldByBookingId(heldByBookingId)
                    .build();
            })
            .collect(Collectors.toList());
        
        // Calculate statistics
        int totalSeats = seats.size();
        int bookedSeats = bookedSeatIds.size();
        int heldSeatsCount = heldSeats.size();
        int availableSeats = totalSeats - bookedSeats - heldSeatsCount;
        
        return ShowtimeSeatStatusResponse.builder()
            .showtimeId(showtimeId)
            .roomId(room.getId())
            .roomName(room.getName())
            .totalSeats(totalSeats)
            .availableSeats(availableSeats)
            .bookedSeats(bookedSeats)
            .heldSeats(heldSeatsCount)
            .seats(seatStatuses)
            .build();
    }
    
    private static class TimeSlot {
        LocalDateTime start;
        LocalDateTime end;
        
        TimeSlot(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
