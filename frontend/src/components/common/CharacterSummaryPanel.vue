<template>
  <section class="detail-panel summary-panel">
    <div v-if="activeCharacter" class="summary-grid summary-grid--modules summary-grid--stacked">
      <article class="summary-card summary-card--module summary-card--equipment">
        <div class="ark-section__block">
          <div class="summary-card__head">
            <p class="summary-eyebrow">장비</p>
            <div class="equipment-toggle">
              <button
                type="button"
                class="equipment-toggle__btn"
                :class="{ 'is-active': equipmentView === 'equipment' }"
                @click="equipmentView = 'equipment'"
              >
                장비
              </button>
              <button
                type="button"
                class="equipment-toggle__btn"
                :class="{ 'is-active': equipmentView === 'avatar' }"
                @click="equipmentView = 'avatar'"
              >
                아바타
              </button>
            </div>
          </div>
          <p v-if="detailLoading" class="summary-note">장비 정보를 정리하는 중입니다...</p>
          <p v-else-if="detailError" class="summary-note summary-note--warning">{{ detailError }}</p>
          <div v-else>
            <div v-if="equipmentView === 'equipment'" class="equipment-row-list">
              <div v-for="row in equipmentRows" :key="row.key" class="equipment-row">
                <template v-if="row.right?.isBracelet && !row.left">
                  <div class="equipment-side equipment-side--bracelet">
                    <div class="equipment-icon-stack">
                      <IconImage :src="row.right.icon || ''" :alt="row.right.name || ''" width="40" height="40"
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
                        <IconImage :src="row.left.icon || ''" :alt="row.left.name || ''" width="40" height="40"
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
                        <IconImage :src="row.right.icon || ''" :alt="row.right.name || ''" width="40" height="40"
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
            <div v-else class="avatar-view">
                <div class="avatar-hero avatar-hero--stacked">
                  <div class="avatar-hero__image" :style="{ backgroundImage: characterImage ? `url(${characterImage})` : undefined }">
                    <span v-if="!characterImage" class="avatar-hero__placeholder">캐릭터 이미지 없음</span>
                    <div class="avatar-overlay-grid">
                      <div class="avatar-slot-column">
                      <div
                        v-for="slot in avatarLeftSlots"
                        :key="slot.key"
                        class="avatar-slot"
                      >
                        <div class="avatar-slot__meta">
                          <p class="avatar-slot__label">{{ slot.label }}</p>
                        </div>
                        <div class="avatar-slot__body" :class="slot.single ? 'avatar-slot__body--single-left' : 'avatar-slot__body--left'">
                          <div
                            class="avatar-slot__icon"
                            :class="[`grade-${slot.base?.gradeKey || 'none'}`, { 'avatar-slot__icon--empty': !slot.base }]"
                          >
                          <IconImage
                            v-if="slot.base?.icon"
                            :src="slot.base.icon"
                            :alt="slot.label"
                            width="40"
                            height="40"
                            imageClass="avatar-slot__img"
                            errorIcon="👗"
                          />
                            <span v-else class="avatar-slot__placeholder">✕</span>
                            <div
                              v-if="slot.base?.tooltipLines?.length"
                              class="avatar-slot__tooltip avatar-slot__tooltip--icon popup-surface popup-surface--tooltip"
                            >
                              <p
                                v-for="(line, tipIdx) in slot.base.tooltipLines"
                                :key="`avatar-base-tip-${slot.key}-${tipIdx}`"
                                class="popup-surface__body"
                                :class="avatarTooltipLineClass(line, false)"
                              >
                                {{ line }}
                              </p>
                            </div>
                          </div>
                          <template v-if="!slot.single">
                            <div
                              class="avatar-slot__icon"
                              :class="[`grade-${slot.overlay?.gradeKey || 'none'}`, { 'avatar-slot__icon--empty': !slot.overlay }]"
                            >
                              <IconImage
                                v-if="slot.overlay?.icon"
                                :src="slot.overlay.icon"
                                :alt="slot.label"
                                width="40"
                                height="40"
                                imageClass="avatar-slot__img"
                                errorIcon="👗"
                              />
                              <span v-else class="avatar-slot__placeholder">✕</span>
                              <div
                                v-if="slot.overlay?.tooltipLines?.length"
                                class="avatar-slot__tooltip avatar-slot__tooltip--icon popup-surface popup-surface--tooltip"
                              >
                                <p
                                  v-for="(line, tipIdx) in slot.overlay.tooltipLines"
                                  :key="`avatar-overlay-tip-${slot.key}-${tipIdx}`"
                                  class="popup-surface__body"
                                  :class="avatarTooltipLineClass(line, true)"
                                >
                                  {{ line }}
                                </p>
                              </div>
                            </div>
                          </template>
                        </div>
                      </div>
                    </div>
                    <div class="avatar-slot-column avatar-slot-column--left">
                      <div
                        v-for="slot in avatarRightSlots"
                        :key="slot.key"
                        class="avatar-slot"
                      >
                        <div class="avatar-slot__meta">
                          <p class="avatar-slot__label">{{ slot.label }}</p>
                        </div>
                        <div class="avatar-slot__body" :class="slot.single ? 'avatar-slot__body--single-right' : 'avatar-slot__body--right'">
                          <template v-if="!slot.single">
                            <div
                              class="avatar-slot__icon"
                              :class="[`grade-${slot.overlay?.gradeKey || 'none'}`, { 'avatar-slot__icon--empty': !slot.overlay }]"
                            >
                              <IconImage
                                v-if="slot.overlay?.icon"
                                :src="slot.overlay.icon"
                                :alt="slot.label"
                                width="40"
                                height="40"
                                imageClass="avatar-slot__img"
                                errorIcon="🎭"
                              />
                              <span v-else class="avatar-slot__placeholder">✕</span>
                              <div
                                v-if="slot.overlay?.tooltipLines?.length"
                                class="avatar-slot__tooltip avatar-slot__tooltip--icon popup-surface popup-surface--tooltip"
                              >
                                <p
                                  v-for="(line, tipIdx) in slot.overlay.tooltipLines"
                                  :key="`avatar-overlay-tip-${slot.key}-${tipIdx}`"
                                  class="popup-surface__body"
                                  :class="avatarTooltipLineClass(line, true)"
                                >
                                  {{ line }}
                                </p>
                              </div>
                            </div>
                          </template>
                          <div
                            class="avatar-slot__icon"
                            :class="[`grade-${slot.base?.gradeKey || 'none'}`, { 'avatar-slot__icon--empty': !slot.base }]"
                          >
                            <IconImage
                              v-if="slot.base?.icon"
                              :src="slot.base.icon"
                              :alt="slot.label"
                              width="40"
                              height="40"
                              imageClass="avatar-slot__img"
                              errorIcon="🎭"
                            />
                            <span v-else class="avatar-slot__placeholder">✕</span>
                            <div
                              v-if="slot.base?.tooltipLines?.length"
                              class="avatar-slot__tooltip avatar-slot__tooltip--icon popup-surface popup-surface--tooltip"
                            >
                              <p
                                v-for="(line, tipIdx) in slot.base.tooltipLines"
                                :key="`avatar-base-tip-${slot.key}-${tipIdx}`"
                                class="popup-surface__body"
                                :class="avatarTooltipLineClass(line, false)"
                              >
                                {{ line }}
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="avatarExtraSlots.length" class="avatar-extra-list">
                <p class="avatar-extra-title">기타</p>
                <div class="avatar-extra-grid">
                  <div
                    v-for="(entry, idx) in avatarExtraSlots"
                    :key="`avatar-extra-${idx}`"
                    class="avatar-extra-card"
                  >
                    <div class="avatar-slot__icon" :class="`grade-${entry.item?.gradeKey || 'none'}`">
                      <IconImage
                        v-if="entry.item?.icon"
                        :src="entry.item.icon"
                        :alt="entry.item?.type || entry.item?.Type || '기타 아바타'"
                        width="46"
                        height="46"
                        imageClass="avatar-slot__img"
                        errorIcon="✨"
                      />
                      <span v-else class="avatar-slot__placeholder">?</span>
                    </div>
                    <p class="avatar-slot__label avatar-slot__label--center">
                      {{ entry.item?.type || entry.item?.Type || '기타' }}
                    </p>
                    <div v-if="entry.item?.tooltipLines?.length" class="avatar-slot__tooltip popup-surface popup-surface--tooltip">
                      <p
                        v-for="(line, tipIdx) in entry.item.tooltipLines"
                        :key="`avatar-extra-tip-${idx}-${tipIdx}`"
                        class="popup-surface__body"
                      >
                        {{ line }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
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
                <div v-for="(row, rowIndex) in (arkSummary.passiveMatrix ?? [])" :key="row.id"
                  class="ark-passive-grid-row" :class="{ 'ark-passive-grid-row--with-divider': rowIndex > 0 }">
                  <div v-for="section in row.sections" :key="`${row.id}-${section.key}`" class="ark-passive-cell">
                    <div v-if="section.effects.length" class="ark-passive-cell-list">
                      <div v-for="effect in section.effects" :key="effect.key" class="ark-passive-chip">
                        <div class="ark-passive-chip-visual">
                          <IconImage v-if="effect.icon" :src="effect.icon" :alt="effect.name" width="30" height="30"
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
                          <IconImage v-if="slot.icon" :src="slot.icon" :alt="slot.name" width="40" height="40"
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
                <IconImage :src="skill.icon" :alt="skill.name" width="40" height="40" imageClass="summary-icon"
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
                  <span v-for="(tripod, index) in (skill.tripods ?? [])" :key="tripod.key" class="summary-tripod-slot-dot"
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
                      <span class="summary-gem-label" :title="skill.gems?.damage?.name || ''">
                        {{ skill.gems?.damage?.label || skill.gems?.damage?.name || '' }}
                      </span>
                      <span class="summary-gem-label" :title="skill.gems?.cooldown?.name || ''">
                        {{ skill.gems?.cooldown?.label || skill.gems?.cooldown?.name || '' }}
                      </span>
                    </div>
                  </div>
                  <div class="summary-gem-row summary-gem-row--icons">
                    <div class="summary-gem-cell">
                    </div>
                    <div class="summary-gem-cell">
                      <span v-if="skill.gems?.damage?.label != undefined" class="summary-gem-level-dot">
                        {{ skill.gems?.damage?.levelLabel?.replace('Lv.', '') || '-' }}
                      </span>
                    </div>
                    <div v-if="skill.gems?.cooldown?.label != undefined" class="summary-gem-cell">
                      <span class="summary-gem-level-dot">
                        {{ skill.gems?.cooldown?.levelLabel?.replace('Lv.', '') || '-' }}
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
                <IconImage v-if="engrave.icon || engravingIcon(engrave.name)"
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
                    <IconImage v-if="card.icon" :src="card.icon" :alt="card.name" :width="`inherit - 10px`"
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
import { computed, ref } from 'vue'
import IconImage from './IconImage.vue'
import EmptyState from './EmptyState.vue'
import type { CharacterProfile } from '@/api/types'
import { stripHtml } from '@/utils/tooltipParser'
import { extractTooltipColor, flattenTooltipLines } from '@/utils/tooltipText'
import { getEngravingIcon } from '@/assets/BuffImage'
import { getEngravingDisplayName, ENGRAVING_NAME_ENTRIES } from '@/data/engravingNames'
import { COMBAT_STATS } from '@/data/combatStats'
import { isRecord } from '@/utils/typeGuards'
import {
  applyEffectAbbreviations,
  hasAbbreviationMatch,
  EFFECT_ABBREVIATION_REPLACEMENTS,
  expandDemeritAbbreviation,
  DEALER_ABBREVIATIONS,
  SUPPORT_ABBREVIATIONS
} from '@/data/effectAbbreviations'

type EquipmentEffect = {
  label?: string
  full?: string
  richLabel?: string
  bgColor?: string
  textColor?: string
  isCombat?: boolean
}

type EquipmentSummaryItem = {
  key?: string
  name?: string
  typeLabel?: string
  grade?: string
  icon?: string
  itemLevel?: string
  enhancement?: string
  harmony?: string
  transcend?: string
  quality?: number | null
  isAccessory?: boolean
  isBracelet?: boolean
  isAbilityStone?: boolean
  special?: string
  effects?: EquipmentEffect[]
}

type EquipmentSummary = {
  gradeBadges?: Array<{ grade: string; count: number }>
  left?: EquipmentSummaryItem[]
  right?: EquipmentSummaryItem[]
}

type AvatarLike = {
  type?: string
  Type?: string
  name?: string
  Name?: string
  grade?: string
  Grade?: string
  icon?: string
  Icon?: string
  tooltip?: string
  Tooltip?: string
  isInner?: boolean
  IsInner?: boolean
  isSet?: boolean
  IsSet?: boolean
  tooltipLines?: string[]
  gradeKey?: string
}

type AvatarSummary = {
  items?: AvatarLike[]
  left?: AvatarLike[]
}

type SkillHighlight = {
  key: string
  name: string
  icon: string
  levelLabel?: string
  pointLabel?: string
  rune?: { name: string; icon?: string; grade?: string; color?: string } | null
  gems?: {
    damage?: { name?: string; label?: string; levelLabel?: string } | null
    cooldown?: { name?: string; label?: string; levelLabel?: string } | null
  } | null
  tripods?: Array<{ key: string; slotLabel: string; name?: string }> | null
  hasGem?: boolean
  isGemOnly?: boolean
}

type LooseGem = {
  key: string
  name: string
  skillName?: string
  icon: string
  levelLabel?: string
  grade?: string
  effectLabel?: string
  typeLabel?: string
}

type EngravingSummaryItem = {
  key: string
  name: string
  displayName: string
  gradeLabel: string
  levelLabel: string
  craftLabel: string
  icon: string
}

type CardSummary = {
  cards: Array<{
    key: string
    name: string
    icon: string
    grade: string
    awakeLabel: string | null
    awakeCount: number | null
    awakeTotal: number | null
  }>
  effects: Array<{ key: string; label: string; descriptions: string[]; setLabel: string }>
}

type CollectionSummaryItem = {
  key: string
  name: string
  levelLabel?: string
  pointLabel?: string
  percentLabel: string
  percentValue: number
}

type ArkCoreSlot = {
  key: string
  name: string
  icon?: string
  initial: string
  pointLabel?: string
  nameColor?: string
  gradeColor?: string
  tooltip?: unknown
  grade?: string
  gradeLabel?: string
  gradeName?: string
}

type ArkSummary = {
  passiveTitle?: string
  appliedPoints?: Array<{ key: string; label: string; value?: string; description?: string }>
  passiveMatrix?: Array<{ id: string; sections: Array<{ key: string; effects: Array<{ key: string; name: string; icon?: string; levelDisplay?: string; levelLine?: string }> }> }>
  coreMatrix: { rows: Array<{ key: string; label: string; cells: Array<{ key: string; label: string; slots: ArkCoreSlot[] }> }> }
}

const props = defineProps<{
  activeCharacter: CharacterProfile | null
  equipmentSummary: EquipmentSummary
  avatarSummary?: AvatarSummary
  characterImage?: string
  detailLoading: boolean
  detailError: string | null
  arkSummary: ArkSummary
  arkGridLoading: boolean
  arkGridError: string | null
  skillHighlights: SkillHighlight[]
  skillLooseGems?: LooseGem[]
  skillLoading: boolean
  skillError: string | null
  engravingSummary: EngravingSummaryItem[]
  cardSummary: CardSummary
  cardLoading: boolean
  cardError: string | null
  collectionSummary: CollectionSummaryItem[]
  collectiblesLoading: boolean
  collectiblesError: string | null
  combatRole?: 'dealer' | 'support' | null
}>()

const equipmentView = ref<'equipment' | 'avatar'>('equipment')
const inlineText = (value?: string | number | null) => (value ?? '').toString().replace(/\s+/g, ' ').trim()

const displaySkillHighlights = computed(() => props.skillHighlights || [])

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

const AVATAR_SLOT_CONFIG = [
  { key: 'weapon', label: '무기 아바타', keywords: ['무기', 'weapon', '아바타무기', '아바타 무기'] },
  { key: 'head', label: '머리', keywords: ['머리', '모자', '헤어', '머리장식', '투구', '모자아바타'] },
  { key: 'face1', label: '얼굴1', keywords: ['얼굴1', '페이스1', '마스크', '얼굴장식', '얼굴', '페이스'] },
  { key: 'face2', label: '얼굴2', keywords: ['얼굴2', '페이스2'] },
  { key: 'top', label: '상의', keywords: ['상의', '코트', '자켓', '상의아바타', '상의아바타', '상의 아바타', '상의(아바타)'] },
  { key: 'bottom', label: '하의', keywords: ['하의', '바지', '스커트', '하의아바타', '하의 아바타', '하의(아바타)'] },
  { key: 'full', label: '원피스', keywords: ['원피스', '한벌', '드레스', '전신', '세트', '원피스아바타'] },
  { key: 'instrument', label: '악기', keywords: ['악기', 'instrument', '악기 아바타', '악기아바타'] },
  { key: 'movement', label: '이동 효과', keywords: ['이동효과', '이동 효과', 'moving', '효과'] }
] as const

const AVATAR_LEFT_KEYS = ['weapon',  'instrument', 'movement']
const AVATAR_RIGHT_KEYS = ['head', 'face1', 'face2','top', 'bottom', 'full']
const AVATAR_SINGLE_SLOT_KEYS = ['face1', 'face2', 'instrument', 'movement', 'full']
const AVATAR_TRADE_KEYWORDS = ['거래', '귀속']
const AVATAR_LINEAGE_KEYWORDS = ['계열']
const AVATAR_BASE_EFFECT_KEYWORDS = ['힘', '민첩', '지능', '체력', ...COMBAT_STATS]
const AVATAR_VIRTUE_KEYWORDS = ['매력', '친절', '지성', '담력']

const avatarItems = computed(() => props.avatarSummary?.items || props.avatarSummary?.left || [])

const avatarGradeKey = (grade?: string | null) => {
  const g = inlineText(grade).toLowerCase()
  if (g.includes('고대')) return 'ancient'
  if (g.includes('유물')) return 'relic'
  if (g.includes('전설')) return 'legendary'
  if (g.includes('영웅')) return 'epic'
  if (g.includes('희귀')) return 'rare'
  if (g.includes('고급')) return 'uncommon'
  if (g.includes('일반')) return 'common'
  return 'none'
}

const normalizeAvatarLabel = (value?: string | null) =>
  inlineText(value).toLowerCase().replace(/[\s()[\]{}]/g, '')

const resolveAvatarSlotKey = (item: AvatarLike) => {
  const label = normalizeAvatarLabel(item?.type || item?.Type || item?.name || item?.Name)
  const name = normalizeAvatarLabel(item?.name || item?.Name)
  const combined = `${label} ${name}`
  const hit = AVATAR_SLOT_CONFIG.find(cfg =>
    cfg.keywords.some(keyword => combined.includes(normalizeAvatarLabel(keyword)))
  )
  return hit?.key || null
}

const avatarTooltipLines = (item: AvatarLike) => {
  const tooltip = item?.tooltip || item?.Tooltip || ''
  if (!tooltip) return []
  return flattenTooltipLines(tooltip)
    .map(line => stripHtml(line).trim())
    .filter(Boolean)
}

const extractVirtueEntries = (text: string) => {
  const entries: string[] = []
  AVATAR_VIRTUE_KEYWORDS.forEach(virtue => {
    const regex = new RegExp(`${virtue}\\s*[:：]?\\s*([\\d+.]+)`, 'gi')
    let match
    while ((match = regex.exec(text)) !== null) {
      entries.push(`${virtue} : ${match[1]}`)
    }
  })
  return entries
}

const formatAvatarTooltipLines = (item: AvatarLike) => {
  const name = inlineText(item?.name || item?.Name)
  const gradeLabel = inlineText(item?.grade || item?.Grade)
  const rawLines = avatarTooltipLines(item)
  const result: string[] = []
  const seen = new Set<string>()

  const pushUnique = (line?: string) => {
    const text = inlineText(line)
    if (!text) return
    if (seen.has(text)) return
    seen.add(text)
    result.push(text)
  }

  const cleanVirtuePrefix = (text: string) => text.replace(/&tdc_[a-z]+/gi, '').trim()

  pushUnique(name)
  pushUnique(gradeLabel)

  const lineageLines: string[] = []
  const baseLines: string[] = []
  const virtueLines: string[] = []
  const tradeLines: string[] = []

  const splitTradeLine = (text: string) => {
    const parts = text
      .split('|')
      .map(part => part.trim())
      .filter(Boolean)
    const rewritten: string[] = []
    parts.forEach(part => {
      if (/거래가능/.test(part)) return
      const tradeCount = part.match(/거래\s*\d+\s*회\s*가능/)
      if (tradeCount) rewritten.push(tradeCount[0].replace(/\s+/g, ' ').trim())
      if (/거래\s*불가/.test(part)) rewritten.push('거래 불가')
      if (part.includes('귀속')) rewritten.push('원정대 귀속됨')
    })
    return rewritten
  }

  rawLines.forEach(line => {
    const rawText = inlineText(line)
    if (!rawText) return
    const text = cleanVirtuePrefix(rawText)
    const virtueMatches = extractVirtueEntries(text)
    if (virtueMatches.length) {
      virtueMatches.forEach(v => virtueLines.push(v))
      return
    }
    const hasTrade = AVATAR_TRADE_KEYWORDS.some(key => text.includes(key))
    const hasLineage = AVATAR_LINEAGE_KEYWORDS.some(key => text.includes(key))
    const hasBase = AVATAR_BASE_EFFECT_KEYWORDS.some(key => text.includes(key)) || text.toLowerCase().includes('기본효과')
    const hasVirtue = AVATAR_VIRTUE_KEYWORDS.some(key => text.includes(key))

    if (hasTrade) {
      splitTradeLine(text).forEach(entry => tradeLines.push(entry))
    } else if (hasLineage) {
      lineageLines.push(text)
    } else if (hasBase) {
      baseLines.push(text)
    } else if (hasVirtue) {
      virtueLines.push(text)
    }
  })

  lineageLines.forEach(pushUnique)
  baseLines.forEach(pushUnique)
  virtueLines.forEach(pushUnique)
  // 거래/귀속 텍스트는 하단에만 표시 (중복 제거).
  tradeLines.forEach(pushUnique)

  return result
}

const avatarTooltipLineClass = (line: string, isOverlay: boolean) => {
  const text = inlineText(line)
  const isStat =
    AVATAR_BASE_EFFECT_KEYWORDS.some(key => text.includes(key)) || text.toLowerCase().includes('기본효과')
  const isVirtue = AVATAR_VIRTUE_KEYWORDS.some(key => text.includes(key))
  const isTrade = AVATAR_TRADE_KEYWORDS.some(key => text.includes(key))
  const gradeKeyFromText = avatarGradeKey(text)
  const isGrade =
    gradeKeyFromText !== 'none' &&
    ['고대', '유물', '전설', '영웅', '희귀', '고급', '일반'].some(word => text.includes(word))
  return {
    'avatar-tooltip__stat': isStat,
    'avatar-tooltip__stat--base': isStat && !isOverlay,
    'avatar-tooltip__stat--overlay': isStat && isOverlay,
    'avatar-tooltip__virtue': isVirtue,
    'avatar-tooltip__trade': isTrade,
    'avatar-tooltip__grade': isGrade,
    [`avatar-tooltip__grade--${gradeKeyFromText}`]: isGrade
  }
}

const avatarSlotBuckets = computed(() => {
  type AvatarSlotItem = AvatarLike & { gradeKey: string; tooltipLines: string[]; isInner: boolean }
  const entries = AVATAR_SLOT_CONFIG.map(cfg => ({
    key: cfg.key,
    label: cfg.label,
    outer: null as AvatarSlotItem | null,
    inner: null as AvatarSlotItem | null
  }))
  const extras: Array<{ item: AvatarSlotItem; isInner: boolean }> = []

  avatarItems.value.forEach(item => {
    const slotKey = resolveAvatarSlotKey(item)
    const normalized: AvatarSlotItem = {
      ...item,
      gradeKey: avatarGradeKey(item.grade || item.Grade),
      tooltipLines: formatAvatarTooltipLines(item),
      isInner: item.isInner ?? item.IsInner ?? false
    }
    if (!slotKey) {
      extras.push({ item: normalized, isInner: normalized.isInner })
      return
    }
    const target = entries.find(entry => entry.key === slotKey)
    if (!target) {
      extras.push({ item: normalized, isInner: normalized.isInner })
      return
    }
    if (normalized.isInner) {
      if (!target.inner) target.inner = normalized
    } else {
      if (!target.outer) target.outer = normalized
    }
  })

  const fullSlot = entries.find(entry => entry.key === 'full')
  const topSlot = entries.find(entry => entry.key === 'top')
  const bottomSlot = entries.find(entry => entry.key === 'bottom')
  const hasFull = (fullSlot?.inner || fullSlot?.outer) != null
  const hasTopBottom = (topSlot?.inner || topSlot?.outer || bottomSlot?.inner || bottomSlot?.outer) != null
  if (hasFull && topSlot) {
    topSlot.inner = null
    topSlot.outer = null
  }
  if (hasFull && bottomSlot) {
    bottomSlot.inner = null
    bottomSlot.outer = null
  }
  if (hasTopBottom && fullSlot && !hasFull) {
    fullSlot.inner = null
    fullSlot.outer = null
  }

  return { entries, extras }
})

const avatarVisibleSlotEntries = computed(() => {
  const entries = avatarSlotBuckets.value.entries

  const fullSlot = entries.find(entry => entry.key === 'full')
  const topSlot = entries.find(entry => entry.key === 'top')
  const bottomSlot = entries.find(entry => entry.key === 'bottom')

  const hasFull = (fullSlot?.inner || fullSlot?.outer) != null
  const hasTopBottom = (topSlot?.inner || topSlot?.outer || bottomSlot?.inner || bottomSlot?.outer) != null

  if (hasFull) {
    return entries.filter(entry => entry.key !== 'top' && entry.key !== 'bottom')
  }
  if (hasTopBottom) {
    return entries.filter(entry => entry.key !== 'full')
  }
  return entries
})

const splitAvatarSlots = (slots: typeof avatarSlotBuckets.value.entries, layout: 'left' | 'right') =>
  slots.map(slot => {
    const single = AVATAR_SINGLE_SLOT_KEYS.includes(slot.key)
    const preferredBase = layout === 'left' ? slot.outer : slot.inner
    const preferredOverlay = layout === 'left' ? slot.inner : slot.outer
    const base = single ? (slot.outer || slot.inner || null) : preferredBase || preferredOverlay || null
    const overlay =
      single || !preferredOverlay || preferredOverlay === base ? null : preferredOverlay

    return {
      ...slot,
      base,
      overlay,
      single,
      layout
    }
  })

const avatarLeftSlots = computed(() => {
  const slots = avatarVisibleSlotEntries.value.filter(slot => AVATAR_LEFT_KEYS.includes(slot.key))
  return splitAvatarSlots(slots, 'left')
})

const avatarRightSlots = computed(() => {
  const slots = avatarVisibleSlotEntries.value.filter(slot => AVATAR_RIGHT_KEYS.includes(slot.key))
  return splitAvatarSlots(slots, 'right')
})
const avatarExtraSlots = computed(() => avatarSlotBuckets.value.extras)

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

const engravingBadgeSlice = (engrave: EngravingSummaryItem): EngravingBadgeSlice => {
  if (engrave.craftLabel) return 'stone'
  const grade = (engrave.gradeLabel || '').toLowerCase()
  if (grade.includes('유물')) return 'relic'
  if (grade.includes('영웅')) return 'heroic'
  if (grade.includes('전설')) return 'legendary'
  return 'default'
}

const engravingLevelBadgeStyle = (engrave: EngravingSummaryItem) => {
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

const gearEnhanceParts = (item?: EquipmentSummaryItem) => {
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

const effectDisplayColor = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
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
    if (!hex) return null
    const toChannel = (chunk: string) => parseInt(chunk.length === 1 ? chunk.repeat(2) : chunk, 16)

    if (hex.length === 3 || hex.length === 4) {
      const rChunk = hex[0]
      const gChunk = hex[1]
      const bChunk = hex[2]
      if (!rChunk || !gChunk || !bChunk) return null
      return {
        r: toChannel(rChunk),
        g: toChannel(gChunk),
        b: toChannel(bChunk)
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

const effectLabelText = (effect: EquipmentEffect) => effect.label || ''

const accessoryDisplayLabel = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
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

const effectDisplayLabel = (effect: EquipmentEffect, item: EquipmentSummaryItem) =>
  isAccessoryItem(item) ? accessoryDisplayLabel(effect, item) : effectLabelText(effect)

const normalizeRoleLabel = (value?: string | null) => (value || '').toLowerCase().trim()

const detectEffectRole = (effect: EquipmentEffect, item: EquipmentSummaryItem): 'dealer' | 'support' | null => {
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

const effectFontWeight = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
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

const isAccessoryItem = (item?: EquipmentSummaryItem) =>
  Boolean(item?.isAccessory && !item?.isBracelet && !item?.isAbilityStone)

const isBraceletCombatStat = (label?: string) => {
  const normalized = (label || '').replace(/\s+/g, '')
  // 전투 특성 단일 라벨(또는 숫자/기호와 함께 시작)만 전투 특성으로 취급
  return COMBAT_STATS.some(
    stat => new RegExp(`^${stat}(\\+|\\s|$)`).test(normalized)
  )
}

const effectPrefixLabel = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
  if (!isAccessoryItem(item)) return ''
  const prefix = effectTierPrefix(effectDisplayColor(effect, item))
  return prefix.trim()
}

const effectTextDisplayColor = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
  if (isAccessoryItem(item)) return 'var(--text-primary)'
  return effectDisplayColor(effect, item)
}

const normalizeCategoryLabel = (value?: string) =>
  (value || '').replace(/\s+/g, '').replace(/[+]/g, '').toLowerCase()

const DEALER_ROLE_PRIORITY_KEYWORDS = ['적주피', '추피', '치피', '치적', '낙', '아덴']
const SUPPORT_ROLE_PRIORITY_KEYWORDS = ['보호막', '회복', '아공강', '아피강']
const COMMON_PERCENT_KEYWORDS = ['공%', '무공%', '공격력%', '무기공격력%']
const COMMON_FLAT_KEYWORDS = ['무공', '공', '공격력', '무기공격력']

type EffectCategory = 'dealer' | 'support' | 'common' | 'junk'

const detectEffectCategory = (effect: EquipmentEffect, item: EquipmentSummaryItem): EffectCategory => {
  if (!isAccessoryItem(item)) return 'common'
  const display = effectDisplayLabel(effect, item)
  if (display === '잡옵') return 'junk'

  const raw = normalizeCategoryLabel(`${effect?.full || ''}${effect?.label || ''}${display}`)
  const hasPercent = /%/.test(raw)
  if (!raw) return 'common'

  const role = detectEffectRole(effect, item)
  if (role) return role

  if (SUPPORT_ROLE_PRIORITY_KEYWORDS.some(key => raw.includes(key))) return 'support'
  if (DEALER_ROLE_PRIORITY_KEYWORDS.some(key => raw.includes(key))) return 'dealer'

  if (
    (hasPercent && COMMON_PERCENT_KEYWORDS.some(key => raw.includes(key))) ||
    (!hasPercent && COMMON_FLAT_KEYWORDS.some(key => raw.includes(key))) ||
    hasAbbreviationMatch(raw)
  ) {
    return 'common'
  }

  return 'common'
}

const effectCategoryPriority = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
  const combatRole = normalizeRoleLabel(props.combatRole)
  const roleOrder: EffectCategory[] = combatRole === 'support'
    ? ['support', 'common', 'dealer', 'junk']
    : ['dealer', 'common', 'support', 'junk']

  const category = detectEffectCategory(effect, item)
  const index = roleOrder.indexOf(category)
  return index === -1 ? roleOrder.length : index
}

const effectDetailPriority = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
  if (detectEffectCategory(effect, item) !== 'common') return 0
  const raw = normalizeCategoryLabel(`${effect?.full || ''}${effect?.label || ''}${effectDisplayLabel(effect, item)}`)
  if (!raw) return 0
  const hasPercent = /%/.test(raw)
  if (hasPercent && COMMON_PERCENT_KEYWORDS.some(key => raw.includes(key))) return 0
  if (!hasPercent && COMMON_FLAT_KEYWORDS.some(key => raw.includes(key))) return 1
  if (hasAbbreviationMatch(raw)) return 2
  return 3
}

const effectTierPriority = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
  const prefix = effectPrefixLabel(effect, item)
  if (prefix.startsWith('상')) return 0
  if (prefix.startsWith('중')) return 1
  if (prefix.startsWith('하')) return 2
  return 3
}

const sortedEffects = (effects: EquipmentEffect[] = [], item: EquipmentSummaryItem) =>
  effects
    .slice()
    .sort((a, b) => {
      const catDiff = effectCategoryPriority(a, item) - effectCategoryPriority(b, item)
      if (catDiff !== 0) return catDiff
      const tierDiff = effectTierPriority(a, item) - effectTierPriority(b, item)
      if (tierDiff !== 0) return tierDiff
      const detailDiff = effectDetailPriority(a, item) - effectDetailPriority(b, item)
      if (detailDiff !== 0) return detailDiff
      return effectDisplayLabel(a, item).localeCompare(effectDisplayLabel(b, item))
    })

const effectsForDisplay = (effects?: EquipmentEffect[], item?: EquipmentSummaryItem) => {
  if (!effects) return []
  if (item?.isBracelet || item?.isAbilityStone) return effects
  if (!item) return effects
  return sortedEffects(effects, item)
}

const braceletEffectParts = (effect: EquipmentEffect, item: EquipmentSummaryItem) => {
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
    const element000 = record['Element_000']
    if (isRecord(element000)) {
      const valueField = element000['value'] ?? element000['Value']
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

const coreNameStyle = (slot: ArkCoreSlot) => {
  const gradeText = slot.grade || slot.gradeLabel || slot.gradeName
  const color =
    slot.nameColor ||
    coreNameColorFromTooltip(slot.tooltip) ||
    slot.gradeColor ||
    extractTooltipColor(typeof slot.tooltip === 'string' ? slot.tooltip : null) ||
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

.equipment-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  padding: 2px;
  border-radius: 999px;
  background: var(--bg-secondary);
}

.equipment-toggle__btn {
  min-width: 64px;
  padding: 6px 10px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: var(--font-xs);
  transition: all 0.16s ease;
}

.equipment-toggle__btn.is-active {
  background: linear-gradient(135deg, rgba(58, 132, 255, 0.18), rgba(94, 234, 212, 0.22));
  color: var(--text-primary);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.avatar-view {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  padding: 12px 0;
}

.avatar-hero {
  grid-column: span 2;
  display: flex;
  justify-content: center;
}

.avatar-hero__image {
  width: min(360px, 100%);
  aspect-ratio: 3 / 5;
  border-radius: 18px;
  background: radial-gradient(circle at 50% 30%, rgba(255, 255, 255, 0.08), rgba(0, 0, 0, 0.2)),
    linear-gradient(145deg, rgba(15, 23, 42, 0.82), rgba(30, 64, 175, 0.48));
  background-size: cover;
  background-position: center;
  border: 1px solid rgba(255, 255, 255, 0.08);
  /* box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.04), 0 18px 60px rgba(0, 0, 0, 0.25); */
  display: grid;
  place-items: center;
  position: relative;
  overflow: visible;
}

.avatar-hero__placeholder {
  color: var(--text-secondary);
  font-size: var(--font-sm);
}

.avatar-overlay-grid {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  align-items: end;
  gap: 100px;
  padding: 16px 12px;
  z-index: 2;
  pointer-events: none;
  overflow: visible;
}

.avatar-overlay-grid .avatar-slot-column {
  pointer-events: auto;
}

.avatar-slot-columns {
  grid-column: span 2;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.avatar-slot-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width:fit-content;
}

.avatar-slot-column--left{
  align-items: flex-end;
}

.avatar-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 5px;
  border-radius: 12px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.02), rgba(255, 255, 255, 0.158));
  /* border: 1px solid rgba(255, 255, 255, 0.06); */
  position: relative;
  width:fit-content;
}

.avatar-slot__body {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  flex: 1;
}

.avatar-slot__body--left {
  flex-direction: row-reverse;
}

.avatar-slot__body--right {
  flex-direction: row;
}

.avatar-slot__body--single-left {
  flex-direction: row;
  justify-content: flex-start;
}

.avatar-slot__body--single-right {
  flex-direction: row;
  justify-content: flex-start;
}

.avatar-slot__icon {
  width: 45px;
  height: 45px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
  position: relative;
}

.avatar-slot__img {
  border-radius: 10px;
}

.avatar-slot__placeholder {
  color: var(--text-secondary);
  font-weight: 700;
}

.avatar-slot__icon--empty {
  border-color: rgba(148, 163, 184, 0.45);
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.avatar-slot__meta {
  width: 100%;
  text-align: center;
}

.avatar-slot__label {
  margin: 0;
  color: var(--text-inverse);
  font-size: var(--font-xxs);
  font-weight: 700;
  letter-spacing: 0.01em;
}

.avatar-slot__label--center {
  text-align: center;
}

.avatar-slot__name {
  margin: 0;
  font-weight: 700;
  color: var(--text-primary);
  font-size: var(--font-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.avatar-slot__icon.grade-ancient {
  border-color: rgba(255, 215, 128, 0.6);
  box-shadow: 0 0 12px rgba(255, 215, 128, 0.3);
}

.avatar-slot__icon.grade-relic {
  border-color: rgba(255, 94, 152, 0.6);
  box-shadow: 0 0 12px rgba(255, 94, 152, 0.3);
}

.avatar-slot__icon.grade-legendary {
  border-color: rgba(255, 168, 46, 0.6);
  box-shadow: 0 0 12px rgba(255, 168, 46, 0.28);
}

.avatar-slot__icon.grade-epic {
  border-color: rgba(181, 125, 255, 0.6);
  box-shadow: 0 0 12px rgba(181, 125, 255, 0.25);
}

.avatar-slot__icon.grade-rare {
  border-color: rgba(88, 170, 255, 0.6);
  box-shadow: 0 0 12px rgba(88, 170, 255, 0.22);
}

.avatar-slot__icon.grade-uncommon {
  border-color: rgba(52, 211, 153, 0.6);
  box-shadow: 0 0 12px rgba(52, 211, 153, 0.2);
}

.avatar-slot__icon.grade-common {
  border-color: rgba(148, 163, 184, 0.6);
  box-shadow: 0 0 12px rgba(148, 163, 184, 0.18);
}

.avatar-slot__icon.grade-none {
  border-color: rgba(255, 255, 255, 0.08);
}

.avatar-slot__tooltip {
  position: absolute;
  left: 0;
  bottom: calc(100% + 8px);
  min-width: 180px;
  max-width: 280px;
  visibility: hidden;
  opacity: 0;
  transform: translateY(6px);
  transition: opacity 0.15s ease, transform 0.15s ease, visibility 0.15s ease;
  z-index: 10;
}

.avatar-slot__tooltip--icon {
  left: 50%;
  transform: translate(-50%, 6px);
}

.avatar-slot:hover .avatar-slot__tooltip:not(.avatar-slot__tooltip--icon),
.avatar-slot:focus-within .avatar-slot__tooltip:not(.avatar-slot__tooltip--icon),
.avatar-extra-card:hover .avatar-slot__tooltip,
.avatar-extra-card:focus-within .avatar-slot__tooltip {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
}

.avatar-slot__icon:hover .avatar-slot__tooltip--icon,
.avatar-slot__icon:focus-within .avatar-slot__tooltip--icon {
  visibility: visible;
  opacity: 1;
  transform: translate(-50%, 0);
}

.avatar-slot__tooltip .popup-surface__body {
  margin-bottom: 5px;
}

.avatar-slot__tooltip .popup-surface__body:last-child {
  margin-bottom: 0;
}

.avatar-tooltip__stat--base {
  font-weight: 700;
}

.avatar-tooltip__stat--overlay {
  color: #9ca3af;
}

.avatar-tooltip__virtue {
  display: block;
  margin-bottom: 4px;
}

.avatar-tooltip__grade--ancient {
  color: var(--rarity-ancient, #eab308);
}

.avatar-tooltip__grade--relic {
  color: var(--rarity-relic, #f97316);
}

.avatar-tooltip__grade--legendary {
  color: var(--rarity-legendary, #fbbf24);
}

.avatar-tooltip__grade--epic {
  color: var(--rarity-heroic, #a78bfa);
}

.avatar-tooltip__grade--rare {
  color: var(--rarity-rare, #60a5fa);
}

.avatar-tooltip__grade--uncommon {
  color: var(--rarity-uncommon, #6ee7b7);
}

.avatar-tooltip__grade--common {
  color: var(--text-secondary, #9ca3af);
}

.avatar-extra-list {
  margin-top: 16px;
}

.avatar-extra-title {
  margin: 0 0 8px;
  font-weight: 700;
  color: var(--text-primary);
}

.avatar-extra-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.avatar-extra-card {
  position: relative;
  display: grid;
  justify-items: center;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.04), rgba(255, 255, 255, 0.02));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06);
  gap: 8px;
}

@media (max-width: 768px) {
  .card-strip {
    grid-template-columns: repeat(3, minmax(40px, 1fr));
    gap: 8px;
  }

  .avatar-view {
    grid-template-columns: 1fr;
  }

  .avatar-hero {
    grid-column: span 1;
  }

  .avatar-slot-columns {
    grid-template-columns: 1fr;
    grid-column: span 1;
  }
}

@media (max-width: 520px) {
  .card-strip {
    grid-template-columns: repeat(3, minmax(40px, 1fr));
  }

  .card-head__effects {
    justify-content: flex-start;
  }
}

</style>
