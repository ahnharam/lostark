import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
})

export interface CharacterProfile {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
  characterImage: string
  expeditionLevel: number
  pvpGradeName: string
  guildName: string
}

export interface Equipment {
  type: string
  name: string
  icon: string
  grade: string
  tooltip: string
}

export interface Engraving {
  name: string
  icon: string
  description: string
}

export interface SiblingCharacter {
  characterName: string
  serverName: string
  characterClassName: string
  itemAvgLevel: string
  itemMaxLevel: string
}

export interface SearchHistory {
  id: number
  userId: string
  characterName: string
  searchedAt: string
}

const USER_ID = 'user123' // 실제로는 로그인 기능에서 받아와야 함

export const lostarkApi = {
  // 기본 검색
  getCharacter: (characterName: string) => {
    return api.get<CharacterProfile>(`/characters/${characterName}`, {
      params: { userId: USER_ID }
    })
  },
  
  // 장비 정보
  getEquipment: (characterName: string) => {
    return api.get<Equipment[]>(`/equipment/${characterName}`)
  },
  
  // 각인 정보
  getEngravings: (characterName: string) => {
    return api.get<Engraving[]>(`/engravings/${characterName}`)
  },
  
  // 보유 캠릭터 목록
  getSiblings: (characterName: string) => {
    return api.get<SiblingCharacter[]>(`/siblings/${characterName}`)
  },
  
  // 즐겨찾기
  addFavorite: (characterName: string) => {
    return api.post('/favorites', { userId: USER_ID, characterName })
  },
  
  removeFavorite: (characterName: string) => {
    return api.delete('/favorites', { params: { userId: USER_ID, characterName } })
  },
  
  getFavorites: () => {
    return api.get<CharacterProfile[]>('/favorites', { params: { userId: USER_ID } })
  },
  
  checkFavorite: (characterName: string) => {
    return api.get<{ isFavorite: boolean }>('/favorites/check', {
      params: { userId: USER_ID, characterName }
    })
  },
  
  // 검색 히스토리
  getHistory: () => {
    return api.get<SearchHistory[]>('/history', { params: { userId: USER_ID } })
  },
  
  clearHistory: () => {
    return api.delete('/history', { params: { userId: USER_ID } })
  },
}
