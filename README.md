# 스몰월드 (Smallworld)

Yu-Gi-Oh! 카드 데이터를 활용한 유틸리티 웹 애플리케이션입니다. YGOProDeck API를 연동하여 카드 정보를 조회하고, 특정 게임 규칙(예: 누메론)에 맞는 조합 계산 기능을 제공합니다.

## 주요 기능

*   **카드 검색 및 조회**: YGOProDeck API를 통해 최신 유희왕 카드 데이터를 실시간으로 검색하고 DB에 캐싱합니다.
*   **연율표고정식 계산기 (Numeron Calculator)**:
    *   엑스트라 덱(융합/엑시즈) 조합을 분석하여 제외/되돌리기 시퀀스를 계산합니다.
    *   제외 조합: 랭크가 같은 엑시즈 2장 + 융합 1장의 별(레벨/랭크) 합계 계산
    *   되돌리기 조합: 제외된 엑시즈/융합 몬스터를 활용한 레벨 매칭 계산
*   **배포 친화적 설정**: 로컬(H2) 및 클라우드(PostgreSQL, Render) 환경을 모두 지원합니다.

## 기술 스택

*   **Backend**: Java 17, Spring Boot 3.x
*   **Frontend**: Thymeleaf, Vanilla JS/CSS
*   **Database**: H2 (Embedded File DB)
*   **External API**: [YGOProDeck API](https://db.ygoprodeck.com/api/v7/cardinfo.php)

## 실행 방법

### 로컬 개발 (Local Development)
별도의 DB 설치 없이 바로 실행 가능합니다.
```bash
./gradlew bootRun
```
실행 후 브라우저가 자동으로 열립니다 (`http://localhost:8080`).

### 배포 (Deployment)
Render 등의 PaaS 환경에 배포 시 다음 환경 변수를 설정하세요.
*   `SMALLWORLD_OPEN_BROWSER`: `false` (서버 환경에서는 브라우저 실행 비활성화)

## 라이선스
이 프로젝트는 개인 학습 및 포트폴리오 목적으로 제작되었습니다. 카드 데이터 및 이미지의 저작권은 Konami에 있습니다.
