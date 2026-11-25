package dev.vj.movie_web.review;

import java.time.Instant;

public record ReviewResponse(String id, String body, Instant createdAt) {

    public static ReviewResponse from(Review r) {
        return new ReviewResponse(r.getId() == null ? null : r.getId().toHexString(), r.getBody(), r.getCreatedAt());
    }
}
