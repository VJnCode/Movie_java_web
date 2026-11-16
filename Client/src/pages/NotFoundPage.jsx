import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="page-message">
      <h1>Page not found</h1>
      <p>The page you&apos;re looking for doesn&apos;t exist or has moved.</p>
      <Link to="/" className="btn btn-warning">Back to movies</Link>
    </div>
  )
}
