package dev.vj.movie_web.movie;

import dev.vj.movie_web.review.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

/** Persistence model. Never returned directly from controllers - see {@link MovieResponse}. */
@Document(collection = "Movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String imdbId;
    private String title;
    private String releaseDate;
    private String trailerLink;
    private String poster;
    private String overview;
    private List<String> genres;
    private List<String> backdrops;

    /** Manual references to the reviews collection, loaded lazily so list endpoints don't fetch them. */
    @DocumentReference(lazy = true)
    private List<Review> reviewIds;
}
