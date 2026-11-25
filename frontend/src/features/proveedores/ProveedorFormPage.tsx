import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ErrorApi } from "../../api/cliente";
import { actualizarProveedor, crearProveedor, obtenerProveedor, type ProveedorRequest } from "./proveedoresApi";

/* En el formulario TODO es string: un <input> no acepta null */
const VACIO = {
    ruc: '',
    razonSocial: '',
    contacto: '',
    telefono: '',
    rubro: '',
    frecuenciaEntrega: 'SEMANAL',
}

export default function ProveedorFormPage() {
    const { id } = useParams()
    const esEdicion = id !== undefined
    const navigate = useNavigate()
    const [datos, setDatos] = useState(VACIO)
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [guardando, setGuardando] = useState(false)
    const [cargando, setCargando] = useState(esEdicion)
    const [errorCarga, setErrorCarga] = useState<string | null>(null)
    const [errorGuardar, setErrorGuardar] = useState<string | null>(null)

    function cambiar(campo: keyof typeof VACIO, valor: string) {
        setDatos(prev => ({ ...prev, [campo]: valor }))
    }

    useEffect(() => {
        if (!esEdicion) return
        obtenerProveedor(Number(id))
            .then(p => setDatos({
                ruc: p.ruc,
                razonSocial: p.razonSocial,
                contacto: p.contacto ?? '',
                telefono: p.telefono ?? '',
                rubro: p.rubro ?? '',
                frecuenciaEntrega: p.frecuenciaEntrega ?? 'SEMANAL',
            }))
            .catch(err => setErrorCarga(err instanceof ErrorApi ? err.problema.detail : 'No se pudo conectar con el servidor.'))
            .finally(() => setCargando(false))
    }, [esEdicion, id])

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorGuardar(null)
        setErrores({})
        setGuardando(true)

        try {
            const enviar: ProveedorRequest = {
                ruc: datos.ruc.trim(),
                razonSocial: datos.razonSocial.trim(),
                contacto: datos.contacto.trim() || null,
                telefono: datos.telefono.trim() || null,
                rubro: datos.rubro.trim() || null,
                frecuenciaEntrega: datos.frecuenciaEntrega as ProveedorRequest['frecuenciaEntrega'],
            }
            if (esEdicion) {
                await actualizarProveedor(Number(id), enviar)
            } else {
                await crearProveedor(enviar)
            }
            navigate('/proveedores')
        } catch (err) {
            if (err instanceof ErrorApi) {
                setErrores(err.problema.errores ?? {})
                setErrorGuardar(err.problema.detail)
            } else {
                setErrorGuardar('No se pudo conectar con el servidor.')
            }
        } finally {
            setGuardando(false)
        }
    }

    /* Salida temprana si falló la carga */
    if (cargando) return <p className='text-muted my-4'>Cargando proveedor...</p>

    if (errorCarga) {
        return (
            <div className="my-4">
                <div className="alert alert-danger">{errorCarga}</div>
                <Link to="/proveedores" className="btn btn-outline-secondary">
                    Volver
                </Link>
            </div>
        )
    }

    return (
        <>
            <h2 className="text-primary my-4">{esEdicion ? 'Editar proveedor' : 'Nuevo proveedor'}</h2>
            {errorGuardar && <div className="alert alert-danger">{errorGuardar}</div>}

            <form onSubmit={guardar} noValidate>
                <div className="mb-3">
                    <label htmlFor="ruc" className="form-label">RUC *</label>
                    <input
                        id="ruc"
                        className={`form-control ${errores.ruc ? 'is-invalid' : ''}`}
                        value={datos.ruc}
                        onChange={e => cambiar('ruc', e.target.value)}
                        maxLength={11}
                    />
                    {errores.ruc && <div className="invalid-feedback">{errores.ruc}</div>}
                </div>

                <div className="mb-3">
                    <label htmlFor="razonSocial" className="form-label">Razón social *</label>
                    <input
                        id="razonSocial"
                        className={`form-control ${errores.razonSocial ? 'is-invalid' : ''}`}
                        value={datos.razonSocial}
                        onChange={e => cambiar('razonSocial', e.target.value)}
                        maxLength={255}
                    />
                    {errores.razonSocial && <div className="invalid-feedback">{errores.razonSocial}</div>}
                </div>

                <div className="mb-3">
                    <label htmlFor="contacto" className="form-label">Contacto</label>
                    <input
                        id="contacto"
                        className={`form-control ${errores.contacto ? 'is-invalid' : ''}`}
                        value={datos.contacto}
                        onChange={e => cambiar('contacto', e.target.value)}
                        maxLength={100}
                    />
                    {errores.contacto && <div className="invalid-feedback">{errores.contacto}</div>}
                </div>

                <div className="mb-3">
                    <label htmlFor="telefono" className="form-label">Teléfono</label>
                    <input
                        id="telefono"
                        className={`form-control ${errores.telefono ? 'is-invalid' : ''}`}
                        value={datos.telefono}
                        onChange={e => cambiar('telefono', e.target.value)}
                        maxLength={20}
                    />
                    {errores.telefono && <div className="invalid-feedback">{errores.telefono}</div>}
                </div>

                <div className="mb-3">
                    <label htmlFor="rubro" className="form-label">Rubro</label>
                    <input
                        id="rubro"
                        className={`form-control ${errores.rubro ? 'is-invalid' : ''}`}
                        value={datos.rubro}
                        onChange={e => cambiar('rubro', e.target.value)}
                        maxLength={100}
                    />
                    {errores.rubro && <div className="invalid-feedback">{errores.rubro}</div>}
                </div>

                <div className="mb-4">
                    <label htmlFor="frecuenciaEntrega" className="form-label">Frecuencia de entrega</label>
                    <select
                        id="frecuenciaEntrega"
                        className={`form-select ${errores.frecuenciaEntrega ? 'is-invalid' : ''}`}
                        value={datos.frecuenciaEntrega}
                        onChange={e => cambiar('frecuenciaEntrega', e.target.value)}
                    >
                        <option value="SEMANAL">Semanal</option>
                        <option value="QUINCENAL">Quincenal</option>
                    </select>
                    {errores.frecuenciaEntrega && (
                        <div className="invalid-feedback">{errores.frecuenciaEntrega}</div>
                    )}
                </div>

                <button type="submit" className="btn btn-primary" disabled={guardando}>
                    {guardando ? 'Guardando...' : 'Guardar'}
                </button>
                <Link to="/proveedores" className="btn btn-outline-secondary ms-2">
                    Cancelar
                </Link>
            </form>
        </>
    )
}