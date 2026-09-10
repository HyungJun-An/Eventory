import { NavLink } from "react-router-dom";
import { BarChart3, FileText, LayoutDashboard, RotateCcw, ScanLine, Store, Users, Wallet } from "lucide-react";
import logo from "../../assets/demo/eventory_bluewriting.png";

const NAV = [
  {
    group: "운영",
    items: [
      { to: "/admin/dashboard", label: "대시보드", icon: LayoutDashboard },
      { to: "/admin/reservation", label: "QR 체크인", icon: ScanLine, end: true },
      { to: "/admin/reservation/list", label: "예약자 명단", icon: Users },
      { to: "/admin/booth", label: "부스 관리", icon: Store },
      { to: "/admin/contents", label: "콘텐츠 관리", icon: FileText },
    ],
  },
  {
    group: "매출 · 정산",
    items: [
      { to: "/admin/sales", label: "매출 분석", icon: BarChart3 },
      { to: "/admin/payment", label: "정산 관리", icon: Wallet },
      { to: "/admin/refund", label: "환불 처리", icon: RotateCcw },
    ],
  },
];

export default function AdminSidebar() {
  return (
    <aside className="adm-sidebar">
      <NavLink to="/admin/dashboard" className="adm-sidebar__brand" aria-label="Eventory 관리자 홈">
        <img src={logo} alt="Eventory" />
        <span className="adm-sidebar__tag">Admin</span>
      </NavLink>

      <nav className="adm-nav" aria-label="관리자 메뉴">
        {NAV.map((section) => (
          <div key={section.group} className="adm-nav__section">
            <div className="adm-nav__label">{section.group}</div>
            {section.items.map(({ to, label, icon: Icon, end }) => (
              <NavLink key={to} to={to} end={end} className={({ isActive }) => `adm-nav__link${isActive ? " is-active" : ""}`}>
                <Icon size={18} />
                <span>{label}</span>
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
    </aside>
  );
}
