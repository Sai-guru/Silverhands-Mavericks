export function homePathForRole(role: string | null | undefined): string {
  if (role === 'PROVIDER') {
    return '/provider'
  }
  if (role === 'CUSTOMER') {
    return '/customer'
  }
  return '/'
}
