<template>
  <section class="detail-panel summary-panel">
    <div v-if="activeCharacter" class="summary-grid summary-grid--modules summary-grid--stacked">
      <article class="summary-card summary-card--module summary-card--equipment">
        <div class="ark-section__block">
          <div class="summary-card__head">
            <p class="summary-eyebrow">장비</p>
          </div>
          <p v-if="detailLoading" class="summary-note">장비 정보를 정리하는 중입니다...</p>
          <p v-else-if="detailError" class="summary-note summary-note--warning">{{ detailError }}</p>
          <div v-else class="equipment-row-list">
            <div v-for="row in equipmentRows" :key="row.key" class="equipment-row">
              <template v-if="row.right?.isBracelet && !row.left">
                <div class="equipment-side equipment-side--bracelet">
                  <div class="equipment-icon-stack">
                    <LazyImage :src="row.right.icon" :alt="row.right.name" width="40" height="40"
                      imageClass="summary-icon" errorIcon="💍" :useProxy="true" />
                    </div>
                  <div class="equipment-info-stack">
                      <span v-if="row.right.itemLevel" class="equipment-item-level equipment-item-level--inline">
                        {{ row.right.itemLevel }}
                      </span>
                    <div v-if="row.right.effects?.length" class="equipment-effect-badges equipment-effect-badges--grid">
                      <span
                        v-for="(effect, idx) in row.right.effects"
                        :key="`effect-${row.key}-${idx}`"
                        class="bracelet-badge bracelet-badge--effect"
                        :style="{ backgroundColor: 'transparent', color: effectTextDisplayColor(effect, row.right) }"
                      >
                        {{ effectLabelDisplay(effect, row.right) }}
                      </span>
                    </div>
                  </div>
                </div>
              </template>
              <template v-else>
                <div class="equipment-side equipment-side--left">
                  <template v-if="row.left">
                    <div class="equipment-icon-stack">
                      <LazyImage :src="row.left.icon" :alt="row.left.name" width="40" height="40"
                        imageClass="summary-icon" errorIcon="🗡️" :useProxy="true" />
                    </div>
                    <div class="equipment-info-stack">
                      <span v-if="row.left.itemLevel" class="equipment-item-level equipment-item-level--inline">
                        {{ row.left.itemLevel }}
                      </span>
                      <p
                        v-if="formatGearEnhanceLabel(row.left)"
                        class="equipment-line equipment-line--primary"
                      >
                        {{ formatGearEnhanceLabel(row.left) }}
                      </p>
                    <div v-if="row.left.transcend && Number(row.left.transcend) !== -1" class="equipment-line equipment-line--transcend">
                      <span class="equipment-progress equipment-progress--transcend">
                        <span class="equipment-progress__fill" :style="transcendBarStyle(row.left.transcend)"></span>
                        <span class="equipment-progress__label equipment-progress__label--inline">
                          초월 {{ row.left.transcend }}
                          <span
                            class="transcend-icon transcend-icon--inline"
                            :class="{ 'transcend-icon--gold': isTranscendGold(row.left.transcend) }"
                            aria-hidden="true"
                          ></span>
                        </span>
                      </span>
                    </div>
                    <div
                      v-if="qualityValue(row.left.quality) !== null && Number(row.left.quality) !== -1"
                      class="equipment-line equipment-line--quality"
                    >
                      <span class="equipment-progress">
                        <span class="equipment-progress__fill" :style="qualityBarStyle(row.left.quality)"></span>
                        <span class="equipment-progress__label equipment-progress__label--inline">
                          품질 {{ row.left.quality }}
                        </span>
                      </span>
                    </div>
                    </div>
                  </template>
                  <p v-else class="equipment-empty">—</p>
                </div>

                <div class="equipment-side">
                  <template v-if="row.right">
                    <div class="equipment-icon-stack">
                      <LazyImage :src="row.right.icon" :alt="row.right.name" width="40" height="40"
                        imageClass="summary-icon" errorIcon="💍" :useProxy="true" />
                    </div>
                    <div class="equipment-info-stack">
                      <span v-if="row.right.itemLevel" class="equipment-item-level equipment-item-level--inline">
                        {{ row.right.itemLevel }}
                      </span>
                      <p
                        v-if="formatGearEnhanceLabel(row.right)"
                        class="equipment-line equipment-line--primary"
                      >
                        {{ formatGearEnhanceLabel(row.right) }}
                      </p>
                      <div v-if="row.right.transcend && Number(row.right.transcend) !== -1" class="equipment-line equipment-line--transcend">
                        <span class="equipment-progress equipment-progress--transcend">
                          <span class="equipment-progress__fill" :style="transcendBarStyle(row.right.transcend)"></span>
                          <span class="equipment-progress__label equipment-progress__label--inline">
                            초월 {{ row.right.transcend }}
                            <span
                              class="transcend-icon transcend-icon--inline"
                              :class="{ 'transcend-icon--gold': isTranscendGold(row.right.transcend) }"
                              aria-hidden="true"
                            ></span>
                          </span>
                        </span>
                      </div>
                      <div v-if="row.right.effects?.length" class="equipment-effect-badges"
                      :class="{ 'equipment-effect-badges--grid': row.right.isBracelet }">
                      <div v-for="(effect, idx) in row.right.effects" :key="`effect-${row.key}-${idx}`"
                        class="equipment-effect-chip" :class="{
                          'equipment-effect-chip--tooltip':
                            row.right.isAccessory && !row.right.isBracelet && !row.right.isAbilityStone,
                            'equipment-badge--combat': effect.isCombat,
                            'equipment-badge--fullrow': !effect.isCombat && row.right.isBracelet
                          }">
                          <span
                            v-if="row.right.isBracelet"
                            class="bracelet-badge bracelet-badge--effect"
                            :style="{ backgroundColor: 'transparent', color: effectTextDisplayColor(effect, row.right) }"
                          >
                            {{ effectLabelDisplay(effect, row.right) }}
                          </span>
                          <span
                            v-else
                            class="equipment-badge equipment-badge--effect"
                            :class="{
                              'equipment-badge--combat': effect.isCombat,
                              'equipment-badge--fullrow': !effect.isCombat && row.right.isBracelet
                            }"
                            :title="row.right.isAccessory && !row.right.isBracelet && !row.right.isAbilityStone
                              ? null
                              : effect.full || effect.label
                            "
                            :style="{ backgroundColor: 'transparent', color: effectTextDisplayColor(effect, row.right) }"
                          >
                            {{ effectLabelDisplay(effect, row.right) }}
                          </span>
                          <div v-if="row.right.isAccessory && !row.right.isBracelet && !row.right.isAbilityStone"
                            class="popup-surface popup-surface--tooltip equipment-effect-tooltip">
                            <p class="popup-surface__body">
                              {{ effect.full || effect.label }}
                            </p>
                          </div>
                        </div>
                        <div
                          v-if="qualityValue(row.right.quality) !== null && Number(row.right.quality) !== -1"
                          class="equipment-line equipment-line--quality"
                        >
                          <span class="equipment-progress">
                            <span class="equipment-progress__fill" :style="qualityBarStyle(row.right.quality)"></span>
                            <span class="equipment-progress__label equipment-progress__label--inline">
                              품질 {{ row.right.quality }}
                            </span>
                          </span>
                        </div>
                      </div>
                    </div>
                  </template>
                  <p v-else class="equipment-empty"> </p>
                </div>
              </template>
            </div>
          </div>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--ark">
        <div class="ark-section">
          <div class="ark-section__block ark-section__block--passive">
            <div class="summary-card__head">
              <p class="summary-eyebrow">아크 패시브</p>
              <h4>{{ arkSummary.passiveTitle || '아크 루트 정보' }}</h4>
            </div>
            <div v-if="arkSummary.passiveMatrix?.length" class="ark-passive-summary">
              <div class="ark-passive-grid">
                <div class="ark-passive-grid-header">
                  <span class="ark-passive-header-cell ark-passive-header-tier">티어</span>
                  <span v-for="point in arkSummary.appliedPoints" :key="point.key" class="ark-passive-header-cell">
                    {{ point.label }}
                  </span>
                </div>
                <div v-for="row in arkSummary.passiveMatrix.slice(0, 4)" :key="row.id" class="ark-passive-grid-row">
                  <span class="ark-passive-tier">{{ row.label }}</span>
                  <div v-for="section in row.sections" :key="`${row.id}-${section.key}`" class="ark-passive-cell">
                    <div v-if="section.effects.length" class="ark-passive-cell-list">
                      <div v-for="effect in section.effects" :key="effect.key" class="ark-passive-chip">
                        <div class="ark-passive-chip-visual">
                          <LazyImage v-if="effect.icon" :src="effect.icon" :alt="effect.name" width="32" height="32"
                            imageClass="summary-icon" errorIcon="🌟" :useProxy="true" />
                          <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">★</div>
                          <!-- <span v-if="effect.levelDisplay" class="ark-passive-level">
                            {{ effect.levelDisplay }}
                          </span> -->
                        </div>
                      </div>
                    </div>
                    <p v-else class="summary-note"> </p>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="summary-note">패시브 정보가 없습니다.</div>
            <div v-if="arkSummary.passiveEffects?.length" class="ark-passive-summary-footer">
              <p class="summary-inline">투자된 패시브</p>
              <div class="ark-passive-columns">
                <div
                  v-for="group in passiveEffectGroups"
                  :key="group.key"
                  class="ark-passive-column"
                >
                  <p class="ark-passive-column__title">
                    {{ group.label }}
                    <span v-if="group.pointLabel" class="ark-passive-column__points">· {{ group.pointLabel }}</span>
                  </p>
                  <ul class="ark-passive-list">
                    <li
                      v-for="effect in group.items"
                      :key="effect.key"
                      class="ark-passive-list__item"
                    >
                      <span class="ark-passive-list__level">{{ effect.levelLabel }}</span>
                      <span class="ark-passive-list__name">{{ effect.name }}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <div class="ark-section__block">
            <div class="summary-card__head">
              <p class="summary-eyebrow">아크 그리드</p>
            </div>
            <p v-if="arkGridLoading" class="summary-note">아크 그리드 정보를 불러오는 중입니다...</p>
            <p v-else-if="arkGridError" class="summary-note summary-note--warning">{{ arkGridError }}</p>
            <div v-else class="ark-core-layout">
              <div v-if="arkSummary.coreMatrix.rows.length" class="ark-core-card-grid">
                <div
                  v-for="row in arkSummary.coreMatrix.rows"
                  :key="`core-row-${row.key}`"
                  class="ark-core-row-group"
                >
                  <p class="ark-core-row-label">{{ row.label }}</p>
                  <div class="ark-core-card-row">
                    <div v-for="cell in row.cells" :key="`core-cell-${cell.key}`" class="ark-core-card-cell">
                      <article v-for="slot in cell.slots" :key="slot.key" class="ark-core-card">
                        <div class="ark-core-card__thumb">
                          <LazyImage
                            v-if="slot.icon"
                            :src="slot.icon"
                            :alt="slot.name"
                            width="40"
                            height="40"
                            imageClass="ark-core-card__image"
                            errorIcon="🧩"
                            :useProxy="true"
                          />
                          <div v-else class="ark-core-card__placeholder" aria-hidden="true">
                            {{ slot.initial }}
                          </div>
                        </div>
                        <div class="ark-core-card__body">
                          <p class="ark-core-card__name">{{ slot.name }}</p>
                          <p v-if="slot.pointLabel" class="ark-core-card__meta">{{ slot.pointLabel }}</p>
                        </div>
                      </article>
                    </div>
                  </div>
                </div>
              </div>
              <p v-else class="summary-note">
                표시할 아크 그리드 정보가 없습니다.
              </p>
            </div>
          </div>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--skills">
        <div class="ark-section__block">
          <div class="summary-card__head">
            <p class="summary-eyebrow">스킬</p>
          </div>
          <p v-if="skillLoading" class="summary-note">스킬 정보를 불러오는 중입니다...</p>
          <p v-else-if="skillError" class="summary-note summary-note--warning">{{ skillError }}</p>
          <ul v-else-if="skillHighlights.length" class="summary-list summary-list--flat summary-skill-list">
            <li v-for="skill in skillHighlights" :key="skill.key"
              class="summary-list-item summary-list-item--plain summary-skill-item"
              :class="{ 'summary-skill-item--with-gems': skill.hasGem || skill.rune }">
              <div class="summary-skill-icon-stack">
                <LazyImage :src="skill.icon" :alt="skill.name" width="40" height="40" imageClass="summary-icon"
                  errorIcon="🎯" :useProxy="true" />
              </div>
              <div class="summary-list-text summary-skill-text">
                <div class="summary-skill-head">
                  <p class="summary-title">
                    {{ skill.name }}
                  </p>
                  <span v-if="skill.levelLabel" class="summary-skill-level-inline">
                    {{ skill.levelLabel }}
                  </span>
                </div>
                <div v-if="skill.tripods?.length" class="summary-tripod-icon-row">
                  <!-- <div v-for="(tripod, index) in skill.tripods" :key="tripod.key" class="summary-tripod-icon"
                    :class="`summary-tripod-icon--${index + 1}`" :title="tripod.name">
                  </div> -->
                  <span v-for="(tripod, index) in skill.tripods" :key="tripod.key" class="summary-tripod-slot-dot" :class="`summary-tripod-icon--${index + 1}`">{{ tripod.slotLabel }}</span>
                </div>
              </div>
              <div v-if="skill.hasGem || skill.rune" class="summary-skill-gems">
                <div class="summary-gem-grid">
                  <div>
                    <div class="summary-gem-row summary-gem-row--label">
                      <span class="summary-gem-label" :title="skill.rune?.name || ''"
                        :style="{ color: skill.rune?.color || undefined }">
                        {{ skill.rune?.name || '룬' }}
                      </span>
                      <span class="summary-gem-label" :title="skill.gems.damage?.name || ''">
                        {{ skill.gems.damage?.label || skill.gems.damage?.name || '' }}
                      </span>
                      <span class="summary-gem-label" :title="skill.gems.cooldown?.name || ''">
                        {{ skill.gems.cooldown?.label || skill.gems.cooldown?.name || '' }}
                      </span>
                    </div>
                  </div>
                <div class="summary-gem-row summary-gem-row--icons">
                  <div class="summary-gem-cell">
                  </div>
                  <div class="summary-gem-cell">
                    <span v-if="skill.gems.damage?.label != undefined" class="summary-gem-level-dot">
                      {{ skill.gems.damage?.levelLabel?.replace('Lv.', '') || '-' }}
                    </span>
                  </div>
                  <div v-if="skill.gems.cooldown?.label != undefined" class="summary-gem-cell">
                    <span class="summary-gem-level-dot">
                      {{ skill.gems.cooldown?.levelLabel?.replace('Lv.', '') || '-' }}
                    </span>
                  </div>
                </div>
                </div>
              </div>
            </li>
          </ul>
              <div v-if="(skillLooseGems?.length || 0) > 0" class="summary-loose-gems">
                <p class="summary-inline summary-inline--muted">장착되지 않은 보석</p>
                <div class="summary-loose-gem-grid">
                  <div v-for="gem in skillLooseGems" :key="gem.key" class="summary-loose-gem">
                    <div class="summary-gem-icon-stack">
                      <div class="summary-gem-icon-wrapper summary-gem-icon-wrapper--empty">
                        <span class="summary-gem-text">{{ gem.skillName?.[0] || '보석' }}</span>
                      </div>
                      <span class="summary-gem-level-dot">
                        {{ gem.levelLabel?.replace('Lv.', '') || '-' }}
                      </span>
                    </div>
                <div class="summary-loose-gem-body">
                  <p class="summary-title">{{ gem.skillName || gem.name }}</p>
                  <p class="summary-sub">
                    {{ gem.grade || gem.typeLabel || gem.effectLabel || '' }}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <!-- <p v-else class="summary-note">요약할 스킬 정보가 없습니다.</p> -->
        </div>
        <div class="ark-section__block ark-section__block--engravings">
          <div class="summary-card__head">
            <p class="summary-eyebrow">각인</p>
          </div>
          <div v-if="engravingSummary.length" class="summary-engraving-grid">
            <div v-for="engrave in engravingSummary" :key="engrave.key" class="summary-engraving-card">
              <div class="summary-engraving-icon">
                <LazyImage
                  v-if="engrave.icon || engravingIcon(engrave.name)"
                  :src="engrave.icon || engravingIcon(engrave.name)"
                  :alt="engrave.name"
                  width="40"
                  height="40"
                  imageClass="summary-icon"
                  errorIcon="🔮"
                  :useProxy="true"
                />
                <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">
                  {{ engrave.gradeLabel?.[0] || 'E' }}
                </div>
              </div>
              <p class="summary-title summary-engraving-name" :style="{ color: engravingColor(engrave.gradeLabel) }">
                {{ formatEngravingName(engrave.name) }}
              </p>
              <div class="summary-engrave-meta-row">
                <img
                  v-if="engrave.levelLabel"
                  :src="engravingLevelBadgeSrc(engrave.levelLabel)"
                  class="engrave-level-image"
                />
                <span v-if="engrave.levelLabel" class="summary-pill summary-pill--primary">
                  {{ engrave.levelLabel }}
                </span>
              </div>
              <div class="summary-engrave-meta-row">
                <div v-if="engrave.craftLabel" class="engrave-craft-badge">
                  <img
                    :src="craftBadgeSrc(engrave.craftLabel)"
                    class="engrave-craft-image"
                  />
                  <span class="engrave-craft-text">{{ craftLevelLabel(engrave.craftLabel) }}</span>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="summary-note">각인 정보가 없습니다.</p>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--collection">
        <div class="ark-section__block">
          <div class="summary-card__head">
            <p class="summary-eyebrow">수집</p>
            <h4>주요 포인트</h4>
          </div>
          <p v-if="collectiblesLoading" class="summary-note">수집 정보를 정리하는 중입니다...</p>
          <p v-else-if="collectiblesError" class="summary-note summary-note--warning">
            {{ collectiblesError }}
          </p>
          <div v-else-if="collectionSummary.length" class="summary-progress-list summary-progress-list--dense">
            <div v-for="item in collectionSummary" :key="item.key" class="summary-progress summary-progress--compact">
              <div class="summary-progress__head">
                <p class="summary-title">{{ item.name }}</p>
                <span v-if="item.levelLabel" class="summary-pill summary-pill--ghost">
                  {{ item.levelLabel }}
                </span>
              </div>
              <div class="summary-progress__bar">
                <span :style="{ width: item.percentLabel }"></span>
              </div>
              <p class="summary-progress__meta">{{ item.pointLabel }}</p>
            </div>
          </div>
          <p v-else class="summary-note">표시할 수집 포인트가 없습니다.</p>
        </div>
      </article>
    </div>
    <EmptyState v-else icon="ℹ️" title="캐릭터를 먼저 선택하세요" description="검색 후 내 정보 간소화 탭에서 핵심 정보를 요약해 드립니다." />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LazyImage from './LazyImage.vue'
