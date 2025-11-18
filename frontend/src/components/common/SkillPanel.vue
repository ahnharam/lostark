<template>
  <div class="skill-panel-shell">
    <div v-if="loading" class="skill-panel-placeholder">
      <LoadingSpinner message="스킬 정보를 불러오는 중입니다..." />
    </div>
    <div v-else-if="errorMessage" class="skill-panel-placeholder">
      <ErrorMessage
        title="스킬 정보를 불러올 수 없어요"
        :message="errorMessage"
        :retry="true"
        :dismissible="false"
        @retry="$emit('retry')"
      />
    </div>
    <div v-else-if="!hasRenderableContent" class="skill-panel-placeholder">
      <EmptyState
        icon="🎯"
        title="스킬 데이터가 감지되지 않았어요"
        :description="emptyStateDescription"
      >
        <button v-if="characterName" type="button" class="skill-panel-retry" @click="$emit('retry')">
          다시 불러오기
        </button>
      </EmptyState>
    </div>
    <div v-else class="skill-panel-layout">
      <section
        v-if="superSkillHighlights.length"
        class="skill-section skill-section--highlight"
      >
        <div class="section-heading">
          <div>
            <h4>초각성 스킬</h4>
          </div>
        </div>
        <div class="skill-card-grid super-skill-grid">
          <article
            v-for="skill in superSkillHighlights"
            :key="`super-${skill.key}`"
            class="skill-card"
            :class="{ 'skill-card--compact': skill.isCompact }"
          >
            <div class="skill-card-main">
              <div class="skill-card-hero">
                <div class="skill-card-icon-block">
                  <div class="skill-icon-wrapper" tabindex="0">
                    <LazyImage
                      v-if="skill.icon"
                      :src="skill.icon"
                      :alt="skill.name"
                      width="40"
                      height="40"
                      imageClass="skill-card-icon"
                      errorIcon="✨"
                      :useProxy="true"
                    />
                  </div>
                  <p class="skill-card-name">{{ skill.name }}</p>
                </div>
              </div>
              <div
                v-if="skill.tripods.length || skill.rune || skill.gemBadges.length"
                class="skill-tripod-rail"
                :class="{ 'skill-tripod-rail--compact': skill.isCompact }"
              >
                <div v-for="tripod in skill.tripods" :key="tripod.key" class="tripod-detail-inline">
                  <div class="tripod-inline-icon">
                    <LazyImage
                      v-if="tripod.icon"
                      :src="tripod.icon"
                      :alt="tripod.name"
                      width="40"
                      height="40"
                      imageClass="tripod-image"
                      errorIcon="🌀"
                      :useProxy="true"
                    />
                    <span v-else class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                      T{{ tripod.tier ?? '?' }}
                    </span>
                  </div>
                  <div class="tripod-inline-name">
                    <span class="tripod-name">{{ tripod.name }}</span>
                    <span v-if="tripod.levelLabel" class="tripod-level">{{ tripod.levelLabel }}</span>
                  </div>
                  <span class="tripod-desc" :class="{ 'tripod-desc--empty': !tripod.description }">
                    {{ tripod.description || '' }}
                  </span>
                  <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                </div>
                <div v-if="skill.rune" class="skill-rune skill-rune--inline">
                  <div class="skill-rune-icon">
                    <LazyImage
                      v-if="skill.rune.icon"
                      :src="skill.rune.icon"
                      :alt="skill.rune.name"
                      width="40"
                      height="40"
                      imageClass="rune-image"
                      errorIcon="💠"
                      :useProxy="true"
                    />
                  </div>
                  <div>
                    <p class="skill-rune-grade" :style="{ color: skill.rune.gradeColor || undefined }">
                      {{ skill.rune.grade || '룬' }}
                    </p>
                    <strong class="skill-rune-name">{{ skill.rune.name }}</strong>
                    <p
                      v-if="getRuneAffixView(skill.rune, skill.runeEffect)?.text"
                      class="skill-rune-description"
                    >
                      {{ getRuneAffixView(skill.rune, skill.runeEffect)!.text }}
                    </p>
                  </div>
                </div>
                <div v-for="gem in skill.gemBadges" :key="`gem-${skill.key}-${gem.key}`" class="tripod-detail-inline gem-detail">
                  <div class="tripod-inline-icon">
                    <span class="gem-icon-wrapper">💎</span>
                  </div>
                  <div class="tripod-inline-name">
                    <span class="tripod-name">{{ gem.name }}</span>
                    <span v-if="gem.levelLabel" class="tripod-level">{{ gem.levelLabel }}</span>
                  </div>
                  <span class="tripod-desc">
                    <span v-if="gem.effectLabel" class="gem-effect-label">{{ gem.effectLabel }}</span>
                    {{ gem.effectText }}
                    <span v-if="gem.extraEffect" class="gem-extra-effect">추가 효과: {{ gem.extraEffect }}</span>
                  </span>
                  <span class="tripod-slot">{{ gem.effectLabel || '보석' }}</span>
                </div>
              </div>
            </div>
          </article>
        </div>
      </section>
      <section
        v-for="section in skillSections"
        :key="section.key"
        class="skill-section"
        :class="section.modifier"
      >
        <div class="section-heading">
          <div>
            <h4>{{ section.title }}</h4>
          </div>
        </div>
        <div
          v-for="row in getSectionRows(section)"
          :key="row.key"
          :class="['skill-card-group', row.layout === 'pair' ? 'skill-card-group--pairs' : null]"
        >
          <template v-if="row.layout === 'pair' && row.pairs?.length">
            <div
              v-for="(pairRow, rowIndex) in getPairChunks(row.pairs)"
              :key="`pair-row-${row.key}-${rowIndex}`"
              class="skill-card-pair-row"
            >
              <div v-for="pair in pairRow" :key="pair.key" class="skill-card-pair">
                <div class="skill-card-pair-columns">
                  <div class="skill-card-pair-column">
                    <template v-if="pair.left">
                      <article
                      class="skill-card"
                      :class="{ 'skill-card--compact': pair.left.isCompact }"
                    >
                      <div class="skill-card-main">
                        <div class="skill-card-hero">
                <div class="skill-card-icon-block">
                  <div class="skill-icon-wrapper" tabindex="0">
                    <LazyImage
                      v-if="pair.left.icon"
                      :src="pair.left.icon"
                      :alt="pair.left.name"
                      width="40"
                      height="40"
                      imageClass="skill-card-icon"
                      errorIcon="✨"
                      :useProxy="true"
                    />
                  </div>
                  <p class="skill-card-name">{{ pair.left.name }}</p>
                  <div v-if="pair.left.gemBadges.length" class="skill-affix-row">
                    <span
                      v-for="gem in pair.left.gemBadges"
                      :key="`gem-affix-${pair.left.key}-${gem.key}`"
                      class="skill-affix skill-affix--gem"
                    >
                      <span class="affix-label">{{ gem.effectLabel || gem.name }}</span>
                      <span class="affix-text">{{ gem.effectText || gem.levelLabel || gem.name }}</span>
                    </span>
                  </div>
                </div>
                        </div>
                          <div
                          v-if="pair.left.tripods.length || pair.left.rune || pair.left.gemBadges.length"
                          class="skill-tripod-rail"
                          :class="{ 'skill-tripod-rail--compact': pair.left.isCompact }"
                        >
                          <div v-for="tripod in pair.left.tripods" :key="tripod.key" class="tripod-detail-inline">
                            <div class="tripod-inline-icon">
                              <LazyImage
                                v-if="tripod.icon"
                                :src="tripod.icon"
                                :alt="tripod.name"
                                width="40"
                                height="40"
                                imageClass="tripod-image"
                                errorIcon="🌀"
                                :useProxy="true"
                              />
                              <span v-else class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                                T{{ tripod.tier ?? '?' }}
                              </span>
                            </div>
                            <div class="tripod-inline-name">
                              <span class="tripod-name">{{ tripod.name }}</span>
                              <span v-if="tripod.levelLabel" class="tripod-level">{{ tripod.levelLabel }}</span>
                            </div>
                            <span class="tripod-desc" :class="{ 'tripod-desc--empty': !tripod.description }">
                              {{ tripod.description || '' }}
                            </span>
                            <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                          </div>
                          <div v-if="pair.left.rune" class="skill-rune skill-rune--inline">
                            <div class="skill-rune-icon">
                              <LazyImage
                                v-if="pair.left.rune.icon"
                                :src="pair.left.rune.icon"
                                  :alt="pair.left.rune.name"
                                  width="40"
                                  height="40"
                                  imageClass="rune-image"
                                  errorIcon="💠"
                                  :useProxy="true"
                                />
                              </div>
                              <div>
                                <p
                                  class="skill-rune-grade"
                                  :style="{ color: pair.left.rune.gradeColor || undefined }"
                                >
                                  {{ pair.left.rune.grade || '룬' }}
                                </p>
                                <strong class="skill-rune-name">{{ pair.left.rune.name }}</strong>
                                <p
                                  v-if="getRuneAffixView(pair.left.rune, pair.left.runeEffect)?.text"
                                  class="skill-rune-description"
                                >
                                  {{ getRuneAffixView(pair.left.rune, pair.left.runeEffect)!.text }}
                                </p>
                              </div>
                            </div>
                          </div>
                          <div v-for="gem in pair.left.gemBadges" :key="`gem-${pair.left.key}-${gem.key}`" class="tripod-detail-inline gem-detail">
                            <div class="tripod-inline-icon">
                              <span class="gem-icon-wrapper">💎</span>
                            </div>
                            <div class="tripod-inline-name">
                              <span class="tripod-name">{{ gem.name }}</span>
                              <span v-if="gem.levelLabel" class="tripod-level">{{ gem.levelLabel }}</span>
                            </div>
                            <span class="tripod-desc">
                              <span v-if="gem.effectLabel" class="gem-effect-label">{{ gem.effectLabel }}</span>
                              {{ gem.effectText }}
                              <span v-if="gem.extraEffect" class="gem-extra-effect">추가 효과: {{ gem.extraEffect }}</span>
                            </span>
                            <span class="tripod-slot">{{ gem.effectLabel || '보석' }}</span>
                          </div>
                        </div>
                      </article>
                    </template>
                    <article v-else class="skill-card skill-card--empty">
                      <span class="skill-card-column-chip">각성기</span>
                      <p>연결된 스킬이 없습니다.</p>
                    </article>
                  </div>
                  <div class="skill-card-pair-column">
                    <template v-if="pair.right">
                      <article
                      class="skill-card"
                      :class="{ 'skill-card--compact': pair.right.isCompact }"
                    >
                      <div class="skill-card-main">
                        <div class="skill-card-hero">
                <div class="skill-card-icon-block">
                  <div class="skill-icon-wrapper" tabindex="0">
                    <LazyImage
                      v-if="pair.right.icon"
                      :src="pair.right.icon"
                      :alt="pair.right.name"
                      width="40"
                      height="40"
                      imageClass="skill-card-icon"
                      errorIcon="✨"
                      :useProxy="true"
                    />
                  </div>
                      <p class="skill-card-name">{{ pair.right.name }}</p>
                      <div v-if="pair.right.gemBadges.length" class="skill-affix-row">
                        <span
                          v-for="gem in pair.right.gemBadges"
                          :key="`gem-affix-${pair.right.key}-${gem.key}`"
                          class="skill-affix skill-affix--gem"
                        >
                          <span class="affix-label">{{ gem.effectLabel || gem.name }}</span>
                          <span class="affix-text">{{ gem.effectText || gem.levelLabel || gem.name }}</span>
                        </span>
                      </div>
                    </div>
                        </div>
                        <div
                          v-if="pair.right.tripods.length || pair.right.rune || pair.right.gemBadges.length"
                          class="skill-tripod-rail"
                          :class="{ 'skill-tripod-rail--compact': pair.right.isCompact }"
                        >
                          <div v-for="tripod in pair.right.tripods" :key="tripod.key" class="tripod-detail-inline">
                            <div class="tripod-inline-icon">
                              <LazyImage
                                v-if="tripod.icon"
                                :src="tripod.icon"
                                :alt="tripod.name"
                                width="40"
                                height="40"
                                imageClass="tripod-image"
                                errorIcon="🌀"
                                :useProxy="true"
                              />
                              <span v-else class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                                T{{ tripod.tier ?? '?' }}
                              </span>
                            </div>
                            <div class="tripod-inline-name">
                              <span class="tripod-name">{{ tripod.name }}</span>
                              <span v-if="tripod.levelLabel" class="tripod-level">{{ tripod.levelLabel }}</span>
                            </div>
                            <span class="tripod-desc" :class="{ 'tripod-desc--empty': !tripod.description }">
                              {{ tripod.description || '' }}
                            </span>
                            <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                          </div>
                          <div v-if="pair.right.rune" class="skill-rune skill-rune--inline">
                            <div class="skill-rune-icon">
                              <LazyImage
                                v-if="pair.right.rune.icon"
                                :src="pair.right.rune.icon"
                                  :alt="pair.right.rune.name"
                                  width="40"
                                  height="40"
                                  imageClass="rune-image"
                                  errorIcon="💠"
                                  :useProxy="true"
                                />
                              </div>
                              <div>
                                <p
                                  class="skill-rune-grade"
                                  :style="{ color: pair.right.rune.gradeColor || undefined }"
                                >
                                  {{ pair.right.rune.grade || '룬' }}
                                </p>
                                <strong class="skill-rune-name">{{ pair.right.rune.name }}</strong>
                                <p
                                  v-if="getRuneAffixView(pair.right.rune, pair.right.runeEffect)?.text"
                                  class="skill-rune-description"
                                >
                                  {{ getRuneAffixView(pair.right.rune, pair.right.runeEffect)!.text }}
                                </p>
                              </div>
                            </div>
                          </div>
                          <div v-for="gem in pair.right.gemBadges" :key="`gem-${pair.right.key}-${gem.key}`" class="tripod-detail-inline gem-detail">
                            <div class="tripod-inline-icon">
                              <span class="gem-icon-wrapper">💎</span>
                            </div>
                            <div class="tripod-inline-name">
                              <span class="tripod-name">{{ gem.name }}</span>
                              <span v-if="gem.levelLabel" class="tripod-level">{{ gem.levelLabel }}</span>
                            </div>
                            <span class="tripod-desc">
                              <span v-if="gem.effectLabel" class="gem-effect-label">{{ gem.effectLabel }}</span>
                              {{ gem.effectText }}
                              <span v-if="gem.extraEffect" class="gem-extra-effect">추가 효과: {{ gem.extraEffect }}</span>
                            </span>
                            <span class="tripod-slot">{{ gem.effectLabel || '보석' }}</span>
                          </div>
                        </div>
                      </article>
                    </template>
                    <article v-else class="skill-card skill-card--empty">
                      <span class="skill-card-column-chip">초각성기</span>
                      <p>연결된 스킬이 없습니다.</p>
                    </article>
                  </div>
                </div>
              </div>
            </div>
          </template>
          <div v-else class="skill-card-grid skill-card-grid--limited">
            <template v-for="skill in getEnhancedSkills(row.cards)" :key="`${skill.key}-enhanced`">
              <article
                class="skill-card skill-card--enhanced-row"
                :class="{ 'skill-card--compact': skill.isCompact }"
              >
                <div class="skill-card-main">
                  <div class="skill-card-hero">
                    <div class="skill-card-icon-block">
                      <div class="skill-icon-wrapper" tabindex="0">
                        <LazyImage
                          v-if="skill.icon"
                          :src="skill.icon"
                          :alt="skill.name"
                          width="40"
                          height="40"
                          imageClass="skill-card-icon"
                          errorIcon="✨"
                          :useProxy="true"
                    />
                      </div>
                      <p class="skill-card-name">{{ skill.name }}</p>
                      <p class="skill-card-meta">
                        <span v-if="skill.levelLabel">{{ skill.levelLabel }}</span>
                      </p>
                    </div>
                    <div
                      v-if="skill.tripods.length || skill.rune || skill.gemBadges.length"
                      class="skill-tripod-rail"
                      :class="{ 'skill-tripod-rail--compact': skill.isCompact }"
                    >
                      <div v-for="tripod in skill.tripods" :key="tripod.key" class="tripod-detail-inline">
                        <div class="tripod-detail-icon">
                          <LazyImage
                            v-if="tripod.icon"
                            :src="tripod.icon"
                            :alt="tripod.name"
                            width="36"
                            height="36"
                            imageClass="tripod-image"
                            errorIcon="🌀"
                            :useProxy="true"
                          />
                          <span class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                              T{{ tripod.tier ?? '?' }}
                            </span>
                        </div>
                        <div class="tripod-detail-body">
                          <div class="tripod-detail-head">

                            <span class="tripod-name">{{ tripod.name }}</span>
                          <span v-if="tripod.description" class="tripod-desc">
                            {{ tripod.description }}
                          </span>
                            <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                            <span v-if="tripod.levelLabel" class="tripod-level">
                              {{ tripod.levelLabel }}
                            </span>
                          </div>
                        </div>
                      </div>
                      <div v-if="skill.rune" class="skill-rune skill-rune--inline">
                        <div class="skill-rune-icon">
                          <LazyImage
                            v-if="skill.rune.icon"
                            :src="skill.rune.icon"
                            :alt="skill.rune.name"
                            width="40"
                            height="40"
                            imageClass="rune-image"
                            errorIcon="💠"
                            :useProxy="true"
                          />
                        </div>
                          <p class="skill-rune-grade" :style="{ color: skill.rune.gradeColor || undefined }">
                            {{ skill.rune.grade || '룬' }}
                          </p>
                          <strong class="skill-rune-name">{{ skill.rune.name }}</strong>
                          <p
                            v-if="getRuneAffixView(skill.rune, skill.runeEffect)?.text"
                            class="skill-rune-description"
                          >
                            {{ getRuneAffixView(skill.rune, skill.runeEffect)!.text }}
                          </p>
                      </div>
                      <div v-for="gem in skill.gemBadges" :key="`gem-${skill.key}-${gem.key}`" class="tripod-detail-inline gem-detail">
                        <div class="tripod-inline-icon">
                          <span class="gem-icon-wrapper">💎</span>
                        </div>
                        <div class="tripod-inline-name">
                          <span class="tripod-name">{{ gem.name }}</span>
                          <span v-if="gem.levelLabel" class="tripod-level">{{ gem.levelLabel }}</span>
                        </div>
                        <span class="tripod-desc">
                          <span v-if="gem.effectLabel" class="gem-effect-label">{{ gem.effectLabel }}</span>
                          {{ gem.effectText }}
                          <span v-if="gem.extraEffect" class="gem-extra-effect">추가 효과: {{ gem.extraEffect }}</span>
                        </span>
                        <span class="tripod-slot">{{ gem.effectLabel || '보석' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </article>
            </template>

            <div v-if="getPlainSkills(row.cards).length" class="skill-card-inline-row">
              <article
                v-for="skill in getPlainSkills(row.cards)"
                :key="`${skill.key}-plain`"
                class="skill-card skill-card--inline"
                :class="{ 'skill-card--compact': skill.isCompact }"
              >
                <div class="skill-card-main">
                  <div class="skill-card-hero">
                    <div class="skill-card-icon-block">
                      <div class="skill-icon-wrapper" tabindex="0">
                        <LazyImage
                          v-if="skill.icon"
                          :src="skill.icon"
                          :alt="skill.name"
                          width="40"
                          height="40"
                          imageClass="skill-card-icon"
                          errorIcon="✨"
                          :useProxy="true"
                        />
                        <div
                          v-if="skill.description || skill.tripods.length"
                          class="skill-icon-tooltip"
                        >
                          <p class="skill-tooltip-title">{{ skill.name }}</p>
                          <p v-if="skill.description" class="skill-tooltip-desc">
                            {{ skill.description }}
                          </p>
                          <div v-if="skill.tripods.length" class="skill-tooltip-tripods">
                            <p class="skill-tooltip-sub">트라이포드</p>
                            <ul>
                              <li v-for="tripod in skill.tripods" :key="tripod.key" class="tripod-detail">
                                <div class="tripod-detail-icon">
                                  <LazyImage
                                    v-if="tripod.icon"
                                    :src="tripod.icon"
                                    :alt="tripod.name"
                                    width="32"
                                    height="32"
                                    imageClass="tripod-image"
                                    errorIcon="🌀"
                                    :useProxy="true"
                                  />
                                  <span v-else class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                                    T{{ tripod.tier ?? '?' }}
                                  </span>
                                </div>
                                <div class="tripod-detail-body">
                                  <div class="tripod-detail-head">
                                    <span class="tripod-tier-pill" :class="`tier-${tripod.tier ?? 'x'}`">
                                      T{{ tripod.tier ?? '?' }}
                                    </span>
                                    <span class="tripod-name">{{ tripod.name }}</span>
                                    <span v-if="tripod.description" class="tripod-desc">
                                      {{ tripod.description }}
                                    </span>
                                    <span class="tripod-slot">슬롯 {{ tripod.slotLabel }}</span>
                                    <span v-if="tripod.levelLabel" class="tripod-level">
                                      {{ tripod.levelLabel }}
                                    </span>
                                  </div>
                                </div>
                              </li>
                            </ul>
                          </div>
                          <div v-if="skill.rune" class="skill-tooltip-rune">
                            <p class="skill-tooltip-sub">룬</p>
                            <div class="skill-tooltip-rune-body">
                              <LazyImage
                                v-if="skill.rune.icon"
                                :src="skill.rune.icon"
                                :alt="skill.rune.name"
                                width="32"
                                height="32"
                                imageClass="rune-image"
                                errorIcon="💠"
                                :useProxy="true"
                              />
                              <div>
                                <p class="skill-tooltip-rune-name">{{ skill.rune.name }}</p>
                                <p v-if="skill.rune.description" class="skill-tooltip-rune-desc">
                                  {{ skill.rune.description }}
                                </p>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <p class="skill-card-name">{{ skill.name }}</p>
                      <p class="skill-card-meta">
                        <span v-if="skill.levelLabel">{{ skill.levelLabel }}</span>
                      </p>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>

      <section v-if="gemCards.length" class="skill-section skill-section--gems">
        <div class="section-heading">
          <div>
            <h4>보석 설정</h4>
            <p>슬롯별 보석 레벨과 연결된 스킬을 확인하세요.</p>
          </div>
        </div>
        <div class="gem-card-grid">
          <article v-for="gem in gemCards" :key="gem.key" class="gem-card">
            <header class="gem-card-head">
              <LazyImage
                v-if="gem.icon"
                :src="gem.icon"
                :alt="gem.name"
                width="48"
                height="48"
                imageClass="gem-card-icon"
                errorIcon="💎"
                :useProxy="true"
              />
              <div>
                <p class="gem-card-grade">{{ gem.grade || '보석' }}</p>
                <strong class="gem-card-name">{{ gem.name }}</strong>
                <span v-if="gem.levelLabel" class="gem-card-level">{{ gem.levelLabel }}</span>
              </div>
            </header>
            <p v-if="gem.skillName" class="gem-card-skill">
              적용 스킬: <strong>{{ gem.skillName }}</strong>
            </p>
            <p v-if="gem.skillDescription" class="gem-card-description">
              {{ gem.skillDescription }}
            </p>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LoadingSpinner from './LoadingSpinner.vue'
import EmptyState from './EmptyState.vue'
import ErrorMessage from './ErrorMessage.vue'
import LazyImage from './LazyImage.vue'
import { stripHtml } from '@/utils/tooltipParser'
import type { CombatSkill, SkillMenuResponse } from '@/api/types'

const props = defineProps<{
  response: SkillMenuResponse | null
  loading: boolean
  errorMessage: string | null
  characterName: string
}>()

defineEmits<{
  (e: 'retry'): void
}>()

interface SkillTripodView {
  key: string
  name: string
  icon?: string
  tier?: number
  levelLabel?: string
  description?: string
}

interface SkillRuneView {
  name: string
  grade?: string
  icon?: string
  description?: string
  gradeColor?: string
}

interface SkillGemBadge {
  key: string
  name: string
  levelLabel?: string
  effectText?: string
  effectLabel?: string
  extraEffect?: string
}

type AwakeningSkillKind = 'superSkill' | 'awakening'

interface SkillCardView {
  key: string
  name: string
  icon?: string
  levelLabel?: string
  typeLabel?: string
  pointLabel?: string
  description?: string
  tripods: SkillTripodView[]
  rune: SkillRuneView | null
  gemBadges: SkillGemBadge[]
  isCompact: boolean
  isAwakening: boolean
  awakeningType?: AwakeningSkillKind
  skillTypeCode?: number | null
  originalIndex: number
  runeEffect?: string
}

interface SkillSectionRow {
  key: string
  title?: string
  cards: SkillCardView[]
  layout?: 'grid' | 'pair'
  pairs?: AwakeningPairGroup[]
}

interface SkillSectionView {
  key: string
  title: string
  subtitle: string
  cards: SkillCardView[]
  modifier?: string
  rows?: SkillSectionRow[]
}

interface AwakeningPairGroup {
  key: string
  title: string
  left?: SkillCardView
  right?: SkillCardView
}

interface GemCardView {
  key: string
  name: string
  icon?: string
  grade?: string
  levelLabel?: string
  skillName?: string
  skillDescription?: string
}

const combatSkills = computed(() => props.response?.combatSkills ?? [])
const skillGems = computed(() => props.response?.skillGems ?? [])

const sanitizeInline = (value?: string | number | null) => {
  if (value === undefined || value === null) return ''
  const source = typeof value === 'number' ? String(value) : value
  return stripHtml(source).replace(/\s+/g, ' ').trim()
}

const normalizeSkillKey = (value?: string | null) =>
  sanitizeInline(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

const resolveGemBadgesForSkill = (skillName: string, map: Map<string, SkillGemBadge[]>) => {
  const key = normalizeSkillKey(skillName)
  if (map.has(key)) return [...map.get(key)!]
  for (const [mapKey, badges] of map.entries()) {
    if (key.includes(mapKey) || mapKey.includes(key)) {
      return [...badges]
    }
  }
  return []
}

const extractFontColor = (value?: string | null) => {
  if (!value) return ''
  const match = value.match(/color=['"]?#?([0-9a-fA-F]{6,8})['"]?/i)
  if (match) {
    const hex = match[1].slice(0, 6).toUpperCase()
    return `#${hex}`
  }
  return ''
}

const extractPairTitle = (value?: string | null) => {
  const sanitized = sanitizeInline(value)
  if (!sanitized) return ''
  const cloneTrigger = sanitized.indexOf(':(클론')
  if (cloneTrigger >= 0) {
    return sanitized.slice(0, cloneTrigger).trim()
  }
  const colonIndex = sanitized.indexOf(':')
  if (colonIndex >= 0) {
    return sanitized.slice(0, colonIndex).trim()
  }
  return sanitized
}

const SUPER_SKILL_CODES = new Set([101])
const AWAKENING_SKILL_CODES = new Set([100])

const AWAKENING_KIND_LABELS: Record<AwakeningSkillKind, string> = {
  superSkill: '초각성기',
  awakening: '각성기'
}

const parseSkillTypeCode = (value?: string | number | null) => {
  if (value === undefined || value === null) return null
  if (typeof value === 'number') {
    return Number.isNaN(value) ? null : value
  }
  const numeric = Number(sanitizeInline(value))
  if (Number.isNaN(numeric)) return null
  return numeric
}

const detectAwakeningKind = (
  skill: CombatSkill,
  parsedSkillType: number | null = parseSkillTypeCode(skill.skillType)
): AwakeningSkillKind | null => {
  if (parsedSkillType !== null) {
    if (SUPER_SKILL_CODES.has(parsedSkillType)) return 'superSkill'
    if (AWAKENING_SKILL_CODES.has(parsedSkillType)) return 'awakening'
  }

  const candidates = [skill.skillType, skill.type, skill.name]
    .map(value => sanitizeInline(value)?.toLowerCase())
    .filter(Boolean) as string[]

  if (candidates.some(value => /초\s*각성/.test(value))) {
    return 'superSkill'
  }
  if (candidates.some(value => /각성/.test(value))) {
    return 'awakening'
  }
  return null
}

const flattenTooltipLines = (tooltip?: string | null): string[] => {
  if (!tooltip) return []
  const bucket: string[] = []
  const visit = (node: unknown) => {
    if (!node) return
    if (typeof node === 'string') {
      bucket.push(node)
      return
    }
    if (Array.isArray(node)) {
      node.forEach(visit)
      return
    }
    if (typeof node === 'object') {
      Object.values(node as Record<string, unknown>).forEach(visit)
    }
  }
  try {
    visit(JSON.parse(tooltip))
  } catch {
    visit(tooltip)
  }
  return bucket
    .map(line => sanitizeInline(line))
    .filter(Boolean)
}

const extractNextLineAfterKeyword = (tooltip?: string | null, keyword?: string) => {
  if (!tooltip || !keyword) return ''
  const lines = flattenTooltipLines(tooltip)
  const idx = lines.findIndex(line => line.includes(keyword))
  if (idx === -1) return ''
  const next = lines.slice(idx + 1).find(Boolean)
  return next || ''
}

const normalizeGemEffectLabel = (effectText?: string | null) => {
  const text = sanitizeInline(effectText)
  if (!text) return ''
  const lowered = text.toLowerCase()
  if (/쿨타임|재사용|대기시간/.test(lowered) && /%/.test(lowered)) return '작열'
  if (/(피해|대미지|데미지)/.test(lowered) && /%/.test(lowered)) return '겁화'
  return text
}

const getRuneAffixView = (rune: SkillRuneView | null, effect?: string) => {
  if (!rune) return null
  return {
    label: [sanitizeInline(rune.grade), sanitizeInline(rune.name) || '룬'].filter(Boolean).join(' '),
    text: sanitizeInline(effect || rune.description || rune.grade),
    icon: rune.icon || undefined
  }
}

const summarizeTooltip = (tooltip?: string | null, fallback = '') => {
  const lines = flattenTooltipLines(tooltip)
  if (!lines.length) return fallback
  if (lines.length === 1) return lines[0]
  const preferred = lines.find((line, index) => index > 0 && !/레벨|Lv/i.test(line))
  return preferred ?? lines[0]
}

const formatLevelLabel = (level?: number | null, prefix = 'Lv.') => {
  if (typeof level !== 'number' || Number.isNaN(level)) return ''
  return `${prefix} ${level}`
}

const pickGemEffectText = (tooltip?: string | null, fallback?: string) => {
  const lines = flattenTooltipLines(tooltip)
  const idx = lines.findIndex(line => /보석\s*효과/.test(line))
  if (idx >= 0) {
    const candidate = lines.slice(idx + 1).find(Boolean)
    if (candidate) return sanitizeInline(candidate)
  }
  const firstMeaningful = lines.find(line => line && !/보석\s*효과/.test(line))
  if (firstMeaningful) return sanitizeInline(firstMeaningful)
  return sanitizeInline(fallback)
}

const parseGemTooltipMapping = (tooltip?: string | null) => {
  if (!tooltip) return null
  try {
    const parsed = JSON.parse(tooltip)
    const part = parsed?.Element_007?.value?.Element_001 ?? parsed?.Element_007?.value?.Element_000
    if (part) {
      const skillMatch = part.match(/<FONT[^>]*>([^<]+)<\/FONT>/i)
      const skillName = sanitizeInline(skillMatch?.[1])
      const lines = flattenTooltipLines(part)
      let effectText = ''
      let extraEffect = ''
      if (lines.length) {
        const first = lines[0]
        if (skillName && first.includes(skillName)) {
          effectText = sanitizeInline(first.replace(skillName, '').replace(/\[[^\]]+\]\s*/, ''))
        } else {
          effectText = sanitizeInline(first.replace(/^\[[^\]]+\]\s*/, ''))
        }
        const extraIdx = lines.findIndex(line => /추가\s*효과/.test(line))
        if (extraIdx >= 0) {
          const after = lines.slice(extraIdx + 1).find(Boolean)
          if (after) extraEffect = sanitizeInline(after)
        }
      }
      return {
        skillName,
        effectText,
        extraEffect
      }
    }
  } catch {
    // ignore
  }
  return null
}

const gemBadgesBySkill = computed(() => {
  const map = new Map<string, SkillGemBadge[]>()
  const effectSkills = ((props.response as any)?.effects?.skills ??
    (props.response as any)?.Effects?.Skills ??
    []) as any[]
  const inventoryGems = ((props.response as any)?.gems ??
    (props.response as any)?.Gems ??
    []) as any[]

  skillGems.value.forEach((gem, index) => {
    const skillName = sanitizeInline(gem.skill?.name) || ''
    const key = normalizeSkillKey(skillName)
    if (!key) return
    const effectTextRaw =
      pickGemEffectText(gem.tooltip, gem.skill?.description) ||
      extractNextLineAfterKeyword(gem.tooltip, '보석 효과') ||
      extractNextLineAfterKeyword(gem.tooltip, '보석효과') ||
      sanitizeInline(gem.skill?.description)
    const effectLabel = normalizeGemEffectLabel(effectTextRaw)
    const badge: SkillGemBadge = {
      key: `${skillName}-${index}`,
      name: sanitizeInline(gem.name) || '보석',
      levelLabel: formatLevelLabel(gem.level),
      effectText: sanitizeInline(effectTextRaw),
      effectLabel
    }
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key)!.push(badge)
  })

  effectSkills.forEach((effect, index) => {
    const skillName = sanitizeInline(effect?.Name) || ''
    const key = normalizeSkillKey(skillName)
    if (!key) return
    const effectText = sanitizeInline(
      Array.isArray(effect?.Description) ? effect.Description.join(' ') : effect?.Description
    )
    const badge: SkillGemBadge = {
      key: `${skillName}-effect-${index}`,
      name: sanitizeInline(effect?.Name) || '보석',
      levelLabel: sanitizeInline(effect?.Option),
      effectText,
      effectLabel: normalizeGemEffectLabel(effectText)
    }
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(badge)
  })

  inventoryGems.forEach((gem, index) => {
    const parsed = parseGemTooltipMapping(gem?.Tooltip)
    const skillName = parsed?.skillName
    const key = normalizeSkillKey(skillName)
    if (!key) return
    const badge: SkillGemBadge = {
      key: `${skillName}-inv-${index}`,
      name: sanitizeInline(gem?.Name) || '보석',
      levelLabel: formatLevelLabel(gem?.Level),
      effectText: parsed.effectText,
      extraEffect: parsed.extraEffect,
      effectLabel: normalizeGemEffectLabel(parsed.effectText)
    }
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(badge)
  })

  return map
})

const skillCards = computed<SkillCardView[]>(() => {
  if (!combatSkills.value.length) return []
  const annotated = combatSkills.value.map((skill, originalIndex) => ({ skill, originalIndex }))
  const sorted = annotated.sort((a, b) => {
    const levelA = typeof a.skill.level === 'number' ? a.skill.level : -1
    const levelB = typeof b.skill.level === 'number' ? b.skill.level : -1
    if (levelA === levelB) {
      return sanitizeInline(b.skill.name).localeCompare(sanitizeInline(a.skill.name))
    }
    return levelB - levelA
  })
  return sorted
    .filter(entry => entry.skill.name)
    .map((entry, index) => {
      const skill = entry.skill
      const name = sanitizeInline(skill.name) || `스킬 ${index + 1}`
      const parsedSkillType = parseSkillTypeCode(skill.skillType)
      const awakeningKind = detectAwakeningKind(skill, parsedSkillType)
      const typeParts: string[] = []
      const payloadType = sanitizeInline(skill.type)
      if (awakeningKind) {
        const explicitLabel =
          parsedSkillType === 100
            ? '각성기'
            : parsedSkillType === 101
              ? '초각성기'
              : null
        typeParts.push(explicitLabel || AWAKENING_KIND_LABELS[awakeningKind])
      }
      if (payloadType) {
        typeParts.push(payloadType)
      }
      const isLowLevel = typeof skill.level === 'number' && skill.level < 4
      const isAwakening = Boolean(awakeningKind)

      const rune = skill.rune?.name
        ? {
            name: sanitizeInline(skill.rune.name),
            grade: sanitizeInline(skill.rune.grade),
            icon: skill.rune.icon || undefined,
            description: summarizeTooltip(skill.rune.tooltip, sanitizeInline(skill.rune.tooltip)),
            gradeColor: extractFontColor(skill.rune.tooltip)
          }
        : null

      const tripods =
        skill.tripods
          ?.filter(tripod => tripod.name && tripod.selected !== false)
          .map((tripod, tripodIndex) => ({
            key: `${name}-tripod-${tripodIndex}`,
            name: sanitizeInline(tripod.name) || `트라이포드 ${tripodIndex + 1}`,
            icon: tripod.icon || undefined,
            tier: tripod.tier + 1 ?? undefined,
            slot: typeof tripod.slot === 'number' ? tripod.slot : undefined,
            slotLabel: typeof tripod.slot === 'number' ? `${tripod.slot}번` : `${tripodIndex + 1}번`,
            levelLabel: formatLevelLabel(tripod.level),
            description: summarizeTooltip(tripod.tooltip, sanitizeInline(tripod.tooltip))
          })) ?? []

      const gemBadges = resolveGemBadgesForSkill(name, gemBadgesBySkill.value)
      const isCompact = isLowLevel && tripods.length === 0 && !rune && !isAwakening

      const tooltipLines = flattenTooltipLines(skill.tooltip)

      return {
        key: `${name}-${skill.level ?? index}`,
        name,
        icon: skill.icon || undefined,
        levelLabel: formatLevelLabel(skill.level),
        typeLabel: typeParts.join(' · ') || undefined,
        pointLabel: typeof skill.skillPoints === 'number' ? `${skill.skillPoints.toLocaleString()} 포인트` : undefined,
        description: summarizeTooltip(skill.tooltip, sanitizeInline(skill.tooltip)),
        tooltipLines,
        tripods,
        rune,
        gemBadges,
        isCompact,
        isAwakening,
        awakeningType: awakeningKind ?? undefined,
        skillTypeCode: parsedSkillType ?? undefined,
        originalIndex: entry.originalIndex,
        runeEffect: skill.rune ? extractNextLineAfterKeyword(skill.rune.tooltip ?? skill.rune.description, '스킬 룬 효과') : undefined
      }
    })
})

const awakeningPairCandidates = computed(() =>
  [...skillCards.value]
    .filter(card => card.skillTypeCode === 100 || card.skillTypeCode === 101)
    .sort((a, b) => a.originalIndex - b.originalIndex)
)

const classicAwakeningPairs = computed<AwakeningPairGroup[]>(() => {
  const ordered: AwakeningPairGroup[] = []
  const pendingLeft: AwakeningPairGroup[] = []

  awakeningPairCandidates.value.forEach(card => {
    const title = extractPairTitle(card.name) || '연관 스킬'
    if (card.skillTypeCode === 100) {
      const pair: AwakeningPairGroup = {
        key: `awakening-${card.originalIndex}`,
        title,
        left: card
      }
      ordered.push(pair)
      pendingLeft.push(pair)
    } else if (card.skillTypeCode === 101) {
      let target = pendingLeft.find(p => !p.right)
      if (target) {
        target.right = card
        pendingLeft.splice(pendingLeft.indexOf(target), 1)
      } else {
        const pair: AwakeningPairGroup = {
          key: `awakening-${card.originalIndex}`,
          title,
          right: card
        }
        ordered.push(pair)
      }
    }
  })

  return ordered
})
const regularSkillCards = computed(() =>
  skillCards.value.filter(card => !card.isAwakening && card.skillTypeCode === 0)
)
const superSkillHighlights = computed(() =>
  skillCards.value.filter(card => card.skillTypeCode === 1)
)

const skillSections = computed<SkillSectionView[]>(() => {
  const sections: SkillSectionView[] = []
  const awakeningRows: SkillSectionRow[] = []
  if (classicAwakeningPairs.value.length) {
    awakeningRows.push({
      key: 'paired-awakening',
      title: '각성기·초각성기',
      cards: [],
      layout: 'pair',
      pairs: classicAwakeningPairs.value
    })
  }
  if (awakeningRows.length) {
    sections.push({
      key: 'awakening',
      title: '각성·초각성기',
      subtitle: '각성기와 초각성기를 구분하여 확인하세요.',
      cards: awakeningPairCandidates.value,
      modifier: 'skill-section--awakening',
      rows: awakeningRows
    })
  }
  if (regularSkillCards.value.length) {
    sections.push({
      key: 'combat',
      title: '전투 스킬 트리',
      subtitle: '선택된 트라이포드, 룬, 보석 정보를 한눈에 살펴보세요.',
      cards: regularSkillCards.value,
      rows: [
        {
          key: 'combat-grid',
          cards: regularSkillCards.value,
          layout: 'grid'
        }
      ]
    })
  }
  return sections
})

const gemCards = computed<GemCardView[]>(() => {
  if (!skillGems.value.length) return []
  return skillGems.value
    .filter(gem => gem.name || gem.skill?.name)
    .map((gem, index) => ({
      key: `${gem.name ?? 'gem'}-${index}`,
      name: sanitizeInline(gem.name) || '보석',
      icon: gem.icon || undefined,
      grade: sanitizeInline(gem.grade),
      levelLabel: formatLevelLabel(gem.level),
      skillName: sanitizeInline(gem.skill?.name),
      skillDescription: sanitizeInline(gem.skill?.description)
    }))
})

const hasRenderableContent = computed(() => skillCards.value.length > 0 || gemCards.value.length > 0)

const emptyStateDescription = computed(() => {
  if (!props.characterName) {
    return '캐릭터를 검색하면 전투 스킬 프리셋을 불러옵니다.'
  }
  return `'${props.characterName}' 캐릭터의 스킬 프리셋이 감지되지 않았어요. 인게임에서 스킬을 저장했는지 확인해 주세요.`
})

const getSectionRows = (section: SkillSectionView): SkillSectionRow[] => {
  if (section.rows?.length) {
    return section.rows
  }
  return [
    {
      key: section.key,
      cards: section.cards,
      layout: 'grid'
    }
  ]
}

const isEnhancedSkill = (skill: SkillCardView) =>
  Boolean(skill.pointLabel || skill.rune || skill.tripods.length || (skill.gemBadges && skill.gemBadges.length))

const getEnhancedSkills = (cards: SkillCardView[]) => cards.filter(isEnhancedSkill)
const getPlainSkills = (cards: SkillCardView[]) => cards.filter(card => !isEnhancedSkill(card))

const getPairChunks = (pairs?: AwakeningPairGroup[] | null, chunkSize = 2): AwakeningPairGroup[][] => {
  if (!pairs || !pairs.length || chunkSize <= 0) return []
  const chunks: AwakeningPairGroup[][] = []
  for (let i = 0; i < pairs.length; i += chunkSize) {
    chunks.push(pairs.slice(i, i + chunkSize))
  }
  return chunks
}
</script>

<style scoped>
.skill-panel-shell {
  width: 100%;
  --icon-scale: 0.8;
  font-size: 0.92rem;
}

.skill-panel-placeholder {
  padding: 32px;
  border-radius: 16px;
  border: 1px dashed var(--border-color, #e5e7eb);
  background: var(--surface-muted, #f9fafb);
}

.skill-panel-retry {
  margin-top: 12px;
  padding: 8px 16px;
  border-radius: 9999px;
  border: 1px solid var(--border-color, #d1d5db);
  background: transparent;
  cursor: pointer;
  font-weight: 500;
}

.skill-panel-layout {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-heading h4 {
  margin: 0 0 4px;
  font-size: 1rem;
  color: var(--text-primary, #1f2937);
}

.section-heading p {
  margin: 0;
  color: var(--text-muted, #6b7280);
  font-size: 0.85rem;
}

.skill-section {
  padding: 15px;
  border-radius: 16px;
  border: 1px solid var(--border-color, #e5e7eb);
  background: var(--card-bg, #fbfbfb);
  width: fit-content;
}

.skill-section--awakening {
  border-color: rgba(251, 146, 60, 0.5);
  background: rgba(251, 191, 36, 0.08);
}

.skill-section--highlight {
  border-color: rgba(59, 130, 246, 0.4);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.08), rgba(191, 219, 254, 0.15));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.skill-card-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skill-card-group--pairs {
  gap: 16px;
}

.skill-card-pair-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.skill-card-group--pairs .skill-card-pair {
  margin-top: 0;
}

.skill-card-row-title {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-secondary, #374151);
}

.skill-card-grid {
  display: flex;
  flex-direction: column;
  /* gap: 12px; */
}

.super-skill-grid {
  margin-top: 5px;
  flex-direction: row;
  gap: 12px;
}

.super-skill-grid > .skill-card {
  /* flex: 1 1 320px; */
  max-width: 420px;
}

.skill-card-pair {
  margin-top: 8px;
  /* padding: 16px; */
  border-radius: 12px;
  /* border: 1px solid var(--border-color, #e5e7eb); */
  /* background: var(--surface-color, #fff); */
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.skill-card-pair-name {
  margin: 0;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
}

.skill-card-pair-columns {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
}

.skill-card-pair-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* min-width: 180px; */
  flex: 1 1 0;
}

.skill-card-column-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(31, 41, 55, 0.08);
  color: var(--text-secondary, #4b5563);
  font-size: 0.75rem;
  font-weight: 600;
  margin-bottom: 6px;
}

.skill-card--empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  width: 100%;
  font-size: 0.9rem;
  color: var(--text-muted, #9ca3af);
  background: var(--surface-muted, #f9fafb);
}

.skill-card--empty .skill-card-column-chip {
  margin-right: 8px;
}

.skill-card {
  width: 100%;
  border-radius: 16px;
  padding: 10px;
  /* background: var(--surface-color, #fff); */
}

.skill-card-inline-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(50px, 1fr));
  gap: 12px;
}

.skill-card--enhanced-row {
  width: 100%;
  border-bottom: 1px dashed lightgray;
}

.skill-card--inline {
  width: fit-content;
  min-width: 200px;
  flex: 1 1 220px;
  max-width: 360px;
}

.skill-card-main {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  width: 100%;
}

.skill-card-hero {
  display: flex;
  width: 100%;
  gap: 16px;
  /* min-width: fit-content; */
}

.skill-card-icon-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
  width: 55px;
}

.skill-card-icon {
  width: calc(64px - 15px);
  height: calc(64px - 15px);
}

.skill-rune-icon{
  width: fit-content;
  height: fit-content;
}

.skill-icon-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.skill-icon-wrapper:focus-visible .skill-icon-tooltip,
.skill-icon-wrapper:hover .skill-icon-tooltip {
  opacity: 1;
  pointer-events: auto;
}

.skill-icon-tooltip {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 10px);
  width: min(320px, 80vw);
  padding: 12px;
  border-radius: 12px;
  background: rgba(17, 24, 39, 0.95);
  color: #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease;
  z-index: 10;
}

.skill-tooltip-title {
  margin: 0 0 6px;
  font-weight: 700;
  font-size: 0.95rem;
}

.skill-tooltip-desc {
  margin: 0 0 10px;
  font-size: 0.85rem;
  line-height: 1.4;
}

.skill-tooltip-tripods {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 8px;
}

.skill-tooltip-tripods ul {
  list-style: none;
  padding: 0;
  margin: 6px 0 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.skill-tooltip-tripods li {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 0.82rem;
}

.tripod-tier-pill {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 32px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.75rem;
  position: absolute;
  z-index:100;
  text-shadow: -1px 0px white, 0px 1px white, 1px 0px white, 0px -1px white;
}

.tripod-name {
  /* flex: 1; */
  min-width: 120px;
  padding:0px 10px;
  font-weight: 600;
}

.tripod-slot {
  font-weight: 600;
  color: #a5b4fc;
}

.tripod-level {
  font-weight: 600;
  color: #c7d2fe;
}

.tripod-inline-name {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  white-space: nowrap;
}

.tripod-inline-name .tripod-name {
  width: 100px;
  min-width: 100px;
  max-width: 100px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tripod-detail-inline .tripod-desc {
  text-overflow: ellipsis;
  min-width: 0;
}

.tripod-detail-inline .tripod-desc.tripod-desc--empty {
  visibility: hidden;
}

.tripod-detail-inline .tripod-slot {
  min-width: 70px;
  max-width: 70px;
  text-align: center;
  white-space: nowrap;
  margin-left: auto;
}

.tripod-desc {
  display: block;
  /* color: #e5e7eb; */
  font-size: 0.8rem;
  line-height: 1.4;
}

.tripod-detail {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  background: var(--surface-muted, #f3f4f6);
}

.tripod-detail-inline {
  display: grid;
  grid-template-columns: 40px 1fr;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 12px;
  background: var(--surface-muted, #f3f4f6);
}

.tripod-inline-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.tripod-detail-icon {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.tripod-detail-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tripod-detail-body {
  gap: 4px;
  width: 100%;
}

.skill-tooltip-rune {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 8px;
  margin-top: 8px;
}

.skill-tooltip-rune-body {
  display: flex;
  gap: 8px;
  align-items: center;
}

.skill-tooltip-rune-name {
  margin: 0;
  font-weight: 700;
}

.skill-tooltip-rune-desc {
  margin: 2px 0 0;
  font-size: 0.82rem;
  color: #e5e7eb;
}
.tripod-desc {
  display: block;
  /* color: #e5e7eb; */
  font-size: 0.8rem;
  line-height: 1.4;
  word-break: keep-all;
  white-space: pre-wrap;
}

.skill-card-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.skill-card-name {
  margin: 0;
  font-size: 0.75rem;
  color: var(--text-primary, #1f2937);
  font-weight: 600;
  word-break: keep-all;
}

.skill-card-meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  color: var(--text-muted, #6b7280);
  font-size: 0.82rem;
  font-weight: 600;
}

.skill-affix-row {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-affix {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--surface-muted, #f3f4f6);
  color: var(--text-secondary, #374151);
  font-size: 0.82rem;
}

.skill-affix--rune {
  background: rgba(37, 99, 235, 0.08);
}

.skill-affix--gem {
  background: rgba(16, 185, 129, 0.08);
}

.affix-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.affix-icon-image {
  width: 20px;
  height: 20px;
}

.affix-label {
  font-weight: 700;
  color: var(--text-primary, #111827);
}

.affix-text {
  color: var(--text-secondary, #374151);
}

.skill-card-description,
.skill-rune-description,
.gem-card-description {
  margin: 0;
  color: var(--text-primary, #374151);
  white-space: pre-line;
  line-height: 1.5;
}

.skill-gem-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-gem-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 9999px;
  background: var(--surface-muted, #f3f4f6);
  font-size: 0.85rem;
  color: var(--text-secondary, #374151);
}

.gem-badge-level {
  font-weight: 600;
  color: var(--accent-color, #2563eb);
}

.skill-tripod-rail {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  /* flex: 1 1 260px; */
  align-items: stretch;
}

.skill-tripod-rail--compact {
  flex: 0 0 auto;
  flex-wrap: nowrap;
  align-items: center;
  gap: 8px;
}

.tripod-pill {
  display: inline-flex;
  align-items: center;
  /* gap: 6px; */
  padding: 4px;
  border-radius: 999px;
  background: var(--surface-muted, #f3f4f6);
  font-size: 0.82rem;
  color: var(--text-secondary, #374151);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.tripod-pill-slot {
  font-weight: 700;
}

.tripod-pill-level {
  color: var(--text-muted, #6b7280);
  font-weight: 600;
}

.tripod-pill[class*='slot-1'] {
  background: rgba(96, 165, 250, 0.12);
  border-color: rgba(96, 165, 250, 0.35);
  color: #1d4ed8;
}

.tripod-pill[class*='slot-2'] {
  background: rgba(52, 211, 153, 0.12);
  border-color: rgba(52, 211, 153, 0.35);
  color: #0f766e;
}

.tripod-pill[class*='slot-3'] {
  background: rgba(251, 191, 36, 0.12);
  border-color: rgba(251, 191, 36, 0.35);
  color: #b45309;
}

.skill-rune {
  /* gap: 12px; */
  display: flex;
  padding: 4px 10px;
  border-radius: 12px;
  background: rgba(37, 99, 235, 0.07);
  text-align: center;
  width: fit-content;
}

.skill-rune--inline {
  margin-top: 8px;
}

.skill-tripod-rail .skill-rune--inline {
  margin-top: 0;
  align-items: center;
  text-align: left;
  gap: 8px;
}

.skill-tripod-rail .skill-rune--inline .skill-rune-name {
  display: block;
  width: 80px;
  min-width: 80px;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-tripod-rail .skill-rune--inline .skill-rune-description {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.skill-gem-line {
  background: rgba(16, 185, 129, 0.08);
}

.skill-gem-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.skill-gem-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
}

.skill-gem-line .skill-rune-name {
  color: var(--text-primary, #1f2937);
}

.skill-gem-line .skill-rune-description {
  margin: 0;
}

.skill-card--compact .skill-card-main {
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
}

.skill-card--compact .skill-card-hero {
  flex: 0 0 auto;
  min-width: 0;
  gap: 12px;
}

.skill-card--compact .skill-card-info {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.skill-card--compact .skill-card-meta {
  flex-wrap: nowrap;
  gap: 6px;
  white-space: nowrap;
}

.skill-card--compact .skill-card-icon-block {
  min-width: calc(64px - 15px);
}

.skill-card--compact .skill-tripod-rail {
  margin-left: auto;
}

.skill-rune-grade {
  padding-left:10px;
  margin: 0;
  font-size: 0.80rem;
  font-weight: 600;
  color: var(--text-muted, #6b7280);
}

.skill-rune-name {
  display: block;
  font-size: 0.8rem;
  color: var(--text-primary, #1f2937);
}

.skill-rune-description{
  font-size: 0.8rem;
}

.gem-card-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.gem-card {
  border: 1px solid var(--border-color, #e5e7eb);
  border-radius: 16px;
  padding: 16px;
  background: var(--surface-color, #fff);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.gem-card-head {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rune-image,
.gem-card-icon {
  width: calc(40px - 15px);
  height: calc(40px - 15px);
}

.gem-card-grade {
  margin: 0;
  font-size: 0.78rem;
  color: var(--text-muted, #6b7280);
}

.gem-card-name {
  font-size: 0.9rem;
  color: var(--text-primary, #1f2937);
}

.gem-card-level {
  font-size: 0.78rem;
  color: var(--accent-color, #2563eb);
  font-weight: 600;
}

.gem-card-skill {
  margin: 0;
  font-size: 0.82rem;
  color: var(--text-secondary, #374151);
}

/* Gem detail styles (tripod-like layout) */
.gem-detail {
  background: rgba(16, 185, 129, 0.05);
  border-left: 3px solid rgba(16, 185, 129, 0.4);
}

.gem-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
}

.gem-effect-label {
  display: inline-block;
  padding: 2px 8px;
  margin-right: 6px;
  background: rgba(16, 185, 129, 0.15);
  border-radius: 4px;
  font-weight: 600;
  color: var(--text-primary, #1f2937);
  font-size: 0.75rem;
}

.gem-extra-effect {
  display: block;
  margin-top: 4px;
  font-style: italic;
  color: var(--text-secondary, #6b7280);
}

@media (max-width: 768px) {
  .skill-card-main {
    flex-direction: column;
  }

  .skill-card-hero {
    flex-direction: column;
    min-width: 0;
  }

  .skill-tripod-rail {
    width: 100%;
  }

  .skill-card-grid {
    flex-direction: column;
  }

  .gem-card-grid {
    grid-template-columns: 1fr;
  }

  .skill-card-pair-row {
    grid-template-columns: 1fr;
  }

  .skill-card-pair-columns {
    flex-direction: column;
  }
}
</style>
