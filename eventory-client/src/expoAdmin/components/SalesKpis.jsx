import { useEffect, useState } from "react";
import { CreditCard, Eye, RotateCcw, Wallet } from "lucide-react";
import api from "../../api/axiosInstance";
import { formatKRW, formatNumber } from "./adminFormat";

/** 매출 분석·정산·환불 화면 상단 공통 요약 카드 (기존 3벌 복제 컴포넌트 + 깨진 외부 아이콘 대체) */
export default function SalesKpis({ expoId }) {
  const [data, setData] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    let alive = true;
    api
      .get(`/admin/expos/${expoId}/sales`)
      .then((res) => alive && setData(res.data))
      .catch(() => alive && setFailed(true));
    return () => {
      alive = false;
    };
  }, [expoId]);

  const value = (fn) => (data ? fn(data) : failed ? "-" : "…");
  const items = [
    { label: "누적 매출", value: value((d) => formatKRW(d.paymentTotal)), icon: Wallet, tone: "blue" },
    { label: "결제 건수", value: value((d) => `${formatNumber(d.reservationCount)}건`), icon: CreditCard, tone: "green" },
    { label: "환불 건수", value: value((d) => `${formatNumber(d.refundCount)}건`), icon: RotateCcw, tone: "red" },
    { label: "페이지 조회", value: value((d) => formatNumber(d.viewCount)), icon: Eye, tone: "violet" },
  ];

  return (
    <div className="adm-kpis">
      {items.map(({ label, value: v, icon: Icon, tone }) => (
        <div className="adm-kpi" key={label}>
          <span className={`adm-kpi__icon adm-kpi__icon--${tone}`}>
            <Icon size={20} />
          </span>
          <div>
            <div className="adm-kpi__label">{label}</div>
            <div className="adm-kpi__value">{v}</div>
          </div>
        </div>
      ))}
    </div>
  );
}
