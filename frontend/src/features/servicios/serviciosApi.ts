import { api } from '../../api/cliente'

export type EstadoServicio = 'INGRESADO' | 'EN_REPARACION' | 'ESPERANDO_REPUESTO' | 'LISTO_PARA_ENTREGA' | 'ENTREGADO'

export const ESTADOS: EstadoServicio[] = ['INGRESADO', 'EN_REPARACION', 'ESPERANDO_REPUESTO', 'LISTO_PARA_ENTREGA', 'ENTREGADO']

export const ETIQUETA_ESTADO: Record<EstadoServicio, string> = {
    INGRESADO: 'Ingresado',
    EN_REPARACION: 'En reparación',
    ESPERANDO_REPUESTO: 'Esperando repuesto',
    LISTO_PARA_ENTREGA: 'Listo para entrega',
    ENTREGADO: 'Entregado',
}

export const COLOR_ESTADO: Record<EstadoServicio, string> = {
    INGRESADO: 'text-bg-secondary',
    EN_REPARACION: 'text-bg-primary',
    ESPERANDO_REPUESTO: 'text-bg-warning',
    LISTO_PARA_ENTREGA: 'text-bg-info',
    ENTREGADO: 'text-bg-success',
}

/* Espejo de ServicioResponse.java */
export interface RepuestoServicio {
    idDetalle: number
    idRepuesto: number
    sku: string
    descripcion: string
    cantidad: number
    precioUnitario: number
    subtotal: number
    fechaHora: string
}

export interface Servicio {
    idServicio: number
    fechaIngreso: string
    fechaEntrega: string | null
    numeroSerie: string
    fallaReportada: string
    diagnostico: string | null
    estado: EstadoServicio
    fechaCompra: string | null
    mesesGarantia: number | null
    garantiaHasta: string | null
    cubiertoPorGarantia: boolean
    idCliente: number
    cliente: string
    idScooter: number
    scooter: string
    idTecnico: number | null
    tecnico: string | null
    repuestos: RepuestoServicio[]
    totalRepuestos: number
    totalACobrar: number
}

export interface Tecnico {
    idUsuario: number
    nombre: string
}

export interface RegistrarServicio {
    idCliente: number | null
    idScooter: number | null
    numeroSerie: string
    fallaReportada: string
    fechaCompra: string | null
    mesesGarantia: number | null
    idTecnico: number | null
}

const BASE = '/api/servicios'

export function listarServicios(estado: string) {
    return api<Servicio[]>(estado ? `${BASE}?estado=${estado}` : BASE)
}

export function obtenerServicio(id: number) {
    return api<Servicio>(`${BASE}/${id}`)
}

export function listarTecnicos() {
    return api<Tecnico[]>(`${BASE}/tecnicos`)
}

export function registrarServicio(datos: RegistrarServicio) {
    return api<Servicio>(BASE, { method: 'POST', body: JSON.stringify(datos) })
}

export function actualizarServicio(id: number, datos: { estado: EstadoServicio; diagnostico: string | null; idTecnico: number | null }) {
    return api<Servicio>(`${BASE}/${id}`, { method: 'PATCH', body: JSON.stringify(datos) })
}

export function consumirRepuesto(id: number, idRepuesto: number | null, cantidad: number | null) {
    return api<Servicio>(`${BASE}/${id}/repuestos`, { method: 'POST', body: JSON.stringify({ idRepuesto, cantidad }) })
}

export function devolverRepuesto(id: number, idDetalle: number) {
    return api<Servicio>(`${BASE}/${id}/repuestos/${idDetalle}`, { method: 'DELETE' })
}
