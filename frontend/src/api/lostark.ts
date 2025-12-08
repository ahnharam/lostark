import { apiClient } from './http'
import { cachedApiCall, createCacheKey, cacheManager } from '@/utils/cache'
import type {
  ApiResult,
  ArkGridResponse,
  CharacterProfile,
  Equipment,
  Engraving,
  Collectible,
  ProfileRankingResponse,
  RankingQueryParams,
  RankingResponse,
  SearchHistory,
  SkillMenuResponse,
  SiblingCharacter,
  CardResponse,
  PageResponse,
  StoredMarketCategory,
  StoredMarketItem,
  MarketOptionsResponse,
  MarketSearchResponse,
  MarketItemSummary,
  MarketItemDetail
} from './types'

const CACHE_TTL = {
  CHARACTER: 5 * 60 * 1000,
  EQUIPMENT: 10 * 60 * 1000,
  ENGRAVINGS: 10 * 60 * 1000,
  SIBLINGS: 15 * 60 * 1000,
  RANKING: 5 * 60 * 1000,
  ARK_GRID: 10 * 60 * 1000,
  PROFILE_RANKING: 5 * 60 * 1000,
  SKILLS: 5 * 60 * 1000,
  COLLECTIBLES: 5 * 60 * 1000
} as const
const CARDS_TTL = 5 * 60 * 1000

const USER_ID_STORAGE_KEY = 'loa:user-id'

