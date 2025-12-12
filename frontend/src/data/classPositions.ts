export type CombatPositionKey = 'head' | 'back' | 'side'

const normalize = (value?: string | null) => (value || '').replace(/\s+/g, '').toLowerCase()

// 기본 분류: 필요에 따라 클래스명이나 직업 각인을 추가하세요.
const HEAD_CLASSES = [
  '워로드',
  '디스트로이어'
]

const BACK_CLASSES = [
  '버서커',
  '슬레이어',
  '창술사',
  '스트라이커',
  '블레이드',
  '리퍼',
]

// 클래스 각인 기반 분류
const HEAD_CLASS_ENGRAVINGS = [
  '고독한기사',
  '전투태세',
  '중력수련',
  '분노의망치',
  '드래곤로어',
  '수라의길',
].map(normalize)

const BACK_CLASS_ENGRAVINGS = [
  '잔재된기운',
  '멈출수없는충동',
  '갈증',
  '만개',
  '충격단련',
  '일격필살',
  '강화무기',
].map(normalize)

const HEAD_SET = new Set([...HEAD_CLASSES.map(normalize), ...HEAD_CLASS_ENGRAVINGS])
const BACK_SET = new Set([...BACK_CLASSES.map(normalize), ...BACK_CLASS_ENGRAVINGS])

export const resolveCombatPosition = (
  className?: string | null,
  classEngravings: string[] = []
): CombatPositionKey => {
  const normalizedClass = normalize(className)
  if (normalizedClass && HEAD_SET.has(normalizedClass)) return 'head'
  if (normalizedClass && BACK_SET.has(normalizedClass)) return 'back'

  for (const engraving of classEngravings) {
    const normalizedEngraving = normalize(engraving)
    if (HEAD_SET.has(normalizedEngraving)) return 'head'
    if (BACK_SET.has(normalizedEngraving)) return 'back'
  }

  return 'side'
}
