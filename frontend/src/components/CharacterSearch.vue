<template>
  <div class="character-search">
    <Teleport to="#character-search-submenu">
      <div class="character-search__submenu-search">
        <div class="header-search__row">
          <div class="search-panel-wrapper" ref="searchPanelWrapperRef">
            <CharacterSearchPanel
              v-model:character-name="characterName"
              :suggestions="[]"
              :show-panel="shouldShowSearchPanel"
              v-model:active-panel-tab="activeSearchPanelTab"
              :history-items="historyItems"
              :favorite-items="favorites"
              :filtered-history-items="panelHistoryItems"
              :filtered-favorite-items="panelFavoriteItems"
              @select="handleSuggestionSelect"
              @search="searchCharacterByInput"
              @focus="handleSearchFocus"
              @clear-history="store.clearHistory"
              @select-history="handleSearchPanelSelect"
              @select-favorite="handleSearchPanelSelect"
            />
          </div>
        </div>
      </div>
    </Teleport>
    <TopPageHeader>
      <div class="page-header">
        <div class="header-right" aria-hidden="true">
          <CharacterResultTabs
            v-model="activeResultTab"
            :tabs="resultTabs"
            :show-tabs="!!(character && !loading)"
          />
        </div>
      </div>
    </TopPageHeader>

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
                :combat-position="combatPositionLabel"
                :combat-position-loading="detailLoading"
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
                  :avatar-summary="avatarSummary"
                  :character-image="characterImageSrc"
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
                    @retry="retrySkillData"
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
                    @retry="retryCollectiblesData"
                  />
                </section>

                <RankingTab
                  v-else-if="activeResultTab === 'ranking'"
                  :character-name="activeCharacter?.characterName"
                />

                <ExpeditionCharacterList
                  v-else-if="activeResultTab === 'expedition'"
                  :groups="expeditionGroups"
                  :selected-character-name="selectedCharacterProfile?.characterName"
                  :total-count="(character ? 1 : 0) + siblings.length"
                  v-model:sort-key="expeditionSortKey"
                  :sort-options="expeditionSortOptions"
                  @select="viewCharacterDetail"
                />

                <section
                  v-else-if="activeResultTab === 'arkGrid'"
                  class="detail-panel ark-grid-panel"
                >
                  <ArkGridPanel
                    :response="arkGridResponse"
                    :loading="arkGridLoading"
                    :error-message="arkGridError"
                    :character-name="character?.characterName || ''"
                    @retry="retryArkGridData"
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
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick, toRefs } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type {
  CharacterProfile,
  SiblingCharacter,
  Equipment,
  CharacterStat,
  ArkPassiveEffect,
  ArmoryGemEffectSkill,
  ArmoryGemItem,
  CombatSkill,
  SkillGem
} from '@/api/types'
import LoadingSpinner from './common/LoadingSpinner.vue'
import ErrorMessage from './common/ErrorMessage.vue'
import EmptyState from './common/EmptyState.vue'
import LazyImage from './common/LazyImage.vue'
import AutocompleteInput from './common/AutocompleteInput.vue'
import CustomSelect, { type SelectOption } from './common/CustomSelect.vue'
import CharacterDetailModal from './common/CharacterDetailModal.vue'
import ArkGridPanel from './common/ArkGridPanel.vue'
import SkillPanel from './common/SkillPanel.vue'
import CollectionPanel from './common/CollectionPanel.vue'
import RankingTab from './ranking/RankingTab.vue'
import CharacterOverviewCard from './common/CharacterOverviewCard.vue'
import CharacterSummaryPanel from './common/CharacterSummaryPanel.vue'
import type { Suggestion } from './common/AutocompleteInput.vue'
import TopPageHeader from './common/TopPageHeader.vue'
import ExpeditionCharacterList from './character/ExpeditionCharacterList.vue'
import type { ExpeditionGroup } from './character/ExpeditionCharacterList.vue'
import CharacterResultTabs from './character/CharacterResultTabs.vue'
import type { TabItem } from './character/CharacterResultTabs.vue'
import CharacterSearchPanel from './character/CharacterSearchPanel.vue'
import type { HistoryItem } from './character/CharacterSearchPanel.vue'
import { cleanTooltipLine, flattenTooltipLines, sanitizeInline } from '@/utils/tooltipText'
import { applyEffectAbbreviations, hasAbbreviationMatch } from '@/data/effectAbbreviations'
import { getEngravingDisplayName } from '@/data/engravingNames'
import { formatItemLevel, formatNumberLocalized, formatCombatPower, formatInteger } from '@/utils/format'
import { useCharacterStore } from '@/stores/characterStore'
import { useExpeditionData } from '@/composables/character/useExpeditionData'
import type { ExpeditionSortKey } from '@/composables/character/useExpeditionData'
import { useCollectibleData } from '@/composables/character/useCollectibleData'
import { useArkGridData } from '@/composables/character/useArkGridData'
import { useEquipmentData } from '@/composables/character/useEquipmentData'
import { useSkillData } from '@/composables/character/useSkillData'
import { resolveCombatPosition } from '@/data/classPositions'
import { isNumber, isRecord, isString } from '@/utils/typeGuards'
import {
  normalizeSkillKey,
  extractGemLabel,
  classifyGemEffect,
  isAwakeningSkill,
  extractSkillNameFromGemTooltip,
  normalizeGemSkillKey,
  skillNameFromGem,
  pickHigherGem,
  formatLevelLabel,
  type GemEffectPlacement,
  type SkillGemSlot,
  type CombatSkillCatalogEntry
} from '@/utils/character/skillDataTransform'
import {
  isSpecialEquipment,
  isAccessory,
  isAvatarEquipment,
  isNecklace,
  isEarring,
  isRing,
  isBracelet,
  isAbilityStone,
  extractEnhancementLevel,
  getSpecialLabel,
  getSpecialHighlights,
  summarizeEquipmentLine,
  parseEquipmentMeta,
  gearOrderIndex,
  SPECIAL_EQUIPMENT_KEYWORDS,
  SPECIAL_EQUIPMENT_DISPLAY_ORDER,
  SPECIAL_EQUIPMENT_FALLBACK_ICON,
  GEAR_ORDER,
  type EquipmentMeta
} from '@/utils/character/equipmentDataTransform'
import {
  stripPassiveStageKeywords,
  parsePassiveTooltip,
  parsePassiveDescription,
  romanToNumber,
  normalizeTierText,
  extractTierGroupLabel,
  PASSIVE_SECTIONS,
  ROMAN_NUMERAL_MAP,
  type PassiveSectionKey,
  type PassiveTooltipParsed,
  type PassiveDescription
} from '@/utils/character/arkGridDataTransform'
import {
  parseAwakeningRequirement,
  normalizeCardGrade,
  formatCardAwakeningLabel,
  calculateActiveEffectIndex
} from '@/utils/character/cardDataTransform'

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
  // { key: 'detail', label: '장비' },
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

