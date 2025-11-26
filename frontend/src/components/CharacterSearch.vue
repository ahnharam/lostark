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
                  :skill-loading="skillLoading"
                  :skill-error="skillError"
                  :engraving-summary="engravingSummary"
                  :collection-summary="collectionSummary"
                  :collectibles-loading="collectiblesLoading"
                  :collectibles-error="collectiblesError"
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
import { lostarkApi } from '@/api/lostark'
import type {
  CharacterProfile,
  SiblingCharacter,
  SearchHistory,
  Equipment,
  Engraving,
  CharacterStat,
  ArkGridResponse,
  ArkPassiveEffect,
  SkillMenuResponse,
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

interface ErrorState {
  message: string
  type: 'error' | 'warning' | 'info'
  title?: string
}

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

const FAVORITES_STORAGE_KEY = 'loa:favorites'
const HISTORY_STORAGE_KEY = 'loa:history'
const DEFAULT_RESULT_TAB: ResultTabKey = 'summary'
const resultTabs: Array<{ key: ResultTabKey; label: string }> = [
  { key: 'summary', label: '내 정보 간소화' },
  { key: 'skills', label: '스킬' },
  { key: 'detail', label: '장비' },
  { key: 'collection', label: '수집' },
  { key: 'ranking', label: '랭킹' },
  { key: 'arkGrid', label: '아크 그리드' },
  { key: 'expedition', label: '보유 캐릭터' }
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

const characterName = ref('')
const character = ref<CharacterProfile | null>(null)
const loading = ref(false)
const error = ref<ErrorState | null>(null)
const siblings = ref<SiblingCharacter[]>([])
const favorites = ref<CharacterProfile[]>([])
const history = ref<SearchHistory[]>([])
const characterAvailability = ref<Record<string, 'available' | 'unavailable' | 'loading'>>({})
const selectedCharacterProfile = ref<CharacterProfile | null>(null)
const activeResultTab = ref<ResultTabKey>(DEFAULT_RESULT_TAB)
const activePlaceholder = computed(() => tabPlaceholderCopy[activeResultTab.value])
const searchPanelWrapperRef = ref<HTMLElement | null>(null)
const searchPanelOpen = ref(false)
const activeSearchPanelTab = ref<'recent' | 'favorites'>('recent')
const isHeroImageLarge = ref(false)
const isHeroImagePopupOpen = ref(false)
const activeCharacter = computed<CharacterProfile | null>(() => selectedCharacterProfile.value ?? character.value)
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

const detailEquipment = ref<Equipment[]>([])
const detailEngravings = ref<Engraving[]>([])
const detailLoading = ref(false)
const detailError = ref<string | null>(null)
const arkGridResponse = ref<ArkGridResponse | null>(null)
const arkGridLoading = ref(false)
const arkGridError = ref<string | null>(null)
const arkGridLoadedFor = ref<string | null>(null)
const skillResponse = ref<SkillMenuResponse | null>(null)
const skillLoading = ref(false)
const skillError = ref<string | null>(null)
const skillLoadedFor = ref<string | null>(null)
const collectibles = ref<Collectible[]>([])
const collectiblesLoading = ref(false)
const collectiblesError = ref<string | null>(null)
const collectiblesLoadedFor = ref<string | null>(null)
const lastRefreshedAt = ref<Date | null>(null)
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

const normalizeSkillKey = (value?: string | null) =>
  inlineText(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

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

const GEM_DAMAGE_REGEX = /(멸화|겁화|피해|대미지|데미지)/i
const GEM_COOLDOWN_REGEX = /(홍염|작열|쿨타임|재사용|대기시간)/i

const extractGemLabel = (gem: SkillGem) => {
  const tooltipText = flattenTooltipLines(gem.tooltip).join(' ')
  const candidates = [inlineText(gem.name), inlineText(gem.skill?.description), tooltipText]
    .filter(Boolean)
    .join(' ')
  const labelMatch = candidates.match(/(멸화|겁화|홍염|작열)/i)
  if (labelMatch?.[1]) return labelMatch[1]
  if (GEM_DAMAGE_REGEX.test(candidates)) return '겁화'
  if (GEM_COOLDOWN_REGEX.test(candidates)) return '홍염'
  return ''
}

const classifyGemEffect = (label: string): GemEffectPlacement => {
  if (/멸화|겁화/i.test(label)) return 'damage'
  if (/홍염|작열/i.test(label)) return 'cooldown'
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
  map.forEach(entry => {
    if (!entry.damage && entry.extras.length) {
      entry.damage = entry.extras.shift() ?? null
    }
    if (!entry.cooldown && entry.extras.length) {
      entry.cooldown = entry.extras.shift() ?? null
    }
  })
  return map
})

const skillHighlights = computed(() => {
  const skills = skillResponse.value?.combatSkills ?? []
  if (!skills.length) return []
  const gemSlots = skillGemSlotsBySkill.value
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
      if (!hasSkillPoints && !hasRune && !hasGem) {
        return null
      }
      return {
        key: key || `skill-${index}`,
        name: inlineText(skill.name) || `스킬 ${index + 1}`,
        icon: skill.icon || '',
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
    if (!/^[0-9a-fA-F]{6}$/.test(raw)) return null
    return `#${raw.toLowerCase()}`
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

    const addEffect = (label: string, full?: string, color?: string) => {
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
      effects.push({ label: normalizedLabel, full, textColor, bgColor })
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
      itemLevel: accessory || bracelet || abilityStone ? gradeLabel : meta.itemLevel || fallbackItemLevel,
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
      left.push(base)
    }
  })

  return {
    gradeBadges,
    left: left.slice(0, 6),
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
    return {
      key: `${engrave.name || 'engrave'}-${index}`,
      name: inlineText(engrave.name),
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
  const name = inlineText(effect.name)
  const displayName = stripPassiveStageKeywords(name) || name || '패시브'
  const levelValueMatch = levelLine.match(/(\d+)/)
  const levelDisplay = levelValueMatch ? `Lv.${levelValueMatch[1]}` : ''
  const section =
    PASSIVE_SECTIONS.find(section => tierLabel.includes(section.keyword)) ||
    PASSIVE_SECTIONS.find(section => levelLine.includes(section.keyword)) ||
    PASSIVE_SECTIONS.find(section => name.includes(section.keyword)) ||
    PASSIVE_SECTIONS[0]

  return {
    key: `${displayName || 'passive'}-${index}`,
    name: displayName,
    icon: effect.icon || '',
    levelDisplay,
    summaryLine: summaryLine || '효과 정보 없음',
    sectionKey: section.key,
    tierLabel,
    levelLine
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
    const MAX_EFFECTS_PER_ROW = 4
    let remaining = MAX_EFFECTS_PER_ROW
    row.sections.forEach(section => {
      if (remaining <= 0) {
        section.effects = []
        return
      }
      if (section.effects.length > remaining) {
        section.effects = section.effects.slice(0, remaining)
      }
      remaining -= section.effects.length
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

const arkSummary = computed(() => {
  const passive = arkGridResponse.value?.arkPassive
  const grid = arkGridResponse.value?.arkGrid
  const passiveMatrix = buildArkPassiveMatrix(passive?.effects ?? [])

  const coreSlots = (grid?.slots ?? [])
    .map((slot, index) => {
      const rawName = inlineText(slot.name) || `코어 ${slot.index ?? index + 1}`
      const meta = parseCoreMeta(rawName)
      const name = meta.displayName
      const pointLabel =
        slot.point !== undefined && slot.point !== null ? `${slot.point}P` : ''
      return {
        key: `slot-${slot.index ?? index}`,
        name,
        alignment: meta.alignment,
        celestial: meta.celestial,
        icon: slot.icon || '',
        pointLabel,
        initial: name.charAt(0) || 'C'
      }
    })
    .filter(slot => slot.icon || slot.pointLabel || slot.name)
  const coreMatrix = buildCoreMatrix(coreSlots)

  const appliedPoints = (passive?.points ?? [])
    .map((point, index) => {
      const label = inlineText(point.name) || `포인트 ${index + 1}`
      let value = ''
      if (typeof point.value === 'number') {
        value = `${point.value}P`
      } else if (point.value !== undefined && point.value !== null) {
        value = inlineText(point.value)
      } else if (point.description) {
        value = inlineText(point.description)
      }
      return {
        key: `point-${label}-${index}`,
        label,
        value
      }
    })
    .filter(point => point.label && point.value)

  const corePassives = passiveMatrix.flatMap(row => row.sections.flatMap(section => section.effects))

  return {
    passiveTitle: inlineText(passive?.title),
    slotCount: grid?.slots?.length ?? 0,
    coreSlots,
    coreMatrix,
    appliedPoints,
    passiveMatrix,
    corePassives
  }
})

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

const loadFavoritesFromStorage = () => {
  const stored = loadFromStorage<CharacterProfile[]>(FAVORITES_STORAGE_KEY, [])
  if (stored.length) {
    favorites.value = stored
  }
}

const persistFavoritesToStorage = () => {
  saveToStorage(FAVORITES_STORAGE_KEY, favorites.value)
}

type FavoriteIdentity = Pick<CharacterProfile, 'characterName' | 'serverName'>

const isSameFavorite = (target: FavoriteIdentity, other: FavoriteIdentity) => {
  return target.characterName === other.characterName && target.serverName === other.serverName
}

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

const loadHistoryFromStorage = () => {
  const stored = loadFromStorage<SearchHistory[]>(HISTORY_STORAGE_KEY, [])
  if (stored.length) {
    history.value = stored
  }
}

const persistHistoryToStorage = () => {
  saveToStorage(HISTORY_STORAGE_KEY, history.value)
}

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
  loadFavoritesFromStorage()
  loadHistoryFromStorage()
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
      if (newCharacter !== activeCharacter.value?.characterName) {
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
    await Promise.all([ensureSkillData(), ensureCollectiblesData(), ensureArkGridData()])
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

const searchCharacter = async (name: string, options: { forceRefresh?: boolean; updateUrl?: boolean } = {}) => {
  loading.value = true
  error.value = null
  if (options.forceRefresh) {
    lostarkApi.invalidateCharacterCache(name)
  }
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
  skillResponse.value = null
  skillLoadedFor.value = null
  skillError.value = null
  skillLoading.value = false
  collectibles.value = []
  collectiblesLoadedFor.value = null
  collectiblesError.value = null
  collectiblesLoading.value = false
  lastRefreshedAt.value = null

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
        loadArkGridData(name, { forceRefresh: true })
      ])
      await loadHistory({ forceRefresh: true })
    } else {
      await loadHistory()
    }
    await Promise.allSettled([
      ensureSkillData({ forceRefresh: options.forceRefresh }),
      ensureCollectiblesData({ forceRefresh: options.forceRefresh }),
      ensureArkGridData({ forceRefresh: options.forceRefresh })
    ])
    activeResultTab.value = DEFAULT_RESULT_TAB

    // URL 업데이트 (기본값: true)
    if (options.updateUrl !== false) {
      router.push({ query: { character: name } })
    }
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
  characterName.value = ''
  character.value = null
  siblings.value = []
  error.value = null
  selectedCharacterProfile.value = null
  detailEquipment.value = []
  detailEngravings.value = []
  detailError.value = null
  characterAvailability.value = {}
  arkGridResponse.value = null
  arkGridLoadedFor.value = null
  arkGridError.value = null
  arkGridLoading.value = false
  skillResponse.value = null
  skillLoadedFor.value = null
  skillError.value = null
  skillLoading.value = false
  collectibles.value = []
  collectiblesLoadedFor.value = null
  collectiblesError.value = null
  collectiblesLoading.value = false
  lastRefreshedAt.value = null
  activeResultTab.value = DEFAULT_RESULT_TAB
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

const toggleFavorite = () => {
  const targetCharacter = activeCharacter.value
  if (!targetCharacter) return

  const snapshot = favorites.value.slice()
  const shouldFavorite = !isCharacterFavorite.value
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

const ensureSkillData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (skillLoading.value) return
  if (!options.forceRefresh && skillLoadedFor.value === targetName && skillResponse.value) return
  await loadSkillData(targetName, options)
}

const loadSkillData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
  skillLoading.value = true
  skillError.value = null
  try {
    const response = await lostarkApi.getSkills(name, { force: options.forceRefresh })
    skillResponse.value = response.data
    skillLoadedFor.value = name
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

const ensureCollectiblesData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (collectiblesLoading.value) return
  if (!options.forceRefresh && collectiblesLoadedFor.value === targetName && collectibles.value.length) return
  await loadCollectiblesData(targetName, options)
}

const loadCollectiblesData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
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

const ensureArkGridData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (arkGridLoading.value) return
  if (!options.forceRefresh && arkGridLoadedFor.value === targetName && arkGridResponse.value) return
  await loadArkGridData(targetName, options)
}

const loadArkGridData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
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
  if (characterAvailability.value[summary.characterName] === 'loading') return
  activeResultTab.value = 'detail'
  const isPrimary = character.value?.characterName === summary.characterName
  const profile = isPrimary ? character.value ?? undefined : undefined
  loadCharacterDetails(summary.characterName, { profile })

  // URL 업데이트
  router.push({ query: { character: summary.characterName } })
}

const formatItemLevel = (value?: string | number) => {
  return formatNumberLocalized(value, 2)
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

const formatNumberLocalized = (value?: number | string, fractionDigits?: number) => {
  if (value === undefined || value === null || value === '') return '—'
  const numeric =
    typeof value === 'number'
      ? value
      : Number(
          value
            .toString()
            .replace(/,/g, '')
            .trim()
        )
  if (Number.isNaN(numeric)) {
    return typeof value === 'string' && value.length ? value : '—'
  }
  const options: Intl.NumberFormatOptions = {}
  if (typeof fractionDigits === 'number') {
    options.minimumFractionDigits = fractionDigits
    options.maximumFractionDigits = fractionDigits
  }
  return numeric.toLocaleString(undefined, options)
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
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  box-shadow: none;
}

.summary-card--module {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: var(--space-sm);
  min-height: 200px;
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
  gap: var(--space-md);
  padding: 8px 0;
}

.summary-list-item--plain {
  padding: 4px;
}

.summary-list-text {
  display: flex;
  flex-direction: column;
  width:100%;
}

.summary-title {
  margin: 0;
  font-size: var(--font-sm);
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
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-xs);
  border: none;
  white-space: nowrap;
  justify-content: space-around;
}

.summary-pill--primary {
  background: rgba(102, 126, 234, 0.16);
  color: var(--primary-color);
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

.summary-skill-item {
  align-items: stretch;
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
  align-items: flex-start;
  justify-content: flex-start;
  flex-direction: column;
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
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
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
  position: absolute;
  right: -6px;
  bottom: -6px;
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

.summary-tripod-icon--1 .summary-tripod-slot-dot {
  background: #00a1e0;
}

.summary-tripod-icon--2 .summary-tripod-slot-dot {
  background: #7cca15;
}

.summary-tripod-icon--3 .summary-tripod-slot-dot {
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
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
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
  gap: var(--space-md) var(--space-lg);
}

.summary-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
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
  flex-direction: row;
  gap: var(--space-sm);
}

.ark-core-card-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: var(--space-sm);
}

.ark-core-card-cell {
  display: contents;
}

.ark-core-card {
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: var(--radius-lg);
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
  position: absolute;
  right: -6px;
  bottom: -6px;
  padding: 3px 6px;
  border-radius: var(--radius-full);
  background: var(--quality-badge-bg, rgba(15, 23, 42, 0.333));
  color: var(--primary-color);
  font-size: var(--font-xxs);
  font-weight: 700;
  text-shadow:
    -1px -1px 0 rgba(255, 255, 255, 0.9),
    1px -1px 0 rgba(255, 255, 255, 0.9),
    -1px 1px 0 rgba(255, 255, 255, 0.9),
    1px 1px 0 rgba(255, 255, 255, 0.9);
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
  grid-template-columns: 40px repeat(3, 1fr);
  gap: 10px;
  justify-items: center;
}

.ark-passive-header-cell {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.ark-passive-tier {
  font-weight: 700;
  color: var(--text-primary);
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  font-size: var(--font-xs);
  position: relative;
  padding-right: 8px;
  width:100%;
}

.ark-passive-header-tier {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding-right: 8px;
  width:100%;
}

.ark-passive-tier::after,
.ark-passive-header-tier::after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--border-color);
}

.ark-passive-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width:100%;
}

.ark-passive-cell-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(32px, 1fr));
  gap: 8px 10px;
  width:100%;
}

.ark-passive-chip {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 6px 0;
}

.ark-passive-chip-visual {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.ark-passive-level {
  font-size: var(--font-xs);
  color: var(--text-secondary);
}

.equipment-icon-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 40px;
  position: relative;
}

.equipment-item-level {
  font-size: var(--font-xs);
  color: var(--text-secondary);
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

.equipment-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
  font-size: var(--font-xxs, var(--font-xs));
  font-weight: 700;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  min-width: 42px;
  width: fit-content;
}

.equipment-badge--enhance {
  background: rgba(99, 102, 241, 0.12);
  border-color: rgba(99, 102, 241, 0.4);
}

.equipment-badge--quality {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.4);
}

