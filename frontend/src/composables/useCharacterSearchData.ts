import { computed, ref } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type {
  ArkGridResponse,
  CardResponse,
  CharacterProfile,
  Collectible,
  Engraving,
  Equipment,
  SearchHistory,
  SiblingCharacter,
  SkillMenuResponse
} from '@/api/types'

interface ErrorState {
  message: string
  type: 'error' | 'warning' | 'info'
  title?: string
}

type CharacterAvailability = 'available' | 'unavailable' | 'loading'
type LoadOptions = { forceRefresh?: boolean }

const FAVORITES_STORAGE_KEY = 'loa:favorites'
const HISTORY_STORAGE_KEY = 'loa:history'

const loadFromStorage = <T>(key: string, fallback: T): T => {
  if (typeof window === 'undefined' || !window.localStorage) return fallback
  try {
    const stored = window.localStorage.getItem(key)
    if (!stored) return fallback
    return JSON.parse(stored) as T
  } catch (err) {
    console.warn(`Failed to parse local storage key '${key}'`, err)
    return fallback
  }
}

const saveToStorage = (key: string, value: unknown) => {
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(key, JSON.stringify(value))
  } catch (err) {
    console.warn(`Failed to persist local storage key '${key}'`, err)
  }
}

const loadFavoritesFromStorage = () => loadFromStorage<CharacterProfile[]>(FAVORITES_STORAGE_KEY, [])
const loadHistoryFromStorage = () => loadFromStorage<SearchHistory[]>(HISTORY_STORAGE_KEY, [])

type FavoriteIdentity = Pick<CharacterProfile, 'characterName' | 'serverName'>

const isSameFavorite = (target: FavoriteIdentity, other: FavoriteIdentity) =>
  target.characterName === other.characterName && target.serverName === other.serverName

