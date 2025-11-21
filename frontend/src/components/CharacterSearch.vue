<template>
  <div class="app-container">
    <header class="page-header">
      <div class="header-left">
        <button class="menu-button" type="button" aria-label="메뉴 열기" @click="openMenu">
          <span></span>
          <span></span>
          <span></span>
        </button>
        <h1>LOA Character Search</h1>
      </div>
      <div class="header-search">
        <div class="search-panel-wrapper" ref="searchPanelWrapperRef">
          <AutocompleteInput
            v-model="characterName"
            :suggestions="[]"
            placeholder="캐릭터명을 입력하세요"
            inputClass="search-input"
            :min-chars="1"
            :max-suggestions="8"
            @select="handleSuggestionSelect"
            @keyup.enter="searchCharacterByInput"
            @focus="handleSearchFocus"
          />

          <div v-if="shouldShowSearchPanel" class="search-panel-dropdown">
            <div class="search-panel-tabs">
              <button
                type="button"
                class="search-panel-tab"
                :class="{ active: activeSearchPanelTab === 'recent' }"
                @click="activeSearchPanelTab = 'recent'"
              >
                최근 검색
              </button>
              <button
                type="button"
                class="search-panel-tab"
                :class="{ active: activeSearchPanelTab === 'favorites' }"
                @click="activeSearchPanelTab = 'favorites'"
              >
                내 즐겨찾기
              </button>
            </div>

            <div class="search-panel-content">
              <template v-if="activeSearchPanelTab === 'recent'">
                <div class="panel-section-header">
                  <span>최근 검색</span>
                  <button
                    v-if="history.length > 0"
                    type="button"
                    class="panel-clear-btn"
                    @click="clearHistory"
                  >
                    전체 삭제
                  </button>
                </div>
                <div v-if="panelHistoryItems.length === 0" class="panel-empty">
                  {{ history.length === 0 ? '검색 기록이 없습니다' : '일치하는 검색 기록이 없습니다' }}
                </div>
                <ul v-else class="panel-list">
                  <li
                    v-for="item in panelHistoryItems"
                    :key="item.id"
                  >
                    <button
                      type="button"
                      class="panel-list-item"
                      @click="handleSearchPanelSelect(item.characterName)"
                    >
                      <span class="panel-list-name">{{ item.characterName }}</span>
                      <span class="panel-list-meta">🕒</span>
                    </button>
                  </li>
                </ul>
              </template>

              <template v-else>
                <div class="panel-section-header">
                  <span>내 즐겨찾기</span>
                </div>
                <div v-if="panelFavoriteItems.length === 0" class="panel-empty">
                  {{ favorites.length === 0 ? '즐겨찾기가 비어있어요' : '일치하는 즐겨찾기가 없습니다' }}
                </div>
                <div v-else class="panel-favorite-list">
                  <button
                    v-for="fav in panelFavoriteItems"
                    :key="fav.characterName"
                    type="button"
                    class="panel-favorite-item"
                    @click="handleSearchPanelSelect(fav.characterName)"
                  >
                    <LazyImage
                      :src="fav.characterImage || ''"
                      :alt="fav.characterName"
                      width="38"
                      height="38"
                      imageClass="panel-favorite-image"
                      errorIcon="❔"
                    />
                    <div class="panel-favorite-details">
                      <span class="panel-favorite-name">{{ fav.characterName }}</span>
                      <span class="panel-favorite-meta">
                        {{ fav.serverName }} · {{ formatItemLevel(fav.itemMaxLevel || fav.itemAvgLevel) }}
                      </span>
                    </div>
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="header-right" aria-hidden="true"></div>
    </header>
    <transition name="sidebar-fade">
      <div v-if="menuOpen" class="sidebar-overlay" @click="closeMenu"></div>
    </transition>
    <transition name="sidebar-slide">
      <aside
        v-if="menuOpen"
        class="sidebar-menu"
        tabindex="-1"
        ref="sidebarRef"
        @keyup.esc="closeMenu"
      >
        <div class="sidebar-header">
          <ThemeToggle />
          <button class="sidebar-close" type="button" aria-label="메뉴 닫기" @click="closeMenu">×</button>
        </div>
        <div class="sidebar-content">
          <h3>전체 메뉴</h3>
          <p class="sidebar-description">이용 가능한 메뉴와 준비 중인 기능을 확인해 보세요.</p>
          <ul class="sidebar-menu-list">
            <li v-for="menu in siteMenuItems" :key="menu.key">
              <button
                type="button"
                class="sidebar-menu-item"
                :class="{ active: activeSiteMenu === menu.key, disabled: !menu.available }"
                :aria-current="activeSiteMenu === menu.key ? 'page' : undefined"
                :disabled="!menu.available"
                @click="handleMenuSelect(menu)"
              >
                <span class="sidebar-menu-icon" aria-hidden="true">{{ menu.icon }}</span>
                <span class="sidebar-menu-text">
                  <span class="sidebar-menu-label">{{ menu.label }}</span>
                  <span class="sidebar-menu-desc">{{ menu.description }}</span>
                </span>
                <span v-if="menu.badge" class="sidebar-menu-badge">{{ menu.badge }}</span>
              </button>
            </li>
          </ul>
          <p class="sidebar-footnote">경매, 생활 메뉴는 순차적으로 공개됩니다.</p>
        </div>
      </aside>
    </transition>

    <div class="content-wrapper">
      <aside class="ad-slot ad-slot--left" aria-label="왼쪽 광고 영역">
        <span>광고 영역</span>
      </aside>
      <main class="main-content">
        <ReforgeMenu v-if="activeSiteMenu === 'reforge'" />
        <div v-else class="search-container">
          <section class="states-section" v-if="false">
            <h2>States</h2>
            <div class="states-grid">
              <div class="state-card">
                <span class="state-label">LoadingSpinner.vue</span>
              </div>
              <div class="state-card">
                <span class="state-label">EmptyState.vue</span>
              </div>
              <div class="state-card">
                <span class="state-label">ErrorMessage.vue</span>
              </div>
            </div>
          </section>

          <div v-if="loading" class="loading-display">
            <LoadingSpinner message="캐릭터 정보를 불러오는 중..." />
          </div>

          <div v-if="error && !loading" class="error-display">
            <ErrorMessage
              :title="error.title"
              :message="error.message"
              :type="error.type"
              :retry="true"
              :dismissible="true"
              @retry="retrySearch"
              @dismiss="dismissError"
            />
          </div>

          <div v-if="!loading && !character && !error" class="empty-display">
            <EmptyState
              icon="🔍"
              title="캐릭터를 검색해 주세요"
              description="캐릭터명을 입력하고 Enter를 누르거나 최근검색 혹은 즐겨찾기에서 선택해주세요."
            />
          </div>

          <section v-if="character && !loading" class="character-results">
            <div class="results-layout">
              <div class="character-overview-card" ref="characterOverviewRef">
                <div class="overview-toolbar">
                  <button
                    type="button"
                    class="refresh-button"
                    :disabled="loading || !activeCharacter"
                    @click="handleRefreshClick"
                  >
                    갱신하기
                  </button>
                  <span class="refresh-timestamp">
                    {{ lastRefreshedLabel }}
                  </span>
                </div>
                <div class="hero-row hero-row--levels" v-if="activeCharacter">
                  <div class="profile-stats-grid hero-levels-grid">
                    <div class="profile-stat">
                      <span>전투력</span>
                      <strong>{{ formatCombatPower(activeCharacter?.combatPower) }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>전투 레벨</span>
                      <strong>Lv. {{ formatInteger(activeCharacter?.characterLevel) }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>아이템 레벨</span>
                      <strong>{{ formatItemLevel(activeCharacter?.itemAvgLevel) }}</strong>
                    </div>
                    <div class="profile-stat">
                      <span>원정대 레벨</span>
                      <strong>{{ formatInteger(activeCharacter?.expeditionLevel) }}</strong>
                    </div>
                  </div>
                </div>

                <div class="hero-row hero-row--image">
                  <div class="hero-avatar-column">
                    <div
                      v-if="hasCharacterImage"
                      class="hero-avatar-controls"
                    >
                      <button
                        type="button"
                        class="hero-avatar-btn"
                        @click="expandHeroImage"
                        :disabled="isHeroImageLarge"
                      >
                        확대
                      </button>
                      <button
                        type="button"
                        class="hero-avatar-btn"
                        @click="shrinkHeroImage"
                        :disabled="!isHeroImageLarge"
                      >
                        축소
                      </button>
                      <button
                        type="button"
                        class="hero-avatar-btn"
                        @click="openHeroImagePopup"
                      >
                        팝업뷰
                      </button>
                    </div>
                    <div
                      class="hero-avatar-wrapper"
                      :class="{ 'is-large': isHeroImageLarge }"
                    >
                      <LazyImage
                        :src="activeCharacter?.characterImage || ''"
                        :alt="activeCharacter?.characterName || ''"
                        :width="isHeroImageLarge ? 300 : 140"
                        :height="isHeroImageLarge ? 350 : 140"
                        imageClass="hero-avatar"
                        errorIcon="👤"
                      />
                    </div>
                  </div>
                  <div class="hero-text">
                    <div class="hero-text__header">
                      <h2>{{ activeCharacter?.characterName }}</h2>
                      <button
                        v-if="activeCharacter"
                        type="button"
                        class="favorite-toggle-btn"
                        :class="{ 'is-active': isCharacterFavorite }"
                        :aria-pressed="isCharacterFavorite"
                        @click="toggleFavorite"
                        aria-label="즐겨찾기 토글"
                      >
                        <span class="favorite-star" aria-hidden="true">★</span>
                      </button>
                    </div>
                    <span class="hero-title" v-if="activeCharacter?.title">{{ activeCharacter?.title }}</span>
                  </div>
                </div>

                <div class="hero-row hero-row--meta">
                  <div class="hero-meta-grid">
                    <div class="meta-item">
                      <span>직업</span>
                      <strong>{{ activeCharacter?.characterClassName }}</strong>
                    </div>
                    <div class="meta-item">
                      <span>서버</span>
                      <strong>{{ activeCharacter?.serverName }}</strong>
                    </div>
                    <div class="meta-item" v-if="activeCharacter?.guildName">
                      <span>길드</span>
                      <strong>{{ activeCharacter?.guildName }}</strong>
                    </div>
                    <div class="meta-item" v-if="activeCharacter?.pvpGradeName">
                      <span>PVP</span>
                      <strong>{{ activeCharacter?.pvpGradeName }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--profile-stats" v-if="displayStats.length">
                  <h3>전투 특성</h3>
                  <div class="profile-stats-grid">
                    <div
                      v-for="stat in displayStats"
                      :key="`${stat.type}-${stat.value}`"
                      class="profile-stat"
                    >
                      <span>{{ stat.type }}</span>
                      <strong>{{ formatProfileStat(stat.value) }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--paradise" v-if="paradiseInfo.power || paradiseInfo.season">
                  <h3>낙원</h3>
                  <div class="paradise-info">
                    <div class="paradise-item" v-if="paradiseInfo.season">
                      <span>시즌</span>
                      <strong>{{ paradiseInfo.season }}</strong>
                    </div>
                    <div class="paradise-item" v-if="paradiseInfo.power">
                      <span>낙원력</span>
                      <strong>{{ formatInteger(paradiseInfo.power) }}</strong>
                    </div>
                  </div>
                </div>
                <div class="hero-row hero-row--special" v-if="specialEquipmentsDetailed.length">
                  <div class="special-header">
                    <h3>기타</h3>
                    <!-- <span class="special-count">{{ specialEquipmentsDetailed.length }}개</span> -->
                  </div>
                    <div class="special-grid special-grid--icons">
                      <div
                        v-for="special in specialEquipmentsDetailed"
                        :key="special.item.name"
                        class="special-icon-wrapper"
                        :class="{ 'is-hovered': hoveredSpecialName === special.item.name }"
                        tabindex="0"
                        @mouseenter="handleSpecialHover(special.item.name)"
                        @mouseleave="scheduleSpecialHoverClear"
                        @focus="handleSpecialHover(special.item.name)"
                        @blur="scheduleSpecialHoverClear"
                      >
                        <div class="special-icon-box">
                          <LazyImage
                            v-if="special.item.icon"
                            :src="special.item.icon"
                            :alt="special.item.name"
                            width="56"
                            height="56"
                            imageClass="special-icon"
                            errorIcon="🧭"
                            :useProxy="true"
                          />
                          <div v-else class="special-icon special-icon--fallback" aria-hidden="true">
                            {{ special.item.name ? special.item.name[0] : '?' }}
                          </div>
                        </div>
                        <span class="special-label">
                          {{ special.label }}
                        </span>
                        <div
                          v-if="hoveredSpecialName === special.item.name"
                          class="special-tooltip popup-surface popup-surface--tooltip"
                          :style="tooltipWidthStyle"
                          role="tooltip"
                          @mouseenter="cancelSpecialHoverTimeout"
                          @mouseleave="scheduleSpecialHoverClear"
                        >
                          <p class="popup-surface__title special-tooltip-title">
                            {{ special.item.name }}
                          </p>
                          <div v-if="special.highlights.length" class="special-highlights">
                            <span
                              v-for="(line, idx) in special.highlights"
                              :key="`${special.item.name}-${idx}`"
                            >
                              {{ line }}
                            </span>
                          </div>
                          <p v-else class="special-tooltip-empty">추가 설명이 없습니다.</p>
                        </div>
                      </div>
                    </div>
                </div>

              </div>

              <div class="results-panel">
                <div class="view-tabs">
                  <button
                    v-for="tab in resultTabs"
                    :key="tab.key"
                    class="view-tab-button"
                    type="button"
                    :class="{ active: activeResultTab === tab.key }"
                    @click="activeResultTab = tab.key"
                  >
                    {{ tab.label }}
                  </button>
                </div>

                <section
                  v-if="activeResultTab === 'summary'"
                  class="detail-panel summary-panel"
                >
                  <div v-if="activeCharacter" class="summary-grid summary-grid--modules summary-grid--stacked">
                    <article class="summary-card summary-card--module summary-card--equipment">
                      <div class="summary-card__head">
                        <div>
                          <p class="summary-eyebrow">장비</p>
                          <h4>세팅 스냅샷</h4>
                        </div>
                        <div class="summary-pill-row summary-pill-row--wrap" v-if="equipmentSummary.gradeBadges.length">
                          <span
                            v-for="badge in equipmentSummary.gradeBadges"
                            :key="badge.grade"
                            class="summary-pill summary-pill--ghost"
                          >
                            {{ badge.grade }} · {{ badge.count }}개
                          </span>
                        </div>
                      </div>
                      <p v-if="detailLoading" class="summary-note">장비 정보를 정리하는 중입니다...</p>
                      <p v-else-if="detailError" class="summary-note summary-note--warning">{{ detailError }}</p>
                      <div v-else class="equipment-grid">
                        <div class="equipment-column">
                          <h5>무기/방어구</h5>
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
                        <div>
                          <p class="summary-eyebrow">아크 그리드</p>
                          <h4>{{ arkSummary.passiveTitle || '아크 루트 정보' }}</h4>
                        </div>
                        <span
                          class="summary-chip"
                          :class="{ 'summary-chip--muted': !arkSummary.slotCount }"
                        >
                          {{ arkSummary.slotCount ? `${arkSummary.slotCount} 슬롯` : '데이터 없음' }}
                        </span>
                      </div>
                      <p v-if="arkGridLoading" class="summary-note">아크 그리드 정보를 불러오는 중입니다...</p>
                      <p v-else-if="arkGridError" class="summary-note summary-note--warning">{{ arkGridError }}</p>
                      <div v-else>
                        <div v-if="arkSummary.gemSummary.length" class="ark-gem-pills">
                          <span
                            v-for="gem in arkSummary.gemSummary"
                            :key="gem.key"
                            class="summary-pill summary-pill--ghost"
                          >
                            {{ gem.label }} · {{ gem.value }}
                          </span>
                        </div>
                        <div v-if="arkSummary.slotPoints.length" class="summary-pill-row summary-pill-row--wrap">
                          <span
                            v-for="slot in arkSummary.slotPoints"
                            :key="slot.key"
                            class="summary-pill summary-pill--primary"
                          >
                            {{ slot.label }} · {{ slot.value }}
                          </span>
                        </div>
                        <ul v-if="arkSummary.effects.length" class="summary-list summary-list--compact">
                          <li
                            v-for="effect in arkSummary.effects"
                            :key="effect.key"
                            class="summary-list-item"
                          >
                            <div class="summary-list-text">
                              <p class="summary-title">{{ effect.title }}</p>
                              <p class="summary-sub">{{ effect.subtitle }}</p>
                            </div>
                            <LazyImage
                              v-if="effect.icon"
                              :src="effect.icon"
                              :alt="effect.title"
                              width="28"
                              height="28"
                              imageClass="summary-icon"
                              errorIcon="⭐"
                              :useProxy="true"
                            />
                            <span v-if="effect.levelLabel" class="summary-pill summary-pill--ghost">
                              {{ effect.levelLabel }}
                            </span>
                          </li>
                        </ul>
                        <p v-else class="summary-note">요약할 아크 패시브가 없습니다.</p>
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

                <section
                  v-else-if="activeResultTab === 'skills'"
                  class="detail-panel skill-panel"
                >
                  <SkillPanel
                    :response="skillResponse"
                    :loading="skillLoading"
                    :error-message="skillError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureSkillData"
                  />
                </section>

                <section
                  v-else-if="activeResultTab === 'detail'"
                  class="detail-panel"
                >
                  <CharacterDetailModal
                    :character="selectedCharacterProfile"
                    :equipment="detailEquipment"
                    :engravings="detailEngravings"
                    :loading="detailLoading"
                    :error-message="detailError"
                  />
                </section>

                <section
                  v-else-if="activeResultTab === 'collection'"
                  class="detail-panel collection-panel-wrapper"
                >
                  <CollectionPanel
                    :collectibles="collectibles"
                    :loading="collectiblesLoading"
                    :error-message="collectiblesError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureCollectiblesData"
                  />
                </section>

                <RankingTab
                  v-else-if="activeResultTab === 'ranking'"
                  :character-name="activeCharacter?.characterName"
                />

                <section
                  v-else-if="activeResultTab === 'expedition'"
                  class="expedition-section"
                >
                  <div class="section-header-bar">
                    <div>
                      <h3>원정대 보유 캐릭터</h3>
                      <p class="section-subtitle">클릭하면 상세 정보가 열립니다.</p>
                    </div>
                    <span class="count-pill">{{ (character ? 1 : 0) + siblings.length }}명</span>
                  </div>
                  <template v-if="expeditionGroups.length">
                    <div
                      v-for="group in expeditionGroups"
                      :key="group.server"
                      class="expedition-group"
                    >
                      <h4>{{ group.server }}</h4>
                      <div class="expedition-grid">
                        <article
                          v-for="member in group.members"
                          :key="member.characterName"
                          class="expedition-card"
                          :class="{ active: selectedCharacterProfile?.characterName === member.characterName }"
                          @click="viewCharacterDetail(member)"
                        >
                          <div class="member-top">
                            <span class="member-level">Lv. {{ formatInteger(member.characterLevel) }}</span>
                            <span class="member-class">{{ member.characterClassName }}</span>
                          </div>
                          <strong class="member-name">{{ member.characterName }}</strong>
                          <span class="member-ilvl">
                            iLv. {{ formatItemLevel(member.itemAvgLevel || member.itemMaxLevel) }}
                          </span>
                          <span class="member-detail">상세 보기</span>
                        </article>
                      </div>
                    </div>
                  </template>
                  <p v-else class="empty-message">원정대 캐릭터가 없습니다.</p>
                </section>

                <section
                  v-else-if="activeResultTab === 'arkGrid'"
                  class="detail-panel ark-grid-panel"
                >
                  <ArkGridPanel
                    :response="arkGridResponse"
                    :loading="arkGridLoading"
                    :error-message="arkGridError"
                    :character-name="character?.characterName || ''"
                    @retry="ensureArkGridData"
                  />
                </section>

                <section
                  v-else
                  class="detail-panel placeholder-panel"
                >
                  <EmptyState
                    :icon="activePlaceholder?.icon || '🛠️'"
                    :title="activePlaceholder?.title || '준비 중인 메뉴입니다'"
                    :description="activePlaceholder?.description || '곧 해당 메뉴의 세부 기능을 제공할 예정입니다.'"
                  />
                </section>
              </div>
            </div>
          </section>
        </div>
      </main>
      <aside class="ad-slot ad-slot--right" aria-label="오른쪽 광고 영역">
        <span>광고 영역</span>
      </aside>
    </div>
    <footer class="page-footer">
      <div class="footer-inner">
        <span class="footer-badge">Copyright</span>
        <span class="footer-copy">© Ferny</span>
      </div>
    </footer>
  </div>
  <div
    v-if="activeSiteMenu === 'character-search' && isHeroImagePopupOpen && hasCharacterImage"
    class="character-portrait-overlay"
    @click.self="closeHeroImagePopup"
  >
    <div class="character-portrait-overlay__content">
      <button
        type="button"
        class="portrait-overlay__close"
        aria-label="캐릭터 이미지 닫기"
        @click="closeHeroImagePopup"
      >
        ✕
      </button>
      <img :src="characterImageSrc" :alt="activeCharacter?.characterName ?? '캐릭터 확대 이미지'" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { lostarkApi } from '@/api/lostark'
import type {
  CharacterProfile,
  SiblingCharacter,
  SearchHistory,
  Equipment,
  Engraving,
  CharacterStat,
  ArkGridResponse,
  SkillMenuResponse,
  Collectible,
  CombatSkill
} from '@/api/types'
import LoadingSpinner from './common/LoadingSpinner.vue'
import ErrorMessage from './common/ErrorMessage.vue'
import EmptyState from './common/EmptyState.vue'
import ThemeToggle from './common/ThemeToggle.vue'
import LazyImage from './common/LazyImage.vue'
import AutocompleteInput from './common/AutocompleteInput.vue'
import CharacterDetailModal from './common/CharacterDetailModal.vue'
import ArkGridPanel from './common/ArkGridPanel.vue'
import SkillPanel from './common/SkillPanel.vue'
import CollectionPanel from './common/CollectionPanel.vue'
import RankingTab from './ranking/RankingTab.vue'
import ReforgeMenu from './ReforgeMenu.vue'
import { useTheme } from '@/composables/useTheme'
import type { Suggestion } from './common/AutocompleteInput.vue'

const { initTheme } = useTheme()
initTheme()

interface ErrorState {
  message: string
  type: 'error' | 'warning' | 'info'
  title?: string
}

type ResultTabKey =
  | 'summary'
  | 'skills'
  | 'detail'
  | 'collection'
  | 'ranking'
  | 'arkGrid'
  | 'expedition'

interface TabPlaceholderCopy {
  icon: string
  title: string
  description: string
}

type SiteMenuKey = 'character-search' | 'reforge' | 'auction' | 'life'

interface SiteMenuItem {
  key: SiteMenuKey
  label: string
  description: string
  icon: string
  available: boolean
  routeName?: string
  badge?: string
}

const FAVORITES_STORAGE_KEY = 'loa:favorites'
const HISTORY_STORAGE_KEY = 'loa:history'
const DEFAULT_RESULT_TAB: ResultTabKey = 'summary'
const DEFAULT_SITE_MENU: SiteMenuKey = 'character-search'
const resultTabs: Array<{ key: ResultTabKey; label: string }> = [
  { key: 'summary', label: '내 정보 간소화' },
  { key: 'skills', label: '스킬' },
  { key: 'detail', label: '장비' },
  { key: 'collection', label: '수집' },
  { key: 'ranking', label: '랭킹' },
  { key: 'arkGrid', label: '아크 그리드' },
  { key: 'expedition', label: '보유 캐릭터' }
]
const tabPlaceholderCopy: Record<ResultTabKey, TabPlaceholderCopy | null> = {
  summary: null,
  detail: null,
  expedition: null,
  skills: null,
  collection: null,
  ranking: null,
  arkGrid: null
}

const siteMenuItems: SiteMenuItem[] = [
  {
    key: 'character-search',
    label: '캐릭터 검색',
    description: '원정대 캐릭터와 상세 스펙을 확인할 수 있는 기본 화면이에요.',
    icon: '🧭',
    available: true,
    routeName: 'character-search',
    badge: '기본'
  },
  {
    key: 'reforge',
    label: '재련',
    description: '제련/상급 제련 재료와 확률을 한눈에 확인하세요.',
    icon: '⚒️',
    available: true,
    routeName: 'reforge',
    badge: 'NEW'
  },
  {
    key: 'auction',
    label: '경매',
    description: '경매장 시세와 수익 계산 도구를 곧 제공할 예정입니다.',
    icon: '💰',
    available: false,
    badge: '준비 중'
  },
  {
    key: 'life',
    label: '생활',
    description: '생활 컨텐츠 수익 분석과 동선 추천을 만들고 있어요.',
    icon: '🌿',
    available: false,
    badge: '준비 중'
  }
]

const router = useRouter()
const route = useRoute()

const characterName = ref('')
const character = ref<CharacterProfile | null>(null)
const loading = ref(false)
const error = ref<ErrorState | null>(null)
const siblings = ref<SiblingCharacter[]>([])
const favorites = ref<CharacterProfile[]>([])
const history = ref<SearchHistory[]>([])
const characterAvailability = ref<Record<string, 'available' | 'unavailable' | 'loading'>>({})
const selectedCharacterProfile = ref<CharacterProfile | null>(null)
const activeResultTab = ref<ResultTabKey>(DEFAULT_RESULT_TAB)
const activePlaceholder = computed(() => tabPlaceholderCopy[activeResultTab.value])
const activeSiteMenu = ref<SiteMenuKey>(DEFAULT_SITE_MENU)
const characterOverviewRef = ref<HTMLElement | null>(null)
const overviewWidth = ref(0)
const searchPanelWrapperRef = ref<HTMLElement | null>(null)
const searchPanelOpen = ref(false)
const activeSearchPanelTab = ref<'recent' | 'favorites'>('recent')
const isHeroImageLarge = ref(false)
const isHeroImagePopupOpen = ref(false)
const activeCharacter = computed<CharacterProfile | null>(() => selectedCharacterProfile.value ?? character.value)
const characterImageSrc = computed(() => activeCharacter.value?.characterImage || '')
const hasCharacterImage = computed(() => Boolean(characterImageSrc.value))
const isCharacterFavorite = computed(() => {
  if (!activeCharacter.value) return false
  return favorites.value.some(
    fav =>
      fav.characterName === activeCharacter.value?.characterName &&
      fav.serverName === activeCharacter.value?.serverName
  )
})

const shouldShowSearchPanel = computed(() => searchPanelOpen.value)
const lastRefreshedLabel = computed(() => {
  if (!lastRefreshedAt.value) return '갱신 이력이 없어요'
  const formatted = new Intl.DateTimeFormat('ko-KR', {
    dateStyle: 'short',
    timeStyle: 'short'
  }).format(lastRefreshedAt.value)
  return `마지막 갱신: ${formatted}`
})

const detailEquipment = ref<Equipment[]>([])
const detailEngravings = ref<Engraving[]>([])
const detailLoading = ref(false)
const detailError = ref<string | null>(null)
const arkGridResponse = ref<ArkGridResponse | null>(null)
const arkGridLoading = ref(false)
const arkGridError = ref<string | null>(null)
const arkGridLoadedFor = ref<string | null>(null)
const skillResponse = ref<SkillMenuResponse | null>(null)
const skillLoading = ref(false)
const skillError = ref<string | null>(null)
const skillLoadedFor = ref<string | null>(null)
const collectibles = ref<Collectible[]>([])
const collectiblesLoading = ref(false)
const collectiblesError = ref<string | null>(null)
const collectiblesLoadedFor = ref<string | null>(null)
const lastRefreshedAt = ref<Date | null>(null)
const specialEquipmentKeywords = ['나침반', '부적', '문장', '보주']

const isSpecialEquipment = (item: Equipment) => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  return specialEquipmentKeywords.some(keyword => target.includes(keyword.toLowerCase()))
}

const specialEquipments = computed(() => {
  return detailEquipment.value.filter(item => isSpecialEquipment(item))
})

const specialEquipmentsDetailed = computed(() => {
  return specialEquipments.value.map(item => ({
    item,
    highlights: getSpecialHighlights(item),
    label: getSpecialLabel(item)
  }))
})

const inlineText = (value?: string | number | null) => {
  if (value === undefined || value === null) return ''
  return cleanTooltipLine(String(value)).replace(/\s+/g, ' ').trim()
}

const normalizeSkillKey = (value?: string | null) =>
  inlineText(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

const isAwakeningSkill = (skill: CombatSkill) => {
  const numericType =
    typeof skill.skillType === 'string' ? Number(skill.skillType) : skill.skillType
  if (typeof numericType === 'number' && (numericType === 100 || numericType === 101)) {
    return true
  }
  const candidates = [skill.skillType, skill.type, skill.name]
    .map(value => inlineText(value).toLowerCase())
    .filter(Boolean)
  return candidates.some(value => value.includes('각성'))
}

const skillGemCountMap = computed(() => {
  const map = new Map<string, number>()
  const gems = skillResponse.value?.skillGems ?? []
  gems.forEach(gem => {
    const key = normalizeSkillKey(gem.skill?.name || gem.skill?.description || gem.name)
    if (!key) return
    map.set(key, (map.get(key) || 0) + 1)
  })
  return map
})

const skillHighlights = computed(() => {
  const skills = skillResponse.value?.combatSkills ?? []
  if (!skills.length) return []
  const gemCounts = skillGemCountMap.value
  return skills
    .filter(skill => !isAwakeningSkill(skill))
    .sort((a, b) => {
      const levelA = typeof a.level === 'number' ? a.level : -1
      const levelB = typeof b.level === 'number' ? b.level : -1
      return levelB - levelA
    })
    .filter(skill => (skill.skillPoints ?? 0) > 0 || Boolean(skill.rune))
    .slice(0, 5)
    .map((skill, index) => {
      const key = normalizeSkillKey(skill.name || `skill-${index}`)
      const gemCount = key ? gemCounts.get(key) || 0 : 0
      const tripodCount = (skill.tripods ?? []).filter(tripod => tripod.name && tripod.selected !== false).length
      return {
        key: key || `skill-${index}`,
        name: inlineText(skill.name) || `스킬 ${index + 1}`,
        icon: skill.icon || '',
        levelLabel: typeof skill.level === 'number' ? `Lv.${skill.level}` : '',
        rune: inlineText(skill.rune?.name) || '',
        gemLabel: gemCount ? `보석 ${gemCount}` : '',
        tripodLabel: tripodCount ? `트포 ${tripodCount}` : '',
        pointLabel: skill.skillPoints ?? 0
      }
    })
})

const summarizeEquipmentLine = (item: Equipment): string => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return ''
  const highlightRegex = /(추가 피해|무력화|치명|신속|특화|공격력|방어력|품질|효과|피해|쿨타임|쿨타운)/i
  const candidate = lines.find(line => highlightRegex.test(line)) || lines[0]
  return inlineText(candidate)
}

const matchStatFromLines = (lines: string[], patterns: RegExp[]) => {
  for (const line of lines) {
    for (const pattern of patterns) {
      const match = line.match(pattern)
      if (match) {
        return match[1] || match[0]
      }
    }
  }
  return ''
}

const parseEquipmentMeta = (item: Equipment) => {
  const lines = extractTooltipLines(item.tooltip)
  const itemLevel = matchStatFromLines(lines, [/아이템\s*레벨\s*([0-9.,]+)/i, /iLv\.?\s*([0-9.,]+)/i])
  const quality = matchStatFromLines(lines, [/품질\s*([0-9]+)/i, /\(품질\)\s*([0-9]+)/i])
  const transcend = matchStatFromLines(lines, [/초월\s*([0-9]+)/i])
  const engravingLine = matchStatFromLines(lines, [/각인\s*효과\s*(.+)/i])
  const harmony = matchStatFromLines(lines, [/상재\s*([0-9]+)/i, /상급\s*재련\s*([0-9]+)/i])
  const mainStat = matchStatFromLines(lines, [/(힘|민첩|지능)[^0-9+]*([0-9.,]+)/i])
  const craft = matchStatFromLines(lines, [/세공\s*([0-9]+)/i])
  return {
    itemLevel: itemLevel ? `iLv. ${itemLevel}` : '',
    quality,
    transcend,
    harmony,
    engravingLine,
    mainStat,
    craft
  }
}

const isAccessory = (item: Equipment) => {
  const label = inlineText(item.type).toLowerCase()
  return /(목걸이|귀걸이|반지|팔찌|어빌리티|돌)/.test(label)
}

const isBracelet = (item: Equipment) => inlineText(item.type).includes('팔찌')
const isAbilityStone = (item: Equipment) => /어빌리/i.test(inlineText(item.name) + inlineText(item.type))

const equipmentSummary = computed(() => {
  if (!detailEquipment.value.length) {
    return { gradeBadges: [], left: [], right: [] }
  }

  const gradeCounts = new Map<string, number>()
  detailEquipment.value.forEach(item => {
    const grade = inlineText(item.grade) || '등급 미상'
    gradeCounts.set(grade, (gradeCounts.get(grade) || 0) + 1)
  })

  const gradeBadges = Array.from(gradeCounts.entries())
    .map(([grade, count]) => ({ grade, count }))
    .sort((a, b) => b.count - a.count)

  const left: any[] = []
  const right: any[] = []

  const stoneCraft = detailEngravings.value.find(eng => typeof eng.abilityStoneLevel === 'number')
    ?.abilityStoneLevel

  detailEquipment.value.forEach((item, index) => {
    const meta = parseEquipmentMeta(item)
    const base = {
      key: `${item.name || 'equipment'}-${index}`,
      name: inlineText(item.name) || `장비 ${index + 1}`,
      typeLabel: inlineText(item.type) || '장비',
      grade: inlineText(item.grade),
      icon: item.icon || '',
      itemLevel: meta.itemLevel,
      quality: meta.quality,
      transcend: meta.transcend,
      meta: summarizeEquipmentLine(item)
    }

    if (isAccessory(item)) {
      const special = isAbilityStone(item)
        ? stoneCraft
          ? `세공 ${stoneCraft}단`
          : meta.craft
            ? `세공 ${meta.craft}단`
            : meta.engravingLine || '세공 정보'
        : isBracelet(item)
          ? (meta.engravingLine || base.meta || '').slice(0, 14)
          : inlineText(meta.harmony || meta.engravingLine || meta.mainStat)
      right.push({
        ...base,
        special: special || undefined
      })
    } else {
      left.push(base)
    }
  })

  return {
    gradeBadges,
    left: left.slice(0, 6),
    right: right.slice(0, 8)
  }
})

const collectionSummary = computed(() => {
  if (!collectibles.value.length) return []
  return collectibles.value
    .map((item, index) => {
      const point = typeof item.point === 'number' ? item.point : 0
      const maxPoint = typeof item.maxPoint === 'number' ? item.maxPoint : 0
      const percent = maxPoint > 0 ? Math.min(point / maxPoint, 1) : null
      const percentLabel = percent === null ? '0%' : `${Math.round(percent * 100)}%`
      return {
        key: `${item.type || 'collectible'}-${index}`,
        name: inlineText(item.type) || `수집 ${index + 1}`,
        levelLabel: typeof item.collectibleLevel === 'number' ? `Lv.${item.collectibleLevel}` : '',
        pointLabel: maxPoint
          ? `${formatNumberLocalized(point)} / ${formatNumberLocalized(maxPoint)}`
          : `포인트 ${formatNumberLocalized(point)}`,
        percentLabel: percentLabel,
        percentValue: percent ?? 0
      }
    })
    .sort((a, b) => b.percentValue - a.percentValue)
})

const engravingSummary = computed(() => {
  return detailEngravings.value.map((engrave, index) => {
    const gradeLabel = inlineText(engrave.grade) || '등급 미상'
    return {
      key: `${engrave.name || 'engrave'}-${index}`,
      name: inlineText(engrave.name),
      gradeLabel,
      levelLabel: typeof engrave.level === 'number' ? `Lv.${engrave.level}` : '',
      craftLabel:
        typeof engrave.abilityStoneLevel === 'number' ? `세공 ${engrave.abilityStoneLevel}단` : '',
      icon: engrave.icon || ''
    }
  })
})

const arkSummary = computed(() => {
  const passive = arkGridResponse.value?.arkPassive
  const grid = arkGridResponse.value?.arkGrid
  const pointSummary =
    [] as Array<{ key: string; label: string; valueLabel: string }>

  const effectsSource = passive?.effects ?? grid?.effects ?? []
  const effects = effectsSource
    .map((effect, index) => {
      const title = inlineText(effect.name)
      const subtitle = inlineText(effect.tooltip || effect.description || '')
      const levelLabel = effect.level ? `Lv.${effect.level}` : ''
      return {
        key: `${title || 'effect'}-${index}`,
        title: title || '효과',
        subtitle: subtitle || '효과 설명이 없습니다.',
        levelLabel,
        icon: effect.icon || ''
      }
    })
    .slice(0, 4)

  const gemSummary =
    grid?.slots
      ?.flatMap(slot =>
        (slot.gems ?? []).map(gem => {
          const text = inlineText(gem.tooltip)
          const isOrder = /질서/i.test(text) || /order/i.test(text)
          const isChaos = /혼돈/i.test(text) || /chaos/i.test(text)
          const nameHint = text.match(/해|달|별/)?.[0] || ''
          const grade = inlineText(gem.grade) || ''
          return {
            key: `${slot.index}-${gem.index}`,
            label: `${isOrder ? '질서' : isChaos ? '혼돈' : '젬'}${nameHint ? `·${nameHint}` : ''}`,
            value: grade || '등급 미상'
          }
        })
      )
      .slice(0, 6) ?? []

  const slotPoints =
    grid?.slots?.slice(0, 6).map((slot, index) => ({
      key: `slot-${slot.index ?? index}`,
      label: inlineText(slot.name) || `슬롯 ${slot.index ?? index + 1}`,
      value: slot.point !== undefined ? `${slot.point}P` : ''
    })) ?? []

  const corePassives = effects
    .filter(effect => /진화|깨달음|도약/i.test(effect.title))
    .map(effect => ({
      ...effect,
      levelLabel: effect.levelLabel || 'Lv.'
    }))

  return {
    passiveTitle: inlineText(passive?.title),
    slotCount: grid?.slots?.length ?? 0,
    pointSummary,
    effects,
    gemSummary,
    slotPoints,
    corePassives
  }
})

const menuOpen = ref(false)
const sidebarRef = ref<HTMLElement | null>(null)

const syncActiveMenuWithRoute = () => {
  const currentName = typeof route.name === 'string' ? route.name : null
  const matchedMenu = currentName
    ? siteMenuItems.find(item => item.routeName === currentName) ?? null
    : null
  activeSiteMenu.value = matchedMenu?.key ?? DEFAULT_SITE_MENU
}

syncActiveMenuWithRoute()

watch(
  () => route.name,
  () => {
    syncActiveMenuWithRoute()
  }
)

const loadFromStorage = <T>(key: string, fallback: T): T => {
  if (typeof window === 'undefined' || !window.localStorage) return fallback
  try {
    const stored = window.localStorage.getItem(key)
    if (!stored) return fallback
    return JSON.parse(stored) as T
  } catch (err) {
    console.warn(`Failed to parse local storage key '${key}'`, err)
    return fallback
  }
}

const saveToStorage = (key: string, value: unknown) => {
  if (typeof window === 'undefined' || !window.localStorage) return
  try {
    window.localStorage.setItem(key, JSON.stringify(value))
  } catch (err) {
    console.warn(`Failed to persist local storage key '${key}'`, err)
  }
}

const loadFavoritesFromStorage = () => {
  const stored = loadFromStorage<CharacterProfile[]>(FAVORITES_STORAGE_KEY, [])
  if (stored.length) {
    favorites.value = stored
  }
}

const persistFavoritesToStorage = () => {
  saveToStorage(FAVORITES_STORAGE_KEY, favorites.value)
}

type FavoriteIdentity = Pick<CharacterProfile, 'characterName' | 'serverName'>

const isSameFavorite = (target: FavoriteIdentity, other: FavoriteIdentity) => {
  return target.characterName === other.characterName && target.serverName === other.serverName
}

const upsertFavoriteLocal = (profile: CharacterProfile) => {
  if (!profile?.characterName || !profile?.serverName) return
  const filtered = favorites.value.filter(fav => !isSameFavorite(fav, profile))
  favorites.value = [...filtered, profile]
  persistFavoritesToStorage()
}

const removeFavoriteLocal = (identity: FavoriteIdentity) => {
  const next = favorites.value.filter(fav => !isSameFavorite(fav, identity))
  if (next.length !== favorites.value.length) {
    favorites.value = next
    persistFavoritesToStorage()
  }
}

const loadHistoryFromStorage = () => {
  const stored = loadFromStorage<SearchHistory[]>(HISTORY_STORAGE_KEY, [])
  if (stored.length) {
    history.value = stored
  }
}

const persistHistoryToStorage = () => {
  saveToStorage(HISTORY_STORAGE_KEY, history.value)
}

const openMenu = () => {
  menuOpen.value = true
  nextTick(() => {
    sidebarRef.value?.focus()
  })
}

const closeMenu = () => {
  menuOpen.value = false
}

const handleMenuSelect = (menu: SiteMenuItem) => {
  if (!menu.available) return
  activeSiteMenu.value = menu.key
  if (menu.routeName) {
    router.push({ name: menu.routeName })
  }
  closeMenu()
}

const expandHeroImage = () => {
  if (hasCharacterImage.value) {
    isHeroImageLarge.value = true
  }
}

const shrinkHeroImage = () => {
  isHeroImageLarge.value = false
}

const openHeroImagePopup = () => {
  if (hasCharacterImage.value) {
    isHeroImagePopupOpen.value = true
  }
}

const closeHeroImagePopup = () => {
  isHeroImagePopupOpen.value = false
}

watch(characterImageSrc, () => {
  isHeroImageLarge.value = false
  isHeroImagePopupOpen.value = false
})

watch(activeCharacter, () => {
  isHeroImageLarge.value = false
  isHeroImagePopupOpen.value = false
})

const hoveredSpecialName = ref<string | null>(null)
const specialHoverTimeout = ref<number | null>(null)

const cancelSpecialHoverTimeout = () => {
  if (specialHoverTimeout.value !== null && typeof window !== 'undefined') {
    window.clearTimeout(specialHoverTimeout.value)
  }
  specialHoverTimeout.value = null
}

const scheduleSpecialHoverClear = () => {
  if (typeof window === 'undefined') {
    hoveredSpecialName.value = null
    return
  }
  cancelSpecialHoverTimeout()
  specialHoverTimeout.value = window.setTimeout(() => {
    hoveredSpecialName.value = null
    specialHoverTimeout.value = null
  }, 120)
}

interface ParadiseInfo {
  season?: string
  power?: string
}

const paradiseInfo = computed<ParadiseInfo>(() => {
  const info: ParadiseInfo = {}
  const extractLastNumber = (text: string) => {
    const matches = text.match(/\d[\d.,]*/g)
    if (!matches || !matches.length) return ''
    return matches[matches.length - 1].replace(/[^\d.,]/g, '')
  }

  for (const special of specialEquipmentsDetailed.value) {
    const tooltipLines = extractTooltipLines(special.item.tooltip)
    const combinedLines = [...special.highlights, ...tooltipLines]
    const powerLine = combinedLines.find(text => /낙원력/i.test(text))
    const seasonLine = combinedLines.find(text => /시즌/i.test(text) && /낙원/i.test(text))

    if (powerLine && !info.power) {
      const value = extractLastNumber(powerLine)
      if (value) {
        info.power = value
      }
    }

    if (seasonLine && !info.season) {
      const match = seasonLine.match(/시즌\s*(\d+)/i)
      info.season = match?.[1] || extractLastNumber(seasonLine) || seasonLine.replace(/\s+/g, ' ').trim()
    }

    if (info.power && info.season) break
  }
  return info
})

const displayStats = computed<CharacterStat[]>(() => {
  const stats = activeCharacter.value?.stats
    ? activeCharacter.value.stats.filter(stat => stat.type !== '낙원력').map(stat => ({ ...stat }))
    : []
  return stats
})

const tooltipWidthValue = computed(() => {
  if (!overviewWidth.value) return 320
  return Math.max(Math.round(overviewWidth.value - 20), 240)
})

const handleSpecialHover = (name: string | null) => {
  cancelSpecialHoverTimeout()
  hoveredSpecialName.value = name
}

const tooltipWidthStyle = computed(() => {
  const width = tooltipWidthValue.value
  return width ? { '--tooltip-width': `${width}px` } : {}
})

const syncOverviewWidth = () => {
  if (!characterOverviewRef.value) return
  const width = characterOverviewRef.value.getBoundingClientRect().width
  if (!width) return
  if (overviewWidth.value !== width) {
    overviewWidth.value = width
  }
}

let overviewObserver: ResizeObserver | null = null

const observeOverviewCard = () => {
  if (typeof window === 'undefined') return
  overviewObserver?.disconnect()
  const el = characterOverviewRef.value
  if (el && 'ResizeObserver' in window) {
    overviewObserver = new ResizeObserver(entries => {
      const entryWidth = entries[0]?.contentRect.width ?? el.getBoundingClientRect().width
      if (entryWidth) {
        overviewWidth.value = entryWidth
      }
    })
    overviewObserver.observe(el)
    syncOverviewWidth()
  } else {
    syncOverviewWidth()
  }
}

const panelFilterQuery = computed(() => characterName.value.trim().toLowerCase())

const panelHistoryItems = computed(() => {
  if (activeSearchPanelTab.value !== 'recent') return history.value
  if (!panelFilterQuery.value) return history.value
  return history.value.filter(item => item.characterName.toLowerCase().includes(panelFilterQuery.value))
})

const panelFavoriteItems = computed(() => favorites.value)

const closeSearchPanel = () => {
  searchPanelOpen.value = false
}

const handleSearchFocus = () => {
  searchPanelOpen.value = true
  activeSearchPanelTab.value = 'recent'
}

const handleSearchPanelSelect = (name: string) => {
  closeSearchPanel()
  executeSearch(name)
}

const handleOutsideSearchClick = (event: MouseEvent) => {
  if (!searchPanelWrapperRef.value) return
  if (searchPanelWrapperRef.value.contains(event.target as Node)) return
  closeSearchPanel()
}

const expeditionGroups = computed(() => {
  const groups = new Map<string, SiblingCharacter[]>()
  const addToGroup = (member: SiblingCharacter) => {
    const key = member.serverName || '기타'
    if (!groups.has(key)) groups.set(key, [])
    const normalizedMember: SiblingCharacter = {
      ...member,
      itemMaxLevel: member.itemMaxLevel || member.itemAvgLevel
    }
    groups.get(key)!.push(normalizedMember)
  }

  if (character.value) {
    addToGroup({
      serverName: character.value.serverName,
      characterName: character.value.characterName,
      characterLevel: character.value.characterLevel ?? undefined,
      characterClassName: character.value.characterClassName,
      itemAvgLevel: character.value.itemAvgLevel,
      itemMaxLevel: character.value.itemMaxLevel || character.value.itemAvgLevel,
      characterImage: character.value.characterImage || ''
    })
  }
  siblings.value.forEach(addToGroup)

  return Array.from(groups.entries()).map(([server, members]) => ({
    server,
    members
  }))
})

const handleResize = () => {
  syncOverviewWidth()
}

onMounted(() => {
  loadFavoritesFromStorage()
  loadHistoryFromStorage()
  loadFavorites()
  loadHistory()
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', handleResize)
  }
  if (typeof document !== 'undefined') {
    document.addEventListener('click', handleOutsideSearchClick)
  }
  nextTick(() => {
    observeOverviewCard()
  })

  // URL에서 캐릭터 이름 확인 후 자동 로드
  const urlCharacter = route.query.character
  if (urlCharacter && typeof urlCharacter === 'string') {
    searchCharacter(urlCharacter, { updateUrl: false })
  }
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', handleResize)
  }
  overviewObserver?.disconnect()
  cancelSpecialHoverTimeout()
  if (typeof document !== 'undefined') {
    document.removeEventListener('click', handleOutsideSearchClick)
  }
})

watch(
  specialEquipmentsDetailed,
  newList => {
    if (!newList.some(special => special.item.name === hoveredSpecialName.value)) {
      handleSpecialHover(null)
    }
  },
  { deep: false }
)

watch(
  () => characterOverviewRef.value,
  () => {
    nextTick(() => {
      if (characterOverviewRef.value) {
        observeOverviewCard()
      } else {
        overviewObserver?.disconnect()
        overviewWidth.value = 0
      }
    })
  }
)

// URL 쿼리 파라미터 변경 감지 (브라우저 뒤로가기/앞으로가기 지원)
watch(
  () => route.query.character,
  (newCharacter, oldCharacter) => {
    if (newCharacter && typeof newCharacter === 'string' && newCharacter !== oldCharacter) {
      // 현재 보고 있는 캐릭터와 다른 경우에만 로드
      if (newCharacter !== activeCharacter.value?.characterName) {
        searchCharacter(newCharacter, { updateUrl: false })
      }
    }
  }
)

watch(activeResultTab, async newTab => {
  await nextTick()
  syncOverviewWidth()
  handleSpecialHover(null)
  if (newTab === 'arkGrid') {
    await ensureArkGridData()
  } else if (newTab === 'skills') {
    await ensureSkillData()
  } else if (newTab === 'collection') {
    await ensureCollectiblesData()
  } else if (newTab === 'summary') {
    await Promise.all([ensureSkillData(), ensureCollectiblesData(), ensureArkGridData()])
  }
})

const searchCharacterByInput = () => {
  const name = characterName.value.trim()
  if (!name) {
    error.value = {
      title: '검색어 필요',
      message: '캐릭터명을 입력해 주세요.',
      type: 'warning'
    }
    return
  }
  executeSearch(name)
}

const searchCharacter = async (name: string, options: { forceRefresh?: boolean; updateUrl?: boolean } = {}) => {
  loading.value = true
  error.value = null
  if (options.forceRefresh) {
    lostarkApi.invalidateCharacterCache(name)
  }
  character.value = null
  siblings.value = []
  selectedCharacterProfile.value = null
  detailEquipment.value = []
  detailEngravings.value = []
  detailError.value = null
  characterAvailability.value = {}
  arkGridResponse.value = null
  arkGridLoadedFor.value = null
  arkGridError.value = null
  arkGridLoading.value = false
  skillResponse.value = null
  skillLoadedFor.value = null
  skillError.value = null
  skillLoading.value = false
  collectibles.value = []
  collectiblesLoadedFor.value = null
  collectiblesError.value = null
  collectiblesLoading.value = false
  lastRefreshedAt.value = null

  try {
    const charResponse = await lostarkApi.getCharacter(name, { force: options.forceRefresh })
    character.value = charResponse.data
    characterName.value = name

    const siblingsResponse = await lostarkApi.getSiblings(name, { force: options.forceRefresh })
    const unique = new Map<string, SiblingCharacter>()
    siblingsResponse.data.forEach(member => {
      if (member.characterName === charResponse.data.characterName) return
      const key = `${member.serverName}-${member.characterName}`
      if (!unique.has(key)) {
        unique.set(key, member)
      }
    })
    siblings.value = Array.from(unique.values())

    await loadCharacterDetails(name, { profile: charResponse.data, forceRefresh: options.forceRefresh })
    if (options.forceRefresh) {
      await Promise.allSettled([
        loadSkillData(name, { forceRefresh: true }),
        loadCollectiblesData(name, { forceRefresh: true }),
        loadArkGridData(name, { forceRefresh: true })
      ])
      await loadHistory({ forceRefresh: true })
    } else {
      await loadHistory()
    }
    await Promise.allSettled([
      ensureSkillData({ forceRefresh: options.forceRefresh }),
      ensureCollectiblesData({ forceRefresh: options.forceRefresh }),
      ensureArkGridData({ forceRefresh: options.forceRefresh })
    ])
    activeResultTab.value = DEFAULT_RESULT_TAB

    // URL 업데이트 (기본값: true)
    if (options.updateUrl !== false) {
      router.push({ query: { character: name } })
    }
  } catch (err: any) {
    const errorData = err.response?.data
    if (err.response?.status === 404) {
      error.value = {
        title: '캐릭터를 찾을 수 없습니다',
        message: errorData?.message || `'${name}' 캐릭터가 존재하지 않아요.`,
        type: 'error'
      }
    } else {
      error.value = {
        title: '검색 실패',
        message: errorData?.message || '예상치 못한 오류가 발생했어요.',
        type: 'error'
      }
    }
  } finally {
    loading.value = false
  }
}
const retrySearch = () => {
  if (characterName.value) {
    searchCharacter(characterName.value)
  }
}

const handleRefreshClick = () => {
  if (loading.value) return
  const target = activeCharacter.value?.characterName?.trim()
  if (!target) return
  searchCharacter(target, { forceRefresh: true })
}

const dismissError = () => {
  error.value = null
}

const clearSearch = () => {
  characterName.value = ''
  character.value = null
  siblings.value = []
  error.value = null
  selectedCharacterProfile.value = null
  detailEquipment.value = []
  detailEngravings.value = []
  detailError.value = null
  characterAvailability.value = {}
  arkGridResponse.value = null
  arkGridLoadedFor.value = null
  arkGridError.value = null
  arkGridLoading.value = false
  skillResponse.value = null
  skillLoadedFor.value = null
  skillError.value = null
  skillLoading.value = false
  collectibles.value = []
  collectiblesLoadedFor.value = null
  collectiblesError.value = null
  collectiblesLoading.value = false
  lastRefreshedAt.value = null
  activeResultTab.value = DEFAULT_RESULT_TAB
  handleSpecialHover(null)
}

const loadFavorites = async () => {
  try {
    const response = await lostarkApi.getFavorites()
    favorites.value = response.data
    persistFavoritesToStorage()
  } catch (err) {
    console.error('즐겨찾기 로딩 실패:', err)
  }
}

let favoriteMutationId = 0

const toggleFavorite = () => {
  const targetCharacter = activeCharacter.value
  if (!targetCharacter) return

  const snapshot = favorites.value.slice()
  const shouldFavorite = !isCharacterFavorite.value
  const currentMutationId = ++favoriteMutationId

  if (shouldFavorite) {
    upsertFavoriteLocal(targetCharacter)
  } else {
    removeFavoriteLocal(targetCharacter)
  }

  const request = shouldFavorite
    ? lostarkApi.addFavorite(targetCharacter.characterName)
    : lostarkApi.removeFavorite(targetCharacter.characterName)

  request
    .catch(err => {
      if (currentMutationId === favoriteMutationId) {
        favorites.value = snapshot
        persistFavoritesToStorage()
      }
      console.error('즐겨찾기 토글 실패:', err)
    })
    .finally(() => {
      if (currentMutationId === favoriteMutationId) {
        loadFavorites().catch(loadErr => {
          console.error('즐겨찾기 동기화 실패:', loadErr)
        })
      }
    })
}

const loadHistory = async (options: { forceRefresh?: boolean } = {}) => {
  try {
    const response = await lostarkApi.getHistory({ force: options.forceRefresh })
    const items = response.data
      .slice()
      .sort((a, b) => new Date(b.searchedAt).getTime() - new Date(a.searchedAt).getTime())
    const seen = new Set<string>()
    const unique: typeof items = []
    for (const it of items) {
      if (!seen.has(it.characterName)) {
        seen.add(it.characterName)
        unique.push(it)
      }
    }
    history.value = unique
    persistHistoryToStorage()
  } catch (err) {
    console.error('검색 기록 로딩 실패:', err)
  }
}

const clearHistory = async () => {
  if (!confirm('검색 기록을 모두 삭제할까요?')) return
  try {
    await lostarkApi.clearHistory()
    history.value = []
    persistHistoryToStorage()
  } catch (err) {
    console.error('검색 기록 삭제 실패:', err)
  }
}

const loadCharacterDetails = async (
  name: string,
  options: { profile?: CharacterProfile; forceRefresh?: boolean } = {}
) => {
  detailLoading.value = true
  detailError.value = null
  characterAvailability.value[name] = 'loading'

  try {
    const profilePromise = options.profile
      ? Promise.resolve(options.profile)
      : lostarkApi.getCharacter(name, { force: options.forceRefresh }).then(res => res.data)

    const [profile, equipmentResponse, engravingsResponse] = await Promise.all([
      profilePromise,
      lostarkApi.getEquipment(name, { force: options.forceRefresh }),
      lostarkApi.getEngravings(name, { force: options.forceRefresh })
    ])

    selectedCharacterProfile.value = profile
    detailEquipment.value = equipmentResponse.data
    detailEngravings.value = engravingsResponse.data
    characterAvailability.value[name] = 'available'
    lastRefreshedAt.value = new Date()
  } catch (err: any) {
    characterAvailability.value[name] = 'unavailable'
    selectedCharacterProfile.value = options.profile || null
    detailEquipment.value = []
    detailEngravings.value = []
    if (err.response?.status === 404) {
      detailError.value = `'${name}' 캐릭터 정보를 불러올 수 없어요. 오랜 기간 미접속 캐릭터일 수 있습니다.`
    } else {
      detailError.value = err.response?.data?.message || '상세 정보를 불러오지 못했어요.'
    }
    console.error('Failed to load character details', err)
  } finally {
    detailLoading.value = false
  }
}

const ensureSkillData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (skillLoading.value) return
  if (!options.forceRefresh && skillLoadedFor.value === targetName && skillResponse.value) return
  await loadSkillData(targetName, options)
}

