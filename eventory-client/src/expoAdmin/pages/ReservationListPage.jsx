import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ScanLine, Search } from "lucide-react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import Pager from "../components/Pager";
import Modal from "../components/Modal";
import Flash, { useFlash } from "../components/Flash";
import { errorMessage, formatDateTime, formatNumber, formatPhone } from "../components/adminFormat";

const TABS = [
  { id: "ALL", label: "전체" },
  { id: "NOT_CHECKED_IN", label: "미입장" },
  { id: "CHECKED_IN", label: "입장 완료" },
];
const PAGE_SIZE = 10;

/** 예약자 명단 — 실제 예약 API 연동 (기존: 코드에 박힌 mock 데이터) */
export default function ReservationListPage() {
  const { expoId } = useAdminExpo();
  const [status, setStatus] = useState("ALL");
  const [keyword, setKeyword] = useState(""); // 입력 중인 검색어
  const [search, setSearch] = useState(""); // 적용된 검색어
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [busyId, setBusyId] = useState(null);
  const [cancelTarget, setCancelTarget] = useState(null);
  const [cancelReason, setCancelReason] = useState("");
  const [flash, showFlash] = useFlash();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/admin/expos/${expoId}/reservations`, {
        params: { status, search: search || undefined, page, size: PAGE_SIZE },
      });
      setData(res.data);
    } catch (e) {
      showFlash("error", errorMessage(e, "예약자 명단을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, [expoId, status, search, page, showFlash]);

  useEffect(() => {
    load();
  }, [load]);

  const changeTab = (id) => {
    setStatus(id);
    setPage(0);
  };

  const submitSearch = (e) => {
    e.preventDefault();
    setSearch(keyword.trim());
    setPage(0);
  };

  const clearSearch = () => {
    setKeyword("");
    setSearch("");
    setPage(0);
  };

  const checkIn = async (row) => {
    setBusyId(row.reservationId);
    try {
      await api.patch(`/admin/expos/${expoId}/reservations/${row.reservationId}/checkin`);
      showFlash("success", `${row.name}님 입장 처리했습니다.`);
      await load();
    } catch (e) {
      showFlash("error", errorMessage(e, "체크인에 실패했습니다."));
    } finally {
      setBusyId(null);
    }
  };

  const confirmCancel = async () => {
    const row = cancelTarget;
    setBusyId(row.reservationId);
    try {
      await api.post(`/admin/expos/${expoId}/reservations/${row.reservationId}/cancel`, { reason: cancelReason.trim() });
      showFlash("success", `예약 ${row.code}을(를) 취소하고 전액 환불했습니다.`);
      setCancelTarget(null);
      await load();
    } catch (e) {
      showFlash("error", errorMessage(e, "예약 취소에 실패했습니다."));
    } finally {
      setBusyId(null);
    }
  };

  const rows = data?.content ?? [];

  return (
    <>
      <Flash flash={flash} />
      <section className="adm-card">
        <div className="adm-toolbar">
          <div className="adm-tabs" role="tablist" aria-label="입장 상태">
            {TABS.map((t) => (
              <button
                key={t.id}
                role="tab"
                aria-selected={status === t.id}
                className={`adm-tab${status === t.id ? " is-active" : ""}`}
                onClick={() => changeTab(t.id)}
              >
                {t.label}
              </button>
            ))}
          </div>
          <div className="adm-toolbar__right">
            <form className="adm-search" onSubmit={submitSearch} role="search">
              <Search size={16} />
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="이름·전화번호·예약번호 검색 후 Enter"
                aria-label="예약자 검색"
              />
            </form>
            <Link to="/admin/reservation" className="adm-btn adm-btn--outline">
              <ScanLine size={16} />
              QR 체크인
            </Link>
          </div>
        </div>

        <div className="adm-table-meta">
          총 {formatNumber(data?.totalElements ?? 0)}건
          {search && (
            <>
              {" "}· “{search}” 검색 결과{" "}
              <button type="button" className="adm-link-btn" onClick={clearSearch}>
                검색 초기화
              </button>
            </>
          )}
        </div>

        <div className="adm-table-wrap">
          <table className="adm-table">
            <thead>
              <tr>
                <th>예약번호</th>
                <th>예약자</th>
                <th>연락처</th>
                <th>티켓</th>
                <th>예약일시</th>
                <th>입장 상태</th>
                <th>체크인 시각</th>
                <th className="is-right">관리</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => {
                const entered = r.status === "입장 완료";
                const busy = busyId === r.reservationId;
                return (
                  <tr key={r.reservationId}>
                    <td className="adm-mono">{r.code}</td>
                    <td>{r.name}</td>
                    <td>{formatPhone(r.phone)}</td>
                    <td>
                      <span className={`adm-badge adm-badge--${r.ticketType === "유료" ? "info" : "neutral"}`}>{r.ticketType}</span>
                    </td>
                    <td>{formatDateTime(r.reservedAt)}</td>
                    <td>
                      <span className={`adm-badge adm-badge--${entered ? "success" : "warning"}`}>{r.status}</span>
                    </td>
                    <td>{formatDateTime(r.lastCheckinAt)}</td>
                    <td className="is-right">
                      <div className="adm-row-actions">
                        <button className="adm-btn adm-btn--sm adm-btn--primary" disabled={entered || busy} onClick={() => checkIn(r)}>
                          체크인
                        </button>
                        <button
                          className="adm-btn adm-btn--sm adm-btn--danger-outline"
                          disabled={entered || busy}
                          title={entered ? "입장한 예약은 취소할 수 없습니다" : undefined}
                          onClick={() => {
                            setCancelTarget(r);
                            setCancelReason("");
                          }}
                        >
                          취소
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
              {!loading && rows.length === 0 && (
                <tr>
                  <td colSpan={8} className="adm-empty-cell">
                    조건에 맞는 예약이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </section>

      {cancelTarget && (
        <Modal
          title="예약 취소"
          onClose={() => setCancelTarget(null)}
          footer={
            <>
              <button className="adm-btn adm-btn--ghost" onClick={() => setCancelTarget(null)}>
                닫기
              </button>
              <button className="adm-btn adm-btn--danger" disabled={!cancelReason.trim() || busyId !== null} onClick={confirmCancel}>
                취소하고 전액 환불
              </button>
            </>
          }
        >
          <p className="adm-modal__lead">
            <b>{cancelTarget.name}</b>님의 예약 <span className="adm-mono">{cancelTarget.code}</span>을(를) 취소합니다.
            결제 금액이 전액 환불되며 되돌릴 수 없습니다.
          </p>
          <label className="adm-field">
            <span>취소 사유</span>
            <textarea
              className="adm-input"
              rows={3}
              maxLength={200}
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="예: 고객 요청으로 취소"
              autoFocus
            />
          </label>
        </Modal>
      )}
    </>
  );
}
