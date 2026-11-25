package dev.vj.movie_web.movie;

import dev.vj.movie_web.common.PageResponse;
import dev.vj.movie_web.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public PageResponse<MovieResponse> findAll(int page, int size) {
        PageRequest request = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate").and(Sort.by("title")));
        return PageResponse.of(movieRepository.findAll(request), MovieResponse::summary);
    }

    public MovieResponse findByImdbId(String imdbId) {
        return movieRepository.findMovieByImdbId(imdbId)
                .map(MovieResponse::detail)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found."));
    }
}
