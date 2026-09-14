# Eventory 트러블슈팅 & 성능 개선 기록

> 구현 곳곳의 선택 이유·대안·한계(왜 Lua 스크립트인가, 왜 AFTER_COMMIT인가 등)는 `docs/TECH-DECISIONS.md`에 따로 정리했다.

---

## [2026-09-12] 비관적 락 타임아웃 힌트가 MySQL에서 무시됨 — 최대 50초 대기

### 문제 발견 배경

기술 선택 문서를 쓰면서 락 설정을 다시 확인했다. 2026-05-16에 "`lock.timeout = 3000ms`로 설정해 락 대기가 3초를 초과하면 즉시 예외"라고 기록했지만, 이를 실제로 측정한 적은 없었다.

### 근본 원인 분석

Testcontainers(MySQL 8.0)에서 앞 트랜잭션이 행 락을 6초 동안 잡고 있을 때, 뒤 트랜잭션의 `findByIdWithLock()`이 얼마나 기다리는지 측정했다.

```
Hibernate: select ... from expo e1_0 where e1_0.expo_id=? for update     ← 타임아웃 절이 없다
[락 타임아웃] 앞 트랜잭션 6초 점유 / 뒤 요청 대기 6046ms / 결과: 락 획득 후 성공
```

Hibernate MySQL 방언은 `jakarta.persistence.lock.timeout` 힌트를 SQL에 반영하지 않았다. 실제 상한은 InnoDB 기본값 `innodb_lock_wait_timeout = 50`초였다. 인기 박람회에 결제가 몰리면 요청 스레드와 DB 커넥션이 최대 50초씩 묶여, 커넥션 풀이 고갈되고 결제와 무관한 API까지 멈출 수 있는 상태였다.

### 정량적 분석

| 지표 | 수정 전 | 수정 후 |
|---|---|---|
| 뒤 요청의 락 대기 (앞 트랜잭션 6초 점유) | 6,046ms 후 락 획득 | **3,031ms 후 예외** |
| 이론상 최대 대기 | 50초 (InnoDB 기본값) | **3초** |
| 대기 초과 시 응답 | (50초 후) 500 | 409 `R017` + PG 결제 자동 취소 |
| 앱 DB 커넥션의 세션 값 | 50 | 3 (커넥션 10개 전부 확인) |

### 해결 방법

커넥션을 만들 때 세션 변수를 설정해 앱의 모든 커넥션에 적용했다.

```yaml
spring:
  datasource:
    hikari:
      connection-init-sql: SET SESSION innodb_lock_wait_timeout = 3
```

대기가 3초를 넘으면 MySQL 1205 오류가 나고, Spring이 이를 `PessimisticLockingFailureException`으로 변환한다. 결제 확정 로직은 이 예외를 받으면 PG 결제를 자동 취소하고, 500 대신 "요청이 몰려 처리하지 못했다"(R017, 409)로 응답한다. 이 동작은 `ExpoReservationConcurrencyTest.lockWaitIsBoundedTo3Seconds`와 `PaymentServiceImplTest.lockTimeout_cancelsAtPg`로 고정했다.

JPA 힌트는 이를 지원하는 DB(Oracle, PostgreSQL 등)를 대비해 남겨 두되, MySQL에서는 무시된다고 주석을 달았다.

### 선택 근거

> `NOWAIT`(즉시 실패)는 잠깐 줄 서면 처리될 요청까지 실패시킨다. 쿼리마다 `SET SESSION`을 호출하는 방식은 누락 위험이 있고, MySQL 서버 설정은 앱 밖에 흩어진다. 커넥션 초기화 SQL은 모든 커넥션에 일관되게 적용되고 설정이 코드와 함께 버전 관리된다.

> 교훈: 설정 "값"을 넣은 것과 설정이 "동작"하는 것은 다르다. 락·타임아웃처럼 DB 방언에 따라 달라지는 설정은 실제 DB에서 측정해야 한다. H2로 테스트했다면 이 문제를 발견할 수 없었다.

---

## [2026-09-12] 시스템관리자 로그인 불가 — 역할별 토큰 저장 키 불일치

### 문제

시스템관리자로 로그인하면 알림 없이 다시 로그인 화면으로 돌아왔다. 로그인 API는 200을 반환했다.

### 원인

토큰 저장 키가 네 곳에 각자 정의되어 있었고, 서로 달랐다.

| 위치 | 시스템관리자 토큰 처리 |
|---|---|
| `LoginPage.jsx` | `adminAccessToken`(박람회관리자 키)에 저장 |
| `AuthContext.login()` | 역할과 무관하게 `accessToken`(참관객 키)에도 저장 |
| `axiosInstance.js` | `sysAdminAccessToken`에서 읽음 → **없음** |
| `LogoutButton.jsx` | (이전 수정으로) `sysAdminAccessToken`에서 읽음 |

`/api/sys/**` 요청에 토큰이 붙지 않아 403이 나고, axios가 재발급을 시도했지만 `sysAdminRefreshToken`도 없어 `/login`으로 되돌렸다.

**그동안 드러나지 않은 이유**: `/api/sys/**`는 인증 없이 열려 있었고, 옛 시스템관리자 화면은 토큰 없이 `fetch`로 호출했다. 토큰이 필요 없었기 때문에 저장 키가 틀려도 동작했다. 2026-09-11에 시스템관리자 API에 인증을 적용하면서 숨어 있던 버그가 드러났다. 당시 브라우저 검증은 토큰을 localStorage에 직접 넣고 진행해, "로그인 화면이 토큰을 어디에 저장하는가"라는 경로를 건너뛰었다.

### 수정

`auth/tokenKeys.js` 한 파일에 역할별 저장 키·재발급 URL·로그아웃 경로를 정의하고, 네 곳 모두 이 파일만 쓰게 바꿨다.

```js
export const TOKEN_KEYS = {
  USER:         { access: "accessToken",         refresh: "refreshToken",         refreshUrl: "/api/auth/refresh" },
  EXPO_ADMIN:   { access: "adminAccessToken",    refresh: "adminRefreshToken",    refreshUrl: "/api/admin/refresh" },
  SYSTEM_ADMIN: { access: "sysAdminAccessToken", refresh: "sysAdminRefreshToken", refreshUrl: "/api/admin/sys/refresh" },
};
```

로그인할 때는 다른 역할의 토큰을 먼저 모두 지운다. 여러 역할의 토큰이 남아 있으면 요청에 어떤 토큰이 붙을지 예측하기 어렵다. 실제 로그인 화면으로 시스템관리자·박람회관리자에 로그인해, 역할별 키에만 토큰이 저장되고 대시보드 API가 정상 응답하는 것을 확인했다.

