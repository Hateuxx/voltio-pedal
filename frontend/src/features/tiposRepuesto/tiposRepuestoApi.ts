import { api } from '../../api/cliente'

/* Espejo de TipoRepuestoResponse.java */
export interface TipoRepuesto {
    idTipoRepuesto: number
    nombre: string
}

export function listarTiposRepuesto() {
    return api<TipoRepuesto[]>('/api/tipos-repuesto')
}

export function crearTipoRepuesto(nombre: string) {
    return api<TipoRepuesto>('/api/tipos-repuesto', {
        method: 'POST',
        body: JSON.stringify({ nombre }),
    })
}

export function actualizarTipoRepuesto(id: number, nombre: string) {
    return api<TipoRepuesto>(`/api/tipos-repuesto/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ nombre }),
    })
}

export function eliminarTipoRepuesto(id: number) {
    return api<void>(`/api/tipos-repuesto/${id}`, { method: 'DELETE' })
}