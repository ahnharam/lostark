/**
 * 로스트아크 장비 툴팁 파서
 * 복잡한 JSON 구조의 툴팁을 구조화된 데이터로 변환
 */

import { isRecord, isString } from './typeGuards'

export interface ParsedTooltip {
  title?: string
  titleColor?: string
  grade?: string
  quality?: number
  itemLevel?: string
  gradeColor?: string
  weaponAttackStat?: StatItem
  additionalEffectStat?: StatItem
  sangjaeStat?: StatItem
  sangjaeStage?: number
  sangjaeValue?: string
  transcendenceSummary?: string
  transcendenceBaseEffect?: string
  transcendenceMaxStage?: string
  transcendenceStages?: string[]
  transcendenceAggregates?: TranscendenceAggregates
  elixirSummary?: string
  elixirStageSummary?: string
  basicStats?: StatItem[]
  additionalStats?: StatItem[]
  elixirEffects?: string[]
  setEffects?: SetEffect[]
  engravingEffects?: string[]
  abilityStoneEngravings?: string[]
  rawElements?: Record<string, unknown>
}

export interface StatItem {
  type: string
  value: string
  color?: string
  richValue?: string
}

export interface SetEffect {
  setName: string
  effects: string[]
  activePieces?: number
}

export interface TranscendenceAggregates {
  weaponAttack?: number
  brandPercent?: number
  allyBuffPercent?: number
}

/**
 * HTML 태그 제거 및 텍스트 추출
 */
interface StripHtmlOptions {
  preserveColor?: boolean
}

export function stripHtml(html: string, options: StripHtmlOptions = {}): string {
  if (!html) return ''
  let working = html
  if (options.preserveColor) {
    working = working.replace(
      /<font[^>]*color=['"]([^'"]+)['"][^>]*>(.*?)<\/font>/gi,
      (_, color, inner) => `<span style="color:${color}">${inner}</span>`
    )
  }
  return working
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .trim()
}

