package dev.vj.movie_web;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
//sets all getter setter from lombok dependencies
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    //Since id is Auto-Generated it cannot be done by lombok we have to use geters and setters
    @Id
    private ObjectId id;
    private String body;

    public Review(String body) {
        this.body = body;
    }
}
