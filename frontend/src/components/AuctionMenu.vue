<template>
  <div class="auction-page">
    <header class="auction-hero">
      <div>
        <p class="eyebrow">경매 데이터 뷰어</p>
        <h2>DB에 저장된 거래소 아이템을 한눈에</h2>
        <p class="hero-desc">
          백엔드에서 동기화한 거래소 스냅샷을 필터링하고, 빠르게 찾아보세요. 카테고리를 바꾸거나 페이지를 넘기면 즉시 새로 불러옵니다.
        </p>
      </div>
      <div class="hero-meta">
        <div class="meta-chip">
          <span class="meta-label">총 카테고리</span>
          <strong>{{ categoryCount }}</strong>
        </div>
        <div class="meta-chip">
          <span class="meta-label">수집된 아이템</span>
          <strong>{{ formatNumber(totalItems) }}</strong>
        </div>
        <div class="meta-chip accent" :class="{ muted: !lastFetchedLabel }">
          <span class="meta-label">최근 수집</span>
          <strong>{{ lastFetchedLabel || '정보 없음' }}</strong>
        </div>
      </div>
    </header>

    <section class="control-panel panel-card">
      <div class="control-row">
        <label class="field">
          <span class="field-label">카테고리</span>
          <select v-model="selectedCategory" class="input" :disabled="loadingCategories">
            <option :value="null">전체 보기</option>
            <option v-for="cat in leafCategories" :key="cat.code" :value="cat.code">
              {{ cat.codeName }}
            </option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">직업</span>
          <select v-model="characterClass" class="input">
            <option :value="null">전체</option>
            <option v-for="cls in classes" :key="cls" :value="cls">{{ cls }}</option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">티어</span>
          <select v-model.number="itemTier" class="input">
            <option :value="null">전체</option>
            <option v-for="tier in itemTiers" :key="tier" :value="tier">{{ tier }} 티어</option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">등급</span>
          <select v-model="itemGrade" class="input">
            <option :value="null">전체</option>
            <option v-for="grade in itemGrades" :key="grade" :value="grade">{{ grade }}</option>
          </select>
        </label>

        <label class="field field--small">
          <span class="field-label">정렬</span>
          <div class="sort-row">
            <select v-model="sort" class="input">
              <option value="RECENT_PRICE">최근 거래가</option>
              <option value="CURRENT_MIN_PRICE">현재 최저가</option>
              <option value="YDAY_AVG_PRICE">전일 평균가</option>
            </select>
            <select v-model="sortCondition" class="input sort-order">
              <option value="ASC">오름차순</option>
              <option value="DESC">내림차순</option>
            </select>
          </div>
        </label>

        <label class="field field--small">
          <span class="field-label">페이징</span>
          <select v-model.number="pageSize" class="input" @change="resetAndLoad">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="30">30</option>
            <option :value="50">50</option>
          </select>
        </label>

        <label class="field">
          <span class="field-label">검색 (이름)</span>
          <input
            v-model.trim="searchText"
            class="input"
            type="text"
            placeholder="예: 파괴석, 각인서..."
            @keyup.enter="handleSearch"
          />
        </label>

        <div class="actions">
          <button type="button" class="btn" @click="handleSearch">검색</button>
          <button type="button" class="btn ghost" @click="clearSearch">검색 초기화</button>
          <button type="button" class="btn ghost refresh-btn" @click="resetAndLoad">
            <span class="btn-icon" aria-hidden="true"></span>
            <span class="btn-label">새로고침</span>
          </button>
        </div>
      </div>

      <p v-if="!leafCategories.length" class="inline-hint">
        백엔드에서 `/api/markets/options/sync` 호출 후 다시 시도하세요.
      </p>
    </section>

    <section class="items-panel panel-card">
      <div class="items-head">
        <div>
          <p class="eyebrow">아이템 목록</p>
          <h3>거래소 스냅샷 {{ filteredItems.length ? `(${filteredItems.length}개 표시 중)` : '' }}</h3>
        </div>
        <div class="pager">
          <button class="pager-btn" type="button" :disabled="page === 1 || loadingItems" @click="changePage(page - 1)">
            이전
          </button>
          <span class="pager-page">{{ page }} / {{ totalPages }}</span>
          <button
            class="pager-btn"
            type="button"
            :disabled="page >= totalPages || loadingItems"
            @click="changePage(page + 1)"
          >
            다음
          </button>
        </div>
      </div>

      <LoadingSpinner v-if="loadingItems && !currentItems.length" message="아이템을 불러오는 중..." />
      <p v-else-if="errorMessage" class="error-text">{{ errorMessage }}</p>
      <p v-else-if="!currentItems.length" class="empty-state">저장된 아이템이 없습니다.</p>

      <div v-else class="table">
        <div class="table-head">
          <span>아이템</span>
          <span class="align-right">현재 최저가</span>
          <span class="align-right">최근 거래가</span>
          <span class="align-right">전일 평균</span>
          <span class="align-right">묶음/거래</span>
          <span class="align-right">카테고리</span>
          <span class="align-right">수집시각</span>
        </div>
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="table-row"
          @click="openDetail(item)"
          role="button"
          tabindex="0"
        >
          <div class="item-cell">
            <LazyImage
              v-if="item.icon"
              :src="item.icon"
              :alt="item.name"
              class="item-image"
              :width="44"
              :height="44"
              :show-skeleton="false"
            />
            <div v-else class="thumb" :data-grade="item.grade">
              <span>{{ gradeInitial(item.grade) }}</span>
            </div>
            <div>
              <p class="item-name">{{ item.name }}</p>
              <p class="item-sub">{{ item.grade || '등급 없음' }}</p>
            </div>
          </div>
          <span class="align-right strong">{{ formatGold(item.currentMinPrice) }}</span>
          <span class="align-right">{{ formatGold(item.recentPrice) }}</span>
          <span class="align-right muted">{{ formatGold(item.yDayAvgPrice) }}</span>
          <span class="align-right muted">
            {{ item.bundleCount ? `${item.bundleCount}개` : '-' }}
            <span v-if="item.tradeRemainCount !== undefined && item.tradeRemainCount !== null">
              / {{ item.tradeRemainCount }}
            </span>
          </span>
          <span class="align-right badge">{{ selectedCategory ?? '전체' }}</span>
          <span class="align-right muted">{{ lastFetchedLabel }}</span>
        </div>
      </div>

      <div v-if="loadingItems && currentItems.length" class="inline-loading">새 페이지를 불러오는 중...</div>
    </section>

    <transition name="fade">
      <div v-if="detailOpen" class="modal-backdrop" @click="closeDetail">
        <div class="modal" ref="modalRef" @click.stop>
          <div class="modal-header">
            <div class="modal-maintitle">
              <h3 class="eyebrow">아이템 상세</h3>
              <button type="button" class="btn ghost" @click="closeDetail">닫기</button>
            </div>
          </div>

          <div v-if="detailLoading">
            <LoadingSpinner message="상세 정보를 불러오는 중..." />
          </div>
          <div v-else-if="detailError">
            <p class="error-text">{{ detailError }}</p>
          </div>
          <div v-else>
            <div class="detail-layout">
              <div class="detail-side">
                <div class="detail-thumb">
                  <LazyImage
                    v-if="selectedItem?.icon"
                    :src="selectedItem.icon"
                    :alt="selectedItem.name"
                    :width="75"
                    :height="75"
                    :show-skeleton="false"
                  />
                  <div v-else class="thumb large" :data-grade="selectedItem?.grade">
                    <span>{{ gradeInitial(selectedItem?.grade) }}</span>
                  </div>
                  <div class="modal-subtitle">
                    <h4>{{ selectedItem?.name || '선택된 아이템 없음' }}</h4>
                    <p class="muted">{{ selectedItem?.grade }}</p>
                  </div>
                </div>
                <div class="detail-grid">
                  <div class="detail-grid__item">
                    <span class="label">현재 최저가</span>
                    <strong>{{ formatGold(selectedItem?.currentMinPrice) }}</strong>
                  </div>
                  <div class="detail-grid__item">
                    <span class="label">최근 거래가</span>
                    <strong>{{ formatGold(selectedItem?.recentPrice) }}</strong>
                  </div>
                  <div class="detail-grid__item">
                    <span class="label">전일 평균</span>
                    <strong>{{ formatGold(selectedYDayAvgPrice) }}</strong>
                  </div>
                  <div class="detail-grid__item">
                    <span class="label">묶음 수량</span>
                    <strong>
                      {{ selectedItem?.bundleCount ?? '-' }}
                      <span
                        v-if="selectedItem?.tradeRemainCount !== undefined && selectedItem?.tradeRemainCount !== null"
                      >
                        / {{ selectedItem?.tradeRemainCount }}
                      </span>
                    </strong>
                  </div>
                </div>
                <div class="axis-dates">
                  <span class="axis-date-line">
                    {{ (sparkline.volume.firstDate || sparkline.price.firstDate) || '' }}
                  </span>
                  <span>
                    /
                  </span>
                  <span class="axis-date-line">
                    {{ (sparkline.volume.lastDate || sparkline.price.lastDate) || '' }}
                  </span>
                </div>
              </div>
              <div class="detail-chart">
                <div v-if="detailStats.length" class="detail-stats">
                  <div class="detail-stats__header">
                    <h5 class="eyebrow">거래 추이 (거래 건수)</h5>
                    <span class="muted">{{ detailStats.length }}일 기준</span>
                  </div>
                  <div class="sparkline-wrapper">
                    <svg
                      class="sparkline"
                      viewBox="0 0 480 210"
                      role="img"
                      aria-label="거래 추이 그래프 (거래량 + 평균가)"
                      ref="chartRef"
                      @pointermove="handleChartMove($event)"
                      @mousemove="handleChartMove($event)"
                      @pointerleave="clearTooltip()"
                      @mouseleave="clearTooltip()"
                    >
                      <line
                        class="axis-line"
                        :x1="sparkline.pad"
                        :y1="sparkline.height - sparkline.pad"
                        :x2="sparkline.width - sparkline.pad"
                        :y2="sparkline.height - sparkline.pad"
                      />
                      <line
                        class="axis-line"
                        :x1="sparkline.pad"
                        :y1="sparkline.pad"
                        :x2="sparkline.pad"
                        :y2="sparkline.height - sparkline.pad"
                      />
                      <line
                        class="axis-line"
                        :x1="sparkline.width - sparkline.pad"
                        :y1="sparkline.pad"
                        :x2="sparkline.width - sparkline.pad"
                        :y2="sparkline.height - sparkline.pad"
                      />
                      <g v-for="(tick, idx) in xAxisTicks" :key="`x-${idx}`">
                        <line
                          class="tick-line x-axis"
                          :x1="tick.x"
                          :y1="sparkline.height - sparkline.pad"
                          :x2="tick.x"
                          :y2="sparkline.height - sparkline.pad + 6"
                        />
                        <text
                          class="x-tick-label"
                          :x="tick.x"
                          :y="sparkline.height - sparkline.pad + 14"
                          text-anchor="middle"
                        >
                          <tspan :x="tick.x" dy="0">{{ tick.labelLines?.[0] || tick.label }}</tspan>
                          <tspan :x="tick.x" dy="10">{{ tick.labelLines?.[1] }}</tspan>
                        </text>
                      </g>
                      <g v-for="(tick, idx) in sparkline.volume.ticks" :key="`v-${idx}`">
                    <text
                      class="tick-label"
                      :x="sparkline.pad - 6"
                      :y="tick.y + 4"
                      text-anchor="end"
                    >
                      {{ formatCompactNumber(tick.value) }}
                    </text>
                        <line
                          class="tick-line"
                          :x1="sparkline.pad"
                          :y1="tick.y"
                          :x2="sparkline.width - sparkline.pad"
                          :y2="tick.y"
                        />
                      </g>
                      <g v-for="(tick, idx) in sparkline.price.ticks" :key="`pr-${idx}`">
                    <text
                      class="tick-label price-tick"
                      :x="sparkline.width - sparkline.pad + 6"
                      :y="tick.y + 4"
                      text-anchor="start"
                    >
                      {{ formatCompactNumber(tick.value) }}
                    </text>
                      </g>
                      <path v-if="sparkline.volume.path" class="volume-path" :d="sparkline.volume.path" />
                      <path v-if="sparkline.price.path" class="price-path" :d="sparkline.price.path" stroke="#16a34a" />
                      <g v-for="(dot, idx) in sparkline.volume.dots" :key="`vol-${idx}`">
                        <circle
                          class="volume-dot"
                          :cx="dot.x"
                          :cy="dot.y"
                          r="5"
                          @pointerenter="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event)"
                          @pointermove="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event)"
                          @mouseenter="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event)"
                          @mousemove="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event)"
                          @touchstart.passive="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event as any)"
                          @touchmove.passive="handleTooltip({ ...dot, tradeVolume: dot.value }, 'volume', $event as any)"
                          @mouseleave="clearTooltip()"
                        />
                      </g>
                      <g v-for="(dot, idx) in sparkline.price.dots" :key="`price-${idx}`">
                        <circle
                          class="price-dot"
                          :cx="dot.x"
                          :cy="dot.y"
                          r="5"
                          stroke="#16a34a"
                          fill="#fff"
                          @pointerenter="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event)"
                          @pointermove="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event)"
                          @mouseenter="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event)"
                          @mousemove="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event)"
                          @touchstart.passive="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event as any)"
                          @touchmove.passive="handleTooltip({ ...dot, avgPrice: dot.value }, 'price', $event as any)"
                          @mouseleave="clearTooltip()"
                        />
                      </g>
                    </svg>
                    <div
                      v-if="tooltip?.visible"
                      class="chart-tooltip"
                      :style="{ left: `${tooltip.x}px`, top: `${tooltip.y}px` }"
                    >
                      <p class="tooltip-date">{{ tooltip.date }}</p>
                      <div class="tooltip-grid">
                        <div class="tooltip-row">
                          <span class="tooltip-label">거래건수</span>
                          <strong class="tooltip-value">{{ formatNumber(tooltip.tradeCount) }}</strong>
                        </div>
                        <div class="tooltip-row">
                          <span class="tooltip-label">평균가</span>
                          <strong class="tooltip-value">{{ formatGold(tooltip.avgPrice) }}</strong>
                        </div>
                        <div class="tooltip-row">
                          <span class="tooltip-label">거래량</span>
                          <strong class="tooltip-value">{{ formatNumber(tooltip.tradeVolume) }}</strong>
                        </div>
                      </div>
                    </div>
                    <div class="axis-labels">
                      <span class="axis-label-left">거래량</span>
                      <span class="axis-label-right">평균가</span>
                    </div>
                  </div>
                </div>
                <div v-else class="muted">추가 통계가 없습니다.</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { lostarkApi } from '@/api/lostark'
