<template>
  <div class="app-container">
    <header class="page-header">
      <button class="menu-button" type="button" aria-label="메뉴 열기" @click="openMenu">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <h1>LOA Character Search</h1>
      <div class="header-search">
        <AutocompleteInput
          v-model="characterName"
          :suggestions="searchSuggestions"
          placeholder="캐릭터명을 입력하세요"
          inputClass="search-input"
          :min-chars="0"
          :max-suggestions="8"
          @select="handleSuggestionSelect"
          @keyup.enter="searchCharacterByInput"
        />
      </div>
    </header>
    <transition name="sidebar-fade">
      <div v-if="menuOpen" class="sidebar-overlay" @click="closeMenu"></div>
    </transition>
    <transition name="sidebar-slide">
      <aside
        v-if="menuOpen"
        class="sidebar-menu"
        tabindex="-1"
        ref="sidebarRef"
        @keyup.esc="closeMenu"
      >
        <div class="sidebar-header">
          <ThemeToggle />
          <button class="sidebar-close" type="button" aria-label="메뉴 닫기" @click="closeMenu">×</button>
        </div>
        <div class="sidebar-content">
          <h3>메뉴</h3>
          <p class="sidebar-placeholder">추가 메뉴는 곧 제공될 예정입니다.</p>
          <button class="sidebar-placeholder-btn" type="button" disabled>메뉴 준비 중</button>
        </div>
      </aside>
    </transition>

    <div class="content-wrapper">
      <main class="main-content">
        <div class="search-container">
          <section class="states-section" v-if="false">
            <h2>States</h2>
            <div class="states-grid">
              <div class="state-card">
                <span class="state-label">LoadingSpinner.vue</span>
              </div>
              <div class="state-card">
                <span class="state-label">EmptyState.vue</span>
              </div>
              <div class="state-card">
                <span class="state-label">ErrorMessage.vue</span>
              </div>
            </div>
          </section>

          <div v-if="loading" class="loading-display">
            <LoadingSpinner message="캐릭터 정보를 불러오는 중..." />
          </div>

          <div v-if="error && !loading" class="error-display">
            <ErrorMessage
              :title="error.title"
              :message="error.message"
              :type="error.type"
              :retry="true"
              :dismissible="true"
              @retry="retrySearch"
              @dismiss="dismissError"
            />
          </div>

          <div v-if="!loading && !character && !error" class="empty-display">
            <EmptyState
              icon="🔍"
              title="캐릭터를 검색해 주세요"
              description="캐릭터명을 입력하고 Enter를 누르거나 추천 목록에서 선택해 주세요"
            />
          </div>

          <section v-if="character && !loading" class="character-results">
            <div class="results-layout">
              <div class="character-overview-card" ref="characterOverviewRef">
                <div class="hero-row hero-row--levels">
                  <div class="profile-stats-grid hero-levels-grid">
                    <div class="profile-stat">
                      <span>전투력</span>
                      <strong>{{ formatCombatPower(character.combatPower) }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>전투 레벨</span>
                      <strong>Lv. {{ character.characterLevel != null ? character.characterLevel : '—' }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>아이템 레벨</span>
                      <strong>{{ formatItemLevel(character.itemAvgLevel) }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>원정대 레벨</span>
                      <strong>{{ character.expeditionLevel || '-' }}</strong>
                    </div>
                  </div>
                </div>

                <div class="hero-row hero-row--image">
                  <LazyImage
                    :src="character.characterImage || ''"
                    :alt="character.characterName"
                    width="140"
                    height="140"
                    imageClass="hero-avatar"
                    errorIcon="👤"
                  />
                  <div class="hero-text">
                    <h2>{{ character.characterName }}</h2>
                    <span class="hero-title" v-if="character.title">{{ character.title }}</span>
                  </div>
                </div>

                <div class="hero-row hero-row--meta">
                  <div class="hero-meta-grid">
                    <div class="meta-item">
                      <span>직업</span>
                      <strong>{{ character.characterClassName }}</strong>
                    </div>
                    <div class="meta-item">
                      <span>서버</span>
                      <strong>{{ character.serverName }}</strong>
                    </div>
                    <div class="meta-item" v-if="character.guildName">
                      <span>길드</span>
                      <strong>{{ character.guildName }}</strong>
                    </div>
                    <div class="meta-item" v-if="character.pvpGradeName">
                      <span>PVP</span>
                      <strong>{{ character.pvpGradeName }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--profile-stats" v-if="displayStats.length">
                  <h3>전투 특성</h3>
                  <div class="profile-stats-grid">
                    <div
                      v-for="stat in displayStats"
                      :key="`${stat.type}-${stat.value}`"
                      class="profile-stat"
                    >
                      <span>{{ stat.type }}</span>
                      <strong>{{ formatProfileStat(stat.value) }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--paradise" v-if="paradiseInfo.power || paradiseInfo.season">
                  <h3>낙원</h3>
                  <div class="paradise-info">
                    <div class="paradise-item" v-if="paradiseInfo.season">
                      <span>시즌</span>
                      <strong>{{ paradiseInfo.season }}</strong>
                    </div>
                    <div class="paradise-item" v-if="paradiseInfo.power">
                      <span>낙원력</span>
                      <strong>{{ paradiseInfo.power }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--special" v-if="specialEquipmentsDetailed.length">
                  <div class="special-header">
                    <h3>기타</h3>
                    <!-- <span class="special-count">{{ specialEquipmentsDetailed.length }}개</span> -->
                  </div>
                    <div class="special-grid special-grid--icons">
                      <div
                        v-for="special in specialEquipmentsDetailed"
                        :key="special.item.name"
                        class="special-icon-wrapper"
                        :class="{ 'is-hovered': hoveredSpecialName === special.item.name }"
                        tabindex="0"
                        @mouseenter="handleSpecialHover(special.item.name)"
                        @mouseleave="handleSpecialHover(null)"
                        @focus="handleSpecialHover(special.item.name)"
                        @blur="handleSpecialHover(null)"
                      >
                        <div class="special-icon-box">
                          <LazyImage
                            v-if="special.item.icon"
                            :src="special.item.icon"
                            :alt="special.item.name"
                            width="56"
                            height="56"
                            imageClass="special-icon"
                            errorIcon="🧭"
                            :useProxy="true"
                          />
                          <div v-else class="special-icon special-icon--fallback" aria-hidden="true">
                            {{ special.item.name ? special.item.name[0] : '?' }}
                          </div>
                        </div>
                        <span class="special-label">
                          {{ special.label }}
                        </span>
                        <span
                          v-if="hoveredSpecialName === special.item.name"
                          class="special-hover-indicator"
                          aria-hidden="true"
                        ></span>
                      </div>
                    </div>
                  <div v-if="hoveredSpecial" class="special-tooltip-layer" aria-live="polite">
                    <div
                      v-if="hoveredSpecial"
                      class="special-tooltip special-tooltip--global"
                      :style="tooltipWidthStyle"
                      role="tooltip"
                    >
                      <strong>{{ hoveredSpecial.item.name }}</strong>
                      <!-- <span class="special-type">{{ hoveredSpecial.item.type }}</span> -->
                      <div v-if="hoveredSpecial.highlights.length" class="special-highlights">
                        <span v-for="(line, idx) in hoveredSpecial.highlights" :key="`${hoveredSpecial.item.name}-${idx}`">
                          {{ line }}
                        </span>
                      </div>
                      <p v-else class="special-tooltip-empty">추가 설명이 없습니다.</p>
                    </div>
                  </div>
                </div>

              </div>

              <div class="results-panel">
                <div class="view-tabs">
                  <button
                    class="view-tab-button"
                    :class="{ active: activeResultTab === 'detail' }"
                    @click="activeResultTab = 'detail'"
                  >
                    상세 보기
                  </button>
                  <button
                    class="view-tab-button"
                    :class="{ active: activeResultTab === 'expedition' }"
                    @click="activeResultTab = 'expedition'"
                  >
                    보유 캐릭터
                  </button>
                </div>

                <section
                  v-if="activeResultTab === 'detail'"
                  class="detail-panel"
                >
                  <CharacterDetailModal
                    :character="selectedCharacterProfile"
                    :equipment="detailEquipment"
                    :engravings="detailEngravings"
                    :loading="detailLoading"
                    :error-message="detailError"
                  />
                </section>

                <section v-else class="expedition-section">
                  <div class="section-header-bar">
                    <div>
                      <h3>원정대 보유 캐릭터</h3>
                      <p class="section-subtitle">클릭하면 상세 정보가 열립니다.</p>
                    </div>
                    <span class="count-pill">{{ (character ? 1 : 0) + siblings.length }}명</span>
                  </div>
                  <template v-if="expeditionGroups.length">
                    <div
                      v-for="group in expeditionGroups"
                      :key="group.server"
                      class="expedition-group"
                    >
                      <h4>{{ group.server }}</h4>
                      <div class="expedition-grid">
                        <article
                          v-for="member in group.members"
                          :key="member.characterName"
                          class="expedition-card"
                          :class="{ active: selectedCharacterProfile?.characterName === member.characterName }"
                          @click="viewCharacterDetail(member)"
                        >
                          <div class="member-top">
                            <span class="member-level">Lv. {{ member.characterLevel || '—' }}</span>
                            <span class="member-class">{{ member.characterClassName }}</span>
                          </div>
                          <strong class="member-name">{{ member.characterName }}</strong>
                          <span class="member-ilvl">
                            iLv. {{ formatItemLevel(member.itemAvgLevel || member.itemMaxLevel) }}
                          </span>
                          <span class="member-detail">상세 보기</span>
                        </article>
                      </div>
                    </div>
                  </template>
                  <p v-else class="empty-message">원정대 캐릭터가 없습니다.</p>
                </section>
              </div>
            </div>
          </section>
        </div>
      </main>

      <aside class="sidebar">
        <div class="sidebar-section">
          <h2>내 즐겨찾기</h2>
          <div v-if="favorites.length === 0" class="empty-message">
            즐겨찾기가 비어있어요
          </div>
          <div v-else class="favorite-list">
            <div
              v-for="fav in favorites"
              :key="fav.characterName"
              class="favorite-item"
              @click="searchCharacter(fav.characterName)"
            >
              <LazyImage
                :src="fav.characterImage || ''"
                :alt="fav.characterName"
                width="40"
                height="40"
                imageClass="fav-image"
                errorIcon="❔"
              />
              <div class="fav-details">
                <div class="fav-name">{{ fav.characterName }}</div>
                <div class="fav-level">{{ fav.itemMaxLevel }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <div class="section-header">
            <h2>최근 검색</h2>
            <button v-if="history.length > 0" @click="clearHistory" class="clear-btn-sm">
              전체 삭제
            </button>
          </div>
          <div v-if="history.length === 0" class="empty-message">
            검색 기록이 없습니다
          </div>
          <div v-else class="history-list">
            <div
              v-for="item in history"
              :key="item.id"
              class="history-item"
              @click="searchCharacter(item.characterName)"
            >
              {{ item.characterName }}
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { lostarkApi, type CharacterProfile, type SiblingCharacter, type SearchHistory, type Equipment, type Engraving, type CharacterStat } from '@/api/lostark'
import LoadingSpinner from './common/LoadingSpinner.vue'
import ErrorMessage from './common/ErrorMessage.vue'
import EmptyState from './common/EmptyState.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import LazyImage from './common/LazyImage.vue'
import AutocompleteInput from './common/AutocompleteInput.vue'
import CharacterDetailModal from './common/CharacterDetailModal.vue'
import { useTheme } from '@/composables/useTheme'
import type { Suggestion } from './common/AutocompleteInput.vue'

const { initTheme } = useTheme()
initTheme()

interface ErrorState {
  message: string
  type: 'error' | 'warning' | 'info'
  title?: string
}

const characterName = ref('')
const character = ref<CharacterProfile | null>(null)
const loading = ref(false)
const error = ref<ErrorState | null>(null)
const siblings = ref<SiblingCharacter[]>([])
const favorites = ref<CharacterProfile[]>([])
const history = ref<SearchHistory[]>([])
const characterAvailability = ref<Record<string, 'available' | 'unavailable' | 'loading'>>({})
const activeResultTab = ref<'detail' | 'expedition'>('detail')
const characterOverviewRef = ref<HTMLElement | null>(null)
const overviewWidth = ref(0)

const selectedCharacterProfile = ref<CharacterProfile | null>(null)
const detailEquipment = ref<Equipment[]>([])
const detailEngravings = ref<Engraving[]>([])
const detailLoading = ref(false)
const detailError = ref<string | null>(null)
const specialEquipmentKeywords = ['나침반', '부적', '문장', '보주']

const isSpecialEquipment = (item: Equipment) => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return specialEquipmentKeywords.some(keyword => target.includes(keyword.toLowerCase()))
}

const specialEquipments = computed(() => {
  return detailEquipment.value.filter(item => isSpecialEquipment(item))
})

const specialEquipmentsDetailed = computed(() => {
  return specialEquipments.value.map(item => ({
    item,
    highlights: getSpecialHighlights(item),
    label: getSpecialLabel(item)
  }))
})

const menuOpen = ref(false)
const sidebarRef = ref<HTMLElement | null>(null)

const openMenu = () => {
  menuOpen.value = true
  nextTick(() => {
    sidebarRef.value?.focus()
  })
}

const closeMenu = () => {
  menuOpen.value = false
}

const hoveredSpecialName = ref<string | null>(null)
const hoveredSpecial = computed(() => {
  if (!hoveredSpecialName.value) return null
  return specialEquipmentsDetailed.value.find(special => special.item.name === hoveredSpecialName.value) ?? null
})
interface ParadiseInfo {
  season?: string
  power?: string
}

const paradiseInfo = computed<ParadiseInfo>(() => {
  const info: ParadiseInfo = {}
  const extractLastNumber = (text: string) => {
    const matches = text.match(/\d[\d.,]*/g)
    if (!matches || !matches.length) return ''
    return matches[matches.length - 1].replace(/[^\d.,]/g, '')
  }

  for (const special of specialEquipmentsDetailed.value) {
    const tooltipLines = extractTooltipLines(special.item.tooltip)
    const combinedLines = [...special.highlights, ...tooltipLines]
    const powerLine = combinedLines.find(text => /낙원력/i.test(text))
    const seasonLine = combinedLines.find(text => /시즌/i.test(text) && /낙원/i.test(text))

    if (powerLine && !info.power) {
      const value = extractLastNumber(powerLine)
      if (value) {
        info.power = value
      }
    }

    if (seasonLine && !info.season) {
      const match = seasonLine.match(/시즌\s*(\d+)/i)
      info.season = match?.[1] || extractLastNumber(seasonLine) || seasonLine.replace(/\s+/g, ' ').trim()
    }

    if (info.power && info.season) break
  }
  return info
})

const displayStats = computed<CharacterStat[]>(() => {
  const stats = character.value?.stats
    ? character.value.stats.filter(stat => stat.type !== '낙원력').map(stat => ({ ...stat }))
    : []
  return stats
})

const tooltipWidthValue = computed(() => {
  if (!overviewWidth.value) return 320
  return Math.max(Math.round(overviewWidth.value - 20), 240)
})

const handleSpecialHover = (name: string | null) => {
  hoveredSpecialName.value = name
}

const tooltipWidthStyle = computed(() => {
  const width = tooltipWidthValue.value
  return width ? { '--tooltip-width': `${width}px` } : {}
})

const syncOverviewWidth = () => {
  if (!characterOverviewRef.value) return
  const width = characterOverviewRef.value.getBoundingClientRect().width
  if (!width) return
  if (overviewWidth.value !== width) {
    overviewWidth.value = width
  }
}

let overviewObserver: ResizeObserver | null = null

const observeOverviewCard = () => {
  if (typeof window === 'undefined') return
  overviewObserver?.disconnect()
  const el = characterOverviewRef.value
  if (el && 'ResizeObserver' in window) {
    overviewObserver = new ResizeObserver(entries => {
      const entryWidth = entries[0]?.contentRect.width ?? el.getBoundingClientRect().width
      if (entryWidth) {
        overviewWidth.value = entryWidth
      }
    })
    overviewObserver.observe(el)
    syncOverviewWidth()
  } else {
    syncOverviewWidth()
  }
}


const searchSuggestions = computed<Suggestion[]>(() => {
  const suggestions: Suggestion[] = []

  favorites.value.forEach(fav => {
    suggestions.push({
      id: `fav-${fav.characterName}`,
      name: fav.characterName,
      info: `${fav.serverName} · ${fav.characterClassName}`,
      level: fav.itemMaxLevel,
      isFavorite: true
    })
  })

  const favoriteNames = new Set(favorites.value.map(f => f.characterName))
  history.value.forEach(h => {
    if (!favoriteNames.has(h.characterName)) {
      suggestions.push({
        id: `history-${h.id}`,
        name: h.characterName,
        info: '최근 검색',
        isFavorite: false
      })
    }
  })

  return suggestions
})

const expeditionGroups = computed(() => {
  const groups = new Map<string, SiblingCharacter[]>()
  const addToGroup = (member: SiblingCharacter) => {
    const key = member.serverName || '기타'
    if (!groups.has(key)) groups.set(key, [])
    const normalizedMember: SiblingCharacter = {
      ...member,
      itemMaxLevel: member.itemMaxLevel || member.itemAvgLevel
    }
    groups.get(key)!.push(normalizedMember)
  }

  if (character.value) {
    addToGroup({
      serverName: character.value.serverName,
      characterName: character.value.characterName,
      characterLevel: character.value.characterLevel ?? undefined,
      characterClassName: character.value.characterClassName,
      itemAvgLevel: character.value.itemAvgLevel,
      itemMaxLevel: character.value.itemMaxLevel || character.value.itemAvgLevel,
      characterImage: character.value.characterImage || ''
    })
  }
  siblings.value.forEach(addToGroup)

  return Array.from(groups.entries()).map(([server, members]) => ({
    server,
    members
  }))
})

const handleResize = () => {
  syncOverviewWidth()
}

onMounted(() => {
  loadFavorites()
  loadHistory()
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
  }
  nextTick(() => {
    observeOverviewCard()
  })
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize)
  }
  overviewObserver?.disconnect()
})

watch(
  specialEquipmentsDetailed,
  newList => {
    if (!newList.some(special => special.item.name === hoveredSpecialName.value)) {
      handleSpecialHover(null)
    }
  },
  { deep: false }
)

watch(
  () => characterOverviewRef.value,
  () => {
    nextTick(() => {
      if (characterOverviewRef.value) {
        observeOverviewCard()
      } else {
        overviewObserver?.disconnect()
        overviewWidth.value = 0
      }
    })
  }
)

watch(activeResultTab, async () => {
  await nextTick()
  syncOverviewWidth()
  handleSpecialHover(null)
})

const searchCharacterByInput = () => {
  const name = characterName.value.trim()
  if (!name) {
    error.value = {
      title: '검색어 필요',
      message: '캐릭터명을 입력해 주세요.',
      type: 'warning'
    }
    return
  }
  executeSearch(name)
}

const searchCharacter = async (name: string) => {
  loading.value = true
  error.value = null
  character.value = null
  siblings.value = []
  selectedCharacterProfile.value = null
  detailEquipment.value = []
  detailEngravings.value = []
  detailError.value = null
  characterAvailability.value = {}

  try {
    const charResponse = await lostarkApi.getCharacter(name)
    character.value = charResponse.data
    characterName.value = name

    const siblingsResponse = await lostarkApi.getSiblings(name)
    const unique = new Map<string, SiblingCharacter>()
    siblingsResponse.data.forEach(member => {
      if (member.characterName === charResponse.data.characterName) return
      const key = `${member.serverName}-${member.characterName}`
      if (!unique.has(key)) {
        unique.set(key, member)
      }
    })
    siblings.value = Array.from(unique.values())

    await loadCharacterDetails(name, { profile: charResponse.data })
    activeResultTab.value = 'detail'
    await loadHistory()
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
  } finally {
    loading.value = false
  }
}
const retrySearch = () => {
  if (characterName.value) {
    searchCharacter(characterName.value)
  }
}

const dismissError = () => {
  error.value = null
}

const clearSearch = () => {
  characterName.value = ''
  character.value = null
  siblings.value = []
  error.value = null
  selectedCharacterProfile.value = null
  detailEquipment.value = []
  detailEngravings.value = []
  detailError.value = null
  characterAvailability.value = {}
  activeResultTab.value = 'detail'
  handleSpecialHover(null)
}

const loadFavorites = async () => {
  try {
    const response = await lostarkApi.getFavorites()
    favorites.value = response.data
  } catch (err) {
    console.error('즐겨찾기 로딩 실패:', err)
  }
}

const loadHistory = async () => {
  try {
    const response = await lostarkApi.getHistory()
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
  } catch (err) {
    console.error('검색 기록 로딩 실패:', err)
  }
}

const clearHistory = async () => {
  if (!confirm('검색 기록을 모두 삭제할까요?')) return
  try {
    await lostarkApi.clearHistory()
    history.value = []
  } catch (err) {
    console.error('검색 기록 삭제 실패:', err)
  }
}

const loadCharacterDetails = async (name: string, options: { profile?: CharacterProfile } = {}) => {
  detailLoading.value = true
  detailError.value = null
  characterAvailability.value[name] = 'loading'

  try {
    const profilePromise = options.profile
      ? Promise.resolve(options.profile)
      : lostarkApi.getCharacter(name).then(res => res.data)

    const [profile, equipmentResponse, engravingsResponse] = await Promise.all([
      profilePromise,
      lostarkApi.getEquipment(name),
      lostarkApi.getEngravings(name)
    ])

    selectedCharacterProfile.value = profile
    detailEquipment.value = equipmentResponse.data
    detailEngravings.value = engravingsResponse.data
    characterAvailability.value[name] = 'available'
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

const executeSearch = (name: string) => {
  const trimmed = name.trim()
  if (!trimmed) return
  characterName.value = trimmed
  searchCharacter(trimmed)
}

const handleSuggestionSelect = (suggestion: Suggestion) => {
  executeSearch(suggestion.name)
}

const viewCharacterDetail = (summary: CharacterProfile | SiblingCharacter) => {
  if (characterAvailability.value[summary.characterName] === 'loading') return
  activeResultTab.value = 'detail'
  const isPrimary = character.value?.characterName === summary.characterName
  const profile = isPrimary ? character.value : undefined
  loadCharacterDetails(summary.characterName, { profile })
}

const formatItemLevel = (value?: string | number) => {
  if (value === undefined || value === null) return '—'
  const raw = typeof value === 'number' ? value : Number(value.replace(/,/g, ''))
  if (Number.isNaN(raw)) return typeof value === 'string' ? value : '—'
  return raw.toFixed(2)
}

const extractTooltipLines = (tooltip?: string): string[] => {
  if (!tooltip) return []
  try {
    const raw = JSON.parse(tooltip)
    const normalize = (value: any): string[] => {
      if (!value) return []
      if (typeof value === 'string') return [cleanTooltipLine(value)]
      if (Array.isArray(value)) return value.flatMap(normalize)
      if (typeof value === 'object') {
        if ('value' in value) return normalize(value.value)
        return Object.values(value).flatMap(normalize)
      }
      return []
    }
    return Object.values(raw).flatMap(normalize).filter(Boolean)
  } catch {
    return [cleanTooltipLine(tooltip)]
  }
}

const addFallbackLineBreaks = (value: string): string => {
  if (!value) return value
  if (value.includes('\n')) return value

  const insertSentenceBreaks = (text: string) => {
    return text.replace(/(?<!\d)([.!?])\s+/g, (_, mark) => `${mark}\n`)
  }

  const insertStatBreaks = (text: string) => {
    return text.replace(/\s+(?=[+-]\d)/g, '\n')
  }

  const formatLines = (text: string) =>
    text
      .split('\n')
      .map(part => part.trim())
      .filter(Boolean)
      .join('\n')

  const withSentenceBreaks = insertSentenceBreaks(value)
  if (withSentenceBreaks.includes('\n')) {
    return formatLines(withSentenceBreaks)
  }

  const withStatBreaks = insertStatBreaks(value)
  if (withStatBreaks.includes('\n')) {
    return formatLines(withStatBreaks)
  }

  if (value.length <= 80) return value

  const segments: string[] = []
  let start = 0
  const length = value.length
  while (start < length) {
    let end = Math.min(start + 70, length)
    if (end === length) {
      segments.push(value.slice(start).trim())
      break
    }
    let breakIndex = value.lastIndexOf(' ', end)
    if (breakIndex <= start + 30) {
      breakIndex = value.indexOf(' ', end)
    }
    if (breakIndex === -1) {
      segments.push(value.slice(start).trim())
      break
    }
    segments.push(value.slice(start, breakIndex).trim())
    start = breakIndex + 1
  }
  return segments.join('\n')
}

const cleanTooltipLine = (text: string) => {
  const normalized = text
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/\\r\\n|\\n|\\r/g, '\n')
    .replace(/&[^;]+;/g, ' ')
    .split('\n')
    .map(part => part.replace(/\s+/g, ' ').trim())
    .filter(Boolean)
    .join('\n')
  return addFallbackLineBreaks(normalized)
}

const tooltipIgnorePatterns = [
  /캐릭터 귀속/,
  /거래 불가/,
  /분해 불가/,
  /판매 불가/,
  /선택 불가/,
  /획득 시 귀속/,
  /추가 설명/,
  /품질/i
]

const getSpecialHighlights = (item: Equipment): string[] => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return []
  const meaningfulLines = lines.filter(
    line => !tooltipIgnorePatterns.some(pattern => pattern.test(line))
  )
  const highlightRegex = /(기본 효과|추가 효과|연마 효과|슬롯 효과|추가 피해|전투 중|아크 패시브|내구도|소지품|이동 속도|선박|항해|효과|낙원력)/i
  const preferred = meaningfulLines.filter(line => highlightRegex.test(line))
  const source =
    preferred.length || meaningfulLines.length ? (preferred.length ? preferred : meaningfulLines) : lines
  return Array.from(new Set(source)).slice(0, 4)
}

const getSpecialLabel = (item: Equipment): string => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  const keyword = specialEquipmentKeywords.find(word => target.includes(word.toLowerCase()))
  if (keyword) return keyword
  if (item.type) return item.type
  return '항해 장비'
}

const normalizeStatValue = (value?: string | string[]) => {
  if (!value) return ''
  if (Array.isArray(value)) {
    return value.join(' / ')
  }
  return value
    .replace(/<br\s*\/?>/gi, ' / ')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
}

const formatProfileStat = (value?: string | string[]) => {
  const normalized = normalizeStatValue(value)
  return normalized.length ? normalized : '—'
}

const formatCombatPower = (value?: number | string) => {
  if (value === undefined || value === null) return '—'
  const raw = typeof value === 'number' ? value : Number(value.toString().replace(/,/g, ''))
  if (Number.isNaN(raw)) {
    return typeof value === 'string' && value.length ? value : '—'
  }
  return raw.toLocaleString()
}
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
  padding: 20px 40px;
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-color);
}

