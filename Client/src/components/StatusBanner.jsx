import { backendStatus, useBackendStatus } from '../services/backendStatus'
import { CloudOffIcon } from './Icons'

export default function StatusBanner() {
  const { status } = useBackendStatus()
  if (status !== 'offline') return null
  return (
    <div className="status-banner" role="status">
      <CloudOffIcon />
      <span>
        Offline mode: our servers are unreachable, so you&apos;re seeing the built-in catalogue. Reviews you write are
        saved on this device and sent automatically when we&apos;re back.
      </span>
      <button type="button" className="btn btn-sm btn-outline-warning" onClick={() => { backendStatus.reset(); window.location.reload() }}>
        Retry
      </button>
    </div>
  )
}
