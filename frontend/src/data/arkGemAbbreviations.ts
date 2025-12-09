import type { EffectAbbreviationRule } from './effectAbbreviations'

// 아크 젬 툴팁 전용 약어 규칙
const GEM_SPECIFIC_RULES: EffectAbbreviationRule[] = [
  { pattern: /질서\s*포인트/gi, replace: '질서' },
  { pattern: /혼돈\s*포인트/gi, replace: '혼돈' },
  { pattern: /필요\s*의지력/gi, replace: '의지력' },
  { pattern: /아군\s*피해\s*강화/gi, replace: '아피강' },
  { pattern: /아군\s*공격\s*강화/gi, replace: '아공강' },
  { pattern: /보스\s*피해/gi, replace: '보피' },
  { pattern: /추가\s*피해/gi, replace: '추피' },
]

const RULES: EffectAbbreviationRule[] = [...GEM_SPECIFIC_RULES]

export const applyArkGemAbbreviations = (label: string): string => {
  let result = label
  for (const rule of RULES) {
    result = result.replace(rule.pattern, rule.replace)
  }
  return result.trim()
}

export const hasArkGemAbbreviation = (label: string): boolean => {
  return RULES.some(rule => {
    rule.pattern.lastIndex = 0
    return rule.pattern.test(label)
  })
}
