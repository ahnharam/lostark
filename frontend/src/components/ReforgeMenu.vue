<template>
  <div class="reforge-page">
    <header class="reforge-header" :class="{ 'optimization-header': isCalculatorTab }">
      <div>
        <p class="eyebrow">{{ headerEyebrow }}</p>
        <h2>{{ headerTitle }}</h2>
        <p class="lead">{{ headerLead }}</p>
      </div>
      <div v-if="isReforgeTab" class="header-actions">
        <button class="pill pill-warning" type="button">주의사항</button>
      </div>
    </header>

    <template v-if="isReforgeTab">
      <div class="reforge-layout">
        <section class="panel material-panel">
          <div class="panel-header">
            <div>
              <p class="eyebrow">재료 정보</p>
              <h3>{{ materialTabs.find(tab => tab.key === activeMaterialTab)?.label }}</h3>
              <p class="muted">제련에 필요한 재료 수량을 빠르게 정리했습니다.</p>
            </div>
            <div class="segmented">
              <button
                v-for="tab in materialTabs"
                :key="tab.key"
                type="button"
                class="segmented__btn"
                :class="{ active: activeMaterialTab === tab.key }"
                @click="activeMaterialTab = tab.key"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div v-if="!activeMaterials.length" class="empty-hint">데이터를 입력하면 재료 목록이 표시됩니다.</div>
          <ul v-else class="material-list">
            <li v-for="item in activeMaterials" :key="item.name" class="material-item">
              <div class="material-icon" :data-rarity="item.rarity">
                <span>{{ item.badge || item.rarity.slice(0, 1).toUpperCase() }}</span>
              </div>
              <div class="material-meta">
                <p class="material-name">{{ item.name }}</p>
                <p class="material-subtitle">{{ item.subtitle }}</p>
              </div>
              <div class="material-count">{{ formatNumber(item.count) }}</div>
            </li>
          </ul>
        </section>

        <section class="panel equipment-panel">
          <div class="panel-header">
            <div>
              <p class="eyebrow">장비 정보</p>
              <h3>{{ activeEquipment.title }}</h3>
              <p class="muted">장비 단계와 연구/버프 적용 여부를 선택하세요.</p>
            </div>
            <span class="chip">장인의 기운 {{ activeEquipment.artisanEnergy }}%</span>
          </div>

          <dl class="equipment-grid">
            <div class="equipment-row">
              <dt>장비 종류</dt>
              <dd>{{ activeEquipment.type }}</dd>
            </div>
            <div class="equipment-row">
              <dt>장비 등급</dt>
              <dd>{{ activeEquipment.grade }}</dd>
            </div>
            <div class="equipment-row">
              <dt>목표 단계</dt>
              <dd>{{ activeEquipment.targetStep }}</dd>
            </div>
            <div class="equipment-row">
              <dt>기본 확률</dt>
              <dd>{{ activeEquipment.baseChance.toFixed(2) }}%</dd>
            </div>
            <div class="equipment-row">
              <dt>성장 지원 & 영지 연구</dt>
              <dd>{{ activeEquipment.researchBonus.toFixed(2) }}%</dd>
            </div>
            <div class="equipment-row">
              <dt>추가 확률</dt>
              <dd>{{ activeEquipment.supportBonus.toFixed(2) }}%</dd>
            </div>
            <div class="equipment-row total">
              <dt>합계 확률</dt>
              <dd>{{ totalChance }}%</dd>
            </div>
          </dl>

          <div class="checkboxes">
            <label class="checkbox">
              <input v-model="applyResearch" type="checkbox" />
              영지 연구 적용
            </label>
            <label class="checkbox">
              <input v-model="applySupport" type="checkbox" />
              슈모의 성장지원 적용
            </label>
          </div>
        </section>

        <section class="panel result-panel">
          <div class="panel-header">
            <div>
              <p class="eyebrow">재련 시뮬레이션</p>
              <h3>{{ scenarioTabs.find(tab => tab.key === activeScenarioTab)?.label }}</h3>
              <p class="muted">노숲/풀숲 선택과 추가 재료 사용 여부를 분리했습니다.</p>
            </div>
            <div class="segmented">
              <button
                v-for="tab in scenarioTabs"
                :key="tab.key"
                type="button"
                class="segmented__btn"
                :class="{ active: activeScenarioTab === tab.key }"
                @click="activeScenarioTab = tab.key"
              >
                {{ tab.label }}
              </button>
            </div>
          </div>

          <div class="cost-summary">
            <div class="cost-card">
              <p class="eyebrow">평균</p>
              <p class="cost-value">{{ formatGold(activeScenario.averageCost) }}</p>
              <p class="muted">평균 비용</p>
            </div>
            <div class="cost-card">
              <p class="eyebrow">장기백</p>
              <p class="cost-value">{{ formatGold(activeScenario.pityCost) }}</p>
              <p class="muted">장기백 비용</p>
            </div>
            <div class="cost-card highlight">
              <p class="eyebrow">합계 확률</p>
              <p class="cost-value">{{ totalChance }}%</p>
              <p class="muted">현재 설정 기준</p>
            </div>
          </div>

          <div class="results-table">
            <div class="results-head">
              <span>트라이</span>
              <span>노숲 확률</span>
              <span>장인의 기운</span>
              <span>추가재료</span>
              <span>트라이 확률</span>
              <span>트라이 비용</span>
            </div>
            <div v-if="!activeScenario.rows.length" class="results-empty">결과 데이터가 없습니다.</div>
            <div v-else v-for="row in activeScenario.rows" :key="row.tryLabel" class="results-row">
              <span>{{ row.tryLabel }}</span>
              <span>{{ row.forestChance }}</span>
              <span>{{ row.artisanEnergy }}</span>
              <span>{{ row.extra }}</span>
              <span>{{ row.tryChance }}</span>
              <span>{{ row.cost }}</span>
            </div>
          </div>
        </section>
      </div>
    </template>

    <template v-else>
      <div class="optimization-layout">
        <BluntThornCalculator v-if="activeSubMenuTab === 'blunt-thorn'" />
        <SupersonicCalculator v-else-if="activeSubMenuTab === 'supersonic'" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import BluntThornCalculator from './reforge/BluntThornCalculator.vue'
