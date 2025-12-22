/**
 * 주간 골드 획득 데이터 Composable
 *
 * 원정대 캐릭터별 주간 골드 획득량을 계산하고 관리합니다.
 *
 * 주요 기능:
 * - 캐릭터를 아이템 레벨 기준 내림차순 정렬
 * - 상위 6캐릭터 자동 선택 (주간 골드 획득 가능 캐릭터 수 제한)
 * - 레이드 난이도별 골드 보상 계산
 * - 선택된 캐릭터들의 총 골드 실시간 합산
 */

import { computed, onMounted, ref, watch, type Ref } from 'vue'
import type { SiblingCharacter, CharacterProfile } from '@/api/types/armory'
import type {
  RaidDifficulty,
  CharacterWeeklyGold,
  WeeklyGoldData
} from '@/api/types/weekly-gold'
import { lostarkApi, type RaidCatalogEntry } from '@/api/lostark'
import { getHttpErrorMessage } from '@/utils/httpError'
import { isNumber, isString } from '@/utils/typeGuards'

type RaidCatalogSeed = {
  raidKey: string
  raidName: string
  itemLevel: number
  goldReward: number
}

/**
 * 주간 골드 표기용 기본 레이드 목록 (Admin 레이드 카탈로그 보정용)
 */
const DEFAULT_RAID_CATALOG: RaidCatalogSeed[] = [
]

/**
 * 아이템 레벨을 숫자로 파싱
 */
const parseItemLevel = (value?: string): number => {
  if (!value) return 0
  const numeric = parseFloat(String(value).replace(/[^\d.]/g, ''))
  return Number.isFinite(numeric) ? numeric : 0
}

type DifficultyId = 'single' | 'normal' | 'hard' | 'nightmare'

const difficultyOrder: DifficultyId[] = ['single', 'normal', 'hard', 'nightmare']
const difficultyLabelMap: Record<DifficultyId, string> = {
  single: '싱글',
  normal: '노말',
  hard: '하드',
  nightmare: '나이트메어'
}

const normalizeDifficultyIds = (values?: string[] | null) => {
  if (!values) return []
  const selected = new Set<DifficultyId>()
  values.forEach(value => {
    if (typeof value !== 'string') return
    const normalized = value.trim().toLowerCase()
    if (difficultyOrder.includes(normalized as DifficultyId)) {
      selected.add(normalized as DifficultyId)
    }
  })
  return difficultyOrder.filter(id => selected.has(id))
}

const selectAutoRaids = (itemLevel: number, raids: RaidDifficulty[]) => {
  const eligible = raids.filter(raid => itemLevel >= raid.itemLevel)
  eligible.sort((a, b) => b.itemLevel - a.itemLevel || a.name.localeCompare(b.name))
  const selected: RaidDifficulty[] = []
  const usedGroups = new Set<string>()
  for (const raid of eligible) {
    const abbreviation = isString(raid.abbreviation) ? raid.abbreviation.trim() : ''
    const groupKey = abbreviation ? `abbr:${abbreviation}` : `raid:${raid.raidKey}`
    if (usedGroups.has(groupKey)) continue
    usedGroups.add(groupKey)
    selected.push(raid)
    if (selected.length >= 3) break
  }
  return selected
}

const resolveDifficultyGold = (entry: RaidCatalogEntry, difficulty: DifficultyId) => {
  const goldMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.goldSingle,
    normal: entry.goldNormal,
    hard: entry.goldHard,
    nightmare: entry.goldNightmare
  }
  const difficultyGold = goldMap[difficulty]
  if (isNumber(difficultyGold) && difficultyGold > 0) return difficultyGold
  if (isNumber(entry.goldReward) && entry.goldReward > 0) return entry.goldReward
  return null
}

const resolveDifficultyItemLevel = (entry: RaidCatalogEntry, difficulty: DifficultyId) => {
  const levelMap: Record<DifficultyId, number | null | undefined> = {
    single: entry.itemLevelSingle,
    normal: entry.itemLevelNormal,
    hard: entry.itemLevelHard,
    nightmare: entry.itemLevelNightmare
  }
  const difficultyLevel = levelMap[difficulty]
  if (isNumber(difficultyLevel) && difficultyLevel > 0) return difficultyLevel
  if (isNumber(entry.itemLevel) && entry.itemLevel > 0) return entry.itemLevel
  return null
}

