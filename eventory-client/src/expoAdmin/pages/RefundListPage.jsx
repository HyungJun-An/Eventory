import { useCallback, useEffect, useState } from "react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import SalesKpis from "../components/SalesKpis";
import Pager from "../components/Pager";
import Modal from "../components/Modal";
import Flash, { useFlash } from "../components/Flash";
import { errorMessage, formatDateTime, formatKRW, formatNumber } from "../components/adminFormat";

const PAGE_SIZE = 10;
const TABS = [
  { id: "ALL", label: "전체" },
  { id: "PENDING", label: "처리 대기" },
  { id: "APPROVED", label: "환불 완료" },
  { id: "REJECTED", label: "반려" },
];
const STATUS_META = {
  PENDING: { label: "처리 대기", tone: "warning" },
  APPROVED: { label: "환불 완료", tone: "success" },
  REJECTED: { label: "반려", tone: "danger" },
};
const REJECT_REASONS = ["환불 요청 시점이 환불 가능 기간을 초과하였습니다.", "환불 정책에 따라 환불이 불가합니다."];
const CUSTOM = "CUSTOM";

/** 환불 처리 — 상태별 조회·페이지 이동·승인(실제 환불)·반려 (기존: 승인 시 오류, 7건만 표시) */
export default function RefundListPage() {
  const { expoId } = useAdminExpo();
  const [status, setStatus] = useState("ALL");
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [action, setAction] = useState(null); // { type: "APPROVE" | "REJECT", refund }
  const [reasonOption, setReasonOption] = useState(REJECT_REASONS[0]);
  const [customReason, setCustomReason] = useState("");
  const [busy, setBusy] = useState(false);
  const [kpiVersion, setKpiVersion] = useState(0); // 처리 후 요약 카드 갱신용
  const [flash, showFlash] = useFlash();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/admin/expos/${expoId}/refund`, {
        params: { status: status === "ALL" ? undefined : status, page, size: PAGE_SIZE },
      });
      setData(res.data);
    } catch (e) {
      showFlash("error", errorMessage(e, "환불 내역을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, [expoId, status, page, showFlash]);

  useEffect(() => {
    load();
  }, [load]);

  const openAction = (type, refund) => {
    setAction({ type, refund });
    setReasonOption(REJECT_REASONS[0]);
    setCustomReason("");
  };

  const rejectReason = reasonOption === CUSTOM ? customReason.trim() : reasonOption;

  const submit = async () => {
    const { type, refund } = action;
    setBusy(true);
    try {
      await api.patch(
        `/admin/refund/${refund.refundId}`,
        type === "APPROVE" ? { status: "APPROVED" } : { status: "REJECTED", reason: rejectReason }
      );
      showFlash(
        "success",
        type === "APPROVE" ? `${refund.code} 환불 승인 — ${formatKRW(refund.amount)} 환불 처리했습니다.` : `${refund.code} 환불 요청을 반려했습니다.`
      );
      setAction(null);
      setKpiVersion((v) => v + 1);
      await load();
    } catch (e) {
      showFlash("error", errorMessage(e, "처리에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  const rows = data?.content ?? [];

  return (
    <>
      <Flash flash={flash} />
      <SalesKpis key={kpiVersion} expoId={expoId} />
      <section className="adm-card">
        <div className="adm-toolbar">
          <div className="adm-tabs" role="tablist" aria-label="환불 상태">
            {TABS.map((t) => (
              <button
                key={t.id}
                role="tab"
                aria-selected={status === t.id}
                className={`adm-tab${status === t.id ? " is-active" : ""}`}
                onClick={() => {
                  setStatus(t.id);
                  setPage(0);
                }}
              >
                {t.label}
              </button>
            ))}
          </div>
        </div>

        <div className="adm-table-meta">총 {formatNumber(data?.totalElements ?? 0)}건</div>
        <div className="adm-table-wrap">
          <table className="adm-table">
            <thead>
              <tr>
                <th>예약번호</th>
                <th>결제수단</th>
                <th className="is-right">결제 금액</th>
                <th>결제일시</th>
                <th>사유</th>
                <th>상태</th>
                <th className="is-right">처리</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => {
                const meta = STATUS_META[r.status] ?? { label: r.status, tone: "neutral" };
                return (
                  <tr key={r.refundId}>
                    <td className="adm-mono">{r.code}</td>
                    <td>{r.method}</td>
                    <td className="is-right adm-strong">{formatKRW(r.amount)}</td>
                    <td>{formatDateTime(r.paidAt)}</td>
                    <td className="is-wrap">{r.reason}</td>
                    <td>
                      <span className={`adm-badge adm-badge--${meta.tone}`}>{meta.label}</span>
                    </td>
                    <td className="is-right">
                      {r.status === "PENDING" ? (
                        <div className="adm-row-actions">
                          <button className="adm-btn adm-btn--sm adm-btn--danger-outline" onClick={() => openAction("REJECT", r)}>
                            반려
                          </button>
                          <button className="adm-btn adm-btn--sm adm-btn--primary" onClick={() => openAction("APPROVE", r)}>
                            승인
                          </button>
                        </div>
                      ) : (
                        <span className="adm-muted">처리 완료</span>
                      )}
                    </td>
                  </tr>
                );
              })}
              {!loading && rows.length === 0 && (
                <tr>
                  <td colSpan={7} className="adm-empty-cell">
                    해당 상태의 환불 내역이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </section>

      {action?.type === "APPROVE" && (
        <Modal
          title="환불 승인"
          onClose={() => setAction(null)}
          footer={
            <>
              <button className="adm-btn adm-btn--ghost" onClick={() => setAction(null)}>
                닫기
              </button>
              <button className="adm-btn adm-btn--primary" disabled={busy} onClick={submit}>
                {busy ? "처리 중…" : `${formatKRW(action.refund.amount)} 환불`}
              </button>
            </>
          }
        >
          <p className="adm-modal__lead">
            예약 <span className="adm-mono">{action.refund.code}</span>의 결제 금액 <b>{formatKRW(action.refund.amount)}</b>을(를) 전액 환불하고
            예약을 취소합니다. 되돌릴 수 없습니다.
          </p>
          <dl className="adm-dl adm-dl--compact">
            <dt>환불 사유</dt>
            <dd>{action.refund.reason}</dd>
          </dl>
        </Modal>
      )}

      {action?.type === "REJECT" && (
        <Modal
          title="환불 요청 반려"
          onClose={() => setAction(null)}
          footer={
            <>
              <button className="adm-btn adm-btn--ghost" onClick={() => setAction(null)}>
                닫기
              </button>
              <button className="adm-btn adm-btn--danger" disabled={busy || !rejectReason} onClick={submit}>
                {busy ? "처리 중…" : "반려하기"}
              </button>
            </>
          }
        >
          <p className="adm-modal__lead">
            예약 <span className="adm-mono">{action.refund.code}</span>의 환불 요청을 반려합니다. 반려 사유를 선택하세요.
          </p>
          {REJECT_REASONS.map((text) => (
            <label key={text} className="adm-radio">
              <input type="radio" name="reject-reason" checked={reasonOption === text} onChange={() => setReasonOption(text)} />
              <span>{text}</span>
            </label>
          ))}
          <label className="adm-radio">
            <input type="radio" name="reject-reason" checked={reasonOption === CUSTOM} onChange={() => setReasonOption(CUSTOM)} />
            <span>직접 입력</span>
          </label>
          {reasonOption === CUSTOM && (
            <textarea className="adm-input" rows={3} maxLength={200} value={customReason} onChange={(e) => setCustomReason(e.target.value)} placeholder="반려 사유를 입력하세요" autoFocus />
          )}
        </Modal>
      )}
    </>
  );
}
