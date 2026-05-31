# Issue Tracker System

<img width="1262" height="653" alt="스크린샷 2026-05-29 오후 6 39 19" src="https://github.com/user-attachments/assets/822129bc-5ded-47e1-beb2-18f9dbccf136" />


이슈 관리 시스템( Issue Tracker System) — 중앙대학교 소프트웨어공학 2026 텀프로젝트

<br>
<br/>

## 프로젝트 개요

소프트웨어 개발 과정에서 이슈를 등록·추적·관리하는 시스템입니다.  
JIRA, Trac, Bugzilla 등을 참고하여 핵심 기능을 Java로 구현하였습니다.

**주요 특징**
- MVC 아키텍처로 UI와 비즈니스 로직 완전 분리
- Swing / JavaFX 두 가지 UI 지원 (동일 모델 재사용)
- JSON 파일 기반 영속 저장소 (DBMS 미사용)
- 해결된 이슈 이력 기반 assignee 자동 추천

<br>
<br/>

## 기술 스택

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Gson](https://img.shields.io/badge/Gson-1B6B3A?style=for-the-badge&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-BC4521?style=for-the-badge&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

<br>
<br/>

## 팀원

| 이름 | 사진 | 담당 |
|------|------| ------|
| 김태우 | <img width="100" alt="Image" src="https://github.com/user-attachments/assets/5d5c615f-886a-4793-8b9a-163397570d7c" /> |  <ul><li>Comment Controller, Service 등 관련 코드 구현</li><li>Swing UI 구현</li></ul>  |
| 박신빈 | <img width="100" alt="박신빈" src="https://github.com/user-attachments/assets/96143f00-2162-496e-bd61-bea93a050af1" />
 |  |
| 안재인 |  | <ul><li>ㅇ</li><li>javafx UI 구현</li><li>Asignee 추천 로직 구현</li></ul> |
| 이주영 | <img src="https://github.com/user-attachments/assets/3ba69cfe-5e6f-40b4-a19c-91199faf5ad9" alt="이주영" width="100"> | <ul><li>프로젝트 기초 구조 설계</li><li>Account Controller, Service 등 관련 코드 구현</li><li>Asignee 추천 로직 구현</li></ul>   |

<br>
<br/>

## 아키텍처

<br>
<br/>

## 주요 기능

- 계정 관리 (admin / PL / dev / tester)
- 이슈 등록·수정·상태 변경 (new → assigned → fixed → resolved → closed / reopened)
- 이슈 검색 및 필터링 (assignee, 상태, reporter 등)
- 코멘트 추가
- 이슈 통계 (일/월별 트렌드)
- assignee 자동 추천 (키워드 유사도 기반)

<br>
<br/>
