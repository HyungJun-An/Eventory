# 🛠️ **스프링부트를 활용한 LMS 서비스**


## 🖥️ eventory
- **박람회(행사) 예약 관리 플랫폼**

---

## 🌟 프로젝트 소개

### ✅ 제작 목표
웹서비스 기반(웹,모바일)  다수의 박람회/행사를 **통합적**으로 관리·홍보·예약·결제·입장검수까지  
한 번에 처리할 수 있는 SaaS형 예약 관리 플랫폼을 구축

행사 운영자의 **업무 효율성 향상**, 참가자 **예약 경험 개선**, **유료 VIP 배너 광고 수익 창출**, **정산 및 통계 자동화**

---

## ⚙️ 시스템 개발 내용

- **고객**
    - 홈 (행사 리스트 + VIP 배너)
    - 행사 상세 (소개, 일정, 장소, 가격, 남은 티켓)
    - 예약 폼 (인원/옵션 선택)
    - 결제 (국내 PG, 해외 카드 선택 옵션)
    - 예약 완료 & QR 티켓 발급
    - 나의 예약 (예약내역, 취소/환불 요청, QR 재발급)

- **박람회 관리자 콘솔**
    - 대시보드 (예약 수, 결제 금액, 체크인율 등 지표)
    - 예약자 명단 (필터링, 검색, 상태변경, 엑셀 다운로드)
    - 콘텐츠 관리 (행사 소개문, 일정, 가격, 배너 이미지 교체)
    - 현장 입장 스캐너 모드 (QR 스캔 & 체크인 처리)
    - 통계 리포트 (기간별, 티켓종류별)
    - 설정 (기본정보, 담당자 연락처, 노출 상태)

- **전체 관리자**
    - 전체 대시보드 (전체 플랫폼 KPI, 행사별 비교)
    - 박람회 관리 (생성/수정/삭제, 코드/아이디/패스워드 발급)
    - 계정 관리 (박람회 관리자 계정 할당/비활성)
    - VIP 배너 광고 관리 (등록, 기간, 과금, 노출 우선순위)
    - 결제/정산 (행사별 매출, 수수료, 송금 상태, 영수증)
    - 통합 통계 (누적 예약자, 활성 유저, 인기 행사 등)
    - 시스템 설정 (PG 연동키, 도메인, 이메일 템플릿, 로고)
    - 로그/보안 (접속 로그)

- **기술 스택**
    - Spring Boot + Spring Security
    - Layered Architecture (Controller/Service/Repository/DTO/Entity)
    - MySQL 기반 DB
    - GitHub 기반 협업

---

## 💡 아이디어 착안

- 각 박람회마다 예약·결제·입장 시스템을 별도로 구축하는 비용과 시간이 큼  
  → **공통 플랫폼** 필요
- 모바일웹을 통한 간편 예약이 시장 표준이 되었으나, 소규모/중소 행사들은 여전히 수기 접수(전화/메일/폼) 의존  
  → **예약 데이터 분산, 중복, 누락 문제**
- 참가자 확인 및 현장 입장 검수 시 명단 대조에 시간이 많이 걸림  
  → **QR 기반 전자 티켓**이 요구됨
- 행사 노출 경쟁 심화  
  → 플랫폼 내 **VIP 유료 배너 광고 슬롯**을 통한 추가 수익 모델 가능
- 운영사(전체 관리자)가 여러 박람회를 호스팅하고, 행사진행사(박람회 관리자)에게 개별 권한 위임하는 기능 필요
- 정산, 통계(예약 수, 결제 금액, 참여자 현황) 자동화 요구 증가

---
## :link: 배포 링크

> ### [⛪ 배포 링크 예시](https:www.naver.com)

---
## 🗣️ 프로젝트 발표 영상 & 발표 문서

> ### 🗓️ 2025.00-.00 - 2025.00.00
> ### [📺 발표 영상 링크]()
> ### [📑 발표 PPT 링크]()

---

