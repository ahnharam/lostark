<template>
  <div class="ark-grid-shell">
    <div v-if="loading" class="ark-grid-placeholder">
      <LoadingSpinner message="아크 그리드 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="ark-grid-placeholder">
      <ErrorMessage
        title="아크 그리드를 불러올 수 없어요"
        :message="errorMessage"
        :retry="true"
        :dismissible="false"
        @retry="$emit('retry')"
      />
    </div>
    <div v-else-if="!hasRenderableContent" class="ark-grid-placeholder">
      <EmptyState
        icon="🌀"
        title="아크 그리드 데이터가 감지되지 않았어요"
        :description="emptyStateDescription"
      >
        <button v-if="characterName" type="button" class="ark-grid-retry" @click="$emit('retry')">
          다시 불러오기
        </button>
      </EmptyState>
    </div>
    <div v-else class="ark-grid-layout">
      <section class="ark-grid-overview">
        <div>
          <p class="ark-grid-label">현재 루트</p>
          <h3>{{ arkPassiveTitle }}</h3>
        </div>
        <ul v-if="pointSummary.length" class="ark-grid-point-list">
          <li v-for="point in pointSummary" :key="point.name" class="ark-grid-point">
            <span class="point-label">{{ point.name }}</span>
            <strong class="point-value">{{ formatPointValue(point.value) }}</strong>
            <span v-if="point.description" class="point-description">{{ sanitizeInline(point.description) }}</span>
          </li>
        </ul>
      </section>

      <section v-if="passiveMatrix.length" class="ark-grid-passives">
        <div class="section-heading">
          <h4>아크 패시브 계층</h4>
        </div>
        <div class="passive-matrix">
          <div class="matrix-header">
            <div class="matrix-cell matrix-cell--tier">티어</div>
            <div
              v-for="section in PASSIVE_SECTIONS"
              :key="section.key"
              class="matrix-cell"
            >
              {{ section.label }}
            </div>
          </div>
          <div v-for="row in passiveMatrix" :key="row.id" class="matrix-row">
            <div class="matrix-cell matrix-cell--tier">{{ row.label }}</div>
            <div
              v-for="section in PASSIVE_SECTIONS"
              :key="`${row.id}-${section.key}`"
              class="matrix-cell matrix-cell--section"
            >
              <article
                v-for="effect in row.columns[section.key]"
                :key="effect.key"
                class="passive-card passive-table-card"
              >
                <div class="passive-card-head">
                  <div class="passive-card-visual" :title="effect.tooltipText">
                    <LazyImage
                      v-if="effect.icon"
                      :src="effect.icon"
                      :alt="effect.name || '아크 패시브 아이콘'"
                      width="44"
                      height="44"
                      imageClass="passive-card-icon"
                      errorIcon="✨"
                      :useProxy="true"
                    />
                    <span v-if="effect.levelDisplay" class="passive-card-level">{{ effect.levelDisplay }}</span>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>

      <section v-if="slotCards.length" class="ark-grid-slots">
        <div class="section-heading">
          <div>
            <h4>코어 & 젬 네트워크</h4>
          </div>
        </div>
        <div class="slot-card-grid">
          <article v-for="slot in slotCards" :key="slot.index" class="slot-card">
            <header class="slot-card-head">
              <LazyImage
                v-if="slot.icon"
                :src="slot.icon"
                :alt="slot.name || '아크 코어'"
                width="45"
                height="45"
                imageClass="slot-card-icon"
                errorIcon="🧱"
                :useProxy="true"
              />
              <div class="slot-card-head-title">
                <div class="slot-card-type">
                  <p class="slot-card-grade" :style="{ color: slot.gradeColor || undefined }">{{ slot.grade || '등급 미상' }}</p>
                  <span v-if="slot.point !== undefined" class="slot-card-point">{{ slot.point }}P</span>
                </div>
                <strong class="slot-card-name">{{ slot.name }}</strong>
              </div>
            </header>
            <!-- <p v-if="slot.tooltipTitle" class="slot-card-title">{{ slot.tooltipTitle }}</p> -->
            <div v-if="slot.tooltipLines.length" class="slot-tooltip-list">
              <div
                v-for="(line, idx) in slot.tooltipLines"
                :key="`slot-line-${slot.index}-${idx}`"
                class="slot-tooltip-line"
                :class="{
                  'slot-tooltip-line--highlighted': line.highlighted,
                  'slot-tooltip-line--locked': line.hasThreshold && !line.highlighted
                }"
              >
                <span v-if="line.pointLabel" class="slot-tooltip-point">{{ line.pointLabel }}</span>
                <span class="slot-tooltip-body">{{ line.text }}</span>
              </div>
            </div>
            <div v-if="slot.gemCards.length" class="slot-gem-stack">
              <div v-for="gem in slot.gemCards" :key="`gem-${slot.index}-${gem.index}`" class="gem-card">
                <div class="gem-card-head">
                  <LazyImage
                    v-if="gem.icon"
                    :src="gem.icon"
                    :alt="gem.title || '젬'"
                    width="30"
                    height="30"
                    imageClass="gem-card-icon"
                    errorIcon="💠"
                    :useProxy="true"
                  />
                  <div>
                    <p class="gem-card-grade" :style="{ color: gem.gradeColor || undefined }">{{ gem.grade || '젬' }}</p>
                    <!-- <strong class="gem-card-name">{{ gem.title || `젬 ${gem.index ?? ''}` }}</strong> -->
                  </div>
                </div>
                <div v-if="gem.badges.length" class="gem-badge-grid">
                  <span
                    v-for="(badge, idx) in gem.badges"
                    :key="`gem-badge-${slot.index}-${gem.index}-${idx}`"
                    class="gem-badge"
                    :title="badge.fullText"
                  >
                    {{ badge.text }}
                  </span>
                </div>
                <div v-if="gem.gemTooltipText" class="gem-tooltip-hover">
                  {{ gem.gemTooltipText }}
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <section v-if="gridEffects.length" class="ark-grid-effects">
        <div class="section-heading">
          <h4>추가 효과</h4>
        </div>
        <div class="effect-list">
          <div v-for="effect in gridEffects" :key="`${effect.name}-${effect.level}`" class="effect-item">
            <span v-if="effect.level" class="effect-level">Lv. {{ effect.level }}</span>
            <strong>{{ sanitizeInline(effect.tooltip) }}</strong>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'