const expeditionSortOptions: SelectOption[] = [
  { value: 'itemLevel', label: '아이템 레벨' },
  { value: 'characterLevel', label: '캐릭터 레벨' },
  { value: 'name', label: '이름' },
  { value: 'class', label: '직업' }
]

const router = useRouter()
const route = useRoute()

// Pinia Store
const store = useCharacterStore()

// Extract reactive refs from store using storeToRefs
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
  detailAvatars,
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
  activeCharacter
} = storeToRefs(store)

const activeResultTab = ref<ResultTabKey>(DEFAULT_RESULT_TAB)
const expeditionSortKey = ref<ExpeditionSortKey>('itemLevel')

// Composables
const { expeditionGroups } = useExpeditionData(character, siblings, expeditionSortKey)
const { collectionSummary } = useCollectibleData(collectibles)
const { arkSummary } = useArkGridData(arkGridResponse)
const { equipmentSummary, avatarSummary, engravingSummary } = useEquipmentData(
  detailEquipment,
  detailAvatars,
  detailEngravings
)

const activePlaceholder = computed(() => tabPlaceholderCopy[activeResultTab.value])
const searchPanelWrapperRef = ref<HTMLElement | null>(null)
const searchPanelOpen = ref(false)
const activeSearchPanelTab = ref<'recent' | 'favorites'>('recent')
const isHeroImageLarge = ref(false)
const isHeroImagePopupOpen = ref(false)
const characterImageSrc = computed(() => activeCharacter.value?.characterImage || '')
const hasCharacterImage = computed(() => Boolean(characterImageSrc.value))
const isCharacterFavorite = computed(() => store.isCharacterFavorite)

