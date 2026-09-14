import { useEffect } from "react";
import { Outlet } from "react-router-dom";
import { CalendarCheck2, LayoutDashboard, UserCog } from "lucide-react";
import AdminSidebar from "../../expoAdmin/layout/AdminSidebar";
import SysHeader from "./SysHeader";
import "../../assets/css/admin/AdminUI.css";

const SYS_NAV = [
  {
    group: "플랫폼 운영",
    items: [
      { to: "/sys/dashboard", label: "대시보드", icon: LayoutDashboard },
      { to: "/sys/expos", label: "박람회 승인", icon: CalendarCheck2 },
      { to: "/sys/manage", label: "박람회관리자 관리", icon: UserCog },
    ],
  },
];

/** 시스템관리자 공통 레이아웃 — 박람회관리자와 같은 사이드바·스타일을 재사용한다 */
export default function SysLayout() {
  // index.css 의 body { display:flex; place-items:center } 해제 (박람회관리자 레이아웃과 동일)
  useEffect(() => {
    document.body.classList.add("adm-body");
    return () => document.body.classList.remove("adm-body");
  }, []);

  return (
    <div className="adm-shell">
      <AdminSidebar nav={SYS_NAV} homePath="/sys/dashboard" tag="System" />
      <div className="adm-main">
        <SysHeader />
        <main className="adm-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