.equipment-badge--harmony {
  background: rgba(14, 165, 233, 0.12);
  border-color: rgba(14, 165, 233, 0.4);
}

.equipment-badge--transcend {
  background: rgba(234, 179, 8, 0.15);
  border-color: rgba(234, 179, 8, 0.35);
}

.equipment-badge--special {
  background: rgba(236, 72, 153, 0.12);
  color: #db2777;
  border-color: rgba(236, 72, 153, 0.35);
}

.equipment-badge--effect {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.equipment-effect-badges {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.equipment-effect-chip {
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
  grid-template-columns: 120px auto;
  gap: 12px;
  align-items: flex-start;
}

.equipment-side {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  min-height:100px;
}

.equipment-side--bracelet {
  grid-column: 1 / span 2;
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

.summary-progress-list--dense .summary-progress {
  padding: 8px 0;
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
  padding: 10px;
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
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
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

.special-tooltip {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translate(-50%, -10px);
  width: min(var(--tooltip-width, 360px), 85vw);
  opacity: 0;
  pointer-events: none;
  box-sizing: border-box;
  z-index: 7;
  text-align: left;
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.special-icon-wrapper.is-hovered .special-tooltip {
  opacity: 1;
  pointer-events: auto;
  transform: translate(-50%, -2px);
}

.special-tooltip::before {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%) rotate(180deg);
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-bottom: 9px solid var(--popover-bg);
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.35));
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

.special-tooltip-empty {
  margin: 0;
  font-size: 0.9rem;
  color: var(--popover-muted);
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
  padding: 10px;
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
  padding: 10px;
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
