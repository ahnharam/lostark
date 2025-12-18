export interface SkillTripod {
  tier?: number
  slot?: number
  name?: string
  icon?: string
  level?: number
  selected?: boolean
  tooltip?: string
}

export interface SkillRune {
  name?: string
  grade?: string
  icon?: string
  tooltip?: string
}

export interface CombatSkill {
  name?: string
  type?: string
  icon?: string
  level?: number
  skillType?: string | number
  skillPoints?: number
  tooltip?: string
  tripods?: SkillTripod[]
  rune?: SkillRune | null
}

export interface SkillGemSkill {
  name?: string
  icon?: string
  description?: string
}

export interface SkillGem {
  slot?: number
  name?: string
  grade?: string
  level?: number
  icon?: string
  tooltip?: string
  skill?: SkillGemSkill | null
}

export interface SkillMenuResponse {
  characterName: string
  combatSkills?: CombatSkill[]
  skillGems?: SkillGem[]
}

export interface SkillCodeResponse {
  characterName: string
  skillCode: string
}
