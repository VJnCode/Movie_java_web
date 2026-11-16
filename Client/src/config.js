// Empty string = same origin (dev proxy or reverse proxy in production).
const rawApiUrl = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/+$/, '')

if (rawApiUrl && !/^https?:\/\//.test(rawApiUrl)) {
  throw new Error('VITE_API_URL must be an absolute http(s) URL')
}

export const config = Object.freeze({
  apiUrl: rawApiUrl,
  requestTimeoutMs: Number(import.meta.env.VITE_API_TIMEOUT_MS) || 6000,
  /** After the API fails, serve the offline catalogue for this long before probing the API again. */
  offlineRetryMs: 30_000,
  reviewMaxLength: 2000,
})
