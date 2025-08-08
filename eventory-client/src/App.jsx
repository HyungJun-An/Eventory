import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './assets/css/App.css'

import RevenuePage from './expoAdmin/RevenuePage';
import PaymentPage from './expoAdmin/PaymentPage';
import RefundPage from './expoAdmin/RefundPage';
import AdminLayout from './expoAdmin/sections/AdminLayout';
import Dashboard from './expoAdmin/DashboardPage';

function App() {

  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/admin/dashboard" element={<AdminLayout><Dashboard /></AdminLayout>} />
        <Route path="/admin/sales" element={<AdminLayout><RevenuePage /></AdminLayout>} />
        <Route path="/admin/payment" element={<AdminLayout><PaymentPage /></AdminLayout>} />
        <Route path="/admin/refund" element={<AdminLayout><RefundPage /></AdminLayout>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
