## docs 디렉토리 구조

- 목적: 문서 위치와 용도를 일관되게 관리하기 위한 인덱스.
- 규칙: 새 문서 추가 시 이 목록을 업데이트하고, `docs/documentation-guidelines.md`를 준수.
- 언어: 기본 한국어, 파일명은 `kebab-case`.

### 루트 문서
- `docs/mcp-usage.md`: Postman MCP 서버 기반 API 호출/테스트 가이드.
- `docs/lostark-armory.md`: 로스트아크 전투정보/거래/컨텐츠 관련 API 포맷 정리.
- `docs/documentation-guidelines.md`: 문서 작성/정리 공통 규칙(이 문서를 우선 참조).

### 서브 디렉토리
- `docs/deployment/`: 배포 관련 가이드  
  - `README.md`, `railway.md`, `vercel.md`, `freedb.md`
- `docs/lostark-armory-responses/`: Lost Ark API 원본 응답 JSON 샘플(수동 편집 금지).

### 프런트엔드 문서
- 경로: `frontend/docs/`
- 파일:
  - `ARK_GRID_GUIDE.md`
  - `CHARACTER_RANKING_GUIDE.md`
  - `UX_OVERVIEW.md`
- 규칙: `docs/documentation-guidelines.md`를 따르되, UI/UX 캡처는 필요 시 포함 가능. 프런트 전용 내용은 `frontend/docs/`에 유지.

### 백엔드 문서
- 경로: `backend/`
- 파일:
  - `HELP.md`
- 규칙: 백엔드 전용 설정/운영/개발 문서는 `backend/` 이하에 배치하고, 공용 가이드와 충돌 없게 파일명 `kebab-case` 유지. 공통 규칙은 `docs/documentation-guidelines.md` 참고.