import type { StoredMarketCategory, MarketOptionsResponse, MarketItemSummary, MarketItemDetail } from '@/api/types'
import LoadingSpinner from './common/LoadingSpinner.vue'
import LazyImage from './common/LazyImage.vue'

type TooltipState = {
  visible: boolean
  x: number
  y: number
  date?: string
  tradeCount?: number | null
  avgPrice?: number | null
  tradeVolume?: number | null
}

const toNumberOrUndefined = (value: unknown) => {
  if (value === null || value === undefined) return undefined
  const normalized =
    typeof value === 'string'
      ? value.replace(/,/g, '').trim()
      : value
  const num = Number(normalized)
  return Number.isNaN(num) ? undefined : num
}

const toSvgCoords = (svg: SVGSVGElement, clientX: number, clientY: number) => {
  if (typeof svg.createSVGPoint === 'function') {
    const point = svg.createSVGPoint()
    point.x = clientX
    point.y = clientY
    const ctm = svg.getScreenCTM()
    if (ctm) {
      const transformed = point.matrixTransform(ctm.inverse())
      return { x: transformed.x, y: transformed.y }
    }
  }
  const rect = svg.getBoundingClientRect()
  const viewBox = svg.viewBox?.baseVal
  const vbWidth = viewBox?.width || 480
  const vbHeight = viewBox?.height || 210
  const scaleX = (rect.width || vbWidth) / vbWidth
  const scaleY = (rect.height || vbHeight) / vbHeight
  return {
    x: (clientX - rect.left) / scaleX,
    y: (clientY - rect.top) / scaleY
  }
}

