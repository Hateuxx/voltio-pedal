import { api } from '../../api/cliente'

export type EstadoConteo = 'ABIERTO' | 'CERRADO'

/* Espejo de ConteoResponse.java */
export interface LineaConteo {
    idMercaderia: number
    sku: string
    descripcion: string
    stockSistema: number
    stockFisico: number
    diferencia: number
    ajustado: boolean
}

export interface Conteo {
    idConteo: number
    fechaInicio: string
    fechaCierre: string | null
    estado: EstadoConteo
    idUsuario: number
    observacion: string | null
    lineas: LineaConteo[]
    productosContados: number
    conDiferencia: number
    exactitud: number | null
}

const BASE = '/api/conteos'

export function listarConteos() {
    return api<Conteo[]>(BASE)
}

export function obtenerConteo(id: number) {
    return api<Conteo>(`${BASE}/${id}`)
}

export function abrirConteo(observacion: string | null) {
    return api<Conteo>(BASE, { method: 'POST', body: JSON.stringify({ observacion }) })
}

export function registrarLineaConteo(id: number, idMercaderia: number, stockFisico: number | null) {
    return api<Conteo>(`${BASE}/${id}/lineas`, {
        method: 'PUT',
        body: JSON.stringify({ idMercaderia, stockFisico }),
    })
}

export function quitarLineaConteo(id: number, idMercaderia: number) {
    return api<Conteo>(`${BASE}/${id}/lineas/${idMercaderia}`, { method: 'DELETE' })
}

export function cerrarConteo(id: number, motivo: string) {
    return api<Conteo>(`${BASE}/${id}/cerrar`, { method: 'POST', body: JSON.stringify({ motivo }) })
}