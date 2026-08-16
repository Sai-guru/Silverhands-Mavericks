import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { resourcesForRole } from '../resources'
import type { AppRole } from '../api/types'

interface HomePageProps {
  role: AppRole
}

export function HomePage({ role }: HomePageProps) {
  const { user } = useAuth()
  const links = resourcesForRole(role)
  const base = role === 'CUSTOMER' ? '/customer' : '/provider'

  return (
    <section className="panel">
      <h1>Welcome{user?.name ? `, ${user.name}` : ''}</h1>
      <p className="muted">
        Signed in as {role === 'CUSTOMER' ? 'a customer' : 'a service provider'}. Open a page to
        call the matching backend route.
      </p>
      <dl className="meta">
        <div>
          <dt>User id</dt>
          <dd className="mono">{user?.id}</dd>
        </div>
      </dl>
      <ul className="link-grid">
        <li>
          <Link to={`${base}/ai`}>AI chat</Link>
          <span className="muted">POST /api/ai/chat</span>
        </li>
        <li>
          <Link to={`${base}/chat`}>Live chat</Link>
          <span className="muted">WS /ws/chat (STOMP)</span>
        </li>
        {links.map((link) => (
          <li key={link.key}>
            <Link to={`${base}/${link.path}`}>{link.title}</Link>
            <span className="muted">{link.apiPath}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}
