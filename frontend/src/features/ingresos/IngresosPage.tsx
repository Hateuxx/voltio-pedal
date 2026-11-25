import { Fragment, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import { listarIngresos, type Ingreso } from './ingresosApi'

const soles = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
const soles4 = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2, maximumFractionDigits: 4 })
const fecha = (iso: string) => new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' })

export default function IngresosPage() {
    const [ingresos, setIngresos] = useState<Ingreso[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [abierto, setAbierto] = useState<number | null>(null)   /* id del ingreso desplegado */

    useEffect(() => {
        let vigente = true
        listarIngresos()
            .then(lista => { if (vigente) setIngresos(lista) })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h1 className="h3 text-primary mb-0">Ingresos de mercadería</h1>
                {/* Los mismos roles que ven esta página pueden registrar (ADMIN y OPERADOR) */}
                <Link to="/ingresos/nuevo" className="btn btn-success">Nuevo ingreso</Link>
            </div>

            {cargando && <p className="text-muted">Cargando ingresos...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Fecha</th>
                            <th>Guía</th>
                            <th>Proveedor</th>
                            <th className="text-center">Productos</th>
                            <th className="text-end">Total</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {ingresos.length === 0 && (
                            <tr><td colSpan={6} className="text-center text-muted">No hay ingresos registrados.</td></tr>
                        )}
                        {ingresos.map(i => (
                            <Fragment key={i.idIngreso}>
                                <tr>
                                    <td className="text-nowrap">{fecha(i.fechaHora)}</td>
                                    <td className="text-nowrap">{i.numeroGuia}</td>
                                    <td>{i.razonSocialProveedor}</td>
                                    <td className="text-center">{i.detalles.length}</td>
                                    <td className="text-end text-nowrap">{soles.format(i.total)}</td>
                                    <td className="text-end">
                                        <button className="btn btn-sm btn-outline-primary"
                                            onClick={() => setAbierto(abierto === i.idIngreso ? null : i.idIngreso)}>
                                            {abierto === i.idIngreso ? 'Ocultar' : 'Ver detalle'}
                                        </button>
                                    </td>
                                </tr>
                                {abierto === i.idIngreso && (
                                    <tr>
                                        <td colSpan={6} className="bg-light">
                                            {i.observacion && <p className="mb-2"><strong>Observación:</strong> {i.observacion}</p>}
                                            <table className="table table-sm mb-0">
                                                <thead>
                                                    <tr>
                                                        <th>SKU</th>
                                                        <th>Descripción</th>
                                                        <th className="text-end">Cantidad</th>
                                                        <th className="text-end">Costo unit.</th>
                                                        <th className="text-end">Subtotal</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    {i.detalles.map(d => (
                                                        <tr key={d.idMercaderia}>
                                                            <td>{d.sku}</td>
                                                            <td>{d.descripcion}</td>
                                                            <td className="text-end">{d.cantidad}</td>
                                                            <td className="text-end">{soles4.format(d.costoUnitario)}</td>
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