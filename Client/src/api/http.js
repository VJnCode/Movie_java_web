import { config } from '../config'

/** The API could not be reached (network failure, timeout, CORS, 5xx, or a non-JSON response). */
export class ServiceUnavailableError extends Error {
  constructor(message, cause) {
    super(message)
    this.name = 'ServiceUnavailableError'
    this.cause = cause
  }
}

/** The API answered with a client error (4xx). */
export class ApiError extends Error {
  constructor(status, problem) {
    super(problem?.detail || `Request failed with status ${status}`)
    this.name = 'ApiError'
    this.status = status
    this.problem = problem
  }
}

/**
 * Minimal fetch wrapper: timeout, JSON-only, no credentials sent cross-origin,
 * and a strict split between "server unavailable" and "request rejected".
 */
export async function request(path, { method = 'GET', body, signal } = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(new DOMException('Timeout', 'TimeoutError')), config.requestTimeoutMs)
  const onAbort = () => controller.abort(signal.reason)
  signal?.addEventListener('abort', onAbort, { once: true })

  let response
  try {
    response = await fetch(`${config.apiUrl}${path}`, {
      method,
      headers: { Accept: 'application/json', ...(body ? { 'Content-Type': 'application/json' } : {}) },
      body: body ? JSON.stringify(body) : undefined,
      credentials: 'omit',
      mode: 'cors',
      redirect: 'error',
      referrerPolicy: 'no-referrer',
      signal: controller.signal,
    })
  } catch (err) {
    if (signal?.aborted) throw err
    throw new ServiceUnavailableError('The server could not be reached.', err)
  } finally {
    clearTimeout(timer)
    signal?.removeEventListener('abort', onAbort)
  }

  const contentType = response.headers.get('content-type') || ''
  const isJson = /application\/(problem\+)?json/.test(contentType)

  if (response.status >= 500 || (response.ok && !isJson)) {
    // 5xx, or an HTML page from a proxy/static host: treat as unavailable.
    throw new ServiceUnavailableError(`Server responded with ${response.status}.`)
  }
  if (response.status === 429) {
    throw new ApiError(429, { detail: 'Too many requests - please wait a minute and try again.' })
  }
  const data = isJson ? await response.json().catch(() => null) : null
  if (!response.ok) {
    if (response.status === 404 && !isJson) {
      throw new ServiceUnavailableError('API route not found.')
    }
    throw new ApiError(response.status, data)
  }
  return data
}
