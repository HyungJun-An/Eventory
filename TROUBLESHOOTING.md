# Eventory 트러블슈팅 & 성능 개선 기록

---

## [2026-05-16] 동시 예약 중복 문제 — 비관적 락 + 낙관적 락 도입

### 문제 발견 배경
결제 완료(`/payment/complete`) 처리 로직을 리뷰하던 중, 동시에 여러 사용자가 같은 박람회에 예약 요청을 보낼 경우 정원을 초과한 예약이 생성될 수 있다는 취약점을 발견.

기존 코드는 `Expo` 엔티티에 수용 인원 필드 자체가 없었고, 결제 성공 시 아무런 정원 체크 없이 `Reservation`을 생성하는 구조였다.

```
[기존 흐름]
complete() → PortOne 금액 검증 → Reservation 저장 (정원 체크 없음)
```

### 근본 원인 분석
- **Race Condition**: 트랜잭션 A, B가 동시에 같은 Expo의 잔여 인원을 읽으면, 둘 다 잔여 인원이 있다고 판단하고 각각 예약을 생성함
- **Lost Update**: A가 먼저 `reservedCount = 5 → 6`으로 업데이트해도 B는 이미 읽은 `5`를 기준으로 `6`으로 덮어씀 → 실제로는 7명 예약됐지만 DB에는 6명으로 기록

### 해결 방법

#### 1. Expo 엔티티 — 도메인 필드 및 메서드 추가

```java
// 수용 인원 관리 핵심 필드
@Column(name = "max_capacity", nullable = false)
private int maxCapacity;

@Column(name = "reserved_count", nullable = false)
private int reservedCount = 0;

// 낙관적 락: 동시 UPDATE 충돌 시 OptimisticLockException 발생
@Version
@Column(name = "version")
private Long version;

public void increaseReservedCount(int people) {
    if (this.reservedCount + people > this.maxCapacity) {
        throw new CustomException(CustomErrorCode.EXPO_CAPACITY_EXCEEDED);
    }
    this.reservedCount += people;
}

public void decreaseReservedCount(int people) {
    this.reservedCount = Math.max(0, this.reservedCount - people);
}
```

#### 2. ExpoRepository — 비관적 락 쿼리 추가

```java
// SELECT e FROM expo e WHERE e.expoId = :expoId FOR UPDATE (MySQL 기준)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
@Query("SELECT e FROM expo e WHERE e.expoId = :expoId")
Optional<Expo> findByIdWithLock(Long expoId);
```

`lock.timeout = 3000ms`: 락 대기가 3초를 초과하면 예외 발생 → 무한 대기 방지

#### 3. PaymentServiceImpl — 락 적용

```java
// complete(): 비관적 락으로 조회 후 정원 증가
Expo expo = expoRepository.findByIdWithLock(req.getExpoId())
    .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
expo.increaseReservedCount(req.getPeople()); // 정원 초과 → 409 Conflict

// refund(): 환불 시 정원 복구
Expo expo = expoRepository.findByIdWithLock(res.getExpo().getExpoId())
    .orElseThrow(...);
expo.decreaseReservedCount(res.getPeople());
```

### 이중 방어 전략

| 계층 | 메커니즘 | 역할 |
|---|---|---|
| DB 레벨 | `SELECT FOR UPDATE` (비관적 락) | 동시 트랜잭션의 같은 행 동시 수정 차단 |
| ORM 레벨 | `@Version` (낙관적 락) | 비관적 락 우회 시나리오(다른 경로 업데이트) 최후 방어선 |
| 도메인 레벨 | `increaseReservedCount()` | 비즈니스 규칙(정원 초과 불가)을 엔티티 내부에 캡슐화 |

### 비관적 락 vs 낙관적 락 선택 기준

**결제 완료 시 비관적 락을 선택한 이유:**
- 결제는 충돌 가능성이 높은 hot path (인기 박람회 = 동시 다수 요청)
- 낙관적 락만 쓰면 충돌 시 클라이언트가 결제 성공 응답을 받고도 예약이 롤백될 수 있음
- 비관적 락은 `SELECT FOR UPDATE`로 직렬화하여 정확한 선착순 보장

**`@Version`을 함께 유지하는 이유:**
- 비관적 락이 걸리지 않는 경로(어드민 수동 수정 등)에서 Lost Update 방지
- 감사(audit) 목적: 몇 번 업데이트됐는지 추적 가능

### 에러 코드 추가

```
EXPO_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "E005", "박람회 수용 인원이 초과되었습니다")
→ HTTP 409 Conflict 반환
```

### 영향 범위

| 파일 | 변경 내용 |
|---|---|
| `common/entity/Expo.java` | maxCapacity, reservedCount, @Version 필드 + 도메인 메서드 2개 |
| `common/repository/ExpoRepository.java` | `findByIdWithLock()` 추가 |
| `common/exception/CustomErrorCode.java` | E005 에러 코드 추가 |
| `payment/service/PaymentServiceImpl.java` | complete()/refund()에 락 적용 |

### DB 마이그레이션 필요 사항

기존 운영 DB에 아래 컬럼을 추가해야 한다:

```sql
ALTER TABLE expo
  ADD COLUMN max_capacity INT NOT NULL DEFAULT 0,
  ADD COLUMN reserved_count INT NOT NULL DEFAULT 0,
  ADD COLUMN version BIGINT DEFAULT 0;
```

---

## [2026-05-16] Docker 빌드 환경 — MySQL 서비스 추가 및 환경변수 오버라이드

### 문제
`application-prod.yml`의 DB URL이 `192.168.0.17` LAN IP로 하드코딩됨  
→ Docker 내부 네트워크에서 호스트 LAN IP 접근 불가 → HikariPool 연결 타임아웃

### 해결
`docker-compose.yml`에 MySQL 서비스 추가 + 환경변수 오버라이드:

```yaml
eventory-db:
  image: mysql:8.0
  environment:
    MYSQL_DATABASE: Eventory
    MYSQL_USER: EventoryUser
    MYSQL_PASSWORD: "1234"
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-uEventoryUser", "-p1234"]

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
로그인 후 `/me` API 호출 시 403 반환

### 원인
`AuthServiceImpl`이 JWT에 `GENERAL_USER`를 저장했지만, Spring Security는 `hasRole("GENERAL_USER")` 해석 시 내부적으로 `ROLE_GENERAL_USER`를 기대함

### 수정
```java
// Before (버그):
String accessToken = jwtTokenProvider.createAccessToken(userId, userType.getName());

// After:
String accessToken = jwtTokenProvider.createAccessToken(userId, "ROLE_" + userType.getName());
```

---

## [2026-05-16] JPA @OneToOne → @ManyToOne 수정 (UNIQUE 제약 오류)

### 문제
두 번째 사용자 가입 시 `Duplicate entry for key 'UKglsbqaoaixrd0eddr7qndn17b'`

### 원인
`User`, `ExpoAdmin`, `SystemAdmin` 엔티티에서 `UserType`을 `@OneToOne`으로 매핑  
→ JPA가 `type_id` 컬럼에 UNIQUE 제약을 자동 생성  
→ 같은 `user_type`을 가진 두 번째 사용자 가입 불가

### 수정
세 엔티티 모두 `@OneToOne` → `@ManyToOne` 변경  
DB에서 UNIQUE 제약 제거 후 일반 인덱스 + FK 재추가
