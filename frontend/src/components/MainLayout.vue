<template>
  <div class="main-layout">
    <Teleport to="#layout-top-menu">
      <nav class="menu-drawer" aria-label="주 메뉴">
        <div class="menu-drawer__inner">
          <div class="menu-left">
            <span class="menu-logo">Loap</span>
          </div>

          <div class="menu-divider" :class="{ 'menu-divider--hidden': !hasCenterMenu }" aria-hidden="true"></div>

          <nav
            class="menu-center"
            aria-label="메인 메뉴"
          >
            <div
              v-for="item in centerMenuItems"
              :key="item.key"
              class="menu-center__group"
            >
              <button
                type="button"
                class="menu-center__item"
                :class="{ active: activeMenu === item.key, disabled: !item.available }"
                :disabled="!item.available"
                @click="selectMenu(item.key)"
              >
                <span class="menu-center__icon" aria-hidden="true">{{ item.icon }}</span>
                <span class="menu-center__label">{{ item.label }}</span>
                <span v-if="item.badge" class="menu-center__badge">{{ item.badge }}</span>
              </button>
              <Transition name="menu-submenu-slide">
                <div
                  v-if="activeMenu === item.key && (activeMenu === 'character-search' || activeSubMenuItems.length)"
                  class="menu-submenu menu-submenu--inline"
                  :class="{ 'menu-submenu--character-search': activeMenu === 'character-search' }"
                  aria-label="서브 메뉴"
                >
                  <div
                    v-if="activeMenu === 'character-search'"
                    id="character-search-submenu"
                    class="menu-submenu__character-search"
                    aria-label="캐릭터 검색"
                  />
                  <template v-else>
                    <button
                      v-for="tab in activeSubMenuItems"
                      :key="tab.key"
                      type="button"
                      class="menu-submenu__item"
                      :class="{ active: activeSubMenuKey === tab.key }"
                      :disabled="tab.disabled"
                      @click="selectSubMenu(tab.key)"
                    >
                      {{ tab.label }}
                    </button>
                  </template>
                </div>
              </Transition>
            </div>
          </nav>

          <div class="menu-divider" :class="{ 'menu-divider--hidden': !hasCenterMenu }" aria-hidden="true"></div>

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
        <component :is="activeComponent" v-bind="activeComponentProps" />
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

type AuctionSubMenuKey = 'market' | 'auction-house'
type ReforgeSubMenuKey = 'normal' | 'advanced' | 'blunt-thorn' | 'supersonic'
type RaidSubMenuKey = 'party'
type AdminSubMenuKey = 'market-records' | 'raid-catalog'

type SubMenuItem = { key: string; label: string; disabled?: boolean }