import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LazyImage from './LazyImage.vue'
import { stripHtml } from '@/utils/tooltipParser'
import type { ArkGridResponse, ArkGridSlot, ArkPassiveEffect, ArkPassivePoint } from '@/api/types'

const props = defineProps<{
  response: ArkGridResponse | null
  loading: boolean
  errorMessage: string | null
  characterName: string
}>()

defineEmits<{
  (e: 'retry'): void
}>()

const arkPassive = computed(() => props.response?.arkPassive ?? null)
const arkGrid = computed(() => props.response?.arkGrid ?? null)

interface PassiveCard {
  key: string
  name: string
  icon?: string
  tierLabel: string
  levelLine: string
  summaryLine: string
  levelDisplay: string
  tooltipText: string
}

interface PassiveSection {
  key: 'evolution' | 'realization' | 'leap'
  label: string
  keyword: string
}

type PassiveSectionKey = PassiveSection['key']

interface PassiveMatrixRow {
  id: string
  label: string
  columns: Record<PassiveSectionKey, PassiveCard[]>
}

interface SlotTooltipLine {
  text: string
  highlighted: boolean
  hasThreshold: boolean
  pointLabel?: string
}

const PASSIVE_SECTIONS: readonly PassiveSection[] = [
  { key: 'evolution', label: '진화', keyword: '진화' },
  { key: 'realization', label: '깨달음', keyword: '깨달음' },
  { key: 'leap', label: '도약', keyword: '도약' }
]
const PASSIVE_SECTION_KEYWORDS = PASSIVE_SECTIONS.map(section => section.keyword)
const PASSIVE_SECTION_PATTERN = PASSIVE_SECTION_KEYWORDS.join('|')
const PASSIVE_SECTION_REGEX = new RegExp(`(${PASSIVE_SECTION_PATTERN})`, 'g')
const PASSIVE_SECTION_PREFIX_REGEX = new RegExp(`^\\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?\\s*([·:\\-]+)?\\s*`, 'g')
const PASSIVE_SECTION_SUFFIX_REGEX = new RegExp(`\\s*([·:\\-]+)?\\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?$`, 'g')

