## MCP 사용 가이드 (Postman 서버로 API 호출/테스트)

- 기본 전제: 이 프로젝트에서 API 호출/테스트는 **Postman MCP 서버**를 통해 수행한다.
- 실행 명령: 리포 루트에서 `npx @postman/postman-mcp-server@2.4.9`
  - `.env`의 `POSTMAN_API_KEY`를 읽어 사용. 키 노출 금지.
  - 서버 실행 후 터미널을 열어둔 채로 테스트 진행, 중단은 `Ctrl+C`.
- 호출 헤더 예시: `Authorization: Bearer $LOSTARK_API_KEY`, `Accept: application/json`
- 샘플 응답: `docs/lostark-armory-responses/`에 저장된 JSON 참고.

### 새 채팅/세션 시작 시 붙여넣을 선언 예시
```
[CONTEXT] 이 세션의 API 호출/테스트는 Postman MCP 서버(npx @postman/postman-mcp-server@2.4.9)로 수행한다. .env의 POSTMAN_API_KEY를 사용.
```
