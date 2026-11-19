import { useEffect, useState } from 'react'

/**
 * Run an abortable async loader whenever deps change.
 * Results are tagged with the deps key, so stale results are never shown as current.
 */
export function useAsync(loader, deps) {
  const key = JSON.stringify(deps)
  const [state, setState] = useState({ key: null, data: null, error: null })

  useEffect(() => {
    const controller = new AbortController()
    loader(controller.signal)
      .then((data) => !controller.signal.aborted && setState({ key, data, error: null }))
      .catch((error) => !controller.signal.aborted && setState({ key, data: null, error }))
    return () => controller.abort()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key])

  const loading = state.key !== key
  return [{ loading, data: loading ? null : state.data, error: loading ? null : state.error }, setState]
}
