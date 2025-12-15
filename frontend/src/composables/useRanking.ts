import { reactive, ref } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type { RankingEntry, RankingResponse, RankingSummary } from '@/api/types'
import { getHttpErrorMessage } from '@/utils/httpError'

export interface RankingFilters {
  leaderboardCode: string
  seasonId?: string
  page: number
}

const defaultFilters: RankingFilters = {
  leaderboardCode: '0101',
  seasonId: undefined,
  page: 1
}

export const useRanking = (initialFilters?: Partial<RankingFilters>) => {
  const filters = reactive<RankingFilters>({ ...defaultFilters, ...initialFilters })
  const entries = ref<RankingEntry[]>([])
  const summary = ref<RankingSummary | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const characterName = ref<string | null>(null)
  const cache = new Map<string, RankingResponse>()

  const buildCacheKey = () =>
    `${characterName.value ?? ''}|${filters.leaderboardCode}|${filters.seasonId ?? ''}|${filters.page}`

  const applyResponse = (response: RankingResponse) => {
    entries.value = response.entries
    summary.value = response.summary
  }

  const resetState = () => {
    entries.value = []
    summary.value = null
  }

  const fetchRanking = async (force = false) => {
    if (!characterName.value) {
      resetState()
      return
    }

    const cacheKey = buildCacheKey()
    if (!force && cache.has(cacheKey)) {
      applyResponse(cache.get(cacheKey)!)
      error.value = null
      return
    }

    loading.value = true
    try {
      const { data } = await lostarkApi.getRanking({
        leaderboardCode: filters.leaderboardCode,
        seasonId: filters.seasonId,
        page: filters.page,
        characterName: characterName.value
      })
      cache.set(cacheKey, data)
      applyResponse(data)
      error.value = null
    } catch (err: unknown) {
      error.value = getHttpErrorMessage(err) || '랭킹 정보를 불러오는 중 문제가 발생했습니다.'
    } finally {
      loading.value = false
    }
  }

  const setCharacter = (name: string | null, resetPage = true) => {
    characterName.value = name?.trim() || null
    if (resetPage) {
      filters.page = 1
    }
  }

  const updateFilters = (next: Partial<RankingFilters>) => {
    Object.assign(filters, next)
  }

  return {
    entries,
    summary,
    loading,
    error,
    filters,
    setCharacter,
    updateFilters,
    fetchRanking,
    resetState
  }
}
