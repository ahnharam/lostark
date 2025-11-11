<template>
  <div class="app-container">
    <header class="page-header">
      <h1>LOA Character Search</h1>
      <ThemeToggle />
    </header>

    <div class="content-wrapper">
      <main class="main-content">
        <div class="search-container">
          <div class="search-section">
            <AutocompleteInput
              v-model="characterName"
              :suggestions="searchSuggestions"
              placeholder="캐릭터명을 입력하고 Enter를 누르거나 추천 목록에서 선택하세요"
              inputClass="search-input"
              :min-chars="0"
              :max-suggestions="8"
              @select="handleSuggestionSelect"
              @keyup.enter="searchCharacterByInput"
            />
            <button @click="searchCharacterByInput" class="search-button" :disabled="loading">
              {{ loading ? '검색 중...' : '검색' }}
            </button>
            <button @click="clearSearch" class="clear-button">
              Clear
            </button>
          </div>

          <div class="hints">
            <span class="hint-label">Hints:</span>
            <span class="hint-text">⌨️ 자동완성 · Enter로 검색 · 화살표로 추천 선택</span>
          </div>

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
              <div class="character-overview-card">
                <div class="hero-row hero-row--levels">
                  <div class="hero-levels">
                    <div class="level-item">
                      <span>전투 레벨</span>
                      <strong>Lv. {{ character.characterLevel != null ? character.characterLevel : '—' }}</strong>
                    </div>
                    <div class="level-item">
                      <span>아이템 레벨</span>
                      <strong>{{ formatItemLevel(character.itemAvgLevel) }}</strong>
                    </div>
                    <div class="level-item">
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

                <div class="hero-row hero-row--profile-stats" v-if="character.stats && character.stats.length">
                  <h3>전투 특성</h3>
                  <div class="profile-stats-grid">
                    <div
                      v-for="stat in character.stats"
                      :key="`${stat.type}-${stat.value}`"
                      class="profile-stat"
                    >
                      <span>{{ stat.type }}</span>
                      <strong>{{ formatProfileStat(stat.value) }}</strong>
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

                <section v-if="activeResultTab === 'detail'" class="detail-panel">
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
import { ref, computed, onMounted } from 'vue'
import { lostarkApi, type CharacterProfile, type SiblingCharacter, type SearchHistory, type Equipment, type Engraving } from '@/api/lostark'
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

const selectedCharacterProfile = ref<CharacterProfile | null>(null)
const detailEquipment = ref<Equipment[]>([])
const detailEngravings = ref<Engraving[]>([])
const detailLoading = ref(false)
const detailError = ref<string | null>(null)

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

onMounted(() => {
  loadFavorites()
  loadHistory()
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

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  background: var(--card-bg);
  padding: 20px;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.search-section :deep(.autocomplete-container) {
  flex: 1;
}

.search-button {
  padding: 0 18px;
  border-radius: 10px;
  border: none;
  background: var(--primary-color);
  color: var(--text-inverse);
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.search-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.clear-button {
  padding: 12px 24px;
  font-size: 0.9rem;
  background-color: var(--text-tertiary);
  color: var(--text-inverse);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.clear-button:hover {
  background-color: var(--text-secondary);
}

.hints {
  padding: 10px 20px;
  background: var(--bg-secondary);
  border-radius: 8px;
  margin-bottom: 25px;
  font-size: 0.85rem;
}

.hint-label {
  font-weight: 700;
  color: var(--text-primary);
  margin-right: 10px;
}

.hint-text {
  color: var(--text-tertiary);
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
  margin-top: 30px;
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
  padding: 24px 32px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  gap: 20px;
  flex: 0 0 380px;
  height: fit-content;
}

.hero-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-row--levels .hero-levels {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  gap: 12px;
}

.level-item {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin: 2px;
  border-radius: 10px;
  text-align: center;
}

.level-item span {
  color: var(--text-tertiary);
  font-size: 14px;
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
}

.hero-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
}

.meta-item {
  padding: 10px;
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

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.hero-row--profile-stats h3 {
  margin: 0;
  font-size: 1rem;
  color: var(--text-secondary);
}

.profile-stat {
  padding: 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
}

.profile-stat span {
  font-size: 0.8rem;
  color: var(--text-tertiary);
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
  }

  .page-header h1 {
    font-size: 1.2rem;
  }

  .main-content {
    padding: 20px 15px;
  }

  .search-section {
    flex-direction: column;
  }

  .hero-row--image {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-row--levels .hero-levels {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .profile-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }
}
</style>
