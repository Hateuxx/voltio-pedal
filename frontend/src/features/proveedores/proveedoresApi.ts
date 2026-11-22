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