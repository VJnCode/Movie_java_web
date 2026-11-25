import Spinner from 'react-bootstrap/Spinner'

export default function Loading({ label = 'Loading…' }) {
  return (
    <div className="loading" role="status" aria-live="polite">
      <Spinner animation="border" variant="warning" />
      <span>{label}</span>
    </div>
  )
}
