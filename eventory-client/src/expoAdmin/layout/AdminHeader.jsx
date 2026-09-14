import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { ChevronDown, UserRound } from "lucide-react";
import api from "../../api/axiosInstance";
import ExpoSelector from "./ExpoSelector";
import Modal from "../components/Modal";
import Flash, { useFlash } from "../components/Flash";
import LogoutButton from "../../components/LogoutButton";
import { errorMessage } from "../components/adminFormat";

const TITLES = {
  "/admin/dashboard": { title: "대시보드", desc: "예약·입장 현황을 한눈에 확인합니다" },
  "/admin/reservation": { title: "QR 체크인", desc: "현장 입장 QR을 스캔합니다" },
  "/admin/reservation/list": { title: "예약자 명단", desc: "예약자 검색, 수동 체크인, 예약 취소" },
  "/admin/booth": { title: "부스 관리", desc: "참가업체 부스 신청을 검토합니다" },
  "/admin/contents": { title: "콘텐츠 관리", desc: "박람회 소개·일정·포스터를 수정합니다" },
  "/admin/sales": { title: "매출 분석", desc: "기간별 매출과 환불 비율" },
  "/admin/payment": { title: "정산 관리", desc: "결제 내역 조회와 엑셀 다운로드" },
  "/admin/refund": { title: "환불 처리", desc: "환불 요청 승인·반려" },
};

/** 관리자 정보 조회·수정 모달 */
function ProfileModal({ onClose, onSaved }) {
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    api
      .get("/admin/profile")
      .then((res) => setForm({ name: res.data.name ?? "", email: res.data.email ?? "", phone: res.data.phone ?? "" }))
      .catch((e) => setError(errorMessage(e, "관리자 정보를 불러오지 못했습니다.")));
  }, []);

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      await api.put("/admin/profile", form);
      onSaved(form);
    } catch (err) {
      setError(errorMessage(err, "수정에 실패했습니다."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal title="관리자 정보" onClose={onClose}>
      {!form ? (
        <p className="adm-hint">{error || "불러오는 중…"}</p>
      ) : (
        <form onSubmit={save}>
          <label className="adm-field">
            <span>이름</span>
            <input className="adm-input" value={form.name} onChange={update("name")} required />
          </label>
          <label className="adm-field">
            <span>이메일</span>
            <input type="email" className="adm-input" value={form.email} onChange={update("email")} required />
          </label>
          <label className="adm-field">
            <span>전화번호</span>
            <input className="adm-input" value={form.phone} onChange={update("phone")} required />
          </label>
          {error && <p className="adm-field-error">{error}</p>}
          <div className="adm-modal__foot adm-modal__foot--flush">
            <button type="button" className="adm-btn adm-btn--ghost" onClick={onClose}>
              닫기
            </button>
            <button type="submit" className="adm-btn adm-btn--primary" disabled={saving}>
              {saving ? "저장 중…" : "저장"}
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
}

export default function AdminHeader({ expos, expo, onSelectExpo, loading }) {
  const { pathname } = useLocation();
  const page = TITLES[pathname] ?? { title: "관리자", desc: "" };
  const [adminName, setAdminName] = useState("");
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const menuRef = useRef(null);
  const [flash, showFlash] = useFlash();

  useEffect(() => {
    api
      .get("/admin/profile")
      .then((res) => setAdminName(res.data?.name ?? ""))
      .catch(() => setAdminName(""));
  }, []);

  useEffect(() => {
    if (!menuOpen) return undefined;
    const onDown = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false);
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [menuOpen]);

  return (
    <header className="adm-header">
      <Flash flash={flash} />
      <div className="adm-header__left">
        <h1 className="adm-header__title">{page.title}</h1>
        {page.desc && <p className="adm-header__desc">{page.desc}</p>}
      </div>

      <div className="adm-header__right">
        <ExpoSelector expos={expos} expo={expo} onSelect={onSelectExpo} loading={loading} />

        <div className="adm-profile" ref={menuRef}>
          <button type="button" className="adm-avatar-btn" onClick={() => setMenuOpen((o) => !o)} aria-haspopup="menu" aria-expanded={menuOpen}>
            <span className="adm-avatar">{adminName ? adminName[0] : <UserRound size={18} />}</span>
            <span className="adm-avatar-btn__name">{adminName || "관리자"}</span>
            <ChevronDown size={16} />
          </button>
          {menuOpen && (
            <div className="adm-menu" role="menu">
              <button
                type="button"
                className="adm-menu__item"
                role="menuitem"
                onClick={() => {
                  setMenuOpen(false);
                  setProfileOpen(true);
                }}
              >
                <UserRound size={16} />
                관리자 정보
              </button>
              <div className="adm-menu__divider" />
              <LogoutButton />
            </div>
          )}
        </div>
      </div>

      {profileOpen && (
        <ProfileModal
          onClose={() => setProfileOpen(false)}
          onSaved={(saved) => {
            setAdminName(saved.name);
            setProfileOpen(false);
            showFlash("success", "관리자 정보를 수정했습니다.");
          }}
        />
      )}
    </header>
  );
}