const ROMAN_NUMERAL_MAP: Record<string, number> = {
  I: 1,
  V: 5,
  X: 10,
  L: 50,
  C: 100,
  D: 500,
  M: 1000
}

interface ParsedTooltip {
  title: string
  lines: string[]
}

const sanitizeInline = (value?: string | null) => {
  if (!value) return ''
  return stripHtml(value)
    .replace(/\\r\\n|\\n|\\r/g, ' ')
    .replace(/\\s+/g, ' ')
    .trim()
}

const stripStageKeywords = (value?: string | null) => {
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

const normalizeTooltipText = (value: string) => {
  return value
    .replace(/\\r\\n/g, '\n')
    .replace(/\\n/g, '\n')
    .replace(/\\r/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\n/g, '\n')
    .replace(/\|/g, '\n')
}

const flattenTooltip = (value: unknown, bucket: string[]) => {
  if (!value) return
  if (typeof value === 'string') {
    normalizeTooltipText(value)
      .split('\\n')
      .map(line => stripHtml(line).trim())
      .filter(Boolean)
      .forEach(line => bucket.push(line))
    return
  }
  if (Array.isArray(value)) {
    value.forEach(item => flattenTooltip(item, bucket))
    return
  }
  if (typeof value === 'object') {
    Object.values(value).forEach(item => flattenTooltip(item, bucket))
  }
}

const parseTooltip = (tooltip?: string | null): ParsedTooltip => {
  if (!tooltip) {
    return { title: '', lines: [] }
  }
  const bucket: string[] = []
  try {
    const parsed = JSON.parse(tooltip)
    flattenTooltip(parsed, bucket)
  } catch {
    flattenTooltip(tooltip, bucket)
  }
  const sanitized = bucket.map(line =>
    line
      .replace(/\\s+/g, ' ')
      .trim()
  )
  const [title, ...rest] = sanitized
  return {
    title: title || '',
    lines: rest
  }
}

interface GemBadge {
  text: string
  fullText: string
}

interface GemTooltipData {
  badges: GemBadge[]
  tooltipText: string
}

const parseGemDescriptionToBadges = (tooltip?: string | null): GemTooltipData => {
  const result: GemTooltipData = {
    badges: [],
    tooltipText: ''
  }

  if (!tooltip) return result

  const badges: GemBadge[] = []
  const tooltipLines: string[] = []

  try {
    const parsed = JSON.parse(tooltip)

    // "젬 효과" Element 찾기
    let gemEffectText = ''
    for (const key in parsed) {
      const element = parsed[key]
      if (element?.type === 'ItemPartBox' && element?.value) {
        const headerText = typeof element.value.Element_000 === 'string'
          ? stripHtml(element.value.Element_000)
          : ''
        if (headerText.includes('젬 효과')) {
          gemEffectText = element.value.Element_001 || ''
          break
        }
      }
    }

    if (!gemEffectText) return result

    // <br> 태그로 분리하여 각 라인 처리
    const segments = gemEffectText.split(/<br\s*\/?>/i).map(segment =>
      stripHtml(segment).trim()
    ).filter(Boolean)

    let i = 0
    while (i < segments.length) {
      const segment = segments[i]

      // 1. 필요 의지력 패턴
      const willpowerMatch = segment.match(/필요\s*의지력\s*:\s*(\d+)/i)
      if (willpowerMatch) {
        const text = `의지력 ${willpowerMatch[1]}`
        badges.push({ text, fullText: text })
        tooltipLines.push(text)
        i++
        continue
      }

      // 2. 질서/혼돈 포인트 패턴
      const pointMatch = segment.match(/(질서|혼돈)\s*포인트\s*:\s*(\d+)/i)
      if (pointMatch) {
        const text = `${pointMatch[1]} 포인트 ${pointMatch[2]}`
        badges.push({ text, fullText: text })
        tooltipLines.push(text)
        i++
        continue
      }

      // 3. 효과 이름과 레벨 패턴 (예: [추가 피해] Lv.1)
      const effectNameMatch = segment.match(/\[([^\]]+)\]\s*Lv\.(\d+)/i)
      if (effectNameMatch) {
        const effectName = effectNameMatch[1].trim()
        const levelNum = effectNameMatch[2]

        // 다음 라인에서 효과 값 찾기
        if (i + 1 < segments.length) {
          const nextSegment = segments[i + 1]
          // 값 패턴 찾기 (예: +0.08%, +0.11%)
          const valueMatch = nextSegment.match(/([\+\-]\d+(?:\.\d+)?%)/i)
          if (valueMatch) {
            const badgeText = `${effectName} ${levelNum}`
            const fullText = `${effectName} Lv.${levelNum} ${valueMatch[1]}`
            badges.push({ text: badgeText, fullText })
            tooltipLines.push(fullText)
            i += 2 // 다음 라인도 건너뜀
            continue
          }
        }

        // 값이 없으면 레벨만 표시
        const text = `${effectName} ${levelNum}`
        badges.push({ text, fullText: text })
        tooltipLines.push(text)
        i++
        continue
      }

      i++
    }

    result.badges = badges
    result.tooltipText = tooltipLines.join('\n')
  } catch (error) {
    console.error('Failed to parse gem tooltip:', error)
  }

  return result
}

