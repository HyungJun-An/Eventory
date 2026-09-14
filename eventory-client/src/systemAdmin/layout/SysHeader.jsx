import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { ChevronDown, ShieldCheck } from "lucide-react";
import { getSysMe } from "../../api/sysAdminApi";
import LogoutButton from "../../components/LogoutButton";

const TITLES = {
  "/sys/dashboard": { title: "플랫폼 대시보드", desc: "전체 결제·예약·입장 현황" },
  "/sys/expos": { title: "박람회 승인", desc: "박람회 개최 신청을 검토하고 승인·반려합니다" },
  "/sys/manage": { title: "박람회관리자 관리", desc: "관리자 계정 조회·수정·비밀번호 재발급" },
};

/** 시스템관리자 헤더 — 기존 SysHeader 는 박람회관리자 API(/admin/expos, /admin/profile)를 호출해 403 */
export default function SysHeader() {
  const { pathname } = useLocation();
  const page = TITLES[pathname] ?? { title: "시스템관리자", desc: "" };
  const [me, setMe] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    getSysMe()
      .then(setMe)
      .catch(() => setMe(null));
  }, []);

  useEffect(() => {
    if (!menuOpen) return undefined;
    const onDown = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false);
    document.addEventListener("mousedown", onDown);
    return () => document.removeEventListener("mousedown", onDown);
  }, [menuOpen]);

  return (
    <header className="adm-header">
      <div className="adm-header__left">
        <h1 className="adm-header__title">{page.title}</h1>
        {page.desc && <p className="adm-header__desc">{page.desc}</p>}
      </div>

      <div className="adm-header__right">
        <div className="adm-profile" ref={menuRef}>
          <button type="button" className="adm-avatar-btn" onClick={() => setMenuOpen((o) => !o)} aria-haspopup="menu" aria-expanded={menuOpen}>
            <span className="adm-avatar">{me?.name ? me.name[0] : <ShieldCheck size={18} />}</span>
            <span className="adm-avatar-btn__name">{me?.name ?? "시스템관리자"}</span>
            <ChevronDown size={16} />
          </button>
          {menuOpen && (
            <div className="adm-menu" role="menu">
              {me && (
                <div className="adm-menu__info">
                  <strong>{me.name}</strong>
                  <span>{me.email}</span>
                </div>
              )}
              <div className="adm-menu__divider" />
              <LogoutButton />
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