import SupersonicCalculator from './reforge/SupersonicCalculator.vue'

type ReforgeTab = 'normal' | 'advanced'
type OptimizationTab = 'blunt-thorn' | 'supersonic'
type SubMenuTab = ReforgeTab | OptimizationTab
type MaterialTab = 'price' | 'bound'
type ScenarioTab = 'optimal' | 'partial' | 'full'
type Rarity = 'rare' | 'epic' | 'legendary' | 'relic'

interface MaterialItem {
  name: string
  subtitle: string
  count: number
  rarity: Rarity
  badge?: string
}

interface EquipmentPreset {
  title: string
  type: string
  grade: string
  targetStep: string
  baseChance: number
  researchBonus: number
  supportBonus: number
  artisanEnergy: number
  applyResearch: boolean
  applySupport: boolean
}

interface ScenarioRow {
  tryLabel: string
  forestChance: string
  artisanEnergy: string
  extra: string
  tryChance: string
  cost: string
}

interface ScenarioInfo {
  averageCost: number
  pityCost: number
  rows: ScenarioRow[]
}

const subMenuTabs: Array<{ key: SubMenuTab; label: string; kind: 'reforge' | 'optimization' }> = [
  { key: 'normal', label: '제련', kind: 'reforge' },
  { key: 'advanced', label: '상급 제련', kind: 'reforge' },
  { key: 'blunt-thorn', label: '뭉가 계산기', kind: 'optimization' },
  { key: 'supersonic', label: '음돌 계산기', kind: 'optimization' }
]

const materialTabs: Array<{ key: MaterialTab; label: string }> = [
  { key: 'price', label: '가격 정보' },
  { key: 'bound', label: '귀속 재료 정보' }
]

const scenarioTabs: Array<{ key: ScenarioTab; label: string }> = [
  { key: 'optimal', label: '최적 제련' },
  { key: 'partial', label: '노숲 제련' },
  { key: 'full', label: '풀숲 제련' }
]

const materialPresets: Record<ReforgeTab, Record<MaterialTab, MaterialItem[]>> = {
  normal: {
    price: [],
    bound: []
  },
  advanced: {
    price: [],
    bound: []
  }
}