const extractColorFromTooltip = (tooltip?: string | null): string | null => {
  if (!tooltip) return null

  try {
    const parsed = JSON.parse(tooltip)
    const extractColorFromObject = (obj: any): string | null => {
      if (typeof obj === 'string') {
        // <FONT color='#COLOR'> 패턴 찾기
        const colorMatch = obj.match(/<FONT[^>]*color=['"]?([#\w]+)['"]?[^>]*>/i)
        if (colorMatch) return colorMatch[1]
      } else if (Array.isArray(obj)) {
        for (const item of obj) {
          const color = extractColorFromObject(item)
          if (color) return color
        }
      } else if (typeof obj === 'object' && obj !== null) {
        for (const value of Object.values(obj)) {
          const color = extractColorFromObject(value)
          if (color) return color
        }
      }
      return null
    }
    return extractColorFromObject(parsed)
  } catch {
    // JSON 파싱 실패 시 직접 문자열에서 찾기
    const colorMatch = tooltip.match(/<FONT[^>]*color=['"]?([#\w]+)['"]?[^>]*>/i)
    return colorMatch ? colorMatch[1] : null
  }
}

const parsePointSummary = (points?: ArkPassivePoint[]) => {
  return (points ?? []).filter(point => point.name)
}

const pointSummary = computed(() => parsePointSummary(arkPassive.value?.points))

const arkPassiveTitle = computed(() => sanitizeInline(arkPassive.value?.title) || '루트 정보 미상')

const buildPassiveCard = (effect: ArkPassiveEffect, index: number): PassiveCard => {
  const tooltip = parseTooltip(effect.toolTip)
  const tierLabel = sanitizeInline(effect.description)
  const levelLine =
    tooltip.lines.find(line => line.includes('아크 패시브 레벨')) || tooltip.title || '아크 패시브 레벨 정보 없음'
  const lastLineCandidate = tooltip.lines.length ? tooltip.lines[tooltip.lines.length - 1] : tooltip.title
  const summaryLine =
    lastLineCandidate && lastLineCandidate !== levelLine ? lastLineCandidate : tooltip.lines.find(line => line !== levelLine) || ''

  const name = sanitizeInline(effect.name)
  const displayName = stripStageKeywords(name) || name
  const levelValueMatch = levelLine.match(/(\d+)/)
  const levelValue = levelValueMatch ? Number(levelValueMatch[1]) : null
  const levelDisplay = levelValue ? `레벨 ${levelValue}` : ''
  const tooltipChunks = [levelLine, summaryLine].filter((line, idx, arr) => {
    if (!line) return false
    if (idx > 0 && line === arr[idx - 1]) return false
    return true
  })
  const tooltipText = tooltipChunks.join('\n') || '효과 정보 없음'

  return {
    key: `${effect.name}-${index}`,
    name: displayName,
    icon: effect.icon,
    tierLabel,
    levelLine,
    summaryLine,
    levelDisplay,
    tooltipText
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
    const current = ROMAN_NUMERAL_MAP[chars[i]]
    if (current < previous) {
      total -= current
    } else {
      total += current
      previous = current
    }
  }
  return total
}

const extractTierGroupLabel = (card: PassiveCard) => {
  const candidates = [card.tierLabel, card.levelLine]
  for (const candidate of candidates) {
    const normalized = normalizeTierText(candidate)
    if (!normalized) continue
    const digitMatch = normalized.match(/(\d+)/)
    if (digitMatch) {
      return `${digitMatch[1]}티어`
    }
    const romanMatch = normalized.match(/\b([IVXLCDM]+)\b/i)
    if (romanMatch) {
      const numericValue = romanToNumber(romanMatch[1])
      if (numericValue) {
        return `${numericValue}티어`
      }
      return `티어 ${romanMatch[1].toUpperCase()}`
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

const passiveMatrix = computed<PassiveMatrixRow[]>(() => {
  const effects = arkPassive.value?.effects ?? []
  const rows: PassiveMatrixRow[] = []
  const rowMap = new Map<string, PassiveMatrixRow>()

  const createEmptyColumns = () => {
    return PASSIVE_SECTIONS.reduce<Record<string, PassiveCard[]>>((acc, section) => {
      acc[section.key] = []
      return acc
    }, {})
  }

  const resolveSection = (card: PassiveCard) =>
    PASSIVE_SECTIONS.find(section => card.tierLabel.includes(section.keyword)) ??
    PASSIVE_SECTIONS.find(section => card.levelLine.includes(section.keyword)) ??
    PASSIVE_SECTIONS[0]

  effects.forEach((effect, index) => {
    const card = buildPassiveCard(effect, index)
    const section = resolveSection(card)
    const rowLabel = extractTierGroupLabel(card)
    let row = rowMap.get(rowLabel)
    if (!row) {
      row = {
        id: `${rowLabel}-${rows.length}`,
        label: rowLabel,
        columns: createEmptyColumns()
      }
      rowMap.set(rowLabel, row)
      rows.push(row)
    }
    row.columns[section.key].push(card)
  })

  return rows
})

const CORE_OPTION_PATTERN = /코어\s*옵션/i
const ITEM_PART_BOX_PATTERN = /itempartbox/i
const GEM_EFFECT_PATTERN = /젬\s*효과/i
const INDENT_STRING_GROUP_PATTERN = /indentstringgroup/i
const CHAOS_CORE_PATTERN = /혼돈.*코어/i
const SINGLE_TEXT_BOX_PATTERN = /singletextbox/i
const POINT_VALUE_REGEX = /\[(\d+)\s*P\]/i

const extractCoreOptionLines = (lines: string[]) => {
  if (!lines || !lines.length) return []
  const normalized = lines.map(line => line.trim()).filter(Boolean)
  if (!normalized.length) return []
  const startIndex = normalized.findIndex(line => CORE_OPTION_PATTERN.test(line))
  if (startIndex === -1) {
    return normalized
  }
  let boundaryIndex = normalized.slice(startIndex + 1).findIndex(line => ITEM_PART_BOX_PATTERN.test(line))
  if (boundaryIndex !== -1) {
    boundaryIndex += startIndex + 1
  } else {
    boundaryIndex = normalized.length
  }
  const sliced = normalized
    .slice(startIndex + 1, boundaryIndex)
    .filter(line => !ITEM_PART_BOX_PATTERN.test(line))
  return sliced.length ? sliced : normalized
}

const extractGemEffectLines = (lines: string[]) => {
  if (!lines || !lines.length) return []
  const normalized = lines.map(line => line.trim()).filter(Boolean)
  if (!normalized.length) return []
  const startIndex = normalized.findIndex(line => GEM_EFFECT_PATTERN.test(line))
  if (startIndex === -1) {
    return normalized
  }
  let boundaryIndex = normalized.slice(startIndex + 1).findIndex(line => INDENT_STRING_GROUP_PATTERN.test(line))
  if (boundaryIndex !== -1) {
    boundaryIndex += startIndex + 1
  } else {
    boundaryIndex = normalized.length
  }
  const sliced = normalized
    .slice(startIndex + 1, boundaryIndex)
    .filter(line => !INDENT_STRING_GROUP_PATTERN.test(line))
  return sliced.length ? sliced : normalized
}

const trimChaosCoreLines = (lines: string[], slotName?: string) => {
  if (!lines || !lines.length) return []
  if (!CHAOS_CORE_PATTERN.test(slotName ?? '')) {
    return lines
  }
  const cutoffIndex = lines.findIndex(line => SINGLE_TEXT_BOX_PATTERN.test(line))
  if (cutoffIndex === -1) {
    return lines
  }
  return lines.slice(0, cutoffIndex).filter(Boolean)
}

const normalizePointValue = (value?: number | string | null) => {
  if (typeof value === 'number') return value
  if (typeof value === 'string') {
    const parsed = Number(value.replace(/[^\d.]/g, ''))
    return Number.isNaN(parsed) ? null : parsed
  }
  return null
}

const extractPointFromLine = (line: string) => {
  const match = line.match(POINT_VALUE_REGEX)
  if (!match || match.index === undefined) return null
  if (match.index > 6) return null
  return Number(match[1])
}

const splitPointLabel = (line: string) => {
  const match = line.match(POINT_VALUE_REGEX)
  if (!match || match.index === undefined) {
    return { label: '', body: line }
  }
  const label = match[0].trim()
  const labelStart = match.index
  if (labelStart > 6) {
    return { label: '', body: line }
  }
  const prefix = line
    .slice(0, labelStart)
    .replace(/^[\s•·\-\*()\[\]]+/, '')
  const suffix = line.slice(labelStart + match[0].length).trimStart()
  const body = `${prefix} ${suffix}`.trim()
  return { label, body: body || suffix || line }
}

const splitLinesByPointPattern = (lines: string[]) => {
  const result: string[] = []

  lines.forEach(line => {
    if (!line.trim()) return

    // [nP] 패턴을 모두 찾기
    const regex = /\[(\d+)\s*P\]/gi
    const matches: Array<{ index: number; match: string }> = []
    let match

    while ((match = regex.exec(line)) !== null) {
      matches.push({ index: match.index, match: match[0] })
    }

    // [nP] 패턴이 없으면 원본 라인 그대로 추가
    if (matches.length === 0) {
      result.push(line)
      return
    }

    // 첫 번째 [nP] 이전에 텍스트가 있으면 맨 앞에 추가
    const firstMatch = matches[0]
    if (firstMatch && firstMatch.index > 0) {
      const prefix = line.slice(0, firstMatch.index).trim()
      if (prefix) {
        result.push(prefix)
      }
    }

    // [nP] 패턴이 있으면 각 패턴을 기준으로 분할
    matches.forEach((m, idx) => {
      const start = m.index
      const nextMatch = matches[idx + 1]
      const end = nextMatch ? nextMatch.index : line.length
      const segment = line.slice(start, end).trim()
      if (segment) {
        result.push(segment)
      }
    })
  })

  return result
}
const slotCards = computed(() => {
  return (arkGrid.value?.slots ?? []).map((slot: ArkGridSlot) => {
    const tooltip = parseTooltip(slot.tooltip)
    const slotColor = extractColorFromTooltip(slot.tooltip)
    const gemCards =
      slot.gems?.map(gem => {
        const gemTooltip = parseTooltip(gem.tooltip)
        const gemColor = extractColorFromTooltip(gem.tooltip)
        const gemData = parseGemDescriptionToBadges(gem.tooltip)
        return {
          ...gem,
          title: gemTooltip.title,
          tooltipLines: extractGemEffectLines(gemTooltip.lines),
          badges: gemData.badges,
          gemTooltipText: gemData.tooltipText,
          gradeColor: gemColor
        }
      }) ?? []
    const coreTooltipLines = trimChaosCoreLines(extractCoreOptionLines(tooltip.lines), slot.name)
    // [nP] 패턴을 기준으로 라인을 추가로 분할
    const splitLines = splitLinesByPointPattern(coreTooltipLines)
    const normalizedPoint = normalizePointValue(slot.point)
    const tooltipLines: SlotTooltipLine[] = splitLines.map(line => {
      const { label, body } = splitPointLabel(line)
      const linePoint = extractPointFromLine(line)
      // 포인트 요구사항이 없으면 항상 활성화, 있으면 코어 포인트와 비교
      const highlighted = linePoint === null || (normalizedPoint !== null && linePoint <= normalizedPoint)
      return {
        text: body,
        pointLabel: label,
        highlighted,
        hasThreshold: linePoint !== null
      }
    })
    return {
      ...slot,
      tooltipTitle: tooltip.title,
      tooltipLines,
      gemCards,
      gradeColor: slotColor
    }
  })
})

const gridEffects = computed(() => arkGrid.value?.effects ?? [])

const hasRenderableContent = computed(() => {
  return (
    pointSummary.value.length > 0 ||
    passiveMatrix.value.length > 0 ||
    slotCards.value.length > 0 ||
    gridEffects.value.length > 0
  )
})

const formatPointValue = (value?: number | null) => {
  if (typeof value !== 'number') return '0P'
  return `${value}P`
}

const emptyStateDescription = computed(() => {
  if (!props.characterName) {
    return '캐릭터를 검색하면 아크 패시브와 코어 구성이 여기에 표시됩니다.'
  }
  return `${props.characterName}의 아크 그리드 기록이 아직 없거나 비공개 상태입니다.`
})

</script>

<style scoped>
.ark-grid-shell {
  width: 100%;
  color: var(--text-primary, #1f2937);
}

.ark-grid-placeholder {
  padding: 32px;
  border: 1px dashed var(--border-color, #d5d5d5);
  border-radius: 16px;
  background: var(--card-bg, #f9fafb);
}

.ark-grid-retry {
  margin-top: 16px;
  padding: 8px 16px;
  border-radius: 999px;
  background: var(--primary-color, #3b82f6);
  color: var(--text-inverse, #ffffff);
  font-weight: 600;
  border: none;
  cursor: pointer;
}

.ark-grid-layout {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.ark-grid-overview {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  width: 100%;
  gap: 100px;
}

.ark-grid-label {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 4px;
}

.ark-grid-overview h3 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary, #1f2937);
}

.ark-grid-subtext {
  margin: 4px 0 0;
  color: var(--text-muted, #6b7280);
}

.ark-grid-point-list {
  display: inline-flex;
  gap: 70px;
  width:fit-content;
  text-align: center;
}

.ark-grid-point {
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.point-label {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.point-value {
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.point-description {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-heading h4 {
  margin: 0 0 4px;
  font-size: 1.1rem;
  color: var(--text-primary, #1f2937);
}

.section-heading p {
  margin: 0;
  color: var(--text-muted, #6b7280);
}

.ark-grid-passives,
.ark-grid-slots,
.ark-grid-effects {
  background: var(--card-bg, #fbfbfb);
}

.passive-matrix {
  margin-top: 10px;
  /* border: 1px solid var(--border-color, #e5e7eb); */
  border-radius: 16px;
  overflow: hidden;
}

.matrix-header,
.matrix-row {
  display: grid;
  grid-template-columns: 60px repeat(3, minmax(0, 1fr));
}

.matrix-header {
  /* background: var(--surface-muted, #f3f4f6); */
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  font-weight: 600;
  color: var(--text-muted, #6b7280);
  text-align: center;
}

.matrix-row:not(:last-child) {
  border-bottom: 1px solid var(--border-color, #e5e7eb);
}

.matrix-cell {
  padding: 4px;
  border-right: 1px solid var(--border-color, #e5e7eb);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.matrix-cell:last-child {
  border-right: none;
}

.matrix-cell--section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, auto));
  justify-content: center;
  gap: 12px;
}

.matrix-cell--section .passive-card {
  width: 100%;
}

.matrix-cell--tier {
  justify-content: center;
  align-items: center;
  font-weight: 600;
  color: var(--text-muted, #6b7280);
  text-align: center;
}

.passive-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.passive-table-card {
  margin: 0;
  border-radius: 12px;
}

.passive-card-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.passive-card-visual {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 48px;
  cursor: help;
}

.passive-card-icon {
  border-radius: 12px;
}

.passive-card-level {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--primary-color, #2563eb);
  line-height: 1.2;
}

.passive-card-tier {
  font-size: 0.8rem;
  color: var(--text-muted, #6b7280);
  margin: 0;
}

.passive-card-name {
  font-size: 1rem;
  color: var(--text-primary, #1f2937);
}

.passive-card-description {
  margin: 0;
  color: var(--text-primary, #1f2937);
  font-size: calc(1rem - 2px);
}

.passive-card-summary {
  margin: 0;
  color: var(--text-muted, #6b7280);
  font-size: calc(0.9rem - 2px);
}

.slot-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 18px;
  margin-top: 10px;
}

.slot-card {
  /* background: var(--surface-color, #fff); */
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.slot-card-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.slot-card-icon {
  border-radius: 12px;
}

.slot-card-head-title{
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.slot-card-type{
  display: flex;
  flex-direction: row;
  gap: 10px;
}

.slot-card-grade {
  margin: 0;
  font-size: 0.7rem;
  color: var(--text-muted, #6b7280);
  background: var(--primary-soft-bg, rgba(59, 130, 246, 0.12));
  padding: 2px 8px;
  border-radius: 999px;
}

.slot-card-name {
  display: block;
  font-size: 0.9rem;
  color: var(--text-primary, #1f2937);
}

.slot-card-point {
  display: inline-flex;
  background: var(--primary-soft-bg, rgba(59, 130, 246, 0.12));
  color: var(--primary-color, #2563eb);
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.8rem;
  font-weight: 600;
}

.slot-card-title {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-muted, #6b7280);
}

.slot-tooltip-list,
.gem-tooltip-list {
  display: grid;
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-primary, #1f2937);
}

.gem-badge-grid {
  /* display: grid;
  grid-template-columns: repeat(2, 90px); */
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 0;
  width: 200px;
}

.gem-badge {
  display: inline-block;
  padding: 3px 5px;
  font-size: 0.7rem;
  font-weight: 500;
  color: var(--text-primary, #1f2937);
  /* background: var(--surface-muted, #f3f4f6); */
  border: 1px dashed var(--border-color, #e5e7eb);
  border-radius: 12px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: fit-content;
  height: fit-content;
}

.slot-tooltip-point {
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.slot-tooltip-body {
  white-space: pre-line;
  word-break: keep-all;
}

.slot-tooltip-line{
  display: grid;
  grid-template-columns: 40px 1fr;
  font-size: 0.8rem;
}

.slot-tooltip-line--highlighted {
  color: var(--accent-color, #f97316);
  font-weight: 700;
}

.slot-tooltip-line--locked {
  color: var(--text-muted, #6b7280);
  opacity: 0.75;
}

.slot-tooltip-line--locked .slot-tooltip-point {
  color: inherit;
}

.slot-tooltip-line--highlighted .slot-tooltip-point {
  color: inherit;
}

.slot-gem-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.gem-card {
  display: flex;
  flex-direction: row;
  gap: 8px;
  position: relative;
}

.gem-card-grade{
  font-size: 0.75rem;
  font-weight: 600;
}

.gem-card:hover .gem-tooltip-hover {
  visibility: visible;
  opacity: 1;
}

.gem-tooltip-hover {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  z-index: 100;
  background: rgba(0, 0, 0, 0.9);
  color: #ffffff;
  padding: 12px;
  border-radius: 8px;
  font-size: 0.8rem;
  line-height: 1.6;
  white-space: pre-line;
  left: 0;
  top: 100%;
  margin-top: 8px;
  min-width: 250px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: opacity 0.2s ease-in-out;
  pointer-events: none;
}

.gem-card-head {
  display: flex;
  flex-direction: column;
  /* gap: 10px; */
  align-items: center;
}

.gem-card-name {
  font-size: 0.95rem;
  color: var(--text-primary, #1f2937);
}

.ark-grid-effects .effect-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 12px;
}

.effect-item {
  display: flex;
  font-size: 0.85rem;
}

.effect-item strong {
  color: var(--text-primary, #1f2937);
}

.effect-level {
  min-width: 50px;
  font-weight: 600;
  color: var(--primary-color, #2563eb);
}

.effect-description {
  margin: 4px 0 0;
  color: var(--text-muted, #6b7280);
}

@media (max-width: 768px) {
  .ark-grid-overview {
    gap: 0;
  }

  .section-heading {
    flex-direction: column;
  }

  .gem-badge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
