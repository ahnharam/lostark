<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon || '📭' }}</div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description">{{ description }}</p>
    <button v-if="action" @click="$emit('action')" class="action-btn">
      {{ actionText }}
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  title: string
  description: string
  action?: boolean
  actionText?: string
}>()

defineEmits<{
  action: []
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 20px;
  opacity: 0.5;
}

.empty-title {
  margin: 0 0 12px 0;
  color: var(--text-primary, #333333);
  font-size: 1.5rem;
  font-weight: 700;
}

.empty-description {
  margin: 0 0 24px 0;
  color: var(--text-secondary, #666666);
  font-size: 1rem;
  line-height: 1.6;
  max-width: 400px;
  word-break: keep-all;
}

.action-btn {
  padding: 12px 32px;
  background: var(--primary-color, #667eea);
  color: var(--text-inverse, #ffffff);
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:hover {
  background: var(--primary-hover, #5568d3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
