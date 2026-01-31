# GitHub 업로드 및 배포 가이드

이 문서는 **smallworld** Spring Boot 프로젝트를 GitHub에 올리고, 실제 서비스로 배포하는 절차를 정리한 것입니다.

---

## 지금 바로 GitHub에 올리기 (이미 커밋됨)

로컬에서 **Git 초기화와 첫 커밋**은 이미 되어 있습니다. 아래만 하면 됩니다.

1. **GitHub에서 새 저장소 만들기**
   - [GitHub New repository](https://github.com/new) 접속
   - 저장소 이름 예: `smallworld`
   - **Public** 선택
   - **Add a README / .gitignore / license** 는 **체크하지 말고** **Create repository** 클릭

2. **터미널에서 원격 연결 후 푸시** (아래 주소는 본인 GitHub 사용자명/저장소명으로 바꾸세요)
   ```bash
   git remote add origin https://github.com/<사용자명>/<저장소명>.git
   git push -u origin master
   ```
   예: 저장소가 `https://github.com/myuser/smallworld` 이면
   ```bash
   git remote add origin https://github.com/myuser/smallworld.git
   git push -u origin master
   ```

---

## 1. GitHub에 프로젝트 업로드하기

### 1-1. 사전 준비
- [GitHub](https://github.com) 계정
- PC에 Git 설치 여부 확인 (`git --version`)

### 1-2. Git 초기화 (아직 안 했다면)
- 프로젝트 루트에서 `git init` 실행
- `.gitignore`는 이미 있으므로 빌드 산출물·IDE 설정 등은 제외됨

### 1-3. GitHub 저장소 생성
- GitHub 웹에서 **New repository** 생성
- 저장소 이름 예: `smallworld`
- **Public** / **Private** 선택
- **README, .gitignore, license**는 생성하지 않음 (로컬에 이미 있음)

### 1-4. 로컬 → GitHub 올리기
1. 모든 변경사항 스테이징: `git add .`
2. 첫 커밋: `git commit -m "Initial commit"`
3. 원격 저장소 연결: `git remote add origin https://github.com/<사용자명>/<저장소명>.git`
4. 푸시: `git push -u origin main` (또는 `master`)

### 1-5. 주의사항
- **비밀정보**: `application.properties`에 DB 비밀번호, API 키 등이 있으면 **절대 커밋하지 말 것**
- 대신 `application.properties`는 예시만 남기고, 실제 값은 환경 변수 또는 `application-local.properties`(git 제외) 등으로 관리

---

## 2. 프로그램 배포하기

이 프로젝트는 **웹 애플리케이션 + H2 내장 DB**이므로, 서버 환경에서 JAR만 실행해도 됩니다.  
**무료로 사이트(URL) 배포**하려면 → **[무료 사이트 배포 가이드](FREE_SITE_DEPLOYMENT.md)** 참고 (Render, Railway, Fly.io 등).

### 2-1. 배포 전 준비

| 항목 | 내용 |
|------|------|
| **빌드** | Gradle로 실행 가능 JAR 생성: `./gradlew bootJar` (Windows: `gradlew.bat bootJar`) |
| **산출물** | `build/libs/smallworld-0.0.1-SNAPSHOT.jar` (실제 이름은 build.gradle의 `version`에 따름) |
| **Java** | 서버에 Java 17 설치 필요 |
| **DB** | H2 내장 DB 사용 시 별도 DB 설치 불필요. (PaaS 무료 DB 사용 시 해당 서비스 설정) |

### 2-2. 배포 방식 선택

#### A. 직접 서버에서 실행 (VPS, AWS EC2 등)
1. 서버에 Java 17, MySQL 설치
2. DB 스키마·사용자 생성 후 `application.properties` 또는 환경 변수로 URL/계정 설정
3. JAR 파일 업로드 후 실행:  
   `java -jar smallworld-0.0.1-SNAPSHOT.jar`
4. 백그라운드 실행이 필요하면: `nohup`, `systemd` 서비스, 또는 `screen`/`tmux` 사용

#### B. 클라우드 PaaS (배포가 쉬운 경우)
- **Heroku**: GitHub 연동 후 자동 빌드·배포 가능. MySQL 대신 Heroku Postgres 등 사용 시 설정 변경 필요
- **Railway, Render** 등: GitHub 연결 후 빌드 명령·실행 명령만 지정하면 배포 가능
- DB는 해당 서비스에서 제공하는 MySQL/Postgres를 쓰고, 접속 정보는 환경 변수로 설정

#### C. 컨테이너 배포 (Docker)
1. **Dockerfile** 작성: Java 17 이미지 기반, JAR 복사 후 `java -jar` 실행
2. **docker-compose**: 앱 컨테이너 + MySQL 컨테이너 한 번에 실행
3. AWS ECS, Google Cloud Run, Kubernetes 등에 이미지 올려서 배포

#### D. CI/CD로 자동 배포
- **GitHub Actions**에서 `main` 브랜치 푸시 시:
  - `./gradlew bootJar` 실행
  - 빌드된 JAR를 서버로 전송(SCP/SFTP) 후 재시작
  - 또는 Docker 이미지 빌드 후 레지스트리 푸시·서버에서 pull 후 재시작
- 이렇게 하면 “코드 푸시 → 자동 빌드·배포” 흐름을 만들 수 있음

### 2-3. 배포 시 꼭 할 일
- **환경별 설정**: 로컬/운영용 `application.properties` 또는 `application-{profile}.properties` 분리
- **DB 마이그레이션**: JPA 사용 시 `ddl-auto`는 개발에서만 `update` 등 사용하고, 운영에서는 스키마 스크립트로 관리하는 편이 안전
- **포트·URL**: 서버 방화벽에서 8080(기본) 등 필요한 포트 개방
- **HTTPS**: 외부 공개 시 Nginx 등 리버스 프록시 + SSL 인증서 적용 권장

---

## 3. 요약 체크리스트

### GitHub 업로드
- [ ] Git 설치 및 사용자 설정
- [ ] `git init` (필요 시)
- [ ] GitHub에 빈 저장소 생성
- [ ] `git add` → `commit` → `remote add` → `push`
- [ ] 비밀정보가 설정 파일에 없도록 확인

### 배포
- [ ] `./gradlew bootJar`로 JAR 생성 확인
- [ ] Java 17 + MySQL 서버 준비
- [ ] DB 접속 정보를 환경 변수 또는 설정 파일로 안전하게 설정
- [ ] 배포 방식 결정: 직접 실행 / PaaS / Docker / CI/CD
- [ ] 포트 개방, (필요 시) 리버스 프록시·HTTPS 설정

---

원하시면 **Dockerfile**, **GitHub Actions 워크플로우**, 또는 **application.properties 예시(비밀 제외)** 형태로 구체적인 코드/설정도 따로 정리해 드릴 수 있습니다.
