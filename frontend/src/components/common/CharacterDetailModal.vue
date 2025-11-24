<template>
  <div class="character-detail-panel">
    <div v-if="loading" class="detail-placeholder">
      <LoadingSpinner message="상세 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="detail-placeholder">
      {{ errorMessage }}
    </div>
    <div v-else-if="!character || !equipmentList.length" class="detail-placeholder">
      캐릭터를 선택하면 상세 정보가 표시됩니다.
    </div>
    <div v-else class="detail-shell">
      <div v-if="globalSummaryItems?.length" class="global-summary">
        <div v-for="item in globalSummaryItems" :key="item.label" class="global-summary__item">
          <span
            v-if="item.iconSrc"
            class="summary-icon-img"
            aria-hidden="true"
          >
            <img :src="item.iconSrc" alt="" />
          </span>
          <span
            v-else-if="item.icon === 'transcendence'"
            class="summary-icon"
            aria-hidden="true"
          >🌀</span>
          <span
            v-else-if="item.icon === 'elixir'"
            class="summary-icon"
            aria-hidden="true"
          >🧪</span>
          <span class="label">{{ item.label }}</span>
          <span class="value">{{ item.value }}</span>
        </div>
      </div>
      <div class="equipment-detail-layout">
        <section
          v-for="panel in equipmentPanels"
          :key="panel.key"
          class="equipment-list-panel"
        >
          <div class="equipment-list-header">
            <div>
              <h4>{{ panel.label }}</h4>
              <p v-if="panel.description">{{ panel.description }}</p>
            </div>
          </div>
          <p v-if="!panel.items.length" class="equipment-empty">
            표시할 {{ panel.label }} 장비가 없습니다.
          </p>
          <div v-else class="equipment-stack">
            <article
              v-for="item in panel.items"
              :key="item.__uid"
              class="equipment-summary-card"
              :class="{ active: item.__uid === selectedEquipment?.__uid }"
              @click="selectEquipment(item)"
            >
              <div class="summary-icon-box">
                <div class="summary-icon-image">
                  <LazyImage
                    v-if="item.icon"
                    :src="item.icon"
                    :alt="item.name"
                    width="35"
                    height="35"
                    imageClass="summary-icon"
                    errorIcon="🛡️"
                    :useProxy="true"
                  />
                  <div v-else class="summary-icon summary-icon--fallback">
                    {{ item.name ? item.name[0] : '?' }}
                  </div>
                </div>
              </div>
              <div class="summary-body">
                <div class="summary-headline">
                  <h5 :style="{ color: getParsedEquipment(item)?.gradeColor || undefined }">
                    {{ item.name }}
                  </h5>
                  <span v-if="getParsedEquipment(item)?.itemLevel" class="summary-level">
                    iLv. {{ getParsedEquipment(item)?.itemLevel }}
                  </span>
                </div>
                <div class="summary-meta">
                  <span
                    v-if="hasValidQuality(getParsedEquipment(item)?.quality)"
                    class="pill pill--quality"
                    :style="{ borderColor: getQualityBadgeColor(item), color: getQualityBadgeColor(item) }"
                  >
                    품질 {{ getQualityDisplayValue(getParsedEquipment(item)?.quality) }}
                  </span>
                  <template v-if="getSangjaeStageForItem(item)">
                    <span class="pill pill--stage">
                      상재 {{ getSangjaeStageForItem(item) }}
                    </span>
                  </template>
                  <template v-if="getSlotInfoForItem(item)?.value">
                    <span class="pill pill--transcend" title="초월 수치">
                      초월 {{ getSlotInfoForItem(item)?.value }}
                    </span>
                  </template>
                </div>
              </div>
            </article>
          </div>
        </section>

        <section class="selected-equipment-panel" v-if="selectedEquipment">
          <article class="equipment-card">
            <header class="equipment-card__header">
              <div class="equipment-card__icon-box">
                <LazyImage
                  v-if="selectedEquipment.icon"
                  :src="selectedEquipment.icon"
                  :alt="selectedEquipment.name"
                  width="60"
                  height="60"
                  imageClass="equipment-card__icon"
                  errorIcon="⚔️"
                  :useProxy="true"
                />
                <div v-else class="equipment-card__icon equipment-card__icon--fallback">
                  {{ selectedEquipment.name ? selectedEquipment.name[0] : '?' }}
                </div>
              </div>
              <div class="equipment-card__summary">
                <div class="equipment-card__title-row">
                  <h3 :style="{ color: selectedTitleColor }">
                    {{ selectedEquipment.name }}
                    <span v-if="selectedParsed?.itemLevel" class="item-level-inline">
                      Lv. {{ selectedParsed.itemLevel }}
                    </span>
                  </h3>
                  <div class="chip-row">
                    <span class="chip chip--type">{{ selectedEquipment.type }}</span>
                    <span class="chip chip--grade" :style="{ color: selectedGradeColor }">
                      {{ selectedEquipment.grade || '등급 미상' }}
                    </span>
                    <span
                      v-if="qualityChipText"
                      class="chip chip--quality"
                      :style="{ borderColor: selectedQualityColor, color: selectedQualityColor }"
                    >
                      {{ qualityChipText }}
                    </span>
                    <span
                      v-if="sangjaeChipText"
                      class="chip chip--sangjae"
                      :style="{
                        color: sangjaeColor,
                        borderColor: sangjaeColor,
                        backgroundColor: sangjaeColor ? sangjaeColor + '22' : undefined
                      }"
                    >
                      {{ sangjaeChipText }}
                    </span>
                  </div>
                </div>
              </div>
            </header>

            <section class="core-stats-section">
              <div class="section-head">
                <span>핵심 능력치</span>
                <small>기본 효과 기준</small>
              </div>
                <div v-if="coreRows.length" class="core-stats-table">
                  <div
                    v-for="(row, idx) in coreRows"
                    :key="`core-row-${idx}`"
                    class="core-row"
                    :class="{ 'core-row--full': row.leftFullWidth }"
                  >
                    <div
                      class="core-cell"
                      :class="{ 'core-cell--full': row.leftFullWidth }"
                      @mouseenter="showHighlightTooltip(row.left)"
                      @mouseleave="clearHighlightTooltip"
                    >
                      <span class="cell-label">{{ row.left.label }}</span>
                      <span class="cell-value" :style="{ color: row.left.color || undefined }">{{ row.left.value }}</span>
                    </div>
                    <div
                      v-if="!row.leftFullWidth && row.right"
                      class="core-cell"
                      @mouseenter="showHighlightTooltip(row.right)"
                      @mouseleave="clearHighlightTooltip"
                    >
                      <span class="cell-label">{{ row.right.label }}</span>
                      <span class="cell-value" :style="{ color: row.right.color || undefined }">{{ row.right.value }}</span>
                    </div>
                  </div>
                  <div v-if="hoveredTooltipLines?.length" class="trans-tooltip">
                    <p v-for="(line, idx) in hoveredTooltipLines" :key="`tooltip-${idx}`">{{ line }}</p>
                  </div>
                </div>
              <p v-else class="core-empty">표시할 핵심 수치가 없습니다.</p>
            </section>
            <p v-if="tradeInfoLines.length" class="trade-status-line">
              <span v-for="(line, idx) in tradeInfoLines" :key="`trade-${idx}`">
                {{ line }}
              </span>
            </p>
          </article>
        </section>
      </div>
      <section class="engraving-panel">
        <div class="engraving-panel__head">
          <div>
            <h4>각인 구성</h4>
            <!-- <p>장비 상세 정보에서 감지된 각인 효과를 한눈에 정리했습니다.</p> -->
          </div>
          <!-- <div class="engraving-metrics">
            <div class="engraving-metric">
              <span>각인 등급</span>
              <strong>{{ engravingSummary.grade }}</strong>
            </div>
            <div class="engraving-metric">
              <span>활성 각인</span>
              <strong>{{ formatPlainNumber(engravingSummary.activeCount) }}개</strong>
            </div>
            <div class="engraving-metric">
              <span>총 레벨</span>
              <strong>{{ formatPlainNumber(engravingSummary.totalLevel) }}포인트</strong>
            </div>
            <div v-if="engravingSummary.debuffCount" class="engraving-metric engraving-metric--alert">
              <span>감소 효과</span>
              <strong>{{ formatPlainNumber(engravingSummary.debuffCount) }}개</strong>
            </div>
          </div> -->
        </div>
        <div v-if="hasEngravings" class="engraving-grid">
          <EngravingCard
            v-for="engraving in engravingCards"
            :key="engraving.__uid"
            :engraving="engraving"
          />
        </div>
        <p v-else class="engraving-empty">
          각인 정보가 감지되지 않았습니다.
        </p>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import LazyImage from './LazyImage.vue'
