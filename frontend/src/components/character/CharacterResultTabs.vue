<template>
  <div v-if="showTabs" class="view-tabs view-tabs--header">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="view-tab-button"
      type="button"
      :class="{ active: modelValue === tab.key }"
      @click="$emit('update:modelValue', tab.key)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>

<script setup lang="ts">
/**
 * 탭 아이템 정의
 */
export interface TabItem {
  key: string
  label: string
}

const props = defineProps<{
  /**
   * 현재 활성화된 탭 키
   */
  modelValue: string

  /**
   * 탭 목록
   */
  tabs: TabItem[]

  /**
   * 탭 표시 여부
   */
  showTabs: boolean
}>()

defineEmits<{
  /**
   * 탭 변경 이벤트
   */
  'update:modelValue': [key: string]
}>()
</script>

<style scoped>
.view-tabs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.view-tabs--header {
  justify-content: flex-end;
}

.view-tab-button {
  padding: 0.5rem 1rem;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.view-tab-button:hover {
  border-color: #4a90e2;
  color: #4a90e2;
  background: #f8fbff;
}

.view-tab-button.active {
  background: #4a90e2;
  border-color: #4a90e2;
  color: #fff;
  font-weight: 600;
}

.view-tab-button.active:hover {
  background: #3a80d2;
  border-color: #3a80d2;
}
</style>
