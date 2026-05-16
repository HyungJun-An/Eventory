# Eventory 트러블슈팅 & 성능 개선 기록

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
