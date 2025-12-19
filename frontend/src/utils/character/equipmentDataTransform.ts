/**
 * 장비 데이터 변환 유틸리티
 *
 * CharacterSearch.vue에서 추출한 순수 함수들입니다.
 * 장비, 악세서리, 아바타 관련 데이터 변환 및 파싱을 담당합니다.
 */

import type { Equipment } from '@/api/types/armory'
import { sanitizeInline } from '@/utils/tooltipText'
import { isNumber, isString } from '@/utils/typeGuards'

// ============================================================================
// Types
// ============================================================================

export interface EquipmentMeta {
  itemLevel: string
  quality: string
  transcend: string
  harmony: string
  engravingLine: string
  mainStat: string
  craft: string
}

// ============================================================================
// Constants
// ============================================================================

export const SPECIAL_EQUIPMENT_KEYWORDS = ['나침반', '부적', '문장', '보주']
export const SPECIAL_EQUIPMENT_DISPLAY_ORDER = ['나침반', '부적', '보주', '문장']
export const SPECIAL_EQUIPMENT_FALLBACK_ICON =
  'https://cdn-lostark.game.onstove.com/2018/obt/assets/images/common/game/bg_special_slot.png?71bb8b720de8066efb86'

export const GEAR_ORDER = [
  ['투구', '헬멧', '머리'],
  ['어깨'],
  ['상의', '상체', '갑옷'],
  ['하의', '바지'],
  ['장갑'],
  ['무기']
]

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
const extractTooltipLines = (tooltip?: string | null): string[] => {
  if (!tooltip) return []
  const lines: string[] = []

  try {
    const parsed = JSON.parse(tooltip) as unknown
    const visit = (node: unknown) => {
      if (node === null || node === undefined) return
      if (typeof node === 'string') {
        const normalized = node.replace(/\\r\\n|\\n|\\r/g, '\n').split('\n')
        lines.push(...normalized.filter(Boolean))
        return
      }
      if (Array.isArray(node)) {
        node.forEach(visit)
        return
      }
      if (typeof node === 'object') {
        const record = node as Record<string, unknown>
        if ('value' in record) {
          visit(record.value)
          return
        }
        Object.values(record).forEach(visit)
      }
    }
    visit(parsed)
  } catch {
    lines.push(...tooltip.split('\n').filter(Boolean))
  }

  return lines.map(line => inlineText(line)).filter(Boolean)
}

/**
 * 라인에서 통계 매칭
 */
const matchStatFromLines = (lines: string[], patterns: RegExp[]): string => {
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

// ============================================================================
// Core Functions
// ============================================================================

/**
 * 특수 장비인지 확인 (나침반, 부적, 문장, 보주)
 * @param item - 장비 아이템
 * @returns 특수 장비 여부
 *
 * @example
 * isSpecialEquipment({ type: '나침반', name: '고요' }) // true
 * isSpecialEquipment({ type: '무기', name: '검' }) // false
 */
export const isSpecialEquipment = (item: Equipment): boolean => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return SPECIAL_EQUIPMENT_KEYWORDS.some(keyword => target.includes(keyword.toLowerCase()))
}

/**
 * 악세서리인지 확인
 * @param item - 장비 아이템
 * @returns 악세서리 여부
 *
 * @example
 * isAccessory({ type: '목걸이', name: '...' }) // true
 * isAccessory({ type: '투구', name: '...' }) // false
 */
export const isAccessory = (item: Equipment): boolean => {
  const label = inlineText(item.type).toLowerCase()
  return /(목걸이|귀걸이|반지|팔찌|어빌리티|돌)/.test(label)
}

/**
 * 아바타 장비인지 확인
 * @param item - 장비 아이템
 * @returns 아바타 장비 여부
 *
 * @example
 * isAvatarEquipment({ type: '아바타', name: '의상' }) // true
 */
export const isAvatarEquipment = (item: Equipment): boolean => {
  const label = inlineText(`${item.type} ${item.name}`).toLowerCase()
  return /아바타|스킨|의상|페이스|헤어|가면/.test(label)
}

/**
 * 목걸이인지 확인
 */
export const isNecklace = (item: Equipment): boolean => /목걸이/i.test(inlineText(item.type))

/**
 * 귀걸이인지 확인
 */
export const isEarring = (item: Equipment): boolean => /귀걸이/i.test(inlineText(item.type))

