import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import api from "../../../api/axiosInstance";
import {
  PageViewIcon,
  ReservationIcon,
  EntranceRateIcon,
} from "./DashboardStatsIcons";
import "../../../assets/css/dashboard/DashboardStatsCard.css";

// ──────────────────────────────────────────────────────────────
// 간단 캐시: 같은 expoId에 대해 카드 3개가 동시에 마운트되어도
// API를 1번만 호출하도록 모듈 스코프 캐시(Map) 사용
// 구조: cache.get(expoId) -> { data, error, promise }
// ──────────────────────────────────────────────────────────────
const summaryCache = new Map();

// 숫자 포맷(안전)
const formatKR = (num) => {
  const n = typeof num === "number" && Number.isFinite(num) ? num : 0;
  return n.toLocaleString("ko-KR");
};
const formatPercent = (val) => {
  const n = Number.isFinite(val) ? Number(val) : 0;
  return `${n.toFixed(1)}%`;
};

/**
 * 공통 훅
 * - expoId는 props로 받은 값을 우선 사용, 없으면 URL(:expoId) 사용
 * - expoId가 없으면 API 호출하지 않음(가드)
 */
function useDashboardSummary(expoIdProp) {
  const { expoId: expoIdParam } = useParams() || {};
  const expoId = expoIdProp ?? expoIdParam; // ✅ props > URL

  const [state, setState] = useState(() => {
    if (!expoId) {
      return {
        data: null,
        error: new Error("expoId not found"),
        loading: false,
      };
    }
    const entry = summaryCache.get(expoId);
    return {
      data: entry?.data ?? null,
      error: entry?.error ?? null,
      loading: !entry?.data && !entry?.error, // 데이터가 없으면 로딩 시작
    };
  });

  useEffect(() => {
    if (!expoId) {
      setState({
        data: null,
        error: new Error("expoId not found"),
        loading: false,
      });
      return;
    }

    let active = true;
    const entry = summaryCache.get(expoId);

    // 캐시 히트: 즉시 반영
    if (entry?.data || entry?.error) {
      setState({
        data: entry.data ?? null,
        error: entry.error ?? null,
        loading: false,
      });
      return;
    }

    if (entry?.promise) {
      // 진행 중인 요청이 있으면 해당 promise를 기다림
      setState((s) => ({ ...s, loading: true }));
      entry.promise
        .then(
          (data) => active && setState({ data, error: null, loading: false })
        )
        .catch(
          (err) =>
            active && setState({ data: null, error: err, loading: false })
        );
      return;
    }

    // 최초 요청: promise를 캐시에 저장하여 중복 방지
    const promise = api
      .get(`/admin/expos/${encodeURIComponent(expoId)}/dashboard/summary`)
      .then((res) => {
        const data = res?.data ?? null;
        summaryCache.set(expoId, { data, error: null, promise: null });
        return data;
      })
      .catch((err) => {
        summaryCache.set(expoId, { data: null, error: err, promise: null });
        throw err;
      });

    summaryCache.set(expoId, { data: null, error: null, promise });

    setState((s) => ({ ...s, loading: true }));

    promise
      .then((data) => active && setState({ data, error: null, loading: false }))
      .catch(
        (err) => active && setState({ data: null, error: err, loading: false })
      );

    return () => {
      active = false;
    };
  }, [expoId]);

  return { ...state, expoId };
}

// ▼▼▼ 카드 컴포넌트(이제 expoId props를 받아 훅에 전달) ▼▼▼
export function PageViewsCard({ expoId }) {
  const { data, loading, error } = useDashboardSummary(expoId);
  const value = loading
    ? "로딩중..."
    : error
    ? "에러"
    : formatKR(data?.viewCount);
  return (
    <DashboardStatsCard
      title="페이지 조회 수"
      value={value}
      icon={<PageViewIcon />}
    />
  );
}

export function TotalReservationsCard({ expoId }) {
  const { data, loading, error } = useDashboardSummary(expoId);
  const value = loading
    ? "로딩중..."
    : error
    ? "에러"
    : formatKR(data?.totalReservation);
  return (
    <DashboardStatsCard
      title="총 예약 수"
      value={value}
      icon={<ReservationIcon />}
    />
  );
}

export function EntranceRateCard({ expoId }) {
  const { data, loading, error } = useDashboardSummary(expoId);
  const value = loading
    ? "로딩중..."
    : error
    ? "에러"
    : formatPercent(data?.checkInRate);
  return (
    <div className="entrance-rate-wrapper">
      <DashboardStatsCard
        title="입장률"
        value={value}
        icon={<EntranceRateIcon />}
      />
    </div>
  );
}

// 기본 DashboardStatsCard (표시 전용)
export function DashboardStatsCard({
  title,
  value,
  icon,
  backgroundColor = "bg-white",
}) {
  return (
    <div className={`dashboard-stats stats-card ${backgroundColor}`}>
      <div className="stats-card-content">
        {/* Icon */}
        <div className="icon-container">
          <div className="icon-wrapper">{icon}</div>
        </div>
        {/* Content */}
        <div className="text-content">
          <h3 className="title">{title}</h3>
          <p className="value">{value}</p>
        </div>
      </div>
    </div>
  );
}

export default DashboardStatsCard;