// Convert LocalSearchHistory to HistoryItem for template
const historyItems = computed<HistoryItem[]>(() =>
  history.value.map((item) => ({
    id: item.timestamp,
    characterName: item.characterName,
    itemAvgLevel: item.itemAvgLevel,
    characterClassName: item.characterClassName,
    characterImage: item.characterImage,
    timestamp: item.timestamp
  } as HistoryItem))
)

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
  store.toggleFavorite()
}
// Equipment constants and functions imported from equipmentDataTransform:
// - SPECIAL_EQUIPMENT_KEYWORDS, SPECIAL_EQUIPMENT_DISPLAY_ORDER, SPECIAL_EQUIPMENT_FALLBACK_ICON
// - isSpecialEquipment

const specialEquipments = computed(() => {
  return detailEquipment.value.filter(item => isSpecialEquipment(item))
})

const specialEquipmentsDetailed = computed(() => {
  const detailed = specialEquipments.value.map(item => ({
    item,
    highlights: getSpecialHighlights(item),
    label: getSpecialLabel(item)
  }))

  const byLabel = new Map<string, (typeof detailed)[number]>()
  detailed.forEach(entry => {
    if (!byLabel.has(entry.label)) {
      byLabel.set(entry.label, entry)
    }
  })

  const slots = SPECIAL_EQUIPMENT_DISPLAY_ORDER.map((label: string) => {
    const hit = byLabel.get(label)
    if (hit) return hit
    return {
      item: {
        type: label,
        name: label,
        icon: SPECIAL_EQUIPMENT_FALLBACK_ICON,
        tooltip: ''
      } as Equipment,
      highlights: [],
      label
    }
  })

  const extras = detailed.filter(entry => !SPECIAL_EQUIPMENT_DISPLAY_ORDER.includes(entry.label))
  return [...slots, ...extras]
})

// ============================================================================
// Ark Passive Functions (아크 그리드 관련)
// ============================================================================

const inlineText = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value !== 'string' && typeof value !== 'number') return ''
  return sanitizeInline(value)
}

const readString = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'number') return String(value)
  return ''
}

const readStringFromRecord = (record: Record<string, unknown>, key: string): string => {
  const value = record[key]
  return typeof value === 'string' ? value : ''
}

const readNumberFromRecord = (record: Record<string, unknown>, key: string): number | undefined => {
  const value = record[key]
  return typeof value === 'number' ? value : undefined
}

// resolveArmoryGems와 resolveArmoryEffectSkills는 armoryEffectGemSlots에서 사용됨
const resolveArmoryGems = (): ArmoryGemItem[] => {
  const resp = armoryGemsResponse.value
  if (!resp) return []
  if (Array.isArray(resp.Gems)) return resp.Gems
  const record = resp as unknown as Record<string, unknown>
  const alt = record['gems']
  return Array.isArray(alt) ? (alt as ArmoryGemItem[]) : []
}

