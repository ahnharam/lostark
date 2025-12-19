/**
 * 아크 그리드(Ark Grid) 데이터 관리 Composable
 *
 * CharacterSearch.vue에서 추출한 아크 그리드 관련 비즈니스 로직입니다.
 * 아크 패시브, 코어, 포인트 관련 데이터 변환 및 파싱을 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { ArkGridResponse, ArkPassiveEffect } from '@/api/types/armory'
import { sanitizeInline, flattenTooltipLines } from '@/utils/tooltipText'
import {
  type PassiveSectionKey,
  PASSIVE_SECTIONS,
  parsePassiveTooltip,
  parsePassiveDescription,
  stripPassiveStageKeywords,
  extractTierGroupLabel
} from '@/utils/character/arkGridDataTransform'

// ============================================================================
// Types
// ============================================================================

export type CoreAlignment = 'order' | 'chaos' | 'unknown'
export type CoreCelestial = 'sun' | 'moon' | 'star' | 'unknown'

export interface ProcessedArkPassiveEffect {
  key: string
  name: string
  sectionKey: PassiveSectionKey
  levelLabel: string
  tierGroup: string
  typeLabel: string
  tierLabel: string
  tierValue: number
}

export interface ArkCoreSlot {
  key: string
  name: string
  alignment: CoreAlignment
  celestial: CoreCelestial
  icon: string
  grade: string
  gradeColor: string
  nameColor: string
  tooltip: string
  pointLabel: string
  initial: string
}

export interface ArkAppliedPoint {
  key: string
  label: string
  value: string
  description: string
}

export interface ArkPassiveSectionRank {
  key: PassiveSectionKey
  label: string
  tier: string
  level: string
}

export type CoreMatrixHeader = { key: string; label: string }
export type CoreMatrixCell = { key: string; label: string; slots: ArkCoreSlot[] }
export type CoreMatrixRow = { key: string; label: string; cells: CoreMatrixCell[] }

export interface PassiveSummaryCard {
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

export interface PassiveSummarySection {
  key: PassiveSectionKey
  label: string
  effects: PassiveSummaryCard[]
}

export interface PassiveSummaryRow {
  id: string
  label: string
  sections: PassiveSummarySection[]
}

export interface ArkSummary {
  passiveTitle: string
  slotCount: number
  coreSlots: ArkCoreSlot[]
  coreMatrix: { headers: CoreMatrixHeader[]; rows: CoreMatrixRow[] }
  appliedPoints: ArkAppliedPoint[]
  passiveMatrix: PassiveSummaryRow[]
  corePassives: PassiveSummaryCard[]
  passiveEffects: ProcessedArkPassiveEffect[]
  passiveSectionRanks: ArkPassiveSectionRank[]
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

/**
 * 알 수 없는 값을 문자열로 변환
 */
const readString = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value === 'string') return value
  if (typeof value === 'number') return String(value)
  return ''
}

/**
 * 코어 메타데이터 파싱 (정렬, 천상 여부)
 */
const parseCoreMeta = (name: string): { displayName: string; alignment: CoreAlignment; celestial: CoreCelestial } => {
  const nameLower = name.toLowerCase()

  // Determine celestial type
  let celestial: CoreCelestial = 'unknown'
  if (nameLower.includes('해') || nameLower.includes('sun')) celestial = 'sun'
  else if (nameLower.includes('달') || nameLower.includes('moon')) celestial = 'moon'
  else if (nameLower.includes('별') || nameLower.includes('star')) celestial = 'star'

  // Determine alignment
  let alignment: CoreAlignment = 'unknown'
  if (nameLower.includes('질서') || nameLower.includes('order')) alignment = 'order'
  else if (nameLower.includes('혼돈') || nameLower.includes('chaos')) alignment = 'chaos'

  const displayName = name.replace(/(해|달|별|천상|질서|혼돈|sun|moon|star|celestial|order|chaos)/gi, '').trim()
  return { displayName, alignment, celestial }
}

/**
 * 툴팁에서 색상 추출
 */
