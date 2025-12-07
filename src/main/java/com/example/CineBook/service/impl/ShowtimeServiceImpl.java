package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.DelFlag;
import com.example.CineBook.common.constant.ShowtimeStatus;
import com.example.CineBook.common.dto.response.PageResponse;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.dto.showtime.CreateShowtimeRequest;
import com.example.CineBook.dto.showtime.ShowtimeResponse;
import com.example.CineBook.dto.showtime.ShowtimeSearchDTO;
import com.example.CineBook.dto.showtime.UpdateShowtimeRequest;
import com.example.CineBook.mapper.BranchMapper;
import com.example.CineBook.mapper.MovieMapper;
import com.example.CineBook.mapper.RoomMapper;
import com.example.CineBook.mapper.ShowtimeMapper;
import com.example.CineBook.model.Branch;
import com.example.CineBook.model.Movie;
import com.example.CineBook.model.Room;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.repository.irepository.BranchRepository;
import com.example.CineBook.repository.irepository.MovieRepository;
import com.example.CineBook.repository.irepository.RoomRepository;
import com.example.CineBook.repository.irepository.ShowtimeRepository;
import com.example.CineBook.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {
    
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final BranchRepository branchRepository;
    private final ShowtimeMapper showtimeMapper;
    private final MovieMapper movieMapper;
    private final RoomMapper roomMapper;
    private final BranchMapper branchMapper;
    
    @Override
    @Transactional
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) {
        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime()) || 
            request.getEndTime().isEqual(request.getStartTime())) {
            throw new BusinessException(MessageCode.SHOWTIME_INVALID_TIME_RANGE);
        }
        
        // Validate movie exists
        Movie movie = movieRepository.findById(request.getMovieId())
            .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));
        
        if (Boolean.TRUE.equals(movie.getIsDelete())) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }
        
        // Validate room exists
        Room room = roomRepository.findById(request.getRoomId())
            .orElseThrow(() -> new BusinessException(MessageCode.ROOM_NOT_FOUND));
        
        if (Boolean.TRUE.equals(room.getIsDelete())) {
            throw new BusinessException(MessageCode.ROOM_NOT_FOUND);
        }
        
        // Check for overlapping showtimes
        List<Showtime> overlapping = showtimeRepository.findOverlappingShowtimes(
            request.getRoomId(),
            request.getStartTime(),
            request.getEndTime()
        );
        
        if (!overlapping.isEmpty()) {
            throw new BusinessException(MessageCode.SHOWTIME_TIME_OVERLAP);
        }
        
        // Create showtime
        Showtime showtime = showtimeMapper.toEntity(request);
        showtime.setBranchId(room.getBranchId());
        showtime.setStatus(ShowtimeStatus.OPEN);
        
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
        
        // Check if showtime is finished
        if (showtime.getStatus() == ShowtimeStatus.CLOSED) {
            throw new BusinessException(MessageCode.SHOWTIME_ALREADY_FINISHED);
        }
        
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
}
