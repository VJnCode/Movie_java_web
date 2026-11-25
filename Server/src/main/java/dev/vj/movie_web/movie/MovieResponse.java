package dev.vj.movie_web.movie;

import dev.vj.movie_web.review.Review;
import dev.vj.movie_web.review.ReviewResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Public API representation of a movie. Internal ids are never exposed. */
public record MovieResponse(
        String imdbId,
        String title,
        String releaseDate,
        String trailerLink,
        String poster,
        String overview,
        List<String> genres,
        List<String> backdrops,
        List<ReviewResponse> reviews) {

    public static MovieResponse summary(Movie m) {
        return new MovieResponse(m.getImdbId(), m.getTitle(), m.getReleaseDate(), m.getTrailerLink(), m.getPoster(),
                m.getOverview(), nullSafe(m.getGenres()), nullSafe(m.getBackdrops()), null);
    }

    public static MovieResponse detail(Movie m) {
        List<ReviewResponse> reviews = nullSafe(m.getReviewIds()).stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
                .map(ReviewResponse::from)
                .toList();
        return new MovieResponse(m.getImdbId(), m.getTitle(), m.getReleaseDate(), m.getTrailerLink(), m.getPoster(),
                m.getOverview(), nullSafe(m.getGenres()), nullSafe(m.getBackdrops()), reviews);
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}
