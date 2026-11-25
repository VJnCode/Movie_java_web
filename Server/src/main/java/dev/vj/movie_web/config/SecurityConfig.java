package dev.vj.movie_web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Stateless JSON API without cookies or sessions: CSRF tokens add nothing here.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/movies", "/api/v1/movies/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Deny-by-default: anything not explicitly listed is rejected.
                        .anyRequest().denyAll())
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(Duration.ofDays(365).toSeconds()))
                        .frameOptions(f -> f.deny())
                        .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()"))
                        .addHeaderWriter(new StaticHeadersWriter("Cross-Origin-Resource-Policy", "cross-origin")));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(AppProperties props) {
        CorsConfiguration config = new CorsConfiguration();
        // Exact origins only - never "*" - configured per environment.
        config.setAllowedOrigins(props.cors().allowedOrigins().stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Retry-After"));
        config.setAllowCredentials(false);
        config.setMaxAge(Duration.ofHours(1));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
