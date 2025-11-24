export interface AuctionSkillTripodOption {
  Value?: number
  Text?: string
  IsGem?: boolean
  Tiers?: number[]
}

export interface AuctionSkillOption {
  Value?: number
  Class?: string
  Text?: string
  IsSkillGroup?: boolean
  Tripods?: AuctionSkillTripodOption[]
}

export interface AuctionEtcSubOption {
  Value?: number
  Text?: string
  Class?: string
  Categorys?: number[] | null
  Tiers?: number[] | null
  EtcValues?: unknown
}

export interface AuctionEtcOption {
  Value?: number
  Text?: string
  Tiers?: number[]
  EtcSubs?: AuctionEtcSubOption[]
}

export interface AuctionCategory {
  Code?: number
  CodeName?: string
  Subs?: AuctionCategory[]
}

export interface AuctionOptionsResponse {
  MaxItemLevel?: number
  ItemGradeQualities?: number[]
  SkillOptions?: AuctionSkillOption[]
  EtcOptions?: AuctionEtcOption[]
  Categories?: AuctionCategory[]
  ItemGrades?: string[]
  ItemTiers?: number[]
  Classes?: string[]
}

export interface AuctionItemOption {
  Type?: string
  OptionName?: string
  OptionNameTripod?: string
  Value?: number
  IsPenalty?: boolean
  ClassName?: string | null
  IsValuePercentage?: boolean
}

export interface AuctionInfo {
  StartPrice?: number
  BuyPrice?: number
  BidPrice?: number
  EndDate?: string
  BidCount?: number
  BidStartPrice?: number
  IsCompetitive?: boolean
  TradeAllowCount?: number
  UpgradeLevel?: number | null
}

export interface AuctionItem {
  Name: string
  Grade?: string
  Tier?: number
  Level?: number
  Icon?: string
  GradeQuality?: number | null
  AuctionInfo?: AuctionInfo
  Options?: AuctionItemOption[]
}

export interface AuctionItemsResponse {
  PageNo?: number
  PageSize?: number
  TotalCount?: number
  Items?: AuctionItem[]
}
