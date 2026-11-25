import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import { fechaHora } from '../../utils/formato'
import { obtenerUsuario } from '../auth/sesion'
import { COLOR_ESTADO, ESTADOS, ETIQUETA_ESTADO, listarServicios, type Servicio } from './serviciosApi'

export default function ServiciosPage() {
    const [servicios, setServicios] = useState<Servicio[]>([])
    const [estado, setEstado] = useState('')
    const [soloMios, setSoloMios] = useState(false)
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const usuario = obtenerUsuario()
    const esTecnico = usuario?.rol === 'TECNICO'

    useEffect(() => {
        let vigente = true
        listarServicios(estado)
            .then(lista => { if (vigente) { setServicios(lista); setError(null) } })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [estado])

    const visibles = soloMios && usuario ? servicios.filter(s => s.tecnico?.startsWith(usuario.nombres)) : servicios

    return (
        <>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h1 className="h3 text-primary mb-0">Servicio técnico</h1>
                {!esTecnico && <Link to="/servicios/nuevo" className="btn btn-success">Recibir scooter</Link>}
            </div>

            <div className="row g-2 mb-3 align-items-center">
                <div className="col-md-4">
                    <select className="form-select" value={estado} onChange={e => { setCargando(true); setEstado(e.target.value) }}>
                        <option value="">Todos los estados</option>
                        {ESTADOS.map(e => <option key={e} value={e}>{ETIQUETA_ESTADO[e]}</option>)}
                    </select>
                </div>
                {esTecnico && (
                    <div className="col-md-4">
                        <div className="form-check">
                            <input id="soloMios" type="checkbox" className="form-check-input" checked={soloMios} onChange={e => setSoloMios(e.target.checked)} />
                            <label htmlFor="soloMios" className="form-check-label">Solo los asignados a mí</label>
                        </div>
                    </div>
                )}
            </div>

            {cargando && <p className="text-muted">Cargando servicios...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>#</th>
                            <th>Ingreso</th>
                            <th>Cliente</th>
                            <th>Scooter / serie</th>
                            <th>Técnico</th>
                            <th>Estado</th>
                            <th>Garantía</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {visibles.length === 0 && (
                            <tr><td colSpan={8} className="text-center text-muted">No hay órdenes de servicio.</td></tr>
                        )}
                        {visibles.map(s => (
                            <tr key={s.idServicio}>
                                <td>{s.idServicio}</td>
                                <td className="text-nowrap">{fechaHora(s.fechaIngreso)}</td>
                                <td>{s.cliente}</td>
                                <td>{s.scooter}<div className="small text-muted">Serie {s.numeroSerie}</div></td>
                                <td>{s.tecnico ?? <span className="text-muted">Sin asignar</span>}</td>
                                <td><span className={`badge ${COLOR_ESTADO[s.estado]}`}>{ETIQUETA_ESTADO[s.estado]}</span></td>
                                <td>{s.cubiertoPorGarantia ? <span className="badge text-bg-success">Cubierto</span> : <span className="text-muted small">No</span>}</td>
                                <td className="text-end"><Link to={`/servicios/${s.idServicio}`} className="btn btn-sm btn-outline-primary">Ver</Link></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}
