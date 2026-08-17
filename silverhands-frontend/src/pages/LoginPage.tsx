import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { GOOGLE_LOGIN_URL, INTENDED_ROLE_KEY } from '../api/config'
import type { AppRole } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { homePathForRole } from '../auth/paths'

export function LoginPage() {
  const { user, loading } = useAuth()
  const { t, i18n } = useTranslation()
  const [role, setRole] = useState<AppRole | null>(null)

  if (loading) {
    return <p className="center muted">{t('common.checkingSession')}</p>
  }

  if (
    user &&
    !user.roleSelectionRequired &&
    (user.role === 'CUSTOMER' || user.role === 'PROVIDER')
  ) {
    return <Navigate to={homePathForRole(user.role)} replace />
  }

  function continueWithGoogle() {
    if (!role) {
      return
    }

    sessionStorage.setItem(INTENDED_ROLE_KEY, role)
    window.location.assign(GOOGLE_LOGIN_URL)
  }

  return (
    <main className="auth">
      <div className="card">
        <div className="language-selector">
          {/* <select
            value={i18n.language}
            onChange={(event) => void i18n.changeLanguage(event.target.value)}
            aria-label={t('common.language')}
          >
            <option value="en">English</option>
            <option value="ta">தமிழ்</option>
            <option value="hi">हिन्दी</option>
          </select> */}
        </div>

        <p className="eyebrow">SilverHands</p>

        <h1>{t('login.title')}</h1>

        <p className="muted">
          {t('login.description')}
        </p>

        <div className="choices">
          <button
            type="button"
            className={role === 'CUSTOMER' ? 'choice selected' : 'choice'}
            onClick={() => setRole('CUSTOMER')}
          >
            <strong>{t('login.customer')}</strong>
            <span>{t('login.customerDescription')}</span>
          </button>

          <button
            type="button"
            className={role === 'PROVIDER' ? 'choice selected' : 'choice'}
            onClick={() => setRole('PROVIDER')}
          >
            <strong>{t('login.serviceProvider')}</strong>
            <span>{t('login.serviceProviderDescription')}</span>
          </button>
        </div>

        <button
          type="button"
          disabled={!role}
          onClick={continueWithGoogle}
        >
          {t('login.continueWithGoogle')}
        </button>
      </div>
    </main>
  )
}