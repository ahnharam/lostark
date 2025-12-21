## 로스트아크 전투정보 API 포맷 가이드 (characterName=아기하람 기준)

목적: 조회된 값을 요약하기보다, 반환 JSON의 형식/필드 구조를 개발 시 참고할 수 있도록 정리. 실제 응답 샘플은 `docs/lostark-armory-responses/*.json`에 있음.

### 공통 요청/응답
- Base URL: `https://developer-lostark.game.onstove.com/armories/characters`
- Path 변수: `{characterName}` (URL 인코딩 필요, 예: `아기하람` → `%EC%95%84%EA%B8%B0%ED%95%98%EB%9E%8C`)
- HTTP 메서드: `GET`
- 헤더: `Authorization: Bearer <LOSTARK_API_KEY>`, `Accept: application/json`
- 응답 공통 특징:
  - 수치가 문자열로 올 수 있음(천 단위 콤마 포함).
  - `Tooltip` 필드는 HTML/리치 텍스트(폰트·색상 태그 등) 또는 JSON 문자열이 들어옴.
  - 일부 서브필드는 null/빈 배열일 수 있음(예: `Rune`, `Engravings`, `Effects`).

### 엔드포인트별 응답 구조
- `/armories/characters/{characterName}`
  - 종합 객체. 키: `ArmoryProfile`, `ArmoryEquipment`, `ArmoryAvatars`, `ArmorySkills`, `ArmoryEngraving`, `ArmoryCard`, `ArmoryGem`, `ArkPassive`, `ArkGrid`, `ColosseumInfo`, `Collectibles`.

- `/armories/characters/{characterName}/profiles`
  - 객체 필드 예: `CharacterImage`, `ExpeditionLevel`, `PvpGradeName`, `TownLevel`, `TownName`, `Title`, `GuildMemberGrade`, `GuildName`, `UsingSkillPoint`, `TotalSkillPoint`, `Stats`(배열 `{Type, Value}`), `Tendencies`(배열 `{Type, Point, MaxPoint}`), `CombatPower`, `Decorations`, `HonorPoint`, `ServerName`, `CharacterName`, `CharacterLevel`, `CharacterClassName`, `ItemAvgLevel`.

- `/equipment`
  - 배열. 각 아이템 필드: `Type`, `Name`, `Icon`, `Grade`, `Tooltip`(HTML/JSON 문자열). 장비 수량은 캐릭터 슬롯에 따라 16개 수준.

- `/avatars`
  - 배열. 필드: `Type`, `Name`, `Icon`, `Grade`, `IsSet`, `IsInner`, `Tooltip`.

- `/combat-skills`
  - 배열. 필드: `Name`, `Icon`, `Level`, `Type`, `SkillType`, `Tripods`(배열 `{Tier, Slot, Name, Icon, IsSelected, Tooltip}`), `Rune`(nullable), `Tooltip`.

- `/engravings`
  - 객체. 키:
    - `Engravings`: 배열(없을 수 있음). 일반적으로 `{Name, Icon, Slot, Tooltip}` 형식.
    - `Effects`: 배열(없을 수 있음). `{Name, Description, Icon?, Tooltip?}` 형식.
    - `ArkPassiveEffects`: 배열 `{AbilityStoneLevel, Grade, Level, Name, Description}` — 아크 패시브 각인 정보가 여기 담길 수 있음.

- `/cards`
  - 객체. 키:
    - `Cards`: 배열 `{Slot, Name, Icon, AwakeCount, AwakeTotal, Grade, Tooltip}`.
    - `Effects`: 배열. 각 항목 `{Index, CardSlots: number[], Items: [{Name, Description}]}`로 세트효과를 표현.

- `/gems`
  - 객체. 키:
    - `Gems`: 배열 `{Slot, Name, Icon, Level, Grade, Tooltip}`.
    - `Effects`: 객체 `{Description, Skills}`. `Skills`는 배열 `{GemSlot, Name, Description: string[], Option, Icon, Tooltip}`로 스킬별 보석 효과를 설명.

- `/colosseums`
  - 객체. 키: `Rank`, `PreRank`, `Exp`, `Colosseums`.
  - `Colosseums`는 시즌별 배열. 각 시즌 객체는 모드별 필드(`TeamDeathmatch`, `TeamElimination`, `CoOpBattle`, `OneDeathmatch`, `Competitive` 등)를 갖고, 모드는 `{PlayCount, VictoryCount, LoseCount, TieCount, KillCount, DeathCount, AssistCount, AceCount, ...}`와 같은 카운터를 포함(모드에 따라 세부 키 다름).

- `/collectibles`
  - 배열. 각 항목 `{Type, Icon, Point, MaxPoint, CollectiblePoints}`. `CollectiblePoints`는 세부 지역/구간별 `{PointName, Point, MaxPoint}` 배열.

- `/arkpassive`
  - 객체. 키:
    - `IsArkPassive`(bool), `Points`(배열 `{Name, Value, Tooltip, Description}`), `Effects`(배열 `{Name, Description, Icon, ToolTip}`; `ToolTip`은 JSON 문자열 형태가 많음).

- `/arkgrid`
  - 객체. 키:
    - `Slots`: 배열 `{Index, Icon, Name, Point, Grade, Tooltip, Gems}`. `Gems`는 아크 그리드 젬 배열 `{Index, Icon, IsActive, Grade, Tooltip}`.
    - `Effects`: 배열 `{Name, Level, Tooltip}` — 아크 그리드 효과(추가 피해, 공격력 등) 레벨별 수치가 HTML로 포함.