import EmptyState from './EmptyState.vue'
import type { CharacterProfile } from '@/api/types'
import { getQualityColor } from '@/utils/tooltipParser'
import { getEngravingIcon } from '@/assets/BuffImage'
import { applyEffectAbbreviations } from '@/data/effectAbbreviations'

const props = defineProps<{
  activeCharacter: CharacterProfile | null
  equipmentSummary: any
  detailLoading: boolean
  detailError: string | null
  arkSummary: any
  arkGridLoading: boolean
  arkGridError: string | null
  skillHighlights: any[]
  skillLooseGems?: any[]
  skillLoading: boolean
  skillError: string | null
  engravingSummary: any[]
  collectionSummary: any[]
  collectiblesLoading: boolean
  collectiblesError: string | null
}>()

const equipmentRows = computed(() => {
  const left = props.equipmentSummary?.left || []
  const right = props.equipmentSummary?.right || []
  const max = Math.max(left.length, right.length)
  return Array.from({ length: max }).map((_, index) => ({
    key: `equipment-row-${index}`,
    left: left[index],
    right: right[index]
  }))
})

const qualityBadgeBackground = 'var(--quality-badge-bg, rgba(15, 23, 42, 0.333))'

const qualityStyle = (quality?: number | string) => {
  const num = typeof quality === 'number' ? quality : Number(quality)
  const color = getQualityColor(Number.isFinite(num) ? num : undefined)
  const fallbackText = '#f9fafb'
  return {
    color: color || fallbackText,
    backgroundColor: qualityBadgeBackground,
    borderColor: color ? `${color}55` : 'rgba(255,255,255,0.15)'
  }
}

