-- =====================================================================
-- Eventory 데모 데이터
-- - DemoDataRunner 가 서버 기동 시 expo 테이블이 비어 있을 때만 실행한다.
-- - 날짜는 CURDATE()/NOW() 기준 상대값 → 언제 실행해도 진행 중/예정 박람회가 생기고 최근 통계가 채워진다.
-- - 모든 데모 계정 비밀번호: Eventory1234!
-- - MySQL 8 문법 사용 (WITH RECURSIVE, 임시 테이블)
-- =====================================================================


-- 1. 기초 코드 ----------------------------------------------------------

INSERT IGNORE INTO user_type (type_id, name) VALUES
  (1, 'SYSTEM_ADMIN'), (2, 'EXPO_ADMIN'), (3, 'COMPANY_USER'), (4, 'GENERAL_USER');

-- 이름이 이미 있는 카테고리는 건너뜀 (name 에 UNIQUE 제약이 없으므로 직접 확인)
INSERT INTO category (name)
SELECT v.name
FROM (SELECT 'IT/기술' AS name UNION ALL SELECT '식품/농업' UNION ALL SELECT '환경/에너지'
      UNION ALL SELECT '의료/바이오' UNION ALL SELECT '패션/뷰티' UNION ALL SELECT '자동차/모빌리티'
      UNION ALL SELECT '교육' UNION ALL SELECT '기타') v
WHERE NOT EXISTS (SELECT 1 FROM category c WHERE c.name = v.name);


-- 2. 계정 ---------------------------------------------------------------

-- BCrypt('Eventory1234!')
SET @pw = '$2a$10$3aTus.ytMqrkEm4U5NSRge7pUMuNKEUkeYlYMpyjdIvm3xiGglyYK';

INSERT INTO system_admin (type_id, customer_id, password, name, email, phone)
VALUES (1, 'sysadmin', @pw, '플랫폼 운영자', 'ops@example.com', '0215880000');

INSERT INTO expo_admin (type_id, customer_id, password, name, email, phone, created_at, updated_at) VALUES
  (2, 'expoadmin1', @pw, '김지훈', 'jihoon.kim@example.com',  '01023456781', NOW() - INTERVAL 240 DAY, NOW() - INTERVAL 240 DAY),
  (2, 'expoadmin2', @pw, '이서연', 'seoyeon.lee@example.com', '01023456782', NOW() - INTERVAL 220 DAY, NOW() - INTERVAL 220 DAY),
  (2, 'expoadmin3', @pw, '박민재', 'minjae.park@example.com', '01023456783', NOW() - INTERVAL 210 DAY, NOW() - INTERVAL 210 DAY),
  (2, 'expoadmin4', @pw, '최유나', 'yuna.choi@example.com',   '01023456784', NOW() - INTERVAL 200 DAY, NOW() - INTERVAL 200 DAY);