## 🖥️ 서비스 소개
|                                                                                                             메인 화면 1                                                                                                            |                                                                                                             메인 화면 2                                                                                                            |                                                                                                           소셜 로그인 1                                                                                                          |                                                                                                            마이페이지 1                                                                                                           |
| :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| <img src="https://lh6.googleusercontent.com/M7RxaY_ZsF5sQcqiIOafdBGHuVGqKrQ0c07hzOtLgkhxYTK-aRKWdhkMfi8DaoZxyDWLZKhdUEDKBIEpETcm2_sH5JdW69mrOXzASMQYHFEiaP0QbgCEHa5bnKzITG-v9ztn0QfbCeZtznJ8q-SDo2qoEA=s2048" alt="MainPage1"> | <img src="https://lh6.googleusercontent.com/WSSZM-N1tpBvXui0ivLMaSJv5u-Jn8oUNXik40GOW7thMqmNdMPrjfO4-halmEZ80GuBBlI5ENm8TtDovjSOMRqHE6Z5w7F5yYxdplO643k3wfhyW4wT0IUT15Rv6kk_FAnST07-50NOOeXHjRfjh6dEBA=s2048" alt="MainPage2"> | <img src="https://lh6.googleusercontent.com/LgLnyK4xbij_SmBQd_9b-zKL7NjFBceXLFr97o-S9z4JMw7bZaO4E9W5SbwJn_xOAVu4xCk9Se0eqShfP8YkzkaL-QilqVa6LRaQEQ5h4PHLZOgmT8ZByBV-eEIZEr9D3I1mB6qu5nhIehup3910FytnrQ=s2048" alt="Login1"> | <img src="https://lh5.googleusercontent.com/KRfPZ8P2TzvLBeHAcApzzHDn6xbkCYp8Z9sDmWSifQNwQwfC7HAjczd-KHVs5dGbKhi2AO5O3A8wd8mewcze3TKb_yM9y5-PHQVE7axz5HVdWsI1alg2-qatjn7G2c0Y6Fx786KMjoiIULFLpCoQx6HWJA=s2048" alt="MyPage1"> |


## 🧰 사용 스택


### :wrench: System Architecture

<img src="https://user-images.githubusercontent.com/90237119/215304129-d8006105-cf1c-49c7-a819-4f819dfac523.png"/>

### FE
<div align=center>
  <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white"> 
  <img src="https://img.shields.io/badge/css-1572B6?style=for-the-badge&logo=css3&logoColor=white"> 
  <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"> 
  <br>

  <img src="https://img.shields.io/badge/react-00A8E1?style=for-the-badge&logo=react&logoColor=black"> 
  <img src="https://img.shields.io/badge/figma-EF2D5E?style=for-the-badge&logo=figma&logoColor=black">
  <img src="https://img.shields.io/badge/node.js-339933?style=for-the-badge&logo=Node.js&logoColor=white">
  <img src="https://img.shields.io/badge/prettier-FF4F8B?style=for-the-badge&logo=prettier&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/axios-6935D3?style=for-the-badge&logo=axios&logoColor=white">
  <img src="https://img.shields.io/badge/styled Components-E9568E?style=for-the-badge&logo=styledComponents&logoColor=white">
  <img src="https://img.shields.io/badge/redux toolkit-66459B?style=for-the-badge&logo=redux&logoColor=white">
  <img src="https://img.shields.io/badge/npm-ED1C24?style=for-the-badge&logo=npm&logoColor=white">

  <br>
</div>

### BE
<div align=center> 
  <img src="https://img.shields.io/badge/JAVA-007396?style=for-the-badge&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> 
  <img src="https://img.shields.io/badge/redis-D0271D?style=for-the-badge&logo=redis&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/spring Boot-6DB33F?style=for-the-badge&logo=springBoot&logoColor=white">
  <img src="https://img.shields.io/badge/fly way-ED1C24?style=for-the-badge&logo=flyway&logoColor=white">
  <img src="https://img.shields.io/badge/linux-FCC624?style=for-the-badge&logo=linux&logoColor=black"> 
  <img src="https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/nginx-006272?style=for-the-badge&logo=nginx&logoColor=green">
  <img src="https://img.shields.io/badge/spring security-6DB33F?style=for-the-badge&logo=springSecurity&logoColor=white">
  <img src="https://img.shields.io/badge/query dsl-008FC7?style=for-the-badge&logo=queryDsl&logoColor=white">
  <br>
</div>


--- 

## :busts_in_silhouette: 팀 동료

### FE

| <a href=https://github.com/><img src="https://avatars.githubusercontent.com/u/93540726?v=4" width=100px/><br/><sub><b>@GithubID</b></sub></a><br/> | <a href=https://github.com/><img src="https://avatars.githubusercontent.com/u/93540726?v=4" width=100px/><br/><sub><b>@GithubID</b></sub></a><br/> | <a href=https://github.com/><img src="https://avatars.githubusercontent.com/u/93540726?v=4" width=100px/><br/><sub><b>@GithubID</b></sub></a><br/> | <a href=https://github.com/><img src="https://avatars.githubusercontent.com/u/111436967?v=4" width=100px/><br/><sub><b>@GithubID</b></sub></a><br/> |
|:----------------------------------:|:----------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------:|:----------:|
|                홍길동                 |    김00     |                                                                            강00                                                                            |    송00     |


### BE

