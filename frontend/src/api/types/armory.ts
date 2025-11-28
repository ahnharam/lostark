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
  honorPoint?: string | number
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

export interface CardResponse {
  characterName: string
  card?: ArmoryCard
}

/**
 * Raw armory API models (field names match upstream responses)
 */
export interface ArmoryStat {
  Type: string
  Value: string | string[]
  Tooltip?: string | string[]
}

export interface ArmoryTendency {
  Type: string
  Point: number
  MaxPoint: number
}

export interface ArmoryProfile {
  CharacterImage?: string
  ExpeditionLevel?: number
  PvpGradeName?: string
  TownLevel?: number
  TownName?: string
  Title?: string
  GuildMemberGrade?: string
  GuildName?: string
  UsingSkillPoint?: number
  TotalSkillPoint?: number
  Stats?: ArmoryStat[]
  Tendencies?: ArmoryTendency[]
  CombatPower?: string | number
  Decorations?: unknown
  HonorPoint?: string | number
  ServerName?: string
  CharacterName?: string
  CharacterLevel?: number
  CharacterClassName?: string
  ItemAvgLevel?: string
  ItemMaxLevel?: string
}

export interface ArmoryEquipmentItem {
  Type: string
  Name: string
  Icon?: string
  Grade?: string
  Tooltip?: string
}

export interface ArmoryAvatar {
  Type: string
  Name: string
  Icon?: string
  Grade?: string
  IsSet?: boolean
  IsInner?: boolean
  Tooltip?: string
}

export interface ArmoryTripod {
  Tier?: number
  Slot?: number
  Name?: string
  Icon?: string
  IsSelected?: boolean
  Tooltip?: string
}

export interface ArmoryRune {
  Name?: string
  Icon?: string
  Grade?: string
  Tooltip?: string
}

export interface ArmorySkill {
  Name?: string
  Icon?: string
  Level?: number
  Type?: string
  SkillType?: string | number
  Tripods?: ArmoryTripod[]
  Rune?: ArmoryRune | null
  Tooltip?: string
}

export interface ArmoryEngravingDetail {
  Slot?: number
  Name?: string
  Icon?: string
  Tooltip?: string
}

export interface ArmoryEngravingEffect {
  Name?: string
  Description?: string
  Icon?: string
  Tooltip?: string
}

export interface ArmoryEngraving {
  Engravings?: ArmoryEngravingDetail[]
  Effects?: ArmoryEngravingEffect[]
  ArkPassiveEffects?: Array<{
    AbilityStoneLevel?: number | null
    Grade?: string
    Level?: number
    Name?: string
    Description?: string
  }>
}

export interface ArmoryCardItem {
  Slot?: number
  Name?: string
  Icon?: string
  AwakeCount?: number
  AwakeTotal?: number
  Grade?: string
  Tooltip?: string
}

export interface ArmoryCardSetEffectItem {
  Name?: string
  Description?: string
}

export interface ArmoryCardSetEffect {
  Index?: number
  CardSlots?: number[]
  Items?: ArmoryCardSetEffectItem[]
}

export interface ArmoryCard {
  Cards?: ArmoryCardItem[]
  Effects?: ArmoryCardSetEffect[]
}

export interface ArmoryGemItem {
  Slot?: number
  Name?: string
  Icon?: string
  Level?: number
  Grade?: string
  Tooltip?: string
}

export interface ArmoryGemEffectSkill {
  GemSlot?: number
  Name?: string
  Description?: string[]
  Option?: string
  Icon?: string
  Tooltip?: string
}

export interface ArmoryGemEffects {
  Description?: string
  Skills?: ArmoryGemEffectSkill[]
}

export interface ArmoryGem {
  Gems?: ArmoryGemItem[]
  Effects?: ArmoryGemEffects
}

export interface ArmoryColosseumModeStats {
  PlayCount?: number
  VictoryCount?: number
  LoseCount?: number
  TieCount?: number
  KillCount?: number
  DeathCount?: number
  AssistCount?: number
  AceCount?: number
  FirstWinCount?: number
  SecondWinCount?: number
  ThirdWinCount?: number
  FirstPlayCount?: number
  SecondPlayCount?: number
  ThirdPlayCount?: number
  AllKillCount?: number
}

export interface ArmoryColosseumSeason {
  SeasonName?: string
  Competitive?: ArmoryColosseumModeStats | null
  TeamDeathmatch?: ArmoryColosseumModeStats | null
  TeamElimination?: ArmoryColosseumModeStats | null
  CoOpBattle?: ArmoryColosseumModeStats | null
  OneDeathmatch?: ArmoryColosseumModeStats | null
  OneDeathmatchRank?: unknown
}

export interface ArmoryColosseum {
  Rank?: number
  PreRank?: number
  Exp?: number
  Colosseums?: ArmoryColosseumSeason[]
}

export interface ArmoryCollectiblePoint {
  PointName?: string
  Point?: number
  MaxPoint?: number
}

export interface ArmoryCollectible {
  Type?: string
  Icon?: string
  Point?: number
  MaxPoint?: number
  CollectiblePoints?: ArmoryCollectiblePoint[]
}

export interface ArmoryArkPassivePoint {
  Name?: string
  Value?: number
  Tooltip?: string
  Description?: string
}

export interface ArmoryArkPassiveEffect {
  Name?: string
  Description?: string
  Icon?: string
  ToolTip?: string
}

export interface ArmoryArkPassive {
  IsArkPassive?: boolean
  Points?: ArmoryArkPassivePoint[]
  Effects?: ArmoryArkPassiveEffect[]
}

export interface ArmoryArkGridGem {
  Index?: number
  Icon?: string
  IsActive?: boolean
  Grade?: string
  Tooltip?: string
}

export interface ArmoryArkGridSlot {
  Index?: number
  Icon?: string
  Name?: string
  Point?: number
  Grade?: string
  Tooltip?: string
  Gems?: ArmoryArkGridGem[]
}

export interface ArmoryArkGridEffect {
  Name?: string
  Level?: number
  Tooltip?: string
}

export interface ArmoryArkGrid {
  Slots?: ArmoryArkGridSlot[]
  Effects?: ArmoryArkGridEffect[]
}

export interface ArmoriesResponse {
  ArmoryProfile?: ArmoryProfile
  ArmoryEquipment?: ArmoryEquipmentItem[]
  ArmoryAvatars?: ArmoryAvatar[]
  ArmorySkills?: ArmorySkill[]
  ArmoryEngraving?: ArmoryEngraving
  ArmoryCard?: ArmoryCard
  ArmoryGem?: ArmoryGem
  ArkPassive?: ArmoryArkPassive
  ArkGrid?: ArmoryArkGrid
  ColosseumInfo?: ArmoryColosseum
  Collectibles?: ArmoryCollectible[]
}