const menuItems: MainMenuItem[] = [
  { key: 'character-search', label: '캐릭터 검색', icon: '😊', available: true, badge: '기본' },
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
const auctionSubMenu = ref<AuctionSubMenuKey>('market')
const reforgeSubMenu = ref<ReforgeSubMenuKey>('normal')
const raidSubMenu = ref<RaidSubMenuKey>('party')
const adminSubMenu = ref<AdminSubMenuKey>('market-records')

const centerMenuItems = computed(() => menuItems)

const hasCenterMenu = computed(() => centerMenuItems.value.length > 0)

const activeSubMenuItems = computed<SubMenuItem[]>(() => {
  if (activeMenu.value === 'auction') {
    return [
      { key: 'market', label: '거래소' },
      { key: 'auction-house', label: '경매장' },
    ]
  }
  if (activeMenu.value === 'reforge') {
    return [
      { key: 'normal', label: '제련' },
      { key: 'advanced', label: '상급 제련' },
      { key: 'blunt-thorn', label: '뭉가 계산기' },
      { key: 'supersonic', label: '음돌 계산기' },
    ]
  }
  if (activeMenu.value === 'raid') {
    return [
      { key: 'party', label: '레이드 모집' },
    ]
  }
  if (activeMenu.value === 'admin') {
    return [
      { key: 'market-records', label: '거래소 기록' },
      { key: 'raid-catalog', label: '레이드 추가' },
    ]
  }
  return []
})

const activeSubMenuKey = computed<string | null>(() => {
  if (activeMenu.value === 'auction') return auctionSubMenu.value
  if (activeMenu.value === 'reforge') return reforgeSubMenu.value
  if (activeMenu.value === 'raid') return raidSubMenu.value
  if (activeMenu.value === 'admin') return adminSubMenu.value
  return null
})

const selectSubMenu = (key: string) => {
  if (activeMenu.value === 'auction') {
    if (key === 'market' || key === 'auction-house') auctionSubMenu.value = key
    return
  }

  if (activeMenu.value === 'reforge') {
    if (key === 'normal' || key === 'advanced' || key === 'blunt-thorn' || key === 'supersonic') {
      reforgeSubMenu.value = key
    }
    return
  }

  if (activeMenu.value === 'raid') {
    if (key === 'party') raidSubMenu.value = key
    return
  }

  if (activeMenu.value === 'admin') {
    if (key === 'market-records' || key === 'raid-catalog') adminSubMenu.value = key
  }
}

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
const activeComponentProps = computed<Record<string, unknown>>(() => {
  if (activeMenu.value === 'auction') return { activeSubMenu: auctionSubMenu.value }
  if (activeMenu.value === 'reforge') return { activeSubMenuTab: reforgeSubMenu.value }
  if (activeMenu.value === 'admin') return { activeTab: adminSubMenu.value }
  return {}
})

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
  } catch (err: unknown) {
    void err
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
  grid-template-columns: auto 1px 1fr 1px auto;
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

.menu-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.menu-submenu {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.menu-submenu--inline {
  flex-wrap: nowrap;
}

.menu-submenu-slide-enter-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.menu-submenu-slide-enter-from {
  opacity: 0;
  transform: translateX(-6px);
}

.menu-submenu-slide-leave-active {
  transition: none;
}

.menu-submenu-slide-leave-to {
  opacity: 0;
  transform: none;
}

.menu-submenu--character-search {
  flex-wrap: nowrap;
}

.menu-submenu__character-search {
  flex: 0 1 260px;
  min-width: 200px;
  width: min(260px, 28vw);
}

.menu-submenu__item {
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  border-radius: 999px;
  padding: 8px 12px;
  cursor: pointer;
  font-weight: 750;
  color: var(--text-secondary, #374151);
  transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
}

.menu-submenu__item.active {
  background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16));
  color: var(--primary-color, #6366f1);
  border-color: rgba(99, 102, 241, 0.35);
}

.menu-submenu__item:hover:not(:disabled) {
  transform: translateY(-1px);
}

.menu-submenu__item:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.menu-divider {
  width: 1px;
  height: 36px;
  background-color: rgba(0, 0, 0, 0.12);
  align-self: center;
  transition: opacity 0.18s ease;
}

.menu-divider--hidden {
  opacity: 0;
}

.menu-center {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  overflow: visible;
  justify-content: center;
  min-width: 0;
  padding: 4px 0;
  scrollbar-width: none;
}

.menu-center__group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.menu-center::-webkit-scrollbar {
  height: 0;
  display: none;
}

.menu-center__item {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: transparent;
  color: var(--text-primary, #111827);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.menu-center__item.active {
  background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16));
  border-color: rgba(99, 102, 241, 0.35);
  color: var(--primary-color, #6366f1);
  font-weight: 700;
}

.menu-center__icon {
  font-size: 1.1rem;
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: var(--card-bg, #ffffff);
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.menu-center__label {
  font-size: 0.92rem;
}

.menu-center__badge {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--bg-secondary, #eef2ff);
  font-size: 0.75rem;
  color: var(--primary-color, #6366f1);
}

.menu-center__item:hover:not(.disabled),
.menu-center__item:focus-visible:not(.disabled) {
  border-color: var(--primary-color, #6366f1);
  transform: translateY(-1px);
}

.menu-center__item.disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
