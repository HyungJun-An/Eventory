import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import "./assets/css/App.css";

import MainLayout from "./components/MainLayout";
import { useAdminExpo } from "./expoAdmin/layout/useAdminExpo";

/*
 * 라우트 단위 코드 분할 — 페이지는 처음 방문할 때 내려받는다.
 * 기존: 모든 화면(관리자·시스템관리자·결제·차트 라이브러리)이 한 파일(785kB)에 묶여
 *       참관객이 메인 화면만 열어도 관리자 화면 코드까지 전부 내려받았다.
 */

// 참관객·참가업체
const UserMainPage = lazy(() => import("./user/userMain").then((m) => ({ default: m.UserMainPage })));
const ExpoDetail = lazy(() => import("./user/ExpoDetail"));
const LoginPage = lazy(() => import("./auth/LoginPage"));
const RegisterPage = lazy(() => import("./auth/UserRegistration"));
const RegisterCompany = lazy(() => import("./auth/RegisterCompany"));
const RegisterCustomer = lazy(() => import("./auth/RegisterCustomer"));
const PaymentCheckout = lazy(() => import("./payment/PaymentCheckout"));
const PaymentRedirect = lazy(() => import("./payment/PaymentRedirect").then((m) => ({ default: m.PaymentRedirect })));
const ReservationDetail = lazy(() => import("./payment/TestReservationDetail"));
const CompanyProfile = lazy(() => import("./companyUser/Profile"));
const BoothList = lazy(() => import("./companyUser/BoothList"));
const BoothEdit = lazy(() => import("./companyUser/BoothEdit"));

// 박람회관리자
const AdminLayout = lazy(() => import("./expoAdmin/layout/AdminLayout"));
const Dashboard = lazy(() => import("./expoAdmin/DashboardPage"));
const QrCheckinPage = lazy(() => import("./expoAdmin/pages/QrCheckinPage"));
const ReservationListPage = lazy(() => import("./expoAdmin/pages/ReservationListPage"));
const BoothManagePage = lazy(() => import("./expoAdmin/pages/BoothManagePage"));
const ContentEditPage = lazy(() => import("./expoAdmin/pages/ContentEditPage"));
const SalesPage = lazy(() => import("./expoAdmin/pages/SalesPage"));
const PaymentListPage = lazy(() => import("./expoAdmin/pages/PaymentListPage"));
const RefundListPage = lazy(() => import("./expoAdmin/pages/RefundListPage"));

// 시스템관리자
const SysLayout = lazy(() => import("./systemAdmin/layout/SysLayout"));
const SysDashboardPage = lazy(() => import("./systemAdmin/pages/SysDashboardPage"));
const SysExpoApprovalPage = lazy(() => import("./systemAdmin/pages/SysExpoApprovalPage"));
const SysExpoAdminPage = lazy(() => import("./systemAdmin/pages/SysExpoAdminPage"));

function PageFallback() {
  return (
    <div role="status" style={{ padding: "48px 16px", textAlign: "center", color: "#64748b", fontSize: 14 }}>
      불러오는 중…
    </div>
  );
}

/** 라우트마다 Suspense 를 둬서 페이지를 받는 동안 레이아웃(사이드바·헤더)은 그대로 보이게 한다 */
const page = (element) => <Suspense fallback={<PageFallback />}>{element}</Suspense>;

// 기존 대시보드 컴포넌트는 expoId 를 props 로 받으므로 레이아웃의 선택 박람회를 연결해준다
function DashboardRoute() {
  const { expoId } = useAdminExpo();
  return <Dashboard expoId={expoId} />;
}

function App() {
  return (
    <BrowserRouter
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        {/******************* 박람회관리자 영역 (공통 레이아웃 + 선택 박람회 공유) ***********************/}
        <Route path="/admin" element={page(<AdminLayout />)}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={page(<DashboardRoute />)} />
          <Route path="reservation" element={page(<QrCheckinPage />)} />
          <Route path="reservation/list" element={page(<ReservationListPage />)} />
          <Route path="booth" element={page(<BoothManagePage />)} />
          <Route path="contents" element={page(<ContentEditPage />)} />
          <Route path="sales" element={page(<SalesPage />)} />
          <Route path="payment" element={page(<PaymentListPage />)} />
          <Route path="refund" element={page(<RefundListPage />)} />
        </Route>

        {/****************** 일반 사용자 영역 ********************/}
        <Route element={<MainLayout />}>
          <Route path="/" element={page(<UserMainPage />)} />
          <Route path="/login" element={page(<LoginPage />)} />
          <Route path="/signup" element={page(<RegisterPage />)} />
          <Route path="/register/company" element={page(<RegisterCompany />)} />
          <Route path="/register/customer" element={page(<RegisterCustomer />)} />
          <Route path="/payment" element={page(<PaymentCheckout />)} />
          <Route path="/payment/redirect" element={page(<PaymentRedirect />)} />
          <Route path="/payment/reservation/:id" element={page(<ReservationDetail />)} />
          {/* company user 영역 추가 */}
          <Route path="/company/profile" element={page(<CompanyProfile />)} />
          <Route path="/company/booths" element={page(<BoothList />)} />
          <Route path="/company/booths/new" element={page(<BoothEdit />)} />
          <Route path="/company/booths/:boothId/edit" element={page(<BoothEdit />)} />
          {/* 사용자 상세 페이지 */}
          <Route path="/expos/:expoId" element={page(<ExpoDetail />)} />
        </Route>

        {/******************* 시스템관리자 영역 (박람회관리자와 같은 레이아웃·스타일) ***********************/}
        <Route path="/sys" element={page(<SysLayout />)}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={page(<SysDashboardPage />)} />
          <Route path="expos" element={page(<SysExpoApprovalPage />)} />
          <Route path="manage" element={page(<SysExpoAdminPage />)} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
