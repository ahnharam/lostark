/**
 * 장비(Equipment) 데이터 관리 Composable
 *
 * CharacterSearch.vue에서 추출한 장비, 아바타, 각인 관련 비즈니스 로직입니다.
 * 장비 요약, 아바타 요약, 각인 요약 데이터 변환을 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { Equipment, Engraving, ArmoryAvatar } from '@/api/types/armory'
import { sanitizeInline, flattenTooltipLines } from '@/utils/tooltipText'
import { applyEffectAbbreviations, hasAbbreviationMatch } from '@/data/effectAbbreviations'
import { getEngravingDisplayName } from '@/data/engravingNames'
import { formatNumberLocalized } from '@/utils/format'
import {
  parseEquipmentMeta,
  extractEnhancementLevel,
  isAccessory,
  isBracelet,
  isAbilityStone,
  isNecklace,
  isEarring,
  isRing,
  isAvatarEquipment,
  summarizeEquipmentLine
} from '@/utils/character/equipmentDataTransform'

// ============================================================================
// Types
// ============================================================================

export interface GradeBadge {
  grade: string
  count: number
}

export interface EquipmentEffect {
  label: string
  full?: string
  richLabel?: string
  bgColor: string
  textColor: string
}

export interface EquipmentSummaryItem {
  key: string
  name: string
  typeLabel: string
  grade: string
  icon: string
  itemLevel: string
  quality: number | null
  transcend: string
  harmony: string
  meta: string
  isAccessory: boolean
  isBracelet: boolean
  isAbilityStone: boolean
  enhancement: string
}

export interface EquipmentSummaryRightItem extends EquipmentSummaryItem {
  special?: string
  effects?: EquipmentEffect[]
}

export interface EquipmentSummary {
  gradeBadges: GradeBadge[]
  left: EquipmentSummaryItem[]
  right: EquipmentSummaryRightItem[]
}

export interface AvatarItem {
  key: string
  name: string
  type: string
  grade: string
  icon: string
  tooltip: string
  isInner: boolean
  isSet: boolean
}

export interface AvatarSummary {
  items: AvatarItem[]
}

export interface EngravingSummaryItem {
  key: string
  name: string
  displayName: string
  gradeLabel: string
  levelLabel: string
  craftLabel: string
  icon: string
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
 * 툴팁에서 라인 추출
 */
const extractTooltipLines = (tooltip?: string): string[] => flattenTooltipLines(tooltip, { fallbackBreaks: true })

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
 * HEX 색상 정규화
 */
