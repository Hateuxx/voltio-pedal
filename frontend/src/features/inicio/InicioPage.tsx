import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'
import { ETIQUETA_TIPO, listarAlertasStock, type Mercaderia } from '../mercaderia/mercaderiaApi'

export default function InicioPage() {
    const usuario = obtenerUsuario()
    const [alertas, setAlertas] = useState<Mercaderia[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        listarAlertasStock()
            .then(lista => { if (vigente) setAlertas(lista) })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    const agotados = alertas.filter(m => m.stock === 0).length

    return (
        <>
            <h1 className="h3 mb-1">Bienvenido{usuario ? `, ${usuario.nombres}` : ''}</h1>
            <p className="text-muted mb-4">Elige una opción del menú.</p>

            <div className="card">
                <div className="card-header d-flex justify-content-between align-items-center">
                    <strong>Alertas de stock</strong>
                    {!cargando && !error && (
                        <span>
                            <span className="badge text-bg-danger me-1">{agotados} agotados</span>
                            <span className="badge text-bg-warning">{alertas.length - agotados} con stock bajo</span>
                        </span>
                    )}
                </div>
                <div className="card-body">
                    {cargando && <p className="text-muted mb-0">Revisando el inventario...</p>}
                    {error && <div className="alert alert-danger mb-0">{error}</div>}
                    {!cargando && !error && alertas.length === 0 && (
                        <p className="text-success mb-0">✓ Todo el inventario está por encima de su stock mínimo.</p>
                    )}
                    {!cargando && !error && alertas.length > 0 && (
                        <table className="table table-sm align-middle mb-0">
                            <thead>
                                <tr>
                                    <th>SKU</th>
                                    <th>Descripción</th>
                                    <th>Tipo</th>
                                    <th className="text-center">Stock</th>
                                    <th className="text-center">Mínimo</th>
                                    <th>Estado</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {alertas.map(m => (
                                    <tr key={m.idMercaderia}>
                                        <td>{m.sku}</td>
                                        <td>{m.descripcion}</td>
                                        <td>{ETIQUETA_TIPO[m.tipo]}</td>
                                        <td className="text-center">{m.stock}</td>
                                        <td className="text-center">{m.stockMinimo}</td>
                                        <td>
                                            {m.stock === 0
                                                ? <span className="badge text-bg-danger">Agotado</span>
                                                : <span className="badge text-bg-warning">Stock bajo</span>}
                                        </td>
                                        <td className="text-end">
                                            <Link to={`/mercaderia/${m.idMercaderia}/kardex`} className="btn btn-sm btn-outline-secondary">Kardex</Link>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </div>
        </>
    )
}