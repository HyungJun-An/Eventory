import Header from "./sections/Header.jsx";
import Sidebar from "./sections/SideBar.jsx";
import {
  PageViewsCard,
  TotalReservationsCard,
  EntranceRateCard,
} from "./sections/dashboard/DashboardStatsCard.jsx";
import ChartSection from "./sections/dashboard/DashboardChartSection.jsx";
import "../assets/css/dashboard/DashboardIndex.css";

// 수정 완료되면 DashboardPage.jsx 로 변경 예정
function DashboardIndex() {
  return (
    <div className="dashboard-index main-container">
      {/* Header */}
      <Header />

      <div className="content-wrapper">
        {/* Sidebar */}
        <div className="sidebar-container">
          <Sidebar />
        </div>

        {/* Main Content */}
        <main className="main-content">
          <div className="content-padding">
            {/* Stats Cards Row */}
            <div className="stats-grid">
              <PageViewsCard />
              <TotalReservationsCard />
              <EntranceRateCard />
            </div>

            {/* Charts Section */}
            <ChartSection />
          </div>
        </main>
      </div>
    </div>
  );
}

export default DashboardIndex;