const toClientCoords = (svg: SVGSVGElement, svgX: number, svgY: number) => {
  if (typeof svg.createSVGPoint === 'function') {
    const point = svg.createSVGPoint()
    point.x = svgX
    point.y = svgY
    const ctm = svg.getScreenCTM()
    if (ctm) {
      const screen = point.matrixTransform(ctm)
      return { x: screen.x, y: screen.y }
    }
  }
  const rect = svg.getBoundingClientRect()
  const viewBox = svg.viewBox?.baseVal
  const vbWidth = viewBox?.width || 480
  const vbHeight = viewBox?.height || 210
  const scale = Math.min(rect.width / vbWidth, rect.height / vbHeight)
  const offsetX = (rect.width - vbWidth * scale) / 2
  const offsetY = (rect.height - vbHeight * scale) / 2
  return {
    x: rect.left + offsetX + svgX * scale,
    y: rect.top + offsetY + svgY * scale
  }
}

const toDateValue = (value?: string | null) => {
  if (!value) return null
  const ts = new Date(value).getTime()
  return Number.isNaN(ts) ? null : ts
}

const categories = ref<StoredMarketCategory[]>([])
const pageCache = ref<Record<number, MarketItemSummary[]>>({})
const selectedCategory = ref<number | null>(null)
const searchText = ref('')
const page = ref(1)
const totalPages = ref(1)
const totalItems = ref(0)
const pageSize = ref(20)
const loadingCategories = ref(false)
const loadingItems = ref(false)
const errorMessage = ref('')
const sort = ref('RECENT_PRICE')
const sortCondition = ref<'ASC' | 'DESC'>('ASC')
const characterClass = ref<string | null>(null)
const classes = ref<string[]>([])
const itemTier = ref<number | null>(null)
const itemGrade = ref<string | null>(null)
const prefetchRange = 3
const itemTiers = ref<number[]>([])
const itemGrades = ref<string[]>([])
const lastFetchedAt = ref<number | null>(null)
const lastCenterPage = ref(1)