const EMPTY_EQUIPMENT: EquipmentPreset = {
  title: '장비 정보를 입력하세요',
  type: '-',
  grade: '-',
  targetStep: '-',
  baseChance: 0,
  researchBonus: 0,
  supportBonus: 0,
  artisanEnergy: 0,
  applyResearch: false,
  applySupport: false
}

const equipmentPresets: Record<ReforgeTab, EquipmentPreset> = {
  normal: { ...EMPTY_EQUIPMENT },
  advanced: { ...EMPTY_EQUIPMENT }
}

const scenarioPresets: Record<ReforgeTab, Record<ScenarioTab, ScenarioInfo>> = {
  normal: {
    optimal: { averageCost: 0, pityCost: 0, rows: [] },
    partial: { averageCost: 0, pityCost: 0, rows: [] },
    full: { averageCost: 0, pityCost: 0, rows: [] }
  },
  advanced: {
    optimal: { averageCost: 0, pityCost: 0, rows: [] },
    partial: { averageCost: 0, pityCost: 0, rows: [] },
    full: { averageCost: 0, pityCost: 0, rows: [] }
  }
}

const props = defineProps<{
  activeSubMenuTab?: SubMenuTab
}>()

const activeReforgeTab = ref<ReforgeTab>('normal')
const activeSubMenuTab = ref<SubMenuTab>(props.activeSubMenuTab ?? 'normal')
const activeMaterialTab = ref<MaterialTab>('price')
const activeScenarioTab = ref<ScenarioTab>('optimal')
const applyResearch = ref(equipmentPresets[activeReforgeTab.value].applyResearch)
const applySupport = ref(equipmentPresets[activeReforgeTab.value].applySupport)

watch(
  () => props.activeSubMenuTab,
  value => {
    if (!value) return
    activeSubMenuTab.value = value
  }
)

const activeMaterials = computed(() => materialPresets[activeReforgeTab.value][activeMaterialTab.value])
const activeEquipment = computed(() => equipmentPresets[activeReforgeTab.value])
const activeScenario = computed(() => scenarioPresets[activeReforgeTab.value][activeScenarioTab.value])

const totalChance = computed(() => {
  let chance = activeEquipment.value.baseChance
  if (applyResearch.value) {
    chance += activeEquipment.value.researchBonus
  }
  if (applySupport.value) {
    chance += activeEquipment.value.supportBonus
  }
  return Number(Math.min(100, chance).toFixed(2))
})

const isReforgeTab = computed(() => activeSubMenuTab.value === 'normal' || activeSubMenuTab.value === 'advanced')
const isCalculatorTab = computed(
  () => activeSubMenuTab.value === 'blunt-thorn' || activeSubMenuTab.value === 'supersonic'
)

const activeSubMenuLabel = computed(() => {
  return subMenuTabs.find(tab => tab.key === activeSubMenuTab.value)?.label ?? '제련'
})

const headerEyebrow = computed(() => (isReforgeTab.value ? '제련 메뉴' : '세팅 최적화'))
const headerTitle = computed(() => (isReforgeTab.value ? `${activeSubMenuLabel.value} 최적화` : activeSubMenuLabel.value))
const headerLead = computed(() => {
  if (isReforgeTab.value) {
    return '이미지에 맞춰 제련/상급 제련 흐름을 3단 구성으로 배치했어요. 재료, 장비 정보, 결과를 한눈에 확인해 주세요.'
  }
  return '아크패시브 계산기를 통해 치명/신속 세팅을 빠르게 확인하세요.'
})

watch(activeSubMenuTab, nextTab => {
  if (nextTab === 'normal' || nextTab === 'advanced') {
    activeReforgeTab.value = nextTab
  }
})

watch(activeReforgeTab, () => {
  const preset = equipmentPresets[activeReforgeTab.value]
  applyResearch.value = preset.applyResearch
  applySupport.value = preset.applySupport
  activeMaterialTab.value = 'price'
  activeScenarioTab.value = 'optimal'
})

const formatNumber = (value: number) => value.toLocaleString('ko-KR')
const formatGold = (value: number) => `${formatNumber(value)} 골드`
</script>

<style scoped>
.reforge-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
  color: var(--text-primary);
}

