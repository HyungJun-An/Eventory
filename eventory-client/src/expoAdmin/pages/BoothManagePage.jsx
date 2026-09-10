import { useCallback, useEffect, useMemo, useState } from "react";
import { MapPin } from "lucide-react";
import api from "../../api/axiosInstance";
import { useAdminExpo } from "../layout/useAdminExpo";
import Modal from "../components/Modal";
import Flash, { useFlash } from "../components/Flash";
import { errorMessage, formatDateTime, formatPhone } from "../components/adminFormat";

const FILTERS = [
  { id: "ALL", label: "전체" },
  { id: "PENDING", label: "검토 대기" },
  { id: "APPROVED", label: "승인" },
  { id: "REJECTED", label: "반려" },
];
const STATUS_META = {
  PENDING: { label: "검토 대기", tone: "warning" },
  APPROVED: { label: "승인", tone: "success" },
  REJECTED: { label: "반려", tone: "danger" },
};

function BoothLogo({ src }) {
  const [broken, setBroken] = useState(false);
  return src && !broken ? (
    <img className="adm-booth__logo" src={src} alt="" onError={() => setBroken(true)} />
  ) : (
    <span className="adm-booth__logo adm-booth__logo--empty" />
  );
}

/** 부스 관리 — 참가업체 부스 신청 승인/반려 (기존: 메뉴만 있고 화면 없음) */
export default function BoothManagePage() {
  const { expoId } = useAdminExpo();
  const [booths, setBooths] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [busyId, setBusyId] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [reason, setReason] = useState("");
  const [flash, showFlash] = useFlash();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get(`/admin/expos/${expoId}/booths`);
      setBooths(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      showFlash("error", errorMessage(e, "부스 목록을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, [expoId, showFlash]);

  useEffect(() => {
    load();
  }, [load]);

  const counts = useMemo(() => booths.reduce((acc, b) => ({ ...acc, [b.status]: (acc[b.status] ?? 0) + 1 }), {}), [booths]);
  const visible = filter === "ALL" ? booths : booths.filter((b) => b.status === filter);

  const changeStatus = async (booth, status, why = null) => {
    setBusyId(booth.boothId);
    try {
      await api.put(`/admin/expos/${expoId}/booths/${booth.boothId}`, { status, reason: why });
      showFlash("success", `${booth.title}을(를) ${STATUS_META[status].label} 처리했습니다.`);
      setRejectTarget(null);
      await load();
    } catch (e) {
      showFlash("error", errorMessage(e, "상태 변경에 실패했습니다."));
    } finally {
      setBusyId(null);
    }
  };

  return (
    <>
      <Flash flash={flash} />
      <section className="adm-card">
        <div className="adm-toolbar">
          <div className="adm-tabs" role="tablist" aria-label="부스 신청 상태">
            {FILTERS.map((f) => (
              <button
                key={f.id}
                role="tab"
                aria-selected={filter === f.id}
                className={`adm-tab${filter === f.id ? " is-active" : ""}`}
                onClick={() => setFilter(f.id)}
              >
                {f.label}
                <span className="adm-tab__count">{f.id === "ALL" ? booths.length : counts[f.id] ?? 0}</span>
              </button>
            ))}
          </div>
        </div>

        {loading ? (
          <div className="adm-empty">불러오는 중…</div>
        ) : visible.length === 0 ? (
          <div className="adm-empty">해당 상태의 부스 신청이 없습니다.</div>
        ) : (
          <div className="adm-booth-grid">
            {visible.map((b) => {
              const meta = STATUS_META[b.status] ?? { label: b.status, tone: "neutral" };
              const busy = busyId === b.boothId;
              return (
                <article className="adm-booth" key={b.boothId}>
                  <div className="adm-booth__head">
                    <BoothLogo src={b.imageUrl} />
                    <div>
                      <h3>{b.title}</h3>
                      <div className="adm-booth__loc">
                        <MapPin size={14} />
                        {b.location}
                      </div>
                    </div>
                    <span className={`adm-badge adm-badge--${meta.tone}`}>{meta.label}</span>
                  </div>
                  <dl className="adm-dl adm-dl--compact">
                    <dt>담당자</dt>
                    <dd>
                      {b.managerName} · {b.department}
                    </dd>
                    <dt>연락처</dt>
                    <dd>{formatPhone(b.phone)}</dd>
                    <dt>이메일</dt>
                    <dd>{b.email}</dd>
                    <dt>신청일</dt>
                    <dd>{formatDateTime(b.createdAt)}</dd>
                    {b.status === "REJECTED" && b.reason && (
                      <>
                        <dt>반려 사유</dt>
                        <dd className="adm-text-danger">{b.reason}</dd>
                      </>
                    )}
                  </dl>
                  <div className="adm-booth__actions">
                    <button
                      className="adm-btn adm-btn--sm adm-btn--danger-outline"
                      disabled={b.status === "REJECTED" || busy}
                      onClick={() => {
                        setRejectTarget(b);
                        setReason("");
                      }}
                    >
                      반려
                    </button>
                    <button className="adm-btn adm-btn--sm adm-btn--primary" disabled={b.status === "APPROVED" || busy} onClick={() => changeStatus(b, "APPROVED")}>
                      승인
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </section>

      {rejectTarget && (
        <Modal
          title="부스 신청 반려"
          onClose={() => setRejectTarget(null)}
          footer={
            <>
              <button className="adm-btn adm-btn--ghost" onClick={() => setRejectTarget(null)}>
                닫기
              </button>
              <button className="adm-btn adm-btn--danger" disabled={!reason.trim() || busyId !== null} onClick={() => changeStatus(rejectTarget, "REJECTED", reason.trim())}>
                반려하기
              </button>
            </>
          }
        >
          <p className="adm-modal__lead">
            <b>{rejectTarget.title}</b> 신청을 반려합니다. 반려 사유는 참가업체에 그대로 표시됩니다.
          </p>
          <label className="adm-field">
            <span>반려 사유</span>
            <textarea className="adm-input" rows={3} maxLength={200} value={reason} onChange={(e) => setReason(e.target.value)} placeholder="예: 전시 품목이 박람회 주제와 맞지 않습니다." autoFocus />
          </label>
        </Modal>
      )}
    </>
  );
}
