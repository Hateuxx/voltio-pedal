import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'
import DialogoMotivo, { type AccionConMotivo } from '../../components/DialogoMotivo'
import {
    darDeBajaMercaderia, eliminarMercaderia, ETIQUETA_TIPO, listarMercaderias,
    listarMercaderiasInactivas, reactivarMercaderia, type Mercaderia, type TipoMercaderia,
} from './mercaderiaApi'

const soles = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })

/* Texto de la segunda línea según el tipo */
function detalle(m: Mercaderia) {
    switch (m.tipo) {
        case 'SCOOTER':
            return `${m.autonomiaKm} km · ${m.potenciaWatts} W · ${m.color} · ${m.mesesGarantia} meses de garantía`
        case 'REPUESTO':
            return `${m.tipoRepuesto} · Compatible con: ${m.modelosCompatibles}`
        case 'ACCESORIO':
            return [m.talla && `Talla ${m.talla}`, m.color].filter(Boolean).join(' · ') || 'Sin detalles'
    }
}

export default function MercaderiaPage() {
    const [mercaderias, setMercaderias] = useState<Mercaderia[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [errorAccion, setErrorAccion] = useState<string | null>(null)
    const [verInactivos, setVerInactivos] = useState(false)
    const [recargar, setRecargar] = useState(0)
    const [pendiente, setPendiente] = useState<AccionConMotivo | null>(null)
    const [filtroTipo, setFiltroTipo] = useState<'TODOS' | TipoMercaderia>('TODOS')
    const [busqueda, setBusqueda] = useState('')
    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    useEffect(() => {
        let vigente = true
        const pedir = verInactivos ? listarMercaderiasInactivas : listarMercaderias
        pedir()
            .then(lista => { if (vigente) { setMercaderias(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [verInactivos, recargar])

    /* Estado DERIVADO: se calcula en cada render a partir de otros estados (no necesita useState ni useEffect) */
    const texto = busqueda.trim().toLowerCase()
    const visibles = mercaderias.filter(m =>
        (filtroTipo === 'TODOS' || m.tipo === filtroTipo) &&
        (texto === '' || m.sku.toLowerCase().includes(texto) || m.descripcion.toLowerCase().includes(texto)))

    function cambiarVista() {
        setCargando(true)
        setErrorAccion(null)
        setVerInactivos(v => !v)
    }

    /* Abre el diálogo de motivo; si la acción sale bien, recarga la lista desde el backend */
    function pedirMotivo(accion: AccionConMotivo) {
        setErrorAccion(null)
        setPendiente({ ...accion, ejecutar: async motivo => { await accion.ejecutar(motivo); setRecargar(n => n + 1) } })
    }

    function darDeBaja(m: Mercaderia) {
        pedirMotivo({
            titulo: 'Dar de baja mercadería', textoBoton: 'Dar de baja', peligro: true,
            mensaje: `"${m.descripcion}" (${m.sku}) dejará de venderse. Podrás reactivarla después.`,
            ejecutar: motivo => darDeBajaMercaderia(m.idMercaderia, motivo),
        })
    }

    function reactivar(m: Mercaderia) {
        pedirMotivo({
            titulo: 'Reactivar mercadería', textoBoton: 'Reactivar',
            mensaje: `"${m.descripcion}" (${m.sku}) volverá al catálogo.`,
            ejecutar: motivo => reactivarMercaderia(m.idMercaderia, motivo),
        })
    }

    function eliminar(m: Mercaderia) {
        pedirMotivo({
            titulo: 'Eliminar mercadería DEFINITIVAMENTE', textoBoton: 'Eliminar', peligro: true,
            mensaje: `Se borrará "${m.descripcion}" (${m.sku}). Esta acción NO se puede deshacer.`,
            ejecutar: motivo => eliminarMercaderia(m.idMercaderia, motivo),
        })
    }

    return (
        <>
            {pendiente && <DialogoMotivo accion={pendiente} onCerrar={() => setPendiente(null)} />}
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 text-primary mb-0">
                    {verInactivos ? 'Mercadería inactiva' : 'Mercadería'}
                </h1>
                {esAdmin && (
                    <div>
                        <button className="btn btn-outline-secondary" onClick={cambiarVista}>
                            {verInactivos ? 'Ver activos' : 'Ver inactivos'}
                        </button>
                        {!verInactivos && (
                            <Link to="/mercaderia/nueva" className="btn btn-success ms-2">
                                Nueva mercadería
                            </Link>
                        )}
                    </div>
                )}
            </div>

            <div className="row g-2 mb-3">
                <div className="col-md-3">
                    <select className="form-select" value={filtroTipo}
                        onChange={e => setFiltroTipo(e.target.value as 'TODOS' | TipoMercaderia)}>
                        <option value="TODOS">Todos los tipos</option>
                        <option value="SCOOTER">Scooters</option>
                        <option value="REPUESTO">Repuestos</option>
                        <option value="ACCESORIO">Accesorios</option>
                    </select>
                </div>
                <div className="col-md-5">
                    <input className="form-control" placeholder="Buscar por SKU o descripción..."
                        value={busqueda} onChange={e => setBusqueda(e.target.value)} />
                </div>
            </div>

            {cargando && <p className="text-muted">Cargando mercadería...</p>}
            {error && <div className="alert alert-danger">{error}</div>}
            {errorAccion && <div className="alert alert-danger">{errorAccion}</div>}

            {!cargando && !error && (
                <table className="table table-striped table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>SKU</th>
                            <th>Tipo</th>
                            <th>Descripción</th>
                            <th>Proveedor</th>
                            <th className="text-center">Stock</th>
                            <th className="text-end">P. venta</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {visibles.length === 0 && (
                            <tr>
                                <td colSpan={7} className="text-center text-muted">
                                    {mercaderias.length === 0 ? 'No hay mercadería registrada.' : 'Ninguna mercadería coincide con el filtro.'}
                                </td>
                            </tr>
                        )}
                        {visibles.map(m => (
                            <tr key={m.idMercaderia}>
                                <td className="text-nowrap">{m.sku}</td>
                                <td><span className="badge text-bg-secondary">{ETIQUETA_TIPO[m.tipo]}</span></td>
                                <td>
                                    {m.descripcion}
                                    <div className="small text-muted">{detalle(m)}</div>
                                </td>
                                <td>{m.proveedor}</td>
                                <td className="text-center">
                                    <span className={`badge ${m.stock <= m.stockMinimo ? 'text-bg-danger' : 'text-bg-success'}`}
                                        title={m.stock <= m.stockMinimo ? 'Stock en o por debajo del mínimo' : undefined}>
                                        {m.stock}
                                    </span>
                                    <div className="small text-muted">mín. {m.stockMinimo}</div>
                                </td>
                                <td className="text-end text-nowrap">{soles.format(m.precioVenta)}</td>
                                {esAdmin && (
                                    <td className="text-nowrap">
                                        <Link to={`/mercaderia/${m.idMercaderia}/kardex`} className="btn btn-sm btn-outline-secondary">
                                            Kardex
                                        </Link>
                                        {esAdmin && (verInactivos ? (
                                            <>
                                                <button className="btn btn-sm btn-outline-success ms-2" onClick={() => reactivar(m)}>Reactivar</button>
                                                <button className="btn btn-sm btn-danger ms-2" onClick={() => eliminar(m)}>Eliminar</button>
                                            </>
                                        ) : (
                                            <>
                                                <Link to={`/mercaderia/${m.idMercaderia}/editar`} className="btn btn-sm btn-outline-primary ms-2">Editar</Link>
                                                <button className="btn btn-sm btn-outline-danger ms-2" onClick={() => darDeBaja(m)}>Dar de baja</button>
                                            </>
                                        ))}
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