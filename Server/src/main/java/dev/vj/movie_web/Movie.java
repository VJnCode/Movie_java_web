package dev.vj.movie_web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import javax.xml.transform.sax.SAXResult;
import java.util.List;

@Document(collection = "Movies")
//sets all getter setter from lombok dependencies
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

    @Id
    private ObjectId id;
    private  String imdbId;
    private  String title;
    private String releaseDate;
    private String trailerLink;
    private String poster;
    private List<String> genres;
    private List<String> backdrops;

    //this will case Db to store iid of review
    //Manual reference relationship
    @DocumentReference
    private List<Review> reviewIds;
}
