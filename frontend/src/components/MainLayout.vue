<template>
  <div class="main-layout">
    <header class="main-header">
      <div class="main-brand">
        <strong>LOA Toolkit</strong>
        <span>Lost Ark 도우미</span>
      </div>
      <nav class="main-nav" aria-label="주 메뉴">
        <button
          v-for="item in menuItems"
          :key="item.key"
          type="button"
          class="main-nav__btn"
          :class="{ active: activeMenu === item.key, disabled: !item.available }"
          :disabled="!item.available"
          @click="selectMenu(item.key)"
        >
          <span aria-hidden="true">{{ item.icon }}</span>
          {{ item.label }}
          <span v-if="item.badge" class="main-nav__badge">{{ item.badge }}</span>
        </button>
      </nav>
      <ThemeToggle class="main-theme-toggle" />
    </header>

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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CharacterSearch from './CharacterSearch.vue'
import ReforgeMenu from './ReforgeMenu.vue'
import AuctionMenu from './AuctionMenu.vue'
import LifeMenu from './LifeMenu.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import { useTheme } from '@/composables/useTheme'

type MainMenuKey = 'character-search' | 'reforge' | 'auction' | 'life'

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
  { key: 'auction', label: '경매', icon: '💰', available: false, badge: '준비 중' },
  { key: 'life', label: '생활', icon: '🌿', available: false, badge: '준비 중' }
]

const router = useRouter()
const route = useRoute()

const normalizeMenu = (value: unknown): MainMenuKey => {
  if (value === 'reforge' || value === 'auction' || value === 'life') return value
  return 'character-search'
}

const { initTheme } = useTheme()
initTheme()

const activeMenu = ref<MainMenuKey>(normalizeMenu(route.params.menu))

watch(
  () => route.params.menu,
  value => {
    activeMenu.value = normalizeMenu(value)
  }
)

const selectMenu = (menu: MainMenuKey) => {
  if (activeMenu.value === menu) return
  activeMenu.value = menu
  router.push({
    name: 'main',
    params: menu === 'character-search' ? {} : { menu }
  })
}

const componentMap: Record<MainMenuKey, unknown> = {
  'character-search': CharacterSearch,
  reforge: ReforgeMenu,
  auction: AuctionMenu,
  life: LifeMenu
}

const activeComponent = computed(() => componentMap[activeMenu.value] ?? CharacterSearch)
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--page-bg, #f7f8fa);
}

.main-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  position: sticky;
  top: 0;
  z-index: 10;
}

.main-brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--text-primary, #1f2937);
}

.main-brand strong {
  font-size: 1.1rem;
}

.main-brand span {
  font-size: 0.85rem;
  color: var(--text-muted, #6b7280);
}

.main-nav {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.main-nav__btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-primary, #111827);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.main-nav__btn:hover {
  border-color: var(--primary-color, #6366f1);
}

.main-nav__btn.active {
  background: rgba(99, 102, 241, 0.16);
  border-color: var(--primary-color, #6366f1);
  color: var(--primary-color, #6366f1);
}

.main-nav__btn.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.main-nav__badge {
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--bg-secondary, #eef2ff);
  font-size: 0.75rem;
  color: var(--primary-color, #6366f1);
}

.main-theme-toggle {
  margin-left: auto;
}

.layout-body {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr) 160px;
  gap: 20px;
  padding: 20px 24px 40px;
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
</style>
