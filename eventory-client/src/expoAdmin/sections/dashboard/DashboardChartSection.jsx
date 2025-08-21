import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
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

/**
 * expoId는 우선 props로 받고, 없으면 URL(:expoId)에서 가져옵니다.
 * 이렇게 하면 Index에서 prop으로 내려줘도, 라우트 파라미터로만 써도 모두 안전하게 동작합니다.
 */
function DashboardChartSection({ expoId: expoIdProp }) {
  const { expoId: expoIdParam } = useParams();
  const expoId = expoIdProp ?? expoIdParam; // ✅ props 우선, 없으면 URL

  const [period, setPeriod] = useState("daily");
  const [reservationData, setReservationData] = useState([]);
  const [ticketTypeData, setTicketTypeData] = useState([]);
  const [loading, setLoading] = useState(true);

  const [expoName, setExpoName] = useState("");

  // ---- 파일 다운로드 공통 유틸 (단일 선언) ----
  function getFilenameFromDisposition(disposition, fallbackName) {
    if (!disposition) return fallbackName;
    // RFC 5987: filename*=UTF-8''...
    const utf8Match = /filename\*\s*=\s*UTF-8''([^;]+)/i.exec(disposition);
    if (utf8Match && utf8Match[1]) {
      try {
        return decodeURIComponent(utf8Match[1]);
      } catch {
        /* ignore */
      }
    }
    // 일반 filename="..."
    const asciiMatch =
      /filename\s*=\s*"(.*?)"/i.exec(disposition) ||
      /filename\s*=\s*([^;]+)/i.exec(disposition);
    if (asciiMatch && asciiMatch[1]) {
      return String(asciiMatch[1]).replace(/['"]/g, "").trim();
    }
    return fallbackName;
  }

  // 파일명 안전 처리 + 오늘 날짜 포맷 + 로컬 파일명 생성
  const sanitizeForFilename = (name) =>
    (name || "")
      .replace(/[\\/:*?"<>|]/g, "") // Windows 금지문자 제거
      .replace(/\s+/g, " ") // 공백 정리
      .trim();

  const formatToday = () => {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, "0");
    const dd = String(d.getDate()).padStart(2, "0");
    return `${yyyy}-${mm}-${dd}`;
  };

  const buildLocalReportName = (ext) => {
    const safeExpo = sanitizeForFilename(expoName) || "expo";
    return `${safeExpo}-${period}-dashboard-report-${formatToday()}.${ext}`;
  };

  // 박람회명 가져오기 (우선 단건 → 실패 시 목록에서 조회)
  const fetchExpoName = async () => {
    if (!expoId) return;
    try {
      // 단건 상세가 있다면: /admin/expos/{expoId}
      const detail = await api.get(
        `/admin/expos/${encodeURIComponent(expoId)}`
      );
      if (detail?.data?.name) {
        setExpoName(detail.data.name);
        return;
      }
    } catch (err) {
      console.warn("Failed to fetch expo detail:", err);
    }
    try {
      // fallback: /admin/expos 목록에서 찾아보기 (id 필드명이 다르면 맞게 수정)
      const list = await api.get(`/admin/expos`);
      const found =
        Array.isArray(list?.data) &&
        list.data.find(
          (x) => String(x.id ?? x.expoId) === String(expoId) // id 키 이름 유연 처리
        );
      if (found?.name) setExpoName(found.name);
    } catch (err) {
      console.warn("Failed to fetch expo name:", err);
    }
  };

  // 예약 현황 데이터 가져오기
  const fetchReservationData = async (selectedPeriod) => {
    if (!expoId) return; // 🔒 expoId 없으면 호출 금지
    try {
      const response = await api.get(
        `/admin/expos/${encodeURIComponent(expoId)}/dashboard/stats`,
        { params: { period: selectedPeriod } } // ✅ 쿼리는 params로
      );
      setReservationData(response.data);
    } catch (error) {
      console.error("Failed to fetch reservation data:", error);
      // TODO: 운영 전 더미데이터 제거 권장
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
    if (!expoId) return; // 🔒
    try {
      const response = await api.get(
        `/admin/expos/${encodeURIComponent(expoId)}/dashboard/ticket-types`
      );
      setTicketTypeData(response.data);
    } catch (error) {
      console.error("Failed to fetch ticket type data:", error);
      // TODO: 운영 전 더미데이터 제거 권장
      setTicketTypeData([
        { type: "FREE", reservationCount: 15, peopleCount: 45, percentage: 60 },
        { type: "PAID", reservationCount: 10, peopleCount: 30, percentage: 40 },
      ]);
    }
  };

  useEffect(() => {
    if (!expoId) return; // 🔒 expoId 없으면 API 호출하지 않음
    const fetchData = async () => {
      setLoading(true);
      // [CHANGED] 박람회명도 함께 로드
      await Promise.all([
        fetchExpoName(),
        fetchReservationData(period),
        fetchTicketTypeData(),
      ]);
      setLoading(false);
    };
    fetchData();
  }, [expoId, period]); // ✅ expoId를 의존성에 포함

  const handlePeriodChange = (newPeriod) => {
    setPeriod(newPeriod);
  };

  // ---- CSV 다운로드 (백엔드: /api/admin/expos/{expoId}/dashboard/{period}/csv) ----
  const downloadCSV = async () => {
    if (!expoId) return; // 🔒
    try {
      const urlPath = `/admin/expos/${encodeURIComponent(
        expoId
      )}/dashboard/${period}/csv`;
      const res = await api.get(urlPath, {
        responseType: "blob",
        headers: { Accept: "text/csv; charset=UTF-8" },
      });

      const disposition = res.headers?.["content-disposition"];
      console.log("Content-Disposition raw =>", disposition); // 네트워크 탭/콘솔 모두 확인
      // 헤더가 없으면 로컬 조립 파일명 사용
      const fileName = getFilenameFromDisposition(
        disposition,
        buildLocalReportName("csv")
      );
      console.log("Parsed filename =>", fileName);

      const blob = new Blob([res.data], { type: "text/csv;charset=utf-8" });
      const blobUrl = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = blobUrl;
      a.setAttribute("download", fileName);
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(blobUrl);
    } catch (error) {
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        console.error("인증 오류로 CSV 다운로드 실패:", error);
      } else {
        console.error("CSV download failed:", error);
      }
    }
  };

  // ---- 엑셀 다운로드 (백엔드: /api/admin/expos/{expoId}/dashboard/{period}/excel) ----
  const downloadExcel = async () => {
    if (!expoId) return; // 🔒
    try {
      const urlPath = `/admin/expos/${encodeURIComponent(
        expoId
      )}/dashboard/${period}/excel`;
      const res = await api.get(urlPath, {
        responseType: "blob",
        headers: {
          Accept:
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        },
      });

      const disposition = res.headers?.["content-disposition"];
      console.log("Content-Disposition raw =>", disposition); // 네트워크 탭/콘솔 모두 확인
      // 헤더가 없으면 로컬 조립 파일명 사용
      const fileName = getFilenameFromDisposition(
        disposition,
        buildLocalReportName("xlsx")
      );
      console.log("Parsed filename =>", fileName);

      const blob = new Blob([res.data], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      });
      const blobUrl = window.URL.createObjectURL(blob);

      const a = document.createElement("a");
      a.href = blobUrl;
      a.setAttribute("download", fileName);
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(blobUrl);
    } catch (error) {
      if (error?.response?.status === 401 || error?.response?.status === 403) {
        console.error("인증 오류로 Excel 다운로드 실패:", error);
      } else {
        console.error("Excel download failed:", error);
      }
    }
  };

  // 파이 차트 색상
  const PIE_COLORS = ["#FFC107", "#007BFF", "#ffc658", "#ff7300"];

  // 파이 차트 데이터 변환 (peopleCount 기준)
  const pieChartData = ticketTypeData.map((item, index) => ({
    name: item.type === "FREE" ? "무료" : "유료",
    value: item.peopleCount,
    percentage: item.percentage,
    color: PIE_COLORS[index % PIE_COLORS.length],
  }));

  // 커스텀 툴팁 컴포넌트
  const CustomTooltip = ({ active, payload, label }) => {
    return active && payload && payload.length ? (
      <div className="custom-tooltip">
        <p className="tooltip-label">{`${label}`}</p>
        <p className="tooltip-value">{`예약수: ${payload[0].value}`}</p>
      </div>
    ) : null;
  };

  // 파이 차트 커스텀 툴팁
  const PieTooltip = ({ active, payload }) => {
    return active && payload && payload.length ? (
      <div className="pie-tooltip">
        <p className="tooltip-label">{payload[0].payload.name}</p>
        <p className="tooltip-value">{`인원수: ${payload[0].payload.value}명`}</p>
        <p className="tooltip-percentage">{`비율: ${payload[0].payload.percentage}%`}</p>
      </div>
    ) : null;
  };

  // expoId 없으면 화면만 그리고 API는 건너뜀 (사용자에게 안내)
  if (!expoId) {
    return (
      <div className="dashboard-chart">
        <div className="chart-section">
          <div className="chart-card" style={{ padding: 16 }}>
            유효한 박람회 ID가 없습니다. 경로 예:{" "}
            <code>/admin/expos/1/dashboard</code>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-chart">
      <div className="chart-section">
        {/* Bar Chart - Reservation Status */}
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
                      fill="#007BFF"
                      radius={[4, 4, 0, 0]}
                      name="예약수"
                    />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

        {/* Pie Chart - Ticket Type Ratio */}
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
  );
}

export default DashboardChartSection;
