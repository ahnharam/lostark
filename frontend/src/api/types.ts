export interface CharacterStat {
  type: string
  value: string | string[]
  tooltip?: string | string[]
}

export interface CharacterProfile {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
  characterImage: string
  expeditionLevel: number
  pvpGradeName: string
  guildName: string
  characterLevel?: number
  title?: string
  stats?: CharacterStat[]
  combatPower?: string | number
}

export interface Equipment {
  type: string
  name: string
  icon?: string
  grade?: string
  tooltip: string
}

export interface Engraving {
  name: string
  icon?: string
  description: string
  grade?: string
  level?: number | null
  abilityStoneLevel?: number | null
}

export interface CollectiblePoint {
  pointName?: string
  point?: number
  maxPoint?: number
}

export interface Collectible {
  collectibleId?: number
  collectibleLevel?: number
  point?: number
  maxPoint?: number
  type?: string
  icon?: string
  collectiblePoints?: CollectiblePoint[]
}

export interface SiblingCharacter {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
  characterLevel?: number
  characterImage?: string
}

export interface SearchHistory {
  id: number
  userId: string
  characterName: string
  searchedAt: string
}

export interface ApiResult<T> {
  data: T
  fromCache: boolean
}

export interface ArkPassivePoint {
  name?: string
  value?: number
  description?: string
  tooltip?: string
}

export interface ArkPassiveEffect {
  name?: string
  icon?: string
  description?: string
  toolTip?: string
}

export interface ArkPassive {
  title?: string
  isArkPassive?: boolean
  points?: ArkPassivePoint[]
  effects?: ArkPassiveEffect[]
}

export interface ArkGridGem {
  index?: number
  icon?: string
  grade?: string
  isActive?: boolean
  tooltip?: string
}

export interface ArkGridSlot {
  index?: number
  name?: string
  icon?: string
  point?: number
  grade?: string
  tooltip?: string
  gems?: ArkGridGem[]
}

export interface ArkGridEffect {
  name?: string
  level?: number
  tooltip?: string
}

export interface ArkGrid {
  slots?: ArkGridSlot[]
  effects?: ArkGridEffect[]
}

export interface ArkGridResponse {
  characterName: string
  arkPassive?: ArkPassive
  arkGrid?: ArkGrid
}

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

export interface RankingEntry {
  rank?: number
  characterName: string
  serverName?: string
  characterClassName?: string
  itemAvgLevel?: string
  itemMaxLevel?: string
  rating?: number
  score?: number
  tier?: number
  division?: number
  seasonId?: string
  seasonName?: string
  guildName?: string
  updatedDate?: string
}

export interface RankingHighlight {
  characterName: string
  serverName?: string
  characterClassName?: string
  rank?: number
  rating?: number
  itemAvgLevel?: string
  tier?: number
  division?: number
}

export interface RankingSummary {
  leaderboardCode: string
  seasonId?: string
  page: number
  totalFetched: number
  lastUpdated?: string
  highlightedCharacter?: RankingHighlight | null
}

export interface RankingResponse {
  summary: RankingSummary
  entries: RankingEntry[]
}

export interface RankingQueryParams {
  leaderboardCode: string
  seasonId?: string
  page?: number
  characterName?: string
}

export interface RankingMetric {
  rank?: number | null
  total?: number | null
  metricValue?: number | null
  unit?: string
  percentile?: number | null
}

export interface ProfileRankingResponse {
  characterName: string
  serverName?: string
  characterClassName?: string
  globalItemLevel?: RankingMetric | null
  globalClassItemLevel?: RankingMetric | null
  serverItemLevel?: RankingMetric | null
  serverClassItemLevel?: RankingMetric | null
  expeditionLevel?: RankingMetric | null
  collectionScore?: RankingMetric | null
}
