import axios from 'axios'
import { cachedApiCall, createCacheKey, cacheManager } from '@/utils/cache'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
})

// 캐시 TTL 설정 (밀리초)
const CACHE_TTL = {
  CHARACTER: 5 * 60 * 1000,    // 5분
  EQUIPMENT: 10 * 60 * 1000,   // 10분
  ENGRAVINGS: 10 * 60 * 1000,  // 10분
  SIBLINGS: 15 * 60 * 1000,    // 15분 (변경 빈도 낮음)
}

export interface CharacterProfile {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
  characterImage: string
  expeditionLevel: number
  pvpGradeName: string
  guildName: string
}

export interface Equipment {
  type: string
  name: string
  icon: string
  grade: string
  tooltip: string
}

export interface Engraving {
  name: string
  icon: string
  description: string
}

export interface SiblingCharacter {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
}

export interface SearchHistory {
  id: number
  userId: string
  characterName: string
  searchedAt: string
}

const USER_ID = 'user123' // 실제로는 로그인 기능에서 받아와야 함

export const lostarkApi = {
  // 기본 검색 (캐시 적용)
  getCharacter: async (characterName: string) => {
    const cacheKey = createCacheKey('character', { name: characterName })
    const { data, fromCache } = await cachedApiCall(
      cacheKey,
      async () => {
        const response = await api.get<CharacterProfile>(`/characters/${characterName}`, {
          params: { userId: USER_ID }
        })
        return response.data
      },
      CACHE_TTL.CHARACTER
    )

    return {
      data,
      fromCache,
      headers: {},
      status: 200,
      statusText: 'OK',
      config: {} as any
    }
  },

  // 장비 정보 (캐시 적용)
  getEquipment: async (characterName: string) => {
    const cacheKey = createCacheKey('equipment', { name: characterName })
    const { data, fromCache } = await cachedApiCall(
      cacheKey,
      async () => {
        const response = await api.get<Equipment[]>(`/equipment/${characterName}`)
        return response.data
      },
      CACHE_TTL.EQUIPMENT
    )

    return {
      data,
      fromCache,
      headers: {},
      status: 200,
      statusText: 'OK',
      config: {} as any
    }
  },

  // 각인 정보 (캐시 적용)
  getEngravings: async (characterName: string) => {
    const cacheKey = createCacheKey('engravings', { name: characterName })
    const { data, fromCache } = await cachedApiCall(
      cacheKey,
      async () => {
        const response = await api.get<Engraving[]>(`/engravings/${characterName}`)
        return response.data
      },
      CACHE_TTL.ENGRAVINGS
    )

    return {
      data,
      fromCache,
      headers: {},
      status: 200,
      statusText: 'OK',
      config: {} as any
    }
  },

  // 보유 캠릭터 목록 (캐시 적용)
  getSiblings: async (characterName: string) => {
    const cacheKey = createCacheKey('siblings', { name: characterName })
    const { data, fromCache } = await cachedApiCall(
      cacheKey,
      async () => {
        const response = await api.get<SiblingCharacter[]>(`/siblings/${characterName}`)
        return response.data
      },
      CACHE_TTL.SIBLINGS
    )

    return {
      data,
      fromCache,
      headers: {},
      status: 200,
      statusText: 'OK',
      config: {} as any
    }
  },
  
  // 즐겨찾기
  addFavorite: (characterName: string) => {
    return api.post('/favorites', { userId: USER_ID, characterName })
  },
  
  removeFavorite: (characterName: string) => {
    return api.delete('/favorites', { params: { userId: USER_ID, characterName } })
  },
  
  getFavorites: () => {
    return api.get<CharacterProfile[]>('/favorites', { params: { userId: USER_ID } })
  },
  
  checkFavorite: (characterName: string) => {
    return api.get<{ isFavorite: boolean }>('/favorites/check', {
      params: { userId: USER_ID, characterName }
    })
  },
  
  // 검색 히스토리
  getHistory: () => {
    return api.get<SearchHistory[]>('/history', { params: { userId: USER_ID } })
  },
  
  clearHistory: () => {
    return api.delete('/history', { params: { userId: USER_ID } })
  },

  // 캐시 관리
  clearCache: () => {
    cacheManager.clear()
  },

  invalidateCharacterCache: (characterName: string) => {
    cacheManager.invalidate(createCacheKey('character', { name: characterName }))
    cacheManager.invalidate(createCacheKey('equipment', { name: characterName }))
    cacheManager.invalidate(createCacheKey('engravings', { name: characterName }))
    cacheManager.invalidate(createCacheKey('siblings', { name: characterName }))
  },

  getCacheStats: () => {
    return cacheManager.getStats()
  },

  getCacheHitRate: () => {
    return cacheManager.getHitRate()
  },
}
