import { useEffect, useState, type ReactNode } from 'react'
import { mensajeDeError } from '../../api/cliente'
import { fecha, fechaHora, hoyIso, soles } from '../../utils/formato'
import { ETIQUETA_MOVIMIENTO, type TipoMovimiento } from '../mercaderia/mercaderiaApi'
import { ETIQUETA_ESTADO, type EstadoServicio } from '../servicios/serviciosApi'
import {
    reporteInventario, reporteMovimientos, reporteServicios, reporteVentas,
    type MovimientoReporte, type ReporteInventario, type ReporteServicios, type ReporteVentas,
} from './reportesApi'

type Pestana = 'inventario' | 'ventas' | 'movimientos' | 'servicios'

const PESTANAS: { id: Pestana; texto: string }[] = [
    { id: 'inventario', texto: 'Inventario valorizado' },
    { id: 'ventas', texto: 'Ventas y utilidad' },
    { id: 'movimientos', texto: 'Movimientos del almacén' },
    { id: 'servicios', texto: 'Servicio técnico' },
]

function Tarjeta({ titulo, valor, extra }: { titulo: string; valor: ReactNode; extra?: ReactNode }) {
    return (
        <div className="col-md-3">
            <div className="card card-body text-center h-100">
                <div className="small text-muted">{titulo}</div>
                <div className="fs-4 fw-semibold">{valor}</div>
                {extra && <div className="small text-muted">{extra}</div>}
            </div>
        </div>
    )
}

type Datos =
    | { tipo: 'inventario'; datos: ReporteInventario }
    | { tipo: 'ventas'; datos: ReporteVentas }
    | { tipo: 'movimientos'; datos: MovimientoReporte[] }
    | { tipo: 'servicios'; datos: ReporteServicios }

