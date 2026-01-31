# 무료로 사이트 배포하기

exe 대신 **웹 사이트(URL)** 로 배포해서, 누구나 브라우저로 접속해 쓰게 하는 방법입니다.  
**무료** 로 쓸 수 있는 옵션 위주로 정리했습니다.

---

## 요약 비교

| 서비스 | 무료 범위 | 특징 | 적합한 용도 |
|--------|-----------|------|-------------|
| **Render** | Web Service 무료 티어 | GitHub 연동, 15분 비활성 시 슬립(재접속 시 느림), **파일 저장소 휘발** | 데모·테스트, 가벼운 공유 |
| **Railway** | 신규 $5 크레딧, 이후 월 $1 크레딧 | GitHub 연동, Spring Boot 지원 | 소규모·단기 데모 |
| **Fly.io** | 소규모 VM 무료 | JAR/Docker 배포, 항상 켜져 있음 | 개인·소규모 서비스 |
| **Oracle Cloud** | Always Free VM | VM 직접 관리, Java 설치 후 JAR 실행 | 학습·장기 무료 호스팅 |
| **Google Cloud Run** | 월 무료 요청량 내 | 컨테이너 기반, 사용한 만큼만 과금 | 트래픽 적을 때 |

※ Heroku는 **2022년 11월부터 무료 티어 없음** (유료만 가능).

---

## 1. Render (추천: 설정 간단, 진짜 무료)

- **사이트**: [render.com](https://render.com)
- **특징**: GitHub 연결만 하면 빌드·배포 자동. Web Service **무료** 티어 있음.
- **주의**:
  - **15분 동안 접속 없으면 슬립** → 다음 접속 시 1분 안쪽으로 깨어남.
  - **디스크가 휘발성**이라, 지금처럼 H2 파일로 저장하면 **재시작/슬립 시 데이터가 사라짐**.  
    → “데모/공유용” 이라 데이터 유지가 중요하지 않으면 그대로 사용 가능.  
    → 데이터를 유지하려면 Render **PostgreSQL 무료** DB를 붙이고, 앱을 Postgres 사용하도록 바꿔야 함.

### 배포 흐름 (요약)

**이 저장소에는 Dockerfile, render.yaml 이 이미 들어 있습니다.**

**방법 A: Blueprint로 한 번에 배포 (권장)**  
1. [Render Dashboard](https://dashboard.render.com) 로그인 후 **New +** → **Blueprint**.  
2. **Connect a repository** 에서 GitHub 연결 후 **EterSide/smallworld** 저장소 선택.  
3. Render가 `render.yaml` 을 읽어 **smallworld** Web Service 를 자동 생성.  
4. **Apply** 클릭 후 배포가 끝나면 부여된 URL(예: `https://smallworld-xxxx.onrender.com`) 로 접속.

**방법 B: Web Service 수동 생성**  
1. [Render Dashboard](https://dashboard.render.com) 로그인 후 **New +** → **Web Service**.  
2. **Connect repository** 에서 **EterSide/smallworld** 연결.  
3. **Name**: `smallworld`, **Runtime**: **Docker**, **Instance Type**: **Free**.  
4. **Create Web Service** 클릭 → Dockerfile 기준으로 빌드·배포됨.  
5. 배포 완료 후 부여된 **사이트 URL** 로 접속.

**적용된 설정**: `Dockerfile`(Java 17 빌드·실행), `render.yaml`(Blueprint), `application.properties`의 `server.port=${PORT:8080}`, `server.address=0.0.0.0`.

→ **사이트를 “URL 하나”로 공유**할 수 있음. 사용자는 exe 없이 브라우저만 열면 됨.

---

## 2. Railway

- **사이트**: [railway.app](https://railway.app)
- **특징**: GitHub 연동, Spring Boot 공식 가이드 있음. 설정이 비교적 단순.
- **무료**: 신규 가입 시 **$5 크레딧**, 소진 후에는 **월 $1 크레딧** (제한적). 그 이상 쓰면 유료.

### 배포 흐름 (요약)

1. [Railway](https://railway.app) 가입 후 **New Project → Deploy from GitHub**.
2. 저장소 선택 후, **Root Directory**·**Build Command**·**Start Command** 만 맞춤.
   - Build: `./gradlew bootJar`
   - Start: `java -Dserver.port=$PORT -jar build/libs/smallworld-0.0.1-SNAPSHOT.jar`
3. 배포 후 Railway가 부여한 URL로 접속.

---

## 3. Fly.io

- **사이트**: [fly.io](https://fly.io)
- **특징**: 소규모 VM을 **무료** 로 쓸 수 있음. JAR를 Docker로 감싸서 배포.
- **장점**: 슬립 없이 계속 떠 있을 수 있어서, “항상 열려 있는 사이트”에 가깝게 쓸 수 있음.

### 배포 흐름 (요약)

1. [Fly.io](https://fly.io) 가입, CLI 설치 (`flyctl`).
2. 프로젝트 루트에 **Dockerfile** 추가 (Java 17 + `build/libs/*.jar` 복사 후 `java -jar` 실행).
3. `fly launch` → 앱 이름·리전 선택 후 `fly deploy`.
4. `https://xxxx.fly.dev` 형태 URL 부여.

---

## 4. Oracle Cloud (Always Free)

- **사이트**: [cloud.oracle.com](https://cloud.oracle.com)
- **특징**: “Always Free” VM이 있어서 **장기적으로 무료** 로 쓸 수 있음. 대신 VM 직접 관리(Java 설치, 방화벽, systemd 등).
- **적합**: 학습·포트폴리오·트래픽 적은 개인 사이트.

### 배포 흐름 (요약)

1. Oracle Cloud 가입 후 **Always Free VM** (Ubuntu 등) 생성.
2. SSH 접속해서 Java 17 설치, 프로젝트에서 만든 JAR 업로드.
3. `java -jar smallworld-0.0.1-SNAPSHOT.jar` 로 실행 (백그라운드 실행 또는 systemd 서비스로 등록).
4. VM 방화벽에서 8080(또는 사용 포트) 열고, 공인 IP 또는 도메인으로 접속.

---

## 이 프로젝트에 맞게 정리

- **지금 구조**: Spring Boot + H2(파일 DB).  
  - **Render 무료** 에 올리면: **URL로 사이트 공유**는 바로 가능. 다만 슬립·재시작 시 H2 데이터는 날아갈 수 있음(데모용이면 괜찮음).
- **데이터를 유지하고 싶으면**:  
  - Render + **Render PostgreSQL(무료)** 로 DB를 붙이고,  
  - 앱 설정을 H2 대신 Postgres로 바꾸면 됨(추가 작업 필요).
- **“무료 + 설정 쉬운 사이트 배포”** 목적이면 **Render** 부터 시도하는 걸 추천.

원하시면 **Render 기준으로** `build`/`start` 명령어와 `application.properties`(포트 등)를 이 프로젝트에 맞게 구체적으로 적어 드리겠습니다.
