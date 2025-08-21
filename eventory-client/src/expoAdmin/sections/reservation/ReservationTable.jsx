import React from 'react';
import "../../../assets/css/reservation/ReservationTable.css";

const mockReservations = [
  {
    id: "1",
    name: "김민수",
    phone: "010-1234-5678",
    reservationNumber: "R2025080001",
    ticketType: "유료",
    reservationDate: "2025-08-01",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "2",
    name: "이영희",
    phone: "010-2345-6789",
    reservationNumber: "R2025080002",
    ticketType: "유료",
    reservationDate: "2025-08-01",
    entryStatus: "입장완료",
    checkinTime: "2025-08-17 15:15",
  },
  {
    id: "3",
    name: "박철수",
    phone: "010-3456-7890",
    reservationNumber: "R2025080003",
    ticketType: "무료",
    reservationDate: "2025-08-03",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 16:20",
  },
  {
    id: "4",
    name: "최지연",
    phone: "010-4567-8901",
    reservationNumber: "R2025080004",
    ticketType: "유료",
    reservationDate: "2025-08-04",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 13:45",
  },
  {
    id: "5",
    name: "정태윤",
    phone: "010-5678-9012",
    reservationNumber: "R2025080005",
    ticketType: "유료",
    reservationDate: "2025-08-05",
    entryStatus: "입장완료",
    checkinTime: "2025-08-16 17:10",
  },
  {
    id: "6",
    name: "한소영",
    phone: "010-6789-0123",
    reservationNumber: "R2025080006",
    ticketType: "유료",
    reservationDate: "2025-08-05",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "7",
    name: "오준호",
    phone: "010-7890-1234",
    reservationNumber: "R2025080007",
    ticketType: "무료",
    reservationDate: "2025-08-05",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 11:25",
  },
  {
    id: "8",
    name: "장미란",
    phone: "010-8901-2345",
    reservationNumber: "R2025080008",
    ticketType: "유료",
    reservationDate: "2025-08-06",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 11:50",
  },
  {
    id: "9",
    name: "송현우",
    phone: "010-9012-3456",
    reservationNumber: "R2025080009",
    ticketType: "유료",
    reservationDate: "2025-08-08",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "10",
    name: "윤서진",
    phone: "010-0123-4567",
    reservationNumber: "R2025080010",
    ticketType: "유료",
    reservationDate: "2025-08-08",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "11",
    name: "강동혁",
    phone: "010-1122-3344",
    reservationNumber: "R2025080011",
    ticketType: "유료",
    reservationDate: "2025-08-09",
    entryStatus: "입장완료",
    checkinTime: "2025-08-16 12:05",
  },
  {
    id: "12",
    name: "임수빈",
    phone: "010-2233-4455",
    reservationNumber: "R2025080012",
    ticketType: "유료",
    reservationDate: "2025-08-09",
    entryStatus: "입장완료",
    checkinTime: "2025-08-16 09:30",
  },
  {
    id: "13",
    name: "조예린",
    phone: "010-3344-5566",
    reservationNumber: "R2025080013",
    ticketType: "유료",
    reservationDate: "2025-08-09",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 15:20",
  },
  {
    id: "14",
    name: "백상훈",
    phone: "010-4455-6677",
    reservationNumber: "R2025080014",
    ticketType: "유료",
    reservationDate: "2025-08-10",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "15",
    name: "홍채원",
    phone: "010-5566-7788",
    reservationNumber: "R2025080015",
    ticketType: "유료",
    reservationDate: "2025-08-11",
    entryStatus: "입장완료",
    checkinTime: "2025-08-17 16:10",
  },
  {
    id: "16",
    name: "남궁민",
    phone: "010-6677-8899",
    reservationNumber: "R2025080016",
    ticketType: "유료",
    reservationDate: "2025-08-11",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 10:15",
  },
  {
    id: "17",
    name: "서지훈",
    phone: "010-7788-9900",
    reservationNumber: "R2025080017",
    ticketType: "무료",
    reservationDate: "2025-08-12",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "18",
    name: "안유진",
    phone: "010-8899-0011",
    reservationNumber: "R2025080018",
    ticketType: "유료",
    reservationDate: "2025-08-13",
    entryStatus: "미입장",
    checkinTime: "-",
  },
  {
    id: "19",
    name: "김도영",
    phone: "010-9900-1122",
    reservationNumber: "R2025080019",
    ticketType: "유료",
    reservationDate: "2025-08-13",
    entryStatus: "입장완료",
    checkinTime: "2025-08-18 09:25",
  },
  {
    id: "20",
    name: "문하늘",
    phone: "010-0011-2233",
    reservationNumber: "R2025080020",
    ticketType: "유료",
    reservationDate: "2025-08-15",
    entryStatus: "입장완료",
    checkinTime: "2025-08-17 13:10",
  },
];

