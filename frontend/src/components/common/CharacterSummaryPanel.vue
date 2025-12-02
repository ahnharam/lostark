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
                    <span v-if="row.right.itemLevel" class="equipment-item-level equipment-item-level--stacked">
                      {{ row.right.itemLevel }}
                    </span>
                    <div v-if="qualityValue(row.right.quality) !== null && Number(row.right.quality) !== -1"
                      class="equipment-quality equipment-quality--stacked">
                      <span class="equipment-progress">
                        <span class="equipment-progress__fill" :style="qualityBarStyle(row.right.quality)"></span>
                        <span class="equipment-progress__label equipment-progress__label--inline">
                          {{ row.right.quality }}
                        </span>
                      </span>
                    </div>
                  </div>
                  <div class="equipment-info-stack">
                    <div v-if="row.right.effects?.length" class="equipment-effect-badges equipment-effect-badges--grid">
                      <span v-for="(effect, idx) in effectsForDisplay(row.right.effects, row.right)"
                        :key="`effect-${row.key}-${idx}`" class="bracelet-badge bracelet-badge--effect"
                        :style="{ backgroundColor: 'transparent', color: braceletEffectParts(effect, row.right).nameColor }">
                        <template v-if="braceletEffectParts(effect, row.right).prefix">
                          <span class="bracelet-effect-prefix"
                            :style="{ color: effectDisplayColor(effect, row.right) }">
                            {{ braceletEffectParts(effect, row.right).prefix }}
                          </span>
                        </template>
                        <span class="bracelet-effect-name" :style="{ fontWeight: effectFontWeight(effect, row.right) }">
                          <span v-if="braceletEffectParts(effect, row.right).richLabel"
                            v-html="braceletEffectParts(effect, row.right).richLabel"></span>
                          <template v-else>
                            <template v-for="(segment, segIdx) in braceletEffectParts(effect, row.right).labelSegments"
                              :key="`bracelet-inline-seg-${row.key}-${idx}-${segIdx}`">
                              <span v-if="segment.isValue" class="bracelet-effect-value"
                                :style="{ color: braceletEffectParts(effect, row.right).valueColor, fontWeight: 700 }">
                                {{ segment.text }}
                              </span>
                              <span v-else>
                                {{ segment.text }}
                              </span>
                            </template>
                          </template>
                        </span>
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
                      <div v-if="qualityValue(row.left.quality) !== null && Number(row.left.quality) !== -1"
                        class="equipment-quality equipment-quality--stacked">
                        <span class="equipment-progress">
                          <span class="equipment-progress__fill" :style="qualityBarStyle(row.left.quality)"></span>
                          <span class="equipment-progress__label equipment-progress__label--inline">
                            {{ row.left.quality }}
                          </span>
                        </span>
                      </div>
                    </div>
                    <div class="equipment-info-stack">
                      <span v-if="row.left.itemLevel" class="equipment-item-level equipment-item-level--inline">
                        {{ row.left.itemLevel }}
                      </span>
                      <p v-if="gearEnhanceParts(row.left).hasValue" class="equipment-line equipment-line--primary">
                        <span v-if="gearEnhanceParts(row.left).typeLabel" class="gear-enhance-part">
                          {{ gearEnhanceParts(row.left).typeLabel }}
                        </span>
                        <span v-if="gearEnhanceParts(row.left).enhanceLabel" class="gear-enhance-part">
                          {{ gearEnhanceParts(row.left).enhanceLabel }}
                        </span>
                        <span v-if="gearEnhanceParts(row.left).harmonyLabel"
                          class="gear-enhance-part gear-enhance-part--harmony">
                          {{ gearEnhanceParts(row.left).harmonyLabel }}
                        </span>
                      </p>
                      <div v-if="row.left.transcend && Number(row.left.transcend) !== -1"
                        class="equipment-line equipment-line--transcend">
                        <!-- <span class="equipment-progress equipment-progress--transcend equipment-progress--bare">
                          <span class="equipment-progress__fill" :style="transcendBarStyle(row.left.transcend)"></span>
                          <span class="equipment-progress__label equipment-progress__label--inline equipment-progress__label--transcend">
                          </span>
                        </span> -->
                        <span class="transcend-icon transcend-icon--inline"
                          :class="{ 'transcend-icon--gold': isTranscendGold(row.left.transcend) }"
                          aria-hidden="true"></span>
                        <span class="equipment-transcend-value">{{ row.left.transcend }}</span>
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
                      <span v-if="row.right.itemLevel" class="equipment-item-level equipment-item-level--stacked">
                        {{ row.right.itemLevel }}
                      </span>
                      <div v-if="qualityValue(row.right.quality) !== null && Number(row.right.quality) !== -1"
                        class="equipment-quality equipment-quality--stacked">
                        <span class="equipment-progress">
                          <span class="equipment-progress__fill" :style="qualityBarStyle(row.right.quality)"></span>
                          <span class="equipment-progress__label equipment-progress__label--inline">
                            {{ row.right.quality }}
                          </span>
                        </span>
                      </div>
                    </div>
                    <div class="equipment-info-stack">
                      <p v-if="gearEnhanceParts(row.right).hasValue" class="equipment-line equipment-line--primary">
                        <span v-if="gearEnhanceParts(row.right).typeLabel" class="gear-enhance-part">
                          {{ gearEnhanceParts(row.right).typeLabel }}
                        </span>
                        <span v-if="gearEnhanceParts(row.right).enhanceLabel" class="gear-enhance-part">
                          {{ gearEnhanceParts(row.right).enhanceLabel }}
                        </span>
                        <span v-if="gearEnhanceParts(row.right).harmonyLabel"
                          class="gear-enhance-part gear-enhance-part--harmony">
                          {{ gearEnhanceParts(row.right).harmonyLabel }}
                        </span>
                      </p>
                      <div v-if="row.right.transcend && Number(row.right.transcend) !== -1"
                        class="equipment-line equipment-line--transcend">
                        <span class="equipment-progress equipment-progress--transcend equipment-progress--bare">
                          <span class="equipment-progress__fill" :style="transcendBarStyle(row.right.transcend)"></span>
                          <span
                            class="equipment-progress__label equipment-progress__label--inline equipment-progress__label--transcend">
                            <span class="transcend-icon transcend-icon--inline"
                              :class="{ 'transcend-icon--gold': isTranscendGold(row.right.transcend) }"
                              aria-hidden="true"></span>
                            <span class="equipment-transcend-value">{{ row.right.transcend }}</span>
                          </span>
                        </span>
                      </div>
                      <div v-if="row.right.effects?.length"
                        class="equipment-effect-badges equipment-effect-badges--with-tooltip" :class="{
                          'equipment-effect-badges--grid': row.right.isBracelet,
                          'equipment-effect-badges--stone': row.right.isAbilityStone
                        }">
                        <div v-for="(effect, idx) in effectsForDisplay(row.right.effects, row.right)"
                          :key="`effect-${row.key}-${idx}`" class="equipment-effect-chip" :class="{
                            'equipment-badge--combat': effect.isCombat,
                            'equipment-badge--fullrow': !effect.isCombat && row.right.isBracelet
                          }">
                          <span v-if="row.right.isBracelet" class="bracelet-badge bracelet-badge--effect"
                            :style="{ backgroundColor: 'transparent', color: braceletEffectParts(effect, row.right).nameColor }">
                            <template v-if="braceletEffectParts(effect, row.right).prefix">
                              <span class="bracelet-effect-prefix"
                                :style="{ color: effectDisplayColor(effect, row.right) }">
                                {{ braceletEffectParts(effect, row.right).prefix }}
                              </span>
                            </template>
                            <span class="bracelet-effect-name"
                              :style="{ fontWeight: effectFontWeight(effect, row.right) }">
                              <template
                                v-for="(segment, segIdx) in braceletEffectParts(effect, row.right).labelSegments"
                                :key="`bracelet-seg-${row.key}-${idx}-${segIdx}`">
                                <span v-if="segment.isValue" class="bracelet-effect-value"
                                  :style="{ color: braceletEffectParts(effect, row.right).valueColor, fontWeight: 700 }">
                                  {{ segment.text }}
                                </span>
                                <span v-else>
                                  {{ segment.text }}
                                </span>
                              </template>
                            </span>
                          </span>
                          <span v-else class="equipment-badge equipment-badge--effect" :class="{
                            'equipment-badge--combat': effect.isCombat,
                            'equipment-badge--fullrow': !effect.isCombat && row.right.isBracelet
                          }"
                            :style="{ backgroundColor: 'transparent', color: effectTextDisplayColor(effect, row.right) }">
                            <span v-if="isAccessoryItem(row.right)" class="effect-prefix"
                              :class="{ 'effect-prefix--empty': !effectPrefixLabel(effect, row.right) }"
                              :style="{ color: effectDisplayColor(effect, row.right) }">
                              {{ effectPrefixLabel(effect, row.right) || '무' }}
                            </span>
                            <span :style="{ fontWeight: effectFontWeight(effect, row.right) }">
                              {{ effectDisplayLabel(effect, row.right) }}
                            </span>
                          </span>
                        </div>
                        <div v-if="row.right.isAccessory && !row.right.isBracelet"
                          class="popup-surface popup-surface--tooltip equipment-effect-tooltip">
                          <p v-for="(effLine, effIdx) in effectsForDisplay(row.right.effects, row.right)"
                            :key="`acc-tooltip-${row.key}-${effIdx}`" class="popup-surface__body">
                            {{ expandEffectTooltipText(effLine.full || effLine.label) || effLine.full || effLine.label
                            }}
                          </p>
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
            <div v-if="(arkSummary.passiveMatrix?.length || 0) > 0 || (arkSummary.appliedPoints?.length || 0) > 0"
              class="ark-passive-summary">
              <div class="ark-passive-grid">
                <div class="ark-passive-grid-header">
                  <span v-for="point in arkSummary.appliedPoints" :key="point.key" class="ark-passive-header-cell">
                    <span class="ark-passive-header-desc" v-if="point.description">{{ point.description }}</span>
                    <div class="ark-passive-header-title">
                      <span class="ark-passive-header-label">{{ point.label }}</span>
                      <span v-if="point.value"> · </span>
                      <span class="ark-passive-header-value" v-if="point.value">{{ point.value }}</span>
                    </div>
                  </span>
                </div>
                <div v-for="(row, rowIndex) in arkSummary.passiveMatrix.slice(0, 4)" :key="row.id"
                  class="ark-passive-grid-row" :class="{ 'ark-passive-grid-row--with-divider': rowIndex > 0 }">
                  <div v-for="section in row.sections" :key="`${row.id}-${section.key}`" class="ark-passive-cell">
                    <div v-if="section.effects.length" class="ark-passive-cell-list">
                      <div v-for="effect in section.effects" :key="effect.key" class="ark-passive-chip">
                        <div class="ark-passive-chip-visual">
                          <LazyImage v-if="effect.icon" :src="effect.icon" :alt="effect.name" width="30" height="30"
                            imageClass="summary-icon" errorIcon="🌟" :useProxy="true" />
                          <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">★</div>
                        </div>
                        <div class="ark-passive-chip-labels">
                          <span v-if="effect.levelDisplay || effect.levelLine" class="ark-passive-chip-level">
                            {{ effect.levelDisplay || effect.levelLine }}
                          </span>
                          <span class="ark-passive-chip-name">
                            {{ effect.name }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <p v-else class="summary-note"> </p>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="summary-note">패시브 정보가 없습니다.</div>
          </div>

          <div class="ark-section__block">
            <div class="summary-card__head">
              <p class="summary-eyebrow">아크 그리드</p>
            </div>
            <p v-if="arkGridLoading" class="summary-note">아크 그리드 정보를 불러오는 중입니다...</p>
            <p v-else-if="arkGridError" class="summary-note summary-note--warning">{{ arkGridError }}</p>
            <div v-else class="ark-core-layout">
              <div v-if="arkSummary.coreMatrix.rows.length" class="ark-core-card-grid">
                <div v-for="row in arkSummary.coreMatrix.rows" :key="`core-row-${row.key}`" class="ark-core-row-group">
                  <p class="ark-core-row-label">{{ row.label }}</p>
                  <div class="ark-core-card-row">
                    <div v-for="cell in row.cells" :key="`core-cell-${cell.key}`" class="ark-core-card-cell">
                      <article v-for="slot in cell.slots" :key="slot.key" class="ark-core-card">
                        <div class="ark-core-card__thumb">
                          <LazyImage v-if="slot.icon" :src="slot.icon" :alt="slot.name" width="40" height="40"
                            imageClass="ark-core-card__image" errorIcon="🧩" :useProxy="true" />
                          <div v-else class="ark-core-card__placeholder" aria-hidden="true">
                            {{ slot.initial }}
                          </div>
                        </div>
                        <div class="ark-core-card__body">
                          <p class="ark-core-card__name" :style="coreNameStyle(slot)">{{ slot.name }}</p>
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
          <ul v-else-if="displaySkillHighlights.length" class="summary-list summary-list--flat summary-skill-list">
            <li v-for="skill in displaySkillHighlights" :key="skill.key"
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
                  <span v-for="(tripod, index) in skill.tripods" :key="tripod.key" class="summary-tripod-slot-dot"
                    :class="`summary-tripod-icon--${index + 1}`">{{ tripod.slotLabel }}</span>
                </div>
              </div>
              <div v-if="skill.hasGem || skill.rune" class="summary-skill-gems">
                <div class="summary-gem-grid">
                  <div>
                    <div class="summary-gem-row summary-gem-row--label">
                      <span class="summary-gem-label" :title="skill.rune?.name || ''"
                        :style="{ color: skill.rune?.color || undefined }">
                        {{ skill.rune?.name || '' }}
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
          <!-- <p v-else class="summary-note">요약할 스킬 정보가 없습니다.</p> -->
        </div>
        <div class="ark-section__block ark-section__block--engravings">
          <div class="summary-card__head">
            <p class="summary-eyebrow">각인</p>
          </div>
          <div v-if="engravingSummary.length" class="summary-engraving-grid">
            <div v-for="engrave in engravingSummary" :key="engrave.key" class="summary-engraving-card">
              <div class="summary-engraving-icon">
                <LazyImage v-if="engrave.icon || engravingIcon(engrave.name)"
                  :src="engrave.icon || engravingIcon(engrave.name)" :alt="engrave.displayName || engrave.name"
                  width="40" height="40" imageClass="summary-icon" errorIcon="🔮" :useProxy="true" />
                <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">
                  {{ engrave.gradeLabel?.[0] || 'E' }}
                </div>
              </div>
              <p class="summary-title summary-engraving-name" :style="{ color: engravingColor(engrave.gradeLabel) }">
                {{ engrave.displayName || formatEngravingName(engrave.name) }}
              </p>
              <div class="summary-engrave-meta-row">
                <span v-if="engrave.levelLabel" class="engrave-level-image engrave-level-image--sprite"
                  :style="engravingLevelBadgeStyle(engrave)"></span>
                <span v-if="engrave.levelLabel" class="summary-pill summary-pill--primary">
                  {{ engrave.levelLabel }}
                </span>
              </div>
              <div class="summary-engrave-meta-row">
                <div v-if="engrave.craftLabel" class="engrave-craft-badge">
                  <span class="engrave-level-image engrave-level-image--sprite" :style="CRAFT_BADGE_STYLE"></span>
                  <span class="engrave-craft-text">{{ craftLevelLabel(engrave.craftLabel) }}</span>
                </div>
              </div>
            </div>
          </div>
          <p v-else class="summary-note">각인 정보가 없습니다.</p>
        </div>

      </article>

      <article class="summary-card summary-card--module summary-card--cards">
        <div class="ark-section__block ark-section__block--cards">
          <div class="summary-card__head card-head">
            <div class="card-head__title">
              <p class="summary-eyebrow">카드</p>
            </div>
            <div v-if="cardSummary?.effects?.length" class="card-head__effects">
              <div v-for="effect in cardSummary.effects" :key="effect.key" class="card-head__effect-chip">
                <span class="card-head__effect-name">{{ effect.label }}</span>
                <!-- <span v-if="effect.setLabel" class="card-head__effect-meta">{{ effect.setLabel }}</span> -->
              </div>
            </div>
          </div>
          <p v-if="cardLoading" class="summary-note">카드 정보를 불러오는 중입니다...</p>
          <p v-else-if="cardError" class="summary-note summary-note--warning">{{ cardError }}</p>
          <div v-else class="card-strip-shell" :class="{ 'card-strip-shell--empty': !cardSummary?.cards?.length }">
            <div v-if="cardSummary?.cards?.length" class="card-strip">
              <article v-for="card in cardSummary.cards" :key="card.key" class="card-slot card-slot--ornate">
                <div class="card-slot__frame">
                  <div class="card-slot__thumb">
                    <LazyImage v-if="card.icon" :src="card.icon" :alt="card.name" :width="`inherit - 10px`"
                      :height="`inherit - 10px`" imageClass="card-slot__icon" errorIcon="✨" :useProxy="true" />
                    <div v-else class="card-slot__placeholder" aria-hidden="true">
                      {{ card.name?.[0] || '?' }}
                    </div>
                  </div>
                  <div class="card-slot__awake-row" v-if="card.awakeTotal">
                    <span v-for="orbIndex in card.awakeTotal" :key="`${card.key}-orb-${orbIndex}`"
                      class="card-awake-orb" :style="awakeOrbSize">
                      <span class="card-awake-orb__layer card-awake-orb__layer--slot"
                        :style="awakeSpriteStyle(orbIndex, 'slot')"></span>
                      <span v-if="card.awakeCount !== null && orbIndex <= card.awakeCount"
                        class="card-awake-orb__layer card-awake-orb__layer--fill"
                        :style="awakeSpriteStyle(orbIndex, 'fill')"></span>
                    </span>
                  </div>
                </div>
                <div class="card-slot__body">
                  <p class="card-slot__name">{{ card.name }}</p>
                </div>
              </article>
            </div>
            <p v-else class="summary-note">카드 정보가 없습니다.</p>
          </div>
        </div>
      </article>
      <article class="summary-card summary-card--module summary-card--collection">
        <div class="ark-section__block">
          <div class="summary-card__head">
            <p class="summary-eyebrow">수집</p>
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
import { extractTooltipColor } from '@/utils/tooltipText'
import { getEngravingIcon } from '@/assets/BuffImage'
import { getEngravingDisplayName, ENGRAVING_NAME_ENTRIES } from '@/data/engravingNames'
import {
  applyEffectAbbreviations,
  hasAbbreviationMatch,
  EFFECT_ABBREVIATION_REPLACEMENTS,
  abbreviationCategory,
  expandDemeritAbbreviation,
  DEALER_ABBREVIATIONS,
  SUPPORT_ABBREVIATIONS
} from '@/data/effectAbbreviations'

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
  cardSummary: any
  cardLoading: boolean
  cardError: string | null
  collectionSummary: any[]
  collectiblesLoading: boolean
  collectiblesError: string | null
  combatRole?: 'dealer' | 'support' | null
}>()

