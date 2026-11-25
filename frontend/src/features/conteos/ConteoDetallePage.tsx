import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'
import { listarMercaderias, type Mercaderia } from '../mercaderia/mercaderiaApi'
import BadgeExactitud from './exactitud'
import {
    cerrarConteo, obtenerConteo, quitarLineaConteo, registrarLineaConteo, type Conteo,
} from './conteosApi'

const fecha = (iso: string | null) =>
    iso ? new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' }) : '—'

/* Muestra +2 en verde, −2 en rojo, 0 en gris */
function Diferencia({ valor }: { valor: number }) {
    if (valor === 0) return <span className="text-muted">0</span>
    return <strong className={valor > 0 ? 'text-success' : 'text-danger'}>{valor > 0 ? `+${valor}` : `−${Math.abs(valor)}`}</strong>
}

/* Primer mensaje de error de un 400 de validación, o el detail general */
function mensajeCampo(err: unknown) {
    if (err instanceof ErrorApi) {
        return Object.values(err.problema.errores ?? {})[0] ?? err.problema.detail
    }
    return mensajeDeError(err)
}

export default function ConteoDetallePage() {
    const { id } = useParams()
    const idConteo = Number(id)
    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    const [conteo, setConteo] = useState<Conteo | null>(null)
    const [mercaderias, setMercaderias] = useState<Mercaderia[]>([])
    const [borradores, setBorradores] = useState<Record<number, string>>({})   /* lo que se escribe y aún no se guarda */
    const [busqueda, setBusqueda] = useState('')
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorAccion, setErrorAccion] = useState<string | null>(null)
    const [guardandoId, setGuardandoId] = useState<number | null>(null)
    const [motivo, setMotivo] = useState('')
    const [cerrando, setCerrando] = useState(false)
    const [errorMotivo, setErrorMotivo] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        Promise.all([obtenerConteo(idConteo), listarMercaderias()])
            .then(([c, mercs]) => {
                if (!vigente) return
                setConteo(c)
                setMercaderias(mercs)
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [idConteo])

    async function guardarLinea(m: Mercaderia) {
        const texto = borradores[m.idMercaderia] ?? ''
        setErrorAccion(null)
        setGuardandoId(m.idMercaderia)
        try {
            /* El backend devuelve el conteo COMPLETO actualizado: lo usamos tal cual */
            setConteo(await registrarLineaConteo(idConteo, m.idMercaderia, texto.trim() === '' ? null : Number(texto)))
            setBorradores(prev => {
                const copia = { ...prev }
                delete copia[m.idMercaderia]
                return copia
            })
        } catch (err) {
            setErrorAccion(`${m.sku}: ${mensajeCampo(err)}`)
        } finally {
            setGuardandoId(null)
        }
    }

    async function quitar(m: Mercaderia) {
        setErrorAccion(null)
        try {
            setConteo(await quitarLineaConteo(idConteo, m.idMercaderia))
        } catch (err) {
            setErrorAccion(mensajeDeError(err))
        }
    }

    async function cerrar(e: FormEvent) {
        e.preventDefault()
        if (!conteo) return
        if (!window.confirm(`Se cerrará el conteo #${conteo.idConteo} y se aplicarán ${conteo.conDiferencia} ajuste(s) al inventario. `
            + 'Esta acción NO se puede deshacer. ¿Continuar?')) return
        setErrorMotivo(null)
        setCerrando(true)
        try {
            setConteo(await cerrarConteo(conteo.idConteo, motivo.trim()))
        } catch (err) {
            setErrorMotivo(mensajeCampo(err))
        } finally {
            setCerrando(false)
        }
    }

    if (cargando) return <p className="text-muted my-4">Cargando conteo...</p>

    if (errorCarga || !conteo) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga ?? 'No se encontró el conteo.'}</div>
                <Link to="/conteos" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    const abierto = conteo.estado === 'ABIERTO'
    const lineaDe = new Map(conteo.lineas.map(l => [l.idMercaderia, l]))
    const texto = busqueda.trim().toLowerCase()
    const visibles = mercaderias.filter(m =>
        texto === '' || m.sku.toLowerCase().includes(texto) || m.descripcion.toLowerCase().includes(texto))

    return (
        <>
            <div className="d-flex justify-content-between align-items-start my-4">
                <div>
                    <h1 className="h3 text-primary mb-1">
                        Conteo #{conteo.idConteo}{' '}
                        <span className={`badge fs-6 align-middle ${abierto ? 'text-bg-primary' : 'text-bg-secondary'}`}>{conteo.estado}</span>
                    </h1>
                    <div className="small text-muted">
                        Inicio: {fecha(conteo.fechaInicio)}{conteo.fechaCierre && ` · Cierre: ${fecha(conteo.fechaCierre)}`}
                        {conteo.observacion && ` · ${conteo.observacion}`}
                    </div>
                </div>
                <Link to="/conteos" className="btn btn-outline-secondary">Volver</Link>
            </div>

            <div className="row g-3 mb-4">
                <div className="col-md-4">
                    <div className="card card-body text-center">
                        <div className="small text-muted">Productos contados</div>
                        <div className="fs-4">{conteo.productosContados}{abierto && <span className="fs-6 text-muted"> / {mercaderias.length}</span>}</div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card card-body text-center">
                        <div className="small text-muted">Con diferencia</div>
                        <div className="fs-4">{conteo.conDiferencia}</div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card card-body text-center">
                        <div className="small text-muted">Exactitud</div>
                        <div className="fs-4"><BadgeExactitud valor={conteo.exactitud} /></div>
                    </div>
                </div>
            </div>

            {errorAccion && <div className="alert alert-danger">{errorAccion}</div>}

            {abierto ? (
                <>
                    <input className="form-control mb-3" placeholder="Buscar por SKU o descripción..."
                        value={busqueda} onChange={e => setBusqueda(e.target.value)} />

                    <table className="table align-middle">
                        <thead className="table-light">
                            <tr>
                                <th>SKU</th>
                                <th>Descripción</th>
                                <th className="text-center">Stock sistema</th>
                                <th style={{ width: '14%' }}>Stock físico</th>
                                <th className="text-center">Diferencia</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {visibles.map(m => {
                                const linea = lineaDe.get(m.idMercaderia)
                                const valor = borradores[m.idMercaderia] ?? (linea ? String(linea.stockFisico) : '')
                                const pendiente = borradores[m.idMercaderia] !== undefined
                                return (
                                    <tr key={m.idMercaderia}
                                        className={linea && !pendiente ? (linea.diferencia === 0 ? 'table-success' : 'table-warning') : ''}>
                                        <td className="text-nowrap">{m.sku}</td>
                                        <td>{m.descripcion}</td>
                                        {/* Si ya se contó, muestra la FOTO; si no, el stock actual */}
                                        <td className="text-center">{linea ? linea.stockSistema : m.stock}</td>
                                        <td>
                                            <input type="number" min={0} step={1} className="form-control form-control-sm" value={valor}
                                                onChange={e => setBorradores(prev => ({ ...prev, [m.idMercaderia]: e.target.value }))}
                                                onKeyDown={e => { if (e.key === 'Enter') guardarLinea(m) }} />
                                        </td>
                                        <td className="text-center">{linea && !pendiente ? <Diferencia valor={linea.diferencia} /> : '—'}</td>
                                        <td className="text-end text-nowrap">
                                            <button className="btn btn-sm btn-primary" disabled={!pendiente || guardandoId === m.idMercaderia}
                                                onClick={() => guardarLinea(m)}>
                                                {guardandoId === m.idMercaderia ? '...' : 'Guardar'}
                                            </button>
                                            {linea && (
                                                <button className="btn btn-sm btn-outline-danger ms-2" onClick={() => quitar(m)}>Quitar</button>
                                            )}
                                        </td>
                                    </tr>
                                )
                            })}
                        </tbody>
                    </table>

                    {esAdmin ? (
                        <form onSubmit={cerrar} className="card card-body border-danger mt-4">
                            <h5 className="text-danger">Cerrar conteo y aplicar ajustes</h5>
                            <p className="small text-muted mb-2">
                                Cada producto con diferencia generará un ajuste (+/−) en su kardex con este motivo. No se puede deshacer.
                            </p>
                            <textarea className={`form-control mb-2 ${errorMotivo ? 'is-invalid' : ''}`} rows={2} maxLength={200}
                                placeholder="Motivo de los ajustes. Ej.: Diferencias del conteo físico mensual"
                                value={motivo} onChange={e => setMotivo(e.target.value)} />
                            {errorMotivo && <div className="invalid-feedback d-block mb-2">{errorMotivo}</div>}
                            <div>
                                <button type="submit" className="btn btn-danger" disabled={cerrando || conteo.productosContados === 0 || motivo.trim() === ''}>
                                    {cerrando ? 'Cerrando...' : `Cerrar conteo (${conteo.conDiferencia} ajuste${conteo.conDiferencia === 1 ? '' : 's'})`}
                                </button>
                            </div>
                        </form>
                    ) : (
                        <div className="alert alert-secondary mt-4">Solo un administrador puede cerrar el conteo y aplicar los ajustes.</div>
                    )}
                </>
            ) : (
                <table className="table table-striped align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>SKU</th>
                            <th>Descripción</th>
                            <th className="text-center">Stock sistema</th>
                            <th className="text-center">Stock físico</th>
                            <th className="text-center">Diferencia</th>
                            <th className="text-center">Ajuste</th>
                        </tr>
                    </thead>
                    <tbody>
                        {conteo.lineas.map(l => (
                            <tr key={l.idMercaderia}>
                                <td>{l.sku}</td>
                                <td>{l.descripcion}</td>
                                <td className="text-center">{l.stockSistema}</td>
                                <td className="text-center">{l.stockFisico}</td>
                                <td className="text-center"><Diferencia valor={l.diferencia} /></td>
                                <td className="text-center">
                                    {l.ajustado
                                        ? <Link to={`/mercaderia/${l.idMercaderia}/kardex`} className="badge text-bg-info text-decoration-none">Ajustado · ver kardex</Link>
                                        : <span className="text-muted">—</span>}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}