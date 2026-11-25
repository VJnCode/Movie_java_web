package dev.vj.movie_web.common;

public final class Validation {

    /** IMDb title identifiers: "tt" followed by 7-10 digits. Rejects anything that could be used for injection. */
    public static final String IMDB_ID_REGEX = "^tt\\d{7,10}$";

    public static final int REVIEW_MAX_LENGTH = 2000;

    private Validation() {
    }
}
