<template>
  <div class="autocomplete-container" ref="containerRef">
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
      :class="inputClass"
      type="text"
    />

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
  placeholder?: string
  inputClass?: string
  minChars?: number
  maxSuggestions?: number
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '',
  inputClass: '',
  minChars: 0,
  maxSuggestions: 8
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'select': [suggestion: Suggestion]
}>()

const containerRef = ref<HTMLElement | null>(null)
const inputRef = ref<HTMLInputElement | null>(null)
const localValue = ref(props.modelValue)
const showSuggestions = ref(false)
const selectedIndex = ref(-1)

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

  // 최소 글자 수 체크
  if (query.length < props.minChars) {
    // 최소 글자 미만이면 즐겨찾기만 표시
    return props.suggestions
      .filter(s => s.isFavorite)
      .slice(0, props.maxSuggestions)
  }

  // 검색어가 있으면 필터링
  const filtered = props.suggestions.filter(suggestion => {
    return suggestion.name.toLowerCase().includes(query)
  })

  // 즐겨찾기 우선, 그 다음 최근 검색
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
  if (filteredSuggestions.value.length > 0) {
    showSuggestions.value = true
  }
}

// 블러 이벤트 (약간의 지연으로 클릭 이벤트 처리)
const handleBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
    selectedIndex.value = -1
  }, 200)
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
    selectSuggestion(filteredSuggestions.value[selectedIndex.value])
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
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: var(--card-bg);
  border: 2px solid var(--border-color);
  border-radius: 10px;
  box-shadow: var(--shadow-lg);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
  animation: slideDown 0.2s ease-out;
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
  padding: 10px 15px;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  border-radius: 8px 8px 0 0;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  cursor: pointer;
  transition: all 0.2s;
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
  font-weight: 600;
  color: var(--text-primary);
  font-size: 1rem;
}

.suggestion-name :deep(.highlight) {
  color: var(--primary-color);
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
}

.suggestion-info {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-top: 2px;
}

.suggestion-level {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--primary-color);
  flex-shrink: 0;
}

.no-suggestions {
  padding: 20px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 0.9rem;
}

/* 스크롤바 스타일 */
.suggestions-dropdown::-webkit-scrollbar {
  width: 8px;
}

.suggestions-dropdown::-webkit-scrollbar-track {
  background: var(--bg-secondary);
  border-radius: 0 10px 10px 0;
}

.suggestions-dropdown::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 4px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 모바일 최적화 */
@media (max-width: 640px) {
  .suggestions-dropdown {
    max-height: 300px;
  }

  .suggestion-item {
    padding: 10px 12px;
  }

  .suggestion-name {
    font-size: 0.95rem;
  }

  .suggestion-info {
    font-size: 0.8rem;
  }
}
</style>
