/**
 * 스킬(Skill) 데이터 관리 Composable
 *
 * CharacterSearch.vue에서 추출한 스킬 및 보석 관련 비즈니스 로직입니다.
 * 스킬 카탈로그, 보석 매핑, 스킬 하이라이트, 미장착 보석 처리를 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { ArmoryGem, ArmoryGemItem, ArmoryGemEffectSkill } from '@/api/types/armory'
import type { SkillMenuResponse, CombatSkill, SkillGem } from '@/api/types/skills'
import { sanitizeInline } from '@/utils/tooltipText'
import { isRecord, isString } from '@/utils/typeGuards'
import {
  normalizeSkillKey,
  extractGemLabel,
  classifyGemEffect,
  isAwakeningSkill,
  extractSkillNameFromGemTooltip,
  normalizeGemSkillKey,
  skillNameFromGem,
  pickHigherGem,
  formatLevelLabel,
  type GemEffectPlacement,
  type SkillGemSlot,
  type CombatSkillCatalogEntry
} from '@/utils/character/skillDataTransform'

// ============================================================================
// Types
// ============================================================================

export interface SkillHighlight {
  key: string
  name: string
  icon: string
  levelLabel?: string
  pointLabel?: string
  rune: { name: string; icon?: string; grade?: string; color?: string } | null
  tripods: {
    key: string
    name: string
    icon?: string
    slotIndex: number
    slotLabel: string
    levelLabel?: string
  }[]
  gems: { damage: SkillGemSlot | null; cooldown: SkillGemSlot | null }
  hasGem: boolean
}

export interface LooseGem {
  key: string
  name: string
  skillName: string
  icon: string
  skillIcon: string
  levelLabel?: string
  grade: string
  effectLabel: string
  typeLabel: string
}

export interface ArmoryGemIconMaps {
  skillByKey: Map<string, string>
  skillByName: Map<string, string>
  gemTooltipIconByName: Map<string, string>
}

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
 * Record에서 문자열 값 읽기
 */
const readStringFromRecord = (record: Record<string, unknown>, key: string): string | undefined => {
  const value = record[key]
  if (typeof value === 'string') return value
  return undefined
}

/**
 * Record에서 숫자 값 읽기
 */
const readNumberFromRecord = (record: Record<string, unknown>, key: string): number | undefined => {
  const value = record[key]
  if (typeof value === 'number') return value
  return undefined
}

/**
 * 툴팁에서 스킬 아이콘 파싱
 */
const parseSkillIconFromTooltip = (tooltip?: string | null): string => {
  if (!tooltip) return ''
  const pick = (icon?: string | null) => (typeof icon === 'string' && icon.trim().length ? icon.trim() : '')
  const search = (node: unknown): string => {
    if (!node) return ''
    if (typeof node === 'string') return ''
    if (Array.isArray(node)) {
      for (const item of node) {
        const found = search(item)
        if (found) return found
      }
      return ''
    }
    if (isRecord(node)) {
      if (pick(readStringFromRecord(node, 'iconPath'))) return pick(readStringFromRecord(node, 'iconPath'))
      if (pick(readStringFromRecord(node, 'Icon'))) return pick(readStringFromRecord(node, 'Icon'))
      const slotData = node['slotData']
      if (isRecord(slotData) && pick(readStringFromRecord(slotData, 'iconPath'))) {
        return pick(readStringFromRecord(slotData, 'iconPath'))
      }
      for (const value of Object.values(node)) {
        const found = search(value)
        if (found) return found
      }
    }
    return ''
  }

  try {
    const parsed = JSON.parse(tooltip)
    const fromParsed = search(parsed)
    if (fromParsed) return fromParsed
  } catch {
    // ignore parse errors
  }

  // fallback: regex search in raw string
  const match =
    String(tooltip).match(/iconPath["']?\s*[:=]\s*["']([^"']+)["']/i) ||
    String(tooltip).match(/"Icon"\s*:\s*"([^"]+)"/i)
  return pick(match?.[1]) || ''
}

/**
 * 툰 색상 추출 (grade 기반)
 */
const runeColorFromGrade = (grade?: string | null) => {
  const g = inlineText(grade).toLowerCase()
  if (!g) return ''
  if (g.includes('고대')) return 'var(--rarity-ancient, #eab308)'
  if (g.includes('유물')) return 'var(--rarity-relic, #f97316)'
  if (g.includes('전설')) return 'var(--rarity-legendary, #fbbf24)'
  if (g.includes('영웅')) return 'var(--rarity-heroic, #a78bfa)'
  if (g.includes('희귀')) return 'var(--rarity-rare, #60a5fa)'
  if (g.includes('고급') || g.includes('언커먼')) return 'var(--rarity-uncommon, #6ee7b7)'
  if (g.includes('일반') || g.includes('노말')) return 'var(--text-secondary, #6b7280)'
  return ''
}

