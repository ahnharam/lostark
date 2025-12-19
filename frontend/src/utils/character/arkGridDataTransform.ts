/**
 * 아크 그리드 데이터 변환 유틸리티
 *
 * CharacterSearch.vue에서 추출한 순수 함수들입니다.
 * 아크 패시브, 코어, 티어 관련 데이터 변환 및 파싱을 담당합니다.
 */

import { sanitizeInline, flattenTooltipLines } from '@/utils/tooltipText'

// ============================================================================
// Types
// ============================================================================

export type PassiveSectionKey = 'evolution' | 'realization' | 'leap'

export interface PassiveTooltipParsed {
  title: string
  lines: string[]
}

export interface PassiveDescription {
  typeLabel: string
  levelLabel: string
  tierLabel: string
  tierValue: number
  passiveName: string
}

// ============================================================================
// Constants
// ============================================================================

export const PASSIVE_SECTIONS = [
  { key: 'evolution' as const, label: '진화', keyword: '진화' },
  { key: 'realization' as const, label: '깨달음', keyword: '깨달음' },
  { key: 'leap' as const, label: '도약', keyword: '도약' }
] as const

const PASSIVE_SECTION_PATTERN = PASSIVE_SECTIONS.map(section => section.keyword).join('|')
const PASSIVE_SECTION_REGEX = new RegExp(`(${PASSIVE_SECTION_PATTERN})`, 'gi')
const PASSIVE_SECTION_PREFIX_REGEX = new RegExp(
  `^\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?\\s*([·:\\-]+)?\\s*`,
  'gi'
)
const PASSIVE_SECTION_SUFFIX_REGEX = new RegExp(
  `\s*([·:\\-]+)?\\s*(?:\\[)?(${PASSIVE_SECTION_PATTERN})(?:\\])?$`,
  'gi'
)

export const ROMAN_NUMERAL_MAP: Record<string, number> = {
  I: 1,
  V: 5,
  X: 10,
  L: 50,
  C: 100,
  D: 500,
  M: 1000
}

// ============================================================================
// Helper Functions (Internal)
// ============================================================================

/**
 * Inline 텍스트로 변환
 */
const inlineText = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value !== 'string' && typeof value !== 'number') return ''
  return sanitizeInline(value)
}

// ============================================================================
// Core Functions
// ============================================================================

/**
 * 패시브 단계 키워드 제거 (진화, 깨달음, 도약)
 * @param value - 원본 텍스트
 * @returns 키워드가 제거된 텍스트
 *
 * @example
 * stripPassiveStageKeywords('[진화] 패시브명') // '패시브명'
 * stripPassiveStageKeywords('깨달음 · 스킬명') // '스킬명'
 */
export const stripPassiveStageKeywords = (value?: string | null): string => {
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

/**
 * 패시브 툴팁 파싱
 * @param tooltip - 툴팁 문자열
 * @returns 파싱된 툴팁 (제목과 본문)
 *
 * @example
 * parsePassiveTooltip('{"Element_001": "..."}')
 * // { title: '패시브명', lines: ['효과 설명1', '효과 설명2'] }
 */
export const parsePassiveTooltip = (tooltip?: string | null): PassiveTooltipParsed => {
  const lines = flattenTooltipLines(tooltip, { fallbackBreaks: true })
  const [title, ...rest] = lines
  return {
    title: inlineText(title),
    lines: rest.map(line => inlineText(line)).filter(Boolean)
  }
}

/**
 * 패시브 설명 파싱 (타입, 레벨, 티어 추출)
 * @param description - 패시브 설명 문자열
 * @returns 파싱된 정보
 *
 * @example
 * parsePassiveDescription('진화 3티어 Lv.5 패시브명')
 * // { typeLabel: '진화', levelLabel: 'Lv.5', tierLabel: '3티어', tierValue: 3, passiveName: '패시브명' }
 */
export const parsePassiveDescription = (description?: string | null): PassiveDescription => {
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

/**
 * 로마 숫자를 아라비아 숫자로 변환
 * @param value - 로마 숫자 문자열 (I, II, III, IV, V, X 등)
 * @returns 변환된 숫자 또는 null (변환 실패 시)
 *
 * @example
 * romanToNumber('III') // 3
 * romanToNumber('IV') // 4
 * romanToNumber('X') // 10
 * romanToNumber('ABC') // null
 */
export const romanToNumber = (value: string): number | null => {
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

/**
 * 티어 텍스트 정규화 (키워드 제거)
 * @param value - 티어 텍스트
 * @returns 정규화된 텍스트
 *
 * @example
 * normalizeTierText('[진화] · 3티어') // '3티어'
 */
export const normalizeTierText = (value?: string | null): string => {
  if (!value) return ''
  return value
    .replace(PASSIVE_SECTION_REGEX, ' ')
    .replace(/[·:]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * 티어 그룹 라벨 추출 (아라비아 숫자 또는 로마 숫자에서)
 * @param tierLabel - 티어 라벨
 * @param levelLine - 레벨 라인
 * @returns 티어 그룹 라벨 ('3티어', '5티어' 등)
 *
 * @example
 * extractTierGroupLabel('3티어', null) // '3티어'
 * extractTierGroupLabel(null, 'III 레벨') // '3티어'
 * extractTierGroupLabel('티어 V', null) // '5티어'
 */
export const extractTierGroupLabel = (tierLabel?: string | null, levelLine?: string | null): string => {
  const candidates = [tierLabel, levelLine]
  for (const candidate of candidates) {
    const normalized = normalizeTierText(candidate ?? '')
    if (!normalized) continue

    // 아라비아 숫자 찾기
    const digitMatch = normalized.match(/(\d+)/)
    if (digitMatch) {
      return `${digitMatch[1]}티어`
    }

    // 로마 숫자 찾기
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
