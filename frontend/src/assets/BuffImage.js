export const ENGRAVING_ICON_MAP = {
  // 전투 각인
  '원한': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_71.png',
  '아드레날린': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_235.png',
  '고독한 기사': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_267.png',
  '돌격대장': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_210.png',
  '바리케이드': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_170.png',
  '타격의 대가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_233.png',
  '안정된 상태': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_105.png',
  '급소 타격': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_168.png',
  '결투의 대가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_224.png',
  '시선 집중': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_234.png',
  '굳은 의지': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_44.png',
  '정밀 단도': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_239.png',
  '에테르 포식자': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_74.png',
  '속전속결': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_236.png',
  '선수필승': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/achieve/achieve_08_62.png',
  '마나 효율 증가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_166.png',
  '강화 방패': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_239.png',
  '전문의': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_237.png',
  '여신의 가호': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_229.png',
  '중갑 착용': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_46.png',
  '각성': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_113.png',
  '강령술': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_29.png',
  '폭발물 전문가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_121.png',
  '달인의 저력': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_147.png',
  '정기 흡수': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_65.png',
  '약자 무시': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/achieve/achieve_04_30.png',
  '위기 모면': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_162.png',
  '구슬동자': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_18.png',
  '최대 마나 증가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_122.png',
  '예리한 둔기': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/achieve/achieve_03_40.png',
  '슈퍼 차지': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/achieve/achieve_06_14.png',
  '기습의 대가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_148.png',
  '긴급 구조': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_238.png',
  '마나의 흐름': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_63.png',
  '번개의 분노': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_191.png',
  '불굴': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_66.png',
  '부러진 뼈': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_94.png',
  '분쇄의 주먹': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_83.png',
  '승부사': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_136.png',
  '실드 관통': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_80.png',
  '저주받은 인형': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_237.png',
  '질량 증가': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_231.png',
  '추진력': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/ability/ability_232.png',
  '탈출의 명수': 'https://cdn-lostark.game.onstove.com/efui_iconatlas/buff/buff_10.png'
}

export const getEngravingIcon = (name = '') => {
  const key = name.trim()
  return key && ENGRAVING_ICON_MAP[key] ? ENGRAVING_ICON_MAP[key] : ''
}

export default ENGRAVING_ICON_MAP
