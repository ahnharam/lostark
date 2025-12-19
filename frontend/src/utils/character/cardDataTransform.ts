/**
 * 카드 데이터 변환 유틸리티
 *
 * CharacterSearch.vue에서 추출한 순수 함수들입니다.
 * 카드, 카드 세트, 각성 관련 데이터 변환 및 파싱을 담당합니다.
 */

import { sanitizeInline } from '@/utils/tooltipText'

// ============================================================================
// Types
// ============================================================================

export interface CardAwakeningRequirement {
  requirement: number
}

// ============================================================================
// Helper Functions (Internal)
// ============================================================================

/**
 * Inline 텍스트로 변환
 */
const inlineText = (value: unknown): string => {
  if (value === undefined || value === null) return ''
  if (typeof value !== 'string' && typeof value !== 'number') return ''
  return sanitizeInline(value)
}

// ============================================================================
// Core Functions
// ============================================================================

/**
 * 각성 요구사항 파싱 (카드 이름에서 "N각성" 추출)
 * @param rawName - 카드 효과 이름
 * @returns 각성 요구 수치 또는 null
 *
 * @example
 * parseAwakeningRequirement('18각성 효과') // 18
 * parseAwakeningRequirement('24각 세트 효과') // 24
 * parseAwakeningRequirement('기본 효과') // null
 */
export const parseAwakeningRequirement = (rawName?: string | null): number | null => {
  const name = inlineText(rawName)
  if (!name) return null
  const match = name.match(/(\d+)\s*(?:각성|각)/)
  if (!match?.[1]) return null
  const value = Number(match[1])
  return Number.isFinite(value) ? value : null
}

/**
 * 카드 등급 정규화
 * @param grade - 원본 등급 문자열
 * @returns 정규화된 등급
 *
 * @example
 * normalizeCardGrade('전설') // '전설'
 * normalizeCardGrade('Legend') // '전설'
 */
export const normalizeCardGrade = (grade?: string | null): string => {
  const normalized = inlineText(grade).toLowerCase()
  if (normalized.includes('ancient') || normalized.includes('고대')) return '고대'
  if (normalized.includes('legend') || normalized.includes('전설')) return '전설'
  if (normalized.includes('hero') || normalized.includes('영웅')) return '영웅'
  if (normalized.includes('rare') || normalized.includes('희귀')) return '희귀'
  if (normalized.includes('uncommon') || normalized.includes('고급')) return '고급'
  if (normalized.includes('common') || normalized.includes('일반')) return '일반'
  return inlineText(grade)
}

/**
 * 카드 각성 라벨 생성
 * @param awakeCount - 현재 각성 수
 * @param awakeTotal - 최대 각성 수
 * @returns 각성 라벨 (예: '3/5') 또는 null
 *
 * @example
 * formatCardAwakeningLabel(3, 5) // '3/5'
 * formatCardAwakeningLabel(null, 5) // null
 */
export const formatCardAwakeningLabel = (
  awakeCount: number | null,
  awakeTotal: number | null
): string | null => {
  if (awakeCount !== null && awakeTotal !== null) {
    return `${awakeCount}/${awakeTotal}`
  }
  return null
}

/**
 * 활성화된 카드 세트 효과 인덱스 계산
 * @param baseIndex - 기본 인덱스 (슬롯 수 기반)
 * @param items - 효과 아이템 목록
 * @param equippedCount - 장착된 카드 수
 * @param totalAwakeCount - 총 각성 수
 * @param maxSlot - 최대 슬롯
 * @param activeSlot - 활성 슬롯
 * @returns 활성화된 효과 인덱스
 *
 * @example
 * calculateActiveEffectIndex(0, items, 6, 24, 6, 6) // 2 (24각성 효과)
 */
export const calculateActiveEffectIndex = (
  baseIndex: number,
  items: Array<{ name?: string | null }>,
  equippedCount: number,
  totalAwakeCount: number,
  maxSlot: number,
  activeSlot: number
): number => {
  if (!items.length) return 0
  const clampedBaseIndex = Math.min(Math.max(0, baseIndex), items.length - 1)

  // 최대 슬롯 미달성 시 기본 인덱스 반환
  if (!maxSlot || equippedCount < maxSlot || activeSlot < maxSlot) return clampedBaseIndex
  if (!totalAwakeCount) return clampedBaseIndex

  // 각성 요구사항 확인
  const awakeningCandidates = items
    .map((item, idx) => ({
      idx,
      requirement: parseAwakeningRequirement(item?.name)
    }))
    .filter((entry): entry is { idx: number; requirement: number } => entry.requirement !== null)
    .filter(entry => entry.idx >= clampedBaseIndex)

  const matchedCandidate = awakeningCandidates
    .filter(entry => totalAwakeCount >= entry.requirement)
    .reduce<{ idx: number; requirement: number } | null>((best, entry) => {
      if (!best) return entry
      if (entry.requirement > best.requirement) return entry
      return best
    }, null)

  return matchedCandidate ? matchedCandidate.idx : clampedBaseIndex
}
