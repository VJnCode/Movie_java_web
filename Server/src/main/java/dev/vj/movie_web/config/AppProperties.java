package dev.vj.movie_web.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, RateLimit rateLimit, Seed seed) {

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }

    public record RateLimit(
            @DefaultValue("true") boolean enabled,
            @DefaultValue("120") @Min(1) int readRequestsPerMinute,
            @DefaultValue("10") @Min(1) int writeRequestsPerMinute) {
    }

    public record Seed(@DefaultValue("false") boolean enabled) {
    }
}
