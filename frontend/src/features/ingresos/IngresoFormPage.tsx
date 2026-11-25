import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { listarProveedores, type Proveedor } from '../proveedores/proveedoresApi'
import { listarMercaderias, type Mercaderia } from '../mercaderia/mercaderiaApi'
import { registrarIngreso } from './ingresosApi'

interface LineaForm {
    clave: string            /* identificador estable para la key de React */
    idMercaderia: string
    cantidad: string
    costoUnitario: string
}

const soles4 = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', minimumFractionDigits: 2, maximumFractionDigits: 4 })

function lineaVacia(): LineaForm {
    return { clave: crypto.randomUUID(), idMercaderia: '', cantidad: '1', costoUnitario: '' }
}

/* Dinero en DIEZMILÉSIMOS enteros (costo con 4 decimales): evita el 0.1 + 0.2 = 0.30000000000000004 */
function subtotalEnteros(l: LineaForm) {
    const cantidad = Number(l.cantidad)
    const costo = Number(l.costoUnitario)
    if (!Number.isInteger(cantidad) || cantidad <= 0 || l.costoUnitario.trim() === '' || Number.isNaN(costo)) return 0
    return Math.round(costo * 10000) * cantidad
}

const nullSiVacio = (texto: string) => (texto.trim() === '' ? null : Number(texto))