const resolveArmoryEffectSkills = (): ArmoryGemEffectSkill[] => {
  const resp = armoryGemsResponse.value
  if (!resp) return []
  const rawEffects: unknown =
    resp.Effects ?? ((resp as unknown as Record<string, unknown>)['effects'] as unknown)
  const effectsArray = Array.isArray(rawEffects) ? rawEffects : rawEffects ? [rawEffects] : []
  const skills: ArmoryGemEffectSkill[] = []
  effectsArray.forEach(effect => {
    if (!isRecord(effect)) return
    const rawSkills = effect['Skills'] ?? effect['skills']
    if (Array.isArray(rawSkills)) {
      skills.push(...(rawSkills as ArmoryGemEffectSkill[]))
    }
  })
  return skills
}

const parseSkillIconFromTooltip = (tooltip?: string | null): string => {
  if (!tooltip) return ''
  const pick = (icon?: string | null) => (typeof icon === 'string' && icon.trim().length ? icon.trim() : '')
  const search = (node: unknown): string => {
    if (!node) return ''
    if (typeof node === 'string') return ''
    if (Array.isArray(node)) {
      for (const item of node) {
        const found = search(item)
        if (found) return found
      }
      return ''
    }
    if (isRecord(node)) {
      if (pick(readStringFromRecord(node, 'iconPath'))) return pick(readStringFromRecord(node, 'iconPath'))
      if (pick(readStringFromRecord(node, 'Icon'))) return pick(readStringFromRecord(node, 'Icon'))
      const slotData = node['slotData']
      if (isRecord(slotData) && pick(readStringFromRecord(slotData, 'iconPath'))) {
        return pick(readStringFromRecord(slotData, 'iconPath'))
      }
      for (const value of Object.values(node)) {
        const found = search(value)
        if (found) return found
      }
    }
    return ''
  }

  try {
    const parsed = JSON.parse(tooltip)
    const fromParsed = search(parsed)
    if (fromParsed) return fromParsed
  } catch {
    // ignore parse errors
  }

  // fallback: regex search in raw string
  const match =
    String(tooltip).match(/iconPath["']?\s*[:=]\s*["']([^"']+)["']/i) ||
    String(tooltip).match(/"Icon"\s*:\s*"([^"]+)"/i)
  return pick(match?.[1]) || ''
}

// armoryEffectGemSlots는 useSkillData에서 사용됨
const armoryEffectGemSlots = computed<SkillGem[]>(() => {
  const armoryGems = resolveArmoryGems()
  return resolveArmoryEffectSkills().map((skill, idx) => {
    const skillRecord = skill as unknown as Record<string, unknown>
    const gemSlot = readNumberFromRecord(skillRecord, 'GemSlot')
    const skillName = inlineText(readStringFromRecord(skillRecord, 'Name') || readStringFromRecord(skillRecord, 'name'))
    const descriptionRaw = skillRecord['Description']
    const description = Array.isArray(descriptionRaw)
      ? descriptionRaw.map((entry: unknown) => String(entry)).join(' ')
      : inlineText(readString(descriptionRaw))
    const matchedGem = typeof gemSlot === 'number' ? armoryGems.find((g: ArmoryGemItem) => g?.Slot === gemSlot) : undefined
    const matchedGemRecord = (matchedGem ?? {}) as unknown as Record<string, unknown>
    const gemName = inlineText(matchedGem?.Name || readStringFromRecord(matchedGemRecord, 'name'))
    const gemTooltip =
      matchedGem?.Tooltip ||
      readStringFromRecord(matchedGemRecord, 'tooltip') ||
      readStringFromRecord(skillRecord, 'Tooltip') ||
      readStringFromRecord(skillRecord, 'tooltip')
    const gemIcon =
      matchedGem?.Icon ||
      readStringFromRecord(matchedGemRecord, 'icon') ||
      parseSkillIconFromTooltip(gemTooltip) ||
      readStringFromRecord(skillRecord, 'Icon') ||
      parseSkillIconFromTooltip(readStringFromRecord(skillRecord, 'Tooltip') || readStringFromRecord(skillRecord, 'tooltip'))
    return {
      slot: gemSlot,
      name: gemName || skillName || `gem-effect-${idx}`,
      icon: gemIcon || '',
      tooltip: gemTooltip,
      level: matchedGem?.Level ?? readNumberFromRecord(matchedGemRecord, 'level'),
      grade: inlineText(matchedGem?.Grade || readStringFromRecord(matchedGemRecord, 'grade')),
      skill: {
        name: skillName,
        description
      }
    }
  })
})