const toRaidDifficulties = (entry: RaidCatalogEntry): RaidDifficulty[] => {
  if (!isString(entry.raidKey) || !isString(entry.raidName)) return []
  const displayName =
    isString(entry.abbreviation) && entry.abbreviation.trim() ? entry.abbreviation.trim() : entry.raidName
  const abbreviation = isString(entry.abbreviation) && entry.abbreviation.trim() ? entry.abbreviation.trim() : null
  const difficulties = normalizeDifficultyIds(entry.difficulties)
  if (difficulties.length === 0) {
    if (!isNumber(entry.itemLevel) || entry.itemLevel <= 0) return []
    if (!isNumber(entry.goldReward) || entry.goldReward <= 0) return []
    return [
      {
        raidKey: entry.raidKey,
        name: displayName,
        abbreviation,
        itemLevel: entry.itemLevel,
        goldReward: entry.goldReward
      }
    ]
  }

  return difficulties
    .map((difficulty) => {
      const gold = resolveDifficultyGold(entry, difficulty)
      const itemLevel = resolveDifficultyItemLevel(entry, difficulty)
      if (!isNumber(gold) || gold <= 0) return null
      if (!isNumber(itemLevel) || itemLevel <= 0) return null
      return {
        raidKey: `${entry.raidKey}:${difficulty}`,
        name: `${displayName} (${difficultyLabelMap[difficulty]})`,
        abbreviation,
        itemLevel,
        goldReward: gold
      }
    })
    .filter((value): value is RaidDifficulty => value !== null)
}

const syncRaidCatalogDefaults = async (catalog: RaidCatalogEntry[]) => {
  const byKey = new Map<string, RaidCatalogEntry>()
  const byName = new Map<string, RaidCatalogEntry>()
  catalog.forEach(entry => {
    if (isString(entry.raidKey)) byKey.set(entry.raidKey, entry)
    if (isString(entry.raidName)) byName.set(entry.raidName, entry)
  })

  const createQueue: RaidCatalogSeed[] = []
  const updateQueue: { raidKey: string; itemLevel?: number; goldReward?: number }[] = []

  DEFAULT_RAID_CATALOG.forEach(seed => {
    const existing = byKey.get(seed.raidKey) ?? byName.get(seed.raidName)
    if (!existing) {
      createQueue.push(seed)
      return
    }
    if (!isString(existing.raidKey)) {
      return
    }
    const hasItemLevel = isNumber(existing.itemLevel) && existing.itemLevel > 0
    const hasGoldReward = isNumber(existing.goldReward) && existing.goldReward > 0
    if (!hasItemLevel || !hasGoldReward) {
      updateQueue.push({
        raidKey: existing.raidKey,
        itemLevel: hasItemLevel ? undefined : seed.itemLevel,
        goldReward: hasGoldReward ? undefined : seed.goldReward
      })
    }
  })

  for (const seed of createQueue) {
    try {
      await lostarkApi.createRaidCatalog({
        raidKey: seed.raidKey,
        raidName: seed.raidName,
        itemLevel: seed.itemLevel,
        goldReward: seed.goldReward,
        active: true
      })
    } catch (err: unknown) {
      console.warn('[weekly-gold] 레이드 기본값 추가 실패:', getHttpErrorMessage(err))
    }
  }

  for (const update of updateQueue) {
    try {
      const body: { itemLevel?: number; goldReward?: number } = {}
      if (update.itemLevel !== undefined) {
        body.itemLevel = update.itemLevel
      }
      if (update.goldReward !== undefined) {
        body.goldReward = update.goldReward
      }
      await lostarkApi.updateRaidCatalog(update.raidKey, body)
    } catch (err: unknown) {
      console.warn('[weekly-gold] 레이드 기본값 보정 실패:', getHttpErrorMessage(err))
    }
  }
}

/**
 * 주간 골드 획득 데이터 처리
 *
 * @param character - 현재 조회 중인 캐릭터
 * @param siblings - 원정대 내 다른 캐릭터 목록
 * @returns 주간 골드 획득 데이터 및 관리 메서드
 *
 * @remarks
 * - 캐릭터는 아이템 레벨 내림차순으로 자동 정렬됩니다
 * - 상위 6캐릭터만 기본 선택 상태로 초기화됩니다 (주간 골드 획득 제한)
 * - 레이드 체크 시 자동으로 골드가 합산됩니다
 *
 * @example
 * const { weeklyGoldData, toggleRaidCompletion, toggleCharacter } = useWeeklyGoldData(character, siblings)
 */