### 추가로 발견: 로그아웃 직후 도착한 응답이 페이지를 강제 새로고침

검증 스크립트가 대시보드 차트를 불러오는 도중에 로그아웃을 누르자, 화면이 `/login?reason=refreshFail`로 새로고침됐다.

```
로그아웃 → 서버: AccessToken 블랙리스트 등록 / 브라우저: 토큰 삭제
        → 아직 대기 중이던 차트 요청 3건이 401 로 도착
        → axios 인터셉터가 재발급 시도 → 리프레시 토큰 없음 → localStorage.clear() + 페이지 새로고침
```

인터셉터가 "로그아웃된 뒤 도착한 응답"과 "토큰이 만료된 응답"을 구분하지 못한 것이 원인이었다. 요청에 실어 보낸 토큰과 지금 저장된 토큰을 비교하도록 바꿨다.

| 보낸 토큰 | 저장된 토큰 | 판단 | 처리 |
|---|---|---|---|
| 있음 | 없음 | 로그아웃 뒤 도착한 응답 | 조용히 실패 (재발급·새로고침 안 함) |
| 있음 | 있음, 다름 | 그사이 다른 요청이 재발급함 | 새 토큰으로 한 번만 재시도 |
| 있음 | 있음, 같음 | 토큰 만료 | 기존대로 재발급 후 재시도 |
| 없음 | — | 비로그인 사용자 | 기존대로 로그인 화면 안내 |

### 교훈

> 인증을 강화하는 변경은 실제 로그인 화면부터 API 호출까지 이어서 검증해야 한다. 같은 개념(역할별 토큰 키)이 여러 파일에 복제되어 있으면 한 곳만 고쳐도 나머지와 어긋난다. 이런 값은 한 곳에 정의한다.

---

## [2026-09-11] 프론트엔드 번들 785kB 단일 파일 — 라우트 단위 코드 분할

### 문제 발견 배경

빌드할 때마다 Vite가 `Some chunks are larger than 500 kB after minification` 경고를 냈다. 빌드 결과는 JS 파일 하나(785.57kB, gzip 238.77kB)였다. 참관객이 메인 화면만 열어도 박람회관리자·시스템관리자 화면, 차트 라이브러리(recharts), 결제 SDK 코드까지 전부 내려받는 구조였다.

### 근본 원인 분석

`App.jsx`가 30여 개 페이지를 모두 정적 `import` 하고 있어 번들러가 나눌 지점이 없었다. 가장 큰 의존성인 recharts(gzip 약 93kB)는 관리자 대시보드와 매출 화면 세 곳에서만 쓰였다. 결제 SDK도 결제 화면 한 곳에서만 썼다. 사용하지 않는 UI·아이콘 라이브러리(antd, @ant-design/icons, fontawesome 5종)와 파일(`Notification.jsx`, `jwtUtils.js`)도 의존성에 남아 있었다.

### 해결 방법

#### 1. 페이지를 `React.lazy`로 분리

```jsx
const SysDashboardPage = lazy(() => import("./systemAdmin/pages/SysDashboardPage"));

/** 라우트마다 Suspense — 페이지를 받는 동안 레이아웃(사이드바·헤더)은 그대로 보인다 */
const page = (element) => <Suspense fallback={<PageFallback />}>{element}</Suspense>;

<Route path="/sys" element={page(<SysLayout />)}>
  <Route path="dashboard" element={page(<SysDashboardPage />)} />
</Route>
```

`<Routes>` 전체를 Suspense 하나로 감싸면, 관리자 메뉴를 이동할 때마다 사이드바까지 로딩 화면으로 바뀌어 깜빡인다. 그래서 라우트마다 Suspense를 뒀다.

#### 2. React 계열만 별도 청크로 (배포 간 캐시 재사용)

```js
manualChunks(id) {
  if (id.includes("commonjsHelpers")) return "react-vendor";
  if (/[\\/]node_modules[\\/](react|react-dom|react-router|react-router-dom|scheduler)[\\/]/.test(id)) return "react-vendor";
}
```

#### 3. 트러블슈팅: 수동 청크가 메인 화면에 차트를 끌어옴

처음에는 recharts도 `charts` 수동 청크로 묶었다. 그런데 manifest를 확인하니 메인 화면이 차트 청크(gzip 97.6kB)까지 받고 있었다.

```
index.html → react-vendor.js → charts.js   ← React 청크가 차트 청크를 import
```

React는 CommonJS 모듈이라 변환 헬퍼(`commonjsHelpers`)가 필요한데, Rollup이 이 헬퍼를 먼저 만난 수동 청크인 `charts`에 넣었다. 그 결과 `react-vendor`가 헬퍼를 쓰려고 `charts`를 import하게 됐다. 차트는 lazy 페이지에서만 import되므로 수동 지정을 빼면 Rollup이 해당 화면용 청크로 자동 분리한다. 그래서 헬퍼만 `react-vendor`에 고정하는 방식으로 바꿨다.

### 정량적 분석

진입 경로별로 처음 내려받는 JS (Vite manifest의 정적 import를 따라가 합산)

| 진입 경로 | 수정 전 (raw / gzip) | 수정 후 (raw / gzip) | gzip 감소 |
|---|---|---|:---:|
| 메인 `/` | 785.6kB / 238.8kB | 316.6kB / **105.0kB** | **−56%** |
| 로그인 | 785.6kB / 238.8kB | 307.7kB / **102.4kB** | −57% |
| 결제 | 785.6kB / 238.8kB | 309.4kB / **103.5kB** | −57% |
| 박람회관리자 대시보드 | 785.6kB / 238.8kB | 660.5kB / 213.2kB | −11% |
| 시스템관리자 대시보드 | 785.6kB / 238.8kB | 631.7kB / 203.6kB | −15% |

- 차트 청크(gzip 93.1kB)는 대시보드·매출 화면에 들어갈 때만 받는다.
- 이후 배포에서 앱 코드만 바뀌면 `react-vendor`(gzip 69.3kB)는 브라우저 캐시를 그대로 쓴다.
- 의존성은 15개에서 8개로 줄었다(antd, @ant-design/icons, fontawesome 5종 제거).

### 선택 근거

> 이 서비스는 참관객(메인·상세·결제)과 관리자(대시보드·차트)의 사용 화면이 거의 겹치지 않는다. 사용자 수가 가장 많은 참관객 경로에서 관리자 코드를 걷어내는 것이 효과가 가장 크다. 관리자 화면은 첫 방문에만 차트를 받고 이후에는 캐시를 쓴다.

---

## [2026-09-11] 관리자 목록 API N+1 쿼리 — 배치 페치·GROUP BY·DTO 프로젝션

### 문제 발견 배경