// ============================================================================
// Skill Data Composable
// ============================================================================

const {
  combatSkillCatalog,
  skillGemSlotsBySkill,
  armoryGemIconMaps,
  skillHighlights,
  combatSkillKeySet,
  skillLooseGems
} = useSkillData(skillResponse, armoryGemsResponse, armoryEffectGemSlots)

// ============================================================================
// 기타 Computed Properties
// ============================================================================

const classEngravingNames = computed(() =>
  detailEngravings.value.map(engraving => inlineText(engraving.name)).filter(Boolean)
)

const combatPositionLabel = computed(() => {
  const className = activeCharacter.value?.characterClassName
  const position = resolveCombatPosition(className, classEngravingNames.value)
  if (position === 'head') return '헤드'
  if (position === 'back') return '백'
  return '타대'
})

// Equipment functions imported from equipmentDataTransform:
// - summarizeEquipmentLine, parseEquipmentMeta, extractEnhancementLevel
// - isAccessory, isAvatarEquipment, isNecklace, isEarring, isRing, isBracelet, isAbilityStone

// equipmentSummary는 이제 useEquipmentData composable에서 제공됩니다

// avatarSummary는 이제 useEquipmentData composable에서 제공됩니다

// collectionSummary는 이제 useCollectibleData composable에서 제공됩니다

// engravingSummary는 이제 useEquipmentData composable에서 제공됩니다

// Ark grid basic functions imported from arkGridDataTransform:
// - PASSIVE_SECTIONS, ROMAN_NUMERAL_MAP
// - stripPassiveStageKeywords, parsePassiveTooltip, parsePassiveDescription
// - normalizeTierText, romanToNumber, extractTierGroupLabel
// - Type: PassiveSectionKey

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
    if (targetSection) {
      targetSection.effects.push(card)
    }
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

type CoreAlignment = 'order' | 'chaos' | 'unknown'
type CoreCelestial = 'sun' | 'moon' | 'star' | 'unknown'

const parseCoreMeta = (rawName: string) => {
  const alignmentMatch = rawName.match(/(질서|혼돈)/)
  const alignment: CoreAlignment =
    alignmentMatch?.[1] === '질서' ? 'order' : alignmentMatch?.[1] === '혼돈' ? 'chaos' : 'unknown'

  const celestialMatch = rawName.match(/(해|달|별)/)
  let celestial: CoreCelestial = 'unknown'
  if (celestialMatch?.[1] === '해') celestial = 'sun'
  if (celestialMatch?.[1] === '달') celestial = 'moon'
  if (celestialMatch?.[1] === '별') celestial = 'star'

  const cleaned = rawName.replace(/^(질서|혼돈)?\s*의?\s*(해|달|별)?\s*코어\s*[:：]?\s*/i, '').trim()
  const displayName = cleaned || rawName

  return {
    alignment,
    celestial,
    displayName
  }
}

type CoreSlot = {
  key: string
  name: string
  icon: string
  initial: string
  pointLabel: string
  grade: string
  gradeColor: string
  nameColor: string
  tooltip: string
  alignment: CoreAlignment
  celestial: CoreCelestial
}
type CoreMatrixHeader = { key: string; label: string }
type CoreMatrixCell = { key: string; label: string; slots: CoreSlot[] }
type CoreMatrixRow = { key: string; label: string; cells: CoreMatrixCell[] }

