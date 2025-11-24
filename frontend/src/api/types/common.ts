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