시스템관리자 화면을 재작성하면서 API별로 실제 실행되는 SQL을 확인했다. `show-sql` 로그를 켠 상태에서 API를 1회 호출하고, 그동안 찍힌 `Hibernate:` 줄 수를 세는 방식으로 측정했다. 페이지 크기 10인 목록 API 하나가 SQL을 20~30개씩 실행하고 있었다.

### 근본 원인 분석

세 가지 유형이 섞여 있었다.

**지연 로딩 연관을 행마다 조회 (고전적 N+1)**  
박람회 목록은 행마다 `expo.getExpoCategories()` → `category`를 지연 로딩했다. 담당 관리자(`@ManyToOne(fetch = EAGER)`)도 페이지 조회 후 행마다 별도 SELECT로 채워졌다.

**서비스 코드에서 행마다 Repository 호출 (명시적 N+1)**  
관리자 목록은 관리자마다 `findFirstByExpoAdminOrderByCreatedAtDesc()`를 호출했다. 환불 목록은 매퍼(`ExpoMapper.toRefundResponseDto`)가 환불 행마다 예약을 다시 조회했다. 조회된 예약의 즉시 로딩 연관(user·expo·payment)까지 따라오면서 10행에 쿼리가 34개로 늘었다.

**집계를 애플리케이션 메모리에서 수행**  
플랫폼 총 결제 금액은 PAID 결제 1,091건을 전부 엔티티로 불러와 `stream().mapToLong().sum()`으로 합산했다. 결제가 늘수록 메모리와 전송량이 선형으로 증가하는 구조였다.

```
[기존 환불 목록]
결제 id 전체 조회 (박람회의 결제 수백 건)
→ refund WHERE payment_id IN (수백 개) LIMIT 10
→ 행마다 reservation 조회 + reservation 의 user·expo·payment 즉시 로딩
```

### 정량적 분석

API 1회 호출 시 실행된 SQL 수 (로컬 Docker, 데모 데이터: 박람회 13개·예약 1,091건, 페이지 크기 10)

| API | 수정 전 | 수정 후 | 적용한 방법 |
|---|:---:|:---:|---|
| 시스템관리자 박람회 목록 `/api/sys/expos` | 25 | **6** | `default_batch_fetch_size` |
| 박람회관리자 결제 내역 `/api/admin/expos/{id}/payment` | 24 | **6** | `default_batch_fetch_size` |
| 박람회관리자 환불 목록 `/api/admin/expos/{id}/refund` | 34 | **4** | 조인 DTO 프로젝션 |
| 시스템관리자 관리자 목록 `/api/sys/admins` | 8 | **3** | GROUP BY 1회 |
| 메인 박람회 목록 `/api/user/expos` | 5 | **2** | `default_batch_fetch_size` |
| 플랫폼 통계 `/api/sys/stats` — 결제 합계 조회 행 | 1,091행 | **1행** | `SUM` 쿼리 |

모든 API의 응답 값은 수정 전과 같다. 환불 목록의 전체·대기·승인 건수도 DB 직접 집계(27건)와 일치함을 확인했다. 응답 시간은 데이터가 작아 20~60ms 사이에서 편차가 커 개선 지표로 쓰지 않았다. 쿼리 수는 데이터가 늘수록 수정 전에만 선형으로 증가한다.

### 해결 방법

#### 1. 지연 로딩 연관 — `default_batch_fetch_size`

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100   # 지연 로딩 연관을 IN 쿼리로 묶어 조회
```

컬렉션(`expoCategories`) 페치 조인은 페이징과 함께 쓰면 Hibernate가 전체 결과를 메모리에서 페이징한다(HHH90003004 경고). 그래서 페이지 조회는 그대로 두고, 연관만 `IN (?, ?, …)` 한 번으로 가져오는 배치 페치를 전역으로 적용했다.

#### 2. 행마다 호출하던 조회 — GROUP BY 한 번

```java
@Query("SELECT new com.eventory.systemAdmin.dto.AdminLastExpoDto(e.expoAdmin.expoAdminId, MAX(e.createdAt)) " +
       "FROM expo e WHERE e.expoAdmin IN :admins GROUP BY e.expoAdmin.expoAdminId")
List<AdminLastExpoDto> findLastCreatedAtByAdmins(@Param("admins") Collection<ExpoAdmin> admins);
```

#### 3. 환불 목록 — 필요한 값만 조인해서 DTO로

```java
@Query(value = """
    SELECT new com.eventory.expoAdmin.dto.RefundResponseDto(
        r.refundId, res.code, p.method, p.amount, p.paidAt, r.reason, r.status)
    FROM refund r
    JOIN r.payment p
    JOIN reservation res ON res.payment = p
    WHERE res.expo.expoId = :expoId
      AND (:status IS NULL OR r.status = :status)
    """, countQuery = "...")
