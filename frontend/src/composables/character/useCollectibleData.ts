/**
 * 수집품(Collectibles) 데이터 관리 Composable
 *
 * CharacterSearch.vue에서 추출한 수집품 관련 비즈니스 로직입니다.
 * 수집품 통계 및 포맷팅을 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { Collectible } from '@/api/types/armory'
import { sanitizeInline } from '@/utils/tooltipText'

// ============================================================================
// Types
// ============================================================================

export interface CollectionSummaryItem {
  key: string
  name: string
  levelLabel: string
  pointLabel: string
  percentLabel: string
  percentValue: number
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

/**
 * 숫자를 지역화된 형식으로 포맷 (천 단위 구분자)
 */
const formatNumberLocalized = (value: number): string => {
  return value.toLocaleString('ko-KR')
}

// ============================================================================
// Composable
// ============================================================================

export const useCollectibleData = (collectibles: Ref<Collectible[]>) => {
  /**
   * 수집품 요약 데이터
   * - 각 수집품의 진행도 계산
   * - 퍼센트별로 내림차순 정렬
   */
  const collectionSummary = computed<CollectionSummaryItem[]>(() => {
    if (!collectibles.value.length) return []
    return collectibles.value
      .map((item, index) => {
        const point = typeof item.point === 'number' ? item.point : 0
        const maxPoint = typeof item.maxPoint === 'number' ? item.maxPoint : 0
        const percent = maxPoint > 0 ? Math.min(point / maxPoint, 1) : null
        const percentLabel = percent === null ? '0%' : `${Math.round(percent * 100)}%`
        return {
          key: `${item.type || 'collectible'}-${index}`,
          name: inlineText(item.type) || `수집 ${index + 1}`,
          levelLabel: typeof item.collectibleLevel === 'number' ? `Lv.${item.collectibleLevel}` : '',
          pointLabel: maxPoint
            ? `${formatNumberLocalized(point)} / ${formatNumberLocalized(maxPoint)}`
            : `포인트 ${formatNumberLocalized(point)}`,
          percentLabel: percentLabel,
          percentValue: percent ?? 0
        }
      })
      .sort((a, b) => b.percentValue - a.percentValue)
  })

  return {
    collectionSummary
  }
}