const displaySkillHighlights = computed(() => {
  const base = props.skillHighlights || []
  const loose = (props.skillLooseGems || []).map(gem => {
    const name = gem.skillName || gem.name || '보석'
    const levelLabel = gem.levelLabel || ''
    const label = gem.effectLabel || gem.typeLabel || ''
    return {
      key: gem.key || `gem-only-${name}`,
      name,
      icon: gem.icon || '',
      levelLabel: '',
      pointLabel: '',
      rune: null,
      tripods: [],
      gems: {
        damage: label
          ? { label, levelLabel }
          : null,
        cooldown: null
      },
      hasGem: true
    }
  })
  return [...base, ...loose]
})

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

const formatEngravingName = (name?: string) => {
  const display = getEngravingDisplayName(name)
  if (display) return display
  const clean = (name || '').trim()
  const abbreviated = applyEffectAbbreviations(clean)
  return abbreviated || clean || '각인'
}

const craftLevelLabel = (craftLabel?: string) => {
  const levelMatch = craftLabel?.match(/(\d+)/)
  const level = levelMatch?.[1]
  return level ? `Lv.${level}` : craftLabel || ''
}

const ENGRAVING_LEVEL_SPRITE =
  'https://cdn-lostark.game.onstove.com/2018/obt/assets/images/pc/profile/img_engrave_icon.png?3e9f6d074e03983e7d45'

