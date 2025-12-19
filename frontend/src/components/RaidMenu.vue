<template>
  <div class="raid-page">
    <header class="raid-header">
      <div>
        <p class="eyebrow">{{ headerEyebrow }}</p>
        <h2>{{ headerTitle }}</h2>
        <p class="lead">{{ headerLead }}</p>
      </div>
      <div class="raid-header-actions">
        <div id="raid-submenu-actions"></div>
      </div>
    </header>

    <KeepAlive>
      <component :is="activeComponent" />
    </KeepAlive>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import RaidPartyManager from './RaidPartyManager.vue'

const activeComponent = computed(() => {
  return RaidPartyManager
})

const headerEyebrow = computed(() => '레이드 모집')
const headerTitle = computed(() => '멤버 구성 · DM 초대 · 상태 확인')
const headerLead = computed(() => '레이드를 생성하고 멤버를 추가해 DM 초대를 보낼 수 있어요.')

</script>

<style scoped>
.raid-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.raid-top {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.raid-menu-bar {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.raid-menu-bar__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.raid-menu-bar__tabs {
  display: inline-flex;
  gap: 8px;
}

.raid-menu-bar__center{
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.raid-menu-bar__right {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.raid-menu-btn {
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  border-radius: 12px;
  padding: 10px 14px;
  cursor: pointer;
  font-weight: 700;
  color: var(--text-secondary, #374151);
  transition: all 0.2s ease;
}

.raid-menu-btn.active {
  background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16));
  color: var(--primary-color, #6366f1);
  border-color: rgba(99, 102, 241, 0.35);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.08);
}

.raid-menu-btn:hover {
  transform: translateY(-1px);
}

.raid-header {
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

.raid-header-actions {
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
