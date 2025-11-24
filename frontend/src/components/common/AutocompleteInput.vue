<template>
  <div class="autocomplete-container" ref="containerRef">
    <!-- 입력 프레임: Horizontal / H=52 / Radius=999 / Padding 16~20 / Gap 12 -->
    <div class="input-frame">
      <!-- 좌: 검색 아이콘 -->
      <div class="icon-left">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
        </svg>
      </div>

      <!-- 중앙: 입력 필드 (W=Fill) -->
      <input
        ref="inputRef"
        v-model="localValue"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown.down.prevent="navigateDown"
        @keydown.up.prevent="navigateUp"
        @keydown.enter.prevent="selectCurrent"
        @keydown.esc="closeSuggestions"
        :placeholder="placeholder"
        class="search-input"
        type="text"
      />

      <!-- 우: Clear 버튼 (W=Hug) -->
      <button
        v-if="localValue"
        @click="clearInput"
        class="icon-right clear-button"
        type="button"
        aria-label="Clear"
      >
        <svg width="20" height="20" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
        </svg>
      </button>
    </div>

    <!-- 하단: 추천어 1줄 예시 -->
    <div v-if="showRecommendations && !showSuggestions" class="recommendations-hint">
      <span class="hint-label">추천:</span>
      <button
        v-for="(rec, idx) in recommendations.slice(0, 3)"
        :key="`rec-${idx}`"
        @click="selectRecommendation(rec)"
        class="recommendation-chip"
      >
        {{ rec }}
      </button>
    </div>

    <!-- 자동완성 드롭다운 -->
    <div v-if="showSuggestions && filteredSuggestions.length > 0" class="suggestions-dropdown">
      <div class="suggestions-header">
        <span v-if="hasFavorites">⭐ 즐겨찾기</span>
        <span v-else-if="hasHistory">🕒 최근 검색</span>
        <span v-else>💡 추천</span>
      </div>

      <div
        v-for="(suggestion, index) in filteredSuggestions"
        :key="suggestion.id"
        :class="['suggestion-item', { active: index === selectedIndex }]"
        @mousedown.prevent="selectSuggestion(suggestion)"
        @mouseenter="selectedIndex = index"
      >
        <div class="suggestion-icon">
          {{ suggestion.isFavorite ? '⭐' : '🕒' }}
        </div>
        <div class="suggestion-content">
          <div class="suggestion-name">
            <span v-html="highlightMatch(suggestion.name, localValue)"></span>
          </div>
          <div v-if="suggestion.info" class="suggestion-info">
            {{ suggestion.info }}
          </div>
        </div>
        <div v-if="suggestion.level" class="suggestion-level">
          {{ suggestion.level }}
        </div>
      </div>

      <div v-if="filteredSuggestions.length === 0 && localValue" class="no-suggestions">
        검색 결과가 없습니다
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

export interface Suggestion {
  id: string | number
  name: string
  info?: string
  level?: string
  isFavorite?: boolean
}

interface Props {
  modelValue: string
  suggestions: Suggestion[]
  recommendations?: string[]
  placeholder?: string
  minChars?: number
  maxSuggestions?: number
}

const props = withDefaults(defineProps<Props>(), {
  recommendations: () => [],
  placeholder: '캐릭터명을 입력하세요',
  minChars: 0,
  maxSuggestions: 8
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'select': [suggestion: Suggestion | { id: string; name: string }]
  'clear': []
  'focus': []
  'blur': []
}>()

const containerRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)
const localValue = ref(props.modelValue)
const showSuggestions = ref(false)
const selectedIndex = ref(-1)

const showRecommendations = computed(() => {
  return !localValue.value && props.recommendations.length > 0
})

// 로컬 값 변경 시 부모에게 전달
watch(localValue, (newValue) => {
  emit('update:modelValue', newValue)
})

// 부모로부터 값 변경 시 로컬 값 업데이트
watch(() => props.modelValue, (newValue) => {
  localValue.value = newValue
})

// 필터링된 제안 목록
const filteredSuggestions = computed(() => {
  const query = localValue.value.trim().toLowerCase()

  if (query.length < props.minChars) {
    return []
  }

  const filtered = props.suggestions.filter(suggestion => {
    return suggestion.name.toLowerCase().includes(query)
  })

  const sorted = filtered.sort((a, b) => {
    if (a.isFavorite && !b.isFavorite) return -1
    if (!a.isFavorite && b.isFavorite) return 1
    return 0
  })

  return sorted.slice(0, props.maxSuggestions)
})

const hasFavorites = computed(() => {
  return filteredSuggestions.value.some(s => s.isFavorite)
})

const hasHistory = computed(() => {
  return filteredSuggestions.value.some(s => !s.isFavorite)
})

// 입력 이벤트
const handleInput = () => {
  showSuggestions.value = true
  selectedIndex.value = -1
}