const engravingIcon = (name?: string) => getEngravingIcon(name || '')

const engravingColor = (grade?: string | null) => {
  const g = (grade || '').toLowerCase()
  if (!g) return ''
  if (g.includes('고대')) return 'var(--rarity-ancient, #eab308)'
  if (g.includes('유물')) return 'var(--rarity-relic, #f97316)'
  if (g.includes('전설')) return 'var(--rarity-legendary, #fbbf24)'
  if (g.includes('영웅')) return 'var(--rarity-heroic, #a78bfa)'
  if (g.includes('희귀')) return 'var(--rarity-rare, #60a5fa)'
  if (g.includes('고급') || g.includes('언커먼')) return 'var(--rarity-uncommon, #6ee7b7)'
  if (g.includes('일반') || g.includes('노말')) return 'var(--text-secondary, #6b7280)'
  return ''
}

const engravingNameMap: Record<string, string> = {
  // 공용 각인
  '원한': '원한',
  '예리한 둔기': '예둔',
  '저주받은 인형': '저받',
  '아드레날린': '아드',
  '돌격대장': '돌대',
  '결투의 대가': '결대',
  '기습의 대가': '기습',
  '타격의 대가': '타대',
  '슈퍼 차지': '슈차',
  '정기 흡수': '정흡',
  '속전속결': '속속',
  '질량 증가': '질증',
  '바리케이드': '바리',
  '구슬동자': '구동',
  '최대 마나 증가': '최마증',
  '안정된 상태': '안정',
  '각성': '각성',
  '중갑 착용': '중갑',
  '강령술': '강령',
  '회심': '회심',
  '공격의 대가': '공대',
  '저지 불가': '저불',
  '부러진 뼈': '부뼈',
  '위기 모면': '위모',
  '정밀 단도': '정단',
  '실드 관통': '실관',
  // 배틀 아이템/각종 유틸
  '마나 효율 증가': '마효증',
  '구슬수급': '구슬',
  // 클래스 각인 (대표)
  '잔재된 기운': '잔재',
  '멈출 수 없는 충동': '충동',
  '절정': '절정',
  '절제': '절제',
  '강화 무기': '강무',
  '오의 강화': '오의',
  '중력 수련': '중수',
  '고독한 기사': '고기',
  '심판자': '심판자',
  '포격 강화': '포강',
  '화력 강화': '화강',
  '수호자': '수호자',
  '전투 태세': '전태',
  '포식자': '포식',
  '두 번째 동료': '두동',
  '죽음의 습격': '죽습',
  '사냥의 시간': '사시',
  '피스메이커': '피메',
  '저주 사슬': '저사',
  '만개': '만개',
  '갈증': '갈증',
  '타격의 대가(격앙)': '타대',
  '황제의 칙령': '황제',
  '황후의 칙령': '황후',
  '상급 소환사': '상소',
  '넘치는 교감': '넘교',
  '진실된 용맹': '진용',
  '전문의': '전문',
  '긴급구조': '긴구',
  '절실한 구원': '절구'
}

