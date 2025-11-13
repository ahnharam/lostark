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
