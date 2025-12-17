<template>
  <div class="main-layout">
    <Teleport to="#layout-top-menu">
      <nav class="menu-drawer" aria-label="주 메뉴">
        <div class="menu-drawer__inner">
          <span class="menu-logo">Loap</span>
          <div class="menu-items-row">
            <button
              v-for="item in menuItems"
              :key="item.key"
              type="button"
              class="menu-item"
              :class="{ active: activeMenu === item.key, disabled: !item.available }"
              :disabled="!item.available"
              @click="selectMenu(item.key)"
            >
              <span class="menu-item__icon" aria-hidden="true">{{ item.icon }}</span>
              <span class="menu-item__label">{{ item.label }}</span>
              <span v-if="item.badge" class="menu-item__badge">{{ item.badge }}</span>
            </button>
          </div>
          <div class="menu-actions">
            <button
              type="button"
              class="menu-myinfo top-menu-action-btn"
              :class="{ active: activeMenu === 'friends' || activeMenu === 'characters' }"
              @click="handleMyInfoClick"
            >
              <span class="menu-myinfo__icon" aria-hidden="true">👤</span>
              내정보
            </button>
            <div class="server-status" :class="statusClass">
              <span class="status-dot" aria-hidden="true"></span>
              <span class="status-label">{{ statusLabel }}</span>
            </div>
            <ThemeToggle class="menu-theme-toggle" />
          </div>
        </div>
      </nav>
    </Teleport>

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

    <MyInfoModal :show="myInfoOpen" @close="closeMyInfo" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CharacterSearch from './CharacterSearch.vue'
import ReforgeMenu from './ReforgeMenu.vue'
import AuctionMenu from './AuctionMenu.vue'
import FriendManager from './FriendManager.vue'
import LifeMenu from './LifeMenu.vue'
import AdminMenu from './AdminMenu.vue'
import CharacterManager from './CharacterManager.vue'
import RaidMenu from './RaidMenu.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import MyInfoModal from './common/MyInfoModal.vue'
import { useTheme } from '@/composables/useTheme'
import { lostarkApi } from '@/api/lostark'

type MainMenuKey =
  | 'character-search'
  | 'reforge'
  | 'auction'
  | 'raid'
  | 'friends'
  | 'characters'
  | 'life'
  | 'admin'

interface MainMenuItem {
  key: MainMenuKey
  label: string
  icon: string
  available: boolean
  badge?: string
}

const menuItems: MainMenuItem[] = [
  { key: 'character-search', label: '캐릭터 검색', icon: '🧭', available: true, badge: '기본' },
  { key: 'auction', label: '아이템 검색', icon: '💰', available: true, badge: '' },
  { key: 'reforge', label: '제련', icon: '⚒️', available: true, badge: '' },
  { key: 'raid', label: '레이드', icon: '⚔️', available: true, badge: '' },
  { key: 'life', label: '생활', icon: '🌿', available: false, badge: '준비 중' },
  { key: 'admin', label: '관리 (내부)', icon: '🛠️', available: true }
]

const router = useRouter()
const route = useRoute()

const normalizeMenu = (value: unknown): MainMenuKey => {
  if (
    value === 'reforge' ||
    value === 'auction' ||
    value === 'raid' ||
    value === 'friends' ||
    value === 'characters' ||
    value === 'life' ||
    value === 'admin'
  )
    return value
  if (value === 'raid-schedule' || value === 'raid-party') return 'raid'
  return 'character-search'
}

const { initTheme } = useTheme()
initTheme()

const activeMenu = ref<MainMenuKey>(normalizeMenu(route.params.menu))
const serverStatus = ref<'unknown' | 'ok' | 'down'>('unknown')
let statusTimer: number | undefined
let menuHeaderResizeObserver: ResizeObserver | null = null
let menuHeaderHeight = 0
let hasMenuHeaderResizeListener = false
let handleMenuHeaderResize: (() => void) | null = null

const setMenuHeaderHeight = (height: number) => {
  if (typeof document === 'undefined') return
  const roundedHeight = Math.ceil(height)
  if (roundedHeight <= menuHeaderHeight) return
  menuHeaderHeight = roundedHeight
  document.documentElement.style.setProperty('--menu-header-height', `${menuHeaderHeight}px`)
}

watch(
  () => route.params.menu,
  value => {
    if (value === 'raid-schedule' || value === 'raid-party') {
      activeMenu.value = 'raid'
      void router
        .replace({
          name: 'main',
          params: { menu: 'raid' },
          query: { ...route.query },
        })
        .catch(() => undefined)
      return
    }
    activeMenu.value = normalizeMenu(value)
  }
)

const myInfoOpen = ref(false)

const closeMyInfo = () => {
  myInfoOpen.value = false
}

const handleMyInfoClick = () => {
  myInfoOpen.value = true
}

const selectMenu = (menu: MainMenuKey) => {
  if (activeMenu.value === menu) return
  activeMenu.value = menu
  void router.push({
    name: 'main',
    params: menu === 'character-search' ? {} : { menu },
  }).catch(() => undefined)
}

const componentMap: Record<MainMenuKey, unknown> = {
  'character-search': CharacterSearch,
  reforge: ReforgeMenu,
  auction: AuctionMenu,
  raid: RaidMenu,
  friends: FriendManager,
  characters: CharacterManager,
  life: LifeMenu,
  admin: AdminMenu
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

  if (typeof document !== 'undefined') {
    const headerHost = document.getElementById('layout-top-main')
    if (headerHost) {
      const measureMenuHeader = () => {
        setMenuHeaderHeight(headerHost.getBoundingClientRect().height)
      }

      measureMenuHeader()
      if (typeof window !== 'undefined') {
        window.requestAnimationFrame(() => {
          measureMenuHeader()
          window.requestAnimationFrame(measureMenuHeader)
        })
      }

      if (typeof ResizeObserver !== 'undefined') {
        menuHeaderResizeObserver = new ResizeObserver(entries => {
          for (const entry of entries) {
            setMenuHeaderHeight(entry.contentRect.height)
          }
        })
        menuHeaderResizeObserver.observe(headerHost)
      } else if (typeof window !== 'undefined') {
        hasMenuHeaderResizeListener = true
        handleMenuHeaderResize = () => {
          setMenuHeaderHeight(headerHost.getBoundingClientRect().height)
        }
        window.addEventListener('resize', handleMenuHeaderResize)
      }
    }
  }
})

onBeforeUnmount(() => {
  if (statusTimer) {
    window.clearInterval(statusTimer)
  }

  if (menuHeaderResizeObserver) {
    menuHeaderResizeObserver.disconnect()
    menuHeaderResizeObserver = null
  }
  if (hasMenuHeaderResizeListener) {
    if (handleMenuHeaderResize) {
      window.removeEventListener('resize', handleMenuHeaderResize)
      handleMenuHeaderResize = null
    }
    hasMenuHeaderResizeListener = false
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
  width: 100%;
  background: var(--card-bg, #ffffff);
}

.menu-drawer__inner {
  display: grid;
  grid-template-columns: 0.5fr 1fr 0.5fr;
  width: 100%;
  height: 100%;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
}

.menu-logo {
  font-size: var(--font-2xl);
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
  justify-content: flex-end;
  gap: 10px;
}

.menu-myinfo.active {
  background: rgba(99, 102, 241, 0.16);
  border-color: var(--primary-color, #6366f1);
  color: var(--primary-color, #6366f1);
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