.page-header h1 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin: 0;
  font-weight: 700;
}

.header-search {
  flex: 1;
}

.header-search :deep(.autocomplete-container) {
  width: 100%;
}

.menu-button {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.menu-button span {
  width: 18px;
  height: 2px;
  background: var(--text-primary);
  display: block;
}

.menu-button:hover {
  background: var(--bg-hover);
  transform: translateY(-1px);
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 80;
}

.sidebar-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100%;
  background: var(--card-bg);
  border-right: 1px solid var(--border-color);
  box-shadow: 8px 0 20px rgba(15, 23, 42, 0.2);
  z-index: 90;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  outline: none;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-close {
  background: transparent;
  border: none;
  font-size: 1.6rem;
  color: var(--text-secondary);
  cursor: pointer;
}

.sidebar-content h3 {
  margin: 0 0 10px;
  color: var(--text-primary);
  font-size: 1rem;
}

.sidebar-placeholder {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.sidebar-placeholder-btn {
  width: 100%;
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px dashed var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-tertiary);
}

.sidebar-slide-enter-from,
.sidebar-slide-leave-to {
  transform: translateX(-100%);
}

.sidebar-slide-enter-active,
.sidebar-slide-leave-active {
  transition: transform 0.25s ease;
}

.sidebar-fade-enter-from,
.sidebar-fade-leave-to {
  opacity: 0;
}

.sidebar-fade-enter-active,
.sidebar-fade-leave-active {
  transition: opacity 0.25s ease;
}

.content-wrapper {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  background: var(--bg-secondary);
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  font-size: 1rem;
  border: 2px solid var(--input-border);
  border-radius: 8px;
  background: var(--input-bg);
  color: var(--text-primary);
}

.states-section {
  margin-bottom: 30px;
}

.states-section h2 {
  font-size: 1.3rem;
  color: var(--text-primary);
  margin-bottom: 15px;
  font-weight: 700;
}

.states-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.state-card {
  padding: 20px;
  background: var(--card-bg);
  border-radius: 12px;
  border: 2px solid var(--border-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 120px;
}

.state-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-weight: 600;
  margin-bottom: 10px;
}

.character-results {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.results-layout {
  display: flex;
  gap: 24px;
  align-items: stretch;
}

.results-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.view-tabs {
  display: flex;
  gap: 10px;
}

.view-tab-button {
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.view-tab-button.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.25);
}

.view-tab-button:not(.active):hover {
  background: var(--bg-hover);
}

.character-overview-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  background: var(--card-bg);
  border-radius: 20px;
  padding: 30px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  gap: 25px;
  flex: 0 0 380px;
  height: fit-content;
  overflow: visible;
}

