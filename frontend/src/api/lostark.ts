import axios from 'axios'
import { cachedApiCall, createCacheKey, cacheManager } from '@/utils/cache'

// 백엔드 API 호출용 Axios 인스턴스
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000, // 10초 초과 시 요청 취소
})

// 캐시 TTL 설정 (밀리초): 데이터 성격에 따라 만료 시간을 다르게 둠
const CACHE_TTL = {
  CHARACTER: 5 * 60 * 1000,    // 캐릭터 프로필은 5분 캐시
  EQUIPMENT: 10 * 60 * 1000,   // 장비는 상대적으로 느리게 바뀌므로 10분 캐시
  ENGRAVINGS: 10 * 60 * 1000,  // 각인 정보도 10분
  SIBLINGS: 15 * 60 * 1000,    // 원정대 리스트는 변경이 드물어 15분
}

// 캐릭터 기본 프로필 응답
export interface CharacterStat {
  type: string
  value: string | string[]
  tooltip?: string | string[]
}

export interface CharacterProfile {
  characterName: string        // 캐릭터명
  serverName: string           // 소속 서버
  characterClassName: string   // 전직/클래스 이름
  itemAvgLevel: string         // 장착 아이템 평균 레벨
  itemMaxLevel: string         // 무기 포함 최대 레벨
  characterImage: string       // 프로필 이미지 URL
  expeditionLevel: number      // 원정대 레벨
  pvpGradeName: string         // PVP 등급명
  guildName: string            // 가입 길드명
  characterLevel?: number      // 캐릭터(전투) 레벨
  title?: string               // 칭호
  stats?: CharacterStat[]      // 전투 특성
  combatPower?: number         // 전투력
}

// 장비 목록 응답
export interface Equipment {
  type: string   // 장비 종류 (무기, 투구 등)
  name: string   // 장비 이름
  icon: string   // 아이콘 이미지 URL
  grade: string  // 장비 등급 (고대, 유물 등)
  tooltip: string // 로스트아크 원본 툴팁(JSON 문자열)
}

// 각인 정보 응답
export interface Engraving {
  name: string        // 각인 이름
  icon: string        // 각인 아이콘 URL
  description: string // 각인 효과 설명
}

// 같은 원정대의 다른 캐릭터 정보
export interface SiblingCharacter {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
  characterLevel?: number
  characterImage?: string
}

// 검색 기록 항목
export interface SearchHistory {
  id: number             // 기록 식별자
  userId: string         // 검색한 사용자 ID
  characterName: string  // 검색 대상 캐릭터 이름
  searchedAt: string     // 검색 시각(ISO 문자열)
}

const USER_ID = 'user123' // 임시 사용자 식별값(향후 로그인 연동 필요)

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
