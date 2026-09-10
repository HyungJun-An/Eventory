# 결제·환불·QR 시나리오 테스트

> 작성일: 2026-09-10 · 대상 브랜치: `refactor/backend-quality`
> "코드상 예상" 열은 **코드 분석으로 예측한 결과**다. 실제 테스트로 확인한 뒤 "결과" 열을 채운다.
> ✅ = 기대대로 동작할 것으로 예상 · ❌ = 결함으로 예상 · ⚠️ = 부분 동작/확인 필요

---

## 1. 결제 흐름 (현재 코드 기준)

```
[박람회 상세 /expos/:id] ──예약하기──▶ /payment?expoId=N
                                         │  ※ PaymentCheckout은 쿼리를 무시하고
                                         │    userId=11, expoId=3, 1,000원 고정값 사용
                                         ▼
① POST /api/payment/ready          서버: paymentId 생성, storeId/channelKey 반환
                                     (금액은 클라이언트가 보낸 totalAmount 그대로)
② PortOne.requestPayment()         브라우저 SDK로 결제창 → PG 승인
③ POST /api/payment/complete       서버: PortOne 결제 단건 조회
                                     → 상태 PAID + 금액 == expectedAmount(클라이언트 값) 검증
                                     → payment 저장
                                     → expo 비관적 락 조회 → reserved_count 증가(정원 초과 시 409)
                                     → reservation 저장
                                     → qr_code + ticket 저장, QR 메일 발송(Gmail SMTP)
                                     ※ ③ 전체가 한 트랜잭션: 중간 실패 시 DB 롤백, PG 결제는 승인 상태로 남음
④ /payment/reservation/:id         예약 상세(라우터 state로만 데이터 전달)
⑤ POST /api/payment/{reservationId}/refund
                                     서버: PortOne 전액 취소 → payment REFUNDED,
                                     reservation CANCELLED, reserved_count 감소
⑥ POST /api/checkin/scan           QR 토큰 HMAC 검증 → ticket 사용 처리, 체크인 로그
```

**관련 파일**

| 구분 | 파일 |
|---|---|
| 프론트 결제 | `eventory-client/src/payment/PaymentCheckout.jsx`, `PaymentRedirect.jsx` |
| 프론트 환불 | `eventory-client/src/payment/TestReservationDetail.jsx`, `RefundPage.jsx` |
| 프론트 관리자 환불 | `eventory-client/src/expoAdmin/sections/refund/RefundElement.jsx` |
| 백엔드 결제 | `payment/controller/PaymentController.java`, `payment/service/PaymentServiceImpl.java`, `PortOneClient.java` |
| 백엔드 웹훅 | `payment/controller/PortOneWebhookController.java` |
| 백엔드 QR | `qr/service/QrService.java`, `qr/conroller/CheckinController.java` |
| 백엔드 관리자 환불 | `expoAdmin/service/SalesAdminServiceImpl.java` (`findAllRefunds`, `updateRefundStatus`) |

**구현 상태 요약**

| 기능 | 상태 |
|---|---|
| PC 결제 → 예약 생성 → QR 메일 | 구현됨 (데모 고정값 사용) |
| 박람회 상세 → 결제 연결 | ❌ `expoId` 쿼리를 결제 화면이 사용하지 않음 |
| 결제 금액 서버 검증 | ❌ `expo.price`를 사용하지 않음. 클라이언트가 보낸 금액끼리만 비교 |
| 모바일 리디렉션 결제 | ❌ `sessionStorage` 값을 저장하는 코드가 없어 폴백값(userId=1, expoId=101) 사용 |
| 사용자 환불 | 구현됨. 즉시 전액 환불(화면에 표시된 90%/50% 정책 미적용), 인증 없음 |
| 관리자 환불 승인/반려 | ❌ `Refund` 행을 생성하는 코드가 없음. 승인해도 PortOne 환불 호출 없음 |
| 웹훅 | ⚠️ 서명 검증 없음, 예약과 연결 안 된 `payment` 행만 추가 저장 |
| QR 체크인 | ❌ 2026-09-10 라이브 테스트에서 단건 스캔도 500 (`CheckInLog` `@MapsId` 매핑 오류) |

---

## 2. 테스트 준비

### 2-1. 환경
- [ ] `docker compose up -d --build`로 전체 스택 기동
- [ ] 필수 초기 데이터(`user_type`, `category`) 주입 — 루트 `CLAUDE.md` 참고
- [ ] **본인 PortOne 테스트 채널 키**를 `.env`로 주입 (주입하지 않으면 yml 기본값인 기존 팀 상점으로 결제됨)
- [ ] PG 테스트 모드 확인 — PG사에 따라 실제 승인 후 자동 취소되는 방식일 수 있으므로 소액 결제 권장
- [ ] QR 메일 발송용 SMTP 계정이 유효한지 확인 (무효면 PAY-01이 롤백됨 → PAY-12 참고)

