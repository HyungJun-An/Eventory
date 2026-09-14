import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { CalendarCheck2, Download, LogIn, UserPlus, Wallet } from "lucide-react";
import { getSysChart, getSysStats } from "../../api/sysAdminApi";
import Flash, { useFlash } from "../../expoAdmin/components/Flash";
import { errorMessage, formatKRW, formatManwon, formatNumber } from "../../expoAdmin/components/adminFormat";

const PERIODS = [
  { id: "monthly", label: "월별" },
  { id: "weekly", label: "주별" },
  { id: "daily", label: "일별" },
];
const AXIS = { fontSize: 12, fill: "#64748b" };

/** 기간 라벨: 월별 "2026-09" → "2026.09" / 주별 YEARWEEK 202637 → "26년 37주" / 일별 "2026-09-10" → "09.10" */
const periodLabel = (period, value) => {
  const s = String(value ?? "");
  if (period === "weekly") return `${s.slice(2, 4)}년 ${Number(s.slice(4))}주`;
  if (period === "daily") return s.slice(5).replace("-", ".");
  return s.replace("-", ".");
};

/** 결제·예약·입장 목록을 날짜 기준으로 합쳐 CSV 로 저장 (엑셀에서 한글이 깨지지 않도록 BOM 포함) */
function downloadCsv(period, chart) {
  const rows = new Map();
  const put = (list, key) => (list ?? []).forEach(({ date, uv }) => rows.set(date, { ...(rows.get(date) ?? {}), [key]: uv }));
  put(chart.paymentList, "payment");
  put(chart.reservationList, "reservation");
  put(chart.checkInList, "checkIn");
  const lines = [["기간", "결제 금액(원)", "예약 건수", "입장 건수"]];
  [...rows.keys()].sort().forEach((d) => {
    const r = rows.get(d);
    lines.push([periodLabel(period, d), r.payment ?? 0, r.reservation ?? 0, r.checkIn ?? 0]);
  });
  const csv = "﻿" + lines.map((l) => l.map((v) => `"${String(v).replaceAll('"', '""')}"`).join(",")).join("\n");
  const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
  const a = document.createElement("a");
  a.href = url;
  a.download = `eventory-platform-${period}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

function ChartCard({ title, hint, data, period, color, formatter = formatNumber, yFormatter = formatNumber }) {
  const sorted = [...(data ?? [])].sort((a, b) => String(a.date).localeCompare(String(b.date)));
  return (
    <section className="adm-card">
      <h2 className="adm-card__title">
        {title} {hint && <small>{hint}</small>}
      </h2>
      {sorted.length === 0 ? (
        <div className="adm-empty adm-empty--chart">표시할 데이터가 없습니다.</div>
      ) : (
        <div className="adm-chart">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={sorted} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
              <CartesianGrid stroke="#eef2f7" vertical={false} />
              <XAxis dataKey="date" tickFormatter={(v) => periodLabel(period, v)} tick={AXIS} axisLine={false} tickLine={false} />
              <YAxis width={64} tickFormatter={yFormatter} tick={AXIS} axisLine={false} tickLine={false} allowDecimals={false} />
              <Tooltip formatter={(v) => [formatter(v), title]} labelFormatter={(v) => periodLabel(period, v)} cursor={{ fill: "rgba(37,99,235,.06)" }} />
              <Bar dataKey="uv" fill={color} radius={[6, 6, 0, 0]} maxBarSize={48} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </section>
  );
}

/** 플랫폼 대시보드 — 전체 결제·예약·입장 지표와 기간별 추이 */
export default function SysDashboardPage() {
  const [stats, setStats] = useState(null);
  const [period, setPeriod] = useState("monthly");
  const [chart, setChart] = useState(null);
  const [flash, showFlash] = useFlash();

  useEffect(() => {
    getSysStats()
      .then(setStats)
      .catch((e) => showFlash("error", errorMessage(e, "통계를 불러오지 못했습니다.")));
  }, [showFlash]);

  useEffect(() => {
    let alive = true;
    setChart(null);
    getSysChart(period)
      .then((d) => alive && setChart(d))
      .catch((e) => alive && showFlash("error", errorMessage(e, "차트를 불러오지 못했습니다.")));
    return () => {
      alive = false;
    };
  }, [period, showFlash]);

  const v = (fn) => (stats ? fn(stats) : "…");
  const kpis = [
    { label: "총 결제 금액", value: v((s) => formatKRW(s.totalPaymentAmount)), icon: Wallet, tone: "blue" },
    { label: "누적 예약", value: v((s) => `${formatNumber(s.totalReservationCount)}건`), icon: CalendarCheck2, tone: "green" },
    { label: "누적 입장", value: v((s) => `${formatNumber(s.totalCheckInCount)}건`), icon: LogIn, tone: "violet" },
    { label: "오늘 신규 가입", value: v((s) => `${formatNumber(s.todayNewUser)}명`), icon: UserPlus, tone: "red" },
  ];

  return (
    <>
      <Flash flash={flash} />
      <div className="adm-kpis adm-kpis--4">
        {kpis.map(({ label, value, icon: Icon, tone }) => (
          <div className="adm-kpi" key={label}>
            <span className={`adm-kpi__icon adm-kpi__icon--${tone}`}>
              <Icon size={20} />
            </span>
            <div>
              <div className="adm-kpi__label">{label}</div>
              <div className="adm-kpi__value">{value}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="adm-toolbar">
        <div className="adm-tabs" role="tablist" aria-label="집계 기간">
          {PERIODS.map((p) => (
            <button key={p.id} role="tab" aria-selected={period === p.id} className={`adm-tab${period === p.id ? " is-active" : ""}`} onClick={() => setPeriod(p.id)}>
              {p.label}
            </button>
          ))}
        </div>
        <button type="button" className="adm-btn adm-btn--outline" disabled={!chart} onClick={() => downloadCsv(period, chart)}>
          <Download size={16} />
          CSV 다운로드
        </button>
      </div>

      {!chart ? (
        <div className="adm-empty">차트를 불러오는 중…</div>
      ) : (
        <>
          <ChartCard title="결제 금액" hint="환불 포함 전체 결제" data={chart.paymentList} period={period} color="#2563eb" formatter={formatKRW} yFormatter={formatManwon} />
          <div className="adm-grid-2">
            <ChartCard title="예약 건수" data={chart.reservationList} period={period} color="#16a34a" formatter={(x) => `${formatNumber(x)}건`} />
            <ChartCard title="입장 건수" data={chart.checkInList} period={period} color="#7c3aed" formatter={(x) => `${formatNumber(x)}건`} />
          </div>
        </>
      )}
    </>
  );
}
