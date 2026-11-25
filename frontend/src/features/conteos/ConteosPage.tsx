import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { mensajeDeError } from '../../api/cliente'
import BadgeExactitud from './exactitud'
import { abrirConteo, listarConteos, type Conteo } from './conteosApi'

const fecha = (iso: string | null) =>
    iso ? new Date(iso).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' }) : '—'

export default function ConteosPage() {
    const navigate = useNavigate()
    const [conteos, setConteos] = useState<Conteo[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState<string | null>(null)
    const [observacion, setObservacion] = useState('')
    const [abriendo, setAbriendo] = useState(false)
    const [errorAbrir, setErrorAbrir] = useState<string | null>(null)

    useEffect(() => {
        let vigente = true
        listarConteos()
            .then(lista => { if (vigente) setConteos(lista) })
            .catch(err => { if (vigente) setError(mensajeDeError(err)) })
            .finally(() => { if (vigente) setCargando(false) })
        return () => { vigente = false }
    }, [])

    const abierto = conteos.find(c => c.estado === 'ABIERTO')

    async function iniciar(e: FormEvent) {
        e.preventDefault()
        setErrorAbrir(null)
        setAbriendo(true)
        try {
            const nuevo = await abrirConteo(observacion.trim() || null)
            navigate(`/conteos/${nuevo.idConteo}`)
        } catch (err) {
            setErrorAbrir(mensajeDeError(err))
        } finally {
            setAbriendo(false)
        }
    }

    return (
        <>
            <h1 className="h3 text-primary mb-4">Conteos de inventario</h1>

            {!cargando && !error && (abierto ? (
                <div className="alert alert-info d-flex justify-content-between align-items-center">
                    <span>Hay un conteo en curso: <strong>#{abierto.idConteo}</strong>, iniciado el {fecha(abierto.fechaInicio)}.</span>
                    <Link to={`/conteos/${abierto.idConteo}`} className="btn btn-primary btn-sm">Continuar conteo</Link>
                </div>
            ) : (
                <form onSubmit={iniciar} className="card card-body mb-4">
                    <label htmlFor="observacion" className="form-label">Nuevo conteo físico</label>
                    <div className="input-group">
                        <input id="observacion" className="form-control" maxLength={255} value={observacion}
                            placeholder="Observación (opcional). Ej.: Conteo de fin de mes"
                            onChange={e => setObservacion(e.target.value)} />
                        <button type="submit" className="btn btn-success" disabled={abriendo}>
                            {abriendo ? 'Iniciando...' : 'Iniciar conteo'}
                        </button>
                    </div>
                    {errorAbrir && <div className="text-danger small mt-2">{errorAbrir}</div>}
                </form>
            ))}

            {cargando && <p className="text-muted">Cargando conteos...</p>}
            {error && <div className="alert alert-danger">{error}</div>}

            {!cargando && !error && (
                <table className="table table-hover align-middle">
                    <thead className="table-light">
                        <tr>
                            <th>#</th>
                            <th>Inicio</th>
                            <th>Cierre</th>
                            <th>Estado</th>
                            <th className="text-center">Contados</th>
                            <th className="text-center">Con diferencia</th>
                            <th className="text-center">Exactitud</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        {conteos.length === 0 && (
                            <tr><td colSpan={8} className="text-center text-muted">Todavía no se ha realizado ningún conteo.</td></tr>
                        )}
                        {conteos.map(c => (
                            <tr key={c.idConteo}>
                                <td>{c.idConteo}</td>
                                <td className="text-nowrap">{fecha(c.fechaInicio)}</td>
                                <td className="text-nowrap">{fecha(c.fechaCierre)}</td>
                                <td>
                                    <span className={`badge ${c.estado === 'ABIERTO' ? 'text-bg-primary' : 'text-bg-secondary'}`}>{c.estado}</span>
                                </td>
                                <td className="text-center">{c.productosContados}</td>
                                <td className="text-center">{c.conDiferencia}</td>
                                <td className="text-center"><BadgeExactitud valor={c.exactitud} /></td>
                                <td className="text-end">
                                    <Link to={`/conteos/${c.idConteo}`} className="btn btn-sm btn-outline-primary">
                                        {c.estado === 'ABIERTO' ? 'Continuar' : 'Ver'}
                                    </Link>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </>
    )
}