function cleanText(input?: string | null): string {
  if (!input) return ''
  return stripHtml(input)
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * 품질 값 추출
 */
function parseQualityText(input: string): number | null {
  const text = stripHtml(input)
  const match = text.match(/품질[^0-9]*(\d{1,3})/i)
  const quality = match?.[1]
  return quality ? parseInt(quality, 10) : null
}

function toNumber(value: unknown): number | null {
  if (value === null || value === undefined) return null
  if (typeof value === 'number') return Number.isNaN(value) ? null : value
  if (typeof value === 'string' && value.trim().length) {
    const numeric = parseInt(value.replace(/[^\d]/g, ''), 10)
    return Number.isNaN(numeric) ? null : numeric
  }
  return null
}

function parseQuality(element: unknown): number | null {
  if (!element) return null
  if (isRecord(element)) {
    const rawValue = element.value ?? element.Value
    if (isRecord(rawValue)) {
      const direct =
        toNumber(rawValue.QualityValue) ??
        toNumber(rawValue.qualityValue) ??
        toNumber(rawValue.ProgressValue) ??
        toNumber(rawValue.progressValue)
      if (direct !== null) {
        return direct
      }
      const leftStr = rawValue.leftStr
      const rightStr = rawValue.rightStr
      if (leftStr || rightStr) {
        const normalized = [leftStr, rightStr]
          .filter((str): str is unknown => Boolean(str))
          .map(str => parseQualityText(String(str)))
          .find(q => q !== null)
        if (normalized !== undefined) {
          return normalized ?? null
        }
      }
    }
  }
  
  const serialized = typeof element === 'string' ? element : JSON.stringify(element)
  return parseQualityText(serialized)
}

/**
 * 아이템 레벨 추출
 */
function parseItemLevel(html: string): string | null {
  const text = stripHtml(html)
  const match = text.match(/아이템\s*레벨\s*(\d+)/i)
  return match?.[1] ?? null
}

/**
 * 스탯 파싱
 */
function normalizeNewlines(input: string): string {
  return input.replace(/\\n/g, '\n').replace(/<br\s*\/?>/gi, '\n')
}

function extractFontColor(html: string): string | undefined {
  const match = html.match(/color=['"]?#?([0-9a-fA-F]{6})['"]?/i)
  const color = match?.[1]
  return color ? `#${color.toUpperCase()}` : undefined
}

function flattenValue(value: unknown): string[] {
  if (value == null) return []
  if (typeof value === 'string') return [value]
  if (Array.isArray(value)) {
    return value.flatMap(flat => flattenValue(flat))
  }
  if (isRecord(value)) {
    return Object.values(value).flatMap(flattenValue)
  }
  return []
}

function extractStatFragments(input: string): string[] {
  if (!input) return []
  const trimmed = input.trim()
  if (
    (trimmed.startsWith('{') && trimmed.endsWith('}')) ||
    (trimmed.startsWith('[') && trimmed.endsWith(']'))
  ) {
    try {
      const parsed = JSON.parse(trimmed)
      const flattened = flattenValue(parsed)
      if (flattened.length) {
        return flattened
          .map(fragment => fragment?.toString?.() ?? '')
          .map(fragment => normalizeNewlines(fragment))
          .flatMap(fragment =>
            fragment.split('\n').map(line => line.trim()).filter(Boolean)
          )
      }
    } catch {
      // fall back to plain text parsing
    }
  }

  return normalizeNewlines(input)
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)
}

function extractIndentGroupLines(group: unknown): string[] {
  if (!isRecord(group)) return []
  const element000 = group.Element_000
  if (!isRecord(element000)) return []
  const content = element000.contentStr
  if (!isRecord(content)) return []
  return Object.values(content)
    .map(entry => (isRecord(entry) ? entry.contentStr : null))
    .filter((line): line is string => typeof line === 'string')
    .map(line => stripHtml(line).replace(/\s+/g, ' ').trim())
    .filter(Boolean)
}

function parseStats(html: string): StatItem[] {
  if (!html) return []

  const rawLines = extractStatFragments(html)
  const stats: StatItem[] = []

  for (const rawLine of rawLines) {
    const color = extractFontColor(rawLine)
    const plain = stripHtml(rawLine)
    const plusIndex = plain.indexOf('+')
    if (plusIndex === -1) {
      continue
    }
    const type = plain.slice(0, plusIndex).trim()
    const value = plain.slice(plusIndex + 1).trim()
    if (type && value && !type.startsWith('{')) {
      stats.push({
        type,
        value,
        color
      })
    }
  }

  return stats
}

/**
 * 세트 효과 파싱
 */
function parseSetEffects(html: string): SetEffect[] {
  if (!html) return []

  const text = stripHtml(html)
  const lines = text.split('\n').filter(line => line.trim())
  const setEffects: SetEffect[] = []

  let currentSet: SetEffect | null = null

  for (const line of lines) {
    // 세트 이름 감지 (보통 "[세트]" 또는 세트명 포함)
    if (line.includes('세트') && !line.includes('효과')) {
      if (currentSet) {
        setEffects.push(currentSet)
      }
      currentSet = {
        setName: line.trim(),
        effects: []
      }
    } else if (currentSet && line.trim()) {
      // 세트 효과 추가
      currentSet.effects.push(line.trim())
    }
  }

  if (currentSet) {
    setEffects.push(currentSet)
  }

  return setEffects
}

/**
 * 엘릭서 효과 파싱
 */
function parseElixirEffects(html: string): string[] {
  if (!html) return []

  const text = stripHtml(html)
  return text
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
}

/**
 * 각인 효과 파싱
 */
function parseEngravingEffects(html: string): string[] {
  if (!html) return []

  const text = stripHtml(html)
  return text
    .split('\n')
    .map(line => line.trim())
    .filter(line => line.length > 0)
}

const ELIXIR_TOPSTR_HEADER = /연성\s*추가\s*효과/i

function extractElixirTopStrDetail(raw: string): { headline?: string; detail?: string } {
  const lines = stripHtml(raw)
    .split('\n')
    .map(line => line.trim())
    .filter(Boolean)

  if (!lines.length) {
    return {}
  }

  const headline = lines[0]
  const detail = lines.find(line => !ELIXIR_TOPSTR_HEADER.test(line)) ?? lines[lines.length - 1]

  return { headline, detail }
}

function findNestedTopStr(value: unknown): string | undefined {
  if (!value) return undefined
  if (!isRecord(value)) return undefined
  if (isString(value.topStr)) {
    return value.topStr
  }
  for (const nested of Object.values(value)) {
    const found = findNestedTopStr(nested)
    if (found) return found
  }
  return undefined
}

/**
 * 메인 툴팁 파서
 */
export function parseTooltip(tooltipJson: string): ParsedTooltip {
  if (!tooltipJson) {
    return {}
  }

  try {
    const tooltip = JSON.parse(tooltipJson)
    const parsed: ParsedTooltip = {
      rawElements: tooltip
    }

    // Element_000: 아이템 제목 및 등급
    if (tooltip.Element_000) {
      const titleHtml = typeof tooltip.Element_000 === 'string'
        ? tooltip.Element_000
        : JSON.stringify(tooltip.Element_000)
      parsed.title = stripHtml(titleHtml)
      parsed.titleColor = extractFontColor(titleHtml)
    }

    // Element_001: 품질 및 아이템 레벨 정보
    if (tooltip.Element_001) {
      const quality = parseQuality(tooltip.Element_001)
      if (quality !== null) {
        parsed.quality = quality
      }
      if (typeof tooltip.Element_001 === 'object') {
        const element001 = tooltip.Element_001 as unknown
        if (isRecord(element001)) {
          const valueBlock = element001['value'] ?? element001['Value']
          if (isRecord(valueBlock)) {
            const leftStr0 = valueBlock['leftStr0']
            if (leftStr0) {
              parsed.gradeColor = extractFontColor(
                typeof leftStr0 === 'string' ? leftStr0 : JSON.stringify(leftStr0)
              )
            }
            const leftStr2 = valueBlock['leftStr2'] ?? valueBlock['leftStr1'] ?? valueBlock['leftStr0']
            if (leftStr2 && !parsed.itemLevel) {
              const level = parseItemLevel(
                typeof leftStr2 === 'string' ? leftStr2 : JSON.stringify(leftStr2)
              )
              if (level) {
                parsed.itemLevel = level
              }
            }
          }
        }
      }
    }

    // Element_002~004: 기본 스탯 및 추가 효과
    const statsElements = []
    for (let i = 2; i <= 4; i++) {
      const key = `Element_00${i}`
      if (tooltip[key]) {
        const html = typeof tooltip[key] === 'string'
          ? tooltip[key]
          : JSON.stringify(tooltip[key])

        // 아이템 레벨 체크
        const itemLevel = parseItemLevel(html)
        if (itemLevel) {
          parsed.itemLevel = itemLevel
        }

        const stats = parseStats(html)
        if (stats.length > 0) {
          statsElements.push(...stats)
        }
      }
    }

    // 스탯을 기본/추가로 분리 (간단하게 처리)
    if (statsElements.length > 0) {
      const midPoint = Math.ceil(statsElements.length / 2)
      parsed.basicStats = statsElements.slice(0, midPoint)
      if (statsElements.length > midPoint) {
        parsed.additionalStats = statsElements.slice(midPoint)
      }
    }

    // Element_005: 엘릭서 효과 (보통)
    if (tooltip.Element_005) {
      const html = typeof tooltip.Element_005 === 'string'
        ? tooltip.Element_005
        : JSON.stringify(tooltip.Element_005)
      const text = stripHtml(html)

      if (text.includes('엘릭서')) {
        parsed.elixirEffects = parseElixirEffects(html)
      } else if (text.includes('세트')) {
        parsed.setEffects = parseSetEffects(html)
      } else {
        // 기타 추가 효과
        if (!parsed.additionalStats) parsed.additionalStats = []
        parsed.additionalStats.push(...parseStats(html))
      }

      const stageMatches = [...text.matchAll(/(\d+)\s*단계\s*-\s*([^\n]+)/gi)]
      if (stageMatches.length) {
        const best = stageMatches.reduce<{ stage: number; value: string }>((acc, match) => {
          const stage = Number(match[1])
          if (Number.isNaN(stage)) {
            return acc
          }
          if (stage > acc.stage) {
            return { stage, value: match[2] ?? '' }
          }
          return acc
        }, { stage: -Infinity, value: '' })
        if (best.stage > -Infinity) {
          parsed.sangjaeStage = best.stage
          const formatted = best.value.replace(/\s+/g, ' ').trim()
          parsed.sangjaeValue = formatted
        }
      }
      if (!parsed.sangjaeStage) {
        const stageMatch = text.match(/상급\s*재련[^0-9]*(\d+)/i)
        if (stageMatch) {
          parsed.sangjaeStage = Number(stageMatch[1])
        }
      }
    }

    // Element_006: 세트 효과 또는 각인
    if (tooltip.Element_006) {
      const html = typeof tooltip.Element_006 === 'string'
        ? tooltip.Element_006
        : JSON.stringify(tooltip.Element_006)
      const text = stripHtml(html)
      const stats = parseStats(html)
      if (stats.length) {
        const weaponAttack = stats.find(stat => stat.type.replace(/\s/g, '').includes('무기공격력'))
        if (weaponAttack && !parsed.weaponAttackStat) {
          parsed.weaponAttackStat = weaponAttack
        }
        const sangjae = stats.find(stat =>
          /상(재|급재련|제)/.test(stat.type.replace(/\s/g, ''))
        )
        if (sangjae && !parsed.sangjaeStat) {
          parsed.sangjaeStat = sangjae
        }
      }

      if (text.includes('세트')) {
        parsed.setEffects = parseSetEffects(html)
      } else if (text.includes('각인')) {
        parsed.engravingEffects = parseEngravingEffects(html)
      }
    }

    // Element_007: 추가 각인 또는 기타 효과
    if (tooltip.Element_007) {
      const html = typeof tooltip.Element_007 === 'string'
        ? tooltip.Element_007
        : JSON.stringify(tooltip.Element_007)
      const text = stripHtml(html)
      const stats = parseStats(html)
      if (stats.length && !parsed.sangjaeStat) {
        const sangjae = stats.find(stat =>
          /상(재|급재련|제)/.test(stat.type.replace(/\s/g, ''))
        )
        if (sangjae) {
          parsed.sangjaeStat = sangjae
        }
      }

      if (text.includes('각인')) {
        if (!parsed.engravingEffects) parsed.engravingEffects = []
        parsed.engravingEffects.push(...parseEngravingEffects(html))
      } else if (text.includes('엘릭서')) {
        if (!parsed.elixirEffects) parsed.elixirEffects = []
        parsed.elixirEffects.push(...parseElixirEffects(html))
      }

      const value =
        tooltip.Element_007 && typeof tooltip.Element_007 === 'object'
          ? (() => {
              const element = tooltip.Element_007 as unknown
              if (!isRecord(element)) return undefined
              return element['value'] ?? element['Value']
            })()
          : undefined
      const topStr = (() => {
        if (!isRecord(value)) return ''
        const top = value['topStr']
        return isString(top) ? cleanText(top) : ''
      })()
      const indentLines = extractIndentGroupLines(value)
      const abilityStoneLines = [topStr, ...indentLines]
        .map(line => cleanText(line))
        .filter(Boolean)
      if (abilityStoneLines.length) {
        parsed.abilityStoneEngravings = abilityStoneLines
      }
    }

    // Element_008: 추가 효과
    if (tooltip.Element_008) {
      const html = typeof tooltip.Element_008 === 'string'
        ? tooltip.Element_008
        : JSON.stringify(tooltip.Element_008)
      const stats = parseStats(html)
      if (stats.length) {
        const additionalEffect = stats.find(stat =>
          stat.type.includes('추가 피해') || stat.type.includes('추가피해') || stat.type.includes('추가 효과')
        )
        if (additionalEffect) {
          parsed.additionalEffectStat = additionalEffect
        }
      }
    }

    // Element_010: 초월 요약
    if (tooltip.Element_010?.value) {
      const lines = extractIndentGroupLines(tooltip.Element_010.value)
      if (lines.length) {
        const summaryLine = lines.find(line => line.includes('모든 장비'))
        if (summaryLine) {
          parsed.transcendenceSummary = summaryLine
        }
        const baseLine = lines.find(
          line => !line.includes('모든 장비') && !line.includes('-')
        )
        if (baseLine) {
          parsed.transcendenceBaseEffect = baseLine
        }
        const stageLines = lines.filter(line => line.includes(' - '))
        if (stageLines.length) {
          parsed.transcendenceStages = stageLines
          parsed.transcendenceMaxStage = stageLines[stageLines.length - 1]
          const aggregates = extractTranscendenceAggregates(stageLines)
          if (aggregates) {
            parsed.transcendenceAggregates = aggregates
          }
        }
      }
    }

    // Element_011~012: 엘릭서 정보
    if (tooltip.Element_011?.value?.topStr) {
      parsed.elixirSummary = cleanText(tooltip.Element_011.value.topStr)
    }

    if (tooltip.Element_012?.value) {
      const topStr = findNestedTopStr(tooltip.Element_012.value)
      if (topStr) {
        const { headline, detail } = extractElixirTopStrDetail(topStr)
        if (detail) {
          parsed.elixirStageSummary = detail
        } else {
          parsed.elixirStageSummary = cleanText(topStr)
        }
        if (!parsed.elixirSummary && headline) {
          parsed.elixirSummary = headline
        }
      }
      if (!parsed.elixirStageSummary) {
        const lines = extractIndentGroupLines(tooltip.Element_012.value)
        if (lines.length) {
          parsed.elixirStageSummary = lines[lines.length - 1]
        }
      }
      if (!parsed.elixirSummary) {
        if (!topStr) {
          const lines = extractIndentGroupLines(tooltip.Element_012.value)
          if (lines.length) {
            parsed.elixirSummary = lines[0]
          }
        } else {
          parsed.elixirSummary = cleanText(topStr)
        }
      }
    }

    return parsed
  } catch (error) {
    console.error('Tooltip parsing error:', error)
    return { rawElements: { error: 'Failed to parse tooltip' } }
  }
}

/**
 * 간단한 품질 색상 반환
 */
export function getQualityColor(quality?: number): string {
  if (!quality) return '#999999'
  if (quality >= 90) return '#ff9900' // 오렌지
  if (quality >= 70) return '#9933ff' // 보라
  if (quality >= 50) return '#3366ff' // 파랑
  if (quality >= 30) return '#33cc33' // 초록
  return '#999999' // 회색
}

/**
 * 등급별 색상 반환
 */
export function getGradeColor(grade: string): string {
  const gradeMap: Record<string, string> = {
    '고대': '#ff6b35',
    '유물': '#ff9900',
    '전설': '#9933ff',
    '영웅': '#3366ff',
    '희귀': '#33cc33',
    '일반': '#999999'
  }

  return gradeMap[grade] || '#999999'
}
function parseNumber(value: string): number {
  const numeric = Number(value.replace(/,/g, ''))
  return Number.isNaN(numeric) ? 0 : numeric
}

function extractTranscendenceAggregates(lines: string[]): TranscendenceAggregates | null {
  let weaponAttack = 0
  let brandPercent = 0
  let allyBuffPercent = 0

  lines.forEach(line => {
    const clean = stripHtml(line)
    for (const match of clean.matchAll(/공격력[^\d%]*([0-9,]+)/g)) {
      const numericText = match[1] ?? ''
      weaponAttack += parseNumber(numericText)
    }
    for (const match of clean.matchAll(/낙인력[^\d%]*([0-9.]+)\s*%/gi)) {
      const percent = match[1]
      if (percent) {
        brandPercent += Number(percent)
      }
    }
    for (const match of clean.matchAll(/아군\s*공격력\s*강화\s*효과[^\d%]*([0-9.]+)\s*%/gi)) {
      const percent = match[1]
      if (percent) {
        allyBuffPercent += Number(percent)
      }
    }
  })

  const aggregates: TranscendenceAggregates = {}
  if (weaponAttack > 0) aggregates.weaponAttack = weaponAttack
  if (brandPercent > 0) aggregates.brandPercent = Number(brandPercent.toFixed(2))
  if (allyBuffPercent > 0) aggregates.allyBuffPercent = Number(allyBuffPercent.toFixed(2))

  return Object.keys(aggregates).length ? aggregates : null
}
