/**
 * 주간 골드 획득 관련 타입 정의
 */

/**
 * 레이드 난이도
 */
export interface RaidDifficulty {
  /**
   * 레이드 이름 (예: "흑요일", "에키드나", "베히모스" 등)
   */
  name: string

  /**
   * 레이드 약어 (없으면 undefined)
   */
  abbreviation?: string | null

  /**
   * 레이드 식별 키
   */
  raidKey: string

  /**
   * 레이드 아이템 레벨 (예: 1730, 1720, 1710 등)
   */
  itemLevel: number

  /**
   * 난이도별 골드 획득량 (예: 4단계: 52000, 3단계: 42000 등)
   */
  goldReward: number
}

/**
 * 캐릭터별 주간 골드 정보
 */
export interface CharacterWeeklyGold {
  /**
   * 캐릭터명
   */
  characterName: string

  /**
   * 캐릭터 아이템 레벨
   */
  itemLevel: number

  /**
   * 서버명
   */
  serverName: string

  /**
   * 직업
   */
  characterClassName: string

  /**
   * 완료한 레이드 난이도 목록 (raidKey 배열)
   */
  completedRaids: string[]

  /**
   * 해당 캐릭터의 총 골드
   */
  totalGold: number

  /**
   * 선택 여부 (체크박스)
   */
  selected: boolean
}

/**
 * 주간 골드 획득 전체 데이터
 */
export interface WeeklyGoldData {
  /**
   * 레이드 난이도 목록 (헤더에 표시될 컬럼)
   */
  raidDifficulties: RaidDifficulty[]

  /**
   * 캐릭터별 주간 골드 정보
   */
  characters: CharacterWeeklyGold[]

  /**
   * 선택된 캐릭터들의 총 골드 합계
   */
  selectedTotalGold: number
}
