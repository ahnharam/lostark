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
