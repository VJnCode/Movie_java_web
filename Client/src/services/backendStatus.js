import { useSyncExternalStore } from 'react'
import { config } from '../config'

/**
 * Tiny circuit breaker shared by the whole app.
 *  - 'unknown' : nothing tried yet
 *  - 'online'  : last API call succeeded
 *  - 'offline' : API failed; serve the bundled catalogue until offlineRetryMs elapses
 */
let state = { status: 'unknown', since: Date.now() }
const listeners = new Set()
const onlineListeners = new Set()

function set(status) {
  if (state.status === status) return
  const wasOnline = state.status === 'online'
  state = { status, since: Date.now() }
  listeners.forEach((l) => l())
  if (status === 'online' && !wasOnline) onlineListeners.forEach((l) => l())
}

export const backendStatus = {
  get: () => state,
  markOnline: () => set('online'),
  markOffline: () => set('offline'),
  /** True while the breaker is open: don't wait on timeouts for a server we know is down. */
  shouldSkipApi: () => state.status === 'offline' && Date.now() - state.since < config.offlineRetryMs,
  /** Close the breaker so the next request probes the API again. */
  reset: () => {
    if (state.status === 'offline') {
      state = { status: 'unknown', since: Date.now() }
      listeners.forEach((l) => l())
    }
  },
  subscribe: (l) => {
    listeners.add(l)
    return () => listeners.delete(l)
  },
  onReconnect: (l) => {
    onlineListeners.add(l)
    return () => onlineListeners.delete(l)
  },
}

export function useBackendStatus() {
  return useSyncExternalStore(backendStatus.subscribe, backendStatus.get, backendStatus.get)
}
