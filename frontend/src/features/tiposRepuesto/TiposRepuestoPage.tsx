import { useEffect, useState, type FormEvent } from 'react'
import { ErrorApi, mensajeDeError } from '../../api/cliente'
import { obtenerUsuario } from '../auth/sesion'
import DialogoMotivo, { type AccionConMotivo } from '../../components/DialogoMotivo'
import {
    actualizarTipoRepuesto, crearTipoRepuesto, eliminarTipoRepuesto,
    listarTiposRepuesto, type TipoRepuesto,
} from './tiposRepuestoApi'

export default function TiposRepuestoPage() {
    const [tipos, setTipos] = useState<TipoRepuesto[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)             // al cargar la lista
    const [errorAccion, setErrorAccion] = useState<string | null>(null) // al eliminar
    const [recargar, setRecargar] = useState(0)
    const [pendiente, setPendiente] = useState<AccionConMotivo | null>(null)

    /* Formulario: el mismo sirve para crear y editar */
    const [nombre, setNombre] = useState('')
    const [editando, setEditando] = useState<TipoRepuesto | null>(null)
    const [errorForm, setErrorForm] = useState<string | null>(null)
    const [guardando, setGuardando] = useState(false)

    const esAdmin = obtenerUsuario()?.rol === 'ADMINISTRADOR'

    useEffect(() => {
        let vigente = true
        listarTiposRepuesto()
            .then(lista => { if (vigente) { setTipos(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [recargar])

    function empezarEdicion(t: TipoRepuesto) {
        setEditando(t)
        setNombre(t.nombre)
        setErrorForm(null)
    }

    function limpiarFormulario() {
        setEditando(null)
        setNombre('')
        setErrorForm(null)
    }

    async function guardar(e: FormEvent) {
        e.preventDefault()
        setErrorForm(null)
        setGuardando(true)
        try {
            if (editando) {
                await actualizarTipoRepuesto(editando.idTipoRepuesto, nombre.trim())
            } else {
                await crearTipoRepuesto(nombre.trim())
            }
            limpiarFormulario()
            setRecargar(n => n + 1)
        } catch (err) {
            /* Si es un 400 de validación, muestra el mensaje del campo; si no, el general */
            setErrorForm(err instanceof ErrorApi
                ? (err.problema.errores?.nombre ?? err.problema.detail)
                : mensajeDeError(err))
        } finally {
            setGuardando(false)
        }
    }

    function eliminar(t: TipoRepuesto) {
        setErrorAccion(null)
        setPendiente({
            titulo: 'Eliminar tipo de repuesto', textoBoton: 'Eliminar', peligro: true,
            mensaje: `Se borrará el tipo "${t.nombre}". Esta acción NO se puede deshacer.`,
            ejecutar: async motivo => {
                await eliminarTipoRepuesto(t.idTipoRepuesto, motivo)
                if (editando?.idTipoRepuesto === t.idTipoRepuesto) limpiarFormulario()
                setRecargar(n => n + 1)
            },
        })
    }

    return (
        <div className="col-lg-8">
            {pendiente && <DialogoMotivo accion={pendiente} onCerrar={() => setPendiente(null)} />}
            <h1 className="h3 text-primary mb-4">Tipos de repuesto</h1>

            {esAdmin && (
                <form onSubmit={guardar} noValidate className="card card-body mb-4">
                    <label htmlFor="nombre" className="form-label">
                        {editando ? `Editando: ${editando.nombre}` : 'Nuevo tipo de repuesto'}
                    </label>
                    <div className="input-group">
                        <input
                            id="nombre"
                            className={`form-control ${errorForm ? 'is-invalid' : ''}`}
                            value={nombre}
                            onChange={e => setNombre(e.target.value)}
                            maxLength={100}
                            placeholder="Ej.: Frenos, Baterías, Llantas"
                        />
                        <button type="submit" className="btn btn-primary" disabled={guardando}>
                            {guardando ? 'Guardando...' : editando ? 'Actualizar' : 'Agregar'}
                        </button>
                        {editando && (
                            <button type="button" className="btn btn-outline-secondary" onClick={limpiarFormulario}>
                                Cancelar
                            </button>
                        )}
                    </div>
                    {errorForm && <div className="text-danger small mt-2">{errorForm}</div>}
                </form>
            )}

            {cargando && <p className="text-muted">Cargando tipos de repuesto...</p>}
            {error && <div className="alert alert-danger">{error}</div>}
            {errorAccion && <div className="alert alert-danger">{errorAccion}</div>}

            {!cargando && !error && (
                <table className="table table-striped table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Nombre</th>
                            {esAdmin && <th className="text-end">Acciones</th>}
                        </tr>
                    </thead>
                    <tbody>
                        {tipos.length === 0 && (
                            <tr>
                                <td colSpan={esAdmin ? 2 : 1} className="text-center text-muted">
                                    No hay tipos de repuesto registrados.
                                </td>
                            </tr>
                        )}
                        {tipos.map(t => (
                            <tr key={t.idTipoRepuesto} className={editando?.idTipoRepuesto === t.idTipoRepuesto ? 'table-primary' : ''}>
                                <td>{t.nombre}</td>
                                {esAdmin && (
                                    <td className="text-end text-nowrap">
                                        <button className="btn btn-sm btn-outline-primary" onClick={() => empezarEdicion(t)}>
                                            Editar
                                        </button>
                                        <button className="btn btn-sm btn-danger ms-2" onClick={() => eliminar(t)}>
                                            Eliminar
                                        </button>
                                    </td>
                                )}
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}