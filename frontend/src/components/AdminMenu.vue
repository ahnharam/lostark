<template>
  <div class="admin-page">
    <header class="admin-header">
      <div>
        <p class="eyebrow">{{ headerEyebrow }}</p>
        <h2>{{ headerTitle }}</h2>
        <p class="lead">{{ headerLead }}</p>
      </div>
      <div class="admin-header-actions">
        <div id="admin-submenu-actions"></div>
      </div>
    </header>

    <KeepAlive>
      <component :is="activeComponent" />
    </KeepAlive>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import AdminStats from './AdminStats.vue'
import AdminRaidCatalog from './AdminRaidCatalog.vue'

type AdminSubTab = 'market-records' | 'raid-catalog'

const props = defineProps<{
  activeTab?: AdminSubTab
}>()

const activeTab = ref<AdminSubTab>(props.activeTab ?? 'market-records')
watch(
  () => props.activeTab,
  value => {
    if (!value) return
    activeTab.value = value
  }
)

const activeComponent = computed(() => {
  if (activeTab.value === 'raid-catalog') return AdminRaidCatalog
  return AdminStats
})

const headerEyebrow = computed(() => {
  if (activeTab.value === 'raid-catalog') return '레이드 추가'
  return '거래소 기록'
})

const headerTitle = computed(() => {
  if (activeTab.value === 'raid-catalog') return 'raid_catalog 관리'
  return '거래소 일별 기록 모니터'
})

const headerLead = computed(() => {
  if (activeTab.value === 'raid-catalog') return 'DB에 레이드(raidKey/raidName)를 추가해 신규 레이드를 확장할 수 있어요.'
  return '거래소 기록을 확인하거나, 필요한 경우 수동 캡처를 실행하세요.'
})
</script>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.admin-menu-bar {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.admin-menu-bar__tabs {
  display: inline-flex;
  gap: 8px;
}

.admin-menu-bar__center {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-menu-bar__right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-menu-btn {
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  border-radius: 12px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 700;
  color: var(--text-secondary, #374151);
  transition: all 0.2s ease;
}

.admin-menu-btn.active {
  background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16));
  color: var(--primary-color, #6366f1);
  border-color: rgba(99, 102, 241, 0.35);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.08);
}

.admin-menu-btn:hover {
  transform: translateY(-1px);
}

.admin-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  box-shadow: var(--shadow-sm);
}

.admin-header-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

h2 {
  margin: 0;
  color: var(--text-primary, #111827);
}

.lead {
  margin: 8px 0 0;
  color: var(--text-muted, #6b7280);
  font-size: 0.95rem;
}
</style>
