import { Link, useParams } from 'react-router-dom'
import Badge from 'react-bootstrap/Badge'
import Loading from '../components/Loading'
import Poster from '../components/Poster'
import ReviewForm from '../components/ReviewForm'
import { BackIcon, PlayIcon } from '../components/Icons'
import { addReview, getMovie, NotFoundError } from '../services/movieService'
import { useAsync } from '../utils/useAsync'
import NotFoundPage from './NotFoundPage'

const dateFmt = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' })

function formatDate(iso) {
  const d = iso ? new Date(iso) : null
  return d && !Number.isNaN(d.getTime()) ? dateFmt.format(d) : null
}

export default function MoviePage() {
  const { imdbId } = useParams()
  const [{ loading, data, error }, setState] = useAsync((signal) => getMovie(imdbId, { signal }), [imdbId])

  if (loading) return <Loading label="Loading movie…" />
  if (error instanceof NotFoundError) return <NotFoundPage />
  if (error) {
    return (
      <div className="page-message" role="alert">
        <h1>We couldn&apos;t load this movie</h1>
        <p>{error.message}</p>
      </div>
    )
  }

  const { movie } = data

  async function handleReview(text) {
    const saved = await addReview(movie.imdbId, text)
    setState((s) => ({ ...s, data: { ...s.data, movie: { ...s.data.movie, reviews: [...s.data.movie.reviews, saved] } } }))
    return saved
  }

  return (
    <article className="movie-page">
      <div className="movie-page-backdrop" aria-hidden="true">
        <Poster src={movie.backdrop} alt="" className="hero-backdrop" eager />
        <div className="hero-scrim" />
      </div>

      <div className="container-xl movie-page-inner">
        <Link to="/" className="back-link"><BackIcon /> All movies</Link>

        <div className="movie-page-grid">
          <Poster src={movie.poster} alt={`${movie.title} poster`} className="movie-page-poster" eager />

          <div>
            <h1 className="movie-page-title">{movie.title}</h1>
            <p className="hero-meta">
              {formatDate(movie.releaseDate) ?? movie.releaseDate}
            </p>
            <div className="genre-list">
              {movie.genres.map((g) => <Badge key={g} bg="secondary">{g}</Badge>)}
            </div>
            {movie.overview && <p className="movie-page-overview">{movie.overview}</p>}
            {movie.trailerId && (
              <Link className="btn btn-warning" to={`/trailer/${movie.trailerId}`}>
                <PlayIcon /> Watch trailer
              </Link>
            )}

            <section className="reviews" aria-labelledby="reviews-title">
              <h2 id="reviews-title" className="section-title">
                Reviews <span className="count">({movie.reviews.length})</span>
              </h2>
              <ReviewForm onSubmit={handleReview} />
              {movie.reviews.length === 0 ? (
                <p className="empty-state">No reviews yet - be the first.</p>
              ) : (
                <ul className="review-list">
                  {[...movie.reviews].reverse().map((r) => (
                    <li key={r.id} className="review">
                      {/* Rendered as text by React: user content is never injected as HTML. */}
                      <p className="review-body">{r.body}</p>
                      <p className="review-meta">
                        {formatDate(r.createdAt)}
                        {r.pending && <Badge bg="warning" text="dark" className="ms-2">Pending sync</Badge>}
                      </p>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        </div>
      </div>
    </article>
  )
}