const formatEngravingName = (name?: string) => {
  const clean = (name || '').trim()
  if (!clean) return '각인'
  const mapped = engravingNameMap[clean]
  if (mapped) return mapped
  const abbreviated = applyEffectAbbreviations(clean)
  return abbreviated || clean
}

const craftBadgeSrc = (craftLabel?: string) => {
  const levelMatch = craftLabel?.match(/(\d+)/)
  const level = levelMatch?.[1] || '0'
  return `/assets/engraving/level-${level}.png`
}

const craftLevelLabel = (craftLabel?: string) => {
  const levelMatch = craftLabel?.match(/(\d+)/)
  const level = levelMatch?.[1]
  return level ? `Lv.${level}` : craftLabel || ''
}

const engravingLevelBadgeSrc = (levelLabel?: string) => {
  const match = levelLabel?.match(/(\d+)/)
  const level = match?.[1] || '0'
  return `/assets/engraving/level-${level}.png`
}

const gradeColor = (grade?: string | null) => {
  const g = (grade || '').toLowerCase()
  if (!g) return ''
  if (g.includes('고대')) return 'var(--rarity-ancient, #eab308)'
  if (g.includes('유물')) return 'var(--rarity-relic, #f97316)'
  if (g.includes('전설')) return 'var(--rarity-legendary, #fbbf24)'
  if (g.includes('영웅')) return 'var(--rarity-heroic, #a78bfa)'
  if (g.includes('희귀')) return 'var(--rarity-rare, #60a5fa)'
  if (g.includes('고급') || g.includes('언커먼')) return 'var(--rarity-uncommon, #6ee7b7)'
  if (g.includes('일반') || g.includes('노말')) return 'var(--text-secondary, #6b7280)'
  return ''
}

