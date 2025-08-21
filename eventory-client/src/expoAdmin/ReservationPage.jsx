import { useState } from "react";
import { Search } from "lucide-react";
import ReservationTable from "./sections/reservation/ReservationTable.jsx";
import Pagination from "./sections/reservation/Pagination.jsx";
import "../assets/css/reservation/ReservationPage.css";

const filterTabs = [
  { id: "all", label: "전체", active: true },
  { id: "entered", label: "입장완료", active: false },
  { id: "not-entered", label: "미입장", active: false },
];

function ReservationPage() {
  const [activeFilter, setActiveFilter] = useState("all");

  return (
    <div className="reservations-container">
      {/* Page Header */}
      <div className="page-header">
        {/* FIX: 제목 영역 제거하고, 탭 + 검색을 같은 행에 배치 */}
        <div className="controls-row"> {/* FIX: 새 컨테이너 추가 */}
          {/* Filter Tabs */}
          <div className="filter-tabs">
            {filterTabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveFilter(tab.id)}
                className={`filter-tab ${activeFilter === tab.id ? 'active' : ''}`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {/* Search Bar */}
          <div className="search-container"> {/* FIX: 옮김(탭 옆으로) */}
            <div className="search-bar">
              <Search className="search-icon" />
              <input
                type="text"
                placeholder="검색어를 입력해주세요."
                className="search-input"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Table Container */}
      <div className="table-container">
        <div className="table-wrapper">
          <ReservationTable />
        </div>
        <Pagination />
      </div>
    </div>
  );
}
export default ReservationPage;