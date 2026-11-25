import { useEffect, useState, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { listarProveedores } from '../proveedores/proveedoresApi'
import { listarTiposRepuesto, type TipoRepuesto } from '../tiposRepuesto/tiposRepuestoApi'
import {
    actualizarMercaderia, crearMercaderia, obtenerMercaderia,
    type MercaderiaRequest, type TipoMercaderia,
} from './mercaderiaApi'

/* En el formulario TODO es string (los <input> trabajan con texto) */
const VACIO = {
    tipo: 'SCOOTER', sku: '', descripcion: '', stockMinimo: '0', precioCosto: '', precioVenta: '', idProveedor: '',
    autonomiaKm: '', potenciaWatts: '', color: '', mesesGarantia: '',
    idTipoRepuesto: '', modelosCompatibles: '', talla: '',
}
type Datos = typeof VACIO

interface Opcion { id: number; nombre: string }

const numeroONull = (texto: string) => (texto.trim() === '' ? null : Number(texto))
const textoONull = (texto: string) => texto.trim() || null
const aTexto = (valor: number | string | null) => (valor === null ? '' : String(valor))

/* Envoltorio de cada campo: etiqueta + control + mensaje de error */
function Campo({ id, label, error, ancho = 'col-md-6', children }:
    { id: string; label: string; error?: string; ancho?: string; children: ReactNode }) {
    return (
        <div className={ancho}>
            <label htmlFor={id} className="form-label">{label}</label>
            {children}
            {error && <div className="invalid-feedback d-block">{error}</div>}
        </div>
    )
}

export default function MercaderiaFormPage() {
    const { id } = useParams()
    const esEdicion = id !== undefined
    const navigate = useNavigate()

    const [datos, setDatos] = useState<Datos>(VACIO)
    const [stockActual, setStockActual] = useState<number | null>(null)
    const [proveedores, setProveedores] = useState<Opcion[]>([])
    const [tiposRepuesto, setTiposRepuesto] = useState<TipoRepuesto[]>([])
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    /* Carga en PARALELO: proveedores, tipos de repuesto y (si edita) la mercadería */
    useEffect(() => {
        let vigente = true
        Promise.all([
            listarProveedores(),
            listarTiposRepuesto(),
            esEdicion ? obtenerMercaderia(Number(id)) : Promise.resolve(null),
        ])
            .then(([provs, tipos, m]) => {
                if (!vigente) return
                const opciones = provs.map(p => ({ id: p.idProveedor, nombre: p.razonSocial }))
                /* Si su proveedor está dado de baja, igual debe aparecer en el select */
                if (m && !opciones.some(o => o.id === m.idProveedor)) {
                    opciones.push({ id: m.idProveedor, nombre: `${m.proveedor} (dado de baja)` })
                }
                setProveedores(opciones)
                setTiposRepuesto(tipos)
                if (m) {
                    setStockActual(m.stock)
                    setDatos({
                        tipo: m.tipo, sku: m.sku, descripcion: m.descripcion,
                        stockMinimo: aTexto(m.stockMinimo), precioCosto: aTexto(m.precioCosto),
                        precioVenta: aTexto(m.precioVenta), idProveedor: aTexto(m.idProveedor),
                        autonomiaKm: aTexto(m.autonomiaKm), potenciaWatts: aTexto(m.potenciaWatts),
                        color: aTexto(m.color), mesesGarantia: aTexto(m.mesesGarantia),
                        idTipoRepuesto: aTexto(m.idTipoRepuesto), modelosCompatibles: aTexto(m.modelosCompatibles),
                        talla: aTexto(m.talla),
                    })
                }
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [esEdicion, id])

    function cambiar(campo: keyof Datos, valor: string) {
        setDatos(prev => ({ ...prev, [campo]: valor }))
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)

        const t = datos.tipo
        /* Solo se envían los campos del tipo elegido; los demás van en null */
        const enviar: MercaderiaRequest = {
            tipo: t as TipoMercaderia,
            sku: datos.sku.trim(),
            descripcion: datos.descripcion.trim(),
            stockMinimo: numeroONull(datos.stockMinimo),
            precioCosto: numeroONull(datos.precioCosto),
            precioVenta: numeroONull(datos.precioVenta),
            idProveedor: numeroONull(datos.idProveedor),
            autonomiaKm: t === 'SCOOTER' ? numeroONull(datos.autonomiaKm) : null,
            potenciaWatts: t === 'SCOOTER' ? numeroONull(datos.potenciaWatts) : null,
            mesesGarantia: t === 'SCOOTER' ? numeroONull(datos.mesesGarantia) : null,
            color: t !== 'REPUESTO' ? textoONull(datos.color) : null,
            idTipoRepuesto: t === 'REPUESTO' ? numeroONull(datos.idTipoRepuesto) : null,
            modelosCompatibles: t === 'REPUESTO' ? textoONull(datos.modelosCompatibles) : null,
            talla: t === 'ACCESORIO' ? textoONull(datos.talla) : null,
        }

        try {
            if (esEdicion) {
                await actualizarMercaderia(Number(id), enviar)
            } else {
                await crearMercaderia(enviar)
            }
            navigate('/mercaderia')
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

    const clase = (campo: string, base = 'form-control') => `${base} ${errores[campo] ? 'is-invalid' : ''}`

    if (cargando) return <p className="text-muted my-4">Cargando...</p>

    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/mercaderia" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    return (
        <>
            <h2 className="text-primary my-4">{esEdicion ? 'Editar mercadería' : 'Nueva mercadería'}</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate>
                <h5>Datos generales</h5>
                <div className="row g-3 mb-4">
                    <Campo id="tipo" label="Tipo *" error={errores.tipo} ancho="col-md-4">
                        <select id="tipo" className={clase('tipo', 'form-select')} value={datos.tipo}
                            onChange={e => cambiar('tipo', e.target.value)} disabled={esEdicion}>
                            <option value="SCOOTER">Scooter</option>
                            <option value="REPUESTO">Repuesto</option>
                            <option value="ACCESORIO">Accesorio</option>
                        </select>
                        {esEdicion && <div className="form-text">El tipo no se puede cambiar.</div>}
                    </Campo>

                    <Campo id="sku" label="SKU *" error={errores.sku} ancho="col-md-4">
                        <input id="sku" className={clase('sku')} value={datos.sku} maxLength={50}
                            onChange={e => cambiar('sku', e.target.value)} placeholder="Ej.: SC-X1-NEGRO" />
                    </Campo>

                    <Campo id="idProveedor" label="Proveedor *" error={errores.idProveedor} ancho="col-md-4">
                        <select id="idProveedor" className={clase('idProveedor', 'form-select')} value={datos.idProveedor}
                            onChange={e => cambiar('idProveedor', e.target.value)}>
                            <option value="">Seleccione...</option>
                            {proveedores.map(p => <option key={p.id} value={p.id}>{p.nombre}</option>)}
                        </select>
                    </Campo>

                    <Campo id="descripcion" label="Descripción *" error={errores.descripcion} ancho="col-12">
                        <input id="descripcion" className={clase('descripcion')} value={datos.descripcion} maxLength={255}
                            onChange={e => cambiar('descripcion', e.target.value)} />
                    </Campo>

                    <Campo id="stockMinimo" label="Stock mínimo *" error={errores.stockMinimo} ancho="col-md-4">
                        <input id="stockMinimo" type="number" min={0} step={1} className={clase('stockMinimo')}
                            value={datos.stockMinimo} onChange={e => cambiar('stockMinimo', e.target.value)} />
                        {esEdicion && <div className="form-text">Stock actual: {stockActual} (solo cambia con ingresos y salidas).</div>}
                    </Campo>

                    <Campo id="precioCosto" label="Precio de costo (S/) *" error={errores.precioCosto} ancho="col-md-4">
                        <input id="precioCosto" type="number" min={0} step="0.0001" className={clase('precioCosto')}
                            value={datos.precioCosto} onChange={e => cambiar('precioCosto', e.target.value)} />
                    </Campo>

                    <Campo id="precioVenta" label="Precio de venta (S/) *" error={errores.precioVenta} ancho="col-md-4">
                        <input id="precioVenta" type="number" min={0} step="0.01" className={clase('precioVenta')}
                            value={datos.precioVenta} onChange={e => cambiar('precioVenta', e.target.value)} />
                    </Campo>
                </div>

                {datos.tipo === 'SCOOTER' && (
                    <>
                        <h5>Datos del scooter</h5>
                        <div className="row g-3 mb-4">
                            <Campo id="autonomiaKm" label="Autonomía (km) *" error={errores.autonomiaKm} ancho="col-md-3">
                                <input id="autonomiaKm" type="number" min={1} className={clase('autonomiaKm')}
                                    value={datos.autonomiaKm} onChange={e => cambiar('autonomiaKm', e.target.value)} />
                            </Campo>
                            <Campo id="potenciaWatts" label="Potencia (W) *" error={errores.potenciaWatts} ancho="col-md-3">
                                <input id="potenciaWatts" type="number" min={1} className={clase('potenciaWatts')}
                                    value={datos.potenciaWatts} onChange={e => cambiar('potenciaWatts', e.target.value)} />
                            </Campo>
                            <Campo id="color" label="Color *" error={errores.color} ancho="col-md-3">
                                <input id="color" className={clase('color')} value={datos.color} maxLength={50}
                                    onChange={e => cambiar('color', e.target.value)} />
                            </Campo>
                            <Campo id="mesesGarantia" label="Garantía (meses) *" error={errores.mesesGarantia} ancho="col-md-3">
                                <input id="mesesGarantia" type="number" min={0} className={clase('mesesGarantia')}
                                    value={datos.mesesGarantia} onChange={e => cambiar('mesesGarantia', e.target.value)} />
                            </Campo>
                        </div>
                    </>
                )}

                {datos.tipo === 'REPUESTO' && (
                    <>
                        <h5>Datos del repuesto</h5>
                        <div className="row g-3 mb-4">
                            <Campo id="idTipoRepuesto" label="Tipo de repuesto *" error={errores.idTipoRepuesto} ancho="col-md-4">
                                <select id="idTipoRepuesto" className={clase('idTipoRepuesto', 'form-select')} value={datos.idTipoRepuesto}
                                    onChange={e => cambiar('idTipoRepuesto', e.target.value)}>
                                    <option value="">Seleccione...</option>
                                    {tiposRepuesto.map(t => <option key={t.idTipoRepuesto} value={t.idTipoRepuesto}>{t.nombre}</option>)}
                                </select>
                            </Campo>
                            <Campo id="modelosCompatibles" label="Modelos compatibles *" error={errores.modelosCompatibles} ancho="col-md-8">
                                <input id="modelosCompatibles" className={clase('modelosCompatibles')} value={datos.modelosCompatibles}
                                    maxLength={255} placeholder="Ej.: X1, X2, Pro 500"
                                    onChange={e => cambiar('modelosCompatibles', e.target.value)} />
                            </Campo>
                        </div>
                    </>
                )}

                {datos.tipo === 'ACCESORIO' && (
                    <>
                        <h5>Datos del accesorio</h5>
                        <div className="row g-3 mb-4">
                            <Campo id="talla" label="Talla" error={errores.talla} ancho="col-md-4">
                                <input id="talla" className={clase('talla')} value={datos.talla} maxLength={20}
                                    onChange={e => cambiar('talla', e.target.value)} />
                            </Campo>
                            <Campo id="color" label="Color" error={errores.color} ancho="col-md-4">
                                <input id="color" className={clase('color')} value={datos.color} maxLength={50}
                                    onChange={e => cambiar('color', e.target.value)} />
                            </Campo>
                        </div>
                    </>
                )}

                <button type="submit" className="btn btn-primary" disabled={guardando}>
                    {guardando ? 'Guardando...' : 'Guardar'}
                </button>
                <Link to="/mercaderia" className="btn btn-outline-secondary ms-2">Cancelar</Link>
            </form>
        </>
    )
}