const detailOpen = ref(false)
const detailLoading = ref(false)
const detailData = ref<MarketItemDetail | null>(null)
const selectedItem = ref<MarketItemSummary | null>(null)
const detailError = ref('')
const modalRef = ref<HTMLElement | null>(null)
const tooltip = ref<TooltipState | null>(null)
const lastHoverKey = ref<string | null>(null)
const chartRef = ref<SVGSVGElement | null>(null)
const detailStats = computed(() => {
  const stats = detailData.value?.stats || []
  const bundleCountForVolume =
    toNumberOrUndefined(detailData.value?.bundleCount ?? selectedItem.value?.bundleCount ?? 1) ?? 1
  const mapped = stats.map(stat => {
    const date = stat.Date || stat.date
    const tradeCount = toNumberOrUndefined(stat.TradeCount ?? stat.tradeCount ?? 0) ?? 0
    const avgPrice = toNumberOrUndefined(stat.AvgPrice ?? stat.avgPrice ?? 0) ?? 0
    const rawVolume =
      (stat as any).TradeVolume ??
      (stat as any).tradeVolume ??
      (stat as any).trade_volume ??
      (stat as any).Volume ??
      (stat as any).volume
    const rawVolumeNumber = rawVolume !== undefined && rawVolume !== null ? toNumberOrUndefined(rawVolume) : undefined
    const tradeVolumeCandidate = rawVolumeNumber !== undefined ? rawVolumeNumber : tradeCount * bundleCountForVolume
    const tradeVolume =
      typeof tradeVolumeCandidate === 'number' && !Number.isNaN(tradeVolumeCandidate) ? tradeVolumeCandidate : undefined
    return {
      date,
      tradeCount,
      avgPrice,
      tradeVolume
    }
  })
  const filtered = mapped.filter(
    s =>
      !Number.isNaN(s.tradeCount) ||
      !Number.isNaN(s.avgPrice) ||
      (typeof s.tradeVolume === 'number' && !Number.isNaN(s.tradeVolume))
  )
  return filtered.sort((a, b) => {
    const aTs = toDateValue(a.date)
    const bTs = toDateValue(b.date)
    if (aTs !== null && bTs !== null) return aTs - bTs
    if (aTs !== null) return -1
    if (bTs !== null) return 1
    return (a.date || '').localeCompare(b.date || '')
  })
})

const sparkline = computed(() => {
  const points = detailStats.value
  const width = 480
  const height = 210
  const pad = 36
  const build = (values: { date?: string; value: number }[], minClamp: number, roundTo?: number) => {
    if (!values.length) return { path: '', dots: [], ticks: [], firstDate: '', lastDate: '' }
    const rawMax = Math.max(...values.map(v => v.value))
    let max = Math.max(rawMax, minClamp)
    if (roundTo && roundTo > 0) {
      max = Math.ceil(max / roundTo) * roundTo
    }
    const min = 0
    const span = Math.max(max - min, 1)
    const step = values.length > 1 ? (width - pad * 2) / (values.length - 1) : 0
    const pathParts: string[] = []
    const dots: { x: number; y: number; value: number; date?: string }[] = []
    values.forEach((v, idx) => {
      const x = pad + idx * step
      const y = height - pad - ((v.value - min) / span) * (height - pad * 2)
      const cmd = idx === 0 ? 'M' : 'L'
      pathParts.push(`${cmd} ${x} ${y}`)
      dots.push({ x, y, value: v.value, date: v.date })
    })
    const ticks = []
    const divisions = 5 // 6 lines 고정 (x축 포함)
    for (let i = 0; i <= divisions; i++) {
      const value = Math.round(min + (span * i) / divisions)
      const y = height - pad - ((value - min) / span) * (height - pad * 2)
      ticks.push({ value, y })
    }
    return {
      path: pathParts.join(' '),
      dots,
      ticks,
      firstDate: values[0]?.date || '',
      lastDate: values[values.length - 1]?.date || ''
    }
  }

  const volumeValues = points
    .filter(p => p.tradeVolume !== undefined && !Number.isNaN(p.tradeVolume as number) && (p.tradeVolume ?? 0) >= 0)
    .map(p => ({ date: p.date, value: p.tradeVolume ?? 0 }))
  const priceValues = points
    .filter(p => !Number.isNaN(p.avgPrice) && p.avgPrice !== undefined && p.avgPrice !== null)
    .map(p => ({ date: p.date, value: p.avgPrice }))

  return {
    volume: build(volumeValues, 10, 10),
    price: build(priceValues, 10, 10),
    width,
    height,
    pad
  }
})

