# 읽기 쉬운 텍스트 블록 스타일 가이드 (WCAG 3.0 초안 기반)

> **카테고리**: Guide
> **최종 수정**: 2025-12-24
> **관련 문서**: [UX_OVERVIEW.md](./UX_OVERVIEW.md)

## 개요
- 목적: WCAG 3.0 “Readable blocks of text” 초안을 기준으로 웹 텍스트 블록의 기본 가독성 기준을 정리
- 적용 범위: 본문, 긴 설명, 안내문, 상세 콘텐츠 등 **문단 단위 텍스트 블록**
- 전제 조건: 언어별 값은 WCAG 3.0 초안 기준이며, 현재 일부 지표는 예시로 표기됨

---

## 1. WCAG 3.0 기준 요약 (Readable Blocks of Text)

### 1.1 요구사항 핵심
- **기본/저자 설정(default/authored) 표현**이 콘텐츠 언어에 해당하는 값에 맞아야 함
- 언어별 값이 없는 경우 **가장 유사한 철자 체계(orthography)** 언어의 값을 참조

### 1.2 초안 상태 안내
- WCAG 3.0 해당 항목은 **Developing(초안)** 상태
- 표의 지표는 **예시값**으로 표시되어 있음 (추후 변경 가능)

---

## 2. 웹 디자인 적용 범위

아래 항목은 “텍스트 블록”에 해당합니다:
- 문단(Paragraph)
- 장문의 설명 텍스트
- 카드/패널 내부의 본문 콘텐츠
- 리스트 아이템의 본문 설명

아래 항목은 본 가이드의 직접 범위가 아닙니다:
- 짧은 레이블/버튼 텍스트
- 단일 단어/숫자 중심의 UI 텍스트

---

## 3. WCAG 3.0 표출값 (초안 예시)

> **출처**: WCAG 3.0 “Readable blocks of text” 표에 기재된 English 기준 예시값

| 특성(Characteristic) | 표출값(English 예시) | 웹 디자인 적용 메모 |
|---|---|---|
| Inline margin | (값 미기재) | 언어별 지표 확정 전, 기존 그리드/레이아웃 기준 유지 |
| Block margin | **문단 주변 ≥ 0.5em** | 문단 간격 최소값으로 적용 |
| Line length | **30–100 characters** | `ch` 단위로 최대 폭 제어 |
| Line height | **1.0 – paragraph separation height** | 문단 간격이 줄 간격 이상이 되도록 설계 |
| Justification | **Left aligned or Justified** | 기본은 좌측 정렬, 강제 양쪽정렬은 콘텐츠 성격에 따라 검토 |

---

## 4. 구현 가이드 (웹)

### 4.1 레이아웃 기준
- **문단 간격**: 최소 `0.5em` 이상 유지
- **줄 길이 제한**: 30–100자 범위를 벗어나지 않도록 컨테이너 폭을 제어
- **정렬**: 기본 좌측 정렬, 양쪽 정렬은 긴 텍스트에서만 제한적으로 사용

### 4.2 CSS 적용 예시 (초안 기준)

```css
:root {
  /* WCAG 3.0 초안 예시값 */
  --text-line-length-min: 30ch;
  --text-line-length-max: 100ch;
  --text-paragraph-gap: 0.5em;
  --text-line-height: 1; /* 초안 표기 그대로 (추후 언어별 확정 필요) */
}

.readable-text-block {
  max-width: var(--text-line-length-max);
  min-width: var(--text-line-length-min);
  line-height: var(--text-line-height);
  text-align: left;
}

.readable-text-block p + p {
  margin-top: var(--text-paragraph-gap);
}
```

---

## 5. 검증 체크리스트

- 기본 텍스트 블록이 언어별 기준(또는 유사 언어 기준)을 만족한다
- 줄 길이가 30–100자 범위에 맞게 제어된다
- 문단 간격이 줄 간격보다 작지 않다
- 기본 정렬이 좌측 정렬이며, 양쪽 정렬은 목적성이 있다
- WCAG 3.0 초안 업데이트 시 기준값을 재검토한다

---

## 6. 변경 관리
- WCAG 3.0 표 기준이 업데이트되면 본 문서의 표출값과 CSS 예시를 먼저 갱신한다
- 언어별 지표(한국어 포함)가 확정되면 별도 표로 분리해 관리한다
