import { useEffect, useState } from "react";
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import SalesKpis from "../components/SalesKpis";
import { formatKRW, formatManwon, formatNumber } from "../components/adminFormat";

const AXIS = { fontSize: 12, fill: "#64748b" };
const Y_AXIS_WIDTH = 64; // 금액 라벨이 잘리지 않도록 고정 폭 (기존: "00,000" 처럼 잘림)
const PIE_COLORS = ["#2563eb", "#f43f5e"];

function ChartCard({ title, hint, empty, children }) {
  return (
    <section className="adm-card">
      <h2 className="adm-card__title">
        {title} {hint && <small>{hint}</small>}
      </h2>
      {empty ? <div className="adm-empty adm-empty--chart">표시할 데이터가 없습니다.</div> : <div className="adm-chart">{children}</div>}
    </section>
  );
}

function AmountBars({ data, xKey, xFormatter, color }) {
  return (
    <ResponsiveContainer width="100%" height="100%">
      <BarChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid stroke="#eef2f7" vertical={false} />
        <XAxis dataKey={xKey} tickFormatter={xFormatter} tick={AXIS} axisLine={false} tickLine={false} />
        <YAxis width={Y_AXIS_WIDTH} tickFormatter={formatManwon} tick={AXIS} axisLine={false} tickLine={false} />
        <Tooltip formatter={(v) => [formatKRW(v), "매출"]} labelFormatter={xFormatter} cursor={{ fill: "rgba(37,99,235,.06)" }} />
        <Bar dataKey="totalAmount" fill={color} radius={[6, 6, 0, 0]} maxBarSize={48} />
      </BarChart>
    </ResponsiveContainer>
  );
}

/** 매출 분석 — 공통 레이아웃 안 그리드 배치 (기존: 절대 좌표로 사이드바를 덮음) */
export default function SalesPage() {
  const { expoId } = useAdminExpo();
  const [charts, setCharts] = useState(null);

  useEffect(() => {
    let alive = true;
    const stats = (range) => api.get(`/admin/expos/${expoId}/stats`, { params: { range } }).then((r) => r.data ?? []);
    Promise.all([stats("daily"), stats("monthly"), stats("yearly"), api.get(`/admin/expos/${expoId}/sales`).then((r) => r.data)])
      .then(([daily, monthly, yearly, summary]) => {
        if (!alive) return;
        setCharts({
          daily: [...daily].sort((a, b) => String(a.date).localeCompare(String(b.date))),
          monthly: [...monthly].sort((a, b) => a.month - b.month),
          yearly: [...yearly].sort((a, b) => a.year - b.year),
          summary,
        });
      })
      .catch(() => alive && setCharts({ daily: [], monthly: [], yearly: [], summary: null }));
    return () => {
      alive = false;
    };
  }, [expoId]);

  const ratio = charts?.summary
    ? [
        { name: "정상 결제", value: charts.summary.reservationCount ?? 0 },
        { name: "환불 요청", value: charts.summary.refundCount ?? 0 },
      ]
    : [];

  return (
    <>
      <SalesKpis expoId={expoId} />
      {!charts ? (
        <div className="adm-empty">차트를 불러오는 중…</div>
      ) : (
        <>
          <div className="adm-grid-2">
            <ChartCard title="최근 7일 매출" hint="결제일 기준" empty={!charts.daily.length}>
              <AmountBars data={charts.daily} xKey="date" xFormatter={(d) => String(d).slice(5).replace("-", "/")} color="#2563eb" />
            </ChartCard>
            <ChartCard title="월별 매출" hint="올해" empty={!charts.monthly.length}>
              <AmountBars data={charts.monthly} xKey="month" xFormatter={(m) => `${m}월`} color="#6366f1" />
            </ChartCard>
          </div>
          <div className="adm-grid-2">
            <ChartCard title="연도별 매출" empty={!charts.yearly.length}>
              <AmountBars data={charts.yearly} xKey="year" xFormatter={(y) => `${y}년`} color="#0ea5e9" />
            </ChartCard>
            <ChartCard title="결제 · 환불 비율" hint="건수 기준" empty={!ratio.some((r) => r.value > 0)}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={ratio} dataKey="value" nameKey="name" innerRadius="55%" outerRadius="80%" paddingAngle={3}>
                    {ratio.map((entry, i) => (
                      <Cell key={entry.name} fill={PIE_COLORS[i]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v, name) => [`${formatNumber(v)}건`, name]} />
                  <Legend verticalAlign="bottom" iconType="circle" />
                </PieChart>
              </ResponsiveContainer>
            </ChartCard>
          </div>
        </>
      )}
    </>
  );
}
