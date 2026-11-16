import { config } from '../config'
import { isImdbId } from '../utils/validation'

/**
 * Reviews written while the API is unreachable are kept in this browser and
 * submitted automatically once the API is back. Storage is treated as untrusted input.
 */
const KEY = 'cinegold.pendingReviews.v1'
const MAX_PENDING = 50

function readAll() {
  try {
    const parsed = JSON.parse(localStorage.getItem(KEY) || '[]')
    if (!Array.isArray(parsed)) return []
    return parsed.filter(
      (r) =>
        r &&
        isImdbId(r.imdbId) &&
        typeof r.body === 'string' &&
        r.body.length > 0 &&
        r.body.length <= config.reviewMaxLength &&
        typeof r.id === 'string',
    )
  } catch {
    return []
  }
}

function writeAll(list) {
  try {
    localStorage.setItem(KEY, JSON.stringify(list.slice(-MAX_PENDING)))
    return true
  } catch {
    return false
  }
}

export const localReviews = {
  forMovie: (imdbId) =>
    readAll()
      .filter((r) => r.imdbId === imdbId)
      .map((r) => ({ id: r.id, body: r.body, createdAt: r.createdAt, pending: true })),

  add(imdbId, body) {
    const review = {
      id: `local-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`,
      imdbId,
      body,
      createdAt: new Date().toISOString(),
    }
    const saved = writeAll([...readAll(), review])
    return { id: review.id, body, createdAt: review.createdAt, pending: true, saved }
  },

  all: readAll,

  remove(id) {
    writeAll(readAll().filter((r) => r.id !== id))
  },
}
