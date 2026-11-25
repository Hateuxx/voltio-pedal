import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { soles } from '../../utils/formato'
import { listarClientes, nombreCompleto, type Cliente } from '../clientes/clientesApi'
import { listarMercaderias, type Mercaderia } from '../mercaderia/mercaderiaApi'
import { registrarVenta, type TipoComprobante } from './ventasApi'

interface LineaForm {
    clave: string
    idMercaderia: string
    cantidad: string
    numeroSerie: string
}

function lineaVacia(): LineaForm {
    return { clave: crypto.randomUUID(), idMercaderia: '', cantidad: '1', numeroSerie: '' }
}

export default function VentaFormPage() {
    const navigate = useNavigate()
    const [clientes, setClientes] = useState<Cliente[]>([])
    const [mercaderias, setMercaderias] = useState<Mercaderia[]>([])
    const [tipo, setTipo] = useState<TipoComprobante>('BOLETA')
    const [idCliente, setIdCliente] = useState('')
    const [lineas, setLineas] = useState<LineaForm[]>(() => [lineaVacia()])
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    useEffect(() => {
        let vigente = true
        Promise.all([listarClientes(), listarMercaderias()])
            .then(([cls, mercs]) => {
                if (!vigente) return
                setClientes(cls)
                setMercaderias(mercs)
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    /* Estado derivado */
    const clientesValidos = tipo === 'FACTURA' ? clientes.filter(c => c.tipoDocumento === 'RUC') : clientes
    const mercaderiaDe = (id: string) => mercaderias.find(m => String(m.idMercaderia) === id)
    const subtotalCentimos = (l: LineaForm) => {
        const m = mercaderiaDe(l.idMercaderia)
        const cantidad = Number(l.cantidad)
        return m && Number.isInteger(cantidad) && cantidad > 0 ? Math.round(m.precioVenta * 100) * cantidad : 0
    }
    const totalCentimos = lineas.reduce((s, l) => s + subtotalCentimos(l), 0)

    function cambiarTipo(valor: TipoComprobante) {
        setTipo(valor)
        /* Una factura exige cliente con RUC: si el elegido no lo tiene, se limpia */
        if (valor === 'FACTURA' && idCliente !== '' && clientes.find(c => String(c.idCliente) === idCliente)?.tipoDocumento !== 'RUC') {
            setIdCliente('')
        }
    }

    function cambiarLinea(clave: string, campo: 'idMercaderia' | 'cantidad' | 'numeroSerie', valor: string) {
        setLineas(prev => prev.map(l => {
            if (l.clave !== clave) return l
            const nueva = { ...l, [campo]: valor }
            if (campo === 'idMercaderia' && mercaderiaDe(valor)?.tipo === 'SCOOTER') nueva.cantidad = '1'
            if (campo === 'idMercaderia' && mercaderiaDe(valor)?.tipo !== 'SCOOTER') nueva.numeroSerie = ''
            return nueva
        }))
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)
        try {
            const venta = await registrarVenta({
                tipoComprobante: tipo,
                idCliente: idCliente === '' ? null : Number(idCliente),
                detalles: lineas.map(l => ({
                    idMercaderia: l.idMercaderia === '' ? null : Number(l.idMercaderia),
                    cantidad: l.cantidad.trim() === '' ? null : Number(l.cantidad),
                    numeroSerie: l.numeroSerie.trim() || null,
                })),
            })
            navigate('/ventas', { state: { mensaje: `Venta ${venta.numeroComprobante} registrada por ${soles.format(venta.total)}.` } })
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

    const errorLinea = (i: number, campo: string) => errores[`detalles[${i}].${campo}`]

    if (cargando) return <p className="text-muted my-4">Cargando...</p>
    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/ventas" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    return (
        <>
            <h2 className="text-primary my-4">Nueva venta</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate>
                <div className="row g-3 mb-4">
                    <div className="col-md-3">
                        <label htmlFor="tipo" className="form-label">Comprobante *</label>
                        <select id="tipo" className="form-select" value={tipo} onChange={e => cambiarTipo(e.target.value as TipoComprobante)}>
                            <option value="BOLETA">Boleta</option>
                            <option value="FACTURA">Factura</option>
                        </select>
                    </div>
                    <div className="col-md-9">
                        <label htmlFor="cliente" className="form-label">Cliente {tipo === 'FACTURA' ? '* (con RUC)' : '(opcional)'}</label>
                        <div className="input-group">
                            <select id="cliente" className="form-select" value={idCliente} onChange={e => setIdCliente(e.target.value)}>
                                <option value="">{tipo === 'FACTURA' ? 'Seleccione una empresa...' : '— Venta al paso —'}</option>
                                {clientesValidos.map(c => (
                                    <option key={c.idCliente} value={c.idCliente}>{c.tipoDocumento} {c.numeroDocumento} · {nombreCompleto(c)}</option>
                                ))}
                            </select>
                            <Link to="/clientes/nuevo" className="btn btn-outline-secondary">+ Cliente</Link>
                        </div>
                    </div>
                </div>

                <h5>Productos</h5>
                {errores.detalles && <div className="text-danger small mb-2">{errores.detalles}</div>}
                <table className="table align-middle">
                    <thead className="table-light">
                        <tr>
                            <th style={{ width: '38%' }}>Mercadería</th>
                            <th className="text-center">Stock</th>
                            <th style={{ width: '10%' }}>Cant.</th>
                            <th style={{ width: '18%' }}>N.º de serie</th>
                            <th className="text-end">P. unit.</th>
                            <th className="text-end">Subtotal</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {lineas.map((l, i) => {
                            const actual = mercaderiaDe(l.idMercaderia)
                            const esScooter = actual?.tipo === 'SCOOTER'
                            /* Los no-scooter no se repiten; los scooters sí (una línea por número de serie) */
                            const usadosEnOtras = new Set(lineas.filter(x => x.clave !== l.clave).map(x => x.idMercaderia))
                            const opciones = mercaderias.filter(m =>
                                String(m.idMercaderia) === l.idMercaderia
                                || (m.stock > 0 && (m.tipo === 'SCOOTER' || !usadosEnOtras.has(String(m.idMercaderia)))))
                            return (
                                <tr key={l.clave}>
                                    <td>
                                        <select className={`form-select ${errorLinea(i, 'idMercaderia') ? 'is-invalid' : ''}`}
                                            value={l.idMercaderia} onChange={e => cambiarLinea(l.clave, 'idMercaderia', e.target.value)}>
                                            <option value="">Seleccione...</option>
                                            {opciones.map(m => (
                                                <option key={m.idMercaderia} value={m.idMercaderia}>{m.sku} · {m.descripcion}</option>
                                            ))}
                                        </select>
                                        {errorLinea(i, 'idMercaderia') && <div className="invalid-feedback">{errorLinea(i, 'idMercaderia')}</div>}
                                    </td>
                                    <td className="text-center">{actual ? actual.stock : '—'}</td>
                                    <td>
                                        <input type="number" min={1} step={1} disabled={esScooter}
                                            className={`form-control ${errorLinea(i, 'cantidad') ? 'is-invalid' : ''}`}
                                            value={l.cantidad} onChange={e => cambiarLinea(l.clave, 'cantidad', e.target.value)} />
                                        {errorLinea(i, 'cantidad') && <div className="invalid-feedback">{errorLinea(i, 'cantidad')}</div>}
                                    </td>
                                    <td>
                                        {esScooter
                                            ? <input className="form-control" placeholder="Serie (garantía)" maxLength={50}
                                                value={l.numeroSerie} onChange={e => cambiarLinea(l.clave, 'numeroSerie', e.target.value)} />
                                            : <span className="text-muted small">No aplica</span>}
                                    </td>
                                    <td className="text-end text-nowrap">{actual ? soles.format(actual.precioVenta) : '—'}</td>
                                    <td className="text-end text-nowrap">{soles.format(subtotalCentimos(l) / 100)}</td>
                                    <td className="text-end">
                                        <button type="button" className="btn btn-sm btn-outline-danger" disabled={lineas.length === 1}
                                            onClick={() => setLineas(prev => prev.filter(x => x.clave !== l.clave))} title="Quitar">✕</button>
                                    </td>
                                </tr>
                            )
                        })}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colSpan={5} className="text-end"><strong>Total</strong></td>
                            <td className="text-end text-nowrap"><strong className="fs-5">{soles.format(totalCentimos / 100)}</strong></td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>
                <p className="small text-muted">El precio lo fija el sistema según el catálogo; el total oficial lo calcula el servidor.</p>

                <button type="button" className="btn btn-outline-primary mb-4" onClick={() => setLineas(prev => [...prev, lineaVacia()])}>
                    + Agregar producto
                </button>

                <div>
                    <button type="submit" className="btn btn-primary" disabled={guardando}>{guardando ? 'Registrando...' : 'Registrar venta'}</button>
                    <Link to="/ventas" className="btn btn-outline-secondary ms-2">Cancelar</Link>
                </div>
            </form>
        </>
    )
}
