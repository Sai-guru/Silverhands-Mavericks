import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { INTENDED_ROLE_KEY } from '../api/config'
import type { AppRole } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { homePathForRole } from '../auth/paths'

function isAppRole(value: string | null): value is AppRole {
  return value === 'CUSTOMER' || value === 'PROVIDER'
}

export function AuthCallbackPage() {
  const { chooseRole, refresh } = useAuth()
  const [error, setError] = useState<string | null>(null)
  const [doneRole, setDoneRole] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function finish() {
      const me = await refresh()
      if (cancelled) {
        return
      }
      if (!me) {
        setError('Google sign-in did not create a session. Try again.')
        return
      }
      if (me.roleSelectionRequired) {
        const intended = sessionStorage.getItem(INTENDED_ROLE_KEY)
        if (!isAppRole(intended)) {
          setError('Choose customer or service provider on the login page, then continue with Google.')
          return
        }
        try {
          const updated = await chooseRole(intended)
          if (!cancelled) {
            setDoneRole(updated.role)
          }
        } catch (err) {
          if (!cancelled) {
            setError(err instanceof Error ? err.message : 'Could not set role')
          }
        }
        return
      }
      setDoneRole(me.role)
    }

    void finish()

    return () => {
      cancelled = true
    }
  }, [chooseRole, refresh])

  if (error) {
    return (
      <main className="auth">
        <div className="card">
          <h1>Could not finish sign-in</h1>
          <p className="banner error">{error}</p>
          <a href="/">Back to login</a>
        </div>
      </main>
    )
  }

  if (doneRole === 'CUSTOMER' || doneRole === 'PROVIDER') {
    return <Navigate to={homePathForRole(doneRole)} replace />
  }

  return <p className="center muted">Finishing Google sign-in…</p>
}