const loadSkillData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
  skillLoading.value = true
  skillError.value = null
  try {
    const response = await lostarkApi.getSkills(name, { force: options.forceRefresh })
    skillResponse.value = response.data
    skillLoadedFor.value = name
  } catch (err: any) {
    skillResponse.value = null
    skillLoadedFor.value = null
    const message =
      err.response?.status === 404
        ? `'${name}' 캐릭터의 스킬 정보를 찾을 수 없어요.`
        : err.response?.data?.message || '스킬 정보를 불러오지 못했어요.'
    skillError.value = message
  } finally {
    skillLoading.value = false
  }
}

const ensureCollectiblesData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (collectiblesLoading.value) return
  if (!options.forceRefresh && collectiblesLoadedFor.value === targetName && collectibles.value.length) return
  await loadCollectiblesData(targetName, options)
}

const loadCollectiblesData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
  collectiblesLoading.value = true
  collectiblesError.value = null
  try {
    const response = await lostarkApi.getCollectibles(name, { force: options.forceRefresh })
    collectibles.value = response.data
    collectiblesLoadedFor.value = name
  } catch (err: any) {
    collectibles.value = []
    collectiblesLoadedFor.value = null
    const message =
      err.response?.status === 404
        ? `'${name}' 캐릭터의 수집 정보를 찾을 수 없어요.`
        : err.response?.data?.message || '수집 정보를 불러오지 못했어요.'
    collectiblesError.value = message
  } finally {
    collectiblesLoading.value = false
  }
}