.hero-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-levels-grid {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.hero-row--image {
  display: flex;
  gap: 16px;
  align-items: center;
}

.hero-row--image :deep(.hero-avatar) {
  border-radius: 16px;
  object-fit: cover;
}

.hero-title {
  font-size: 0.95rem;
  color: var(--text-secondary);
}

.hero-text h2 {
  margin: 4px 0;
  font-size: 1.6rem;
  color: var(--text-primary);
  text-align: center;
}

.hero-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.meta-item {
  padding: 10px 30px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.meta-item span {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  margin-right: 5px;
}

.meta-item strong {
  font-size: 1rem;
  color: var(--text-primary);
}

.hero-row--special {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: visible;
  align-items: center;
}

.special-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.special-header h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-secondary);
}

.special-count {
  font-size: 0.85rem;
  color: var(--text-tertiary);
}

.special-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.special-grid--icons {
  display: grid;
  grid-template-columns: repeat(4, minmax(90px, 1fr));
  gap: 12px 18px;
  overflow: visible;
  justify-items: center;
}

.special-icon-wrapper {
  position: relative;
  width: 100%;
  max-width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px;
  border: 2px solid transparent;
  border-radius: 16px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  box-sizing: border-box;
}

.special-icon-wrapper.is-hovered .special-icon-box {
  box-shadow: none;
}