const isTranscendGold = (value?: string | number) => {
  const numeric = Number(String(value ?? '').replace(/,/g, ''))
  return Number.isFinite(numeric) && numeric >= 21
}

const qualityValue = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') return null
  const num = Number(String(value).replace(/[^0-9.\-]/g, ''))
  if (!Number.isFinite(num)) return null
  return Math.max(0, Math.min(100, num))
}

type ProgressTier = 'normal' | 'rare' | 'heroic' | 'legendary'

const PROGRESS_COLORS: Record<ProgressTier, string> = {
  normal: 'rgba(145, 254, 2, 0.16)',
  rare: 'rgba(0, 181, 255, 0.16)',
  heroic: 'rgba(206, 67, 252, 0.16)',
  legendary: 'rgba(254, 150, 0, 0.16)'
}

const qualityTier = (value?: string | number | null): ProgressTier => {
  const q = qualityValue(value) ?? 0
  if (q >= 100) return 'legendary'
  if (q >= 90) return 'heroic'
  if (q >= 70) return 'rare'
  return 'normal'
}

const qualityBarStyle = (value?: string | number | null) => {
  const q = qualityValue(value)
  const percent = q === null ? 0 : q
  return {
    width: `${percent}%`,
    background: PROGRESS_COLORS[qualityTier(value)]
  }
}