const buildCoreMatrix = (coreSlots: CoreSlot[]): { headers: CoreMatrixHeader[]; rows: CoreMatrixRow[] } => {
  const hasSlots = coreSlots.length > 0
  if (!hasSlots) return { headers: [], rows: [] }

  const baseHeaders: CoreMatrixHeader[] = [
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

// arkSummary는 이제 useArkGridData composable에서 제공됩니다

const cardSummary = computed(() => {
  type CardSlotEntry = {
    slot?: number | string | null
    name?: string | null
    icon?: string | null
    grade?: string | null
    awakeCount?: number | string | null
    awakeTotal?: number | string | null
  }

  type CardEffectItem = {
    name?: string | null
    description?: string | null
  }

  type CardEffectEntry = {
    index?: number | null
    cardSlots?: number[] | null
    items?: CardEffectItem[] | null
  }

  const cardData = cardResponse.value?.card as
    | {
        cards?: CardSlotEntry[] | null
        effects?: CardEffectEntry[] | null
      }
    | null

  const cards =
    cardData?.cards
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
	  const totalAwakeCount = cards.reduce((sum, card) => sum + (card.awakeCount ?? 0), 0)

	  const effects =
	    cardData?.effects?.map((effect, index) => {
	      const slots = (effect.cardSlots ?? []).filter(slot => Number.isFinite(slot))
	      const items = effect.items ?? []

	      const maxSlot = slots.reduce((best, slot) => Math.max(best, slot), 0)
	      const activeSlot = slots.reduce((best, slot) => {
	        if (equippedCount < slot) return best
	        return Math.max(best, slot)
	      }, 0)

	      const baseIndex = (() => {
	        if (!slots.length) return 0
	        if (!activeSlot) return 0
	        const idx = slots.lastIndexOf(activeSlot)
	        return idx >= 0 ? idx : 0
	      })()

	      const parseAwakeningRequirement = (rawName?: string | null) => {
	        const name = inlineText(rawName)
	        if (!name) return null
	        const match = name.match(/(\d+)\s*(?:각성|각)/)
	        if (!match?.[1]) return null
	        const value = Number(match[1])
	        return Number.isFinite(value) ? value : null
	      }

	      const activeIndex = (() => {
	        if (!items.length) return 0
	        const clampedBaseIndex = Math.min(Math.max(0, baseIndex), items.length - 1)

	        if (!maxSlot || equippedCount < maxSlot || activeSlot < maxSlot) return clampedBaseIndex
	        if (!totalAwakeCount) return clampedBaseIndex

	        const awakeningCandidates = items
	          .map((item, idx) => ({
	            idx,
	            requirement: parseAwakeningRequirement(item?.name)
	          }))
	          .filter((entry): entry is { idx: number; requirement: number } => entry.requirement !== null)
	          .filter(entry => entry.idx >= clampedBaseIndex)

	        const matchedCandidate = awakeningCandidates
	          .filter(entry => totalAwakeCount >= entry.requirement)
	          .reduce<{ idx: number; requirement: number } | null>((best, entry) => {
	            if (!best) return entry
	            return entry.requirement >= best.requirement ? entry : best
	          }, null)

	        if (matchedCandidate) return matchedCandidate.idx

	        const extraCount = items.length - (clampedBaseIndex + 1)
	        if (extraCount <= 0) return clampedBaseIndex
	        const thresholds = Array.from({ length: extraCount }, (_, idx) => 12 + idx * 6)
	        const metCount = thresholds.filter(threshold => totalAwakeCount >= threshold).length
	        return Math.min(items.length - 1, clampedBaseIndex + metCount)
	      })()

	      const activeItem = items[activeIndex]

	      const label =
	        inlineText(activeItem?.name) ||
	        items.map(item => inlineText(item.name)).find(Boolean) ||
	        `세트 효과 ${effect.index ?? index + 1}`

	      const descriptions = [inlineText(activeItem?.description)].filter(Boolean)

	      const setLabel =
	        activeSlot > 0
	          ? `${activeSlot}세트`
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
  const items = activeSearchPanelTab.value !== 'recent'
    ? historyItems.value
    : !panelFilterQuery.value
      ? historyItems.value
      : historyItems.value.filter(item => item.characterName.toLowerCase().includes(panelFilterQuery.value))

  // 최대 5개만 표시
  return items.slice(0, 5)
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

// expeditionGroups는 이제 useExpeditionData composable에서 제공됩니다

onMounted(() => {
  store.loadFavorites()
  store.loadHistory()
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
  const name = activeCharacter.value?.characterName
  if (!name) return

  // 탭별 데이터 로드 (store.searchCharacter에서 이미 로드 중이면 중복 호출 방지됨)
  if (newTab === 'arkGrid') {
    await store.ensureArkGridData(name)
  } else if (newTab === 'skills') {
    await store.ensureSkillData(name)
  } else if (newTab === 'collection') {
    await store.ensureCollectiblesData(name)
  }
  // summary 탭은 store.searchCharacter에서 이미 모든 데이터를 병렬 로드하므로 추가 호출 불필요
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
  await store.searchCharacter(name, options.forceRefresh ?? false)
  if (error.value) return
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
  store.clearError()
}

// Retry handlers for template
const retrySkillData = async () => {
  const name = activeCharacter.value?.characterName
  if (name) await store.ensureSkillData(name)
}

const retryCollectiblesData = async () => {
  const name = activeCharacter.value?.characterName
  if (name) await store.ensureCollectiblesData(name)
}

const retryArkGridData = async () => {
  const name = activeCharacter.value?.characterName
  if (name) await store.ensureArkGridData(name)
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

// Equipment functions imported from equipmentDataTransform:
// - getSpecialHighlights, getSpecialLabel

const normalizeStatValue = (value?: string | string[] | number | null) => {
  if (value === undefined || value === null) return ''
  if (Array.isArray(value)) {
    return value.join(' / ')
  }
  if (typeof value === 'number') {
    return String(value)
  }
  return value
    .replace(/<br\s*\/?>/gi, ' / ')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
}

const formatProfileStat = (value?: string | string[] | number | null) => {
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
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  align-items: center;
  gap: 20px;
  justify-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.character-search__submenu-search {
  width: 100%;
  justify-self: center;
}

.character-search__submenu-search .search-panel-wrapper {
  flex: 1 1 220px;
  min-width: 200px;
}

.character-search__submenu-search :deep(.search-input) {
  padding: 7px 12px;
  font-size: calc(var(--font-base) - 2px);
  border: 1px solid var(--border-color);
  border-radius: 999px;
  background: var(--card-bg);
}

.character-search__submenu-search .search-panel-dropdown {
  width: min(300px, 100%);
  max-width: 300px;
}

.page-header h1 {
  font-size: calc(1.5rem - 2px);
  color: var(--text-primary);
  margin: 0;
  font-weight: 700;
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

.character-search__submenu-search :deep(.autocomplete-container) {
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
  justify-content: center;
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
  justify-content: space-around;
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
  /* padding: 10px; */
  /* border: 1px solid var(--border-color); */
  /* box-shadow: var(--shadow-md); */
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

.section-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.expedition-sort {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
}

.expedition-sort label {
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
  white-space: nowrap;
}

.expedition-sort__select,
:deep(.expedition-sort__select) {
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-weight: 600;
  outline: none;
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

  .summary-grid--stacked {
    grid-template-columns: repeat(2, minmax(300px, 1fr))
  }
}

@media (max-width: 640px) {
  .page-header {
    gap: 12px;
  }

  .header-left {
    display:block;
    text-align: center;
  }

  .header-right{
    width:unset;
  }

  .header-search__row {
    gap: 10px;
  }

  .hero-row--levels .hero-levels {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .profile-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .summary-grid--stacked {
    grid-template-columns: repeat(1, minmax(300px, 1fr))
  }

  .search-panel-dropdown {
    padding: 16px;
  }

  .equipment-side{
    grid-template-columns: 90px 1fr;
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
