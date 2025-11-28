export type EngravingNameEntry = {
  full: string
  short: string
}

export type EngravingDisplayOptions = {
  /** 기본값: 환경변수(VITE_SHOW_FULL_ENGRAVING_NAME)가 true일 때만 풀네임 */
  useFullName?: boolean
}

export const ENGRAVING_NAME_ENTRIES: EngravingNameEntry[] = [
  // 공용 각인
  { full: '원한', short: '원한' },
  { full: '예리한 둔기', short: '예둔' },
  { full: '저주받은 인형', short: '저받' },
  { full: '아드레날린', short: '아드' },
  { full: '돌격대장', short: '돌대' },
  { full: '결투의 대가', short: '결대' },
  { full: '기습의 대가', short: '기습' },
  { full: '타격의 대가', short: '타대' },
  { full: '슈퍼 차지', short: '슈차' },
  { full: '정기 흡수', short: '정흡' },
  { full: '속전속결', short: '속속' },
  { full: '질량 증가', short: '질증' },
  { full: '바리케이드', short: '바리' },
  { full: '구슬동자', short: '구동' },
  { full: '최대 마나 증가', short: '최마증' },
  { full: '안정된 상태', short: '안정' },
  { full: '각성', short: '각성' },
  { full: '중갑 착용', short: '중갑' },
  { full: '강령술', short: '강령' },
  { full: '회심', short: '회심' },
  { full: '공격의 대가', short: '공대' },
  { full: '저지 불가', short: '저불' },
  { full: '부러진 뼈', short: '부뼈' },
  { full: '위기 모면', short: '위모' },
  { full: '정밀 단도', short: '정단' },
  { full: '실드 관통', short: '실관' },
  // 배틀 아이템/각종 유틸
  { full: '마나 효율 증가', short: '마효증' },
  { full: '구슬수급', short: '구슬' },
  // 클래스 각인 (대표)
  { full: '잔재된 기운', short: '잔재' },
  { full: '멈출 수 없는 충동', short: '충동' },
  { full: '절정', short: '절정' },
  { full: '절제', short: '절제' },
  { full: '강화 무기', short: '강무' },
  { full: '오의 강화', short: '오의' },
  { full: '중력 수련', short: '중수' },
  { full: '고독한 기사', short: '고기' },
  { full: '심판자', short: '심판자' },
  { full: '포격 강화', short: '포강' },
  { full: '화력 강화', short: '화강' },
  { full: '수호자', short: '수호자' },
  { full: '전투 태세', short: '전태' },
  { full: '포식자', short: '포식' },
  { full: '두 번째 동료', short: '두동' },
  { full: '죽음의 습격', short: '죽습' },
  { full: '사냥의 시간', short: '사시' },
  { full: '피스메이커', short: '피메' },
  { full: '저주 사슬', short: '저사' },
  { full: '만개', short: '만개' },
  { full: '갈증', short: '갈증' },
  { full: '타격의 대가(격앙)', short: '타대' },
  { full: '황제의 칙령', short: '황제' },
  { full: '황후의 칙령', short: '황후' },
  { full: '상급 소환사', short: '상소' },
  { full: '넘치는 교감', short: '넘교' },
  { full: '진실된 용맹', short: '진용' },
  { full: '전문의', short: '전문' },
  { full: '긴급구조', short: '긴구' },
  { full: '절실한 구원', short: '절구' }
]

const engravingMap = new Map<string, EngravingNameEntry>()
ENGRAVING_NAME_ENTRIES.forEach(entry => {
  engravingMap.set(entry.full.trim(), entry)
})

const DEFAULT_USE_FULL_NAME =
  typeof import.meta !== 'undefined' &&
  typeof import.meta.env !== 'undefined' &&
  import.meta.env.VITE_SHOW_FULL_ENGRAVING_NAME === 'true'

const cleanName = (name?: string | null) => (name || '').trim()

export const getEngravingAbbreviation = (name?: string | null) => {
  const clean = cleanName(name)
  if (!clean) return ''
  const entry = engravingMap.get(clean)
  return (entry?.short || clean).trim()
}

export const getEngravingDisplayName = (
  name?: string | null,
  options?: EngravingDisplayOptions
) => {
  const clean = cleanName(name)
  if (!clean) return '각인'
  const useFullName = options?.useFullName ?? DEFAULT_USE_FULL_NAME
  if (useFullName) return clean
  return getEngravingAbbreviation(clean) || clean
}