type EngravingBadgeSlice = 'stone' | 'default' | 'legendary' | 'heroic' | 'relic'

const engravingBadgeSlice = (engrave: any): EngravingBadgeSlice => {
  if (typeof engrave?.abilityStoneLevel === 'number') return 'stone'
  const grade = (engrave?.gradeLabel || engrave?.grade || '').toLowerCase()
  if (grade.includes('유물')) return 'relic'
  if (grade.includes('영웅')) return 'heroic'
  if (grade.includes('전설')) return 'legendary'
  return 'default'
}

const engravingLevelBadgeStyle = (engrave: any) => {
  const slice = engravingBadgeSlice(engrave)
  const positionMap: Record<EngravingBadgeSlice, string> = {
    stone: '0% 0%',
    default: '25% 0%',
    legendary: '50% 0%',
    heroic: '75% 0%',
    relic: '100% 0%'
  }
  return {
    backgroundImage: `url('${ENGRAVING_LEVEL_SPRITE}')`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: '500% 100%',
    backgroundPosition: positionMap[slice]
  }
}

const CRAFT_BADGE_STYLE = {
  backgroundImage: `url('${ENGRAVING_LEVEL_SPRITE}')`,
  backgroundRepeat: 'no-repeat',
  backgroundSize: '500% 100%',
  backgroundPosition: '-3% 0%'
}

