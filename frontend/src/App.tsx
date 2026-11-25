import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './features/auth/LoginPage'
import InicioPage from './features/inicio/InicioPage'
import ProveedoresPage from './features/proveedores/ProveedoresPage'
import RutaProtegida from './features/auth/RutaProtegida'
import { rolesDe } from './features/auth/permisos'
import Layout from './components/Layout'
import ProveedorFormPage from './features/proveedores/ProveedorFormPage'
import TiposRepuestoPage from './features/tiposRepuesto/TiposRepuestoPage'
import MercaderiaPage from './features/mercaderia/MercaderiaPage'
import MercaderiaFormPage from './features/mercaderia/MercaderiaFormPage'
import IngresosPage from './features/ingresos/IngresosPage'
import IngresoFormPage from './features/ingresos/IngresoFormPage'
import KardexPage from './features/mercaderia/KardexPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<RutaProtegida><Layout /></RutaProtegida>}>
        <Route path="/inicio" element={<InicioPage />} />
        <Route path="/proveedores" element={<RutaProtegida roles={rolesDe('/proveedores')}><ProveedoresPage /></RutaProtegida>} />
        <Route path="/proveedores/nuevo" element={<RutaProtegida roles={['ADMINISTRADOR']}><ProveedorFormPage /></RutaProtegida>} />
        <Route path="/proveedores/:id/editar" element={<RutaProtegida roles={['ADMINISTRADOR']}><ProveedorFormPage /></RutaProtegida>} />
        <Route path="/tipos-repuesto" element={<RutaProtegida roles={rolesDe('/tipos-repuesto')}><TiposRepuestoPage /></RutaProtegida>} />
        <Route path="/mercaderia" element={<RutaProtegida roles={rolesDe('/mercaderia')}><MercaderiaPage /></RutaProtegida>} />
        <Route path="/mercaderia/nueva" element={<RutaProtegida roles={['ADMINISTRADOR']}><MercaderiaFormPage /></RutaProtegida>} />
        <Route path="/mercaderia/:id/editar" element={<RutaProtegida roles={['ADMINISTRADOR']}><MercaderiaFormPage /></RutaProtegida>} />
        <Route path="/ingresos" element={<RutaProtegida roles={rolesDe('/ingresos')}><IngresosPage /></RutaProtegida>} />
        <Route path="/ingresos/nuevo" element={<RutaProtegida roles={rolesDe('/ingresos')}><IngresoFormPage /></RutaProtegida>} />
        <Route path="/mercaderia/:id/kardex" element={<RutaProtegida roles={rolesDe('/mercaderia')}><KardexPage /></RutaProtegida>} />
      </Route>

      <Route path="*" element={<Navigate to="/inicio" replace />} />
    </Routes>
  )
}

export default App
