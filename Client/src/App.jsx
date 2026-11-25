import { lazy, Suspense } from 'react'
import { Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Loading from './components/Loading'
import HomePage from './pages/HomePage'

const MoviePage = lazy(() => import('./pages/MoviePage'))
const TrailerPage = lazy(() => import('./pages/TrailerPage'))
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'))

export default function App() {
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="movies/:imdbId" element={<MoviePage />} />
          <Route path="trailer/:ytTrailerId" element={<TrailerPage />} />
          {/* Backwards-compatible links from the previous version */}
          <Route path="Reviews/:imdbId" element={<MoviePage />} />
          <Route path="reviews/:imdbId" element={<MoviePage />} />
          <Route path="Trailer/:ytTrailerId" element={<TrailerPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </Suspense>
  )
}