/**
 * 반지인지 확인
 */
export const isRing = (item: Equipment): boolean => /반지/i.test(inlineText(item.type))

/**
 * 팔찌인지 확인
 */
export const isBracelet = (item: Equipment): boolean => inlineText(item.type).includes('팔찌')

/**
 * 어빌리티 스톤인지 확인
 */
export const isAbilityStone = (item: Equipment): boolean =>
  /어빌리/i.test(inlineText(item.name) + inlineText(item.type))

/**
 * 강화 레벨 추출 (+15, +20 등)
 * @param item - 장비 아이템
 * @returns 강화 레벨 문자열
 *
 * @example
 * extractEnhancementLevel({ name: '무기 +20' }) // '20'
 * extractEnhancementLevel({ name: '투구' }) // ''
 */
export const extractEnhancementLevel = (item: Equipment): string => {
  const name = inlineText(item.name)
  const match = name.match(/\+(\d{1,2})/)
  return match?.[1] || ''
}

/**
 * 특수 장비 라벨 추출
 * @param item - 특수 장비 아이템
 * @returns 라벨 (나침반/부적/문장/보주)
 *
 * @example
 * getSpecialLabel({ type: '나침반', name: '고요의 나침반' }) // '나침반'
 */
export const getSpecialLabel = (item: Equipment): string => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  const keyword = SPECIAL_EQUIPMENT_KEYWORDS.find(word => target.includes(word.toLowerCase()))
  if (keyword) return keyword

  // fallback: 타입 또는 이름 그대로
  const fallback = inlineText(item.type || item.name)
  if (fallback.length >= 2) return fallback.slice(0, 3)
  return '특수'
}

/**
 * 특수 장비 하이라이트 추출
 * @param item - 특수 장비 아이템
 * @returns 하이라이트 문자열 배열
 *
 * @example
 * getSpecialHighlights({ tooltip: '...' }) // ['추가 피해 +10%', '무력화 +5%']
 */
export const getSpecialHighlights = (item: Equipment): string[] => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return []

  const meaningfulLines = lines.filter(
    line => line && !/^(품질|거래|귀속|분해|장착|최대|레벨|봉인)/i.test(line)
  )

  const highlightRegex = /(추가 피해|무력화|치명|신속|특화|공격력|방어력|품질|효과|피해|쿨타임)/i
  const highlights = meaningfulLines.filter(line => highlightRegex.test(line))

  return highlights.length > 0 ? highlights.slice(0, 3) : meaningfulLines.slice(0, 2)
}

/**
 * 장비 한 줄 요약
 * @param item - 장비 아이템
 * @returns 요약 문자열
 *
 * @example
 * summarizeEquipmentLine({ tooltip: '...' }) // '추가 피해 +10%'
 */
export const summarizeEquipmentLine = (item: Equipment): string => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return ''
  const highlightRegex = /(추가 피해|무력화|치명|신속|특화|공격력|방어력|품질|효과|피해|쿨타임|쿨타운)/i
  const candidate = lines.find(line => highlightRegex.test(line)) || lines[0]
  return inlineText(candidate)
}

/**
 * 장비 메타 데이터 파싱 (아이템 레벨, 품질, 초월, 재련 등)
 * @param item - 장비 아이템
 * @returns 파싱된 메타 데이터
 *
 * @example
 * parseEquipmentMeta({ tooltip: '...' })
 * // { itemLevel: 'iLv. 1690', quality: '100', transcend: '25', ... }
 */
export const parseEquipmentMeta = (item: Equipment): EquipmentMeta => {
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

    // fallback: look for standalone 초월 숫자 표기
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

  let transcend = extractTranscendValue()

  // Fallback: JSON 파싱
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

/**
 * 장비 순서 인덱스 계산 (투구, 어깨, 상의, 하의, 장갑, 무기)
 * @param label - 장비 타입 라벨
 * @returns 순서 인덱스 (0-5, 없으면 99)
 *
 * @example
 * gearOrderIndex('투구') // 0
 * gearOrderIndex('무기') // 5
 * gearOrderIndex('목걸이') // 99
 */
export const gearOrderIndex = (label: string): number => {
  const lower = inlineText(label).toLowerCase()
  const found = GEAR_ORDER.findIndex(group =>
    group.some(keyword => lower.includes(keyword.toLowerCase()))
  )
  return found === -1 ? 99 : found
}
