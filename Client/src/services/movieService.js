import { request, ApiError, ServiceUnavailableError } from '../api/http'
import catalogue from '../data/movies.json'
import { backendStatus } from './backendStatus'
import { localReviews } from './localReviews'
import { normalizeMovie, normalizeReview, isImdbId } from '../utils/validation'

const bundledMovies = catalogue.map(normalizeMovie).filter(Boolean)
// Last list received from the API this session; preferred over the bundled catalogue when the API drops.
let lastKnownMovies = null
const offlineMovies = () => lastKnownMovies ?? bundledMovies

const isUnavailable = (err) => err instanceof ServiceUnavailableError

/** Try the API; if it's down, flip the breaker and serve the bundled catalogue instead. */
async function withFallback(online, offline) {
  if (backendStatus.shouldSkipApi()) return { ...(await offline()), source: 'offline' }
  try {
    const result = await online()
    backendStatus.markOnline()
    return { ...result, source: 'api' }
  } catch (err) {
    if (!isUnavailable(err)) throw err
    backendStatus.markOffline()
    return { ...(await offline()), source: 'offline' }
  }
}

export class NotFoundError extends Error {}

export async function getMovies({ signal } = {}) {
  return withFallback(
    async () => {
      const page = await request('/api/v1/movies?page=0&size=100', { signal })
      const movies = (page?.content ?? []).map(normalizeMovie).filter(Boolean)
      if (movies.length) lastKnownMovies = movies
      return { movies }
    },
    async () => ({ movies: offlineMovies() }),
  )
}

export async function getMovie(imdbId, { signal } = {}) {
  if (!isImdbId(imdbId)) throw new NotFoundError('Invalid movie id')

  const result = await withFallback(
    async () => {
      try {
        return { movie: normalizeMovie(await request(`/api/v1/movies/${imdbId}`, { signal })) }
      } catch (err) {
        if (err instanceof ApiError && err.status === 404) throw new NotFoundError('Movie not found')
        throw err
      }
    },
    async () => {
      const movie =
        offlineMovies().find((m) => m.imdbId === imdbId) ?? bundledMovies.find((m) => m.imdbId === imdbId)
      if (!movie) throw new NotFoundError('Movie not found')
      return { movie: { ...movie, reviews: [] } }
    },
  )
  if (!result.movie) throw new NotFoundError('Movie not found')
  // Reviews written offline (not yet synced) are shown too, flagged as pending.
  result.movie = { ...result.movie, reviews: [...result.movie.reviews, ...localReviews.forMovie(imdbId)] }
  return result
}

/** Returns the saved review. When the API is down the review is queued locally (pending: true). */
export async function addReview(imdbId, text) {
  const body = text.trim()
  if (!backendStatus.shouldSkipApi()) {
    try {
      const saved = await request('/api/v1/reviews', { method: 'POST', body: { imdbId, reviewBody: body } })
      backendStatus.markOnline()
      return normalizeReview(saved)
    } catch (err) {
      if (!isUnavailable(err)) throw err
      backendStatus.markOffline()
    }
  }
  const queued = localReviews.add(imdbId, body)
  if (!queued.saved) throw new Error('Could not save your review offline (browser storage unavailable).')
  return queued
}

let syncing = false
/** Push queued offline reviews once the API is reachable again. */
export async function syncPendingReviews() {
  if (syncing) return
  syncing = true
  try {
    for (const r of localReviews.all()) {
      try {
        await request('/api/v1/reviews', { method: 'POST', body: { imdbId: r.imdbId, reviewBody: r.body } })
        localReviews.remove(r.id)
      } catch (err) {
        if (err instanceof ApiError && err.status !== 429) localReviews.remove(r.id) // permanently rejected
        else break // still offline or rate limited: try again later
      }
    }
  } finally {
    syncing = false
  }
}

backendStatus.onReconnect(() => {
  syncPendingReviews()
})
