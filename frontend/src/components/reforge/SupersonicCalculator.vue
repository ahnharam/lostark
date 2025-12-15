<template>
  <div class="supersonic-page">
    <div class="supersonic-grid">
      <section class="panel-card input-panel">
        <div class="panel-head">
          <div>
            <p class="eyebrow">음속 돌파 계산기</p>
            <h3>신속 기반 공이속 계산</h3>
            <p class="muted">신속 스탯, 버프, 혼돈 코어를 합산해 목표 공이속을 맞춰 보세요.</p>
          </div>
          <div class="pill-note">신속 1 당 {{ swiftPerStat }}%</div>
        </div>

        <div class="field-grid">
          <div class="field-block">
            <p class="group-title">직업 각인</p>
            <div class="dual-grid">
              <label class="input-field">
                <span>공격속도 버프 (%)</span>
                <input v-model.number="engravingAtk" type="number" min="0" step="0.1" />
              </label>
              <label class="input-field">
                <span>이동속도 버프 (%)</span>
                <input v-model.number="engravingMove" type="number" min="0" step="0.1" />
              </label>
            </div>
          </div>

          <div class="field-block">
            <p class="group-title">스킬 트라이포드</p>
            <div class="dual-grid">
              <label class="input-field">
                <span>공격속도 버프 (%)</span>
                <input v-model.number="tripodAtk" type="number" min="0" step="0.1" />
              </label>
              <label class="input-field">
                <span>이동속도 버프 (%)</span>
                <input v-model.number="tripodMove" type="number" min="0" step="0.1" />
              </label>
            </div>
          </div>

          <div class="field-block">
            <p class="group-title">팔찌</p>
            <label class="input-field">
              <span>공이속 버프 (%)</span>
              <input v-model.number="braceletAtk" type="number" min="0" step="0.1" />
            </label>
          </div>

          <div class="field-block">
            <p class="group-title">버프 효과</p>
            <div class="checkbox-grid">
              <label class="checkbox">
                <input v-model="buffSupport" type="checkbox" />
                서포터 버프 (갈망 +9%)
              </label>
              <label class="checkbox">
                <input v-model="buffMeal" type="checkbox" />
                공속 음식 (+3%)
              </label>
              <label class="checkbox">
                <input v-model="buffFeast" type="checkbox" />
                공이속 만찬 (+5%)
              </label>
              <label class="checkbox warning">
                <input v-model="buffMassIncrease" type="checkbox" />
                질량 증가 (-10%)
              </label>
            </div>
          </div>

          <div class="field-block">
            <p class="group-title">아크 그리드 (혼돈 코어)</p>
            <div class="inline-grid">
              <label class="input-field">
                <span>혼돈의 해: 재빠른 공격</span>
                <select v-model.number="seaFastAttack">
                  <option v-for="option in seaOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </label>
              <label class="input-field">
                <span>혼돈의 별: 속도</span>
                <select v-model="starSpeedKey">
                  <option v-for="option in starOptions" :key="option.key" :value="option.key">
                    {{ option.label }}
                  </option>
                </select>
              </label>
            </div>
          </div>

          <div class="field-block">
            <p class="group-title">목표 / 신속</p>
            <div class="dual-grid">
              <label class="input-field">
                <span>목표 공격속도 (%)</span>
                <input v-model.number="targetAttack" type="number" min="0" step="0.1" />
              </label>
              <label class="input-field">
                <span>목표 이동속도 (%)</span>
                <input v-model.number="targetMove" type="number" min="0" step="0.1" />
              </label>
            </div>
            <label class="input-field">
              <span>현재 신속 스탯</span>
              <input v-model.number="swiftStat" type="number" min="0" step="1" />
            </label>
          </div>
        </div>
      </section>

      <section class="panel-card result-panel">
        <div class="result-grid">
          <div class="result-box">
            <p class="eyebrow">현재 공격속도</p>
            <p class="result-value">{{ formatPercent(currentAttack) }}%</p>
            <p class="muted">신속 포함</p>
          </div>
          <div class="result-box">
            <p class="eyebrow">현재 이동속도</p>
            <p class="result-value">{{ formatPercent(currentMove) }}%</p>
            <p class="muted">신속 포함</p>
          </div>
          <div class="result-box highlight">
            <p class="eyebrow">필요 신속 스탯</p>
            <p class="result-value large">{{ formatNumber(requiredSwift) }}</p>
            <p class="muted">목표 공/이속 기준 최대 필요값</p>
          </div>
        </div>

        <div class="breakdown-card">
          <div class="breakdown-head">
            <p class="eyebrow">합산 버프 상세</p>
            <span class="pill-note">신속 기여 {{ formatPercent(swiftContribution) }}%</span>
          </div>
          <div class="breakdown-list">
            <div v-for="row in breakdownRows" :key="row.label" class="breakdown-row">
              <span>{{ row.label }}</span>
              <span class="value">+{{ formatPercent(row.value) }}%</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const swiftPerStat = 0.0172

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

const seaFastAttack = ref(0)
const starSpeedKey = ref<StarSpeedKey>('none')

const targetAttack = ref(140)
const targetMove = ref(140)
const swiftStat = ref(0)

const defaultStar = starOptions[0] ?? { key: 'none', label: '없음', attack: 0, move: 0 }
const selectedStar = computed(
  () => starOptions.find(option => option.key === starSpeedKey.value) ?? defaultStar
)

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
  if (buffMeal.value) value += 3
  if (buffFeast.value) value += 5
  return value
})

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
  () => engravingMove.value + tripodMove.value + buffMove.value + selectedStar.value.move
)

const currentAttack = computed(() => Number((baseAttack.value + swiftContribution.value).toFixed(2)))
const currentMove = computed(() => Number((baseMove.value + swiftContribution.value).toFixed(2)))

const requiredSwift = computed(() => {
  const needAttack = Math.max(0, targetAttack.value - baseAttack.value)
  const needMove = Math.max(0, targetMove.value - baseMove.value)
  const worstNeed = Math.max(needAttack, needMove)
  return Math.ceil(worstNeed / swiftPerStat)
})

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
const formatNumber = (value: number) => value.toLocaleString('ko-KR')
</script>

<style scoped>
.supersonic-page {
  width: 100%;
  display: flex;
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

.supersonic-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 16px;
  width: 100%;
}

.panel-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 14px;
  padding: 16px;
  box-shadow: var(--shadow-sm);
}

.input-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
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
  font-weight: 700;
}

.field-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.group-title {
  font-weight: 800;
  color: var(--text-primary);
}

.dual-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.inline-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.input-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: var(--text-secondary);
}

.input-field input,
.input-field select {
  border: 1px solid var(--border-color);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
}

.input-field input:focus,
.input-field select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.12);
}

.checkbox-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

.checkbox {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--text-secondary);
}

.checkbox.warning {
  color: var(--danger-color, #dc2626);
}

.result-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 10px;
}

.result-box {
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 12px;
  background: var(--bg-secondary);
}

.result-box.highlight {
  border-color: var(--primary-color);
  background: var(--primary-soft-bg);
}

.result-value {
  font-size: 1.3rem;
  font-weight: 800;
  margin: 4px 0;
}

.result-value.large {
  font-size: 1.6rem;
}

.breakdown-card {
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

.breakdown-list {
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

@media (max-width: 1100px) {
  .supersonic-grid {
    grid-template-columns: 1fr;
  }
}
</style>