const normalizeHexColor = (value?: string | null): string | null => {
  if (!value) return null
  const raw = value.replace(/['"#]/g, '').trim()
  if (!/^[0-9a-fA-F]{3,4}$/.test(raw) && !/^[0-9a-fA-F]{6}$/.test(raw) && !/^[0-9a-fA-F]{8}$/.test(raw)) {
    return null
  }
  const expand = (hex: string) => (hex.length === 1 ? hex.repeat(2) : hex)
  const normalized =
    raw.length === 3 || raw.length === 4 ? raw.split('').map(expand).join('').slice(0, 6) : raw.slice(0, 6)
  return `#${normalized.toLowerCase()}`
}

/**
 * 효과 라벨에서 불필요한 부분 제거
 */
const shouldSkipEffectLabel = (label?: string): boolean => {
  if (!label) return true
  const clean = inlineText(label)
  return /아크\s*패시브\s*포인트|깨달음/i.test(clean)
}

/**
 * 효과 라벨 정규화
 */
const normalizeEffectLabel = (label: string): string => inlineText(label).replace(/^>\s*/, '').trim()

/**
 * 효과 라벨 축약
 */
const abbreviateEffect = (label: string): string => applyEffectAbbreviations(normalizeEffectLabel(label))

/**
 * 효과 라벨 포맷팅
 */
const formatEffectLabel = (label: string) => {
  const normalized = normalizeEffectLabel(label)
  const abbreviated = applyEffectAbbreviations(normalized)
  return {
    label: abbreviated,
    normalized,
    changed: abbreviated !== normalized
  }
}

/**
 * HEX to RGBA 변환
 */
const hexToRgba = (hex: string, alpha = 0.16): string => {
  const cleaned = hex.replace('#', '')
  if (cleaned.length !== 6) return `rgba(0,0,0,${alpha})`
  const r = parseInt(cleaned.slice(0, 2), 16)
  const g = parseInt(cleaned.slice(2, 4), 16)
  const b = parseInt(cleaned.slice(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

/**
 * HEX to RGB 변환
 */
const hexToRgb = (hex?: string | null): { r: number; g: number; b: number } | null => {
  const normalized = normalizeHexColor(hex)
  if (!normalized) return null
  const cleaned = normalized.replace('#', '')
  const r = parseInt(cleaned.slice(0, 2), 16)
  const g = parseInt(cleaned.slice(2, 4), 16)
  const b = parseInt(cleaned.slice(4, 6), 16)
  return { r, g, b }
}

/**
 * 팔찌 티어 색상 기준
 */
const TIER_BASES = [
  { r: 0, g: 181, b: 255 }, // 하
  { r: 206, g: 67, b: 252 }, // 중
  { r: 254, g: 150, b: 0 } // 상
]

/**
 * 색상 티어 거리 계산
 */
const colorTierDistance = (hex?: string | null): number => {
  const rgb = hexToRgb(hex)
  if (!rgb) return Infinity
  const distance = (a: typeof rgb, b: typeof rgb) => {
    const dr = a.r - b.r
    const dg = a.g - b.g
    const db = a.b - b.b
    return Math.sqrt(dr * dr + dg * dg + db * db)
  }
  return Math.min(...TIER_BASES.map(base => distance(rgb, base)))
}

/**
 * 티어 정렬된 색상 선택
 */
const pickTierAlignedColor = (colors: string[]): string | null => {
  if (!colors.length) return null
  let bestColor: string | null = null
  let bestDist = Number.POSITIVE_INFINITY
  colors.forEach(color => {
    const dist = colorTierDistance(color)
    if (dist < bestDist) {
      bestDist = dist
      bestColor = color
    }
  })
  return bestColor ?? colors[0] ?? null
}

/**
 * 색상 있는 텍스트 정리
 */
const sanitizeColoredText = (value?: string | null): string => {
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

/**
 * 효과 데코레이션
 */
const decorateEffect = (effect: { label: string; full?: string }): EquipmentEffect | null => {
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

// ============================================================================
// Equipment Effect Extractors
// ============================================================================

/**
 * 악세서리 효과 추출
 */
const extractAccessoryEffects = (item: Equipment): EquipmentEffect[] => {
  const effects: EquipmentEffect[] = []

  const addEffect = (label: string, full?: string, color?: string) => {
    const cleanedLabel = normalizeEffectLabel(label)
    const formatted = formatEffectLabel(cleanedLabel)
    const normalizedLabel = formatted.label
    const key = `${normalizedLabel}|${full || ''}`
    if (effects.find(e => `${e.label}|${e.full || ''}` === key)) return
    const hasAbbrev = formatted.changed
    const textColor = hasAbbrev ? color || 'var(--text-primary)' : 'var(--text-secondary)'
    const neutralBg = 'var(--bg-secondary)'
    const bgColor = hasAbbrev ? (color ? hexToRgba(color, 0.16) : neutralBg) : neutralBg
    effects.push({ label: normalizedLabel, full, textColor, bgColor })
  }

  try {
    const parsed = JSON.parse(item.tooltip)
    const raw = parsed?.Element_006?.value?.Element_001 || parsed?.Element_006?.value?.Element_000 || ''
    if (typeof raw === 'string' && raw.length) {
      const cleaned = raw.replace(/<img[^>]*>/gi, '')
      const segments = cleaned
        .split(/<br\s*\/?>/i)
        .map(seg => seg.trim())
        .filter(Boolean)
      segments.forEach(seg => {
        const colorMatch = seg.match(/color=['"]?(#?[0-9a-fA-F]{6})/i)
        const color = normalizeHexColor(colorMatch?.[1] || null)
        const label = inlineText(seg.replace(/<[^>]+>/g, ''))
          .replace(/\s+/g, ' ')
          .trim()
        if (!label) return
        addEffect(label, label, color || undefined)
      })
    }
  } catch {
    // ignore parse errors
  }

  if (!effects.length) {
    const lines = extractTooltipLines(item.tooltip)
    const startIndex = lines.findIndex((line: string) => /연마\s*효과/i.test(line))
    const candidates = startIndex >= 0 ? lines.slice(startIndex + 1) : lines.filter((line: string) => /연마/i.test(line))
    const effectPattern = /[+]\s*\d|%/
    const meaningful = candidates.filter((line: string) => effectPattern.test(line))
    meaningful.forEach((line: string) => {
      const decorated = decorateEffect({ label: inlineText(line) })
      if (decorated) effects.push(decorated)
    })
  }

  return effects.slice(0, 6)
}

/**
 * 어빌리티 스톤 효과 추출
 */
const extractStoneEffects = (item: Equipment): EquipmentEffect[] => {
  const lines = extractTooltipLines(item.tooltip)
  const candidates = lines.filter(
    (line: string) => (/\[.*\]/.test(line) || /세공|각인/i.test(line)) && !/세공\s*단계\s*보너스/i.test(line)
  )
  return candidates
    .map((line: string) => decorateEffect({ label: inlineText(line) }))
    .filter(Boolean)
    .slice(0, 3) as EquipmentEffect[]
}

/**
 * 팔찌 효과 추출
 */
const extractBraceletEffects = (item: Equipment): EquipmentEffect[] => {
  const effects: EquipmentEffect[] = []
  const combatStatKeywords = /(치명|특화|제압|신속|인내|숙련)/i

  const addEffect = (label: string, full?: string, color?: string, richLabel?: string) => {
    const cleaned = normalizeEffectLabel(label)
    const normalizedLabel = abbreviateEffect(cleaned)
    const key = `${normalizedLabel}|${full || ''}`
    if (effects.find(e => `${e.label}|${e.full || ''}` === key)) return
    const hasAbbrev = hasAbbreviationMatch(cleaned)
    const textColor = color || (hasAbbrev ? 'var(--text-primary)' : '#6b7280')
    const bgColor = color ? hexToRgba(color, 0.16) : hasAbbrev ? 'var(--bg-secondary)' : 'rgba(107, 114, 128, 0.15)'
    effects.push({
      label: normalizedLabel,
      full: richLabel || full || normalizedLabel,
      richLabel,
      textColor,
      bgColor
    })
  }

  try {
    const parsed = JSON.parse(item.tooltip)
    const raw = parsed?.Element_005?.value?.Element_001 || parsed?.Element_005?.value?.Element_000 || ''
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
        const label = inlineText(seg.replace(/<[^>]+>/g, ''))
          .replace(/\s+/g, ' ')
          .trim()
        if (!label) return
        const richLabel = sanitizeColoredText(seg)
        addEffect(label, label, color || undefined, richLabel)
      })
    }
  } catch {
    // ignore parse errors
  }

  if (!effects.length) {
    const lines = extractTooltipLines(item.tooltip)
      .map((line: string) => inlineText(line))
      .filter(Boolean)
    const traitKeyword = /(피해|공격|방어|회복|증가|감소|무력|출혈|중첩|발동|생명력|치명|제압|특화|신속|인내|숙련)/i
    lines.forEach((line: string) => {
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

// ============================================================================
// Composable
// ============================================================================

export const useEquipmentData = (
  detailEquipment: Ref<Equipment[]>,
  detailAvatars: Ref<ArmoryAvatar[] | Equipment[]>,
  detailEngravings: Ref<Engraving[]>
) => {
  /**
   * 장비 요약 데이터
   */
  const equipmentSummary = computed<EquipmentSummary>(() => {
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
      const found = gearOrder.findIndex(group => group.some(keyword => lower.includes(keyword.toLowerCase())))
      return found === -1 ? 99 : found
    }

    const left: Array<{ item: EquipmentSummaryItem; orderRank: number; orderSeq: number }> = []
    const right: EquipmentSummaryRightItem[] = []

    const stoneCraft = detailEngravings.value.find(eng => typeof eng.abilityStoneLevel === 'number')?.abilityStoneLevel

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

        const rawLevel = (() => {
          const record = item as unknown as Record<string, unknown>
          return parseLevel(record['itemLevel']) || parseLevel(record['ItemLevel'])
        })()

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

      const qualityNumber = (() => {
        if (!meta.quality) return null
        const cleaned = meta.quality.replace(/[^0-9.\-]/g, '')
        const num = Number(cleaned)
        return Number.isFinite(num) ? num : null
      })()

      const base: EquipmentSummaryItem = {
        key: `${item.name || 'equipment'}-${index}`,
        name: enhancement ? `+${enhancement}` : inlineText(item.name) || `장비 ${index + 1}`,
        typeLabel,
        grade: gradeLabel,
        icon: item.icon || '',
        itemLevel: accessory || bracelet || abilityStone ? '' : meta.itemLevel || fallbackItemLevel,
        quality: qualityNumber,
        transcend: accessory ? '' : meta.transcend,
        harmony: accessory ? '' : meta.harmony,
        meta: summarizeEquipmentLine(item),
        isAccessory: accessory,
        isBracelet: bracelet,
        isAbilityStone: abilityStone,
        enhancement
      }

      let effects: EquipmentEffect[] = []

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
          orderRank: gearOrderIndex(typeLabel),
          orderSeq: left.length,
          item: base
        })
      }
    })

    const sortedLeft = left
      .slice()
      .sort((a, b) => (a.orderRank ?? 99) - (b.orderRank ?? 99) || (a.orderSeq ?? 0) - (b.orderSeq ?? 0))
      .map(entry => entry.item)

    return {
      gradeBadges,
      left: sortedLeft.slice(0, 6),
      right: right.slice(0, 8)
    }
  })

  /**
   * 아바타 요약 데이터
   */
  const avatarSummary = computed<AvatarSummary>(() => {
    const source =
      detailAvatars.value.length > 0
        ? detailAvatars.value
        : detailEquipment.value.filter(item => isAvatarEquipment(item))

    const avatars = source.map((item, index) => {
      const record = item as unknown as Record<string, unknown>
      const name = inlineText(record['Name'] ?? record['name']) || '아바타'
      const type = inlineText(record['Type'] ?? record['type'] ?? name)
      const grade = inlineText(record['Grade'] ?? record['grade'])
      const icon = readString(record['Icon'] ?? record['icon'])
      const tooltip = readString(record['Tooltip'] ?? record['tooltip'])
      const isInnerRaw = record['IsInner'] ?? record['isInner']
      const isSetRaw = record['IsSet'] ?? record['isSet']
      return {
        key: `${name}-${index}`,
        name,
        type,
        grade,
        icon,
        tooltip,
        isInner: typeof isInnerRaw === 'boolean' ? isInnerRaw : Boolean(isInnerRaw),
        isSet: typeof isSetRaw === 'boolean' ? isSetRaw : Boolean(isSetRaw)
      }
    })

    return { items: avatars }
  })

  /**
   * 각인 요약 데이터
   */
  const engravingSummary = computed<EngravingSummaryItem[]>(() => {
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
        craftLabel: typeof engrave.abilityStoneLevel === 'number' ? `세공 ${engrave.abilityStoneLevel}단` : '',
        icon: engrave.icon || ''
      }
    })
  })

  return {
    equipmentSummary,
    avatarSummary,
    engravingSummary
  }
}
