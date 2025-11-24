/**
 * 로스트아크 각인 파서
 * 각인 설명에서 레벨과 효과를 추출
 */

export interface ParsedEngraving {
  name: string
  level: number
  effectValue: string
  isDebuff: boolean
  rawDescription: string
}

/**
 * 각인 설명 파싱
 * 예: "바드 Lv.3 +45" -> { level: 3, effectValue: "+45" }
 */
export function parseEngravingDescription(description: string): ParsedEngraving {
  if (!description) {
    return {
      name: '',
      level: 0,
      effectValue: '',
      isDebuff: false,
      rawDescription: description
    }
  }

  // 레벨 추출 (Lv.1, Lv.2, Lv.3 등)
  const levelMatch = description.match(/Lv\.?\s*(\d+)/i)
  const level = levelMatch?.[1] ? parseInt(levelMatch[1], 10) : 0

  // 효과 값 추출 (+15, +30, -10 등)
  const effectMatch = description.match(/([+-]\s*\d+)/i)
  const effectValue = effectMatch?.[1] ? effectMatch[1].replace(/\s/g, '') : ''

  // 디버프 여부 (감소, 페널티 등의 키워드 또는 - 부호)
  const isDebuff = description.includes('감소') ||
                   description.includes('페널티') ||
                   effectValue.startsWith('-')

  // 각인명 추출 (Lv. 앞부분)
  let name = description
  if (levelMatch) {
    name = description.substring(0, levelMatch.index).trim()
  }

  return {
    name,
    level,
    effectValue,
    isDebuff,
    rawDescription: description
  }
}

/**
 * 각인 레벨에 따른 색상 반환
 */
export function getEngravingLevelColor(level: number, isDebuff: boolean = false): string {
  if (isDebuff) {
    return '#ff6b6b' // 빨강 (디버프)
  }

  switch (level) {
    case 3:
      return '#9333ea' // 보라 (Lv.3)
    case 2:
      return '#3b82f6' // 파랑 (Lv.2)
    case 1:
      return '#10b981' // 초록 (Lv.1)
    default:
      return '#6b7280' // 회색 (기타)
  }
}

/**
 * 각인 레벨 텍스트 반환
 */
export function getEngravingLevelText(level: number): string {
  if (level === 0) return ''
  return `Lv.${level}`
}

/**
 * 각인 등급 계산 (총 각인 개수와 레벨 기준)
 */
export function calculateEngravingGrade(engravings: ParsedEngraving[]): string {
  const activeEngravings = engravings.filter(e => e.level > 0 && !e.isDebuff)
  const totalLevel = activeEngravings.reduce((sum, e) => sum + e.level, 0)
  const lv3Count = activeEngravings.filter(e => e.level === 3).length

  if (lv3Count >= 6) return '최상급'
  if (lv3Count >= 5) return '상급'
  if (lv3Count >= 4) return '중상급'
  if (lv3Count >= 3) return '중급'
  if (totalLevel >= 6) return '초급'
  return '미설정'
}