.special-icon-wrapper.is-hovered {
  border-color: rgba(99, 102, 241, 0.45);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.12);
}

.special-icon-wrapper:focus-visible .special-icon-box {
  outline: 2px solid var(--primary-color);
  border-radius: 14px;
  outline-offset: 2px;
}

.special-icon-wrapper:focus-visible {
  border-color: var(--primary-color);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.25);
}

.special-icon-box {
  position: relative;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.special-icon-box :deep(.special-icon),
.special-icon {
  width: 100%;
  height: 100%;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--text-secondary);
}

.special-icon--fallback {
  font-size: 1.1rem;
}

.special-label {
  width: 100%;
  font-size: 0.8rem;
  color: var(--text-secondary);
  text-align: center;
  white-space: nowrap;
  line-height: 1.2;
}

.special-tooltip {
  position: relative;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  box-shadow: var(--shadow-md);
  width: 100%;
  opacity: 1;
  visibility: visible;
  transition: opacity 0.15s ease, transform 0.2s ease;
  pointer-events: auto;
  z-index: 15;
}

.special-tooltip::after {
  display: none;
}

.special-tooltip-layer {
  position: relative;
  width: 100%;
  display: flex;
  justify-content: center;
  pointer-events: none;
  margin-top: 20px;
}

.special-tooltip--global {
  text-align: left;
  pointer-events: auto;
}

