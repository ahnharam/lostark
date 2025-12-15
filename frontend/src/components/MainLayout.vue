<template>
  <div class="main-layout">
    <button
      type="button"
      class="menu-trigger"
      :aria-expanded="menuOpen"
      aria-label="메인 메뉴 열기"
      @click="toggleMenu"
    >
      <span class="menu-trigger__icon">☰</span>
    </button>

    <transition name="menu-slide">
      <nav
        v-if="menuOpen"
        class="menu-drawer"
        aria-label="주 메뉴"
      >
        <div class="menu-drawer__inner">
          <div class="menu-inline">
            <span class="menu-logo">Loap</span>
            <div class="menu-items-row">
              <button
                v-for="item in menuItems"
                :key="item.key"
                type="button"
                class="menu-item"
                :class="{ active: activeMenu === item.key, disabled: !item.available }"
                :disabled="!item.available"
                @click="handleMenuSelect(item.key)"
              >
                <span class="menu-item__icon" aria-hidden="true">{{ item.icon }}</span>
                <span class="menu-item__label">{{ item.label }}</span>
                <span v-if="item.badge" class="menu-item__badge">{{ item.badge }}</span>
              </button>
            </div>
            <div class="menu-actions">
              <div class="server-status" :class="statusClass">
                <span class="status-dot" aria-hidden="true"></span>
                <span class="status-label">{{ statusLabel }}</span>
              </div>
              <ThemeToggle class="menu-theme-toggle" />
            </div>
          </div>
          <button class="menu-close" type="button" @click="closeMenu" aria-label="메뉴 닫기"></button>
        </div>
      </nav>
    </transition>

    <div class="layout-body">
      <aside class="ad-slot ad-slot--left" aria-label="왼쪽 광고 영역">
        <span>광고 영역</span>
      </aside>
      <main class="layout-content">
        <component :is="activeComponent" />
      </main>
      <aside class="ad-slot ad-slot--right" aria-label="오른쪽 광고 영역">
        <span>광고 영역</span>
      </aside>
    </div>

    <footer class="page-footer">
      <div class="footer-inner">
        <span class="footer-badge">Copyright</span>
        <span class="footer-copy">© Ferny</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CharacterSearch from './CharacterSearch.vue'
import ReforgeMenu from './ReforgeMenu.vue'
import AuctionMenu from './AuctionMenu.vue'
import LifeMenu from './LifeMenu.vue'
import AdminStats from './AdminStats.vue'
import RaidSchedule from './RaidSchedule.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import { useTheme } from '@/composables/useTheme'
import { lostarkApi } from '@/api/lostark'

type MainMenuKey = 'character-search' | 'reforge' | 'auction' | 'raid-schedule' | 'life' | 'admin'

interface MainMenuItem {
  key: MainMenuKey
  label: string
  icon: string
  available: boolean
  badge?: string
}

const menuItems: MainMenuItem[] = [
  { key: 'character-search', label: '캐릭터 검색', icon: '🧭', available: true, badge: '기본' },
  { key: 'reforge', label: '제련', icon: '⚒️', available: true, badge: 'NEW' },
  { key: 'auction', label: '경매', icon: '💰', available: true, badge: 'DB' },
  { key: 'raid-schedule', label: '레이드 일정', icon: '🗓️', available: true, badge: 'T4' },
  { key: 'life', label: '생활', icon: '🌿', available: false, badge: '준비 중' },
  { key: 'admin', label: '관리 (내부)', icon: '🛠️', available: true }
]

const router = useRouter()
const route = useRoute()

const normalizeMenu = (value: unknown): MainMenuKey => {
  if (
    value === 'reforge' ||
    value === 'auction' ||
    value === 'raid-schedule' ||
    value === 'life' ||
    value === 'admin'
  )
    return value
  return 'character-search'
}

const { initTheme } = useTheme()
initTheme()

const activeMenu = ref<MainMenuKey>(normalizeMenu(route.params.menu))
const menuOpen = ref(false)
const serverStatus = ref<'unknown' | 'ok' | 'down'>('unknown')
let statusTimer: number | undefined

watch(
  () => route.params.menu,
  value => {
    activeMenu.value = normalizeMenu(value)
  }
)

const toggleMenu = () => {
  menuOpen.value = !menuOpen.value
}

const closeMenu = () => {
  menuOpen.value = false
}

const selectMenu = (menu: MainMenuKey) => {
  if (activeMenu.value === menu) return
  activeMenu.value = menu
  router.push({
    name: 'main',
    params: menu === 'character-search' ? {} : { menu }
  })
}

const handleMenuSelect = (menu: MainMenuKey) => {
  selectMenu(menu)
  closeMenu()
}