const xAxisTicks = computed(() => {
  const dots = sparkline.value.volume.dots.length ? sparkline.value.volume.dots : sparkline.value.price.dots
  if (!dots.length) return []
  return dots.map(dot => {
    const label = dot.date || ''
    const parts = label.split('-')
    if (parts.length >= 3) {
      return {
        x: dot.x,
        label,
        labelLines: [parts[0], `${parts[1]}.${parts[2]}`]
      }
    }
    return { x: dot.x, label, labelLines: [label, ''] }
  })
})

const selectedYDayAvgPrice = computed(() => {
  const normalized = selectedItem.value?.yDayAvgPrice
  if (normalized !== undefined && normalized !== null) return normalized
  // fallback: use previous day avg from stats if available
  const stats = detailStats.value
  if (stats.length > 1 && stats[1].avgPrice) return stats[1].avgPrice
  if (stats.length && stats[0].avgPrice) return stats[0].avgPrice
  return null
})

const leafCategories = computed(() => categories.value.filter(cat => !cat.hasSubs))
const categoryCount = computed(() => categories.value.length)

const currentItems = computed(() => pageCache.value[page.value] ?? pageCache.value[String(page.value)] ?? [])

const filteredItems = computed(() => {
  return currentItems.value
})

const lastFetchedLabel = computed(() => {
  if (!lastFetchedAt.value) return ''
  return formatDateTime(new Date(lastFetchedAt.value).toISOString())
})

const handleTooltip = (
  dot: { x: number; y: number; value: number; date?: string; avgPrice?: number; tradeVolume?: number },
  type: 'volume' | 'price',
  _evt?: PointerEvent
) => {
  const svg = chartRef.value
  if (!svg) return

  const dotClient = toClientCoords(svg, dot.x, dot.y)
  const modalRect = modalRef.value?.getBoundingClientRect()
  const margin = 12
  const tooltipWidth = 220
  const tooltipHeight = 120
  const halfW = tooltipWidth / 2
  const bounds = modalRect || { left: 0, right: window.innerWidth, top: 0, bottom: window.innerHeight }
  const clampedX = Math.max(bounds.left + margin + halfW, Math.min(bounds.right - margin - halfW, dotClient.x))
  const clampedYAnchor = Math.max(bounds.top + margin + tooltipHeight, Math.min(bounds.bottom - margin, dotClient.y))

  const stat = detailStats.value.find(s => s.date === dot.date)
  const tradeCount = stat?.tradeCount
  const avgPrice = stat?.avgPrice ?? (type === 'price' ? dot.value : undefined)
  const tradeVolume = stat?.tradeVolume ?? (type === 'volume' ? dot.value : undefined)
  const hoverKey = `${type}-${dot.date}-${tradeCount}-${avgPrice}-${tradeVolume}`
  if (lastHoverKey.value !== hoverKey) {
    lastHoverKey.value = hoverKey
    console.log('[AuctionMenu] chart hover', {
      chart: type,
      date: dot.date,
      value: dot.value,
      tradeCount,
      avgPrice,
      tradeVolume,
      coords: { x: clampedX, y: clampedYAnchor }
    })
  }
  tooltip.value = {
    visible: true,
    x: clampedX,
    y: clampedYAnchor,
    date: dot.date,
    tradeCount,
    avgPrice,
    tradeVolume
  }
}

const clearTooltip = () => {
  tooltip.value = null
  lastHoverKey.value = null
}

const handleChartMove = (evt: PointerEvent | MouseEvent) => {
  const ref = chartRef.value
  if (!ref) return
  const volumeDots = sparkline.value.volume.dots.map(d => ({ ...d, type: 'volume' as const }))
  const priceDots = sparkline.value.price.dots.map(d => ({ ...d, type: 'price' as const }))
  const dots = [...volumeDots, ...priceDots]
  if (!dots.length) return
  const svgPoint = toSvgCoords(ref, evt.clientX, evt.clientY)
  const cursorX = svgPoint.x
  const cursorY = svgPoint.y
  let closest = dots[0]
  let minDist = Number.MAX_VALUE
  dots.forEach(d => {
    const dx = d.x - cursorX
    const dy = d.y - cursorY
    const dist = Math.sqrt(dx * dx + dy * dy)
    if (dist < minDist) {
      minDist = dist
      closest = d
    }
  })
  const stepGuess = volumeDots.length > 1 ? Math.abs(volumeDots[1].x - volumeDots[0].x) : 0
  const threshold = stepGuess > 0 ? Math.max(8, Math.min(24, stepGuess / 2)) : 14
  if (minDist <= threshold) {
    handleTooltip(closest, closest.type, evt as PointerEvent)
  } else {
    clearTooltip()
  }
}

