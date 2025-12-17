<template>
  <div class="theme-toggle">
    <button
      type="button"
      @click="toggleTheme"
      class="top-menu-action-btn"
      :title="getThemeLabel()"
      :aria-label="getThemeLabel()"
    >
      <span class="theme-icon" aria-hidden="true">{{ getThemeIcon() }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'

const { currentTheme, toggleTheme } = useTheme()

const getThemeIcon = () => {
  switch (currentTheme.value) {
    case 'light':
      return '☀️'
    case 'dark':
      return '🌙'
    case 'auto':
      return '🌗'
    default:
      return '🌗'
  }
}

const getThemeLabel = () => {
  switch (currentTheme.value) {
    case 'light':
      return '라이트 모드'
    case 'dark':
      return '다크 모드'
    case 'auto':
      return '자동'
    default:
      return '자동'
  }
}
</script>

<style scoped>
.theme-toggle {
  display: flex;
  align-items: center;
}

.theme-icon {
  font-size: 1.2rem;
  display: flex;
  align-items: center;
}
</style>
