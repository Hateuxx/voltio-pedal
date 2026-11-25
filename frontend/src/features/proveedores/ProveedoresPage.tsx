import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listarProveedores, type Proveedor } from './proveedoresApi'
import { ErrorApi } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'

function ProveedoresPage() {
    const [proveedores, setProveedores] = useState<Proveedor[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    useEffect(() => {
        listarProveedores()
            .then(lista => setProveedores(lista))
            .catch(err =>
                setError(err instanceof ErrorApi ? err.problema.detail : 'No se pudo conectar con el servidor.')
            )
            .finally(() => setCargando(false))
    }, [])

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="h3 text-primary mb-0">Proveedores</h1>
                {esAdmin && (
                    <Link to="/proveedores/nuevo" className="btn btn-success">
                        Nuevo proveedor
                    </Link>
                )}
            </div>

            {cargando && <p className="text-muted">Cargando proveedores...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-striped table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>RUC</th>
                            <th>Razón social</th>
                            <th>Contacto</th>
                            <th>Teléfono</th>
                            <th>Rubro</th>
                            <th>Frecuencia</th>
                            {esAdmin && <th>Acciones</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {proveedores.map(p => (
                            <tr key={p.idProveedor}>
                                <td>{p.ruc}</td>
                                <td>{p.razonSocial}</td>
                                <td>{p.contacto ?? '—'}</td>
                                <td>{p.telefono ?? '—'}</td>
                                <td>{p.rubro ?? '—'}</td>
                                <td>{p.frecuenciaEntrega}</td>
                                {esAdmin && (
                                    <td>
                                        <Link to={`/proveedores/${p.idProveedor}/editar`} className="btn btn-sm btn-outline-primary">
                                            Editar
                                        </Link>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}

export default ProveedoresPage