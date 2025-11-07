<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2>🌟 즐겨찾기</h2>
        <ThemeToggle />
      </div>
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
          <img v-if="fav.characterImage" :src="fav.characterImage" alt="" />
          <div>
            <div class="fav-name">{{ fav.characterName }}</div>
            <div class="fav-level">{{ fav.itemMaxLevel }}</div>
          </div>
        </div>
      </div>

      <h2>🕒 최근 검색</h2>
      <div v-if="history.length === 0" class="empty-message">
        검색 기록이 없습니다
      </div>
      <div v-else>
        <div class="history-header">
          <button @click="clearHistory" class="clear-btn">전체 삭제</button>
        </div>
        <div class="history-list">
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

    <main class="main-content">
      <div class="search-container">
        <h1>로스트아크 캐릭터 검색</h1>
        
        <div class="search-box">
          <input
            v-model="characterName"
            @keyup.enter="searchCharacterByInput"
            type="text"
            placeholder="캐릭터명을 입력하세요"
            class="search-input"
          />
          <button @click="searchCharacterByInput" :disabled="loading" class="search-button">
            {{ loading ? '검색 중...' : '검색' }}
          </button>
        </div>

        <ErrorMessage
          v-if="error"
          :title="error.title"
          :message="error.message"
          :type="error.type"
          :retry="true"
          :dismissible="true"
          @retry="retrySearch"
          @dismiss="dismissError"
        />

        <LoadingSpinner v-if="loading" message="캐릭터 정보를 불러오는 중..." />

        <div v-if="character && !loading" class="character-info">
          <div class="character-header">
            <img 
              v-if="character.characterImage" 
              :src="character.characterImage" 
              :alt="character.characterName"
              class="character-image"
            />
            <div class="character-basic">
              <div class="header-top">
                <h2>{{ character.characterName }}</h2>
                <button @click="toggleFavorite" class="favorite-btn">
                  {{ isFavorite ? '⭐' : '☆' }}
                </button>
              </div>
              <p class="server">{{ character.serverName }}</p>
            </div>
          </div>

          <div class="tabs">
            <button
              v-for="tab in tabs"
              :key="tab.id"
              :class="['tab', { active: currentTab === tab.id }]"
              @click="currentTab = tab.id"
            >
              {{ tab.name }}
            </button>
          </div>

          <div class="tab-content">
            <div v-if="currentTab === 'basic'" class="basic-info">
              <div class="detail-item">
                <span class="label">클래스:</span>
                <span class="value">{{ character.characterClassName }}</span>
              </div>
              <div class="detail-item">
                <span class="label">아이템 레벨:</span>
                <span class="value highlight">{{ character.itemAvgLevel }}</span>
              </div>
              <div class="detail-item" v-if="character.expeditionLevel">
                <span class="label">원정대 레벨:</span>
                <span class="value">{{ character.expeditionLevel }}</span>
              </div>
              <div class="detail-item" v-if="character.guildName">
                <span class="label">길드:</span>
                <span class="value">{{ character.guildName }}</span>
              </div>
              <div class="detail-item" v-if="character.pvpGradeName">
                <span class="label">PVP 등급:</span>
                <span class="value">{{ character.pvpGradeName }}</span>
              </div>
            </div>

            <div v-if="currentTab === 'equipment'" class="equipment-info">
              <LoadingSpinner v-if="loadingEquipment" message="장비 정보 로딩 중..." />
              <EmptyState
                v-else-if="equipment.length === 0"
                icon="🎒"
                title="장비 정보 없음"
                description="이 캐릭터의 장비 정보를 불러올 수 없습니다."
              />
              <div v-else class="equipment-grid">
                <div
                  v-for="item in equipment"
                  :key="item.name"
                  class="equipment-item clickable"
                  @click="showEquipmentDetail(item)"
                >
                  <img v-if="item.icon" :src="item.icon" :alt="item.name" />
                  <div class="equipment-details">
                    <div class="equipment-type">{{ item.type }}</div>
                    <div class="equipment-name" :class="item.grade">{{ item.name }}</div>
                  </div>
                </div>
              </div>

              <!-- 장비 상세 모달 -->
              <div v-if="selectedEquipment" class="equipment-modal" @click="selectedEquipment = null">
                <div class="modal-content" @click.stop>
                  <button class="modal-close" @click="selectedEquipment = null">×</button>
                  <h3>{{ selectedEquipment.name }}</h3>
                  <div class="modal-info">
                    <span class="modal-type">{{ selectedEquipment.type }}</span>
                    <span class="modal-grade" :class="selectedEquipment.grade">{{ selectedEquipment.grade }}</span>
                  </div>
                  <div v-if="selectedEquipment.tooltip" class="modal-tooltip" v-html="parseTooltip(selectedEquipment.tooltip)"></div>
                </div>
              </div>
            </div>

            <div v-if="currentTab === 'engravings'" class="engravings-info">
              <LoadingSpinner v-if="loadingEngravings" message="각인 정보 로딩 중..." />
              <EmptyState
                v-else-if="engravings.length === 0"
                icon="📜"
                title="각인 정보 없음"
                description="이 캐릭터의 각인 정보를 불러올 수 없습니다."
              />
              <div v-else class="engravings-grid">
                <div v-for="eng in engravings" :key="eng.name" class="engraving-item">
                  <img v-if="eng.icon" :src="eng.icon" :alt="eng.name" />
                  <div class="engraving-name">{{ eng.name }}</div>
                </div>
              </div>
            </div>

            <div v-if="currentTab === 'siblings'" class="siblings-info">
              <LoadingSpinner v-if="loadingSiblings" message="보유 캐릭터 로딩 중..." />
              <EmptyState
                v-else-if="siblings.length === 0"
                icon="👥"
                title="보유 캐릭터 없음"
                description="이 계정의 다른 캐릭터 정보를 불러올 수 없습니다."
              />
              <div v-else class="siblings-by-server">
                <div v-for="(chars, serverName) in groupedSiblings" :key="serverName" class="server-group">
                  <h3 class="server-name">{{ serverName }}</h3>
                  <div class="siblings-grid">
                    <div
                      v-for="sibling in chars"
                      :key="sibling.characterName"
                      class="sibling-item"
                      @click="searchCharacter(sibling.characterName)"
                    >
                      <div class="sibling-name">{{ sibling.characterName }}</div>
                      <div class="sibling-class">{{ sibling.characterClassName }}</div>
                      <div class="sibling-level">{{ sibling.itemMaxLevel }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { lostarkApi, type CharacterProfile, type Equipment, type Engraving, type SiblingCharacter, type SearchHistory } from '@/api/lostark'
import LoadingSpinner from './common/LoadingSpinner.vue'
import ErrorMessage from './common/ErrorMessage.vue'
import EmptyState from './common/EmptyState.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import { useTheme } from '@/composables/useTheme'

// 테마 초기화
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
const isFavorite = ref(false)

const equipment = ref<Equipment[]>([])
const loadingEquipment = ref(false)
const selectedEquipment = ref<Equipment | null>(null)

const engravings = ref<Engraving[]>([])
const loadingEngravings = ref(false)

const siblings = ref<SiblingCharacter[]>([])
const loadingSiblings = ref(false)

const favorites = ref<CharacterProfile[]>([])
const history = ref<SearchHistory[]>([])

const currentTab = ref('basic')
const tabs = [
  { id: 'basic', name: '기본 정보' },
  { id: 'equipment', name: '장비' },
  { id: 'engravings', name: '각인' },
  { id: 'siblings', name: '보유 캐릭터' },
]

// 보유 캐릭터를 서버별로 그룹핑하고 아이템 레벨 순으로 정렬
const groupedSiblings = computed(() => {
  const grouped: Record<string, SiblingCharacter[]> = {}

  siblings.value.forEach(sibling => {
    if (!grouped[sibling.serverName]) {
      grouped[sibling.serverName] = []
    }
    grouped[sibling.serverName].push(sibling)
  })

  // 각 서버 내에서 아이템 레벨 높은 순으로 정렬
  Object.keys(grouped).forEach(serverName => {
    grouped[serverName].sort((a, b) => {
      // null 체크 추가
      const levelA = a.itemMaxLevel ? parseFloat(a.itemMaxLevel.replace(/,/g, '')) : 0
      const levelB = b.itemMaxLevel ? parseFloat(b.itemMaxLevel.replace(/,/g, '')) : 0
      return levelB - levelA
    })
  })

  return grouped
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
  equipment.value = []
  engravings.value = []
  siblings.value = []
  currentTab.value = 'basic'

  try {
    const response = await lostarkApi.getCharacter(name)
    character.value = response.data
    characterName.value = name

    await Promise.all([
      checkFavoriteStatus(name),
      loadHistory()
    ])
  } catch (err: any) {
    const errorData = err.response?.data

    if (err.response?.status === 404) {
      error.value = {
        title: '캐릭터를 찾을 수 없습니다',
        message: errorData?.message || `'${name}' 캐릭터가 존재하지 않습니다. 캐릭터명을 확인해주세요.`,
        type: 'error'
      }
    } else if (err.response?.status === 503) {
      error.value = {
        title: 'API 서비스 오류',
        message: errorData?.message || '로스트아크 API 서비스에 일시적인 문제가 발생했습니다.',
        type: 'warning'
      }
    } else {
      error.value = {
        title: '검색 실패',
        message: errorData?.message || '알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
        type: 'error'
      }
    }
    console.error('검색 실패:', err)
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

const loadEquipment = async () => {
  if (!character.value || equipment.value.length > 0) return
  
  loadingEquipment.value = true
  try {
    const response = await lostarkApi.getEquipment(character.value.characterName)
    equipment.value = response.data
  } catch (err) {
    console.error('장비 정보 로딩 실패:', err)
  } finally {
    loadingEquipment.value = false
  }
}

const loadEngravings = async () => {
  if (!character.value || engravings.value.length > 0) return
  
  loadingEngravings.value = true
  try {
    const response = await lostarkApi.getEngravings(character.value.characterName)
    engravings.value = response.data
  } catch (err) {
    console.error('각인 정보 로딩 실패:', err)
  } finally {
    loadingEngravings.value = false
  }
}

const loadSiblings = async () => {
  if (!character.value || siblings.value.length > 0) return
  
  loadingSiblings.value = true
  try {
    const response = await lostarkApi.getSiblings(character.value.characterName)
    siblings.value = response.data
  } catch (err) {
    console.error('보유 캐릭터 로딩 실패:', err)
  } finally {
    loadingSiblings.value = false
  }
}

const checkFavoriteStatus = async (name: string) => {
  try {
    const response = await lostarkApi.checkFavorite(name)
    isFavorite.value = response.data.isFavorite
  } catch (err) {
    console.error('즐겨찾기 상태 확인 실패:', err)
  }
}

const toggleFavorite = async () => {
  if (!character.value) return
  
  try {
    if (isFavorite.value) {
      await lostarkApi.removeFavorite(character.value.characterName)
      isFavorite.value = false
    } else {
      await lostarkApi.addFavorite(character.value.characterName)
      isFavorite.value = true
    }
    await loadFavorites()
  } catch (err) {
    console.error('즐겨찾기 토글 실패:', err)
  }
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

// 장비 상세 정보 표시
const showEquipmentDetail = (item: Equipment) => {
  selectedEquipment.value = item
}

// tooltip HTML 파싱 (간단한 텍스트 표시)
const parseTooltip = (tooltip: string) => {
  if (!tooltip) return ''

  try {
    // JSON 파싱 시도
    const parsed = JSON.parse(tooltip)

    // 로스트아크 tooltip 구조에 따라 간단하게 표시
    let html = '<div class="tooltip-content">'

    if (typeof parsed === 'object') {
      // 객체인 경우 주요 정보만 표시
      Object.keys(parsed).forEach(key => {
        if (typeof parsed[key] === 'string' || typeof parsed[key] === 'number') {
          html += `<div class="tooltip-line"><strong>${key}:</strong> ${parsed[key]}</div>`
        }
      })
    } else {
      html += `<pre>${JSON.stringify(parsed, null, 2)}</pre>`
    }

    html += '</div>'
    return html
  } catch (e) {
    // JSON이 아닌 경우 그대로 표시
    return `<div class="tooltip-text">${tooltip}</div>`
  }
}

// 탭 변경 시 데이터 로딩
const watchTab = () => {
  if (currentTab.value === 'equipment') {
    loadEquipment()
  } else if (currentTab.value === 'engravings') {
    loadEngravings()
  } else if (currentTab.value === 'siblings') {
    loadSiblings()
  }
}

// currentTab 변경 감지
import { watch } from 'vue'
watch(currentTab, watchTab)
</script>

<style scoped>
.app-container {
  display: flex;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
}

.sidebar {
  width: 280px;
  background: var(--sidebar-bg);
  padding: 20px;
  overflow-y: auto;
  box-shadow: 2px 0 10px var(--sidebar-shadow);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--border-color-light);
}

.sidebar h2 {
  font-size: 1.2rem;
  margin: 0;
  color: var(--text-primary);
}

.empty-message {
  color: var(--text-tertiary);
  font-size: 0.9rem;
  padding: 10px;
  text-align: center;
}

.favorite-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.favorite-item:hover {
  background: var(--bg-hover);
}

.favorite-item img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.fav-name {
  font-weight: 600;
  color: var(--text-primary);
}

.fav-level {
  font-size: 0.85rem;
  color: var(--primary-color);
}

.history-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.clear-btn {
  padding: 5px 10px;
  font-size: 0.85rem;
  background: var(--error-color);
  color: var(--text-inverse);
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.history-item {
  padding: 10px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 0.9rem;
}

.history-item:hover {
  background: var(--bg-hover);
}

.main-content {
  flex: 1;
  padding: 40px 20px;
  overflow-y: auto;
}

.search-container {
  max-width: 1000px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  color: var(--text-inverse);
  font-size: 2.5rem;
  margin-bottom: 40px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  padding: 15px 20px;
  font-size: 1.1rem;
  border: 2px solid var(--input-border);
  border-radius: 10px;
  background: var(--input-bg);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--input-focus-shadow);
}

.search-button {
  padding: 15px 40px;
  font-size: 1.1rem;
  background-color: var(--success-color);
  color: var(--text-inverse);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
  box-shadow: var(--shadow-sm);
}

.search-button:hover:not(:disabled) {
  background-color: var(--success-hover);
}

.search-button:disabled {
  background-color: var(--text-tertiary);
  cursor: not-allowed;
}

.character-info {
  background: var(--card-bg);
  border-radius: 15px;
  padding: 30px;
  box-shadow: var(--shadow-lg);
  animation: fadeIn 0.5s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.character-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid var(--border-color-light);
}

.character-image {
  width: 120px;
  height: 120px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: var(--shadow-md);
}

.character-basic {
  flex: 1;
}

.header-top {
  display: flex;
  align-items: center;
  gap: 15px;
}

.character-basic h2 {
  color: var(--text-primary);
  margin: 0;
  font-size: 2rem;
}

.favorite-btn {
  font-size: 2rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
}

.favorite-btn:hover {
  transform: scale(1.2);
}

.server {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin: 10px 0 0 0;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid var(--border-color-light);
}

.tab {
  padding: 10px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  color: var(--text-secondary);
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab:hover {
  color: var(--primary-color);
}

.tab.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
  font-weight: 600;
}

.tab-content {
  min-height: 200px;
}

.basic-info {
  display: grid;
  gap: 15px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 15px;
  background-color: var(--bg-secondary);
  border-radius: 8px;
}

.label {
  font-weight: 600;
  color: var(--text-secondary);
}

.value {
  color: var(--text-primary);
  font-weight: 500;
}

.value.highlight {
  color: var(--primary-color);
  font-size: 1.2rem;
  font-weight: 700;
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.equipment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 8px;
  transition: all 0.3s;
}

.equipment-item.clickable {
  cursor: pointer;
}

.equipment-item.clickable:hover {
  background: var(--bg-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.equipment-item img {
  width: 50px;
  height: 50px;
  object-fit: contain;
}

.equipment-details {
  flex: 1;
}

.equipment-type {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.equipment-name {
  font-weight: 600;
  margin-top: 5px;
  color: var(--text-primary);
}

/* 장비 상세 모달 */
.equipment-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--modal-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.3s;
}

.modal-content {
  background: var(--modal-bg);
  border-radius: 15px;
  padding: 30px;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  box-shadow: var(--shadow-xl);
}

.modal-close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 2rem;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  line-height: 1;
}

.modal-close:hover {
  color: var(--text-primary);
}

.modal-content h3 {
  margin: 0 0 15px 0;
  color: var(--text-primary);
  font-size: 1.5rem;
}

.modal-info {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.modal-type,
.modal-grade {
  padding: 5px 12px;
  border-radius: 5px;
  font-size: 0.9rem;
  font-weight: 600;
}

.modal-type {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.modal-grade {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.modal-tooltip {
  background: var(--bg-secondary);
  padding: 15px;
  border-radius: 8px;
  font-size: 0.9rem;
  line-height: 1.6;
}

.tooltip-content {
  color: var(--text-primary);
}

.tooltip-line {
  margin-bottom: 8px;
  padding: 5px 0;
  border-bottom: 1px solid var(--border-color);
}

.tooltip-line:last-child {
  border-bottom: none;
}

.tooltip-text {
  white-space: pre-wrap;
  word-break: break-word;
}

.engravings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.engraving-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  background: var(--bg-secondary);
  border-radius: 8px;
  text-align: center;
}

.engraving-item img {
  width: 60px;
  height: 60px;
  margin-bottom: 10px;
}

.engraving-name {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.siblings-by-server {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.server-group {
  background: var(--bg-secondary);
  padding: 20px;
  border-radius: 12px;
}

.server-name {
  color: var(--primary-color);
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0 0 15px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid var(--primary-color);
}

.siblings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.sibling-item {
  padding: 20px;
  background: var(--card-bg);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  text-align: center;
  border: 2px solid transparent;
}

.sibling-item:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
  transform: translateY(-5px);
  box-shadow: 0 5px 15px var(--shadow-color);
  border-color: var(--primary-color);
}

.sibling-name {
  font-weight: 700;
  font-size: 1.1rem;
  margin-bottom: 5px;
  color: var(--text-primary);
}

.sibling-item:hover .sibling-name {
  color: var(--text-inverse);
}

.sibling-class {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.sibling-item:hover .sibling-class {
  color: rgba(255, 255, 255, 0.8);
}

.sibling-level {
  color: var(--primary-color);
  font-weight: 600;
  font-size: 1rem;
}

.sibling-item:hover .sibling-level {
  color: var(--text-inverse);
}

/* 모바일 반응형 */
@media (max-width: 1024px) {
  .app-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    max-height: 300px;
    overflow-y: auto;
  }

  h1 {
    font-size: 2rem;
  }

  .character-image {
    width: 80px;
    height: 80px;
  }

  .character-basic h2 {
    font-size: 1.5rem;
  }

  .equipment-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .siblings-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }

  .tabs {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .tab {
    flex-shrink: 0;
  }
}

@media (max-width: 640px) {
  .main-content {
    padding: 20px 10px;
  }

  .search-container {
    padding: 0 10px;
  }

  h1 {
    font-size: 1.5rem;
    margin-bottom: 20px;
  }

  .search-box {
    flex-direction: column;
  }

  .search-button {
    width: 100%;
    padding: 12px;
  }

  .character-header {
    flex-direction: column;
    text-align: center;
  }

  .character-image {
    width: 100px;
    height: 100px;
  }

  .header-top {
    justify-content: center;
  }

  .character-basic h2 {
    font-size: 1.3rem;
  }

  .character-info {
    padding: 20px;
  }

  .tabs {
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .tabs::-webkit-scrollbar {
    display: none;
  }

  .tab {
    padding: 8px 16px;
    font-size: 0.9rem;
    white-space: nowrap;
  }

  .equipment-grid {
    grid-template-columns: 1fr;
  }

  .engravings-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .siblings-grid {
    grid-template-columns: 1fr;
  }

  .modal-content {
    max-width: 90%;
    max-height: 90vh;
    padding: 20px;
  }

  .sidebar h2 {
    font-size: 1rem;
  }

  .favorite-list,
  .history-list {
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>