const generateUserId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `user-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const resolveUserId = (): string => {
  if (typeof window === 'undefined' || !window.localStorage) {
    return 'user-default'
  }
  const stored = window.localStorage.getItem(USER_ID_STORAGE_KEY)
  if (stored) return stored
  const newId = generateUserId()
  window.localStorage.setItem(USER_ID_STORAGE_KEY, newId)
  return newId
}

const USER_ID = resolveUserId()

const cachedRequest = async <T>(
  namespace: string,
  params: Record<string, unknown>,
  requester: () => Promise<T>,
  ttl: number,
  options?: { force?: boolean }
): Promise<ApiResult<T>> => {
  const cacheKey = createCacheKey(namespace, params)
  if (options?.force) {
    cacheManager.invalidate(cacheKey)
    const data = await requester()
    cacheManager.set(cacheKey, data, ttl)
    return { data, fromCache: false }
  }
  const { data, fromCache } = await cachedApiCall(cacheKey, requester, ttl)
  return { data, fromCache }
}

export const lostarkApi = {
  async getCharacter(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<CharacterProfile>> {
    return cachedRequest(
      'character',
      { name: characterName },
      async () => {
        const response = await apiClient.get<CharacterProfile>(`/characters/${characterName}`, {
          params: { userId: USER_ID, force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.CHARACTER,
      options
    )
  },

  async getCards(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<CardResponse>> {
    return cachedRequest(
      'cards',
      { name: characterName },
      async () => {
        const response = await apiClient.get<CardResponse>(`/cards/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CARDS_TTL,
      options
    )
  },

  async getEquipment(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<Equipment[]>> {
    return cachedRequest(
      'equipment',
      { name: characterName },
      async () => {
        const response = await apiClient.get<Equipment[]>(`/equipment/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.EQUIPMENT,
      options
    )
  },

  async getEngravings(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<Engraving[]>> {
    return cachedRequest(
      'engravings',
      { name: characterName },
      async () => {
        const response = await apiClient.get<Engraving[]>(`/engravings/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.ENGRAVINGS,
      options
    )
  },

  async getSiblings(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<SiblingCharacter[]>> {
    return cachedRequest(
      'siblings',
      { name: characterName },
      async () => {
        const response = await apiClient.get<SiblingCharacter[]>(`/siblings/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.SIBLINGS,
      options
    )
  },

  async getArkGrid(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<ArkGridResponse>> {
    return cachedRequest(
      'arkGrid',
      { name: characterName },
      async () => {
        const response = await apiClient.get<ArkGridResponse>(`/ark-grid/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.ARK_GRID,
      options
    )
  },

  async getSkills(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<SkillMenuResponse>> {
    return cachedRequest(
      'skills',
      { name: characterName },
      async () => {
        const response = await apiClient.get<SkillMenuResponse>(`/skills/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.SKILLS,
      options
    )
  },

  async getCollectibles(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<Collectible[]>> {
    return cachedRequest(
      'collectibles',
      { name: characterName },
      async () => {
        const response = await apiClient.get<Collectible[]>(`/collectibles/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.COLLECTIBLES,
      options
    )
  },

  async getRanking(
    params: RankingQueryParams,
    options?: { force?: boolean }
  ): Promise<ApiResult<RankingResponse>> {
    const normalizedParams: Record<string, unknown> = { ...params }
    return cachedRequest(
      'ranking',
      normalizedParams,
      async () => {
        const response = await apiClient.get<RankingResponse>('/rankings', { params: normalizedParams })
        return response.data
      },
      CACHE_TTL.RANKING,
      options
    )
  },

  async getProfileRanking(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<ProfileRankingResponse>> {
    return cachedRequest(
      'profileRanking',
      { characterName },
      async () => {
        const response = await apiClient.get<ProfileRankingResponse>(`/rankings/profile/${characterName}`)
        return response.data
      },
      CACHE_TTL.PROFILE_RANKING,
      options
    )
  },

  async getArmoryGems(
    characterName: string,
    options?: { force?: boolean }
  ): Promise<ApiResult<any>> {
    return cachedRequest(
      'armoryGems',
      { name: characterName },
      async () => {
        const response = await apiClient.get<any>(`/gems/${characterName}`, {
          params: { force: options?.force }
        })
        return response.data
      },
      CACHE_TTL.SKILLS,
      options
    )
  },

  async getMarketCategories(): Promise<StoredMarketCategory[]> {
    const response = await apiClient.get<StoredMarketCategory[]>('/markets/categories')
    return response.data
  },

  async getMarketOptions(): Promise<MarketOptionsResponse> {
    const response = await apiClient.get<MarketOptionsResponse>('/markets/options')
    return response.data
  },

  async searchMarketItems(params: {
    categoryCode: number
    characterClass?: string
    itemTier?: number
    itemGrade?: string
    itemName?: string
    sort?: string
    sortCondition?: string
    page?: number
    size?: number
    prefetchRange?: number
  }): Promise<MarketSearchResponse> {
    const response = await apiClient.get<MarketSearchResponse>('/markets/items', {
      params: {
        categoryCode: params.categoryCode,
        characterClass: params.characterClass,
        itemTier: params.itemTier,
        itemGrade: params.itemGrade,
        itemName: params.itemName,
        sort: params.sort,
        sortCondition: params.sortCondition,
        page: params.page,
        size: params.size,
        prefetchRange: params.prefetchRange
      }
    })
    return response.data
  },

  async refreshMarketItem(apiItemId: number, body: {
    categoryCode: number
    itemName?: string
    characterClass?: string
    itemTier?: number
    itemGrade?: string
    sort?: string
    sortCondition?: string
    pageSize?: number
  }): Promise<MarketItemSummary> {
    const response = await apiClient.post<MarketItemSummary>(`/markets/items/${apiItemId}/refresh`, body)
    return response.data
  },

  async getMarketItemDetail(apiItemId: number): Promise<MarketItemDetail> {
    const response = await apiClient.get<MarketItemDetail>(`/markets/items/${apiItemId}/detail`)
    return response.data
  },

  addFavorite(characterName: string) {
    return apiClient.post('/favorites', { userId: USER_ID, characterName })
  },

  removeFavorite(characterName: string) {
    return apiClient.delete('/favorites', { params: { userId: USER_ID, characterName } })
  },

  getFavorites(options?: { force?: boolean }) {
    if (options?.force) {
      cacheManager.invalidate(createCacheKey('favorites', { userId: USER_ID }))
    }
    return apiClient.get<CharacterProfile[]>('/favorites', { params: { userId: USER_ID } })
  },

  checkFavorite(characterName: string) {
    return apiClient.get<{ isFavorite: boolean }>('/favorites/check', {
      params: { userId: USER_ID, characterName }
    })
  },

  getHistory(options?: { force?: boolean }) {
    if (options?.force) {
      cacheManager.invalidate(createCacheKey('history', { userId: USER_ID }))
    }
    return apiClient.get<SearchHistory[]>('/history', { params: { userId: USER_ID } })
  },

  clearHistory() {
    return apiClient.delete('/history', { params: { userId: USER_ID } })
  },

  invalidateCharacterCache(characterName: string) {
    const targets = [
      createCacheKey('character', { name: characterName }),
      createCacheKey('equipment', { name: characterName }),
      createCacheKey('engravings', { name: characterName }),
      createCacheKey('siblings', { name: characterName }),
      createCacheKey('skills', { name: characterName }),
      createCacheKey('collectibles', { name: characterName }),
      createCacheKey('arkGrid', { name: characterName }),
      createCacheKey('ranking', { characterName })
    ]
    targets.forEach(key => cacheManager.invalidate(key))
  },

  clearCache() {
    cacheManager.clear()
  }
}

export type {
  ApiResult,
  ArkGridResponse,
  CharacterProfile,
  CharacterStat,
  Equipment,
  Engraving,
  Collectible,
  SkillMenuResponse,
  SearchHistory,
  SiblingCharacter
} from './types'
