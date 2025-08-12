import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './assets/css/App.css'

import RevenuePage from './expoAdmin/RevenuePage';
import PaymentPage from './expoAdmin/PaymentPage';
import RefundPage from './expoAdmin/RefundPage';
import AdminLayout from './expoAdmin/sections/AdminLayout';
import Dashboard from './expoAdmin/DashboardPage';
import LoginPage from './user/LoginPage';
import RegisterPage from './user/UserRegistration';
import Main, { UserMainPage } from './user/userMain';
import Navbar from './components/Navbar';
import MainLayout from './components/MainLayout';
function App() {

  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/admin/dashboard" element={<AdminLayout><Dashboard /></AdminLayout>} />
        <Route path="/admin/sales" element={<AdminLayout><RevenuePage /></AdminLayout>} />
        <Route path="/admin/payment" element={<AdminLayout><PaymentPage /></AdminLayout>} />
        <Route path="/admin/refund" element={<AdminLayout><RefundPage /></AdminLayout>} />
        <Route element={<MainLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<RegisterPage />} />
        </Route>
        <Route path="/" element={<UserMainPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