import LoadingSpinner from './LoadingSpinner.vue'
import EngravingCard from './EngravingCard.vue'
import {
  parseTooltip,
  stripHtml,
  type ParsedTooltip,
  type StatItem,
  type TranscendenceAggregates,
  getQualityColor,
  getGradeColor
} from '@/utils/tooltipParser'
import { parseEngravingDescription, calculateEngravingGrade, type ParsedEngraving } from '@/utils/engravingParser'
import type { Equipment, Engraving } from '@/api/types'

interface Character {
  characterName: string
  characterClassName: string
  characterImage?: string
  itemMaxLevel: string
}

interface Props {
  character: Character | null
  equipment?: Equipment[]
  engravings?: Engraving[]
  loading?: boolean
  errorMessage?: string | null
}

type EquipmentWithId = Equipment & { __uid: string }

interface EquipmentPanel {
  key: 'armor' | 'accessory'
  label: string
  description?: string
  items: EquipmentWithId[]
}

type EngravingWithId = Engraving & { __uid: string }

const props = withDefaults(defineProps<Props>(), {
  character: null,
  equipment: () => [],
  engravings: () => [],
  loading: false,
  errorMessage: null
})

const specialEquipmentKeywords = ['나침반', '부적', '보주', '문장']
const accessoryTypeKeywords = ['목걸이', '귀걸이', '반지', '팔찌', '돌', '어빌리티', '능력', '보석']
const mainStatKeywords = ['힘', '민첩', '지능']
const defenseStatKeywords = ['물리 방어력', '물리방어력', '마법 방어력', '마법방어력', '피해 감소', '피해감소']
type EquipmentCategory = 'weapon' | 'armor' | 'accessory' | 'bracelet' | 'abilityStone' | 'unknown'

const weaponTypeKeywords = [
  '무기',
  '건틀릿',
  '로드',
  '대검',
  '권총',
  '장궁',
  '랜스',
  '건블레이드',
  '창',
  '활',
  '대거',
  '타도',
  '스태프'
]
const armorTypeKeywords = [
  '투구',
  '머리',
  '머리장식',
  '머리 방어구',
  '어깨',
  '상의',
  '하의',
  '장갑',
  '헬멧',
  '모자',
  '바지',
  '장화',
  '신발',
  '갑옷',
  '허리',
  '벨트',
  '가슴'
]
const accessoryCoreKeywords = ['목걸이', '귀걸이', '반지']
const braceletTypeKeywords = ['팔찌', 'bracelet']
const abilityStoneTypeKeywords = ['어빌리티', 'ability stone', 'abilitystone', '어빌리티스톤', '능력돌', '능력 돌', '스톤']

const matchesKeyword = (text: string, keywords: string[]) =>
  keywords.some(keyword => text.includes(keyword.toLowerCase()))

const detectEquipmentCategory = (item?: Equipment | null): EquipmentCategory => {
  if (!item) return 'unknown'
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  if (matchesKeyword(target, braceletTypeKeywords)) return 'bracelet'
  if (matchesKeyword(target, abilityStoneTypeKeywords)) return 'abilityStone'
  if (matchesKeyword(target, accessoryCoreKeywords)) return 'accessory'
  if (matchesKeyword(target, weaponTypeKeywords)) return 'weapon'
  if (matchesKeyword(target, armorTypeKeywords)) return 'armor'
  return 'unknown'
}

const isSpecialEquipment = (item: Equipment) => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return specialEquipmentKeywords.some(keyword => target.includes(keyword.toLowerCase()))
}

const isAccessoryEquipment = (item: Equipment) => {
  const typeText = (item.type ?? '').toLowerCase()
  return accessoryTypeKeywords.some(keyword => typeText.includes(keyword.toLowerCase()))
}

const equipmentWithIds = computed<EquipmentWithId[]>(() =>
  props.equipment.map((item, index) => ({
    ...item,
    __uid: `${item.type ?? 'slot'}-${item.name}-${index}`
  }))
)

const equipmentList = computed(() =>
  equipmentWithIds.value.filter(item => !isSpecialEquipment(item))
)

// 장비 타입별 우선순위 정의 (작을수록 앞에 표시)
const getEquipmentOrderPriority = (item: EquipmentWithId): number => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()

  // 머리장식
  if (matchesKeyword(target, ['투구', '머리', '머리장식', '머리 방어구', '헬멧', '모자'])) {
    return 1
  }
  // 견갑 (어깨)
  if (matchesKeyword(target, ['어깨', '견갑'])) {
    return 2
  }
  // 상의
  if (matchesKeyword(target, ['상의', '갑옷', '가슴'])) {
    return 3
  }
  // 하의
  if (matchesKeyword(target, ['하의', '바지'])) {
    return 4
  }
  // 장갑
  if (matchesKeyword(target, ['장갑'])) {
    return 5
  }
  // 무기
  if (matchesKeyword(target, weaponTypeKeywords)) {
    return 6
  }
  // 기타 (허리, 벨트, 장화, 신발 등)
  return 7
}

const armorItems = computed(() => {
  const items = equipmentList.value.filter(item => !isAccessoryEquipment(item))
  return items.sort((a, b) => getEquipmentOrderPriority(a) - getEquipmentOrderPriority(b))
})
const accessoryItems = computed(() => equipmentList.value.filter(item => isAccessoryEquipment(item)))

const equipmentPanels = computed<EquipmentPanel[]>(() => [
  {
    key: 'armor',
    label: '무기 및 방어구',
    items: armorItems.value
  },
  {
    key: 'accessory',
    label: '장신구',
    items: accessoryItems.value
  }
])

const engravingCards = computed<EngravingWithId[]>(() =>
  props.engravings.map((engraving, index) => ({
    ...engraving,
    __uid: `${engraving.name ?? 'engraving'}-${index}`
  }))
)

const parsedEngravingDetails = computed<ParsedEngraving[]>(() =>
  props.engravings.map(engraving => {
    const parsed = parseEngravingDescription(engraving.description ?? '')
    return {
      ...parsed,
      name: parsed.name || engraving.name,
      level:
        typeof engraving.level === 'number' && engraving.level > 0 ? engraving.level : parsed.level,
      rawDescription: engraving.description ?? parsed.rawDescription
    }
  })
)

const engravingSummary = computed(() => {
  const parsed = parsedEngravingDetails.value
  if (!parsed.length) {
    return {
      grade: '미설정',
      activeCount: 0,
      debuffCount: 0,
      totalLevel: 0
    }
  }
  const active = parsed.filter(entry => entry.level > 0 && !entry.isDebuff)
  const debuffCount = parsed.filter(entry => entry.isDebuff && entry.level > 0).length
  const totalLevel = active.reduce((sum, entry) => sum + entry.level, 0)
  return {
    grade: calculateEngravingGrade(parsed),
    activeCount: active.length,
    debuffCount,
    totalLevel
  }
})

const hasEngravings = computed(() => engravingCards.value.length > 0)

const selectedEquipmentKey = ref<string | null>(null)

watch(
  () => equipmentWithIds.value,
  () => {
    const filtered = equipmentList.value
    if (!filtered.length) {
      selectedEquipmentKey.value = null
      return
    }
    if (
      !selectedEquipmentKey.value ||
      !filtered.some(item => item.__uid === selectedEquipmentKey.value)
    ) {
      selectedEquipmentKey.value = filtered[0]?.__uid ?? null
    }
  },
  { immediate: true }
)

const selectEquipment = (item: EquipmentWithId) => {
  selectedEquipmentKey.value = item.__uid
}

const parsedEquipment = computed<Record<string, ParsedTooltip>>(() => {
  const map: Record<string, ParsedTooltip> = {}
  equipmentWithIds.value.forEach(item => {
    if (item.tooltip) {
      map[item.__uid] = parseTooltip(item.tooltip)
    }
  })
  return map
})

const getParsedEquipment = (item: EquipmentWithId) => parsedEquipment.value[item.__uid]

