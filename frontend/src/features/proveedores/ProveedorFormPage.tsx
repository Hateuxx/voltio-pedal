import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
    darDeBajaProveedor, eliminarProveedor, listarProveedores,
    listarProveedoresInactivos, reactivarProveedor, type Proveedor,
} from './proveedoresApi'
import { ErrorApi } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'

function mensajeDe(err: unknown) {
    return err instanceof ErrorApi ? err.problema.detail : 'No se pudo conectar con el servidor.'
}

function ProveedoresPage() {
    const [proveedores, setProveedores] = useState<Proveedor[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)          // error al CARGAR la lista
    const [errorAccion, setErrorAccion] = useState<string | null>(null) // error de baja/reactivar/eliminar
    const [verInactivos, setVerInactivos] = useState(false)
    const [recargar, setRecargar] = useState(0)                      // cambiarlo vuelve a pedir la lista
    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    useEffect(() => {
        let vigente = true
        const pedir = verInactivos ? listarProveedoresInactivos : listarProveedores
        pedir()
            .then(lista => {
                if (!vigente) return
                setProveedores(lista)
                setError(null)
            })
            .catch(err => { if (vigente) setError(mensajeDe(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [verInactivos, recargar])

    function cambiarVista() {
        setCargando(true)
        setErrorAccion(null)
        setVerInactivos(v => !v)
    }

    /* Ejecuta una acción y, si sale bien, recarga la lista desde el backend (opción a) */
    async function ejecutar(accion: () => Promise<void>) {
        setErrorAccion(null)
        try {
            await accion()
            setRecargar(n => n + 1)
        } catch (err) {
            setErrorAccion(mensajeDe(err))
        }
    }

    function darDeBaja(p: Proveedor) {
        if (!window.confirm(`¿Dar de baja a "${p.razonSocial}"? Podrás reactivarlo después.`)) return
        ejecutar(() => darDeBajaProveedor(p.idProveedor))
    }

    function reactivar(p: Proveedor) {
        if (!window.confirm(`¿Reactivar a "${p.razonSocial}"?`)) return
        ejecutar(() => reactivarProveedor(p.idProveedor))
    }

    function eliminar(p: Proveedor) {
        if (!window.confirm(`¿Eliminar DEFINITIVAMENTE a "${p.razonSocial}"? Esta acción NO se puede deshacer.`)) return
        ejecutar(() => eliminarProveedor(p.idProveedor))
    }

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="h3 text-primary mb-0">
                    {verInactivos ? 'Proveedores inactivos' : 'Proveedores'}
                </h1>
                {esAdmin && (
                    <div>
                        <button className="btn btn-outline-secondary" onClick={cambiarVista}>
                            {verInactivos ? 'Ver activos' : 'Ver inactivos'}
                        </button>
                        {!verInactivos && (
                            <Link to="/proveedores/nuevo" className="btn btn-success ms-2">
                                Nuevo proveedor
                            </Link>
                        )}
                    </div>
                )}
            </div>

            {cargando && <p className="text-muted">Cargando proveedores...</p>}
            {error && <div className="alert alert-danger">{error}</div>}
            {errorAccion && <div className="alert alert-danger">{errorAccion}</div>}

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
                        {proveedores.length === 0 && (
                            <tr>
                                <td colSpan={esAdmin ? 7 : 6} className="text-center text-muted">
                                    {verInactivos ? 'No hay proveedores inactivos.' : 'No hay proveedores registrados.'}
                                </td>
                            </tr>
                        )}
                        {proveedores.map(p => (
                            <tr key={p.idProveedor}>
                                <td>{p.ruc}</td>
                                <td>{p.razonSocial}</td>
                                <td>{p.contacto ?? '—'}</td>
                                <td>{p.telefono ?? '—'}</td>
                                <td>{p.rubro ?? '—'}</td>
                                <td>{p.frecuenciaEntrega}</td>
                                {esAdmin && (
                                    <td className="text-nowrap">
                                        {verInactivos ? (
                                            <>
                                                <button className="btn btn-sm btn-outline-success" onClick={() => reactivar(p)}>
                                                    Reactivar
                                                </button>
                                                <button className="btn btn-sm btn-danger ms-2" onClick={() => eliminar(p)}>
                                                    Eliminar
                                                </button>
                                            </>
                                        ) : (
                                            <>
                                                <Link to={`/proveedores/${p.idProveedor}/editar`} className="btn btn-sm btn-outline-primary">
                                                    Editar
                                                </Link>
                                                <button className="btn btn-sm btn-outline-danger ms-2" onClick={() => darDeBaja(p)}>
                                                    Dar de baja
                                                </button>
                                            </>
                                        )}
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