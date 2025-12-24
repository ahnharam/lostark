<template>
  <div class="supersonic-calculator">
    <div class="calc-grid">
      <section class="panel-card input-card">
        <div class="card-head">
          <div class="card-head-title">
            <p class="eyebrow">자버프</p>
            <div class="pill-note">신속 1 당 {{ swiftPerStat }}%</div>
          </div>

            <div class="level-toggle">
              <button
                v-for="level in supersonicLevels"
                :key="level.key"
                type="button"
                class="level-btn"
                :class="{ active: selectedSupersonicLevel === level.key && selectedSupersonicTarget === 'max' }"
                @click="selectSupersonicLevel(level.key)"
              >
                {{ level.label }} ({{ level.maxBonus }}%)
              </button>
              <button
                type="button"
                class="level-btn"
                :class="{ active: selectedSupersonicTarget === 'iptar21' }"
                @click="selectSupersonicIptar21"
              >
                입타 (21%)
              </button>
            </div>
        </div>


        <p class="group-title">직업 각인</p>
        <div class="form-grid">
          <label class="input-field">
            <span>공격속도 버프 (%)</span>
            <input v-model.number="engravingAtk" type="number" min="0" step="0.1" />
          </label>
          <label class="input-field">
            <span>이동속도 버프 (%)</span>
            <input v-model.number="engravingMove" type="number" min="0" step="0.1" />
          </label>
        </div>

        <p class="group-title">스킬 트라이포드</p>
        <div class="form-grid">
          <label class="input-field">
            <span>공격속도 버프 (%)</span>
            <input v-model.number="tripodAtk" type="number" min="0" step="0.1" />
          </label>
          <label class="input-field">
            <span>이동속도 버프 (%)</span>
            <input v-model.number="tripodMove" type="number" min="0" step="0.1" />
          </label>
        </div>

        <div class="inline-row">
          <div>
            <p class="group-title">팔찌</p>
            <label class="input-field">
              <span>공이속 버프 (%)</span>
              <input v-model.number="braceletAtk" type="number" min="0" step="0.1" />
            </label>
          </div>
          <div>
          </div>
        </div>

        <div class="subsection-head">
          <p class="group-title">아크 그리드 (혼돈 코어)</p>
          <div class="pill-note">신속 기여 {{ formatPercent(swiftContribution) }}%</div>
        </div>
        <div class="form-grid form-grid-compact">
          <label class="input-field">
            <span>혼돈의 해: 재빠른 공격</span>
            <CustomSelect v-model="seaFastAttack" :options="seaOptions" />
          </label>
          <label class="input-field">
            <span>혼돈의 별: 속도</span>
            <CustomSelect v-model="starSpeedKey" :options="starSpeedOptions" />
          </label>
        </div>
      </section>
      <section></section>
      <section class="panel-card buff-card">
        <div class="card-head">
          <div class="card-head-title">
            <p class="eyebrow">버프 효과</p>
            <div class="pill-note">공속 {{ formatSigned(buffAttack) }}% / 이속 {{ formatSigned(buffMove) }}%</div>
          </div>
        </div>

        <div class="buff-grid">
          <button
            v-for="buff in buffOptions"
            :key="buff.key"
            type="button"
            class="buff-chip"
            :class="{
              active: isBuffActive(buff.key),
              warning: buff.variant === 'warning'
            }"
            @click="toggleBuff(buff.key)"
          >
            <span>{{ buff.label }}</span>
            <span class="chip-value">{{ formatSigned(buff.value) }}%</span>
          </button>
        </div>
      </section>

      <section class="panel-card result-card">
        <div class="result-grid">
          <div class="result-box primary readable-text-block">
            <p class="eyebrow">현재 음돌 피해증가</p>
            <p class="result-value">{{ formatPercent(currentSupersonicBonus) }}%</p>
            <p class="muted">최대 {{ selectedSupersonicConfig.maxBonus }}% 증가</p>
          </div>
          <div class="result-box readable-text-block">
            <p class="eyebrow">필요 신속 스탯</p>
            <p class="result-value">{{ requiredSwiftForTarget === null ? '-' : formatNumber(requiredSwiftForTarget) }}</p>
            <p class="muted">
              추가 필요 {{ remainingSwiftForTarget === null ? '-' : formatNumber(remainingSwiftForTarget) }}
            </p>
          </div>
          <div class="result-box readable-text-block">
            <p class="eyebrow">현재 공격속도</p>
            <p class="result-value">{{ formatPercent(currentAttackTotalCapped) }}%</p>
            <p class="muted">상한 적용 <br/> (초과 {{ formatPercent(attackOverCap) }}%)</p>
          </div>
          <div class="result-box readable-text-block">
            <p class="eyebrow">현재 이동속도</p>
            <p class="result-value">{{ formatPercent(currentMoveTotalCapped) }}%</p>
            <p class="muted">상한 적용 <br/> (초과 {{ formatPercent(moveOverCap) }}%)</p>
          </div>
        </div>

        <div class="breakdown readable-text-block">
          <div class="breakdown-head">
            <p class="eyebrow">합산 버프 상세</p>
            <span class="pill-note">총 {{ formatSigned(totalSpeedIncrease) }}%</span>
          </div>
        <div class="breakdown-body">
          <div v-for="row in breakdownRows" :key="row.label" class="breakdown-row">
            <span>{{ row.label }}</span>
            <span class="value" :class="{ negative: row.value < 0 }">{{ formatSigned(row.value) }}%</span>
          </div>
        </div>
      </div>
    </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import CustomSelect, { type SelectOption } from '../common/CustomSelect.vue'

