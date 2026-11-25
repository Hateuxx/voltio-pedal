import { Fragment, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import DialogoMotivo, { type AccionConMotivo } from '../../components/DialogoMotivo'
import { fechaHora, hoyIso, soles } from '../../utils/formato'
import { obtenerUsuario } from '../auth/sesion'
import { anularVenta, listarVentas, type Venta } from './ventasApi'

export default function VentasPage() {
    const location = useLocation()
    const aviso = (location.state as { mensaje?: string } | null)?.mensaje
    const [ventas, setVentas] = useState<Venta[]>([])
    const [desde, setDesde] = useState(hoyIso(-30))
    const [hasta, setHasta] = useState(hoyIso())
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [abierta, setAbierta] = useState<number | null>(null)
    const [recargar, setRecargar] = useState(0)
    const [pendiente, setPendiente] = useState<AccionConMotivo | null>(null)
    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    useEffect(() => {
        let vigente = true
        listarVentas(desde, hasta)
            .then(lista => { if (vigente) { setVentas(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [desde, hasta, recargar])

    const activas = ventas.filter(v => v.activo)
    /* Suma en céntimos enteros para no arrastrar errores de punto flotante */
    const totalCentimos = activas.reduce((s, v) => s + Math.round(v.total * 100), 0)

    function anular(v: Venta) {
        setPendiente({
            titulo: `Anular ${v.numeroComprobante}`, textoBoton: 'Anular venta', peligro: true,
            mensaje: `Se anulará la venta por ${soles.format(v.total)} y las ${v.detalles.reduce((s, d) => s + d.cantidad, 0)} unidades volverán al stock (kardex: devolución de cliente).`,
            ejecutar: async motivo => { await anularVenta(v.idVenta, motivo); setRecargar(n => n + 1) },
        })
    }

    return (
        <>
            {pendiente && <DialogoMotivo accion={pendiente} onCerrar={() => setPendiente(null)} />}

            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 text-primary mb-0">Ventas</h1>
                <Link to="/ventas/nueva" className="btn btn-success">Nueva venta</Link>
            </div>

            {aviso && <div className="alert alert-success">{aviso}</div>}

            <div className="row g-2 mb-3 align-items-end">
                <div className="col-md-3">
                    <label className="form-label small mb-1">Desde</label>
                    <input type="date" className="form-control" value={desde} max={hasta}
                        onChange={e => { setCargando(true); setDesde(e.target.value) }} />
                </div>
                <div className="col-md-3">
                    <label className="form-label small mb-1">Hasta</label>
                    <input type="date" className="form-control" value={hasta} min={desde}
                        onChange={e => { setCargando(true); setHasta(e.target.value) }} />
                </div>
                <div className="col-md-6 text-md-end">
                    <span className="text-muted me-2">{activas.length} venta(s) válidas:</span>
                    <strong className="fs-5">{soles.format(totalCentimos / 100)}</strong>
                </div>
            </div>

            {cargando && <p className="text-muted">Cargando ventas...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Fecha</th>
                            <th>Comprobante</th>
                            <th>Cliente</th>
                            <th>Vendedor</th>
                            <th className="text-end">Total</th>
                            <th>Estado</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {ventas.length === 0 && (
                            <tr><td colSpan={7} className="text-center text-muted">No hay ventas en ese rango.</td></tr>
                        )}
                        {ventas.map(v => (
                            <Fragment key={v.idVenta}>
                                <tr className={v.activo ? '' : 'text-decoration-line-through text-muted'}>
                                    <td className="text-nowrap">{fechaHora(v.fechaHora)}</td>
                                    <td className="text-nowrap">
                                        <span className="badge text-bg-secondary me-1">{v.tipoComprobante}</span>{v.numeroComprobante}
                                    </td>
                                    <td>{v.cliente ?? <span className="text-muted">Venta al paso</span>}</td>
                                    <td>{v.vendedor}</td>
                                    <td className="text-end text-nowrap">{soles.format(v.total)}</td>
                                    <td>{v.activo ? <span className="badge text-bg-success">Válida</span> : <span className="badge text-bg-danger">Anulada</span>}</td>
                                    <td className="text-end text-nowrap">
                                        <button className="btn btn-sm btn-outline-primary"
                                            onClick={() => setAbierta(abierta === v.idVenta ? null : v.idVenta)}>
                                            {abierta === v.idVenta ? 'Ocultar' : 'Detalle'}
                                        </button>
                                        {esAdmin && v.activo && (
                                            <button className="btn btn-sm btn-outline-danger ms-2" onClick={() => anular(v)}>Anular</button>
                                        )}
                                    </td>
                                </tr>
                                {abierta === v.idVenta && (
                                    <tr>
                                        <td colSpan={7} className="bg-light">
                                            <table className="table table-sm mb-0">
                                                <thead>
                                                    <tr>
                                                        <th>SKU</th>
                                                        <th>Descripción</th>
                                                        <th>N.º de serie</th>
                                                        <th className="text-end">Cant.</th>
                                                        <th className="text-end">P. unit.</th>
                                                        <th className="text-end">Subtotal</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {v.detalles.map((d, i) => (
                                                        <tr key={i}>
                                                            <td>{d.sku}</td>
                                                            <td>{d.descripcion}</td>
                                                            <td>{d.numeroSerie ?? '—'}</td>
                                                            <td className="text-end">{d.cantidad}</td>
                                                            <td className="text-end">{soles.format(d.precioUnitario)}</td>
                                                            <td className="text-end">{soles.format(d.subtotal)}</td>
                                                        </tr>
                                                    ))}
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                )}
                            </Fragment>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}
