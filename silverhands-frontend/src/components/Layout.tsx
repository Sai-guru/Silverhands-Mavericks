import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { resourcesForRole } from '../resources'
import type { AppRole } from '../api/types'

interface LayoutProps {
  role: AppRole
}

export function Layout({ role }: LayoutProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const links = resourcesForRole(role)
  const base = role === 'CUSTOMER' ? '/customer' : '/provider'

  async function onLogout() {
    await logout()
    navigate('/', { replace: true })
  }

  return (
    <div className="shell">
      <aside>
        <p className="brand">SilverHands</p>
        <p className="muted small">{role === 'CUSTOMER' ? 'Customer' : 'Service provider'}</p>
        <nav>
          <NavLink to={base} end>
            Home
          </NavLink>
          <NavLink to={`${base}/ai`}>AI chat</NavLink>
          <NavLink to={`${base}/chat`}>Live chat</NavLink>
          {links.map((link) => (
            <NavLink key={link.key} to={`${base}/${link.path}`}>
              {link.title}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main">
        <header className="topbar">
          <div className="who">
            {user?.profileImageUrl ? (
              <img src={user.profileImageUrl} alt="" width={32} height={32} />
            ) : null}
            <div>
              <strong>{user?.name}</strong>
              <p className="muted small">{user?.email}</p>
            </div>
          </div>
          <button type="button" className="ghost" onClick={() => void onLogout()}>
            Log out
          </button>
        </header>
        <Outlet />
      </div>
    </div>
  )
}
