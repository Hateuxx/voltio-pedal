import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './features/auth/LoginPage'
import InicioPage from './features/inicio/InicioPage'
import ProveedoresPage from './features/proveedores/ProveedoresPage'
import RutaProtegida from './features/auth/RutaProtegida'
import { rolesDe } from './features/auth/permisos'
import Layout from './components/Layout'
import ProveedorFormPage from './features/proveedores/ProveedorFormPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<RutaProtegida><Layout /></RutaProtegida>}>
        <Route path="/inicio" element={<InicioPage />} />
        <Route path="/proveedores" element={<RutaProtegida roles={rolesDe('/proveedores')}><ProveedoresPage /></RutaProtegida>} />
        <Route path="/proveedores/nuevo" element={<RutaProtegida roles={['ADMINISTRADOR']}><ProveedorFormPage /></RutaProtegida>} />
      </Route>

      <Route path="*" element={<Navigate to="/inicio" replace />} />
    </Routes>
  )
}

export default App