### 2-2. 테스트 데이터
결제 화면이 `userId=11`, `expoId=3`을 고정 사용하므로 같은 ID로 데이터를 만든다.
(결제 API는 로그인 없이 `userId`를 body로 받으므로 로그인 가능한 비밀번호는 필요 없다 — SEC-04 결함 참고)

```sql
-- 이메일은 QR 메일을 받을 본인 주소로 변경
INSERT INTO user (user_id, customer_id, name, email, password, phone, gender, birth, type_id, created_at, updated_at)
VALUES (11, 'paytest', '결제테스터', 'YOUR_EMAIL@example.com', 'x', '01000000000', 'male', '1995-01-01', 4, NOW(), NOW());

-- 가격 10,000원 / 정원 2명 (결제 화면은 1,000원을 청구 → SEC-01)
INSERT INTO expo (expo_id, title, image_url, description, start_date, end_date, location, visibility,
                  created_at, updated_at, display_start_date, display_update_date, status, price,
                  max_capacity, reserved_count, version)
VALUES (3, '결제테스트 박람회', 'x', '결제 시나리오용', '2026-12-01', '2026-12-31', '코엑스', 1,
        NOW(), NOW(), NOW(), NOW(), 'APPROVED', 10000, 2, 0, 0);
```

### 2-3. 결과 확인 쿼리
```sql
SELECT expo_id, price, max_capacity, reserved_count, version FROM expo WHERE expo_id = 3;
SELECT payment_id, amount, method, status, portone_payment_id FROM payment ORDER BY payment_id DESC LIMIT 5;
SELECT reservation_id, code, status, people, payment_id FROM reservation ORDER BY reservation_id DESC LIMIT 5;
SELECT q.qr_id, q.status, t.ticket_id, t.status AS used FROM qr_code q JOIN ticket t ON t.qr_id = q.qr_id ORDER BY q.qr_id DESC LIMIT 5;
SELECT * FROM refund;
SELECT * FROM checkin_log;
```
PortOne 관리자 콘솔의 결제 내역에서 PG 쪽 상태(승인/취소)도 함께 확인한다.

---

## 3. 시나리오

### A. 정상 흐름

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| PAY-01 | PC 카드 결제 성공 | `/payment` → 결제하기 → 테스트 카드 결제 | payment 1건(PAID), reservation 1건(RESERVED), qr_code+ticket 생성, `reserved_count` 0→1, QR 메일 수신, 예약 상세로 이동 | ✅ (단, 1,000원 결제로 성공 — SEC-01) | |
| PAY-02 | 예약 상세 새로고침 | PAY-01 직후 예약 상세 화면에서 F5 | 예약 정보 유지 | ❌ "예약 데이터가 없습니다" (라우터 state에만 의존) | |
| PAY-03 | 박람회 상세에서 결제 진입 | `/expos/3` → 예약하기 | 선택한 박람회·가격으로 결제 화면 표시 | ❌ 쿼리 무시, 항상 expoId=3 / 1,000원 | |
| QR-01 | QR 체크인 | PAY-01의 `qr_code.data` 값을 `POST /api/checkin/scan` `{"token":"..."}`로 전송 | 200 `OK`, `ticket.status=1`, `qr_code.status=CHECKED_IN`, checkin_log 1건 | ❌ 500 (`CheckInLog` 매핑 오류) | |
| QR-02 | 같은 QR 재스캔 | QR-01 성공 후 같은 토큰 재전송 | 400 `ALREADY_CHECKED_IN` | ⚠️ QR-01 수정 후 확인 | |
| REF-01 | 사용자 환불 | 예약 상세 → 환불 요청 → 사유 입력·동의 → 환불 | PortOne 취소, payment REFUNDED, reservation CANCELLED, `reserved_count` 1→0 | ✅ | |

### B. 결제 실패·중단

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| PAY-10 | 결제창에서 취소 | 결제창을 닫거나 취소 | 오류 메시지 표시, DB 변화 없음 | ✅ | |
| PAY-11 | 존재하지 않는 paymentId로 complete | `POST /api/payment/complete` (`paymentId:"fake"`) | 4xx + 에러 코드 | ❌ 500 (PortOne 404 예외 미처리, 라이브 테스트로 확인됨) | |
| PAY-12 | 메일 발송 실패 | SMTP 비밀번호를 틀리게 설정 후 PAY-01 | 결제·예약은 성공, 메일만 재시도/실패 기록 | ❌ 예약 전체 롤백, PG 결제는 승인 상태로 남음(자동 환불 없음) | |
| PAY-20 | 모바일 리디렉션 결제 | 모바일 브라우저(또는 기기 에뮬레이션)로 결제 → `/payment/redirect` 복귀 | 결제한 사용자·박람회로 예약 생성 | ❌ `sessionStorage` 미설정 → userId=1, expoId=101 폴백으로 complete 실패 | |

