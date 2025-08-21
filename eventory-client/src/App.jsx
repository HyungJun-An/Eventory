import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./assets/css/App.css";

import RevenuePage from './expoAdmin/RevenuePage';
import PaymentPage from './expoAdmin/PaymentPage';
import RefundPage from './expoAdmin/RefundPage';
import AdminLayout from './expoAdmin/sections/AdminLayout';
import Dashboard from './expoAdmin/DashboardPage';
import Reservation from './expoAdmin/ReservationPage';
import QRCheckIn from './expoAdmin/QRCheckInPage';
import LoginPage from './auth/LoginPage';
import RegisterPage from './auth/UserRegistration';
import { UserMainPage } from './user/userMain';
import MainLayout from './components/MainLayout';
import RegisterCompany from './auth/RegisterCompany';
import RegisterCustomer from './auth/RegisterCustomer';
import PaymentCheckout from './payment/PaymentCheckout';
import { PaymentRedirect } from './payment/PaymentRedirect';
import ReservationDetail from "./payment/TestReservationDetail";
import CompanyProfile from "./companyUser/Profile";
import BoothList from "./companyUser/BoothList";
import BoothEdit from "./companyUser/BoothEdit";
import ExpoDetail from "./user/ExpoDetail";

import ContentPage from './expoAdmin/ContentPage';
import { SysExpoList } from "./systemAdmin/SysExpoList";
import ExpoManagerManagement from "./systemAdmin/ExpoManagerManagement";

function App() {
  return (
    <BrowserRouter
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        {/******************* 어드민 영역 ***********************/}
        <Route
          path="/admin/dashboard"
          element={
            <AdminLayout>
              <Dashboard />
            </AdminLayout>
          }
        />
        <Route
          path="/admin/reservation"
          element={
            <AdminLayout>
              <QRCheckIn />
            </AdminLayout>
          }
        />

        <Route
          path="/admin/reservation/list"
          element={
            <AdminLayout>
              <Reservation />
            </AdminLayout>
          }
        />

        <Route
          path="/admin/sales"
          element={
            <AdminLayout>
              <RevenuePage />
            </AdminLayout>
          }
        />
        <Route
          path="/admin/payment"
          element={
            <AdminLayout>
              <PaymentPage />
            </AdminLayout>
          }
        />
        <Route
          path="/admin/refund"
          element={
            <AdminLayout>
              <RefundPage />
            </AdminLayout>
          }
        />
        <Route
          path="/admin/contents"
          element={
            <AdminLayout>
              <ContentPage />
            </AdminLayout>
          }
        />

        {/****************** 일반 사용자 영역 ********************/}
        <Route element={<MainLayout />}>
          <Route path="/" element={<UserMainPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<RegisterPage />} />
          <Route path="/register/company" element={<RegisterCompany />} />
          <Route path="/register/customer" element={<RegisterCustomer />} />
          <Route path="/payment" element={<PaymentCheckout />} />
          <Route path="/payment/redirect" element={<PaymentRedirect />} />
          <Route path="/payment/reservation/:id" element={<ReservationDetail />} />
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
          element={<AdminLayout>{<SysExpoList />}</AdminLayout>}
        />
        <Route
          path="/sys/manage"
          element={<AdminLayout>{<ExpoManagerManagement />}</AdminLayout>}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
