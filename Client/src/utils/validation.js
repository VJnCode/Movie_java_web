export const IMDB_ID = /^tt\d{7,10}$/
const YOUTUBE_ID = /^[A-Za-z0-9_-]{11}$/

export const isImdbId = (v) => typeof v === 'string' && IMDB_ID.test(v)
export const isYouTubeId = (v) => typeof v === 'string' && YOUTUBE_ID.test(v)

/** Accept only absolute https URLs for images; anything else (javascript:, data:, relative) is dropped. */
export function safeImageUrl(value) {
  if (typeof value !== 'string') return null
  try {
    const url = new URL(value)
    return url.protocol === 'https:' ? url.href : null
  } catch {
    return null
  }
}

/** Extract the 11-char video id from common YouTube URL shapes. Returns null if it isn't one. */
export function youTubeIdFromUrl(link) {
  if (typeof link !== 'string') return null
  try {
    const url = new URL(link)
    const host = url.hostname.replace(/^www\.|^m\./, '')
    let id = null
    if (host === 'youtube.com' || host === 'youtube-nocookie.com') {
      id = url.searchParams.get('v') || url.pathname.match(/^\/(?:embed|shorts)\/([^/?#]+)/)?.[1] || null
    } else if (host === 'youtu.be') {
      id = url.pathname.slice(1)
    }
    return isYouTubeId(id) ? id : null
  } catch {
    return null
  }
}

/** Normalise a movie from any source into the exact shape the UI renders. */
export function normalizeMovie(raw) {
  if (!raw || !isImdbId(raw.imdbId)) return null
  const strings = (arr) => (Array.isArray(arr) ? arr.filter((s) => typeof s === 'string') : [])
  return {
    imdbId: raw.imdbId,
    title: typeof raw.title === 'string' ? raw.title : 'Untitled',
    releaseDate: typeof raw.releaseDate === 'string' ? raw.releaseDate : '',
    overview: typeof raw.overview === 'string' ? raw.overview : '',
    poster: safeImageUrl(raw.poster),
    backdrop: strings(raw.backdrops).map(safeImageUrl).find(Boolean) || safeImageUrl(raw.poster),
    genres: strings(raw.genres),
    trailerId: youTubeIdFromUrl(raw.trailerLink),
    reviews: Array.isArray(raw.reviews) ? raw.reviews.map(normalizeReview).filter(Boolean) : [],
  }
}

export function normalizeReview(raw) {
  if (!raw || typeof raw.body !== 'string') return null
  return {
    id: typeof raw.id === 'string' ? raw.id : `r-${Math.random().toString(36).slice(2)}`,
    body: raw.body,
    createdAt: typeof raw.createdAt === 'string' ? raw.createdAt : null,
    pending: raw.pending === true,
  }
}
