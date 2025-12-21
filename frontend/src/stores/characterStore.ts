import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type {
  ArkGridResponse,
  ArmoryAvatar,
  ArmoryGem,
  CardResponse,
  CharacterProfile,
  Collectible,
  Engraving,
  Equipment,
  SearchHistory,
  SiblingCharacter,
  SkillMenuResponse
} from '@/api/types'
import { getHttpErrorMessage, getHttpStatus } from '@/utils/httpError'

// ===== Types =====
interface ErrorState {
  message: string
  type: 'error' | 'warning' | 'info'
  title?: string
}

interface LocalSearchHistory {
  characterName: string
  itemAvgLevel: string
  characterClassName: string
  characterImage: string
  timestamp: number
}

type CharacterAvailability = 'available' | 'unavailable' | 'loading'
type LoadOptions = { forceRefresh?: boolean }
type FavoriteIdentity = Pick<CharacterProfile, 'characterName'>

// ===== Constants =====
const FAVORITES_STORAGE_KEY = 'loa:favorites'
const HISTORY_STORAGE_KEY = 'loa:history'

// ===== Storage Utilities =====
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

const isSameFavorite = (target: FavoriteIdentity, other: FavoriteIdentity) =>
  target.characterName === other.characterName

// ===== Pinia Store =====
export const useCharacterStore = defineStore('character', () => {
  // ===== State =====
  // Basic character data
  const characterName = ref('')
  const character = ref<CharacterProfile | null>(null)
  const loading = ref(false)
  const error = ref<ErrorState | null>(null)
  const siblings = ref<SiblingCharacter[]>([])

  // Favorites & History (localStorage backed)
  const favorites = ref<CharacterProfile[]>(loadFromStorage(FAVORITES_STORAGE_KEY, []))
  const history = ref<LocalSearchHistory[]>(loadFromStorage(HISTORY_STORAGE_KEY, []))

  // Character availability tracking
  const characterAvailability = ref<Record<string, CharacterAvailability>>({})

  // Selected character profile
  const selectedCharacterProfile = ref<CharacterProfile | null>(null)

  // Detail data (equipment, avatars, engravings)
  const detailEquipment = ref<Equipment[]>([])
  const detailAvatars = ref<ArmoryAvatar[]>([])
  const detailEngravings = ref<Engraving[]>([])
  const detailLoading = ref(false)
  const detailError = ref<string | null>(null)

  // Ark Grid data
  const arkGridResponse = ref<ArkGridResponse | null>(null)
  const arkGridLoading = ref(false)
  const arkGridError = ref<string | null>(null)
  const arkGridLoadedFor = ref<string | null>(null)

  // Card data
  const cardResponse = ref<CardResponse | null>(null)
  const cardLoading = ref(false)
  const cardError = ref<string | null>(null)
  const cardLoadedFor = ref<string | null>(null)

  // Skills data
  const skillResponse = ref<SkillMenuResponse | null>(null)
  const skillLoading = ref(false)
  const skillError = ref<string | null>(null)
  const skillLoadedFor = ref<string | null>(null)
  const armoryGemsResponse = ref<ArmoryGem | null>(null)

  // Collectibles data
  const collectibles = ref<Collectible[]>([])
  const collectiblesLoading = ref(false)
  const collectiblesError = ref<string | null>(null)
  const collectiblesLoadedFor = ref<string | null>(null)

  // Meta
  const lastRefreshedAt = ref<Date | null>(null)

  // ===== Getters =====
  const activeCharacter = computed<CharacterProfile | null>(
    () => selectedCharacterProfile.value ?? character.value
  )

  const hasCharacter = computed(() => !!character.value)

  const isCharacterFavorite = computed(() => {
    if (!activeCharacter.value) return false
    return favorites.value.some(fav => isSameFavorite(fav, activeCharacter.value!))
  })

  // ===== Actions =====

  // ----- Storage Persistence -----
  function persistFavoritesToStorage() {
    saveToStorage(FAVORITES_STORAGE_KEY, favorites.value)
  }

  function persistHistoryToStorage() {
    saveToStorage(HISTORY_STORAGE_KEY, history.value)
  }

  // ----- Favorites Management -----
  function upsertFavoriteLocal(profile: CharacterProfile) {
    const existingIndex = favorites.value.findIndex(fav => isSameFavorite(fav, profile))
    if (existingIndex >= 0) {
      favorites.value[existingIndex] = profile
    } else {
      favorites.value.unshift(profile)
    }
    persistFavoritesToStorage()
  }

  function removeFavoriteLocal(target: FavoriteIdentity) {
    favorites.value = favorites.value.filter(fav => !isSameFavorite(fav, target))
    persistFavoritesToStorage()
  }

  function toggleFavorite(profile?: CharacterProfile) {
    const target = profile ?? activeCharacter.value
    if (!target) return

    const existingIndex = favorites.value.findIndex(fav => isSameFavorite(fav, target))
    if (existingIndex >= 0) {
      removeFavoriteLocal(target)
    } else {
      upsertFavoriteLocal(target)
    }
  }

  function loadFavorites() {
    favorites.value = loadFromStorage(FAVORITES_STORAGE_KEY, [])
  }

  // ----- History Management -----
  function addToHistory(profile: CharacterProfile) {
    const item: LocalSearchHistory = {
      characterName: profile.characterName,
      itemAvgLevel: profile.itemAvgLevel ?? '0',
      characterClassName: profile.characterClassName ?? '',
      characterImage: profile.characterImage ?? '',
      timestamp: Date.now()
    }

    // Remove duplicate
    history.value = history.value.filter(h => !isSameFavorite(h, item))

    // Add to front
    history.value.unshift(item)

    // Keep only 20 recent items
    if (history.value.length > 20) {
      history.value = history.value.slice(0, 20)
    }

    persistHistoryToStorage()
  }

  function clearHistory() {
    history.value = []
    persistHistoryToStorage()
  }

  function loadHistory() {
    history.value = loadFromStorage(HISTORY_STORAGE_KEY, [])
  }

  // ----- Character Search -----
  async function searchCharacter(name: string, forceRefresh = false) {
    if (!name.trim()) {
      error.value = {
        title: '캐릭터명을 입력해주세요',
        message: '검색할 캐릭터의 이름을 입력해주세요.',
        type: 'warning'
      }
      return
    }

    characterName.value = name
    loading.value = true
    error.value = null

    // 이전 캐릭터의 데이터 및 로딩 상태 초기화
    character.value = null
    selectedCharacterProfile.value = null
    siblings.value = []

    // 상세 데이터 초기화
    detailEquipment.value = []
    detailAvatars.value = []
    detailEngravings.value = []
    detailLoading.value = false
    detailError.value = null

    // 아크그리드 초기화
    arkGridResponse.value = null
    arkGridLoading.value = false
    arkGridError.value = null
    arkGridLoadedFor.value = ''

    // 카드 초기화
    cardResponse.value = null
    cardLoading.value = false
    cardError.value = null
    cardLoadedFor.value = ''

    // 스킬 초기화
    skillResponse.value = null
    armoryGemsResponse.value = null
    skillLoading.value = false
    skillError.value = null
    skillLoadedFor.value = ''

    // 수집품 초기화
    collectibles.value = []
    collectiblesLoading.value = false
    collectiblesError.value = null
    collectiblesLoadedFor.value = ''

    try {
      // 메인 캐릭터 정보 먼저 로드
      const mainResult = await lostarkApi.getCharacter(name, { force: forceRefresh })

      character.value = mainResult.data
      selectedCharacterProfile.value = null
      lastRefreshedAt.value = new Date()

      // Add to history
      addToHistory(mainResult.data)

      // 통합 메뉴에 필요한 모든 데이터를 병렬 로드 (실패해도 계속 진행)
      await Promise.allSettled([
        loadSiblings(name, forceRefresh),
        ensureDetailData(name, { forceRefresh }),
        ensureArkGridData(name, { forceRefresh }),
        ensureCardData(name, { forceRefresh }),
        ensureSkillData(name, { forceRefresh }),
        ensureCollectiblesData(name, { forceRefresh })
      ])

    } catch (err: any) {
      const status = getHttpStatus(err)
      const message = getHttpErrorMessage(err)

      if (status === 404) {
        error.value = {
          title: '캐릭터를 찾을 수 없어요',
          message: `'${name}' 캐릭터가 존재하지 않거나, 공개 설정이 비공개로 되어있어요.`,
          type: 'error'
        }
      } else if (status === 403) {
        error.value = {
          title: '접근이 제한되었어요',
          message: '이 캐릭터의 정보를 조회할 권한이 없어요.',
          type: 'error'
        }
      } else {
        error.value = {
          title: '오류가 발생했어요',
          message: message || '캐릭터 정보를 불러오는 중 문제가 발생했어요.',
          type: 'error'
        }
      }

      character.value = null
      selectedCharacterProfile.value = null

    } finally {
      loading.value = false
    }
  }

  // ----- Siblings -----
  async function loadSiblings(name: string, forceRefresh = false) {
    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const result = await lostarkApi.getSiblings(name, { force: forceRefresh })
        siblings.value = result.data
        return
      } catch (err) {
        retryCount++
        console.error(`Failed to load siblings (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          siblings.value = []
          return
        }

        // 재시도 전 1초 대기
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Detail Data (Equipment, Avatars, Engravings) -----
  async function ensureDetailData(name: string, options: LoadOptions = {}) {
    if (detailLoading.value) return

    detailLoading.value = true
    detailError.value = null

    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const [equipment, avatars, engravings] = await Promise.all([
          lostarkApi.getEquipment(name, { force: options.forceRefresh }),
          lostarkApi.getAvatars(name, { force: options.forceRefresh }),
          lostarkApi.getEngravings(name, { force: options.forceRefresh })
        ])

        detailEquipment.value = equipment.data
        detailAvatars.value = avatars.data
        detailEngravings.value = engravings.data

        // 성공하면 로딩 종료
        detailLoading.value = false
        return

      } catch (err) {
        retryCount++
        console.error(`Failed to load detail data (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          detailError.value = '장비 정보를 불러오는 데 실패했어요'
          detailLoading.value = false
          return
        }

        // 재시도 전 1초 대기
        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Ark Grid Data -----
  async function ensureArkGridData(name: string, options: LoadOptions = {}) {
    if (arkGridLoading.value) return
    if (!options.forceRefresh && arkGridLoadedFor.value === name && arkGridResponse.value) {
      return
    }

    arkGridLoading.value = true
    arkGridError.value = null

    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const result = await lostarkApi.getArkGrid(name, { force: options.forceRefresh })
        arkGridResponse.value = result.data
        arkGridLoadedFor.value = name
        arkGridLoading.value = false
        return
      } catch (err) {
        retryCount++
        console.error(`Failed to load ark grid data (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          arkGridError.value = '아크 그리드 정보를 불러오는 데 실패했어요'
          arkGridLoading.value = false
          return
        }

        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Card Data -----
  async function ensureCardData(name: string, options: LoadOptions = {}) {
    if (cardLoading.value) return
    if (!options.forceRefresh && cardLoadedFor.value === name && cardResponse.value) {
      return
    }

    cardLoading.value = true
    cardError.value = null

    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const result = await lostarkApi.getCards(name, { force: options.forceRefresh })
        cardResponse.value = result.data
        cardLoadedFor.value = name
        cardLoading.value = false
        return
      } catch (err) {
        retryCount++
        console.error(`Failed to load card data (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          cardError.value = '카드 정보를 불러오는 데 실패했어요'
          cardLoading.value = false
          return
        }

        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Skills Data -----
  async function ensureSkillData(name: string, options: LoadOptions = {}) {
    if (skillLoading.value) return
    if (!options.forceRefresh && skillLoadedFor.value === name && skillResponse.value) {
      return
    }

    skillLoading.value = true
    skillError.value = null

    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const [skills, gems] = await Promise.all([
          lostarkApi.getSkills(name, { force: options.forceRefresh }),
          lostarkApi.getArmoryGems(name, { force: options.forceRefresh })
        ])

        skillResponse.value = skills.data
        armoryGemsResponse.value = gems.data
        skillLoadedFor.value = name
        skillLoading.value = false
        return
      } catch (err) {
        retryCount++
        console.error(`Failed to load skill data (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          skillError.value = '스킬 정보를 불러오는 데 실패했어요'
          skillLoading.value = false
          return
        }

        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Collectibles Data -----
  async function ensureCollectiblesData(name: string, options: LoadOptions = {}) {
    if (collectiblesLoading.value) return
    if (!options.forceRefresh && collectiblesLoadedFor.value === name && collectibles.value.length > 0) {
      return
    }

    collectiblesLoading.value = true
    collectiblesError.value = null

    const maxRetries = 3
    let retryCount = 0

    while (retryCount < maxRetries) {
      try {
        const result = await lostarkApi.getCollectibles(name, { force: options.forceRefresh })
        collectibles.value = result.data
        collectiblesLoadedFor.value = name
        collectiblesLoading.value = false
        return
      } catch (err) {
        retryCount++
        console.error(`Failed to load collectibles data (attempt ${retryCount}/${maxRetries}):`, err)

        if (retryCount >= maxRetries) {
          collectiblesError.value = '수집품 정보를 불러오는 데 실패했어요'
          collectiblesLoading.value = false
          return
        }

        await new Promise(resolve => setTimeout(resolve, 1000))
      }
    }
  }

  // ----- Error Management -----
  function clearError() {
    error.value = null
  }

  function setError(errorState: ErrorState) {
    error.value = errorState
  }

  // ----- Character Selection (for expedition view) -----
  function selectCharacter(profile: CharacterProfile | null) {
    selectedCharacterProfile.value = profile
  }

  // ----- Reset State -----
  function resetCharacterData() {
    character.value = null
    selectedCharacterProfile.value = null
    siblings.value = []
    detailEquipment.value = []
    detailAvatars.value = []
    detailEngravings.value = []
    arkGridResponse.value = null
    cardResponse.value = null
    skillResponse.value = null
    armoryGemsResponse.value = null
    collectibles.value = []
    error.value = null
    lastRefreshedAt.value = null
  }

  // ===== Return =====
  return {
    // State
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
    detailAvatars,
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

    // Getters
    activeCharacter,
    hasCharacter,
    isCharacterFavorite,

    // Actions
    searchCharacter,
    loadSiblings,
    ensureDetailData,
    ensureArkGridData,
    ensureCardData,
    ensureSkillData,
    ensureCollectiblesData,
    loadFavorites,
    toggleFavorite,
    loadHistory,
    addToHistory,
    clearHistory,
    clearError,
    setError,
    selectCharacter,
    resetCharacterData
  }
})