export const useCharacterSearchData = () => {
  const characterName = ref('')
  const character = ref<CharacterProfile | null>(null)
  const loading = ref(false)
  const error = ref<ErrorState | null>(null)
  const siblings = ref<SiblingCharacter[]>([])
  const favorites = ref<CharacterProfile[]>(loadFavoritesFromStorage())
  const history = ref<SearchHistory[]>(loadHistoryFromStorage())
  const characterAvailability = ref<Record<string, CharacterAvailability>>({})
  const selectedCharacterProfile = ref<CharacterProfile | null>(null)
  const detailEquipment = ref<Equipment[]>([])
  const detailEngravings = ref<Engraving[]>([])
  const detailLoading = ref(false)
  const detailError = ref<string | null>(null)
  const arkGridResponse = ref<ArkGridResponse | null>(null)
  const arkGridLoading = ref(false)
  const arkGridError = ref<string | null>(null)
  const arkGridLoadedFor = ref<string | null>(null)
  const cardResponse = ref<CardResponse | null>(null)
  const cardLoading = ref(false)
  const cardError = ref<string | null>(null)
  const cardLoadedFor = ref<string | null>(null)
  const skillResponse = ref<SkillMenuResponse | null>(null)
  const skillLoading = ref(false)
  const skillError = ref<string | null>(null)
  const skillLoadedFor = ref<string | null>(null)
  const armoryGemsResponse = ref<any | null>(null)
  const collectibles = ref<Collectible[]>([])
  const collectiblesLoading = ref(false)
  const collectiblesError = ref<string | null>(null)
  const collectiblesLoadedFor = ref<string | null>(null)
  const lastRefreshedAt = ref<Date | null>(null)

  const activeCharacter = computed<CharacterProfile | null>(
    () => selectedCharacterProfile.value ?? character.value
  )

  const persistFavoritesToStorage = () => saveToStorage(FAVORITES_STORAGE_KEY, favorites.value)
  const persistHistoryToStorage = () => saveToStorage(HISTORY_STORAGE_KEY, history.value)

  const upsertFavoriteLocal = (profile: CharacterProfile) => {
    if (!profile?.characterName || !profile?.serverName) return
    const filtered = favorites.value.filter(fav => !isSameFavorite(fav, profile))
    favorites.value = [...filtered, profile]
    persistFavoritesToStorage()
  }

  const removeFavoriteLocal = (identity: FavoriteIdentity) => {
    const next = favorites.value.filter(fav => !isSameFavorite(fav, identity))
    if (next.length !== favorites.value.length) {
      favorites.value = next
      persistFavoritesToStorage()
    }
  }

  const resetCharacterData = () => {
    character.value = null
    siblings.value = []
    selectedCharacterProfile.value = null
    detailEquipment.value = []
    detailEngravings.value = []
    detailError.value = null
    characterAvailability.value = {}
    arkGridResponse.value = null
    arkGridLoadedFor.value = null
    arkGridError.value = null
    arkGridLoading.value = false
    cardResponse.value = null
    cardLoadedFor.value = null
    cardError.value = null
    cardLoading.value = false
    skillResponse.value = null
    skillLoadedFor.value = null
    skillError.value = null
    skillLoading.value = false
    collectibles.value = []
    collectiblesLoadedFor.value = null
    collectiblesError.value = null
    collectiblesLoading.value = false
    lastRefreshedAt.value = null
  }

  const loadFavorites = async () => {
    try {
      const response = await lostarkApi.getFavorites()
      favorites.value = response.data
      persistFavoritesToStorage()
    } catch (err) {
      console.error('즐겨찾기 로딩 실패:', err)
    }
  }

  let favoriteMutationId = 0

  const toggleFavorite = (targetCharacter: CharacterProfile | null) => {
    if (!targetCharacter) return

    const snapshot = favorites.value.slice()
    const shouldFavorite = !favorites.value.some(fav =>
      isSameFavorite(fav, targetCharacter)
    )
    const currentMutationId = ++favoriteMutationId

    if (shouldFavorite) {
      upsertFavoriteLocal(targetCharacter)
    } else {
      removeFavoriteLocal(targetCharacter)
    }

    const request = shouldFavorite
      ? lostarkApi.addFavorite(targetCharacter.characterName)
      : lostarkApi.removeFavorite(targetCharacter.characterName)

    request
      .catch(err => {
        if (currentMutationId === favoriteMutationId) {
          favorites.value = snapshot
          persistFavoritesToStorage()
        }
        console.error('즐겨찾기 토글 실패:', err)
      })
      .finally(() => {
        if (currentMutationId === favoriteMutationId) {
          loadFavorites().catch(loadErr => {
            console.error('즐겨찾기 동기화 실패:', loadErr)
          })
        }
      })
  }

  const loadHistory = async (options: { forceRefresh?: boolean } = {}) => {
    try {
      const response = await lostarkApi.getHistory({ force: options.forceRefresh })
      const items = response.data
        .slice()
        .sort((a, b) => new Date(b.searchedAt).getTime() - new Date(a.searchedAt).getTime())
      const seen = new Set<string>()
      const unique: typeof items = []
      for (const it of items) {
        if (!seen.has(it.characterName)) {
          seen.add(it.characterName)
          unique.push(it)
        }
      }
      history.value = unique
      persistHistoryToStorage()
    } catch (err) {
      console.error('검색 기록 로딩 실패:', err)
    }
  }

  const clearHistory = async () => {
    if (!confirm('검색 기록을 모두 삭제할까요?')) return
    try {
      await lostarkApi.clearHistory()
      history.value = []
      persistHistoryToStorage()
    } catch (err) {
      console.error('검색 기록 삭제 실패:', err)
    }
  }

  const loadCharacterDetails = async (
    name: string,
    options: { profile?: CharacterProfile; forceRefresh?: boolean } = {}
  ) => {
    detailLoading.value = true
    detailError.value = null
    characterAvailability.value[name] = 'loading'

    try {
      const profilePromise = options.profile
        ? Promise.resolve(options.profile)
        : lostarkApi.getCharacter(name, { force: options.forceRefresh }).then(res => res.data)

      const [profile, equipmentResponse, engravingsResponse] = await Promise.all([
        profilePromise,
        lostarkApi.getEquipment(name, { force: options.forceRefresh }),
        lostarkApi.getEngravings(name, { force: options.forceRefresh })
      ])

      selectedCharacterProfile.value = profile
      detailEquipment.value = equipmentResponse.data
      detailEngravings.value = engravingsResponse.data
      characterAvailability.value[name] = 'available'
      lastRefreshedAt.value = new Date()
    } catch (err: any) {
      characterAvailability.value[name] = 'unavailable'
      selectedCharacterProfile.value = options.profile || null
      detailEquipment.value = []
      detailEngravings.value = []
      if (err.response?.status === 404) {
        detailError.value = `'${name}' 캐릭터 정보를 불러올 수 없어요. 오랜 기간 미접속 캐릭터일 수 있습니다.`
      } else {
        detailError.value = err.response?.data?.message || '상세 정보를 불러오지 못했어요.'
      }
      console.error('Failed to load character details', err)
    } finally {
      detailLoading.value = false
    }
  }

  const ensureSkillData = async (options: LoadOptions = {}) => {
    const targetName = character.value?.characterName
    if (!targetName) return
    if (skillLoading.value) return
    if (!options.forceRefresh && skillLoadedFor.value === targetName && skillResponse.value) return
    await loadSkillData(targetName, options)
  }

  const loadSkillData = async (name: string, options: LoadOptions = {}) => {
    skillLoading.value = true
    skillError.value = null
    try {
      const [skillRes, armoryGemRes] = await Promise.all([
        lostarkApi.getSkills(name, { force: options.forceRefresh }),
        lostarkApi.getArmoryGems(name, { force: options.forceRefresh }).catch(() => null)
      ])
      skillResponse.value = skillRes.data
      skillLoadedFor.value = name
      armoryGemsResponse.value = armoryGemRes?.data ?? null
    } catch (err: any) {
      skillResponse.value = null
      skillLoadedFor.value = null
      const message =
        err.response?.status === 404
          ? `'${name}' 캐릭터의 스킬 정보를 찾을 수 없어요.`
          : err.response?.data?.message || '스킬 정보를 불러오지 못했어요.'
      skillError.value = message
    } finally {
      skillLoading.value = false
    }
  }

  const ensureCollectiblesData = async (options: LoadOptions = {}) => {
    const targetName = character.value?.characterName
    if (!targetName) return
    if (collectiblesLoading.value) return
    if (!options.forceRefresh && collectiblesLoadedFor.value === targetName && collectibles.value.length) return
    await loadCollectiblesData(targetName, options)
  }

  const ensureCardData = async (options: LoadOptions = {}) => {
    const targetName = character.value?.characterName
    if (!targetName) return
    if (cardLoading.value) return
    if (!options.forceRefresh && cardLoadedFor.value === targetName && cardResponse.value) return
    await loadCardData(targetName, options)
  }

  const loadCollectiblesData = async (name: string, options: LoadOptions = {}) => {
    collectiblesLoading.value = true
    collectiblesError.value = null
    try {
      const response = await lostarkApi.getCollectibles(name, { force: options.forceRefresh })
      collectibles.value = response.data
      collectiblesLoadedFor.value = name
    } catch (err: any) {
      collectibles.value = []
      collectiblesLoadedFor.value = null
      const message =
        err.response?.status === 404
          ? `'${name}' 캐릭터의 수집 정보를 찾을 수 없어요.`
          : err.response?.data?.message || '수집 정보를 불러오지 못했어요.'
      collectiblesError.value = message
    } finally {
      collectiblesLoading.value = false
    }
  }

  const loadCardData = async (name: string, options: LoadOptions = {}) => {
    cardLoading.value = true
    cardError.value = null
    try {
      const response = await lostarkApi.getCards(name, { force: options.forceRefresh })
      cardResponse.value = response.data
      cardLoadedFor.value = name
    } catch (err: any) {
      cardResponse.value = null
      cardLoadedFor.value = null
      const message =
        err.response?.status === 404
          ? `'${name}' 캐릭터의 카드 정보를 확인할 수 없어요.`
          : err.response?.data?.message || '카드 정보를 불러오지 못했어요.'
      cardError.value = message
    } finally {
      cardLoading.value = false
    }
  }

  const ensureArkGridData = async (options: LoadOptions = {}) => {
    const targetName = character.value?.characterName
    if (!targetName) return
    if (arkGridLoading.value) return
    if (!options.forceRefresh && arkGridLoadedFor.value === targetName && arkGridResponse.value) return
    await loadArkGridData(targetName, options)
  }

  const loadArkGridData = async (name: string, options: LoadOptions = {}) => {
    arkGridLoading.value = true
    arkGridError.value = null
    try {
      const response = await lostarkApi.getArkGrid(name, { force: options.forceRefresh })
      arkGridResponse.value = response.data
      arkGridLoadedFor.value = name
    } catch (err: any) {
      arkGridResponse.value = null
      const message =
        err.response?.status === 404
          ? `'${name}' 캐릭터의 아크 그리드 정보를 확인할 수 없어요.`
          : err.response?.data?.message || '아크 그리드 정보를 불러오지 못했어요.'
      arkGridError.value = message
    } finally {
      arkGridLoading.value = false
    }
  }

  const searchCharacter = async (
    name: string,
    options: { forceRefresh?: boolean } = {}
  ): Promise<boolean> => {
    loading.value = true
    error.value = null
    if (options.forceRefresh) {
      lostarkApi.invalidateCharacterCache(name)
    }
    resetCharacterData()

    try {
      const charResponse = await lostarkApi.getCharacter(name, { force: options.forceRefresh })
      character.value = charResponse.data
      characterName.value = name

      const siblingsResponse = await lostarkApi.getSiblings(name, { force: options.forceRefresh })
      const unique = new Map<string, SiblingCharacter>()
      siblingsResponse.data.forEach(member => {
        if (member.characterName === charResponse.data.characterName) return
        const key = `${member.serverName}-${member.characterName}`
        if (!unique.has(key)) {
          unique.set(key, member)
        }
      })
      siblings.value = Array.from(unique.values())

      await loadCharacterDetails(name, { profile: charResponse.data, forceRefresh: options.forceRefresh })
      if (options.forceRefresh) {
        await Promise.allSettled([
          loadSkillData(name, { forceRefresh: true }),
          loadCollectiblesData(name, { forceRefresh: true }),
          loadArkGridData(name, { forceRefresh: true }),
          loadCardData(name, { forceRefresh: true })
        ])
        await loadHistory({ forceRefresh: true })
      } else {
        await loadHistory()
      }
      await Promise.allSettled([
        ensureSkillData({ forceRefresh: options.forceRefresh }),
        ensureCollectiblesData({ forceRefresh: options.forceRefresh }),
        ensureArkGridData({ forceRefresh: options.forceRefresh }),
        ensureCardData({ forceRefresh: options.forceRefresh })
      ])
      return true
    } catch (err: any) {
      const errorData = err.response?.data
      if (err.response?.status === 404) {
        error.value = {
          title: '캐릭터를 찾을 수 없습니다',
          message: errorData?.message || `'${name}' 캐릭터가 존재하지 않아요.`,
          type: 'error'
        }
      } else {
        error.value = {
          title: '검색 실패',
          message: errorData?.message || '예상치 못한 오류가 발생했어요.',
          type: 'error'
        }
      }
      return false
    } finally {
      loading.value = false
    }
  }

  const retrySearch = () => {
    if (characterName.value) {
      void searchCharacter(characterName.value)
    }
  }

  const clearSearch = () => {
    characterName.value = ''
    resetCharacterData()
    error.value = null
  }

  const handleRefreshClick = () => {
    if (loading.value) return
    const target = activeCharacter.value?.characterName?.trim()
    if (!target) return
    void searchCharacter(target, { forceRefresh: true })
  }

  return {
    // state
    characterName,
    character,
    loading,
    error,
    siblings,
    favorites,
    history,
    characterAvailability,
    selectedCharacterProfile,
    detailEquipment,
    detailEngravings,
    detailLoading,
    detailError,
    arkGridResponse,
    arkGridLoading,
    arkGridError,
    arkGridLoadedFor,
    cardResponse,
    cardLoading,
    cardError,
    cardLoadedFor,
    skillResponse,
    skillLoading,
    skillError,
    skillLoadedFor,
    armoryGemsResponse,
    collectibles,
    collectiblesLoading,
    collectiblesError,
    collectiblesLoadedFor,
    lastRefreshedAt,
    activeCharacter,
    // actions
    loadFavorites,
    toggleFavorite,
    loadHistory,
    clearHistory,
    loadCharacterDetails,
    ensureSkillData,
    ensureCollectiblesData,
    ensureArkGridData,
    ensureCardData,
    loadSkillData,
    loadCollectiblesData,
    loadArkGridData,
    loadCardData,
    searchCharacter,
    retrySearch,
    clearSearch,
    handleRefreshClick,
    resetCharacterData
  }
}
