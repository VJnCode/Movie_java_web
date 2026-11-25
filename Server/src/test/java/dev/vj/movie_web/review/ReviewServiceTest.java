package dev.vj.movie_web.review;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewServiceTest {

    @Test
    void sanitizeStripsControlCharactersButKeepsNewlines() {
        assertThat(ReviewService.sanitize("  Great\u0000 movie\r\nloved it\u001b[31m  "))
                .isEqualTo("Great movie\nloved it[31m");
    }
}