const swiftPerStat = 0.0172

type SupersonicLevelKey = 'level1' | 'level2'
type SupersonicTargetKey = 'max' | 'iptar21'

type SupersonicLevelConfig = {
  key: SupersonicLevelKey
  label: string
  baseRatio: number
  overCapRatio: number
  bothOverCapBonus: number
  maxBonus: number
}

const supersonicLevels: SupersonicLevelConfig[] = [
  { key: 'level1', label: '1레벨', baseRatio: 0.05, overCapRatio: 0.15, bothOverCapBonus: 4, maxBonus: 12 },
  { key: 'level2', label: '2레벨', baseRatio: 0.1, overCapRatio: 0.3, bothOverCapBonus: 8, maxBonus: 24 }
]

type StarSpeedKey =
  | 'none'
  | 'legend-10'
  | 'legend-14'
  | 'relic-17'
  | 'relic-18'
  | 'relic-19'
  | 'relic-20'
  | 'ancient-17'
  | 'ancient-18'
  | 'ancient-19'
  | 'ancient-20'

const starOptions: Array<{ key: StarSpeedKey; label: string; attack: number; move: number }> = [
  { key: 'none', label: '없음', attack: 0, move: 0 },
  { key: 'legend-10', label: '전설 10P: 공속 +0.90%', attack: 0.9, move: 0 },
  { key: 'legend-14', label: '전설 14P: 이속 +0.90%', attack: 0, move: 0.9 },
  { key: 'relic-17', label: '유물 17P: 공이속 +1.80%', attack: 1.8, move: 1.8 },
  { key: 'relic-18', label: '유물 18P: 공이속 +2.10%', attack: 2.1, move: 2.1 },
  { key: 'relic-19', label: '유물 19P: 공이속 +2.40%', attack: 2.4, move: 2.4 },
  { key: 'relic-20', label: '유물 20P: 공이속 +2.70%', attack: 2.7, move: 2.7 },
  { key: 'ancient-17', label: '고대 17P: 공이속 +2.70%', attack: 2.7, move: 2.7 },
  { key: 'ancient-18', label: '고대 18P: 공이속 +3.00%', attack: 3.0, move: 3.0 },
  { key: 'ancient-19', label: '고대 19P: 공이속 +3.30%', attack: 3.3, move: 3.3 },
  { key: 'ancient-20', label: '고대 20P: 공이속 +3.60%', attack: 3.6, move: 3.6 }
]

