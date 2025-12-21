# docs 디렉토리 구조

> **최종 수정**: 2025-12-21
> **관련 문서**: [documentation-guidelines.md](./documentation-guidelines.md)

## 개요

- **목적**: 문서 위치와 용도를 일관되게 관리하기 위한 색인
- **규칙**: 새 문서 추가 시 반드시 이 목록 업데이트 + [documentation-guidelines.md](./documentation-guidelines.md) 준수
- **언어**: 한국어, 파일명은 `kebab-case`

---

## 폴더 구조

```
docs/
├── README.md                    # 이 문서 (색인)
├── dev-quickstart.md            # 개발 퀵스타트 (중앙 허브)
├── documentation-guidelines.md  # 문서 작성 규칙
│
├── guides/                      # 📁 가이드 문서
│   └── mcp-usage.md
│
├── references/                  # 📁 참조 문서
│   ├── lostark-armory.md
│   ├── refactoring-*.md
│   └── lostark-armory-responses/
│
├── changelog/                   # 📁 변경 이력 (메뉴별)
│   ├── character.md             # 메뉴별 변경 이력
│   ├── auction.md
│   └── ...
│
└── deployment/                  # 📁 배포 가이드
    ├── vercel.md
    └── ...
```

---

## 문서 목록

### 필수 문서 (항상 최신 유지)

| 파일 | 설명 | 역할 |
|-----|------|------|
| `dev-quickstart.md` | 개발 퀵스타트 가이드 | **중앙 허브** - 프로젝트 전체 파악 |
| `documentation-guidelines.md` | 문서 작성 규칙 | 명명 규칙, 카테고리, AI 가이드 |
| `README.md` (이 문서) | docs 폴더 색인 | 문서 목록 및 위치 안내 |

### 가이드 문서 (guides/)

| 파일 | 설명 |
|-----|------|
| `guides/mcp-usage.md` | Postman MCP 서버 기반 API 호출/테스트 가이드 |

### 참조 문서 (references/)

| 파일 | 설명 |
|-----|------|
| `references/lostark-armory.md` | 로스트아크 전투정보/거래/컨텐츠 API 포맷 정리 |
| `references/refactoring-code-snippets.md` | 재사용 가능한 코드 스니펫 모음 |
| `references/refactoring-guide-utils-extraction.md` | 유틸리티 추출 리팩토링 가이드 |
| `references/refactoring-automation-prompt.md` | 리팩토링 자동화 프롬프트 |
| `references/lostark-armory-responses/` | Lost Ark API 원본 응답 JSON 샘플 |

### 변경 이력 (changelog/)

메뉴별 변경 이력과 Phase 상세 문서를 관리합니다.

#### 메뉴별 변경 이력

| 파일 | 대상 |
|-----|------|
| `changelog/README.md` | changelog 작성 규칙 |
| `changelog/character.md` | 캐릭터 검색, 프로필, 원정대, 통합 메뉴 |
| `changelog/auction.md` | 경매장, 거래소 |
| `changelog/reforge.md` | 재련 계산기 |
| `changelog/raid.md` | 레이드 파티 |
| `changelog/admin.md` | 관리자 메뉴 |
| `changelog/common.md` | 라우팅, 레이아웃, 공통 컴포넌트 |
| `changelog/backend.md` | API, 서비스, 스케줄러 |

### 배포 문서 (deployment/)

| 파일 | 설명 |
|-----|------|
| `deployment/README.md` | 배포 문서 색인 |
| `deployment/vercel.md` | Vercel 프론트엔드 배포 가이드 |
| `deployment/railway.md` | Railway 백엔드 배포 가이드 |
| `deployment/oracle-vm.md` | Oracle VM 배포 가이드 |
| `deployment/freedb.md` | FreedDB MySQL 설정 가이드 |

---

## 프론트엔드 문서 (frontend/docs/)

프론트엔드 전용 UI/UX, 컴포넌트 관련 문서입니다.

| 파일 | 설명 |
|-----|------|
| `frontend/docs/UX_OVERVIEW.md` | UX 방향 및 컨셉 |
| `frontend/docs/ARK_GRID_GUIDE.md` | 아크 그리드 컴포넌트 가이드 |
| `frontend/docs/CHARACTER_RANKING_GUIDE.md` | 캐릭터 랭킹 화면 가이드 |

---

## AI 세션/에이전트를 위한 안내

### 새 세션 시작 시 필독 순서
1. **`docs/dev-quickstart.md`** - 프로젝트 전체 구조 파악
2. **`docs/documentation-guidelines.md`** - 문서 규칙
3. **`AGENTS.md`** - 코드 규칙

### 문서 추가/수정 시 체크리스트
- [ ] 이 파일(`docs/README.md`) 색인에 추가
- [ ] `docs/dev-quickstart.md` 섹션 13에 링크 추가
- [ ] 해당 폴더의 README.md 업데이트
- [ ] 명명 규칙 준수 확인

자세한 규칙은 [documentation-guidelines.md](./documentation-guidelines.md)를 참고하세요.
