import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './assets/css/App.css'

import Screen from './expoAdmin/DivWrapperScreen';

function App() {

  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/hi" element={<Screen />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
