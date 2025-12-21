# 백엔드 변경 이력

> **대상**: API, 서비스, 스케줄러, 컨트롤러
> **관련 패키지**: `com.lostark.backend.*`

---

## 2025-12

### 2025-12-21: Phase 5 백엔드 성능 최적화

**변경 유형**: Performance / Fix

**변경 내용**:
1. **병렬 처리 도입 (`CharacterService.java`)**
   - CompletableFuture 기반 API 병렬 호출 (캐릭터 프로필 + 수집품)
   - ExecutorService 스레드 풀 (10개)
   - DB 저장 비동기 처리

2. **스냅샷 중복 키 예외 처리**
   - `DataIntegrityViolationException` 발생 시 retry 로직 추가
   - `saveOrUpdateSnapshot()` 헬퍼 메서드 도입

3. **이미지 프록시 로그 정리 (`ImageProxyController.java`)**
   - 프록시 과정 로그 주석 처리
   - API 호출 로그만 유지 (`log.debug("Proxy API call: GET {}", url)`)

**성과**:
- API 호출 시간 40-50% 단축
- 스냅샷 저장 안정화

**영향받은 파일**:
- `backend/src/main/java/.../service/CharacterService.java`
- `backend/src/main/java/.../controller/ImageProxyController.java`

**관련 문서**: `docs/phase5-performance-optimization-plan.md`
