import { Navigate, useParams } from 'react-router-dom'
import { ResourceCrud } from '../components/ResourceCrud'
import { findResource } from '../resources'
import type { AppRole } from '../api/types'

interface ResourcePageProps {
  role: AppRole
}

export function ResourcePage({ role }: ResourcePageProps) {
  const { resourcePath } = useParams()
  const resource = resourcePath ? findResource(role, resourcePath) : undefined
  if (!resource) {
    return <Navigate to={role === 'CUSTOMER' ? '/customer' : '/provider'} replace />
  }
  return <ResourceCrud resource={resource} />
}
