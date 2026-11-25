import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import {
    ETIQUETA_MOVIMIENTO, ETIQUETA_TIPO, obtenerKardex, obtenerMercaderia,
    type Mercaderia, type Movimiento,
} from './mercaderiaApi'

const fecha = (iso: string) => new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' })

export default function KardexPage() {
    const { id } = useParams()
    const [mercaderia, setMercaderia] = useState<Mercaderia | null>(null)
    const [movimientos, setMovimientos] = useState<Movimiento[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        Promise.all([obtenerMercaderia(Number(id)), obtenerKardex(Number(id))])
            .then(([m, movs]) => {
                if (!vigente) return
                setMercaderia(m)
                setMovimientos(movs)
            })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [id])

    if (cargando) return <p className="text-muted my-4">Cargando kardex...</p>

    if (error || !mercaderia) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{error ?? 'No se encontró la mercadería.'}</div>
                <Link to="/mercaderia" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    /* Estado derivado: totales y verificación del cuadre */
    const totalEntradas = movimientos.filter(m => m.entrada).reduce((s, m) => s + m.cantidad, 0)
    const totalSalidas = movimientos.filter(m => !m.entrada).reduce((s, m) => s + m.cantidad, 0)
    const saldoKardex = movimientos.at(-1)?.stockResultante ?? 0
    const cuadra = saldoKardex === mercaderia.stock

    return (
        <>
            <div className="d-flex justify-content-between align-items-start my-4">
                <div>
                    <h1 className="h3 text-primary mb-1">Kardex</h1>
                    <div>
                        <span className="badge text-bg-secondary me-2">{ETIQUETA_TIPO[mercaderia.tipo]}</span>
                        <strong>{mercaderia.sku}</strong> · {mercaderia.descripcion}
                    </div>
                    <div className="small text-muted">Proveedor: {mercaderia.proveedor} · Stock mínimo: {mercaderia.stockMinimo}</div>
                </div>
                <Link to="/mercaderia" className="btn btn-outline-secondary">Volver</Link>
            </div>

            <div className={`alert ${cuadra ? 'alert-success' : 'alert-danger'}`}>
                {cuadra
                    ? `El kardex cuadra con el stock actual: ${mercaderia.stock} unidades.`
                    : `¡Descuadre! El kardex suma ${saldoKardex}, pero el stock registrado es ${mercaderia.stock}.`}
            </div>

            <table className="table table-striped align-middle">
                <thead className="table-light">
                    <tr>
                        <th>Fecha</th>
                        <th>Movimiento</th>
                        <th>Origen</th>
                        <th>Usuario</th>
                        <th className="text-end">Entrada</th>
                        <th className="text-end">Salida</th>
                        <th className="text-end">Saldo</th>
                    </tr>
                </thead>
                <tbody>
                    {movimientos.length === 0 && (
                        <tr><td colSpan={7} className="text-center text-muted">Este producto aún no tiene movimientos.</td></tr>
                    )}
                    {movimientos.map(m => (
                        <tr key={m.idMovimiento}>
                            <td className="text-nowrap">{fecha(m.fechaHora)}</td>
                            <td>
                                <span className={`badge ${m.entrada ? 'text-bg-success' : 'text-bg-danger'}`}>
                                    {ETIQUETA_MOVIMIENTO[m.tipo]}
                                </span>
                                {m.observacion && <div className="small text-muted">{m.observacion}</div>}
                            </td>
                            <td>{m.origen}</td>
                            <td>{m.usuario}</td>
                            <td className="text-end text-success">{m.entrada ? `+${m.cantidad}` : ''}</td>
                            <td className="text-end text-danger">{!m.entrada ? `−${m.cantidad}` : ''}</td>
                            <td className="text-end fw-bold">{m.stockResultante}</td>
                        </tr>
                    ))}
                </tbody>
                {movimientos.length > 0 && (
                    <tfoot>
                        <tr className="table-light">
                            <td colSpan={4} className="text-end"><strong>Totales</strong></td>
                            <td className="text-end text-success"><strong>+{totalEntradas}</strong></td>
                            <td className="text-end text-danger"><strong>{totalSalidas > 0 ? `−${totalSalidas}` : 0}</strong></td>
                            <td className="text-end"><strong>{saldoKardex}</strong></td>
                        </tr>
                    </tfoot>
                )}
            </table>
        </>
    )
}