/**
 * 툴팁에서 룬 색상 추출
 */
const extractRuneColor = (tooltip?: string | null, grade?: string | null) => {
  const scanRaw = (value?: string | null) => {
    if (!value) return ''
    const match = String(value).match(/color=['"]?(#?[0-9a-fA-F]{6,8})['"]?/i)
    if (match?.[1]) {
      const raw = match[1].startsWith('#') ? match[1] : `#${match[1]}`
      return raw
    }
    return ''
  }

  const extractTooltipColor = (text?: string | null): string => {
    if (!text) return ''
    const match = String(text).match(/color=['"]?([#\w]+)['"]?/i)
    const raw = match?.[1]
    if (!raw) return ''
    // Normalize hex color if needed
    if (raw.startsWith('#')) return raw
    if (/^[0-9a-fA-F]{6,8}$/.test(raw)) return `#${raw}`
    return ''
  }

  const direct = scanRaw(tooltip) || extractTooltipColor(tooltip)
  if (direct) return direct

  if (tooltip) {
    try {
      const parsed = JSON.parse(tooltip) as unknown
      const candidates = isRecord(parsed) ? Object.values(parsed) : []
      for (const cand of candidates) {
        const candidateValue = isRecord(cand) ? (cand['value'] ?? cand['Value'] ?? cand) : cand
        const candidateText =
          typeof candidateValue === 'string' ? candidateValue : JSON.stringify(candidateValue)
        const found = scanRaw(candidateText) || extractTooltipColor(candidateText)
        if (found) return found
      }
    } catch {
      // ignore parse errors
    }
  }

  return runeColorFromGrade(grade) || ''
}

// ============================================================================
// Composable
// ============================================================================

export const useSkillData = (
  skillResponse: Ref<SkillMenuResponse | null>,
  armoryGemsResponse: Ref<ArmoryGem | null>,
  armoryEffectGemSlots: Ref<SkillGem[]>
) => {
  /**
   * Armory Gems 배열 추출
   */
  const resolveArmoryGems = (): ArmoryGemItem[] => {
    const resp = armoryGemsResponse.value
    if (!resp) return []
    if (Array.isArray(resp.Gems)) return resp.Gems
    const record = resp as unknown as Record<string, unknown>
    const alt = record['gems']
    return Array.isArray(alt) ? (alt as ArmoryGemItem[]) : []
  }

  /**
   * Armory Effect Skills 배열 추출
   */
  const resolveArmoryEffectSkills = (): ArmoryGemEffectSkill[] => {
    const resp = armoryGemsResponse.value
    if (!resp) return []
    const rawEffects: unknown =
      resp.Effects ?? ((resp as unknown as Record<string, unknown>)['effects'] as unknown)
    const effectsArray = Array.isArray(rawEffects) ? rawEffects : rawEffects ? [rawEffects] : []
    const skills: ArmoryGemEffectSkill[] = []
    effectsArray.forEach((effect: unknown) => {
      if (!isRecord(effect)) return
      const rawSkills = effect['Skills'] ?? effect['skills']
      if (Array.isArray(rawSkills)) {
        skills.push(...(rawSkills as ArmoryGemEffectSkill[]))
      }
    })
    return skills
  }

  /**
   * 전투 스킬 카탈로그 (스킬명 정규화)
   */
  const combatSkillCatalog = computed<CombatSkillCatalogEntry[]>(() => {
    const skills = skillResponse.value?.combatSkills ?? []
    return skills
      .map((skill: CombatSkill) => ({
        key: normalizeSkillKey(skill.name),
        name: inlineText(skill.name)
      }))
      .filter((entry: CombatSkillCatalogEntry) => entry.key)
  })

  /**
   * 스킬별 보석 슬롯 매핑
   */
  const skillGemSlotsBySkill = computed(() => {
    const map = new Map<
      string,
      { damage: SkillGemSlot | null; cooldown: SkillGemSlot | null; extras: SkillGemSlot[] }
    >()
    const baseGems = skillResponse.value?.skillGems ?? []
    const gems = [...baseGems, ...armoryEffectGemSlots.value]
    gems.forEach((gem: SkillGem, index: number) => {
      const key = normalizeGemSkillKey(gem, undefined, combatSkillCatalog.value)
      if (!key) return
      const label = extractGemLabel(gem)
      const effectType = classifyGemEffect(label)
      const levelValue = typeof gem.level === 'number' ? gem.level : -1
      const slot: SkillGemSlot = {
        key: `${key}-gem-${index}`,
        name: inlineText(gem.name) || '보석',
        icon: gem.icon || '',
        label,
        grade: inlineText(gem.grade),
        levelLabel: formatLevelLabel(gem.level),
        levelValue,
        effectType
      }
      const entry = map.get(key) ?? { damage: null, cooldown: null, extras: [] }
      if (effectType === 'damage') {
        entry.damage = pickHigherGem(entry.damage, slot)
      } else if (effectType === 'cooldown') {
        entry.cooldown = pickHigherGem(entry.cooldown, slot)
      } else {
        entry.extras.push(slot)
      }
      map.set(key, entry)
    })
    return map
  })

  /**
   * Armory Gem Icon Maps (효과 스킬 및 보석에서 아이콘 추출)
   */
  const armoryGemIconMaps = computed<ArmoryGemIconMaps>(() => {
    const skillByKey = new Map<string, string>()
    const skillByName = new Map<string, string>()
    const gemTooltipIconByName = new Map<string, string>()

    const pickIcon = (icon?: string | null) => (typeof icon === 'string' && icon.trim().length ? icon : '')

    const armoryGems = resolveArmoryGems()
    const effectSkills = resolveArmoryEffectSkills()

    effectSkills.forEach((effectSkill: ArmoryGemEffectSkill) => {
      const record = effectSkill as unknown as Record<string, unknown>
      const skKey = normalizeSkillKey(readStringFromRecord(record, 'Name') || readStringFromRecord(record, 'name'))
      const icon = pickIcon(readStringFromRecord(record, 'Icon')) || parseSkillIconFromTooltip(readStringFromRecord(record, 'Tooltip') || readStringFromRecord(record, 'tooltip'))
      if (skKey && icon) {
        if (!skillByKey.has(skKey)) skillByKey.set(skKey, icon)
        if (!skillByName.has(skKey)) skillByName.set(skKey, icon)
      }
    })

    armoryGems.forEach((gem: ArmoryGemItem) => {
      const gemRecord = gem as unknown as Record<string, unknown>
      const nameKey = normalizeSkillKey(inlineText(gem?.Name || readStringFromRecord(gemRecord, 'name')))
      const tooltipIcon = parseSkillIconFromTooltip(gem?.Tooltip || readStringFromRecord(gemRecord, 'tooltip'))
      if (nameKey && tooltipIcon && !gemTooltipIconByName.has(nameKey)) {
        gemTooltipIconByName.set(nameKey, tooltipIcon)
      }
    })

    return { skillByKey, skillByName, gemTooltipIconByName }
  })

  /**
   * 전투 스킬 키 세트
   */
  const combatSkillKeySet = computed(() => {
    const set = new Set<string>()
    const skills = skillResponse.value?.combatSkills ?? []
    skills.forEach((skill: CombatSkill) => {
      const key = normalizeSkillKey(skill.name)
      if (key) set.add(key)
    })
    return set
  })

  /**
   * 스킬 하이라이트 (메인 스킬 목록)
   */
  const skillHighlights = computed<SkillHighlight[]>(() => {
    const skills = skillResponse.value?.combatSkills ?? []
    const skillKeySet = new Set<string>()
    skills.forEach((skill: CombatSkill) => {
      const key = normalizeSkillKey(skill.name)
      if (key) skillKeySet.add(key)
    })

    const gemOnlySkills = armoryEffectGemSlots.value
      .map((gem: SkillGem, idx: number) => {
        const name = skillNameFromGem(gem, gem.tooltip, combatSkillCatalog.value)
        const key = normalizeSkillKey(name)
        if (!key || skillKeySet.has(key)) return null
        const icon = parseSkillIconFromTooltip(gem.tooltip) || gem.icon || ''
        return {
          name,
          icon,
          level: undefined,
          skillPoints: 0,
          skillType: 0,
          tripods: [],
          rune: null,
          tooltip: gem.tooltip,
          skillPointsScroll: 0,
          order: 9999 + idx,
          isGemOnly: true
        } as CombatSkill
      })
      .filter(Boolean) as CombatSkill[]

    const mergedSkills = [...skills, ...gemOnlySkills]
    if (!mergedSkills.length) return []
    const gemSlots = skillGemSlotsBySkill.value
    const armoryIcons = armoryGemIconMaps.value
    return mergedSkills
      .filter((skill: CombatSkill) => !isAwakeningSkill(skill))
      .sort((a: CombatSkill, b: CombatSkill) => {
        const levelA = typeof a.level === 'number' ? a.level : -1
        const levelB = typeof b.level === 'number' ? b.level : -1
        return levelB - levelA
      })
      .map((skill: CombatSkill, index: number) => {
        const key = normalizeSkillKey(skill.name || `skill-${index}`)
        const gem = key ? gemSlots.get(key) : undefined
        const skillRecord = skill as unknown as Record<string, unknown>
        const runePayload: unknown = skill.rune ?? skillRecord['rune'] ?? skillRecord['Rune']
        const runeRecord = isRecord(runePayload) ? runePayload : null
        const runeTooltipRaw = runeRecord ? (runeRecord['tooltip'] ?? runeRecord['Tooltip']) : undefined
        const runeName = runeRecord ? (runeRecord['name'] ?? runeRecord['Name']) : undefined
        const runeGrade = runeRecord ? (runeRecord['grade'] ?? runeRecord['Grade']) : undefined
        const runeIcon = runeRecord ? (runeRecord['icon'] ?? runeRecord['Icon']) : undefined
        const runeTooltip = isString(runeTooltipRaw) ? runeTooltipRaw : null
        const runeNameText = isString(runeName) ? runeName : null
        const runeGradeText = isString(runeGrade) ? runeGrade : null
        const rune = runeName
          ? {
              name: inlineText(runeNameText),
              icon: isString(runeIcon) ? runeIcon : '',
              grade: inlineText(runeGradeText),
              color: extractRuneColor(runeTooltip, runeGradeText) || undefined
            }
          : null
        const tripods =
          skill.tripods
            ?.filter((tripod: any) => tripod.name && tripod.selected !== false)
            .map((tripod: any, tripodIndex: number) => {
              const slotIndex =
                typeof tripod.slot === 'number' && tripod.slot > 0 ? tripod.slot : tripodIndex + 1
              return {
                key: `${key || 'skill'}-tripod-${tripodIndex}`,
                name: inlineText(tripod.name) || `트포 ${tripodIndex + 1}`,
                icon: tripod.icon || '',
                slotIndex,
                slotLabel: `${slotIndex}`,
                levelLabel: formatLevelLabel(tripod.level)
              }
            }) ?? []
        const isGemOnly = skillRecord['isGemOnly'] === true
        const skillPointValue = Number(skill.skillPoints ?? 0)
        const hasSkillPoints = Number.isFinite(skillPointValue) && skillPointValue > 0
        const hasRune = Boolean(rune)
        const damageGem = gem?.damage ?? gem?.extras?.[0] ?? null
        const cooldownGem = gem?.cooldown ?? gem?.extras?.[1] ?? null
        const hasGem = Boolean(damageGem || cooldownGem)
        const overrideIcon = key ? armoryIcons.skillByKey.get(key) || armoryIcons.skillByName.get(key) : ''
        if (overrideIcon && inlineText(skill.name).includes('종언')) {
          console.debug('[skill icon override] name=%s source=armoryEffects icon=%s', inlineText(skill.name), overrideIcon)
        }
        if (!hasSkillPoints && !hasRune && !hasGem) {
          return null
        }
        return {
          key: key || `skill-${index}`,
          name: inlineText(skill.name) || `스킬 ${index + 1}`,
          icon: overrideIcon || skill.icon || '',
          levelLabel: isGemOnly ? '' : formatLevelLabel(skill.level),
          pointLabel:
            hasSkillPoints && Number.isFinite(skillPointValue)
              ? `${skillPointValue.toLocaleString()}P`
              : '',
          rune,
          tripods,
          gems: {
            damage: damageGem,
            cooldown: cooldownGem
          },
          hasGem
        }
      })
      .filter(Boolean) as SkillHighlight[]
  })

  /**
   * 미장착 보석 (loose gems)
   */
  const skillLooseGems = computed<LooseGem[]>(() => {
    const gems = skillResponse.value?.skillGems ?? []
    if (!gems.length) return []

    const {
      skillByKey: armorySkillIconBySkillKey,
      skillByName: armorySkillIconByName,
      gemTooltipIconByName
    } = armoryGemIconMaps.value

    const armoryGemBySlot = new Map<number, ArmoryGemItem>()
    resolveArmoryGems().forEach((gem: ArmoryGemItem) => {
      const gemRecord = gem as unknown as Record<string, unknown>
      const slot = typeof gem.Slot === 'number' ? gem.Slot : readNumberFromRecord(gemRecord, 'slot')
      if (typeof slot === 'number' && !armoryGemBySlot.has(slot)) {
        armoryGemBySlot.set(slot, gem)
      }
    })

    const skillIconByKey = new Map<string, string>()
    const skills = skillResponse.value?.combatSkills ?? []
    skills.forEach((skill: CombatSkill) => {
      const key = normalizeSkillKey(skill.name)
      if (key && skill.icon && !skillIconByKey.has(key)) {
        skillIconByKey.set(key, skill.icon)
      }
    })

    const skillKeys = combatSkillKeySet.value
    return gems
      .filter((gem: SkillGem) => {
        const gemRecord = gem as unknown as Record<string, unknown>
        const gemSlot = gem.slot ?? readNumberFromRecord(gemRecord, 'Slot') ?? readNumberFromRecord(gemRecord, 'GemSlot')
        const armoryGem = typeof gemSlot === 'number' ? armoryGemBySlot.get(gemSlot) : undefined
        const armoryGemRecord = (armoryGem ?? {}) as unknown as Record<string, unknown>
        const armoryTooltip = armoryGem?.Tooltip || readStringFromRecord(armoryGemRecord, 'tooltip')
        const key = normalizeGemSkillKey(gem, armoryTooltip, combatSkillCatalog.value)
        if (key && skillKeys.has(key)) return false
        const skillName = inlineText(gem.skill?.name || gem.skill?.description)
        if (!skillName && !key) return false
        if (!key) return false
        return true
      })
      .map((gem: SkillGem, index: number) => {
        const effectLabel = extractGemLabel(gem)
        const gemRecord = gem as unknown as Record<string, unknown>
        const gemSlot = gem.slot ?? readNumberFromRecord(gemRecord, 'Slot') ?? readNumberFromRecord(gemRecord, 'GemSlot')
        const armoryGem = typeof gemSlot === 'number' ? armoryGemBySlot.get(gemSlot) : undefined
        const armoryGemRecord = (armoryGem ?? {}) as unknown as Record<string, unknown>
        const armoryTooltip = armoryGem?.Tooltip || readStringFromRecord(armoryGemRecord, 'tooltip')
        const normalizedKey = normalizeGemSkillKey(gem, armoryTooltip, combatSkillCatalog.value)
        const catalogHit = normalizedKey
          ? combatSkillCatalog.value.find((entry: CombatSkillCatalogEntry) => entry.key === normalizedKey)
          : null
        const skillName =
          catalogHit?.name || skillNameFromGem(gem, armoryTooltip, combatSkillCatalog.value)
        const candidateKeys = [
          normalizedKey,
          normalizeSkillKey(extractSkillNameFromGemTooltip(armoryTooltip)),
          normalizeSkillKey(effectLabel),
          normalizeSkillKey(armoryGem?.Name || readStringFromRecord(armoryGemRecord, 'name'))
        ].filter(Boolean) as string[]
        const pickIconFromKeys = (keys: string[], ...maps: Array<Map<string, string>>) => {
          for (const key of keys) {
            for (const map of maps) {
              const hit = map.get(key)
              if (hit) return hit
            }
          }
          return ''
        }
        const skillIcon = pickIconFromKeys(candidateKeys, armorySkillIconBySkillKey, armorySkillIconByName, skillIconByKey)
        const tooltipIcon = parseSkillIconFromTooltip(
          gem.tooltip || readStringFromRecord(gemRecord, 'Tooltip') || armoryTooltip
        )
        const armoryIcon =
          pickIconFromKeys(candidateKeys, armorySkillIconBySkillKey, armorySkillIconByName, gemTooltipIconByName) || ''
        if (inlineText(skillName).includes('종언')) {
          console.debug('[loose gem icon pick]', {
            skillName,
            candidateKeys,
            armoryIcon,
            skillIcon,
            tooltipIcon,
            gemIcon: gem.icon || '',
            armoryGemSlot: gemSlot,
            source: armoryIcon ? 'armory' : skillIcon ? 'skillMatch' : tooltipIcon ? 'tooltip' : 'gemIcon'
          })
        }
        return {
          key: `${gem.name || 'gem'}-loose-${index}`,
          name: skillName || inlineText(gem.name) || '보석',
          skillName,
          icon: armoryIcon || skillIcon || tooltipIcon || gem.icon || '',
          skillIcon: armoryIcon || skillIcon || tooltipIcon,
          levelLabel: formatLevelLabel(gem.level),
          grade: inlineText(gem.grade),
          effectLabel: effectLabel || inlineText(gem.skill?.description),
          typeLabel: skillName || inlineText(gem.skill?.name)
        }
      })
  })

  return {
    combatSkillCatalog,
    skillGemSlotsBySkill,
    armoryGemIconMaps,
    skillHighlights,
    combatSkillKeySet,
    skillLooseGems
  }
}
