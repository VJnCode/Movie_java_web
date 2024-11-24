package dev.vj.movie_web;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//Repository comes with buit in method to search in ID
@Repository
public interface MovieRepository extends MongoRepository<Movie , ObjectId> {

    //implementing method to search with  imdbId using automatic queries
    Optional<Movie> findMovieByImdbId(String imdbId);

}