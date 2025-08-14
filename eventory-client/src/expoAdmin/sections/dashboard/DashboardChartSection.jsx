import React, { useState, useEffect } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import api from "../../../api/axiosInstance";
import "../../../assets/css/dashboard/DashboardChartSection.css";

function DashboardChartSection() {
  const [period, setPeriod] = useState("daily");
  const [reservationData, setReservationData] = useState([]);
  const [ticketTypeData, setTicketTypeData] = useState([]);
  const [loading, setLoading] = useState(true);

  const expoId = 1; // TODO: 필요에 따라 props나 useParams로 치환 권장

  // 예약 현황 데이터 가져오기
  const fetchReservationData = async (selectedPeriod) => {
    try {
      const response = await api.get(
        `/admin/expos/${expoId}/dashboard/stats`,
        { params: { period: selectedPeriod } } // ✅ 쿼리 파라미터는 params로
      );
      setReservationData(response.data);
    } catch (error) {
      console.error("Failed to fetch reservation data:", error);
      // 에러 시 더미 데이터 사용
      setReservationData([
        { label: "08/05 (화)", reservationCount: 12 },
        { label: "08/06 (수)", reservationCount: 19 },
        { label: "08/07 (목)", reservationCount: 15 },
        { label: "08/08 (금)", reservationCount: 25 },
        { label: "08/09 (토)", reservationCount: 32 },
        { label: "08/10 (일)", reservationCount: 28 },
        { label: "08/11 (월)", reservationCount: 18 },
      ]);
    }
  };

  // 티켓 타입별 데이터 가져오기
  const fetchTicketTypeData = async () => {
    try {
      const response = await api.get(
        `/admin/expos/${expoId}/dashboard/ticket-types`
      );
      setTicketTypeData(response.data);
    } catch (error) {
      console.error("Failed to fetch ticket type data:", error);
      // 에러 시 더미 데이터 사용
      setTicketTypeData([
        { type: "FREE", reservationCount: 15, peopleCount: 45, percentage: 60 },
        { type: "PAID", reservationCount: 10, peopleCount: 30, percentage: 40 },
      ]);
    }
  };

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      await Promise.all([fetchReservationData(period), fetchTicketTypeData()]);
      setLoading(false);
    };

    fetchData();
  }, [period]);

  const handlePeriodChange = (newPeriod) => {
    setPeriod(newPeriod);
  };

  // 서버가 내려주는 Content-Disposition에서 파일명 파싱
  const getFilenameFromDisposition = (disposition, fallback) => {
    try {
      if (!disposition) return fallback;
      // filename*=UTF-8''... 우선 처리
      const utf8Match = disposition.match(/filename\*\s*=\s*UTF-8''([^;]+)/i);
      if (utf8Match) return decodeURIComponent(utf8Match[1]);

      const asciiMatch = disposition.match(/filename\s*=\s*"?([^";]+)"?/i);
      if (asciiMatch) return asciiMatch[1];

      return fallback;
    } catch {
      return fallback;
    }
  };

  // CSV 다운로드 (인증 포함)
  const downloadCSV = async () => {
    try {
      const res = await api.get(
        `/admin/expos/${expoId}/dashboard/${period}/csv`,
        { responseType: "blob" }
      );
      const disposition = res.headers?.["content-disposition"];
      const fileName = getFilenameFromDisposition(
        disposition,
        `reservation_${period}_data.csv`
      );
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("CSV download failed:", error);
    }
  };

  // 엑셀 다운로드 (인증 포함)
  const downloadExcel = async () => {
    try {
      const res = await api.get(
        `/admin/expos/${expoId}/dashboard/${period}/excel`,
        { responseType: "blob" }
      );
      const disposition = res.headers?.["content-disposition"];
      const fileName = getFilenameFromDisposition(
        disposition,
        `reservation_${period}_data.xlsx`
      );
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("Excel download failed:", error);
    }
  };

  // 파이 차트 색상
  const PIE_COLORS = ["#8884d8", "#82ca9d", "#ffc658", "#ff7300"];

  // 파이 차트 데이터 변환 (peopleCount 기준)
  const pieChartData = ticketTypeData.map((item, index) => ({
    name: item.type === "FREE" ? "무료" : "유료",
    value: item.peopleCount,
    percentage: item.percentage,
    color: PIE_COLORS[index % PIE_COLORS.length],
  }));

  // 커스텀 툴팁 컴포넌트
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="custom-tooltip">
          <p className="tooltip-label">{`${label}`}</p>
          <p className="tooltip-value">{`예약수: ${payload[0].value}`}</p>
        </div>
      );
    }
    return null;
  };

  // 파이 차트 커스텀 툴팁
  const PieTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="pie-tooltip">
          <p className="tooltip-label">{data.name}</p>
          <p className="tooltip-value">{`인원수: ${data.value}명`}</p>
          <p className="tooltip-percentage">{`비율: ${data.percentage}%`}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="dashboard-chart">
      <div className="chart-section">
        {/* Bar Chart - Reservation Status */}
        <div className="bar-chart-container">
          <div className="chart-card">
            <div className="chart-header">
              <h2 className="chart-title">예약 현황</h2>

              <div className="download-buttons">
                {/* Download Buttons */}
                <button onClick={downloadCSV} className="download-btn csv-btn">
                  <span className="btn-text">CSV 다운로드</span>
                </button>

                <button
                  onClick={downloadExcel}
                  className="download-btn excel-btn"
                >
                  <span className="btn-text">엑셀 다운로드</span>
                </button>
              </div>
            </div>

            {/* Chart Period Toggle */}
            <div className="period-toggle-container">
              <div className="period-toggle">
                <button
                  onClick={() => handlePeriodChange("daily")}
                  className={`period-btn ${period === "daily" ? "active" : ""}`}
                >
                  일별
                </button>
                <button
                  onClick={() => handlePeriodChange("weekly")}
                  className={`period-btn ${
                    period === "weekly" ? "active" : ""
                  }`}
                >
                  주별
                </button>
                <button
                  onClick={() => handlePeriodChange("monthly")}
                  className={`period-btn ${
                    period === "monthly" ? "active" : ""
                  }`}
                >
                  월별
                </button>
              </div>
            </div>

            {/* Bar Chart */}
            <div className="chart-container">
              {loading ? (
                <div className="loading-container">
                  <div className="loading-text">로딩 중...</div>
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart
                    data={reservationData}
                    margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                  >
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis
                      dataKey="label"
                      tick={{ fontSize: 12 }}
                      stroke="#666"
                    />
                    <YAxis tick={{ fontSize: 12 }} stroke="#666" />
                    <Tooltip content={<CustomTooltip />} />
                    <Bar
                      dataKey="reservationCount"
                      fill="#8884d8"
                      radius={[4, 4, 0, 0]}
                      name="예약수"
                    />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>
        </div>

        {/* Pie Chart - Ticket Type Ratio */}
        <div className="pie-chart-container">
          <div className="chart-card">
            <h2 className="chart-title">티켓 종류별 예약 비율</h2>

            {/* Pie Chart */}
            <div className="chart-container">
              {loading ? (
                <div className="loading-container">
                  <div className="loading-text">로딩 중...</div>
                </div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieChartData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percentage }) => `${name} ${percentage}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {pieChartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip content={<PieTooltip />} />
                  </PieChart>
                </ResponsiveContainer>
              )}
            </div>

            {/* Legend */}
            {!loading && (
              <div className="legend-container">
                {pieChartData.map((item, index) => (
                  <div key={index} className="legend-item">
                    <div
                      className="legend-color"
                      style={{ backgroundColor: item.color }}
                    ></div>
                    <span className="legend-text">
                      {item.name}: {item.value}명 ({item.percentage}%)
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default DashboardChartSection;
