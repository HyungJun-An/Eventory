import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./assets/css/App.css";

import RevenuePage from "./expoAdmin/RevenuePage";
import PaymentPage from "./expoAdmin/PaymentPage";
import RefundPage from "./expoAdmin/RefundPage";

function App() {
  return (
    <BrowserRouter
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/hi" element={<RevenuePage />} />
        <Route path="/hello" element={<PaymentPage />} />
        <Route path="/refund" element={<RefundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
