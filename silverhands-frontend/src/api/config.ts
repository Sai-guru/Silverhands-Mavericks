export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8000'

// /oauth2/authorization/google redirects to /auth/google on the new Express backend
export const GOOGLE_LOGIN_URL = `${API_URL}/oauth2/authorization/google`

export const INTENDED_ROLE_KEY = 'silverhands.intendedRole'