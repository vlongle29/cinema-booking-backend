package com.example.CineBook.service.impl;

import com.example.CineBook.common.constant.BookingStatus;
import com.example.CineBook.common.exception.BusinessException;
import com.example.CineBook.common.exception.MessageCode;
import com.example.CineBook.common.security.SecurityUtils;
import com.example.CineBook.dto.review.CreateReviewRequest;
import com.example.CineBook.dto.review.MovieRatingSummaryResponse;
import com.example.CineBook.dto.review.RatingCountDTO;
import com.example.CineBook.dto.review.ReviewResponse;
import com.example.CineBook.mapper.ReviewMapper;
import com.example.CineBook.model.Movie;
import com.example.CineBook.model.Review;
import com.example.CineBook.model.ReviewLike;
import com.example.CineBook.repository.irepository.*;
import com.example.CineBook.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final MovieRepository movieRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;
    private final ReviewRepository reviewRepository;
    private final SysUserRepository sysUserRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResponse createReview(CreateReviewRequest request) {
        // 1. Kiểm tra xem phim có tồn tại không
        if (!movieRepository.existsById(request.getMovieId())) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }

        // 2. Lấy ID user từ Security Context
        UUID userId = SecurityUtils.getCurrentUserId();

        // 3. Chặn nghiệp vụ: Mỗi user chỉ được review 1 phim 1 lần
        if (reviewRepository.existsByUserIdAndMovieId(userId, request.getMovieId())) {
            throw new BusinessException(MessageCode.REVIEW_ALREADY_EXISTS);
        }

        // Lấy username để trả về UI (Throw lỗi nếu không tìm thấy user trong DB)
        String username = sysUserRepository.findUsernameById(userId)
                .orElseThrow(() -> new BusinessException(MessageCode.USER_NOT_FOUND));

        List<UUID> showtimeIds = showtimeRepository.findIdsByMovieId(request.getMovieId());
        boolean hasPurchased = bookingRepository.existsByUserIdAndShowtimeIdAndStatus(userId, showtimeIds, BookingStatus.PAID);

        // 4. Xác định huy hiệu "Verified Ticket"
        // Trả về true nếu có đơn hàng PAID, false nếu chưa mua hoặc mua chưa thành công

        if (!hasPurchased) {
           throw new BusinessException(MessageCode.TICKET_REQUIRED_FOR_REVIEW);
        }

        // 5. Khởi tạo và Map dữ liệu Entity
        Review review = reviewMapper.toEntity(request);
        review.setUserId(userId);
        review.setIsVerified(hasPurchased); // Gán huy hiệu dựa trên lịch sử mua vé
        review.setLikeCount(0);

        // 6. Lưu xuống Database
        Review savedReview = reviewRepository.save(review);

        // 7. Cập nhật lại thông số đánh giá của phim (averageRating, totalReviews)
        updateMovieRating(request.getMovieId());

        // 8. Build Response trả về UI
        ReviewResponse response = reviewMapper.toResponse(savedReview);
        response.setUsername(username);

        return response;
    }

    private void updateMovieRating(UUID movieId) {
        // 1. Lấy phim ra
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new BusinessException(MessageCode.MOVIE_NOT_FOUND));

        // 2. Gọi DB tính toán lại thông số mới nhất
        Integer totalReviews = reviewRepository.countByMovieId(movieId);
        Double averageRating = reviewRepository.getAverageRatingByMovieId(movieId);

        // Làm tròn đến 1 chữ số thập phân (Ví dụ: 4.56 -> 4.6)
        averageRating = (double) Math.round(averageRating * 10) / 10;

        // 3. Cập nhật lại vào Entity Movie
        movie.setTotalReviews(totalReviews);
        movie.setAverageRating(averageRating);

        // 4. Lưu lại
        movieRepository.save(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByMovieId(UUID movieId, Pageable pageable) {
        // 1. Kiểm tra phim tồn tại (1 câu SQL)
        if (!movieRepository.existsById(movieId)) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }

        // 2. Lấy danh sách phân trang Reviews (1 câu SQL)
        Page<Review> reviews = reviewRepository.findByMovieId(movieId, pageable);

        // Nếu trang hiện tại không có review nào, trả về trang trống luôn, không cần chạy tiếp
        if (reviews.isEmpty()) {
            return Page.empty(pageable);
        }

        // ---- BẮT ĐẦU QUÁ TRÌNH TỐI ƯU BATCHING ----

        // Thu thập tất cả userId và reviewId có trong Page hiện tại (Dùng Set để loại bỏ trùng lặp)
        Set<UUID> userIds = reviews.getContent().stream()
                .map(Review::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<UUID> reviewIds = reviews.getContent().stream()
                .map(Review::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Câu SQL số 2: Lấy tất cả username của các user trong list (Chỉ 1 câu duy nhất)
        Map<UUID, String> userIdToUsernameMap = sysUserRepository.findUsernamesByIds(userIds).stream()
                .collect(Collectors.toMap(
                        user -> user.getId(),
                        user -> user.getUsername()
                ));

        // Câu SQL số 3 (Nếu user đã đăng nhập): Lấy danh sách những reviewId mà user này ĐÃ LIKE
        UUID currentUserId = SecurityUtils.getCurrentUserId();
        Set<UUID> likedReviewIds = new HashSet<>();
        if (currentUserId != null) {
            likedReviewIds = reviewLikeRepository.findLikedReviewIdsByUserIdAndReviewIds(currentUserId, reviewIds);
        }

        // ---- KẾT THÚC QUÁ TRÌNH BATCHING ----
        // Lúc này toàn bộ dữ liệu đã nằm trên RAM (userMap và likedReviewIds)
        // Vòng lặp map dưới đây chạy hoàn toàn trên RAM, tốc độ cực kỳ nhanh (O(1) lookup)
        final Set<UUID> finalLikedReviewIds = likedReviewIds; // Biến final để dùng trong lambda
        return reviews.map(review -> {
            ReviewResponse response = reviewMapper.toResponse(review);

            // Lấy username từ Map trên RAM, nếu không thấy thì để "Unknown"
            String username = userIdToUsernameMap.getOrDefault(review.getUserId(), "Unknown");
            response.setUsername(username);

            // Kiểm tra xem reviewId này có nằm trong danh sách đã Like thu thập ở trên không
            if (currentUserId != null) {
                response.setIsLikedByCurrentUser(finalLikedReviewIds.contains(review.getId()));
            } else {
                response.setIsLikedByCurrentUser(false);
            }

            return response;
        });
    }

    @Override
    @Transactional
    public void toggleLike(UUID reviewId) {
        // 1. Kiểm tra review có tồn tại không (Chỉ cần check exists, không cần lấy nguyên object nặng nề lên)
        if (!reviewRepository.existsById(reviewId)) {
            throw new BusinessException(MessageCode.REVIEW_NOT_FOUND);
        }
        
        UUID userId = SecurityUtils.getCurrentUserId();
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByUserIdAndReviewId(userId, reviewId);
        
        if (existingLike.isPresent()) {
            // Hủy Like
            reviewLikeRepository.delete(existingLike.get());
            reviewRepository.decrementLikeCount(reviewId); // Ép DB tự trừ
        } else {
            // Thêm Like
            ReviewLike newLike = ReviewLike.builder()
                    .userId(userId)
                    .reviewId(reviewId)
                    .build();
            reviewLikeRepository.save(newLike);
            reviewRepository.incrementLikeCount(reviewId); // Ép DB tự cộng
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MovieRatingSummaryResponse getMovieRatingSummary(UUID movieId) {
        // 1. Kiểm tra phim tồn tại
        if (!movieRepository.existsById(movieId)) {
            throw new BusinessException(MessageCode.MOVIE_NOT_FOUND);
        }

        // 2. Lấy tổng số đánh giá và điểm trung bình
        Integer totalReviews = reviewRepository.countByMovieId(movieId);
        Double averageRating = reviewRepository.getAverageRatingByMovieId(movieId);
        averageRating = averageRating != null ? Math.round(averageRating * 10.0) / 10.0 : 0.0; // Làm tròn đến 1 chữ số thập phân

        // 3. Lấy phân bố điểm đánh giá (rating distribution)
        List<RatingCountDTO> ratingDistribution = reviewRepository.getRatingDistributionByMovieId(movieId);

        // 4. Build Response trả về UI
        return MovieRatingSummaryResponse.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .distribution(ratingDistribution)
                .build();
    }
}
