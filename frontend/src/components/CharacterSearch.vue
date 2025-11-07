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
              placeholder="罹먮┃?곕챸?��낅젰?섍퀬 Enter ?먮뒗 異붿쿇?대? ?좏깮"
              inputClass="search-input"
              :min-chars="0"
              :max-suggestions="8"
              @select="handleSuggestionSelect"
              @keyup.enter="searchCharacterByInput"
            />
            <button @click="clearSearch" class="clear-button">
              Clear
            </button>
          </div>

          <div class="hints">
            <span class="hint-label">Hints:</span>
            <span class="hint-text">??Header autocomplete ??Enter submits ??Arrow selects suggestion</span>
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
            <LoadingSpinner message="罹먮┃?��뺣낫瑜?遺덈윭?ㅻ뒗 以?.." />
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
              icon="?뵇"
              title="罹먮┃?곕? 寃?됲빐二쇱꽭??
              description="罹먮┃?곕챸?��낅젰?섍퀬 Enter瑜??꾨Ⅴ嫄곕굹 異붿쿇 紐⑸줉?먯꽌 ?좏깮?섏꽭??"
            />
          </div>

          <section v-if="character && !loading" class="results-section">
            <div class="result-tabs">
              <button
                v-for="tab in resultTabs"
                :key="tab.id"
                :class="['result-tab-button', { active: activeResultTab === tab.id }]"
                @click="activeResultTab = tab.id"
              >
                {{ tab.label }}
              </button>
            </div>

            <div v-show="activeResultTab === 'characters'" class="tab-panel">
              <h2>보유캐릭터</h2>
              <div class="character-grid">
                <div 
                  v-for="sibling in displayCharacters" 
                  :key="sibling.characterName" 
                  class="character-card"
                  :class="{ unavailable: characterAvailability[sibling.characterName] === 'unavailable' }"
                  @click="selectCharacterFromCard(sibling)"
                >
                  <div class="unavailable-badge" v-if="characterAvailability[sibling.characterName] === 'unavailable'">
                    정보 없음
                  </div>
                  <div class="character-image">
                    <LazyImage
                      v-if="getCharacterImage(sibling)"
                      :src="getCharacterImage(sibling)"
                      :alt="sibling.characterName"
                      width="100%"
                      height="200"
                      imageClass="char-img"
                      errorIcon="?��"
                    />
                  </div>
                  <div class="character-info">
                    <h3>{{ sibling.characterName }}</h3>
                    <p class="character-class">
                      {{ sibling.characterClassName }} ??iLv. {{ sibling.itemMaxLevel }}
                    </p>
                    <div class="engravings-mini" v-if="characterEngravings[sibling.characterName]">
                      <span 
                        v-for="(eng, idx) in characterEngravings[sibling.characterName].slice(0, 5)" 
                        :key="idx" 
                        class="engraving-tag"
                      >
                        {{ eng }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-show="activeResultTab === 'details'" class="tab-panel detail-panel">
              <CharacterDetailModal
                :character="selectedCharacterProfile"
                :equipment="detailEquipment"
                :engravings="detailEngravings"
                :loading="detailLoading"
                :error-message="detailError"
              />
            </div>
          </section>
        </div>
      </main>

      <aside class="sidebar">
        <div class="sidebar-section">
          <h2>?뙚 利먭꺼李얘린</h2>
          <div v-if="favorites.length === 0" class="empty-message">
            利먭꺼李얘린媛 鍮꾩뼱?덉뒿?덈떎
          </div>
          <div v-else class="favorite-list">
            <div
              v-for="fav in favorites"
              :key="fav.characterName"
              class="favorite-item"
              @click="searchCharacter(fav.characterName)"
            >
              <LazyImage
                v-if="fav.characterImage"
                :src="fav.characterImage"
                :alt="fav.characterName"
                width="40"
                height="40"
                imageClass="fav-image"
                errorIcon="?뫀"
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
            <h2>?븩 理쒓렐 寃??/h2>
            <button v-if="history.length > 0" @click="clearHistory" class="clear-btn-sm">
              ?꾩껜 ??젣
            </button>
          </div>
          <div v-if="history.length === 0" class="empty-message">
            寃??湲곕줉?��놁뒿?덈떎
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
const characterEngravings = ref<Record<string, string[]>>({})
const characterImages = ref<Record<string, string>>({})
const characterAvailability = ref<Record<string, 'available' | 'unavailable' | 'loading'>>({})

const resultTabs = [
  { id: 'characters', label: '보유캐릭터' },
  { id: 'details', label: '상세 보기' }
]
const activeResultTab = ref<'characters' | 'details'>('characters')

const selectedCharacterProfile = ref<CharacterProfile | null>(null)
const detailEquipment = ref<Equipment[]>([])
const detailEngravings = ref<Engraving[]>([])
const detailLoading = ref(false)
const detailError = ref<string | null>(null)

const displayCharacters = computed(() => {
  const chars: SiblingCharacter[] = []
  
  if (character.value) {
    chars.push({
      serverName: character.value.serverName,
      characterName: character.value.characterName,
      characterLevel: character.value.characterLevel || 0,
      characterClassName: character.value.characterClassName,
      itemAvgLevel: character.value.itemAvgLevel,
      itemMaxLevel: character.value.itemMaxLevel || character.value.itemAvgLevel,
      characterImage: character.value.characterImage || ''
    })
  }
  
  return [...chars, ...siblings.value].slice(0, 10)
})

const searchSuggestions = computed<Suggestion[]>(() => {
  const suggestions: Suggestion[] = []

  favorites.value.forEach(fav => {
    suggestions.push({
      id: `fav-${fav.characterName}`,
      name: fav.characterName,
      info: `${fav.serverName} ??${fav.characterClassName}`,
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
        info: '理쒓렐 寃??,
        isFavorite: false
      })
    }
  })

  return suggestions
})

