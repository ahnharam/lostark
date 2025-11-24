export interface NewsNotice {
  Title: string
  Date: string
  Link: string
  Type?: string
}

export interface NewsEvent {
  Title: string
  Thumbnail?: string
  Link?: string
  StartDate?: string
  EndDate?: string
  RewardDate?: string | null
}

export interface NewsAlarm {
  // No sample fields available; keep loose typing.
  [key: string]: unknown
}

export interface NewsAlarmsResponse {
  RequirePolling?: boolean
  Alarms?: NewsAlarm[]
}