export default function ReportesPage() {
    const [pestana, setPestana] = useState<Pestana>('inventario')
    const [desde, setDesde] = useState(hoyIso(-30))
    const [hasta, setHasta] = useState(hoyIso())
    const [tipoMov, setTipoMov] = useState('')
    const [resultado, setResultado] = useState<Datos | null>(null)
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        const pedir: Promise<Datos> =
            pestana === 'inventario' ? reporteInventario().then(d => ({ tipo: 'inventario' as const, datos: d }))
            : pestana === 'ventas' ? reporteVentas(desde, hasta).then(d => ({ tipo: 'ventas' as const, datos: d }))
            : pestana === 'movimientos' ? reporteMovimientos(desde, hasta, tipoMov).then(d => ({ tipo: 'movimientos' as const, datos: d }))
            : reporteServicios(desde, hasta).then(d => ({ tipo: 'servicios' as const, datos: d }))
        pedir
            .then(r => { if (vigente) { setResultado(r); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [pestana, desde, hasta, tipoMov])

    /* Cada cambio de filtro vuelve a pedir el reporte */
    function con<T>(set: (v: T) => void) {
        return (v: T) => { setCargando(true); set(v) }
    }

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-3 d-print-none">
                <h1 className="h3 text-primary mb-0">Reportes</h1>
                <button className="btn btn-outline-secondary" onClick={() => window.print()}>Imprimir / PDF</button>
            </div>

            <ul className="nav nav-tabs mb-3 d-print-none">
                {PESTANAS.map(p => (
                    <li className="nav-item" key={p.id}>
                        <button className={`nav-link ${pestana === p.id ? 'active' : ''}`} onClick={() => con(setPestana)(p.id)}>{p.texto}</button>
                    </li>
                ))}
            </ul>

            {pestana !== 'inventario' && (
                <div className="row g-2 mb-3 align-items-end d-print-none">
                    <div className="col-md-3">
                        <label className="form-label small mb-1">Desde</label>
                        <input type="date" className="form-control" value={desde} max={hasta} onChange={e => con(setDesde)(e.target.value)} />
                    </div>
                    <div className="col-md-3">
                        <label className="form-label small mb-1">Hasta</label>
                        <input type="date" className="form-control" value={hasta} min={desde} onChange={e => con(setHasta)(e.target.value)} />
                    </div>
                    {pestana === 'movimientos' && (
                        <div className="col-md-4">
                            <label className="form-label small mb-1">Tipo de movimiento</label>
                            <select className="form-select" value={tipoMov} onChange={e => con(setTipoMov)(e.target.value)}>
                                <option value="">Todos</option>
                                {(Object.keys(ETIQUETA_MOVIMIENTO) as TipoMovimiento[]).map(t => <option key={t} value={t}>{ETIQUETA_MOVIMIENTO[t]}</option>)}
                            </select>
                        </div>
                    )}
                </div>
            )}

            <h2 className="h5 d-none d-print-block">Voltio &amp; Pedal · {PESTANAS.find(p => p.id === pestana)?.texto}</h2>

            {cargando && <p className="text-muted">Generando reporte...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && resultado?.tipo === 'inventario' && <Inventario r={resultado.datos} />}
            {!cargando && !error && resultado?.tipo === 'ventas' && <Ventas r={resultado.datos} />}
            {!cargando && !error && resultado?.tipo === 'movimientos' && <Movimientos filas={resultado.datos} />}
            {!cargando && !error && resultado?.tipo === 'servicios' && <Servicios r={resultado.datos} />}
        </>
    )
}

function Inventario({ r }: { r: ReporteInventario }) {
    return (
        <>
            <div className="row g-3 mb-4">
                <Tarjeta titulo="Productos activos" valor={r.productos} extra={`${r.unidades} unidades`} />
                <Tarjeta titulo="Valor al costo" valor={soles.format(r.valorCosto)} />
                <Tarjeta titulo="Valor a precio de venta" valor={soles.format(r.valorVenta)} />
                <Tarjeta titulo="En o bajo el mínimo" valor={<span className={r.bajoMinimo > 0 ? 'text-danger' : 'text-success'}>{r.bajoMinimo}</span>} />
            </div>
            <table className="table table-sm table-striped align-middle">
                <thead className="table-light">
                    <tr>
                        <th>SKU</th><th>Descripción</th><th>Tipo</th><th>Proveedor</th>
                        <th className="text-end">Stock</th><th className="text-end">Mín.</th>
                        <th className="text-end">Costo unit.</th><th className="text-end">Valor costo</th><th className="text-end">Valor venta</th>
                    </tr>
                </thead>
                <tbody>
                    {r.lineas.map(l => (
                        <tr key={l.sku} className={l.bajoMinimo ? 'table-warning' : ''}>
                            <td>{l.sku}</td><td>{l.descripcion}</td><td>{l.tipo}</td><td>{l.proveedor}</td>
                            <td className="text-end">{l.stock}</td><td className="text-end">{l.stockMinimo}</td>
                            <td className="text-end">{soles.format(l.costoUnitario)}</td>
                            <td className="text-end">{soles.format(l.valorCosto)}</td>
                            <td className="text-end">{soles.format(l.valorVenta)}</td>
                        </tr>
                    ))}
                </tbody>
                <tfoot>
                    <tr className="fw-bold">
                        <td colSpan={7} className="text-end">Totales</td>
                        <td className="text-end">{soles.format(r.valorCosto)}</td>
                        <td className="text-end">{soles.format(r.valorVenta)}</td>
                    </tr>
                </tfoot>
            </table>
            <p className="small text-muted">Generado el {fechaHora(r.generado)}.</p>
        </>
    )
}

function Ventas({ r }: { r: ReporteVentas }) {
    return (
        <>
            <p className="text-muted">Del {fecha(r.desde)} al {fecha(r.hasta)}</p>
            <div className="row g-3 mb-4">
                <Tarjeta titulo="Ventas válidas" valor={r.cantidadVentas} extra={`${r.ventasAnuladas} anulada(s)`} />
                <Tarjeta titulo="Total vendido" valor={soles.format(r.totalVendido)} />
                <Tarjeta titulo="Costo de lo vendido" valor={soles.format(r.costoTotal)} />
                <Tarjeta titulo="Utilidad bruta" valor={<span className="text-success">{soles.format(r.utilidad)}</span>} extra={`Margen ${r.margen}%`} />
            </div>
            <div className="row g-4">
                <div className="col-lg-7">
                    <h6>Productos más vendidos</h6>
                    <table className="table table-sm table-striped">
                        <thead className="table-light">
                            <tr><th>SKU</th><th>Descripción</th><th className="text-end">Cant.</th><th className="text-end">Importe</th><th className="text-end">Utilidad</th></tr>
                        </thead>
                        <tbody>
                            {r.productos.length === 0 && <tr><td colSpan={5} className="text-center text-muted">Sin ventas en el rango.</td></tr>}
                            {r.productos.map(p => (
                                <tr key={p.sku}>
                                    <td>{p.sku}</td><td>{p.descripcion}</td><td className="text-end">{p.cantidad}</td>
                                    <td className="text-end">{soles.format(p.importe)}</td><td className="text-end">{soles.format(p.utilidad)}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="col-lg-5">
                    <h6>Ventas por día</h6>
                    <table className="table table-sm table-striped">
                        <thead className="table-light"><tr><th>Fecha</th><th className="text-end">Ventas</th><th className="text-end">Total</th></tr></thead>
                        <tbody>
                            {r.porDia.length === 0 && <tr><td colSpan={3} className="text-center text-muted">—</td></tr>}
                            {r.porDia.map(d => (
                                <tr key={d.fecha}><td>{fecha(d.fecha)}</td><td className="text-end">{d.ventas}</td><td className="text-end">{soles.format(d.total)}</td></tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </>
    )
}

function Movimientos({ filas }: { filas: MovimientoReporte[] }) {
    const entradas = filas.filter(f => f.entrada).reduce((s, f) => s + f.cantidad, 0)
    const salidas = filas.filter(f => !f.entrada).reduce((s, f) => s + f.cantidad, 0)
    return (
        <>
            <div className="row g-3 mb-4">
                <Tarjeta titulo="Movimientos" valor={filas.length} />
                <Tarjeta titulo="Unidades que entraron" valor={<span className="text-success">+{entradas}</span>} />
                <Tarjeta titulo="Unidades que salieron" valor={<span className="text-danger">−{salidas}</span>} />
            </div>
            <table className="table table-sm table-striped align-middle">
                <thead className="table-light">
                    <tr><th>Fecha</th><th>Movimiento</th><th>Producto</th><th>Origen</th><th>Usuario</th><th className="text-end">Cant.</th><th className="text-end">Saldo</th></tr>
                </thead>
                <tbody>
                    {filas.length === 0 && <tr><td colSpan={7} className="text-center text-muted">Sin movimientos en el rango.</td></tr>}
                    {filas.map((f, i) => (
                        <tr key={i}>
                            <td className="text-nowrap">{fechaHora(f.fechaHora)}</td>
                            <td>
                                <span className={`badge ${f.entrada ? 'text-bg-success' : 'text-bg-danger'}`}>{ETIQUETA_MOVIMIENTO[f.tipo as TipoMovimiento] ?? f.tipo}</span>
                                {f.observacion && <div className="small text-muted">{f.observacion}</div>}
                            </td>
                            <td>{f.sku} · {f.descripcion}</td>
                            <td>{f.origen}</td>
                            <td>{f.usuario}</td>
                            <td className={`text-end ${f.entrada ? 'text-success' : 'text-danger'}`}>{f.entrada ? '+' : '−'}{f.cantidad}</td>
                            <td className="text-end">{f.stockAnterior} → <strong>{f.stockResultante}</strong></td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    )
}

function Servicios({ r }: { r: ReporteServicios }) {
    return (
        <>
            <p className="text-muted">Órdenes ingresadas del {fecha(r.desde)} al {fecha(r.hasta)}</p>
            <div className="row g-3 mb-4">
                <Tarjeta titulo="Órdenes de servicio" valor={r.total} extra={`${r.entregados} entregadas`} />
                <Tarjeta titulo="Cubiertas por garantía" valor={r.cubiertosPorGarantia} />
                <Tarjeta titulo="Repuestos usados (valor)" valor={soles.format(r.importeRepuestos)} />
                <Tarjeta titulo="Repuestos cobrados" valor={soles.format(r.importeCobrado)} />
            </div>
            <div className="row g-4">
                <div className="col-lg-4">
                    <h6>Por estado</h6>
                    <table className="table table-sm">
                        <tbody>
                            {Object.entries(r.porEstado).map(([estado, n]) => (
                                <tr key={estado}><td>{ETIQUETA_ESTADO[estado as EstadoServicio] ?? estado}</td><td className="text-end">{n}</td></tr>
                            ))}
                        </tbody>
                    </table>
                </div>
                <div className="col-lg-4">
                    <h6>Por técnico</h6>
                    <table className="table table-sm">
                        <thead className="table-light"><tr><th>Técnico</th><th className="text-end">Asignadas</th><th className="text-end">Entregadas</th></tr></thead>
                        <tbody>
                            {r.porTecnico.length === 0 && <tr><td colSpan={3} className="text-center text-muted">—</td></tr>}
                            {r.porTecnico.map(t => <tr key={t.tecnico}><td>{t.tecnico}</td><td className="text-end">{t.asignados}</td><td className="text-end">{t.entregados}</td></tr>)}
                        </tbody>
                    </table>
                </div>
                <div className="col-lg-4">
                    <h6>Repuestos más usados</h6>
                    <table className="table table-sm">
                        <thead className="table-light"><tr><th>Repuesto</th><th className="text-end">Cant.</th><th className="text-end">Importe</th></tr></thead>
                        <tbody>
                            {r.repuestos.length === 0 && <tr><td colSpan={3} className="text-center text-muted">—</td></tr>}
                            {r.repuestos.map(x => <tr key={x.sku}><td>{x.sku} · {x.descripcion}</td><td className="text-end">{x.cantidad}</td><td className="text-end">{soles.format(x.importe)}</td></tr>)}
                        </tbody>
                    </table>
                </div>
            </div>
        </>
    )
}
