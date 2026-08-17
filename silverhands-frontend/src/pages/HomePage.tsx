import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/AuthContext'
import { resourcesForRole } from '../resources'
import type { AppRole } from '../api/types'

interface HomePageProps {
  role: AppRole
}

export function HomePage({ role }: HomePageProps) {
  const { user } = useAuth()
  const { t } = useTranslation()

  const links = resourcesForRole(role)
  const base = role === 'CUSTOMER' ? '/customer' : '/provider'

  return (
    <section className="panel">
      <h1>
        {t('home.welcome')}
        {user?.name ? `, ${user.name}` : ''}
      </h1>

      <p className="muted">
        {t('home.signedInAs')}{' '}
        {role === 'CUSTOMER'
          ? t('home.customer')
          : t('home.serviceProvider')}
        . {t('home.openPage')}
      </p>

      <dl className="meta">
        <div>
          <dt>{t('home.userId')}</dt>
          <dd className="mono">{user?.id}</dd>
        </div>
      </dl>

      <ul className="link-grid">
        <li>
          <Link to={`${base}/ai`}>
            {t('aiChat.title')}
          </Link>
          <span className="muted">POST /api/ai/chat</span>
        </li>

        <li>
          <Link to={`${base}/chat`}>
            {t('common.liveChat')}
          </Link>
          <span className="muted">WS /ws/chat (STOMP)</span>
        </li>

        {links.map((link) => (
          <li key={link.key}>
            <Link to={`${base}/${link.path}`}>
              {t(link.title)}
            </Link>
            <span className="muted">{link.apiPath}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}