const normalizeItem = (item: any): MarketItemSummary => {
  const pickNumber = (keys: string[]) => {
    for (const key of keys) {
      const v = item?.[key]
      if (v !== undefined && v !== null) {
        const num = Number(v)
        return Number.isNaN(num) ? undefined : num
      }
    }
    return undefined
  }

  return {
    id: item?.id ?? item?.Id ?? 0,
    name: item?.name ?? item?.Name ?? '',
    grade: item?.grade ?? item?.Grade,
    icon: item?.icon ?? item?.Icon,
    bundleCount: pickNumber(['bundleCount', 'BundleCount']),
    tradeRemainCount: pickNumber(['tradeRemainCount', 'TradeRemainCount']),
    yDayAvgPrice: pickNumber(['yDayAvgPrice', 'YDayAvgPrice', 'ydayAvgPrice', 'ydayavgprice', 'y_day_avg_price']),
    recentPrice: pickNumber(['recentPrice', 'RecentPrice', 'recent_price']),
    currentMinPrice: pickNumber(['currentMinPrice', 'CurrentMinPrice', 'current_min_price'])
  }
}

const formatNumber = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return new Intl.NumberFormat('ko-KR').format(value)
}

const formatCompactNumber = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  const abs = Math.abs(value)
  if (abs >= 1_000_000_000) return `${(value / 1_000_000_000).toFixed(1).replace(/\\.0$/, '')}B`
  if (abs >= 1_000_000) return `${(value / 1_000_000).toFixed(1).replace(/\\.0$/, '')}M`
  if (abs >= 1_000) return `${(value / 1_000).toFixed(1).replace(/\\.0$/, '')}K`
  return formatNumber(value)
}

const formatGold = (value?: number | null) => {
  if (value === null || value === undefined) return '-'
  return `${formatNumber(value)}`
}

const formatDateTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

const gradeInitial = (grade?: string | null) => {
  if (!grade) return 'N'
  return grade.slice(0, 1).toUpperCase()
}

const handleSearch = () => {
  page.value = 1
  lastCenterPage.value = 1
  loadItems()
}

const clearSearch = () => {
  searchText.value = ''
  page.value = 1
  lastCenterPage.value = 1
  loadItems()
}

const changePage = (nextPage: number) => {
  if (nextPage < 1 || nextPage > totalPages.value) return
  page.value = nextPage
  if (pageCache.value[nextPage] && Math.abs(nextPage - lastCenterPage.value) <= prefetchRange) {
    return
  }
  loadItems()
}

const resetAndLoad = () => {
  page.value = 1
  lastCenterPage.value = 1
  loadItems()
}

const loadCategories = async () => {
  try {
    loadingCategories.value = true
    const data = await lostarkApi.getMarketCategories()
    categories.value = data
    const options: MarketOptionsResponse = await lostarkApi.getMarketOptions()
    classes.value = options.Classes || []
    itemTiers.value = options.ItemTiers || []
    itemGrades.value = options.ItemGrades || []
    // 카테고리가 비어있으면 옵션 응답으로 채움
    if (!categories.value.length && options.Categories?.length) {
      const flattened: StoredMarketCategory[] = []
      const walk = (list: any[], parentCode: number | null, depth = 0) => {
        list.forEach(cat => {
          flattened.push({
            id: cat.Code,
            code: cat.Code,
            codeName: cat.CodeName,
            parentCode: parentCode || undefined,
            hasSubs: Boolean(cat.Subs && cat.Subs.length),
            depth
          } as StoredMarketCategory)
          if (cat.Subs?.length) {
            walk(cat.Subs, cat.Code, depth + 1)
          }
        })
      }
      walk(options.Categories || [], null)
      categories.value = flattened
    }
    if (!selectedCategory.value && leafCategories.value.length) {
      selectedCategory.value = leafCategories.value[0].code ?? null
    }
  } catch (error) {
    console.error(error)
    errorMessage.value = '카테고리를 불러오지 못했습니다. 백엔드 동기화 상태를 확인해 주세요.'
  } finally {
    loadingCategories.value = false
  }
}

const loadItems = async () => {
  if (!selectedCategory.value) {
    errorMessage.value = '카테고리를 먼저 선택하세요.'
    return
  }
  try {
    loadingItems.value = true
    errorMessage.value = ''
    const data = await lostarkApi.searchMarketItems({
      categoryCode: selectedCategory.value,
      characterClass: characterClass.value || undefined,
      itemTier: itemTier.value || undefined,
      itemGrade: itemGrade.value || undefined,
      itemName: searchText.value || undefined,
      sort: sort.value,
      sortCondition: sortCondition.value,
      page: page.value,
      size: pageSize.value,
      prefetchRange
    })

    const normalized: Record<number, MarketItemSummary[]> = {}
    Object.entries(data.pages || {}).forEach(([key, value]) => {
      const numKey = Number(key)
      const items = (value || []).map(normalizeItem)
      normalized[Number.isNaN(numKey) ? (key as any) : numKey] = items
    })
    pageCache.value = normalized
    totalPages.value = data.totalPages ?? 1
    totalItems.value = data.totalCount ?? 0
    page.value = data.page ?? page.value
    lastFetchedAt.value = data.fetchedAt ?? Date.now()
    lastCenterPage.value = data.page ?? page.value
  } catch (error) {
    console.error(error)
    errorMessage.value = '아이템을 불러오지 못했습니다.'
  } finally {
    loadingItems.value = false
  }
}

