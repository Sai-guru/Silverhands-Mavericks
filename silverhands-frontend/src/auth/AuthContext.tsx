import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api, logoutRequest } from '../api/http'
import { INTENDED_ROLE_KEY } from '../api/config'
import type { AppRole, CurrentUser } from '../api/types'

interface AuthState {
  user: CurrentUser | null
  loading: boolean
  refresh: () => Promise<CurrentUser | null>
  chooseRole: (role: AppRole) => Promise<CurrentUser>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null)
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    try {
      const me = await api<CurrentUser>('/api/me')
      setUser(me)
      return me
    } catch {
      setUser(null)
      return null
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const chooseRole = useCallback(async (role: AppRole) => {
    const me = await api<CurrentUser>('/api/me/role', {
      method: 'POST',
      body: JSON.stringify({ role }),
    })
    sessionStorage.removeItem(INTENDED_ROLE_KEY)
    setUser(me)
    return me
  }, [])

  const logout = useCallback(async () => {
    await logoutRequest()
    sessionStorage.removeItem(INTENDED_ROLE_KEY)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      loading,
      refresh,
      chooseRole,
      logout,
    }),
    [user, loading, refresh, chooseRole, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthState {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
