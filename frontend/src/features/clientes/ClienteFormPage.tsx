import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { actualizarCliente, crearCliente, obtenerCliente, type TipoDocumentoCliente } from './clientesApi'

const VACIO = { tipoDocumento: 'DNI', numeroDocumento: '', nombres: '', apellidos: '', telefono: '' }
type Datos = typeof VACIO

export default function ClienteFormPage() {
    const { id } = useParams()
    const esEdicion = id !== undefined
    const navigate = useNavigate()
    const [datos, setDatos] = useState<Datos>(VACIO)
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [cargando, setCargando] = useState(esEdicion)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    useEffect(() => {
        if (!esEdicion) return
        let vigente = true
        obtenerCliente(Number(id))
            .then(c => {
                if (vigente) setDatos({
                    tipoDocumento: c.tipoDocumento, numeroDocumento: c.numeroDocumento, nombres: c.nombres,
                    apellidos: c.apellidos ?? '', telefono: c.telefono ?? '',
                })
            })
            .catch(err => { if (vigente) setErrorCarga(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [esEdicion, id])

    const esRuc = datos.tipoDocumento === 'RUC'

    function cambiar(campo: keyof Datos, valor: string) {
        setDatos(prev => ({ ...prev, [campo]: valor }))
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)
        const enviar = {
            tipoDocumento: datos.tipoDocumento as TipoDocumentoCliente,
            numeroDocumento: datos.numeroDocumento.trim(),
            nombres: datos.nombres.trim(),
            apellidos: esRuc ? null : (datos.apellidos.trim() || null),
            telefono: datos.telefono.trim() || null,
        }
        try {
            if (esEdicion) await actualizarCliente(Number(id), enviar)
            else await crearCliente(enviar)
            navigate('/clientes')
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

    if (cargando) return <p className="text-muted my-4">Cargando cliente...</p>
    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/clientes" className="btn btn-outline-secondary">Volver</Link>
            </div>
        )
    }

    return (
        <div className="col-lg-8">
            <h2 className="text-primary my-4">{esEdicion ? 'Editar cliente' : 'Nuevo cliente'}</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate className="row g-3">
                <div className="col-md-4">
                    <label htmlFor="tipoDocumento" className="form-label">Tipo de documento *</label>
                    <select id="tipoDocumento" className={clase('tipoDocumento', 'form-select')} value={datos.tipoDocumento}
                        onChange={e => cambiar('tipoDocumento', e.target.value)}>
                        <option value="DNI">DNI</option>
                        <option value="CE">Carné de extranjería</option>
                        <option value="PASAPORTE">Pasaporte</option>
                        <option value="RUC">RUC (empresa)</option>
                    </select>
                </div>
                <div className="col-md-8">
                    <label htmlFor="numeroDocumento" className="form-label">Número de documento *</label>
                    <input id="numeroDocumento" className={clase('numeroDocumento')} value={datos.numeroDocumento}
                        maxLength={esRuc ? 11 : datos.tipoDocumento === 'DNI' ? 8 : 20}
                        onChange={e => cambiar('numeroDocumento', e.target.value)} />
                    {errores.numeroDocumento && <div className="invalid-feedback">{errores.numeroDocumento}</div>}
                </div>
                <div className={esRuc ? 'col-12' : 'col-md-6'}>
                    <label htmlFor="nombres" className="form-label">{esRuc ? 'Razón social *' : 'Nombres *'}</label>
                    <input id="nombres" className={clase('nombres')} value={datos.nombres} maxLength={100}
                        onChange={e => cambiar('nombres', e.target.value)} />
                    {errores.nombres && <div className="invalid-feedback">{errores.nombres}</div>}
                </div>
                {!esRuc && (
                    <div className="col-md-6">
                        <label htmlFor="apellidos" className="form-label">Apellidos *</label>
                        <input id="apellidos" className={clase('apellidos')} value={datos.apellidos} maxLength={100}
                            onChange={e => cambiar('apellidos', e.target.value)} />
                        {errores.apellidos && <div className="invalid-feedback">{errores.apellidos}</div>}
                    </div>
                )}
                <div className="col-md-6">
                    <label htmlFor="telefono" className="form-label">Teléfono</label>
                    <input id="telefono" className={clase('telefono')} value={datos.telefono} maxLength={20}
                        onChange={e => cambiar('telefono', e.target.value)} />
                    {errores.telefono && <div className="invalid-feedback">{errores.telefono}</div>}
                </div>
                <div className="col-12">
                    <button type="submit" className="btn btn-primary" disabled={guardando}>{guardando ? 'Guardando...' : 'Guardar'}</button>
                    <Link to="/clientes" className="btn btn-outline-secondary ms-2">Cancelar</Link>
                </div>
            </form>
        </div>
    )
}
