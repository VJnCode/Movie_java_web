import { useNavigate, useParams } from 'react-router-dom'
import { isYouTubeId } from '../utils/validation'
import { BackIcon } from '../components/Icons'
import NotFoundPage from './NotFoundPage'

export default function TrailerPage() {
  const { ytTrailerId } = useParams()
  const navigate = useNavigate()

  // The id goes into an iframe URL, so it must be a strict YouTube id - nothing else.
  if (!isYouTubeId(ytTrailerId)) return <NotFoundPage />

  return (
    <section className="trailer-page" aria-label="Trailer">
      <button type="button" className="back-link btn btn-link" onClick={() => navigate(-1)}>
        <BackIcon /> Back
      </button>
      <div className="trailer-frame">
        <iframe
          src={`https://www.youtube-nocookie.com/embed/${ytTrailerId}?autoplay=1&rel=0&modestbranding=1`}
          title="Movie trailer"
          allow="autoplay; encrypted-media; picture-in-picture; fullscreen"
          allowFullScreen
          referrerPolicy="strict-origin-when-cross-origin"
          sandbox="allow-scripts allow-same-origin allow-presentation allow-popups"
        />
      </div>
    </section>
  )
}
