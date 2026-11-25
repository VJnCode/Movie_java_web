package dev.vj.movie_web.review;

import dev.vj.movie_web.common.ResourceNotFoundException;
import dev.vj.movie_web.movie.Movie;
import dev.vj.movie_web.movie.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReviewService {

    /** Strip control characters (keeps newline and tab) to avoid log/terminal injection and garbage storage. */
    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\\n\\t]]");

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final MongoTemplate mongoTemplate;
    private final Clock clock = Clock.systemUTC();

    public ReviewResponse createReview(CreateReviewRequest request) {
        String imdbId = request.imdbId();
        if (!movieRepository.existsByImdbId(imdbId)) {
            throw new ResourceNotFoundException("Movie not found.");
        }

        String body = sanitize(request.reviewBody());
        if (body.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text is empty.");
        }
        Review review = reviewRepository.insert(Review.builder()
                .body(body)
                .imdbId(imdbId)
                .createdAt(Instant.now(clock))
                .build());

        var result = mongoTemplate.updateFirst(
                Query.query(Criteria.where("imdbId").is(imdbId)),
                new Update().push("reviewIds", review.getId()),
                Movie.class);

        if (result.getMatchedCount() == 0) {
            // Movie deleted concurrently - don't leave an orphaned review behind.
            reviewRepository.deleteById(review.getId());
            throw new ResourceNotFoundException("Movie not found.");
        }
        return ReviewResponse.from(review);
    }

    static String sanitize(String raw) {
        return CONTROL_CHARS.matcher(raw).replaceAll("").strip();
    }
}
