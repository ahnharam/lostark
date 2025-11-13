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
              :key="item.name"
              class="equipment-summary-card"
              :class="{ active: item.name === selectedEquipment?.name }"
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
                <span
                  v-if="hasValidQuality(getParsedEquipment(item)?.quality)"
                  class="summary-quality"
                  :style="{ borderColor: getQualityBadgeColor(item), color: getQualityBadgeColor(item) }"
                >
                  {{ getQualityDisplayValue(getParsedEquipment(item)?.quality) }}
                </span>
              </div>
              <div class="summary-body">
                <div class="summary-headline">
                  <h5>{{ item.name }}</h5>
                  <span v-if="getParsedEquipment(item)?.itemLevel" class="summary-level">
                    iLv. {{ getParsedEquipment(item)?.itemLevel }}
                  </span>
                </div>
                <div class="summary-meta">
                  <span>{{ item.type }}</span>
                  <span :style="{ color: getParsedEquipment(item)?.gradeColor || undefined }">
                    {{ item.grade }}
                  </span>
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
                  width="80"
                  height="80"
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
                  <div v-for="(row, idx) in coreRows" :key="`core-row-${idx}`" class="core-row">
                    <div class="core-cell" @mouseenter="showHighlightTooltip(row.left)" @mouseleave="clearHighlightTooltip">
                      <span class="cell-label">{{ row.left.label }}</span>
                      <span class="cell-value">{{ row.left.value }}</span>
                    </div>
                    <div class="core-cell" @mouseenter="showHighlightTooltip(row.right)" @mouseleave="clearHighlightTooltip">
                      <span class="cell-label">{{ row.right.label }}</span>
                      <span class="cell-value">{{ row.right.value }}</span>
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import LazyImage from './LazyImage.vue'
import LoadingSpinner from './LoadingSpinner.vue'
import {
  parseTooltip,
  type ParsedTooltip,
  type StatItem,
  type TranscendenceAggregates,
  getQualityColor,
  getGradeColor
} from '@/utils/tooltipParser'
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

interface EquipmentPanel {
  key: 'armor' | 'accessory'
  label: string
  description?: string
  items: Equipment[]
}

const props = withDefaults(defineProps<Props>(), {
  character: null,
  equipment: () => [],
  engravings: () => [],
  loading: false,
  errorMessage: null
})

const specialEquipmentKeywords = ['나침반', '부적', '보주', '문장']
const accessoryTypeKeywords = ['목걸이', '귀걸이', '반지', '팔찌', '돌', '어빌리티', '능력', '보석']

const isSpecialEquipment = (item: Equipment) => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return specialEquipmentKeywords.some(keyword => target.includes(keyword.toLowerCase()))
}

const isAccessoryEquipment = (item: Equipment) => {
  const typeText = (item.type ?? '').toLowerCase()
  return accessoryTypeKeywords.some(keyword => typeText.includes(keyword.toLowerCase()))
}

const equipmentList = computed(() => props.equipment.filter(item => !isSpecialEquipment(item)))
const armorItems = computed(() => equipmentList.value.filter(item => !isAccessoryEquipment(item)))
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

const selectedEquipmentName = ref<string | null>(null)

watch(
  () => props.equipment,
  newList => {
    const filtered = equipmentList.value
    if (!filtered.length) {
      selectedEquipmentName.value = null
      return
    }
    if (!selectedEquipmentName.value || !filtered.some(item => item.name === selectedEquipmentName.value)) {
      selectedEquipmentName.value = filtered[0].name
    }
  },
  { immediate: true }
)

const selectEquipment = (item: Equipment) => {
  selectedEquipmentName.value = item.name
}

const parsedEquipment = computed<Record<string, ParsedTooltip>>(() => {
  const map: Record<string, ParsedTooltip> = {}
  props.equipment.forEach(item => {
    if (item.tooltip) {
      map[item.name] = parseTooltip(item.tooltip)
    }
  })
  return map
})

const getParsedEquipment = (item: Equipment) => parsedEquipment.value[item.name]

const selectedEquipment = computed(() => {
  const list = equipmentList.value
  if (!list.length) return null
  return list.find(item => item.name === selectedEquipmentName.value) ?? list[0]
})

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

const findStatByKeywords = (keywords: string[]) =>
  allStats.value.find(stat => keywords.some(keyword => stat.type.includes(keyword)))

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
  const buckets = tooltipValueMap.value[selectedEquipment.value.name]
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

const sangjaeInfo = computed(() => {
  const parsed = selectedParsed.value
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
})

interface CoreCell {
  label: string
  value: string
  tooltipLines?: string[]
  tooltipTotals?: TranscendenceAggregates
}

