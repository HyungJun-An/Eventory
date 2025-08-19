import { useParams } from "react-router-dom";
import {
  PageViewsCard,
  TotalReservationsCard,
  EntranceRateCard,
} from "./sections/dashboard/DashboardStatsCard.jsx";
import ChartSection from "./sections/dashboard/DashboardChartSection.jsx";
import "../assets/css/dashboard/DashboardIndex.css";

/**
 * expoId는 우선 props로 받되, 없으면 라우터 파라미터(:expoId)로 대체한다.
 * 이렇게 하면 상위에서 expoId를 내려주지 않아도 URL만 맞으면 동작한다.
 */
const DashboardPage = ({ expoId: expoIdProp }) => {
  const { expoId: expoIdParam } = useParams();
  const expoId = expoIdProp ?? expoIdParam; // props > URL 파라미터

  // expoId가 없을 때는 API 호출 컴포넌트 렌더링을 막는다(400 방지)
  if (!expoId) {
    return (
      <div className="dashboard-index main-container">
        <div className="content-wrapper">
          <main className="main-content">
            <div className="content-padding">
              <p style={{ padding: "16px" }}>
                유효한 박람회 ID가 없습니다. 경로 예:{" "}
                <code>/admin/expos/1/dashboard</code>
              </p>
            </div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-index main-container">
      <div className="content-wrapper">
        {/* Main Content */}
        <main className="main-content">
          <div className="content-padding">
            {/* Stats Cards Row */}
            <div className="stats-grid">
              {/* 하위 컴포넌트에 expoId를 명시적으로 전달 (안전) */}
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

export default DashboardPage;
