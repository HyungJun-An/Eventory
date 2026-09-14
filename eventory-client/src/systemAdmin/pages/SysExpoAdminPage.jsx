import { useCallback, useEffect, useState } from "react";
import { Search } from "lucide-react";
import { deleteExpoAdmin, getExpoAdminExpos, getExpoAdmins, resetExpoAdminPassword, updateExpoAdmin } from "../../api/sysAdminApi";
import Modal from "../../expoAdmin/components/Modal";
import Pager from "../../expoAdmin/components/Pager";
import Flash, { useFlash } from "../../expoAdmin/components/Flash";
import { errorMessage, formatDate, formatNumber, formatPhone } from "../../expoAdmin/components/adminFormat";
import CredentialModal from "./CredentialModal";

const PAGE_SIZE = 10;
const STATUS_META = {
  PENDING: { label: "승인 대기", tone: "warning" },
  APPROVED: { label: "승인", tone: "success" },
  REJECTED: { label: "반려", tone: "danger" },
};

/** 관리자가 개최(신청)한 박람회 목록 */
function AdminExposModal({ admin, onClose }) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);

  useEffect(() => {
    getExpoAdminExpos(admin.id, { page, size: 5 })
      .then(setData)
      .catch(() => setData({ content: [], totalPages: 0 }));
  }, [admin.id, page]);

  return (
    <Modal title={`${admin.name} 님의 박람회`} wide onClose={onClose}>
      {!data ? (
        <p className="adm-hint">불러오는 중…</p>
      ) : data.content.length === 0 ? (
        <div className="adm-empty adm-empty--inline">개최한 박람회가 없습니다.</div>
      ) : (
        <>
          <div className="adm-table-wrap">
            <table className="adm-table">
              <thead>
                <tr>
                  <th>박람회명</th>
                  <th>카테고리</th>
                  <th>개최 기간</th>
                  <th>상태</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((expo) => {
                  const meta = STATUS_META[expo.status] ?? { label: expo.status, tone: "neutral" };
                  return (
                    <tr key={expo.id}>
                      <td className="adm-strong">{expo.title}</td>
                      <td>{expo.category}</td>
                      <td>
                        {formatDate(expo.startDate)} ~ {formatDate(expo.endDate)}
                      </td>
                      <td>
                        <span className={`adm-badge adm-badge--${meta.tone}`}>{meta.label}</span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </Modal>
  );
}

/** 관리자 정보 수정 */
function AdminEditModal({ admin, onClose, onSaved }) {
  const [form, setForm] = useState({ name: admin.name ?? "", phone: admin.phone ?? "", email: admin.email ?? "" });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await updateExpoAdmin(admin.id, { name: form.name.trim(), phone: form.phone.trim(), email: form.email.trim() });
      onSaved();
    } catch (err) {
      setError(errorMessage(err, "수정에 실패했습니다."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal title="박람회관리자 정보 수정" onClose={onClose}>
      <form onSubmit={save}>
        <label className="adm-field">
          <span>이름</span>
          <input className="adm-input" value={form.name} onChange={update("name")} required />
        </label>
        <label className="adm-field">
          <span>전화번호</span>
          <input className="adm-input" value={form.phone} onChange={update("phone")} required />
        </label>
        <label className="adm-field">
          <span>이메일</span>
          <input type="email" className="adm-input" value={form.email} onChange={update("email")} required />
        </label>
        {error && <p className="adm-field-error">{error}</p>}
        <div className="adm-modal__foot adm-modal__foot--flush">
          <button type="button" className="adm-btn adm-btn--ghost" onClick={onClose}>
            닫기
          </button>
          <button type="submit" className="adm-btn adm-btn--primary" disabled={busy}>
            {busy ? "저장 중…" : "저장"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

/** 박람회관리자 관리 — 검색·페이지 이동·정보 수정·비밀번호 재발급·삭제 (기존: 삭제 확인 없음, 재전송 버튼 미동작) */
export default function SysExpoAdminPage() {
  const [keyword, setKeyword] = useState("");
  const [applied, setApplied] = useState("");
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [exposOf, setExposOf] = useState(null);
  const [editing, setEditing] = useState(null);
  const [confirm, setConfirm] = useState(null); // { type: "reset" | "delete", admin }
  const [credential, setCredential] = useState(null);
  const [busy, setBusy] = useState(false);
  const [flash, showFlash] = useFlash();

  const load = useCallback(async () => {
    setLoading(true);
    try {
      setData(await getExpoAdmins({ keyword: applied, page, size: PAGE_SIZE }));
    } catch (e) {
      showFlash("error", errorMessage(e, "관리자 목록을 불러오지 못했습니다."));
    } finally {
      setLoading(false);
    }
  }, [applied, page, showFlash]);

  useEffect(() => {
    load();
  }, [load]);

  const runConfirm = async () => {
    const { type, admin } = confirm;
    setBusy(true);
    try {
      if (type === "reset") {
        const issued = await resetExpoAdminPassword(admin.id);
        setConfirm(null);
        setCredential({ admin, ...issued });
      } else {
        await deleteExpoAdmin(admin.id);
        setConfirm(null);
        showFlash("success", `${admin.name} 계정을 삭제했습니다.`);
        load();
      }
    } catch (e) {
      showFlash("error", errorMessage(e, "처리에 실패했습니다."));
      setConfirm(null);
    } finally {
      setBusy(false);
    }
  };

  const rows = data?.content ?? [];

  return (
    <>
      <Flash flash={flash} />
      <section className="adm-card">
        <div className="adm-toolbar">
          <form
            className="adm-search"
            role="search"
            onSubmit={(e) => {
              e.preventDefault();
              setApplied(keyword.trim());
              setPage(0);
            }}
          >
            <Search size={16} />
            <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="이름·전화번호·이메일 검색 후 Enter" aria-label="관리자 검색" />
          </form>
        </div>

        <div className="adm-table-meta">총 {formatNumber(data?.totalElements ?? 0)}명</div>
        <div className="adm-table-wrap">
          <table className="adm-table">
            <thead>
              <tr>
                <th>관리자</th>
                <th>연락처</th>
                <th>계정 생성일</th>
                <th>마지막 박람회</th>
                <th className="is-right">관리</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((admin) => (
                <tr key={admin.id}>
                  <td>
                    <div className="adm-strong">{admin.name}</div>
                    {/* 승인 전 임시 계정은 로그인 아이디가 없다 (서버가 loginId 를 비워 내려줌) */}
                    {admin.loginId ? <div className="adm-cell-sub adm-mono">{admin.loginId}</div> : <span className="adm-badge adm-badge--warning">승인 전 신청자</span>}
                  </td>
                  <td>
                    <div>{formatPhone(admin.phone)}</div>
                    <div className="adm-cell-sub">{admin.email}</div>
                  </td>
                  <td>{formatDate(admin.createdAt)}</td>
                  <td>{admin.lastAppliedAt ? formatDate(admin.lastAppliedAt) : "-"}</td>
                  <td className="is-right">
                    <div className="adm-row-actions">
                      <button className="adm-btn adm-btn--sm adm-btn--outline" onClick={() => setExposOf(admin)}>
                        박람회
                      </button>
                      <button className="adm-btn adm-btn--sm adm-btn--outline" onClick={() => setEditing(admin)}>
                        수정
                      </button>
                      <button
                        className="adm-btn adm-btn--sm adm-btn--outline"
                        disabled={!admin.loginId}
                        title={admin.loginId ? "임시 비밀번호 재발급" : "박람회 승인 후 계정이 활성화됩니다"}
                        onClick={() => setConfirm({ type: "reset", admin })}
                      >
                        비밀번호
                      </button>
                      <button className="adm-btn adm-btn--sm adm-btn--danger-outline" onClick={() => setConfirm({ type: "delete", admin })}>
                        삭제
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {!loading && rows.length === 0 && (
                <tr>
                  <td colSpan={5} className="adm-empty-cell">
                    검색 결과가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pager page={page} totalPages={data?.totalPages ?? 0} onChange={setPage} />
      </section>

      {exposOf && <AdminExposModal admin={exposOf} onClose={() => setExposOf(null)} />}

      {editing && (
        <AdminEditModal
          admin={editing}
          onClose={() => setEditing(null)}
          onSaved={() => {
            showFlash("success", "관리자 정보를 수정했습니다.");
            setEditing(null);
            load();
          }}
        />
      )}

      {confirm && (
        <Modal
          title={confirm.type === "reset" ? "비밀번호 재발급" : "관리자 계정 삭제"}
          onClose={() => setConfirm(null)}
          footer={
            <>
              <button className="adm-btn adm-btn--ghost" onClick={() => setConfirm(null)} disabled={busy}>
                취소
              </button>
              <button className={`adm-btn ${confirm.type === "reset" ? "adm-btn--primary" : "adm-btn--danger"}`} onClick={runConfirm} disabled={busy}>
                {busy ? "처리 중…" : confirm.type === "reset" ? "재발급" : "삭제"}
              </button>
            </>
          }
        >
          <p className="adm-modal__lead">
            {confirm.type === "reset" ? (
              <>
                <b>{confirm.admin.name}</b> 님의 비밀번호를 새 임시 비밀번호로 바꿉니다. 기존 비밀번호로는 더 이상 로그인할 수 없습니다.
              </>
            ) : (
              <>
                <b>{confirm.admin.name}</b> 님의 계정을 삭제합니다. 담당 박람회가 있는 계정은 삭제할 수 없습니다.
              </>
            )}
          </p>
        </Modal>
      )}

      {credential && (
        <CredentialModal
          title="임시 비밀번호 발급"
          description={`${credential.admin.name} 님의 새 로그인 정보입니다.`}
          credential={credential}
          onClose={() => setCredential(null)}
        />
      )}
    </>
  );
}