Page<RefundResponseDto> findRefundRowsByExpoId(Long expoId, RefundStatus status, Pageable pageable);
```

화면에 필요한 7개 컬럼만 조회하므로 엔티티와 즉시 로딩 연관을 만들 필요가 없다. 결제 id 수백 개를 IN 조건으로 넘기던 전처리 쿼리도 사라졌다.

#### 4. 집계 — DB에서 계산

```java
@Query("SELECT COALESCE(SUM(p.amount), 0) FROM payment p WHERE p.status = :status")
BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);
```

### 선택 근거

> N+1은 원인에 따라 도구가 다르다. 지연 로딩 연관은 설정 한 줄(배치 페치)로 전역 해결되지만, 서비스 코드가 직접 반복 호출하는 조회는 설정으로 막을 수 없어 쿼리를 다시 설계해야 했다. 목록 화면은 수정 없이 읽기만 하므로 엔티티 대신 DTO 프로젝션을 쓰면 연관 로딩 자체가 발생하지 않는다.

---

## [2026-09-11] 테스트 0개 → 41개 — Testcontainers로 동시성 제어 전략 검증

### 문제 발견 배경

결제·인증 로직을 여러 차례 고치는 동안 검증 수단이 수동 API 호출뿐이었다. 테스트 파일이 하나도 없어 수정할 때마다 같은 시나리오를 손으로 반복해야 했다. 특히 동시 예약 락은 수동으로 재현하기 어려워, 코드만 보고 동작을 믿어야 하는 상태였다.

### 해결 방법

위험도가 높은 영역부터 테스트를 추가했다.

| 영역 | 종류 | 검증 내용 | 수 |
|---|---|---|:---:|
| `PaymentServiceImpl` | 단위 (Mockito) | 금액 위변조·예약 실패 시 PG 자동 취소, 타인 주문 거부, 조회 실패 시 주문 복구, 웹훅 멱등·선도착 대기 | 12 |
| `PortOneWebhookVerifier` | 단위 | 정상 서명, 본문 변조, 다른 키, 재전송(5분 초과), 헤더 누락, 키 교체 | 7 |
| `AdminAuthServiceImpl` | 단위 | 다른 계정 종류의 리프레시 토큰으로 재발급 불가 (권한 상승 회귀) | 6 |
| `SystemAdminService` | 단위 | 승인 시 계정 발급, 사유 없는 반려, 관리자 삭제 차단, 재발급 시 세션 폐기 | 5 |
| `Expo` 도메인 | 단위 | 정원 초과 거부, 0 미만 방지 | 3 |
| `RedisTokenStore` | 통합 (Redis 컨테이너) | 계정 종류별 토큰 분리·회전·폐기 | 5 |
| 동시 예약 | 통합 (MySQL 컨테이너) | 비관적 락 / 낙관적 락 비교 | 3 |

동시성 테스트는 실제 MySQL(InnoDB)에서 20개 스레드가 `CountDownLatch`로 동시에 출발해, 결제 완료와 같은 방식(락 조회 → 도메인 메서드로 인원 증가 → 커밋)을 실행한다.

### 정량적 분석

**동시 20건 예약 결과 (Testcontainers MySQL 8.0)**

| 시나리오 | 성공 | 정원 초과 거부 | 낙관적 락 충돌 | 최종 예약 인원 |
|---|:---:|:---:|:---:|:---:|
| 비관적 락, 남은 자리 1석 | **1** | 19 | 0 | 정원과 정확히 일치 |
| 비관적 락, 남은 자리 100석 | **20** | 0 | 0 | +20 (갱신 손실 없음) |
| 낙관적 락(`@Version`)만, 남은 자리 100석 | **2** | 0 | **18** | +2 |

2026-05-16에 비관적 락을 고른 근거("낙관적 락만 쓰면 이미 결제된 사용자가 예약 실패를 받을 수 있다")가 수치로 확인됐다. 자리가 100석 남아 있어도 동시 요청의 90%가 버전 충돌로 실패한다. 결제 흐름에서는 이 실패가 곧 PG 자동 취소와 사용자 재결제로 이어진다.

**커버리지 (JaCoCo, 라인 기준)**  
`RedisTokenStore` 81%, `PortOneWebhookVerifier` 78%, `PaymentServiceImpl` 49%(환불 경로는 아직 미포함), 전체 13%.

### 트러블슈팅: 테스트 환경 구성

**JDK 23 + Lombok 1.18.30 컴파일 실패**  
`java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`. 로컬 기본 JDK(23)를 pom에 고정된 Lombok 버전이 지원하지 않았다. 프로젝트 기준인 JDK 21로 실행해 해결했다(`JAVA_HOME` 지정).

**Testcontainers가 Docker를 찾지 못함**  
`Could not find a valid Docker environment`. 로컬 Docker Engine 29(OrbStack)는 최소 API 버전이 1.40인데, Testcontainers 1.21.3(docker-java)은 기본으로 1.32를 요청해 거부됐다. 테스트 리소스에 API 버전을 명시해 해결했다.

```properties
# src/test/resources/docker-java.properties
api.version=1.44
```

**테스트 설정 분리**  
`application.yml`이 dev 프로파일을 활성화하고, dev 설정은 `.env`의 시크릿을 요구한다. 그래서 테스트는 `@ActiveProfiles("test")`와 `application-test.yml`만으로 동작하게 해 `.env` 없이도 실행된다.

```bash
cd eventory-server && mvn test        # 리포트: target/site/jacoco/index.html
```

---

## [2026-09-11] 시크릿 평문 커밋 — 환경변수 이전과 기동 전 검증

### 문제 발견 배경

결제 키를 교체하다가 `application-dev.yml`과 `application-prod.yml`에 운영에 쓰이는 비밀값이 평문으로 들어 있는 것을 확인했다.

| 값 | 위험 |
|---|---|
| JWT 서명키 | 저장소를 읽을 수 있는 누구나 임의 사용자·관리자 토큰 위조 가능 |
| PortOne V2 API Secret, 웹훅 시크릿 | 결제 조회·취소 API 호출 가능 |
| Gmail 계정·앱 비밀번호 | 메일 계정 탈취 |
| DB·키스토어 비밀번호 | 인프라 접근 |
| QR 서명키 기본값 `change-me` | 설정을 빠뜨리면 공개된 기본값으로 **입장 QR 위조 가능** |

### 해결 방법

1. 모든 비밀값을 기본값 없는 환경변수(`${JWT_SECRET}`)로 바꿨다. 값이 없으면 서버가 뜨지 않는다.
2. `docker-compose.yml`은 `${VAR:?메시지}` 문법으로 `.env`에 값이 없으면 **컨테이너를 만들기 전에** 중단한다. MySQL healthcheck도 `$$MYSQL_PASSWORD`로 컨테이너 내부 변수를 읽어 파일에 비밀번호를 남기지 않는다.
3. IDE로 로컬 실행할 때도 같은 `.env`를 쓰도록 `spring.config.import: optional:file:../.env[.properties]`를 추가했다. `.env`의 `KEY=VALUE` 형식은 properties로 그대로 읽힌다.
4. 값 없는 템플릿 `.env.example`에 발급 위치를 적어 두었다.

```yaml
# docker-compose.yml
environment:
  - JWT_SECRET=${JWT_SECRET:?.env 에 JWT_SECRET 을 설정하세요}
  - QR_HMAC_SECRET=${QR_HMAC_SECRET:?.env 에 QR_HMAC_SECRET 을 설정하세요}
```

### 남은 조치

파일에서는 지웠지만 **git 이력에는 기존 값이 남아 있다**. JWT 서명키와 QR 서명키는 새로 발급해 교체했다. QR 서명키를 교체했기 때문에 그 이전에 발급된 입장 QR은 무효가 된다. 이력에 남은 PortOne 키와 Gmail 앱 비밀번호는 발급한 계정에서 폐기해야 한다. 이력 자체를 지우려면 `git filter-repo`로 재작성하고 강제 푸시해야 한다.

---

## [2026-09-11] PortOne 웹훅 — 서명 검증 부재와 중복 결제 레코드

### 문제 발견 배경

리팩터링 후보를 점검하던 중 `PortOneWebhookController`의 서명 검증 코드가 주석 처리된 것을 발견했다. 웹훅은 PortOne 서버가 호출해야 하므로 인증 없이 공개(`permitAll`)되어 있다. 서명 검증이 없으면 누구나 이 URL을 호출할 수 있다.

```java
// 기존 코드
String signature = headers.getOrDefault("portone-webhook-signature", "");
// PortOneServerSdk.verifySignature(props.getWebhookSecret(), body, signature);