const CARD_AWAKE_SPRITE =
  'https://cdn-lostark.game.onstove.com/2018/obt/assets/images/pc/profile/img_profile_awake.png?565e26c78052c03041e3'
const AWAKE_SPRITE_WIDTH = 80
const AWAKE_SPRITE_HEIGHT = 60
const AWAKE_FRAME_COUNT = 5
const AWAKE_FRAME_WIDTH = AWAKE_SPRITE_WIDTH / AWAKE_FRAME_COUNT
const AWAKE_ROW_HEIGHT = AWAKE_SPRITE_HEIGHT / 2
const awakeOrbSize = {
  width: `${AWAKE_FRAME_WIDTH}px`,
  height: `${AWAKE_ROW_HEIGHT}px`
}

const awakeSpriteStyle = (index: number, layer: 'slot' | 'fill') => {
  const frameIndex = Math.max(0, Math.min(AWAKE_FRAME_COUNT - 1, index - 1))
  const x = -frameIndex * AWAKE_FRAME_WIDTH
  const y = layer === 'slot' ? 0 : -AWAKE_ROW_HEIGHT
  return {
    width: `${AWAKE_FRAME_WIDTH}px`,
    height: `${AWAKE_ROW_HEIGHT}px`,
    backgroundImage: `url('${CARD_AWAKE_SPRITE}')`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: `${AWAKE_SPRITE_WIDTH}px ${AWAKE_SPRITE_HEIGHT}px`,
    backgroundPosition: `${x}px ${y}px`
  }
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
  parts.push(String(item.typeLabel || '').trim())
  if (enhanceNum !== null) parts.push(`+${enhanceNum}`)
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

const gearEnhanceParts = (item?: any) => {
  if (!item || item.isAccessory || item.isBracelet || item.isAbilityStone) {
    return { typeLabel: '', enhanceLabel: '', harmonyLabel: '', hasValue: false }
  }
  const typeLabel = String(item.typeLabel ?? '').trim()
  const { enhance: enhanceLabel, harmony: harmonyLabel, hasValue } = enhanceHarmonyParts(
    item.enhancement,
    item.harmony
  )
  return {
    typeLabel,
    enhanceLabel,
    harmonyLabel,
    hasValue: Boolean(typeLabel || hasValue)
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
  if (item?.isAbilityStone) return 'var(--text-primary)'
  const source = (effect?.bgColor || '').trim()
  if (source) {
    const rgbaMatch = source.match(/rgba?\s*\(\s*([\d.]+)\s*,\s*([\d.]+)\s*,\s*([\d.]+)(?:\s*,\s*([\d.]+))?\s*\)/i)
    if (rgbaMatch) {
      const r = Number(rgbaMatch[1])
      const g = Number(rgbaMatch[2])
      const b = Number(rgbaMatch[3])
      return `rgb(${r}, ${g}, ${b})`
    }
    return source
  }
  return 'var(--text-primary)'
}

const parseRgbColor = (value?: string | null) => {
  const color = (value || '').trim()
  if (!color) return null

  const rgbaMatch = color.match(/rgba?\s*\(\s*([\d.]+)\s*,\s*([\d.]+)\s*,\s*([\d.]+)/i)
  if (rgbaMatch) {
    return {
      r: Number(rgbaMatch[1]),
      g: Number(rgbaMatch[2]),
      b: Number(rgbaMatch[3])
    }
  }

  const hexMatch = color.match(/^#?([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$/i)
  if (hexMatch) {
    const hex = hexMatch[1]
    const toChannel = (chunk: string) => parseInt(chunk.length === 1 ? chunk.repeat(2) : chunk, 16)

    if (hex.length === 3 || hex.length === 4) {
      return {
        r: toChannel(hex[0]),
        g: toChannel(hex[1]),
        b: toChannel(hex[2])
      }
    }

    return {
      r: toChannel(hex.slice(0, 2)),
      g: toChannel(hex.slice(2, 4)),
      b: toChannel(hex.slice(4, 6))
    }
  }

  return null
}

const effectTierPrefix = (color?: string | null) => {
  const rgb = parseRgbColor(color)
  if (!rgb) return ''

  const tiers = [
    { label: '하 ', r: 0, g: 181, b: 255 },
    { label: '중 ', r: 206, g: 67, b: 252 },
    { label: '상 ', r: 254, g: 150, b: 0 }
  ]

  const distance = (a: typeof rgb, b: typeof rgb) => {
    const dr = a.r - b.r
    const dg = a.g - b.g
    const db = a.b - b.b
    return Math.sqrt(dr * dr + dg * dg + db * db)
  }

  const closest = tiers
    .map(tier => ({ tier, dist: distance(rgb, tier) }))
    .sort((a, b) => a.dist - b.dist)[0]

  // 색상이 너무 멀면 (예: 폰트 색상만 있고 등급색이 아닌 경우) 접두사 표시 안 함
  const MAX_DISTANCE = 40
  if (!closest || closest.dist > MAX_DISTANCE) return ''

  return closest.tier.label
}

const effectLabelText = (effect: any) => effect?.label || ''

const effectHasPercent = (effect: any) => /%/.test(effectLabelText(effect))

const accessoryDisplayLabel = (effect: any, item: any) => {
  if (!isAccessoryItem(item)) return effectLabelText(effect)
  const raw = effectLabelText(effect)
  const hasPercent = /%/.test(raw)
  const cleaned = raw.replace(/[+]/g, '').replace(/[\d.,]+%?/g, '').trim()
  if (!cleaned) return '잡옵'
  // 1) check raw/cleaned against rules
  const isKnownAbbrev = EFFECT_ABBREVIATION_REPLACEMENTS.includes(cleaned)
  const hasRule = isKnownAbbrev || hasAbbreviationMatch(cleaned) || hasAbbreviationMatch(raw)
  if (!hasRule) return '잡옵'
  // 2) apply abbreviation once
  const abbreviated = applyEffectAbbreviations(cleaned)
  const base = abbreviated || cleaned
  // 3) render
  if (hasPercent) return `${base}${base.endsWith('%') ? '' : ' %'}`
  return base
}

const effectDisplayLabel = (effect: any, item: any) =>
  isAccessoryItem(item) ? accessoryDisplayLabel(effect, item) : effectLabelText(effect)

const normalizeRoleLabel = (value?: string | null) => (value || '').toLowerCase().trim()

const detectEffectRole = (effect: any, item: any): 'dealer' | 'support' | null => {
  if (!isAccessoryItem(item)) return null
  const rawLabel = effectDisplayLabel(effect, item)
  const hasPercent = /%/.test(rawLabel)
  const label = normalizeRoleLabel(rawLabel).replace(/[\d.+%\s]/g, '')
  if (!label) return null
  // 무공 % 만 볼드 처리: 퍼센트가 없으면 무시
  if (hasPercent && label.includes('무공')) {
    const combatRole = normalizeRoleLabel(props.combatRole)
    if (combatRole === 'support') return 'support'
    if (combatRole === 'dealer') return 'dealer'
  }
  // 아공강/아피강은 서폿 전용
  if (label.includes('아공강') || label.includes('아피강')) {
    return 'support'
  }
  // 딜러/서폿 약어는 퍼센트가 있어야만 볼드 판정
  if (hasPercent && DEALER_ABBREVIATIONS.some(keyword => label.includes(normalizeRoleLabel(keyword)))) return 'dealer'
  if (hasPercent && SUPPORT_ABBREVIATIONS.some(keyword => label.includes(normalizeRoleLabel(keyword)))) return 'support'
  return null
}

const effectFontWeight = (effect: any, item: any) => {
  if (!isAccessoryItem(item)) return undefined
  const display = effectDisplayLabel(effect, item)
  if (display === '잡옵') return 400
  const role = detectEffectRole(effect, item)
  const combatRole = normalizeRoleLabel(props.combatRole)
  if (role && combatRole) {
    return role === combatRole ? 700 : 400
  }
  // 역할을 특정하지 못한 장신구 옵션은 굵게 표시하지 않음
  return 400
}

const isAccessoryItem = (item?: any) =>
  Boolean(item?.isAccessory && !item?.isBracelet && !item?.isAbilityStone)

const BRACELET_COMBAT_STATS = ['치명', '특화', '제압', '신속', '인내', '숙련']

const isBraceletCombatStat = (label?: string) => {
  const normalized = (label || '').replace(/\s+/g, '')
  // 전투 특성 단일 라벨(또는 숫자/기호와 함께 시작)만 전투 특성으로 취급
  return BRACELET_COMBAT_STATS.some(
    stat => new RegExp(`^${stat}(\\+|\\s|$)`).test(normalized)
  )
}

const effectPrefixLabel = (effect: any, item: any) => {
  if (!isAccessoryItem(item)) return ''
  const prefix = effectTierPrefix(effectDisplayColor(effect, item))
  return prefix.trim()
}

const effectTextDisplayColor = (effect: any, item: any) => {
  if (isAccessoryItem(item)) return 'var(--text-primary)'
  return effectDisplayColor(effect, item)
}

const normalizeCategoryLabel = (value?: string) =>
  (value || '').replace(/\s+/g, '').replace(/[+]/g, '').toLowerCase()

const DEALER_SUPPORT_KEYWORDS = ['적주피', '추피', '치피', '치적', '낙', '아덴', '보호막', '회복', '아공강', '아피강']
const COMMON_PERCENT_KEYWORDS = ['공%', '무공%', '공격력%', '무기공격력%']
const COMMON_FLAT_KEYWORDS = ['무공', '공', '공격력', '무기공격력']

const effectCategoryPriority = (effect: any, item: any) => {
  const display = effectDisplayLabel(effect, item)
  if (display === '잡옵') return 3
  const raw = normalizeCategoryLabel(`${effect?.full || ''}${effect?.label || ''}${display}`)
  if (!raw) return 2
  const hasPercent = /%/.test(raw)
  if (DEALER_SUPPORT_KEYWORDS.some(key => raw.includes(key))) return 0
  if (hasPercent && COMMON_PERCENT_KEYWORDS.some(key => raw.includes(key))) return 1
  if (!hasPercent && COMMON_FLAT_KEYWORDS.some(key => raw.includes(key))) return 2
  if (hasAbbreviationMatch(raw)) return 2
  return 2
}

const effectTierPriority = (effect: any, item: any) => {
  const prefix = effectPrefixLabel(effect, item)
  if (prefix.startsWith('상')) return 0
  if (prefix.startsWith('중')) return 1
  if (prefix.startsWith('하')) return 2
  return 3
}

const sortedEffects = (effects: any[] = [], item: any) =>
  effects
    .slice()
    .sort((a, b) => {
      const catDiff = effectCategoryPriority(a, item) - effectCategoryPriority(b, item)
      if (catDiff !== 0) return catDiff
      const tierDiff = effectTierPriority(a, item) - effectTierPriority(b, item)
      if (tierDiff !== 0) return tierDiff
      return effectDisplayLabel(a, item).localeCompare(effectDisplayLabel(b, item))
    })

const effectsForDisplay = (effects?: any[], item?: any) => {
  if (!effects) return []
  if (item?.isBracelet || item?.isAbilityStone) return effects
  return sortedEffects(effects, item)
}

const braceletEffectParts = (effect: any, item: any) => {
  const rawLabel = effect.full || effect.label || effectDisplayLabel(effect, item) || ''
  const baseLabel = rawLabel.replace(/<[^>]+>/g, '')
  const prefix = isBraceletCombatStat(baseLabel) ? '' : effectTierPrefix(effectDisplayColor(effect, item)).trim()
  const labelSegments = baseLabel
    .split(/([+-]?\d[\d.,]*\s*%?)/g)
    .filter(Boolean)
    .map(part => ({
      text: part.trim(),
      isValue: /[0-9]/.test(part)
    }))
  if (!labelSegments.length && baseLabel) labelSegments.push({ text: baseLabel, isValue: false })
  return {
    labelSegments,
    prefix,
    valueColor: effectDisplayColor(effect, item),
    richLabel: effect.richLabel,
    nameColor: 'var(--text-primary)'
  }
}

const engravingFullByShort = new Map(ENGRAVING_NAME_ENTRIES.map(entry => [entry.short, entry.full]))
const expandEngravingBracket = (text?: string) => {
  if (!text) return ''
  return text.replace(/\[([^\]]+)\]/g, (_, inner) => {
    const full = engravingFullByShort.get(inner.trim()) || inner.trim()
    return `[${full}]`
  })
}

const expandEffectTooltipText = (text?: string) => {
  const base = expandEngravingBracket(text)
  return expandDemeritAbbreviation(base)
}

const normalizeHexColor = (value?: string | null) => {
  if (!value) return ''
  const normalized = String(value).trim().toUpperCase()
  return normalized.startsWith('#') ? normalized : `#${normalized}`
}

const extractColorFromFontString = (text?: string | null) => {
  if (!text) return ''
  const match = String(text).match(/color=['"]?([#\w]+)['"]?/i)
  const raw = match?.[1]
  return raw ? normalizeHexColor(raw) : ''
}

const scanTooltipForCoreColor = (node: unknown): string => {
  if (!node) return ''
  if (typeof node === 'string') return extractColorFromFontString(node)
  if (Array.isArray(node)) {
    for (const item of node) {
      const found = scanTooltipForCoreColor(item)
      if (found) return found
    }
    return ''
  }
  if (typeof node === 'object') {
    const record = node as Record<string, unknown>
    if (record.Element_000 && typeof record.Element_000 === 'object') {
      const valueField = (record.Element_000 as any).value
      const fromElement = scanTooltipForCoreColor(valueField)
      if (fromElement) return fromElement
    }
    for (const child of Object.values(record)) {
      const found = scanTooltipForCoreColor(child)
      if (found) return found
    }
  }
  return ''
}

const coreNameColorFromTooltip = (tooltip?: unknown) => {
  if (!tooltip) return ''
  if (typeof tooltip === 'string') {
    try {
      const parsed = JSON.parse(tooltip)
      const fromParsed = scanTooltipForCoreColor(parsed)
      if (fromParsed) return fromParsed
    } catch {
      // ignore parse error and keep scanning
    }
    const direct = extractColorFromFontString(tooltip)
    if (direct) return direct
    return scanTooltipForCoreColor(tooltip)
  }
  return scanTooltipForCoreColor(tooltip)
}

const coreNameStyle = (slot: any) => {
  const gradeText = slot?.grade || slot?.gradeLabel || slot?.gradeName
  const color =
    slot?.nameColor ||
    coreNameColorFromTooltip(slot?.tooltip) ||
    slot?.gradeColor ||
    extractTooltipColor(slot?.tooltip) ||
    engravingColor(gradeText) ||
    ''
  return color ? { color } : undefined
}

</script>

<style scoped>
.equipment-item-level--stacked {
  margin-top: 2px;
}

.equipment-quality--stacked {
  width: 100%;
  display: flex;
  justify-content: center;
}

.equipment-quality--stacked .equipment-progress {
  width: 40px;
  min-width: 40px;
  flex: 0 0 40px;
}

.equipment-progress--bare {
  background: transparent;
}

.equipment-quality--stacked .equipment-progress__label--inline {
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: var(--font-xxs);
}

.equipment-progress__label--transcend {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: var(--font-xs);
}

.gear-enhance-part {
  margin-right: 6px;
  font-weight: 700;
}

.gear-enhance-part--harmony {
  color: var(--rarity-legendary, #fbbf24);
}

.equipment-transcend-value {
  font-weight: 700;
}

.equipment-effect-badges {
  position: relative;
}

.equipment-effect-badges--stone .equipment-effect-chip,
.equipment-effect-badges--stone .equipment-badge {
  font-weight: 400;
}

.equipment-effect-tooltip {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  z-index: 50;
  left: 0;
  top: calc(100% + 6px);
  transform: translateY(4px);
  transition: opacity 0.15s ease, transform 0.15s ease;
  min-width: 220px;
}

.equipment-effect-badges--with-tooltip:hover .equipment-effect-tooltip {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
}

.bracelet-effect-prefix {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 1px 4px;
  margin-right: 4px;
  border: 1px solid currentColor;
  border-radius: 6px;
  font-weight: 700;
  width: 21px;
  height: 21px;
}

.bracelet-effect-name {
  margin-right: 4px;
}

.bracelet-effect-value {
  font-weight: 700;
  margin-left: 1px;
  margin-right: 1px;
  /* background-color: lightgray; */
  /* text-shadow: 0 1px 1px white; */
  /* text-shadow:0px 1px black; */
}

.bracelet-effect-sep {
  margin-right: 2px;
}

.ark-passive-header-title {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}

.ark-passive-header-value {
  color: var(--primary-color);
  font-weight: 700;
  margin-left: 4px;
}

.ark-passive-header-label {
  display: block;
  color: var(--text-primary);
  font-weight: 700;
  margin-right: 4px;
}

.ark-passive-header-desc {
  display: block;
  color: var(--text-primary);
  font-size: var(--font-xs);
  margin-bottom: 4px;
  text-align: center;
}

.ark-passive-rank-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-secondary);
}

.ark-passive-rank-label {
  font-weight: 700;
  color: var(--text-primary);
}

.ark-passive-rank-value {
  font-weight: 700;
  color: var(--text-secondary);
  white-space: nowrap;
}

.engrave-level-image {
  width: 20px;
  height: 20px;
  display: inline-block;
  background-size: 500% 100%;
  background-repeat: no-repeat;
  background-position: 0 0;
  border-radius: 4px;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-head__title h4 {
  margin: 2px 0 0;
}

.card-head__effects {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.card-head__effect-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-primary);
  font-weight: 700;
  font-size: var(--font-xs);
}

.card-head__effect-meta {
  color: var(--text-secondary);
  font-weight: 600;
}

.card-strip-shell {
  border: none;
  margin-top: 8px;
}

.card-strip-shell--empty {
  padding: 16px;
}

.card-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(90px, 1fr));
  gap: 6px;
}

.card-slot {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--text-primary);
}

.card-slot__frame {
  position: relative;
  padding: 3px;
  /* border-radius: 12px; */
  background: var(--surface-muted, var(--bg-secondary));
  background-image: url('https://cdn-lostark.game.onstove.com/2018/obt/assets/images/pc/profile/img_card_grade_tooltip4.png?a9467e903872aa545f4e');
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
  border: none;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
  min-height: 140px;
}

.card-slot__thumb {
  position: relative;
  width: 100%;
  aspect-ratio: 7 / 10;
  overflow: hidden;
  border-radius: 8px;
  background: var(--card-bg);
  border: none;
  box-shadow: inset 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-slot__icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-slot__placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--text-secondary);
}

.card-slot__awake-row {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: inline-flex;
  justify-content: center;
  align-items: flex-end;
  gap: 4px;
  width: calc(100% - 12px);
  padding: 0 6px;
}

.card-awake-orb {
  position: relative;
  display: inline-flex;
  justify-content: center;
  align-items: flex-end;
}

.card-awake-orb__layer {
  position: absolute;
  inset: 0;
  background-repeat: no-repeat;
  background-size: cover;
}

.card-awake-orb__layer--fill {
  filter: drop-shadow(0 0 4px rgba(255, 193, 94, 0.4));
}

.card-slot__body {
  text-align: center;
}

.card-slot__name {
  margin: 0;
  font-weight: 700;
  font-size: var(--font-xxs);
  color: var(--text-primary);
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.4);
}

.card-effect-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.card-effect {
  padding: 10px;
  border-radius: 12px;
  background: var(--surface-muted, #f9fafb);
  border: 1px solid var(--border-color);
}

.card-effect__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.card-effect__name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
}

.card-effect__lines {
  margin: 6px 0 0;
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: var(--font-sm);
  display: flex;
  flex-direction: column;
  gap: 2px;
}
</style>
