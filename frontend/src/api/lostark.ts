import { apiClient } from './http'
import { cachedApiCall, createCacheKey, cacheManager } from '@/utils/cache'
import type {
  ApiResult,
  CharacterProfile,
  Equipment,
  Engraving,
  SearchHistory,
  SiblingCharacter
} from './types'

const CACHE_TTL = {
  CHARACTER: 5 * 60 * 1000,
  EQUIPMENT: 10 * 60 * 1000,
  ENGRAVINGS: 10 * 60 * 1000,
  SIBLINGS: 15 * 60 * 1000
} as const

const USER_ID = 'user123'

const cachedRequest = async <T>(
  namespace: string,
  params: Record<string, unknown>,
  requester: () => Promise<T>,
  ttl: number
): Promise<ApiResult<T>> => {
  const cacheKey = createCacheKey(namespace, params)
  const { data, fromCache } = await cachedApiCall(cacheKey, requester, ttl)
  return { data, fromCache }
}

export const lostarkApi = {
  async getCharacter(characterName: string): Promise<ApiResult<CharacterProfile>> {
    return cachedRequest(
      'character',
      { name: characterName },
      async () => {
        const response = await apiClient.get<CharacterProfile>(`/characters/${characterName}`, {
          params: { userId: USER_ID }
        })
        return response.data
      },
      CACHE_TTL.CHARACTER
    )
  },

  async getEquipment(characterName: string): Promise<ApiResult<Equipment[]>> {
    return cachedRequest(
      'equipment',
      { name: characterName },
      async () => {
        const response = await apiClient.get<Equipment[]>(`/equipment/${characterName}`)
        return response.data
      },
      CACHE_TTL.EQUIPMENT
    )
  },

  async getEngravings(characterName: string): Promise<ApiResult<Engraving[]>> {
    return cachedRequest(
      'engravings',
      { name: characterName },
      async () => {
        const response = await apiClient.get<Engraving[]>(`/engravings/${characterName}`)
        return response.data
      },
      CACHE_TTL.ENGRAVINGS
    )
  },

  async getSiblings(characterName: string): Promise<ApiResult<SiblingCharacter[]>> {
    return cachedRequest(
      'siblings',
      { name: characterName },
      async () => {
        const response = await apiClient.get<SiblingCharacter[]>(`/siblings/${characterName}`)
        return response.data
      },
      CACHE_TTL.SIBLINGS
    )
  },

  addFavorite(characterName: string) {
    return apiClient.post('/favorites', { userId: USER_ID, characterName })
  },

  removeFavorite(characterName: string) {
    return apiClient.delete('/favorites', { params: { userId: USER_ID, characterName } })
  },

  getFavorites() {
    return apiClient.get<CharacterProfile[]>('/favorites', { params: { userId: USER_ID } })
  },

  checkFavorite(characterName: string) {
    return apiClient.get<{ isFavorite: boolean }>('/favorites/check', {
      params: { userId: USER_ID, characterName }
    })
  },

  getHistory() {
    return apiClient.get<SearchHistory[]>('/history', { params: { userId: USER_ID } })
  },

  clearHistory() {
    return apiClient.delete('/history', { params: { userId: USER_ID } })
  },

  clearCache() {
    cacheManager.clear()
  }
}

export type {
  ApiResult,
  CharacterProfile,
  CharacterStat,
  Equipment,
  Engraving,
  SearchHistory,
  SiblingCharacter
} from './types'
