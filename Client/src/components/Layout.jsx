import { Outlet } from 'react-router-dom'
import Header from './Header'
import Footer from './Footer'
import StatusBanner from './StatusBanner'

export default function Layout() {
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main">Skip to content</a>
      <Header />
      <StatusBanner />
      <main id="main" className="app-main" tabIndex={-1}>
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
