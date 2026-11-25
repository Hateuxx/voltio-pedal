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
import ConteosPage from './features/conteos/ConteosPage'
import ConteoDetallePage from './features/conteos/ConteoDetallePage'
import ClientesPage from './features/clientes/ClientesPage'
import ClienteFormPage from './features/clientes/ClienteFormPage'
import VentasPage from './features/ventas/VentasPage'
import VentaFormPage from './features/ventas/VentaFormPage'
import ServiciosPage from './features/servicios/ServiciosPage'
import ServicioFormPage from './features/servicios/ServicioFormPage'
import ServicioDetallePage from './features/servicios/ServicioDetallePage'
import ReportesPage from './features/reportes/ReportesPage'
import AuditoriaPage from './features/auditoria/AuditoriaPage'

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
        <Route path="/conteos" element={<RutaProtegida roles={rolesDe('/conteos')}><ConteosPage /></RutaProtegida>} />
        <Route path="/conteos/:id" element={<RutaProtegida roles={rolesDe('/conteos')}><ConteoDetallePage /></RutaProtegida>} />
        <Route path="/clientes" element={<RutaProtegida roles={rolesDe('/clientes')}><ClientesPage /></RutaProtegida>} />
        <Route path="/clientes/nuevo" element={<RutaProtegida roles={['ADMINISTRADOR', 'OPERADOR_TIENDA']}><ClienteFormPage /></RutaProtegida>} />
        <Route path="/clientes/:id/editar" element={<RutaProtegida roles={['ADMINISTRADOR']}><ClienteFormPage /></RutaProtegida>} />
        <Route path="/ventas" element={<RutaProtegida roles={rolesDe('/ventas')}><VentasPage /></RutaProtegida>} />
        <Route path="/ventas/nueva" element={<RutaProtegida roles={rolesDe('/ventas')}><VentaFormPage /></RutaProtegida>} />
        <Route path="/servicios" element={<RutaProtegida roles={rolesDe('/servicios')}><ServiciosPage /></RutaProtegida>} />
        <Route path="/servicios/nuevo" element={<RutaProtegida roles={['ADMINISTRADOR', 'OPERADOR_TIENDA']}><ServicioFormPage /></RutaProtegida>} />
        <Route path="/servicios/:id" element={<RutaProtegida roles={rolesDe('/servicios')}><ServicioDetallePage /></RutaProtegida>} />
        <Route path="/reportes" element={<RutaProtegida roles={rolesDe('/reportes')}><ReportesPage /></RutaProtegida>} />
        <Route path="/auditoria" element={<RutaProtegida roles={rolesDe('/auditoria')}><AuditoriaPage /></RutaProtegida>} />
      </Route>

      <Route path="*" element={<Navigate to="/inicio" replace />} />
    </Routes>
  )
}

export default App
