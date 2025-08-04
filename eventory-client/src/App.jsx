import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './assets/css/App.css'

import DivWrapperScreen from './expoAdmin/DivWrapperScreen';

function App() {

  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/hello" element={<DivWrapperScreen />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
