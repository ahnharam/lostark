export interface MarketCategory {
  Code?: number
  CodeName?: string
  Subs?: MarketCategory[]
}

export interface MarketOptionsResponse {
  Categories?: MarketCategory[]
  ItemGrades?: string[]
  ItemTiers?: number[]
  Classes?: string[]
}

export interface MarketItemSummary {
  Id: number
  Name: string
  Grade?: string
  Icon?: string
  BundleCount?: number
  TradeRemainCount?: number | null
  YDayAvgPrice?: number
  RecentPrice?: number
  CurrentMinPrice?: number
}

export interface MarketItemsResponse {
  PageNo?: number
  PageSize?: number
  TotalCount?: number
  Items?: MarketItemSummary[]
}

export interface MarketItemStatEntry {
  AvgPrice?: number
  TradeCount?: number
  Date?: string
}

export interface MarketItemDetail {
  Name: string
  BundleCount?: number
  TradeRemainCount?: number | null
  ToolTip?: string
  Stats?: MarketItemStatEntry[]
}
