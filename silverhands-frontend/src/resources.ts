import type { AppRole } from './api/types'

export type FieldType = 'text' | 'textarea' | 'number' | 'datetime'

export interface FieldConfig {
  name: string
  label: string
  type: FieldType
  required?: boolean
}

export interface ResourceConfig {
  key: string
  path: string
  title: string
  apiPath: string
  roles: AppRole[]
  writeRoles: AppRole[]
  fields: FieldConfig[]
  listParams?: Record<string, string>
  searchable?: boolean
}

// CUSTOMER resources (read-only discovery)
const customerResources: ResourceConfig[] = [
  {
    key: 'browseProviders',
    path: 'providers',
    title: 'resources.serviceProviders',
    apiPath: '/api/providers',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'resources.name', type: 'text' },
      { name: 'email', label: 'resources.email', type: 'text' },
      { name: 'area', label: 'resources.area', type: 'text' },
    ],
  },
  {
    key: 'browseServices',
    path: 'services',
    title: 'resources.browseServices',
    apiPath: '/api/services',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'resources.serviceName', type: 'text' },
      { name: 'description', label: 'resources.description', type: 'textarea' },
      { name: 'category', label: 'resources.category', type: 'text' },
      { name: 'phoneNumber', label: 'resources.phoneNumber', type: 'text' },
      { name: 'area', label: 'resources.area', type: 'text' },
    ],
  },
  {
    key: 'browseProducts',
    path: 'products',
    title: 'resources.browseProducts',
    apiPath: '/api/products',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'resources.productName', type: 'text' },
      { name: 'description', label: 'resources.description', type: 'textarea' },
      { name: 'price', label: 'resources.price', type: 'number' },
      { name: 'category', label: 'resources.category', type: 'text' },
      { name: 'area', label: 'resources.area', type: 'text' },
      { name: 'imageUrl', label: 'resources.imageUrl', type: 'text' },
    ],
  },
]

// PROVIDER resources (full CRUD on their own listings)
const providerResources: ResourceConfig[] = [
  {
    key: 'providerServices',
    path: 'services',
    title: 'resources.myServices',
    apiPath: '/api/services',
    roles: ['PROVIDER'],
    writeRoles: ['PROVIDER'],
    listParams: { mine: 'true' },
    fields: [
      { name: 'name', label: 'resources.serviceName', type: 'text', required: true },
      { name: 'description', label: 'resources.description', type: 'textarea' },
      { name: 'category', label: 'resources.category', type: 'text' },
      { name: 'phoneNumber', label: 'resources.phoneNumber', type: 'text', required: true },
      { name: 'area', label: 'resources.area', type: 'text', required: true },
      { name: 'availableFrom', label: 'resources.availableFrom', type: 'datetime' },
      { name: 'availableTo', label: 'resources.availableTo', type: 'datetime' },
    ],
  },
  {
    key: 'providerProducts',
    path: 'products',
    title: 'resources.myProducts',
    apiPath: '/api/products',
    roles: ['PROVIDER'],
    writeRoles: ['PROVIDER'],
    listParams: { mine: 'true' },
    fields: [
      { name: 'name', label: 'resources.productName', type: 'text', required: true },
      { name: 'description', label: 'resources.description', type: 'textarea' },
      { name: 'price', label: 'resources.price', type: 'number' },
      { name: 'category', label: 'resources.category', type: 'text' },
      { name: 'area', label: 'resources.area', type: 'text' },
      { name: 'imageUrl', label: 'resources.imageUrl', type: 'text' },
    ],
  },
]

export const resources: ResourceConfig[] = [
  ...customerResources,
  ...providerResources,
]

export function resourcesForRole(role: AppRole): ResourceConfig[] {
  return resources.filter((resource) => resource.roles.includes(role))
}

export function findResource(
  role: AppRole,
  path: string,
): ResourceConfig | undefined {
  return resources.find(
    (resource) =>
      resource.roles.includes(role) && resource.path === path,
  )
}