const ensureArkGridData = async (options: { forceRefresh?: boolean } = {}) => {
  const targetName = character.value?.characterName
  if (!targetName) return
  if (arkGridLoading.value) return
  if (!options.forceRefresh && arkGridLoadedFor.value === targetName && arkGridResponse.value) return
  await loadArkGridData(targetName, options)
}

const loadArkGridData = async (name: string, options: { forceRefresh?: boolean } = {}) => {
  arkGridLoading.value = true
  arkGridError.value = null
  try {
    const response = await lostarkApi.getArkGrid(name, { force: options.forceRefresh })
    arkGridResponse.value = response.data
    arkGridLoadedFor.value = name
  } catch (err: any) {
    arkGridResponse.value = null
    const message =
      err.response?.status === 404
        ? `'${name}' 캐릭터의 아크 그리드 정보를 확인할 수 없어요.`
        : err.response?.data?.message || '아크 그리드 정보를 불러오지 못했어요.'
    arkGridError.value = message
  } finally {
    arkGridLoading.value = false
  }
}

const executeSearch = (name: string) => {
  const trimmed = name.trim()
  if (!trimmed) return
  closeSearchPanel()
  characterName.value = trimmed
  searchCharacter(trimmed, { forceRefresh: true })
}

type AutocompleteSelectPayload = Suggestion | { id: string; name: string }