const openDetail = async (item: MarketItemSummary) => {
  selectedItem.value = item
  detailOpen.value = true
  detailLoading.value = true
  detailData.value = null
  detailError.value = ''
  clearTooltip()
  try {
    const refreshed = await lostarkApi.refreshMarketItem(item.id, {
      categoryCode: selectedCategory.value as number,
      itemName: item.name,
      characterClass: characterClass.value || undefined,
      itemTier: itemTier.value || undefined,
      itemGrade: itemGrade.value || undefined,
      sort: sort.value,
      sortCondition: sortCondition.value,
      pageSize: pageSize.value
    })
    const normalizedRefreshed = normalizeItem(refreshed)
    selectedItem.value = normalizedRefreshed
    // 캐시 업데이트
    const cache = { ...pageCache.value }
    const currentPageItems = cache[page.value] || []
    cache[page.value] = currentPageItems.map(i => (i.id === normalizedRefreshed.id ? normalizedRefreshed : i))
    pageCache.value = cache

    detailData.value = await lostarkApi.getMarketItemDetail(item.id)
  } catch (error: any) {
    console.error(error)
    detailError.value = error?.response?.data?.message || '아이템 상세를 불러오지 못했습니다.'
  } finally {
    detailLoading.value = false
  }
}

const closeDetail = () => {
  detailOpen.value = false
  detailData.value = null
  selectedItem.value = null
  clearTooltip()
}

watch([selectedCategory, sort, sortCondition, characterClass, itemTier, itemGrade, pageSize], () => resetAndLoad())

onMounted(async () => {
  await loadCategories()
  await loadItems()
})
</script>

<style scoped>
.auction-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.auction-hero {
  padding: 22px;
  border-radius: 18px;
  background: linear-gradient(120deg, #0f172a, #111827 55%, #0b2b4d);
  color: #f8fafc;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.18);
}

.auction-hero h2 {
  margin: 2px 0 6px;
  font-size: 1.6rem;
}

.hero-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.78);
  max-width: 720px;
}

.hero-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.meta-chip {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 10px 12px;
  display: grid;
  gap: 4px;
  min-width: 120px;
}

.meta-chip strong {
  font-size: 1.1rem;
}

.meta-chip.accent {
  background: linear-gradient(120deg, rgba(125, 211, 252, 0.15), rgba(255, 255, 255, 0.08));
  border-color: rgba(125, 211, 252, 0.4);
}

.meta-chip.muted {
  color: var(--text-secondary);
}

.meta-label {
  font-size: 0.82rem;
  color: rgba(255, 255, 255, 0.8);
}

.control-panel {
  display: grid;
  gap: 12px;
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
}

.control-row {
  display: grid;
  grid-template-columns: 0.3fr repeat(3, minmax(0, 0.2fr)) 0.4fr 0.2fr 0.5fr auto;
  gap: 12px;
  align-items: end;
}

.field {
  display: grid;
  gap: 6px;
}

.field-label {
  font-size: 0.86rem;
  color: var(--text-secondary, #4b5563);
  text-align: center;
}

.input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f8fafc);
  color: var(--text-primary, #111827);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  text-align: center;
}

.input:focus {
  border-color: var(--primary-color, #2563eb);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
  outline: none;
}

.sort-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.sort-order {
  padding-left: 10px;
}

.actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  align-items: center;
}

