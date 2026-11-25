import { api } from '../../api/cliente'

export type TipoComprobante = 'BOLETA' | 'FACTURA'

/* Espejo de VentaResponse.java */
export interface LineaVenta {
    idMercaderia: number
    sku: string
    descripcion: string
    cantidad: number
    precioUnitario: number
    subtotal: number
    numeroSerie: string | null
}

export interface Venta {
    idVenta: number
    tipoComprobante: TipoComprobante
    numeroComprobante: string
    fechaHora: string
    total: number
    activo: boolean
    idCliente: number | null
    cliente: string | null
    idUsuario: number
    vendedor: string
    detalles: LineaVenta[]
}

/* Espejo de VentaRequest.java: el precio NO se envía, lo pone el servidor */
export interface VentaRequest {
    idCliente: number | null
    tipoComprobante: TipoComprobante
    detalles: { idMercaderia: number | null; cantidad: number | null; numeroSerie: string | null }[]
}

export function listarVentas(desde: string, hasta: string) {
    return api<Venta[]>(`/api/ventas?desde=${desde}&hasta=${hasta}`)
}

export function registrarVenta(datos: VentaRequest) {
    return api<Venta>('/api/ventas', { method: 'POST', body: JSON.stringify(datos) })
}

export function anularVenta(id: number, motivo: string) {
    return api<Venta>(`/api/ventas/${id}/anular`, { method: 'POST', body: JSON.stringify({ motivo }) })
}