| <a href=https://github.com/ddolly518/><img src="https://avatars.githubusercontent.com/u/80440158?v=4" width=100px/><br/><sub><b>@ddolly518</b></sub></a><br/> | <a href=https://github.com/dokdokee><img src="https://avatars.githubusercontent.com/u/203818385?v=4" width=100px/><br/><sub><b>@dokdokee</b></sub></a><br/> | <a href=https://github.com/yujineeo/><img src="https://avatars.githubusercontent.com/u/200905114?v=4" width=100px/><br/><sub><b>@yujineeo</b></sub></a><br/> | <a href=https://github.com/Seungmi97/><img src="https://avatars.githubusercontent.com/u/132995507?v=4" width=100px/><br/><sub><b>@Seungmi97</b></sub></a><br/> |
|:----------------------------------:|:----------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------:|:----------:|
|                강민서                 |    신드보라     |                                                                            김유진                                                                            |    황승미     |


## 📑 프로젝트 규칙

### Branch Strategy
> - main / dev 브랜치 기본 생성
> - main과 dev로 직접 push 제한
> - PR 전 최소 1인 이상 승인 필수

### Git Convention
> 1. 적절한 커밋 접두사 작성
> 2. 커밋 메시지 내용 작성
> 3. 내용 뒤에 이슈 (#이슈 번호)와 같이 작성하여 이슈 연결

> | 접두사        | 설명                           |
> | ------------- | ------------------------------ |
> | Feat :     | 새로운 기능 구현               |
> | Add :      | 에셋 파일 추가                 |
> | Fix :      | 버그 수정                      |
> | Docs :     | 문서 추가 및 수정              |
> | Style :    | 스타일링 작업                  |
> | Refactor : | 코드 리팩토링 (동작 변경 없음) |
> | Test :     | 테스트                         |
> | Deploy :   | 배포                           |
> | Conf :     | 빌드, 환경 설정                |
> | Chore :    | 기타 작업                      |


### Pull Request
> ### Title
> * 제목은 '[Feat] 홈 페이지 구현'과 같이 작성합니다.

> ### PR Type
> - [ ] FEAT: 새로운 기능 구현
> - [ ] ADD : 에셋 파일 추가
> - [ ] FIX: 버그 수정
> - [ ] DOCS: 문서 추가 및 수정
> - [ ] STYLE: 포맷팅 변경
> - [ ] REFACTOR: 코드 리팩토링
> - [ ] TEST: 테스트 관련
> - [ ] DEPLOY: 배포 관련
> - [ ] CONF: 빌드, 환경 설정
> - [ ] CHORE: 기타 작업

> ### Description
> * 구체적인 작업 내용을 작성해주세요.
> * 이미지를 별도로 첨부하면 더 좋습니다 👍

> ### Discussion
> * 추후 논의할 점에 대해 작성해주세요.

### Code Convention
>BE
> - 패키지명 전체 소문자
> - 클래스명, 인터페이스명 CamelCase
> - 클래스 이름 명사 사용
> - 상수명 SNAKE_CASE
> - Controller, Service, Dto, Repository, mapper 앞에 접미사로 통일(ex. MemberController)
> - service 계층 메서드명 create, update, find, delete로 CRUD 통일(ex. createMember)
> - Test 클래스는 접미사로 Test 사용(ex. memberFindTest)


> FE
> - styled-Component 변수명 S + 변수명 (ex. Swrap)
> - styled-Component는 return문 위에 작성
> - 크게는 styled-Component, 그 안에서 className 사용
> - Event handler 사용 (ex. handle ~)
> - export방식 (ex. export default ~)
> - 화살표 함수 사용

### Communication Rules
> - Discord 활용
> - 정기 회의 00시 ~ 00시


## :clipboard: Documents
> [📜 API 명세서 예시](https://docs.google.com/spreadsheets/d/1utipxj0PCM7cDAgiXP1EOu6pdmhNR-IkrVWJD_GMxtg/edit?usp=sharing)
>
> [📜 요구사항 정의서 예시](https://docs.google.com/spreadsheets/d/1utipxj0PCM7cDAgiXP1EOu6pdmhNR-IkrVWJD_GMxtg/edit?gid=611143583#gid=611143583)
>
> [📜 ERD 예시](https://www.erdcloud.com/)
>
> [📜 테이블 명세서 예시](https://docs.google.com/spreadsheets/d/1utipxj0PCM7cDAgiXP1EOu6pdmhNR-IkrVWJD_GMxtg/edit?gid=95563668#gid=95563668)
>
> [📜 화면 정의서 예시]([https://docs.google.com/spreadsheets/d/1vud5xV8rB9Y6akOIma49hSzdZsoj8DVG0-fYE4NdP_g/edit?usp=sharing](https://docs.google.com/spreadsheets/d/1utipxj0PCM7cDAgiXP1EOu6pdmhNR-IkrVWJD_GMxtg/edit?gid=400714320#gid=400714320)