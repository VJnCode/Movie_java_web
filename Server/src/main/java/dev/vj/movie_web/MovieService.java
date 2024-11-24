package dev.vj.movie_web;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    //it will let framework know to instanciate this class for us
    @Autowired
    private MovieRepository movieRepository;
    public List<Movie> allMovies(){
        //Described in mongo repository
        return movieRepository.findAll();
    }

    public Optional<Movie>  singleMovie(String imdbId){
        return movieRepository.findMovieByImdbId(imdbId);
    }
}
