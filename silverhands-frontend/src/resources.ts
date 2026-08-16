import type { AppRole } from './api/types'

export type FieldType = 'text' | 'textarea' | 'number' | 'datetime'

export interface FieldConfig {
  name: string
  label: string
  type: FieldType
  required?: boolean
}

// Query params appended when LISTING rows (e.g. provider's own records).
export interface ResourceConfig {
  key: string
  path: string
  title: string
  apiPath: string
  roles: AppRole[]
  writeRoles: AppRole[]
  fields: FieldConfig[]
  listParams?: Record<string, string>
  // enables search/filter inputs for customer discovery pages
  searchable?: boolean
}

// ─────────────────────────────────────────────────────────────────────────────
// CUSTOMER resources (read-only discovery)
// ─────────────────────────────────────────────────────────────────────────────
const customerResources: ResourceConfig[] = [
  {
    key: 'browseProviders',
    path: 'providers',
    title: 'Service providers',
    apiPath: '/api/providers',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'Name', type: 'text' },
      { name: 'email', label: 'Email', type: 'text' },
      { name: 'area', label: 'Area', type: 'text' },
    ],
  },
  {
    key: 'browseServices',
    path: 'services',
    title: 'Browse services',
    apiPath: '/api/services',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'Service name', type: 'text' },
      { name: 'description', label: 'Description', type: 'textarea' },
      { name: 'category', label: 'Category', type: 'text' },
      { name: 'pricePerHour', label: 'Price per hour', type: 'number' },
      { name: 'area', label: 'Area', type: 'text' },
    ],
  },
  {
    key: 'browseProducts',
    path: 'products',
    title: 'Browse products',
    apiPath: '/api/products',
    roles: ['CUSTOMER'],
    writeRoles: [],
    searchable: true,
    fields: [
      { name: 'name', label: 'Product name', type: 'text' },
      { name: 'description', label: 'Description', type: 'textarea' },
      { name: 'price', label: 'Price', type: 'number' },
      { name: 'category', label: 'Category', type: 'text' },
      { name: 'area', label: 'Area', type: 'text' },
      { name: 'imageUrl', label: 'Image URL', type: 'text' },
    ],
  },
]

// ─────────────────────────────────────────────────────────────────────────────
// PROVIDER resources (full CRUD on their own listings)
// ─────────────────────────────────────────────────────────────────────────────
const providerResources: ResourceConfig[] = [
  {
    key: 'providerServices',
    path: 'services',
    title: 'My services',
    apiPath: '/api/services',
    roles: ['PROVIDER'],
    writeRoles: ['PROVIDER'],
    listParams: { mine: 'true' },
    fields: [
      { name: 'name', label: 'Service name', type: 'text', required: true },
      { name: 'description', label: 'Description', type: 'textarea' },
      { name: 'category', label: 'Category', type: 'text' },
      { name: 'pricePerHour', label: 'Price per hour', type: 'number' },
      { name: 'area', label: 'Area', type: 'text', required: true },
      { name: 'availableFrom', label: 'Available from', type: 'datetime' },
      { name: 'availableTo', label: 'Available to', type: 'datetime' },
    ],
  },
  {
    key: 'providerProducts',
    path: 'products',
    title: 'My products',
    apiPath: '/api/products',
    roles: ['PROVIDER'],
    writeRoles: ['PROVIDER'],
    listParams: { mine: 'true' },
    fields: [
      { name: 'name', label: 'Product name', type: 'text', required: true },
      { name: 'description', label: 'Description', type: 'textarea' },
      { name: 'price', label: 'Price', type: 'number' },
      { name: 'category', label: 'Category', type: 'text' },
      { name: 'area', label: 'Area', type: 'text' },
      { name: 'imageUrl', label: 'Image URL', type: 'text' },
    ],
  },
]

export const resources: ResourceConfig[] = [...customerResources, ...providerResources]

export function resourcesForRole(role: AppRole): ResourceConfig[] {
  return resources.filter((resource) => resource.roles.includes(role))
}

export function findResource(role: AppRole, path: string): ResourceConfig | undefined {
  return resources.find((resource) => resource.roles.includes(role) && resource.path === path)
}
