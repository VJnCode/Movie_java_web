package dev.vj.movie_web.seed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vj.movie_web.movie.Movie;
import dev.vj.movie_web.movie.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/** Populates an empty catalogue on first start (disable with SEED_DATA=false). */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class CatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;

    public CatalogSeeder(MovieRepository movieRepository, ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.objectMapper = objectMapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (movieRepository.count() > 0) {
                return;
            }
            try (InputStream in = new ClassPathResource("seed/movies.json").getInputStream()) {
                List<Movie> movies = objectMapper.readValue(in, new TypeReference<>() {
                });
                movieRepository.saveAll(movies);
                log.info("Seeded {} movies into an empty catalogue", movies.size());
            }
        } catch (Exception e) {
            // Seeding is best-effort; the API must still start if the database is temporarily unreachable.
            log.warn("Catalogue seeding skipped: {}", e.getMessage());
        }
    }
}