// 포커스 이벤트
const handleFocus = () => {
  emit('focus')
  if (filteredSuggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

// 블러 이벤트 (약간의 지연으로 클릭 이벤트 처리)
const handleBlur = () => {
  emit('blur')
  setTimeout(() => {
    showSuggestions.value = false
    selectedIndex.value = -1
  }, 200)
}

// 입력 지우기
const clearInput = () => {
  localValue.value = ''
  showSuggestions.value = false
  emit('clear')
  inputRef.value?.focus()
}

// 추천어 선택
const selectRecommendation = (recommendation: string) => {
  localValue.value = recommendation
  emit('select', { id: 'recommendation', name: recommendation })
}

// 키보드 네비게이션 - 아래
const navigateDown = () => {
  if (selectedIndex.value < filteredSuggestions.value.length - 1) {
    selectedIndex.value++
  }
}

// 키보드 네비게이션 - 위
const navigateUp = () => {
  if (selectedIndex.value > 0) {
    selectedIndex.value--
  }
}

// 현재 선택된 항목 선택
const selectCurrent = () => {
  if (selectedIndex.value >= 0 && selectedIndex.value < filteredSuggestions.value.length) {
    const suggestion = filteredSuggestions.value[selectedIndex.value]
    if (suggestion) {
      selectSuggestion(suggestion)
    }
  } else if (localValue.value.trim()) {
    // 선택된 항목이 없으면 입력값으로 검색
    closeSuggestions()
    emit('select', { id: 'manual', name: localValue.value.trim() })
  }
}

// 제안 선택
const selectSuggestion = (suggestion: Suggestion) => {
  localValue.value = suggestion.name
  showSuggestions.value = false
  selectedIndex.value = -1
  emit('select', suggestion)
}

// 제안 닫기
const closeSuggestions = () => {
  showSuggestions.value = false
  selectedIndex.value = -1
}

// 검색어 하이라이트
const highlightMatch = (text: string, query: string): string => {
  if (!query.trim()) return text

  const regex = new RegExp(`(${query.trim()})`, 'gi')
  return text.replace(regex, '<strong class="highlight">$1</strong>')
}

// 외부 클릭 감지
const handleClickOutside = (event: MouseEvent) => {
  if (containerRef.value && !containerRef.value.contains(event.target as Node)) {
    closeSuggestions()
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

// 포커스 메서드 노출
defineExpose({
  focus: () => inputRef.value?.focus(),
  blur: () => inputRef.value?.blur()
})
</script>

<style scoped>
.autocomplete-container {
  position: relative;
  width: 100%;
  margin: 0 auto;
}

/* 입력 프레임: Horizontal / H=52 / Radius=999 / Padding 16~20 / Gap 12 */
.input-frame {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  height: 52px;
  width: 350px;
  padding: 0 var(--space-xl);
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-full);
  transition: all var(--transition-slow);
  box-shadow: var(--shadow-sm);
  justify-content: space-between;
}

.input-frame:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 좌 아이콘 (W=Hug) */
.icon-left {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  flex-shrink: 0;
}

/* 중앙 입력 (W=Fill) */
.search-input {
  border: none;
  background: none;
  outline: none;
  font-size: var(--font-base);
  color: var(--text-primary);
  font-weight: var(--font-medium);
  width: 100%;
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

/* 우 Clear 버튼 (W=Hug) */
.icon-right {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.clear-button {
  padding: var(--space-sm);
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  transition: all var(--transition-base);
  border-radius: 50%;
}

.clear-button:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

/* 추천어 힌트 */
.recommendations-hint {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  margin-top: var(--space-md);
  padding: 0 var(--space-xl);
  flex-wrap: wrap;
}

.hint-label {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
}

.recommendation-chip {
  padding: var(--space-sm) var(--space-md);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: var(--font-sm);
  color: var(--text-primary);
  cursor: pointer;
  transition: all var(--transition-base);
  font-weight: var(--font-medium);
}

.recommendation-chip:hover {
  background: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

/* 자동완성 드롭다운 */
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + var(--space-sm));
  left: 0;
  right: 0;
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  max-height: 400px;
  max-width: 350px;
  overflow-y: auto;
  z-index: var(--z-dropdown);
  animation: slideDown var(--transition-base) ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.suggestions-header {
  padding: var(--space-md) var(--space-lg);
  font-size: var(--font-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  border-radius: var(--radius-sm) var(--radius-sm) 0 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  border-bottom: 1px solid var(--border-color-light);
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover,
.suggestion-item.active {
  background: var(--bg-hover);
}

.suggestion-item.active {
  border-left: 3px solid var(--primary-color);
}

.suggestion-icon {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-name {
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  font-size: var(--font-base);
}

.suggestion-name :deep(.highlight) {
  color: var(--primary-color);
  background: rgba(102, 126, 234, 0.1);
  padding: 2px var(--space-xs);
  border-radius: 3px;
}

.suggestion-info {
  font-size: var(--font-sm);
  color: var(--text-secondary);
  margin-top: 2px;
}

.suggestion-level {
  font-size: var(--font-sm);
  font-weight: var(--font-bold);
  color: var(--primary-color);
  flex-shrink: 0;
}

.no-suggestions {
  padding: var(--space-xl);
  text-align: center;
  color: var(--text-tertiary);
  font-size: var(--font-sm);
}

/* 스크롤바 스타일 */
.suggestions-dropdown::-webkit-scrollbar {
  width: 8px;
}

.suggestions-dropdown::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 0 12px 12px 0;
}

.suggestions-dropdown::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 모바일 최적화 */
@media (max-width: 768px) {
  .autocomplete-container {
    max-width: 100%;
  }

  .input-frame {
    height: 48px;
    padding: 0 16px;
  }

  .search-input {
    font-size: 0.95rem;
  }

  .recommendations-hint {
    padding: 0 16px;
  }

  .suggestions-dropdown {
    max-height: 300px;
  }

  .suggestion-item {
    padding: 10px 16px;
  }
}
</style>
