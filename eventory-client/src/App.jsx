import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./assets/css/App.css";

import RevenuePage from "./expoAdmin/RevenuePage";
import PaymentPage from "./expoAdmin/PaymentPage";
import RefundPage from "./expoAdmin/RefundPage";
import AdminLayout from "./expoAdmin/sections/AdminLayout";
import Dashboard from "./expoAdmin/DashboardPage";
import LoginPage from "./user/LoginPage";
import RegisterPage from "./user/UserRegistration";
import { UserMainPage } from "./user/userMain";
import MainLayout from "./components/MainLayout";
import { SysExpoList } from "./systemAdmin/SysExpoList";
import RegisterCompany from "./user/RegisterCompany";
import RegisterCustomer from "./user/RegisterCustomer";
import ExpoManagerManagement from "./systemAdmin/ExpoManagerManagement";
import SysDashboard from "./systemAdmin/SysDashboard";

function App() {
  return (
    <BrowserRouter
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route
          path="/admin/dashboard"
          element={
            <AdminLayout>
              <Dashboard />
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
        <Route element={<MainLayout />}>
          <Route path="/" element={<UserMainPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<RegisterPage />} />
          <Route path="/register/company" element={<RegisterCompany />} />
          <Route path="/register/customer" element={<RegisterCustomer />} />
        </Route>
        <Route
          path="/sys/expos"
          element={<AdminLayout>{<SysExpoList />}</AdminLayout>}
        />
        <Route
          path="/sys/manage"
          element={<AdminLayout>{<ExpoManagerManagement />}</AdminLayout>}
        />
        <Route
          path="/sys/dashboard"
          element={<AdminLayout>{<SysDashboard />}</AdminLayout>}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
