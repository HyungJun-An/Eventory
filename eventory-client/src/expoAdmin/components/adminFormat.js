// 관리자 화면 공통 포맷/유틸

const pad = (n) => String(n).padStart(2, "0");

/** 로컬 기준 오늘 (YYYY-MM-DD) — 서버 날짜 문자열과 그대로 비교하기 위해 문자열로 만든다 */
export const todayString = () => {
  const d = new Date();
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
};

export const formatNumber = (v) => Number(v ?? 0).toLocaleString("ko-KR");

export const formatKRW = (v) => `${formatNumber(v)}원`;

/** 차트 축용 짧은 금액 (12,500,000 → 1,250만) */
export const formatManwon = (v) =>
  Math.abs(v) >= 10000 ? `${(v / 10000).toLocaleString("ko-KR")}만` : formatNumber(v);

/** "2026-09-10T15:57:05" 또는 "2026-09-10 15:57:05" → "2026.09.10 15:57" */
export const formatDateTime = (v) => {
  if (!v) return "-";
  const d = new Date(String(v).replace(" ", "T"));
  if (Number.isNaN(d.getTime())) return String(v);
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

/** "2026-09-10" → "2026.09.10" */
export const formatDate = (v) => (v ? String(v).slice(0, 10).replaceAll("-", ".") : "-");

export const formatPhone = (v) => {
  const s = String(v ?? "").replace(/\D/g, "");
  if (s.length === 11) return `${s.slice(0, 3)}-${s.slice(3, 7)}-${s.slice(7)}`;
  if (s.length === 10) return `${s.slice(0, 3)}-${s.slice(3, 6)}-${s.slice(6)}`;
  return v || "-";
};

/** 백엔드 ErrorResponse({ errorCode, message })의 메시지를 우선 사용 */
export const errorMessage = (err, fallback = "요청 처리 중 오류가 발생했습니다.") =>
  err?.response?.data?.message || fallback;

/** 박람회 진행 상태: 진행 중 / D-n / 종료 */
export const expoPhase = (expo) => {
  if (!expo?.startDate || !expo?.endDate) return { label: "-", tone: "neutral" };
  const today = todayString();
  if (today > expo.endDate) return { label: "종료", tone: "neutral" };
  if (today >= expo.startDate) return { label: "진행 중", tone: "success" };
  const days = Math.round((new Date(`${expo.startDate}T00:00:00`) - new Date(`${today}T00:00:00`)) / 86400000);
  return { label: `D-${days}`, tone: "info" };
};
