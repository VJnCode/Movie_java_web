import { Component } from 'react'

export default class ErrorBoundary extends Component {
  state = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error, info) {
    // Hook point for an error-reporting service. Never render error details to users.
    if (import.meta.env.DEV) console.error(error, info)
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="fatal-error" role="alert">
          <h1>Something went wrong</h1>
          <p>Please reload the page. If the problem continues, try again later.</p>
          <button type="button" className="btn btn-warning" onClick={() => window.location.assign('/')}>
            Back to home
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
