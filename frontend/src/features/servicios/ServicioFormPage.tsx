import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { hoyIso } from '../../utils/formato'
import { listarClientes, nombreCompleto, type Cliente } from '../clientes/clientesApi'
import { listarMercaderias, type Mercaderia } from '../mercaderia/mercaderiaApi'
import { listarTecnicos, registrarServicio, type Tecnico } from './serviciosApi'

const VACIO = { idCliente: '', idScooter: '', numeroSerie: '', fallaReportada: '', idTecnico: '', fechaCompra: '', mesesGarantia: '' }
type Datos = typeof VACIO

/* RF10: recepción de un scooter en el taller */
export default function ServicioFormPage() {
    const navigate = useNavigate()
    const [datos, setDatos] = useState<Datos>(VACIO)
    const [clientes, setClientes] = useState<Cliente[]>([])
    const [scooters, setScooters] = useState<Mercaderia[]>([])
    const [tecnicos, setTecnicos] = useState<Tecnico[]>([])
    const [compradoFuera, setCompradoFuera] = useState(false)
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [cargando, setCargando] = useState(true)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    useEffect(() => {
        let vigente = true
        Promise.all([listarClientes(), listarMercaderias(), listarTecnicos()])
            .then(([cls, mercs, tecs]) => {
                if (!vigente) return
                setClientes(cls)
                setScooters(mercs.filter(m => m.tipo === 'SCOOTER'))
                setTecnicos(tecs)
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    function cambiar(campo: keyof Datos, valor: string) {
        setDatos(prev => ({ ...prev, [campo]: valor }))
    }

    const numero = (t: string) => (t.trim() === '' ? null : Number(t))

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)
        try {
            const servicio = await registrarServicio({
                idCliente: numero(datos.idCliente),
                idScooter: numero(datos.idScooter),
                numeroSerie: datos.numeroSerie.trim(),
                fallaReportada: datos.fallaReportada.trim(),
                idTecnico: numero(datos.idTecnico),
                fechaCompra: compradoFuera && datos.fechaCompra ? datos.fechaCompra : null,
                mesesGarantia: compradoFuera ? numero(datos.mesesGarantia) : null,
            })
            navigate(`/servicios/${servicio.idServicio}`)
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
    const feedback = (campo: string) => errores[campo] && <div className="invalid-feedback">{errores[campo]}</div>

    if (cargando) return <p className="text-muted my-4">Cargando...</p>
    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/servicios" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    return (
        <div className="col-lg-9">
            <h2 className="text-primary my-4">Recibir scooter en taller</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate className="row g-3">
                <div className="col-md-6">
                    <label htmlFor="idCliente" className="form-label">Cliente *</label>
                    <div className="input-group">
                        <select id="idCliente" className={clase('idCliente', 'form-select')} value={datos.idCliente}
                            onChange={e => cambiar('idCliente', e.target.value)}>
                            <option value="">Seleccione...</option>
                            {clientes.map(c => <option key={c.idCliente} value={c.idCliente}>{c.numeroDocumento} · {nombreCompleto(c)}</option>)}
                        </select>
                        <Link to="/clientes/nuevo" className="btn btn-outline-secondary">+ Cliente</Link>
                        {feedback('idCliente')}
                    </div>
                </div>
                <div className="col-md-6">
                    <label htmlFor="idScooter" className="form-label">Modelo de scooter *</label>
                    <select id="idScooter" className={clase('idScooter', 'form-select')} value={datos.idScooter}
                        onChange={e => cambiar('idScooter', e.target.value)}>
                        <option value="">Seleccione...</option>
                        {scooters.map(s => <option key={s.idMercaderia} value={s.idMercaderia}>{s.sku} · {s.descripcion}</option>)}
                    </select>
                    {feedback('idScooter')}
                </div>
                <div className="col-md-6">
                    <label htmlFor="numeroSerie" className="form-label">Número de serie *</label>
                    <input id="numeroSerie" className={clase('numeroSerie')} value={datos.numeroSerie} maxLength={50}
                        onChange={e => cambiar('numeroSerie', e.target.value)} />
                    {feedback('numeroSerie')}
                    <div className="form-text">Si el scooter se vendió en la tienda, la garantía se calcula sola desde la venta.</div>
                </div>
                <div className="col-md-6">
                    <label htmlFor="idTecnico" className="form-label">Técnico asignado</label>
                    <select id="idTecnico" className="form-select" value={datos.idTecnico} onChange={e => cambiar('idTecnico', e.target.value)}>
                        <option value="">Sin asignar por ahora</option>
                        {tecnicos.map(t => <option key={t.idUsuario} value={t.idUsuario}>{t.nombre}</option>)}
                    </select>
                </div>
                <div className="col-12">
                    <label htmlFor="fallaReportada" className="form-label">Falla reportada por el cliente *</label>
                    <textarea id="fallaReportada" rows={3} className={clase('fallaReportada')} value={datos.fallaReportada} maxLength={2000}
                        onChange={e => cambiar('fallaReportada', e.target.value)} />
                    {feedback('fallaReportada')}
                </div>

                <div className="col-12">
                    <div className="form-check">
                        <input id="compradoFuera" type="checkbox" className="form-check-input" checked={compradoFuera}
                            onChange={e => setCompradoFuera(e.target.checked)} />
                        <label htmlFor="compradoFuera" className="form-check-label">El scooter NO se compró en la tienda (registrar fecha de compra manual)</label>
                    </div>
                </div>
                {compradoFuera && (
                    <>
                        <div className="col-md-4">
                            <label htmlFor="fechaCompra" className="form-label">Fecha de compra</label>
                            <input id="fechaCompra" type="date" className={clase('fechaCompra')} max={hoyIso()} value={datos.fechaCompra}
                                onChange={e => cambiar('fechaCompra', e.target.value)} />
                            {feedback('fechaCompra')}
                        </div>
                        <div className="col-md-4">
                            <label htmlFor="mesesGarantia" className="form-label">Meses de garantía</label>
                            <input id="mesesGarantia" type="number" min={0} className={clase('mesesGarantia')} value={datos.mesesGarantia}
                                placeholder="Por defecto, la del modelo" onChange={e => cambiar('mesesGarantia', e.target.value)} />
                            {feedback('mesesGarantia')}
                        </div>
                    </>
                )}

                <div className="col-12">
                    <button type="submit" className="btn btn-primary" disabled={guardando}>{guardando ? 'Registrando...' : 'Registrar orden de servicio'}</button>
                    <Link to="/servicios" className="btn btn-outline-secondary ms-2">Cancelar</Link>
                </div>
            </form>
        </div>
    )
}
