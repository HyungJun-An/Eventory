import { useEffect, useState } from "react";
import { Download, Search } from "lucide-react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import SalesKpis from "../components/SalesKpis";
import Pager from "../components/Pager";
import Flash, { useFlash } from "../components/Flash";
import { errorMessage, formatDateTime, formatKRW, formatNumber } from "../components/adminFormat";

const PAGE_SIZE = 10;
const EMPTY_FILTER = { code: "", startDate: "", endDate: "" };

/** 정산 관리 — 결제 내역 검색·기간 조회·페이지 이동·엑셀 다운로드 (기존: 검색·필터·페이지 미동작) */
export default function PaymentListPage() {
  const { expoId, expo } = useAdminExpo();
  const [filters, setFilters] = useState(EMPTY_FILTER); // 입력 중
  const [applied, setApplied] = useState(EMPTY_FILTER); // 조회에 적용됨
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [downloading, setDownloading] = useState(false);
  const [flash, showFlash] = useFlash();

  useEffect(() => {
    let alive = true;
    setLoading(true);
    api
      .get(`/admin/expos/${expoId}/payment`, {
        params: {
          page,
          size: PAGE_SIZE,
          code: applied.code || undefined,
          startDate: applied.startDate || undefined,
          endDate: applied.endDate || undefined,
        },
      })
      .then((res) => alive && setData(res.data))
      .catch((e) => alive && showFlash("error", errorMessage(e, "결제 내역을 불러오지 못했습니다.")))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [expoId, page, applied, showFlash]);

  const rangeError = filters.startDate && filters.endDate && filters.endDate < filters.startDate;
  const filtering = Boolean(applied.code || applied.startDate || applied.endDate);
  const setField = (key) => (e) => setFilters((f) => ({ ...f, [key]: e.target.value }));

  const apply = (e) => {
    e.preventDefault();
    if (rangeError) return;
    setApplied({ ...filters, code: filters.code.trim() });
    setPage(0);
  };

  const reset = () => {
    setFilters(EMPTY_FILTER);
    setApplied(EMPTY_FILTER);
    setPage(0);
  };

  const download = async () => {
    setDownloading(true);
    try {
      const res = await api.post(`/admin/expos/${expoId}/payment/report`, {}, { responseType: "blob" });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${expo?.title ?? "expo"}-결제내역.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch {
      showFlash("error", "엑셀 다운로드에 실패했습니다.");
    } finally {
      setDownloading(false);
    }
  };

  const rows = data?.content ?? [];

  return (
    <>
      <Flash flash={flash} />
      <SalesKpis expoId={expoId} />
      <section className="adm-card">
        <form className="adm-toolbar" onSubmit={apply}>
          <div className="adm-filter">
            <div className="adm-search">
              <Search size={16} />
              <input value={filters.code} onChange={setField("code")} placeholder="예약번호 검색" aria-label="예약번호 검색" />
            </div>
            <input type="date" className="adm-input adm-input--date" value={filters.startDate} onChange={setField("startDate")} aria-label="결제 시작일" />
            <span className="adm-filter__sep">~</span>
            <input type="date" className="adm-input adm-input--date" value={filters.endDate} min={filters.startDate || undefined} onChange={setField("endDate")} aria-label="결제 종료일" />
            <button type="submit" className="adm-btn adm-btn--primary" disabled={rangeError}>
              조회
            </button>
            {filtering && (
              <button type="button" className="adm-btn adm-btn--ghost" onClick={reset}>
                초기화
              </button>
            )}
          </div>
          <button type="button" className="adm-btn adm-btn--outline" onClick={download} disabled={downloading}>
            <Download size={16} />
            {downloading ? "준비 중…" : "엑셀 다운로드"}
          </button>
        </form>
        {rangeError && <p className="adm-field-error">종료일은 시작일 이후여야 합니다.</p>}

        <div className="adm-table-meta">총 {formatNumber(data?.totalElements ?? 0)}건 {filtering && "· 조건 적용됨"}</div>
        <div className="adm-table-wrap">
          <table className="adm-table">
            <thead>
              <tr>
                <th>No</th>
                <th>예약번호</th>
                <th>예약자</th>
                <th className="is-right">인원</th>
                <th>결제수단</th>
                <th className="is-right">결제 금액</th>
                <th>결제일시</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((p, i) => (
                <tr key={`${p.code}-${i}`}>
                  <td className="adm-muted">{page * PAGE_SIZE + i + 1}</td>
                  <td className="adm-mono">{p.code}</td>
                  <td>{p.name}</td>
                  <td className="is-right">{p.people}명</td>
                  <td>{p.method}</td>
                  <td className="is-right adm-strong">{formatKRW(p.amount)}</td>
                  <td>{formatDateTime(p.paidAt)}</td>
                </tr>
              ))}
              {!loading && rows.length === 0 && (
                <tr>
                  <td colSpan={7} className="adm-empty-cell">
                    조건에 맞는 결제 내역이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </section>
    </>
  );
}