onMounted(() => {
  loadFavorites()
  loadHistory()
})

const searchCharacter = async (name: string) => {
  loading.value = true
  error.value = null
  character.value = null
  siblings.value = []
  activeResultTab.value = 'characters'
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
    siblings.value = siblingsResponse.data

    if (character.value?.characterImage) {
      characterImages.value[character.value.characterName] = character.value.characterImage
    }

    const engravingsPromises = displayCharacters.value.map(async (char) => {
      try {
        const engResponse = await lostarkApi.getEngravings(char.characterName)
        const engNames = engResponse.data
          .map(e => e.name.split(' ')[0])
          .slice(0, 5)
        characterEngravings.value[char.characterName] = engNames
      } catch {
        characterEngravings.value[char.characterName] = []
      }
    })

    const historyPromise = loadHistory()
    await Promise.all([...engravingsPromises, historyPromise])
    await loadCharacterDetails(name, { profile: charResponse.data })
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
  activeResultTab.value = 'characters'
  characterAvailability.value = {}
}

const loadFavorites = async () => {
  try {
    const response = await lostarkApi.getFavorites()
    favorites.value = response.data
  } catch (err) {
    console.error('利먭꺼李얘린 濡쒕뵫 ?ㅽ뙣:', err)
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
    console.error('?덉뒪?좊━ 濡쒕뵫 ?ㅽ뙣:', err)
  }
}

const clearHistory = async () => {
  if (!confirm('寃??湲곕줉??紐⑤몢 ??젣?섏떆寃좎뒿?덇퉴?')) return
  try {
    await lostarkApi.clearHistory()
    history.value = []
  } catch (err) {
    console.error('?덉뒪?좊━ ??젣 ?ㅽ뙣:', err)
  }
}

const loadCharacterDetails = async (name: string, options: { profile?: CharacterProfile } = {}) => {
  detailLoading.value = true
  detailError.value = null
  activeResultTab.value = 'details'
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
    characterImages.value[name] = profile.characterImage || ''
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

const handleSuggestionSelect = (suggestion: Suggestion) => {
  searchCharacter(suggestion.name)
}

const selectCharacterFromCard = (sibling: SiblingCharacter) => {
  activeResultTab.value = 'details'
  if (characterAvailability.value[sibling.characterName] === 'unavailable') {
    detailError.value = `'${sibling.characterName}' 정보가 없어요.`
    selectedCharacterProfile.value = null
    return
  }
  loadCharacterDetails(sibling.characterName)
}

const getCharacterImage = (sibling: SiblingCharacter) => {
  return sibling.characterImage || characterImages.value[sibling.characterName] || ''
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

.results-section {
  margin-top: 30px;
}

.result-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.result-tab-button {
  padding: 10px 22px;
  border-radius: 999px;
  border: 2px solid var(--border-color);
  background: var(--card-bg);
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.result-tab-button.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
}

.tab-panel {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 20px;
  border: 1px solid var(--border-color);
}

.tab-panel.detail-panel {
  padding: 0;
  background: transparent;
  border: none;
  margin-top: 20px;
}

.results-section h2 {
  font-size: 1.3rem;
  color: var(--text-primary);
  margin-bottom: 20px;
  font-weight: 700;
}

.character-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.character-card {
  background: var(--card-bg);
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid var(--border-color);
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.character-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.character-image {
  width: 100%;
  height: 180px;
  background: var(--bg-secondary);
  overflow: hidden;
}

.character-image :deep(.char-img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.character-info {
  padding: 15px;
}

.character-card h3 {
  font-size: 1rem;
  color: var(--text-primary);
  margin: 0 0 6px 0;
  font-weight: 700;
}

.character-class {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin: 0 0 10px 0;
}

.engravings-mini {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.engraving-tag {
  padding: 3px 8px;
  background: var(--primary-color);
  color: var(--text-inverse);
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.character-card.unavailable {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

.unavailable-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.6);
  color: var(--text-inverse);
  border-radius: 999px;
  font-size: 0.75rem;
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

  .character-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
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

  .character-grid {
    grid-template-columns: 1fr;
  }
}
</style>

