/**
 * 스킬 데이터 변환 유틸리티
 *
 * CharacterSearch.vue에서 추출한 순수 함수들입니다.
 * 스킬, 보석, 각성 관련 데이터 변환 및 정규화를 담당합니다.
 */

import type { CombatSkill, SkillGem } from '@/api/types/skills'
import { sanitizeInline, flattenTooltipLines } from '@/utils/tooltipText'
import { isRecord, isString } from '@/utils/typeGuards'

// ============================================================================
// Types
// ============================================================================

export type GemEffectPlacement = 'damage' | 'cooldown' | 'unknown'

export interface SkillGemSlot {
  key: string
  name: string
  icon?: string
  label?: string
  grade?: string
  levelLabel?: string
  levelValue: number
  effectType: GemEffectPlacement
}

export interface CombatSkillCatalogEntry {
  key: string
  name: string
}

// ============================================================================
// Constants
// ============================================================================

export const GEM_DAMAGE_REGEX = /(멸화|겁화|피해|대미지|데미지|해방|지원\s*효과)/i
export const GEM_COOLDOWN_REGEX = /(홍염|작열|광휘|쿨타임|재사용|대기시간)/i

// ============================================================================
// Helper Functions (Internal)
// ============================================================================

/**
 * Inline 텍스트로 변환 (공백, 줄바꿈 제거)
 */
const inlineText = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value !== 'string' && typeof value !== 'number') return ''
  return sanitizeInline(value)
}

/**
 * 문자열에서 추출
 */
const readString = (value: unknown): string => (isString(value) ? value : '')

// ============================================================================
// Core Functions
// ============================================================================

/**
 * 스킬 이름을 정규화하여 고유 키로 변환
 * - HTML 태그 제거
 * - 공백, 특수문자 제거
 * - 소문자로 변환
 *
 * @param value - 스킬 이름
 * @returns 정규화된 키
 *
 * @example
 * normalizeSkillKey('오라 검술') // 'orasugul'
 * normalizeSkillKey('<FONT>파워 스트라이크</FONT>') // 'pawosuteuraikeu'
 */
export const normalizeSkillKey = (value?: string | null): string =>
  inlineText(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

/**
 * 레벨 라벨 포맷팅
 * @param value - 레벨 값
 * @returns Lv.10 형식의 문자열
 */
export const formatLevelLabel = (value?: number | string | null): string => {
  const num = typeof value === 'number' ? value : Number(value)
  if (!Number.isFinite(num)) return ''
  return `Lv.${num}`
}

/**
 * 스킬이 각성 스킬인지 확인
 * @param skill - 스킬 정보
 * @returns 각성 스킬 여부
 */
export const isAwakeningSkill = (skill: CombatSkill): boolean => {
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

/**
 * 보석 라벨 추출 (멸화, 겁화, 홍염, 작열, 광휘 등)
 * @param gem - 보석 정보
 * @returns 보석 라벨 문자열
 */
export const extractGemLabel = (gem: SkillGem): string => {
  const tooltipText = flattenTooltipLines(gem.tooltip).join(' ')
  const candidates = [inlineText(gem.name), inlineText(gem.skill?.description), tooltipText]
    .filter(Boolean)
    .join(' ')
  const labelMatch = candidates.match(/(멸화|겁화|홍염|작열|광휘|지원\s*효과)/i)
  const label = labelMatch?.[1]
  const isDamageHint = GEM_DAMAGE_REGEX.test(candidates)
  const isCooldownHint = GEM_COOLDOWN_REGEX.test(candidates)

  // 광휘 보석은 피해증가 → 겁화, 쿨감 → 작열로 매핑
  if (label && /광휘/i.test(label)) {
    if (isDamageHint) return '겁화'
    if (isCooldownHint) return '작열'
  }

  if (label) return label
  if (isDamageHint) return '멸화'
  if (isCooldownHint) return '홍염'
  return ''
}

/**
 * 보석 효과 분류 (피해/쿨감)
 * @param label - 보석 라벨
 * @returns 보석 효과 타입
 */
export const classifyGemEffect = (label: string): GemEffectPlacement => {
  if (/멸화|겁화|해방|지원\s*효과/i.test(label)) return 'damage'
  if (/홍염|작열|광휘/i.test(label)) return 'cooldown'
  return 'unknown'
}

/**
 * 보석 툴팁에서 스킬 이름 추출
 * @param tooltip - 보석 툴팁
 * @returns 추출된 스킬 이름
 */
export const extractSkillNameFromGemTooltip = (tooltip?: string | null): string => {
  if (!tooltip) return ''

  const extractFromString = (value: string) => {
    const fontMatch = value.match(/<FONT[^>]*>([^<]+)<\/FONT>/i)
    if (fontMatch?.[1]) return inlineText(fontMatch[1])
    const bracketMatch = value.match(/\[([^\]]+)\]/)
    if (bracketMatch?.[1]) return inlineText(bracketMatch[1])
    return ''
  }

  const scanLinesForName = (lines: string[]) => {
    for (const line of lines) {
      const trimmed = inlineText(line)
      if (!trimmed || /보석\s*효과/i.test(trimmed)) continue
      const fromLine = extractFromString(trimmed)
      if (fromLine) return fromLine
      if (trimmed.length >= 2) return trimmed
    }
    return ''
  }

  try {
    const parsed = JSON.parse(tooltip) as unknown
    const part = (() => {
      if (!isRecord(parsed)) return undefined
      const element007 = parsed['Element_007']
      if (!isRecord(element007)) return undefined
      const value = element007['value'] ?? element007['Value']
      if (!isRecord(value)) return undefined
      return value['Element_001'] ?? value['Element_000']
    })()

    if (part) {
      const fromPart = extractFromString(String(part))
      if (fromPart) return fromPart
      const firstLine = flattenTooltipLines(String(part)).find(Boolean)
      if (firstLine) {
        const fromLine = extractFromString(firstLine)
        if (fromLine) return fromLine
        return inlineText(firstLine.replace(/^\[[^\]]+\]\s*/, ''))
      }
    }

    // fallback: scan every string in parsed tooltip
    const findInNode = (node: unknown): string => {
      if (!node) return ''
      if (typeof node === 'string') return extractFromString(node)
      if (Array.isArray(node)) {
        for (const item of node) {
          const found = findInNode(item)
          if (found) return found
        }
        return ''
      }
      if (isRecord(node)) {
        for (const value of Object.values(node)) {
          const found = findInNode(value)
          if (found) return found
        }
      }
      return ''
    }
    const scanned = findInNode(parsed)
    if (scanned) return scanned
  } catch {
    // ignore parse/shape errors
  }

  const lines = flattenTooltipLines(tooltip)
  const wideLines = flattenTooltipLines(tooltip, { preferValueField: false })
  const fromLines = scanLinesForName(lines)
  if (fromLines) return fromLines
  const fromWideLines = scanLinesForName(wideLines)
  if (fromWideLines) return fromWideLines
  return ''
}

