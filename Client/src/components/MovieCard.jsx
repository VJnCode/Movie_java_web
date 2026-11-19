import { Link } from 'react-router-dom'
import Poster from './Poster'

export default function MovieCard({ movie }) {
  return (
    <article className="movie-card">
      <Link to={`/movies/${movie.imdbId}`} className="movie-card-link" aria-label={`${movie.title} - details and reviews`}>
        <Poster src={movie.poster} alt="" className="movie-card-poster" />
        <div className="movie-card-body">
          <h3 className="movie-card-title">{movie.title}</h3>
          <p className="movie-card-meta">
            {movie.releaseDate?.slice(0, 4)}
            {movie.genres[0] && ` · ${movie.genres[0]}`}
          </p>
        </div>
      </Link>
    </article>
  )
}
