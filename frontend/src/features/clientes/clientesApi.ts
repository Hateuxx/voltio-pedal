import { api } from '../../api/cliente'

export type TipoDocumentoCliente = 'DNI' | 'CE' | 'PASAPORTE' | 'RUC'

/* Espejo de ClienteResponse.java */
export interface Cliente {
    idCliente: number
    tipoDocumento: TipoDocumentoCliente
    numeroDocumento: string
    nombres: string
    apellidos: string | null
    telefono: string | null
}

export interface ClienteRequest {
    tipoDocumento: TipoDocumentoCliente
    numeroDocumento: string
    nombres: string
    apellidos: string | null
    telefono: string | null
}

export function nombreCompleto(c: Cliente) {
    return c.apellidos ? `${c.nombres} ${c.apellidos}` : c.nombres
}

export function listarClientes() {
    return api<Cliente[]>('/api/clientes')
}

export function obtenerCliente(id: number) {
    return api<Cliente>(`/api/clientes/${id}`)
}

export function crearCliente(datos: ClienteRequest) {
    return api<Cliente>('/api/clientes', { method: 'POST', body: JSON.stringify(datos) })
}

export function actualizarCliente(id: number, datos: ClienteRequest) {
    return api<Cliente>(`/api/clientes/${id}`, { method: 'PUT', body: JSON.stringify(datos) })
}

export function eliminarCliente(id: number) {
    return api<void>(`/api/clientes/${id}`, { method: 'DELETE' })
}
