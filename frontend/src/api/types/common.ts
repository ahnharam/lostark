export interface ApiResult<T> {
  data: T
  fromCache: boolean
}

export interface SearchHistory {
  id: number
  userId: string
  characterName: string
  searchedAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first?: boolean
  last?: boolean
}
