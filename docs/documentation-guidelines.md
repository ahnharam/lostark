## 문서 작성/정리 규칙

이 프로젝트에서 새 문서를 추가하거나 기존 문서를 수정할 때 따르는 공통 규칙입니다.
**모든 AI 세션/에이전트는 문서 작업 전 반드시 이 규칙을 참고하세요.**

---

### 1. 문서 카테고리 및 명명 규칙

#### 1.1 카테고리별 접두사 (Prefix)

| 카테고리 | 접두사 | 용도 | 예시 |
|---------|--------|------|------|
| 가이드 | `guide-` | 기능/기술 사용법, 개발 가이드 | `guide-routing.md`, `guide-ux.md` |
| 단계별 계획 | `phase{N}-` | 프로젝트 단계별 계획/구현 문서 | `phase5-performance.md` |
| 리포트 | `report-` | 완료된 작업 결과 보고서 | `report-phase2-3.md` |
| 참조 | `ref-` | API 참조, 데이터 구조, 스니펫 | `ref-armory-api.md`, `ref-snippets.md` |
| 변경 이력 | (폴더로 구분) | 메인메뉴별 변경 이력 | `changelog/character.md` |
| 배포 | (폴더로 구분) | 배포 환경별 가이드 | `deployment/vercel.md` |

#### 1.2 파일명 규칙

```
[접두사]-[주제]-[세부사항].md

예시:
✅ guide-ark-grid.md          # 아크 그리드 사용 가이드
✅ phase5-performance.md      # Phase 5 성능 최적화 계획
✅ report-phase2-3.md         # Phase 2-3 완료 보고서
✅ ref-armory-api.md          # 아모리 API 참조
❌ phase2-3-completion-report.md  # 너무 김
❌ ARK_GRID_GUIDE.md              # UPPER_CASE 사용 금지
```

- **케이스**: `kebab-case` 사용 (소문자, 하이픈 구분)
- **확장자**: `.md` (문서), `.json` (API 샘플)
- **길이**: 가능하면 3단어 이내로 간결하게

---

### 2. 디렉토리 구조

```
lostark/
├── README.md                    # 프로젝트 개요 (필수)
├── AGENTS.md                    # AI 에이전트 규칙 (필수)
│
├── docs/                        # 📁 메인 문서 폴더
│   ├── README.md                # docs 폴더 색인 (필수)
│   ├── dev-quickstart.md        # 개발 퀵스타트 (필수, 중앙 허브)
│   ├── documentation-guidelines.md  # 이 문서 (필수)
│   │
│   ├── guides/                  # 📁 가이드 문서
│   │   └── mcp-usage.md
│   │
│   ├── references/              # 📁 참조 문서
│   │   ├── lostark-armory.md
│   │   ├── refactoring-*.md
│   │   └── lostark-armory-responses/
│   │
│   ├── changelog/               # 📁 변경 이력 (메뉴별)
│   │   ├── character.md         # 메뉴별 변경 이력
│   │   ├── auction.md
│   │   └── ...
│   │
│   └── deployment/              # 📁 배포 문서
│       ├── vercel.md
│       └── ...
│
└── frontend/
    └── docs/                    # 📁 프론트엔드 전용 문서
        ├── guide-*.md
        └── ref-*.md
```

#### 2.1 폴더별 역할

| 위치 | 역할 | 작성 대상 |
|------|------|----------|
| `docs/` | 필수 문서만 | README, dev-quickstart, documentation-guidelines |
| `docs/guides/` | 가이드 문서 | How-to, 사용법 |
| `docs/references/` | 참조 문서 | API 참조, 코드 스니펫, 데이터 구조 |
| `docs/changelog/` | 변경 이력 | 메뉴별 변경사항 |
| `docs/deployment/` | 배포 가이드 | Vercel, Railway, Oracle VM 등 |
| `frontend/docs/` | 프론트엔드 전용 | UI/UX, 컴포넌트 가이드 |

---

### 3. 필수 문서 및 역할

#### 3.1 중앙 허브 문서 (항상 최신 유지)

