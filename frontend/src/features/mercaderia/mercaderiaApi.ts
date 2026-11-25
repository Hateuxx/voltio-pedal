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

export function darDeBajaMercaderia(id: number, motivo: string) {
    return api<void>(`${BASE}/${id}`, { method: 'DELETE', body: JSON.stringify({ motivo }) })
}

export function reactivarMercaderia(id: number, motivo: string) {
    return api<void>(`${BASE}/${id}/reactivar`, { method: 'PATCH', body: JSON.stringify({ motivo }) })
}

export function eliminarMercaderia(id: number, motivo: string) {
    return api<void>(`${BASE}/${id}/fisico`, { method: 'DELETE', body: JSON.stringify({ motivo }) })
}

export type TipoMovimiento =
    | 'ENTRADA_PROVEEDOR' | 'DEVOLUCION_PROVEEDOR'
    | 'SALIDA_VENTA' | 'DEVOLUCION_VENTA'
    | 'SALIDA_REPARACION' | 'DEVOLUCION_REPARACION'
    | 'AJUSTE_ENTRADA' | 'AJUSTE_SALIDA'

export const ETIQUETA_MOVIMIENTO: Record<TipoMovimiento, string> = {
    ENTRADA_PROVEEDOR: 'Entrada por compra',
    DEVOLUCION_PROVEEDOR: 'Devolución a proveedor',
    SALIDA_VENTA: 'Salida por venta',
    DEVOLUCION_VENTA: 'Devolución de cliente',
    SALIDA_REPARACION: 'Salida a reparación',
    DEVOLUCION_REPARACION: 'Devolución de reparación',
    AJUSTE_ENTRADA: 'Ajuste (+)',
    AJUSTE_SALIDA: 'Ajuste (−)',
}

/* Espejo de MovimientoResponse.java */
export interface Movimiento {
    idMovimiento: number
    fechaHora: string
    tipo: TipoMovimiento
    entrada: boolean
    cantidad: number
    stockAnterior: number
    stockResultante: number
    usuario: string
    origen: string
    observacion: string | null
}

export function obtenerKardex(id: number) {
    return api<Movimiento[]>(`${BASE}/${id}/kardex`)
}

export function listarAlertasStock() {
    return api<Mercaderia[]>(`${BASE}/alertas-stock`)
}