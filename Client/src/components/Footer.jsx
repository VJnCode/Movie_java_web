export default function Footer() {
  return (
    <footer className="app-footer">
      <p>
        © {new Date().getFullYear()} CineGold. Artwork courtesy of{' '}
        <a href="https://www.themoviedb.org/" target="_blank" rel="noopener noreferrer">TMDB</a>; trailers via YouTube.
        This product uses the TMDB image CDN but is not endorsed or certified by TMDB.
      </p>
    </footer>
  )
}
