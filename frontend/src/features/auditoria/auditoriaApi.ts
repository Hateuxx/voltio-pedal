import { api } from '../../api/cliente'

export type AccionAuditoria = 'INACTIVACION' | 'REACTIVACION' | 'ELIMINACION_FISICA'

export const ETIQUETA_ACCION: Record<AccionAuditoria, string> = {
    INACTIVACION: 'Baja / anulación',
    REACTIVACION: 'Reactivación',
    ELIMINACION_FISICA: 'Eliminación física',
}

export const ENTIDADES = ['PROVEEDOR', 'MERCADERIA', 'TIPO_REPUESTO', 'VENTA'] as const

/* Espejo de AuditoriaResponse.java */
export interface RegistroAuditoria {
    idRegistroAuditoria: number
    fechaHora: string
    usuario: string
    accion: AccionAuditoria
    entidadAfectada: string
    idRegistro: string
    motivo: string
}

export function listarAuditoria(entidad: string, accion: string) {
    const params = new URLSearchParams()
    if (entidad) params.set('entidad', entidad)
    if (accion) params.set('accion', accion)
    const query = params.toString()
    return api<RegistroAuditoria[]>(`/api/auditoria${query ? `?${query}` : ''}`)
}
