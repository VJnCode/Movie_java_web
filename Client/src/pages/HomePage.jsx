import { useDeferredValue, useMemo, useState } from 'react'
import Form from 'react-bootstrap/Form'
import Hero from '../components/Hero'
import MovieCard from '../components/MovieCard'
import Loading from '../components/Loading'
import { getMovies } from '../services/movieService'
import { useAsync } from '../utils/useAsync'

export default function HomePage() {
  const [{ loading, data, error }] = useAsync((signal) => getMovies({ signal }), [])
  const [query, setQuery] = useState('')
  const [genre, setGenre] = useState('')
  const deferredQuery = useDeferredValue(query)

  const movies = useMemo(() => data?.movies ?? [], [data])
  const genres = useMemo(() => [...new Set(movies.flatMap((m) => m.genres))].sort(), [movies])
  const filtered = useMemo(() => {
    const q = deferredQuery.trim().toLowerCase()
    return movies.filter((m) => (!genre || m.genres.includes(genre)) && (!q || m.title.toLowerCase().includes(q)))
  }, [movies, genre, deferredQuery])

  if (loading) return <Loading label="Loading movies…" />
  if (error) {
    return (
      <div className="page-message" role="alert">
        <h1>We couldn&apos;t load movies</h1>
        <p>{error.message}</p>
      </div>
    )
  }

  return (
    <>
      <Hero movies={movies.slice(0, 5)} />

      <section id="all-movies" className="catalogue container-xl" aria-labelledby="catalogue-title">
        <div className="catalogue-head">
          <h1 id="catalogue-title" className="section-title">All movies</h1>
          <div className="catalogue-filters">
            <Form.Control
              type="search"
              placeholder="Search titles"
              aria-label="Search titles"
              value={query}
              maxLength={100}
              onChange={(e) => setQuery(e.target.value)}
            />
            <Form.Select aria-label="Filter by genre" value={genre} onChange={(e) => setGenre(e.target.value)}>
              <option value="">All genres</option>
              {genres.map((g) => (
                <option key={g} value={g}>{g}</option>
              ))}
            </Form.Select>
          </div>
        </div>

        {filtered.length === 0 ? (
          <p className="empty-state">No movies match your search.</p>
        ) : (
          <div className="movie-grid">
            {filtered.map((m) => (
              <MovieCard key={m.imdbId} movie={m} />
            ))}
          </div>
        )}
      </section>
    </>
  )
}
