// Inline SVG icons (no icon-font dependency).
const base = { width: '1em', height: '1em', fill: 'currentColor', 'aria-hidden': true, focusable: false }

export const PlayIcon = (props) => (
  <svg viewBox="0 0 24 24" {...base} {...props}>
    <path d="M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20Zm-2 14.5v-9l7 4.5-7 4.5Z" />
  </svg>
)

export const LogoIcon = (props) => (
  <svg viewBox="0 0 64 64" {...base} {...props}>
    <circle cx="32" cy="32" r="20" fill="none" stroke="currentColor" strokeWidth="6" />
    <path d="M27 22l15 10-15 10z" />
  </svg>
)

export const BackIcon = (props) => (
  <svg viewBox="0 0 24 24" {...base} {...props}>
    <path d="M15.4 5.4 14 4l-8 8 8 8 1.4-1.4L8.8 12z" />
  </svg>
)

export const CloudOffIcon = (props) => (
  <svg viewBox="0 0 24 24" {...base} {...props}>
    <path d="M19.35 10.04A7.49 7.49 0 0 0 12 4c-1.48 0-2.85.43-4.01 1.17l1.46 1.46A5.5 5.5 0 0 1 17.5 11.5v.5H19a3 3 0 0 1 1.31 5.7l1.45 1.45A5 5 0 0 0 19.35 10.04ZM3 5.27l2.75 2.74C2.56 8.15 0 10.77 0 14a6 6 0 0 0 6 6h11.73l2 2L21 20.73 4.27 4 3 5.27ZM7.73 10l8 8H6a4 4 0 0 1 0-8h1.73Z" />
  </svg>
)
