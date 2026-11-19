import { useState } from 'react'

/** Image with graceful fallback when the CDN is unreachable or the URL is missing. */
export default function Poster({ src, alt, className = '', eager = false }) {
  const [failed, setFailed] = useState(false)
  if (!src || failed) {
    return (
      <div className={`poster-fallback ${className}`} role="img" aria-label={alt}>
        <span>{alt}</span>
      </div>
    )
  }
  return (
    <img
      src={src}
      alt={alt}
      className={className}
      loading={eager ? 'eager' : 'lazy'}
      decoding="async"
      referrerPolicy="no-referrer"
      onError={() => setFailed(true)}
    />
  )
}
