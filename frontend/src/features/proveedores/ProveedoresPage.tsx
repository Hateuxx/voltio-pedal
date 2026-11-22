import { useEffect, useState } from 'react'
import { listarProveedores, type Proveedor } from './proveedoresApi'
import { ErrorApi } from '../../api/cliente'

function ProveedoresPage() {
    const [proveedores, setProveedores] = useState<Proveedor[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)

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
            <h1 className="h3 text-primary mb-4">Proveedores</h1>

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
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}

export default ProveedoresPage