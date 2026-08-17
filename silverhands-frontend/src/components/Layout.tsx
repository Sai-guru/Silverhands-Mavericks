import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/AuthContext'
import { resourcesForRole } from '../resources'
import type { AppRole } from '../api/types'

interface LayoutProps {
  role: AppRole
}

export function Layout({ role }: LayoutProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()

  const [theme, setTheme] = useState(
  () => localStorage.getItem('theme') || 'light',
)

  const links = resourcesForRole(role)
  const base = role === 'CUSTOMER' ? '/customer' : '/provider'

  async function onLogout() {
    await logout()
    navigate('/', { replace: true })
  }

  function changeLanguage(language: string) {
    void i18n.changeLanguage(language)
  }

  return (
    <div className="shell">
      <aside>
        <p className="brand">SilverHands</p>

        <p className="muted small">
          {role === 'CUSTOMER'
            ? t('common.customer')
            : t('common.serviceProvider')}
        </p>

        <nav>
          <NavLink to={base} end>
            {t('common.home')}
          </NavLink>

          <NavLink to={`${base}/ai`}>
            {t('aiChat.title')}
          </NavLink>

          <NavLink to={`${base}/chat`}>
            {t('common.chat')}
          </NavLink>

          {links.map((link) => (
            <NavLink key={link.key} to={`${base}/${link.path}`}>
              {t(link.title)}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="who">
            {user?.profileImageUrl ? (
              <img
                src={user.profileImageUrl}
                alt=""
                width={32}
                height={32}
              />
            ) : null}

            <div>
              <strong>{user?.name}</strong>
              <p className="muted small">{user?.email}</p>
            </div>
          </div>

          <div className="row-actions">
            {/* Language */}
            <select
              value={i18n.language}
              onChange={(event) => changeLanguage(event.target.value)}
              aria-label={t('common.language')}
            >
              <option value="en">English</option>
              <option value="ta">தமிழ்</option>
              <option value="hi">हिन्दी</option>
            </select>

            {/* Theme */}
<button
  type="button"
  className="ghost"
  onClick={() => {
    const nextTheme = theme === 'dark' ? 'light' : 'dark'

    setTheme(nextTheme)
    document.documentElement.setAttribute('data-theme', nextTheme)
    localStorage.setItem('theme', nextTheme)
  }}
>
  {theme === 'dark' ? '☀️' : '🌙'}
</button>

            <button
              type="button"
              className="ghost"
              onClick={() => void onLogout()}
            >
              {t('common.logout')}
            </button>
          </div>
        </header>

        <Outlet />
      </div>
    </div>
  )
}