### C. 검증·보안

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| SEC-01 | 결제 금액 위변조 | 가격 10,000원 박람회를 1,000원으로 결제 (PAY-01이 그대로 이 케이스) | 금액 불일치로 거부 + 자동 취소 | ❌ 성공 (서버가 `expo.price × people`로 검증하지 않음) | |
| SEC-02 | expectedAmount 불일치 | PAY-01 결제 후 complete를 `expectedAmount: 2000`으로 재현 | 거부 + PG 결제 자동 취소 | ⚠️ 거부는 되나 500, 자동 취소 없음 | |
| SEC-03 | 같은 paymentId로 complete 2회 | PAY-01 성공 후 같은 body로 complete 재호출 | 멱등 처리(기존 결과 반환) 또는 409 | ⚠️ `portone_payment_id` UNIQUE 제약으로 막히지만 500 | |
| SEC-04 | 비로그인·타인 명의 결제 확정 | 토큰 없이 complete 호출, `userId`를 다른 사용자로 지정 | 401 | ❌ 인증 없이 처리, body의 `userId`로 예약 생성 | |
| SEC-05 | 비로그인·타인 예약 환불 | 토큰 없이 `POST /api/payment/{다른 사람 reservationId}/refund` | 401/403 | ❌ 누구나 어떤 예약이든 환불 가능 | |

```bash
# SEC-05 예시 (로그인 없이 호출)
curl -k -X POST https://localhost:8080/api/payment/1/refund \
  -H 'Content-Type: application/json' -d '{"reason":"test"}'
```

### D. 정원·동시성

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| CAP-01 | 정원 초과 결제 | 정원 2명 박람회에서 결제 3회 | 3번째 결제는 결제창 호출 전(ready 단계)에 차단 | ⚠️ PG 승인 후 complete에서 409(E005), 결제는 승인 상태로 남고 자동 환불 없음 | |
| CAP-02 | 동시 결제 | 남은 자리 1개에 동시 N건 complete | 1건만 성공, `reserved_count`가 정원 이하 | ⚠️ 비관적 락 적용됨. PortOne 호출을 Mock한 JUnit 통합 테스트로 검증 필요 (브라우저로는 재현 어려움) | |
| CAP-03 | 환불 후 자리 복구 | 정원 가득 찬 상태에서 1건 환불 후 재결제 | 재결제 성공 | ✅ | |

### E. 환불 정책·관리자

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| REF-02 | 이미 환불된 예약 재환불 | REF-01 후 같은 예약 환불 재요청 | 200, PortOne 재호출 없음 | ✅ 멱등 처리 코드 있음 | |
| REF-03 | 체크인한 티켓 환불 | QR 체크인 후 환불 요청 | 거부 | ❌ 체크 로직 없음 (QR-01 수정 후 확인) | |
| REF-04 | 환불 정책 적용 | 박람회 시작 5일 전 환불 | 화면 문구대로 50% 환불 | ❌ 항상 전액 환불 | |
| REF-05 | 관리자 환불 목록 | 사용자 환불 후 박람회관리자 `/admin/refund` 확인 | 환불 요청 목록에 표시 | ❌ 항상 빈 목록 (`Refund` 생성 코드 없음) | |
| REF-06 | 관리자 환불 승인 | 관리자 화면에서 승인 | PortOne 환불 실행 | ❌ 상태값만 변경, 실제 환불 없음 | |

### F. 웹훅 (선택)

로컬 서버는 PortOne이 접근할 수 없으므로 ngrok 등으로 외부 URL을 열고 콘솔에 웹훅 URL을 등록해야 한다.

| ID | 시나리오 | 절차 | 기대 결과 | 코드상 예상 | 결과 |
|---|---|---|---|---|---|
| WH-01 | 결제 완료 웹훅 수신 | PAY-01 수행 시 웹훅 수신 | 기존 결제 상태만 동기화 | ⚠️ 서명 검증 없이, `portone_payment_id` 없는 `payment` 행이 추가로 저장됨 | |
| WH-02 | 위조 웹훅 | 서명 없이 `POST /api/portone-webhook` | 401 | ❌ 서명 검증 없음 (단, PortOne 재조회로 PAID가 아니면 무시됨) | |

---

## 4. 우선 수정 권장 순서

1. **SEC-01, SEC-04, SEC-05** — 금액 위변조·타인 명의 결제·타인 예약 환불 (돈과 직결)
2. **PAY-12, CAP-01, SEC-02** — PG 승인 후 서버 실패 시 자동 취소(보상 트랜잭션), 메일 발송을 커밋 이후로 분리
3. **QR-01** — 체크인 매핑 오류 (핵심 기능)
4. **PAY-03, PAY-20, PAY-02** — 화면 흐름 연결 (실제 박람회·로그인 사용자 사용)
5. **REF-04 ~ REF-06** — 환불 정책과 관리자 승인 흐름 설계 (사용자 즉시 환불 vs 관리자 승인 중 하나로 정리)
6. **CAP-02** — 동시성 통합 테스트 작성
