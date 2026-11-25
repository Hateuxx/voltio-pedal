import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'
import { eliminarCliente, listarClientes, nombreCompleto, type Cliente } from './clientesApi'

export default function ClientesPage() {
    const [clientes, setClientes] = useState<Cliente[]>([])
    const [busqueda, setBusqueda] = useState('')
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [errorAccion, setErrorAccion] = useState<string | null>(null)
    const [recargar, setRecargar] = useState(0)
    const rol = obtenerUsuario()?.rol
    const esAdmin = rol === 'ADMINISTRADOR'
    const puedeRegistrar = esAdmin || rol === 'OPERADOR_TIENDA'

    useEffect(() => {
        let vigente = true
        listarClientes()
            .then(lista => { if (vigente) { setClientes(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [recargar])

    const texto = busqueda.trim().toLowerCase()
    const visibles = clientes.filter(c => texto === ''
        || c.numeroDocumento.includes(texto)
        || nombreCompleto(c).toLowerCase().includes(texto))

    async function eliminar(c: Cliente) {
        if (!window.confirm(`¿Eliminar DEFINITIVAMENTE al cliente "${nombreCompleto(c)}"? Esta acción NO se puede deshacer.`)) return
        setErrorAccion(null)
        try {
            await eliminarCliente(c.idCliente)
            setRecargar(n => n + 1)
        } catch (err) {
            setErrorAccion(mensajeDeError(err))
        }
    }

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 text-primary mb-0">Clientes</h1>
                {puedeRegistrar && <Link to="/clientes/nuevo" className="btn btn-success">Nuevo cliente</Link>}
            </div>

            <input className="form-control mb-3" placeholder="Buscar por documento o nombre..."
                value={busqueda} onChange={e => setBusqueda(e.target.value)} />

            {cargando && <p className="text-muted">Cargando clientes...</p>}
            {error && <div className="alert alert-danger">{error}</div>}
            {errorAccion && <div className="alert alert-danger">{errorAccion}</div>}

            {!cargando && !error && (
                <table className="table table-striped table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Documento</th>
                            <th>Nombre / Razón social</th>
                            <th>Teléfono</th>
                            {esAdmin && <th>Acciones</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {visibles.length === 0 && (
                            <tr><td colSpan={esAdmin ? 4 : 3} className="text-center text-muted">
                                {clientes.length === 0 ? 'No hay clientes registrados.' : 'Ningún cliente coincide con la búsqueda.'}
                            </td></tr>
                        )}
                        {visibles.map(c => (
                            <tr key={c.idCliente}>
                                <td className="text-nowrap"><span className="badge text-bg-secondary me-2">{c.tipoDocumento}</span>{c.numeroDocumento}</td>
                                <td>{nombreCompleto(c)}</td>
                                <td>{c.telefono ?? '—'}</td>
                                {esAdmin && (
                                    <td className="text-nowrap">
                                        <Link to={`/clientes/${c.idCliente}/editar`} className="btn btn-sm btn-outline-primary">Editar</Link>
                                        <button className="btn btn-sm btn-danger ms-2" onClick={() => eliminar(c)}>Eliminar</button>
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