const transcendBarStyle = (value?: string | number | null) => {
  if (value === undefined || value === null || value === '') {
    return { width: '0%', background: PROGRESS_COLORS.normal }
  }
  const num = Number(String(value).replace(/[^0-9.\-]/g, ''))
  const clamped = !Number.isFinite(num) ? 0 : Math.max(0, Math.min(21, num))
  const percent = (clamped / 21) * 100
  const tier: ProgressTier =
    clamped >= 21 ? 'legendary' : clamped >= 16 ? 'heroic' : clamped >= 11 ? 'rare' : 'normal'
  return {
    width: `${percent}%`,
    background: PROGRESS_COLORS[tier]
  }
}

const formatGearEnhanceLabel = (item?: any) => {
  if (!item || item.isAccessory || item.isBracelet || item.isAbilityStone) return ''
  const toNumber = (val?: string | number | null) => {
    if (val === null || val === undefined) return null
    const num = Number(String(val).replace(/[^0-9.\-]/g, ''))
    return Number.isFinite(num) ? num : null
  }
  const enhanceNum = toNumber(item.enhancement)
  const harmonyNum = toNumber(item.harmony)
  const harmonyAdjusted = harmonyNum !== null ? (harmonyNum >= 20 ? harmonyNum - 10 : harmonyNum) : null

  const parts: string[] = []
  if (enhanceNum !== null) parts.push(`+${enhanceNum}`)
  parts.push(String(item.typeLabel || '').trim())
  if (harmonyAdjusted !== null) parts.push(`+${harmonyAdjusted}`)

  return parts.filter(Boolean).join(' ')
}