const handleSuggestionSelect = (suggestion: AutocompleteSelectPayload) => {
  executeSearch(suggestion.name)
}

const viewCharacterDetail = (summary: CharacterProfile | SiblingCharacter) => {
  if (characterAvailability.value[summary.characterName] === 'loading') return
  activeResultTab.value = 'detail'
  const isPrimary = character.value?.characterName === summary.characterName
  const profile = isPrimary ? character.value : undefined
  loadCharacterDetails(summary.characterName, { profile })

  // URL 업데이트
  router.push({ query: { character: summary.characterName } })
}

const formatItemLevel = (value?: string | number) => {
  return formatNumberLocalized(value, 2)
}

const extractTooltipLines = (tooltip?: string): string[] => {
  if (!tooltip) return []
  try {
    const raw = JSON.parse(tooltip)
    const normalize = (value: any): string[] => {
      if (!value) return []
      if (typeof value === 'string') return [cleanTooltipLine(value)]
      if (Array.isArray(value)) return value.flatMap(normalize)
      if (typeof value === 'object') {
        if ('value' in value) return normalize(value.value)
        return Object.values(value).flatMap(normalize)
      }
      return []
    }
    return Object.values(raw).flatMap(normalize).filter(Boolean)
  } catch {
    return [cleanTooltipLine(tooltip)]
  }
}