Payment saved = paymentRepository.save(Payment.builder()   // 호출될 때마다 새 결제 저장
        .amount(amount).status(PaymentStatus.PAID) ... .build());
// TODO: 우리 쪽 주문/예약과 매핑하여 Reservation 생성/갱신 필요
```

### 근본 원인 분석

두 가지 결함이 겹쳐 있었다.

**인증 부재**  
서명을 검증하지 않아 요청이 PortOne에서 온 것인지 확인할 수 없었다.

**멱등성 부재**  
호출될 때마다 `portone_payment_id`도 예약도 없는 PAID 결제를 새로 저장했다. PortOne은 응답이 늦으면 웹훅을 재전송하므로, 정상 운영에서도 같은 결제가 중복 저장될 수 있는 구조였다. 시스템관리자 통계는 PAID 결제를 합산하므로 매출 지표가 그대로 부풀려진다.

### 정량적 분석

**재현 (수정 전 서버)**  
실제로 결제된 paymentId 하나로 서명 없는 요청을 3회 보냈다.

| 지표 | 요청 전 | 서명 없는 요청 3회 후 |
|---|---|---|
| 응답 | — | 3회 모두 `200 OK` |
| payment 행 수 | 1,091 | **1,094** (예약과 연결되지 않은 행 3개) |
| 플랫폼 총 결제 금액 | 21,822,000원 | **21,867,000원** (+45,000원) |

재현으로 생긴 행은 확인 후 삭제했다.

**수정 후 검증**

| 요청 | 결과 |
|---|---|
| 서명 헤더 없음 | 401 |
| 잘못된 서명 | 401 |
| 10분 전 서명 (재전송 공격) | 401 |
| 서명 후 본문 변조 | 401 |
| 올바른 서명, 이미 확정된 결제 (4회 반복) | 200, **payment 행 수 변화 없음** |

### 해결 방법

#### 1. 서명 검증 — Standard Webhooks 규격

PortOne V2 웹훅은 Standard Webhooks 규격을 따른다. SDK 없이 직접 구현했다.

```java
// 서명 대상 = "{webhook-id}.{webhook-timestamp}.{원문 body}"
byte[] expected = hmacSha256(base64Decode(secret.removePrefix("whsec_")), id + "." + ts + "." + body);

if (Math.abs(now - ts) > 5분) throw new InvalidWebhookException("재전송 의심");
for (String sig : header.split(" ")) {                 // 키 교체 기간에는 서명이 여러 개
    if (MessageDigest.isEqual(expected, decode(sig)))  // 상수 시간 비교 (타이밍 공격 방지)
        return;
}
throw new InvalidWebhookException("서명 불일치");
```

본문을 DTO로 받으면 역직렬화 과정에서 원문이 달라져 서명이 맞지 않는다. 그래서 `@RequestBody String`으로 받아 검증한 뒤 파싱한다.

#### 2. 결제 확정 로직 재사용 + 멱등 처리

웹훅의 역할을 "사용자가 결제 직후 브라우저를 닫아 결제 완료 요청이 오지 않은 경우의 보조 경로"로 정의했다. 그래서 `Transaction.Paid`만 처리하고, 사용자 결제 완료 요청과 같은 `confirm()`(PortOne 재조회 → 금액 검증 → 예약 확정 → 실패 시 PG 자동 취소)을 재사용한다.

```java
public void completeByWebhook(String paymentId) {
    if (reservationRepository.findByPayment_PortonePaymentId(paymentId).isPresent()) return; // 이미 확정
    PendingPayment order = pendingPayments.take(paymentId).orElse(null);                      // 원자적으로 꺼냄
    if (order == null) return;                                                                // 다른 요청이 처리 중
    confirm(order);
}
```

#### 3. 결제 완료 요청과 웹훅의 경쟁 처리

웹훅은 사용자 브라우저의 결제 완료 요청보다 먼저 도착하는 경우가 흔하다. 기존에는 대기 주문을 `GETDEL`로 꺼냈기 때문에, 웹훅이 먼저 꺼내 가면 뒤늦게 온 사용자 요청은 "결제 정보 없음" 오류를 받았다.

주문을 꺼내면서 "처리 중" 표시를 남기는 동작을 **Lua 스크립트로 원자 실행**하고, 사용자 요청은 표시가 사라질 때까지 최대 5초 기다렸다가 같은 결과를 돌려주도록 했다.

```lua
local v = redis.call('GET', KEYS[1])          -- payment:pending:{id}
if v then
  redis.call('DEL', KEYS[1])
  redis.call('SET', KEYS[2], '1', 'EX', 60)   -- payment:processing:{id}