const starSpeedOptions = computed<SelectOption[]>(() =>
  starOptions.map(option => ({ label: option.label, value: option.key }))
)

const seaOptions = [
  { label: '없음', value: 0 },
  { label: '전설 10P (+1.00%)', value: 1 },
  { label: '유물 10P (+1.00%)', value: 1 },
  { label: '유물 17P (+2.00%)', value: 2 },
  { label: '고대 10P (+1.00%)', value: 1 },
  { label: '고대 17P (+3.00%)', value: 3 }
]

const engravingAtk = ref(0)
const engravingMove = ref(0)
const tripodAtk = ref(0)
const tripodMove = ref(0)
const braceletAtk = ref(0)

const buffSupport = ref(false)
const buffMeal = ref(false)
const buffFeast = ref(false)
const buffMassIncrease = ref(false)

type BuffKey = 'support' | 'meal' | 'feast' | 'massIncrease'
type BuffVariant = 'default' | 'warning'

const buffOptions: Array<{ key: BuffKey; label: string; value: number; variant?: BuffVariant }> = [
  { key: 'support', label: '서포터 버프 (갈망)', value: 9 },
  { key: 'meal', label: '공속 음식', value: 3 },
  { key: 'feast', label: '공이속 만찬', value: 5 },
  { key: 'massIncrease', label: '질량 증가', value: -10, variant: 'warning' }
]

const seaFastAttack = ref(0)
const starSpeedKey = ref<StarSpeedKey>('none')

const swiftStat = ref(0)

const selectedSupersonicLevel = ref<SupersonicLevelKey>('level2')
const selectedSupersonicTarget = ref<SupersonicTargetKey>('max')

const selectSupersonicLevel = (level: SupersonicLevelKey) => {
  selectedSupersonicLevel.value = level
  selectedSupersonicTarget.value = 'max'
}

const selectSupersonicIptar21 = () => {
  selectedSupersonicLevel.value = 'level2'
  selectedSupersonicTarget.value = 'iptar21'
}

const isBuffActive = (key: BuffKey) => {
  if (key === 'support') return buffSupport.value
  if (key === 'meal') return buffMeal.value
  if (key === 'feast') return buffFeast.value
  return buffMassIncrease.value
}

const toggleBuff = (key: BuffKey) => {
  if (key === 'support') {
    buffSupport.value = !buffSupport.value
    return
  }
  if (key === 'meal') {
    buffMeal.value = !buffMeal.value
    return
  }
  if (key === 'feast') {
    buffFeast.value = !buffFeast.value
    return
  }
  buffMassIncrease.value = !buffMassIncrease.value
}

const defaultStar = starOptions[0] ?? { key: 'none', label: '없음', attack: 0, move: 0 }
const selectedStar = computed(
  () => starOptions.find(option => option.key === starSpeedKey.value) ?? defaultStar
)

const selectedSupersonicConfig = computed(() => {
  const fallback = supersonicLevels[0] ?? {
    key: 'level1',
    label: '1레벨',
    baseRatio: 0.05,
    overCapRatio: 0.15,
    bothOverCapBonus: 4,
    maxBonus: 12
  }
  return supersonicLevels.find(level => level.key === selectedSupersonicLevel.value) ?? fallback
})

const buffAttack = computed(() => {
  let value = 0
  if (buffSupport.value) value += 9
  if (buffMeal.value) value += 3
  if (buffFeast.value) value += 5
  if (buffMassIncrease.value) value -= 10
  return value
})

const buffMove = computed(() => {
  let value = 0
  if (buffSupport.value) value += 9
  if (buffFeast.value) value += 5
  return value
})

const truncateToTenth = (value: number) => Math.floor(value * 10) / 10

const swiftContribution = computed(() => Number((swiftStat.value * swiftPerStat).toFixed(3)))

