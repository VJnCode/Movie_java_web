package dev.vj.movie_web.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private ObjectId id;
    private String body;
    @Indexed
    private String imdbId;
    private Instant createdAt;
}