end
return v
```

두 명령을 따로 보내면 그 사이에 들어온 요청이 주문도 표시도 보지 못하는 틈이 생긴다. Lua 스크립트는 Redis에서 한 번에 실행되므로 이 틈이 없다.

### 선택 근거

> 웹훅은 "누가 보냈는가(서명)"와 "몇 번 와도 같은 결과인가(멱등)"를 모두 보장해야 한다. PG사는 재전송을 전제로 설계되어 있어, 서명만 검증하고 매번 저장하면 정상 웹훅만으로도 데이터가 중복된다. 확정 로직을 사용자 요청과 공유하면 검증 규칙이 한 곳에만 있어 두 경로의 동작이 어긋날 여지가 없다.

---

## [2026-09-11] 리프레시 토큰 권한 상승 — 계정 종류 없는 토큰 저장소

### 문제 발견 배경

시스템관리자 화면을 손보면서 관리자 토큰 재발급 흐름을 확인했다. 참관객 계정의 리프레시 토큰을 박람회관리자용 재발급 API에 넣어 봤더니, **시스템관리자 권한의 AccessToken이 발급됐다.**

```
POST /api/admin/refresh   (X-Refresh-Token: 참관객 user001 의 리프레시 토큰)
→ 200 { accessToken: sub=1, role=ROLE_SYSTEM_ADMIN }
```

### 근본 원인 분석

**id 만 저장하는 토큰 저장소**  
참관객(`user`), 박람회관리자(`expo_admin`), 시스템관리자(`system_admin`)는 서로 다른 테이블이고 id가 모두 1부터 시작한다. 그런데 Redis 토큰 저장소는 `리프레시 토큰 → id`만 저장해, 토큰의 주인이 어느 테이블의 1번인지 구분할 정보가 없었다.

**재발급이 시스템관리자 테이블부터 조회**  
관리자 재발급 로직은 받은 id로 `system_admin`을 먼저 찾고, 없으면 `expo_admin`을 찾았다. 참관객 user_id=1의 토큰이 system_admin_id=1로 해석됐다.

```
[기존 흐름]
refresh token ──(Redis)──▶ id=1 ──▶ systemAdminRepository.findById(1) 성공 ──▶ ROLE_SYSTEM_ADMIN 발급
```

게다가 `/api/sys/**`는 SecurityConfig에서 `permitAll`이어서, 토큰 없이도 박람회 승인과 관리자 삭제 API를 호출할 수 있었다.

### 해결 방법

토큰을 계정 종류와 함께 저장하고, 재발급 API는 자기 종류의 토큰만 받는다.

```
refresh:{TYPE}:{id}        → 리프레시 토큰 (계정당 1개)
refresh:owner:{token}      → "{TYPE}:{id}"  (역인덱스)
```

```java
private RefreshTokenOwner requireOwner(String refreshToken, AccountType expected) {
    return tokenStore.findOwner(refreshToken)
            .filter(owner -> owner.type() == expected)   // 다른 종류의 토큰이면 거부
            .orElseThrow(() -> new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN));
}
```

- 재발급할 때마다 새 토큰을 발급하고 이전 토큰을 폐기한다(토큰 회전). `findOwner()`는 역인덱스가 남아 있어도 계정의 현재 토큰과 일치할 때만 유효로 본다.
- 시스템관리자 재발급 API(`/api/admin/sys/refresh`)가 없어 15분마다 강제 로그아웃되던 문제도 함께 해결했다.
- `/api/sys/**`는 `hasRole("SYSTEM_ADMIN")`으로 막았다.

### 검증

| 요청 | 결과 |
|---|---|
| 참관객 토큰 → 박람회관리자 재발급 | 401 |
| 참관객 토큰 → 시스템관리자 재발급 | 401 |
| 시스템관리자 토큰 → 참관객 재발급 | 401 |
| 시스템관리자 토큰 재발급 후 이전 토큰 재사용 | 401 |
| 토큰 없이 `/api/sys/stats` / 참관객 토큰으로 `/api/sys/stats` | 403 / 403 |

같은 시나리오를 `AdminAuthServiceImplTest`(단위)와 `RedisTokenStoreTest`(실제 Redis)에 회귀 테스트로 남겼다.

### 선택 근거

> JWT 대신 UUID 리프레시 토큰을 쓰는 설계라면 토큰의 신원은 전적으로 저장소에 달려 있다. 여러 사용자 테이블을 쓰는 시스템에서 "id"는 식별자가 아니라 "(계정 종류, id)" 쌍이 식별자다. 저장소 키에 계정 종류를 포함하는 것이, 재발급 로직마다 조회 순서를 조심하는 것보다 실수를 구조적으로 막는다.

---

## [2026-09-11] 결제 채널 Strategy 패턴 — `payMethod violates EQUALS("EASY_PAY")`

### 문제

본인 PortOne 채널(토스페이)로 결제 키를 바꾸자 결제창 호출이 실패했다.

```
결제 창 호출에 실패하였습니다. 요청을 파싱하는 과정에서 에러가 발생했습니다.
payMethod violates the rule EQUALS("EASY_PAY") (value="card")
```

### 원인

결제창 파라미터가 기존 채널(KG이니시스 카드)에 맞춰 프론트엔드에 `payMethod: "CARD"`로 하드코딩되어 있었다. 토스페이 같은 간편결제 전용 채널은 `EASY_PAY`와 `easyPay.easyPayProvider`를 요구한다. 결국 PG사를 바꿀 때마다 프론트 코드를 고쳐야 하는 구조였다.

### 해결

채널별 결제창 파라미터를 전략 객체로 분리하고, 서버 설정(`PORTONE_CHANNEL_TYPE`)으로 전략을 고른다. 프론트는 서버가 준 파라미터를 SDK에 그대로 전달한다.

```java
public interface PaymentChannelStrategy {
    PaymentChannelType type();                       // TOSSPAY, KAKAOPAY, TOSSPAYMENTS_CARD, INICIS_CARD
    PaymentMethodParams methodParams(Buyer buyer);   // payMethod, easyPay, customer
}

@Component
public class PaymentChannelRegistry {               // EnumMap 으로 등록, 설정값에 맞는 전략이 없으면 기동 실패
    public PaymentChannelStrategy current() { ... }
}
```

새 PG사는 전략 클래스 하나만 추가하면 된다(OCP). 같은 작업에서 결제 흐름도 함께 정리했다.

- **금액 위변조 방지:** `ready` 단계에서 금액을 서버가 계산해 Redis에 30분 보관한다. `complete` 단계에서는 PortOne 실제 결제 금액과 대조한다(기존: 클라이언트가 보낸 금액을 신뢰).
- **보상 트랜잭션:** 결제는 승인됐는데 예약 확정이 실패하면(정원 초과 등) DB는 롤백되고 PG 결제는 자동 취소된다. 이를 위해 예약 확정을 `ReservationCompletionService` 트랜잭션으로 분리했다.
- **메일 분리:** QR 메일은 커밋 후 비동기로 발송한다(`@TransactionalEventListener(AFTER_COMMIT)` + `@Async`). 메일 서버 장애가 결제를 롤백시키지 않는다.

---

## [2026-09-11] 시스템관리자 박람회 목록 전체 500 — 복수 카테고리

### 문제

시스템관리자 박람회 목록 API가 항상 500을 반환했다.

```
IncorrectResultSizeDataAccessException: Query did not return a unique result: 2 results were returned
```

### 원인

박람회와 카테고리는 N:M(`expo_category`)인데, 목록 변환 코드가 `Optional<ExpoCategory> findByExpo(expo)` 단건 조회를 썼다. 카테고리가 2개인 박람회가 하나라도 페이지에 있으면 목록 전체가 실패했다. 데모 데이터에 복수 카테고리 박람회가 들어가면서 드러났다.

### 수정

엔티티의 `expoCategories` 컬렉션에서 이름을 이어 붙이도록 바꿨다. 이 지연 로딩은 이후 `default_batch_fetch_size`로 한 번에 조회된다.

```java
String categories = expo.getExpoCategories().stream()
        .map(ec -> ec.getCategory().getName())
        .collect(Collectors.joining(", "));   // "IT/기술, 환경/에너지"
```

---

## [2026-09-10] JWT 필터 — 컨트롤러 예외까지 401로 둔갑

### 문제

정상 로그인 상태에서도 특정 화면에 들어가면 강제로 로그아웃됐다. 서버 로그에는 컨트롤러의 비즈니스 예외가 찍혔지만, 응답은 항상 `401 Unauthorized token`이었다.

### 원인

`JwtAuthenticationFilter`가 `filterChain.doFilter()`를 토큰 검증과 같은 `try` 블록 안에서 호출하고 있었다. 필터 뒤에서 실행되는 컨트롤러·서비스의 모든 예외가 이 `catch`로 올라와 401로 바뀌었다. 프론트 axios 인터셉터는 401을 받으면 토큰 재발급을 시도하고, 실패하면 로그아웃한다.

```java
// 구조만 남긴 단순화 코드
// Before — 요청 처리 전체가 try 안에 있음
try {
    validate(token); setAuthentication(token);
    filterChain.doFilter(request, response);   // 컨트롤러 예외도 여기서 잡힘
} catch (Exception e) {
    response.sendError(401, "Unauthorized token");
}

// After — 토큰 검증만 try 안에서, 요청 처리는 밖에서
try {
    validate(token); setAuthentication(token);
} catch (JwtException e) { ... 401 ... return; }
filterChain.doFilter(request, response);
```

같은 작업에서 인증 버그 세 가지를 함께 고쳤다. 참가업체(`ROLE_COMPANY_USER`) 인증 분기가 없어 참가업체 API가 전부 403이던 문제, 로그아웃 블랙리스트 키가 필터와 달라 로그아웃한 토큰이 계속 통과되던 문제, 관리자 프로필 응답에 비밀번호 해시가 포함되던 문제다.

---

## [2026-09-10] 모든 API 호출 실패 — 환경별 절대 URL과 프록시 대상

### 문제

백엔드는 정상인데 프론트에서 회원가입을 포함한 모든 API 호출이 실패했다.

### 원인

axios `baseURL`이 환경변수에 따라 절대 주소로 분기되어 있었다.

- Docker 빌드: 응답하지 않는 운영 도메인(`https://eventory.kro.kr:8080/api`)으로 요청했다.
- `npm run dev`: HTTPS로 뜬 로컬 서버에 `http://`로 요청했다.
- Nginx `proxy_pass https://localhost`: 컨테이너 안에서 `localhost`는 Nginx 컨테이너 자신이라 요청이 되돌아왔다.

### 수정

`baseURL`을 상대경로 `/api`로 고정해 요청이 항상 같은 출처로 가게 했다. 전달은 환경별 프록시가 맡는다. 개발 서버는 Vite 프록시(`API_PROXY_TARGET`, 기본 `https://localhost:8080`, self-signed 허용), Docker는 Nginx가 compose 서비스명(`https://eventory-server:8080`)으로 전달한다. 같은 출처 요청이 되면서 CORS 설정도 필요 없어졌다.

---

## [2026-05-16] 동시 예약 중복 — 비관적 락 + 낙관적 락 도입

### 문제 발견 배경

결제 완료(`/payment/complete`) 로직을 리뷰하던 중, 동시에 여러 사용자가 같은 박람회의 마지막 자리를 예약할 경우 정원을 초과한 예약이 생성될 수 있다는 취약점이 있었다.

기존 코드는 `Expo` 엔티티에 수용 인원 필드 자체가 없었고, 결제 성공 시 아무런 정원 체크 없이 `Reservation`을 저장하는 구조였다.

```
[기존 흐름]
complete() → PortOne 금액 검증 → Reservation 저장 (정원 체크 없음)
```

### 근본 원인 분석

두 가지 동시성 문제가 중첩되어 있었다.

**Race Condition (경쟁 조건)**  
트랜잭션 A와 B가 동시에 같은 Expo의 `reserved_count`를 읽으면 둘 다 정원이 남아있다고 판단하고, 각각 독립적으로 예약을 생성한다.

**Lost Update (갱신 손실)**  
A가 먼저 `reserved_count = 4 → 5`로 업데이트하더라도, B는 이미 읽은 `4`를 기준으로 `5`로 덮어쓴다. 실제로는 2명이 예약됐지만 DB에는 1명만 증가한 것으로 기록된다.

```
[경쟁 조건 시나리오]
트랜잭션 A  ── read(reserved=9) ──────────────── write(10) ── commit
트랜잭션 B  ── read(reserved=9) ─────────── write(10) ── commit
결과: 실제 예약 11명, DB reserved_count = 10 (정원 초과 + 갱신 손실)
```

### 정량적 분석

**락 미적용 상태의 초과 예약 발생 확률**  
정원이 1자리 남았을 때 N개의 동시 요청이 들어오면, 최대 N−1건의 초과 예약이 발생할 수 있다.  
가령 마지막 1자리에 10명이 동시 요청 시 → 10건 전부 예약 성공, 9건 초과.

| 동시 요청 수 | 예상 초과 예약 건수 (락 없음) | 초과 예약 건수 (락 적용) |
|:---:|:---:|:---:|
| 2 | 최대 1건 | **0건** |
| 5 | 최대 4건 | **0건** |
| 10 | 최대 9건 | **0건** |
| 50 | 최대 49건 | **0건** |

**실측: 비관적 락 직렬화 동작 확인 (로컬 Docker)**  
트랜잭션 A가 락을 300ms 동안 점유하는 동안 트랜잭션 B의 락 대기 시간을 측정했다.

```
트랜잭션 A: SELECT FOR UPDATE → SLEEP(0.3s) → UPDATE → COMMIT
트랜잭션 B: SELECT FOR UPDATE (대기) → 락 획득
트랜잭션 B 대기 시간: 268ms (A의 점유 시간과 일치)
```

두 트랜잭션이 직렬화되어 `reserved_count`가 정확히 1만 증가함을 확인했다.

**비경쟁 상태 응답 속도 (ab, n=500, c=50)**

| 지표 | 수치 |
|---|---|
| 중위값 (p50) | 5ms |
| p90 | 16ms |
| p95 | 18ms |
| p99 | 207ms |
| SELECT FOR UPDATE 추가 오버헤드 (비경쟁) | < 0.2ms |

비경쟁 상태에서는 `SELECT FOR UPDATE`가 일반 SELECT 대비 거의 차이가 없다. 성능 저하는 동시 요청이 같은 행을 경쟁할 때만 발생하며, 이는 정확히 막아야 하는 상황이다.

### 해결 방법

#### 1. Expo 엔티티 — 도메인 필드 및 메서드 추가

정원 관련 비즈니스 규칙(정원 초과 불가)을 엔티티 내부에 캡슐화했다. 서비스 레이어에서 직접 필드를 건드리지 않고 도메인 메서드를 통해서만 변경한다.

```java
@Column(name = "max_capacity", nullable = false)
private int maxCapacity;

@Column(name = "reserved_count", nullable = false)
private int reservedCount = 0;

@Version  // 낙관적 락: UPDATE 충돌 시 OptimisticLockException
@Column(name = "version")
private Long version;

public void increaseReservedCount(int people) {
    if (this.reservedCount + people > this.maxCapacity) {
        throw new CustomException(CustomErrorCode.EXPO_CAPACITY_EXCEEDED); // 409
    }
    this.reservedCount += people;
}

public void decreaseReservedCount(int people) {
    this.reservedCount = Math.max(0, this.reservedCount - people);
}
```

#### 2. ExpoRepository — SELECT FOR UPDATE 쿼리 추가

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
@Query("SELECT e FROM expo e WHERE e.expoId = :expoId")
Optional<Expo> findByIdWithLock(Long expoId);
```

`lock.timeout = 3000ms`로 설정해 락 대기가 3초를 초과하면 즉시 예외를 발생시킨다. 이를 통해 서버 자원이 무한 대기 스레드에 묶이는 것을 방지한다.

> **정정 (2026-09-12)**: 측정해 보니 이 힌트는 Hibernate MySQL 방언에서 SQL에 반영되지 않아, 실제로는 InnoDB 기본값인 50초까지 대기했다. 지금은 커넥션 초기화 SQL(`innodb_lock_wait_timeout = 3`)로 제한한다. 맨 위 [2026-09-12] 항목 참고.

#### 3. PaymentServiceImpl — 락 적용

```java
// complete(): 락 획득 → 정원 확인 → 예약 생성 (원자적 처리)
Expo expo = expoRepository.findByIdWithLock(req.getExpoId())
    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
expo.increaseReservedCount(req.getPeople()); // 정원 초과 시 409

// refund(): 락 획득 → 정원 복구
Expo expo = expoRepository.findByIdWithLock(res.getExpo().getExpoId());
expo.decreaseReservedCount(res.getPeople());
```

### 이중 방어 전략

비관적 락 하나만으로도 충분하지만, `@Version`을 함께 사용해 어드민 직접 수정 등 비관적 락이 적용되지 않는 경로까지 방어한다.

| 계층 | 메커니즘 | 담당 역할 |
|---|---|---|
| DB 레벨 | `SELECT FOR UPDATE` (비관적 락) | 결제 트랜잭션 간 동시 접근 직렬화 |
| ORM 레벨 | `@Version` (낙관적 락) | 비관적 락 우회 경로 Lost Update 방지 |
| 도메인 레벨 | `increaseReservedCount()` | 정원 초과 불가 규칙 캡슐화 |

### 선택 근거

> 결제는 충돌 가능성이 높은 hot path다. 낙관적 락만 쓰면 충돌 시 트랜잭션이 롤백되고 클라이언트는 이미 결제된 상태에서 예약 실패를 받을 수 있다. 비관적 락은 락 획득 시점에 직렬화를 보장하므로, 정확한 선착순 처리가 필요한 결제 완료 흐름에 적합하다.

### DB 마이그레이션

기존 운영 DB에 컬럼을 추가해야 한다. `ddl-auto: update`로 서버 재기동 시 자동 추가되나, 명시적 마이그레이션 권장.

```sql
ALTER TABLE expo
  ADD COLUMN max_capacity INT NOT NULL DEFAULT 0,
  ADD COLUMN reserved_count INT NOT NULL DEFAULT 0,
  ADD COLUMN version BIGINT DEFAULT 0;
```

---

## [2026-05-16] Docker 빌드 — MySQL 서비스 추가 및 환경변수 오버라이드

### 문제

`docker compose up` 실행 시 Spring Boot 서버가 DB 연결에 실패하는 문제가 있었다.

```
HikariPool-1 - Exception during pool initialization.
com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure
```

### 원인

`application-prod.yml`의 DB URL이 개발자 로컬 LAN IP(`192.168.0.17`)로 하드코딩되어 있었다. Docker 컨테이너 내부 네트워크에서는 호스트 LAN IP에 직접 접근할 수 없어 매번 연결 타임아웃이 발생했다.

### 해결

`docker-compose.yml`에 MySQL 서비스를 추가하고, 환경변수로 Spring의 DB/Redis 설정을 오버라이드했다. `depends_on` + healthcheck 조건으로 MySQL이 완전히 준비된 후 Spring Boot가 기동되도록 순서를 보장했다.

```yaml
eventory-db:
  image: mysql:8.0
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uEventoryUser", "-p1234"]
    interval: 5s
    retries: 10

eventory-server:
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://eventory-db:3306/Eventory
    - SPRING_REDIS_HOST=redis
  depends_on:
    eventory-db:
      condition: service_healthy
```

---

## [2026-05-16] Spring Security — JWT role 클레임 ROLE_ 접두사 누락

### 문제

로그인은 성공하지만 인증이 필요한 모든 API(`/api/user/me` 등)에서 403이 반환되는 문제가 있었다.

### 원인

`AuthServiceImpl`이 JWT에 role을 `GENERAL_USER`로 저장했지만, Spring Security는 `hasRole("GENERAL_USER")` 조건을 평가할 때 내부적으로 `ROLE_GENERAL_USER`를 기대한다. `ROLE_` 접두사 규칙을 따르지 않아 권한 검사가 항상 실패했다.

### 수정

```java
// Before — GENERAL_USER 저장 → ROLE_GENERAL_USER 기대 → 403
String accessToken = jwtTokenProvider.createAccessToken(userId, userType.getName());

// After
String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_" + userType.getName());
```

---

## [2026-05-16] JPA @OneToOne → @ManyToOne 수정 (UNIQUE 제약 오류)

### 문제

첫 번째 사용자 가입은 성공하지만 두 번째 사용자 가입 시 500 에러가 발생하는 문제가 있었다.

```
SQLIntegrityConstraintViolationException:
Duplicate entry '4' for key 'UKglsbqaoaixrd0eddr7qndn17b'
```

### 원인

`User`, `ExpoAdmin`, `SystemAdmin` 엔티티에서 `UserType` 관계를 `@OneToOne`으로 매핑한 것이 문제였다. JPA는 `@OneToOne`의 외래키 컬럼에 자동으로 UNIQUE 제약을 생성하는데, 이 때문에 동일한 `user_type`(예: typeId=4, GENERAL_USER)을 가진 두 번째 사용자는 가입 자체가 불가능했다. UserType은 역할을 나타내는 코드 테이블이므로 당연히 여러 사용자가 동일한 type을 가져야 한다.

### 수정

세 엔티티 모두 `@OneToOne` → `@ManyToOne`으로 변경하고, DB에서 UNIQUE 제약을 제거한 뒤 일반 인덱스와 FK를 재추가했다.

```java
// Before — type_id에 UNIQUE 제약 생성
@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "type_id", nullable = false)
private UserType type;

// After
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "type_id", nullable = false)
private UserType type;
```
