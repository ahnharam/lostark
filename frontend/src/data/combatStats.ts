const normalizeStat = (value?: string | null) => (value || '').replace(/\s+/g, '').toLowerCase()

export const COMBAT_STATS = ['치명', '특화', '제압', '신속', '인내', '숙련'] as const
export type CombatStat = (typeof COMBAT_STATS)[number]

export const COMBAT_STAT_SET = new Set(COMBAT_STATS.map(normalizeStat))

export const isCombatStat = (label?: string | null) => {
  if (!label) return false
  return COMBAT_STAT_SET.has(normalizeStat(label))
}