.special-hover-indicator {
  position: absolute;
  left: 50%;
  bottom: -2px;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  pointer-events: none;
}

.special-hover-indicator::before {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-top: 10px solid rgba(99, 102, 241, 0.55);
  filter: drop-shadow(0 4px 6px rgba(15, 23, 42, 0.25));
}

.special-type {
  font-size: 0.85rem;
  color: var(--text-tertiary);
}

.special-highlights {
  margin: 0;
  /* padding-left: 18px; */
  color: var(--text-secondary);
  font-size: 0.85rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.special-highlights span {
  list-style: disc;
  white-space: pre-line;
}

.special-tooltip-empty {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-tertiary);
}

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.hero-row--profile-stats h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-secondary);
  text-align: center;
}

.profile-stat {
  padding: 10px 30px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.profile-stat span {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  word-break: keep-all;
  /* min-width: 100px; */
}

.profile-stat strong {
  font-size: 1rem;
  color: var(--text-primary);
}

.expedition-section,
.detail-panel {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

@media (max-width: 1024px) {
  .results-layout {
    flex-direction: column;
  }

  .character-overview-card {
    flex: 1;
  }
}

.section-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header-bar h3 {
  margin: 0;
  color: var(--text-primary);
}

.section-subtitle {
  margin: 4px 0 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.count-pill {
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-secondary);
}

.expedition-group + .expedition-group {
  margin-top: 20px;
}

.expedition-group h4 {
  margin: 0 0 10px 0;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.expedition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.expedition-card {
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 14px;
  background: var(--bg-secondary);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border 0.2s, transform 0.2s;
}

.expedition-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-3px);
}

.expedition-card.active {
  border-color: var(--primary-color);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.2);
}

