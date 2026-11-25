import { useEffect, useState } from 'react'
import { mensajeDeError } from '../../api/cliente'
import { fechaHora } from '../../utils/formato'
import { ENTIDADES, ETIQUETA_ACCION, listarAuditoria, type AccionAuditoria, type RegistroAuditoria } from './auditoriaApi'

const COLOR_ACCION: Record<AccionAuditoria, string> = {
    INACTIVACION: 'text-bg-warning',
    REACTIVACION: 'text-bg-success',
    ELIMINACION_FISICA: 'text-bg-danger',
}

/* RF15: quién dio de baja, reactivó o eliminó qué, cuándo y por qué */
export default function AuditoriaPage() {
    const [registros, setRegistros] = useState<RegistroAuditoria[]>([])
    const [entidad, setEntidad] = useState('')
    const [accion, setAccion] = useState('')
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        listarAuditoria(entidad, accion)
            .then(lista => { if (vigente) { setRegistros(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [entidad, accion])

    return (
        <>
            <h1 className="h3 text-primary mb-1">Auditoría</h1>
            <p className="text-muted mb-4">Registro inalterable de bajas, reactivaciones, eliminaciones y anulaciones (RF15).</p>

            <div className="row g-2 mb-3">
                <div className="col-md-3">
                    <select className="form-select" value={entidad} onChange={e => { setCargando(true); setEntidad(e.target.value) }}>
                        <option value="">Todas las entidades</option>
                        {ENTIDADES.map(x => <option key={x} value={x}>{x.replace('_', ' ')}</option>)}
                    </select>
                </div>
                <div className="col-md-3">
                    <select className="form-select" value={accion} onChange={e => { setCargando(true); setAccion(e.target.value) }}>
                        <option value="">Todas las acciones</option>
                        {(Object.keys(ETIQUETA_ACCION) as AccionAuditoria[]).map(a => <option key={a} value={a}>{ETIQUETA_ACCION[a]}</option>)}
                    </select>
                </div>
            </div>

            {cargando && <p className="text-muted">Cargando auditoría...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-striped align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>Fecha</th>
                            <th>Usuario</th>
                            <th>Acción</th>
                            <th>Entidad</th>
                            <th>Registro</th>
                            <th>Motivo</th>
                        </tr>
                    </thead>
                    <tbody>
                        {registros.length === 0 && (
                            <tr><td colSpan={6} className="text-center text-muted">No hay registros con ese filtro.</td></tr>
                        )}
                        {registros.map(r => (
                            <tr key={r.idRegistroAuditoria}>
                                <td className="text-nowrap">{fechaHora(r.fechaHora)}</td>
                                <td>{r.usuario}</td>
                                <td><span className={`badge ${COLOR_ACCION[r.accion]}`}>{ETIQUETA_ACCION[r.accion]}</span></td>
                                <td>{r.entidadAfectada.replace('_', ' ')}</td>
                                <td className="text-nowrap"><strong>{r.idRegistro}</strong></td>
                                <td style={{ whiteSpace: 'pre-wrap' }}>{r.motivo}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}