.reforge-top {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.reforge-menu-bar {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.reforge-menu-bar__title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.topbar-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  font-size: 1rem;
}

.reforge-menu-bar__tabs {
  display: inline-flex;
  gap: 8px;
  justify-content: center;
}

.reforge-menu-btn {
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-secondary);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.reforge-menu-btn.active {
  background: var(--primary-soft-bg);
  color: var(--primary-color);
  border-color: rgba(99, 102, 241, 0.35);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.08);
}

.reforge-menu-btn:hover {
  transform: translateY(-1px);
}

.reforge-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
}

.optimization-header {
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 12px 30px rgba(99, 102, 241, 0.08);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.eyebrow {
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
  margin-bottom: 4px;
}

.lead {
  color: var(--text-secondary);
  margin-top: 6px;
  font-size: 14px;
}

.pill {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--warning-soft-bg);
  color: var(--text-primary);
  font-weight: 600;
}

.pill-warning {
  background: rgba(255, 210, 0, 0.16);
  border-color: rgba(255, 210, 0, 0.45);
}

.tab-switch {
  display: inline-flex;
  padding: 4px;
  border-radius: 999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  gap: 4px;
}

.tab-switch__btn {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid transparent;
  background: transparent;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
}

.tab-switch__btn.active {
  background: var(--primary-soft-bg);
  color: var(--primary-color);
  border-color: rgba(102, 126, 234, 0.2);
}

.reforge-layout {
  display: grid;
  grid-template-columns: minmax(260px, 320px) minmax(320px, 380px) 1fr;
  gap: 16px;
}

.panel {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.muted {
  color: var(--text-secondary);
  font-size: 13px;
}

.segmented {
  display: inline-flex;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  overflow: hidden;
  background: var(--bg-secondary);
}

.segmented__btn {
  padding: 8px 12px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
}

.segmented__btn.active {
  background: var(--primary-soft-bg);
  color: var(--primary-color);
  font-weight: 700;
}

.material-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 4px;
}

.material-item {
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-secondary);
}

.material-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: var(--text-inverse);
  font-weight: 700;
  letter-spacing: 0.02em;
}

.material-icon[data-rarity='rare'] {
  background: linear-gradient(135deg, #60a5fa, #3b82f6);
}

.material-icon[data-rarity='epic'] {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6);
}

.material-icon[data-rarity='legendary'] {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}

.material-icon[data-rarity='relic'] {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.material-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.material-name {
  font-weight: 700;
  color: var(--text-primary);
}

.material-subtitle {
  color: var(--text-secondary);
  font-size: 13px;
}

.material-count {
  font-weight: 800;
  font-size: 1rem;
  color: var(--primary-color);
}

.equipment-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
}

.equipment-row {
  display: grid;
  grid-template-columns: 150px 1fr;
  padding: 10px 12px;
  border: 1px dashed var(--border-color);
  border-radius: 12px;
  background: var(--bg-secondary);
  align-items: center;
}

.equipment-row.total {
  border-style: solid;
  border-color: var(--primary-color);
  background: var(--primary-soft-bg);
}

.equipment-row dt {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}

.equipment-row dd {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
}

.checkboxes {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-secondary);
}

.chip {
  align-self: flex-start;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  font-weight: 700;
  color: var(--text-secondary);
}

.cost-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.cost-card {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.cost-card.highlight {
  border-color: var(--primary-color);
  background: var(--primary-soft-bg);
}

.cost-value {
  font-weight: 800;
  font-size: 1.1rem;
  margin: 4px 0;
}

.results-table {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
}

.results-head,
.results-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
  padding: 10px 12px;
}

.results-head {
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 700;
}

.results-row:nth-child(odd) {
  background: rgba(0, 0, 0, 0.02);
}

.results-row span:last-child {
  font-weight: 700;
  color: var(--primary-color);
}

.results-empty,
.empty-hint {
  padding: 12px;
  color: var(--text-secondary);
  text-align: center;
}

.optimization-layout {
  display: flex;
  width: 100%;
}

@media (max-width: 1200px) {
  .reforge-layout {
    grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  }
}

@media (max-width: 768px) {
  .reforge-header {
    flex-direction: column;
  }

  .panel-header {
    flex-direction: column;
  }
}
</style>
