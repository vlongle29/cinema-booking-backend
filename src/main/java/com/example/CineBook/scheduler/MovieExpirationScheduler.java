package com.example.CineBook.scheduler;

import com.example.CineBook.common.constant.MovieStatus;
import com.example.CineBook.model.Movie;
import com.example.CineBook.model.Showtime;
import com.example.CineBook.repository.irepository.MovieRepository;
import com.example.CineBook.repository.irepository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MovieExpirationScheduler {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    private static final int DRAFT_EXPIRATION_CHECK_INTERVAL = 60000; // 1 minute

    @Scheduled(fixedRate = DRAFT_EXPIRATION_CHECK_INTERVAL)
    public void updateMovieStatus() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            LocalDate today = LocalDate.now();

            if (movie.getReleaseDate().isAfter(today)) {
                movie.setStatus(MovieStatus.COMING_SOON);
            } else {
                List<Showtime> showtimes = showtimeRepository.findShowtimesByMovieId(movie.getId());
                if (showtimes.isEmpty()) {
                    movie.setStatus(MovieStatus.ENDED);
                } else {
                    boolean allEnded = showtimes.stream()
                            .allMatch(s -> s.getEndTime().isBefore(LocalDateTime.now()));
                    movie.setStatus(allEnded ? MovieStatus.ENDED : MovieStatus.SHOWING);
                }
            }
        }
        movieRepository.saveAll(movies);
    }
}