export default function IngresoFormPage() {
    const navigate = useNavigate()
    const [proveedores, setProveedores] = useState<Proveedor[]>([])
    const [mercaderias, setMercaderias] = useState<Mercaderia[]>([])
    const [idProveedor, setIdProveedor] = useState('')
    const [numeroGuia, setNumeroGuia] = useState('')
    const [observacion, setObservacion] = useState('')
    const [lineas, setLineas] = useState<LineaForm[]>(() => [lineaVacia()])   /* inicialización perezosa */
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    useEffect(() => {
        let vigente = true
        Promise.all([listarProveedores(), listarMercaderias()])
            .then(([provs, mercs]) => {
                if (!vigente) return
                setProveedores(provs)
                setMercaderias(mercs)
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    /* Estado derivado */
    const productosDelProveedor = mercaderias.filter(m => String(m.idProveedor) === idProveedor)
    const totalEnteros = lineas.reduce((suma, l) => suma + subtotalEnteros(l), 0)

    function cambiarProveedor(valor: string) {
        const hayDatos = lineas.some(l => l.idMercaderia !== '')
        if (hayDatos && !window.confirm('Al cambiar de proveedor se borrarán los productos agregados. ¿Continuar?')) return
        setIdProveedor(valor)
        setLineas([lineaVacia()])
    }

    function cambiarLinea(clave: string, campo: 'idMercaderia' | 'cantidad' | 'costoUnitario', valor: string) {
        setLineas(prev => prev.map(l => {
            if (l.clave !== clave) return l
            const nueva = { ...l, [campo]: valor }
            /* Al elegir un producto, propone su precio de costo actual */
            if (campo === 'idMercaderia' && l.costoUnitario === '') {
                const m = mercaderias.find(x => String(x.idMercaderia) === valor)
                if (m) nueva.costoUnitario = String(m.precioCosto)
            }
            return nueva
        }))
    }

    function agregarLinea() {
        setLineas(prev => [...prev, lineaVacia()])
    }

    function quitarLinea(clave: string) {
        setLineas(prev => prev.filter(l => l.clave !== clave))
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)
        try {
            await registrarIngreso({
                idProveedor: nullSiVacio(idProveedor),
                numeroGuia: numeroGuia.trim(),
                observacion: observacion.trim() || null,
                detalles: lineas.map(l => ({
                    idMercaderia: nullSiVacio(l.idMercaderia),
                    cantidad: nullSiVacio(l.cantidad),
                    costoUnitario: nullSiVacio(l.costoUnitario),
                })),
            })
            navigate('/ingresos')
        } catch (err) {
            if (err instanceof ErrorApi) {
                setErrores(err.problema.errores ?? {})
                setErrorGuardar(err.problema.detail)
            } else {
                setErrorGuardar(mensajeDeError(err))
            }
        } finally {
            setGuardando(false)
        }
    }

    /* Los errores de cada línea llegan como "detalles[0].cantidad" */
    const errorLinea = (i: number, campo: string) => errores[`detalles[${i}].${campo}`]

    if (cargando) return <p className="text-muted my-4">Cargando...</p>

    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/ingresos" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    return (
        <>
            <h2 className="text-primary my-4">Nuevo ingreso de mercadería</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate>
                <div className="row g-3 mb-4">
                    <div className="col-md-4">
                        <label htmlFor="idProveedor" className="form-label">Proveedor *</label>
                        <select id="idProveedor" className={`form-select ${errores.idProveedor ? 'is-invalid' : ''}`}
                            value={idProveedor} onChange={e => cambiarProveedor(e.target.value)}>
                            <option value="">Seleccione...</option>
                            {proveedores.map(p => <option key={p.idProveedor} value={p.idProveedor}>{p.razonSocial}</option>)}
                        </select>
                        {errores.idProveedor && <div className="invalid-feedback">{errores.idProveedor}</div>}
                    </div>
                    <div className="col-md-4">
                        <label htmlFor="numeroGuia" className="form-label">N.º de guía de remisión *</label>
                        <input id="numeroGuia" className={`form-control ${errores.numeroGuia ? 'is-invalid' : ''}`}
                            value={numeroGuia} maxLength={20} placeholder="Ej.: T001-00004521"
                            onChange={e => setNumeroGuia(e.target.value)} />
                        {errores.numeroGuia && <div className="invalid-feedback">{errores.numeroGuia}</div>}
                    </div>
                    <div className="col-md-4">
                        <label htmlFor="observacion" className="form-label">Observación</label>
                        <input id="observacion" className="form-control" value={observacion} maxLength={255}
                            onChange={e => setObservacion(e.target.value)} />
                    </div>
                </div>

                <h5>Productos</h5>
                {idProveedor !== '' && productosDelProveedor.length === 0 && (
                    <div className="alert alert-warning">Este proveedor no tiene mercadería activa registrada.</div>
                )}
                {errores.detalles && <div className="text-danger small mb-2">{errores.detalles}</div>}

                <table className="table align-middle">
                    <thead className="table-light">
                        <tr>
                            <th style={{ width: '40%' }}>Mercadería</th>
                            <th className="text-center">Stock actual</th>
                            <th style={{ width: '12%' }}>Cantidad</th>
                            <th style={{ width: '16%' }}>Costo unit. (S/)</th>
                            <th className="text-end">Subtotal</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {lineas.map((l, i) => {
                            /* No ofrecer productos ya elegidos en OTRAS líneas */
                            const elegidosEnOtras = new Set(lineas.filter(x => x.clave !== l.clave).map(x => x.idMercaderia))
                            const opciones = productosDelProveedor.filter(m => !elegidosEnOtras.has(String(m.idMercaderia)))
                            const actual = mercaderias.find(m => String(m.idMercaderia) === l.idMercaderia)
                            return (
                                <tr key={l.clave}>
                                    <td>
                                        <select className={`form-select ${errorLinea(i, 'idMercaderia') ? 'is-invalid' : ''}`}
                                            value={l.idMercaderia} disabled={idProveedor === ''}
                                            onChange={e => cambiarLinea(l.clave, 'idMercaderia', e.target.value)}>
                                            <option value="">{idProveedor === '' ? 'Primero elija un proveedor' : 'Seleccione...'}</option>
                                            {opciones.map(m => (
                                                <option key={m.idMercaderia} value={m.idMercaderia}>{m.sku} · {m.descripcion}</option>
                                            ))}
                                        </select>
                                        {errorLinea(i, 'idMercaderia') && <div className="invalid-feedback">{errorLinea(i, 'idMercaderia')}</div>}
                                    </td>
                                    <td className="text-center">{actual ? actual.stock : '—'}</td>
                                    <td>
                                        <input type="number" min={1} step={1}
                                            className={`form-control ${errorLinea(i, 'cantidad') ? 'is-invalid' : ''}`}
                                            value={l.cantidad} onChange={e => cambiarLinea(l.clave, 'cantidad', e.target.value)} />
                                        {errorLinea(i, 'cantidad') && <div className="invalid-feedback">{errorLinea(i, 'cantidad')}</div>}
                                    </td>
                                    <td>
                                        <input type="number" min={0} step="0.0001"
                                            className={`form-control ${errorLinea(i, 'costoUnitario') ? 'is-invalid' : ''}`}
                                            value={l.costoUnitario} onChange={e => cambiarLinea(l.clave, 'costoUnitario', e.target.value)} />
                                        {errorLinea(i, 'costoUnitario') && <div className="invalid-feedback">{errorLinea(i, 'costoUnitario')}</div>}
                                    </td>
                                    <td className="text-end text-nowrap">{soles4.format(subtotalEnteros(l) / 10000)}</td>
                                    <td className="text-end">
                                        <button type="button" className="btn btn-sm btn-outline-danger"
                                            onClick={() => quitarLinea(l.clave)} disabled={lineas.length === 1}
                                            title="Quitar línea">✕</button>
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colSpan={4} className="text-end"><strong>Total estimado</strong></td>
                            <td className="text-end text-nowrap"><strong>{soles4.format(totalEnteros / 10000)}</strong></td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>

                <button type="button" className="btn btn-outline-primary mb-4" onClick={agregarLinea}
                    disabled={idProveedor === '' || lineas.length >= productosDelProveedor.length}>
                    + Agregar producto
                </button>

                <div>
                    <button type="submit" className="btn btn-primary" disabled={guardando}>
                        {guardando ? 'Registrando...' : 'Registrar ingreso'}
                    </button>
                    <Link to="/ingresos" className="btn btn-outline-secondary ms-2">Cancelar</Link>
                </div>
            </form>
        </>
    )
}