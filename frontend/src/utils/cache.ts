/**
 * API 응답 캐싱 유틸리티
 * TTL(Time To Live) 기반 메모리 및 localStorage 캐시
 */

interface CacheEntry<T> {
  data: T
  timestamp: number
  ttl: number
}

interface CacheStats {
  hits: number
  misses: number
  size: number
}

class CacheManager {
  private memoryCache: Map<string, CacheEntry<any>> = new Map()
  private stats: CacheStats = { hits: 0, misses: 0, size: 0 }
  private readonly DEFAULT_TTL = 5 * 60 * 1000 // 5분
  private readonly MAX_MEMORY_SIZE = 100 // 최대 메모리 캐시 항목 수

  /**
   * 캐시 키 생성
   */
  private generateKey(prefix: string, params: Record<string, any>): string {
    const sortedParams = Object.keys(params)
      .sort()
      .map(key => `${key}=${params[key]}`)
      .join('&')
    return `${prefix}:${sortedParams}`
  }

  /**
   * 캐시에서 데이터 조회
   */
  get<T>(key: string): T | null {
    // 메모리 캐시 확인
    const memoryEntry = this.memoryCache.get(key)
    if (memoryEntry) {
      if (this.isValid(memoryEntry)) {
        this.stats.hits++
        return memoryEntry.data as T
      } else {
        this.memoryCache.delete(key)
      }
    }

    // localStorage 캐시 확인
    try {
      const stored = localStorage.getItem(key)
      if (stored) {
        const entry: CacheEntry<T> = JSON.parse(stored)
        if (this.isValid(entry)) {
          // 메모리 캐시에도 저장
          this.memoryCache.set(key, entry)
          this.stats.hits++
          return entry.data
        } else {
          localStorage.removeItem(key)
        }
      }
    } catch (error) {
      console.warn('Cache read error:', error)
    }

    this.stats.misses++
    return null
  }

  /**
   * 캐시에 데이터 저장
   */
  set<T>(key: string, data: T, ttl: number = this.DEFAULT_TTL): void {
    const entry: CacheEntry<T> = {
      data,
      timestamp: Date.now(),
      ttl
    }

    // 메모리 캐시 크기 제한
    if (this.memoryCache.size >= this.MAX_MEMORY_SIZE) {
      // 가장 오래된 항목 제거
      const oldestKey = Array.from(this.memoryCache.keys())[0]
      if (oldestKey !== undefined) {
        this.memoryCache.delete(oldestKey)
      }
    }

    // 메모리 캐시에 저장
    this.memoryCache.set(key, entry)

    // localStorage에도 저장 (용량 제한 주의)
    try {
      localStorage.setItem(key, JSON.stringify(entry))
      this.stats.size = this.memoryCache.size
    } catch (error) {
      console.warn('Cache write error (storage full?):', error)
      // localStorage 실패 시 메모리만 사용
    }
  }

  /**
   * 특정 키 또는 패턴 삭제
   */
  invalidate(keyOrPattern: string): void {
    // 정확한 키 삭제
    this.memoryCache.delete(keyOrPattern)
    localStorage.removeItem(keyOrPattern)

    // 패턴 매칭 삭제 (와일드카드 지원)
    if (keyOrPattern.includes('*')) {
      const pattern = new RegExp(keyOrPattern.replace(/\*/g, '.*'))

      // 메모리 캐시
      for (const key of this.memoryCache.keys()) {
        if (pattern.test(key)) {
          this.memoryCache.delete(key)
        }
      }

      // localStorage
      for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i)
        if (key && pattern.test(key)) {
          localStorage.removeItem(key)
        }
      }
    }

    this.stats.size = this.memoryCache.size
  }

  /**
   * 모든 캐시 삭제
   */
  clear(): void {
    this.memoryCache.clear()

    // lostark 관련 캐시만 삭제
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith('lostark:')) {
        keysToRemove.push(key)
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))

    this.stats = { hits: 0, misses: 0, size: 0 }
  }

  /**
   * 캐시 엔트리가 유효한지 확인
   */
  private isValid<T>(entry: CacheEntry<T>): boolean {
    return Date.now() - entry.timestamp < entry.ttl
  }

  /**
   * 캐시 통계 조회
   */
  getStats(): CacheStats {
    return { ...this.stats }
  }

  /**
   * 캐시 히트율 계산
   */
  getHitRate(): number {
    const total = this.stats.hits + this.stats.misses
    return total === 0 ? 0 : (this.stats.hits / total) * 100
  }

  /**
   * 만료된 캐시 정리
   */
  cleanup(): void {
    // 메모리 캐시 정리
    for (const [key, entry] of this.memoryCache.entries()) {
      if (!this.isValid(entry)) {
        this.memoryCache.delete(key)
      }
    }

    // localStorage 정리
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith('lostark:')) {
        try {
          const stored = localStorage.getItem(key)
          if (stored) {
            const entry = JSON.parse(stored)
            if (!this.isValid(entry)) {
              keysToRemove.push(key)
            }
          }
        } catch (error) {
          keysToRemove.push(key)
        }
      }
    }
    keysToRemove.forEach(key => localStorage.removeItem(key))

    this.stats.size = this.memoryCache.size
  }
}

// 싱글톤 인스턴스
export const cacheManager = new CacheManager()

// 주기적으로 만료된 캐시 정리 (10분마다) - 브라우저 환경에서만 실행
if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
  setInterval(() => {
    cacheManager.cleanup()
  }, 10 * 60 * 1000)
}

/**
 * API 호출을 캐시로 래핑하는 헬퍼 함수
 */
export async function cachedApiCall<T>(
  cacheKey: string,
  apiCall: () => Promise<T>,
  ttl?: number
): Promise<{ data: T; fromCache: boolean }> {
  // 캐시 확인
  const cached = cacheManager.get<T>(cacheKey)
  if (cached !== null) {
    return { data: cached, fromCache: true }
  }

  // API 호출
  const data = await apiCall()

  // 캐시 저장
  cacheManager.set(cacheKey, data, ttl)

  return { data, fromCache: false }
}

/**
 * 캐시 키 생성 헬퍼
 */
export function createCacheKey(resource: string, params?: Record<string, any>): string {
  const prefix = `lostark:${resource}`
  if (!params) return prefix

  const sortedParams = Object.keys(params)
    .sort()
    .map(key => `${key}=${params[key]}`)
    .join('&')

  return `${prefix}:${sortedParams}`
}
