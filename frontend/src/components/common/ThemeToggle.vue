<template>
  <div class="theme-toggle">
    <button
      @click="toggleTheme"
      class="theme-toggle-btn"
      :title="getThemeLabel()"
    >
      <span class="theme-icon">{{ getThemeIcon() }}</span>
      <span class="theme-label">{{ getThemeLabel() }}</span>
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

.theme-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  color: var(--text-primary);
}

.theme-toggle-btn:hover {
  background: var(--bg-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px var(--shadow-color);
}

.theme-icon {
  font-size: 1.2rem;
  display: flex;
  align-items: center;
}

.theme-label {
  font-weight: 600;
}

@media (max-width: 640px) {
  .theme-toggle-btn {
    padding: 6px 12px;
    font-size: 0.85rem;
  }

  .theme-label {
    display: none;
  }

  .theme-icon {
    font-size: 1.5rem;
  }
}
</style>
