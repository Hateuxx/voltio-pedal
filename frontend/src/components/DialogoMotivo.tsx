import { useState, type FormEvent } from 'react'
import { mensajeDeError } from '../api/cliente'

/* Lo que una pantalla necesita para pedir un motivo antes de una acción (RF15) */
export interface AccionConMotivo {
    titulo: string
    mensaje: string
    textoBoton: string
    peligro?: boolean
    ejecutar: (motivo: string) => Promise<void>
}

/* Modal de Bootstrap controlado por React (sin el JS de Bootstrap) */
export default function DialogoMotivo({ accion, onCerrar }: { accion: AccionConMotivo; onCerrar: () => void }) {
    const [motivo, setMotivo] = useState('')
    const [enviando, setEnviando] = useState(false)
    const [error, setError] = useState<string | null>(null)

    async function confirmar(e: FormEvent) {
        e.preventDefault()
        setError(null)
        setEnviando(true)
        try {
            await accion.ejecutar(motivo.trim())
            onCerrar()
        } catch (err) {
            setError(mensajeDeError(err))
            setEnviando(false)
        }
    }

    return (
        <>
            <div className="modal d-block" tabIndex={-1} role="dialog" aria-modal="true">
                <div className="modal-dialog modal-dialog-centered">
                    <form className="modal-content" onSubmit={confirmar}>
                        <div className="modal-header">
                            <h5 className="modal-title">{accion.titulo}</h5>
                            <button type="button" className="btn-close" aria-label="Cerrar" onClick={onCerrar} disabled={enviando} />
                        </div>
                        <div className="modal-body">
                            <p>{accion.mensaje}</p>
                            <label htmlFor="motivo" className="form-label">Motivo (queda registrado en la auditoría) *</label>
                            <textarea id="motivo" className="form-control" rows={3} maxLength={500} autoFocus
                                value={motivo} onChange={e => setMotivo(e.target.value)} />
                            {error && <div className="alert alert-danger mt-3 mb-0">{error}</div>}
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-outline-secondary" onClick={onCerrar} disabled={enviando}>Cancelar</button>
                            <button type="submit" className={`btn ${accion.peligro ? 'btn-danger' : 'btn-primary'}`}
                                disabled={enviando || motivo.trim() === ''}>
                                {enviando ? 'Procesando...' : accion.textoBoton}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
            <div className="modal-backdrop show" />
        </>
    )
}