const componentMap: Record<MainMenuKey, unknown> = {
  'character-search': CharacterSearch,
  reforge: ReforgeMenu,
  auction: AuctionMenu,
  'raid-schedule': RaidSchedule,
  life: LifeMenu,
  admin: AdminStats
}

const activeComponent = computed(() => componentMap[activeMenu.value] ?? CharacterSearch)

const statusClass = computed(() => {
  if (serverStatus.value === 'ok') return 'status--ok'
  if (serverStatus.value === 'down') return 'status--down'
  return 'status--unknown'
})

const statusLabel = computed(() => {
  if (serverStatus.value === 'ok') return '서버상태'
  if (serverStatus.value === 'down') return '서버상태'
  return '서버상태'
})

const checkServer = async () => {
  try {
    const ok = await lostarkApi.checkServerStatus()
    serverStatus.value = ok ? 'ok' : 'down'
  } catch {
    serverStatus.value = 'down'
  }
}

onMounted(() => {
  checkServer()
  statusTimer = window.setInterval(checkServer, 30000)
})

onBeforeUnmount(() => {
  if (statusTimer) {
    window.clearInterval(statusTimer)
  }
})
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--page-bg, #f7f8fa);
}

.menu-trigger {
  position: fixed;
  top: 12px;
  left: 12px;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  display: grid;
  place-items: center;
  font-size: 1.2rem;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, border-color 0.2s ease;
  z-index: 10;
}

.menu-trigger:hover,
.menu-trigger:focus-visible {
  transform: translateY(-1px);
  border-color: var(--primary-color, #6366f1);
}

.menu-trigger__icon {
  line-height: 1;
  color: var(--text-primary, #111827);
}

.main-brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--text-primary, #1f2937);
}

.main-brand strong {
  font-size: 1.05rem;
}

.main-brand span {
  font-size: 0.82rem;
  color: var(--text-muted, #6b7280);
}

.menu-drawer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: var(--card-bg, #ffffff);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
  z-index: 30;
  overflow: visible;
}

.menu-drawer__inner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  overflow: visible;
}

.menu-inline {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  white-space: nowrap;
}

.menu-logo {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--text-primary, #111827);
}

.menu-items-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  overflow: visible;
  padding: 4px 0;
}

.menu-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.server-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  font-size: 0.92rem;
  color: var(--text-primary, #111827);
}

.server-status .status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d1d5db;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
}

.server-status.status--ok .status-dot {
  background: #16a34a;
}

.server-status.status--down .status-dot {
  background: #dc2626;
}

.server-status.status--unknown .status-dot {
  background: #d1d5db;
}

.menu-item {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-primary, #111827);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item__icon {
  font-size: 1.1rem;
}

.menu-item__label {
  flex: 1;
  text-align: left;
}

.menu-item__badge {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--bg-secondary, #eef2ff);
  font-size: 0.75rem;
  color: var(--primary-color, #6366f1);
}

.menu-item:hover:not(.disabled),
.menu-item:focus-visible:not(.disabled) {
  border-color: var(--primary-color, #6366f1);
  transform: translateY(-1px);
}

.menu-item.active {
  background: rgba(99, 102, 241, 0.16);
  border-color: var(--primary-color, #6366f1);
  color: var(--primary-color, #6366f1);
}

.menu-item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.menu-close {
  position: absolute;
  left: 50%;
  bottom: 0px;
  transform: translate(-50%, 50%) ;
  width: 40px;
  height: 34px;
  border-radius: 999px;
  border: none;
  background: var(--card-bg, #ffffff);
  box-shadow: 0 20px 30px rgba(0, 0, 0, 0.08);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.menu-close::after {
  content: '▲';
  font-size: 0.9rem;
  color: var(--text-primary, #111827);
  line-height: 1;
}

.menu-slide-enter-from,
.menu-slide-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.menu-slide-enter-active,
.menu-slide-leave-active {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.layout-body {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr) 160px;
  gap: 20px;
  padding: 0px 20px 20px 20px;
  flex: 1;
}

.layout-content {
  background: none;
}

.ad-slot {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  border-radius: 16px;
  border: 1px dashed var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  color: var(--text-muted, #9ca3af);
  font-weight: 600;
  margin-top:20px;
}

.page-footer {
  padding: 20px 24px;
  border-top: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
}

.footer-inner {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary, #6b7280);
  font-weight: 600;
}

.footer-badge {
  padding: 6px 10px;
  border-radius: 12px;
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-primary, #111827);
}

@media (max-width: 1100px) {
  .layout-body {
    grid-template-columns: 1fr;
  }

  .ad-slot {
    min-height: 120px;
  }
}

@media (max-width: 600px) {
  .layout-body {
    display: block;
    padding: 0px;
  }

  .ad-slot {
    min-height: 120px;
  }
}
</style>
