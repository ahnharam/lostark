export type EffectAbbreviationRule = {
  pattern: RegExp
  replace: string | ((...groups: string[]) => string)
}

const RULES: EffectAbbreviationRule[] = [
  // 귀걸이/공용
  { pattern: /적에게\s*주는\s*피해/gi, replace: '적주피' },
  { pattern: /추가\s*피해/gi, replace: '추피' },
  { pattern: /낙인력/gi, replace: '낙' },
  { pattern: /(세레나데|신앙|조화)\s*게이지\s*획득량/gi, replace: '아덴' },

  // 귀걸이 추가
  {
    pattern: /무기\s*공격력\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `무공 ${val}%`
  },
  {
    pattern: /공격력\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `공 ${val}%`
  },
  {
    pattern: /파티원\s*회복\s*효과\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `회복 ${val}%`
  },
  {
    pattern: /파티원\s*보호막\s*효과\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `보호막 ${val}%`
  },

  // 반지
  {
    pattern: /아군\s*공격력\s*강화\s*효과\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `아공강 ${val}%`
  },
  {
    pattern: /아군\s*피해량\s*강화\s*효과\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `아피강 ${val}%`
  },
  {
    pattern: /치명타\s*피해\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `치피 ${val}%`
  },
  {
    pattern: /치명타\s*적중률\s*\+?\s*([\d.]+)%?/gi,
    replace: (_m, val) => `치적 ${val}%`
  },

  // 공용 옵션
  { pattern: /무기\s*공격력/gi, replace: '무공' },
  { pattern: /공격력/gi, replace: '공' },
  { pattern: /최대\s*생명력/gi, replace: '최생' }
]

export const applyEffectAbbreviations = (label: string): string => {
  let result = label
  for (const rule of RULES) {
    const next = result.replace(rule.pattern, (...args) => {
      const groups = args.slice(1, -2) as string[]
      if (typeof rule.replace === 'function') {
        return (rule.replace as (...groups: string[]) => string)(...groups)
      }
      return rule.replace
    })
    result = next
  }
  return result.trim()
}

export const hasAbbreviationMatch = (label: string): boolean => {
  return RULES.some(rule => rule.pattern.test(label))
}
