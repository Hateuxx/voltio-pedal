import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { fecha, fechaHora, soles } from '../../utils/formato'
import { listarMercaderias, type Mercaderia } from '../mercaderia/mercaderiaApi'
import {
    actualizarServicio, COLOR_ESTADO, consumirRepuesto, devolverRepuesto, ESTADOS, ETIQUETA_ESTADO,
    listarTecnicos, obtenerServicio, type EstadoServicio, type Servicio, type Tecnico,
} from './serviciosApi'

function primerError(err: unknown) {
    if (err instanceof ErrorApi) return Object.values(err.problema.errores ?? {})[0] ?? err.problema.detail
    return mensajeDeError(err)
}

export default function ServicioDetallePage() {
    const { id } = useParams()
    const idServicio = Number(id)
    const [servicio, setServicio] = useState<Servicio | null>(null)
    const [tecnicos, setTecnicos] = useState<Tecnico[]>([])
    const [repuestos, setRepuestos] = useState<Mercaderia[]>([])
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)

    /* Formulario de avance */
    const [estado, setEstado] = useState<EstadoServicio>('INGRESADO')
    const [diagnostico, setDiagnostico] = useState('')
    const [idTecnico, setIdTecnico] = useState('')
    const [guardando, setGuardando] = useState(false)
    const [errorAvance, setErrorAvance] = useState<string | null>(null)
    const [aviso, setAviso] = useState<string | null>(null)

    /* Formulario de consumo de repuestos (RF17) */
    const [idRepuesto, setIdRepuesto] = useState('')
    const [cantidad, setCantidad] = useState('1')
    const [errorRepuesto, setErrorRepuesto] = useState<string | null>(null)
    const [consumiendo, setConsumiendo] = useState(false)

    /* Copia los datos del servidor a los formularios */
    function mostrar(s: Servicio) {
        setServicio(s)
        setEstado(s.estado)
        setDiagnostico(s.diagnostico ?? '')
        setIdTecnico(s.idTecnico ? String(s.idTecnico) : '')
    }

    useEffect(() => {
        let vigente = true
        Promise.all([obtenerServicio(idServicio), listarTecnicos(), listarMercaderias()])
            .then(([s, tecs, mercs]) => {
                if (!vigente) return
                mostrar(s)
                setTecnicos(tecs)
                setRepuestos(mercs.filter(m => m.tipo === 'REPUESTO'))
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [idServicio])

    async function guardarAvance(e: FormEvent) {
        e.preventDefault()
        setErrorAvance(null)
        setAviso(null)
        if (estado === 'ENTREGADO' && !window.confirm('Al marcarlo como ENTREGADO la orden se cierra y ya no se podrá modificar. ¿Continuar?')) return
        setGuardando(true)
        try {
            mostrar(await actualizarServicio(idServicio, {
                estado, diagnostico: diagnostico.trim() || null, idTecnico: idTecnico === '' ? null : Number(idTecnico),
            }))
            setAviso('Servicio actualizado.')
        } catch (err) {
            setErrorAvance(primerError(err))
        } finally {
            setGuardando(false)
        }
    }

    async function agregarRepuesto(e: FormEvent) {
        e.preventDefault()
        setErrorRepuesto(null)
        setConsumiendo(true)
        try {
            mostrar(await consumirRepuesto(idServicio, idRepuesto === '' ? null : Number(idRepuesto), cantidad === '' ? null : Number(cantidad)))
            /* El stock cambió: se vuelve a pedir el catálogo para mostrar el disponible real */
            setRepuestos((await listarMercaderias()).filter(m => m.tipo === 'REPUESTO'))
            setIdRepuesto('')
            setCantidad('1')
        } catch (err) {
            setErrorRepuesto(primerError(err))
        } finally {
            setConsumiendo(false)
        }
    }

    async function devolver(idDetalle: number, descripcion: string) {
        if (!window.confirm(`¿Devolver "${descripcion}" al almacén? Se registrará en el kardex.`)) return
        setErrorRepuesto(null)
        try {
            mostrar(await devolverRepuesto(idServicio, idDetalle))
            setRepuestos((await listarMercaderias()).filter(m => m.tipo === 'REPUESTO'))
        } catch (err) {
            setErrorRepuesto(primerError(err))
        }
    }

    if (cargando) return <p className="text-muted my-4">Cargando servicio...</p>
    if (errorCarga || !servicio) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga ?? 'No se encontró el servicio.'}</div>
                <Link to="/servicios" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    const abierto = servicio.estado !== 'ENTREGADO'
    const seleccionado = repuestos.find(r => String(r.idMercaderia) === idRepuesto)

    return (
        <>
            <div className="d-flex justify-content-between align-items-start my-4">
                <div>
                    <h1 className="h3 text-primary mb-1">
                        Orden de servicio #{servicio.idServicio}{' '}
                        <span className={`badge fs-6 align-middle ${COLOR_ESTADO[servicio.estado]}`}>{ETIQUETA_ESTADO[servicio.estado]}</span>
                    </h1>
                    <div className="small text-muted">
                        Ingreso: {fechaHora(servicio.fechaIngreso)}{servicio.fechaEntrega && ` · Entregado: ${fechaHora(servicio.fechaEntrega)}`}
                    </div>
                </div>
                <Link to="/servicios" className="btn btn-outline-secondary">Volver</Link>
            </div>

            <div className="row g-3 mb-4">
                <div className="col-md-6">
                    <div className="card card-body h-100">
                        <div><strong>Cliente:</strong> {servicio.cliente}</div>
                        <div><strong>Scooter:</strong> {servicio.scooter}</div>
                        <div><strong>Serie:</strong> {servicio.numeroSerie}</div>
                        <div><strong>Técnico:</strong> {servicio.tecnico ?? 'Sin asignar'}</div>
                        <hr />
                        <div className="small text-muted">Falla reportada</div>
                        <div style={{ whiteSpace: 'pre-wrap' }}>{servicio.fallaReportada}</div>
                    </div>
                </div>
                <div className="col-md-6">
                    <div className={`card card-body h-100 ${servicio.cubiertoPorGarantia ? 'border-success' : ''}`}>
                        <h6>Garantía</h6>
                        {servicio.fechaCompra ? (
                            <>
                                <div>Compra: {fecha(servicio.fechaCompra)} · {servicio.mesesGarantia} meses</div>
                                <div>Vigente hasta: {fecha(servicio.garantiaHasta)}</div>
                                <div className="mt-2">
                                    {servicio.cubiertoPorGarantia
                                        ? <span className="badge text-bg-success">Cubierto por garantía: los repuestos no se cobran</span>
                                        : <span className="badge text-bg-secondary">Garantía vencida: se cobran los repuestos</span>}
                                </div>
                            </>
                        ) : <div className="text-muted">Sin datos de compra: se cobran los repuestos.</div>}
                        <hr />
                        <div className="d-flex justify-content-between"><span>Repuestos usados</span><span>{soles.format(servicio.totalRepuestos)}</span></div>
                        <div className="d-flex justify-content-between fs-5"><strong>A cobrar</strong><strong>{soles.format(servicio.totalACobrar)}</strong></div>
                    </div>
                </div>
            </div>

            {abierto && (
                <form onSubmit={guardarAvance} className="card card-body mb-4">
                    <h5>Avance del servicio</h5>
                    {aviso && <div className="alert alert-success py-2">{aviso}</div>}
                    {errorAvance && <div className="alert alert-danger py-2">{errorAvance}</div>}
                    <div className="row g-3">
                        <div className="col-md-4">
                            <label htmlFor="estado" className="form-label">Estado</label>
                            <select id="estado" className="form-select" value={estado} onChange={e => setEstado(e.target.value as EstadoServicio)}>
                                {ESTADOS.map(x => <option key={x} value={x}>{ETIQUETA_ESTADO[x]}</option>)}
                            </select>
                        </div>
                        <div className="col-md-8">
                            <label htmlFor="tecnico" className="form-label">Técnico</label>
                            <select id="tecnico" className="form-select" value={idTecnico} onChange={e => setIdTecnico(e.target.value)}>
                                <option value="">Sin asignar</option>
                                {tecnicos.map(t => <option key={t.idUsuario} value={t.idUsuario}>{t.nombre}</option>)}
                            </select>
                        </div>
                        <div className="col-12">
                            <label htmlFor="diagnostico" className="form-label">Diagnóstico</label>
                            <textarea id="diagnostico" rows={3} className="form-control" maxLength={2000} value={diagnostico}
                                onChange={e => setDiagnostico(e.target.value)} />
                        </div>
                        <div className="col-12">
                            <button type="submit" className="btn btn-primary" disabled={guardando}>{guardando ? 'Guardando...' : 'Guardar avance'}</button>
                        </div>
                    </div>
                </form>
            )}
            {!abierto && servicio.diagnostico && (
                <div className="card card-body mb-4">
                    <h6>Diagnóstico</h6>
                    <div style={{ whiteSpace: 'pre-wrap' }}>{servicio.diagnostico}</div>
                </div>
            )}

            <h5>Repuestos consumidos (RF17)</h5>
            {errorRepuesto && <div className="alert alert-danger py-2">{errorRepuesto}</div>}
            {abierto && (
                <form onSubmit={agregarRepuesto} className="row g-2 align-items-end mb-3">
                    <div className="col-md-6">
                        <label htmlFor="repuesto" className="form-label small mb-1">Repuesto</label>
                        <select id="repuesto" className="form-select" value={idRepuesto} onChange={e => setIdRepuesto(e.target.value)}>
                            <option value="">Seleccione...</option>
                            {repuestos.filter(r => r.stock > 0).map(r => (
                                <option key={r.idMercaderia} value={r.idMercaderia}>
                                    {r.sku} · {r.descripcion} ({r.tipoRepuesto}) — stock {r.stock}
                                </option>
                            ))}
                        </select>
                        {seleccionado && <div className="form-text">Compatible con: {seleccionado.modelosCompatibles}</div>}
                    </div>
                    <div className="col-md-2">
                        <label htmlFor="cantidad" className="form-label small mb-1">Cantidad</label>
                        <input id="cantidad" type="number" min={1} className="form-control" value={cantidad} onChange={e => setCantidad(e.target.value)} />
                    </div>
                    <div className="col-md-4">
                        <button type="submit" className="btn btn-outline-primary" disabled={consumiendo}>
                            {consumiendo ? 'Registrando...' : '+ Usar repuesto (descuenta stock)'}
                        </button>
                    </div>
                </form>
            )}
            <table className="table align-middle">
                <thead className="table-light">
                    <tr>
                        <th>Fecha</th>
                        <th>SKU</th>
                        <th>Descripción</th>
                        <th className="text-end">Cant.</th>
                        <th className="text-end">P. unit.</th>
                        <th className="text-end">Subtotal</th>
                        {abierto && <th></th>}
                    </tr>
                </thead>
                <tbody>
                    {servicio.repuestos.length === 0 && (
                        <tr><td colSpan={abierto ? 7 : 6} className="text-center text-muted">Aún no se usaron repuestos.</td></tr>
                    )}
                    {servicio.repuestos.map(r => (
                        <tr key={r.idDetalle}>
                            <td className="text-nowrap">{fechaHora(r.fechaHora)}</td>
                            <td>{r.sku}</td>
                            <td>{r.descripcion}</td>
                            <td className="text-end">{r.cantidad}</td>
                            <td className="text-end">{soles.format(r.precioUnitario)}</td>
                            <td className="text-end">{soles.format(r.subtotal)}</td>
                            {abierto && (
                                <td className="text-end">
                                    <button className="btn btn-sm btn-outline-secondary" onClick={() => devolver(r.idDetalle, r.descripcion)}>Devolver</button>
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    )
}
