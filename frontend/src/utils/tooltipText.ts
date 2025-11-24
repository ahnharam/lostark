import { stripHtml } from './tooltipParser'

export interface FlattenTooltipOptions {
  /**
   * 객체에 value 키가 있으면 우선적으로 내려가고, 없으면 모든 값을 순회합니다.
   */
  preferValueField?: boolean
  /**
   * 긴 문장을 자동으로 줄바꿈할지 여부
   */
  fallbackBreaks?: boolean
}

export function sanitizeInline(value?: string | number | null): string {
  if (value === null || value === undefined) return ''
  return stripHtml(String(value))
    .replace(/\\r\\n|\\n|\\r/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

export function addFallbackLineBreaks(value: string): string {
  if (!value) return value
  if (value.includes('\n')) return value

  const insertSentenceBreaks = (text: string) =>
    text.replace(/(?<!\d)([.!?])\s+/g, (_, mark) => `${mark}\n`)

  const insertStatBreaks = (text: string) => text.replace(/\s+(?=[+-]\d)/g, '\n')

  const formatLines = (text: string) =>
    text
      .split('\n')
      .map(part => part.trim())
      .filter(Boolean)
      .join('\n')

  const withSentenceBreaks = insertSentenceBreaks(value)
  if (withSentenceBreaks.includes('\n')) {
    return formatLines(withSentenceBreaks)
  }

  const withStatBreaks = insertStatBreaks(value)
  if (withStatBreaks.includes('\n')) {
    return formatLines(withStatBreaks)
  }

  if (value.length <= 80) return value

  const segments: string[] = []
  let start = 0
  const length = value.length
  while (start < length) {
    let end = Math.min(start + 70, length)
    if (end === length) {
      segments.push(value.slice(start).trim())
      break
    }
    let breakIndex = value.lastIndexOf(' ', end)
    if (breakIndex <= start + 30) {
      breakIndex = value.indexOf(' ', end)
    }
    if (breakIndex === -1) {
      segments.push(value.slice(start).trim())
      break
    }
    segments.push(value.slice(start, breakIndex).trim())
    start = breakIndex + 1
  }
  return segments.join('\n')
}

export function cleanTooltipLine(value: string, options: { fallbackBreaks?: boolean } = {}): string {
  if (!value) return ''
  const normalized = stripHtml(value)
    .replace(/\\r\\n|\\n|\\r/g, '\n')
    .split('\n')
    .map(part => part.replace(/\s+/g, ' ').trim())
    .filter(Boolean)
    .join('\n')
  return options.fallbackBreaks ? addFallbackLineBreaks(normalized) : normalized
}

export function flattenTooltipLines(
  tooltip?: string | null,
  options: FlattenTooltipOptions = {}
): string[] {
  if (!tooltip) return []
  const lines: string[] = []
  const preferValueField = options.preferValueField ?? true

  const visit = (node: unknown) => {
    if (node === null || node === undefined) return
    if (typeof node === 'string') {
      const normalized = node
        .replace(/\\r\\n|\\n|\\r/g, '\n')
        .split('\n')
        .map(part => part.trim())
        .filter(Boolean)
      lines.push(...normalized)
      return
    }
    if (Array.isArray(node)) {
      node.forEach(visit)
      return
    }
    if (typeof node === 'object') {
      const record = node as Record<string, unknown>
      if (preferValueField && 'value' in record) {
        visit(record.value)
        return
      }
      Object.values(record).forEach(visit)
    }
  }

  try {
    visit(JSON.parse(String(tooltip)))
  } catch {
    visit(tooltip)
  }

  return lines
    .map(line => cleanTooltipLine(line, { fallbackBreaks: options.fallbackBreaks }))
    .filter(Boolean)
}

export function extractTooltipColor(tooltip?: string | null): string | null {
  if (!tooltip) return null

  const searchColor = (value: unknown): string | null => {
    if (!value) return null
    if (typeof value === 'string') {
      const colorMatch = value.match(/color=['"]?([#\\w]+)['"]?/i)
      const raw = colorMatch?.[1]
      if (raw) {
        const normalized = raw.toUpperCase()
        return normalized.startsWith('#') ? normalized : `#${normalized}`
      }
    } else if (Array.isArray(value)) {
      for (const item of value) {
        const found = searchColor(item)
        if (found) return found
      }
    } else if (typeof value === 'object') {
      for (const child of Object.values(value)) {
        const found = searchColor(child)
        if (found) return found
      }
    }
    return null
  }

  try {
    const parsed = JSON.parse(String(tooltip))
    return searchColor(parsed)
  } catch {
    const directMatch = tooltip.match(/color=['"]?([#\\w]+)['"]?/i)
    const raw = directMatch?.[1]
    if (raw) {
      const normalized = raw.toUpperCase()
      return normalized.startsWith('#') ? normalized : `#${normalized}`
    }
    return null
  }
}
