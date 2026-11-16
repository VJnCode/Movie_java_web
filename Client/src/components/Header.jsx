import Container from 'react-bootstrap/Container'
import Nav from 'react-bootstrap/Nav'
import Navbar from 'react-bootstrap/Navbar'
import { Link, NavLink } from 'react-router-dom'
import { LogoIcon } from './Icons'

export default function Header() {
  return (
    <Navbar expand="md" variant="dark" className="app-header" sticky="top">
      <Container fluid="xl">
        <Navbar.Brand as={Link} to="/" className="brand">
          <LogoIcon className="brand-icon" />
          <span>Cine<strong>Gold</strong></span>
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="main-nav" />
        <Navbar.Collapse id="main-nav">
          <Nav className="ms-auto">
            <Nav.Link as={NavLink} to="/" end>Home</Nav.Link>
            <Nav.Link href="/#all-movies">All movies</Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  )
}
