package dev.vj.movie_web;

import dev.vj.movie_web.movie.MovieRepository;
import dev.vj.movie_web.review.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Full stack against a real (embedded) MongoDB: seeding, paging, review linking. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "de.flapdoodle.mongodb.embedded.version=7.0.14",
        "spring.data.mongodb.uri=",
        "app.seed.enabled=true",
        "app.rate-limit.enabled=false"
})
class MovieApiIntegrationTest {

    private static final String ID = "tt3915174";

    @Autowired
    MockMvc mvc;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Test
    void seedsCatalogueAndLinksReviewsToMovies() throws Exception {
        assertThat(movieRepository.count()).isEqualTo(9);

        mvc.perform(get("/api/v1/movies").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(9))
                .andExpect(jsonPath("$.content[0].reviews").doesNotExist());

        mvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imdbId\":\"" + ID + "\",\"reviewBody\":\"  Purr-fect sequel <script>alert(1)</script> \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.body").value("Purr-fect sequel <script>alert(1)</script>"));

        mvc.perform(get("/api/v1/movies/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Puss in Boots: The Last Wish"))
                .andExpect(jsonPath("$.reviews", hasSize(1)))
                .andExpect(jsonPath("$.reviews[0].body").value("Purr-fect sequel <script>alert(1)</script>"))
                .andExpect(jsonPath("$.reviews[0].createdAt").isString());
    }

    @Test
    void reviewForUnknownMovieIsRejectedWithoutOrphans() throws Exception {
        long before = reviewRepository.count();
        mvc.perform(post("/api/v1/reviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imdbId\":\"tt0000001\",\"reviewBody\":\"hello there\"}"))
                .andExpect(status().isNotFound());
        assertThat(reviewRepository.count()).isEqualTo(before);
    }

    @Test
    void healthEndpointIsPublicButTerse() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }
}