const addFallbackLineBreaks = (value: string): string => {
  if (!value) return value
  if (value.includes('\n')) return value

  const insertSentenceBreaks = (text: string) => {
    return text.replace(/(?<!\d)([.!?])\s+/g, (_, mark) => `${mark}\n`)
  }

  const insertStatBreaks = (text: string) => {
    return text.replace(/\s+(?=[+-]\d)/g, '\n')
  }

  const formatLines = (text: string) =>
    text
      .split('\n')
      .map(part => part.trim())
      .filter(Boolean)
      .join('\n')

  const withSentenceBreaks = insertSentenceBreaks(value)
  if (withSentenceBreaks.includes('\n')) {
    return formatLines(withSentenceBreaks)
  }

  const withStatBreaks = insertStatBreaks(value)
  if (withStatBreaks.includes('\n')) {
    return formatLines(withStatBreaks)
  }

  if (value.length <= 80) return value

  const segments: string[] = []
  let start = 0
  const length = value.length
  while (start < length) {
    let end = Math.min(start + 70, length)
    if (end === length) {
      segments.push(value.slice(start).trim())
      break
    }
    let breakIndex = value.lastIndexOf(' ', end)
    if (breakIndex <= start + 30) {
      breakIndex = value.indexOf(' ', end)
    }
    if (breakIndex === -1) {
      segments.push(value.slice(start).trim())
      break
    }
    segments.push(value.slice(start, breakIndex).trim())
    start = breakIndex + 1
  }
  return segments.join('\n')
}

