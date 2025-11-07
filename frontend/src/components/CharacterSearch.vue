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
              placeholder="캐릭터명을 입력하고 Enter 또는 추천어를 선택"
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
            <span class="hint-text">• Header autocomplete • Enter submits • Arrow selects suggestion</span>
          </div>

          <section class="states-section">
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
              title="캐릭터를 검색해주세요"
              description="캐릭터명을 입력하고 Enter를 누르거나 추천 목록에서 선택하세요."
            />
          </div>

          <section v-if="character && !loading" class="results-section">
            <h2>Results</h2>
            <div class="character-grid">
              <div 
                v-for="sibling in displayCharacters" 
                :key="sibling.characterName" 
                class="character-card"
                @click="openModalForCharacter(sibling)"
              >
                <div class="character-image">
                  <LazyImage
                    v-if="sibling.characterImage"
                    :src="sibling.characterImage"
                    :alt="sibling.characterName"
                    width="100%"
                    height="200"
                    imageClass="char-img"
                    errorIcon="👤"
                  />
                </div>
                <div class="character-info">
                  <h3>{{ sibling.characterName }}</h3>
                  <p class="character-class">
                    {{ sibling.characterClassName }} • iLv. {{ sibling.itemMaxLevel }}
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
          </section>
        </div>
      </main>

      <aside class="sidebar">
        <div class="sidebar-section">
          <h2>🌟 즐겨찾기</h2>
          <div v-if="favorites.length === 0" class="empty-message">
            즐겨찾기가 비어있습니다
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
                errorIcon="👤"
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
            <h2>🕒 최근 검색</h2>
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
    <!-- Character Detail Modal -->
    <CharacterDetailModal
      v-if="isModalOpen"
      :is-open="isModalOpen"
      :character="modalCharacter"
      :equipment="modalEquipment"
      :engravings="modalEngravings"
      @close="isModalOpen = false"
    />
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

// Modal state for Card → Modal (상세 보기)
const isModalOpen = ref(false)
const modalCharacter = ref<{ characterName: string; characterClassName: string; characterImage?: string; itemMaxLevel: string }>({
  characterName: '',
  characterClassName: '',
  characterImage: '',
  itemMaxLevel: ''
})
const modalEquipment = ref<Equipment[]>([])
const modalEngravings = ref<Engraving[]>([])

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
      info: `${fav.serverName} • ${fav.characterClassName}`,
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

onMounted(() => {
  loadFavorites()
  loadHistory()
})

const searchCharacterByInput = () => {
  if (!characterName.value.trim()) {
    error.value = {
      message: '캐릭터명을 입력해주세요.',
      type: 'warning'
    }
    return
  }
  searchCharacter(characterName.value.trim())
}

const searchCharacter = async (name: string) => {
  loading.value = true
  error.value = null
  character.value = null
  siblings.value = []

  try {
    const charResponse = await lostarkApi.getCharacter(name)
    character.value = charResponse.data
    characterName.value = name

    const siblingsResponse = await lostarkApi.getSiblings(name)
    siblings.value = siblingsResponse.data

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
    
    await Promise.all([...engravingsPromises, loadHistory()])
  } catch (err: any) {
    const errorData = err.response?.data
    if (err.response?.status === 404) {
      error.value = {
        title: '캐릭터를 찾을 수 없습니다',
        message: errorData?.message || `'${name}' 캐릭터가 존재하지 않습니다.`,
        type: 'error'
      }
    } else {
      error.value = {
        title: '검색 실패',
        message: errorData?.message || '알 수 없는 오류가 발생했습니다.',
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
    history.value = response.data
  } catch (err) {
    console.error('히스토리 로딩 실패:', err)
  }
}

const clearHistory = async () => {
  if (!confirm('검색 기록을 모두 삭제하시겠습니까?')) return
  try {
    await lostarkApi.clearHistory()
    history.value = []
  } catch (err) {
    console.error('히스토리 삭제 실패:', err)
  }
}

const handleSuggestionSelect = (suggestion: Suggestion) => {
  searchCharacter(suggestion.name)
}

// Open modal for selected character card
const openModalForCharacter = async (sibling: SiblingCharacter) => {
  modalCharacter.value = {
    characterName: sibling.characterName,
    characterClassName: sibling.characterClassName,
    characterImage: (sibling as any).characterImage || '',
    itemMaxLevel: sibling.itemMaxLevel
  }
  isModalOpen.value = true
  modalEquipment.value = []
  modalEngravings.value = []
  try {
    const [eq, eng] = await Promise.all([
      lostarkApi.getEquipment(sibling.characterName),
      lostarkApi.getEngravings(sibling.characterName)
    ])
    modalEquipment.value = eq.data
    modalEngravings.value = eng.data
  } catch (e) {
    // Fail silently in modal; keep it open with whatever data we have
    console.error('Failed to load modal data', e)
  }
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
