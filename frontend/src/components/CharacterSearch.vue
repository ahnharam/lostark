<template>
  <div class="character-search">
    <header class="page-header">
      <div class="header-left">
        <h1>LOA Character Search</h1>
      </div>
      <div class="header-search">
        <div class="header-search__row">
          <div class="search-panel-wrapper" ref="searchPanelWrapperRef">
            <AutocompleteInput
              v-model="characterName"
              :suggestions="[]"
              placeholder="캐릭터명을 입력하세요"
              inputClass="search-input"
              :min-chars="1"
              :max-suggestions="8"
              @select="handleSuggestionSelect"
              @keyup.enter="searchCharacterByInput"
              @focus="handleSearchFocus"
            />

            <div v-if="shouldShowSearchPanel" class="search-panel-dropdown">
              <div class="search-panel-tabs">
                <button
                  type="button"
                  class="search-panel-tab"
                  :class="{ active: activeSearchPanelTab === 'recent' }"
                  @click="activeSearchPanelTab = 'recent'"
                >
                  최근 검색
                </button>
                <button
                  type="button"
                  class="search-panel-tab"
                  :class="{ active: activeSearchPanelTab === 'favorites' }"
                  @click="activeSearchPanelTab = 'favorites'"
                >
                  내 즐겨찾기
                </button>
              </div>

              <div class="search-panel-content">
                <template v-if="activeSearchPanelTab === 'recent'">
                  <div class="panel-section-header">
                    <span>최근 검색</span>
                    <button
                      v-if="history.length > 0"
                      type="button"
                      class="panel-clear-btn"
                      @click="clearHistory"
                    >
                      전체 삭제
                    </button>
                  </div>
                  <div v-if="panelHistoryItems.length === 0" class="panel-empty">
                    {{ history.length === 0 ? '검색 기록이 없습니다' : '일치하는 검색 기록이 없습니다' }}
                  </div>
                  <ul v-else class="panel-list">
                    <li
                      v-for="item in panelHistoryItems"
                      :key="item.id"
                    >
                      <button
                        type="button"
                        class="panel-list-item"
                        @click="handleSearchPanelSelect(item.characterName)"
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
                  <div v-if="panelFavoriteItems.length === 0" class="panel-empty">
                    {{ favorites.length === 0 ? '즐겨찾기가 비어있어요' : '일치하는 즐겨찾기가 없습니다' }}
                  </div>
                  <div v-else class="panel-favorite-list">
                    <button
                      v-for="fav in panelFavoriteItems"
                      :key="fav.characterName"
                      type="button"
                      class="panel-favorite-item"
                      @click="handleSearchPanelSelect(fav.characterName)"
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
        </div>
      </div>
      <div class="header-right" aria-hidden="true">
        <div v-if="character && !loading" class="view-tabs view-tabs--header">
            <button
              v-for="tab in resultTabs"
              :key="tab.key"
              class="view-tab-button"
              type="button"
              :class="{ active: activeResultTab === tab.key }"
              @click="activeResultTab = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>
      </div>
    </header>

    <div class="search-container">
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
              description="캐릭터명을 입력하고 Enter를 누르거나 최근검색 혹은 즐겨찾기에서 선택해주세요."
            />
          </div>

          <section v-if="character && !loading" class="character-results">
            <div class="results-layout">
              <CharacterOverviewCard
                :active-character="activeCharacter"
                :has-character-image="hasCharacterImage"
                :is-hero-image-large="isHeroImageLarge"
                :is-character-favorite="isCharacterFavorite"
                :last-refreshed-label="lastRefreshedLabel"
                :display-stats="displayStats"
                :paradise-info="paradiseInfo"
                :special-equipments="specialEquipmentsDetailed"
                :combat-role="combatRole"
                :combat-role-loading="arkGridLoading"
                :honor-point="activeCharacter?.honorPoint"
                :loading="loading"
                :format-combat-power="formatCombatPower"
                :format-integer="formatInteger"
                :format-item-level="formatItemLevel"
                :format-profile-stat="formatProfileStat"
                @refresh="handleRefreshClick"
                @expand-hero="expandHeroImage"
                @shrink-hero="shrinkHeroImage"
                @open-hero-popup="openHeroImagePopup"
                @toggle-favorite="toggleFavorite"
              />

              <div class="results-panel">
                <CharacterSummaryPanel
                  v-if="activeResultTab === 'summary'"
                  :active-character="activeCharacter"
                  :equipment-summary="equipmentSummary"
                  :detail-loading="detailLoading"
                  :detail-error="detailError"
                  :ark-summary="arkSummary"
                  :ark-grid-loading="arkGridLoading"
                  :ark-grid-error="arkGridError"
                :skill-highlights="skillHighlights"
                :skill-loose-gems="skillLooseGems"
                :skill-loading="skillLoading"
                :skill-error="skillError"
                :engraving-summary="engravingSummary"
                :card-summary="cardSummary"
                :card-loading="cardLoading"
                :card-error="cardError"
                :collection-summary="collectionSummary"
                :collectibles-loading="collectiblesLoading"
                :collectibles-error="collectiblesError"
                :combat-role="combatRole"
              />

                <section
                  v-else-if="activeResultTab === 'skills'"
                  class="detail-panel skill-panel"
                >
                  <SkillPanel
                    :response="skillResponse"
                    :loading="skillLoading"
                    :error-message="skillError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureSkillData"
                  />
                </section>

                <section
                  v-else-if="activeResultTab === 'detail'"
                  class="detail-panel"
                >
                  <CharacterDetailModal
                    :character="activeCharacter"
                    :equipment="detailEquipment"
                    :engravings="detailEngravings"
                    :loading="detailLoading"
                    :error-message="detailError"
                  />
                </section>

                <section
                  v-else-if="activeResultTab === 'collection'"
                  class="detail-panel collection-panel"
                >
                  <CollectionPanel
                    :collectibles="collectibles"
                    :loading="collectiblesLoading"
                    :error-message="collectiblesError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureCollectiblesData"
                  />
                </section>

                <RankingTab
                  v-else-if="activeResultTab === 'ranking'"
                  :character-name="activeCharacter?.characterName"
                />

                <section
                  v-else-if="activeResultTab === 'expedition'"
                  class="expedition-section"
                >
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
                            <span class="member-level">Lv. {{ formatInteger(member.characterLevel) }}</span>
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

                <section
                  v-else-if="activeResultTab === 'arkGrid'"
                  class="detail-panel ark-grid-panel"
                >
                  <ArkGridPanel
                    :response="arkGridResponse"
                    :loading="arkGridLoading"
                    :error-message="arkGridError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureArkGridData"
                  />
                </section>

                <section
                  v-else
                  class="detail-panel placeholder-panel"
                >
                  <EmptyState
                    :icon="activePlaceholder?.icon || '🛠️'"
                    :title="activePlaceholder?.title || '준비 중인 메뉴입니다'"
                    :description="activePlaceholder?.description || '곧 해당 메뉴의 세부 기능을 제공할 예정입니다.'"
                  />
                </section>
              </div>
            </div>
          </section>
        </div>

    <div
      v-if="isHeroImagePopupOpen && hasCharacterImage"
      class="character-portrait-overlay"
      @click.self="closeHeroImagePopup"
    >
      <div class="character-portrait-overlay__content">
        <button
          type="button"
          class="portrait-overlay__close"
          aria-label="캐릭터 이미지 닫기"
          @click="closeHeroImagePopup"
        >
          ✕
        </button>
        <img :src="characterImageSrc" :alt="activeCharacter?.characterName ?? '캐릭터 확대 이미지'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type {
  CharacterProfile,
  SiblingCharacter,
  Equipment,
  Engraving,
  CharacterStat,
  ArkPassiveEffect,
  Collectible,
  CombatSkill,
  SkillGem
} from '@/api/types'
import LoadingSpinner from './common/LoadingSpinner.vue'
import ErrorMessage from './common/ErrorMessage.vue'
import EmptyState from './common/EmptyState.vue'
import LazyImage from './common/LazyImage.vue'
import AutocompleteInput from './common/AutocompleteInput.vue'
import CharacterDetailModal from './common/CharacterDetailModal.vue'
import ArkGridPanel from './common/ArkGridPanel.vue'
import SkillPanel from './common/SkillPanel.vue'
import CollectionPanel from './common/CollectionPanel.vue'
import RankingTab from './ranking/RankingTab.vue'
import CharacterOverviewCard from './common/CharacterOverviewCard.vue'
import CharacterSummaryPanel from './common/CharacterSummaryPanel.vue'
import type { Suggestion } from './common/AutocompleteInput.vue'
import { cleanTooltipLine, flattenTooltipLines, extractTooltipColor } from '@/utils/tooltipText'
import { applyEffectAbbreviations, hasAbbreviationMatch } from '@/data/effectAbbreviations'
import { getEngravingDisplayName } from '@/data/engravingNames'
import { formatItemLevel, formatNumberLocalized, formatCombatPower, formatInteger } from '@/utils/format'
import { useCharacterSearchData } from '@/composables/useCharacterSearchData'

type ResultTabKey =
  | 'summary'
  | 'skills'
  | 'detail'
  | 'collection'
  | 'ranking'
  | 'arkGrid'
  | 'expedition'

interface TabPlaceholderCopy {
  icon: string
  title: string
  description: string
}

type CombatRole = 'dealer' | 'support'

const DEFAULT_RESULT_TAB: ResultTabKey = 'summary'
const resultTabs: Array<{ key: ResultTabKey; label: string }> = [
  { key: 'summary', label: '통합' },
  { key: 'detail', label: '장비' },
  { key: 'arkGrid', label: '아크' },
  { key: 'skills', label: '스킬' },
  { key: 'collection', label: '수집' },
  { key: 'ranking', label: '랭킹' },
  { key: 'expedition', label: '원정대' }
]
const tabPlaceholderCopy: Record<ResultTabKey, TabPlaceholderCopy | null> = {
  summary: null,
  detail: null,
  expedition: null,
  skills: null,
  collection: null,
  ranking: null,
  arkGrid: null
}

const router = useRouter()
const route = useRoute()

const {
  characterName,
  character,
  loading,
  error,
  siblings,
  favorites,
  history,
  selectedCharacterProfile,
  detailEquipment,
  detailEngravings,
  detailLoading,
  detailError,
  arkGridResponse,
  arkGridLoading,
  arkGridError,
  cardResponse,
  cardLoading,
  cardError,
  skillResponse,
  skillLoading,
  skillError,
  armoryGemsResponse,
  collectibles,
  collectiblesLoading,
  collectiblesError,
  lastRefreshedAt,
  activeCharacter,
  loadFavorites,
  toggleFavorite: toggleFavoriteBase,
  loadHistory,
  clearHistory,
  ensureSkillData,
  ensureCollectiblesData,
  ensureArkGridData,
  ensureCardData,
  searchCharacter: searchCharacterData,
  clearSearch: clearSearchData
} = useCharacterSearchData()

const activeResultTab = ref<ResultTabKey>(DEFAULT_RESULT_TAB)
const activePlaceholder = computed(() => tabPlaceholderCopy[activeResultTab.value])
const searchPanelWrapperRef = ref<HTMLElement | null>(null)
const searchPanelOpen = ref(false)
const activeSearchPanelTab = ref<'recent' | 'favorites'>('recent')
const isHeroImageLarge = ref(false)
const isHeroImagePopupOpen = ref(false)
const characterImageSrc = computed(() => activeCharacter.value?.characterImage || '')
const hasCharacterImage = computed(() => Boolean(characterImageSrc.value))
const isCharacterFavorite = computed(() => {
  if (!activeCharacter.value) return false
  return favorites.value.some(
    fav =>
      fav.characterName === activeCharacter.value?.characterName &&
      fav.serverName === activeCharacter.value?.serverName
  )
})

const shouldShowSearchPanel = computed(() => searchPanelOpen.value)
const lastRefreshedLabel = computed(() => {
  if (!lastRefreshedAt.value) return '갱신 이력이 없어요'
  const formatted = new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(lastRefreshedAt.value)
  return `마지막 갱신: ${formatted}`
})

const toggleFavorite = () => {
  toggleFavoriteBase(activeCharacter.value)
}
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

const inlineText = (value?: string | number | null) => {
  if (value === undefined || value === null) return ''
  return cleanTooltipLine(String(value)).replace(/\s+/g, ' ').trim()
}

const normalizeHexColor = (value?: string | null) => {
  if (!value) return ''
  const normalized = String(value).trim().toUpperCase()
  if (!normalized) return ''
  return normalized.startsWith('#') ? normalized : `#${normalized}`
}

const parseSkillIconFromTooltip = (tooltip?: string | null): string => {
  if (!tooltip) return ''
  try {
    const parsed = JSON.parse(tooltip)
    const search = (node: any): string => {
      if (!node || typeof node !== 'object') return ''
      const pick = (icon: string) => (typeof icon === 'string' && icon.trim().length ? icon : '')
      if (typeof node.iconPath === 'string' && pick(node.iconPath)) return node.iconPath
      if (node.slotData && typeof node.slotData.iconPath === 'string' && pick(node.slotData.iconPath)) {
        return node.slotData.iconPath
      }
      for (const value of Object.values(node)) {
        const found = search(value as any)
        if (found) return found
      }
      return ''
    }
    return search(parsed)
  } catch {
    return ''
  }
}