const cleanTooltipLine = (text: string) => {
  const normalized = text
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/\\r\\n|\\n|\\r/g, '\n')
    .replace(/&[^;]+;/g, ' ')
    .split('\n')
    .map(part => part.replace(/\s+/g, ' ').trim())
    .filter(Boolean)
    .join('\n')
  return addFallbackLineBreaks(normalized)
}

const tooltipIgnorePatterns = [
  /캐릭터 귀속/,
  /거래 불가/,
  /분해 불가/,
  /판매 불가/,
  /선택 불가/,
  /획득 시 귀속/,
  /추가 설명/,
  /품질/i
]

const getSpecialHighlights = (item: Equipment): string[] => {
  const lines = extractTooltipLines(item.tooltip)
  if (!lines.length) return []
  const meaningfulLines = lines.filter(
    line => !tooltipIgnorePatterns.some(pattern => pattern.test(line))
  )
  const highlightRegex = /(기본 효과|추가 효과|연마 효과|슬롯 효과|추가 피해|전투 중|아크 패시브|내구도|소지품|이동 속도|선박|항해|효과|낙원력)/i
  const preferred = meaningfulLines.filter(line => highlightRegex.test(line))
  const source =
    preferred.length || meaningfulLines.length ? (preferred.length ? preferred : meaningfulLines) : lines
  return Array.from(new Set(source)).slice(0, 4)
}

const getSpecialLabel = (item: Equipment): string => {
  const target = `${item.type ?? ''} ${item.name ?? ''}`.toLowerCase()
  const keyword = specialEquipmentKeywords.find(word => target.includes(word.toLowerCase()))
  if (keyword) return keyword
  if (item.type) return item.type
  return '항해 장비'
}

