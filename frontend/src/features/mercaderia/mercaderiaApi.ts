import { api } from '../../api/cliente'

export type TipoMercaderia = 'SCOOTER' | 'REPUESTO' | 'ACCESORIO'

export const ETIQUETA_TIPO: Record<TipoMercaderia, string> = {
    SCOOTER: 'Scooter',
    REPUESTO: 'Repuesto',
    ACCESORIO: 'Accesorio',
}

/* Espejo de MercaderiaResponse.java (los campos de otros tipos llegan en null) */
export interface Mercaderia {
    idMercaderia: number
    tipo: TipoMercaderia
    sku: string
    descripcion: string
    stock: number
    stockMinimo: number
    precioCosto: number
    precioVenta: number
    fechaIngreso: string
    activo: boolean
    idProveedor: number
    proveedor: string
    autonomiaKm: number | null
    potenciaWatts: number | null
    color: string | null
    mesesGarantia: number | null
    idTipoRepuesto: number | null
    tipoRepuesto: string | null
    modelosCompatibles: string | null
    talla: string | null
}

/* Lo que se ENVÍA (espejo de MercaderiaRequest.java). null = "no aplica" o "vacío": el backend valida */
export interface MercaderiaRequest {
    tipo: TipoMercaderia
    sku: string
    descripcion: string
    stockMinimo: number | null
    precioCosto: number | null
    precioVenta: number | null
    idProveedor: number | null
    autonomiaKm: number | null
    potenciaWatts: number | null
    color: string | null
    mesesGarantia: number | null
    idTipoRepuesto: number | null
    modelosCompatibles: string | null
    talla: string | null
}

const BASE = '/api/mercaderias'

export function listarMercaderias() {
    return api<Mercaderia[]>(BASE)
}

export function listarMercaderiasInactivas() {
    return api<Mercaderia[]>(`${BASE}/inactivos`)
}

export function obtenerMercaderia(id: number) {
    return api<Mercaderia>(`${BASE}/${id}`)
}

export function crearMercaderia(datos: MercaderiaRequest) {
    return api<Mercaderia>(BASE, { method: 'POST', body: JSON.stringify(datos) })
}

export function actualizarMercaderia(id: number, datos: MercaderiaRequest) {
    return api<Mercaderia>(`${BASE}/${id}`, { method: 'PUT', body: JSON.stringify(datos) })
}

export function darDeBajaMercaderia(id: number) {
    return api<void>(`${BASE}/${id}`, { method: 'DELETE' })
}

export function reactivarMercaderia(id: number) {
    return api<void>(`${BASE}/${id}/reactivar`, { method: 'PATCH' })
}

export function eliminarMercaderia(id: number) {
    return api<void>(`${BASE}/${id}/fisico`, { method: 'DELETE' })
}