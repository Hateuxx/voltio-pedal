import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './features/auth/LoginPage'
import ProveedoresPage from './features/proveedores/ProveedoresPage'
import RutaProtegida from './features/auth/RutaProtegida'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/proveedores" element={<RutaProtegida><ProveedoresPage /></RutaProtegida>} />
      <Route path="*" element={<Navigate to="/proveedores" replace />} />
    </Routes>
  )
}

export default App
