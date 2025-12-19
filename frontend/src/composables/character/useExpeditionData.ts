/**
 * 원정대 캐릭터 데이터 Composable
 *
 * 원정대 캐릭터 목록을 서버별로 그룹화하고 정렬하는 로직을 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { CharacterProfile, SiblingCharacter } from '@/api/types/armory'
import type { ExpeditionGroup } from '@/components/character/ExpeditionCharacterList.vue'

export type ExpeditionSortKey = 'itemLevel' | 'characterLevel' | 'name' | 'class'

/**
 * 원정대 캐릭터 데이터 처리
 *
 * @param character - 현재 조회 중인 캐릭터
 * @param siblings - 원정대 내 다른 캐릭터 목록
 * @param sortKey - 정렬 기준
 * @returns 서버별로 그룹화되고 정렬된 원정대 캐릭터 목록
 *
 * @example
 * const { expeditionGroups } = useExpeditionData(character, siblings, expeditionSortKey)
 */
export const useExpeditionData = (
  character: Ref<CharacterProfile | null>,
  siblings: Ref<SiblingCharacter[]>,
  sortKey: Ref<ExpeditionSortKey>
) => {
  /**
   * 아이템 레벨을 숫자로 파싱
   */
  const parseItemLevel = (value?: string): number => {
    if (!value) return -Infinity
    const numeric = Number(String(value).replace(/[^\d.]/g, ''))
    return Number.isFinite(numeric) ? numeric : -Infinity
  }

  /**
   * 서버별로 그룹화되고 정렬된 원정대 캐릭터 목록
   */
  const expeditionGroups = computed<ExpeditionGroup[]>(() => {
    const groups = new Map<string, SiblingCharacter[]>()

    /**
     * 캐릭터를 서버 그룹에 추가
     */
    const addToGroup = (member: SiblingCharacter) => {
      const key = member.serverName || '기타'
      if (!groups.has(key)) groups.set(key, [])
      const normalizedMember: SiblingCharacter = {
        ...member,
        itemMaxLevel: member.itemMaxLevel || member.itemAvgLevel
      }
      groups.get(key)!.push(normalizedMember)
    }

    // 현재 캐릭터 추가
    if (character.value) {
      addToGroup({
        serverName: character.value.serverName,
        characterName: character.value.characterName,
        characterLevel: character.value.characterLevel ?? undefined,
        characterClassName: character.value.characterClassName,
        itemAvgLevel: character.value.itemAvgLevel,
        itemMaxLevel: character.value.itemMaxLevel || character.value.itemAvgLevel,
        characterImage: character.value.characterImage || ''
      })
    }

    // 원정대 캐릭터 추가
    siblings.value.forEach(addToGroup)

    /**
     * 정렬 기준에 따라 캐릭터 비교
     */
    const compareMembers = (a: SiblingCharacter, b: SiblingCharacter): number => {
      if (sortKey.value === 'itemLevel') {
        const levelA = parseItemLevel(a.itemMaxLevel || a.itemAvgLevel)
        const levelB = parseItemLevel(b.itemMaxLevel || b.itemAvgLevel)
        if (levelA !== levelB) return levelB - levelA
      }
      if (sortKey.value === 'characterLevel') {
        const levelA = a.characterLevel ?? 0
        const levelB = b.characterLevel ?? 0
        if (levelA !== levelB) return levelB - levelA
      }
      if (sortKey.value === 'class') {
        const classCompare = (a.characterClassName || '').localeCompare(b.characterClassName || '')
        if (classCompare !== 0) return classCompare
      }
      return (a.characterName || '').localeCompare(b.characterName || '')
    }

    // 서버별로 그룹화하고 정렬
    return Array.from(groups.entries()).map(([server, members]) => ({
      server,
      members: members.slice().sort(compareMembers)
    }))
  })

  return {
    expeditionGroups
  }
}