/**
 * 보석의 스킬 키 정규화
 * @param gem - 보석 정보
 * @param fallbackTooltip - 대체 툴팁
 * @param skillCatalog - 스킬 카탈로그
 * @returns 정규화된 스킬 키
 */
export const normalizeGemSkillKey = (
  gem: SkillGem,
  fallbackTooltip?: string | null,
  skillCatalog?: CombatSkillCatalogEntry[]
): string => {
  const tooltipSource = gem.tooltip || fallbackTooltip
  const candidates = [
    gem.skill?.name,
    gem.skill?.description,
    extractSkillNameFromGemTooltip(tooltipSource),
    gem.name
  ]
  const catalog = skillCatalog ?? []

  for (const candidate of candidates) {
    const key = normalizeSkillKey(candidate)
    if (key && (!catalog.length || catalog.some(entry => entry.key === key))) return key
  }

  const combinedText = [tooltipSource, gem.skill?.description, gem.name].filter(Boolean).join(' ')
  if (combinedText && catalog.length) {
    const normalizedCombined = normalizeSkillKey(combinedText)
    const hit = catalog.find(entry => entry.key && normalizedCombined.includes(entry.key))
    if (hit) return hit.key
  }

  // fallback: return first non-empty key even if 카탈로그 매치 실패
  for (const candidate of candidates) {
    const key = normalizeSkillKey(candidate)
    if (key) return key
  }
  return ''
}

/**
 * 보석에서 스킬 이름 추출 (카탈로그 매칭 포함)
 * @param gem - 보석 정보
 * @param fallbackTooltip - 대체 툴팁
 * @param skillCatalog - 스킬 카탈로그
 * @returns 스킬 이름
 */
export const skillNameFromGem = (
  gem: SkillGem,
  fallbackTooltip?: string | null,
  skillCatalog?: CombatSkillCatalogEntry[]
): string => {
  const tooltipSource = gem.tooltip || fallbackTooltip
  const direct =
    inlineText(gem.skill?.name) ||
    inlineText(gem.skill?.description) ||
    extractSkillNameFromGemTooltip(tooltipSource) ||
    ''

  if (skillCatalog?.length) {
    const normalized = normalizeSkillKey(direct)
    const hit = normalized ? skillCatalog.find(entry => entry.key === normalized) : null
    if (hit?.name) return hit.name
  }
  return direct
}

/**
 * 두 보석 슬롯 중 레벨이 높은 것을 선택
 * @param current - 현재 보석 슬롯
 * @param next - 비교할 보석 슬롯
 * @returns 레벨이 더 높은 보석 슬롯
 */
export const pickHigherGem = (
  current: SkillGemSlot | null,
  next: SkillGemSlot
): SkillGemSlot => {
  if (!current) return next
  return next.levelValue > current.levelValue ? next : current
}
