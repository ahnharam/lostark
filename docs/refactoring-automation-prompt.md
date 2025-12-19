# Utils 추출 자동화 프롬프트

## 사용 방법

이 프롬프트를 Claude에게 제공하여 새로운 utils 파일을 생성할 수 있습니다.

## 기본 프롬프트 템플릿

```
다음 요구사항에 따라 utils 파일을 생성해주세요:

**파일 경로:** `frontend/src/utils/[도메인]/[기능]DataTransform.ts`

**원본 컴포넌트:** `frontend/src/components/[컴포넌트명].vue`

**추출할 함수 목록:**
- `함수명1()` - 간단한 설명
- `함수명2()` - 간단한 설명
- `함수명3()` - 간단한 설명

**참고할 예시:** `frontend/src/utils/character/skillDataTransform.ts`

**요구사항:**
1. `skillDataTransform.ts`와 동일한 구조 사용
2. JSDoc 주석 포함
3. TypeScript 타입 안전성 보장
4. 모든 함수에 `@example` 추가
5. 내부 헬퍼 함수는 export하지 않음
6. 상수는 `export const`로 선언

**필요한 import:**
- API 타입: `@/api/types/...`
- 기존 유틸: `@/utils/...`

**추가 지시사항:**
[필요시 추가 요구사항 작성]
```

## 예시 1: equipmentDataTransform.ts 생성

```
다음 요구사항에 따라 utils 파일을 생성해주세요:

**파일 경로:** `frontend/src/utils/character/equipmentDataTransform.ts`

**원본 컴포넌트:** `frontend/src/components/CharacterSearch.vue`

**추출할 함수 목록:**
- `parseEquipmentMeta()` - 장비 메타 데이터 파싱
- `extractTranscendence()` - 초월 정보 추출
- `extractElixirLevel()` - 엘릭서 레벨 추출
- `calculateQuality()` - 품질 계산
- `extractSetEffect()` - 세트 효과 추출
- `parseEquipmentGrade()` - 장비 등급 파싱
- `extractItemLevel()` - 아이템 레벨 추출

**참고할 예시:** `frontend/src/utils/character/skillDataTransform.ts`

**요구사항:**
1. `skillDataTransform.ts`와 동일한 구조 사용
2. JSDoc 주석 포함
3. TypeScript 타입 안전성 보장
4. 모든 함수에 `@example` 추가
5. 내부 헬퍼 함수는 export하지 않음
6. 상수는 `export const`로 선언

**필요한 import:**
- API 타입: `@/api/types/armory` (Equipment, ArmoryProfile 등)
- 기존 유틸: `@/utils/tooltipText`, `@/utils/typeGuards`

**추가 지시사항:**
- 장비 등급 색상 매핑을 위한 `EQUIPMENT_GRADE_COLORS` 상수 추가
- 초월 단계별 정규식 패턴 추가
- 엘릭서 효과 파싱 로직 포함
```

## 예시 2: arkGridDataTransform.ts 생성

```
다음 요구사항에 따라 utils 파일을 생성해주세요:

**파일 경로:** `frontend/src/utils/character/arkGridDataTransform.ts`

**원본 컴포넌트:** `frontend/src/components/CharacterSearch.vue`

**추출할 함수 목록:**
- `parsePassiveTooltip()` - 패시브 툴팁 파싱
- `parsePassiveDescription()` - 패시브 설명 파싱
- `stripPassiveStageKeywords()` - 패시브 단계 키워드 제거
- `romanToNumber()` - 로마 숫자 변환 (I, II, III → 1, 2, 3)
- `extractTierGroupLabel()` - 티어 그룹 라벨 추출
- `buildPassiveCard()` - 패시브 카드 빌드
- `buildArkPassiveMatrix()` - 아크 패시브 매트릭스 빌드
- `buildCoreMatrix()` - 코어 매트릭스 빌드
- `parseCoreMeta()` - 코어 메타 데이터 파싱

**참고할 예시:** `frontend/src/utils/character/skillDataTransform.ts`

**요구사항:**
1. `skillDataTransform.ts`와 동일한 구조 사용
2. JSDoc 주석 포함
3. TypeScript 타입 안전성 보장
4. 모든 함수에 `@example` 추가
5. 내부 헬퍼 함수는 export하지 않음
6. 상수는 `export const`로 선언

**필요한 import:**
- API 타입: `@/api/types/arkgrid` (ArkGridResponse 등)
- 기존 유틸: `@/utils/tooltipText`, `@/utils/typeGuards`

**추가 지시사항:**
- `ROMAN_NUMERAL_MAP` 상수 추가 (I-X 매핑)
- `PASSIVE_SECTIONS` 상수 추가 (깨달음, 도약, 진화 등)
- 아크 그리드 좌표 타입 정의 (행/열)
- 코어 등급별 색상 매핑 추가
```

## 예시 3: cardDataTransform.ts 생성

