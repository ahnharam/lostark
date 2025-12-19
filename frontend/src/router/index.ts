import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/components/MainLayout.vue'),
      children: [
        // ========================================
        // 캐릭터 검색 (홈)
        // ========================================
        {
          path: '',
          name: 'character-search',
          component: () => import('@/components/CharacterSearch.vue'),
          meta: { menu: 'character-search' }
        },

        // ========================================
        // 경매장 (Auction)
        // ========================================
        {
          path: 'auction',
          name: 'auction',
          component: () => import('@/components/AuctionMenu.vue'),
          redirect: { name: 'auction-market' },
          meta: { menu: 'auction' },
          children: [
            {
              path: 'market',
              name: 'auction-market',
              component: () => import('@/components/auction/MarketView.vue'),
              meta: { menu: 'auction', submenu: 'market' }
            },
            {
              path: 'auction-house',
              name: 'auction-house',
              component: () => import('@/components/auction/AuctionHouseView.vue'),
              meta: { menu: 'auction', submenu: 'auction-house' }
            }
          ]
        },

        // ========================================
        // 재련 (Reforge)
        // ========================================
        {
          path: 'reforge',
          name: 'reforge',
          component: () => import('@/components/ReforgeMenu.vue'),
          redirect: { name: 'reforge-normal' },
          meta: { menu: 'reforge' },
          children: [
            {
              path: 'normal',
              name: 'reforge-normal',
              component: () => import('@/components/ReforgeMenu.vue'),
              meta: { menu: 'reforge', submenu: 'normal' }
            },
            {
              path: 'advanced',
              name: 'reforge-advanced',
              component: () => import('@/components/ReforgeMenu.vue'),
              meta: { menu: 'reforge', submenu: 'advanced' }
            },
            {
              path: 'blunt-thorn',
              name: 'reforge-blunt-thorn',
              component: () => import('@/components/ReforgeMenu.vue'),
              meta: { menu: 'reforge', submenu: 'blunt-thorn' }
            },
            {
              path: 'supersonic',
              name: 'reforge-supersonic',
              component: () => import('@/components/ReforgeMenu.vue'),
              meta: { menu: 'reforge', submenu: 'supersonic' }
            }
          ]
        },

        // ========================================
        // 레이드 (Raid)
        // ========================================
        {
          path: 'raid',
          name: 'raid',
          component: () => import('@/components/RaidMenu.vue'),
          redirect: { name: 'raid-party' },
          meta: { menu: 'raid' },
          children: [
            {
              path: 'party',
              name: 'raid-party',
              component: () => import('@/components/RaidPartyManager.vue'),
              meta: { menu: 'raid', submenu: 'party' }
            }
          ]
        },

        // ========================================
        // 친구 관리 (Friends)
        // ========================================
        {
          path: 'friends',
          name: 'friends',
          component: () => import('@/components/FriendManager.vue'),
          meta: { menu: 'friends' }
        },

        // ========================================
        // 캐릭터 관리 (Characters)
        // ========================================
        {
          path: 'characters',
          name: 'characters',
          component: () => import('@/components/CharacterManager.vue'),
          meta: { menu: 'characters' }
        },

        // ========================================
        // 생활 (Life)
        // ========================================
        {
          path: 'life',
          name: 'life',
          component: () => import('@/components/LifeMenu.vue'),
          meta: { menu: 'life' }
        },

        // ========================================
        // 관리자 (Admin)
        // ========================================
        {
          path: 'admin',
          name: 'admin',
          component: () => import('@/components/AdminMenu.vue'),
          redirect: { name: 'admin-market-records' },
          meta: { menu: 'admin' },
          children: [
            {
              path: 'market-records',
              name: 'admin-market-records',
              component: () => import('@/components/AdminStats.vue'),
              meta: { menu: 'admin', submenu: 'market-records' }
            },
            {
              path: 'raid-catalog',
              name: 'admin-raid-catalog',
              component: () => import('@/components/AdminRaidCatalog.vue'),
              meta: { menu: 'admin', submenu: 'raid-catalog' }
            }
          ]
        },

        // ========================================
        // 레거시 URL 리다이렉트
        // ========================================
        {
          path: ':menu(auction|reforge|raid|admin|friends|characters|life)',
          redirect: to => {
            const menu = to.params.menu as string
            const defaultSubMenus: Record<string, string> = {
              auction: 'market',
              reforge: 'normal',
              raid: 'party',
              admin: 'market-records'
            }

            // 서브메뉴가 있는 경우
            if (defaultSubMenus[menu]) {
              return {
                path: `/${menu}/${defaultSubMenus[menu]}`,
                query: to.query  // 쿼리 파라미터 보존
              }
            }

            // 서브메뉴가 없는 경우 (friends, characters, life)
            return {
              name: menu as string,
              query: to.query
            }
          }
        },

        // 레거시 raid-schedule, raid-party 리다이렉트
        {
          path: ':legacy(raid-schedule|raid-party)',
          redirect: to => ({
            name: 'raid-party',
            query: to.query
          })
        }
      ]
    }
  ]
})

export default router
