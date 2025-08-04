import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./assets/css/App.css";

import Screen from "./expoAdmin/DivWrapperScreen";
import Dashboard from "./expoAdmin/sections/Dashboard";

function App() {
  return (
    <BrowserRouter
      future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
    >
      <Routes>
        <Route path="/hi" element={<Screen />} />
        <Route path="/hello" element={<Dashboard />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