```
다음 요구사항에 따라 utils 파일을 생성해주세요:

**파일 경로:** `frontend/src/utils/character/cardDataTransform.ts`

**원본 컴포넌트:** `frontend/src/components/CharacterSearch.vue`

**추출할 함수 목록:**
- `parseCardSetEffect()` - 카드 세트 효과 파싱
- `extractAwakeningLevel()` - 각성 레벨 추출
- `calculateCardSetBonus()` - 카드 세트 보너스 계산
- `extractCardGrade()` - 카드 등급 추출
- `formatCardEffect()` - 카드 효과 포맷팅

**참고할 예시:** `frontend/src/utils/character/skillDataTransform.ts`

**요구사항:**
1. `skillDataTransform.ts`와 동일한 구조 사용
2. JSDoc 주석 포함
3. TypeScript 타입 안전성 보장
4. 모든 함수에 `@example` 추가
5. 내부 헬퍼 함수는 export하지 않음
6. 상수는 `export const`로 선언

**필요한 import:**
- API 타입: `@/api/types/cards` (CardResponse 등)
- 기존 유틸: `@/utils/tooltipText`, `@/utils/typeGuards`

**추가 지시사항:**
- 카드 등급 색상 매핑 상수 추가
- 세트 효과 활성화 조건 (2장, 4장, 6장 등) 처리
- 각성 단계별 효과 증가율 계산
```

## 빠른 생성 워크플로우

### 1단계: 함수 목록 추출

```bash
# CharacterSearch.vue에서 특정 도메인 함수 찾기
grep -n "^const.*Equipment.*=" frontend/src/components/CharacterSearch.vue
grep -n "^const.*Passive.*=" frontend/src/components/CharacterSearch.vue
grep -n "^const.*Card.*=" frontend/src/components/CharacterSearch.vue
```

### 2단계: 프롬프트 생성

위 템플릿을 복사하고 함수 목록 채우기

### 3단계: Claude에게 요청

프롬프트를 Claude에게 제공

### 4단계: 파일 생성 및 검증

```bash
# 타입 체크
cd frontend
npm run type-check

# 실행 테스트
npm run dev
```

## 일괄 생성 프롬프트

여러 파일을 한번에 생성하려면:

```
다음 3개의 utils 파일을 `skillDataTransform.ts` 패턴을 따라 생성해주세요:

1. **equipmentDataTransform.ts**
   - 장비 메타 파싱
   - 초월/엘릭서 추출
   - 품질 계산

2. **arkGridDataTransform.ts**
   - 패시브 툴팁 파싱
   - 로마 숫자 변환
   - 아크 매트릭스 빌드

3. **cardDataTransform.ts**
   - 카드 세트 효과 파싱
   - 각성 레벨 추출
   - 세트 보너스 계산

각 파일은:
- `skillDataTransform.ts`와 동일한 구조
- JSDoc + @example 포함
- TypeScript 타입 안전성 보장
- 적절한 상수 정의

한 파일씩 순서대로 생성해주세요.
```

## 검증 프롬프트

생성된 파일을 검증하려면:

```
방금 생성한 `equipmentDataTransform.ts` 파일을 검토해주세요:

**검토 항목:**
1. [ ] 모든 함수에 JSDoc 있는가?
2. [ ] 모든 함수에 @example 있는가?
3. [ ] 타입 안전성 보장되는가? (any 없음)
4. [ ] export/non-export 올바른가?
5. [ ] import 경로 정확한가?
6. [ ] 상수는 모두 export const인가?
7. [ ] skillDataTransform.ts와 구조 일치하는가?

문제가 있으면 수정 제안해주세요.
```

## 원본 컴포넌트 수정 프롬프트

```
`CharacterSearch.vue`에서 다음 함수들을 제거하고 import로 대체해주세요:

**제거할 함수:**
- `normalizeSkillKey`
- `extractGemLabel`
- `classifyGemEffect`
- `isAwakeningSkill`
- `pickHigherGem`

**Import 추가:**
```typescript
import {
  normalizeSkillKey,
  extractGemLabel,
  classifyGemEffect,
  isAwakeningSkill,
  pickHigherGem,
  type GemEffectPlacement,
  type SkillGemSlot
} from '@/utils/character/skillDataTransform'
```

**주의사항:**
- 함수 사용 코드는 그대로 유지
- 타입도 함께 import
- 제거 후 타입 체크 실행
```

## 팁

### 효율적인 작업 순서

1. **하나씩 생성** - 한번에 하나씩 파일 생성 및 테스트
2. **즉시 검증** - 생성 직후 `npm run type-check` 실행
3. **원본 수정** - utils 파일 생성 완료 후 원본 컴포넌트 수정
4. **커밋** - 각 파일 생성마다 git commit

### 문제 발생 시

**타입 오류:**
```
"[파일명]에서 타입 오류가 발생했습니다:
[오류 메시지]

원인을 분석하고 수정해주세요."
```

**함수 동작 변경:**
```
"[함수명]의 동작이 원본과 다릅니다.
원본 로직을 다시 확인하고 정확히 복제해주세요."
```

**Import 오류:**
```
"import 경로가 잘못되었습니다.
프로젝트 구조를 확인하고 올바른 경로로 수정해주세요."
```

## 참고

- [리팩토링 가이드](./refactoring-guide-utils-extraction.md)
- [계획 문서](../C:/Users/blues/.claude/plans/functional-honking-perlis.md)
- [예시 파일](../frontend/src/utils/character/skillDataTransform.ts)
