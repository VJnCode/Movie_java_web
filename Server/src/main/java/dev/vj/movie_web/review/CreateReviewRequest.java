package dev.vj.movie_web.review;

import dev.vj.movie_web.common.Validation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Strictly typed request body (unknown JSON fields are rejected globally). */
public record CreateReviewRequest(
        @NotNull @Pattern(regexp = Validation.IMDB_ID_REGEX, message = "must be a valid IMDb id")
        String imdbId,

        @NotBlank @Size(min = 2, max = Validation.REVIEW_MAX_LENGTH)
        String reviewBody) {
}