const selectedEquipment = computed(() => {
  const list = equipmentList.value
  if (!list.length) return null
  return list.find(item => item.__uid === selectedEquipmentKey.value) ?? list[0]
})

const equipmentCategory = computed<EquipmentCategory>(() => detectEquipmentCategory(selectedEquipment.value))

const selectedParsed = computed<ParsedTooltip | undefined>(() => {
  if (!selectedEquipment.value) return undefined
  return getParsedEquipment(selectedEquipment.value)
})

const selectedGradeColor = computed(() => {
  if (selectedParsed.value?.gradeColor) return selectedParsed.value.gradeColor
  return selectedEquipment.value ? getGradeColor(selectedEquipment.value.grade || '') : '#A0A3AD'
})

const selectedTitleColor = computed(() => selectedParsed.value?.titleColor || selectedGradeColor.value)

const allStats = computed<StatItem[]>(() => [
  ...(selectedParsed.value?.basicStats ?? []),
  ...(selectedParsed.value?.additionalStats ?? [])
])

const fallbackBasicStats = computed<StatItem[]>(() => {
  const sources = [
    selectedParsed.value?.rawElements?.Element_006,
    selectedParsed.value?.rawElements?.Element_004
  ]
  const stats: StatItem[] = []
  sources.forEach(source => {
    extractItemPartLines(source, /기본\s*효과/i).forEach(({ text, rich }) => {
      const plusIndex = text.indexOf('+')
      if (plusIndex === -1) return
      const type = text.slice(0, plusIndex).trim()
      const value = text.slice(plusIndex + 1).trim()
      if (!type || !value) return
      stats.push({ type, value, richValue: rich })
    })
  })
  return stats
})

interface ExtractedLine {
  text: string
  rich: string
  color?: string
}

