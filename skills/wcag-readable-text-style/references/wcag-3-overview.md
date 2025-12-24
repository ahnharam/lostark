# WCAG 3.0 전체 구조 요약 (웹 디자인 관점)

> **출처**: https://www.w3.org/TR/wcag-3.0/  
> **상태**: Draft (초안)

## 1. 문서 구조 개요
- **Guidelines (2.x)**: 실제 요구사항이 모여 있는 핵심 섹션
- **Conformance (3.x)**: 준수 범위/모델 정의
- **Glossary & References**: 용어와 근거 문서

## 2. Guidelines 섹션 맵 (2.x)

| 구분 | 제목 | 하위 범주 |
|---|---|---|
| 2.1 | Image and media alternatives | Image alternatives, Media alternatives, Non-text alternatives, Captions, Audio descriptions, Figure captions, Single sense |
| 2.2 | Text and wording | Text appearance, Text-to-speech, Clear language |
| 2.3 | Interactive components | Keyboard focus appearance, Pointer focus appearance, Navigating content, Expected behavior, Control information |
| 2.4 | Input / operation | Keyboard interface input, Physical/cognitive effort when using keyboard, Pointer input, Speech/voice input, Input operation, Authentication |
| 2.5 | Error handling | Correct errors, Prevent errors |
| 2.6 | Animation and movement | Avoid physical harm |
| 2.7 | Layout | Relationships, Recognizable layouts, Orientation, Structure, No obstruction |
| 2.8 | Consistency across views | Consistency |
| 2.9 | Process and task completion | Avoid exclusionary cognitive tasks, Adequate time, Unnecessary steps, Avoid deception, Retain information, Complete tasks |
| 2.10 | Policy and protection | Content source, Information privacy, Agreement and risk, Algorithms |
| 2.11 | Help and feedback | Help available, Feedback |
| 2.12 | User control | Control text, Adjustable viewport, Transform content, Media control, Control interruptions, Control possible harm, User agent support |

## 3. 웹 디자인 적용 포인트 (요약)

### 3.1 텍스트/콘텐츠 가독성
- Text appearance의 **문단 간격, 줄 길이, 정렬, 라인 높이** 기준을 기본 토큰으로 정리
- Clear language의 **불필요한 단어 제거, 중첩 문장 회피, 요약 제공**을 콘텐츠 작성 가이드에 반영

### 3.2 포커스/상호작용
- Keyboard/Pointer focus appearance 기준에 맞는 **명확한 포커스 링과 대비** 확보
- Expected behavior: **예상 가능한 상호작용**과 피드백 제공

### 3.3 입력/조작
- Keyboard interface input: **모든 기능 키보드 사용 가능**
- Pointer input: **취소 가능, 오작동 방지**, hover/keyboard focus 콘텐츠 제어
- Authentication: **인지 부하 낮춘 인증 흐름**

### 3.4 오류 처리
- 오류 원인, 위치, 해결 제안을 **즉시 식별 가능**하게 제공
- 제출 전/후 검증 흐름 설계 (validate-as-you-go, confirmation 등)

### 3.5 움직임/애니메이션
- Flash/과도한 모션 회피
- 모션 경고/차단 옵션 제공

### 3.6 레이아웃/구조
- 의미 기반 구조(heading, lists, regions)로 **정보 관계 명확화**
- 오버레이/고정 요소가 콘텐츠를 **가리지 않도록** 설계
- 다양한 방향(orientation)에서도 구조 유지

### 3.7 일관성/작업 완료 지원
- 화면 간 UI/행동 패턴의 일관성 유지
- 불필요한 단계 제거, 시간 제한 명확화
- 사용자의 진행 정보 유지 (retain information)

### 3.8 사용자 보호/지원
- 개인정보/민감정보 처리 고지
- 도움말/피드백 채널 제공
- 사용자 제어(텍스트, 뷰포트, 미디어, 방해 요소) 옵션 제공

## 4. 디자인 산출물 매핑 예시
- **Typography tokens**: line-height, line-length, paragraph spacing
- **Focus tokens**: focus ring color/size/offset
- **Motion tokens**: reduced motion, flashing 금지
- **Layout tokens**: spacing scale, content max-width, overlay positioning

## 5. 사용 방법
- 신규 UI/페이지 제작 시: 섹션 3 체크리스트를 기준으로 디자인 리뷰
- 텍스트 스타일 가이드 작성 시: `wcag-readable-blocks-of-text.md`와 함께 적용