const baseAttack = computed(
  () =>
    engravingAtk.value +
    tripodAtk.value +
    braceletAtk.value +
    buffAttack.value +
    seaFastAttack.value +
    selectedStar.value.attack
)

const baseMove = computed(
  () =>
    engravingMove.value +
    tripodMove.value +
    braceletAtk.value +
    buffMove.value +
    selectedStar.value.move
)

const currentAttackIncrease = computed(() =>
  Number((truncateToTenth(baseAttack.value + swiftContribution.value)).toFixed(1))
)
const currentMoveIncrease = computed(() => Number((truncateToTenth(baseMove.value + swiftContribution.value)).toFixed(1)))

const currentAttackTotal = computed(() => 100 + currentAttackIncrease.value)
const currentMoveTotal = computed(() => 100 + currentMoveIncrease.value)

const currentAttackTotalCapped = computed(() => Math.min(140, currentAttackTotal.value))
const currentMoveTotalCapped = computed(() => Math.min(140, currentMoveTotal.value))

const attackOverCap = computed(() => Math.max(0, currentAttackTotal.value - 140))
const moveOverCap = computed(() => Math.max(0, currentMoveTotal.value - 140))

const computeSupersonicBonus = (swift: number, config: SupersonicLevelConfig) => {
  const swiftSpeed = swift * swiftPerStat
  const attackTotal = truncateToTenth(100 + baseAttack.value + swiftSpeed)
  const moveTotal = truncateToTenth(100 + baseMove.value + swiftSpeed)

  const cappedAttackIncrease = Math.max(0, Math.min(140, attackTotal) - 100)
  const cappedMoveIncrease = Math.max(0, Math.min(140, moveTotal) - 100)

  const attackOver = Math.max(0, attackTotal - 140)
  const moveOver = Math.max(0, moveTotal - 140)
  const hasBothOver = attackOver > 0 && moveOver > 0

  const baseBonus = (cappedAttackIncrease + cappedMoveIncrease) * config.baseRatio
  const overCapBonus = hasBothOver
    ? config.bothOverCapBonus + (attackOver + moveOver) * config.overCapRatio
    : 0

  return Math.min(config.maxBonus, baseBonus + overCapBonus)
}

const currentSupersonicBonus = computed(() =>
  Number(computeSupersonicBonus(swiftStat.value, selectedSupersonicConfig.value).toFixed(3))
)

const targetSupersonicBonus = computed(() => {
  if (selectedSupersonicTarget.value === 'iptar21') return 21
  return selectedSupersonicConfig.value.maxBonus
})

const requiredSwiftForTarget = computed<number | null>(() => {
  const config = selectedSupersonicConfig.value
  const target = targetSupersonicBonus.value
  const eps = 1e-8

  if (target > config.maxBonus + eps) return null
  if (computeSupersonicBonus(0, config) >= target - eps) return 0

  let low = 0
  let high = 1
  while (high < 50000 && computeSupersonicBonus(high, config) < target - eps) {
    high *= 2
  }
  if (high >= 50000) return 50000

  while (low + 1 < high) {
    const mid = Math.floor((low + high) / 2)
    if (computeSupersonicBonus(mid, config) >= target - eps) {
      high = mid
    } else {
      low = mid
    }
  }
  return high
})

const remainingSwiftForTarget = computed<number | null>(() => {
  const required = requiredSwiftForTarget.value
  if (required === null) return null
  return Math.max(0, required - swiftStat.value)
})

const totalSpeedIncrease = computed(() =>
  Number((currentAttackIncrease.value + currentMoveIncrease.value).toFixed(3))
)

const breakdownRows = computed(() =>
  [
    { label: '직업 각인 (공속)', value: engravingAtk.value },
    { label: '직업 각인 (이속)', value: engravingMove.value },
    { label: '트라이포드 (공속)', value: tripodAtk.value },
    { label: '트라이포드 (이속)', value: tripodMove.value },
    { label: '팔찌 공이속', value: braceletAtk.value },
    { label: '버프 합계 (공속)', value: buffAttack.value },
    { label: '버프 합계 (이속)', value: buffMove.value },
    { label: '혼돈의 해: 재빠른 공격', value: seaFastAttack.value },
    { label: '혼돈의 별: 속도 (공속)', value: selectedStar.value.attack },
    { label: '혼돈의 별: 속도 (이속)', value: selectedStar.value.move }
  ].filter(row => row.value !== 0)
)