.btn {
  padding: 10px 14px;
  border-radius: 10px;
  border: 1px solid var(--primary-color, #2563eb);
  background: linear-gradient(120deg, #2563eb, #1d4ed8);
  color: #ffffff;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 20px rgba(37, 99, 235, 0.2);
}

.btn.ghost {
  border-color: var(--border-color, #e5e7eb);
  background: var(--card-bg, #ffffff);
  color: var(--text-primary, #111827);
}

.refresh-btn .btn-icon {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid var(--border-color, #d1d5db);
  background: linear-gradient(135deg, #e5e7eb, #f8fafc);
  display: inline-block;
}

.refresh-btn .btn-label {
  line-height: 1;
}

.inline-hint {
  margin: 0;
  color: var(--text-secondary, #4b5563);
  font-size: 0.95rem;
}

.items-panel {
  display: grid;
  gap: 12px;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
}

.items-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pager {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pager-btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f3f4f6);
  cursor: pointer;
}

.pager-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pager-page {
  font-weight: var(--font-semibold, 600);
}

.table {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 1.8fr repeat(6, 1fr);
  gap: 8px;
  align-items: center;
  padding: 10px 12px;
}

.table-head {
  background: var(--bg-secondary, #f3f4f6);
  color: var(--text-secondary, #4b5563);
  font-size: 0.9rem;
}

.table-row:nth-child(odd) {
  background: var(--card-bg, #ffffff);
}

.table-row:nth-child(even) {
  background: var(--bg-secondary, #f8fafc);
}

.item-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.item-image {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--bg-secondary, #f8fafc);
  object-fit: cover;
}

.thumb {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #0b1225;
  background: linear-gradient(135deg, #e5e7eb, #f9fafb);
  border: 1px solid var(--border-color, #e5e7eb);
}

.thumb[data-grade*="영웅"] {
  background: linear-gradient(135deg, #c084fc, #a855f7);
  color: #fff;
}

.thumb[data-grade*="전설"] {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: #2b1a00;
}

.thumb[data-grade*="유물"] {
  background: linear-gradient(135deg, #fb7185, #f97316);
  color: #fff;
}

.thumb[data-grade*="고대"] {
  background: linear-gradient(135deg, #22d3ee, #0ea5e9);
  color: #062b3b;
}

.item-name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.item-sub {
  margin: 2px 0 0;
  color: var(--text-secondary, #4b5563);
  font-size: 0.88rem;
}

.align-right {
  text-align: right;
}

.strong {
  font-weight: 700;
}

.muted {
  color: var(--text-secondary, #6b7280);
  font-size: var(--font-sm);
}

.badge {
  display: inline-flex;
  justify-content: flex-end;
  padding: 4px 8px;
  /* background: var(--bg-secondary, #f3f4f6); */
  border-radius: 10px;
  color: var(--text-secondary, #4b5563);
  font-size: 0.9rem;
}

.empty-state,
.error-text,
.inline-loading {
  margin: 6px 0 0;
  color: var(--text-secondary, #4b5563);
}

.error-text {
  color: #b91c1c;
}

.inline-loading {
  font-size: 0.95rem;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: grid;
  place-items: center;
  z-index: 50;
}

.modal {
  background: var(--card-bg, #ffffff);
  border-radius: 16px;
  padding: 18px;
  width: min(720px, 92vw);
  max-height: 90vh;
  overflow: auto;
  border: 1px solid var(--border-color, #e5e7eb);
  box-shadow: var(--shadow-lg);
}

.modal-header {
  display: flex;
  justify-content: space-between;
}

.modal-maintitle{
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
  justify-content: space-between;
}

.modal-subtitle{
  display: flex;
  gap: 10px;
  align-items: center;
}

.detail-prices {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
  margin-bottom: 12px;
}

.detail-layout {
  display: grid;
  grid-template-columns: minmax(220px, 0.4fr) 1fr;
  gap: 16px;
  align-items: flex-start;
}

.detail-side {
  display: grid;
  gap: 10px;
}

.detail-chart {
  width: 100%;
  margin-top: auto;
}

.detail-thumb {
  width: 100%;
  height: 120px;
  display: grid;
  place-items: center;
  /* background: var(--bg-secondary, #f8fafc);
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb); */
}

.thumb.large {
  width: 96px;
  height: 96px;
  font-size: 1.6rem;
}

.detail-meta {
  display: grid;
  gap: 8px;
}

.detail-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.detail-grade {
  margin: 0;
  color: var(--text-secondary, #4b5563);
}

.detail-grid {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  background: var(--bg-secondary, #f8fafc);
  padding: 8px 12px;
}

.detail-grid__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 0;
}

.detail-grid__item + .detail-grid__item {
  border-top: 1px solid var(--border-color, #e5e7eb);
}

.detail-grid__item .label {
  display: inline-block;
  color: var(--text-secondary, #4b5563);
  font-size: 0.9rem;
}

.detail-stats ul {
  list-style: none;
  padding: 0;
  margin: 8px 0 0;
  display: grid;
  gap: 6px;
}

.detail-stats li {
  display: flex;
  justify-content: space-between;
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 10px;
  padding: 8px 10px;
  background: var(--card-bg, #ffffff);
}

.detail-stats__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sparkline {
  width: 100%;
  height: 240px;
  background: var(--bg-secondary, #f8fafc);
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 12px;
  padding: 4px;
  box-sizing: border-box;
  pointer-events: auto;
}

.sparkline-wrapper {
  position: relative;
  overflow: visible;
}

.chart-tooltip {
  position: fixed;
  transform: translate(-50%, -100%);
  background: #111827;
  color: #f9fafb;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  pointer-events: none;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.18);
  z-index: 2000;
  transition: opacity 0.1s ease, transform 0.1s ease;
}

.tooltip-date {
  margin: 0 0 6px;
  font-size: 0.82rem;
  color: rgba(248, 250, 252, 0.78);
}

.tooltip-grid {
  display: grid;
  gap: 4px;
}

.tooltip-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.tooltip-label {
  color: rgba(248, 250, 252, 0.72);
  font-size: 0.82rem;
}

.tooltip-value {
  color: #f8fafc;
  font-weight: 700;
}

.sparkline text {
  user-select: none;
}

.price-path {
  fill: none;
  stroke: #16a34a;
  stroke-width: 2;
  pointer-events: none;
}

.price-dot {
  fill: #fff;
  stroke: #16a34a;
  stroke-width: 2;
  pointer-events: auto;
}

.axis-line {
  stroke: var(--border-color, #d1d5db);
  stroke-width: 1;
  pointer-events: none;
}

.tick-line {
  stroke: rgba(0, 0, 0, 0.04);
  stroke-width: 1;
  pointer-events: none;
}
.tick-line.price {
  stroke: rgba(22, 163, 74, 0.12);
}
.tick-line.x-axis {
  stroke: rgba(0, 0, 0, 0.12);
}

.tick-label {
  fill: var(--text-secondary, #4b5563);
  font-size: 10px;
  pointer-events: none;
}

.x-tick-label {
  fill: var(--text-secondary, #4b5563);
  font-size: 9px;
  pointer-events: none;
}

.volume-path {
  fill: none;
  stroke: var(--primary-color, #2563eb);
  stroke-width: 2;
  pointer-events: none;
}

.volume-dot {
  fill: #fff;
  stroke: var(--primary-color, #2563eb);
  stroke-width: 2;
  pointer-events: auto;
}

.price-tick {
  fill: #16a34a;
}

.axis-label-right {
  color: #16a34a;
  font-weight: 600;
}

.axis-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  color: var(--text-secondary, #4b5563);
  font-size: var(--font-sm);
  height:22px;
}

.axis-label-left {
  color: var(--primary-color, #2563eb);
  font-weight: 600;
}

.axis-dates {
  display: flex;
  font-size: var(--font-xs);
  flex-direction: row;
  align-items: center;
  justify-content: center;
  /* justify-content: space-between; */
  gap:5px;
  height:22px;
}

.axis-date-line {
  line-height: 1.2;
}

.table-row {
  cursor: pointer;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 1024px) {
  .control-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .table-head,
  .table-row {
    grid-template-columns: 1.6fr repeat(3, 1fr);
  }

  .table-head span:nth-child(n+5),
  .table-row span:nth-child(n+5) {
    display: none;
  }
}

@media (max-width: 640px) {
  .auction-hero {
    padding: 16px;
  }

  .control-row {
    grid-template-columns: 1fr;
  }

  .items-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .detail-layout{
    display: block;
  }
}
</style>
