# JavaFX UI — Issue Tracker (소프트웨어공학 02조)

React 디자인 시안을 그대로 JavaFX로 옮긴 버전. 손그림 효과(rough.js)는 제외하고 종이톤 컬러 + 손글씨 폰트로 분위기만 살렸습니다.

## 1. 기존 프로젝트(`SE_Term_Project_02`)에 합치는 방법

이 폴더의 구조:

```
javafx/
├── build.gradle.snippet      ← 기존 build.gradle 에 합칠 부분
├── src/main/java/com/issuetracker/ui/         ← Java 소스 (UI 전체)
└── src/main/resources/com/issuetracker/ui/    ← CSS, 폰트
```

### 1) 기존 `build.gradle` 에 추가할 것

`build.gradle.snippet` 의 내용을 기존 `build.gradle` 에 머지하세요. 핵심:

```gradle
plugins {
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.1.0'
}

javafx {
    version = '21'
    modules = ['javafx.controls', 'javafx.graphics']
}

application {
    mainClass = 'com.issuetracker.ui.App'
}
```

### 2) 소스 옮기기

```bash
# 프로젝트 루트(SE_Term_Project_02)에서
cp -r javafx/src/main/java/com/issuetracker/ui      src/main/java/com/issuetracker/
cp -r javafx/src/main/resources/com/issuetracker/ui src/main/resources/com/issuetracker/
```

### 3) 실행

```bash
./gradlew run
```

## 2. 폰트 (선택)

`src/main/resources/com/issuetracker/ui/fonts/` 에 다음 폰트를 넣으면 손글씨 분위기가 살아요. 없어도 시스템 폰트로 잘 동작합니다.

- **Kalam** — https://fonts.google.com/specimen/Kalam (Regular, Bold)
- **Gaegu** — https://fonts.google.com/specimen/Gaegu (Regular, Bold)
- **JetBrains Mono** — https://www.jetbrains.com/lp/mono/ (Regular)

`.ttf` 파일을 `fonts/` 폴더에 넣으면 `App.java` 의 `FontLoader.init()` 이 자동으로 로드합니다.

## 3. 백엔드 연결 (Mock → Real)

지금은 `Store.java` 에 mock 데이터가 있어요. 실제 백엔드 service/controller를 쓰려면:

1. `Store` 안의 `accounts`, `issues`, `projects`, `comments`, `members` ObservableList 를 backend 의 repository.findAll() 결과로 채우기
2. `Store.createIssue(...)` 같은 메서드를 `IssueController.createIssue(...)` 로 위임
3. 백엔드 controller 들이 현재 `System.out.println` 으로 알림하는 걸 `CommonResponse<T>` 리턴으로 바꾸면 UI에서 success/error 처리 가능

DTO 시그니처는 그대로 따라갔으니 큰 수정 없이 연결 가능합니다.

## 4. 화면 구성

- **LoginView** — 로그인 + Quick Login (admin/pl1/dev1/dev2/tester1)
- **ProjectsView** — 프로젝트 목록 + 새 프로젝트
- **BoardView** — 칸반 6컬럼 (NEW → ASSIGNED → FIXED → RESOLVED → CLOSED + REOPENED), 드래그앤드롭으로 상태 전환
- **IssueDetailView** — 이슈 상세 + 상태 타임라인 + 코멘트 + assignee 자동 추천
- **StatsView** — 일별 트렌드 / priority / status / assignee load (JavaFX 내장 차트)
- **MembersView** — 멤버 추가/제외/역할 변경
