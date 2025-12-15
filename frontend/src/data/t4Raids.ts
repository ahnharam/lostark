export type RaidCategory = 'epic' | 'kazeros'

export type RaidDifficultyKey = 'normal' | 'hard' | 'the-first'

export interface RaidDifficultyDefinition {
  key: RaidDifficultyKey
  label: string
  minItemLevel: number
}

export interface RaidDefinition {
  id: string
  category: RaidCategory
  name: string
  players: number
  gates: number
  difficulties: RaidDifficultyDefinition[]
}

export const T4_RAIDS: RaidDefinition[] = [
  {
    id: 'epic-behemoth',
    category: 'epic',
    name: '폭풍의 지휘관, 베히모스',
    players: 16,
    gates: 2,
    difficulties: [{ key: 'normal', label: '노말', minItemLevel: 1640 }],
  },
  {
    id: 'kazeros-prologue',
    category: 'kazeros',
    name: '서막: 붉어진 백야의 나선',
    players: 8,
    gates: 2,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1620 },
      { key: 'hard', label: '하드', minItemLevel: 1640 },
    ],
  },
  {
    id: 'kazeros-act-1',
    category: 'kazeros',
    name: '1막: 대지를 부수는 업화의 궤적',
    players: 8,
    gates: 2,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1660 },
      { key: 'hard', label: '하드', minItemLevel: 1680 },
    ],
  },
  {
    id: 'kazeros-act-2',
    category: 'kazeros',
    name: '2막: 부유하는 악몽의 진혼곡',
    players: 8,
    gates: 2,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1670 },
      { key: 'hard', label: '하드', minItemLevel: 1690 },
    ],
  },
  {
    id: 'kazeros-act-3',
    category: 'kazeros',
    name: '3막: 침묵, 폭풍의 밤',
    players: 8,
    gates: 3,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1680 },
      { key: 'hard', label: '하드', minItemLevel: 1700 },
    ],
  },
  {
    id: 'kazeros-act-4',
    category: 'kazeros',
    name: '4막: 파멸의 성채',
    players: 8,
    gates: 2,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1700 },
      { key: 'hard', label: '하드', minItemLevel: 1720 },
    ],
  },
  {
    id: 'kazeros-finale',
    category: 'kazeros',
    name: '종막: 최후의 날',
    players: 8,
    gates: 2,
    difficulties: [
      { key: 'normal', label: '노말', minItemLevel: 1710 },
      { key: 'hard', label: '하드', minItemLevel: 1730 },
      { key: 'the-first', label: 'The FIRST', minItemLevel: 1740 },
    ],
  },
]

export const getRaidById = (raidId: string) => T4_RAIDS.find(raid => raid.id === raidId)