const extractColorFromHtml = (html?: string): string | undefined => {
  if (!html) return undefined
  const match = html.match(/color\s*[:=]\s*['"]?#?([0-9a-f]{6})/i)
  const color = match?.[1]
  if (!color) return undefined
  return `#${color.toUpperCase()}`
}

const extractItemPartLines = (
  rawElement: unknown,
  labelPattern: RegExp = /.*/,
  options: { groupByImage?: boolean } = {}
): ExtractedLine[] => {
  if (!rawElement || typeof rawElement !== 'object') return []
  const elementValue =
    (rawElement as any).value && typeof (rawElement as any).value === 'object'
      ? (rawElement as any).value
      : rawElement
  const label = elementValue?.Element_000
  const content = elementValue?.Element_001
  if (typeof label !== 'string' || !labelPattern.test(stripSummaryHtml(label))) return []
  if (!content) return []
  const raw =
    typeof content === 'string'
      ? content
      : typeof content === 'object'
        ? JSON.stringify(content)
        : ''
  if (!raw) return []

  const normalize = (value: string) =>
    value.replace(/<br\s*\/?>/gi, '<br>').replace(/\r?\n/g, '<br>').replace(/\\n/gi, '<br>')

  const buildLine = (segment: string, alreadyNormalized = false): ExtractedLine | null => {
    const normalizedSegment = alreadyNormalized ? segment : normalize(segment)
    const rich = stripHtml(normalizedSegment, { preserveColor: true }).replace(/\s+/g, ' ').trim()
    const text = formatNumbersInText(stripSummaryHtml(normalizedSegment)).replace(/\s+/g, ' ').trim()
    const color = extractColorFromHtml(segment)
    if (!text && !rich) return null
    return { rich, text, color }
  }

  if (options.groupByImage && /<img/i.test(raw)) {
    return raw
      .split(/<img[^>]*>/i)
      .map(chunk => chunk.trim())
      .filter(Boolean)
      .map(chunk => buildLine(chunk))
      .filter((entry): entry is ExtractedLine => Boolean(entry && (entry.text || entry.rich)))
  }

  const normalized = normalize(raw)
  return normalized
    .split(/<br>/g)
    .map(segment => segment.trim())
    .filter(Boolean)
    .map(segment => buildLine(segment, true))
    .filter((entry): entry is ExtractedLine => Boolean(entry && (entry.text || entry.rich)))
}

const includesKeyword = (text: string, keyword: string) =>
  text.replace(/\s+/g, '').toLowerCase().includes(keyword.replace(/\s+/g, '').toLowerCase())

const findStatByKeywords = (keywords: string[]) =>
  allStats.value.find(stat => keywords.some(keyword => includesKeyword(stat.type, keyword)))

const findFallbackStatByKeywords = (keywords: string[]) =>
  fallbackBasicStats.value.find(stat => keywords.some(keyword => includesKeyword(stat.type, keyword)))

const resolveStatWithFallback = (keywords: string[]) =>
  findFallbackStatByKeywords(keywords) ?? findStatByKeywords(keywords)

const distinctStats = computed(() => {
  const seen = new Set<string>()
  return allStats.value.filter(stat => {
    const key = stat.type ? stat.type.replace(/\s+/g, '').toLowerCase() : ''
    if (!key || seen.has(key)) return false
    seen.add(key)
    return true
  })
})

const physicalDefenseStat = computed(() =>
  resolveStatWithFallback(['물리 방어력', '물리방어력', '물방'])
)

const magicDefenseStat = computed(() =>
  resolveStatWithFallback(['마법 방어력', '마법방어력', '마방'])
)

const vitalityStat = computed(() =>
  resolveStatWithFallback(['생명력', '최대 생명력', '체력'])
)

const damageReductionStat = computed(() =>
  resolveStatWithFallback(['피해 감소', '피해감소', '받는 피해', '방어력'])
)

const mainStat = computed(() => resolveStatWithFallback(mainStatKeywords))

const normalizedMainKeywords = mainStatKeywords.map(keyword =>
  keyword.replace(/\s+/g, '').toLowerCase()
)

const isMainStatType = (type?: string) => {
  if (!type) return false
  const normalized = type.replace(/\s+/g, '').toLowerCase()
  return normalizedMainKeywords.some(keyword => normalized.includes(keyword))
}

const isDefenseStatType = (type?: string) => {
  if (!type) return false
  const normalized = type.replace(/\s+/g, '').toLowerCase()
  return defenseStatKeywords.some(keyword => normalized.includes(keyword.replace(/\s+/g, '').toLowerCase()))
}

const statsWithoutMain = computed(() => {
  return distinctStats.value.filter(stat => !isMainStatType(stat.type))
})

const combatStatKeywords = ['치명', '특화', '제압', '신속', '인내', '숙련']

const braceletFallbackStats = computed(() => {
  const existingKeys = new Set(
    statsWithoutMain.value.map(stat => stat.type?.replace(/\s+/g, '').toLowerCase())
  )
  const normalizedKeywords = combatStatKeywords.map(keyword => keyword.replace(/\s+/g, '').toLowerCase())

  return fallbackBasicStats.value.filter(stat => {
    const normalized = stat.type?.replace(/\s+/g, '').toLowerCase() ?? ''
    if (!normalizedKeywords.some(keyword => normalized.includes(keyword))) {
      return false
    }
    if (existingKeys.has(normalized)) {
      return false
    }
    existingKeys.add(normalized)
    return true
  })
})

const normalizeKeyword = (value: string) => value.replace(/\s+/g, '').toLowerCase()

type ParsedBraceletLine =
  | { isEffect: true; effect: ExtractedLine }
  | { isEffect: false; stat: StatItem }

const parsedBraceletLines = computed<ParsedBraceletLine[]>(() => {
  const lines = extractItemPartLines(selectedParsed.value?.rawElements?.Element_005, /팔찌\s*효과/i, {
    groupByImage: true
  })
  return lines.map(line => {
    const { text, rich, color } = line
    const plusIndex = text.indexOf('+')
    if (plusIndex === -1) {
      return { isEffect: true, effect: line }
    }
    const label = text.slice(0, plusIndex).trim()
    const value = text.slice(plusIndex + 1).trim()
    const normalized = normalizeKeyword(label)
    const isPrimary = combatStatKeywords.some(keyword => normalized.includes(normalizeKeyword(keyword)))
    if (!isPrimary) {
      return { isEffect: true, effect: line }
    }
    const richValue = (() => {
      if (!rich) return value
      const marker = rich.indexOf(value)
      if (marker !== -1) return rich.slice(marker).trim()
      const lastPlus = rich.lastIndexOf('+')
      return lastPlus !== -1 ? rich.slice(lastPlus).trim() : rich
    })()
    return {
      isEffect: false,
      stat: {
        type: label,
        value,
        richValue,
        color
      }
    }
  })
})

const formatBraceletEffectLine = (line: string) => {
  if (!line) return ''
  const trimmed = line.replace(/\s+/g, ' ').trim()
  return trimmed.replace(/(?<!\d)\.(\s*)(?=[^\s<])/g, '.\r\n').replace(/\s*\r\n\s*/g, '\r\n').trim()
}

const braceletEffectLines = computed<ExtractedLine[]>(() => {
  const seen = new Set<string>()
  return parsedBraceletLines.value
    .filter((item): item is { isEffect: true; effect: ExtractedLine } => item.isEffect)
    .map(item => {
      const formatted = formatBraceletEffectLine(item.effect.text || item.effect.rich)
      return {
        text: formatted,
        rich: item.effect.rich || item.effect.text,
        color: item.effect.color
      }
    })
    .filter(line => {
      const normalized = stripHtml(line.text).replace(/\s+/g, ' ').trim()
      if (!normalized) return false
      if (seen.has(normalized)) return false
      seen.add(normalized)
      return true
    })
})

interface BraceletStatGrouping {
  combatStats: StatItem[]
  braceletStats: StatItem[]
}

const makeUniqueStats = (stats: StatItem[]) => {
  const seen = new Set<string>()
  return stats.filter(stat => {
    const key = `${normalizeKeyword(stat.type)}|${stat.value}|${stat.richValue ?? ''}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  })
}

const groupedBraceletStats = computed<BraceletStatGrouping>(() => {
  const hasAdditional = Boolean(selectedParsed.value?.additionalStats?.length)
  const baseStats = hasAdditional ? selectedParsed.value!.additionalStats! : statsWithoutMain.value
  const numericPartStats = parsedBraceletLines.value
    .filter((item): item is { isEffect: false; stat: StatItem } => !item.isEffect && Boolean(item.stat))
    .map(item => item.stat)

  const baseCombatStats = makeUniqueStats(
    baseStats.filter(stat =>
      combatStatKeywords.some(keyword => normalizeKeyword(stat.type).includes(normalizeKeyword(keyword)))
    )
  )
  const baseOtherStats = baseStats.filter(stat => !baseCombatStats.includes(stat))

  const combatStatsOrdered = combatStatKeywords
    .map(keyword => {
      const normalizedKeyword = normalizeKeyword(keyword)
      return (
        numericPartStats.find(stat => normalizeKeyword(stat.type).includes(normalizedKeyword)) ||
        baseCombatStats.find(stat => normalizeKeyword(stat.type).includes(normalizedKeyword)) ||
        braceletFallbackStats.value.find(stat => normalizeKeyword(stat.type).includes(normalizedKeyword))
      )
    })
    .filter((stat): stat is StatItem => Boolean(stat))

  const uniqueCombatStats = makeUniqueStats(combatStatsOrdered)
  const combatSet = new Set(uniqueCombatStats)
  const remainingNumeric = numericPartStats.filter(stat => !combatSet.has(stat))

  return {
    combatStats: uniqueCombatStats.length ? uniqueCombatStats : [],
    braceletStats: makeUniqueStats([...baseOtherStats, ...braceletFallbackStats.value, ...remainingNumeric])
  }
})

const abilityStoneStatBuckets = computed(() => {
  const penaltyPattern = /(감소|패널티|감소된|감소 효과)/i
  const abilityKeywords = /(활성도|감소|패널티)/i
  const relevantStats = [
    ...distinctStats.value,
    ...fallbackBasicStats.value
  ].filter(stat => abilityKeywords.test(stat.type))
  const unique = (stats: StatItem[]) => {
    const seen = new Set<string>()
    return stats.filter(stat => {
      const key = `${stat.type}|${stat.value}`
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
  }
  return {
    positives: unique(relevantStats.filter(stat => !penaltyPattern.test(stat.type))),
    penalties: unique(relevantStats.filter(stat => penaltyPattern.test(stat.type)))
  }
})

const abilityStoneStageBonusLines = computed(() =>
  extractItemPartLines(selectedParsed.value?.rawElements?.Element_006, /세공\s*단계\s*보너스/i).map(
    line => line.rich || line.text
  )
)

const abilityStoneEngravingLines = computed(() => selectedParsed.value?.abilityStoneEngravings ?? [])

const abilityStoneBaseStats = computed<StatItem[]>(() => {
  const lines = extractItemPartLines(selectedParsed.value?.rawElements?.Element_004, /기본\s*효과/i)
  const unique = new Map<string, StatItem>()
  lines.forEach(({ text, rich }) => {
    const plusIndex = text.indexOf('+')
    if (plusIndex === -1) return
    const type = text.slice(0, plusIndex).trim()
    const value = text.slice(plusIndex + 1).trim()
    if (!type || !value) return
    const key = type.replace(/\s+/g, '').toLowerCase()
    if (!unique.has(key)) {
      unique.set(key, { type, value, richValue: rich })
    }
  })
  return Array.from(unique.values())
})

const weaponAttackStat = computed(() =>
  selectedParsed.value?.weaponAttackStat ??
  findStatByKeywords(['무기 공격력', '무기공격력'])
)
const sangjaeStat = computed(() =>
  selectedParsed.value?.sangjaeStat ??
  findStatByKeywords(['상재 효과', '상재', '상급 재련', '상급재련', '상제'])
)
const transcendenceLastStage = computed(() => selectedParsed.value?.transcendenceMaxStage ?? '')
const additionalEffectStat = computed(() =>
  selectedParsed.value?.additionalEffectStat ??
  findStatByKeywords(['추가 피해', '추가피해', '추가 효과'])
)

const LOSTARK_EMOTICON_BASE =
  'https://cdn-lostark.game.onstove.com/efui_iconatlas/emoticon/'
const TRANSCENDENCE_ICON_FALLBACK =
  'https://cdn-lostark.game.onstove.com/2018/obt/assets/images/common/game/ico_tooltip_transcendence.png'

const resolveEmoticonUrl = (src?: string) => {
  if (!src) return ''
  if (src.startsWith('http')) return src
  return `${LOSTARK_EMOTICON_BASE}${src}.png`
}

const extractEmoticonId = (html?: string) => {
  if (!html) return ''
  const match = html.match(/<img[^>]*src=['"]([^'"]+)['"]/i)
  return match ? match[1] : ''
}

const hasValidQuality = (quality?: number) =>
  typeof quality === 'number' && Number.isFinite(quality) && quality >= 0

const selectedQualityColor = computed(() => {
  const quality = hasValidQuality(selectedParsed.value?.quality) ? selectedParsed.value?.quality : undefined
  return getQualityColor(quality)
})

const qualityChipText = computed(() => {
  if (!hasValidQuality(selectedParsed.value?.quality)) return ''
  return `품질 ${selectedParsed.value?.quality}`
})

const getQualityDisplayValue = (quality?: number) => {
  if (!hasValidQuality(quality)) return ''
  return quality
}

const sangjaeChipText = computed(() =>
  sangjaeStat.value ? `상재 +${sangjaeStat.value.value}` : ''
)

const sangjaeColor = computed(() => sangjaeStat.value?.color || '#b45309')

const isJsonLike = (text?: string) => {
  if (!text) return false
  const trimmed = text.trim()
  return trimmed.startsWith('{') || trimmed.includes(':"') || trimmed.includes('\\"type\\"')
}

const stripSummaryHtml = (text: string) =>
  text.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()

const normalizeSangjaeValue = (value?: string) => {
  if (!value) return undefined
  return value.replace(/\s+/g, ' ').replace(/["']+$/g, '').trim()
}

interface SlotEffectInfo {
  stage?: number
  value?: number
}

const slotEffectInfo = ref<SlotEffectInfo | null>(null)

const NUMBER_PATTERN = /([+-]?\d[\d,]*(?:\.\d+)?)/g

const formatNumbersInText = (text: string) => {
  if (!text) return text
  return text.replace(NUMBER_PATTERN, match => {
    const hasPlus = match.startsWith('+')
    const normalized = match.replace(/,/g, '')
    const numeric = Number(normalized)
    if (Number.isNaN(numeric)) {
      return match
    }
    const decimalPart = normalized.split('.')[1]
    const decimals = decimalPart ? decimalPart.length : 0
    const formatted = numeric.toLocaleString(undefined, {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    })
    if (hasPlus && !formatted.startsWith('+') && !formatted.startsWith('-')) {
      return `+${formatted}`
    }
    return formatted
  })
}

const formatPlainNumber = (value?: number | string) => {
  if (value === undefined || value === null || value === '') return '-'
  const text = typeof value === 'number' ? String(value) : value
  const formatted = formatNumbersInText(text)
  return formatted || '-'
}

const formatStatValue = (value?: string) => {
  if (!value) return '—'
  const trimmed = value.trim()
  const withSign = trimmed.startsWith('+') || trimmed.startsWith('-') ? trimmed : `+${trimmed}`
  return formatNumbersInText(withSign)
}

const additionalEffectLines = computed(() => {
  slotEffectInfo.value = null
  if (!selectedEquipment.value) return []
  const parsed = getParsedEquipment(selectedEquipment.value)
  const lines: string[] = []
  if (parsed?.additionalEffectStat) {
    lines.push(`${parsed.additionalEffectStat.type} +${parsed.additionalEffectStat.value}`)
  }
  parsed?.additionalStats?.forEach(stat => {
    lines.push(`${stat.type} +${stat.value}`)
  })
  parsed?.elixirEffects?.forEach(effect => lines.push(effect))
  parsed?.engravingEffects?.forEach(effect => lines.push(effect))
  const buckets = tooltipValueMap.value[selectedEquipment.value.__uid]
  if (buckets) {
    lines.push(...buckets.enhancements)
  }
  const condensed = condenseBasicEffectStages(lines)
  const filtered: string[] = []

  condensed.forEach(line => {
    const trimmed = line.trim()
    if (/^추가\s*효과\s+추가\s*효과$/i.test(trimmed)) {
      return
    }
    const slotMatch = trimmed.match(/슬롯\s*효과\s*\[초월\]\s*(\d+)\s*단계\s*(\d+)/i)
    if (slotMatch) {
      slotEffectInfo.value = {
        stage: Number(slotMatch[1]),
        value: Number(slotMatch[2])
      }
      return
    }
    filtered.push(line)
  })

  const cleaned = filterNoise(filtered)
  return cleaned.map(line => formatNumbersInText(line))
})

const refinementLines = computed<ExtractedLine[]>(() => {
  const parsed = selectedParsed.value
  if (!parsed) return []
  const elementLines = extractItemPartLines(parsed.rawElements?.Element_006, /연마\s*효과/i)
  if (elementLines.length) return elementLines
  const lines = [
    parsed.elixirStageSummary,
    parsed.elixirSummary,
    ...(parsed.elixirEffects ?? [])
  ].filter((line): line is string => Boolean(line))
  const target = lines.find(line => /(연성|연마)/.test(line))
  return target
    ? [
        {
          text: formatNumbersInText(target),
          rich: formatNumbersInText(target)
        }
      ]
    : []
})

const extractSangjaeSegments = (rawElement: unknown): string[] => {
  if (!rawElement) return []
  let htmlValue = ''
  if (typeof rawElement === 'string') {
    htmlValue = rawElement
  } else if (
    typeof rawElement === 'object' &&
    rawElement !== null &&
    'value' in rawElement &&
    typeof (rawElement as { value?: unknown }).value === 'string'
  ) {
    htmlValue = (rawElement as { value?: string }).value ?? ''
  } else {
    htmlValue = JSON.stringify(rawElement)
  }

  return htmlValue
    .split(/\r?\n|\\n/g)
    .map(segment => stripSummaryHtml(segment))
    .map(segment => segment.replace(/\\t/g, ' ').trim())
    .filter(Boolean)
}

const parseSangjaeFromElement = (rawElement: unknown) => {
  const segments = extractSangjaeSegments(rawElement)
  if (!segments.length) return { stage: undefined, value: undefined }

  let bestStage = -Infinity
  let bestValue: string | undefined

  segments.forEach(segment => {
    const stageMatch = segment.match(/(\d+)\s*단계/i)
    if (!stageMatch) return
    const stageNumber = Number(stageMatch[1])
    const hyphenIndex = segment.indexOf('-')
    if (hyphenIndex === -1) return
    const effectText = segment.slice(hyphenIndex + 1).trim()
    if (!effectText) return
    if (stageNumber > bestStage) {
      bestStage = stageNumber
      bestValue = normalizeSangjaeValue(effectText) ?? effectText.replace(/\s+/g, ' ')
    }
  })

  if (bestStage === -Infinity) {
    const fallbackLine = segments.find(line => /상급\s*재련/i.test(line))
    const fallbackMatch = fallbackLine?.match(/(\d+)\s*단계/i)
    return { stage: fallbackMatch ? Number(fallbackMatch[1]) : undefined, value: undefined }
  }

  return { stage: bestStage, value: bestValue }
}

type SangjaeInfo = {
  stage?: number
  value?: string
}

const computeSangjaeInfoFromParsed = (parsed?: ParsedTooltip): SangjaeInfo => {
  const fallback = parseSangjaeFromElement(parsed?.rawElements?.Element_005)

  const parsedStage = parsed?.sangjaeStage
  const fallbackStage = fallback.stage

  const normalizedParsedValue = normalizeSangjaeValue(parsed?.sangjaeValue)
  const normalizedFallbackValue = normalizeSangjaeValue(fallback.value)

  const bestStage = Math.max(
    parsedStage ?? Number.NEGATIVE_INFINITY,
    fallbackStage ?? Number.NEGATIVE_INFINITY
  )
  const stage = Number.isFinite(bestStage) ? bestStage : undefined

  let value: string | undefined
  if (stage !== undefined) {
    if (stage === fallbackStage && normalizedFallbackValue) {
      value = normalizedFallbackValue
    } else if (stage === parsedStage && normalizedParsedValue) {
      value = normalizedParsedValue
    } else {
      value = normalizedFallbackValue ?? normalizedParsedValue
    }
  } else {
    value = normalizedFallbackValue ?? normalizedParsedValue
  }

  return { stage, value }
}

const sangjaeInfo = computed(() => computeSangjaeInfoFromParsed(selectedParsed.value))

interface CoreCell {
  label: string
  value: string
  color?: string
  tooltipLines?: string[]
  tooltipTotals?: TranscendenceAggregates
}

interface CoreRow {
  left: CoreCell
  right?: CoreCell
  leftFullWidth?: boolean
}

const hoveredTooltipLines = ref<string[] | null>(null)
const showHighlightTooltip = (cell?: CoreCell) => {
  if (!cell) {
    hoveredTooltipLines.value = null
    return
  }
  const lines: string[] = []
  if (cell.tooltipLines?.length) {
    lines.push(...cell.tooltipLines)
  }
  hoveredTooltipLines.value = lines.length ? lines : null
}

const clearHighlightTooltip = () => {
  hoveredTooltipLines.value = null
}

const getStatDisplayValue = (stat?: StatItem | null) => (stat ? formatStatValue(stat.value) : '-')

const createStatCell = (label: string, stat?: StatItem | null, useStatLabel = false): CoreCell => ({
  label: useStatLabel && stat?.type ? stat.type : label,
  value: getStatDisplayValue(stat),
  color: stat?.color
})

const createMainStatCell = (stat?: StatItem | null): CoreCell =>
  createStatCell('힘/민/지', stat ?? null, false)

const createTextCell = (label: string, text?: string | null, color?: string): CoreCell => ({
  label,
  value: text && text.trim() ? text : '-',
  color
})

const coreRows = computed<CoreRow[]>(() => {
  additionalEffectLines.value
  const rows: CoreRow[] = []
  let category = equipmentCategory.value
  const selectedType = selectedEquipment.value?.type?.toLowerCase() ?? ''

  if (category === 'unknown') {
    if (weaponAttackStat.value) {
      category = 'weapon'
    } else if (physicalDefenseStat.value || magicDefenseStat.value) {
      category = 'armor'
    } else if (matchesKeyword(selectedType, braceletTypeKeywords)) {
      category = 'bracelet'
    } else if (matchesKeyword(selectedType, abilityStoneTypeKeywords)) {
      category = 'abilityStone'
    } else if (matchesKeyword(selectedType, accessoryTypeKeywords)) {
      category = 'accessory'
    } else if (abilityStoneStatBuckets.value.positives.length || abilityStoneStatBuckets.value.penalties.length) {
      category = 'abilityStone'
    } else if (statsWithoutMain.value.length) {
      category = 'accessory'
    }
  }

  const hasValue = (cell?: CoreCell) => {
    if (!cell) return false
    const value = cell.value?.toString().trim()
    if (!value) return false
    return value !== '-' && value !== '—'
  }

  const createSpacerCell = (): CoreCell => ({
    label: '',
    value: ''
  })

  const pushRow = (left?: CoreCell, right?: CoreCell, options?: { fullWidth?: boolean }) => {
    if (!left && !right) return
    const leftHasValue = hasValue(left)
    const rightHasValue = hasValue(right)
    const fullWidth = Boolean(options?.fullWidth)
    if (!leftHasValue && (!rightHasValue || fullWidth)) return
    rows.push({
      left: left ?? { label: '', value: '-' },
      right: fullWidth ? undefined : right ?? { label: '', value: '-' },
      leftFullWidth: fullWidth
    })
  }

  const createDynamicStatCell = (stat?: StatItem, fallbackLabel = '능력치'): CoreCell =>
    createStatCell(fallbackLabel, stat ?? null, true)

  const buildRowsFromStats = (stats: StatItem[], fallbackLabel: string) => {
    if (!stats.length) return
    for (let i = 0; i < stats.length; i += 2) {
      const leftStat = stats[i]
      const rightStat = stats[i + 1]
      pushRow(
        createDynamicStatCell(leftStat, `${fallbackLabel} ${i + 1}`),
        rightStat ? createDynamicStatCell(rightStat, `${fallbackLabel} ${i + 2}`) : undefined
      )
    }
  }

  switch (category) {
    case 'weapon':
      pushRow(
        createStatCell('무기 공격력', weaponAttackStat.value ?? null),
        createStatCell('추가 피해', additionalEffectStat.value ?? null)
      )
      break
    case 'armor': {
      pushRow(
        createStatCell('물리 방어력', physicalDefenseStat.value ?? null, true),
        createStatCell('마법 방어력', magicDefenseStat.value ?? null, true)
      )
      pushRow(
        createMainStatCell(mainStat.value ?? null),
        createStatCell('체력', vitalityStat.value ?? null, true)
      )
      const armorBonusStat = additionalEffectStat.value ?? damageReductionStat.value ?? null
      if (armorBonusStat && !isDefenseStatType(armorBonusStat.type)) {
        pushRow(createStatCell('방어 옵션', armorBonusStat), undefined)
      }
      break
    }
    case 'accessory': {
      const stats = [...statsWithoutMain.value]
      if (mainStat.value) {
        const paired = stats.shift()
        pushRow(createMainStatCell(mainStat.value), paired ? createDynamicStatCell(paired) : undefined)
      }
      buildRowsFromStats(stats.slice(0, 4), '장신구 옵션')
      if (refinementLines.value.length) {
        refinementLines.value.forEach(line => {
          pushRow(createTextCell('연마 효과', line.text, line.color), undefined, { fullWidth: true })
        })
      }
      break
    }
    case 'bracelet': {
      const combatStats = groupedBraceletStats.value.combatStats
      if (combatStats.length) {
        pushRow(
          createTextCell('전투 특성', ''),
          createSpacerCell(),
          { fullWidth: true }
        )
        for (let i = 0; i < combatStats.length; i += 2) {
          const left = combatStats[i]
          if (!left) continue
          const right = combatStats[i + 1]
          pushRow(createStatCell(left.type, left, true), right ? createStatCell(right.type, right, true) : undefined)
        }
      } else {
        pushRow(createTextCell('전투 특성', '부여된 전투 특성이 없습니다.'), undefined, { fullWidth: true })
      }



      braceletEffectLines.value.forEach(line => {
        pushRow(createTextCell('팔찌 특성', line.text, line.color), undefined, { fullWidth: true })
      })

      break
    }
    case 'abilityStone': {
      const baseStats = [...abilityStoneBaseStats.value]
      const stageLines = [...abilityStoneStageBonusLines.value]
      if (baseStats.length) {
        const primary = baseStats.shift()!
        const pairedStage = stageLines.shift()
        pushRow(
          createStatCell(primary.type, primary, true),
          pairedStage ? createTextCell('세공 단계 보너스', pairedStage) : undefined
        )
        baseStats.forEach(stat => {
          pushRow(createStatCell(stat.type, stat, true), undefined)
        })
      }
      const { positives, penalties } = abilityStoneStatBuckets.value
      buildRowsFromStats(positives.slice(0, 4), '각인 효과')
      buildRowsFromStats(penalties.slice(0, 2), '감소 효과')
      stageLines.forEach(line => {
        pushRow(createTextCell('세공 단계 보너스', line), undefined, { fullWidth: true })
      })
      abilityStoneEngravingLines.value.forEach((line, idx) => {
        pushRow(createTextCell(idx === 0 ? '세공' : '', formatNumbersInText(line)), undefined, {
          fullWidth: true
        })
      })
      break
    }
    default: {
      const stats = [...statsWithoutMain.value]
      if (mainStat.value) {
        const paired = stats.shift()
        pushRow(createMainStatCell(mainStat.value), paired ? createDynamicStatCell(paired) : undefined)
      }
      buildRowsFromStats(stats.slice(0, 4), '능력치')
      break
    }
  }

  const sangjae = sangjaeInfo.value
  if (sangjae.stage !== undefined || sangjae.value) {
    pushRow(
      {
        label: '상재 단계',
        value: sangjae.stage !== undefined ? formatPlainNumber(sangjae.stage) : '-'
      },
      {
        label: '상재 수치',
        value: sangjae.value ? formatNumbersInText(sangjae.value) : '-'
      }
    )
  }

  const slot = slotEffectInfo.value
  const stageLines = selectedParsed.value?.transcendenceStages
  const totals = selectedParsed.value?.transcendenceAggregates
  if (slot?.stage !== undefined || slot?.value !== undefined) {
    pushRow(
      {
        label: '초월 단계',
        value: slot?.stage !== undefined ? formatPlainNumber(slot.stage) : '-',
        tooltipLines: stageLines,
        tooltipTotals: totals
      },
      {
        label: '초월 수치',
        value: slot?.value !== undefined ? formatPlainNumber(slot.value) : '-',
        tooltipLines: stageLines,
        tooltipTotals: totals
      }
    )
  }

  return rows
})

const tooltipValueMap = computed<Record<string, TooltipValueBuckets>>(() => {
  const map: Record<string, TooltipValueBuckets> = {}
  equipmentWithIds.value.forEach(item => {
    const values = extractTooltipValues(item)
    map[item.__uid] = categorizeTooltipValues(values)
  })
  return map
})

const slotInfoMap = computed<Record<string, SlotEffectInfo | null>>(() => {
  const map: Record<string, SlotEffectInfo | null> = {}
  equipmentWithIds.value.forEach(item => {
    const parsed = getParsedEquipment(item)
    const bucket = tooltipValueMap.value[item.__uid]
    map[item.__uid] = computeSlotEffectInfoFromParsed(parsed, bucket)
  })
  return map
})

const getSlotInfoForItem = (item: EquipmentWithId) => slotInfoMap.value[item.__uid] ?? null
const sangjaeInfoMap = computed<Record<string, SangjaeInfo>>(() => {
  const map: Record<string, SangjaeInfo> = {}
  equipmentWithIds.value.forEach(item => {
    map[item.__uid] = computeSangjaeInfoFromParsed(getParsedEquipment(item))
  })
  return map
})
const getSangjaeStageForItem = (item: EquipmentWithId) => sangjaeInfoMap.value[item.__uid]?.stage ?? null

const filterNoise = (lines: string[]) => {
  const cleaned = lines
    .map(line => line?.trim())
    .filter((line): line is string => Boolean(line) && !isJsonLike(line!))
  return Array.from(new Set(cleaned))
}

const condenseBasicEffectStages = (lines: string[]) => {
  let maxStage = -Infinity
  let maxLine: string | null = null
  const others: string[] = []

  lines.forEach(line => {
    const match = line.match(/(\d+)\s*단계[^\n]*기본\s*효과/i)
    if (match) {
      const stage = Number(match[1])
      if (stage > maxStage) {
        maxStage = stage
        maxLine = line
      }
    } else {
      others.push(line)
    }
  })

  if (maxLine) {
    others.push(maxLine)
  }
  return others
}

const buildSlotEffectLines = (parsed?: ParsedTooltip, bucket?: TooltipValueBuckets) => {
  const lines: string[] = []
  if (!parsed) return lines
  if (parsed.additionalEffectStat) {
    lines.push(`${parsed.additionalEffectStat.type} +${parsed.additionalEffectStat.value}`)
  }
  parsed.additionalStats?.forEach(stat => {
    lines.push(`${stat.type} +${stat.value}`)
  })
  parsed.elixirEffects?.forEach(effect => lines.push(effect))
  parsed.engravingEffects?.forEach(effect => lines.push(effect))
  if (bucket) {
    lines.push(...bucket.enhancements)
  }
  return lines
}

const computeSlotEffectInfoFromParsed = (
  parsed?: ParsedTooltip,
  bucket?: TooltipValueBuckets
): SlotEffectInfo | null => {
  const lines = buildSlotEffectLines(parsed, bucket)
  if (!lines.length) return null
  const condensed = condenseBasicEffectStages(lines)
  for (const line of condensed) {
    const trimmed = line.trim()
    const slotMatch = trimmed.match(/슬롯\s*효과\s*\[초월\]\s*(\d+)\s*단계\s*(\d+)/i)
    if (slotMatch) {
      return {
        stage: Number(slotMatch[1]),
        value: Number(slotMatch[2])
      }
    }
  }
  return null
}

const parseTradeStatus = (rawElements?: Record<string, any>) => {
  if (!rawElements) return ''
  const candidates = [rawElements.Element_003, rawElements.Element_004]

  const extractText = (element: any): string => {
    if (!element) return ''
    if (typeof element === 'string') return element
    if (typeof element === 'object') {
      if (typeof element.value === 'string') return element.value
      return JSON.stringify(element.value ?? element)
    }
    return ''
  }

  for (const candidate of candidates) {
    const text = extractText(candidate)
    if (!text) continue
    const normalized = stripSummaryHtml(text).replace(/\|\s*/g, '| ').replace(/\s+/g, ' ').trim()
    if (!/거래/.test(normalized)) continue
    const remainingMatch = normalized.match(/(\d+)\s*(?:회|번)/)
    const remaining = remainingMatch ? Number(remainingMatch[1]) : null
    const remainingText = remaining !== null ? ` (남은 횟수 ${remaining}번)` : ''
    const isAvailable = /거래\s*가능/.test(normalized)
    return isAvailable ? `| 거래 가능${remainingText}` : '| 거래 불가'
  }

  return ''
}

const tradeInfoLines = computed(() => {
  if (!selectedEquipment.value) return []
  const buckets = tooltipValueMap.value[selectedEquipment.value.__uid]
  if (!buckets) return []
  const lines = [
    ...buckets.system,
    ...buckets.ownership,
    ...buckets.restrictions,
    ...buckets.durability,
    ...buckets.flavor,
    ...buckets.misc
  ]
    .filter(Boolean)
    .filter(line => /거래/.test(line))
    .map(line => line.replace(/\|\s*/g, '| ').replace(/\s+/g, ' ').trim())

  const tradeEntry = parseTradeStatus(selectedParsed.value?.rawElements)
  if (tradeEntry) {
    lines.unshift(tradeEntry)
  }

  return filterNoise(lines)
})

const findFirstEquipmentValue = (selector: (parsed: ParsedTooltip) => string | undefined): string => {
  for (const item of equipmentList.value) {
    const parsed = getParsedEquipment(item)
    const value = parsed ? selector(parsed) : undefined
    if (value) {
      return value
    }
  }
  return ''
}

const globalTranscendenceSummaryRaw = computed(() =>
  findFirstEquipmentValue(parsed => parsed.transcendenceSummary)
)

const displayTranscendenceSummary = computed(() => {
  const raw = globalTranscendenceSummaryRaw.value
  if (!raw) return { text: '', iconSrc: '' }
  const extracted = resolveEmoticonUrl(extractEmoticonId(raw))
  const iconSrc = extracted || TRANSCENDENCE_ICON_FALLBACK
  const clean = stripSummaryHtml(raw)
  const countMatch = clean.match(/(\d+)\s*개/)
  if (countMatch) {
    return { text: `${countMatch[1]}개`, iconSrc }
  }
  const fallback = clean.match(/\d+/)
  return { text: fallback ? `${fallback[0]}개` : clean, iconSrc }
})

const globalElixirSummary = computed(() =>
  findFirstEquipmentValue(parsed => parsed.elixirStageSummary || parsed.elixirSummary)
)

const globalSummaryItems = computed(() => {
  const items: { label: string; value: string; icon?: 'transcendence' | 'elixir'; iconSrc?: string }[] =
    []
  if (displayTranscendenceSummary.value.text) {
    items.push({
      label: '초월 총합',
      value: displayTranscendenceSummary.value.text,
      icon: displayTranscendenceSummary.value.iconSrc ? 'transcendence' : undefined,
      iconSrc: displayTranscendenceSummary.value.iconSrc
    })
  }
  if (globalElixirSummary.value) {
    items.push({ label: '엘릭서 세트 효과', value: globalElixirSummary.value, icon: 'elixir' })
  }
  return items
})

const statToLine = (stat?: StatItem | null) => {
  if (!stat || isJsonLike(stat.type)) return null
  return `${stat.type} ${formatStatValue(stat.value)}`
}

const getSummaryLines = (item: EquipmentWithId) => {
  const parsed = getParsedEquipment(item)
  if (!parsed) return []
  const lines: string[] = []
  parsed.basicStats?.slice(0, 2).forEach(stat => {
    const line = statToLine(stat)
    if (line) lines.push(line)
  })
  if (parsed.additionalStats?.length) {
    const extra = statToLine(parsed.additionalStats[0])
    if (extra) lines.push(extra)
  }
  if (!lines.length && parsed.engravingEffects?.length) {
    const firstEngraving = parsed.engravingEffects[0]
    if (firstEngraving) {
      lines.push(firstEngraving)
    }
  }
  const sanitized = lines.filter(line => !/상재/.test(line))
  return sanitized.slice(0, 2)
}

const getQualityBadgeColor = (item: EquipmentWithId) => getQualityColor(getParsedEquipment(item)?.quality)

interface TooltipValueBuckets {
  stats: string[]
  descriptions: string[]
  restrictions: string[]
  urls: string[]
  flavor: string[]
  system: string[]
  enhancements: string[]
  ownership: string[]
  durability: string[]
  summary: string[]
  misc: string[]
}

const extractTooltipValues = (item: Equipment): string[] => {
  if (!item.tooltip) return []
  try {
    const raw = JSON.parse(item.tooltip)
    const normalize = (value: any): string[] => {
      if (!value) return []
      if (typeof value === 'string') return [cleanText(value)]
      if (Array.isArray(value)) return value.flatMap(normalize)
      if (typeof value === 'object') {
        if ('value' in value) return normalize(value.value)
        return Object.values(value).flatMap(normalize)
      }
      return []
    }
    return Object.values(raw).flatMap(normalize).filter(Boolean)
  } catch {
    return [cleanText(item.tooltip)]
  }
}

const categorizeTooltipValues = (values: string[]): TooltipValueBuckets => {
  const buckets: TooltipValueBuckets = {
    stats: [],
    descriptions: [],
    restrictions: [],
    urls: [],
    flavor: [],
    system: [],
    enhancements: [],
    ownership: [],
    durability: [],
    summary: [],
    misc: []
  }

  values.forEach(value => {
    const text = value.trim()
    if (!text) return

    if (/https?:\/\//i.test(text)) {
      buckets.urls.push(text)
    } else if (/(장착|전용|요구|불가|사용 가능)/.test(text)) {
      buckets.restrictions.push(text)
    } else if (/(레벨|티어|힘|민첩|지능|체력|치명|특화|신속|제압|인내|숙련|공격력|방어력|품질|피해|생명력)/.test(text)) {
      buckets.stats.push(text)
    } else if (/(무기|방패|창|검|활|장갑|투구|어깨|상의|하의|악세서리|보석|목걸이|귀걸이|반지)/.test(text)) {
      buckets.descriptions.push(text)
    } else if (/(연마|초월|추가 효과|슬롯 효과|강화|아크 패시브)/.test(text)) {
      buckets.enhancements.push(text)
    } else if (/(귀속|거래 불가|교환 불가|캐릭터 귀속)/.test(text)) {
      buckets.ownership.push(text)
    } else if (/(내구도)/.test(text)) {
      buckets.durability.push(text)
    } else if (/(총|전체|모든 장비|적용된)/.test(text)) {
      buckets.summary.push(text)
    } else if (/(제작|재련|해체|분해|거래|획득|교환|연성)/.test(text)) {
      buckets.system.push(text)
    } else if (/(느껴|전설|고대|기억|전해지|신화)/.test(text)) {
      buckets.flavor.push(text)
    } else {
      buckets.misc.push(text)
    }
  })

  return buckets
}

const cleanText = (text: string) =>
  text
    .replace(/<[^>]+>/g, ' ')
    .replace(/\\n/g, ' ')
    .replace(/&[^;]+;/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
</script>

<style scoped>
.character-detail-panel {
  width: 100%;
}

.detail-placeholder {
  padding: 40px;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--card-bg);
  text-align: center;
  color: var(--text-secondary);
}

.detail-shell {
  width: 100%;
  color: var(--text-primary);
}

.global-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}

.global-summary__item {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: var(--shadow-sm);
}

.global-summary__item .summary-icon {
  font-size: calc(1rem - 1px);
}

.global-summary__item .summary-icon-img {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.global-summary__item .summary-icon-img img {
  width: 18px;
  height: 18px;
  object-fit: contain;
}

.global-summary__item .label {
  font-size: calc(0.85rem - 1px);
  color: var(--text-secondary);
}

.global-summary__item .value {
  font-size: calc(0.85rem);
  font-weight: 600;
  color: var(--text-primary);
}

.equipment-detail-layout {
  display: flex;
  /* grid-template-columns: 230px 230px minmax(520px, 1fr) ; */
  gap: 20px;
  align-items: start;
}

.engraving-panel {
  margin-top: 28px;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.engraving-panel__head {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.engraving-panel__head h4 {
  margin: 0;
}

.engraving-panel__head p {
  margin: 4px 0 0;
  color: var(--text-secondary);
}

.engraving-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.engraving-metric {
  min-width: 120px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.engraving-metric span {
  font-size: calc(0.8rem - 1px);
  color: var(--text-secondary);
}

.engraving-metric strong {
  font-size: calc(1rem - 1px);
  color: var(--text-primary);
}

.engraving-metric--alert strong {
  color: var(--error-color, #ef4444);
}

.engraving-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.engraving-empty {
  margin: 0;
  padding: 16px;
  border-radius: 12px;
  background: var(--bg-secondary);
  color: var(--text-secondary);
  text-align: center;
}

.selected-equipment-panel {
  max-width: 560px;
}

.equipment-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
}

.equipment-card__header {
  display: flex;
  gap: 16px;
}

.equipment-card__icon-box {
  flex-shrink: 0;
  width: 65px;
  height: 65px;
  border-radius: 16px;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border-color);
}

.equipment-card__icon {
  border-radius: 12px;
  object-fit: cover;
}

.equipment-card__icon--fallback {
  font-size: calc(2rem - 1px);
  color: var(--text-tertiary);
}

.equipment-card__summary {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.equipment-card__title-row h3 {
  margin: 0 0 8px 0;
  font-size: calc(1.25rem - 1px);
}

.chip-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.chip {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--bg-secondary);
  font-size: calc(0.8rem - 1px);
  font-weight: 600;
  color: var(--text-secondary);
}

.chip--quality {
  /* border: 1px solid currentColor; */
  background: var(--bg-secondary);
}

.chip--sangjae {
  border: 1px solid currentColor;
  background: transparent;
}

.item-level-inline {
  margin-left: 8px;
  font-size: calc(0.9rem - 1px);
  color: var(--primary-color);
}

.core-stats-section {
  background: var(--bg-secondary);
  border-radius: 14px;
  padding: 18px;
  /* border: 1px solid var(--border-color); */
  width: 500px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
}

.section-head small {
  color: var(--text-secondary);
}

.core-stats-table {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.core-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.core-row--full {
  grid-template-columns: minmax(0, 1fr);
}

.core-cell {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px solid var(--border-color-light);
}

.core-cell--full {
  grid-column: 1 / -1;
}

.core-cell .cell-label {
  min-width: 80px;
  font-size: calc(0.85rem - 1px);
  color: var(--text-secondary);
}

.core-cell .cell-value {
  font-size: calc(1rem - 1px);
  font-weight: 700;
  color: var(--text-primary);
  text-align: right;
  word-break: keep-all;
  white-space: pre-wrap;
}

.trans-tooltip {
  margin-top: 8px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 14px;
  box-shadow: var(--shadow-sm);
  font-size: calc(0.85rem - 1px);
  color: var(--text-secondary);
  width: inherit;
  word-break: keep-all;
  white-space: pre-wrap;
}

.trans-tooltip p {
  margin: 4px 0;
  line-height: 1.4;
}

.core-empty {
  margin: 0;
  color: var(--text-tertiary);
  font-size: calc(0.85rem - 1px);
}

.trade-status-line {
  margin: 12px 0 0;
  font-size: calc(0.9rem - 1px);
  color: var(--text-secondary);
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.trade-status-line span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.equipment-list-panel {
  background: var(--card-bg);
  border-radius: 16px;
  width: fit-content;
  /* padding: 20px; */
  /* border: 1px solid var(--border-color); */
  /* box-shadow: var(--shadow-lg); */
}

.equipment-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.equipment-list-header h4 {
  margin: 0;
}

.equipment-list-header p {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: calc(0.85rem - 1px);
}

.equipment-empty {
  margin: 0;
  padding: 12px;
  border-radius: 10px;
  border: 1px dashed var(--border-color);
  font-size: calc(0.85rem - 1px);
  color: var(--text-secondary);
  background: var(--bg-secondary);
}

.count-pill {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--bg-secondary);
  font-size: calc(0.85rem - 1px);
}

.equipment-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.equipment-summary-card {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  /* border: 1px solid var(--border-color); */
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s, background 0.2s;
  background: var(--bg-secondary);
  width: fit-content;
  min-width: 250px;
}

.equipment-summary-card.active {
  border-color: var(--primary-color);
  background: var(--primary-soft-bg, rgba(102, 126, 234, 0.15));
}

.equipment-summary-card:hover {
  transform: translateY(-2px);
  border-color: var(--primary-color);
}

.summary-icon-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.summary-icon-image {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  /* background: var(--card-bg); */
  display: flex;
  align-items: center;
  justify-content: center;
  /* border: 1px solid var(--border-color-light); */
}

.summary-icon {
  border-radius: 10px;
  object-fit: cover;
}

.summary-icon--fallback {
  font-size: calc(1.25rem - 1px);
  color: var(--text-secondary);
}

.summary-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.summary-headline {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.summary-headline h5 {
  margin: 0;
  font-size: calc(0.85rem - 1px);
}

.summary-level {
  display: inline-flex;
  font-size: calc(0.85rem - 1px);
  color: var(--primary-color, #4b7bff);
  font-weight: 600;
}

.summary-meta {
  display: flex;
  gap: var(--space-sm);
  font-size: var(--font-sm);
  color: var(--text-secondary);
  flex-wrap: wrap;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 2px var(--space-sm);
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
  font-weight: var(--font-semibold);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  gap: var(--space-xs);
}

.pill--stage {
  border-color: var(--warning-color, #f0a500);
  color: var(--warning-color, #f0a500);
}

.pill--transcend {
  border-color: var(--primary-color, #4b7bff);
  color: var(--primary-color, #4b7bff);
}

.pill--value-only {
  padding: 2px var(--space-sm);
}

.summary-effects {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-quality {
  min-width: 36px;
  text-align: center;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  font-size: var(--font-xs);
  font-weight: var(--font-semibold);
}

@media (max-width: 1080px) {
  .equipment-detail-layout {
    grid-template-columns: 1fr;
  }

  .selected-equipment-panel {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .equipment-card {
    padding: 18px;
  }

  .equipment-card__header {
    flex-direction: column;
  }

  .core-row {
    grid-template-columns: 1fr;
  }

  .equipment-summary-card {
    grid-template-columns: 1fr;
  }

  .summary-quality {
    justify-self: flex-start;
  }
}
</style>
