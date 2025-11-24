export interface GameContentsRewardItem {
  Name?: string
  Icon?: string
  Grade?: string
  StartTimes?: string | null
}

export interface GameContentsRewardGroup {
  ItemLevel?: number
  Items?: GameContentsRewardItem[]
}

export interface GameContentCalendarEntry {
  CategoryName?: string
  ContentsName?: string
  ContentsIcon?: string
  MinItemLevel?: number
  StartTimes?: string[]
  Location?: string
  RewardItems?: GameContentsRewardGroup[]
}
