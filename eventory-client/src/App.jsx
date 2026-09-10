import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import "./assets/css/App.css";

import AdminLayout from "./expoAdmin/layout/AdminLayout";
import { useAdminExpo } from "./expoAdmin/layout/useAdminExpo";
import Dashboard from "./expoAdmin/DashboardPage";
import QrCheckinPage from "./expoAdmin/pages/QrCheckinPage";
import ReservationListPage from "./expoAdmin/pages/ReservationListPage";
import BoothManagePage from "./expoAdmin/pages/BoothManagePage";
import ContentEditPage from "./expoAdmin/pages/ContentEditPage";
import SalesPage from "./expoAdmin/pages/SalesPage";
import PaymentListPage from "./expoAdmin/pages/PaymentListPage";
import RefundListPage from "./expoAdmin/pages/RefundListPage";
import LoginPage from "./auth/LoginPage";
import RegisterPage from "./auth/UserRegistration";
import { UserMainPage } from "./user/userMain";
import MainLayout from "./components/MainLayout";
import RegisterCompany from "./auth/RegisterCompany";
import RegisterCustomer from "./auth/RegisterCustomer";
import PaymentCheckout from "./payment/PaymentCheckout";
import { PaymentRedirect } from "./payment/PaymentRedirect";
import ReservationDetail from "./payment/TestReservationDetail";
import CompanyProfile from "./companyUser/Profile";
import BoothList from "./companyUser/BoothList";
import BoothEdit from "./companyUser/BoothEdit";
import ExpoDetail from "./user/ExpoDetail";

import { SysExpoList } from "./systemAdmin/SysExpoList";
import ExpoManagerManagement from "./systemAdmin/ExpoManagerManagement";
import SysDashboard from "./systemAdmin/SysDashboard";
import AdminSidebar from "./systemAdmin/AdminSidebar";

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
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<Navigate to="dashboard" replace />} />
          <Route path="dashboard" element={<DashboardRoute />} />
          <Route path="reservation" element={<QrCheckinPage />} />
          <Route path="reservation/list" element={<ReservationListPage />} />
          <Route path="booth" element={<BoothManagePage />} />
          <Route path="contents" element={<ContentEditPage />} />
          <Route path="sales" element={<SalesPage />} />
          <Route path="payment" element={<PaymentListPage />} />
          <Route path="refund" element={<RefundListPage />} />
        </Route>

        {/****************** 일반 사용자 영역 ********************/}
        <Route element={<MainLayout />}>
          <Route path="/" element={<UserMainPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<RegisterPage />} />
          <Route path="/register/company" element={<RegisterCompany />} />
          <Route path="/register/customer" element={<RegisterCustomer />} />
          <Route path="/payment" element={<PaymentCheckout />} />
          <Route path="/payment/redirect" element={<PaymentRedirect />} />
          <Route
            path="/payment/reservation/:id"
            element={<ReservationDetail />}
          />
          {/* company user 영역 추가 */}
          <Route path="/company/profile" element={<CompanyProfile />} />
          <Route path="/company/booths" element={<BoothList />} />
          <Route path="/company/booths/new" element={<BoothEdit />} />
          <Route path="/company/booths/:boothId/edit" element={<BoothEdit />} />
          {/* 사용자 상세 페이지 */}
          <Route path="/expos/:expoId" element={<ExpoDetail />} />
        </Route>
        <Route
          path="/sys/expos"
          element={<SysExpoList></SysExpoList>}
        />
        <Route
          path="/sys/manage"
          element={<ExpoManagerManagement></ExpoManagerManagement>}
        />
        <Route
          path="/sys/dashboard"
          element={<SysDashboard></SysDashboard>}
        />
        <Route path="/sys/sidebar" element={<AdminSidebar></AdminSidebar>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