const normalizeStatValue = (value?: string | string[]) => {
  if (!value) return ''
  if (Array.isArray(value)) {
    return value.join(' / ')
  }
  return value
    .replace(/<br\s*\/?>/gi, ' / ')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .trim()
}

const formatNumberLocalized = (value?: number | string, fractionDigits?: number) => {
  if (value === undefined || value === null || value === '') return '—'
  const numeric =
    typeof value === 'number'
      ? value
      : Number(
          value
            .toString()
            .replace(/,/g, '')
            .trim()
        )
  if (Number.isNaN(numeric)) {
    return typeof value === 'string' && value.length ? value : '—'
  }
  const options: Intl.NumberFormatOptions = {}
  if (typeof fractionDigits === 'number') {
    options.minimumFractionDigits = fractionDigits
    options.maximumFractionDigits = fractionDigits
  }
  return numeric.toLocaleString(undefined, options)
}

const formatProfileStat = (value?: string | string[]) => {
  const normalized = normalizeStatValue(value)
  if (!normalized.length) return '—'
  const percentMatch = normalized.match(/^([+-]?\d+(?:\.\d+)?)\s*%$/)
  if (percentMatch) {
    const formatted = formatNumberLocalized(
      percentMatch[1],
      percentMatch[1].includes('.') ? 2 : undefined
    )
    return formatted === '—' ? normalized : `${formatted}%`
  }
  const numericMatch = normalized.match(/^([+-]?\d+(?:\.\d+)?)(?:\s*점)?$/)
  if (numericMatch) {
    const formatted = formatNumberLocalized(
      numericMatch[1],
      numericMatch[1].includes('.') ? 2 : undefined
    )
    return formatted
  }
  return normalized
}

const formatCombatPower = (value?: number | string) => formatNumberLocalized(value)

const formatInteger = (value?: number | string) => formatNumberLocalized(value)
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
}

.page-header {
  display: grid;
  grid-template-columns: auto minmax(320px, 1fr) auto;
  align-items: center;
  column-gap: 20px;
  padding: 20px 40px;
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
  border-bottom: 1px solid var(--border-color);
  justify-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-header h1 {
  font-size: calc(1.5rem - 2px);
  color: var(--text-primary);
  margin: 0;
  font-weight: 700;
}

.header-search {
  justify-self: center;
  width: min(720px, 100%);
}

.header-search :deep(.autocomplete-container) {
  width: 100%;
}

.header-right {
  width: 48px;
  height: 1px;
}

.page-footer {
  margin-top: auto;
  padding: 18px 40px;
  background: var(--card-bg);
  border-top: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  display: flex;
  justify-content: center;
}

.footer-inner {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
  font-weight: 700;
}

.footer-badge {
  padding: 8px 12px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  box-shadow: var(--shadow-xs, 0 2px 6px rgba(0, 0, 0, 0.06));
}

.footer-copy {
  font-size: calc(0.95rem - 2px);
  color: var(--text-secondary);
}

.menu-button {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
}

.menu-button span {
  width: 18px;
  height: 2px;
  background: var(--text-primary);
  display: block;
}

.menu-button:hover {
  background: var(--bg-hover);
  transform: translateY(-1px);
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 80;
}

.sidebar-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 280px;
  height: 100%;
  background: var(--card-bg);
  border-right: 1px solid var(--border-color);
  box-shadow: 8px 0 20px rgba(15, 23, 42, 0.2);
  z-index: 90;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  outline: none;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-close {
  background: transparent;
  border: none;
  font-size: calc(1.6rem - 2px);
  color: var(--text-secondary);
  cursor: pointer;
}

.sidebar-content h3 {
  margin: 0 0 10px;
  color: var(--text-primary);
  font-size: calc(1rem - 2px);
}

.sidebar-description {
  margin: 0 0 16px;
  color: var(--text-secondary);
  font-size: calc(0.9rem - 2px);
}

.sidebar-menu-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar-menu-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.sidebar-menu-item:hover:not(.disabled) {
  border-color: var(--primary-color);
  transform: translateX(2px);
}

.sidebar-menu-item.active {
  border-color: var(--primary-color);
  background: rgba(102, 126, 234, 0.12);
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.12);
}

.dark .sidebar-menu-item.active {
  background: rgba(124, 143, 216, 0.2);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.45);
}

.sidebar-menu-item.disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.sidebar-menu-icon {
  font-size: calc(1.4rem - 2px);
}

.sidebar-menu-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-menu-label {
  font-weight: 600;
  color: var(--text-primary);
}

.sidebar-menu-desc {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.sidebar-menu-badge {
  font-size: var(--font-xs);
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
}

.sidebar-footnote {
  margin: 16px 0 0;
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
}

.sidebar-slide-enter-from,
.sidebar-slide-leave-to {
  transform: translateX(-100%);
}

.sidebar-slide-enter-active,
.sidebar-slide-leave-active {
  transition: transform 0.25s ease;
}

.sidebar-fade-enter-from,
.sidebar-fade-leave-to {
  opacity: 0;
}

.sidebar-fade-enter-active,
.sidebar-fade-leave-active {
  transition: opacity 0.25s ease;
}

.content-wrapper {
  display: flex;
  flex: 1;
  gap: var(--space-2xl);
  padding: 30px clamp(var(--space-xl), 5vw, 0px);
  background: var(--bg-secondary);
  box-sizing: border-box;
}

.main-content {
  flex: 1 1 960px;
  /* max-width: 1200px; */
  padding: 0px var(--space-2xl);
  /* overflow-y: auto; */
  background: var(--bg-secondary);
  /* border-radius: var(--radius-2xl); */
  /* box-shadow: var(--shadow-sm); */
  border-left: 1px dashed var(--border-color);
  border-right: 1px dashed var(--border-color);
}

.ad-slot {
  flex: 0 0 clamp(140px, 12vw, 220px);
  border-radius: var(--radius-2xl);
  border: 1px dashed var(--border-color);
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  font-size: var(--font-sm);
  text-align: center;
  padding: var(--space-xl);
  min-height: 60vh;
}

.ad-slot span {
  pointer-events: none;
}

.search-input {
  flex: 1;
  padding: var(--space-md) var(--space-lg);
  font-size: var(--font-base);
  border: 2px solid var(--input-border);
  border-radius: var(--radius-sm);
  background: var(--input-bg);
  color: var(--text-primary);
}

.states-section {
  margin-bottom: 30px;
}

.states-section h2 {
  font-size: var(--font-xl);
  color: var(--text-primary);
  margin-bottom: 15px;
  font-weight: var(--font-bold);
}

.states-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-xl);
}

.state-card {
  padding: var(--space-xl);
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 2px solid var(--border-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 120px;
}

.state-label {
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
  font-weight: 600;
  margin-bottom: 10px;
}

.character-results {
  display: flex;
  flex-direction: column;
  gap: var(--space-2xl);
}

.results-layout {
  display: flex;
  gap: var(--space-2xl);
  align-items: stretch;
}

.results-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}

.view-tabs {
  display: flex;
  gap: var(--space-sm);
}

.view-tab-button {
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-size: var(--font-sm);
  font-weight: var(--font-semibold);
  cursor: pointer;
  transition: all var(--transition-base);
}

.view-tab-button.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.25);
}

.view-tab-button:not(.active):hover {
  background: var(--bg-hover);
}

.summary-panel {
  min-height: 360px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: var(--space-xl);
}

.summary-grid--modules {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.summary-grid--stacked {
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
}

.summary-card {
  border: none;
  border-radius: var(--radius-lg);
  background: transparent;
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  box-shadow: none;
}

.summary-card--module {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: var(--space-md);
  min-height: 200px;
}

.summary-card__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-md);
}

.summary-card h4 {
  margin: 0;
  font-size: calc(var(--font-lg) - 4px);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.summary-eyebrow {
  margin: 0 0 4px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: calc(var(--font-xs) - 1px);
  color: var(--primary-color);
  font-weight: 700;
}

.summary-chip {
  align-self: center;
  border-radius: var(--radius-full);
  padding: 6px 12px;
  font-size: var(--font-sm);
  font-weight: 700;
  background: rgba(102, 126, 234, 0.12);
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
}

.summary-chip--muted {
  background: var(--bg-hover);
  color: var(--text-secondary);
  border-color: var(--border-color);
}

.summary-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.summary-list--flat {
  gap: var(--space-sm);
}

.summary-list--split .summary-list-item {
  align-items: center;
}

.summary-list--compact .summary-list-item {
  padding: 10px 12px;
}

.summary-list-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-md);
  padding: 8px 0;
}

.summary-list-item--plain {
  padding: 8px 6px;
}

.summary-list-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-title {
  margin: 0;
  font-size: calc(var(--font-base) - 1px);
  font-weight: 600;
  color: var(--text-primary);
}

.summary-sub {
  margin: 0;
  font-size: calc(var(--font-sm) - 1px);
  color: var(--text-secondary);
}

.summary-pill-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

.summary-pill-row--wrap {
  flex-wrap: wrap;
}

.summary-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: var(--radius-full);
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-xs);
  border: none;
  white-space: nowrap;
}

.summary-pill--primary {
  background: rgba(102, 126, 234, 0.16);
  color: var(--primary-color);
}

.summary-pill--accent {
  background: rgba(236, 72, 153, 0.2);
  color: #ec4899;
}

.summary-pill--ghost {
  background: var(--bg-secondary);
}

.summary-list-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.summary-inline {
  margin: 0;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-progress-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.summary-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0;
}

.summary-progress__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.summary-progress__bar {
  position: relative;
  width: 100%;
  height: 8px;
  border-radius: 12px;
  background: var(--bg-hover);
  overflow: hidden;
}