### Auctions (경매장)
- `/auctions/options` (GET)
  - 응답 키:
    - `MaxItemLevel`(number)
    - `ItemGradeQualities`(number[])
    - `SkillOptions`: 배열 `{Value, Class, Text, IsSkillGroup, Tripods[]}`; `Tripods` 항목 `{Value, Text, IsGem, Tiers[]}`.
    - `EtcOptions`: 배열 `{Value, Text, Tiers?, EtcSubs[]}`; `EtcSubs` `{Value, Text, Class, Categorys?, Tiers?, EtcValues?}`.
    - `Categories`: 배열 `{Code, CodeName, Subs[]}` (카테고리 트리).
    - `ItemGrades`(string[]), `ItemTiers`(number[]), `Classes`(string[]).
  - 원본 샘플: `docs/lostark-armory-responses/auction-options.json`.
- `/auctions/items` (POST)
  - 요청 필드 예시: `ItemLevelMin`, `ItemLevelMax`, `ItemGrade`, `ItemTier`, `CategoryCode`, `CharacterClass`, `ItemName`, `PageNo`, `PageSize`, `Sort`, `SortCondition`, `IsFirstCategory`, `IsTierFiltered`, `EtcOptions`(array), `SkillOptions`(array).
  - 응답 키:
    - 페이징: `PageNo`, `PageSize`, `TotalCount`.
    - `Items`: 배열. 각 항목 `{Name, Grade, Tier, Level, Icon, GradeQuality, AuctionInfo, Options}`.
      - `AuctionInfo`: `{StartPrice, BuyPrice, BidPrice, EndDate, BidCount, BidStartPrice, IsCompetitive, TradeAllowCount, UpgradeLevel}`.
      - `Options`: `{Type, OptionName, OptionNameTripod, Value, IsPenalty, ClassName, IsValuePercentage}`.
  - 원본 샘플: `docs/lostark-armory-responses/auction-items.json`.

### Markets (거래소)
- `/markets/options` (GET)
  - 응답 키:
    - `Categories`: 배열 `{Code, CodeName, Subs[]}` (카테고리 트리)
    - `ItemGrades`(string[]), `ItemTiers`(number[]), `Classes`(string[])
  - 샘플: `docs/lostark-armory-responses/market-options.json`.
- `/markets/items` (POST)
  - 요청 필드 예시: `Sort`, `CategoryCode`, `ItemTier`, `ItemGrade`, `ItemName`, `PageNo`, `PageSize`, `SortCondition`. 카테고리 코드가 필수이며 유효한 코드(예: 강화 재료 50010 등)를 사용해야 함.
  - 응답 키:
    - 페이징: `PageNo`, `PageSize`, `TotalCount`.
    - `Items`: 배열 `{Id, Name, Grade, Icon, BundleCount, TradeRemainCount, YDayAvgPrice, RecentPrice, CurrentMinPrice}`.
  - 샘플: `docs/lostark-armory-responses/market-items.json`.
- `/markets/items/{itemId}` (GET)
  - 응답: 배열 형태. 항목 예시 `{Name, BundleCount, TradeRemainCount, ToolTip, Stats}`.
    - `Stats`: `{AvgPrice, TradeCount, Date}` 배열(일자별 거래량/평균가).
    - `ToolTip`: 아이템 상세 HTML/JSON 문자열.
  - 샘플: `docs/lostark-armory-responses/market-item-66102001.json`.

### Gamecontents (주간 캘린더)
- `/gamecontents/calendar` (GET)
  - 응답: 배열. 항목 필드 `{CategoryName, ContentsName, ContentsIcon, MinItemLevel, StartTimes[], Location, RewardItems}`.
    - `StartTimes`: ISO datetime 문자열 배열.
    - `RewardItems`: 배열 `{ItemLevel, Items[]}`; `Items` 항목 `{Name, Icon, Grade, StartTimes}`(보상 아이템 별 시작 시간은 대부분 null).
  - 샘플: `docs/lostark-armory-responses/gamecontents-calendar.json`.

### News
- `/news/notices` (GET)
  - 응답: 배열 `{Title, Date, Link, Type}`.
  - 샘플: `docs/lostark-armory-responses/news-notices.json`.
- `/news/events` (GET)
  - 응답: 배열 `{Title, Thumbnail, Link, StartDate, EndDate, RewardDate}` (`RewardDate`는 null일 수 있음).
  - 샘플: `docs/lostark-armory-responses/news-events.json`.
- `/news/alarms` (GET)
  - 응답: 객체 `{RequirePolling: boolean, Alarms: []}`. 현재 샘플에서는 `Alarms`가 비어 있으며 필드 구조는 추후 확인 필요.
  - 샘플: `docs/lostark-armory-responses/news-alarms.json`.

### 재호출/검증용 명령
- MCP 서버 실행: `npx @postman/postman-mcp-server@2.4.9`
- 단일 엔드포인트 호출 예시(프로필):
  ```bash
  bash -lc 'source .env; base=\"https://developer-lostark.game.onstove.com/armories/characters\"; name=$(python3 - <<\"PY\"; import urllib.parse; print(urllib.parse.quote(\"아기하람\")); PY); curl -H \"accept: application/json\" -H \"Authorization: Bearer $LOSTARK_API_KEY\" \"$base/$name/profiles\"'
  ```
