import {
  PageViewsCard,
  TotalReservationsCard,
  EntranceRateCard,
} from "./sections/dashboard/DashboardStatsCard.jsx";
import ChartSection from "./sections/dashboard/DashboardChartSection.jsx";
import "../assets/css/dashboard/DashboardIndex.css";

// 수정 완료되면 DashboardPage.jsx 로 변경 예정
const DashboardIndex = ({ expoId }) => {
  return (
    <div className="dashboard-index main-container">
      <div className="content-wrapper">
        {/* Main Content */}
        <main className="main-content">
          <div className="content-padding">
            {/* Stats Cards Row */}
            <div className="stats-grid">
              <PageViewsCard expoId={expoId} />
              <TotalReservationsCard expoId={expoId} />
              <EntranceRateCard expoId={expoId} />
            </div>

            {/* Charts Section */}
            <ChartSection expoId={expoId} />
          </div>
        </main>
      </div>
    </div>
  );
};

export default DashboardIndex;
