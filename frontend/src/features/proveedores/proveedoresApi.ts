import { api } from '../../api/cliente'

/* Reemplaza 2 valores exactos de ProveedorRequest.java */
export type FrecuenciaEntrega = 'SEMANAL' | 'QUINCENAL'

/* Espejo de ProveedorResponse.java */
export interface Proveedor {
    idProveedor: number
    ruc: string
    razonSocial: string
    contacto: string | null
    telefono: string | null
    rubro: string | null
    frecuenciaEntrega: FrecuenciaEntrega | null
    activo: boolean
}

export function listarProveedores() {
    return api<Proveedor[]>('/api/proveedores')
}

/* Lo que el frontend ENVIA (espejo de ProveedorRequest.java) */
export interface ProveedorRequest {
    ruc: string
    razonSocial: string
    contacto: string | null
    telefono: string | null
    rubro: string | null
    frecuenciaEntrega: FrecuenciaEntrega
}

export function crearProveedor(datos: ProveedorRequest) {
    return api<Proveedor>('/api/proveedores', {
        method: 'POST',
        body: JSON.stringify(datos),
    })
}

export function obtenerProveedor(id: number) {
    return api<Proveedor>(`/api/proveedores/${id}`)
}

export function actualizarProveedor(id: number, datos: ProveedorRequest) {
    return api<Proveedor>(`/api/proveedores/${id}`, {
        method: 'PUT',
        body: JSON.stringify(datos),
    })
}

export function darDeBajaProveedor(id: number) {
    return api<void>(`/api/proveedores/${id}`, { method: 'DELETE' })
}

export function listarProveedoresInactivos() {
    return api<Proveedor[]>('/api/proveedores/inactivos')
}

export function reactivarProveedor(id: number) {
    return api<void>(`/api/proveedores/${id}/reactivar`, { method: 'PATCH' })
}

export function eliminarProveedor(id: number) {
    return api<void>(`/api/proveedores/${id}/fisico`, { method: 'DELETE' })
}