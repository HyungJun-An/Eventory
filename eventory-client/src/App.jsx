import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './assets/css/App.css'

import RevenuePage from './expoAdmin/RevenuePage';
import PaymentPage from './expoAdmin/PaymentPage';
import RefundPage from './expoAdmin/RefundPage';
import AdminLayout from './expoAdmin/sections/AdminLayout';
import Dashboard from './expoAdmin/DashboardPage';
import LoginPage from './auth/LoginPage';
import RegisterPage from './auth/UserRegistration';
import { UserMainPage } from './user/userMain';
import MainLayout from './components/MainLayout';
import RegisterCompany from './auth/RegisterCompany';
import RegisterCustomer from './auth/RegisterCustomer';
import PaymentCheckout from './payment/PaymentCheckout';
import { PaymentRedirect } from './payment/PaymentRedirect';

function App() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/admin/dashboard" element={<AdminLayout><Dashboard /></AdminLayout>} />
        <Route path="/admin/sales" element={<AdminLayout><RevenuePage /></AdminLayout>} />
        <Route path="/admin/payment" element={<AdminLayout><PaymentPage /></AdminLayout>} />
        <Route path="/admin/refund" element={<AdminLayout><RefundPage /></AdminLayout>} />
        <Route path="/test" element={<AdminLayout></AdminLayout>} />
        <Route element={<MainLayout />}>
          <Route path="/" element={<UserMainPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<RegisterPage/>} />
          <Route path="/register/company" element={<RegisterCompany/>} />
          <Route path="/register/customer" element={<RegisterCustomer/>} />
          <Route path="/payment" element={<PaymentCheckout />} />
          <Route path="/payment/redirect" element={<PaymentRedirect />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
