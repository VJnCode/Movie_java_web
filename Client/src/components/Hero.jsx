import Carousel from 'react-bootstrap/Carousel'
import { Link } from 'react-router-dom'
import Poster from './Poster'
import { PlayIcon } from './Icons'

export default function Hero({ movies }) {
  if (!movies.length) return null
  return (
    <section className="hero" aria-label="Featured movies">
      <Carousel fade interval={7000} pause="hover" indicators={movies.length > 1} controls={movies.length > 1}>
        {movies.map((movie, i) => (
          <Carousel.Item key={movie.imdbId}>
            <div className="hero-slide">
              <Poster src={movie.backdrop} alt="" className="hero-backdrop" eager={i === 0} />
              <div className="hero-scrim" />
              <div className="hero-content">
                <Poster src={movie.poster} alt={`${movie.title} poster`} className="hero-poster" eager={i === 0} />
                <div className="hero-text">
                  <p className="hero-meta">
                    {movie.releaseDate?.slice(0, 4)} {movie.genres.length > 0 && `· ${movie.genres.slice(0, 3).join(' / ')}`}
                  </p>
                  <h2 className="hero-title">{movie.title}</h2>
                  {movie.overview && <p className="hero-overview">{movie.overview}</p>}
                  <div className="hero-actions">
                    {movie.trailerId && (
                      <Link className="btn btn-warning btn-lg" to={`/trailer/${movie.trailerId}`}>
                        <PlayIcon /> Watch trailer
                      </Link>
                    )}
                    <Link className="btn btn-outline-light btn-lg" to={`/movies/${movie.imdbId}`}>
                      Reviews
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </Carousel.Item>
        ))}
      </Carousel>
    </section>
  )
}
