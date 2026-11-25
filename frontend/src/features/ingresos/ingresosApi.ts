import { api } from '../../api/cliente'

/* Espejo de IngresoMercaderiaResponse.java */
export interface LineaIngreso {
    idMercaderia: number
    sku: string
    descripcion: string
    cantidad: number
    costoUnitario: number
    subtotal: number
}

export interface Ingreso {
    idIngreso: number
    numeroGuia: string
    fechaHora: string
    idUsuario: number
    observacion: string | null
    activo: boolean
    idProveedor: number
    rucProveedor: string
    razonSocialProveedor: string
    detalles: LineaIngreso[]
    total: number
}

/* Espejo de IngresoRequest.java (null = vacío; el backend valida) */
export interface IngresoRequest {
    idProveedor: number | null
    numeroGuia: string
    observacion: string | null
    detalles: {
        idMercaderia: number | null
        cantidad: number | null
        costoUnitario: number | null
    }[]
}

export function listarIngresos() {
    return api<Ingreso[]>('/api/ingresos')
}

export function registrarIngreso(datos: IngresoRequest) {
    return api<Ingreso>('/api/ingresos', { method: 'POST', body: JSON.stringify(datos) })
}