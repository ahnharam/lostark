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
              :class="{
                'menu-center__group--active': visualActiveMenu === item.key,
                'menu-center__group--closing': closingMenuKey === item.key,
                'menu-center__group--disabled': !item.available
              }"
            >
              <button
                type="button"
                class="menu-center__item"
                :class="{
                  active: visualActiveMenu === item.key || closingMenuKey === item.key,
                  disabled: !item.available
                }"
                :disabled="!item.available"
                @click="selectMenu(item.key)"
              >
                <!-- <span class="menu-center__icon" aria-hidden="true">{{ item.icon }}</span> -->
                <span class="menu-center__label">{{ item.label }}</span>
                <span v-if="item.badge" class="menu-center__badge">{{ item.badge }}</span>
              </button>
              <Transition name="menu-submenu-slide">
                <div
                  v-if="shouldShowSubMenu(item.key)"
                  class="menu-submenu menu-submenu--inline"
                  :class="{ 'menu-submenu--character-search': item.key === 'character-search' }"
                  aria-label="서브 메뉴"
                >
                  <div
                    v-if="item.key === 'character-search'"
                    id="character-search-submenu"
                    class="menu-submenu__character-search"
                    aria-label="캐릭터 검색"
                  />
                  <template v-else>
                    <button
                      v-for="tab in getSubMenuItemsForRender(item.key)"
                      :key="tab.key"
                      type="button"
                      class="menu-submenu__item"
                      :class="{ active: visualActiveMenu === item.key && activeSubMenuKey === tab.key }"
                      :disabled="tab.disabled || closingMenuKey === item.key"
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
        <router-view v-slot="{ Component }">
          <component :is="Component" v-bind="activeComponentProps" />
        </router-view>
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
import { computed, ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

// route.meta에서 activeMenu 추출
const activeMenu = computed<MainMenuKey>(() => {
  const menu = route.meta.menu as MainMenuKey | undefined
  return menu || 'character-search'
})

// route.meta에서 activeSubMenu 추출
const activeSubMenuKey = computed<string | null>(() => {
  return (route.meta.submenu as string) || null
})

const auctionSubMenu = ref<AuctionSubMenuKey>('market')
const reforgeSubMenu = ref<ReforgeSubMenuKey>('normal')
const raidSubMenu = ref<RaidSubMenuKey>('party')
const adminSubMenu = ref<AdminSubMenuKey>('market-records')

const centerMenuItems = computed(() => menuItems)

const hasCenterMenu = computed(() => centerMenuItems.value.length > 0)

const getSubMenuItems = (menu: MainMenuKey): SubMenuItem[] => {
  if (menu === 'auction') {
    return [
      { key: 'market', label: '거래소' },
      { key: 'auction-house', label: '경매장' },
    ]
  }
  if (menu === 'reforge') {
    return [
      { key: 'normal', label: '일반 제련' },
      { key: 'advanced', label: '상급 제련' },
      { key: 'blunt-thorn', label: '뭉가 계산기' },
      { key: 'supersonic', label: '음돌 계산기' },
    ]
  }
  if (menu === 'raid') {
    return [
      { key: 'party', label: '레이드 모집' },
    ]
  }
  if (menu === 'admin') {
    return [
      { key: 'market-records', label: '거래소 기록' },
      { key: 'raid-catalog', label: '레이드 관리' },
    ]
  }
  return []
}

const activeSubMenuItems = computed<SubMenuItem[]>(() => {
  return getSubMenuItems(activeMenu.value)
})

const closingSubMenuItems = computed<SubMenuItem[]>(() => {
  if (!closingMenuKey.value) return []
  return getSubMenuItems(closingMenuKey.value)
})

const shouldShowSubMenu = (menu: MainMenuKey): boolean => {
  if (visualActiveMenu.value === menu) {
    return menu === 'character-search' || activeSubMenuItems.value.length > 0
  }
  if (closingMenuKey.value === menu) {
    return menu === 'character-search' || closingSubMenuItems.value.length > 0
  }
  return false
}

const getSubMenuItemsForRender = (menu: MainMenuKey): SubMenuItem[] => {
  if (visualActiveMenu.value === menu) {
    return activeSubMenuItems.value
  }
  if (closingMenuKey.value === menu) {
    return closingSubMenuItems.value
  }
  return []
}

const visualActiveMenu = ref<MainMenuKey | null>(null)
const closingMenuKey = ref<MainMenuKey | null>(null)
const MENU_SWITCH_DURATION_MS = 1000
let menuSwitchTimer: number | undefined

watch(
  activeMenu,
  (nextMenu, prevMenu) => {
    if (!prevMenu) {
      visualActiveMenu.value = nextMenu
      closingMenuKey.value = null
      return
    }
    if (nextMenu === prevMenu) return

    if (menuSwitchTimer) {
      if (typeof window !== 'undefined') {
        window.clearTimeout(menuSwitchTimer)
      }
      menuSwitchTimer = undefined
    }

    visualActiveMenu.value = nextMenu
    closingMenuKey.value = prevMenu

    if (typeof window === 'undefined') {
      closingMenuKey.value = null
      return
    }

    menuSwitchTimer = window.setTimeout(() => {
      closingMenuKey.value = null
      menuSwitchTimer = undefined
    }, MENU_SWITCH_DURATION_MS)
  },
  { immediate: true }
)

const selectSubMenu = (key: string) => {
  // router.push를 사용하여 서브메뉴 이동
  if (activeMenu.value === 'auction') {
    if (key === 'market') {
      void router.push({ name: 'auction-market' }).catch(() => undefined)
    } else if (key === 'auction-house') {
      void router.push({ name: 'auction-house' }).catch(() => undefined)
    }
    return
  }

  if (activeMenu.value === 'reforge') {
    if (key === 'normal') {
      void router.push({ name: 'reforge-normal' }).catch(() => undefined)
    } else if (key === 'advanced') {
      void router.push({ name: 'reforge-advanced' }).catch(() => undefined)
    } else if (key === 'blunt-thorn') {
      void router.push({ name: 'reforge-blunt-thorn' }).catch(() => undefined)
    } else if (key === 'supersonic') {
      void router.push({ name: 'reforge-supersonic' }).catch(() => undefined)
    }
    return
  }

  if (activeMenu.value === 'raid') {
    if (key === 'party') {
      void router.push({ name: 'raid-party' }).catch(() => undefined)
    }
    return
  }

  if (activeMenu.value === 'admin') {
    if (key === 'market-records') {
      void router.push({ name: 'admin-market-records' }).catch(() => undefined)
    } else if (key === 'raid-catalog') {
      void router.push({ name: 'admin-raid-catalog' }).catch(() => undefined)
    }
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

// route.meta에서 activeMenu를 computed로 가져오므로 watch 불필요

const myInfoOpen = ref(false)

const closeMyInfo = () => {
  myInfoOpen.value = false
}

const handleMyInfoClick = () => {
  myInfoOpen.value = true
}

const selectMenu = (menu: MainMenuKey) => {
  // 이미 같은 메뉴면 리턴
  if (activeMenu.value === menu) return

  // router.push를 사용하여 메뉴 이동
  if (menu === 'character-search') {
    void router.push({ name: 'character-search' }).catch(() => undefined)
  } else if (menu === 'auction') {
    void router.push({ name: 'auction-market' }).catch(() => undefined)
  } else if (menu === 'reforge') {
    void router.push({ name: 'reforge-normal' }).catch(() => undefined)
  } else if (menu === 'raid') {
    void router.push({ name: 'raid-party' }).catch(() => undefined)
  } else if (menu === 'admin') {
    void router.push({ name: 'admin-market-records' }).catch(() => undefined)
  } else if (menu === 'friends') {
    void router.push({ name: 'friends' }).catch(() => undefined)
  } else if (menu === 'characters') {
    void router.push({ name: 'characters' }).catch(() => undefined)
  } else if (menu === 'life') {
    void router.push({ name: 'life' }).catch(() => undefined)
  }
}

// activeComponentProps는 각 컴포넌트에서 route.meta.submenu를 직접 사용하도록 변경 예정
const activeComponentProps = computed<Record<string, unknown>>(() => {
  // 기존 props 방식 유지 (추후 각 컴포넌트에서 route.meta 직접 사용하도록 마이그레이션)
  if (activeMenu.value === 'auction') return { activeSubMenu: activeSubMenuKey.value || 'market' }
  if (activeMenu.value === 'reforge') return { activeSubMenuTab: activeSubMenuKey.value || 'normal' }
  if (activeMenu.value === 'admin') return { activeTab: activeSubMenuKey.value || 'market-records' }
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
  if (menuSwitchTimer) {
    window.clearTimeout(menuSwitchTimer)
    menuSwitchTimer = undefined
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
  flex-wrap: nowrap;
  white-space: nowrap;
  border: 0;
  border-radius: 0;
  padding: 4px 10px 4px 6px;
  overflow: hidden;
  max-width: var(--submenu-max-width, 640px);
}

.menu-submenu--inline {
  flex-wrap: nowrap;
}

.menu-submenu-slide-enter-active {
  transition: max-width 1s ease, opacity 1s ease, transform 1s ease, padding 1s ease;
}

.menu-submenu-slide-leave-active {
  transition: max-width 1s ease, opacity 1s ease, transform 1s ease, padding 1s ease;
}

.menu-submenu-slide-enter-from,
.menu-submenu-slide-leave-to {
  max-width: 0;
  opacity: 0;
  transform: translateX(-10px);
  padding-left: 0;
  padding-right: 0;
}

.menu-submenu-slide-enter-to,
.menu-submenu-slide-leave-from {
  max-width: var(--submenu-max-width, 640px);
  opacity: 1;
  transform: translateX(0);
  padding-left: 6px;
  padding-right: 10px;
}

.menu-submenu--character-search {
  flex-wrap: nowrap;
  --submenu-max-width: 320px;
}

.menu-submenu__character-search {
  flex: 0 1 260px;
  min-width: 200px;
  width: min(260px, 28vw);
}

.menu-submenu__item {
  border: unset;
  background: unset;
  /* border-radius: 999px; */
  padding: 8px 12px;
  white-space: nowrap;
  cursor: pointer;
  font-weight: 750;
  color: var(--text-secondary, #374151);
  transition: transform 0.18s ease, border-color 0.18s ease, background-color 0.18s ease;
  max-height: 35px;
}

.menu-submenu__item.active {
  /* background: var(--primary-soft-bg, rgba(99, 102, 241, 0.16)); */
  color: var(--primary-color, #6366f1);
  /* border-color: rgba(99, 102, 241, 0.35); */
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
  border: 1px solid transparent;
  border-radius: 999px;
  overflow: hidden;
  transition: border-color 0.2s ease;
  min-width: 0;
}

.menu-center__group:not(.menu-center__group--disabled):not(.menu-center__group--active):not(.menu-center__group--closing):hover,
.menu-center__group:not(.menu-center__group--disabled):not(.menu-center__group--active):not(.menu-center__group--closing):focus-within {
  border-color: var(--primary-color, #6366f1);
}

.menu-center__group--active {
  border-color: var(--primary-color, #6366f1);
}

.menu-center__group--closing {
  border-color: transparent;
  transition: border-color 2s ease;
}

.menu-center::-webkit-scrollbar {
  height: 0;
  display: none;
}

.menu-center__item {
  display: inline-flex;
  align-items: center;
  /* gap: 10px; */
  padding: 10px 16px;
  border-radius: 999px 0px 0px 999px;
  border: 0;
  background: transparent;
  color: var(--text-primary, #111827);
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s ease, background-color 0.2s ease;
  white-space: nowrap;
}

.menu-center__item.active {
  color: var(--primary-color, #6366f1);
  font-weight: 700;
  border-right: 1px dashed var(--primary-color, #6366f1);
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
  color: var(--primary-color, #6366f1);
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