-- 승인 대기 박람회용 임시 관리자: 실제 신청 흐름(ExpoAdminServiceImpl.createExpo)과 동일하게 "아이디 = 박람회 제목"
-- → 시스템관리자가 승인하면 SystemAdminService 가 이 계정을 찾아 정식 아이디/비밀번호를 발급한다
INSERT INTO expo_admin (type_id, customer_id, password, name, email, phone, created_at, updated_at) VALUES
  (2, '2026 스마트시티 & 프롭테크 엑스포', 'password', '신청 담당자', 'apply1@example.com', '01055501001', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
  (2, '반려동물 라이프 페어',              'password', '신청 담당자', 'apply2@example.com', '01055501002', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- 참관객 60명 (user001 ~ user060), 마지막 3명은 오늘 가입 → 시스템관리자 "오늘 신규 가입" 지표
INSERT INTO `user` (type_id, customer_id, name, email, password, phone, birth, gender, created_at, updated_at)
WITH RECURSIVE seq (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 60),
people AS (
  SELECT n,
         MOD((n - 1) * 7, 20) AS g,  -- 이름 인덱스 (짝수 = 남성)
         IF(n > 57, NOW() - INTERVAL (30 + n) MINUTE,
                    NOW() - INTERVAL (5 + MOD(CRC32(CONCAT('join', n)), 150)) DAY) AS joined_at
  FROM seq
)
SELECT 4,
       CONCAT('user', LPAD(n, 3, '0')),
       CONCAT(ELT(MOD(n - 1, 20) + 1, '김','이','박','최','정','강','조','윤','장','임','한','오','서','신','권','황','안','송','전','홍'),
              ELT(g + 1, '민준','서윤','도윤','서연','하준','지우','시우','하은','주원','지유',
                         '지호','채원','예준','수아','유준','지안','건우','윤서','현우','다은')),
       CONCAT('user', LPAD(n, 3, '0'), '@example.com'),
       @pw,
       CONCAT('010', LPAD(MOD(CRC32(CONCAT('phone', n)), 100000000), 8, '0')),
       DATE('1975-01-01') + INTERVAL MOD(CRC32(CONCAT('birth', n)), 10950) DAY,
       IF(MOD(g, 2) = 0, 'male', 'female'),
       joined_at, joined_at
FROM people;

-- 참가업체 8곳 (company01 ~ company08), name = 부스 담당자
INSERT INTO `user` (type_id, customer_id, name, email, password, phone,
                    company_name_kr, company_name_eng, ceo_name_kr, ceo_name_eng, company_address, registration_num,
                    created_at, updated_at) VALUES
  (3, 'company01', '송하린', 'company01@example.com', @pw, '01061110001', '(주)뉴로웨이브',   'NeuroWave Inc.',   '정태윤', 'Taeyoon Jung', '서울특별시 강남구 테헤란로 427',     '214-88-10231', NOW() - INTERVAL 150 DAY, NOW() - INTERVAL 150 DAY),
  (3, 'company02', '윤채원', 'company02@example.com', @pw, '01061110002', '그린셀에너지(주)', 'GreenCell Energy', '한도윤', 'Doyun Han',    '부산광역시 해운대구 센텀중앙로 97',  '617-81-45520', NOW() - INTERVAL 140 DAY, NOW() - INTERVAL 140 DAY),
  (3, 'company03', '임지아', 'company03@example.com', @pw, '01061110003', '팜투테이블(주)',   'FarmToTable Co.',  '배서준', 'Seojun Bae',   '세종특별자치시 한누리대로 250',      '320-86-01987', NOW() - INTERVAL 130 DAY, NOW() - INTERVAL 130 DAY),
  (3, 'company04', '조현우', 'company04@example.com', @pw, '01061110004', '메디큐브랩',       'MediCube Lab',     '서예린', 'Yerin Seo',    '대전광역시 유성구 대학로 99',        '305-87-66012', NOW() - INTERVAL 120 DAY, NOW() - INTERVAL 120 DAY),
  (3, 'company05', '권도현', 'company05@example.com', @pw, '01061110005', '루미에르코스메틱', 'Lumiere Cosmetic', '강하은', 'Haeun Kang',   '서울특별시 성동구 성수이로 118',     '108-81-77341', NOW() - INTERVAL 110 DAY, NOW() - INTERVAL 110 DAY),
  (3, 'company06', '황수빈', 'company06@example.com', @pw, '01061110006', '(주)모토라인',     'Motoline Corp.',   '유건우', 'Gunwoo Yoo',   '경기도 화성시 동탄첨단산업1로 27',   '135-86-40218', NOW() - INTERVAL 100 DAY, NOW() - INTERVAL 100 DAY),
  (3, 'company07', '노은서', 'company07@example.com', @pw, '01061110007', '클래스메이트(주)', 'Classmate Inc.',   '신지호', 'Jiho Shin',    '서울특별시 마포구 월드컵북로 396',   '105-88-20931', NOW() - INTERVAL 90 DAY,  NOW() - INTERVAL 90 DAY),
  (3, 'company08', '안소율', 'company08@example.com', @pw, '01061110008', '캠프그라운드',     'Campground',       '장민호', 'Minho Jang',   '강원특별자치도 춘천시 중앙로 55',    '221-81-55190', NOW() - INTERVAL 80 DAY,  NOW() - INTERVAL 80 DAY);


-- 3. 박람회 13개 --------------------------------------------------------
--   진행 중 1 / 예정 8 / 종료 1 (매출 이력용) / 승인 대기 2 / 반려 1
--   expoadmin1: AI 로보틱스(진행 중), 스타트업 서밋, 게임 페스티벌(종료) → 대시보드·매출 데모의 중심 계정

INSERT INTO expo (expo_admin_id, title, image_url, description, location, start_date, end_date,
                  visibility, status, reason, price, max_capacity, reserved_count, version,
                  created_at, updated_at, display_start_date, display_update_date)
SELECT (SELECT expo_admin_id FROM expo_admin WHERE customer_id = s.admin_login),
       s.title, s.poster, s.description, s.location,
       CURDATE() + INTERVAL s.start_offset DAY,
       CURDATE() + INTERVAL (s.start_offset + s.days - 1) DAY,
       s.status = 'APPROVED', s.status, s.reason, s.price, s.capacity, 0, 0,
       NOW() - INTERVAL s.applied_days_ago DAY, NOW() - INTERVAL s.applied_days_ago DAY,
       NOW() - INTERVAL s.applied_days_ago DAY, NOW() - INTERVAL s.applied_days_ago DAY
FROM (
  SELECT 'expoadmin1' AS admin_login, '2026 AI & 로보틱스 코리아' AS title, '/demo/posters/expo-01.svg' AS poster,
         '생성형 AI, 휴머노이드 로봇, 스마트 팩토리까지 국내외 200여 개 기업이 참가하는 AI·로보틱스 전문 전시회입니다. 매일 기조연설과 로봇 시연 무대가 열립니다.' AS description,
         '코엑스 A·B홀 (서울 삼성동)' AS location, -2 AS start_offset, 5 AS days,
         'APPROVED' AS status, NULL AS reason, 15000 AS price, 3000 AS capacity, 90 AS applied_days_ago
  UNION ALL SELECT 'expoadmin4', '캠핑 & 아웃도어 라이프 페어', '/demo/posters/expo-02.svg',
         '텐트·캠핑카·아웃도어 의류부터 차박 용품까지. 150개 브랜드의 신제품을 직접 체험하고 현장 한정 특가로 만나보세요.',
         '수원메쎄 (경기 수원)', 3, 4, 'APPROVED', NULL, 13000, 2500, 85
  UNION ALL SELECT 'expoadmin3', '서울 뷰티 & 패션 위크 2026', '/demo/posters/expo-03.svg',
         'K-뷰티 브랜드 쇼케이스와 신진 디자이너 런웨이, 퍼스널 컬러 진단 부스가 함께하는 도심형 패션·뷰티 페스티벌입니다.',
         '동대문디자인플라자 DDP (서울 중구)', 6, 4, 'APPROVED', NULL, 8000, 4000, 80
  UNION ALL SELECT 'expoadmin1', '스타트업 인베스트 서밋 2026', '/demo/posters/expo-04.svg',
         '시드부터 시리즈B까지, 유망 스타트업 120곳의 IR 피칭과 VC 1:1 밋업이 열립니다. 창업 교육 세션도 함께 진행됩니다.',
         '킨텍스 제2전시장 (경기 고양)', 12, 3, 'APPROVED', NULL, 20000, 1500, 75
  UNION ALL SELECT 'expoadmin4', '에듀테크 코리아 페어', '/demo/posters/expo-05.svg',
         'AI 튜터, 디지털 교과서, 코딩 교육 로봇 등 미래 교육 솔루션을 한자리에서. 교사·학부모 대상 세미나가 매일 열립니다.',
         '코엑스 C홀 (서울 삼성동)', 16, 3, 'APPROVED', NULL, 5000, 2000, 70
  UNION ALL SELECT 'expoadmin2', 'K-푸드 & 스마트팜 페어', '/demo/posters/expo-06.svg',
         '로컬 식품 브랜드 시식관과 수직농장·스마트팜 기술관을 함께 운영합니다. 농가와 유통사를 잇는 상담회도 마련됩니다.',
         'aT센터 제1전시장 (서울 양재)', 20, 4, 'APPROVED', NULL, 10000, 2500, 68
  UNION ALL SELECT 'expoadmin2', '그린에너지 & 탄소중립 엑스포', '/demo/posters/expo-07.svg',
         '태양광, 수소, ESS, 탄소 포집 기술까지 에너지 전환의 현재를 보여주는 전시회입니다. 기업 ESG 컨퍼런스가 동시 개최됩니다.',
         '벡스코 제1전시장 (부산 해운대)', 30, 3, 'APPROVED', NULL, 12000, 2000, 66
  UNION ALL SELECT 'expoadmin3', '바이오 헬스케어 인사이트 2026', '/demo/posters/expo-08.svg',
         '디지털 헬스케어, 신약 개발 플랫폼, 의료 AI 스타트업이 참가하는 B2B 중심 바이오 전시회입니다.',
         '코엑스 D홀 (서울 삼성동)', 41, 3, 'APPROVED', NULL, 25000, 1200, 62
  UNION ALL SELECT 'expoadmin4', '모빌리티 쇼 코리아 2026', '/demo/posters/expo-09.svg',
         '전기차, 자율주행, UAM, 전장 부품까지. 완성차 브랜드 신차 공개와 시승 체험 존을 운영합니다.',
         '킨텍스 제1전시장 (경기 고양)', 50, 5, 'APPROVED', NULL, 18000, 5000, 60
  UNION ALL SELECT 'expoadmin1', '게임 & e스포츠 페스티벌 2026', '/demo/posters/expo-10.svg',
         '인디 게임 쇼케이스와 e스포츠 결승전 현장 관람, 게임 개발자 커리어 토크가 열린 게임 축제입니다.',
         '벡스코 오디토리움 (부산 해운대)', -40, 4, 'APPROVED', NULL, 12000, 3000, 130
  -- 승인 대기 / 반려: 실제 신청 흐름처럼 expo_admin_id 없음, 비공개
  UNION ALL SELECT NULL, '2026 스마트시티 & 프롭테크 엑스포', '/demo/posters/expo-11.svg',
         '스마트 빌딩, 도시 데이터 플랫폼, 부동산 테크 기업이 참가하는 도시 혁신 전시회입니다.',
         '송도컨벤시아 (인천 연수)', 70, 3, 'PENDING', NULL, 15000, 2000, 2
  UNION ALL SELECT NULL, '반려동물 라이프 페어', '/demo/posters/expo-12.svg',
         '펫푸드, 펫테크, 반려동물 동반 여행까지. 반려인과 반려동물이 함께 즐기는 라이프스타일 박람회입니다.',
         '세텍 SETEC (서울 대치동)', 60, 3, 'PENDING', NULL, 9000, 3000, 1
  UNION ALL SELECT NULL, '글로벌 코인 투자 박람회', '/demo/posters/expo-13.svg',
         '가상자산 투자 설명회 및 거래소 홍보 부스 운영 신청 건입니다.',
         '여의도 IFC 컨벤션홀 (서울 영등포)', 45, 2, 'REJECTED', '투자 권유성 콘텐츠 포함 및 사업자 정보 확인 불가로 반려합니다.', 30000, 500, 9
) s;

INSERT INTO expo_category (expo_id, category_id)
SELECT e.expo_id, (SELECT MIN(c.category_id) FROM category c WHERE c.name = m.category)
FROM (
            SELECT '2026 AI & 로보틱스 코리아' AS title, 'IT/기술' AS category
  UNION ALL SELECT '캠핑 & 아웃도어 라이프 페어',        '기타'
  UNION ALL SELECT '서울 뷰티 & 패션 위크 2026',         '패션/뷰티'
  UNION ALL SELECT '스타트업 인베스트 서밋 2026',        'IT/기술'
  UNION ALL SELECT '스타트업 인베스트 서밋 2026',        '교육'
  UNION ALL SELECT '에듀테크 코리아 페어',               '교육'
  UNION ALL SELECT '에듀테크 코리아 페어',               'IT/기술'
  UNION ALL SELECT 'K-푸드 & 스마트팜 페어',             '식품/농업'
  UNION ALL SELECT '그린에너지 & 탄소중립 엑스포',       '환경/에너지'
  UNION ALL SELECT '바이오 헬스케어 인사이트 2026',      '의료/바이오'
  UNION ALL SELECT '모빌리티 쇼 코리아 2026',            '자동차/모빌리티'
  UNION ALL SELECT '게임 & e스포츠 페스티벌 2026',       'IT/기술'
  UNION ALL SELECT '2026 스마트시티 & 프롭테크 엑스포',  '환경/에너지'
  UNION ALL SELECT '2026 스마트시티 & 프롭테크 엑스포',  'IT/기술'
  UNION ALL SELECT '반려동물 라이프 페어',               '기타'
  UNION ALL SELECT '글로벌 코인 투자 박람회',            '기타'
) m
JOIN expo e ON e.title = m.title;


-- 4. 예약 계획표 (임시 테이블) -------------------------------------------
--   승인된 박람회마다 N건의 예약을 만들고, 행마다 CRC32 해시로 인원·결제수단·환불 여부·체크인을 결정한다.
--   (RAND() 대신 해시를 써서 실행할 때마다 같은 결과가 나온다)

CREATE TEMPORARY TABLE demo_plan
WITH RECURSIVE seq (n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM seq WHERE n < 220),
volume (title, cnt) AS (
            SELECT '2026 AI & 로보틱스 코리아', 220
  UNION ALL SELECT '캠핑 & 아웃도어 라이프 페어', 130
  UNION ALL SELECT '서울 뷰티 & 패션 위크 2026', 160
  UNION ALL SELECT '스타트업 인베스트 서밋 2026', 120
  UNION ALL SELECT '에듀테크 코리아 페어', 50
  UNION ALL SELECT 'K-푸드 & 스마트팜 페어', 90
  UNION ALL SELECT '그린에너지 & 탄소중립 엑스포', 70
  UNION ALL SELECT '바이오 헬스케어 인사이트 2026', 40
  UNION ALL SELECT '모빌리티 쇼 코리아 2026', 60
  UNION ALL SELECT '게임 & e스포츠 페스티벌 2026', 150
),
base AS (
  SELECT e.expo_id, e.price, e.start_date, e.end_date, s.n,
         CRC32(CONCAT(e.expo_id, '-', s.n)) AS h,
         LEAST(CURDATE(), e.end_date) AS window_end
  FROM expo e
  JOIN volume v ON v.title = e.title
  JOIN seq s ON s.n <= v.cnt
),
timed AS (
  -- 예약일: 최근 50일, 최근일수록 완만하게 많아지는 삼각 분포(1 - √u) / 시각: 08:00 ~ 23:59
  SELECT b.*,
         TIMESTAMP(b.window_end - INTERVAL FLOOR((1 - SQRT(MOD(b.h, 1000) / 1000)) * 50) DAY)
           + INTERVAL (28800 + MOD(b.h DIV 1000, 57600)) SECOND AS raw_at
  FROM base b
)
SELECT t.expo_id, t.price, t.start_date, t.end_date, t.h,
       CONCAT('seed_', t.expo_id, '_', t.n) AS pay_key,   -- payment ↔ 계획표 연결 키 (portone_payment_id)
       IF(t.raw_at > NOW(), NOW() - INTERVAL (1 + MOD(t.h, 90)) MINUTE, t.raw_at) AS reserved_at,
       CASE WHEN MOD(t.h DIV 7, 100) < 60 THEN 1
            WHEN MOD(t.h DIV 7, 100) < 88 THEN 2
            WHEN MOD(t.h DIV 7, 100) < 96 THEN 3
            ELSE 4 END AS people,
       CASE WHEN MOD(t.h DIV 11, 100) < 7  THEN 'CANCELLED'        -- 환불 완료
            WHEN MOD(t.h DIV 11, 100) < 10 THEN 'REFUND_PENDING'   -- 환불 요청 대기
            WHEN MOD(t.h DIV 11, 100) < 11 THEN 'REFUND_REJECTED'  -- 환불 반려
            ELSE 'RESERVED' END AS outcome,
       CASE WHEN MOD(t.h DIV 37, 100) < 45 THEN 'Credit Card'
            WHEN MOD(t.h DIV 37, 100) < 65 THEN 'KakaoPay'
            WHEN MOD(t.h DIV 37, 100) < 77 THEN 'TossPay'
            WHEN MOD(t.h DIV 37, 100) < 87 THEN 'NaverPay'
            WHEN MOD(t.h DIV 37, 100) < 95 THEN 'Bank Transfer'
            ELSE 'Virtual Account' END AS method,
       CONCAT('user', LPAD(1 + MOD(t.h DIV 13, 60), 3, '0')) AS user_login,
       CAST(NULL AS DATETIME) AS checked_in_at
FROM timed t;

-- 체크인 시각: 박람회 기간(시작 ~ min(종료, 오늘)) 중 10~18시, 종료된 박람회 82% / 진행 중 55%
UPDATE demo_plan
SET checked_in_at =
      TIMESTAMP(GREATEST(start_date, DATE(reserved_at))
                + INTERVAL MOD(h DIV 19, DATEDIFF(LEAST(end_date, CURDATE()), GREATEST(start_date, DATE(reserved_at))) + 1) DAY)
      + INTERVAL (600 + MOD(h DIV 23, 480)) MINUTE
WHERE outcome = 'RESERVED'
  AND MOD(h DIV 17, 100) < IF(end_date < CURDATE(), 82, 55)
  AND GREATEST(start_date, DATE(reserved_at)) <= LEAST(end_date, CURDATE());

-- 아직 오지 않은 시각이거나 예약보다 앞선 체크인은 제외
UPDATE demo_plan SET checked_in_at = NULL
WHERE checked_in_at IS NOT NULL AND (checked_in_at <= reserved_at OR checked_in_at >= NOW());


-- 5. 결제 → 예약 → QR → 티켓 → 체크인 → 환불 ------------------------------

INSERT INTO payment (amount, method, status, paid_at, portone_payment_id)
SELECT price * people, method, IF(outcome = 'CANCELLED', 'REFUNDED', 'PAID'), reserved_at, pay_key
FROM demo_plan;

INSERT INTO reservation (user_id, expo_id, payment_id, status, code, people, created_at, updated_at)
SELECT u.user_id, d.expo_id, p.payment_id,
       IF(d.outcome = 'CANCELLED', 'CANCELLED', 'RESERVED'),
       CONCAT('RES-', DATE_FORMAT(d.reserved_at, '%Y%m%d'), '-', LOWER(LPAD(HEX(MOD(d.h, 16777216)), 6, '0'))),
       d.people, d.reserved_at, d.reserved_at
FROM demo_plan d
JOIN payment p ON p.portone_payment_id = d.pay_key
JOIN `user` u ON u.customer_id = d.user_login;

-- 데모 QR 토큰은 자리표시 값 (HMAC 서명이 없어 실제 스캔 불가 — 실결제로 발급된 QR만 스캔 가능)
INSERT INTO qr_code (reservation_id, data, status, created_at)
SELECT r.reservation_id,
       CONCAT('r=', r.reservation_id, '&c=', r.code, '&e=0&s=demo'),
       IF(d.checked_in_at IS NULL, 'PENDING', 'CHECKED_IN'),
       d.reserved_at
FROM demo_plan d
JOIN payment p ON p.portone_payment_id = d.pay_key
JOIN reservation r ON r.payment_id = p.payment_id;

INSERT INTO ticket (qr_id, status, created_at)
SELECT q.qr_id, d.checked_in_at IS NOT NULL, d.reserved_at
FROM demo_plan d
JOIN payment p ON p.portone_payment_id = d.pay_key
JOIN reservation r ON r.payment_id = p.payment_id
JOIN qr_code q ON q.reservation_id = r.reservation_id;

INSERT INTO checkin_log (ticket_id, time)
SELECT t.ticket_id, d.checked_in_at
FROM demo_plan d
JOIN payment p ON p.portone_payment_id = d.pay_key
JOIN reservation r ON r.payment_id = p.payment_id
JOIN qr_code q ON q.reservation_id = r.reservation_id
JOIN ticket t ON t.qr_id = q.qr_id
WHERE d.checked_in_at IS NOT NULL;

-- 환불 완료(APPROVED)는 결제 REFUNDED·예약 CANCELLED 와 짝, 대기(PENDING)·반려(REJECTED)는 결제 PAID 유지
INSERT INTO refund (payment_id, status, reason, created_at, approved_at)
SELECT p.payment_id,
       CASE d.outcome WHEN 'CANCELLED' THEN 'APPROVED' WHEN 'REFUND_PENDING' THEN 'PENDING' ELSE 'REJECTED' END,
       IF(d.outcome = 'REFUND_REJECTED', '행사 2일 전 이후 요청은 환불 규정상 불가합니다.',
          ELT(1 + MOD(d.h DIV 29, 5), '일정 변경으로 참석이 어려워졌습니다.', '동행인이 참석하지 못하게 되었습니다.',
              '중복 결제되어 한 건 취소 요청합니다.', '개인 사정으로 방문이 어렵습니다.', '건강 문제로 참석이 어렵습니다.')),
       LEAST(d.reserved_at + INTERVAL (1 + MOD(d.h DIV 31, 5)) DAY, NOW()),
       IF(d.outcome = 'REFUND_PENDING', NULL, LEAST(d.reserved_at + INTERVAL (2 + MOD(d.h DIV 31, 5)) DAY, NOW()))
FROM demo_plan d
JOIN payment p ON p.portone_payment_id = d.pay_key
WHERE d.outcome <> 'RESERVED';

DROP TEMPORARY TABLE demo_plan;


-- 6. 집계값을 실제 데이터와 일치시키기 -------------------------------------

UPDATE expo e
SET e.reserved_count = (SELECT COALESCE(SUM(r.people), 0) FROM reservation r
                        WHERE r.expo_id = e.expo_id AND r.status = 'RESERVED');

-- 매출 화면은 expo_statistics 행이 없으면 실패하므로 승인된 박람회 전부 생성
INSERT INTO expo_statistics (expo_id, view_count, reservation_count, payment_total)
SELECT e.expo_id,
       COUNT(r.reservation_id) * (18 + MOD(e.expo_id * 7, 25)) + 200,
       COUNT(r.reservation_id),
       COALESCE(SUM(p.amount), 0)
FROM expo e
LEFT JOIN reservation r ON r.expo_id = e.expo_id AND r.status = 'RESERVED'
LEFT JOIN payment p ON p.payment_id = r.payment_id
WHERE e.status = 'APPROVED'
GROUP BY e.expo_id;


-- 7. 부스 · 배너 ---------------------------------------------------------

INSERT INTO booth (expo_id, user_id, payment_id, title, image_url, location, manager_name, department,
                   phone, email, status, reason, created_at, updated_at)
SELECT e.expo_id, u.user_id, NULL,
       CONCAT(TRIM(REPLACE(u.company_name_kr, '(주)', '')), ' 부스'),
       CONCAT('/demo/posters/company-', RIGHT(b.login, 2), '.svg'),
       CONCAT(b.hall, ' 구역'), u.name,
       ELT(CAST(RIGHT(b.login, 2) AS UNSIGNED), '사업개발팀', '마케팅팀', '영업팀', '임상사업부',
           '브랜드팀', '전장사업팀', '파트너십팀', '영업기획팀'),
       u.phone, u.email, b.status, b.reason,
       TIMESTAMP(LEAST(e.start_date - INTERVAL 30 DAY, CURDATE() - INTERVAL 1 DAY), '14:00:00'),
       TIMESTAMP(LEAST(e.start_date - INTERVAL 30 DAY, CURDATE() - INTERVAL 1 DAY), '14:00:00')
FROM (
            SELECT 'company01' AS login, '2026 AI & 로보틱스 코리아' AS title, 'A-12' AS hall, 'APPROVED' AS status, NULL AS reason
  UNION ALL SELECT 'company01', '스타트업 인베스트 서밋 2026',   'S-04', 'APPROVED', NULL
  UNION ALL SELECT 'company01', '게임 & e스포츠 페스티벌 2026',  'G-07', 'APPROVED', NULL
  UNION ALL SELECT 'company02', '그린에너지 & 탄소중립 엑스포',  'E-21', 'APPROVED', NULL
  UNION ALL SELECT 'company02', '2026 AI & 로보틱스 코리아',     'A-30', 'PENDING',  NULL
  UNION ALL SELECT 'company03', 'K-푸드 & 스마트팜 페어',        'F-03', 'APPROVED', NULL
  UNION ALL SELECT 'company03', '캠핑 & 아웃도어 라이프 페어',   'C-11', 'APPROVED', NULL
  UNION ALL SELECT 'company04', '바이오 헬스케어 인사이트 2026', 'B-08', 'APPROVED', NULL
  UNION ALL SELECT 'company04', '2026 AI & 로보틱스 코리아',     'A-44', 'REJECTED', '전시 품목이 박람회 주제와 맞지 않습니다.'
  UNION ALL SELECT 'company05', '서울 뷰티 & 패션 위크 2026',    'D-02', 'APPROVED', NULL
  UNION ALL SELECT 'company06', '모빌리티 쇼 코리아 2026',       'M-15', 'PENDING',  NULL
  UNION ALL SELECT 'company06', '2026 AI & 로보틱스 코리아',     'A-51', 'APPROVED', NULL
  UNION ALL SELECT 'company07', '에듀테크 코리아 페어',          'C-05', 'APPROVED', NULL
  UNION ALL SELECT 'company07', '스타트업 인베스트 서밋 2026',   'S-11', 'PENDING',  NULL
  UNION ALL SELECT 'company08', '캠핑 & 아웃도어 라이프 페어',   'C-01', 'APPROVED', NULL
  UNION ALL SELECT 'company08', '서울 뷰티 & 패션 위크 2026',    'D-19', 'REJECTED', '부스 위치가 다른 신청과 중복됩니다.'
) b
JOIN expo e ON e.title = b.title
JOIN `user` u ON u.customer_id = b.login;

-- 배너는 박람회당 최대 1개 (BannerRepository.findByExpo_ExpoId 가 단건 조회)
INSERT INTO banner (expo_id, payment_id, image_url, start_date, end_date, status, reason, created_at, updated_at)
SELECT e.expo_id, NULL, e.image_url, CURDATE() - INTERVAL 5 DAY, e.end_date, b.status, b.reason,
       NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY
FROM (
            SELECT '2026 AI & 로보틱스 코리아' AS title, 'APPROVED' AS status, NULL AS reason
  UNION ALL SELECT '캠핑 & 아웃도어 라이프 페어', 'APPROVED', NULL
  UNION ALL SELECT '서울 뷰티 & 패션 위크 2026',  'APPROVED', NULL
  UNION ALL SELECT 'K-푸드 & 스마트팜 페어',      'PENDING',  NULL
  UNION ALL SELECT '모빌리티 쇼 코리아 2026',     'REJECTED', '배너 이미지 해상도 기준(1920×600)에 미달합니다.'
) b
JOIN expo e ON e.title = b.title;

INSERT INTO banner_click_log (banner_id, clicked_count)
SELECT banner_id, 300 + MOD(CRC32(CONCAT('banner-', banner_id)), 2700)
FROM banner
WHERE status = 'APPROVED';
