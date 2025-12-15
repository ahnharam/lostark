import { ref } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type { ProfileRankingResponse } from '@/api/types'
import { getHttpErrorMessage } from '@/utils/httpError'

export const useProfileRanking = () => {
  const data = ref<ProfileRankingResponse | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const characterName = ref<string | null>(null)
  const cache = new Map<string, ProfileRankingResponse>()

  const setCharacter = (name: string | null) => {
    characterName.value = name?.trim() || null
  }

  const reset = () => {
    data.value = null
  }

  const fetchProfileRanking = async (force = false) => {
    if (!characterName.value) {
      reset()
      return
    }

    if (!force && cache.has(characterName.value)) {
      data.value = cache.get(characterName.value)!
      error.value = null
      return
    }

    loading.value = true
    try {
      const response = await lostarkApi.getProfileRanking(characterName.value)
      cache.set(characterName.value, response.data)
      data.value = response.data
      error.value = null
    } catch (err: unknown) {
      error.value =
        getHttpErrorMessage(err) || '내부 데이터베이스를 기반으로 한 랭킹 정보를 불러오지 못했습니다.'
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    setCharacter,
    fetchProfileRanking,
    reset
  }
}
