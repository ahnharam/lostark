import { ref, watch, onMounted } from 'vue'

export type Theme = 'light' | 'dark' | 'auto'

const THEME_STORAGE_KEY = 'lostark-theme'

// 전역 상태
const currentTheme = ref<Theme>('auto')
const isDark = ref(false)

export function useTheme() {
  // 시스템 다크모드 감지
  const getSystemTheme = (): 'light' | 'dark' => {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }

  // 실제 테마 계산
  const updateActualTheme = () => {
    if (currentTheme.value === 'auto') {
      isDark.value = getSystemTheme() === 'dark'
    } else {
      isDark.value = currentTheme.value === 'dark'
    }

    // HTML 루트에 클래스 적용
    if (isDark.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // 테마 설정
  const setTheme = (theme: Theme) => {
    currentTheme.value = theme
    localStorage.setItem(THEME_STORAGE_KEY, theme)
    updateActualTheme()
  }

  // 테마 토글
  const toggleTheme = () => {
    if (currentTheme.value === 'light') {
      setTheme('dark')
    } else if (currentTheme.value === 'dark') {
      setTheme('auto')
    } else {
      setTheme('light')
    }
  }

  // 초기화
  const initTheme = () => {
    // LocalStorage에서 테마 불러오기
    const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as Theme | null
    if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
      currentTheme.value = savedTheme
    }

    updateActualTheme()

    // 시스템 테마 변경 감지
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    const handleChange = () => {
      if (currentTheme.value === 'auto') {
        updateActualTheme()
      }
    }

    // 최신 브라우저
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange)
    } else {
      // 구형 브라우저 지원
      mediaQuery.addListener(handleChange)
    }
  }

  // 컴포넌트 마운트 시 초기화
  onMounted(() => {
    initTheme()
  })

  // 테마 변경 감지
  watch(currentTheme, updateActualTheme)

  return {
    currentTheme,
    isDark,
    setTheme,
    toggleTheme,
    initTheme
  }
}