const Button = ({ variant = 'default', size = 'default', className = '', children, ...props }) => {
  return (
    <button className={`btn btn-${variant} btn-${size} ${className}`} {...props}>
      {children}
    </button>
  );
};

// [CHANGED] activeFilter를 받아서 필터링 적용
function ReservationTable({ activeFilter = 'all' }) { // [CHANGED]
  // [ADDED] 탭 → 한국어 상태 매핑 및 필터링 로직
  const filteredReservations = mockReservations.filter((r) => { // [ADDED]
    if (activeFilter === 'entered') return r.entryStatus === '입장완료';
    if (activeFilter === 'not-entered') return r.entryStatus === '미입장';
    return true; // 'all'
  });

  return (
    <div className="reservation-table-container">
      {/* Table Header */}
      <div className="table-header">
        <div className="header-cell">이름</div>
        <div className="header-cell">전화번호</div>
        <div className="header-cell">예약번호</div>
        <div className="header-cell">티켓 종류</div>
        <div className="header-cell">예약일</div>
        <div className="header-cell">입장 여부</div>
        <div className="header-cell">체크인</div>
        <div className="header-cell">취소</div>
        <div className="header-cell">체크인시간</div>
      </div>

      {/* Table Rows */}
      <div className="table-body">
        {/* [CHANGED] mockReservations → filteredReservations 로 대체 */}
        {filteredReservations.map((reservation, index) => ( // [CHANGED]
          <div key={reservation.id}>
            {/* Desktop Layout */}
            <div className="table-row desktop-row">
              <div className="table-cell">{reservation.name}</div>
              <div className="table-cell">{reservation.phone}</div>
              <div className="table-cell">{reservation.reservationNumber}</div>
              <div className={`table-cell ticket-type ${reservation.ticketType === "무료" || reservation.ticketType === "FREE" ? "ticket-free" : ""}`}>
                {reservation.ticketType}
              </div>
              <div className="table-cell">{reservation.reservationDate}</div>
              <div className={`table-cell status-cell ${reservation.entryStatus === "입장완료" ? "status-completed" : "status-pending"}`}>
                {reservation.entryStatus}
              </div>
              <div className="table-cell">
                <Button
                  variant={reservation.entryStatus === "입장완료" ? "secondary" : "outline"}
                  size="sm"
                  className={`action-btn ${reservation.entryStatus === "입장완료" ? "btn-disabled" : "btn-checkin"}`}
                >
                  체크인
                </Button>
              </div>
              <div className="table-cell">
                <Button
                  variant={reservation.entryStatus === "미입장" ? "secondary" : "outline"}
                  size="sm"
                  className={`action-btn ${reservation.entryStatus === "미입장" ? "btn-disabled" : "btn-cancel"}`}
                >
                  취소
                </Button>
              </div>
              <div className="table-cell">{reservation.checkinTime}</div>
            </div>

            {/* Mobile Layout */}
            <div className="mobile-card">
              <div className="mobile-card-content">
                <div className="mobile-row">
                  <span className="mobile-label">이름:</span>
                  <span className="mobile-value mobile-name">{reservation.name}</span>
                </div>
                <div className="mobile-row">
                  <span className="mobile-label">전화번호:</span>
                  <span className="mobile-value">{reservation.phone}</span>
                </div>
                <div className="mobile-row">
                  <span className="mobile-label">예약번호:</span>
                  <span className="mobile-value">{reservation.reservationNumber}</span>
                </div>
                <div className="mobile-row">
                  <span className="mobile-label">입장여부:</span>
                  <span className={`mobile-value mobile-status ${reservation.entryStatus === "입장완료" ? "status-completed" : "status-pending"}`}>
                    {reservation.entryStatus}
                  </span>
                </div>
                <div className="mobile-actions">
                  <Button size="sm" className="mobile-btn mobile-btn-checkin">
                    체크인
                  </Button>
                  <Button size="sm" variant="outline" className="mobile-btn mobile-btn-cancel">
                    취소
                  </Button>
                </div>
              </div>
            </div>

            {/* [CHANGED] 구분선 조건도 filtered 기준으로 계산 */}
            {index < filteredReservations.length - 1 && <div className="row-divider"></div>} {/* [CHANGED] */}
          </div>
        ))}
      </div>
    </div>
  );
}
export default ReservationTable;