interface CoreRow {
  left: CoreCell
  right: CoreCell
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

const coreRows = computed<CoreRow[]>(() => {
  additionalEffectLines.value
  const rows: CoreRow[] = []
  rows.push({
    left: {
      label: '무기 공격력',
      value: weaponAttackStat.value ? formatStatValue(weaponAttackStat.value.value) : '-'
    },
    right: {
      label: '추가 피해',
      value: additionalEffectStat.value ? formatStatValue(additionalEffectStat.value.value) : '-'
    }
  })

  const sangjae = sangjaeInfo.value
  rows.push({
    left: {
      label: '상재 단계',
      value: sangjae.stage !== undefined ? formatPlainNumber(sangjae.stage) : '-'
    },
    right: {
      label: '상재 수치',
      value: sangjae.value ? formatNumbersInText(sangjae.value) : '-'
    }
  })

  const slot = slotEffectInfo.value
  const stageLines = selectedParsed.value?.transcendenceStages
  const totals = selectedParsed.value?.transcendenceAggregates

  rows.push({
    left: {
      label: '초월 단계',
      value: slot?.stage !== undefined ? formatPlainNumber(slot.stage) : '-',
      tooltipLines: stageLines,
      tooltipTotals: totals
    },
    right: {
      label: '초월 수치',
      value: slot?.value !== undefined ? formatPlainNumber(slot.value) : '-',
      tooltipLines: stageLines,
      tooltipTotals: totals
    }
  })
  return rows
})

const tooltipValueMap = computed<Record<string, TooltipValueBuckets>>(() => {
  const map: Record<string, TooltipValueBuckets> = {}
  props.equipment.forEach(item => {
    const values = extractTooltipValues(item)
    map[item.name] = categorizeTooltipValues(values)
  })
  return map
})

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

const tradeInfoLines = computed(() => {
  if (!selectedEquipment.value) return []
  const buckets = tooltipValueMap.value[selectedEquipment.value.name]
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
    .filter(line => line.includes('거래'))
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

const getSummaryLines = (item: Equipment) => {
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
    lines.push(parsed.engravingEffects[0])
  }
  const sanitized = lines.filter(line => !/상재/.test(line))
  return sanitized.slice(0, 2)
}

const getQualityBadgeColor = (item: Equipment) => getQualityColor(getParsedEquipment(item)?.quality)

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
  font-size: 1rem;
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
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.global-summary__item .value {
  font-weight: 600;
  color: var(--text-primary);
}

.equipment-detail-layout {
  display: grid;
  grid-template-columns: 230px 230px minmax(520px, 1fr) ;
  gap: 20px;
  align-items: start;
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
  width: 88px;
  height: 88px;
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
  font-size: 2rem;
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
  font-size: 1.25rem;
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
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.chip--quality {
  border: 1px solid currentColor;
  background: transparent;
}

.chip--sangjae {
  border: 1px solid currentColor;
  background: transparent;
}

.item-level-inline {
  margin-left: 8px;
  font-size: 0.9rem;
  color: var(--primary-color);
}

.core-stats-section {
  background: var(--bg-secondary);
  border-radius: 14px;
  padding: 18px;
  border: 1px solid var(--border-color);
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

.core-cell {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 12px;
  padding: 6px 0;
  border-bottom: 1px solid var(--border-color-light);
}

.core-cell .cell-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.core-cell .cell-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.trans-tooltip {
  margin-top: 8px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 14px;
  box-shadow: var(--shadow-sm);
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.trans-tooltip p {
  margin: 4px 0;
  line-height: 1.4;
}

.core-empty {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 0.85rem;
}

.trade-status-line {
  margin: 12px 0 0;
  font-size: 0.9rem;
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
  font-size: 0.85rem;
}

.equipment-empty {
  margin: 0;
  padding: 12px;
  border-radius: 10px;
  border: 1px dashed var(--border-color);
  font-size: 0.85rem;
  color: var(--text-secondary);
  background: var(--bg-secondary);
}

.count-pill {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--bg-secondary);
  font-size: 0.85rem;
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
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s, background 0.2s;
  background: var(--bg-secondary);
}

.equipment-summary-card.active {
  border-color: var(--primary-color);
  background: rgba(102, 126, 234, 0.15);
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
  font-size: 1.25rem;
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
  font-size: 0.85rem;
}

.summary-level {
  display: inline-flex;
  font-size: 0.85rem;
  color: #4b7bff;
  font-weight: 600;
}

.summary-meta {
  display: flex;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.summary-effects {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.summary-quality {
  min-width: 36px;
  text-align: center;
  padding: 4px 6px;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  font-size: 0.75rem;
  font-weight: 600;
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
