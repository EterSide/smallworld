# Render PostgreSQL 연결 + 첫 카드 로딩

PostgreSQL로 바꾼 뒤, **첫 로딩은 한 번만** 하면 됩니다.  
이후에는 슬립에서 깨어나도 DB에 데이터가 남아 있어 검색이 빠릅니다.

---

## 1. Render에서 PostgreSQL 추가

1. [Render Dashboard](https://dashboard.render.com) → **New +** → **PostgreSQL**.
2. **Name**: 예) `smallworld-db`
3. **Region**: smallworld Web Service와 같은 리전(예: Oregon) 선택.
4. **Plan**: **Free** 선택 후 **Create Database**.
5. 생성된 DB의 **Internal Database URL** 복사 (예: `postgres://user:pass@host:port/dbname`).

---

## 2. Web Service에 DB 연결

1. **smallworld** Web Service → **Environment**.
2. **Environment Variables**에서 **Add from Render** 또는 **Add Environment Variable**.
3. **Key**: `DATABASE_URL`  
   **Value**: 위에서 복사한 Internal Database URL (Render가 DB 추가 시 자동으로 넣어 주는 경우도 있음).
4. **PostgreSQL dialect** 설정:  
   **Key**: `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`  
   **Value**: `org.hibernate.dialect.PostgreSQLDialect`  
   (Render에서 Postgres 추가 시 자동으로 넣어 주는 경우는 생략 가능.)

5. 저장 후 **Manual Deploy** 한 번 실행 (설정 반영).

---

## 3. 동작 방식

- **DATABASE_URL**이 있으면 앱이 **PostgreSQL**을 사용합니다.
- **DATABASE_URL**이 없으면(로컬 등) **H2**를 그대로 사용합니다.
- PostgreSQL 사용 시 데이터는 **Render Postgres**에 유지되므로, 슬립 후에도 사라지지 않습니다.

---

## 4. 첫 로딩 (전체 카드 DB에 넣기)

배포가 끝난 뒤 **한 번만** 아래 URL을 브라우저에서 열거나 호출하면 됩니다.

```
https://smallworld-xxxx.onrender.com/admin/sync-cards
```

(실제 서비스 URL로 바꾸세요.)

- **동작**: YGOProDeck API에서 전체 카드를 가져와 DB에 저장합니다. 수 분 걸릴 수 있습니다.
- **응답**: "전체 카드 동기화를 시작했습니다..." 메시지가 나오면, 백그라운드에서 동기화가 진행 중입니다.
- **확인**: Render **Logs**에서 `Saved cards ...` / `Sync complete: N cards saved` 로 진행·완료 여부를 확인할 수 있습니다.

---

## 5. 이후 사용

- **첫 로딩 한 번** 해 두면, 이후 검색은 **DB만** 사용해서 빠릅니다.
- 슬립에서 깨어날 때는 **기동 시간(약 50초)** 만 기다리면 되고, 카드 데이터는 이미 DB에 있으므로 다시 동기화할 필요 없습니다.

---

## 요약

| 단계 | 할 일 |
|------|--------|
| 1 | Render에서 PostgreSQL 생성 후 **Internal Database URL** 복사 |
| 2 | smallworld Web Service **Environment**에 `DATABASE_URL` 추가 후 재배포 |
| 3 | 배포 완료 후 **한 번만** `https://...onrender.com/admin/sync-cards` 접속 |
| 4 | 로그에서 동기화 완료 확인 후, 평소처럼 사이트 사용 |