const extractColorFromFontString = (text?: string | null) => {
  if (!text) return ''
  const match = String(text).match(/color=['"]?([#\w]+)['"]?/i)
  const raw = match?.[1]
  return raw ? normalizeHexColor(raw) : ''
}

const scanTooltipForColor = (node: unknown): string => {
  if (!node) return ''
  if (typeof node === 'string') return extractColorFromFontString(node)
  if (Array.isArray(node)) {
    for (const item of node) {
      const found = scanTooltipForColor(item)
      if (found) return found
    }
    return ''
  }
  if (typeof node === 'object') {
    const record = node as Record<string, unknown>
    // Element_000.value에 우선 접근
    if (record.Element_000 && typeof record.Element_000 === 'object') {
      const valueField = (record.Element_000 as any).value
      const fromElement = scanTooltipForColor(valueField)
      if (fromElement) return fromElement
    }
    for (const child of Object.values(record)) {
      const found = scanTooltipForColor(child)
      if (found) return found
    }
  }
  return ''
}

const coreNameColorFromTooltip = (tooltip?: unknown) => {
  if (!tooltip) return ''
  if (typeof tooltip === 'string') {
    try {
      const parsed = JSON.parse(tooltip)
      const fromParsed = scanTooltipForColor(parsed)
      if (fromParsed) return fromParsed
    } catch {
      // 문자열 자체에서 검색
    }
    const direct = extractColorFromFontString(tooltip)
    if (direct) return direct
    return scanTooltipForColor(tooltip)
  }
  return scanTooltipForColor(tooltip)
}

const normalizeSkillKey = (value?: string | null) =>
  inlineText(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

const coreGradeColor = (grade?: string | null) => {
  const g = inlineText(grade).toLowerCase()
  if (!g) return ''
  if (g.includes('고대') || g.includes('ancient')) return '#eab308'
  if (g.includes('유물') || g.includes('relic')) return '#f97316'
  if (g.includes('전설') || g.includes('legend')) return '#fbbf24'
  if (g.includes('영웅') || g.includes('hero')) return '#a78bfa'
  if (g.includes('희귀') || g.includes('rare')) return '#60a5fa'
  if (g.includes('고급') || g.includes('언커먼') || g.includes('uncommon')) return '#6ee7b7'
  if (g.includes('일반') || g.includes('노말') || g.includes('common')) return '#6b7280'
  return ''
}

const formatLevelLabel = (value?: number | string | null) => {
  const num = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(num)) return ''
  return `Lv.${num}`
}

const runeColorFromGrade = (grade?: string | null) => {
  const g = inlineText(grade).toLowerCase()
  if (!g) return ''
  if (g.includes('고대')) return 'var(--rarity-ancient, #eab308)'
  if (g.includes('유물')) return 'var(--rarity-relic, #f97316)'
  if (g.includes('전설')) return 'var(--rarity-legendary, #fbbf24)'
  if (g.includes('영웅')) return 'var(--rarity-heroic, #a78bfa)'
  if (g.includes('희귀')) return 'var(--rarity-rare, #60a5fa)'
  if (g.includes('고급') || g.includes('언커먼')) return 'var(--rarity-uncommon, #6ee7b7)'
  if (g.includes('일반') || g.includes('노말')) return 'var(--text-secondary, #6b7280)'
  return ''
}

const extractRuneColor = (tooltip?: string | null, grade?: string | null) => {
  const scanRaw = (value?: string | null) => {
    if (!value) return ''
    const match = String(value).match(/color=['"]?(#?[0-9a-fA-F]{6,8})['"]?/i)
    if (match?.[1]) {
      const raw = match[1].startsWith('#') ? match[1] : `#${match[1]}`
      return raw
    }
    return ''
  }

  const direct = scanRaw(tooltip) || extractTooltipColor(tooltip)
  if (direct) return direct

  if (tooltip) {
    try {
      const parsed = JSON.parse(tooltip)
      const candidates = Object.values(parsed || {})
      for (const cand of candidates) {
        const candidateValue = (cand as any)?.value || cand
        const found = scanRaw(typeof candidateValue === 'string' ? candidateValue : undefined) ||
          extractTooltipColor(candidateValue as any)
        if (found) return found
      }
    } catch {
      // ignore parse errors
    }
  }

  return runeColorFromGrade(grade) || ''
}

const isAwakeningSkill = (skill: CombatSkill) => {
  const numericType =
    typeof skill.skillType === 'string' ? Number(skill.skillType) : skill.skillType
  if (typeof numericType === 'number' && (numericType === 100 || numericType === 101)) {
    return true
  }
  const candidates = [skill.skillType, skill.type, skill.name]
    .map(value => inlineText(value).toLowerCase())
    .filter(Boolean)
  return candidates.some(value => value.includes('각성'))
}

type GemEffectPlacement = 'damage' | 'cooldown' | 'unknown'

interface SkillGemSlot {
  key: string
  name: string
  icon?: string
  label?: string
  grade?: string
  levelLabel?: string
  levelValue: number
  effectType: GemEffectPlacement
}

const GEM_DAMAGE_REGEX = /(멸화|겁화|피해|대미지|데미지|해방|지원\s*효과)/i
const GEM_COOLDOWN_REGEX = /(홍염|작열|광휘|쿨타임|재사용|대기시간)/i

const extractGemLabel = (gem: SkillGem) => {
  const tooltipText = flattenTooltipLines(gem.tooltip).join(' ')
  const candidates = [inlineText(gem.name), inlineText(gem.skill?.description), tooltipText]
    .filter(Boolean)
    .join(' ')
  if (GEM_DAMAGE_REGEX.test(candidates)) return '겁화'
  if (GEM_COOLDOWN_REGEX.test(candidates)) return '작열'
  const labelMatch = candidates.match(/(멸화|겁화|홍염|작열|광휘|지원\s*효과)/i)
  if (labelMatch?.[1]) return labelMatch[1]
  return ''
}

const classifyGemEffect = (label: string): GemEffectPlacement => {
  if (/멸화|겁화|해방|지원\s*효과/i.test(label)) return 'damage'
  if (/홍염|작열|광휘/i.test(label)) return 'cooldown'
  return 'unknown'
}

const pickHigherGem = (current: SkillGemSlot | null, next: SkillGemSlot) => {
  if (!current) return next
  return next.levelValue > current.levelValue ? next : current
}

const skillGemSlotsBySkill = computed(() => {
  const map = new Map<
    string,
    { damage: SkillGemSlot | null; cooldown: SkillGemSlot | null; extras: SkillGemSlot[] }
  >()
  const gems = skillResponse.value?.skillGems ?? []
  gems.forEach((gem, index) => {
    const key = normalizeSkillKey(gem.skill?.name || gem.skill?.description || gem.name)
    if (!key) return
    const label = extractGemLabel(gem)
    const effectType = classifyGemEffect(label)
    const levelValue = typeof gem.level === 'number' ? gem.level : -1
    const slot: SkillGemSlot = {
      key: `${key}-gem-${index}`,
      name: inlineText(gem.name) || '보석',
      icon: gem.icon || '',
      label,
      grade: inlineText(gem.grade),
      levelLabel: formatLevelLabel(gem.level),
      levelValue,
      effectType
    }
    const entry = map.get(key) ?? { damage: null, cooldown: null, extras: [] }
    if (effectType === 'damage') {
      entry.damage = pickHigherGem(entry.damage, slot)
    } else if (effectType === 'cooldown') {
      entry.cooldown = pickHigherGem(entry.cooldown, slot)
    } else {
      entry.extras.push(slot)
    }
    map.set(key, entry)
  })
  return map
})

const armoryGemIconMaps = computed(() => {
  const skillByKey = new Map<string, string>()
  const skillByName = new Map<string, string>()
  const gemTooltipIconByName = new Map<string, string>()

  const pickIcon = (icon?: string | null) => (typeof icon === 'string' && icon.trim().length ? icon : '')

  const armoryGems = armoryGemsResponse.value?.Gems ?? armoryGemsResponse.value?.gems ?? []
  const rawEffects = armoryGemsResponse.value?.Effects ?? armoryGemsResponse.value?.effects ?? []
  const effectsArray = Array.isArray(rawEffects) ? rawEffects : rawEffects ? [rawEffects] : []
  const effectSkills = effectsArray.flatMap((eff: any) => {
    const skills = (eff?.Skills ?? eff?.skills) as any
    return Array.isArray(skills) ? skills : []
  })

  effectSkills.forEach((sk: any) => {
    const skKey = normalizeSkillKey(sk?.Name || sk?.name)
    const icon = pickIcon(sk?.Icon) || parseSkillIconFromTooltip(sk?.Tooltip || sk?.tooltip)
    if (skKey && icon) {
      if (!skillByKey.has(skKey)) skillByKey.set(skKey, icon)
      if (!skillByName.has(skKey)) skillByName.set(skKey, icon)
    }
  })

  armoryGems.forEach(gem => {
    const nameKey = normalizeSkillKey(inlineText(gem?.Name || gem?.name))
    const tooltipIcon = parseSkillIconFromTooltip(gem?.Tooltip || gem?.tooltip)
    if (nameKey && tooltipIcon && !gemTooltipIconByName.has(nameKey)) {
      gemTooltipIconByName.set(nameKey, tooltipIcon)
    }
  })

  return { skillByKey, skillByName, gemTooltipIconByName }
})

const skillHighlights = computed(() => {
  const skills = skillResponse.value?.combatSkills ?? []
  if (!skills.length) return []
  const gemSlots = skillGemSlotsBySkill.value
  const armoryIcons = armoryGemIconMaps.value
  return skills
    .filter(skill => !isAwakeningSkill(skill))
    .sort((a, b) => {
      const levelA = typeof a.level === 'number' ? a.level : -1
      const levelB = typeof b.level === 'number' ? b.level : -1
      return levelB - levelA
    })
    .map((skill, index) => {
      const key = normalizeSkillKey(skill.name || `skill-${index}`)
      const gem = key ? gemSlots.get(key) : undefined
      const runePayload = (skill as any).rune || (skill as any).Rune || skill.rune
      const runeTooltip = runePayload?.tooltip ?? runePayload?.Tooltip
      const runeName = runePayload?.name ?? runePayload?.Name
      const runeGrade = runePayload?.grade ?? runePayload?.Grade
      const runeIcon = runePayload?.icon ?? runePayload?.Icon
      const rune = runeName
        ? {
          name: inlineText(runeName),
          icon: runeIcon || '',
          grade: inlineText(runeGrade),
          color: extractRuneColor(runeTooltip, runeGrade) || undefined
        }
        : null
      const tripods =
        skill.tripods
          ?.filter(tripod => tripod.name && tripod.selected !== false)
          .map((tripod, tripodIndex) => {
            const slotIndex =
              typeof tripod.slot === 'number' && tripod.slot > 0 ? tripod.slot : tripodIndex + 1
            return {
              key: `${key || 'skill'}-tripod-${tripodIndex}`,
              name: inlineText(tripod.name) || `트포 ${tripodIndex + 1}`,
              icon: tripod.icon || '',
              slotIndex,
              slotLabel: `${slotIndex}`,
              levelLabel: formatLevelLabel(tripod.level)
            }
          }) ?? []
      const skillPointValue = Number(skill.skillPoints ?? 0)
      const hasSkillPoints = Number.isFinite(skillPointValue) && skillPointValue > 0
      const hasRune = Boolean(rune)
      const damageGem = gem?.damage ?? null
      const cooldownGem = gem?.cooldown ?? null
      const hasGem = Boolean(damageGem || cooldownGem)
      const overrideIcon = key ? armoryIcons.skillByKey.get(key) || armoryIcons.skillByName.get(key) : ''
      if (overrideIcon && inlineText(skill.name).includes('종언')) {
        console.debug('[skill icon override] name=%s source=armoryEffects icon=%s', inlineText(skill.name), overrideIcon)
      }
      if (!hasSkillPoints && !hasRune && !hasGem) {
        return null
      }
      return {
        key: key || `skill-${index}`,
        name: inlineText(skill.name) || `스킬 ${index + 1}`,
        icon: overrideIcon || skill.icon || '',
        levelLabel: formatLevelLabel(skill.level),
        pointLabel:
          hasSkillPoints && Number.isFinite(skillPointValue)
            ? `${skillPointValue.toLocaleString()}P`
            : '',
        rune,
        tripods,
        gems: {
          damage: damageGem,
          cooldown: cooldownGem
        },
        hasGem
      }
    })
    .filter(Boolean) as Array<{
      key: string
      name: string
      icon: string
      levelLabel?: string
      pointLabel?: string
      rune: { name: string; icon?: string; grade?: string; color?: string } | null
      tripods: {
        key: string
        name: string
        icon?: string
        slotIndex: number
        slotLabel: string
        levelLabel?: string
      }[]
      gems: { damage: SkillGemSlot | null; cooldown: SkillGemSlot | null }
      hasGem: boolean
    }>
})

const combatSkillKeySet = computed(() => {
  const set = new Set<string>()
  const skills = skillResponse.value?.combatSkills ?? []
  skills.forEach(skill => {
    const key = normalizeSkillKey(skill.name)
    if (key) set.add(key)
  })
  return set
})

const skillLooseGems = computed(() => {
  const gems = skillResponse.value?.skillGems ?? []
  if (!gems.length) return []

  const {
    skillByKey: armorySkillIconBySkillKey,
    skillByName: armorySkillIconByName,
    gemTooltipIconByName
  } = armoryGemIconMaps.value

  const armoryGemBySlot = new Map<number, any>()
  const armoryGems = armoryGemsResponse.value?.Gems ?? armoryGemsResponse.value?.gems ?? []
  armoryGems.forEach(gem => {
    if (typeof gem?.Slot === 'number' && !armoryGemBySlot.has(gem.Slot)) {
      armoryGemBySlot.set(gem.Slot, gem)
    }
  })

  const skillIconByKey = new Map<string, string>()
  const skills = skillResponse.value?.combatSkills ?? []
  skills.forEach(skill => {
    const key = normalizeSkillKey(skill.name)
    if (key && skill.icon && !skillIconByKey.has(key)) {
      skillIconByKey.set(key, skill.icon)
    }
  })

  const skillKeys = combatSkillKeySet.value
  return gems
    .filter(gem => {
      const key = normalizeSkillKey(gem.skill?.name || gem.skill?.description)
      if (key && skillKeys.has(key)) return false
      return true
    })
    .map((gem, index) => {
      const effectLabel = extractGemLabel(gem)
      const skillName = inlineText(gem.skill?.name || gem.skill?.description)
      const normalizedKey = normalizeSkillKey(gem.skill?.name || gem.skill?.description)
      const armoryGem = typeof (gem as any).Slot === 'number' ? armoryGemBySlot.get((gem as any).Slot) : undefined
      const candidateKeys = [
        normalizedKey,
        normalizeSkillKey(effectLabel),
        normalizeSkillKey(armoryGem?.Name || armoryGem?.name)
      ].filter(Boolean) as string[]
      const pickIconFromKeys = (keys: string[], ...maps: Array<Map<string, string>>) => {
        for (const key of keys) {
          for (const map of maps) {
            const hit = map.get(key)
            if (hit) return hit
          }
        }
        return ''
      }
      const skillIcon = pickIconFromKeys(candidateKeys, armorySkillIconBySkillKey, armorySkillIconByName, skillIconByKey)
      const tooltipIcon = parseSkillIconFromTooltip(
        (gem as any).tooltip || (gem as any).Tooltip || armoryGem?.Tooltip || armoryGem?.tooltip
      )
      const armoryIcon =
        pickIconFromKeys(candidateKeys, armorySkillIconBySkillKey, armorySkillIconByName, gemTooltipIconByName) || ''
      if (inlineText(skillName).includes('종언')) {
        console.debug('[loose gem icon pick]', {
          skillName,
          candidateKeys,
          armoryIcon,
          skillIcon,
          tooltipIcon,
          gemIcon: gem.icon || '',
          armoryGemSlot: (gem as any).Slot ?? (gem as any).slot ?? (gem as any).GemSlot,
          source: armoryIcon ? 'armory' : skillIcon ? 'skillMatch' : tooltipIcon ? 'tooltip' : 'gemIcon'
        })
      }
      return {
        key: `${gem.name || 'gem'}-loose-${index}`,
        name: skillName || inlineText(gem.name) || '보석',
        skillName,
        icon: armoryIcon || skillIcon || tooltipIcon || gem.icon || '',
        skillIcon: armoryIcon || skillIcon || tooltipIcon,
        levelLabel: formatLevelLabel(gem.level),
        grade: inlineText(gem.grade),
        effectLabel: effectLabel || inlineText(gem.skill?.description),
        typeLabel: inlineText(gem.skill?.name)
      }
    })
})

const summarizeEquipmentLine = (item: Equipment): string => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return ''
  const highlightRegex = /(추가 피해|무력화|치명|신속|특화|공격력|방어력|품질|효과|피해|쿨타임|쿨타운)/i
  const candidate = lines.find(line => highlightRegex.test(line)) || lines[0]
  return inlineText(candidate)
}

const matchStatFromLines = (lines: string[], patterns: RegExp[]) => {
  for (const line of lines) {
    for (const pattern of patterns) {
      const match = line.match(pattern)
      if (match) {
        return match[1] || match[0]
      }
    }
  }
  return ''
}

const parseEquipmentMeta = (item: Equipment) => {
  const lines = extractTooltipLines(item.tooltip)
  const itemLevel = matchStatFromLines(lines, [/아이템\s*레벨\s*([0-9.,]+)/i, /iLv\.?\s*([0-9.,]+)/i])
  let quality = matchStatFromLines(lines, [/품질\s*([0-9]+)/i, /\(품질\)\s*([0-9]+)/i])
  const engravingLine = matchStatFromLines(lines, [/각인\s*효과\s*(.+)/i])
  let harmony = matchStatFromLines(lines, [
    /상재\s*([0-9]+)/i,
    /상급\s*재련\s*([0-9]+)/i,
    /상급\s*재련[^0-9]*([0-9]+)/i
  ])
  const mainStat = matchStatFromLines(lines, [/(힘|민첩|지능)[^0-9+]*([0-9.,]+)/i])
  const craft = matchStatFromLines(lines, [/세공\s*([0-9]+)/i])

  const extractTranscendValue = (): string => {
    const cleaned = lines.map(line => inlineText(line)).filter(Boolean)

    const extractStageValue = (source?: string) => {
      if (!source) return ''
      const match = source.match(/(\d+)\s*(?:단계|단|레벨|lv\.?)\s*([0-9][0-9.,]*)/i)
      if (match?.[2]) {
        return match[2].replace(/,/g, '')
      }
      return ''
    }

    const valueLine = cleaned.find(line => /초월\s*(수치|경험|exp|게이지)/i.test(line))
    const fromValueLine = (source?: string) => {
      if (!source) return ''
      const match = source.match(/([0-9][0-9.,]*)/)
      return match?.[1] ? match[1].replace(/,/g, '') : ''
    }
    let value = fromValueLine(valueLine)
    if (!value) {
      const stageValueLine = cleaned.find(line => /초월/i.test(line) && /단계|단|레벨|lv\.?/i.test(line))
      value = extractStageValue(stageValueLine)
    }
    if (value) return value

    // fallback: look for standalone 초월 숫자 표기 (비율/확률/소모 등 제외)
    const blacklist = /(확률|소모|재료|획득|피해|추가|필요|상자|매트릭스|재료|UP|%|\+|-)/i
    const patterns = [
      /초월\s*[:\-]?\s*(\d+)\s*(?:단계|단|레벨|lv\.?)/i,
      /\[?초월\]?\s*(\d+)\s*(?:단계|단|레벨|lv\.?)/i,
      /^초월\s*(\d+)/i
    ]
    for (const line of cleaned) {
      if (!/초월/i.test(line)) continue
      if (blacklist.test(line)) continue
      for (const pattern of patterns) {
        const match = line.match(pattern)
        if (match?.[1]) {
          return match[1]
        }
      }
    }
    if (item.tooltip) {
      const rawValueMatch = item.tooltip.match(/초월[^\\n]{0,15}(수치|경험|exp)[^0-9]{0,6}([0-9][0-9.,]*)/i)
      if (rawValueMatch?.[2]) return rawValueMatch[2].replace(/,/g, '')
      const rawStageValue = extractStageValue(item.tooltip.match(/초월[^\\n]{0,40}/i)?.[0])
      if (rawStageValue) return rawStageValue
    }
    return ''
  }

  const transcend = extractTranscendValue()

  if ((!quality || !quality.length) && item.tooltip) {
    try {
      const parsed = JSON.parse(item.tooltip)
      const qualityValue =
        parsed?.Element_001?.value?.qualityValue ??
        parsed?.Element_001?.value?.QualityValue ??
        parsed?.Element_001?.value?.Quality
      if (qualityValue !== undefined && qualityValue !== null) {
        quality = String(qualityValue)
      }
    } catch {
      // ignore parse errors
    }
  }
  if ((!harmony || !harmony.length) && item.tooltip) {
    const harmonyMatch =
      item.tooltip.match(/상급\s*재련[^0-9]*([0-9]+)/i) || item.tooltip.match(/상재[^0-9]*([0-9]+)/i)
    if (harmonyMatch?.[1]) {
      harmony = harmonyMatch[1]
    }
  }
  if ((!transcend || !transcend.length) && item.tooltip) {
    const transcendMatch = item.tooltip.match(/초월[^0-9]*([0-9]+)\s*(?:단계|단|레벨|lv\.?)/i)
    if (transcendMatch?.[1]) {
      transcend = transcendMatch[1]
    }
  }
  return {
    itemLevel: itemLevel ? `iLv. ${itemLevel}` : '',
    quality,
    transcend,
    harmony,
    engravingLine,
    mainStat,
    craft
  }
}

const isAccessory = (item: Equipment) => {
  const label = inlineText(item.type).toLowerCase()
  return /(목걸이|귀걸이|반지|팔찌|어빌리티|돌)/.test(label)
}

const isNecklace = (item: Equipment) => /목걸이/i.test(inlineText(item.type))
const isEarring = (item: Equipment) => /귀걸이/i.test(inlineText(item.type))
const isRing = (item: Equipment) => /반지/i.test(inlineText(item.type))
const isBracelet = (item: Equipment) => inlineText(item.type).includes('팔찌')
const isAbilityStone = (item: Equipment) => /어빌리/i.test(inlineText(item.name) + inlineText(item.type))

const extractEnhancementLevel = (item: Equipment) => {
  const name = inlineText(item.name)
  const match = name.match(/\+(\d{1,2})/)
  return match?.[1] || ''
}

const equipmentSummary = computed(() => {
  if (!detailEquipment.value.length) {
    return { gradeBadges: [], left: [], right: [] }
  }

  const gradeCounts = new Map<string, number>()
  detailEquipment.value.forEach(item => {
    const grade = inlineText(item.grade) || '등급 미상'
    gradeCounts.set(grade, (gradeCounts.get(grade) || 0) + 1)
  })

  const gradeBadges = Array.from(gradeCounts.entries())
    .map(([grade, count]) => ({ grade, count }))
    .sort((a, b) => b.count - a.count)

  const gearOrder = [
    ['투구', '헬멧', '머리'],
    ['어깨'],
    ['상의', '상체', '갑옷'],
    ['하의', '바지'],
    ['장갑'],
    ['무기']
  ]

  const gearOrderIndex = (label: string) => {
    const lower = inlineText(label).toLowerCase()
    const found = gearOrder.findIndex(group =>
      group.some(keyword => lower.includes(keyword.toLowerCase()))
    )
    return found === -1 ? 99 : found
  }

  const left: any[] = []
  const right: any[] = []

  const stoneCraft = detailEngravings.value.find(eng => typeof eng.abilityStoneLevel === 'number')
    ?.abilityStoneLevel

  const shouldSkipEffectLabel = (label?: string) => {
    if (!label) return true
    const clean = inlineText(label)
    return /아크\s*패시브\s*포인트|깨달음/i.test(clean)
  }

  const normalizeHexColor = (value?: string | null) => {
    if (!value) return null
    const raw = value.replace(/['"#]/g, '').trim()
    if (!/^[0-9a-fA-F]{3,4}$/.test(raw) && !/^[0-9a-fA-F]{6}$/.test(raw) && !/^[0-9a-fA-F]{8}$/.test(raw)) {
      return null
    }
    const expand = (hex: string) => (hex.length === 1 ? hex.repeat(2) : hex)
    const normalized =
      raw.length === 3 || raw.length === 4
        ? raw.split('').map(expand).join('').slice(0, 6)
        : raw.slice(0, 6)
    return `#${normalized.toLowerCase()}`
  }

  const normalizeEffectLabel = (label: string) => inlineText(label).replace(/^>\s*/, '').trim()
  const abbreviateEffect = (label: string) => applyEffectAbbreviations(normalizeEffectLabel(label))

  const formatEffectLabel = (label: string) => {
    const normalized = normalizeEffectLabel(label)
    const abbreviated = applyEffectAbbreviations(normalized)
    return {
      label: abbreviated,
      normalized,
      changed: abbreviated !== normalized
    }
  }

  const decorateEffect = (effect: { label: string; full?: string }) => {
    const { label } = formatEffectLabel(effect.label)
    const { full } = effect
    if (shouldSkipEffectLabel(label)) return null
    let hash = 0
    for (let i = 0; i < label.length; i += 1) {
      hash = (hash << 5) - hash + label.charCodeAt(i)
      hash |= 0
    }
    const hue = Math.abs(hash) % 360
    const bgColor = ``
    const textColor = `hsl(${hue}deg 45% 30%)`
    return { label, full, bgColor, textColor }
  }

  const extractAccessoryEffects = (item: Equipment) => {
    const effects: Array<{ label: string; full?: string; bgColor: string; textColor: string }> = []

    const hexToRgba = (hex: string, alpha = 0.16) => {
      const cleaned = hex.replace('#', '')
      if (cleaned.length !== 6) return `rgba(0,0,0,${alpha})`
      const r = parseInt(cleaned.slice(0, 2), 16)
      const g = parseInt(cleaned.slice(2, 4), 16)
      const b = parseInt(cleaned.slice(4, 6), 16)
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }

    const addEffect = (label: string, full?: string, color?: string) => {
      const cleanedLabel = normalizeEffectLabel(label)
      const formatted = formatEffectLabel(cleanedLabel)
      const normalizedLabel = formatted.label
      const key = `${normalizedLabel}|${full || ''}`
      if (effects.find(e => `${e.label}|${e.full || ''}` === key)) return
      const hasAbbrev = formatted.changed
      const textColor = hasAbbrev ? color || 'var(--text-primary)' : 'var(--text-secondary)'
      const neutralBg = 'var(--bg-secondary)'
      const bgColor = hasAbbrev
        ? color
          ? hexToRgba(color, 0.16)
          : neutralBg
        : neutralBg
      effects.push({ label: normalizedLabel, full, textColor, bgColor })
    }

    try {
      const parsed = JSON.parse(item.tooltip)
      const raw =
        parsed?.Element_006?.value?.Element_001 ||
        parsed?.Element_006?.value?.Element_000 ||
        ''
      if (typeof raw === 'string' && raw.length) {
        const cleaned = raw.replace(/<img[^>]*>/gi, '')
        const segments = cleaned.split(/<br\s*\/?>/i).map(seg => seg.trim()).filter(Boolean)
        segments.forEach(seg => {
          const colorMatch = seg.match(/color=['"]?(#?[0-9a-fA-F]{6})/i)
          const color = normalizeHexColor(colorMatch?.[1] || null)
          const label = inlineText(seg.replace(/<[^>]+>/g, '')).replace(/\s+/g, ' ').trim()
          if (!label) return
          addEffect(label, label, color)
        })
      }
    } catch {
      // ignore parse errors
    }

    if (!effects.length) {
      const lines = extractTooltipLines(item.tooltip)
      const startIndex = lines.findIndex(line => /연마\s*효과/i.test(line))
      const candidates =
        startIndex >= 0 ? lines.slice(startIndex + 1) : lines.filter(line => /연마/i.test(line))
      const effectPattern = /[+]\s*\d|%/
      const meaningful = candidates.filter(line => effectPattern.test(line))
      meaningful.forEach(line => {
        const decorated = decorateEffect({ label: inlineText(line) })
        if (decorated) effects.push(decorated as any)
      })
    }

    return effects.slice(0, 6)
  }

  const extractStoneEffects = (item: Equipment) => {
    const lines = extractTooltipLines(item.tooltip)
    const candidates = lines.filter(
      line =>
        (/\[.*\]/.test(line) || /세공|각인/i.test(line)) &&
        !/세공\s*단계\s*보너스/i.test(line)
    )
    return candidates
      .map(line => decorateEffect({ label: inlineText(line) }))
      .filter(Boolean)
      .slice(0, 3) as Array<{ label: string; full?: string; bgColor: string; textColor: string }>
  }

  const extractBraceletEffects = (item: Equipment) => {
    const effects: Array<{ label: string; full?: string; bgColor: string; textColor: string }> = []
    const combatStatKeywords = /(치명|특화|제압|신속|인내|숙련)/i

    const hexToRgba = (hex: string, alpha = 0.16) => {
      const cleaned = hex.replace('#', '')
      if (cleaned.length !== 6) return `rgba(0,0,0,${alpha})`
      const r = parseInt(cleaned.slice(0, 2), 16)
      const g = parseInt(cleaned.slice(2, 4), 16)
      const b = parseInt(cleaned.slice(4, 6), 16)
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }

    const tierBases = [
      { r: 0, g: 181, b: 255 }, // 하
      { r: 206, g: 67, b: 252 }, // 중
      { r: 254, g: 150, b: 0 } // 상
    ]

    const hexToRgb = (hex?: string | null) => {
      const normalized = normalizeHexColor(hex)
      if (!normalized) return null
      const cleaned = normalized.replace('#', '')
      const r = parseInt(cleaned.slice(0, 2), 16)
      const g = parseInt(cleaned.slice(2, 4), 16)
      const b = parseInt(cleaned.slice(4, 6), 16)
      return { r, g, b }
    }

    const colorTierDistance = (hex?: string | null) => {
      const rgb = hexToRgb(hex)
      if (!rgb) return Infinity
      const distance = (a: typeof rgb, b: typeof rgb) => {
        const dr = a.r - b.r
        const dg = a.g - b.g
        const db = a.b - b.b
        return Math.sqrt(dr * dr + dg * dg + db * db)
      }
      return Math.min(...tierBases.map(base => distance(rgb, base)))
    }

    const pickTierAlignedColor = (colors: string[]) => {
      if (!colors.length) return null
      let best: { color: string; dist: number } | null = null
      colors.forEach(color => {
        const dist = colorTierDistance(color)
        if (best === null || dist < best.dist) {
          best = { color, dist }
        }
      })
      return best?.color || colors[0]
    }

    const sanitizeColoredText = (value?: string | null) => {
      if (!value) return ''
      const withoutImgs = value.replace(/<img[^>]*>/gi, '')
      let html = withoutImgs.replace(
        /<font[^>]*color=['"]?([^'" >]+)['"]?[^>]*>(.*?)<\/font>/gi,
        (_m, color, inner) => `<span style="color:${color}">${inner}</span>`
      )
      html = html.replace(
        /<span[^>]*style=["'][^"']*color\s*:\s*([^;"']+)[^"']*["'][^>]*>(.*?)<\/span>/gi,
        (_m, color, inner) => `<span style="color:${color}">${inner}</span>`
      )
      html = html
        .replace(/<br\s*\/?>/gi, '<br />')
        .replace(/<(?!br\s*\/?|span\b|\/span\b)[^>]+>/gi, '')
      return html.trim()
    }

    const addEffect = (label: string, full?: string, color?: string, richLabel?: string) => {
      const cleaned = normalizeEffectLabel(label)
      const normalizedLabel = abbreviateEffect(cleaned)
      const key = `${normalizedLabel}|${full || ''}`
      if (effects.find(e => `${e.label}|${e.full || ''}` === key)) return
      const hasAbbrev = hasAbbreviationMatch(cleaned)
      const textColor = color || (hasAbbrev ? 'var(--text-primary)' : '#6b7280')
      const bgColor = color
        ? hexToRgba(color, 0.16)
        : hasAbbrev
          ? 'var(--bg-secondary)'
          : 'rgba(107, 114, 128, 0.15)'
      effects.push({ label: normalizedLabel, full: richLabel || full || normalizedLabel, richLabel, textColor, bgColor })
    }

    try {
      const parsed = JSON.parse(item.tooltip)
      const raw =
        parsed?.Element_005?.value?.Element_001 ||
        parsed?.Element_005?.value?.Element_000 ||
        ''
      if (typeof raw === 'string' && raw.length) {
        const cleaned = raw.replace(/<img[^>]*>/gi, '\r\n')
        const segments = cleaned
          .split(/\r?\n/)
          .map(seg => seg.trim())
          .filter(Boolean)
        segments.forEach(seg => {
          const colorMatches = Array.from(seg.matchAll(/color=['"]?(#?[0-9a-fA-F]{3,8})/gi))
            .map(match => normalizeHexColor(match[1] || null))
            .filter(Boolean) as string[]
          const color = pickTierAlignedColor(colorMatches)
          const label = inlineText(seg.replace(/<[^>]+>/g, '')).replace(/\s+/g, ' ').trim()
          if (!label) return
          const richLabel = sanitizeColoredText(seg)
          addEffect(label, label, color, richLabel)
        })
      }
    } catch {
      // ignore parse errors
    }

    if (!effects.length) {
      const lines = extractTooltipLines(item.tooltip)
        .map(line => inlineText(line))
        .filter(Boolean)
      const traitKeyword = /(피해|공격|방어|회복|증가|감소|무력|출혈|중첩|발동|생명력|치명|제압|특화|신속|인내|숙련)/i
      lines.forEach(line => {
        if (!/[0-9]/.test(line)) return
        if (!traitKeyword.test(line)) return
        addEffect(line, line)
      })
    }

    const prioritized = [
      ...effects.filter(effect => combatStatKeywords.test(effect.label)),
      ...effects.filter(effect => !combatStatKeywords.test(effect.label))
    ]

    return prioritized.slice(0, 12)
  }

  detailEquipment.value.forEach((item, index) => {
    const meta = parseEquipmentMeta(item)
    const enhancement = extractEnhancementLevel(item)
    let typeLabel = inlineText(item.type) || '장비'
    const accessory = isAccessory(item)
    const bracelet = isBracelet(item)
    const abilityStone = isAbilityStone(item)
    const gradeLabel = inlineText(item.grade) || '등급 미상'
    if (abilityStone) {
      typeLabel = '돌'
    }
    const fallbackItemLevel = (() => {
      const parseLevel = (value: unknown) => {
        if (value === undefined || value === null) return null
        const numeric = Number(String(value).replace(/,/g, '').trim())
        if (!Number.isFinite(numeric)) return null
        return Math.round(numeric)
      }

      const rawLevel =
        parseLevel((item as any).itemLevel) ||
        parseLevel((item as any).ItemLevel)

      if (rawLevel) {
        return `iLv. ${formatNumberLocalized(rawLevel, 0)}`
      }

      const tooltipMatch =
        item.tooltip?.match(/거래\s*제한\s*아이템\s*레벨\s*([0-9.,]+)/i) ||
        item.tooltip?.match(/아이템\s*레벨\s*([0-9.,]+)/i)
      const tooltipLevel = parseLevel(tooltipMatch?.[1])
      if (tooltipLevel) {
        return `iLv. ${formatNumberLocalized(tooltipLevel, 0)}`
      }

      return ''
    })()

    const base = {
      key: `${item.name || 'equipment'}-${index}`,
      name: enhancement ? `+${enhancement}` : inlineText(item.name) || `장비 ${index + 1}`,
      typeLabel,
      grade: gradeLabel,
      icon: item.icon || '',
      itemLevel: accessory || bracelet || abilityStone ? '' : meta.itemLevel || fallbackItemLevel,
      quality: meta.quality,
      transcend: accessory ? '' : meta.transcend,
      harmony: accessory ? '' : meta.harmony,
      meta: summarizeEquipmentLine(item),
      isAccessory: accessory,
      isBracelet: bracelet,
      isAbilityStone: abilityStone,
      enhancement
    }

    let effects: Array<{ label: string; full?: string }> = []

    if (accessory) {
      const special = abilityStone
        ? stoneCraft
          ? `세공 ${stoneCraft}단`
          : meta.craft
            ? `세공 ${meta.craft}단`
            : meta.engravingLine || '세공 정보'
        : bracelet
          ? (meta.engravingLine || base.meta || '').slice(0, 14)
          : inlineText(meta.harmony || meta.engravingLine || meta.mainStat)
      if (isNecklace(item) || isEarring(item) || isRing(item)) {
        effects = extractAccessoryEffects(item)
      } else if (abilityStone) {
        effects = extractStoneEffects(item)
      } else if (bracelet) {
        effects = extractBraceletEffects(item)
      } else {
        effects = []
      }
      right.push({
        ...base,
        special: special || undefined,
        effects
      })
    } else {
      left.push({
        ...base,
        orderRank: gearOrderIndex(typeLabel),
        orderSeq: left.length
      })
    }
  })

  const sortedLeft = left
    .slice()
    .sort(
      (a, b) =>
        (a.orderRank ?? 99) - (b.orderRank ?? 99) || (a.orderSeq ?? 0) - (b.orderSeq ?? 0)
    )
    .map(({ orderRank, orderSeq, ...rest }) => rest)

  return {
    gradeBadges,
    left: sortedLeft.slice(0, 6),
    right: right.slice(0, 8)
  }
})

const collectionSummary = computed(() => {
  if (!collectibles.value.length) return []
  return collectibles.value
    .map((item, index) => {
      const point = typeof item.point === 'number' ? item.point : 0
      const maxPoint = typeof item.maxPoint === 'number' ? item.maxPoint : 0
      const percent = maxPoint > 0 ? Math.min(point / maxPoint, 1) : null
      const percentLabel = percent === null ? '0%' : `${Math.round(percent * 100)}%`
      return {
        key: `${item.type || 'collectible'}-${index}`,
        name: inlineText(item.type) || `수집 ${index + 1}`,
        levelLabel: typeof item.collectibleLevel === 'number' ? `Lv.${item.collectibleLevel}` : '',
        pointLabel: maxPoint
          ? `${formatNumberLocalized(point)} / ${formatNumberLocalized(maxPoint)}`
          : `포인트 ${formatNumberLocalized(point)}`,
        percentLabel: percentLabel,
        percentValue: percent ?? 0
      }
    })
    .sort((a, b) => b.percentValue - a.percentValue)
})

const engravingSummary = computed(() => {
  return detailEngravings.value.map((engrave, index) => {
    const gradeLabel = inlineText(engrave.grade) || '등급 미상'
    const rawName = inlineText(engrave.name)
    const displayName = getEngravingDisplayName(rawName)
    return {
      key: `${engrave.name || 'engrave'}-${index}`,
      name: rawName,
      displayName,
      gradeLabel,
      levelLabel: typeof engrave.level === 'number' ? `Lv.${engrave.level}` : '',
      craftLabel:
        typeof engrave.abilityStoneLevel === 'number' ? `세공 ${engrave.abilityStoneLevel}단` : '',
      icon: engrave.icon || ''
    }
  })
})

const PASSIVE_SECTIONS = [
  { key: 'evolution', label: '진화', keyword: '진화' },
  { key: 'realization', label: '깨달음', keyword: '깨달음' },
  { key: 'leap', label: '도약', keyword: '도약' }
] as const

type PassiveSectionKey = (typeof PASSIVE_SECTIONS)[number]['key']

const PASSIVE_SECTION_PATTERN = PASSIVE_SECTIONS.map(section => section.keyword).join('|')
const PASSIVE_SECTION_REGEX = new RegExp(`(${PASSIVE_SECTION_PATTERN})`, 'gi')
const PASSIVE_SECTION_PREFIX_REGEX = new RegExp(
  `^\\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?\\s*([·:\\-]+)?\\s*`,
  'gi'
)
const PASSIVE_SECTION_SUFFIX_REGEX = new RegExp(
  `\\s*([·:\\-]+)?\\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?$`,
  'gi'
)

const ROMAN_NUMERAL_MAP: Record<string, number> = {
  I: 1,
  V: 5,
  X: 10,
  L: 50,
  C: 100,
  D: 500,
  M: 1000
}

const stripPassiveStageKeywords = (value?: string | null) => {
  if (!value) return ''
  return value
    .replace(PASSIVE_SECTION_PREFIX_REGEX, ' ')
    .replace(PASSIVE_SECTION_SUFFIX_REGEX, ' ')
    .replace(PASSIVE_SECTION_REGEX, ' ')
    .replace(/[\[\]\(\)]/g, ' ')
    .replace(/[·:\-]{2,}/g, ' ')
    .replace(/\s+/g, ' ')
    .replace(/^[·:\-\s]+/, '')
    .replace(/[·:\-\s]+$/, '')
    .trim()
}

const parsePassiveTooltip = (tooltip?: string | null) => {
  const lines = flattenTooltipLines(tooltip, { fallbackBreaks: true })
  const [title, ...rest] = lines
  return {
    title: inlineText(title),
    lines: rest.map(line => inlineText(line)).filter(Boolean)
  }
}

const parsePassiveDescription = (description?: string | null) => {
  const text = inlineText(description)
  const typeMatch = text.match(/(진화|깨달음|도약)/)
  const typeLabel = typeMatch?.[1] || '기타'
  const levelMatch = text.match(/Lv\.?\s*(\d+)/i)
  const levelLabel = levelMatch?.[1] ? `Lv.${levelMatch[1]}` : ''
  const tierMatch = text.match(/(\d+)\s*티어/i)
  const tierValue = tierMatch?.[1] ? Number(tierMatch[1]) : Number.POSITIVE_INFINITY
  const tierLabel = tierMatch?.[1] ? `${tierMatch[1]}티어` : ''
  let passiveName = text
  if (typeMatch?.[0]) passiveName = passiveName.replace(typeMatch[0], ' ')
  if (tierMatch?.[0]) passiveName = passiveName.replace(tierMatch[0], ' ')
  if (levelMatch?.[0]) passiveName = passiveName.replace(levelMatch[0], ' ')
  passiveName = passiveName.replace(/\s+/g, ' ').trim()
  if (!passiveName) passiveName = typeLabel
  return { typeLabel, levelLabel, tierLabel, tierValue, passiveName }
}

const normalizeTierText = (value?: string | null) => {
  if (!value) return ''
  return value
    .replace(PASSIVE_SECTION_REGEX, ' ')
    .replace(/[·:]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

const romanToNumber = (value: string) => {
  const chars = value.toUpperCase().split('')
  if (!chars.every(char => ROMAN_NUMERAL_MAP[char])) {
    return null
  }
  let total = 0
  let previous = 0
  for (let i = chars.length - 1; i >= 0; i -= 1) {
    const char = chars[i]
    if (!char) continue
    const current = ROMAN_NUMERAL_MAP[char]
    if (!current) continue
    if (current < previous) {
      total -= current
    } else {
      total += current
      previous = current
    }
  }
  return total
}

const extractTierGroupLabel = (tierLabel?: string | null, levelLine?: string | null) => {
  const candidates = [tierLabel, levelLine]
  for (const candidate of candidates) {
    const normalized = normalizeTierText(candidate ?? '')
    if (!normalized) continue
    const digitMatch = normalized.match(/(\d+)/)
    if (digitMatch) {
      return `${digitMatch[1]}티어`
    }
    const romanMatch = normalized.match(/\b([IVXLCDM]+)\b/i)
    if (romanMatch) {
      const numeral = romanMatch[1] ?? ''
      const numericValue = numeral ? romanToNumber(numeral) : null
      if (numericValue) {
        return `${numericValue}티어`
      }
      return `티어 ${numeral.toUpperCase()}`
    }
    if (normalized.includes('티어')) {
      return normalized
    }
    if (normalized.includes('계층') || normalized.includes('단계')) {
      return normalized.replace(/(계층|단계)/g, '티어')
    }
    return normalized
  }
  return '티어 정보 없음'
}

interface PassiveSummaryCard {
  key: string
  name: string
  icon: string
  levelDisplay: string
  summaryLine: string
  sectionKey: PassiveSectionKey
  tierLabel: string
  levelLine: string
  tierGroup: string
  typeLabel: string
  tierValue: number
}

interface PassiveSummaryRow {
  id: string
  label: string
  sections: Array<{
    key: PassiveSectionKey
    label: string
    effects: PassiveSummaryCard[]
  }>
}

const buildPassiveCard = (effect: ArkPassiveEffect, index: number): PassiveSummaryCard => {
  const tooltip = parsePassiveTooltip(effect.toolTip)
  const tierLabel = inlineText(effect.description)
  const levelLine =
    tooltip.lines.find(line => /아크\s*패시브\s*레벨/i.test(line)) || tooltip.title || ''
  const summaryLine =
    tooltip.lines.find(line => line && line !== levelLine) || tooltip.title || tierLabel
  const descriptor = parsePassiveDescription(effect.description)
  const name = inlineText(effect.name)
  const displayName = stripPassiveStageKeywords(descriptor.passiveName) || descriptor.passiveName || name || '패시브'
  const levelValueMatch = levelLine.match(/(\d+)/)
  const levelDisplay = levelValueMatch ? `Lv.${levelValueMatch[1]}` : ''
  const section =
    PASSIVE_SECTIONS.find(section => tierLabel.includes(section.keyword)) ||
    PASSIVE_SECTIONS.find(section => levelLine.includes(section.keyword)) ||
    PASSIVE_SECTIONS.find(section => name.includes(section.keyword)) ||
    PASSIVE_SECTIONS[0]

  const tierGroup = extractTierGroupLabel(tierLabel, levelLine)

  return {
    key: `${displayName || 'passive'}-${index}`,
    name: displayName,
    icon: effect.icon || '',
    levelDisplay: descriptor.levelLabel || levelDisplay,
    summaryLine: summaryLine || '효과 정보 없음',
    sectionKey: section.key,
    tierLabel,
    levelLine,
    tierGroup: descriptor.tierLabel || tierGroup,
    typeLabel: descriptor.typeLabel,
    tierValue: descriptor.tierValue
  }
}

const buildArkPassiveMatrix = (effects: ArkPassiveEffect[] = []): PassiveSummaryRow[] => {
  const rows: PassiveSummaryRow[] = []
  const rowMap = new Map<string, PassiveSummaryRow>()

  effects.forEach((effect, index) => {
    const card = buildPassiveCard(effect, index)
    const rowLabel = extractTierGroupLabel(card.tierLabel, card.levelLine)
    let row = rowMap.get(rowLabel)
    if (!row) {
      row = {
        id: `${rowLabel}-${rows.length}`,
        label: rowLabel,
        sections: PASSIVE_SECTIONS.map(section => ({
          key: section.key,
          label: section.label,
          effects: []
        }))
      }
      rowMap.set(rowLabel, row)
      rows.push(row)
    }
    const targetSection = row.sections.find(section => section.key === card.sectionKey) ?? row.sections[0]
    targetSection.effects.push(card)
  })

  const tierValue = (label: string) => {
    const numericMatch = label.match(/(\d+)/)
    if (numericMatch) return Number(numericMatch[1])
    return Number.POSITIVE_INFINITY
  }

  rows.forEach(row => {
    row.sections.forEach(section => {
      // 모든 효과를 노출하도록 제한을 제거
      section.effects = [...section.effects]
    })
  })

  return rows.sort((a, b) => tierValue(a.label) - tierValue(b.label))
}

const parseCoreMeta = (rawName: string) => {
  const alignmentMatch = rawName.match(/(질서|혼돈)/)
  const alignment: 'order' | 'chaos' | 'unknown' =
    alignmentMatch?.[1] === '질서' ? 'order' : alignmentMatch?.[1] === '혼돈' ? 'chaos' : 'unknown'

  const celestialMatch = rawName.match(/(해|달|별)/)
  const celestialMap: Record<string, 'sun' | 'moon' | 'star'> = {
    '해': 'sun',
    '달': 'moon',
    '별': 'star'
  }
  const celestial = celestialMap[celestialMatch?.[1] ?? ''] ?? 'unknown'

  const cleaned = rawName.replace(/^(질서|혼돈)?\s*의?\s*(해|달|별)?\s*코어\s*[:：]?\s*/i, '').trim()
  const displayName = cleaned || rawName

  return {
    alignment,
    celestial,
    displayName
  }
}

const buildCoreMatrix = (coreSlots: Array<{ alignment: string; celestial: string }>) => {
  const hasSlots = coreSlots.length > 0
  if (!hasSlots) return { headers: [] as Array<{ key: string; label: string }>, rows: [] as any[] }

  const baseHeaders: Array<{ key: string; label: string }> = [
    { key: 'sun', label: '해' },
    { key: 'moon', label: '달' },
    { key: 'star', label: '별' }
  ]
  const hasUnknownCelestial = coreSlots.some(slot => slot.celestial === 'unknown')
  const headers = hasUnknownCelestial ? [...baseHeaders, { key: 'unknown', label: '기타' }] : baseHeaders

  const baseRows: Array<{ key: string; label: string }> = [
    { key: 'order', label: '질서' },
    { key: 'chaos', label: '혼돈' }
  ]
  const hasUnknownAlignment = coreSlots.some(slot => slot.alignment === 'unknown')
  const rowsWithUnknown = hasUnknownAlignment ? [...baseRows, { key: 'unknown', label: '기타' }] : baseRows

  const rows = rowsWithUnknown.map(row => ({
    key: row.key,
    label: row.label,
    cells: headers.map(header => ({
      key: `${row.key}-${header.key}`,
      label: header.label,
      slots: coreSlots.filter(slot => slot.alignment === row.key && slot.celestial === header.key)
    }))
  }))

  return { headers, rows }
}

const SUPPORT_POINT_KEYWORDS = ['축복의여신', '축복의 여신', '정열의춤사위', '정열의 춤사위']
const normalizeSupportText = (value?: string | number | null) => inlineText(value).replace(/\s+/g, '')

const combatRole = computed<CombatRole | null>(() => {
  const passive = arkGridResponse.value?.arkPassive
  if (!passive) return null

  const points = passive.points ?? []

  const hasSupportPoint = points.some(point => {
    const pointValue =
      typeof point.value === 'number'
        ? point.value
        : Number(String(point.value ?? '').replace(/,/g, ''))
    const hasAssignedPoint = Number.isFinite(pointValue) ? pointValue > 0 : Boolean(point.value)

    const normalizedLines = [
      point.name,
      point.description,
      ...(point.tooltip ? flattenTooltipLines(point.tooltip) : [])
    ]
      .map(normalizeSupportText)
      .filter(Boolean)

    if (!normalizedLines.length) return false

    const mentionsSupportKeyword = normalizedLines.some(text =>
      SUPPORT_POINT_KEYWORDS.some(keyword => text.includes(normalizeSupportText(keyword)))
    )

    return hasAssignedPoint && mentionsSupportKeyword
  })

  if (hasSupportPoint) return 'support'

  const effects = passive.effects ?? []
  const hasSupportEffect = effects.some(effect => {
    const normalizedLines = [effect.name, effect.description, effect.toolTip]
      .flatMap(item => (Array.isArray(item) ? item : [item]))
      .flatMap(item => (item ? flattenTooltipLines(String(item)) : []))
      .map(normalizeSupportText)
      .filter(Boolean)

    if (!normalizedLines.length) return false

    return normalizedLines.some(text =>
      SUPPORT_POINT_KEYWORDS.some(keyword => text.includes(normalizeSupportText(keyword)))
    )
  })

  if (hasSupportEffect) return 'support'

  return 'dealer'
})

const arkSummary = computed(() => {
  const passive = arkGridResponse.value?.arkPassive
  const grid = arkGridResponse.value?.arkGrid
  const passiveMatrix = buildArkPassiveMatrix(passive?.effects ?? [])
  const passiveEffects =
    passive?.effects?.map((effect, index) => {
      const card = buildPassiveCard(effect, index)
      return {
        key: card.key,
        name: card.name,
        sectionKey: card.sectionKey,
        levelLabel: card.levelDisplay,
        tierGroup: card.tierGroup,
        typeLabel: card.typeLabel,
        tierLabel: card.tierLabel,
        tierValue: card.tierValue
      }
    }).filter(effect => effect.name) ?? []

  const parseLevelNumeric = (label?: string | null) => {
    if (!label) return -Infinity
    const match = String(label).match(/(\d+)/)
    return match ? Number(match[1]) : -Infinity
  }

  const bestRankBySection = (sectionKey: PassiveSectionKey) => {
    const candidatesFromMatrix = passiveMatrix.flatMap(row =>
      row.sections.find(section => section.key === sectionKey)?.effects ?? []
    )
    const candidates =
      candidatesFromMatrix.length > 0
        ? candidatesFromMatrix
        : passiveEffects.filter(effect => effect.sectionKey === sectionKey)
    if (!candidates.length) return null

    const best = candidates
      .slice()
      .sort((a, b) => {
        const tierA = Number.isFinite((a as any).tierValue) ? (a as any).tierValue : -Infinity
        const tierB = Number.isFinite((b as any).tierValue) ? (b as any).tierValue : -Infinity
        if (tierA !== tierB) return tierB - tierA
        const levelA = parseLevelNumeric((a as any).levelLabel || (a as any).levelDisplay || (a as any).levelLine)
        const levelB = parseLevelNumeric((b as any).levelLabel || (b as any).levelDisplay || (b as any).levelLine)
        if (levelA !== levelB) return levelB - levelA
        return 0
      })[0]
    return {
      name: (best as any).name || '',
      tier: (best as any).tierLabel || (best as any).tierGroup || (best as any).levelLine || '',
      level: (best as any).levelLabel || (best as any).levelDisplay || (best as any).levelLine || ''
    }
  }

  const rawSlots = (grid?.slots ?? (grid as any)?.Slots ?? []) as any[]
  const coreSlots = rawSlots
    .map((slot, index) => {
      const getField = (...keys: string[]) => {
        for (const key of keys) {
          if (slot[key] !== undefined && slot[key] !== null && slot[key] !== '') return slot[key]
        }
        return null
      }

      const rawName =
        inlineText(getField('name', 'Name')) || `코어 ${getField('index', 'Index') ?? index + 1}`
      const meta = parseCoreMeta(rawName)
      const name = meta.displayName
      const gradeLabel = inlineText(getField('grade', 'Grade', 'gradeName', 'GradeName'))
      const tooltipRaw = getField('tooltip', 'Tooltip')
      const nameColor = coreNameColorFromTooltip(tooltipRaw) || extractTooltipColor(tooltipRaw)
      const gradeColor =
        nameColor ||
        coreGradeColor(gradeLabel) ||
        (meta.alignment === 'order' ? '#e11d48' : meta.alignment === 'chaos' ? '#2563eb' : '')
      const pointValue = getField('point', 'Point')
      const pointLabel = pointValue !== null && pointValue !== undefined ? `${pointValue}P` : ''
      const icon = getField('icon', 'Icon') || ''
      return {
        key: `slot-${getField('index', 'Index') ?? index}`,
        name,
        alignment: meta.alignment,
        celestial: meta.celestial,
        icon,
        grade: gradeLabel,
        gradeColor,
        nameColor,
        tooltip: tooltipRaw || '',
        pointLabel,
        initial: name.charAt(0) || 'C'
      }
    })
    .filter(slot => slot.icon || slot.pointLabel || slot.name)
  const coreMatrix = buildCoreMatrix(coreSlots)

  const appliedPoints = (passive?.points ?? [])
    .map((point, index) => {
      const label = inlineText(point.name) || `포인트 ${index + 1}`
      const normalizeValue = (): string => {
        if (typeof point.value === 'number') return `${point.value}P`
        if (point.value !== undefined && point.value !== null) return inlineText(point.value)
        if (point.description) return inlineText(point.description)
        return ''
      }
      const value = normalizeValue() || '0P'
      const description = inlineText(point.description) ||
        (point.tooltip ? flattenTooltipLines(point.tooltip).map(inlineText).filter(Boolean).join(' ') : '')
      return {
        key: `point-${label}-${index}`,
        label,
        value,
        description
      }
    })
    .filter(point => point.label)

  const corePassives = passiveMatrix.flatMap(row => row.sections.flatMap(section => section.effects))

  const passiveSectionRanks = PASSIVE_SECTIONS.map(section => {
    const rank = bestRankBySection(section.key)
    return {
      key: section.key,
      label: section.label,
      tier: rank?.tier || '',
      level: rank?.level || ''
    }
  })

  return {
    passiveTitle: inlineText(passive?.title),
    slotCount: grid?.slots?.length ?? 0,
    coreSlots,
    coreMatrix,
    appliedPoints,
    passiveMatrix,
    corePassives,
    passiveEffects,
    passiveSectionRanks
  }
})

const cardSummary = computed(() => {
  const cards =
    cardResponse.value?.card?.cards
      ?.filter(card => inlineText(card.name))
      .map((card, index) => {
        const awakeCountRaw = Number(card.awakeCount)
        const awakeTotalRaw = Number(card.awakeTotal)
        const awakeCount = Number.isFinite(awakeCountRaw) ? awakeCountRaw : null
        const awakeTotal = Number.isFinite(awakeTotalRaw) ? awakeTotalRaw : null
        const awakeLabel =
          awakeCount !== null && awakeTotal !== null ? `${awakeCount}/${awakeTotal}` : null
        return {
          key: `card-${card.slot ?? index}`,
          name: inlineText(card.name),
          icon: card.icon || '',
          grade: inlineText(card.grade),
          awakeLabel,
          awakeCount,
          awakeTotal
        }
      }) ?? []


  const equippedCount = cards.length

  const effects =
    cardResponse.value?.card?.effects?.map((effect, index) => {
      const slots = effect.cardSlots ?? []
      const slotWithIndex = slots.map((slot, idx) => ({ slot, idx }))
      const activeSlot =
        slotWithIndex
          .filter(entry => equippedCount >= entry.slot)
          .reduce((best, entry) => {
            if (!best) return entry
            return entry.slot >= best.slot ? entry : best
          }, null) ?? null

      const activeIndex =
        activeSlot?.idx ?? (effect.items?.length ? effect.items.length - 1 : 0)
      const activeItem = effect.items?.[activeIndex]

      const label =
        inlineText(activeItem?.name) ||
        effect.items?.map(item => inlineText(item.name)).find(Boolean) ||
        `세트 효과 ${effect.index ?? index + 1}`

      const descriptions = [inlineText(activeItem?.description)].filter(Boolean)

      const setLabel =
        activeSlot?.slot && activeSlot.slot > 0
          ? `${activeSlot.slot}세트`
          : slots.length
            ? `${slots.join(' / ')}세트`
            : ''
      return {
        key: `effect-${effect.index ?? index}`,
        label,
        descriptions,
        setLabel
      }
    }) ?? []

  return {
    cards,
    effects
  }
})

const expandHeroImage = () => {
  if (hasCharacterImage.value) {
    isHeroImageLarge.value = true
  }
}

const shrinkHeroImage = () => {
  isHeroImageLarge.value = false
}

const openHeroImagePopup = () => {
  if (hasCharacterImage.value) {
    isHeroImagePopupOpen.value = true
  }
}

const closeHeroImagePopup = () => {
  isHeroImagePopupOpen.value = false
}

watch(characterImageSrc, () => {
  isHeroImageLarge.value = false
  isHeroImagePopupOpen.value = false
})

watch(activeCharacter, () => {
  isHeroImageLarge.value = false
  isHeroImagePopupOpen.value = false
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
      const last = matches[matches.length - 1] ?? ''
      return last.replace(/[^\d.,]/g, '')
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
  const stats = activeCharacter.value?.stats
    ? activeCharacter.value.stats.filter(stat => stat.type !== '낙원력').map(stat => ({ ...stat }))
    : []
  return stats
})

const panelFilterQuery = computed(() => characterName.value.trim().toLowerCase())

const panelHistoryItems = computed(() => {
  if (activeSearchPanelTab.value !== 'recent') return history.value
  if (!panelFilterQuery.value) return history.value
  return history.value.filter(item => item.characterName.toLowerCase().includes(panelFilterQuery.value))
})

const panelFavoriteItems = computed(() => favorites.value)

const closeSearchPanel = () => {
  searchPanelOpen.value = false
}

const handleSearchFocus = () => {
  searchPanelOpen.value = true
  activeSearchPanelTab.value = 'recent'
}

const handleSearchPanelSelect = (name: string) => {
  closeSearchPanel()
  executeSearch(name)
}

const handleOutsideSearchClick = (event: MouseEvent) => {
  if (!searchPanelWrapperRef.value) return
  if (searchPanelWrapperRef.value.contains(event.target as Node)) return
  closeSearchPanel()
}

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
  if (typeof document !== 'undefined') {
    document.addEventListener('click', handleOutsideSearchClick)
  }

  // URL에서 캐릭터 이름 확인 후 자동 로드
  const urlCharacter = route.query.character
  if (urlCharacter && typeof urlCharacter === 'string') {
    searchCharacter(urlCharacter, { updateUrl: false })
  }
})

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.removeEventListener('click', handleOutsideSearchClick)
  }
})

// URL 쿼리 파라미터 변경 감지 (브라우저 뒤로가기/앞으로가기 지원)
watch(
  () => route.query.character,
  (newCharacter, oldCharacter) => {
    if (newCharacter && typeof newCharacter === 'string' && newCharacter !== oldCharacter) {
      // 현재 보고 있는 캐릭터와 다른 경우에만 로드
      if (newCharacter !== character.value?.characterName) {
        searchCharacter(newCharacter, { updateUrl: false })
      }
    }
  }
)

watch(activeResultTab, async newTab => {
  await nextTick()
  if (newTab === 'arkGrid') {
    await ensureArkGridData()
  } else if (newTab === 'skills') {
    await ensureSkillData()
  } else if (newTab === 'collection') {
    await ensureCollectiblesData()
  } else if (newTab === 'summary') {
    await Promise.all([ensureSkillData(), ensureCollectiblesData(), ensureArkGridData(), ensureCardData()])
  }
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

const searchCharacter = async (
  name: string,
  options: { forceRefresh?: boolean; updateUrl?: boolean } = {}
) => {
  const success = await searchCharacterData(name, { forceRefresh: options.forceRefresh })
  if (!success) return
  activeResultTab.value = DEFAULT_RESULT_TAB
  if (options.updateUrl !== false) {
    router.push({ query: { character: name } })
  }
}
const retrySearch = () => {
  if (characterName.value) {
    searchCharacter(characterName.value)
  }
}

const handleRefreshClick = () => {
  if (loading.value) return
  const target = activeCharacter.value?.characterName?.trim()
  if (!target) return
  searchCharacter(target, { forceRefresh: true })
}

const dismissError = () => {
  error.value = null
}

const clearSearch = () => {
  clearSearchData()
  activeResultTab.value = DEFAULT_RESULT_TAB
}

const executeSearch = (name: string) => {
  const trimmed = name.trim()
  if (!trimmed) return
  closeSearchPanel()
  characterName.value = trimmed
  searchCharacter(trimmed, { forceRefresh: true })
}

type AutocompleteSelectPayload = Suggestion | { id: string; name: string }

const handleSuggestionSelect = (suggestion: AutocompleteSelectPayload) => {
  executeSearch(suggestion.name)
}

const viewCharacterDetail = (summary: CharacterProfile | SiblingCharacter) => {
  const targetName = summary.characterName
  if (!targetName) return
  searchCharacter(targetName)
}

const extractTooltipLines = (tooltip?: string): string[] =>
  flattenTooltipLines(tooltip, { fallbackBreaks: true })

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
  if (!normalized.length) return '—'
  const percentMatch = normalized.match(/^([+-]?\d+(?:\.\d+)?)\s*%$/)
  const percentValue = percentMatch?.[1]
  if (percentValue) {
    const formatted = formatNumberLocalized(
      percentValue,
      percentValue.includes('.') ? 2 : undefined
    )
    return formatted === '—' ? normalized : `${formatted}%`
  }
  const numericMatch = normalized.match(/^([+-]?\d+(?:\.\d+)?)(?:\s*점)?$/)
  const numericValue = numericMatch?.[1]
  if (numericValue) {
    const formatted = formatNumberLocalized(
      numericValue,
      numericValue.includes('.') ? 2 : undefined
    )
    return formatted
  }
  return normalized
}

const formatCombatPower = (value?: number | string) => formatNumberLocalized(value)

const formatInteger = (value?: number | string) => formatNumberLocalized(value)
</script>

<style>
.character-search {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
  --quality-badge-bg: rgba(15, 23, 42, 0.333);
}

.page-header {
  /* display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr)); */
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20px;
  padding: 10px 20px;
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-color);
  justify-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.page-header h1 {
  font-size: calc(1.5rem - 2px);
  color: var(--text-primary);
  margin: 0;
  font-weight: 700;
}

.header-search {
  justify-self: center;
  width: min(1100px, 100%);
}

.header-search__row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
}

.header-search__row .search-panel-wrapper {
  flex: 1 1 340px;
  min-width: 280px;
}

.header-search :deep(.autocomplete-container) {
  width: 100%;
}

.header-right {
  width: 100%;
}

.search-container {
  background: var(--surface-color);
  padding: 22px;
  box-shadow: var(--shadow-sm);
  width: min(1280px, 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 24px;
  width:100%;
  min-height: inherit;
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
  font-size: calc(1.6rem - 2px);
  color: var(--text-secondary);
  cursor: pointer;
}

.sidebar-content h3 {
  margin: 0 0 10px;
  color: var(--text-primary);
  font-size: calc(1rem - 2px);
}

.sidebar-description {
  margin: 0 0 16px;
  color: var(--text-secondary);
  font-size: calc(0.9rem - 2px);
}

.sidebar-menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-menu-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.sidebar-menu-item:hover:not(.disabled) {
  border-color: var(--primary-color);
  transform: translateX(2px);
}

.sidebar-menu-item.active {
  border-color: var(--primary-color);
  background: rgba(102, 126, 234, 0.12);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}

.dark .sidebar-menu-item.active {
  background: rgba(124, 143, 216, 0.2);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.45);
}

.sidebar-menu-item.disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.sidebar-menu-icon {
  font-size: calc(1.4rem - 2px);
}

.sidebar-menu-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-menu-label {
  font-weight: 600;
  color: var(--text-primary);
}

.sidebar-menu-desc {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.sidebar-menu-badge {
  font-size: var(--font-xs);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.sidebar-footnote {
  margin: 16px 0 0;
  font-size: calc(0.8rem - 2px);
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

.search-input {
  flex: 1;
  padding: var(--space-md) var(--space-lg);
  font-size: var(--font-base);
  border: 2px solid var(--input-border);
  border-radius: var(--radius-sm);
  background: var(--input-bg);
  color: var(--text-primary);
}

.states-section {
  margin-bottom: 30px;
}

.states-section h2 {
  font-size: var(--font-xl);
  color: var(--text-primary);
  margin-bottom: 15px;
  font-weight: var(--font-bold);
}

.states-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-xl);
}

.state-card {
  padding: var(--space-xl);
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 2px solid var(--border-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 120px;
}

.state-label {
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
  font-weight: 600;
  margin-bottom: 10px;
}

.character-results {
  display: flex;
  flex-direction: column;
  gap: var(--space-2xl);
}

.results-layout {
  display: flex;
  gap: var(--space-2xl);
  align-items: stretch;
}

.results-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.view-tabs {
  display: flex;
  gap: var(--space-sm);
}

.view-tabs--header {
  justify-content: flex-end;
  align-items: center;
}

.view-tab-button {
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-xs);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: all var(--transition-base);
  word-break: keep-all;
  white-space: pre-wrap;
}

.view-tabs--header .view-tab-button {
  padding: 5px 10px;
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

.summary-panel {
  min-height: 360px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--space-md);
}

.summary-grid--modules {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.summary-grid--stacked {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.summary-card {
  border: none;
  border-radius: var(--radius-lg);
  background: transparent;
  /* padding: var(--space-md); */
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  box-shadow: none;
}

.summary-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-md);
}

.summary-card h4 {
  margin: 0;
  font-size: calc(var(--font-lg) - 4px);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.summary-eyebrow {
  margin: 0 0 4px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: calc(var(--font-xs) - 1px);
  color: var(--primary-color);
  font-weight: 700;
}

.ark-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.ark-section__block {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  padding: var(--space-md);
  background-color: var(--bg-secondary);
  border-radius: var(--radius-xl);
}

.ark-section__subhead {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.ark-section__title {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-chip {
  align-self: center;
  border-radius: var(--radius-full);
  padding: 2px 6px;
  font-size: var(--font-xs);
  font-weight: 700;
  background: rgba(102, 126, 234, 0.12);
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.summary-chip--muted {
  background: var(--bg-hover);
  color: var(--text-secondary);
  border-color: var(--border-color);
}

.summary-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.summary-list--split .summary-list-item {
  align-items: center;
}

.summary-list--compact .summary-list-item {
  padding: 10px 12px;
}

.summary-list-item {
  display: grid;
  grid-template-columns: 50px 1fr 1fr;
  align-items: center;
  gap: var(--space-xs);
  padding:4px;
}

.summary-list-text {
  display: flex;
  flex-direction: column;
  justify-content: center;
  width:100%;
  height: 100%;
}

.summary-title {
  margin: 0;
  font-size: var(--font-xs);
  font-weight: 600;
  color: var(--text-primary);
}

.summary-sub {
  margin: 0;
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.summary-pill-row {
  display: flex;
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.summary-pill {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.summary-pill--accent {
  background: rgba(236, 72, 153, 0.2);
  color: #ec4899;
}

.summary-pill--ghost {
  background: var(--bg-secondary);
}

.summary-list-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.summary-skill-icon-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.summary-skill-icon-wrapper {
  position: relative;
  display: inline-block;
}

.summary-skill-level-dot {
  position: absolute;
  right: -6px;
  bottom: -6px;
  /* min-width: 22px; */
  padding: 2px 6px;
  border-radius: 999px;
  background: var(--primary-color);
  color: #ffffff;
  font-size: var(--font-xxs, var(--font-xs));
  font-weight: 700;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.summary-skill-head {
  display: flex;
  align-items: center;
  /* justify-content: space-between; */
}

.summary-skill-level-inline {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  margin-left:5px;
}

.summary-skill-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.summary-sub--point {
  font-weight: 700;
  color: var(--primary-color);
}

.summary-skill-meta {
  flex-wrap: wrap;
}

.summary-pill--icon {
  gap: 6px;
}

.summary-pill-icon {
  border-radius: 6px;
}

.summary-tripod-icon-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.summary-tripod-icon {
  position: relative;
  /* width: 26px;
  height: 26px; */
  /* border-radius: 8px; */
  /* background: var(--bg-secondary); */
  /* border: 1px solid var(--border-color); */
  display: grid;
  place-items: center;
}

.summary-tripod-icon--1 {
  border-color: #00a1e0;
  box-shadow: 0 0 0 1px rgba(0, 161, 224, 0.35);
}

.summary-tripod-icon--2 {
  border-color: #7cca15;
  box-shadow: 0 0 0 1px rgba(124, 202, 21, 0.35);
}

.summary-tripod-icon--3 {
  border-color: #ff9500;
  box-shadow: 0 0 0 1px rgba(255, 149, 0, 0.35);
}

.summary-tripod-img {
  border-radius: 8px;
}

.summary-tripod-fallback {
  font-weight: 700;
  color: var(--text-secondary);
}

.summary-tripod-slot-dot {
  /* position: absolute;
  right: -6px;
  bottom: -6px; */
  width: 18px;
  height: 18px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  font-size: var(--font-xxs, var(--font-xs));
  font-weight: 700;
  color: #ffffff;
  background: #4b5563;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.summary-tripod-icon--1 {
  background: #00a1e0;
}

.summary-tripod-icon--2 {
  background: #7cca15;
}

.summary-tripod-icon--3 {
  background: #ff9500;
}

.summary-skill-gems {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
}

.summary-gem-grid {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 5px;
}

.summary-gem-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 4px;
  align-items: center;
}

.summary-gem-row--icons .summary-gem-cell {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
}

.summary-gem-icon-stack {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.summary-gem-icon-wrapper {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-primary);
  display: grid;
  place-items: center;
}

.summary-gem-icon-wrapper--empty {
  background: var(--bg-hover);
}

.summary-gem-text {
  font-size: var(--font-xxs, var(--font-xs));
  color: var(--text-secondary);
  padding: 2px 4px;
  text-align: center;
}

.summary-gem-empty {
  font-size: 1rem;
  color: var(--text-tertiary);
  line-height: 1;
}

.summary-gem-label {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
  text-align: center;
}

.summary-gem-level-dot {
  padding: 2px;
  border-radius: 999px;
  background: var(--primary-color);
  color: #ffffff;
  font-size: var(--font-xxs);
  font-weight: 700;
  height: 18px;
  width: 18px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.summary-loose-gems {
  margin-top: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.summary-inline--muted {
  color: var(--text-secondary);
}

.summary-loose-gem-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--space-sm);
}

.summary-loose-gem {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px;
  align-items: center;
  padding: 8px;
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
}

.summary-loose-gem-body .summary-title {
  font-size: var(--font-sm);
}

.summary-engraving-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(auto, 1fr));
  gap: var(--space-sm);
}

.summary-engraving-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  background: var(--bg-secondary);
}

.engrave-craft-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.engrave-craft-image {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.engrave-craft-text {
  font-size: var(--font-xs);
  font-weight: 700;
  color: var(--text-primary);
}

.summary-engraving-name {
  text-align: center;
  width: 100%;
}

.summary-engrave-meta-row {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
  min-height: 24px;
}

.engrave-level-image {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.summary-inline {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-progress-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.summary-progress-list.summary-progress-list--dense {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-sm) var(--space-lg);
}

.summary-progress {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 5px 2px;
}

.summary-progress__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-progress__bar {
  position: relative;
  width: 100%;
  height: 8px;
  border-radius: 12px;
  background: var(--bg-hover);
  overflow: hidden;
}

.summary-progress__bar span {
  position: absolute;
  inset: 0;
  width: 0;
  display: block;
  background: linear-gradient(90deg, var(--primary-color), #a855f7);
  transition: width 0.4s ease;
}

.summary-progress__meta {
  margin: 0;
  font-size: calc(var(--font-xs) - 1px);
  color: var(--text-secondary);
}

.summary-footnotes {
  list-style: none;
  margin: 0;
  padding: 10px 0 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-top: 1px solid var(--border-color);
}

.summary-footnote-label {
  font-size: var(--font-xs);
  font-weight: 700;
  color: var(--primary-color);
  margin-right: 6px;
}

.summary-footnote-text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-note {
  margin: 2px 0 0;
  font-size: calc(var(--font-sm) - 1px);
  color: var(--text-secondary);
}

.summary-note--warning {
  color: var(--warning-color, #d97706);
}

.summary-icon {
  border-radius: 8px;
}

.summary-icon--fallback {
  width: 55px;
  height: 55px;
  border-radius: 8px;
  background: var(--bg-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: var(--text-secondary);
}

.ark-core-layout{
  display:flex;
  flex-direction: column;
  gap: 10px;
}

.ark-core-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
  gap: var(--space-sm);
  margin-bottom: var(--space-sm);
}

.ark-core-matrix {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ark-core-matrix__header,
.ark-core-matrix__row {
  display: grid;
  grid-template-columns: 40px repeat(var(--ark-core-col-count, 3), minmax(0, 1fr));
  gap: 10px;
  align-items: center;
  justify-items: center;
}

.ark-core-matrix__header {
  align-items: center;
}

.ark-core-matrix__row {
  align-items: stretch;
}

.ark-core-matrix__header-cell {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.ark-core-matrix__corner {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  font-size: var(--font-xs);
  color: var(--text-secondary);
  position: relative;
  padding-right: 8px;
  width: 100%;
}

.ark-core-matrix__row-label {
  font-weight: 700;
  color: var(--text-primary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  font-size: var(--font-xs);
  position: relative;
  padding-right: 8px;
  width: 100%;
}

.ark-core-matrix__row-label::after,
.ark-core-matrix__corner::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--border-color);
}

.ark-core-matrix__cell--empty {
  display: none;
}

.ark-core-card-grid {
  display: flex;
  /* flex-direction: row; */
  gap: var(--space-sm);
  position: relative;
  justify-content: space-between;
}

.ark-core-card-grid::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  border-left: 1px dashed var(--border-color, rgba(148, 163, 184, 0.4));
  pointer-events: none;
}

.ark-core-row-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ark-core-row-label {
  margin: 0;
  font-size: var(--font-sm);
  font-weight: 700;
  color: var(--text-primary);
  padding: 0px 10px;
}

.ark-core-card-row {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  padding: 0px 10px;
}

.ark-core-card-cell {
  display: flex;
}

.ark-core-card {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--bg-secondary);
}

.ark-core-card__thumb {
  position: relative;
  width: 40px;
  height: 40px;
  /* border: 1px solid var(--border-color); */
  /* border-radius: var(--radius-lg); */
  /* background: var(--bg-primary); */
  display: flex;
  align-items: center;
  justify-content: center;
}

.ark-core-card__image {
  border-radius: var(--radius-md);
}

.ark-core-card__placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: var(--text-secondary);
  font-size: var(--font-lg);
}

.ark-core-card__point {
  display: none;
}

.ark-core-card__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.ark-core-card__name {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-primary);
  font-weight: 700;
}

.ark-core-card__meta {
  margin: 0;
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.ark-passive-summary {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.ark-passive-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.ark-passive-grid-header{
  align-items: center;
}

.ark-passive-grid-row {
  align-items: stretch;
}

.ark-passive-grid-header,
.ark-passive-grid-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  justify-items: center;
}

.ark-passive-header-cell {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  font-weight: 700;
}

.ark-passive-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width:100%;
  position: relative;
}

.ark-passive-cell-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(40px, 1fr));
  gap: 10px;
  justify-items: center;
  width:100%;
}

.ark-passive-cell:not(:first-child)::before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: -4px;
  border-left: 1px dashed var(--border-color, rgba(148, 163, 184, 0.4));
  pointer-events: none;
}

.ark-passive-summary-footer {
  margin-top: var(--space-sm);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ark-passive-columns {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.ark-passive-column {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ark-passive-column__title {
  margin: 0;
  font-size: var(--font-sm);
  font-weight: 700;
  color: var(--text-primary);
}

.ark-passive-column__points {
  margin-left: 6px;
  font-size: var(--font-xs);
  font-weight: 700;
  color: var(--text-secondary);
}

.ark-passive-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.ark-passive-list__item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: var(--bg-secondary);
}

.ark-passive-list__level {
  font-weight: 700;
  color: var(--primary-color);
  font-size: var(--font-xs);
}

.ark-passive-list__name {
  font-size: var(--font-xs);
  color: var(--text-primary);
}

.ark-passive-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  border-radius: 12px;
}

.ark-passive-chip-visual {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.ark-passive-chip-labels {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  text-align: center;
}

.ark-passive-chip-level {
  font-size: var(--font-xs);
  color: var(--text-secondary);
  font-weight: 700;
}

.ark-passive-chip-name {
  font-size: var(--font-xs);
  color: var(--text-primary);
  font-weight: 700;
  line-height: 1.2;
  text-align: center;
  word-break: keep-all;
}

.equipment-icon-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height:65px;
  position: relative;
  gap:4px;
}

.equipment-item-level {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  font-weight: 600;
  line-height: 1.1;
}

.equipment-quality-badge {
  position: absolute;
  right: -4px;
  top: 20px;
  padding: 2px 6px;
  border-radius: var(--radius-full);
  background: transparent;
  color: inherit;
  font-size: var(--font-xxs, var(--font-xs));
  font-weight: 700;
  border: 1px solid transparent;
  line-height: 1;
  text-shadow:
    -1px -1px 0 rgba(255, 255, 255, 0.9),
    1px -1px 0 rgba(255, 255, 255, 0.9),
    -1px 1px 0 rgba(255, 255, 255, 0.9),
    1px 1px 0 rgba(255, 255, 255, 0.9);
}

.summary-card--equipment .summary-title,
.summary-card--equipment .summary-inline,
.summary-card--equipment .summary-pill {
  font-size: var(--font-xs);
}

.summary-card--equipment .summary-inline {
  margin: 2px 0;
}

.equipment-slot-label {
  min-width: 42px;
  font-size: var(--font-xs);
  color: var(--text-secondary);
  letter-spacing: 0.05em;
  text-align: center;
}

.equipment-badge-stack {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 6px;
}

.equipment-info-stack {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  min-height:65px;
}

.equipment-line {
  margin: 0;
  font-size: var(--font-xs);
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
}

.equipment-line--quality,
.equipment-line--transcend {
  color: var(--text-secondary);
}

.equipment-progress {
  position: relative;
  flex: 0 0 92px;
  height: 16px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.2);
  overflow: hidden;
}

.equipment-progress--transcend {
  background: transparent;
  flex: 0 0 auto;
  height: auto;
  overflow: visible;
}

.equipment-progress__fill {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 0%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(94, 234, 212, 0.5), rgba(59, 130, 246, 0.6));
}

.equipment-progress--transcend .equipment-progress__fill {
  display: none;
}

.equipment-progress__label {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.equipment-progress__label--inline {
  position: absolute;
  left: 6px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-primary);
  font-weight: 700;
}

.equipment-progress--transcend .equipment-progress__label--inline {
  position: static;
  left: auto;
  top: auto;
  transform: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
}

.equipment-progress__label--transcend .transcend-icon {
  margin: 0;
}

.equipment-transcend-value {
  font-weight: 700;
  color: var(--text-secondary);
  line-height: 1.2;
}

.equipment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  /* padding: 2px 8px; */
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  font-weight: 700;
  background: var(--bg-secondary);
  color: var(--text-primary);
  min-width: 42px;
  width: fit-content;
}

.equipment-badge--enhance {
  background: rgba(99, 102, 241, 0.12);
}

.equipment-badge--quality {
  background: rgba(34, 197, 94, 0.12);
}

.equipment-badge--harmony {
  background: rgba(14, 165, 233, 0.12);
}

.equipment-badge--ilevel {
  background: rgba(148, 163, 184, 0.15);
  color: var(--text-secondary);
}

.bracelet-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: var(--font-xs);
  background: rgba(94, 234, 212, 0.14);
  color: #0f172a;
  min-width: 42px;
  width: fit-content;
}

.bracelet-badge--effect {
  justify-content: center;
}

.bracelet-badge--fullrow {
  width: 100%;
  justify-content: flex-start;
}

.equipment-badge--transcend {
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.18), rgba(59, 130, 246, 0.16));
  color: var(--text-primary);
  gap: 6px;
  justify-content: flex-start;
  min-width: 56px;
}

.equipment-badge--transcend-gold {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(234, 179, 8, 0.2));
  color: #f59e0b;
}

.equipment-badge--special {
  background: rgba(236, 72, 153, 0.12);
  color: #db2777;
}

.transcend-icon {
  display: inline-block;
  width: 21px;
  height: 21px;
  background-image: url('https://cdn-lostark.game.onstove.com/2018/obt/assets/images/common/game/ico_tooltip_transcendence.png');
  background-size: 100%;
  background-position: center top;
  background-repeat: no-repeat;
  border-radius: var(--radius-xs);
}

.transcend-icon--gold {
  background-position: center bottom;
}

.transcend-icon--inline {
  margin-right: 4px;
  vertical-align: middle;
}

.equipment-badge--effect {
  display:inline-block;
  background: var(--bg-secondary);
  color: var(--text-primary);
  white-space:nowrap;
  max-width: 100px;
  text-overflow: ellipsis;
}

.effect-prefix {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 1px 4px;
  margin-right: 4px;
  border: 1px solid currentColor;
  border-radius: 6px;
  font-weight: 700;
  width: 21px;
  height: 21px;
}

.effect-prefix--empty {
  color: transparent;
}

.equipment-badge__divider {
  display: inline-block;
  width: 1px;
  height: 12px;
  margin: 0 8px;
  vertical-align: middle;
}

.equipment-badge__divider--dashed {
  border-left: 1px dashed currentColor;
  opacity: 0.65;
}

.equipment-effect-badges {
  display: inline-flex;
  flex-direction: column;
  height:inherit;
  gap:2px;
}

.equipment-effect-chip {
  display:flex;
  position: relative;
}

.equipment-effect-chip--tooltip .equipment-effect-tooltip {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  z-index: 10;
  opacity: 0;
  pointer-events: none;
  transform: translateY(-4px);
  white-space:none;
  word-break:none;
  width: max-content;
}

.equipment-effect-chip--tooltip:hover .equipment-effect-tooltip,
.equipment-effect-chip--tooltip:focus-within .equipment-effect-tooltip {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.equipment-row-list {
  display: flex;
  flex-direction: column;
}

.equipment-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  align-items: flex-start;
}

.equipment-side {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;
  min-height: 80px;
}

.equipment-side--bracelet {
  grid-column: 1 / span 2;
  border-radius: var(--radius-md);
  align-items: center;
}

.equipment-side--left {
  border-right: 1px dashed var(--border-color);
}

.equipment-empty {
  margin: 0;
  color: var(--text-tertiary);
  font-size: var(--font-xs);
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--space-lg);
}

.equipment-column h5 {
  margin: 0 0 8px;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-pill-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.placeholder-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}

.character-overview-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  background: var(--card-bg);
  border-radius: 20px;
  padding: 15px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  gap: 15px;
  flex: 0 0 380px;
  height: fit-content;
  overflow: visible;
  max-width: 350px;
}

.overview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.refresh-button {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.refresh-button:hover,
.refresh-button:focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-timestamp {
  margin-left: auto;
  color: var(--text-secondary);
  font-size: calc(0.95rem - 2px);
  white-space: nowrap;
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

.hero-avatar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.hero-avatar-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 16px;
  overflow: hidden;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: width 0.2s ease, height 0.2s ease, background-color 0.3s ease, border-color 0.3s ease;
}

.hero-avatar-wrapper.is-large {
  width: 300px;
  height: 350px;
}

.hero-avatar-wrapper :deep(.hero-avatar) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
}

.hero-avatar-controls {
  display: flex;
  justify-content: space-evenly;
  padding: 4px;
}

.hero-avatar-btn {
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: calc(0.8rem - 2px);
  font-weight: 600;
  cursor: pointer;
  padding: 2px 8px;
  transition: color 0.2s ease;
}

.hero-avatar-btn + .hero-avatar-btn {
  border-left: 1px solid var(--border-color);
}

.hero-avatar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  color: var(--text-secondary);
}

.hero-avatar-btn:not(:disabled):hover,
.hero-avatar-btn:not(:disabled):focus-visible {
  color: var(--primary-color);
}

.hero-row--image :deep(.hero-avatar) {
  border-radius: 16px;
  object-fit: cover;
}

.character-portrait-overlay {
  position: fixed;
  inset: 0;
  background: var(--modal-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2200;
  padding: 24px;
}

.character-portrait-overlay__content {
  position: relative;
  max-width: min(90vw, 720px);
  max-height: 90vh;
  border-radius: 18px;
  overflow: hidden;
  background: var(--modal-bg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xl);
}

.character-portrait-overlay__content img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portrait-overlay__close {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: var(--text-inverse, #ffffff);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.dark .portrait-overlay__close {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.portrait-overlay__close:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.hero-title {
  font-size: calc(0.95rem - 2px);
  color: var(--text-secondary);
}

.hero-text{
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-text h2 {
  margin: 4px 0;
  font-size: calc(1.6rem - 2px);
  color: var(--text-primary);
  text-align: center;
}

.hero-text__header {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.favorite-toggle-btn {
  height: fit-content;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.favorite-toggle-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.favorite-toggle-btn:not(:disabled):hover {
  /* transform: scale(1.05); */
}

.favorite-star {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--surface-color, #ffffff);
  color: var(--text-tertiary, #d1d5db);
  font-size: calc(1.2rem - 2px);
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.1);
  padding-bottom: 3px;
}

.favorite-toggle-btn.is-active .favorite-star {
  background: var(--warning-soft-bg, #ffe792);
  color: var(--text-inverse, #1a1a1a);
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.15);
}

.hero-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.meta-item {
  padding: 5px 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.meta-item span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  margin-right: 5px;
}

.meta-item strong {
  font-size: calc(1rem - 2px);
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
  font-size: calc(0.9rem);
  color: var(--text-secondary);
}

.special-count {
  font-size: calc(0.85rem - 2px);
  color: var(--text-tertiary);
}

.special-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  width:100%;
}

.special-grid--icons {
  display: grid;
  grid-template-columns: repeat(4, minmax(70px, 1fr));
  gap: 10px;
  overflow: visible;
  justify-items: center;
}

.special-icon-wrapper {
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  border: 2px solid transparent;
  border-radius: 16px;
  box-sizing: border-box;
  padding:10px;
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
  width: 50px;
  height: 50px;
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
  font-size: calc(1.1rem - 2px);
}

.special-label {
  width: 100%;
  font-size: calc(0.8rem - 2px);
  color: var(--text-secondary);
  text-align: center;
  white-space: nowrap;
  line-height: 1.2;
}

.special-type {
  font-size: calc(0.85rem - 2px);
  color: var(--text-tertiary);
}

.special-highlights {
  margin: 0;
  /* padding-left: 18px; */
  color: inherit;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.special-highlights span {
  list-style: disc;
  white-space: pre-line;
}

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.hero-row--profile-stats h3 {
  margin: 0;
  font-size: calc(0.9rem);
  color: var(--text-secondary);
  text-align: center;
}

.profile-stat {
  padding: 5px 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.profile-stat span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  word-break: keep-all;
  /* min-width: 100px; */
}

.profile-stat strong {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}

.expedition-section,
.detail-panel {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 10px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}

@media (max-width: 1280px) {
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
  font-size: calc(0.9rem - 2px);
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
  font-size: calc(0.95rem - 2px);
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
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.member-name {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}

.member-ilvl {
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
}

.member-detail {
  font-size: calc(0.8rem - 2px);
  color: var(--primary-color);
  font-weight: 600;
}

.search-panel-wrapper {
  position: relative;
  width: 100%;
}

.search-panel-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  width: min(350px, 100%);
  max-width: 350px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  z-index: 1200;
}

.search-panel-tabs {
  display: flex;
  gap: 8px;
}

.search-panel-tab {
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.search-panel-tab.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.25);
}

.search-panel-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 320px;
  overflow-y: auto;
}

.panel-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
}

.panel-clear-btn {
  padding: 4px 12px;
  border-radius: 8px;
  border: none;
  background: var(--error-color);
  color: var(--text-inverse);
  cursor: pointer;
  font-size: calc(0.8rem - 2px);
  font-weight: 600;
}

.panel-empty {
  padding: 20px;
  text-align: center;
  border: 1px dashed var(--border-color);
  border-radius: 12px;
  color: var(--text-tertiary);
  font-size: calc(0.9rem - 2px);
}

.panel-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-list-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-secondary);
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  cursor: pointer;
  transition: border 0.2s, background 0.2s;
}

.panel-list-item:hover {
  border-color: var(--primary-color);
  background: var(--bg-hover);
}

.panel-list-name {
  font-size: calc(0.95rem - 2px);
  color: var(--text-primary);
  font-weight: 600;
}

.panel-list-meta {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.panel-favorite-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-favorite-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--bg-secondary);
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: border 0.2s, transform 0.2s;
}

.panel-favorite-item:hover {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.panel-favorite-item :deep(.panel-favorite-image) {
  border-radius: 50%;
  object-fit: cover;
}

.panel-favorite-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.panel-favorite-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: calc(0.95rem - 2px);
}

.panel-favorite-meta {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
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
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header-left {
    text-align: center;
    width: 350px;
    justify-content: space-between;
  }

  .page-header h1 {
    font-size: calc(1.2rem - 2px);
  }

  .header-search {
    width: 100%;
  }

  .header-search__row {
    gap: 10px;
  }

  .header-right {
    display: none;
  }

  .hero-row--levels .hero-levels {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .profile-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .search-panel-dropdown {
    padding: 16px;
  }
}

.hero-row--paradise h3 {
  margin: 0;
  font-size: calc(0.9rem);
  color: var(--text-secondary);
  text-align: center;
}

.paradise-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 2px;
}

.paradise-item {
  padding: 5px 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.paradise-item span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  min-width: 70px;
}

.paradise-item strong {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}
</style>
