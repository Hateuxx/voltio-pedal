import { useState, type FormEvent } from "react";
import { Link, useNavigate } from 'react-router-dom'
import { ErrorApi } from "../../api/cliente";
import { crearProveedor, type ProveedorRequest } from "./proveedoresApi";

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
    const navigate = useNavigate()
    const [datos, setDatos] = useState(VACIO)
    const [errores, setErrores] = useState<Record<string, string>>({})
    const [error, setError] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    function cambiar(campo: keyof typeof VACIO, valor: string) {
        setDatos(prev => ({ ...prev, [campo]: valor }))
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setError(null)
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
            await crearProveedor(enviar)
            navigate('/proveedores')
        } catch (err) {
            if (err instanceof ErrorApi) {
                setErrores(err.problema.errores ?? {})
                setError(err.problema.detail)
            } else {
                setError('No se pudo conectar con el servidor.')
            }
        } finally {
            setGuardando(false)
        }
    }

    return (
        <>
            <h2 className="text-primary my-4">Nuevo proveedor</h2>
            {error && <div className="alert alert-danger">{error}</div>}

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