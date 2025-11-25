export type EffectAbbreviationRule = {
  pattern: RegExp
  replace: string
}

// 긴 문구 → 약어 매핑 (숫자/기타 텍스트는 그대로 유지)
const RULES: EffectAbbreviationRule[] = [
  // 공용/기본
  { pattern: /적에게\s*주는\s*피해/gi, replace: '적주피' },
  { pattern: /추가\s*피해/gi, replace: '추피' },
  { pattern: /낙인력/gi, replace: '낙' },
  { pattern: /(세레나데|신앙|조화)\s*게이지\s*획득량/gi, replace: '아덴' },
  { pattern: /최대\s*생명력/gi, replace: '최생' },

  // 반지
  { pattern: /아군\s*공격력\s*강화\s*효과/gi, replace: '아공강' },
  { pattern: /아군\s*피해량\s*강화\s*효과/gi, replace: '아피강' },
  { pattern: /치명타\s*피해/gi, replace: '치피' },
  { pattern: /치명타\s*적중률/gi, replace: '치적' },

  // 귀걸이
  { pattern: /파티원\s*보호막\s*효과/gi, replace: '보호막' },
  { pattern: /파티원\s*회복\s*효과/gi, replace: '회복' },
  { pattern: /공격력\s*감소/gi, replace: '공감' },
  { pattern: /무기\s*공격력/gi, replace: '무공' },
  { pattern: /공격력/gi, replace: '공' },

]

export const applyEffectAbbreviations = (label: string): string => {
  let result = label
  for (const rule of RULES) {
    result = result.replace(rule.pattern, rule.replace)
  }
  return result.trim()
}

export const hasAbbreviationMatch = (label: string): boolean => {
  return RULES.some(rule => rule.pattern.test(label))
}
