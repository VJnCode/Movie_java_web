import { useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Form from 'react-bootstrap/Form'
import { config } from '../config'

const MIN = 2

export default function ReviewForm({ onSubmit }) {
  const [text, setText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const trimmed = text.trim()
  const valid = trimmed.length >= MIN && trimmed.length <= config.reviewMaxLength

  async function handleSubmit(e) {
    e.preventDefault()
    if (!valid || submitting) return
    setSubmitting(true)
    setError(null)
    setNotice(null)
    try {
      const saved = await onSubmit(trimmed)
      setText('')
      setNotice(saved?.pending ? 'Saved on this device - it will be posted when the server is back.' : 'Thanks! Your review was posted.')
    } catch (err) {
      setError(err?.message || 'Could not post your review. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Form onSubmit={handleSubmit} noValidate className="review-form">
      <Form.Group controlId="review-text">
        <Form.Label>Write a review</Form.Label>
        <Form.Control
          as="textarea"
          rows={4}
          value={text}
          maxLength={config.reviewMaxLength}
          onChange={(e) => setText(e.target.value)}
          placeholder="What did you think?"
          aria-describedby="review-help"
          disabled={submitting}
        />
        <Form.Text id="review-help" muted>
          {trimmed.length}/{config.reviewMaxLength} characters
        </Form.Text>
      </Form.Group>
      {error && <Alert variant="danger" className="mt-2 mb-0">{error}</Alert>}
      {notice && <Alert variant="success" className="mt-2 mb-0">{notice}</Alert>}
      <Button type="submit" variant="warning" className="mt-3" disabled={!valid || submitting}>
        {submitting ? 'Posting…' : 'Post review'}
      </Button>
    </Form>
  )
}
