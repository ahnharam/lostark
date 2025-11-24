<template>
  <section class="detail-panel summary-panel">
    <div v-if="activeCharacter" class="summary-grid summary-grid--modules summary-grid--stacked">
      <article class="summary-card summary-card--module summary-card--equipment">
        <div class="summary-card__head">
          <p class="summary-eyebrow">장비</p>
          <h5>무기/방어구</h5>
        </div>
        <p v-if="detailLoading" class="summary-note">장비 정보를 정리하는 중입니다...</p>
        <p v-else-if="detailError" class="summary-note summary-note--warning">{{ detailError }}</p>
        <div v-else class="equipment-grid">
          <div class="equipment-column">
            <ul class="summary-list summary-list--flat">
              <li
                v-for="item in equipmentSummary.left"
                :key="item.key"
                class="summary-list-item summary-list-item--plain"
              >
                <LazyImage
                  :src="item.icon"
                  :alt="item.name"
                  width="32"
                  height="32"
                  imageClass="summary-icon"
                  errorIcon="🗡️"
                  :useProxy="true"
                />
                <div class="summary-list-text">
                  <p class="summary-title">{{ item.name }}</p>
                  <p class="summary-sub">{{ item.typeLabel }}</p>
                  <p class="summary-inline">{{ item.meta }}</p>
                </div>
                <div class="summary-pill-col">
                  <span v-if="item.itemLevel" class="summary-pill summary-pill--primary">
                    {{ item.itemLevel }}
                  </span>
                  <span v-if="item.quality" class="summary-pill summary-pill--ghost">
                    품질 {{ item.quality }}
                  </span>
                  <span v-if="item.transcend" class="summary-pill summary-pill--accent">
                    초월 {{ item.transcend }}
                  </span>
                </div>
              </li>
            </ul>
          </div>
          <div class="equipment-column">
            <h5>장신구</h5>
            <ul class="summary-list summary-list--flat">
              <li
                v-for="item in equipmentSummary.right"
                :key="item.key"
                class="summary-list-item summary-list-item--plain"
              >
                <LazyImage
                  :src="item.icon"
                  :alt="item.name"
                  width="32"
                  height="32"
                  imageClass="summary-icon"
                  errorIcon="💍"
                  :useProxy="true"
                />
                <div class="summary-list-text">
                  <p class="summary-title">{{ item.name }}</p>
                  <p class="summary-sub">{{ item.typeLabel }}</p>
                  <p class="summary-inline">{{ item.meta }}</p>
                </div>
                <div class="summary-pill-col">
                  <span v-if="item.quality" class="summary-pill summary-pill--ghost">
                    품질 {{ item.quality }}
                  </span>
                  <span v-if="item.special" class="summary-pill summary-pill--primary">
                    {{ item.special }}
                  </span>
                  <span v-if="item.transcend" class="summary-pill summary-pill--accent">
                    초월 {{ item.transcend }}
                  </span>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--ark">
        <div class="summary-card__head">
          <p class="summary-eyebrow">아크 그리드</p>
          <h4>{{ arkSummary.passiveTitle || '아크 루트 정보' }}</h4>
        </div>
        <p v-if="arkGridLoading" class="summary-note">아크 그리드 정보를 불러오는 중입니다...</p>
        <p v-else-if="arkGridError" class="summary-note summary-note--warning">{{ arkGridError }}</p>
        <div v-else class="ark-core-layout">
          <div v-if="arkSummary.appliedPoints.length" class="summary-pill-row summary-pill-row--wrap">
            <span
              v-for="point in arkSummary.appliedPoints"
              :key="point.key"
              class="summary-pill summary-pill--primary"
            >
              {{ point.label }} · {{ point.value }}
            </span>
          </div>
          <div v-if="arkSummary.coreSlots.length" class="ark-core-grid">
            <div
              v-for="slot in arkSummary.coreSlots"
              :key="slot.key"
              class="ark-core"
            >
              <div class="ark-core__thumb">
                <LazyImage
                  v-if="slot.icon"
                  :src="slot.icon"
                  :alt="slot.name"
                  width="64"
                  height="64"
                  imageClass="ark-core__image"
                  errorIcon="🧩"
                  :useProxy="true"
                />
                <div v-else class="ark-core__placeholder" aria-hidden="true">
                  {{ slot.initial }}
                </div>
                <span v-if="slot.pointLabel" class="ark-core__point">{{ slot.pointLabel }}</span>
              </div>
              <p class="ark-core__name">{{ slot.name }}</p>
            </div>
          </div>
          <p
            v-if="!arkSummary.coreSlots.length && !arkSummary.appliedPoints.length"
            class="summary-note"
          >
            표시할 아크 그리드 정보가 없습니다.
          </p>
        </div>
      </article>

      <article class="summary-card summary-card--module summary-card--arkpassive">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">아크 패시브</p>
            <h4>진화 · 깨달음 · 도약</h4>
          </div>
        </div>
        <div v-if="arkSummary.corePassives.length" class="summary-list summary-list--flat">
          <div
            v-for="effect in arkSummary.corePassives"
            :key="effect.key"
            class="summary-list-item summary-list-item--plain"
          >
            <LazyImage
              v-if="effect.icon"
              :src="effect.icon"
              :alt="effect.title"
              width="32"
              height="32"
              imageClass="summary-icon"
              errorIcon="🌟"
              :useProxy="true"
            />
            <div class="summary-list-text">
              <p class="summary-title">{{ effect.title }}</p>
              <p class="summary-sub">{{ effect.subtitle }}</p>
            </div>
            <span class="summary-pill summary-pill--primary">{{ effect.levelLabel || 'Lv.1' }}</span>
          </div>
        </div>
        <p v-else class="summary-note">패시브 정보가 없습니다.</p>
      </article>

      <article class="summary-card summary-card--module summary-card--skills">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">스킬</p>
            <h4>핵심 스킬 라인업</h4>
          </div>
          <span class="summary-chip" :class="{ 'summary-chip--muted': !skillHighlights.length }">
            {{ skillHighlights.length ? `${skillHighlights.length}개` : '데이터 없음' }}
          </span>
        </div>
        <p v-if="skillLoading" class="summary-note">스킬 정보를 불러오는 중입니다...</p>
        <p v-else-if="skillError" class="summary-note summary-note--warning">{{ skillError }}</p>
        <ul v-else-if="skillHighlights.length" class="summary-list summary-list--flat">
          <li
            v-for="skill in skillHighlights"
            :key="skill.key"
            class="summary-list-item summary-list-item--plain"
          >
            <LazyImage
              :src="skill.icon"
              :alt="skill.name"
              width="34"
              height="34"
              imageClass="summary-icon"
              errorIcon="🎯"
              :useProxy="true"
            />
            <div class="summary-list-text">
              <p class="summary-title">{{ skill.name }}</p>
              <p class="summary-sub">스킬 포인트 {{ skill.pointLabel }}</p>
              <div class="summary-pill-row summary-pill-row--wrap">
                <span v-if="skill.levelLabel" class="summary-pill summary-pill--primary">
                  {{ skill.levelLabel }}
                </span>
                <span v-if="skill.rune" class="summary-pill summary-pill--accent">
                  {{ skill.rune }}
                </span>
                <span v-if="skill.gemLabel" class="summary-pill summary-pill--ghost">
                  {{ skill.gemLabel }}
                </span>
                <span v-if="skill.tripodLabel" class="summary-pill summary-pill--ghost">
                  {{ skill.tripodLabel }}
                </span>
              </div>
            </div>
          </li>
        </ul>
        <p v-else class="summary-note">요약할 스킬 정보가 없습니다.</p>
      </article>

      <article class="summary-card summary-card--module summary-card--engravings">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">각인</p>
            <h4>전설 · 유물 · 고대</h4>
          </div>
          <span
            class="summary-chip"
            :class="{ 'summary-chip--muted': !engravingSummary.length }"
          >
            {{ engravingSummary.length ? `${engravingSummary.length}개` : '데이터 없음' }}
          </span>
        </div>
        <div v-if="engravingSummary.length" class="summary-list summary-list--flat">
          <div
            v-for="engrave in engravingSummary"
            :key="engrave.key"
            class="summary-list-item summary-list-item--plain"
          >
            <LazyImage
              v-if="engrave.icon"
              :src="engrave.icon"
              :alt="engrave.name"
              width="32"
              height="32"
              imageClass="summary-icon"
              errorIcon="🔮"
              :useProxy="true"
            />
            <div v-else class="summary-icon summary-icon--fallback" aria-hidden="true">
              {{ engrave.gradeLabel?.[0] || 'E' }}
            </div>
            <div class="summary-list-text">
              <p class="summary-title">{{ engrave.name }}</p>
              <p class="summary-sub">{{ engrave.gradeLabel }}</p>
            </div>
            <div class="summary-pill-row summary-pill-row--wrap">
              <span v-if="engrave.levelLabel" class="summary-pill summary-pill--primary">
                {{ engrave.levelLabel }}
              </span>
              <span v-if="engrave.craftLabel" class="summary-pill summary-pill--ghost">
                {{ engrave.craftLabel }}
              </span>
            </div>
          </div>
        </div>
        <p v-else class="summary-note">각인 정보가 없습니다.</p>
      </article>

      <article class="summary-card summary-card--module summary-card--collection">
        <div class="summary-card__head">
          <div>
            <p class="summary-eyebrow">수집</p>
            <h4>주요 포인트</h4>
          </div>
          <span
            class="summary-chip"
            :class="{ 'summary-chip--muted': !collectionSummary.length }"
          >
            {{ collectionSummary.length ? `${collectionSummary.length}종` : '데이터 없음' }}
          </span>
        </div>
        <p v-if="collectiblesLoading" class="summary-note">수집 정보를 정리하는 중입니다...</p>
        <p v-else-if="collectiblesError" class="summary-note summary-note--warning">
          {{ collectiblesError }}
        </p>
        <div v-else-if="collectionSummary.length" class="summary-progress-list summary-progress-list--dense">
          <div
            v-for="item in collectionSummary"
            :key="item.key"
            class="summary-progress summary-progress--compact"
          >
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
      </article>
    </div>
    <EmptyState
      v-else
      icon="ℹ️"
      title="캐릭터를 먼저 선택하세요"
      description="검색 후 내 정보 간소화 탭에서 핵심 정보를 요약해 드립니다."
    />
  </section>
</template>

<script setup lang="ts">
import LazyImage from './LazyImage.vue'
import EmptyState from './EmptyState.vue'
import type { CharacterProfile } from '@/api/types'

defineProps<{
  activeCharacter: CharacterProfile | null
  equipmentSummary: any
  detailLoading: boolean
  detailError: string | null
  arkSummary: any
  arkGridLoading: boolean
  arkGridError: string | null
  skillHighlights: any[]
  skillLoading: boolean
  skillError: string | null
  engravingSummary: any[]
  collectionSummary: any[]
  collectiblesLoading: boolean
  collectiblesError: string | null
}>()
</script>
