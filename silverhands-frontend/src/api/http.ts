import { API_URL } from './config'
import type { SpringPage } from './types'

function errorMessage(payload: unknown, fallback: string): string {
  if (payload && typeof payload === 'object') {
    const record = payload as Record<string, unknown>
    if (typeof record.message === 'string' && record.message.length > 0) {
      return record.message
    }
    if (typeof record.detail === 'string' && record.detail.length > 0) {
      return record.detail
    }
  }
  return fallback
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  if (init?.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    credentials: 'include',
    headers,
  })

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  const payload = text.length > 0 ? (JSON.parse(text) as unknown) : null

  if (!response.ok) {
    throw new Error(errorMessage(payload, `${response.status} ${response.statusText}`))
  }

  return payload as T
}

// Newer endpoints return plain JSON arrays instead of Spring pages.
export async function getPage<T>(path: string, size = 100): Promise<SpringPage<T>> {
  const separator = path.includes('?') ? '&' : '?'
  const payload = await api<unknown>(`${path}${separator}size=${size}`)
  if (Array.isArray(payload)) {
    return {
      content: payload as T[],
      totalElements: payload.length,
      totalPages: 1,
      number: 0,
      size: payload.length,
    }
  }
  return payload as SpringPage<T>
}

export function logoutRequest(): Promise<void> {
  return fetch(`${API_URL}/logout`, {
    method: 'POST',
    credentials: 'include',
    redirect: 'manual',
  }).then(() => undefined)
}
