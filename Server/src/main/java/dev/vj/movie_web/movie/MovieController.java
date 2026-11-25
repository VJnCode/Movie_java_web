package dev.vj.movie_web.movie;

import dev.vj.movie_web.common.PageResponse;
import dev.vj.movie_web.common.Validation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<PageResponse<MovieResponse>> getMovies(
            @RequestParam(defaultValue = "0") @Min(0) @Max(10_000) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .body(movieService.findAll(page, size));
    }

    @GetMapping("/{imdbId}")
    public ResponseEntity<MovieResponse> getMovie(@PathVariable @Pattern(regexp = Validation.IMDB_ID_REGEX) String imdbId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(movieService.findByImdbId(imdbId));
    }
}