.summary-progress__bar span {
  position: absolute;
  inset: 0;
  width: 0;
  display: block;
  background: linear-gradient(90deg, var(--primary-color), #a855f7);
  transition: width 0.4s ease;
}

.summary-progress__meta {
  margin: 0;
  font-size: calc(var(--font-xs) - 1px);
  color: var(--text-secondary);
}

.summary-footnotes {
  list-style: none;
  margin: 0;
  padding: 10px 0 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-top: 1px solid var(--border-color);
}

.summary-footnote-label {
  font-size: var(--font-xs);
  font-weight: 700;
  color: var(--primary-color);
  margin-right: 6px;
}

.summary-footnote-text {
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-note {
  margin: 2px 0 0;
  font-size: calc(var(--font-sm) - 1px);
  color: var(--text-secondary);
}

.summary-note--warning {
  color: var(--warning-color, #d97706);
}

.summary-icon {
  border-radius: 8px;
}

.summary-icon--fallback {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--bg-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: var(--text-secondary);
}

.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: var(--space-lg);
}

.equipment-column h5 {
  margin: 0 0 8px;
  font-size: var(--font-sm);
  color: var(--text-secondary);
}

.summary-pill-col {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.summary-progress-list--dense .summary-progress {
  padding: 8px 0;
}

.placeholder-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}

.character-overview-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  background: var(--card-bg);
  border-radius: 20px;
  padding: 15px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
  gap: 25px;
  flex: 0 0 380px;
  height: fit-content;
  overflow: visible;
  max-width: 350px;
}

.overview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.refresh-button {
  padding: 8px 12px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 700;
  cursor: pointer;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.refresh-button:hover,
.refresh-button:focus-visible {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-timestamp {
  margin-left: auto;
  color: var(--text-secondary);
  font-size: calc(0.95rem - 2px);
  white-space: nowrap;
}

.hero-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-levels-grid {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.hero-row--image {
  display: flex;
  gap: 16px;
  align-items: center;
}

.hero-avatar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.hero-avatar-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 16px;
  overflow: hidden;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: width 0.2s ease, height 0.2s ease, background-color 0.3s ease, border-color 0.3s ease;
}

.hero-avatar-wrapper.is-large {
  width: 300px;
  height: 350px;
}

.hero-avatar-wrapper :deep(.hero-avatar) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 16px;
}

.hero-avatar-controls {
  display: flex;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  padding: 4px;
  border-radius: 999px;
  backdrop-filter: blur(8px);
}

.hero-avatar-btn {
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-size: calc(0.8rem - 2px);
  font-weight: 600;
  cursor: pointer;
  padding: 2px 8px;
  transition: color 0.2s ease;
}

.hero-avatar-btn + .hero-avatar-btn {
  border-left: 1px solid var(--border-color);
}

.hero-avatar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  color: var(--text-secondary);
}

.hero-avatar-btn:not(:disabled):hover,
.hero-avatar-btn:not(:disabled):focus-visible {
  color: var(--primary-color);
}

.hero-row--image :deep(.hero-avatar) {
  border-radius: 16px;
  object-fit: cover;
}

.character-portrait-overlay {
  position: fixed;
  inset: 0;
  background: var(--modal-overlay);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2200;
  padding: 24px;
}

.character-portrait-overlay__content {
  position: relative;
  max-width: min(90vw, 720px);
  max-height: 90vh;
  border-radius: 18px;
  overflow: hidden;
  background: var(--modal-bg);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xl);
}

.character-portrait-overlay__content img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.portrait-overlay__close {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: rgba(0, 0, 0, 0.6);
  color: var(--text-inverse, #ffffff);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.dark .portrait-overlay__close {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.portrait-overlay__close:hover {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.hero-title {
  font-size: calc(0.95rem - 2px);
  color: var(--text-secondary);
}

.hero-text{
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-text h2 {
  margin: 4px 0;
  font-size: calc(1.6rem - 2px);
  color: var(--text-primary);
  text-align: center;
}

.hero-text__header {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.favorite-toggle-btn {
  height: fit-content;
  border: none;
  background: transparent;
  cursor: pointer;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.favorite-toggle-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.favorite-toggle-btn:not(:disabled):hover {
  /* transform: scale(1.05); */
}

.favorite-star {
  display: inline-flex;
  width: 34px;
  height: 34px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--surface-color, #ffffff);
  color: var(--text-tertiary, #d1d5db);
  font-size: calc(1.2rem - 2px);
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.1);
  padding-bottom: 3px;
}

.favorite-toggle-btn.is-active .favorite-star {
  background: var(--warning-soft-bg, #ffe792);
  color: var(--text-inverse, #1a1a1a);
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.15);
}

.hero-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.meta-item {
  padding: 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.meta-item span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  margin-right: 5px;
}

.meta-item strong {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}

.hero-row--special {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: visible;
  align-items: center;
}

.special-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.special-header h3 {
  margin: 0;
  font-size: calc(1rem - 2px);
  color: var(--text-secondary);
}

.special-count {
  font-size: calc(0.85rem - 2px);
  color: var(--text-tertiary);
}

.special-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.special-grid--icons {
  display: grid;
  grid-template-columns: repeat(4, minmax(70px, 1fr));
  gap: 10px;
  overflow: visible;
  justify-items: center;
}

.special-icon-wrapper {
  position: relative;
  width: 100%;
  max-width: 110px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px;
  border: 2px solid transparent;
  border-radius: 16px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
  box-sizing: border-box;
}

.special-icon-wrapper.is-hovered .special-icon-box {
  box-shadow: none;
}

.special-icon-wrapper.is-hovered {
  border-color: rgba(99, 102, 241, 0.45);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.12);
}

.special-icon-wrapper:focus-visible .special-icon-box {
  outline: 2px solid var(--primary-color);
  border-radius: 14px;
  outline-offset: 2px;
}

.special-icon-wrapper:focus-visible {
  border-color: var(--primary-color);
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.25);
}

.special-icon-box {
  position: relative;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.special-icon-box :deep(.special-icon),
.special-icon {
  width: 100%;
  height: 100%;
  border-radius: 14px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: var(--text-secondary);
}

.special-icon--fallback {
  font-size: calc(1.1rem - 2px);
}

.special-label {
  width: 100%;
  font-size: calc(0.8rem - 2px);
  color: var(--text-secondary);
  text-align: center;
  white-space: nowrap;
  line-height: 1.2;
}

.special-tooltip {
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translate(-50%, -10px);
  width: min(var(--tooltip-width, 360px), 85vw);
  opacity: 0;
  pointer-events: none;
  box-sizing: border-box;
  z-index: 7;
  text-align: left;
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.special-icon-wrapper.is-hovered .special-tooltip {
  opacity: 1;
  pointer-events: auto;
  transform: translate(-50%, -2px);
}

.special-tooltip::before {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%) rotate(180deg);
  border-left: 9px solid transparent;
  border-right: 9px solid transparent;
  border-bottom: 9px solid var(--popover-bg);
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.35));
}

.special-type {
  font-size: calc(0.85rem - 2px);
  color: var(--text-tertiary);
}

.special-highlights {
  margin: 0;
  /* padding-left: 18px; */
  color: inherit;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.special-highlights span {
  list-style: disc;
  white-space: pre-line;
}

.special-tooltip-empty {
  margin: 0;
  font-size: 0.9rem;
  color: var(--popover-muted);
}

.profile-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.hero-row--profile-stats h3 {
  margin: 0;
  font-size: calc(1rem - 2px);
  color: var(--text-secondary);
  text-align: center;
}

.profile-stat {
  padding: 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.profile-stat span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  word-break: keep-all;
  /* min-width: 100px; */
}

.profile-stat strong {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}

.expedition-section,
.detail-panel {
  background: var(--card-bg);
  border-radius: 20px;
  padding: 24px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-md);
}

@media (max-width: 1024px) {
  .results-layout {
    flex-direction: column;
  }

  .character-overview-card {
    flex: 1;
  }
}

.section-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header-bar h3 {
  margin: 0;
  color: var(--text-primary);
}

.section-subtitle {
  margin: 4px 0 0;
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
}

.count-pill {
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-secondary);
}

.expedition-group + .expedition-group {
  margin-top: 20px;
}

.expedition-group h4 {
  margin: 0 0 10px 0;
  color: var(--text-secondary);
  font-size: calc(0.95rem - 2px);
}

.expedition-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.expedition-card {
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 14px;
  background: var(--bg-secondary);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: border 0.2s, transform 0.2s;
}

.expedition-card:hover {
  border-color: var(--primary-color);
  transform: translateY(-3px);
}

.expedition-card.active {
  border-color: var(--primary-color);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.2);
}

.member-top {
  display: flex;
  justify-content: space-between;
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.member-name {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}

.member-ilvl {
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
}

.member-detail {
  font-size: calc(0.8rem - 2px);
  color: var(--primary-color);
  font-weight: 600;
}

.search-panel-wrapper {
  position: relative;
  width: 100%;
}

.search-panel-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  left: 0;
  width: min(350px, 100%);
  max-width: 350px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: var(--shadow-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  z-index: 1200;
}

.search-panel-tabs {
  display: flex;
  gap: 8px;
}

.search-panel-tab {
  padding: 8px 18px;
  border-radius: 999px;
  border: 1px solid var(--border-color);
  background: var(--bg-secondary);
  color: var(--text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.search-panel-tab.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-inverse);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.25);
}

.search-panel-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 320px;
  overflow-y: auto;
}

.panel-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: calc(0.9rem - 2px);
  color: var(--text-secondary);
}

.panel-clear-btn {
  padding: 4px 12px;
  border-radius: 8px;
  border: none;
  background: var(--error-color);
  color: var(--text-inverse);
  cursor: pointer;
  font-size: calc(0.8rem - 2px);
  font-weight: 600;
}

.panel-empty {
  padding: 20px;
  text-align: center;
  border: 1px dashed var(--border-color);
  border-radius: 12px;
  color: var(--text-tertiary);
  font-size: calc(0.9rem - 2px);
}

.panel-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-list-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-secondary);
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  cursor: pointer;
  transition: border 0.2s, background 0.2s;
}

.panel-list-item:hover {
  border-color: var(--primary-color);
  background: var(--bg-hover);
}

.panel-list-name {
  font-size: calc(0.95rem - 2px);
  color: var(--text-primary);
  font-weight: 600;
}

.panel-list-meta {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

.panel-favorite-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-favorite-item {
  width: 100%;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  background: var(--bg-secondary);
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: border 0.2s, transform 0.2s;
}

.panel-favorite-item:hover {
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.panel-favorite-item :deep(.panel-favorite-image) {
  border-radius: 50%;
  object-fit: cover;
}

.panel-favorite-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.panel-favorite-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: calc(0.95rem - 2px);
}

.panel-favorite-meta {
  font-size: calc(0.85rem - 2px);
  color: var(--text-secondary);
}

@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
    padding: 0px 20px;
    gap: 20px;
  }

  .ad-slot {
    display: none;
  }

  .main-content {
    max-width: 100%;
    padding: 20px;
    /* border-radius: 16px; */
    box-shadow: none;
  }

  .states-grid {
    grid-template-columns: 1fr;
  }

  .character-layout {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 640px) {
  .page-header {
    padding: 15px 20px;
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .header-left {
    text-align: center;
    width: 350px;
    justify-content: space-between;
  }

  .page-header h1 {
    font-size: calc(1.2rem - 2px);
  }

  .header-search {
    width: 100%;
  }

  .header-right {
    display: none;
  }

  .page-footer {
    padding: 16px 20px;
  }

  .footer-inner {
    flex-direction: column;
    gap: 6px;
    text-align: center;
    letter-spacing: 0.06em;
  }

  .main-content {
    padding: 20px 15px;
  }

  .hero-row--levels .hero-levels {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .profile-stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  }

  .search-panel-dropdown {
    padding: 16px;
  }
}

.hero-row--paradise h3 {
  margin: 0;
  font-size: calc(1rem - 2px);
  color: var(--text-secondary);
  text-align: center;
}

.paradise-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
}

.paradise-item {
  padding: 10px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
}

.paradise-item span {
  font-size: calc(0.8rem - 2px);
  color: var(--text-tertiary);
  min-width: 70px;
}

.paradise-item strong {
  font-size: calc(1rem - 2px);
  color: var(--text-primary);
}
</style>