const enhanceHarmonyParts = (enhancement?: string | number, harmony?: string | number) => {
  const toNumber = (val?: string | number | null) => {
    if (val === null || val === undefined) return null
    const num = Number(String(val).replace(/[^0-9.\-]/g, ''))
    return Number.isFinite(num) ? num : null
  }

  const enhanceNum = toNumber(enhancement)
  const harmonyNum = toNumber(harmony)

  const enhanceLabel = enhanceNum !== null ? `+${enhanceNum}` : ''
  const harmonyAdjusted = harmonyNum !== null ? (harmonyNum >= 20 ? harmonyNum - 10 : harmonyNum) : null
  const harmonyLabel = harmonyAdjusted !== null ? `+${harmonyAdjusted}` : ''

  return {
    enhance: enhanceLabel,
    harmony: harmonyLabel,
    hasValue: Boolean(enhanceLabel || harmonyLabel)
  }
}

const effectBg = (color?: string | null) => {
  const cleaned = (color || '').trim()
  const isNeutral =
    !cleaned ||
    /var\(--bg-secondary/i.test(cleaned) ||
    cleaned.toLowerCase() === 'transparent' ||
    cleaned === '#ffffff' ||
    cleaned === '#fff'

  if (!isNeutral) return cleaned
  return 'rgba(148, 163, 184, 0.18)'
}

const effectDisplayColor = (effect: any, item: any) => {
  const direct = (effect?.bgColor || '').trim()
  if (direct) {
    const rgbaMatch = direct.match(/rgba?\s*\(\s*([\d.]+)\s*,\s*([\d.]+)\s*,\s*([\d.]+)(?:\s*,\s*([\d.]+))?\s*\)/i)
    if (rgbaMatch) {
      const r = Number(rgbaMatch[1])
      const g = Number(rgbaMatch[2])
      const b = Number(rgbaMatch[3])
      return `rgb(${r}, ${g}, ${b})`
    }
    return direct
  }
  return 'var(--text-primary)'
}

const effectTierPrefix = (color?: string | null) => {
  const normalized = (color || '').replace(/\s+/g, '').toLowerCase()
  if (normalized.includes('rgb(0,181,255)')) return '하 '
  if (normalized.includes('rgb(206,67,252)')) return '중 '
  if (normalized.includes('rgb(254,150,0)')) return '상 '
  return ''
}

const effectTextDisplayColor = (effect: any, item: any) => {
  const isAccessory = item?.isAccessory && !item?.isBracelet && !item?.isAbilityStone
  if (isAccessory) return 'var(--text-primary)'
  return effectDisplayColor(effect, item)
}

const effectLabelDisplay = (effect: any, item: any) => {
  const isAccessory = item?.isAccessory && !item?.isBracelet && !item?.isAbilityStone
  const prefix = isAccessory ? effectTierPrefix(effectDisplayColor(effect, item)) : ''
  return `${prefix}${effect.label || ''}`.trim()
}

const passiveEffectGroups = computed(() => {
  const typeOrder = ['진화', '깨달음', '도약']
  const normalizeLabel = (label?: string | null) => (label || '').replace(/\s+/g, '')
  const appliedPointValueFor = (label: string) => {
    const normalizedLabel = normalizeLabel(label)
    if (!normalizedLabel) return ''
    const matched = (props.arkSummary?.appliedPoints ?? []).find(point => {
      const normalizedPoint = normalizeLabel(point.label)
      if (!normalizedPoint) return false
      return (
        normalizedPoint === normalizedLabel ||
        normalizedPoint.endsWith(normalizedLabel) ||
        normalizedLabel.endsWith(normalizedPoint)
      )
    })
    return matched?.value || ''
  }
  const effects = (props.arkSummary?.passiveEffects ?? []).map(effect => {
    const tierValueMatch = effect.tierLabel?.match(/(\d+)/)
    const tierValue = tierValueMatch?.[1] ? Number(tierValueMatch[1]) : Number.POSITIVE_INFINITY
    return {
      ...effect,
      typeLabel: effect.typeLabel || '기타',
      tierLabel: effect.tierLabel || '',
      tierValue
    }
  })

  const groupMap = new Map<
    string,
    { key: string; label: string; items: typeof effects; pointLabel?: string }
  >()
  const ensureGroup = (label: string) => {
    const key = label
    if (!groupMap.has(key)) {
      groupMap.set(key, { key, label, items: [] })
    }
    return groupMap.get(key)!
  }

  effects.forEach(effect => {
    const groupLabel = typeOrder.includes(effect.typeLabel) ? effect.typeLabel : '기타'
    ensureGroup(groupLabel).items.push(effect)
  })

  const groups = Array.from(groupMap.values()).sort((a, b) => {
    const ai = typeOrder.indexOf(a.label)
    const bi = typeOrder.indexOf(b.label)
    return (ai === -1 ? 99 : ai) - (bi === -1 ? 99 : bi)
  })

  groups.forEach(group => {
    const pointLabel = appliedPointValueFor(group.label)
    if (pointLabel) {
      group.pointLabel = pointLabel
    }
    group.items.sort((a, b) => a.tierValue - b.tierValue || a.name.localeCompare(b.name))
  })

  return groups
})
</script>
