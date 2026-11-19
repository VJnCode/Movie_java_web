package dev.vj.movie_web;

import dev.vj.movie_web.common.GlobalExceptionHandler;
import dev.vj.movie_web.common.PageResponse;
import dev.vj.movie_web.common.ResourceNotFoundException;
import dev.vj.movie_web.config.AppProperties;
import dev.vj.movie_web.config.RateLimitFilter;
import dev.vj.movie_web.config.SecurityConfig;
import dev.vj.movie_web.movie.MovieController;
import dev.vj.movie_web.movie.MovieResponse;
import dev.vj.movie_web.movie.MovieService;
import dev.vj.movie_web.review.ReviewController;
import dev.vj.movie_web.review.ReviewResponse;
import dev.vj.movie_web.review.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MovieController.class, ReviewController.class})
@Import({SecurityConfig.class, RateLimitFilter.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {
        "app.cors.allowed-origins=https://app.example.com",
        "app.rate-limit.write-requests-per-minute=3",
        "app.rate-limit.read-requests-per-minute=1000",
        "spring.jackson.deserialization.fail-on-unknown-properties=true"
})
class ApiSecurityTest {

    private static final String VALID_ID = "tt3915174";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    MovieService movieService;

    @MockitoBean
    ReviewService reviewService;

    private MovieResponse movie() {
        return new MovieResponse(VALID_ID, "Puss in Boots", "2022-12-21", "https://www.youtube.com/watch?v=tHb7WlgyaUc",
                "p.jpg", null, List.of("Animation"), List.of("b.jpg"), List.of());
    }

    @Test
    void listsMoviesWithSecurityHeaders() throws Exception {
        when(movieService.findAll(anyInt(), anyInt())).thenReturn(new PageResponse<>(List.of(movie()), 0, 50, 1, 1));

        mvc.perform(get("/api/v1/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].imdbId").value(VALID_ID))
                .andExpect(jsonPath("$.content[0].id").doesNotExist())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void rejectsOversizedPage() throws Exception {
        mvc.perform(get("/api/v1/movies").param("size", "100000")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMalformedImdbIdBeforeHittingDatabase() throws Exception {
        mvc.perform(get("/api/v1/movies/{id}", "{\"$ne\":null}")).andExpect(status().isBadRequest());
        verify(movieService, never()).findByImdbId(any());
    }

    @Test
    void unknownMovieReturns404ProblemJson() throws Exception {
        when(movieService.findByImdbId("tt0000001")).thenThrow(new ResourceNotFoundException("Movie not found."));
        mvc.perform(get("/api/v1/movies/tt0000001"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void internalErrorsDoNotLeakDetails() throws Exception {
        when(movieService.findByImdbId(VALID_ID)).thenThrow(new IllegalStateException("mongodb+srv://secret@host"));
        mvc.perform(get("/api/v1/movies/" + VALID_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(not(containsString("secret"))));
    }

    @Test
    void createsValidReview() throws Exception {
        when(reviewService.createReview(any())).thenReturn(new ReviewResponse("abc", "Great film", Instant.EPOCH));
        mvc.perform(post("/api/v1/reviews").with(r -> { r.setRemoteAddr("10.0.0.1"); return r; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"imdbId\":\"" + VALID_ID + "\",\"reviewBody\":\"Great film\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.body").value("Great film"));
    }

    @Test
    void rejectsInvalidReviewPayloads() throws Exception {
        String[] bad = {
                "{\"imdbId\":\"" + VALID_ID + "\",\"reviewBody\":\"\"}",
                "{\"imdbId\":{\"$gt\":\"\"},\"reviewBody\":\"hi there\"}",
                "{\"imdbId\":\"" + VALID_ID + "\",\"reviewBody\":\"" + "x".repeat(2001) + "\"}",
                "{\"imdbId\":\"" + VALID_ID + "\",\"reviewBody\":\"ok ok\",\"id\":\"injected\"}"
        };
        int ip = 20;
        for (String body : bad) {
            String addr = "10.0.0." + ip++;
            mvc.perform(post("/api/v1/reviews").with(r -> { r.setRemoteAddr(addr); return r; })
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verify(reviewService, never()).createReview(any());
    }

    @Test
    void rateLimitsReviewSubmissions() throws Exception {
        when(reviewService.createReview(any())).thenReturn(new ReviewResponse("abc", "Great film", Instant.EPOCH));
        String body = "{\"imdbId\":\"" + VALID_ID + "\",\"reviewBody\":\"Great film\"}";
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/v1/reviews").with(r -> { r.setRemoteAddr("10.9.9.9"); return r; })
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        }
        mvc.perform(post("/api/v1/reviews").with(r -> { r.setRemoteAddr("10.9.9.9"); return r; })
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void corsAllowsOnlyConfiguredOrigin() throws Exception {
        mvc.perform(options("/api/v1/movies").header("Origin", "https://app.example.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://app.example.com"));

        mvc.perform(options("/api/v1/movies").header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unlistedEndpointsAndMethodsAreDenied() throws Exception {
        mvc.perform(delete("/api/v1/movies/" + VALID_ID)).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/env")).andExpect(status().isForbidden());
        verify(movieService, never()).findByImdbId(eq(VALID_ID));
    }
}
