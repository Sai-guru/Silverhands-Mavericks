import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { homePathForRole } from './paths'
import type { AppRole } from '../api/types'

interface GuardProps {
  role: AppRole
}

export function RequireRole({ role }: GuardProps) {
  const { user, loading } = useAuth()

  if (loading) {
    return <p className="center muted">Loading…</p>
  }

  if (!user) {
    return <Navigate to="/" replace />
  }

  if (user.roleSelectionRequired || user.role === 'ROLE_PENDING') {
    return <Navigate to="/auth/callback" replace />
  }

  if (user.role !== role) {
    return <Navigate to={homePathForRole(user.role)} replace />
  }

  return <Outlet />
}