const formatPercent = (value: number) => Number(value.toFixed(2))
const formatSigned = (value: number) => (value < 0 ? `${formatPercent(value)}` : `+${formatPercent(value)}`)
const formatNumber = (value: number) => value.toLocaleString('ko-KR')
</script>

<style scoped>
.supersonic-calculator {
  width: 100%;
  --readable-text-min: 30ch;
  --readable-text-max: 100ch;
  --readable-text-paragraph-gap: 0.5em;
  --readable-text-line-height: 1;
}

.level-toggle {
  display: inline-flex;
  gap: 8px;
  flex-wrap: wrap;
}

.level-btn {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.level-btn.active {
  background: var(--primary-soft-bg);
  color: var(--primary-color);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.12);
}

.eyebrow {
  font-size: 12px;
  color: var(--text-tertiary);
  letter-spacing: 0.02em;
  margin-bottom: 4px;
}

.muted {
  color: var(--text-secondary);
  font-size: 13px;
}

.calc-grid {
  display: grid;
  grid-template-columns: 3fr 1fr 1fr 1fr;
  gap: 14px;
  width: 100%;
}

.panel-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
}

.readable-text-block {
  min-inline-size: min(100%, var(--readable-text-min));
  max-inline-size: var(--readable-text-max);
  line-height: var(--readable-text-line-height);
  text-align: left;
}

.card-head {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: space-between;
}

.card-head-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom:10px;
}

.pill-note {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  font-weight: 700;
}

.group-title {
  margin: 14px 0 6px;
  font-weight: 800;
  color: var(--text-primary);
}

.form-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(0, 1fr));
}

.form-grid-compact {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
}

.inline-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 14px;
  margin-top: 10px;
}

.input-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: var(--text-secondary);
}

.input-field input,
.input-field select,
:deep(.input-field .custom-select__trigger) {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
}

.input-field input:focus,
.input-field select:focus,
:deep(.input-field .custom-select__trigger:focus) {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.buff-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 8px;
  margin-top: 14px;
}

.buff-chip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all 0.15s ease;
  font-weight: 700;
  color: var(--text-primary);
}

.buff-chip .chip-value {
  color: var(--primary-color);
}

.buff-chip.warning {
  color: var(--danger-color, #dc2626);
}

.buff-chip.warning .chip-value {
  color: var(--danger-color, #dc2626);
}

.buff-chip.active {
  background: var(--primary-soft-bg);
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.12);
}

.buff-chip.warning.active {
  background: rgba(220, 38, 38, 0.08);
  border-color: rgba(220, 38, 38, 0.35);
  box-shadow: 0 8px 16px rgba(220, 38, 38, 0.12);
}

.subsection-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 14px;
}

.subsection-head .group-title {
  margin: 0;
}

.result-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.result-box {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.result-value {
  font-size: 1.4rem;
  font-weight: 800;
  margin: 4px 0;
}

.result-box.primary {
  border-color: var(--primary-color);
  background: var(--primary-soft-bg);
}

.breakdown {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.breakdown-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.breakdown-body {
  display: grid;
  gap: 6px;
}

.breakdown-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 10px;
  background: var(--card-bg);
  border: 1px dashed var(--border-color);
  font-weight: 600;
}

.breakdown-row .value {
  color: var(--primary-color);
}

.breakdown-row .value.negative {
  color: var(--danger-color, #dc2626);
}

@media (max-width: 1100px) {
  .calc-grid {
    grid-template-columns: 1fr;
  }

  .buff-card,
  .result-card {
    grid-column: auto;
  }
}
</style>