| 문서 | 역할 | 업데이트 시점 |
|------|------|-------------|
| `docs/dev-quickstart.md` | 프로젝트 전체 파악, 모든 문서 링크 | 모든 작업 후 |
| `docs/README.md` | docs 폴더 색인 | 문서 추가/삭제 시 |
| `AGENTS.md` | AI 에이전트 공통 규칙 | 규칙 변경 시 |

#### 3.2 문서 작성 시 체크리스트

새 문서 작성 후 반드시:
1. [ ] `docs/README.md` 색인에 추가
2. [ ] `docs/dev-quickstart.md` 섹션 13 (참고 문서 목록)에 링크 추가
3. [ ] 명명 규칙 준수 확인 (접두사, kebab-case)

---

### 4. 문서 내용 규칙

#### 4.1 헤더 구조
```markdown
# 제목 (최상위 - 문서당 1개만)

## 섹션 (주요 구분)

### 서브섹션

#### 세부 항목 (필요 시)
```

#### 4.2 문서 상단 템플릿
```markdown
# 문서 제목

> **카테고리**: Guide | Phase | Report | Reference
> **최종 수정**: YYYY-MM-DD
> **관련 문서**: [링크](./other-doc.md)

## 개요
- 목적: (1줄 요약)
- 적용 범위: (어떤 상황에서 참고)
- 전제 조건: (필요 시)

---
(본문)
```

#### 4.3 코드/경로 표기
- 파일 경로: `` `frontend/src/components/CharacterSearch.vue` ``
- 명령어: fenced code block (```bash, ```typescript 등)
- API 필드: 원문 그대로 유지 (`characterName`, `itemAvgLevel`)

#### 4.4 언어
- 기본: **한국어**
- 코드/API 용어: 영문 원문 유지
- 파일명: 영문 kebab-case

---

### 5. AI 세션/에이전트 가이드

#### 5.1 새 세션 시작 시 필독 문서
1. `docs/dev-quickstart.md` - 프로젝트 전체 구조 파악
2. `docs/documentation-guidelines.md` - 문서 규칙 (이 문서)
3. `AGENTS.md` - 코드 규칙

#### 5.2 문서 작업 시 원칙
- **문서 검색**: 먼저 `docs/README.md`에서 관련 문서 확인
- **기존 문서 우선**: 새 문서 생성보다 기존 문서 업데이트 선호
- **색인 갱신**: 문서 추가/삭제 시 `docs/README.md` 반드시 업데이트
- **허브 갱신**: 중요 변경 시 `docs/dev-quickstart.md` 업데이트

#### 5.3 어떤 문서에 작성할지 결정 흐름

```
작업 완료
    │
    ▼
┌─────────────────────────────────────┐
│ 새 기능/변경이 기존 문서에 해당?    │
└─────────────────────────────────────┘
    │                    │
   Yes                  No
    │                    │
    ▼                    ▼
기존 문서 업데이트    새 문서 생성
    │                    │
    ▼                    ▼
dev-quickstart.md    1. 명명 규칙 준수
섹션 12에 이력 추가   2. docs/README.md 추가
                     3. dev-quickstart.md 링크 추가
```

---

### 6. 정렬/업데이트 원칙
- 목록/표: 알파벳 또는 논리 순서(경로→기능→세부)
- 샘플 JSON: 수정 금지, 재수집 시 덮어쓰기
- 외부 비밀값: 문서에 절대 기재 금지 (.env에만 보관)

---

### 7. 예시: 기존 문서 명명 개선

| 기존 파일명 | 개선된 파일명 | 사유 |
|------------|--------------|------|
| `phase2-3-completion-report.md` | `report-phase2-3.md` | report 접두사, 간결화 |
| `phase4-routing-guide.md` | `guide-routing.md` 또는 `phase4-routing.md` | 일관성 |
| `refactoring-code-snippets.md` | `ref-snippets.md` | ref 접두사, 간결화 |
| `lostark-armory.md` | `ref-armory-api.md` | ref 접두사 |
| `ARK_GRID_GUIDE.md` | `guide-ark-grid.md` | kebab-case |

**참고**: 기존 문서는 호환성을 위해 점진적으로 개선하며, 새 문서는 반드시 규칙을 준수합니다.
