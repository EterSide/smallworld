# jpackage로 exe 만들기

Spring Boot 앱을 **Windows exe 실행 이미지**로 만드는 방법입니다.

---

## 필요 조건

- **JDK 14 이상** (jpackage 포함). 프로젝트는 Java 17 사용 중이므로 JDK 17 설치 시 함께 제공됩니다.
- **Windows**에서 실행하면 Windows용 exe가 생성됩니다.

---

## 실행 방법

### 1. JAR 빌드 + exe 이미지 생성

```bash
./gradlew jpackage
```

Windows PowerShell/CMD:

```cmd
gradlew.bat jpackage
```

### 2. 결과물 위치

- **폴더**: `build/jpackage/Smallworld/`
- **실행 파일**: `build/jpackage/Smallworld/Smallworld.exe`

이 폴더 전체를 통째로 복사해서 배포하면 됩니다. **JRE가 포함**되어 있어서, 받는 사람 PC에 Java를 설치하지 않아도 실행할 수 있습니다.

---

## 실행 후 동작

1. `Smallworld.exe` 더블클릭 → Spring Boot(내장 Tomcat) 서버가 기동됩니다.
2. 서버가 뜨면 **기본 브라우저가 자동으로** http://localhost:8080 을 엽니다. (직접 주소 입력 불필요)
3. exe를 닫으면 서버가 종료됩니다.

---

## 사용자 설정 (포트·브라우저 자동 열기 등)

exe를 받은 사용자도 **설정 파일 하나**로 포트나 브라우저 자동 열기를 바꿀 수 있습니다.

- **위치**: `Smallworld.exe`가 있는 폴더와 **같은 폴더**에 `application.properties` 파일을 만들면 됩니다.
- **안내 파일**: exe 옆에 **"설정 예시.txt"** 가 있습니다. 그 내용대로 `application.properties`를 만들고 필요한 줄만 넣으면 됩니다.
- **예시**
  - 포트를 9090으로 바꾸기: `server.port=9090`
  - 브라우저 자동 열기 끄기: `smallworld.open-browser=false`
- 저장 후 exe를 다시 실행하면 적용됩니다. (코드나 JAR를 수정할 필요 없음)

---

## 참고

- **DB**: H2 내장 DB를 사용하므로 **별도 DB 설치 없이** exe만 실행하면 됩니다. 데이터는 exe가 있는 폴더 아래 `data/smallworld.mv.db` 파일로 저장됩니다 (실행 폴더와 같은 위치에 `data` 폴더가 생김).
- **방화벽**: 첫 실행 시 Windows 방화벽에서 "네트워크 접근 허용" 메시지가 나올 수 있습니다. localhost만 쓸 경우 허용해도 무방합니다.
