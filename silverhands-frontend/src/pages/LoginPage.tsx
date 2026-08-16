import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { GOOGLE_LOGIN_URL, INTENDED_ROLE_KEY } from '../api/config'
import type { AppRole } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { homePathForRole } from '../auth/paths'

export function LoginPage() {
  const { user, loading } = useAuth()
  const [role, setRole] = useState<AppRole | null>(null)

  if (loading) {
    return <p className="center muted">Checking session…</p>
  }

  if (user && !user.roleSelectionRequired && (user.role === 'CUSTOMER' || user.role === 'PROVIDER')) {
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
        <p className="eyebrow">SilverHands</p>
        <h1>Enter as a customer or a service provider</h1>
        <p className="muted">
          Choose how you want to use the app. Continue with Google stays off until one option is
          selected.
        </p>

        <div className="choices">
          <button
            type="button"
            className={role === 'CUSTOMER' ? 'choice selected' : 'choice'}
            onClick={() => setRole('CUSTOMER')}
          >
            <strong>Customer</strong>
            <span>Find services, save them, leave reviews</span>
          </button>
          <button
            type="button"
            className={role === 'PROVIDER' ? 'choice selected' : 'choice'}
            onClick={() => setRole('PROVIDER')}
          >
            <strong>Service provider</strong>
            <span>List your services and products</span>
          </button>
        </div>

        <button type="button" disabled={!role} onClick={continueWithGoogle}>
          Continue with Google
        </button>
      </div>
    </main>
  )
}
