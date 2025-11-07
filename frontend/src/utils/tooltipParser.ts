/**
 * 로스트아크 장비 툴팁 파서
 * 복잡한 JSON 구조의 툴팁을 구조화된 데이터로 변환
 */

export interface ParsedTooltip {
  title?: string
  grade?: string
  quality?: number
  itemLevel?: string
  basicStats?: StatItem[]
  additionalStats?: StatItem[]
  elixirEffects?: string[]
  setEffects?: SetEffect[]
  engravingEffects?: string[]
  rawElements?: Record<string, any>
}

export interface StatItem {
  type: string
  value: string
}

export interface SetEffect {
  setName: string
  effects: string[]
  activePieces?: number
}

/**
 * HTML 태그 제거 및 텍스트 추출
 */
function stripHtml(html: string): string {
  if (!html) return ''
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .trim()
}

/**
 * HTML에서 색상 정보 추출
 */
function extractColor(html: string): string | null {
  const colorMatch = html.match(/color:\s*#([0-9a-fA-F]{6})/i)
  return colorMatch ? `#${colorMatch[1]}` : null
}

/**
 * 품질 값 추출
 */
function parseQuality(html: string): number | null {
  const text = stripHtml(html)
  const match = text.match(/품질.*?(\d+)/i)
  return match ? parseInt(match[1], 10) : null
}

/**
 * 아이템 레벨 추출
 */
function parseItemLevel(html: string): string | null {
  const text = stripHtml(html)
  const match = text.match(/아이템\s*레벨\s*(\d+)/i)
  return match ? match[1] : null
}

/**
 * 스탯 파싱
 */
function parseStats(html: string): StatItem[] {
  if (!html) return []

  const text = stripHtml(html)
  const lines = text.split('\n').filter(line => line.trim())
  const stats: StatItem[] = []

  for (const line of lines) {
    // "힘 +1234" 또는 "치명 +567" 형태 파싱
    const match = line.match(/^(.+?)\s*\+?\s*(\d+(?:,\d+)*)/)
    if (match) {
      stats.push({
        type: match[1].trim(),
        value: match[2]
      })
    } else if (line.trim()) {
      // 다른 형태의 스탯
      stats.push({
        type: line.trim(),
        value: ''
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
    }

    // Element_001: 품질 정보
    if (tooltip.Element_001) {
      const qualityHtml = typeof tooltip.Element_001 === 'string'
        ? tooltip.Element_001
        : JSON.stringify(tooltip.Element_001)
      const quality = parseQuality(qualityHtml)
      if (quality !== null) {
        parsed.quality = quality
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
    }

    // Element_006: 세트 효과 또는 각인
    if (tooltip.Element_006) {
      const html = typeof tooltip.Element_006 === 'string'
        ? tooltip.Element_006
        : JSON.stringify(tooltip.Element_006)
      const text = stripHtml(html)

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

      if (text.includes('각인')) {
        if (!parsed.engravingEffects) parsed.engravingEffects = []
        parsed.engravingEffects.push(...parseEngravingEffects(html))
      } else if (text.includes('엘릭서')) {
        if (!parsed.elixirEffects) parsed.elixirEffects = []
        parsed.elixirEffects.push(...parseElixirEffects(html))
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
