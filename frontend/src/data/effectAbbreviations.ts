import { ENGRAVING_NAME_ENTRIES } from './engravingNames'

export type EffectAbbreviationRule = {
  pattern: RegExp
  replace: string
}

// 긴 문구 → 약어 매핑 (숫자/기타 텍스트는 그대로 유지)
// 세분화된 카테고리를 두고 최종 RULES에서 병합합니다.
const ENGRAVING_RULES: EffectAbbreviationRule[] = ENGRAVING_NAME_ENTRIES.map(
  ({ full, short }) => ({
    pattern: new RegExp(full.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi'),
    replace: short
  })
)

const DEMERIT_RULES: EffectAbbreviationRule[] = [
  // TODO: 감소효과 약어를 추가하세요.
  { pattern: /공격력\s*감소\s*/gi, replace: '공감' },
  { pattern: /방어력\s*감소\s*/gi, replace: '방감' },
  { pattern: /이동\s*속도\s*감소\s*/gi, replace: '이속감' },
  { pattern: /공격\s*속도\s*감소\s*/gi, replace: '공속감' },
]

const DEALER_RULES: EffectAbbreviationRule[] = [
  // TODO: 딜러용 약어를 추가하세요.
  // 목걸이
  { pattern: /적에게\s*주는\s*피해/gi, replace: '적주피' },
  { pattern: /추가\s*피해/gi, replace: '추피' },

  // 귀걸이
  { pattern: /무기\s*공격력/gi, replace: '무공' },
  { pattern: /공격력/gi, replace: '공' },

  // 반지
  { pattern: /치명타\s*피해/gi, replace: '치피' },
  { pattern: /치명타\s*적중률/gi, replace: '치적' },
]

const SUPPORT_RULES: EffectAbbreviationRule[] = [
  // TODO: 서폿용 약어를 추가하세요.

  // 목걸이
  { pattern: /낙인력/gi, replace: '낙인' },
  { pattern: /세레나데\s*신앙\s*조화\s*게이지\s*획득량/gi, replace: '아덴' },

  // 귀걸이
  { pattern: /파티원\s*보호막\s*효과/gi, replace: '보호막' },
  { pattern: /파티원\s*회복\s*효과/gi, replace: '회복' },
  { pattern: /무기\s*공격력/gi, replace: '무공'  },

  // 반지
  { pattern: /아군\s*공격력\s*강화\s*효과/gi, replace: '아공강' },
  { pattern: /아군\s*피해량\s*강화\s*효과/gi, replace: '아피강' },
]

const COMMON_RULES: EffectAbbreviationRule[] = [
  // 공용/기본
  { pattern: /최대\s*생명력/gi, replace: '최생' },
  { pattern: /무기\s*공격력/gi, replace: '무공' },
  { pattern: /공격력/gi, replace: '공' },
]

const RULES: EffectAbbreviationRule[] = [
  ...DEMERIT_RULES,
  ...ENGRAVING_RULES,
  ...SUPPORT_RULES,
  ...DEALER_RULES,
  ...COMMON_RULES
]

export const DEALER_ABBREVIATIONS = Array.from(
  new Set(DEALER_RULES.map(rule => rule.replace).filter(Boolean))
)

export const SUPPORT_ABBREVIATIONS = Array.from(
  new Set(SUPPORT_RULES.map(rule => rule.replace).filter(Boolean))
)

export const EFFECT_ABBREVIATION_REPLACEMENTS = Array.from(
  new Set(RULES.map(rule => rule.replace).filter(Boolean))
)

const DEMERIT_LABELS: Record<string, string> = {
  '공감': '공격력 감소',
  '방감': '방어력 감소',
  '이속감': '이동 속도 감소',
  '공속감': '공격 속도 감소'
}

const DEMERIT_EXPANSIONS = DEMERIT_RULES.map(rule => {
  const short = rule.replace
  const full = DEMERIT_LABELS[short] || short
  // []가 둘러싸인 경우에도 치환되도록 선택적 대괄호 허용
  const regex = new RegExp(`\\[?${short}\\]?`, 'g')
  return { short, full, regex }
})

export const applyEffectAbbreviations = (label: string): string => {
  let result = label
  for (const rule of RULES) {
    result = result.replace(rule.pattern, rule.replace)
  }
  return result.trim()
}

export const hasAbbreviationMatch = (label: string): boolean => {
  return RULES.some(rule => {
    rule.pattern.lastIndex = 0
    return rule.pattern.test(label)
  })
}

export const expandDemeritAbbreviation = (text?: string) => {
  if (!text) return ''
  return DEMERIT_EXPANSIONS.reduce((acc, entry) => acc.replace(entry.regex, entry.full), text)
}
