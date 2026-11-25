import { api } from '../../api/cliente'

/* Espejos de ReporteResponses.java */
export interface ReporteInventario {
    generado: string
    productos: number
    unidades: number
    bajoMinimo: number
    valorCosto: number
    valorVenta: number
    lineas: {
        sku: string; descripcion: string; tipo: string; proveedor: string
        stock: number; stockMinimo: number; costoUnitario: number; precioVenta: number
        valorCosto: number; valorVenta: number; bajoMinimo: boolean
    }[]
}

export interface ReporteVentas {
    desde: string
    hasta: string
    cantidadVentas: number
    ventasAnuladas: number
    totalVendido: number
    costoTotal: number
    utilidad: number
    margen: number
    productos: { sku: string; descripcion: string; cantidad: number; importe: number; utilidad: number }[]
    porDia: { fecha: string; ventas: number; total: number }[]
}

export interface MovimientoReporte {
    fechaHora: string; tipo: string; entrada: boolean; sku: string; descripcion: string
    cantidad: number; stockAnterior: number; stockResultante: number; usuario: string; origen: string; observacion: string | null
}

export interface ReporteServicios {
    desde: string
    hasta: string
    total: number
    entregados: number
    cubiertosPorGarantia: number
    porEstado: Record<string, number>
    porTecnico: { tecnico: string; asignados: number; entregados: number }[]
    repuestos: { sku: string; descripcion: string; cantidad: number; importe: number }[]
    importeRepuestos: number
    importeCobrado: number
}

const rango = (desde: string, hasta: string) => `desde=${desde}&hasta=${hasta}`

export const reporteInventario = () => api<ReporteInventario>('/api/reportes/inventario')
export const reporteVentas = (desde: string, hasta: string) => api<ReporteVentas>(`/api/reportes/ventas?${rango(desde, hasta)}`)
export const reporteMovimientos = (desde: string, hasta: string, tipo: string) =>
    api<MovimientoReporte[]>(`/api/reportes/movimientos?${rango(desde, hasta)}${tipo ? `&tipo=${tipo}` : ''}`)
export const reporteServicios = (desde: string, hasta: string) => api<ReporteServicios>(`/api/reportes/servicios?${rango(desde, hasta)}`)