const coreNameColorFromTooltip = (tooltip: unknown): string => {
  const text = readString(tooltip)
  const match = text.match(/color["\s:=]*([#a-f0-9]{6,8})/i)
  return match?.[1] ? `#${match[1]}` : ''
}

/**
 * 툴팁 텍스트에서 색상 추출
 */
const extractTooltipColor = (text: string): string => {
  const match = text.match(/color["\s:=]*([#a-f0-9]{6,8})/i)
  return match?.[1] ? `#${match[1]}` : ''
}

/**
 * 등급으로부터 색상 결정
 */
const coreGradeColor = (grade: string): string => {
  const g = grade.toLowerCase()
  if (g.includes('ancient') || g.includes('고대')) return '#f59e0b'
  if (g.includes('legend') || g.includes('전설')) return '#eab308'
  if (g.includes('hero') || g.includes('영웅')) return '#8b5cf6'
  if (g.includes('rare') || g.includes('희귀')) return '#3b82f6'
  return ''
}

/**
 * 코어 매트릭스 생성 (그리드 레이아웃용)
 */
const buildCoreMatrix = (coreSlots: ArkCoreSlot[]): { headers: CoreMatrixHeader[]; rows: CoreMatrixRow[] } => {
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

// ============================================================================
// Ark Passive Matrix Building
// ============================================================================

/**
 * 아크 패시브 효과를 카드로 변환
 */
const buildPassiveCard = (effect: ArkPassiveEffect, index: number): PassiveSummaryCard => {
  const tooltip = parsePassiveTooltip(effect.toolTip)
  const tierLabel = inlineText(effect.description)
  const levelLine = tooltip.lines.find(line => /아크\s*패시브\s*레벨/i.test(line)) || tooltip.title || ''
  const summaryLine = tooltip.lines.find(line => line && line !== levelLine) || tooltip.title || tierLabel
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

/**
 * 아크 패시브 효과 목록을 매트릭스로 변환
 */
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

  rows.forEach(row => {
    row.sections.forEach(section => {
      section.effects = [...section.effects]
    })
  })

  rows.sort((a, b) => {
    const tierValue = (label: string) => {
      const numericMatch = label.match(/(\d+)/)
      if (numericMatch) return Number(numericMatch[1])
      return Number.POSITIVE_INFINITY
    }
    return tierValue(b.label) - tierValue(a.label)
  })

  return rows
}

// ============================================================================
// Composable
// ============================================================================

export const useArkGridData = (arkGridResponse: Ref<ArkGridResponse | null>) => {
  /**
   * 아크 그리드 통합 요약 데이터
   */
  const arkSummary = computed<ArkSummary>(() => {
    const passive = arkGridResponse.value?.arkPassive
    const grid = arkGridResponse.value?.arkGrid
    const passiveMatrix = buildArkPassiveMatrix(passive?.effects ?? [])
    const passiveEffects =
      passive?.effects
        ?.map((effect, index) => {
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
        })
        .filter(effect => effect.name) ?? []

    const parseLevelNumeric = (label?: string | null) => {
      if (!label) return -Infinity
      const match = String(label).match(/(\d+)/)
      return match ? Number(match[1]) : -Infinity
    }

    const bestRankBySection = (sectionKey: PassiveSectionKey) => {
      type PassiveEffectCandidate = {
        name?: string
        tierValue?: number
        tierLabel?: string
        tierGroup?: string
        levelLabel?: string
        levelDisplay?: string
        levelLine?: string
      }

      const candidatesFromMatrix = passiveMatrix.flatMap(row =>
        row.sections.find(section => section.key === sectionKey)?.effects ?? []
      ) as PassiveEffectCandidate[]

      const candidates: PassiveEffectCandidate[] =
        candidatesFromMatrix.length > 0
          ? candidatesFromMatrix
          : (passiveEffects.filter(effect => effect.sectionKey === sectionKey) as PassiveEffectCandidate[])
      if (!candidates.length) return null

      const best = candidates
        .slice()
        .sort((a, b) => {
          const tierA = typeof a.tierValue === 'number' && Number.isFinite(a.tierValue) ? a.tierValue : -Infinity
          const tierB = typeof b.tierValue === 'number' && Number.isFinite(b.tierValue) ? b.tierValue : -Infinity
          if (tierA !== tierB) return tierB - tierA
          const levelA = parseLevelNumeric(a.levelLabel || a.levelDisplay || a.levelLine)
          const levelB = parseLevelNumeric(b.levelLabel || b.levelDisplay || b.levelLine)
          if (levelA !== levelB) return levelB - levelA
          return 0
        })[0]
      return {
        name: best?.name || '',
        tier: best?.tierLabel || best?.tierGroup || best?.levelLine || '',
        level: best?.levelLabel || best?.levelDisplay || best?.levelLine || ''
      }
    }

    const rawSlots = (() => {
      if (Array.isArray(grid?.slots)) return grid.slots as unknown[]
      const gridRecord = (grid ?? {}) as unknown as Record<string, unknown>
      const alt = gridRecord['Slots']
      return Array.isArray(alt) ? alt : []
    })()
    const coreSlots = rawSlots
      .map((slot, index) => {
        const getField = (...keys: string[]) => {
          const slotRecord = (slot ?? {}) as unknown as Record<string, unknown>
          for (const key of keys) {
            const value = slotRecord[key]
            if (value !== undefined && value !== null && value !== '') return value
          }
          return null
        }

        const rawName = inlineText(getField('name', 'Name')) || `코어 ${getField('index', 'Index') ?? index + 1}`
        const meta = parseCoreMeta(rawName)
        const name = meta.displayName
        const gradeLabel = inlineText(getField('grade', 'Grade', 'gradeName', 'GradeName'))
        const tooltipRaw = getField('tooltip', 'Tooltip')
        const tooltipText = readString(tooltipRaw)
        const nameColor = coreNameColorFromTooltip(tooltipRaw) || extractTooltipColor(tooltipText) || ''
        const gradeColor =
          nameColor ||
          coreGradeColor(gradeLabel) ||
          (meta.alignment === 'order' ? '#e11d48' : meta.alignment === 'chaos' ? '#2563eb' : '')
        const pointValue = getField('point', 'Point')
        const pointLabel =
          typeof pointValue === 'number' || typeof pointValue === 'string' ? `${String(pointValue).trim()}P` : ''
        const icon = readString(getField('icon', 'Icon'))
        const indexValue = getField('index', 'Index')
        const keySuffix = typeof indexValue === 'number' || typeof indexValue === 'string' ? indexValue : index
        return {
          key: `slot-${keySuffix}`,
          name,
          alignment: meta.alignment,
          celestial: meta.celestial,
          icon,
          grade: gradeLabel,
          gradeColor,
          nameColor,
          tooltip: tooltipText,
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
        const description =
          inlineText(point.description) ||
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

  return {
    arkSummary
  }
}
