<template>
  <div class="character-search__submenu-search">
    <AutocompleteInput
      :model-value="characterName"
      :suggestions="suggestions"
      placeholder="캐릭터명 입력 (최소 2자)"
      :min-search-length="2"
      @update:model-value="$emit('update:characterName', $event)"
      @select="$emit('select', $event)"
      @keyup.enter="$emit('search')"
      @focus="$emit('focus')"
    />

    <div v-if="showPanel" class="search-panel-dropdown">
      <div class="search-panel-tabs">
        <button
          type="button"
          class="search-panel-tab"
          :class="{ active: activePanelTab === 'recent' }"
          @click="$emit('update:activePanelTab', 'recent')"
        >
          최근 검색
        </button>
        <button
          type="button"
          class="search-panel-tab"
          :class="{ active: activePanelTab === 'favorites' }"
          @click="$emit('update:activePanelTab', 'favorites')"
        >
          내 즐겨찾기
        </button>
      </div>

      <div class="search-panel-content">
        <template v-if="activePanelTab === 'recent'">
          <div class="panel-section-header">
            <span>최근 검색</span>
            <button
              v-if="historyItems.length > 0"
              type="button"
              class="panel-clear-btn"
              @click="$emit('clear-history')"
            >
              전체 삭제
            </button>
          </div>
          <div v-if="filteredHistoryItems.length === 0" class="panel-empty">
            {{ historyItems.length === 0 ? '검색 기록이 없습니다' : '일치하는 검색 기록이 없습니다' }}
          </div>
          <ul v-else class="panel-list">
            <li
              v-for="item in filteredHistoryItems"
              :key="item.id"
            >
              <button
                type="button"
                class="panel-list-item"
                @click="$emit('select-history', item.characterName)"
              >
                <span class="panel-list-name">{{ item.characterName }}</span>
                <span class="panel-list-meta">🕒</span>
              </button>
            </li>
          </ul>
        </template>

        <template v-else>
          <div class="panel-section-header">
            <span>내 즐겨찾기</span>
          </div>
          <div v-if="filteredFavoriteItems.length === 0" class="panel-empty">
            {{ favoriteItems.length === 0 ? '즐겨찾기가 비어있어요' : '일치하는 즐겨찾기가 없습니다' }}
          </div>
          <div v-else class="panel-favorite-list">
            <button
              v-for="fav in filteredFavoriteItems"
              :key="fav.characterName"
              type="button"
              class="panel-favorite-item"
              @click="$emit('select-favorite', fav.characterName)"
            >
              <LazyImage
                :src="fav.characterImage || ''"
                :alt="fav.characterName"
                width="38"
                height="38"
                imageClass="panel-favorite-image"
                errorIcon="❔"
              />
              <div class="panel-favorite-details">
                <span class="panel-favorite-name">{{ fav.characterName }}</span>
                <span class="panel-favorite-meta">
                  {{ fav.serverName }} · {{ formatItemLevel(fav.itemMaxLevel || fav.itemAvgLevel) }}
                </span>
              </div>
            </button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AutocompleteInput from '@/components/common/AutocompleteInput.vue'
import type { Suggestion } from '@/components/common/AutocompleteInput.vue'
import LazyImage from '@/components/common/LazyImage.vue'
import type { CharacterProfile } from '@/api/types/armory'

/**
 * 검색 기록 아이템 (제네릭 타입)
 */
export interface HistoryItem {
  id: string | number
  characterName: string
  [key: string]: any
}

const props = defineProps<{
  /**
   * 현재 입력된 캐릭터명
   */
  characterName: string

  /**
   * 자동완성 제안 목록
   */
  suggestions: Suggestion[]

  /**
   * 패널 표시 여부
   */
  showPanel: boolean

  /**
   * 현재 활성 패널 탭
   */
  activePanelTab: 'recent' | 'favorites'

  /**
   * 검색 기록 목록
   */
  historyItems: HistoryItem[]

  /**
   * 즐겨찾기 목록
   */
  favoriteItems: CharacterProfile[]

  /**
   * 필터링된 검색 기록
   */
  filteredHistoryItems: HistoryItem[]

  /**
   * 필터링된 즐겨찾기
   */
  filteredFavoriteItems: CharacterProfile[]
}>()

defineEmits<{
  /**
   * 캐릭터명 입력 변경
   */
  'update:characterName': [value: string]

  /**
   * 자동완성 항목 선택
   */
  select: [value: any]

  /**
   * 검색 실행 (Enter)
   */
  search: []

  /**
   * 검색창 포커스
   */
  focus: []

  /**
   * 활성 패널 탭 변경
   */
  'update:activePanelTab': [tab: 'recent' | 'favorites']

  /**
   * 검색 기록 전체 삭제
   */
  'clear-history': []

  /**
   * 검색 기록 항목 선택
   */
  'select-history': [characterName: string]

  /**
   * 즐겨찾기 항목 선택
   */
  'select-favorite': [characterName: string]
}>()

/**
 * 아이템 레벨 포맷 (소수점 2자리)
 */
const formatItemLevel = (value?: string): string => {
  if (!value) return '0.00'
  const numeric = parseFloat(String(value).replace(/[^\d.]/g, ''))
  if (!Number.isFinite(numeric)) return '0.00'
  return numeric.toFixed(2)
}
</script>

<style scoped>
.character-search__submenu-search {
  position: relative;
  width: 100%;
  max-width: 400px;
}

.search-panel-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  max-height: 400px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.search-panel-tabs {
  display: flex;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f8f8;
}

.search-panel-tab {
  flex: 1;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  font-size: 0.875rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
}

.search-panel-tab:hover {
  background: #f0f0f0;
  color: #333;
}

.search-panel-tab.active {
  color: #4a90e2;
  background: #fff;
  border-bottom-color: #4a90e2;
}

.search-panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.panel-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  font-size: 0.75rem;
  font-weight: 600;
  color: #888;
  text-transform: uppercase;
}

.panel-clear-btn {
  padding: 0.25rem 0.5rem;
  background: none;
  border: none;
  font-size: 0.75rem;
  color: #e74c3c;
  cursor: pointer;
  transition: color 0.2s;
}

.panel-clear-btn:hover {
  color: #c0392b;
}

.panel-empty {
  padding: 2rem 1rem;
  text-align: center;
  font-size: 0.875rem;
  color: #999;
}

.panel-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.panel-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 0.75rem 1rem;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
  border-radius: 6px;
}

.panel-list-item:hover {
  background: #f0f7ff;
}

.panel-list-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: #333;
}

.panel-list-meta {
  font-size: 0.75rem;
  color: #999;
}

.panel-favorite-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.panel-favorite-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  background: none;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  text-align: left;
}

.panel-favorite-item:hover {
  background: #f0f7ff;
}

:deep(.panel-favorite-image) {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
}

.panel-favorite-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
  min-width: 0;
}

.panel-favorite-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-favorite-meta {
  font-size: 0.75rem;
  color: #888;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