export const useWeeklyGoldData = (
  character: Ref<CharacterProfile | null>,
  siblings: Ref<SiblingCharacter[]>
) => {
  const raidCatalog = ref<RaidCatalogEntry[]>([])

  const raidDifficulties = computed<RaidDifficulty[]>(() =>
    raidCatalog.value
      .filter(entry => entry.active)
      .flatMap(toRaidDifficulties)
      .sort((a, b) => b.itemLevel - a.itemLevel || a.name.localeCompare(b.name))
  )

  /**
   * 캐릭터별 주간 골드 데이터
   */
  const charactersGoldData = ref<CharacterWeeklyGold[]>([])

  /**
   * 전체 캐릭터 목록 (현재 캐릭터 + 원정대 캐릭터)
   */
  const allCharacters = computed<SiblingCharacter[]>(() => {
    const chars: SiblingCharacter[] = []

    // 현재 캐릭터 추가
    if (character.value) {
      chars.push({
        serverName: character.value.serverName,
        characterName: character.value.characterName,
        characterLevel: character.value.characterLevel ?? undefined,
        characterClassName: character.value.characterClassName,
        itemAvgLevel: character.value.itemAvgLevel,
        itemMaxLevel: character.value.itemMaxLevel || character.value.itemAvgLevel,
        characterImage: character.value.characterImage || ''
      })
    }

    // 원정대 캐릭터 추가 (현재 캐릭터는 제외하여 중복 방지)
    const currentName = character.value?.characterName
    siblings.value
      .filter((s) => s.characterName !== currentName)
      .forEach((s) => chars.push(s))

    return chars
  })

  /**
   * 캐릭터별 주간 골드 데이터 초기화 및 계산
   */
  const initializeWeeklyGoldData = () => {
    // 아이템 레벨 기준으로 내림차순 정렬
    const sortedChars = [...allCharacters.value].sort((a, b) => {
      const levelA = parseItemLevel(a.itemMaxLevel || a.itemAvgLevel)
      const levelB = parseItemLevel(b.itemMaxLevel || b.itemAvgLevel)
      return levelB - levelA
    })

    charactersGoldData.value = sortedChars.map((char, index) => {
      const itemLevel = parseItemLevel(char.itemMaxLevel || char.itemAvgLevel)

      const selectedRaids = selectAutoRaids(itemLevel, raidDifficulties.value)
      const completedRaids = selectedRaids.map(raid => raid.raidKey)
      const totalGold = selectedRaids.reduce((sum, raid) => sum + raid.goldReward, 0)

      return {
        characterName: char.characterName,
        itemLevel,
        serverName: char.serverName,
        characterClassName: char.characterClassName,
        completedRaids,
        totalGold,
        selected: index < 6 // 상위 6캐릭터만 선택 (주간 골드 획득 가능 캐릭터 수)
      }
    })
  }

  /**
   * 레이드 완료 토글
   *
   * @param characterName - 캐릭터명
   * @param raidKey - 레이드 키
   */
  const toggleRaidCompletion = (characterName: string, raidKey: string) => {
    const charData = charactersGoldData.value.find((c) => c.characterName === characterName)
    if (!charData) return

    const raidIndex = charData.completedRaids.indexOf(raidKey)
    const raid = raidDifficulties.value.find((r) => r.raidKey === raidKey)

    if (!raid) return

    if (raidIndex > -1) {
      // 이미 완료된 레이드 -> 제거
      charData.completedRaids.splice(raidIndex, 1)
      charData.totalGold -= raid.goldReward
    } else {
      // 완료되지 않은 레이드 -> 추가
      charData.completedRaids.push(raidKey)
      charData.totalGold += raid.goldReward
    }
  }

  /**
   * 캐릭터 선택 토글
   *
   * @param characterName - 캐릭터명
   */
  const toggleCharacter = (characterName: string) => {
    const charData = charactersGoldData.value.find((c) => c.characterName === characterName)
    if (charData) {
      charData.selected = !charData.selected
    }
  }

  /**
   * 전체 캐릭터 선택/해제 토글
   */
  const toggleAllCharacters = () => {
    const allSelected = charactersGoldData.value.every((c) => c.selected)
    charactersGoldData.value.forEach((c) => {
      c.selected = !allSelected
    })
  }

  /**
   * 선택된 캐릭터들의 총 골드 합계
   */
  const selectedTotalGold = computed(() => {
    return charactersGoldData.value
      .filter((c) => c.selected)
      .reduce((sum, c) => sum + c.totalGold, 0)
  })

  /**
   * 주간 골드 획득 전체 데이터
   */
  const weeklyGoldData = computed<WeeklyGoldData>(() => ({
    raidDifficulties: raidDifficulties.value,
    characters: charactersGoldData.value, // 이미 아이템 레벨 내림차순으로 정렬됨
    selectedTotalGold: selectedTotalGold.value
  }))

  watch([allCharacters, raidDifficulties], () => {
    initializeWeeklyGoldData()
  }, { immediate: true })

  const loadRaidCatalog = async () => {
    try {
      const catalog = await lostarkApi.getRaidCatalog()
      raidCatalog.value = catalog
      await syncRaidCatalogDefaults(catalog)
      raidCatalog.value = await lostarkApi.getRaidCatalog()
    } catch (err: unknown) {
      console.warn('[weekly-gold] 레이드 카탈로그 불러오기 실패:', getHttpErrorMessage(err))
    }
  }

  onMounted(() => {
    loadRaidCatalog()
  })

  return {
    weeklyGoldData,
    toggleRaidCompletion,
    toggleCharacter,
    toggleAllCharacters,
    initializeWeeklyGoldData
  }
}
