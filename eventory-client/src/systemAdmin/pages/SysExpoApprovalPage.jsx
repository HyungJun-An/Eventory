import { useCallback, useEffect, useState } from "react";
import { Search } from "lucide-react";
import { approveSysExpo, getSysExpoDetail, getSysExpos, rejectSysExpo } from "../../api/sysAdminApi";
import Modal from "../../expoAdmin/components/Modal";
import Pager from "../../expoAdmin/components/Pager";
import Flash, { useFlash } from "../../expoAdmin/components/Flash";
import { errorMessage, formatDate, formatDateTime, formatKRW, formatNumber, formatPhone } from "../../expoAdmin/components/adminFormat";
import CredentialModal from "./CredentialModal";

const PAGE_SIZE = 10;
const TABS = [
  { id: "ALL", label: "전체" },
  { id: "PENDING", label: "승인 대기" },
  { id: "APPROVED", label: "승인" },
  { id: "REJECTED", label: "반려" },
];
const STATUS_META = {
  PENDING: { label: "승인 대기", tone: "warning" },
  APPROVED: { label: "승인", tone: "success" },
  REJECTED: { label: "반려", tone: "danger" },
};

/** 박람회 신청 심사 모달 — 상세 정보 확인 후 승인·반려 */
function ExpoReviewModal({ expoId, onClose, onDone }) {
  const [expo, setExpo] = useState(null);
  const [error, setError] = useState("");
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    getSysExpoDetail(expoId)
      .then(setExpo)
      .catch((e) => setError(errorMessage(e, "박람회 정보를 불러오지 못했습니다.")));
  }, [expoId]);

  const act = async (fn, message) => {
    setBusy(true);
    setError("");
    try {
      const result = await fn();
      onDone(message, result?.credential);
    } catch (e) {
      setError(errorMessage(e, "처리에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  const pending = expo?.status === "PENDING";
  const meta = STATUS_META[expo?.status] ?? { label: expo?.status, tone: "neutral" };

  return (
    <Modal
      title="박람회 신청 심사"
      wide
      onClose={onClose}
      footer={
        pending && (
          <>
            {rejecting ? (
              <>
                <button className="adm-btn adm-btn--ghost" onClick={() => setRejecting(false)} disabled={busy}>
                  돌아가기
                </button>
                <button className="adm-btn adm-btn--danger" disabled={busy || !reason.trim()} onClick={() => act(() => rejectSysExpo(expoId, reason.trim()), `${expo.title} 신청을 반려했습니다.`)}>
                  반려 확정
                </button>
              </>
            ) : (
              <>
                <button className="adm-btn adm-btn--danger-outline" onClick={() => setRejecting(true)} disabled={busy}>
                  반려
                </button>
                <button className="adm-btn adm-btn--primary" disabled={busy} onClick={() => act(() => approveSysExpo(expoId), `${expo.title}을(를) 승인했습니다.`)}>
                  {busy ? "처리 중…" : "승인"}
                </button>
              </>
            )}
          </>
        )
      }
    >
      {!expo ? (
        <p className="adm-hint">{error || "불러오는 중…"}</p>
      ) : (
        <div className="adm-modal__body--scroll">
          <div className="adm-review">
            <img className="adm-review__poster" src={expo.imageUrl} alt="" onError={(e) => (e.currentTarget.style.visibility = "hidden")} />
            <div>
              <h3 style={{ margin: "0 0 10px", fontSize: 18 }}>
                {expo.title} <span className={`adm-badge adm-badge--${meta.tone}`}>{meta.label}</span>
              </h3>
              <dl className="adm-dl">
                <dt>기간</dt>
                <dd>
                  {formatDate(expo.startDate)} ~ {formatDate(expo.endDate)}
                </dd>
                <dt>장소</dt>
                <dd>{expo.location}</dd>
                <dt>카테고리</dt>
                <dd>{expo.categories?.join(", ") || "-"}</dd>
                <dt>입장료</dt>
                <dd>{formatKRW(expo.price)}</dd>
                <dt>정원</dt>
                <dd>
                  {formatNumber(expo.maxCapacity)}명 (예약 {formatNumber(expo.reservedCount)}명)
                </dd>
                <dt>신청일</dt>
                <dd>{formatDateTime(expo.createdAt)}</dd>
              </dl>
            </div>
          </div>
          {expo.description && <p className="adm-review__desc">{expo.description}</p>}

          <div className="adm-review__section">
            <h4>신청 담당자</h4>
            {expo.applicant ? (
              <dl className="adm-dl adm-dl--compact">
                <dt>이름</dt>
                <dd>{expo.applicant.name}</dd>
                <dt>이메일</dt>
                <dd>{expo.applicant.email}</dd>
                <dt>연락처</dt>
                <dd>{formatPhone(expo.applicant.phone)}</dd>
                {expo.status === "APPROVED" && (
                  <>
                    <dt>관리자 ID</dt>
                    <dd className="adm-mono">{expo.applicant.loginId}</dd>
                  </>
                )}
              </dl>
            ) : (
              <p className="adm-hint">담당자 정보가 없습니다.</p>
            )}
          </div>

          {expo.status === "REJECTED" && expo.reason && (
            <div className="adm-review__section">
              <h4>반려 사유</h4>
              <p className="adm-text-danger" style={{ margin: 0 }}>
                {expo.reason}
              </p>
            </div>
          )}

          {pending && rejecting && (
            <label className="adm-field adm-review__section">
              <span>반려 사유 (신청자에게 전달됩니다)</span>
              <textarea className="adm-input" rows={3} maxLength={200} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="예: 행사 주최 정보가 확인되지 않습니다." autoFocus />
            </label>
          )}
          {pending && !rejecting && <p className="adm-hint">승인하면 박람회가 메인 화면에 공개되고, 신청 담당자에게 박람회관리자 계정이 발급됩니다.</p>}
          {error && <p className="adm-field-error">{error}</p>}
        </div>
      )}
    </Modal>
  );
}

/** 박람회 승인 — 상태별 조회·검색·페이지 이동·심사 (기존: 상태 탭 미동작, 페이지 수 오류, 처리 후 목록 미갱신) */
export default function SysExpoApprovalPage() {
  const [status, setStatus] = useState("PENDING");
  const [keyword, setKeyword] = useState("");
  const [title, setTitle] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [reviewId, setReviewId] = useState(null);
  const [credential, setCredential] = useState(null);
  const [flash, showFlash] = useFlash();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setData(await getSysExpos({ status: status === "ALL" ? undefined : status, title, page, size: PAGE_SIZE }));
    } catch (e) {
      showFlash("error", errorMessage(e, "박람회 목록을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, [status, title, page, showFlash]);

  useEffect(() => {
    load();
  }, [load]);

  const rows = data?.content ?? [];

  return (
    <>
      <Flash flash={flash} />
      <section className="adm-card">
        <div className="adm-toolbar">
          <div className="adm-tabs" role="tablist" aria-label="신청 상태">
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
          <form
            className="adm-search"
            role="search"
            onSubmit={(e) => {
              e.preventDefault();
              setTitle(keyword.trim());
              setPage(0);
            }}
          >
            <Search size={16} />
            <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="박람회명 검색 후 Enter" aria-label="박람회명 검색" />
          </form>
        </div>

        <div className="adm-table-meta">총 {formatNumber(data?.totalElements ?? 0)}건</div>
        <div className="adm-table-wrap">
          <table className="adm-table">
            <thead>
              <tr>
                <th>박람회</th>
                <th>카테고리</th>
                <th>개최 기간</th>
                <th>신청일</th>
                <th>상태</th>
                <th className="is-right">심사</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((expo) => {
                const meta = STATUS_META[expo.status] ?? { label: expo.status, tone: "neutral" };
                return (
                  <tr key={expo.id}>
                    <td>
                      <div className="adm-strong">{expo.title}</div>
                      <div className="adm-cell-sub">{expo.location}</div>
                    </td>
                    <td>{expo.category}</td>
                    <td>
                      {formatDate(expo.startDate)} ~ {formatDate(expo.endDate)}
                    </td>
                    <td>{formatDate(expo.createdAt)}</td>
                    <td>
                      <span className={`adm-badge adm-badge--${meta.tone}`}>{meta.label}</span>
                    </td>
                    <td className="is-right">
                      <button className={`adm-btn adm-btn--sm ${expo.status === "PENDING" ? "adm-btn--primary" : "adm-btn--outline"}`} onClick={() => setReviewId(expo.id)}>
                        {expo.status === "PENDING" ? "심사하기" : "상세"}
                      </button>
                    </td>
                  </tr>
                );
              })}
              {!loading && rows.length === 0 && (
                <tr>
                  <td colSpan={6} className="adm-empty-cell">
                    해당 조건의 박람회가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </section>

      {reviewId && (
        <ExpoReviewModal
          expoId={reviewId}
          onClose={() => setReviewId(null)}
          onDone={(message, issued) => {
            setReviewId(null);
            showFlash("success", message);
            if (issued) setCredential(issued);
            load();
          }}
        />
      )}

      {credential && (
        <CredentialModal
          title="박람회관리자 계정 발급"
          description="승인과 함께 신청 담당자의 박람회관리자 계정이 발급되었습니다. 아래 정보로 박람회관리자 로그인을 할 수 있습니다."
          credential={credential}
          onClose={() => setCredential(null)}
        />
      )}
    </>
  );
}
