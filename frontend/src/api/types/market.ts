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
  id: number
  name: string
  grade?: string
  icon?: string
  bundleCount?: number
  tradeRemainCount?: number | null
  yDayAvgPrice?: number
  recentPrice?: number
  currentMinPrice?: number
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
  name: string
  bundleCount?: number
  tradeRemainCount?: number | null
  toolTip?: string
  stats?: MarketItemStatEntry[]
}

export interface MarketSearchResponse {
  categoryCode?: number
  characterClass?: string
  itemTier?: number
  itemGrade?: string
  sort?: string
  sortCondition?: string
  page?: number
  pageSize?: number
  totalCount?: number
  totalPages?: number
  pages?: Record<number, MarketItemSummary[]>
  fetchedAt?: number
}

export interface StoredMarketCategory {
  id: number
  code: number
  codeName: string
  parentCode?: number | null
  hasSubs: boolean
  depth?: number
  createdAt?: string
  updatedAt?: string
}

export interface StoredMarketItem {
  id: number
  apiItemId: number
  categoryCode?: number | null
  pageNo?: number | null
  name: string
  grade?: string
  icon?: string
  bundleCount?: number
  tradeRemainCount?: number | null
  yDayAvgPrice?: number | null
  recentPrice?: number | null
  currentMinPrice?: number | null
  raw?: string | null
  fetchedAt?: string
  createdAt?: string
  updatedAt?: string
}