.member-top {
  display: flex;
  justify-content: space-between;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.member-name {
  font-size: 1rem;
  color: var(--text-primary);
}

.member-ilvl {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.member-detail {
  font-size: 0.8rem;
  color: var(--primary-color);
  font-weight: 600;
}

.sidebar {
  width: 280px;
  background: var(--sidebar-bg);
  padding: 20px;
  overflow-y: auto;
  box-shadow: -2px 0 10px var(--sidebar-shadow);
}

.sidebar-section {
  margin-bottom: 30px;
}

.sidebar-section h2 {
  font-size: 1.1rem;
  color: var(--text-primary);
  margin: 0 0 15px 0;
  font-weight: 700;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.clear-btn-sm {
  padding: 4px 10px;
  font-size: 0.75rem;
  background: var(--error-color);
  color: var(--text-inverse);
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-weight: 600;
}

.empty-message {
  color: var(--text-tertiary);
  font-size: 0.85rem;
  padding: 10px;
  text-align: center;
}

.favorite-list,
.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-item:hover {
  background: var(--bg-hover);
}

.favorite-item :deep(.fav-image) {
  border-radius: 50%;
  object-fit: cover;
}

.fav-details {
  flex: 1;
}

.fav-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.fav-level {
  font-size: 0.8rem;
  color: var(--primary-color);
}

.history-item {
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.85rem;
  color: var(--text-primary);
}

.history-item:hover {
  background: var(--bg-hover);
}

@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column-reverse;
  }

  .sidebar {
    width: 100%;
    max-height: 250px;
  }

  .states-grid {
    grid-template-columns: 1fr;
  }

  .character-layout {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 640px) {
  .page-header {
    padding: 15px 20px;
    flex-direction: column;
    gap: 12px;
  }

  .page-header h1 {
    font-size: 1.2rem;
    text-align: center;
  }

  .header-search {
    width: 100%;
  }

  .main-content {
    padding: 20px 15px;
  }

  .hero-row--levels .hero-levels {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .profile-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
}

.hero-row--paradise h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-secondary);
  text-align: center;
}

.paradise-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.paradise-item {
  padding: 10px 30px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.paradise-item span {
  font-size: 0.8rem;
  color: var(--text-tertiary);
  min-width: 70px;
}

.paradise-item strong {
  font-size: 1rem;
  color: var(--text-primary);
}
</style>
