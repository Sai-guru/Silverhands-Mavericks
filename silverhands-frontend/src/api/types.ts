export type AppRole = 'CUSTOMER' | 'PROVIDER'

export interface CurrentUser {
  id: string
  email: string
  name: string
  profileImageUrl?: string | null
  role: string
  roleSelectionRequired: boolean
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export type JsonRecord = Record<string, unknown>

export interface RecommendedService {
  serviceId?: string
  id?: string
  providerId?: string
  name?: string | null
  serviceName?: string | null
  description?: string | null
  category?: string | null
  area?: string | null
  phoneNumber?: string | null
}

export interface AiChatResponse {
  reply: string
  providerDataSummary?: string | null
  model?: string | null
  usedAi?: boolean | null
  recommendedServices?: RecommendedService[] | null
}
